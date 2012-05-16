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

import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsCommon3D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleEvolveLevelSet2D tracks a springls constellation with MOGAC.
 */
public class MuscleEvolveLevelSet2D extends WEMOGAC2D {
	
	/** The commons. */
	protected SpringlsCommon2D commons;

	/** The step size. */
	protected float stepSize = 2.0f;

	/**
	 * Instantiates a new muscle evolve level set2 d.
	 *
	 * @param refImage the reference image
	 * @param type the type
	 */
	public MuscleEvolveLevelSet2D(ImageData refImage, Type type) {
		super(refImage, type);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new muscle evolve level set 2d.
	 *
	 * @param commons the commons
	 * @param refImage the reference image
	 * @param curvatureWeight the curvature weight
	 */
	public MuscleEvolveLevelSet2D(SpringlsCommon2D commons, ImageData refImage,
			float curvatureWeight) {
		super(commons.rows, commons.cols, commons.context, commons.queue);
		this.image = refImage;
		this.commons = commons;
		this.stepSize = 2.0f;
		this.maxIterations = 4;
		this.clampSpeed = true;

		setCurvatureWeight(curvatureWeight);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Evolve.
	 *
	 * @return the double
	 */
	public double evolve() {
		commons.setActiveSetValid(false);
		for (int i = 0; i < maxIterations && activeListSize > 0; i++) {
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
		for (int i = 0; i < maxIterations && activeListSize > 0; i++) {
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

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC2D#deleteElements()
	 */
	@Override
	protected void deleteElements() {
		final CLKernel deleteCountActiveList = commons.kernelMap
				.get("deleteCountActiveListMogac");

		final CLKernel prefixScanList = commons.kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = commons.kernelMap
				.get("compactActiveList");
		deleteCountActiveList
				.putArgs(offsetBuffer, activeListBuffer,
						oldDistanceFieldBuffer, commons.indexBuffer,
						commons.unsignedLevelSetBuffer).putArg(activeListSize)
				.rewind();
		commons.queue.put1DRangeKernel(deleteCountActiveList, 0,
				1 + (activeListSize / STRIDE), 1);

		commons.queue.finish();
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		commons.queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		commons.queue.finish();
		commons.queue.putReadBuffer(maxValueBuffer, true);

		int newElements = maxValueBuffer.getBuffer().get(0);

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

	/**
	 * Extend distance field.
	 *
	 * @param distFieldExtend the distance field extend
	 */
	public void extendDistanceField(int distFieldExtend) {
		CLKernel extendDistanceField = commons.kernelMap
				.get(SpringlsCommon3D.EXTEND_DISTANCE_FIELD);
		for (int i = 0; i < distFieldExtend; i++) {
			extendDistanceField.putArgs(commons.unsignedLevelSetBuffer)
					.putArg(i).rewind();
			commons.queue.put1DRangeKernel(extendDistanceField, 0, commons.rows
					* commons.cols, SpringlsCommon3D.WORKGROUP_SIZE);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC2D#rebuildNarrowBand()
	 */
	@Override
	public void rebuildNarrowBand() {
		super.rebuildNarrowBand();
		(commons).activeListArraySize = activeListArraySize;
		(commons).activeListSize = activeListSize;
		(commons).activeListBuffer = activeListBuffer;
		if (commons.indexBuffer == null) {
			commons.indexBuffer = commons.context.createIntBuffer(commons.rows
					* commons.cols, READ_WRITE, USE_BUFFER);
		}
		final CLKernel initIndexMap = commons.kernelMap.get("initIndexMap");
		initIndexMap.setArgs(commons.indexBuffer);
		commons.queue.put1DRangeKernel(initIndexMap, 0, commons.rows
				* commons.cols, SpringlsCommon2D.WORKGROUP_SIZE);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.WEMOGAC2D#step()
	 */
	@Override
	public boolean step() {
		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		final CLKernel gradientSpeedKernel = kernelMap
				.get("gradientSpeedKernel");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");
		int global_size = roundToWorkgroupPower(activeListSize);

		gradientSpeedKernel
				.putArgs(activeListBuffer, commons.unsignedLevelSetBuffer,
						oldDistanceFieldBuffer, oldImageLabelBuffer,
						deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
						forceIndexesBuffer).putArg(stepSize)
				.putArg(curvatureWeight).putArg(activeListSize).rewind();
		queue.put1DRangeKernel(gradientSpeedKernel, 0, global_size,
				WORKGROUP_SIZE);
		if (topologyPreservation) {

			for (int nn = 0; nn < 4; nn++) {
				applyForces
						.putArgs(activeListBuffer, oldDistanceFieldBuffer,
								oldImageLabelBuffer, deltaLevelSetBuffer,
								idBuffer, distanceFieldBuffer, imageLabelBuffer)
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
		return true;
	}
}
