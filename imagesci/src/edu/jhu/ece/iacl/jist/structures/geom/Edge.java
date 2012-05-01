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
 * The Class Edge.
 */
public class Edge extends Segment implements Comparable<Edge> {

	/** The triangles. */
	Triangle[] triangles = new Triangle[2];

	/**
	 * Instantiates a new edge.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 */
	protected Edge(Vertex v1, Vertex v2) {
		super(v1, v2);
		v1.add(this);
		v2.add(this);
	}

	/**
	 * Link.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * 
	 * @return the edge
	 */
	public static Edge link(Vertex v1, Vertex v2) {
		int index = v1.edges.indexOf(v2);
		if (index >= 0) {
			return v1.edges.get(index);
		} else {
			return new Edge(v1, v2);
		}

	}

	/**
	 * Adds the.
	 * 
	 * @param tri
	 *            the tri
	 */
	public void add(Triangle tri) {
		if (triangles[0] == null) {
			triangles[0] = tri;
		} else {
			triangles[1] = tri;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Edge e) {
		return this.hashCode() - e.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Edge) {
			Edge e = (Edge) o;
			return (e.v1 == v1 && e.v2 == v2) || (e.v1 == v2 && e.v2 == v1);
		} else if (o instanceof Vertex) {
			Vertex v = (Vertex) o;
			return (v1 == v || v2 == v);
		} else {
			return false;
		}
	}
}
