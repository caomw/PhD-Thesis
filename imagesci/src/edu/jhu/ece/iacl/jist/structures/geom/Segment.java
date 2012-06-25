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

// TODO: Auto-generated Javadoc
/**
 * The Class Segment.
 * 
 * @author Blake Lucas
 */
public class Segment {

	/** The v2. */
	public Vertex v1, v2;

	/**
	 * Instantiates a new segment.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 */
	protected Segment(Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	/**
	 * Contains.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return true, if successful
	 */
	public boolean contains(Vertex v) {
		return (v1 == v || v2 == v);
	}

	/**
	 * Contains.
	 * 
	 * @param V1
	 *            the v1
	 * @param V2
	 *            the v2
	 * 
	 * @return true, if successful
	 */
	public boolean contains(Vertex V1, Vertex V2) {
		return ((v1 == V1 && v2 == V2) || (v1 == V2 && v2 == V1));
	}

	/**
	 * Flip.
	 */
	public void flip() {
		Vertex tmp = v1;
		v1 = v2;
		v2 = tmp;
	}

	/**
	 * Gets the opposite.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the opposite
	 */
	public Vertex getOpposite(Vertex v) {
		return (v == v1) ? v2 : v1;
	}

	/**
	 * Gets the vector.
	 * 
	 * @return the vector
	 */
	public Vector3 getVector() {
		Vector3 vec = new Vector3();
		vec.sub(v2, v1);
		return vec;
	}

	/**
	 * To array.
	 * 
	 * @return the vertex[]
	 */
	public Vertex[] toArray() {
		return new Vertex[] { v1, v2 };
	}
}
