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

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class Vertex.
 */
public class Vertex extends Point3 implements Comparable<Vertex> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5600652963543029264L;

	/** The edges. */
	public ArrayList<Edge> edges = new ArrayList<Edge>();

	/** The index. */
	public int index = -1;

	/**
	 * Instantiates a new vertex.
	 */
	public Vertex() {
		super();
	}

	/**
	 * Instantiates a new vertex.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public Vertex(double x, double y, double z) {
		super(x, y, z);
	}

	/**
	 * Instantiates a new vertex.
	 * 
	 * @param d
	 *            the d
	 */
	public Vertex(double[] d) {
		super(d);
	}

	/**
	 * Instantiates a new vertex.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public Vertex(float x, float y, float z) {
		super(x, y, z);
	}

	/**
	 * Instantiates a new vertex.
	 * 
	 * @param p
	 *            the p
	 */
	public Vertex(Point3 p) {
		super(p.x, p.y, p.z);
	}

	/**
	 * Adds the.
	 * 
	 * @param e
	 *            the e
	 */
	public void add(Edge e) {
		edges.add(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Vertex v) {
		return this.hashCode() - v.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.vecmath.Tuple3f#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj == this);
		/*
		 * if(obj==this) return true; if(obj instanceof Vertex){ Vertex
		 * v=(Vertex)obj; return (x==v.x&&y==v.y&&z==v.z); } return false;
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.vecmath.Tuple3f#hashCode()
	 */
	@Override
	public int hashCode() {
		return System.class.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Point3#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " :" + index;
	}
}
