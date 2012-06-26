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


import java.io.IOException;
import java.nio.FloatBuffer;

import org.imagesci.mogac.MOGAC2D;
import org.imagesci.springls.SpringlsAdvect2D;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsConstants;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;


// TODO: Auto-generated Javadoc
/**
 * The Class MuscleAdvect2D.
 */
public class MuscleAdvect2D extends SpringlsAdvect2D {
	
	/** The mogac. */
	MOGAC2D mogac;

	/**
	 * Instantiates a new muscle advect2 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MuscleAdvect2D(SpringlsCommon2D commons, MOGAC2D mogac)
			throws IOException {
		super(commons);
		this.mogac = mogac;
	}

	/**
	 * Instantiates a new muscle advect2 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 * @param pressureBuffer the pressure buffer
	 * @param advectBuffer the advect buffer
	 * @param pressureWeight the pressure weight
	 * @param advectionWeight the advection weight
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MuscleAdvect2D(SpringlsCommon2D commons, MOGAC2D mogac,
			CLBuffer<FloatBuffer> pressureBuffer,
			CLBuffer<FloatBuffer> advectBuffer, float pressureWeight,
			float advectionWeight) throws IOException {
		super(commons, pressureBuffer, advectBuffer, pressureWeight,
				advectionWeight);
		this.mogac = mogac;
	}

	/**
	 * Instantiates a new muscle advect2 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 * @param pressureImage the pressure image
	 * @param pressureWeight the pressure weight
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MuscleAdvect2D(SpringlsCommon2D commons, MOGAC2D mogac,
			float[][] pressureImage, float pressureWeight) throws IOException {
		super(commons, pressureImage, pressureWeight);
		this.mogac = mogac;
	}

	/**
	 * Instantiates a new muscle advect2 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 * @param pressureImage the pressure image
	 * @param vecFieldImage the vec field image
	 * @param pressureWeight the pressure weight
	 * @param advectionWeight the advection weight
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MuscleAdvect2D(SpringlsCommon2D commons, MOGAC2D mogac,
			float[][] pressureImage, float[][][] vecFieldImage,
			float pressureWeight, float advectionWeight) throws IOException {
		super(commons, pressureImage, vecFieldImage, pressureWeight,
				advectionWeight);
		this.mogac = mogac;
	}

	/**
	 * Instantiates a new muscle advect2 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 * @param vecFieldImage the vec field image
	 * @param advectionWeight the advection weight
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MuscleAdvect2D(SpringlsCommon2D commons, MOGAC2D mogac,
			float[][][] vecFieldImage, float advectionWeight)
			throws IOException {
		super(commons, vecFieldImage, advectionWeight);
		this.mogac = mogac;
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
				commons.elements * 2, READ_WRITE, USE_BUFFER);
		CLBuffer<FloatBuffer> maxForces = commons.context.createFloatBuffer(
				(commons.arrayLength / SpringlsCommon2D.STRIDE), READ_WRITE,
				USE_BUFFER);

		CLKernel applyForces = (!commons.isPreserveToplogy()) ? commons.kernelMap
				.get(SpringlsCommon2D.APPLY_FORCES + "Mogac")
				: commons.kernelMap.get(SpringlsCommon2D.APPLY_FORCES_TOPO_RULE
						+ "Mogac");

		CLKernel computeMaxForces = commons.kernelMap
				.get(SpringlsCommon2D.COMPUTE_MAX_FORCES);
		CLKernel computeForces;
		// Compute forces applied to each particle and store them in an array.
		if (pressureImageBuffer != null) {
			if (vecFieldImageBuffer != null) {
				computeForces = commons.kernelMap
						.get(SpringlsCommon2D.COMPUTE_FORCES_PRESSURE_VECFIELD
								+ "Mogac");
				computeForces
						.putArgs(commons.capsuleBuffer, pressureImageBuffer,
								commons.springlLabelBuffer,
								mogac.imageLabelBuffer, vecFieldImageBuffer,
								pointUpdates).putArg(pressureWeight)
						.putArg(advectionWeight).putArg(commons.elements)
						.rewind();
			} else {
				computeForces = commons.kernelMap
						.get(SpringlsCommon2D.COMPUTE_FORCES_PRESSURE + "Mogac");
				computeForces
						.putArgs(commons.capsuleBuffer, pressureImageBuffer,
								commons.springlLabelBuffer,
								mogac.imageLabelBuffer, pointUpdates)
						.putArg(pressureWeight).putArg(commons.elements)
						.rewind();
			}
		} else {
			computeForces = commons.kernelMap
					.get(SpringlsCommon2D.COMPUTE_FORCES_VECFIELD + "Mogac");
			computeForces
					.putArgs(commons.capsuleBuffer, vecFieldImageBuffer,
							pointUpdates).putArg(advectionWeight)
					.putArg(commons.elements).rewind();
		}

		commons.queue.put1DRangeKernel(computeForces, 0, commons.arrayLength,
				SpringlsCommon2D.WORKGROUP_SIZE);
		computeMaxForces.putArgs(pointUpdates, maxForces)
				.putArg(SpringlsCommon2D.STRIDE).putArg(commons.elements)
				.rewind();
		// Find the maximum force in order to choose an appropriate step size.
		commons.queue.put1DRangeKernel(computeMaxForces, 0,
				(commons.arrayLength / SpringlsCommon2D.STRIDE), Math.min(
						SpringlsCommon2D.WORKGROUP_SIZE,
						(commons.arrayLength / SpringlsCommon2D.STRIDE)));

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
				SpringlsCommon2D.WORKGROUP_SIZE);
		commons.queue.finish();
		maxForces.release();
		pointUpdates.release();
		return displacement;
	}
}
