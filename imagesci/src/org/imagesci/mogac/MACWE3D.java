/**
 * ImageSci Toolkit
 *
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 * @author Blake Lucas (blake@cs.jhu.edu)
 */
package org.imagesci.mogac;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;

import org.imagesci.springls.SpringlsCommon3D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MACWE3D is an implementation of Multi-object Active Contours
 * Without Edges 3D
 */
public class MACWE3D extends WEMOGAC3D {

	/** The areas. */
	protected CLBuffer<FloatBuffer> areas;

	/** The averages. */
	protected CLBuffer<FloatBuffer> averages;

	/** The intensity estimation. */
	protected boolean intensityEstimation = false;

	/** The intensity estimation interval. */
	protected int intensityEstimationInterval = 10;

	/** The std. dev. buffer */
	protected CLBuffer<FloatBuffer> stddev;

	/**
	 * Instantiates a new Multi-object Active Contours Without Edges 3D
	 * 
	 * @param refImage
	 *            the reference image
	 * @param context
	 *            the context
	 * @param queue
	 *            the queue
	 */
	public MACWE3D(ImageData refImage, CLContext context, CLCommandQueue queue) {
		super(refImage, context, queue);
	}

	/**
	 * Instantiates a new Multi-object Active Contours Without Edges 3D
	 * 
	 * @param refImage
	 *            the reference image
	 * @param type
	 *            the type
	 */
	public MACWE3D(ImageData refImage, CLDevice.Type type) {
		super(refImage, type);
	}

	/**
	 * Solve.
	 * 
	 * @param unsignedImage
	 *            the unsigned image
	 * @param labelImage
	 *            the label image
	 * @param intensityPriors
	 *            the intensity priors
	 * @param containsOverlaps
	 *            the contains overlaps
	 * @return the image data float
	 */
	public ImageDataFloat solve(ImageDataFloat unsignedImage,
			ImageDataInt labelImage, double[] intensityPriors,
			boolean containsOverlaps) {
		try {
			init(unsignedImage, labelImage, intensityPriors, containsOverlaps);
			queue.finish();

			time = 0;
			long startTime = lastStartTime = System.nanoTime();

			for (int outerIter = 0; outerIter < maxIterations; outerIter++) {
				if (!step()) {
					break;
				}
			}
			queue.finish();
			long endTime = System.nanoTime();
			this.elapsedTime = endTime - startTime;
			System.out.printf(
					"Elapsed Time: %8.4f sec\nFrame Rate: %6.2f fps\n",
					1E-9 * (endTime - startTime), 1E9 * maxIterations
							/ (endTime - startTime));
			finish();
			context.release();
			markCompleted();
			return distFieldImage;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Initializes the OpenCL device.
	 * 
	 * @param unsignedLevelSetBuffer
	 *            the unsigned level set buffer
	 * @param labelBuffer
	 *            the label buffer
	 * @param intensityPriors
	 *            the intensity priors
	 * @param containsOverlaps
	 *            the contains overlaps
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init(ImageDataFloat unsignedLevelSetBuffer,
			ImageDataInt labelBuffer, double[] intensityPriors,
			boolean containsOverlaps) throws IOException {
		targetPressure = Float.NaN;
		super.init(unsignedLevelSetBuffer, labelBuffer, containsOverlaps);
		averages = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		areas = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		stddev = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		setAverages(intensityPriors);
	}

	/**
	 * Sets the averages.
	 * 
	 * @param data
	 *            the new averages
	 */
	public void setAverages(double[] data) {

		FloatBuffer avgs = averages.getBuffer();
		for (int i = 0; i < data.length; i++) {
			float avg = (float) data[i];
			avgs.put(avg);
			System.out.println(i + ": " + avg);

		}
		avgs.rewind();
		queue.putWriteBuffer(averages, true);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC3D#step()
	 */
	@Override
	public boolean step() {
		final CLKernel regionAverage = kernelMap.get("regionAverage");
		final CLKernel sumAverages = kernelMap.get("sumAverages");
		final CLKernel pressureSpeedKernel = kernelMap.get("acweSpeedKernel");
		final CLKernel maxImageValue = kernelMap.get("maxImageValue");
		final CLKernel maxTimeStep = kernelMap.get("maxTimeStep");
		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");

		int global_size = roundToWorkgroupPower(activeListSize);
		long startTime = System.nanoTime();
		if (intensityEstimation && time % intensityEstimationInterval == 0) {
			regionAverage.putArgs(imageLabelBuffer, distanceFieldBuffer,
					pressureBuffer, labelMaskBuffer, averages, areas, stddev)
					.rewind();
			queue.put1DRangeKernel(regionAverage, 0,
					roundToWorkgroupPower(slices), WORKGROUP_SIZE);
			queue.finish();
			sumAverages.putArgs(averages, areas, stddev).rewind();
			queue.put1DRangeKernel(sumAverages, 0,
					roundToWorkgroupPower(numLabels), WORKGROUP_SIZE);
		}
		pressureSpeedKernel
				.putArgs(activeListBuffer, pressureBuffer,
						oldDistanceFieldBuffer, oldImageLabelBuffer,
						deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
						forceIndexesBuffer, averages).putArg(pressureWeight)
				.putArg(curvatureWeight).putArg(activeListSize).rewind();
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

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File fimg = new File(PlaceHolder.class.getResource("metacube.nii")
					.toURI());
			File flabel = new File(PlaceHolder.class.getResource(
					"ufo_labels.nii").toURI());
			File fdistfield = new File(PlaceHolder.class.getResource(
					"ufo_distfield.nii").toURI());
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat initDistfield = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdistfield));
			ImageDataFloat refImage = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fimg));

			MACWE3D activeContour = new MACWE3D(refImage, CLDevice.Type.CPU);
			activeContour.setPressure(refImage, 1.0f);
			activeContour.setCurvatureWeight(0.5f);
			activeContour.setMaxIterations(340);
			activeContour.setClampSpeed(true);
			if (showGUI) {
				try {
					activeContour.init(initDistfield, initLabels, new double[] {
							0, 2, 1 }, false);

					VisualizationMOGAC3D visual = new VisualizationMOGAC3D(512,
							512, activeContour);
					VisualizationApplication app = new VisualizationApplication(
							visual);
					app.setPreferredSize(new Dimension(920, 650));
					app.setShowToolBar(true);
					app.addListener(visual);
					app.runAndWait();
					visual.dispose();
					System.exit(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				activeContour.solve(initDistfield, initLabels, new double[] {
						0, 2, 1 }, false);
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC3D#init(edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat, edu.jhu.ece.iacl.jist.structures.image.ImageDataInt, boolean)
	 */
	@Override
	public void init(ImageDataFloat unsignedLevelSetBuffer,
			ImageDataInt labelBuffer, boolean containsOverlaps)
			throws IOException {
		targetPressure = Float.NaN;
		super.init(unsignedLevelSetBuffer, labelBuffer, containsOverlaps);
		averages = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		areas = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
		stddev = context.createFloatBuffer(
				SpringlsCommon3D.roundToWorkgroupPower(numLabels * slices),
				READ_WRITE, USE_BUFFER);
	}

	/**
	 * Sets the intensity estimation.
	 * 
	 * @param dynamic
	 *            the new intensity estimation
	 */
	public void setIntensityEstimation(boolean dynamic) {
		this.intensityEstimation = dynamic;
	}

	/**
	 * Sets the intensity estimation interval.
	 * 
	 * @param interval
	 *            the new intensity estimation interval
	 */
	public void setIntensityEstimationInterval(int interval) {
		this.intensityEstimationInterval = interval;
	}

	/**
	 * Prints the averages.
	 */
	private void printAverages() {
		queue.putReadBuffer(averages, true).putReadBuffer(areas, true)
				.putReadBuffer(stddev, true);
		FloatBuffer avgs = averages.getBuffer();
		FloatBuffer area = areas.getBuffer();
		FloatBuffer std = stddev.getBuffer();
		int count = 0;
		while (avgs.hasRemaining()) {
			System.out.println(count + ": " + avgs.get() + ": " + area.get()
					+ " : " + std.get());
			count++;
			if (count >= numLabels) {
				break;
			}

		}
		std.rewind();
		avgs.rewind();
		area.rewind();
	}
}
