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

import java.util.Vector;

// TODO: Auto-generated Javadoc
/**
 * Created by IntelliJ IDEA. User: bennett Date: Nov 20, 2005 Time: 9:43:48 AM
 * To change this template use Options | File Templates.
 * ************************************ Magnetic Resonance in Medicine Final
 * Project Released: December 1, 2005
 * 
 * class Polyhedron Represent a triangulated 3D polyhedron.
 * 
 * Copyright (C) 2005 Bennett Landman, bennett@bme.jhu.edu
 */
public class Polyhedron {
	// Bounding box for all points in this polyhedron
	/** The bounding box. */
	BndBox boundingBox;

	// An array of faces
	/** The faces. */
	TriangleSigned faces[];

	// Create a new Polyhedron from a list of vertices and faces.
	// Note: faces are zero-indexed into the vertex array
	/**
	 * Instantiates a new polyhedron.
	 * 
	 * @param vertData
	 *            the vert data
	 * @param faceData
	 *            the face data
	 */
	public Polyhedron(double[][] vertData, double[][] faceData) {
		PT[] verts = new PT[vertData.length];
		for (int i = 0; i < verts.length; i++) {
			verts[i] = new PT(vertData[i][0], vertData[i][1], vertData[i][2]);
		}
		faces = new TriangleSigned[faceData.length];
		for (int i = 0; i < faces.length; i++) {
			faces[i] = new TriangleSigned(verts[(int) faceData[i][0]],
					verts[(int) faceData[i][1]], verts[(int) faceData[i][2]]);
		}
		boundingBox = new BndBox();
		for (int i = 0; i < faces.length; i++) {
			boundingBox.union(faces[i]);
		}
	}

	// Create a new Polyhedron from a Vector of Triangles
	/**
	 * Instantiates a new polyhedron.
	 * 
	 * @param in
	 *            the in
	 */
	public Polyhedron(Vector in) {
		faces = new TriangleSigned[in.size()];
		boundingBox = new BndBox();
		for (int i = 0; i < in.size(); i++) {
			faces[i] = (TriangleSigned) in.get(i);
			boundingBox.union(faces[i]);
		}
	}

	// detect if this polyhedron contains a particular point
	/**
	 * Contains.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return true, if successful
	 */
	public boolean contains(PT p) {
		// Algorithm: select a random direction. Count the number of faces that
		// the a ray from the point crosses
		// a face. If it is odd, then p is interior, if it is even, then p is
		// exterior. If the ray hits an edge or a
		// vertex, select a new random vector
		while (true) {
			try {
				PT p2 = new PT(p.x + (float) (Math.random() - .5) * 10000, p.y
						+ (float) (Math.random() - .5) * 10000, p.z
						+ (float) (Math.random() - .5) * 10000);
				int crossings = 0;
				for (int i = 0; i < faces.length; i++) {
					try {
						IntersectResult s = faces[i].findIntersect(p, p2);
						if (s.fractionalDistance > 0) {
							crossings++;
						}
						if (s.fractionalDistance == 0) {
							return true; // On an edge or vertex
						}
					} catch (Exception e) {
					}
					;

				}
				return (crossings % 2) == 1;
			} catch (Exception e) {
				// We are expecting a DegenerateIntersectionException. We need
				// to choose a new random ray.
			}
		}
	}

	// Find the location of the first intersection
	/**
	 * First intersection.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * 
	 * @return the intersect result
	 */
	public IntersectResult firstIntersection(PT a, PT b) {
		if ((!boundingBox.inside(a)) && (!boundingBox.inside(b))) {
			// assume that we will not jump across rois
			return null;
		}
		IntersectResult firstHit = null;
		float firstDist = Float.MAX_VALUE;
		for (int i = 0; i < faces.length; i++) {
			try {
				IntersectResult hit = faces[i].findIntersect(a, b);
				if (hit != null) {
					if (hit.fractionalDistance < firstDist
							&& hit.fractionalDistance > 0) {
						firstDist = hit.fractionalDistance;
						firstHit = hit;
					}
				}
			} catch (DegenerateIntersectionException e) {
				System.out.println(getClass().getCanonicalName() + "\t" + e);
			}
		}
		return firstHit;
	}

	// getter for the boundingbox
	/**
	 * Get Boundingbox.
	 *
	 * @return BndBox the BoundingBox enclosing the Polyhedron
	 */
	public BndBox getBndBox() {
		return boundingBox;
	}

	// This is a test function to create a unit cube
	/**
	 * Unit cube.
	 * 
	 * @return the polyhedron
	 */
	static Polyhedron unitCube() {
		Vector v = new Vector();
		PT PT000 = new PT(0, 0, 0);
		PT PT001 = new PT(0, 0, 1);
		PT PT010 = new PT(0, 1, 0);
		PT PT011 = new PT(0, 1, 1);
		PT PT100 = new PT(1, 0, 0);
		PT PT101 = new PT(1, 0, 1);
		PT PT110 = new PT(1, 1, 0);
		PT PT111 = new PT(1, 1, 1);
		// left
		v.add(new TriangleSigned(PT000, PT001, PT010));
		v.add(new TriangleSigned(PT011, PT001, PT010));
		// right
		v.add(new TriangleSigned(PT100, PT101, PT110));
		v.add(new TriangleSigned(PT111, PT101, PT110));
		// down
		v.add(new TriangleSigned(PT000, PT001, PT100));
		v.add(new TriangleSigned(PT101, PT001, PT100));
		// up
		v.add(new TriangleSigned(PT010, PT011, PT110));
		v.add(new TriangleSigned(PT111, PT011, PT110));
		// out
		v.add(new TriangleSigned(PT000, PT010, PT100));
		v.add(new TriangleSigned(PT110, PT010, PT100));
		// in
		v.add(new TriangleSigned(PT001, PT011, PT101));
		v.add(new TriangleSigned(PT111, PT011, PT101));
		return new Polyhedron(v);
		// return new Polyhedron(v);
	}
}
