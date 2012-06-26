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
package org.imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsAdvect moves springls with a pressure force and vector
 * field.
 */
public class SpringlsAdvectNoResample3D extends SpringlsAdvect3D {

	/**
	 * Instantiates a new springls advect no resample3 d.
	 *
	 * @param commons the commons
	 */
	public SpringlsAdvectNoResample3D(SpringlsCommon3D commons) {
		super(commons);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new springls advect no resample3 d.
	 *
	 * @param commons the commons
	 * @param pressureBuffer the pressure buffer
	 * @param advectBuffer the advect buffer
	 * @param pressureWeight the pressure weight
	 * @param advectionWeight the advection weight
	 */
	public SpringlsAdvectNoResample3D(SpringlsCommon3D commons,
			CLBuffer<FloatBuffer> pressureBuffer,
			CLBuffer<FloatBuffer> advectBuffer, float pressureWeight,
			float advectionWeight) {
		super(commons, pressureBuffer, advectBuffer, pressureWeight,
				advectionWeight);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new springls advect no resample3 d.
	 *
	 * @param commons the commons
	 * @param pressureImage the pressure image
	 * @param pressureWeight the pressure weight
	 */
	public SpringlsAdvectNoResample3D(SpringlsCommon3D commons,
			float[][][] pressureImage, float pressureWeight) {
		super(commons, pressureImage, pressureWeight);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new springls advect no resample3 d.
	 *
	 * @param commons the commons
	 * @param pressureImage the pressure image
	 * @param vecFieldImage the vec field image
	 * @param pressureWeight the pressure weight
	 * @param advectionWeight the advection weight
	 */
	public SpringlsAdvectNoResample3D(SpringlsCommon3D commons,
			float[][][] pressureImage, float[][][][] vecFieldImage,
			float pressureWeight, float advectionWeight) {
		super(commons, pressureImage, vecFieldImage, pressureWeight,
				advectionWeight);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new springls advect no resample3 d.
	 *
	 * @param commons the commons
	 * @param vecFieldImage the vec field image
	 * @param advectionWeight the advection weight
	 */
	public SpringlsAdvectNoResample3D(SpringlsCommon3D commons,
			float[][][][] vecFieldImage, float advectionWeight) {
		super(commons, vecFieldImage, advectionWeight);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Advect.
	 * 
	 * @param timeStep
	 *            the time step
	 * @return the double
	 */
	@Override
	public double advect(double timeStep) {
		CLBuffer<FloatBuffer> pointUpdates = commons.context.createFloatBuffer(
				commons.elements * 9, READ_WRITE, USE_BUFFER);
		CLBuffer<FloatBuffer> maxForces = commons.context.createFloatBuffer(
				(3 * commons.arrayLength / SpringlsCommon3D.STRIDE) + 1,
				READ_WRITE, USE_BUFFER);
		// Topology Preservation not implemented
		final CLKernel applyForces = commons.kernelMap
				.get("applyForcesNoResample");

		CLKernel computeMaxForces = commons.kernelMap
				.get(SpringlsCommon3D.COMPUTE_MAX_FORCES);
		CLKernel computeForces;
		// Compute forces applied to each particle and store them in an array.
		if (pressureImageBuffer != null) {
			if (vecFieldImageBuffer != null) {
				computeForces = commons.kernelMap
						.get(SpringlsCommon3D.COMPUTE_FORCES_PRESSURE_VECFIELD);
				computeForces
						.putArgs(commons.capsuleBuffer, pressureImageBuffer,
								vecFieldImageBuffer,
								commons.signedLevelSetBuffer, pointUpdates)
						.putArg(pressureWeight).putArg(advectionWeight)
						.putArg(commons.elements).rewind();
			} else {
				computeForces = commons.kernelMap
						.get("computeAdvectionForcesNoResampleP");
				computeForces
						.putArgs(commons.capsuleBuffer, pressureImageBuffer,
								commons.signedLevelSetBuffer, pointUpdates)
						.putArg(pressureWeight).putArg(commons.elements)
						.rewind();
			}
		} else {
			computeForces = commons.kernelMap
					.get(SpringlsCommon3D.COMPUTE_FORCES_VECFIELD);
			computeForces
					.putArgs(commons.capsuleBuffer, vecFieldImageBuffer,
							pointUpdates).putArg(advectionWeight)
					.putArg(commons.elements).rewind();
		}

		commons.queue.put1DRangeKernel(computeForces, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		computeMaxForces.putArgs(pointUpdates, maxForces)
				.putArg(SpringlsCommon3D.STRIDE).putArg(3 * commons.elements)
				.rewind();
		// Find the maximum force in order to choose an appropriate step size.
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
				.putArgs(commons.capsuleBuffer, commons.signedLevelSetBuffer,
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
