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

import javax.vecmath.Point3f;

// TODO: Auto-generated Javadoc
/**
 * The Class CurvePath.
 */
public class CurvePath implements Curve {

	/** The pts. */
	Vector<Point3f> pts;

	/**
	 * Instantiates a new curve path.
	 */
	public CurvePath() {
		pts = new Vector<Point3f>();
	}

	/**
	 * Adds the.
	 * 
	 * @param p
	 *            the p
	 */
	public void add(Point3f p) {
		pts.add(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Curve#getCurve()
	 */
	@Override
	public Point3f[] getCurve() {
		Point3f[] points = new Point3f[pts.size()];
		for (int i = 0; i < pts.size(); i++) {
			points[i] = pts.get(i);
		}
		return points;
	}

	/**
	 * Gets the points.
	 * 
	 * @return the points
	 */
	public float[][] getPoints() {
		float[][] points = new float[3][pts.size()];
		for (int i = 0; i < pts.size(); i++) {
			Point3f pt = pts.get(i);
			points[0][i] = pt.x;
			points[1][i] = pt.y;
			points[2][i] = pt.z;
		}
		return points;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Curve#getValue()
	 */
	@Override
	public double getValue() {
		double length = 0;
		for (int i = 1; i < pts.size(); i++) {
			length += pts.get(i - 1).distance(pts.get(i));
		}
		return length;
	}

}
