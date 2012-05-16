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

import java.nio.IntBuffer;

import org.imagesci.springls.SpringlsCommon3D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleEvolveLevelSet3D tracks a springls constellation with MOGAC.
 */
public class MuscleEvolveLevelSet3D extends WEMOGAC3D {

	/** The commons. */
	protected SpringlsCommon3D commons;
	/** The step size. */
	protected float stepSize = 0.75f;

	/**
	 * Instantiates a new muscle evolve level set 3d.
	 * 
	 * @param commons
	 *            the commons
	 * @param refImage
	 *            the reference image
	 * @param curvatureWeight
	 *            the curvature weight
	 */
	public MuscleEvolveLevelSet3D(SpringlsCommon3D commons, ImageData refImage,
			float curvatureWeight) {
		super(refImage, commons.context, commons.queue);
		this.commons = commons;
		this.stepSize = 2.0f;
		this.curvatureWeight = curvatureWeight;
		this.maxIterations = 4;
		this.clampSpeed = true;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC3D#evolve()
	 */
	@Override
	public double evolve() {
		long startTime;
		commons.setActiveSetValid(false);
		for (int i = 0; i < maxIterations && activeListSize > 0; i++) {
			startTime = System.nanoTime();

			this.step();
			stepElapsedTime += (System.nanoTime() - startTime);
			startTime = System.nanoTime();
			deleteElements();
			startTime = System.nanoTime();
			addElements();
			time++;
		}
		(commons).activeListArraySize = activeListArraySize;
		(commons).activeListSize = activeListSize;
		(commons).activeListBuffer = activeListBuffer;
		/*
		 * if (time % (4*maxIterations) == 0) saveLevelSet();
		 */
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC3D#evolve(boolean)
	 */
	@Override
	public double evolve(boolean checkConvergence) {
		long startTime;
		for (int i = 0; i < maxIterations && activeListSize > 0; i++) {
			startTime = System.nanoTime();
			this.step();
			stepElapsedTime += (System.nanoTime() - startTime);
			startTime = System.nanoTime();
			deleteElements();
			startTime = System.nanoTime();
			addElements();
			time++;
		}
		(commons).activeListArraySize = activeListArraySize;
		(commons).activeListSize = activeListSize;
		(commons).activeListBuffer = activeListBuffer;

		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC3D#deleteElements()
	 */
	@Override
	protected int deleteElements() {
		final CLKernel deleteCountActiveList = commons.kernelMap
				.get("deleteCountActiveListMogac");
		final CLKernel deleteCountActiveListHistory = commons.kernelMap
				.get("deleteCountActiveListHistoryMogac");
		final CLKernel prefixScanList = commons.kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = commons.kernelMap
				.get("compactActiveList");
		final CLKernel compactActiveListHistory = kernelMap
				.get("compactActiveListHistory");

		if (useAdaptiveActiveSet
				&& time % sampling_interval == sampling_interval - 1) {

			deleteCountActiveListHistory
					.putArgs(offsetBuffer, activeListBuffer,
							oldDistanceFieldBuffer, commons.indexBuffer,
							commons.unsignedLevelSetBuffer, historyBuffer)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(deleteCountActiveListHistory, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		} else {

			deleteCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							oldDistanceFieldBuffer, commons.indexBuffer,
							commons.unsignedLevelSetBuffer)
					.putArg(activeListSize).rewind();
			commons.queue.put1DRangeKernel(deleteCountActiveList, 0,
					1 + (activeListSize / STRIDE), 1);
		}

		commons.queue.finish();
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);

		int newElements = maxValueBuffer.getBuffer().get(0);

		int delete = activeListSize - newElements;

		System.nanoTime();

		if ((useAdaptiveActiveSet) || newElements != activeListSize) {

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

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC3D#rebuildNarrowBand()
	 */
	@Override
	public void rebuildNarrowBand() {
		super.rebuildNarrowBand();
		(commons).activeListArraySize = activeListArraySize;
		(commons).activeListSize = activeListSize;
		(commons).activeListBuffer = activeListBuffer;
		if (commons.indexBuffer == null) {
			commons.indexBuffer = commons.context.createIntBuffer(commons.rows
					* commons.cols * commons.slices, READ_WRITE, USE_BUFFER);
		}
		final CLKernel initIndexMap = commons.kernelMap.get("initIndexMapNB");
		initIndexMap.setArgs(commons.indexBuffer);
		commons.queue.put1DRangeKernel(
				initIndexMap,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC3D#step()
	 */
	@Override
	public boolean step() {

		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");
		int global_size = roundToWorkgroupPower(activeListSize);
		final CLKernel gradientSpeedKernel = kernelMap
				.get("gradientSpeedKernel");

		gradientSpeedKernel
				.putArgs(activeListBuffer, commons.unsignedLevelSetBuffer,
						oldDistanceFieldBuffer, oldImageLabelBuffer,
						deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
						forceIndexesBuffer).putArg(stepSize)
				.putArg(curvatureWeight).putArg(activeListSize).rewind();
		queue.put1DRangeKernel(gradientSpeedKernel, 0, global_size,
				WORKGROUP_SIZE);

		if (topologyPreservation) {
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

		} else {

			applyForces
					.putArgs(activeListBuffer, oldDistanceFieldBuffer,
							oldImageLabelBuffer, deltaLevelSetBuffer, idBuffer,
							distanceFieldBuffer, imageLabelBuffer).putArg(0.5f)
					.putArg(activeListSize).rewind();

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
			} else if ((time) % sampling_interval == sampling_interval - 1) {
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
				final CLKernel markStaticSpringls = commons.kernelMap
						.get("markStaticSpringlsMogac");
				markStaticSpringls
						.putArgs(commons.capsuleBuffer, historyBuffer)
						.putArg(commons.elements).rewind();
				commons.queue.put1DRangeKernel(markStaticSpringls, 0,
						commons.arrayLength, SpringlsCommon3D.WORKGROUP_SIZE);
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
		return true;
	}

	/**
	 * Extend distance field.
	 *
	 * @param layers the layers
	 */
	public void extendDistanceField(int layers) {
		CLKernel extendDistanceField = commons.kernelMap
				.get(SpringlsCommon3D.EXTEND_DISTANCE_FIELD);
		for (int i = MAX_LAYERS - 1; i < layers; i++) {
			extendDistanceField.putArgs(distanceFieldBuffer).putArg(i).rewind();
			commons.queue.put1DRangeKernel(extendDistanceField, 0, commons.rows
					* commons.cols * commons.slices,
					SpringlsCommon3D.WORKGROUP_SIZE);
		}
	}
}
