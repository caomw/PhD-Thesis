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
 * The Class Face.
 */
public abstract class Face implements Comparable<Face> {
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Face o) {
		return this.hashCode() - o.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (this == obj);
	}

	/**
	 * Gets the edges.
	 * 
	 * @return the edges
	 */
	public abstract Edge[] getEdges();

	/*
	 * protected ArrayList<Surface> parents=new ArrayList<Surface>(); public
	 * ArrayList<Surface> getParentSurfaces(){ return parents; } public void
	 * add(Surface surf){ parents.add(surf); }
	 */
	/**
	 * Gets the vertices.
	 * 
	 * @return the vertices
	 */
	public abstract Vertex[] getVertices();
}
