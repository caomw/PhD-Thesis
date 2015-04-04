/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 */
package edu.jhu.ece.iacl.jist.structures.image;

// TODO: Auto-generated Javadoc

/**
 * 26-way neighbors for volume Note: the C implementations sometimes use a
 * different ordering of these neighbors.
 * 
 * @author Blake Lucas
 */
public class MaskVolume26 implements MaskVolume {


	/** The xoff26. */
	static byte xoff26[] = { 0, 1, 0, 0, -1, 0, 1, 1, -1, -1, 1, 1, -1, -1, 0,
			0, 0, 0, -1, -1, 1, 1, -1, -1, 1, 1 };

	/** The yoff26. */
	static byte yoff26[] = { 1, 0, 0, -1, 0, 0, 1, -1, 1, -1, 0, 0, 0, 0, 1, 1,
			-1, -1, -1, -1, -1, -1, 1, 1, 1, 1 };

	/** The zoff26. */
	static byte zoff26[] = { 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 1, -1, 1, -1, 1,
			-1, 1, -1, -1, 1, -1, 1, -1, 1, -1, 1 };

	/** The Constant length. */
	public static final int length = xoff26.length;
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.MaskVolume#getNeighborsX()
	 */
	@Override
	public byte[] getNeighborsX() {
		return xoff26;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.MaskVolume#getNeighborsY()
	 */
	@Override
	public byte[] getNeighborsY() {
		return yoff26;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.MaskVolume#getNeighborsZ()
	 */
	@Override
	public byte[] getNeighborsZ() {
		return zoff26;
	}

}
