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
package edu.jhu.cs.cisst.vent.structures.processing;


import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.imagesci.utility.GeomUtil;

import processing.core.PApplet;
import processing.core.PConstants;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * The Class PTriangleMesh.
 */
public class PTriangleMesh {

	/** The norms. */
	protected Vector3f[] faceNorms;

	/** The indexes. */
	protected int[] indexes;
	/** The norms. */
	protected Vector3f[] norms;
	/** The points. */
	protected Point3f[] points;
	
	/**
	 * Instantiates a new p triangle mesh.
	 *
	 * @param surf the surf
	 */
	public PTriangleMesh(EmbeddedSurface surf) {
		this.points = surf.getVertexCopy();
		this.norms = surf.getNormalCopy();
		this.indexes = surf.getIndexCopy();
		faceNorms = new Vector3f[indexes.length / 3];
		for (int i = 0; i < indexes.length; i += 3) {
			Vector3f norm = cross(points[indexes[i]], points[indexes[i + 1]],
					points[indexes[i + 2]]);
			GeomUtil.normalize(norm);
			faceNorms[i / 3] = norm;
		}
	}

	/**
	 * Instantiates a new p triangle mesh.
	 * 
	 * @param points
	 *            the points
	 * @param norms
	 *            the norms
	 * @param indexes
	 *            the indexes
	 */
	public PTriangleMesh(Point3f[] points, Vector3f[] norms, int[] indexes) {
		this.points = points;
		this.norms = norms;
		this.indexes = indexes;
		faceNorms = new Vector3f[indexes.length / 3];
		for (int i = 0; i < indexes.length; i += 3) {
			Vector3f norm = cross(points[indexes[i]], points[indexes[i + 1]],
					points[indexes[i + 2]]);
			GeomUtil.normalize(norm);
			faceNorms[i / 3] = norm;
		}
	}

	/**
	 * Cross.
	 *
	 * @param pa the pa
	 * @param pb the pb
	 * @param pc the pc
	 * @return the vector3f
	 */
	public static Vector3f cross(Point3f pa, Point3f pb, Point3f pc) {
		Point3f p1 = new Point3f((pb.x - pa.x), (pb.y - pa.y), (pb.z - pa.z));
		Point3f p2 = new Point3f((pc.x - pa.x), (pc.y - pa.y), (pc.z - pa.z));
		Vector3f p3 = new Vector3f(p1.y * p2.z - p1.z * p2.y, p1.z * p2.x
				- p1.x * p2.z, p1.x * p2.y - p1.y * p2.x);
		return p3;
	}

	/**
	 * Draw.
	 * 
	 * @param applet
	 *            the applet
	 * @param gouraud
	 *            the gouraud
	 * @param flipNormals
	 *            the flip normals
	 */
	public void draw(PApplet applet, boolean gouraud, boolean flipNormals) {

		Point3f point;
		Vector3f norm;
		applet.beginShape(PConstants.TRIANGLES);
		// System.out.println("INDEX LENGTH "+indexes.length);
		for (int i = 0; i < indexes.length; i++) {
			int vid = indexes[i];
			point = points[vid];
			norm = norms[vid];
			applet.vertex(point.x, point.y, point.z);

			if (gouraud) {
				if (flipNormals) {
					applet.normal(-norm.x, -norm.y, -norm.z);
				} else {
					applet.normal(norm.x, norm.y, norm.z);
				}
			}
		}
		applet.endShape();

	}

	/**
	 * Gets the normals.
	 * 
	 * @return the normals
	 */
	public Vector3f[] getFaceNormals() {
		return faceNorms;
	}

	/**
	 * Gets the indexes.
	 *
	 * @return the indexes
	 */
	public int[] getIndexes() {
		return indexes;
	}

	/**
	 * Gets the normals.
	 * 
	 * @return the normals
	 */
	public Vector3f[] getNormals() {
		return norms;
	}

	/**
	 * Gets the points.
	 * 
	 * @return the points
	 */
	public Point3f[] getPoints() {
		return points;
	}
}
