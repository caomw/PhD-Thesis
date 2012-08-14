package org.imagesci.robopaint.segmentation;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.imagesci.mogac.MACWE3D;
import org.imagesci.mogac.MOGAC3D.FrameUpdateListener;
import org.imagesci.springls.SpringlsCommon3D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class RoboSegment extends MACWE3D {
	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC3D#step()
	 */
	@Override
	public boolean step() {
		final CLKernel pressureSpeedKernel = kernelMap
				.get("robosegSpeedKernel");
		final CLKernel maxImageValue = kernelMap.get("maxImageValue");
		final CLKernel maxTimeStep = kernelMap.get("maxTimeStep");
		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");

		int global_size = roundToWorkgroupPower(activeListSize);
		long startTime = System.nanoTime();
		if (time % intensityEstimationInterval == 0) {
			updateAveragesAutoIntensity();
		}
		pressureSpeedKernel
				.putArgs(activeListBuffer, pressureBuffer,
						oldDistanceFieldBuffer, oldImageLabelBuffer,
						deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
						forceIndexesBuffer, averages, pressureWeightsBuffer,
						curvatureWeightsBuffer, objectStatusBuffer)
				.putArg(activeListSize).rewind();
		queue.put1DRangeKernel(pressureSpeedKernel, 0, global_size,
				WORKGROUP_SIZE);
		if (topologyPreservation) {
			if (!clampSpeed) {
				// Find max
				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.putArg(activeListSize).rewind();
				queue.put1DRangeKernel(
						maxImageValue,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE),
								WORKGROUP_SIZE / 8), WORKGROUP_SIZE / 8);
				maxTimeStep.putArg(maxTmpBuffer)
						.putArg(1 + (activeListSize / STRIDE)).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 1, 1);
				for (int nn = 0; nn < 8; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldDistanceFieldBuffer,
									oldImageLabelBuffer, deltaLevelSetBuffer,
									idBuffer, distanceFieldBuffer,
									imageLabelBuffer, maxTmpBuffer,
									topologyRuleBuffer).putArg(activeListSize)
							.putArg(nn).rewind();

					queue.put1DRangeKernel(applyForces, 0, global_size,
							WORKGROUP_SIZE);
				}
			} else {
				for (int nn = 0; nn < 8; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldDistanceFieldBuffer,
									oldImageLabelBuffer, deltaLevelSetBuffer,
									idBuffer, distanceFieldBuffer,
									imageLabelBuffer, topologyRuleBuffer)
							.putArg(0.5f).putArg(activeListSize).putArg(nn)
							.rewind();
					queue.put1DRangeKernel(applyForces, 0, global_size,
							WORKGROUP_SIZE);
				}
			}
		} else {
			if (!clampSpeed) {
				// Find max

				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.putArg(activeListSize).rewind();
				queue.put1DRangeKernel(
						maxImageValue,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE),
								WORKGROUP_SIZE / 8), WORKGROUP_SIZE / 8);
				maxTimeStep.putArg(maxTmpBuffer)
						.putArg(1 + (activeListSize / STRIDE)).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 1, 1);
				applyForces
						.putArgs(activeListBuffer, oldDistanceFieldBuffer,
								oldImageLabelBuffer, deltaLevelSetBuffer,
								idBuffer, distanceFieldBuffer,
								imageLabelBuffer, maxTmpBuffer)
						.putArg(activeListSize).rewind();
			} else {
				applyForces
						.putArgs(activeListBuffer, oldDistanceFieldBuffer,
								oldImageLabelBuffer, deltaLevelSetBuffer,
								idBuffer, distanceFieldBuffer, imageLabelBuffer)
						.putArg(0.5f).putArg(activeListSize).rewind();
			}
			queue.put1DRangeKernel(applyForces, 0, global_size, WORKGROUP_SIZE);
		}
		for (int i = 1; i <= MAX_LAYERS; i++) {
			extendDistanceField
					.putArgs(activeListBuffer, oldDistanceFieldBuffer,
							distanceFieldBuffer, imageLabelBuffer).putArg(i)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0, global_size,
					WORKGROUP_SIZE);
		}

		final CLKernel plugLevelSet = kernelMap.get("plugLevelSet");
		plugLevelSet
				.putArgs(activeListBuffer, distanceFieldBuffer,
						imageLabelBuffer).putArg(activeListSize).rewind();
		queue.put1DRangeKernel(plugLevelSet, 0, global_size, WORKGROUP_SIZE);
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");

		if (useAdaptiveActiveSet) {
			copyBuffers
					.putArgs(activeListBuffer, oldDistanceFieldBuffer,
							oldImageLabelBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(activeListSize).rewind();
			queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
			final CLKernel rememberImageLabels = kernelMap
					.get("rememberImageLabels");
			final CLKernel diffImageLabels = kernelMap.get("diffImageLabels");
			if ((time) % sampling_interval == 0) {
				rememberImageLabels.putArgs(imageLabelBuffer, historyBuffer)
						.rewind();
				queue.put1DRangeKernel(rememberImageLabels, 0,
						roundToWorkgroupPower(rows * cols * slices),
						WORKGROUP_SIZE);
			}
			if ((time) % sampling_interval == sampling_interval - 1) {
				diffImageLabels.putArgs(imageLabelBuffer, historyBuffer)
						.rewind();
				queue.put1DRangeKernel(diffImageLabels, 0,
						roundToWorkgroupPower(rows * cols * slices),
						WORKGROUP_SIZE);
				final CLKernel dilateLabels = kernelMap.get("dilateLabels");
				for (int cycle = 0; cycle < 4; cycle++) {

					for (int kk = 0; kk < 8; kk++) {
						dilateLabels.putArgs(activeListBuffer, historyBuffer)
								.putArg(activeListSize).putArg(kk).rewind();

						queue.put1DRangeKernel(dilateLabels, 0, global_size,
								WORKGROUP_SIZE);
					}
				}

			}
		} else {
			copyBuffers
					.putArgs(activeListBuffer, oldDistanceFieldBuffer,
							oldImageLabelBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(activeListSize).rewind();
			queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
		}
		queue.finish();
		dirty = true;
		stepElapsedTime += (System.nanoTime() - startTime);
		startTime = System.nanoTime();
		deleteElements();
		startTime = System.nanoTime();
		addElements();
		if (activeListSize == 0) {
			return false;
		}
		if (time % getResamplingRate() == 0) {
			queue.finish();

			long tmp = System.nanoTime();
			for (FrameUpdateListener updater : listeners) {
				updater.frameUpdate(time, 1E9 * getResamplingRate()
						/ (tmp - lastStartTime));
			}
			lastStartTime = tmp;
			incrementCompletedUnits();
		}
		time++;
		return true;
	}

	protected CLBuffer<FloatBuffer> pressureWeightsBuffer;
	protected CLBuffer<FloatBuffer> curvatureWeightsBuffer;
	protected CLBuffer<ByteBuffer> objectStatusBuffer;

	public void setObjectStatus(int index, byte status) {
		objectStatusBuffer.getBuffer().put(index, status).rewind();
		queue.putWriteBuffer(objectStatusBuffer, true);
	}

	public void setPressureWeight(int index, float weight) {
		pressureWeightsBuffer.getBuffer().put(index, weight).rewind();
		queue.putWriteBuffer(pressureWeightsBuffer, true);
	}

	public void setTargetIntensity(int index, float intensity) {
		averages.getBuffer().put(index, intensity).rewind();
		queue.putWriteBuffer(averages, true);
	}

	public void setCurvatureWeight(int index, float weight) {
		curvatureWeightsBuffer.getBuffer().put(index, weight).rewind();
		queue.putWriteBuffer(curvatureWeightsBuffer, true);
	}

	public void updateAveragesAutoIntensity() {

		final CLKernel regionAverage = kernelMap.get("regionAverageAutoUpdate");
		final CLKernel sumAverages = kernelMap.get("sumAveragesAutoUpdate");
		regionAverage.putArgs(imageLabelBuffer, distanceFieldBuffer,
				pressureBuffer, labelMaskBuffer, averages, areas, stddev,
				objectStatusBuffer).rewind();
		queue.put1DRangeKernel(regionAverage, 0, roundToWorkgroupPower(slices),
				WORKGROUP_SIZE);
		queue.finish();
		sumAverages.putArgs(averages, areas, stddev, objectStatusBuffer)
				.rewind();
		queue.put1DRangeKernel(sumAverages, 0,
				roundToWorkgroupPower(numLabels), WORKGROUP_SIZE);
		queue.finish();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC3D#init(edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat, edu.jhu.ece.iacl.jist.structures.image.ImageDataInt, boolean)
	 */
	@Override
	public void init(ImageDataFloat unsignedLevelSetBuffer,
			ImageDataInt labelBuffer, boolean containsOverlaps)
			throws IOException {
		super.init(unsignedLevelSetBuffer, labelBuffer, containsOverlaps);
		pressureWeightsBuffer = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		curvatureWeightsBuffer = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		objectStatusBuffer = context.createByteBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		for (int i = 0; i < numLabels; i++) {
			pressureWeightsBuffer.getBuffer().put(pressureWeight);
			curvatureWeightsBuffer.getBuffer().put(curvatureWeight);
			objectStatusBuffer.getBuffer().put(
					(byte) (((intensityEstimation) ? 8 : 0) | (2)));
		}
		pressureWeightsBuffer.getBuffer().rewind();
		curvatureWeightsBuffer.getBuffer().rewind();
		objectStatusBuffer.getBuffer().rewind();
		queue.putWriteBuffer(pressureWeightsBuffer, true)
				.putWriteBuffer(curvatureWeightsBuffer, true)
				.putWriteBuffer(objectStatusBuffer, true);
	}

	public void setImageSegmentation(ImageDataInt labelImage,
			ImageDataFloat unsignedImage) {
		super.setImageSegmentation(labelImage, unsignedImage);
		if (image != null) {
			pressureBuffer = context.createFloatBuffer(rows * cols * slices,
					READ_ONLY);
			FloatBuffer buff = pressureBuffer.getBuffer();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						buff.put(image.getFloat(i, j, k));
					}
				}
			}
			buff.rewind();
			queue.putWriteBuffer(pressureBuffer, true);
		}
		targetPressure = Float.NaN;
		averages = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		areas = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		stddev = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		pressureWeightsBuffer = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		curvatureWeightsBuffer = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		objectStatusBuffer = context.createByteBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		for (int i = 0; i < numLabels; i++) {
			pressureWeightsBuffer.getBuffer().put(pressureWeight);
			curvatureWeightsBuffer.getBuffer().put(curvatureWeight);
			objectStatusBuffer.getBuffer().put(
					(byte) (((intensityEstimation) ? 8 : 0) | (2)));
		}
		pressureWeightsBuffer.getBuffer().rewind();
		curvatureWeightsBuffer.getBuffer().rewind();
		objectStatusBuffer.getBuffer().rewind();
		queue.putWriteBuffer(pressureWeightsBuffer, true)
				.putWriteBuffer(curvatureWeightsBuffer, true)
				.putWriteBuffer(objectStatusBuffer, true);
		updateAverages();
	}
}
