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

import java.util.HashSet;

import javax.media.j3d.GeometryArray;

// TODO: Auto-generated Javadoc
/**
 * The Class Surface.
 */
public abstract class Surface {

	/** The edges. */
	protected HashSet<Edge> edges;

	/** The faces. */
	protected HashSet<Face> faces;

	/** The vertices. */
	protected HashSet<Vertex> vertices;

	/**
	 * Instantiates a new surface.
	 * 
	 * @param vertexCount
	 *            the vertex count
	 */
	public Surface(int vertexCount) {
		faces = new HashSet<Face>(vertexCount / 3);
		vertices = new HashSet<Vertex>(vertexCount);
		edges = new HashSet<Edge>(vertexCount / 2);
	}

	/**
	 * Adds the.
	 * 
	 * @param f
	 *            the f
	 */
	public void add(Face f) {
		// f.add(this);
		faces.add(f);
		Vertex[] vs = f.getVertices();
		Edge[] es = f.getEdges();
		for (Vertex v : vs) {
			if (!vertices.contains(v)) {
				vertices.add(v);
			}
		}
		for (Edge e : es) {
			if (!edges.contains(e)) {
				edges.add(e);
			}
		}
	}

	/**
	 * Gets the edges.
	 * 
	 * @return the edges
	 */
	public Edge[] getEdges() {
		Edge es[] = new Edge[edges.size()];
		edges.toArray(es);
		return es;
	}

	/**
	 * Gets the faces.
	 * 
	 * @return the faces
	 */
	public Face[] getFaces() {
		Face[] fs = new Face[faces.size()];
		faces.toArray(fs);
		return fs;
	}

	/**
	 * Gets the geometry array.
	 * 
	 * @return the geometry array
	 */
	public abstract GeometryArray getGeometryArray();

	/**
	 * Gets the vertices.
	 * 
	 * @return the vertices
	 */
	public Vertex[] getVertices() {
		Vertex[] verts = new Vertex[vertices.size()];
		vertices.toArray(verts);
		return verts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[faces=" + faces.size() + ",edges=" + edges.size()
				+ ",vertices=" + vertices.size() + "]";
	}
}
