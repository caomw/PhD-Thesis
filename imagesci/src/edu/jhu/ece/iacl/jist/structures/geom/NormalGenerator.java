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

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

// TODO: Auto-generated Javadoc
/**
 * The Class NormalGenerator.
 */
public class NormalGenerator {

	/**
	 * Generate.
	 * 
	 * @param mesh
	 *            the mesh
	 * 
	 * @return the vector3f[]
	 */
	public static Vector3f[] generate(EmbeddedSurface mesh) {
		int[] indices = new int[mesh.getIndexCount()];
		Point3d[] points = new Point3d[mesh.getVertexCount()];
		Vector3f[] vectors = new Vector3f[points.length];
		int in1, in2, in3;
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point3d();
			mesh.getCoordinate(i, points[i]);
			vectors[i] = new Vector3f();
		}
		mesh.getCoordinateIndices(0, indices);
		for (int i = 0; i < indices.length; i += 3) {
			Vector3f p = cross(points[in1 = indices[i]],
					points[in2 = indices[i + 1]], points[in3 = indices[i + 2]]);
			if (p.length() == 0) {
				System.err.println("jist.base" + "NO AREA " + p + " "
						+ points[in1 = indices[i]] + " "
						+ points[in2 = indices[i + 1]] + " "
						+ points[in3 = indices[i + 2]]);
			}
			p.normalize();
			vectors[in1].add(p);
			vectors[in2].add(p);
			vectors[in3].add(p);

		}
		for (int i = 0; i < vectors.length; i++) {
			if (vectors[i].length() > 0) {
				vectors[i].normalize();
			}
		}
		return vectors;
	}

	/**
	 * Cross.
	 * 
	 * @param pa
	 *            the pa
	 * @param pb
	 *            the pb
	 * @param pc
	 *            the pc
	 * 
	 * @return the vector3f
	 */
	private static Vector3f cross(Point3d pa, Point3d pb, Point3d pc) {
		Point3f p1 = new Point3f((float) (pb.x - pa.x), (float) (pb.y - pa.y),
				(float) (pb.z - pa.z));
		Point3f p2 = new Point3f((float) (pc.x - pa.x), (float) (pc.y - pa.y),
				(float) (pc.z - pa.z));
		Vector3f p3 = new Vector3f(p1.y * p2.z - p1.z * p2.y, p1.z * p2.x
				- p1.x * p2.z, p1.x * p2.y - p1.y * p2.x);
		return p3;
	}

	/**
	 * Generate.
	 * 
	 * @param points
	 *            the points
	 * @param indices
	 *            the indices
	 * 
	 * @return the vector3f[]
	 */
	public static Vector3f[] generate(Point3f[] points, int indices[]) {
		Vector3f[] vectors = new Vector3f[points.length];
		int in1, in2, in3;
		for (int i = 0; i < points.length; i++) {
			vectors[i] = new Vector3f();
		}
		for (int i = 0; i < indices.length; i += 3) {
			Vector3f p = cross(points[in1 = indices[i]],
					points[in2 = indices[i + 1]], points[in3 = indices[i + 2]]);
			/*
			 * if(p.length()==0){
			 * System.err.println("jist.base"+"NO AREA "+p+" "
			 * +points[in1=indices
			 * [i]]+" "+points[in2=indices[i+1]]+" "+points[in3=indices[i+2]]);
			 * }
			 */
			vectors[in1].add(p);
			vectors[in2].add(p);
			vectors[in3].add(p);
		}
		for (int i = 0; i < vectors.length; i++) {
			if (vectors[i].length() > 0) {
				vectors[i].normalize();
			} // else {
				// System.err.println("jist.base"+"Could not Normalize "+vectors[i]);
			// }
		}
		return vectors;
	}

	/**
	 * Cross.
	 * 
	 * @param pa
	 *            the pa
	 * @param pb
	 *            the pb
	 * @param pc
	 *            the pc
	 * 
	 * @return the vector3f
	 */
	private static Vector3f cross(Point3f pa, Point3f pb, Point3f pc) {
		Point3f p1 = new Point3f((pb.x - pa.x), (pb.y - pa.y), (pb.z - pa.z));
		Point3f p2 = new Point3f((pc.x - pa.x), (pc.y - pa.y), (pc.z - pa.z));
		Vector3f p3 = new Vector3f(p1.y * p2.z - p1.z * p2.y, p1.z * p2.x
				- p1.x * p2.z, p1.x * p2.y - p1.y * p2.x);
		return p3;
	}
}
