/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.mogac;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.TreeSet;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class WEMOGAC2D is an implementation of Work-Effficient Multi-Object
 * Geodesic Active Contours 2D.
 */
public class WEMOGAC2D extends MOGAC2D {

	/** The Constant MAX_DISTANCE. */
	public static final float MAX_DISTANCE = 3.5f;

	/** The active list array size. */
	protected int activeListArraySize;

	/** The active list buffer. */
	protected CLBuffer<IntBuffer> activeListBuffer;

	/** The active list size. */
	protected int activeListSize;

	/** The max temporary buffer. */
	protected CLBuffer<FloatBuffer> maxTmpBuffer = null;

	/** The max value buffer. */
	protected CLBuffer<IntBuffer> maxValueBuffer = null;

	/** The offset buffer. */
	protected CLBuffer<IntBuffer> offsetBuffer = null;

	/** The temporary active buffer. */
	protected CLBuffer<IntBuffer> tmpActiveBuffer = null;

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 2D.
	 * 
	 * @param refImage
	 *            the reference image
	 */
	public WEMOGAC2D(ImageData refImage) {
		super(refImage, CLDevice.Type.CPU);
	}

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 2D.
	 * 
	 * @param refImage
	 *            the reference image
	 * @param type
	 *            the type
	 */
	public WEMOGAC2D(ImageData refImage, CLDevice.Type type) {
		super(refImage, type);

	}

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 2D.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param context
	 *            the context
	 * @param queue
	 *            the queue
	 */
	public WEMOGAC2D(int rows, int cols, CLContext context, CLCommandQueue queue) {
		super(rows, cols, context, queue);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#init(edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat, edu.jhu.ece.iacl.jist.structures.image.ImageDataInt, boolean)
	 */
	@Override
	public void init(ImageDataFloat unsignedImage, ImageDataInt labelImage,
			boolean containsOverlaps) throws IOException {

		rows = labelImage.getRows();
		cols = labelImage.getCols();
		this.containsOverlaps = containsOverlaps;
		int l;
		int mask = 0x00000001;
		this.labels = labelImage.toArray2d();
		if (containsOverlaps) {
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			int totalMask = 0;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					l = labels[i][j];
					totalMask |= l;
					labelHash.add(l);
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
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					l = labels[i][j];
					labelHash.add(l);
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
				getClass().getResourceAsStream("WEMogacEvolveLevelSet2D.cl"))
				.build(define("ROWS", rows), define("COLS", cols),
						define("CONTAINS_OVERLAPS", containsOverlaps),
						define("CLAMP_SPEED", clampSpeed ? 1 : 0),
						define("NUM_LABELS", numLabels),
						define("STRIDE", STRIDE),
						define("MAX_DISTANCE", MAX_DISTANCE + "f"));

		kernelMap = program.createCLKernels();
		imageLabelBuffer = context.createIntBuffer(rows * cols, READ_WRITE,
				USE_BUFFER);
		oldImageLabelBuffer = context.createIntBuffer(rows * cols, READ_WRITE,
				USE_BUFFER);
		IntBuffer label = imageLabelBuffer.getBuffer();
		IntBuffer oldLabel = oldImageLabelBuffer.getBuffer();
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				int lab = labels[i][j];
				label.put(lab);
				oldLabel.put(lab);
			}
		}
		label.rewind();
		oldLabel.rewind();
		oldDistanceFieldBuffer = context.createFloatBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);
		if (unsignedImage == null) {
			convertLabelsToLevelSet();
			unsignedImage = new ImageDataFloat(rows, cols);
			unsignedImage.setName(image.getName() + "_distfield");
			queue.putReadBuffer(distanceFieldBuffer, true);
			FloatBuffer buff = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			distField = unsignedImage.toArray2d();
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					float tmp = distField[i][j] = buff.get();
					oldUnsignedLevelSet.put(tmp);
				}
			}
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(oldDistanceFieldBuffer, true);
			buff.rewind();
		} else {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols,
					READ_WRITE, USE_BUFFER);
			FloatBuffer unsignedLevelSet = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray2d();
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					float val = Math.min(
							Math.max(distField[i][j], -(maxLayers + 1)),
							(maxLayers + 1));
					unsignedLevelSet.put(val);
					oldUnsignedLevelSet.put(val);
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
			rescale(pressureImage.toArray2d());
			float[][] pressure = pressureImage.toArray2d();
			pressureBuffer = context.createFloatBuffer(rows * cols, READ_ONLY);
			FloatBuffer buff = pressureBuffer.getBuffer();
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					buff.put(pressure[i][j]);
				}
			}
			buff.rewind();
			queue.putWriteBuffer(pressureBuffer, true);
			if (vecFieldImage != null) {
				float[][][] vecField = vecFieldImage.toArray3d();
				vecFieldBuffer = context.createFloatBuffer(rows * cols * 2,
						READ_ONLY);
				FloatBuffer advectBuff = vecFieldBuffer.getBuffer();
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						advectBuff.put(vecField[i][j][0]);
						advectBuff.put(vecField[i][j][1]);
					}
				}
				advectBuff.rewind();
				queue.putWriteBuffer(vecFieldBuffer, true);
			}
		}
		queue.putWriteBuffer(labelMaskBuffer, true).putWriteBuffer(
				forceIndexesBuffer, true);
		this.distFieldImage = unsignedImage;
		this.labelImage = labelImage;
		this.labelImage.setName(image.getName() + "_labels");
		distFieldImage.setName(image.getName() + "_distfield");
		setTotalUnits(maxIterations / resamplingInterval);
		finish();
		rebuildNarrowBand();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#init(com.jogamp.opencl.CLBuffer, int)
	 */
	@Override
	public void init(CLBuffer<IntBuffer> labelBuffer, int numClusters)
			throws IOException {
		init(null, labelBuffer, numClusters);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#init(com.jogamp.opencl.CLBuffer, com.jogamp.opencl.CLBuffer, int)
	 */
	@Override
	public void init(CLBuffer<FloatBuffer> unsignedLevelSetBuffer,
			CLBuffer<IntBuffer> labelBuffer, int numClusters)
			throws IOException {
		int l;
		numLabels = numClusters;
		numObjects = numClusters - 1;
		this.containsOverlaps = false;
		CLProgram program = context.createProgram(
				getClass().getResourceAsStream("WEMogacEvolveLevelSet2D.cl"))
				.build(define("ROWS", rows), define("COLS", cols),
						define("CONTAINS_OVERLAPS", containsOverlaps),
						define("CLAMP_SPEED", clampSpeed ? 1 : 0),
						define("NUM_LABELS", numLabels),
						define("STRIDE", STRIDE),
						define("MAX_DISTANCE", MAX_DISTANCE));

		kernelMap = program.createCLKernels();

		this.imageLabelBuffer = labelBuffer;

		this.distanceFieldBuffer = unsignedLevelSetBuffer;
		oldImageLabelBuffer = context.createIntBuffer(rows * cols, READ_WRITE,
				USE_BUFFER);
		oldDistanceFieldBuffer = context.createFloatBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);
		if (unsignedLevelSetBuffer == null) {
			convertLabelsToLevelSet();
		}

		FloatBuffer unsignedLevelSet = this.distanceFieldBuffer.getBuffer();
		FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer.getBuffer();
		IntBuffer label = labelBuffer.getBuffer();
		IntBuffer oldLabel = oldImageLabelBuffer.getBuffer();
		oldUnsignedLevelSet.put(unsignedLevelSet).rewind();
		oldLabel.put(label).rewind();
		queue.putWriteBuffer(oldDistanceFieldBuffer, true);
		queue.putWriteBuffer(oldImageLabelBuffer, true);

		l = 0;
		labelMasks = new int[numLabels];
		for (l = 0; l < numClusters; l++) {
			labelMasks[l] = l;
		}
		forceIndexes = new int[numLabels];
		for (l = 0; l < numLabels; l++) {
			forceIndexes[l] = l - 1;
		}
		labelMaskBuffer = context.createIntBuffer(labelMasks.length, READ_ONLY,
				USE_BUFFER);
		labelMaskBuffer.getBuffer().put(labelMasks).rewind();
		forceIndexesBuffer = context.createIntBuffer(forceIndexes.length,
				READ_ONLY, USE_BUFFER);
		forceIndexesBuffer.getBuffer().put(forceIndexes).rewind();
		unsignedLevelSet.rewind();
		oldUnsignedLevelSet.rewind();
		label.rewind();
		oldLabel.rewind();
		queue.putWriteBuffer(oldDistanceFieldBuffer, true)
				.putWriteBuffer(labelMaskBuffer, true)
				.putWriteBuffer(forceIndexesBuffer, true)
				.putWriteBuffer(oldImageLabelBuffer, true);
		setTotalUnits(maxIterations / resamplingInterval);
		rebuildNarrowBand();

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#convertLabelsToLevelSet()
	 */
	@Override
	public void convertLabelsToLevelSet() {
		final CLKernel labelsToLevelSet = kernelMap.get("labelsToLevelSet");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceFieldFullGrid");
		if (distanceFieldBuffer == null) {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols,
					USE_BUFFER, READ_WRITE);
		}
		labelsToLevelSet.putArgs(imageLabelBuffer, oldImageLabelBuffer,
				distanceFieldBuffer, oldDistanceFieldBuffer).rewind();
		queue.put1DRangeKernel(labelsToLevelSet, 0, rows * cols, WORKGROUP_SIZE);
		for (int i = 1; i <= maxLayers; i++) {
			extendDistanceField
					.putArgs(oldDistanceFieldBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(i).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0, rows * cols,
					WORKGROUP_SIZE);
		}

		queue.putReadBuffer(distanceFieldBuffer, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.algorithms.segmentation.gac.MultiGeodesicActiveContour2D
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
		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel maxTimeStep = kernelMap.get("maxTimeStep");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");
		int global_size = roundToWorkgroupPower(activeListSize);
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
				for (int nn = 0; nn < 4; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldDistanceFieldBuffer,
									oldImageLabelBuffer, deltaLevelSetBuffer,
									idBuffer, distanceFieldBuffer,
									imageLabelBuffer, maxTmpBuffer)
							.putArg(activeListSize).putArg(nn).rewind();

					queue.put1DRangeKernel(applyForces, 0, global_size,
							WORKGROUP_SIZE);
				}
			} else {
				for (int nn = 0; nn < 4; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldDistanceFieldBuffer,
									oldImageLabelBuffer, deltaLevelSetBuffer,
									idBuffer, distanceFieldBuffer,
									imageLabelBuffer).putArg(0.5f)
							.putArg(activeListSize).putArg(nn).rewind();

					queue.put1DRangeKernel(applyForces, 0, global_size,
							WORKGROUP_SIZE);
				}
			}
		} else {
			if (!clampSpeed) {

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
		for (int i = 1; i <= maxLayers; i++) {
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

		copyBuffers
				.putArgs(activeListBuffer, oldDistanceFieldBuffer,
						oldImageLabelBuffer, distanceFieldBuffer,
						imageLabelBuffer).putArg(activeListSize).rewind();
		queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
		contours = null;
		dirty = true;
		time++;
		deleteElements();
		addElements();
		if (time % resamplingInterval == 0) {
			queue.finish();
			long tmp = System.nanoTime();
			for (FrameUpdateListener updater : listeners) {
				updater.frameUpdate((int) time, 1E9 * resamplingInterval
						/ (tmp - lastStartTime));
			}
			lastStartTime = tmp;
			incrementCompletedUnits();
		}
		return true;
	}

	/**
	 * Adds elements from the active list.
	 */
	protected void addElements() {
		final CLKernel addCountActiveList = kernelMap.get("addCountActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel expandActiveList = kernelMap.get("expandActiveList");
		// Do after SWAP!
		for (int nn = 0; nn < 4; nn++) {
			addCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							distanceFieldBuffer).putArg(activeListSize)
					.putArg(nn).rewind();
			queue.put1DRangeKernel(addCountActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		}
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(4 * (1 + (activeListSize / STRIDE))).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		int addElements = maxValueBuffer.getBuffer().get(0);
		int newElements = addElements + activeListSize;
		if (newElements != activeListSize) {
			if (newElements > activeListArraySize) {
				rebuildNarrowBand();
				return;
			}
			for (int nn = 0; nn < 4; nn++) {
				expandActiveList
						.putArgs(offsetBuffer, activeListBuffer,
								oldDistanceFieldBuffer, distanceFieldBuffer)
						.putArg(activeListSize).putArg(nn).rewind();
				queue.put1DRangeKernel(
						expandActiveList,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1),
						1);
			}
		}
		activeListSize = newElements;
		queue.finish();

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
		CLBuffer<IntBuffer> rebuildOffsetBuffer = context.createIntBuffer(cols,
				READ_WRITE, USE_BUFFER);
		countActiveList.putArgs(rebuildOffsetBuffer, oldDistanceFieldBuffer,
				distanceFieldBuffer).rewind();

		queue.put1DRangeKernel(countActiveList, 0, cols, WORKGROUP_SIZE / 4);
		prefixScanList.putArgs(rebuildOffsetBuffer, maxValueBuffer)
				.putArg(cols).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);

		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		activeListSize = maxValueBuffer.getBuffer().get(0);
		activeListArraySize = (int) Math.min(rows * cols,
				Math.max(activeListSize * 1.25f, Math.ceil(rows * cols * 0.1)));
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
		queue.put1DRangeKernel(buildActiveList, 0, cols, 2);

		queue.finish();

		rebuildOffsetBuffer.release();
		if (offsetBuffer != null) {
			offsetBuffer.release();
		}
		offsetBuffer = context.createIntBuffer(
				4 * (1 + (activeListArraySize / STRIDE)), USE_BUFFER,
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
				activeListArraySize * 5, USE_BUFFER, READ_WRITE);
		if (idBuffer != null) {
			idBuffer.release();
		}
		idBuffer = context.createIntBuffer(activeListArraySize * 5, USE_BUFFER,
				READ_WRITE);
	}

	/**
	 * Delete elements from the active list.
	 */
	protected void deleteElements() {
		final CLKernel deleteCountActiveList = kernelMap
				.get("deleteCountActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = kernelMap.get("compactActiveList");
		// Do after SWAP!
		deleteCountActiveList
				.putArgs(offsetBuffer, activeListBuffer, oldDistanceFieldBuffer)
				.putArg(activeListSize).rewind();
		queue.put1DRangeKernel(deleteCountActiveList, 0,
				roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		int newElements = maxValueBuffer.getBuffer().get(0);

		if (newElements != activeListSize) {
			// System.out.println("DELETE " + (newElements - activeListSize));
			compactActiveList
					.putArgs(offsetBuffer, activeListBuffer, tmpActiveBuffer,
							oldDistanceFieldBuffer, distanceFieldBuffer)
					.putArg(activeListSize).rewind();

			queue.put1DRangeKernel(compactActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);

			activeListSize = newElements;
			queue.finish();
			CLBuffer<IntBuffer> tmp = activeListBuffer;
			activeListBuffer = tmpActiveBuffer;
			tmpActiveBuffer = tmp;
		}
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
			File fdist = new File(PlaceHolder.class.getResource(
					"shapes_overlap_distfield.nii").toURI());

			File flabel = new File(PlaceHolder.class.getResource(
					"shapes_overlap_labels.nii").toURI());
			File fimg = new File(PlaceHolder.class.getResource("x.png").toURI());
			ImageDataFloat initDistField = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdist));
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat refImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(fimg));
			WEMOGAC2D activeContour = new WEMOGAC2D(refImage, CLDevice.Type.CPU);
			activeContour.setPressure(refImage, -0.5f);
			activeContour.setCurvatureWeight(1.0f);
			activeContour.setTargetPressure(128.0f);
			activeContour.setMaxIterations(601);
			activeContour.setClampSpeed(true);
			if (showGUI) {
				try {
					activeContour.init(initDistField, initLabels, true);

					VisualizationMOGAC2D visual = new VisualizationMOGAC2D(512,
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
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
