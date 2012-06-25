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

import javax.vecmath.Point2f;

import org.imagesci.utility.ContourArray;


// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsContour.
 */
public class SpringlsContour extends ContourArray {

	/** The capsules. */
	protected List<Springl2D> capsules;

	/**
	 * Instantiates a new springls contour.
	 * 
	 * @param contour
	 *            the contour
	 */
	public SpringlsContour(ContourArray contour) {
		super(contour.points, contour.indexes);
		capsules = new ArrayList<Springl2D>(contour.getIndexCount() / 2);
		int indexCount = this.getIndexCount();
		int N = indexCount / 2;
		float scaleDown = SpringlsConstants.scaleDown;
		float scaleUp = SpringlsConstants.scaleUp;
		double[][] cellData = contour.edgeData;
		if (cellData == null || cellData[0].length == 0) {
			this.edgeData = new double[N][6];
			for (int i = 0; i < indexCount; i += 2) {
				int faceId = i / 2;
				Point2f v1 = new Point2f(this.getVertex(contour
						.getCoordinateIndex(i)));
				Point2f v2 = new Point2f(this.getVertex(contour
						.getCoordinateIndex(i + 1)));
				v1.scale(scaleDown);
				v2.scale(scaleDown);
				Springl2D obj = (new Springl2D(v1, v2));
				this.edgeData[faceId][0] = scaleUp * obj.particle.x;
				this.edgeData[faceId][1] = scaleUp * obj.particle.y;
				this.edgeData[faceId][2] = scaleUp * obj.referencePoint.x;
				this.edgeData[faceId][3] = scaleUp * obj.referencePoint.y;
				obj.referenceId = faceId;
				obj.id = faceId;
				((ArrayList<Springl2D>) capsules).add(obj);
			}
		} else {
			System.out.println("Initialize Contour From Embedded Data");
			for (int i = 0; i < indexCount; i += 2) {
				int faceId = i / 2;
				Point2f v1 = this.getVertex(contour.getCoordinateIndex(i));
				Point2f v2 = this.getVertex(contour.getCoordinateIndex(i + 1));
				v1.scale(scaleDown);
				v2.scale(scaleDown);
				Point2f pt = new Point2f(scaleDown
						* (float) cellData[faceId][0], scaleDown
						* (float) cellData[faceId][1]);
				Point2f refPt = new Point2f(scaleDown
						* (float) cellData[faceId][2], scaleDown
						* (float) cellData[faceId][3]);
				Springl2D obj = (new Springl2D(v1, v2, pt, refPt));
				// obj.referenceId = (int) cellData[faceId][3];
				obj.id = faceId;
				((ArrayList<Springl2D>) capsules).add(obj);
			}
		}
	}

	/**
	 * Instantiates a new springls contour.
	 * 
	 * @param capsules
	 *            the capsules
	 * @param pts
	 *            the pts
	 * @param indexes
	 *            the indexes
	 * @param edgeData
	 *            the edge data
	 */
	public SpringlsContour(List<Springl2D> capsules, Point2f[] pts,
			int[] indexes, double[][] edgeData) {
		super(pts, indexes);
		this.capsules = capsules;
		this.edgeData = edgeData;
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new springls contour.
	 * 
	 * @param pts
	 *            the pts
	 * @param indexes
	 *            the indexes
	 */
	public SpringlsContour(Point2f[] pts, int[] indexes) {
		super(pts, indexes);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Creates the.
	 * 
	 * @param capsules
	 *            the capsules
	 * @return the springls surface
	 */
	public static SpringlsContour create(List<Springl2D> capsules) {
		int N = capsules.size();
		Point2f[] points = new Point2f[N * 2];
		int[] indexes = new int[N * 2];
		double[][] edgeData = new double[N][4];
		int index = 0;
		float scale = SpringlsConstants.scaleUp;
		for (Springl2D capsule : capsules) {
			int faceId = index / 2;
			edgeData[faceId][0] = scale * capsule.particle.x;
			edgeData[faceId][1] = scale * capsule.particle.y;
			edgeData[faceId][2] = scale * capsule.referencePoint.x;
			edgeData[faceId][3] = scale * capsule.referencePoint.y;
			for (int i = 0; i < 2; i++) {
				Point2f pt = points[index] = new Point2f(capsule.vertexes[i]);
				pt.x *= scale;
				pt.y *= scale;
				indexes[index] = index;
				index++;
			}
		}
		SpringlsContour surf = new SpringlsContour(capsules, points, indexes,
				edgeData);
		return surf;
	}

	/**
	 * Gets the capsules.
	 * 
	 * @return the capsules
	 */
	public List<Springl2D> getCapsules() {
		return capsules;
	}
}
