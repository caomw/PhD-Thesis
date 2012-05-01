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
 * The Class Triangle.
 */
public class Triangle {

	/** The e3. */
	public Edge e1, e2, e3;

	/** The v3. */
	public Vertex v1, v2, v3;

	/**
	 * Instantiates a new triangle.
	 * 
	 * @param e1
	 *            the e1
	 * @param e2
	 *            the e2
	 * @param e3
	 *            the e3
	 */
	public Triangle(Edge e1, Edge e2, Edge e3) {
		this.e1 = e1;
		this.e2 = e2;
		this.e3 = e3;
		this.v1 = e1.v1;
		this.v2 = e1.v2;
		this.v3 = e2.getOpposite(this.v2);
		e1.add(this);
		e2.add(this);
		e3.add(this);
	}

	/**
	 * Instantiates a new triangle.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * @param v3
	 *            the v3
	 */
	public Triangle(Vertex v1, Vertex v2, Vertex v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		e1 = Edge.link(v1, v2);
		e2 = Edge.link(v2, v3);
		e3 = Edge.link(v3, v1);
		e1.add(this);
		e2.add(this);
		e3.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Triangle) {
			Triangle tri = (Triangle) obj;
			return (tri.e1 == e1 && tri.e2 == e2 && tri.e3 == e3)
					|| (tri.e1 == e2 && tri.e2 == e3 && tri.e3 == e1)
					|| (tri.e1 == e3 && tri.e2 == e1 && tri.e3 == e2);
		} else {
			return false;
		}
	}

	/**
	 * Gets the opposite.
	 * 
	 * @param e
	 *            the e
	 * 
	 * @return the opposite
	 */
	public Vertex getOpposite(Edge e) {
		if (e.contains(v1, v2)) {
			return v3;
		} else if (e.contains(v1, v3)) {
			return v2;
		} else {
			return v1;
		}
	}

	/**
	 * Gets the opposite.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the opposite
	 */
	public Edge getOpposite(Vertex v) {
		if (!e1.contains(v)) {
			return e1;
		} else if (!e2.contains(v)) {
			return e2;
		} else {
			return e3;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("(%d,%d,%d)", v1.index, v2.index, v3.index);
	}
}
