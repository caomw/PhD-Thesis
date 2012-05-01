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
package edu.jhu.ece.iacl.jist.structures.geom;

import javax.vecmath.Point3f;

// TODO: Auto-generated Javadoc
/**
 * The Class Point3.
 * 
 * @author Blake Lucas
 */
public class Point3 extends Point3f {

	/**
	 * Instantiates a new point3.
	 */
	public Point3() {
		super();
	}

	/**
	 * Instantiates a new point3.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public Point3(double x, double y, double z) {
		super((float) x, (float) y, (float) z);
	}

	/**
	 * Instantiates a new point3.
	 * 
	 * @param d
	 *            the d
	 */
	public Point3(double[] d) {
		super((float) d[0], (float) d[1], (float) d[2]);
	}

	/**
	 * Instantiates a new point3.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public Point3(float x, float y, float z) {
		super(x, y, z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.vecmath.Tuple3f#toString()
	 */
	@Override
	public String toString() {
		return "(" + x + "," + y + "," + z + ")";
	}
}
