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

import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsRelax.
 */
public class SpringlsRelax2D {

	/** The max iterations. */
	protected static int maxIterations = SpringlsConstants.relaxIterations;

	/** The commons. */
	protected SpringlsCommon2D commons;

	/** The time step. */
	protected float timeStep = SpringlsConstants.relaxTimeStep;

	/**
	 * Instantiates a new springls relax.
	 * 
	 * @param commons
	 *            the commons
	 * @param timeStep
	 *            the time step
	 */
	public SpringlsRelax2D(SpringlsCommon2D commons, float timeStep) {
		this.commons = commons;
		this.timeStep = timeStep;
	}

	/**
	 * Gets the max iterations.
	 * 
	 * @return the max iterations
	 */
	public static int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Sets the max iterations.
	 * 
	 * @param maxIterations
	 *            the new max iterations
	 */
	public static void setMaxIterations(int maxIterations) {
		SpringlsRelax2D.maxIterations = maxIterations;
	}

	/**
	 * Gets the time step.
	 * 
	 * @return the time step
	 */
	public float getTimeStep() {
		return timeStep;
	}

	/**
	 * Relax.
	 */
	public void relax() {
		CLBuffer<FloatBuffer> pointUpdates = commons.context.createFloatBuffer(
				commons.elements * 4, READ_WRITE, USE_BUFFER);
		CLKernel relaxKernel = commons.kernelMap
				.get(SpringlsCommon2D.RELAX_NEIGHBORS);
		CLKernel applyUpdates = commons.kernelMap
				.get(SpringlsCommon2D.APPLY_UPDATES);
		relaxKernel
				.putArgs(commons.capsuleBuffer, commons.capsuleNeighborBuffer,
						pointUpdates).putArg(timeStep).putArg(commons.elements)
				.rewind();
		applyUpdates.putArgs(commons.capsuleBuffer, pointUpdates)
				.putArg(commons.elements).rewind();

		for (int i = 0; i < maxIterations; i++) {
			commons.queue.put1DRangeKernel(relaxKernel, 0, commons.arrayLength,
					SpringlsCommon2D.WORKGROUP_SIZE);
			commons.queue.put1DRangeKernel(applyUpdates, 0,
					commons.arrayLength, SpringlsCommon2D.WORKGROUP_SIZE);
		}

		commons.queue.finish();
		pointUpdates.release();
	}

	/**
	 * Sets the time step.
	 * 
	 * @param timeStep
	 *            the new time step
	 */
	public void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}
}
