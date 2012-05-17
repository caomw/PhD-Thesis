/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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
 * 6-way neighbors for volume Note: the C implementations sometimes use a
 * different ordering of these neighbors.
 * 
 * @author Blake Lucas
 */
public class MaskVolume6 implements MaskVolume {

	/** The Constant neighborsX. */
	private static final byte[] neighborsX = new byte[] { 1, 0, -1, 0, 0, 0 };

	/** The Constant neighborsY. */
	private static final byte[] neighborsY = new byte[] { 0, 1, 0, -1, 0, 0 };

	/** The Constant neighborsZ. */
	private static final byte[] neighborsZ = new byte[] { 0, 0, 0, 0, 1, -1 };

	/** The Constant length. */
	public static final int length = neighborsX.length;

	/**
	 * Gets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param vol
	 *            the vol
	 * 
	 * @return the number
	 */
	public static Number get(int i, int j, int k, int l, ImageData vol) {
		if (l < 0 || l >= length) {
			return null;
		}
		int x = i + neighborsX[l];
		int y = j + neighborsY[l];
		int z = k + neighborsZ[l];
		if (x < vol.getRows() || y < vol.getCols() || z < vol.getSlices()) {
			return null;
		}
		if (x >= vol.getRows() || y >= vol.getCols() || z >= vol.getSlices()) {
			return null;
		}
		return vol.get(x, y, z);
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.MaskVolume#getNeighborsX()
	 */
	@Override
	public byte[] getNeighborsX() {
		return neighborsX;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.MaskVolume#getNeighborsY()
	 */
	@Override
	public byte[] getNeighborsY() {
		return neighborsY;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.MaskVolume#getNeighborsZ()
	 */
	@Override
	public byte[] getNeighborsZ() {
		return neighborsZ;
	}
}
