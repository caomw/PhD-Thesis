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
package edu.jhu.ece.iacl.jist.structures.geom;

import javax.vecmath.Vector3f;

// TODO: Auto-generated Javadoc
/**
 * The Class Vector3.
 * 
 * @author Blake Lucas
 */
public class Vector3 extends Vector3f {

	/**
	 * Instantiates a new vector3.
	 */
	public Vector3() {
		super();
	}

	/**
	 * Instantiates a new vector3.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public Vector3(double x, double y, double z) {
		super((float) x, (float) y, (float) z);
	}

	/**
	 * Instantiates a new vector3.
	 * 
	 * @param d
	 *            the d
	 */
	public Vector3(double[] d) {
		super((float) d[0], (float) d[1], (float) d[2]);
	}

	/**
	 * Instantiates a new vector3.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public Vector3(float x, float y, float z) {
		super(x, y, z);
	}

}
