/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */
package edu.jhu.ece.iacl.jist.structures.geom;

import javax.vecmath.Point3f;

// TODO: Auto-generated Javadoc
/**
 * Created by IntelliJ IDEA. User: bennett Date: Nov 20, 2005 Time: 9:21:15 AM
 * To change this template use Options | File Templates.
 * ************************************ Magnetic Resonance in Medicine Final
 * Project Released: December 1, 2005
 * 
 * class PT Represent a 3-tuple. For use as a point, vector, etc.
 * 
 * Copyright (C) 2005 Bennett Landman, bennett@bme.jhu.edu
 */
public class PT extends Point3f {
	// create a new PT
	/**
	 * Instantiates a new pT.
	 */
	public PT() {
		super();
	}

	// create a new PT
	/**
	 * Instantiates a new pT.
	 * 
	 * @param x0
	 *            the x0
	 * @param y0
	 *            the y0
	 * @param z0
	 *            the z0
	 */
	public PT(double x0, double y0, double z0) {
		x = (float) x0;
		y = (float) y0;
		z = (float) z0;
	}

	/**
	 * Instantiates a new pT.
	 * 
	 * @param x0
	 *            the x0
	 * @param y0
	 *            the y0
	 * @param z0
	 *            the z0
	 */
	public PT(float x0, float y0, float z0) {
		super(x0, y0, z0);
	}

	// perform the vector cross-product
	/**
	 * Cross.
	 * 
	 * @param b
	 *            the b
	 * 
	 * @return the pT
	 */
	public PT cross(PT b) {
		return new PT(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
	}

	// perform the vector dot product
	/**
	 * Dot.
	 * 
	 * @param b
	 *            the b
	 * 
	 * @return the float
	 */
	public float dot(PT b) {
		return x * b.x + y * b.y + z * b.z;
	}

	// perform coordinate-wise equality test
	/**
	 * Equals.
	 * 
	 * @param pt
	 *            the pt
	 * 
	 * @return true, if successful
	 */
	public boolean equals(PT pt) {
		return (pt.x == x) && (pt.y == y) && (pt.z == z);
	}

	// compute the L-2 norm of the vector
	/**
	 * Length.
	 * 
	 * @return the double
	 */
	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	// perform coordinate-wise subtraction
	/**
	 * Minus.
	 * 
	 * @param b
	 *            the b
	 * 
	 * @return the pT
	 */
	public PT minus(PT b) {
		return new PT(x - b.x, y - b.y, z - b.z);
	}

	// perform coordinate-wise addition
	/**
	 * Plus.
	 * 
	 * @param b
	 *            the b
	 * 
	 * @return the pT
	 */
	public PT plus(PT b) {
		return new PT(x + b.x, y + b.y, z + b.z);
	}

	// multiply by a scalar
	/**
	 * Times.
	 * 
	 * @param t
	 *            the t
	 * 
	 * @return the pT
	 */
	public PT times(float t) {
		return new PT(x * t, y * t, z * t);
	}

	// convert coordinate to a string for debugging
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.vecmath.Tuple3f#toString()
	 */
	@Override
	public String toString() {
		return "(" + (x) + "," + (y) + "," + (z) + ")";
	}

	/**
	 * Project this point onto the input point Computes
	 * [dot(this,pt)/(length(pt)^2)]pt.
	 * 
	 * @param p
	 *            The point to be projected onto
	 * 
	 * @return The vector resulting from this projection
	 */
	public PT vectorProject(PT p) {
		double len = p.length(); // the length of p
		double dot = this.dot(p); // the dot product of this and p
		return p.times((float) (len * len * dot));
	}

	// convert position to an Matlab coordinate index
	/**
	 * Cor2ind.
	 * 
	 * @param sx
	 *            the sx
	 * @param sy
	 *            the sy
	 * 
	 * @return the int
	 */
	int cor2ind(int sx, int sy) {
		return Math.round(x) + sx * (Math.round(y) + Math.round(z) * sy);
	}

}
