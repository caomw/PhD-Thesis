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

import javax.vecmath.Vector3f;

// TODO: Auto-generated Javadoc
/**
 * The Class TriangleFace.
 */
public class TriangleFace extends Face {

	/** The triangle. */
	public Triangle triangle;

	/**
	 * Instantiates a new triangle face.
	 * 
	 * @param e1
	 *            the e1
	 * @param e2
	 *            the e2
	 * @param e3
	 *            the e3
	 */
	public TriangleFace(Edge e1, Edge e2, Edge e3) {
		triangle = new Triangle(e1, e2, e3);
	}

	/**
	 * Instantiates a new triangle face.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * @param v3
	 *            the v3
	 */
	public TriangleFace(Vertex v1, Vertex v2, Vertex v3) {
		triangle = new Triangle(v1, v2, v3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Face#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TriangleFace) {
			return triangle.equals(((TriangleFace) obj).triangle);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Face#getEdges()
	 */
	@Override
	public Edge[] getEdges() {
		return new Edge[] { triangle.e1, triangle.e2, triangle.e3 };
	}

	/**
	 * Gets the normal.
	 * 
	 * @return the normal
	 */
	public Vector3f getNormal() {
		Vector3 normal = new Vector3();
		normal.cross(triangle.e1.getVector(), triangle.e2.getVector());
		return normal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Face#getVertices()
	 */
	@Override
	public Vertex[] getVertices() {
		return new Vertex[] { triangle.v1, triangle.v2, triangle.v3 };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return triangle.toString();
	}
}
