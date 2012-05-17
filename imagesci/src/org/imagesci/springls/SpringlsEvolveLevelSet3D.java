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
package org.imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.io.File;
import java.nio.ByteBuffer;
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
public class SpringlsEvolveLevelSet3D {

	/** The Constant MAX_DISTANCE. */
	public static final float MAX_DISTANCE = 3.5f;
	/** The Constant MAX_LAYERS. */
	public static final int MAX_LAYERS = 3;

	/** The Constant STRIDE. */
	public static final int STRIDE = SpringlsCommon3D.STRIDE;

	/** The active list array size. */
	protected int activeListArraySize;

	/** The active list buffer. */
	protected CLBuffer<IntBuffer> activeListBuffer;

	/** The active list size. */
	protected int activeListSize;

	/** The add elapsed time. */
	protected long addElapsedTime = 0;
	/** The commons. */
	protected SpringlsCommon3D commons;

	/** The compact elapsed time. */
	protected long compactElapsedTime = 0;
	/** The curvature weight. */
	protected float curvatureWeight = 0.25f;

	/** The delete elapsed time. */
	protected long deleteElapsedTime = 0;
	/** The dice bins. */
	CLBuffer<IntBuffer> diceBins = null;

	/** The history buffer. */
	protected CLBuffer<ByteBuffer> historyBuffer = null;
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

	/** The sampling_interval. */
	protected int sampling_interval = 16;

	/** The step elapsed time. */
	protected long stepElapsedTime = 0;

	/** The step size. */
	protected float stepSize = 0.75f;

	/** The time. */
	protected int time = 0;

	/** The tmp active buffer. */
	protected CLBuffer<IntBuffer> tmpActiveBuffer = null;

	/** The use adaptive active set. */
	protected boolean useAdaptiveActiveSet = false;

	/**
	 * Instantiates a new springls evolve level set.
	 * 
	 * @param commons
	 *            the commons
	 * @param curvatureWeight
	 *            the curvature weight
	 */
	public SpringlsEvolveLevelSet3D(SpringlsCommon3D commons,
			float curvatureWeight) {
		this.commons = commons;
		this.curvatureWeight = curvatureWeight;
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
	public SpringlsEvolveLevelSet3D(SpringlsCommon3D commons,
			float[][][] imageMat, float curvatureWeight) {
		this.commons = commons;
		this.curvatureWeight = curvatureWeight;
		FloatBuffer levelSet = commons.signedLevelSetBuffer.getBuffer();
		FloatBuffer originalLevelSet = commons.originalUnsignedLevelSetBuffer
				.getBuffer();
		for (int k = 0; k < commons.slices; k++) {
			for (int j = 0; j < commons.cols; j++) {
				for (int i = 0; i < commons.rows; i++) {
					float val = imageMat[i][j][k];
					levelSet.put(val);
					originalLevelSet.put(Math.abs(val));
				}
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
	 * @param checkConvergence
	 *            the check convergence
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
		final CLKernel deleteCountActiveListHistory = commons.kernelMap
				.get("deleteCountActiveListHistory");
		final CLKernel compactActiveListHistory = commons.kernelMap
				.get("compactActiveListHistory");
		final CLKernel prefixScanList = commons.kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = commons.kernelMap
				.get("compactActiveList");

		if (useAdaptiveActiveSet
				&& time % sampling_interval == sampling_interval - 1) {
			deleteCountActiveListHistory
					.putArgs(offsetBuffer, activeListBuffer,
							commons.signedLevelSetBuffer, commons.indexBuffer,
							commons.unsignedLevelSetBuffer, historyBuffer)
					.putArg(activeListSize).rewind();
			commons.queue.put1DRangeKernel(deleteCountActiveListHistory, 0,
					SpringlsCommon3D.roundToWorkgroupPower(
							1 + (activeListSize / STRIDE), 1), 1);
		} else {

			deleteCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							commons.signedLevelSetBuffer, commons.indexBuffer,
							commons.unsignedLevelSetBuffer)
					.putArg(activeListSize).rewind();
			commons.queue.put1DRangeKernel(deleteCountActiveList, 0,
					SpringlsCommon3D.roundToWorkgroupPower(
							1 + (activeListSize / STRIDE), 1), 1);
		}
		commons.queue.finish();
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);
		int newElements = maxValueBuffer.getBuffer().get(0);
		int delete = activeListSize - newElements;

		long startTime = System.nanoTime();
		if (newElements != activeListSize) {

			if (useAdaptiveActiveSet && time > sampling_interval
					&& time % sampling_interval == sampling_interval - 1) {
				compactActiveListHistory
						.putArgs(offsetBuffer, activeListBuffer,
								tmpActiveBuffer, commons.signedLevelSetBuffer,
								newSignedLevelSetBuffer, historyBuffer)
						.putArg(activeListSize).rewind();

				commons.queue.put1DRangeKernel(compactActiveListHistory, 0,
						SpringlsCommon3D.roundToWorkgroupPower(
								1 + (activeListSize / STRIDE), 1), 1);
			} else {

				compactActiveList
						.putArgs(offsetBuffer, activeListBuffer,
								tmpActiveBuffer, commons.signedLevelSetBuffer,
								newSignedLevelSetBuffer).putArg(activeListSize)
						.rewind();

				commons.queue.put1DRangeKernel(compactActiveList, 0,
						SpringlsCommon3D.roundToWorkgroupPower(
								1 + (activeListSize / STRIDE), 1), 1);

			}
			activeListSize = newElements;
			commons.queue.finish();
			CLBuffer<IntBuffer> tmp = activeListBuffer;
			activeListBuffer = tmpActiveBuffer;
			tmpActiveBuffer = tmp;

		}

		compactElapsedTime += System.nanoTime() - startTime;
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
		for (int nn = 0; nn < 6; nn++) {
			addCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							newSignedLevelSetBuffer).putArg(activeListSize)
					.putArg(nn).rewind();
			commons.queue.put1DRangeKernel(addCountActiveList, 0,
					SpringlsCommon3D.roundToWorkgroupPower(
							1 + (activeListSize / STRIDE), 1), 1);
		}
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(6 * (1 + (activeListSize / STRIDE))).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);
		int addElements = maxValueBuffer.getBuffer().get(0);
		int newElements = addElements + activeListSize;
		// System.out.println(activeListSize + " ADDED " + addElements);
		if (newElements != activeListSize) {
			if (newElements > activeListArraySize) {
				rebuildNarrowBand();
				return addElements;
			}

			for (int nn = 0; nn < 6; nn++) {
				expandActiveList
						.putArgs(offsetBuffer, activeListBuffer,
								commons.signedLevelSetBuffer,
								newSignedLevelSetBuffer).putArg(activeListSize)
						.putArg(nn).rewind();
				commons.queue.put1DRangeKernel(expandActiveList, 0,
						SpringlsCommon3D.roundToWorkgroupPower(
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
		if (historyBuffer == null && useAdaptiveActiveSet) {
			historyBuffer = commons.context.createByteBuffer(commons.rows
					* commons.cols * commons.slices, READ_WRITE, USE_BUFFER);
		}
		if (commons.indexBuffer == null) {
			commons.indexBuffer = commons.context.createIntBuffer(commons.rows
					* commons.cols * commons.slices, READ_WRITE, USE_BUFFER);
		}
		if (newSignedLevelSetBuffer == null) {
			newSignedLevelSetBuffer = commons.context.createFloatBuffer(
					commons.rows * commons.cols * commons.slices, READ_WRITE,
					USE_BUFFER);
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
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
		CLBuffer<IntBuffer> rebuildOffsetBuffer = commons.context
				.createIntBuffer(commons.slices, READ_WRITE, USE_BUFFER);
		countActiveList.putArgs(rebuildOffsetBuffer,
				commons.signedLevelSetBuffer, newSignedLevelSetBuffer).rewind();

		commons.queue.put1DRangeKernel(countActiveList, 0, SpringlsCommon3D
				.roundToWorkgroupPower(commons.slices,
						SpringlsCommon3D.WORKGROUP_SIZE / 4),
				SpringlsCommon3D.WORKGROUP_SIZE / 4);
		prefixScanList.putArgs(rebuildOffsetBuffer, maxValueBuffer)
				.putArg(commons.slices).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);

		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);
		activeListSize = maxValueBuffer.getBuffer().get(0);
		activeListArraySize = (int) Math.min(
				commons.rows * commons.cols * commons.slices,
				Math.max(
						activeListSize * 1.25f,
						Math.ceil(commons.rows * commons.cols * commons.slices
								* 0.1)));

		System.err.println("Rebuilding Narro-Band with [" + activeListSize
				+ "," + activeListArraySize + ","
				+ (commons.rows * commons.cols * commons.slices * 0.1) + "] "
				+ (100 * activeListSize / activeListArraySize)
				+ "% gridPoints.");
		if (activeListBuffer != null) {
			activeListBuffer.release();
		}
		activeListBuffer = commons.context.createIntBuffer(activeListArraySize,
				READ_WRITE, USE_BUFFER);

		buildActiveList.putArgs(rebuildOffsetBuffer, activeListBuffer,
				commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(buildActiveList, 0, SpringlsCommon3D
				.roundToWorkgroupPower(commons.slices,
						SpringlsCommon3D.WORKGROUP_SIZE / 4),
				SpringlsCommon3D.WORKGROUP_SIZE / 4);
		commons.queue.finish();
		rebuildOffsetBuffer.release();
		if (offsetBuffer != null) {
			offsetBuffer.release();
		}
		offsetBuffer = commons.context.createIntBuffer(
				6 * (1 + (activeListArraySize / STRIDE)), USE_BUFFER,
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

	}

	/**
	 * Extend unsigned distance field.
	 * 
	 * @param layers
	 *            the layers
	 */
	public void extendSignedDistanceField(int layers) {
		CLKernel extendDistanceField = commons.kernelMap
				.get("extendSignedDistanceField");
		for (int i = MAX_LAYERS - 1; i < layers; i++) {
			extendDistanceField.putArgs(commons.signedLevelSetBuffer).putArg(i)
					.rewind();
			commons.queue.put1DRangeKernel(
					extendDistanceField,
					0,
					SpringlsCommon3D.roundToWorkgroupPower(commons.rows
							* commons.cols * commons.slices),
					SpringlsCommon3D.WORKGROUP_SIZE);
		}
		commons.queue.finish();
	}

	/**
	 * Extend unsigned distance field.
	 * 
	 * @param layers
	 *            the layers
	 */
	public void extendUnsignedDistanceField(int layers) {
		CLKernel extendDistanceField = commons.kernelMap
				.get(SpringlsCommon3D.EXTEND_DISTANCE_FIELD);
		for (int i = 0; i < layers; i++) {
			extendDistanceField.putArgs(commons.unsignedLevelSetBuffer)
					.putArg(i).rewind();
			commons.queue.put1DRangeKernel(
					extendDistanceField,
					0,
					SpringlsCommon3D.roundToWorkgroupPower(commons.rows
							* commons.cols * commons.slices),
					SpringlsCommon3D.WORKGROUP_SIZE);
		}
		commons.queue.finish();
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
	 * Step.
	 */
	public void step() {

		CLKernel evolveLevelSet = (commons.topologyRuleBuffer == null) ? commons.kernelMap
				.get(SpringlsCommon3D.EVOLVE_LEVELSET) : commons.kernelMap
				.get(SpringlsCommon3D.EVOLVE_LEVELSET_TOPO);
		CLKernel updateDistanceField = commons.kernelMap
				.get(SpringlsCommon3D.UPDATE_DISTANCE_FIELD);
		final CLKernel copyBuffers = commons.kernelMap.get("copyBuffers");
		int global_size = SpringlsCommon3D
				.roundToWorkgroupPower(activeListSize);
		if (commons.topologyRuleBuffer != null) {
			for (int nn = 0; nn < 8; nn++) {
				evolveLevelSet
						.putArgs(activeListBuffer,
								commons.unsignedLevelSetBuffer,
								commons.signedLevelSetBuffer,
								newSignedLevelSetBuffer,
								commons.topologyRuleBuffer)
						.putArg(commons.flip ? -1 : 1).putArg(stepSize)
						.putArg(curvatureWeight * stepSize)
						.putArg(activeListSize).putArg(nn).rewind();
				commons.queue.put1DRangeKernel(evolveLevelSet, 0, global_size,
						SpringlsCommon3D.WORKGROUP_SIZE);
			}
		} else {
			evolveLevelSet
					.putArgs(activeListBuffer, commons.unsignedLevelSetBuffer,
							commons.signedLevelSetBuffer,
							newSignedLevelSetBuffer).putArg(stepSize)
					.putArg(curvatureWeight * stepSize).putArg(activeListSize)
					.rewind();
			commons.queue.put1DRangeKernel(evolveLevelSet, 0, global_size,
					SpringlsCommon3D.WORKGROUP_SIZE);
		}
		for (int i = 1; i <= MAX_LAYERS; i++) {
			updateDistanceField
					.putArgs(activeListBuffer, commons.signedLevelSetBuffer,
							newSignedLevelSetBuffer).putArg(i)
					.putArg(activeListSize).rewind();
			commons.queue.put1DRangeKernel(updateDistanceField, 0, global_size,
					SpringlsCommon3D.WORKGROUP_SIZE);
		}

		final CLKernel plugLevelSet = commons.kernelMap.get("plugLevelSet");
		plugLevelSet.putArgs(activeListBuffer, newSignedLevelSetBuffer)
				.putArg(activeListSize).rewind();
		commons.queue.put1DRangeKernel(plugLevelSet, 0, global_size,
				SpringlsCommon3D.WORKGROUP_SIZE);

		if (useAdaptiveActiveSet) {
			copyBuffers
					.putArgs(activeListBuffer, commons.signedLevelSetBuffer,
							newSignedLevelSetBuffer).putArg(activeListSize)
					.rewind();
			commons.queue.put1DRangeKernel(copyBuffers, 0, global_size,
					SpringlsCommon3D.WORKGROUP_SIZE);
			final CLKernel rememberImageLabels = commons.kernelMap
					.get("rememberImageLabels");
			final CLKernel diffImageLabels = commons.kernelMap
					.get("diffImageLabels");
			if ((time) % sampling_interval == 0) {
				rememberImageLabels.putArgs(newSignedLevelSetBuffer,
						historyBuffer).rewind();
				commons.queue.put1DRangeKernel(
						rememberImageLabels,
						0,
						SpringlsCommon3D.roundToWorkgroupPower(commons.rows
								* commons.cols * commons.slices),
						SpringlsCommon3D.WORKGROUP_SIZE);
			} else if ((time) % sampling_interval == sampling_interval - 1) {

				diffImageLabels.putArgs(newSignedLevelSetBuffer, historyBuffer)
						.rewind();

				commons.queue.put1DRangeKernel(
						diffImageLabels,
						0,
						SpringlsCommon3D.roundToWorkgroupPower(commons.rows
								* commons.cols * commons.slices),
						SpringlsCommon3D.WORKGROUP_SIZE);
				final CLKernel dilateLabels = commons.kernelMap
						.get("dilateLabels");

				for (int cycle = 0; cycle < 4; cycle++) {
					for (int kk = 0; kk < 8; kk++) {
						dilateLabels.putArgs(activeListBuffer, historyBuffer)
								.putArg(activeListSize).putArg(kk).rewind();
						commons.queue.put1DRangeKernel(dilateLabels, 0,
								global_size, SpringlsCommon3D.WORKGROUP_SIZE);
					}
				}
				final CLKernel markStaticSpringls = commons.kernelMap
						.get("markStaticSpringls");
				markStaticSpringls
						.putArgs(commons.capsuleBuffer, historyBuffer)
						.putArg(commons.elements).rewind();
				commons.queue.put1DRangeKernel(markStaticSpringls, 0,
						commons.arrayLength, SpringlsCommon3D.WORKGROUP_SIZE);

			}
		} else {
			copyBuffers
					.putArgs(activeListBuffer, commons.signedLevelSetBuffer,
							newSignedLevelSetBuffer).putArg(activeListSize)
					.rewind();
			commons.queue.put1DRangeKernel(copyBuffers, 0, global_size,
					SpringlsCommon3D.WORKGROUP_SIZE);
		}

	}

}
