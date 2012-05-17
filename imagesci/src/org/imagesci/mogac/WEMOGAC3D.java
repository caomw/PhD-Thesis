/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.TreeSet;

import javax.vecmath.Point3i;

import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.utility.PhantomMetasphere;
import org.imagesci.utility.RandomSphereCollection;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class WEMOGAC3D is an implementation of Work-Effficient Multi-Object
 * Geodesic Active Contours 3D.
 */
public class WEMOGAC3D extends MOGAC3D {

	/** The Constant STRIDE. */
	protected static final int STRIDE = SpringlsCommon3D.STRIDE;

	/** The active list array size. */
	public int activeListArraySize;

	/** The active list buffer. */
	public CLBuffer<IntBuffer> activeListBuffer;

	/** The active list size. */
	public int activeListSize;

	/** The history buffer. */
	protected CLBuffer<IntBuffer> historyBuffer = null;

	/** The max distance. */
	final float MAX_DISTANCE = 3.5f;

	/** The max temporary buffer. */
	protected CLBuffer<FloatBuffer> maxTmpBuffer = null;

	/** The max value buffer. */
	protected CLBuffer<IntBuffer> maxValueBuffer = null;

	/** The offset buffer. */
	protected CLBuffer<IntBuffer> offsetBuffer = null;

	/** The sampling_interval. */
	protected int sampling_interval = 8;

	/** The temporary active buffer. */
	protected CLBuffer<IntBuffer> tmpActiveBuffer = null;

	/** The use adaptive active set. */
	protected boolean useAdaptiveActiveSet = false;

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 3D.
	 * 
	 * @param refImage
	 *            the reference image
	 */
	public WEMOGAC3D(ImageData refImage) {
		super(refImage, CLDevice.Type.CPU);
	}

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 3D.
	 * 
	 * @param refImage
	 *            the reference image
	 * @param context
	 *            the context
	 * @param queue
	 *            the queue
	 */
	public WEMOGAC3D(ImageData refImage, CLContext context, CLCommandQueue queue) {
		super(refImage, context, queue);

	}

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 3D.
	 * 
	 * @param refImage
	 *            the reference image
	 * @param type
	 *            the type
	 */
	public WEMOGAC3D(ImageData refImage, CLDevice.Type type) {
		super(refImage, type);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC3D#init(edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat, edu.jhu.ece.iacl.jist.structures.image.ImageDataInt, boolean)
	 */
	@Override
	public void init(ImageDataFloat unsignedImage, ImageDataInt labelImage,
			boolean containsOverlaps) throws IOException {
		rows = labelImage.getRows();
		cols = labelImage.getCols();
		slices = labelImage.getSlices();
		this.containsOverlaps = containsOverlaps;
		int mask = 0x00000001;
		int l = 0;
		this.labels = labelImage.toArray3d();
		if (containsOverlaps) {
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			int totalMask = 0;
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						l = labels[i][j][k];
						totalMask |= l;
						labelHash.add(l);
					}
				}
			}
			numObjects = 0;
			while (totalMask != 0) {
				if ((totalMask & 0x01) != 0) {
					numObjects++;
				}
				totalMask >>= 1;
			}
			numLabels = labelHash.size();
			l = 0;
			labelMasks = new int[numLabels];
			for (Integer val : labelHash) {
				labelMasks[l++] = val;
			}
			forceIndexes = new int[numLabels];
			for (int i = 0; i < numLabels; i++) {
				mask = labelMasks[i];
				forceIndexes[i] = -1;
				for (int b = 0; b < numObjects; b++) {
					if ((0x01 << b) == mask) {
						forceIndexes[i] = b;
						break;
					}
				}
			}
		} else {
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						l = labels[i][j][k];
						labelHash.add(l);
					}
				}
			}
			numLabels = labelHash.size();
			numObjects = numLabels - 1;
			l = 0;

			labelMasks = new int[numLabels];
			for (Integer val : labelHash) {
				labelMasks[l++] = val;
			}
			forceIndexes = new int[numLabels];
			for (int i = 0; i < numLabels; i++) {
				mask = labelMasks[i];
				forceIndexes[i] = i - 1;
			}
		}
		CLProgram program = context.createProgram(
				getClass().getResourceAsStream("WEMogacEvolveLevelSet3D.cl"))
				.build(define("ROWS", rows), define("COLS", cols),
						define("SLICES", slices),
						define("CONTAINS_OVERLAPS", containsOverlaps),
						define("CLAMP_SPEED", clampSpeed ? 1 : 0),
						define("NUM_LABELS", numLabels),
						define("STRIDE", STRIDE),
						define("MAX_DISTANCE", MAX_DISTANCE));

		kernelMap = program.createCLKernels();
		imageLabelBuffer = context.createIntBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);
		oldImageLabelBuffer = context.createIntBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);
		IntBuffer label = imageLabelBuffer.getBuffer();
		IntBuffer oldLabel = oldImageLabelBuffer.getBuffer();
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					int lab = labels[i][j][k];
					label.put(lab);
					oldLabel.put(lab);
				}
			}
		}
		label.rewind();
		oldLabel.rewind();
		queue.putWriteBuffer(imageLabelBuffer, true).putWriteBuffer(
				oldImageLabelBuffer, true);
		oldDistanceFieldBuffer = context.createFloatBuffer(
				rows * cols * slices, READ_WRITE, USE_BUFFER);
		if (unsignedImage == null) {

			convertLabelsToLevelSet();
			unsignedImage = new ImageDataFloat(rows, cols, slices);
			unsignedImage.setName(image.getName() + "_distfield");
			queue.putReadBuffer(distanceFieldBuffer, true);
			FloatBuffer buff = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray3d();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {

						float val = distField[i][j][k] = buff.get();
						oldUnsignedLevelSet.put(val);
					}
				}
			}
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(oldDistanceFieldBuffer, true);
			buff.rewind();
		} else {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols
					* slices, READ_WRITE, USE_BUFFER);

			FloatBuffer unsignedLevelSet = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray3d();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						float val = Math.min(MAX_DISTANCE + 0.5f,
								distField[i][j][k]);
						unsignedLevelSet.put(val);
						oldUnsignedLevelSet.put(val);
					}
				}
			}
			unsignedLevelSet.rewind();
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(distanceFieldBuffer, true).putWriteBuffer(
					oldDistanceFieldBuffer, true);
		}
		labelMaskBuffer = context.createIntBuffer(labelMasks.length, READ_ONLY,
				USE_BUFFER);
		labelMaskBuffer.getBuffer().put(labelMasks).rewind();
		forceIndexesBuffer = context.createIntBuffer(forceIndexes.length,
				READ_ONLY, USE_BUFFER);
		forceIndexesBuffer.getBuffer().put(forceIndexes).rewind();

		if (pressureImage != null) {
			rescale(pressureImage.toArray3d());
			float[][][] pressure = pressureImage.toArray3d();
			pressureBuffer = context.createFloatBuffer(rows * cols * slices,
					READ_ONLY);
			FloatBuffer buff = pressureBuffer.getBuffer();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						buff.put(pressure[i][j][k]);
					}
				}
			}
			buff.rewind();
			queue.putWriteBuffer(pressureBuffer, true);
		}
		if (vecFieldImage != null) {
			float[][][][] vecField = vecFieldImage.toArray4d();
			vecFieldBuffer = context.createFloatBuffer(
					rows * cols * slices * 3, READ_ONLY);
			FloatBuffer advectBuff = vecFieldBuffer.getBuffer();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						advectBuff.put(vecField[i][j][k][0]);
						advectBuff.put(vecField[i][j][k][1]);
						advectBuff.put(vecField[i][j][k][2]);
					}
				}
			}
			advectBuff.rewind();
			queue.putWriteBuffer(vecFieldBuffer, true);
		}
		queue.putWriteBuffer(labelMaskBuffer, true).putWriteBuffer(
				forceIndexesBuffer, true);

		this.distFieldImage = unsignedImage;
		this.labelImage = labelImage;
		if (image != null) {
			this.labelImage.setName(image.getName() + "_labels");
			distFieldImage.setName(image.getName() + "_distfield");
		} else {
			this.labelImage.setName("labels");
			distFieldImage.setName("distfield");

		}
		setTotalUnits(maxIterations / getResamplingRate());

		if (topologyPreservation) {
			loadLUT626();
		}
		finish();
		CLDevice.Type type = queue.getDevice().getType();
		if (type == CLDevice.Type.CPU) {
			WORKGROUP_SIZE = STRIDE;
		} else if (type == CLDevice.Type.GPU) {
			WORKGROUP_SIZE = 128;

		}

		rebuildNarrowBand();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC3D#convertLabelsToLevelSet()
	 */
	@Override
	public void convertLabelsToLevelSet() {
		final CLKernel labelsToLevelSet = kernelMap.get("labelsToLevelSet");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceFieldFullGrid");
		if (distanceFieldBuffer == null) {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols
					* slices, USE_BUFFER, READ_WRITE);
		}
		labelsToLevelSet.putArgs(imageLabelBuffer, oldImageLabelBuffer,
				distanceFieldBuffer, oldDistanceFieldBuffer).rewind();
		queue.put1DRangeKernel(labelsToLevelSet, 0, roundToWorkgroupPower(rows
				* cols * slices), WORKGROUP_SIZE);
		for (int i = 1; i <= 2 * MAX_LAYERS; i++) {
			extendDistanceField
					.putArgs(oldDistanceFieldBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(i).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0,
					roundToWorkgroupPower(rows * cols * slices), WORKGROUP_SIZE);
		}
		queue.putReadBuffer(distanceFieldBuffer, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.algorithms.segmentation.gac.MultiGeodesicActiveContour3D
	 * #step()
	 */
	@Override
	public boolean step() {
		final CLKernel pressureSpeedKernel = kernelMap
				.get("pressureSpeedKernel");
		final CLKernel vecFieldSpeedKernel = kernelMap
				.get("vecFieldSpeedKernel");
		final CLKernel pressureVecFieldSpeedKernel = kernelMap
				.get("pressureVecFieldSpeedKernel");
		final CLKernel maxImageValue = kernelMap.get("maxImageValue");
		final CLKernel maxTimeStep = kernelMap.get("maxTimeStep");
		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");
		int global_size = roundToWorkgroupPower(activeListSize);
		long startTime = System.nanoTime();
		if (pressureBuffer != null) {
			if (vecFieldBuffer != null) {
				pressureVecFieldSpeedKernel
						.putArgs(activeListBuffer, pressureBuffer,
								vecFieldBuffer, oldDistanceFieldBuffer,
								oldImageLabelBuffer, deltaLevelSetBuffer,
								idBuffer, labelMaskBuffer, forceIndexesBuffer)
						.putArg(pressureWeight).putArg(vecFieldWeight)
						.putArg(curvatureWeight).putArg(activeListSize)
						.rewind();
				queue.put1DRangeKernel(pressureVecFieldSpeedKernel, 0,
						global_size, WORKGROUP_SIZE);
			} else {
				pressureSpeedKernel
						.putArgs(activeListBuffer, pressureBuffer,
								oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
								forceIndexesBuffer).putArg(pressureWeight)
						.putArg(curvatureWeight).putArg(activeListSize)
						.rewind();
				queue.put1DRangeKernel(pressureSpeedKernel, 0, global_size,
						WORKGROUP_SIZE);
			}
		} else {
			vecFieldSpeedKernel
					.putArgs(activeListBuffer, vecFieldBuffer,
							oldDistanceFieldBuffer, oldImageLabelBuffer,
							deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
							forceIndexesBuffer).putArg(vecFieldWeight)
					.putArg(curvatureWeight).putArg(activeListSize).rewind();
			queue.put1DRangeKernel(vecFieldSpeedKernel, 0, global_size,
					WORKGROUP_SIZE);
		}
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
		deleteElements();
		addElements();
		stepElapsedTime += (System.nanoTime() - startTime);
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
	 * Adds elements to the active list.
	 * 
	 * @return the nubmer of added elements.
	 */
	protected int addElements() {
		if (activeListSize == 0) {
			return 0;
		}
		final CLKernel addCountActiveList = kernelMap.get("addCountActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel expandActiveList = (useAdaptiveActiveSet) ? kernelMap
				.get("expandActiveListHistory") : kernelMap
				.get("expandActiveList");
		for (int nn = 0; nn < 6; nn++) {
			addCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							distanceFieldBuffer).putArg(activeListSize)
					.putArg(nn).rewind();
			queue.put1DRangeKernel(addCountActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		}
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(6 * (1 + (activeListSize / STRIDE))).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		int addElements = maxValueBuffer.getBuffer().get(0);
		int newElements = addElements + activeListSize;
		if (newElements != activeListSize) {
			if (newElements > activeListArraySize) {
				rebuildNarrowBand();
				return addElements;
			}

			for (int nn = 0; nn < 6; nn++) {
				if (useAdaptiveActiveSet) {
					expandActiveList
							.putArgs(offsetBuffer, activeListBuffer,
									oldDistanceFieldBuffer,
									distanceFieldBuffer, historyBuffer)
							.putArg(activeListSize).putArg(nn).rewind();
				} else {
					expandActiveList
							.putArgs(offsetBuffer, activeListBuffer,
									oldDistanceFieldBuffer, distanceFieldBuffer)
							.putArg(activeListSize).putArg(nn).rewind();
				}
				queue.put1DRangeKernel(
						expandActiveList,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1),
						1);
			}
		}
		activeListSize = newElements;
		queue.finish();
		return addElements;
	}

	/**
	 * Rebuild narrow band.
	 */
	public void rebuildNarrowBand() {
		final CLKernel countActiveList = kernelMap.get("countActiveList");
		final CLKernel buildActiveList = kernelMap.get("buildActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		if (maxValueBuffer == null) {
			maxValueBuffer = context.createIntBuffer(1, READ_WRITE, USE_BUFFER);
		}
		if (useAdaptiveActiveSet && historyBuffer == null) {
			historyBuffer = context.createIntBuffer(rows * cols * slices,
					USE_BUFFER, READ_WRITE);
		}
		CLBuffer<IntBuffer> rebuildOffsetBuffer = context.createIntBuffer(
				slices, READ_WRITE, USE_BUFFER);
		countActiveList.putArgs(rebuildOffsetBuffer, oldDistanceFieldBuffer,
				distanceFieldBuffer).rewind();

		queue.put1DRangeKernel(countActiveList, 0,
				roundToWorkgroupPower(slices, WORKGROUP_SIZE / 4),
				WORKGROUP_SIZE / 4);
		prefixScanList.putArgs(rebuildOffsetBuffer, maxValueBuffer)
				.putArg(slices).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);

		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		activeListSize = maxValueBuffer.getBuffer().get(0);
		activeListArraySize = (int) Math.min(
				rows * cols * slices,
				Math.max(activeListSize * 1.25f,
						Math.ceil(rows * cols * slices * 0.1)));
		System.out.println("Building narrowband with " + activeListSize
				+ " active voxels out of " + activeListArraySize
				+ " total voxels. ");
		if (activeListBuffer != null) {
			activeListBuffer.release();
		}
		activeListBuffer = context.createIntBuffer(activeListArraySize,
				READ_WRITE, USE_BUFFER);

		buildActiveList.putArgs(rebuildOffsetBuffer, activeListBuffer,
				oldDistanceFieldBuffer).rewind();
		queue.put1DRangeKernel(buildActiveList, 0, slices, 2);

		queue.finish();

		rebuildOffsetBuffer.release();

		if (offsetBuffer != null) {
			offsetBuffer.release();
		}
		offsetBuffer = context.createIntBuffer(
				6 * (1 + (activeListArraySize / STRIDE)), USE_BUFFER,
				READ_WRITE);

		if (maxTmpBuffer != null) {
			maxTmpBuffer.release();
		}
		maxTmpBuffer = context.createFloatBuffer(
				1 + (activeListArraySize / STRIDE), READ_WRITE, USE_BUFFER);

		if (tmpActiveBuffer != null) {
			tmpActiveBuffer.release();
		}

		tmpActiveBuffer = context.createIntBuffer(activeListArraySize,
				USE_BUFFER, READ_WRITE);

		if (deltaLevelSetBuffer != null) {
			deltaLevelSetBuffer.release();
		}
		deltaLevelSetBuffer = context.createFloatBuffer(
				activeListArraySize * 7, USE_BUFFER, READ_WRITE);

		if (idBuffer != null) {
			idBuffer.release();
		}
		idBuffer = context.createIntBuffer(activeListArraySize * 7, USE_BUFFER,
				READ_WRITE);
	}

	/**
	 * Delete elements from the active list.
	 * 
	 * @return the number of deleted elements.
	 */
	protected int deleteElements() {
		final CLKernel deleteCountActiveList = kernelMap
				.get("deleteCountActiveList");
		final CLKernel deleteCountActiveListHistory = kernelMap
				.get("deleteCountActiveListHistory");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = kernelMap.get("compactActiveList");
		final CLKernel compactActiveListHistory = kernelMap
				.get("compactActiveListHistory");

		if (useAdaptiveActiveSet
				&& time % sampling_interval == sampling_interval - 1) {

			deleteCountActiveListHistory
					.putArgs(offsetBuffer, activeListBuffer,
							oldDistanceFieldBuffer, historyBuffer)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(deleteCountActiveListHistory, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		} else {

			deleteCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							oldDistanceFieldBuffer).putArg(activeListSize)
					.rewind();
			queue.put1DRangeKernel(deleteCountActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		}
		queue.finish();
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		int newElements = maxValueBuffer.getBuffer().get(0);

		int delete = activeListSize - newElements;
		if (newElements == 0) {
			activeListSize = newElements;
			return delete;
		}

		if ((useAdaptiveActiveSet) || newElements != activeListSize) {

			System.nanoTime();

			if (useAdaptiveActiveSet
					&& time % sampling_interval == sampling_interval - 1) {
				compactActiveListHistory
						.putArgs(offsetBuffer, activeListBuffer,
								tmpActiveBuffer, oldDistanceFieldBuffer,
								distanceFieldBuffer, historyBuffer)
						.putArg(activeListSize).rewind();
				queue.put1DRangeKernel(
						compactActiveListHistory,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1),
						1);
			} else {

				compactActiveList
						.putArgs(offsetBuffer, activeListBuffer,
								tmpActiveBuffer, oldDistanceFieldBuffer,
								distanceFieldBuffer).putArg(activeListSize)
						.rewind();

				queue.put1DRangeKernel(
						compactActiveList,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1),
						1);
			}

			activeListSize = newElements;
			queue.finish();
			CLBuffer<IntBuffer> tmp = activeListBuffer;
			activeListBuffer = tmpActiveBuffer;
			tmpActiveBuffer = tmp;
		}
		return delete;
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
		RandomSphereCollection rando = new RandomSphereCollection(128, 128,
				128, 27, 15);
		PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(128,
				128, 128));
		metasphere.setNoiseLevel(0.1);
		metasphere.setFuzziness(0.5f);
		metasphere.setInvertImage(true);
		metasphere.solve();
		ImageDataFloat refImage = metasphere.getImage();

		ImageDataFloat initDistField = rando.getDistanceField();
		ImageDataInt initLabels = rando.getLabelImage();
		WEMOGAC3D activeContour = new WEMOGAC3D(refImage, CLDevice.Type.CPU);
		activeContour.setPressure(refImage, 0.5f);
		activeContour.setCurvatureWeight(1.0f);
		activeContour.setTargetPressure(0.5f);
		activeContour.setMaxIterations(620);
		activeContour.setClampSpeed(true);
		activeContour.setAdaptiveUpdate(true);
		activeContour.setAdaptiveUpdateInterval(20);
		if (showGUI) {
			try {
				activeContour.init(initDistField, initLabels, false);

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
			activeContour.solve(initDistField, initLabels, true);
		}
	}

	/**
	 * Sets the adaptive update.
	 * 
	 * @param adaptive
	 *            the new adaptive update
	 */
	public void setAdaptiveUpdate(boolean adaptive) {
		this.useAdaptiveActiveSet = adaptive;
	}

	/**
	 * Sets the adaptive update interval.
	 * 
	 * @param interval
	 *            the new adaptive update interval
	 */
	public void setAdaptiveUpdateInterval(int interval) {
		this.sampling_interval = interval;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC3D#solve(edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat, edu.jhu.ece.iacl.jist.structures.image.ImageDataInt, boolean)
	 */
	@Override
	public ImageDataFloat solve(ImageDataFloat unsignedImage,
			ImageDataInt labelImage, boolean containsOverlaps) {
		ImageDataFloat vol = super.solve(unsignedImage, labelImage,
				containsOverlaps);
		return vol;
	}
}
