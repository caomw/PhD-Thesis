/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
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
package org.imagesci.muscle;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;


import java.nio.FloatBuffer;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsConstants;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;


// TODO: Auto-generated Javadoc
/**
 * The Class MuscleAdvectNoResampleACWE3D.
 */
public class MuscleAdvectNoResampleACWE3D extends MuscleAdvectACWE3D {

	/**
	 * Instantiates a new muscle advect no resample acw e3 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 * @param pressureImage the pressure image
	 * @param pressureWeight the pressure weight
	 */
	public MuscleAdvectNoResampleACWE3D(SpringlsCommon3D commons,
			MOGAC3D mogac, float[][][] pressureImage, float pressureWeight) {
		super(commons, mogac, pressureImage, pressureWeight);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.muscle.MuscleAdvectACWE3D#advect(double)
	 */
	@Override
	public double advect(double timeStep) {
		final CLKernel regionAverage = mogac.kernelMap.get("regionAverage");
		final CLKernel sumAverages = mogac.kernelMap.get("sumAverages");
		if (intensityEstimation && time % estimationInterval == 0) {
			regionAverage.putArgs(mogac.imageLabelBuffer,
					mogac.distanceFieldBuffer, pressureImageBuffer,
					mogac.labelMaskBuffer, averages, areas, stddev).rewind();
			commons.queue.put1DRangeKernel(regionAverage, 0,
					SpringlsCommon3D.roundToWorkgroupPower(commons.slices),
					SpringlsCommon3D.WORKGROUP_SIZE);
			commons.queue.finish();
			sumAverages.putArgs(averages, areas, stddev).rewind();
			commons.queue.put1DRangeKernel(sumAverages, 0, SpringlsCommon3D
					.roundToWorkgroupPower(mogac.getNumLabels()),
					SpringlsCommon3D.WORKGROUP_SIZE);
		}
		time++;
		CLBuffer<FloatBuffer> pointUpdates = commons.context.createFloatBuffer(
				commons.elements * 9, READ_WRITE, USE_BUFFER);
		CLBuffer<FloatBuffer> maxForces = commons.context.createFloatBuffer(
				(3 * commons.arrayLength / SpringlsCommon3D.STRIDE),
				READ_WRITE, USE_BUFFER);
		final CLKernel applyForces = (commons.topologyRuleBuffer == null) ? commons.kernelMap
				.get("applyForcesNoResampleMogac") : null;

		final CLKernel computeMaxForces = commons.kernelMap
				.get(SpringlsCommon3D.COMPUTE_MAX_FORCES);

		// Compute forces applied to each particle and store them in an
		// array.

		final CLKernel computeForces = commons.kernelMap
				.get("computeAdvectionForcesNoResampleACWE");
		computeForces
				.putArgs(commons.capsuleBuffer, pressureImageBuffer,
						commons.springlLabelBuffer, mogac.distanceFieldBuffer,
						mogac.imageLabelBuffer, pointUpdates, averages)
				.putArg(pressureWeight).putArg(commons.elements).rewind();

		commons.queue.put1DRangeKernel(computeForces, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		computeMaxForces.putArgs(pointUpdates, maxForces)
				.putArg(SpringlsCommon3D.STRIDE).putArg(3 * commons.elements)
				.rewind();
		// Find the maximum force in order to choose an appropriate step
		// size.
		commons.queue.put1DRangeKernel(
				computeMaxForces,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(3 * commons.arrayLength
						/ SpringlsCommon3D.STRIDE),
				SpringlsCommon3D.WORKGROUP_SIZE);

		commons.queue.putReadBuffer(maxForces, true);
		FloatBuffer buff = maxForces.getBuffer();
		float maxForce = 0;
		while (buff.hasRemaining()) {
			maxForce = Math.max(maxForce, buff.get());
		}

		if (maxForce < 1E-3) {
			pointUpdates.release();
			maxForces.release();
			return 0;
		}
		// Rescale forces and update the position of all paritcles.
		double displacement = (Math.sqrt(maxForce));
		maxForce = (float) (0.5f * SpringlsConstants.scaleDown
				* SpringlsConstants.vExtent / displacement);
		applyForces
				.putArgs(commons.capsuleBuffer, mogac.distanceFieldBuffer,
						mogac.imageLabelBuffer, commons.springlLabelBuffer,
						pointUpdates).putArg(maxForce).putArg(commons.elements)
				.rewind();
		commons.queue.put1DRangeKernel(applyForces, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
		maxForces.release();
		pointUpdates.release();
		return displacement;
	}
}
