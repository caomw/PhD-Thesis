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
 * Created by IntelliJ IDEA. User: bennett Date: Dec 14, 2005 Time: 11:27:30 AM
 * To change this template use Options | File Templates.
 */
public class Polygon {

	/** The bnd box. */
	public BndBox bndBox;

	/** The faces. */
	TriangleSigned faces[];

	// Create a new Polyhedron from a list of vertices and faces.
	// Note: faces are zero-indexed into the vertex array
	/**
	 * Instantiates a new polygon.
	 * 
	 * @param vertData
	 *            the vert data
	 * @param faceData
	 *            the face data
	 */
	public Polygon(double[][] vertData, double[][] faceData) {
		bndBox = new BndBox();
		PT[] verts = new PT[vertData.length];
		for (int i = 0; i < verts.length; i++) {
			verts[i] = new PT(vertData[i][0], vertData[i][1], vertData[i][2]);
			bndBox.union(verts[i]);
		}
		faces = new TriangleSigned[faceData.length];
		for (int i = 0; i < faces.length; i++) {
			// if(faceData[i].length<3) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"Polygon face length: "+i+" "+faceData[i].length);
			// }
			faces[i] = new TriangleSigned(verts[(int) faceData[i][0]],
					verts[(int) faceData[i][1]], verts[(int) faceData[i][2]]);
		}
	}

	/**
	 * Intersect.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * 
	 * @return true, if successful
	 */
	public boolean intersect(PT a, PT b) {
		for (int i = 0; i < faces.length; i++) {
			try {
				IntersectResult ir = faces[i].findIntersect(a, b);
				if (ir.fractionalDistance >= 0 && ir.fractionalDistance <= 1) {
					return true;
				} else {
					return false;
				}

			} catch (Exception e) {
				return false;
			}

			/*
			 * try { if(faces[i].intersect(a,b)) return true; } catch(Exception
			 * e) { return false; }
			 */
		}
		return false;
	}

	/**
	 * Report intersect.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * 
	 * @return the intersect result
	 */
	public IntersectResult reportIntersect(PT a, PT b) {
		for (int i = 0; i < faces.length; i++) {
			try {
				IntersectResult ir = faces[i].findIntersect(a, b);
				if (ir.fractionalDistance >= 0 && ir.fractionalDistance <= 1) {
					return ir;
					// else
					// return null;
				}

			} catch (Exception e) {
				// return null;
			}

			/*
			 * try { if(faces[i].intersect(a,b)) return true; } catch(Exception
			 * e) { return false; }
			 */
		}
		return null;
	}
}
