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
package org.imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsEvolveLevelSet.
 */
public class SpringlsEvolveLevelSet2D {
	
	/** The Constant MAX_DISTANCE. */
	public static final float MAX_DISTANCE = 3.5f;
	/** The Constant MAX_LAYERS. */
	public static final int MAX_LAYERS = 3;
	
	/** The Constant STRIDE. */
	public static final int STRIDE = SpringlsCommon2D.STRIDE;
	
	/** The active list array size. */
	protected int activeListArraySize;
	
	/** The active list buffer. */
	protected CLBuffer<IntBuffer> activeListBuffer;
	
	/** The active list size. */
	protected int activeListSize;
	/** The commons. */
	protected SpringlsCommon2D commons;
	/** The curvature weight. */
	protected float curvatureWeight = 0.25f;
	/** The dice bins. */
	CLBuffer<IntBuffer> diceBins = null;
	/** The max iterations. */
	protected int maxIterations = 4;
	
	/** The max tmp buffer. */
	protected CLBuffer<FloatBuffer> maxTmpBuffer = null;
	
	/** The max value buffer. */
	protected CLBuffer<IntBuffer> maxValueBuffer = null;

	/** The new signed level set buffer. */
	protected CLBuffer<FloatBuffer> newSignedLevelSetBuffer;

	/** The offset buffer. */
	protected CLBuffer<IntBuffer> offsetBuffer = null;

	/** The old level set buffer. */
	CLBuffer<FloatBuffer> oldLevelSetBuffer = null;

	/** The step size. */
	protected float stepSize = 0.75f;

	/** The time. */
	protected int time = 0;

	/** The tmp active buffer. */
	protected CLBuffer<IntBuffer> tmpActiveBuffer = null;

	/**
	 * Instantiates a new springls evolve level set.
	 * 
	 * @param commons
	 *            the commons
	 * @param curvatureWeight
	 *            the curvature weight
	 */
	public SpringlsEvolveLevelSet2D(SpringlsCommon2D commons,
			float curvatureWeight) {
		this.commons = commons;
		this.curvatureWeight = curvatureWeight;
		commons.signedLevelSetBuffer = commons.context.createFloatBuffer(
				commons.rows * commons.cols, READ_WRITE, USE_BUFFER);
	}

	/**
	 * Instantiates a new springls evolve level set.
	 * 
	 * @param commons
	 *            the commons
	 * @param imageMat
	 *            the image mat
	 * @param curvatureWeight
	 *            the curvature weight
	 */
	public SpringlsEvolveLevelSet2D(SpringlsCommon2D commons,
			float[][] imageMat, float curvatureWeight) {
		this.commons = commons;
		this.curvatureWeight = curvatureWeight;
		commons.signedLevelSetBuffer = commons.context.createFloatBuffer(
				commons.rows * commons.cols, READ_WRITE, USE_BUFFER);
		FloatBuffer levelSet = commons.signedLevelSetBuffer.getBuffer();
		FloatBuffer originalLevelSet = commons.originalUnsignedLevelSetBuffer
				.getBuffer();
		for (int j = 0; j < commons.cols; j++) {
			for (int i = 0; i < commons.rows; i++) {
				float val = imageMat[i][j];
				levelSet.put(val);
				originalLevelSet.put(Math.abs(val));
			}
		}
		originalLevelSet.rewind();
		levelSet.rewind();
		commons.queue.putWriteBuffer(commons.signedLevelSetBuffer, true);
		commons.queue.putWriteBuffer(commons.originalUnsignedLevelSetBuffer,
				true);
	}

	/**
	 * Evolve.
	 *
	 * @return the double
	 */
	public double evolve() {
		commons.setActiveSetValid(false);
		for (int i = 0; i < maxIterations; i++) {
			this.step();
			deleteElements();
			addElements();
			time++;
		}
		(commons).activeListArraySize = activeListArraySize;
		(commons).activeListSize = activeListSize;
		(commons).activeListBuffer = activeListBuffer;
		return 0;
	}

	/**
	 * Evolve.
	 *
	 * @param checkConvergence the check convergence
	 * @return the double
	 */
	public double evolve(boolean checkConvergence) {
		int add = 0, delete = 0;
		int total = this.activeListSize;
		double avgDice = 0;
		double dice;
		commons.setActiveSetValid(false);
		for (int i = 0; i < maxIterations; i++) {
			this.step();
			total = this.activeListSize;
			delete = deleteElements();
			add = addElements();
			dice = 2 * (total - delete) / (double) (2 * total + add - delete);
			avgDice += dice;

			time++;
		}
		(commons).activeListArraySize = activeListArraySize;
		(commons).activeListSize = activeListSize;
		(commons).activeListBuffer = activeListBuffer;
		return avgDice / maxIterations;
	}

	/**
	 * Delete elements.
	 *
	 * @return the int
	 */
	private int deleteElements() {
		final CLKernel deleteCountActiveList = commons.kernelMap
				.get("deleteCountActiveList");
		final CLKernel prefixScanList = commons.kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = commons.kernelMap
				.get("compactActiveList");

		deleteCountActiveList
				.putArgs(offsetBuffer, activeListBuffer,
						commons.signedLevelSetBuffer, commons.indexBuffer,
						commons.unsignedLevelSetBuffer).putArg(activeListSize)
				.rewind();
		commons.queue.put1DRangeKernel(deleteCountActiveList, 0,
				SpringlsCommon2D.roundToWorkgroupPower(
						1 + (activeListSize / STRIDE), 1), 1);
		commons.queue.finish();
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);
		int newElements = maxValueBuffer.getBuffer().get(0);
		int delete = activeListSize - newElements;

		System.nanoTime();
		if (newElements != activeListSize) {

			compactActiveList
					.putArgs(offsetBuffer, activeListBuffer, tmpActiveBuffer,
							commons.signedLevelSetBuffer,
							newSignedLevelSetBuffer).putArg(activeListSize)
					.rewind();

			commons.queue.put1DRangeKernel(compactActiveList, 0,
					SpringlsCommon2D.roundToWorkgroupPower(
							1 + (activeListSize / STRIDE), 1), 1);

			activeListSize = newElements;
			commons.queue.finish();
			CLBuffer<IntBuffer> tmp = activeListBuffer;
			activeListBuffer = tmpActiveBuffer;
			tmpActiveBuffer = tmp;

		}

		return delete;
	}

	/**
	 * Adds the elements.
	 *
	 * @return the int
	 */
	private int addElements() {
		final CLKernel addCountActiveList = commons.kernelMap
				.get("addCountActiveList");
		final CLKernel prefixScanList = commons.kernelMap.get("prefixScanList");
		final CLKernel expandActiveList = commons.kernelMap
				.get("expandActiveList");
		for (int nn = 0; nn < 4; nn++) {
			addCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							newSignedLevelSetBuffer).putArg(activeListSize)
					.putArg(nn).rewind();
			commons.queue.put1DRangeKernel(addCountActiveList, 0,
					SpringlsCommon2D.roundToWorkgroupPower(
							1 + (activeListSize / STRIDE), 1), 1);
		}
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(4 * (1 + (activeListSize / STRIDE))).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);
		int addElements = maxValueBuffer.getBuffer().get(0);
		int newElements = addElements + activeListSize;
		if (newElements != activeListSize) {
			if (newElements > activeListArraySize) {
				System.err.println("Array Not Big Enough! " + newElements + "/"
						+ activeListArraySize);

				rebuildNarrowBand();
				return addElements;
			}

			for (int nn = 0; nn < 4; nn++) {
				expandActiveList
						.putArgs(offsetBuffer, activeListBuffer,
								commons.signedLevelSetBuffer,
								newSignedLevelSetBuffer).putArg(activeListSize)
						.putArg(nn).rewind();
				commons.queue.put1DRangeKernel(expandActiveList, 0,
						SpringlsCommon2D.roundToWorkgroupPower(
								1 + (activeListSize / STRIDE), 1), 1);
			}
		}
		activeListSize = newElements;
		commons.queue.finish();
		return addElements;
	}

	/**
	 * Rebuild narrow band.
	 */
	public void rebuildNarrowBand() {
		final CLKernel countActiveList = commons.kernelMap
				.get("countActiveList");
		final CLKernel buildActiveList = commons.kernelMap
				.get("buildActiveList");
		final CLKernel prefixScanList = commons.kernelMap.get("prefixScanList");
		if (maxValueBuffer == null) {
			maxValueBuffer = commons.context.createIntBuffer(1, READ_WRITE,
					USE_BUFFER);
		}

		if (commons.indexBuffer == null) {
			commons.indexBuffer = commons.context.createIntBuffer(commons.rows
					* commons.cols, READ_WRITE, USE_BUFFER);
		}

		if (newSignedLevelSetBuffer == null) {
			newSignedLevelSetBuffer = commons.context.createFloatBuffer(
					commons.rows * commons.cols, READ_WRITE, USE_BUFFER);
			newSignedLevelSetBuffer.getBuffer()
					.put(commons.signedLevelSetBuffer.getBuffer()).rewind();
			commons.signedLevelSetBuffer.getBuffer().rewind();
			commons.queue.putWriteBuffer(newSignedLevelSetBuffer, true);
		}
		final CLKernel initIndexMap = commons.kernelMap.get("initIndexMapNB");

		initIndexMap.setArgs(commons.indexBuffer);

		commons.queue.put1DRangeKernel(
				initIndexMap,
				0,
				SpringlsCommon2D.roundToWorkgroupPower(commons.rows
						* commons.cols), SpringlsCommon2D.WORKGROUP_SIZE);

		CLBuffer<IntBuffer> rebuildOffsetBuffer = commons.context
				.createIntBuffer(commons.cols, READ_WRITE, USE_BUFFER);
		countActiveList.putArgs(rebuildOffsetBuffer,
				commons.signedLevelSetBuffer, newSignedLevelSetBuffer).rewind();

		commons.queue.put1DRangeKernel(countActiveList, 0, SpringlsCommon2D
				.roundToWorkgroupPower(commons.cols,
						SpringlsCommon2D.WORKGROUP_SIZE / 4),
				SpringlsCommon2D.WORKGROUP_SIZE / 4);
		prefixScanList.putArgs(rebuildOffsetBuffer, maxValueBuffer)
				.putArg(commons.cols).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);

		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);
		activeListSize = maxValueBuffer.getBuffer().get(0);
		activeListArraySize = (int) Math.min(
				commons.rows * commons.cols,
				Math.max(activeListSize * 1.25f,
						Math.ceil(commons.rows * commons.cols * 0.1)));

		System.err.println("Rebuilding Narro-Band with [" + activeListSize
				+ "," + activeListArraySize + ","
				+ (commons.rows * commons.cols * 0.1) + "] "
				+ (100 * activeListSize / activeListArraySize)
				+ "% gridPoints.");
		if (activeListBuffer != null) {
			activeListBuffer.release();
		}
		activeListBuffer = commons.context.createIntBuffer(activeListArraySize,
				READ_WRITE, USE_BUFFER);

		buildActiveList.putArgs(rebuildOffsetBuffer, activeListBuffer,
				commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(buildActiveList, 0, SpringlsCommon2D
				.roundToWorkgroupPower(commons.cols,
						SpringlsCommon2D.WORKGROUP_SIZE / 4),
				SpringlsCommon2D.WORKGROUP_SIZE / 4);
		commons.queue.finish();
		rebuildOffsetBuffer.release();
		if (offsetBuffer != null) {
			offsetBuffer.release();
		}
		offsetBuffer = commons.context.createIntBuffer(
				4 * (1 + (activeListArraySize / STRIDE)), USE_BUFFER,
				READ_WRITE);

		if (maxTmpBuffer != null) {
			maxTmpBuffer.release();
		}
		maxTmpBuffer = commons.context.createFloatBuffer(
				1 + (activeListArraySize / STRIDE), READ_WRITE, USE_BUFFER);

		if (tmpActiveBuffer != null) {
			tmpActiveBuffer.release();
		}

		tmpActiveBuffer = commons.context.createIntBuffer(activeListArraySize,
				USE_BUFFER, READ_WRITE);
		(commons).activeListArraySize = activeListArraySize;
		(commons).activeListSize = activeListSize;
		(commons).activeListBuffer = activeListBuffer;
		// saveLevelSet();
	}

	/**
	 * Extend unsigned distance field.
	 * 
	 */
	public void extendSignedDistanceField() {
		commons.kernelMap.get(SpringlsCommon2D.UPDATE_DISTANCE_FIELD);
		final CLKernel copyLevelSet = commons.kernelMap.get("copyLevelSet");

		final CLKernel thresholdDistanceField = commons.kernelMap
				.get("thresholdDistanceField");
		CLBuffer<FloatBuffer> updateBuff = commons.context.createFloatBuffer(
				commons.rows * commons.cols, READ_WRITE, USE_BUFFER);

		thresholdDistanceField
				.putArgs(commons.signedLevelSetBuffer, updateBuff).rewind();
		commons.queue.put1DRangeKernel(thresholdDistanceField, 0, commons.rows
				* commons.cols, SpringlsCommon2D.WORKGROUP_SIZE);

		copyLevelSet.putArgs(updateBuff, commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(copyLevelSet, 0, commons.rows
				* commons.cols, SpringlsCommon2D.WORKGROUP_SIZE);
		updateBuff.release();
	}

	/**
	 * Extend unsigned distance field.
	 * 
	 * @param layers
	 *            the layers
	 */
	public void extendUnsignedDistanceField(int layers) {
		CLKernel extendDistanceField = commons.kernelMap
				.get(SpringlsCommon2D.EXTEND_DISTANCE_FIELD);
		for (int i = 1; i < layers; i++) {
			extendDistanceField.putArgs(commons.unsignedLevelSetBuffer)
					.putArg(i).rewind();
			commons.queue.put1DRangeKernel(extendDistanceField, 0, commons.rows
					* commons.cols, SpringlsCommon2D.WORKGROUP_SIZE);
		}

	}

	/**
	 * Sets the max iterations.
	 * 
	 * @param maxIterations
	 *            the new max iterations
	 */
	public void setMaxIterations(int maxIterations) {
		// Must be multiple of 2!
		this.maxIterations = 2 * (maxIterations / 2);
	}

	/**
	 * Sets the time step.
	 * 
	 * @param stepSize
	 *            the new time step
	 */
	public void setTimeStep(float stepSize) {
		this.stepSize = stepSize;
	}

	/**
	 * Evolve.
	 *
	 * @return the double
	 */
	public void step() {
		final CLKernel evolveLevelSet = (!commons.isPreserveToplogy()) ? commons.kernelMap
				.get(SpringlsCommon2D.EVOLVE_LEVELSET) : commons.kernelMap
				.get(SpringlsCommon2D.EVOLVE_LEVELSET_TOPO);
		final CLKernel updateDistanceField = commons.kernelMap
				.get(SpringlsCommon2D.UPDATE_DISTANCE_FIELD);
		final CLKernel plugLevelSet = commons.kernelMap.get("plugLevelSet");
		final CLKernel copyBuffers = commons.kernelMap.get("copyBuffers");
		int global_size = SpringlsCommon2D
				.roundToWorkgroupPower(activeListSize);
		if (!commons.isPreserveToplogy()) {
			evolveLevelSet
					.putArgs(activeListBuffer, commons.unsignedLevelSetBuffer,
							commons.signedLevelSetBuffer,
							newSignedLevelSetBuffer).putArg(stepSize)
					.putArg(curvatureWeight * stepSize).putArg(activeListSize)
					.rewind();

			commons.queue.put1DRangeKernel(evolveLevelSet, 0, global_size,
					SpringlsCommon2D.WORKGROUP_SIZE);
		} else {
			// Enforce mutual exclusion of neighbors
			for (int ii = 0; ii <= 1; ii++) {
				for (int jj = 0; jj <= 1; jj++) {
					evolveLevelSet
							.putArgs(activeListBuffer,
									commons.unsignedLevelSetBuffer,
									commons.signedLevelSetBuffer,
									newSignedLevelSetBuffer).putArg(1)
							.putArg(stepSize)
							.putArg(curvatureWeight * stepSize)
							.putArg(activeListSize).putArg(ii).putArg(jj)
							.rewind();
					commons.queue.put1DRangeKernel(evolveLevelSet, 0,
							global_size, SpringlsCommon2D.WORKGROUP_SIZE);
				}
			}
		}
		for (int i = 1; i <= MAX_LAYERS + 1; i++) {
			updateDistanceField
					.putArgs(activeListBuffer, commons.signedLevelSetBuffer,
							newSignedLevelSetBuffer).putArg(i)
					.putArg(activeListSize).rewind();
			commons.queue.put1DRangeKernel(updateDistanceField, 0, global_size,
					SpringlsCommon2D.WORKGROUP_SIZE);
		}
		plugLevelSet.putArgs(activeListBuffer, newSignedLevelSetBuffer)
				.putArg(activeListSize).rewind();
		commons.queue.put1DRangeKernel(plugLevelSet, 0, global_size,
				SpringlsCommon2D.WORKGROUP_SIZE);
		copyBuffers
				.putArgs(activeListBuffer, commons.signedLevelSetBuffer,
						newSignedLevelSetBuffer).putArg(activeListSize)
				.rewind();
		commons.queue.put1DRangeKernel(copyBuffers, 0, global_size,
				SpringlsCommon2D.WORKGROUP_SIZE);
	}

	/**
	 * Save level set.
	 */
	private void saveLevelSet() {
		FloatBuffer buff = commons.signedLevelSetBuffer.getBuffer();
		ImageDataFloat distFieldImage = new ImageDataFloat(commons.rows,
				commons.cols);
		float[][] distField = distFieldImage.toArray2d();
		distFieldImage.setName("levelset");
		for (int j = 0; j < commons.cols; j++) {
			for (int i = 0; i < commons.rows; i++) {
				distField[i][j] = buff.get();
			}
		}
		buff.rewind();
		NIFTIReaderWriter.getInstance().write(
				distFieldImage,
				new File("C:\\Users\\Blake\\Desktop\\tracking\\levelset" + time
						+ ".xml"));
		// labelImage.dispose();
		// distFieldImage.dispose();
		commons.queue.putReadBuffer(newSignedLevelSetBuffer, true);
		buff = newSignedLevelSetBuffer.getBuffer();
		distFieldImage = new ImageDataFloat(commons.rows, commons.cols);
		distField = distFieldImage.toArray2d();
		distFieldImage.setName("levelset");
		for (int j = 0; j < commons.cols; j++) {
			for (int i = 0; i < commons.rows; i++) {
				distField[i][j] = buff.get();
			}
		}
		buff.rewind();
		NIFTIReaderWriter.getInstance().write(
				new ImageDataFloat(commons.getUnsignedLevelSet()),
				new File("C:\\Users\\Blake\\Desktop\\tracking\\unsigned" + time
						+ ".xml"));
		/*
		 * ImageDataReaderWriter.getInstance().write( distFieldImage, new File(
		 * "C:\\Users\\Blake\\Desktop\\multiobject\\opencl\\new_levelset" + time
		 * + ".xml"));
		 */
		// labelImage.dispose();
		// distFieldImage.dispose();
		commons.queue.putReadBuffer(activeListBuffer, true);
		IntBuffer buff2 = activeListBuffer.getBuffer();
		distFieldImage = new ImageDataFloat(commons.rows, commons.cols);
		distField = distFieldImage.toArray2d();
		distFieldImage.setName("narrowband");

		FloatBuffer buff3 = commons.signedLevelSetBuffer.getBuffer();
		for (int j = 0; j < commons.cols; j++) {
			for (int i = 0; i < commons.rows; i++) {
				distField[i][j] = 5 * Math.signum(buff3.get());
			}
		}
		buff3.rewind();
		int count = 0;
		while (buff2.hasRemaining()) {
			int index = buff2.get();
			int j = index / commons.rows;
			int i = index - j * commons.rows;
			distField[i][j] = Math.round(buff3.get(index));
			count++;
			if (count >= activeListSize) {
				break;
			}
		}
		buff2.rewind();
		buff3.rewind();

		NIFTIReaderWriter.getInstance().write(
				distFieldImage,
				new File("C:\\Users\\Blake\\Desktop\\tracking\\narrowband"
						+ time + ".xml"));

	}
}
