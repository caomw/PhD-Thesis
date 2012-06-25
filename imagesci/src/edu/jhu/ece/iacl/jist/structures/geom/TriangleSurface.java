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

import java.util.ArrayList;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedTriangleArray;

// TODO: Auto-generated Javadoc
/**
 * The Class TriangleSurface.
 */
public class TriangleSurface extends Surface {

	/**
	 * Instantiates a new triangle surface.
	 * 
	 * @param surf
	 *            the surf
	 */
	public TriangleSurface(IndexedTriangleArray surf) {
		super(surf.getVertexCount());
		int szv = surf.getVertexCount();
		int szi = surf.getIndexCount();
		ArrayList<Vertex> verts = new ArrayList<Vertex>(szv);
		for (int i = 0; i < szv; i++) {
			Vertex v = new Vertex();
			surf.getCoordinate(i, v);
			verts.add(v);
		}
		int[] indices = new int[szi];
		surf.getCoordinateIndices(0, indices);
		for (int i = 0; i < szi; i += 3) {
			add(new TriangleFace(verts.get(indices[i]),
					verts.get(indices[i + 1]), verts.get(indices[i + 2])));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Surface#getGeometryArray()
	 */
	@Override
	public GeometryArray getGeometryArray() {

		int index = 0;
		Face[] faces = getFaces();
		Vertex[] verts = getVertices();
		// System.out.println(getClass().getCanonicalName()+"\t"+"CREATE SURFACE "+verts.length+" "+faces.length);
		IndexedTriangleArray surf = new IndexedTriangleArray(verts.length,
				GeometryArray.COORDINATES | GeometryArray.NORMALS,
				faces.length * 3);

		for (Vertex v : verts) {
			surf.setCoordinate(index, v);
			v.index = index++;
		}

		index = 0;
		for (Face f : faces) {
			Triangle tri = ((TriangleFace) f).triangle;
			surf.setCoordinateIndex(index++, tri.v1.index);
			surf.setCoordinateIndex(index++, tri.v2.index);
			surf.setCoordinateIndex(index++, tri.v3.index);
		}
		return surf;
	}

}
