/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.springls;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsSurface.
 */
public class SpringlsSurface extends EmbeddedSurface {

	/** The capsules. */
	protected List<Springl3D> capsules;

	/** The label masks. */
	protected int[] labelMasks;

	/**
	 * Instantiates a new springls surface.
	 * 
	 * @param surf
	 *            the surf
	 */
	public SpringlsSurface(EmbeddedSurface surf) {
		super(surf);

		setCellData(surf.getCellData());
		capsules = new ArrayList<Springl3D>(surf.getIndexCount() / 3);
		int indexCount = this.getIndexCount();
		int N = indexCount / 3;
		float scaleDown = SpringlsConstants.scaleDown;
		float scaleUp = SpringlsConstants.scaleUp;
		double[][] cellData = this.getCellData();
		if (cellData == null || cellData[0].length == 0) {
			this.cellData = new double[N][6];
			for (int i = 0; i < indexCount; i += 3) {
				int faceId = i / 3;
				Point3f v1 = this.getVertex(surf.getCoordinateIndex(i));
				Point3f v2 = this.getVertex(surf.getCoordinateIndex(i + 1));
				Point3f v3 = this.getVertex(surf.getCoordinateIndex(i + 2));
				v1.scale(scaleDown);
				v2.scale(scaleDown);
				v3.scale(scaleDown);
				Springl3D obj = (new Springl3D(v1, v2, v3));
				this.cellData[faceId][0] = scaleUp * obj.particle.x;
				this.cellData[faceId][1] = scaleUp * obj.particle.y;
				this.cellData[faceId][2] = scaleUp * obj.particle.z;
				this.cellData[faceId][3] = scaleUp * obj.referencePoint.x;
				this.cellData[faceId][4] = scaleUp * obj.referencePoint.y;
				this.cellData[faceId][5] = scaleUp * obj.referencePoint.z;
				obj.referenceId = 1;
				obj.id = 1;
				((ArrayList<Springl3D>) capsules).add(obj);
			}
		} else if (cellData[0].length == 1) {
			System.err.println("Constellation has labels");
			this.cellData = new double[N][7];
			for (int i = 0; i < indexCount; i += 3) {
				int faceId = i / 3;
				Point3f v1 = this.getVertex(surf.getCoordinateIndex(i));
				Point3f v2 = this.getVertex(surf.getCoordinateIndex(i + 1));
				Point3f v3 = this.getVertex(surf.getCoordinateIndex(i + 2));
				v1.scale(scaleDown);
				v2.scale(scaleDown);
				v3.scale(scaleDown);
				Springl3D obj = (new Springl3D(v1, v2, v3));
				this.cellData[faceId][0] = scaleUp * obj.particle.x;
				this.cellData[faceId][1] = scaleUp * obj.particle.y;
				this.cellData[faceId][2] = scaleUp * obj.particle.z;
				this.cellData[faceId][3] = scaleUp * obj.referencePoint.x;
				this.cellData[faceId][4] = scaleUp * obj.referencePoint.y;
				this.cellData[faceId][5] = scaleUp * obj.referencePoint.z;
				this.cellData[faceId][6] = cellData[faceId][0];
				obj.referenceId = (int) cellData[faceId][0];
				obj.id = (int) cellData[faceId][0];
				((ArrayList<Springl3D>) capsules).add(obj);
			}
		} else {
			System.out.println("Initialize Surface From Embedded Data");
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			for (int i = 0; i < indexCount; i += 3) {
				int faceId = i / 3;
				Point3f v1 = this.getVertex(surf.getCoordinateIndex(i));
				Point3f v2 = this.getVertex(surf.getCoordinateIndex(i + 1));
				Point3f v3 = this.getVertex(surf.getCoordinateIndex(i + 2));
				v1.scale(scaleDown);
				v2.scale(scaleDown);
				v3.scale(scaleDown);
				Point3f pt = new Point3f(scaleDown
						* (float) cellData[faceId][0], scaleDown
						* (float) cellData[faceId][1], scaleDown
						* (float) cellData[faceId][2]);
				Point3f refPt = new Point3f(scaleDown
						* (float) cellData[faceId][3], scaleDown
						* (float) cellData[faceId][4], scaleDown
						* (float) cellData[faceId][5]);
				Springl3D obj = (new Springl3D(v1, v2, v3, pt, refPt));
				if (cellData[faceId].length > 6) {
					obj.referenceId = (int) Math.abs(cellData[faceId][6]);
					labelHash.add(obj.referenceId);
				} else {
					obj.referenceId = i / 3;
				}
				obj.id = faceId;
				((ArrayList<Springl3D>) capsules).add(obj);
			}
			int l = 1;
			int numLabels = labelHash.size();
			labelMasks = new int[numLabels + 1];
			labelMasks[0] = 0;
			for (Integer val : labelHash) {
				labelMasks[l++] = val;
			}
		}
	}

	/**
	 * Instantiates a new springls surface.
	 * 
	 * @param capsules
	 *            the capsules
	 * @param points
	 *            the points
	 * @param indexes
	 *            the indexes
	 * @param cellData
	 *            the cell data
	 */
	public SpringlsSurface(List<Springl3D> capsules, Point3f[] points,
			int[] indexes, double[][] cellData) {
		super(points, indexes);
		this.capsules = capsules;
		this.setCellData(cellData);
		this.setVertexData(vertexData);
	}

	/**
	 * Creates the.
	 * 
	 * @param capsules
	 *            the capsules
	 * @return the springls surface
	 */
	public static SpringlsSurface create(List<Springl3D> capsules) {
		int N = capsules.size();
		Point3f[] points = new Point3f[N * 3];
		int[] indexes = new int[N * 3];
		double[][] faceData = new double[N][6];
		int index = 0;
		float scale = SpringlsConstants.scaleUp;
		for (Springl3D capsule : capsules) {
			int faceId = index / 3;
			faceData[faceId][0] = scale * capsule.particle.x;
			faceData[faceId][1] = scale * capsule.particle.y;
			faceData[faceId][2] = scale * capsule.particle.z;
			faceData[faceId][3] = scale * capsule.referencePoint.x;
			faceData[faceId][4] = scale * capsule.referencePoint.y;
			faceData[faceId][5] = scale * capsule.referencePoint.z;
			for (int i = 0; i < 3; i++) {
				Point3f pt = points[index] = new Point3f(capsule.vertexes[i]);
				pt.x *= scale;
				pt.y *= scale;
				pt.z *= scale;
				indexes[index] = index;
				index++;
			}
		}
		SpringlsSurface surf = new SpringlsSurface(capsules, points, indexes,
				faceData);
		return surf;
	}

	/**
	 * Gets the capsules.
	 * 
	 * @return the capsules
	 */
	public List<Springl3D> getCapsules() {
		return capsules;
	}

	/**
	 * Gets the label masks.
	 *
	 * @return the label masks
	 */
	public int[] getLabelMasks() {
		return labelMasks;
	}

	/**
	 * Gets the num labels.
	 *
	 * @return the num labels
	 */
	public int getNumLabels() {
		return (labelMasks == null) ? 1 : labelMasks.length;
	}

	/**
	 * Gets the num objects.
	 *
	 * @return the num objects
	 */
	public int getNumObjects() {
		return (labelMasks == null) ? 1 : labelMasks.length - 1;
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
