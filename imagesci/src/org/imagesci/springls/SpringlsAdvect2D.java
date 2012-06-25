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
package org.imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsAdvect moves springls with a pressure force and vector
 * field.
 */
public class SpringlsAdvect2D {

	/** The Constant MAX_ITERATIONS. */
	protected static final int MAX_ITERATIONS = 20;

	/** The advection weight. */
	protected float advectionWeight = 1.0f;

	/** The commons. */
	protected SpringlsCommon2D commons;

	/** The pressure image buffer. */
	protected CLBuffer<FloatBuffer> pressureImageBuffer = null;

	/** The pressure weight. */
	protected float pressureWeight = 1.0f;

	/** The vec field image buffer. */
	protected CLBuffer<FloatBuffer> vecFieldImageBuffer = null;

	/**
	 * Instantiates a new springls advect.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsAdvect2D(SpringlsCommon2D commons) {
		this.commons = commons;
	}

	/**
	 * Instantiates a new springls advect.
	 * 
	 * @param commons
	 *            the commons
	 * @param pressureBuffer
	 *            the pressure buffer
	 * @param advectBuffer
	 *            the advect buffer
	 * @param pressureWeight
	 *            the pressure weight
	 * @param advectionWeight
	 *            the advection weight
	 */
	public SpringlsAdvect2D(SpringlsCommon2D commons,
			CLBuffer<FloatBuffer> pressureBuffer,
			CLBuffer<FloatBuffer> advectBuffer, float pressureWeight,
			float advectionWeight) {
		this.commons = commons;
		pressureImageBuffer = pressureBuffer;
		this.vecFieldImageBuffer = advectBuffer;
		this.pressureWeight = pressureWeight;
		this.advectionWeight = advectionWeight;
	}

	/**
	 * Instantiates a new springls advect.
	 * 
	 * @param commons
	 *            the commons
	 * @param pressureImage
	 *            the pressure image
	 * @param pressureWeight
	 *            the pressure weight
	 */
	public SpringlsAdvect2D(SpringlsCommon2D commons, float[][] pressureImage,
			float pressureWeight) {
		this.commons = commons;
		pressureImageBuffer = commons.context.createFloatBuffer(
				pressureImage.length * pressureImage[0].length, READ_ONLY,
				USE_BUFFER);
		FloatBuffer pressureBuff = pressureImageBuffer.getBuffer();
		for (int j = 0; j < pressureImage[0].length; j++) {
			for (int i = 0; i < pressureImage.length; i++) {
				pressureBuff.put(pressureImage[i][j]);
			}
		}
		pressureBuff.rewind();
		this.pressureWeight = pressureWeight;
		this.advectionWeight = 0;

		commons.queue.putWriteBuffer(pressureImageBuffer, true);
	}

	/**
	 * Instantiates a new springls advect.
	 * 
	 * @param commons
	 *            the commons
	 * @param pressureImage
	 *            the pressure image
	 * @param vecFieldImage
	 *            the vec field image
	 * @param pressureWeight
	 *            the pressure weight
	 * @param advectionWeight
	 *            the advection weight
	 */
	public SpringlsAdvect2D(SpringlsCommon2D commons, float[][] pressureImage,
			float[][][] vecFieldImage, float pressureWeight,
			float advectionWeight) {
		this.commons = commons;
		pressureImageBuffer = commons.context.createFloatBuffer(
				pressureImage.length * pressureImage[0].length, READ_ONLY,
				USE_BUFFER);
		FloatBuffer pressureBuff = pressureImageBuffer.getBuffer();
		for (int j = 0; j < pressureImage[0].length; j++) {
			for (int i = 0; i < pressureImage.length; i++) {
				pressureBuff.put(pressureImage[i][j]);
			}
		}
		pressureBuff.rewind();
		vecFieldImageBuffer = commons.context.createFloatBuffer(
				vecFieldImage.length * vecFieldImage[0].length
						* vecFieldImage[0][0].length, READ_ONLY, USE_BUFFER);
		FloatBuffer advectBuff = vecFieldImageBuffer.getBuffer();
		// Put buffer in backwards for efficiency in vector lookups

		for (int j = 0; j < vecFieldImage[0].length; j++) {
			for (int i = 0; i < vecFieldImage.length; i++) {
				for (int l = 0; l < vecFieldImage[0][0].length; l++) {
					advectBuff.put(vecFieldImage[i][j][l]);
				}
			}
		}
		advectBuff.rewind();
		this.pressureWeight = pressureWeight;
		this.advectionWeight = advectionWeight;
		commons.queue.putWriteBuffer(pressureImageBuffer, true);
		commons.queue.putWriteBuffer(vecFieldImageBuffer, true);
	}

	/**
	 * Instantiates a new springls advect.
	 * 
	 * @param commons
	 *            the commons
	 * @param vecFieldImage
	 *            the vec field image
	 * @param advectionWeight
	 *            the advection weight
	 */
	public SpringlsAdvect2D(SpringlsCommon2D commons,
			float[][][] vecFieldImage, float advectionWeight) {
		this.commons = commons;
		vecFieldImageBuffer = commons.context.createFloatBuffer(
				vecFieldImage.length * vecFieldImage[0].length
						* vecFieldImage[0][0].length, READ_ONLY, USE_BUFFER);
		FloatBuffer advectBuff = vecFieldImageBuffer.getBuffer();
		// Put buffer in backwards for efficiency in vector lookups
		for (int j = 0; j < vecFieldImage[0].length; j++) {
			for (int i = 0; i < vecFieldImage.length; i++) {
				for (int l = 0; l < vecFieldImage[0][0].length; l++) {
					advectBuff.put(vecFieldImage[i][j][l]);
				}
			}
		}
		advectBuff.rewind();
		this.pressureWeight = 0;
		this.advectionWeight = advectionWeight;

		commons.queue.putWriteBuffer(vecFieldImageBuffer, true);
	}

	/**
	 * Advect.
	 * 
	 * @param timeStep
	 *            the time step
	 * @return the double
	 */
	public double advect(double timeStep) {
		CLBuffer<FloatBuffer> pointUpdates = commons.context.createFloatBuffer(
				commons.elements * 2, READ_WRITE, USE_BUFFER);
		CLBuffer<FloatBuffer> maxForces = commons.context.createFloatBuffer(
				(commons.arrayLength / SpringlsCommon2D.STRIDE), READ_WRITE,
				USE_BUFFER);
		CLKernel applyForces = (!commons.isPreserveToplogy()) ? commons.kernelMap
				.get(SpringlsCommon2D.APPLY_FORCES) : commons.kernelMap
				.get(SpringlsCommon2D.APPLY_FORCES_TOPO_RULE);

		CLKernel computeMaxForces = commons.kernelMap
				.get(SpringlsCommon2D.COMPUTE_MAX_FORCES);
		CLKernel computeForces;
		// Compute forces applied to each particle and store them in an array.
		if (pressureImageBuffer != null) {
			if (vecFieldImageBuffer != null) {
				computeForces = commons.kernelMap
						.get(SpringlsCommon2D.COMPUTE_FORCES_PRESSURE_VECFIELD);
				computeForces
						.putArgs(commons.capsuleBuffer, pressureImageBuffer,
								vecFieldImageBuffer, pointUpdates)
						.putArg(pressureWeight).putArg(advectionWeight)
						.putArg(commons.elements).rewind();
			} else {
				computeForces = commons.kernelMap
						.get(SpringlsCommon2D.COMPUTE_FORCES_PRESSURE);
				computeForces
						.putArgs(commons.capsuleBuffer, pressureImageBuffer,
								pointUpdates).putArg(pressureWeight)
						.putArg(commons.elements).rewind();
			}
		} else {
			computeForces = commons.kernelMap
					.get(SpringlsCommon2D.COMPUTE_FORCES_VECFIELD);
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
				.putArgs(commons.capsuleBuffer, commons.signedLevelSetBuffer,
						pointUpdates).putArg(maxForce).putArg(commons.elements)
				.rewind();
		commons.queue.put1DRangeKernel(applyForces, 0, commons.arrayLength,
				SpringlsCommon2D.WORKGROUP_SIZE);
		commons.queue.finish();
		maxForces.release();
		pointUpdates.release();
		return displacement;
	}

	/**
	 * Gets the advection weight.
	 * 
	 * @return the advection weight
	 */
	public float getAdvectionWeight() {
		return advectionWeight;
	}

	/**
	 * Gets the pressure weight.
	 * 
	 * @return the pressure weight
	 */
	public float getPressureWeight() {
		return pressureWeight;
	}

	/**
	 * Sets the advection weight.
	 * 
	 * @param advectionWeight
	 *            the new advection weight
	 */
	public void setAdvectionWeight(float advectionWeight) {
		this.advectionWeight = advectionWeight;
	}

	/**
	 * Sets the pressure image.
	 * 
	 * @param pressureImage
	 *            the new pressure image
	 */
	public void setPressureImage(ImageDataFloat pressureImage) {
		float[][] pressureImageMat = pressureImage.toArray2d();
		FloatBuffer pressureBuff = pressureImageBuffer.getBuffer();
		for (int j = 0; j < pressureImageMat[0].length; j++) {
			for (int i = 0; i < pressureImageMat.length; i++) {
				pressureBuff.put(pressureImageMat[i][j]);
			}
		}
		pressureBuff.rewind();
		commons.queue.putWriteBuffer(pressureImageBuffer, true);
	}

	/**
	 * Sets the pressure weight.
	 * 
	 * @param pressureWeight
	 *            the new pressure weight
	 */
	public void setPressureWeight(float pressureWeight) {
		this.pressureWeight = pressureWeight;
	}

	/**
	 * Sets the vector field.
	 * 
	 * @param vecFieldImage
	 *            the new vector field
	 */
	public void setVectorField(ImageDataFloat vecFieldImage) {
		float[][][][] vecFieldImageMat = vecFieldImage.toArray4d();
		FloatBuffer advectBuff = vecFieldImageBuffer.getBuffer();
		// Put buffer in backwards for efficiency in vector lookups
		for (int k = 0; k < vecFieldImageMat[0][0].length; k++) {
			for (int j = 0; j < vecFieldImageMat[0].length; j++) {
				for (int i = 0; i < vecFieldImageMat.length; i++) {
					for (int l = 0; l < vecFieldImageMat[0][0][0].length; l++) {
						advectBuff.put(vecFieldImageMat[i][j][k][l]);
					}
				}
			}
		}
		advectBuff.rewind();
		commons.queue.putWriteBuffer(vecFieldImageBuffer, true);
	}

}
