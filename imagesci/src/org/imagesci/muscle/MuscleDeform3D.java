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
package org.imagesci.muscle;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;


import java.nio.FloatBuffer;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.springls.SpringlsCommon3D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleDeform3D.
 */
public class MuscleDeform3D extends MuscleAdvect3D {
	
	/** The last buffer size. */
	protected int lastBufferSize = -1;
	
	/** The max force. */
	protected float maxForce = 1;
	
	/** The max forces. */
	protected CLBuffer<FloatBuffer> maxForces;

	/** The point updates. */
	protected CLBuffer<FloatBuffer> pointUpdates;

	/** The vec field image buffer. */
	protected CLBuffer<FloatBuffer> vecFieldImageBuffer;

	/**
	 * Instantiates a new muscle deform3 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 */
	public MuscleDeform3D(SpringlsCommon3D commons, MOGAC3D mogac) {
		super(commons, mogac);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.muscle.MuscleAdvect3D#advect(double)
	 */
	@Override
	public double advect(double timeStep) {
		CLKernel applyForces = commons.kernelMap.get("applyDeformationMogac");
		applyForces
				.putArgs(commons.capsuleBuffer, mogac.distanceFieldBuffer,
						mogac.imageLabelBuffer, commons.springlLabelBuffer,
						pointUpdates).putArg((float) (timeStep * maxForce))
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(applyForces, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
		return timeStep * maxForce;
	}

	/**
	 * Inits the.
	 *
	 * @param defField the def field
	 * @return the float
	 */
	public float init(ImageDataFloat defField) {
		if (vecFieldImageBuffer == null) {
			vecFieldImageBuffer = commons.context.createFloatBuffer(
					commons.rows * commons.cols * commons.slices * 3,
					READ_ONLY, USE_BUFFER);
		}
		if (commons.elements != lastBufferSize) {
			if (pointUpdates != null) {
				pointUpdates.release();
			}
			if (maxForces != null) {
				maxForces.release();
			}
			pointUpdates = commons.context.createFloatBuffer(
					commons.elements * 9, USE_BUFFER, READ_WRITE);
			maxForces = commons.context.createFloatBuffer(
					(commons.arrayLength / SpringlsCommon3D.STRIDE),
					READ_WRITE, USE_BUFFER);
			lastBufferSize = commons.elements;

		}
		float[][][][] vecFieldImage = defField.toArray4d();
		FloatBuffer advectBuff = vecFieldImageBuffer.getBuffer();
		float maxDisplacement = 0;
		// Put buffer in backwards for efficiency in vector lookups
		for (int k = 0; k < vecFieldImage[0][0].length; k++) {
			for (int j = 0; j < vecFieldImage[0].length; j++) {
				for (int i = 0; i < vecFieldImage.length; i++) {
					float mag = 0;
					for (int l = 0; l < 3; l++) {
						float val = vecFieldImage[i][j][k][l];
						advectBuff.put(val);
						mag += val * val;
					}
					maxDisplacement = (float) Math.max(maxDisplacement,
							Math.sqrt(mag));
				}
			}
		}
		advectBuff.rewind();
		commons.queue.putWriteBuffer(vecFieldImageBuffer, true);
		final CLKernel initDeformationField = commons.kernelMap
				.get("initDeformationField");
		initDeformationField
				.putArgs(commons.capsuleBuffer, vecFieldImageBuffer,
						pointUpdates).putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(initDeformationField, 0,
				commons.arrayLength, SpringlsCommon3D.WORKGROUP_SIZE);
		final CLKernel computeMaxForces = commons.kernelMap
				.get(SpringlsCommon3D.COMPUTE_MAX_FORCES);
		computeMaxForces.putArgs(pointUpdates, maxForces)
				.putArg(SpringlsCommon3D.STRIDE).putArg(commons.elements)
				.rewind();
		// Find the maximum force in order to choose an appropriate step size.
		commons.queue.put1DRangeKernel(
				computeMaxForces,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.arrayLength
						/ SpringlsCommon3D.STRIDE),
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.putReadBuffer(maxForces, true);
		FloatBuffer buff = maxForces.getBuffer();
		maxForce = 0;
		while (buff.hasRemaining()) {
			maxForce = Math.max(maxForce, buff.get());
		}
		buff.rewind();
		// Rescale forces and update the position of all paritcles.
		float displacement = (float) (Math.sqrt(maxForce));
		maxForce = 1.0f / displacement;
		return displacement;
	}
}
