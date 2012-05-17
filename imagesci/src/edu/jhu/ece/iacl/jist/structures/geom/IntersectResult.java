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
package edu.jhu.ece.iacl.jist.structures.geom;

// TODO: Auto-generated Javadoc
/**
 * Created by IntelliJ IDEA. User: bennett Date: Nov 20, 2005 Time: 11:12:10 AM
 * To change this template use Options | File Templates.
 * ************************************ Magnetic Resonance in Medicine Final
 * Project Released: December 1, 2005
 * 
 * class IntersectResult Store the results associated with detecting an
 * intersection.
 * 
 * Copyright (C) 2005 Bennett Landman, bennett@bme.jhu.edu
 */
public class IntersectResult {

	/** The fractional distance. */
	public float fractionalDistance; // store the fractional distance between
										// the ends of a line segement until
										// intersection

	/** The intersection normal. */
	public PT intersectionNormal; // normal vector at point of intercept

	/** The intersection point. */
	public PT intersectionPoint; // store the point of intersection

	/** The result code. */
	public char resultCode;

	/**
	 * Instantiates a new intersect result.
	 * 
	 * @param code
	 *            the code
	 * @param a
	 *            the a
	 */
	public IntersectResult(char code, PT a) {
		intersectionPoint = a;
		resultCode = code;
	}

	// Create a new IntersectionResult
	/**
	 * Instantiates a new intersect result.
	 * 
	 * @param a
	 *            the a
	 * @param s
	 *            the s
	 * @param norm
	 *            the norm
	 */
	public IntersectResult(PT a, float s, PT norm) {
		intersectionPoint = a;
		fractionalDistance = s;
		intersectionNormal = norm;
	}
}
