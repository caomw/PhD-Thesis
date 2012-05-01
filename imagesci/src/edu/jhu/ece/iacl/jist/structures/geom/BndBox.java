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

// TODO: Auto-generated Javadoc
/**
 * Created by IntelliJ IDEA. User: bennett Date: Nov 20, 2005 Time: 9:22:24 AM
 * ************************************ Magnetic Resonance in Medicine Final
 * Project Released: December 1, 2005
 * 
 * class BndBox Store bounding box information for accelerated collision
 * detection.
 * 
 * Copyright (C) 2005 Bennett Landman, bennett@bme.jhu.edu
 */
public class BndBox {

	// Locations of the corners of the boudning box
	/** The b. */
	private PT a, b;

	// Create a new, empty bounding box
	/**
	 * Instantiates a new bnd box.
	 */
	public BndBox() {
		a = null;
		b = null;
	}

	// Createa a new bounding box containing two points
	/**
	 * Instantiates a new bnd box.
	 * 
	 * @param a0
	 *            the a0
	 * @param b0
	 *            the b0
	 */
	public BndBox(PT a0, PT b0) {
		a = new PT(Math.min(a0.x, b0.x), Math.min(a0.y, b0.y), Math.min(a0.z,
				b0.z));
		b = new PT(Math.max(a0.x, b0.x), Math.max(a0.y, b0.y), Math.max(a0.z,
				b0.z));
	}

	/*
	 * @return max point with smallest boundary values
	 */
	/**
	 * Gets the max pt.
	 *
	 * @return the max pt
	 */
	public PT getMaxPt() {
		PT max = b;
		return max;
	}

	// D. Polders: added get min and max points of BndBox
	/*
	 * @return min point with smallest boundary values
	 */
	/**
	 * Gets the min pt.
	 *
	 * @return the min pt
	 */
	public PT getMinPt() {
		PT min = a;
		return min;
	}

	// Detect if a point is inside a test point
	/**
	 * Inside.
	 * 
	 * @param test
	 *            the test
	 * 
	 * @return true, if successful
	 */
	public boolean inside(PT test) {
		return (test.x >= a.x) && (test.x <= b.x) && (test.y >= a.y)
				&& (test.y <= b.y) && (test.z >= a.z) && (test.z <= b.z);
	}

	// Detect if two bounding boxes ovelap
	/**
	 * Intersect.
	 * 
	 * @param test
	 *            the test
	 * 
	 * @return true, if successful
	 */
	public boolean intersect(BndBox test) {
		double dx = Math.min(test.b.x, b.x) - Math.max(test.a.x, a.x);
		double dy = Math.min(test.b.y, b.y) - Math.max(test.a.y, a.y);
		double dz = Math.min(test.b.z, b.z) - Math.max(test.a.z, a.z);
		boolean ret = (dx >= 0) && (dy >= 0) && (dz >= 0);
		/*
		 * if(this.a.z>49.f) return true;
		 */
		return ret;
	}

	/**
	 * Union.
	 * 
	 * @param pt
	 *            the pt
	 */
	public void union(PT pt) {
		if (a == null) {
			a = pt;
			b = pt;
		} else {
			a = new PT(Math.min(a.x, pt.x), Math.min(a.y, pt.y), Math.min(a.z,
					pt.z));
			b = new PT(Math.max(b.x, pt.x), Math.max(b.y, pt.y), Math.max(b.z,
					pt.z));
		}

	}

	// Force this bounding box to include a triangle
	/**
	 * Union.
	 * 
	 * @param tri
	 *            the tri
	 */
	public void union(TriangleSigned tri) {
		if (a == null) {
			a = tri.pts[0];
			b = a;
		}
		for (int j = 0; j < tri.pts.length; j++) {
			a = new PT(Math.min(a.x, tri.pts[j].x),
					Math.min(a.y, tri.pts[j].y), Math.min(a.z, tri.pts[j].z));
			b = new PT(Math.max(b.x, tri.pts[j].x),
					Math.max(b.y, tri.pts[j].y), Math.max(b.z, tri.pts[j].z));
		}
	}
}
