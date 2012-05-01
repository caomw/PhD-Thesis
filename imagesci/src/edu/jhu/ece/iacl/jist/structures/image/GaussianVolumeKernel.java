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
 * The Class GaussianVolumeKernel.
 */
public class GaussianVolumeKernel implements ConvolutionVolumeKernel {

	/** The kernel volume. */
	private double kernelVolume[][][];

	/**
	 * Create gaussian kernel for convolution.
	 * 
	 * @param sx
	 *            Standard deviation in X
	 * @param sy
	 *            Standard deviation in Y
	 * @param sz
	 *            Standard deviation in Z
	 */
	public GaussianVolumeKernel(double sx, double sy, double sz) {
		int kx, ky, kz;
		double sum = 0;

		// kernel size
		kx = (int) Math.ceil(Math.max(3.0 * sx - 0.5f, 0.0f));
		ky = (int) Math.ceil(Math.max(3.0 * sy - 0.5f, 0.0f));
		kz = (int) Math.ceil(Math.max(3.0 * sz - 0.5f, 0.0f));

		// create the kernel
		kernelVolume = new double[2 * kx + 1][2 * ky + 1][2 * kz + 1];
		for (int i = -kx; i <= kx; i++) {
			for (int j = -ky; j <= ky; j++) {
				for (int l = -kz; l <= kz; l++) {
					kernelVolume[kx + i][ky + j][kz + l] = (float) Math
							.exp(-0.5f
									* (i * i / sx * sx + j * j / sy * sy + l
											* l / sz * sz));
					sum += kernelVolume[kx + i][ky + j][kz + l];
				}
			}
		}
		// normalize
		for (int i = -kx; i <= kx; i++) {
			for (int j = -ky; j <= ky; j++) {
				for (int l = -kz; l <= kz; l++) {
					kernelVolume[kx + i][ky + j][kz + l] = kernelVolume[kx + i][ky
							+ j][kz + l]
							/ sum;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.ConvolutionVolumeKernel#getVolume
	 * ()
	 */
	@Override
	public double[][][] getVolume() {
		return kernelVolume;
	}

}
