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
package edu.jhu.ece.iacl.jist.structures.image;

// TODO: Auto-generated Javadoc

/**
 * Create one-dimensional guassian kernel with specified sigma.
 * 
 * @author Blake Lucas
 */
public class GaussianArrayKernel implements ConvolutionArrayKernel {

	/** The kernel array. */
	private double kernelArray[];

	/**
	 * Create gaussian kernel for specified sigma.
	 * 
	 * @param sx
	 *            sigma
	 */
	public GaussianArrayKernel(double sx) {
		int kx;
		double sum = 0;

		// kernel size
		kx = (int) Math.ceil(Math.max(3.0f * sx - 0.5f, 0.0f));

		// create the kernel
		kernelArray = new double[2 * kx + 1];
		for (int i = -kx; i <= kx; i++) {
			kernelArray[kx + i] = Math.exp(-0.5f * (i * i / sx * sx));
			sum += kernelArray[kx + i];
		}
		for (int i = -kx; i <= kx; i++) {
			kernelArray[kx + i] = kernelArray[kx + i] / sum;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.ConvolutionArrayKernel#getArray()
	 */
	@Override
	public double[] getArray() {
		return kernelArray;
	}
}
