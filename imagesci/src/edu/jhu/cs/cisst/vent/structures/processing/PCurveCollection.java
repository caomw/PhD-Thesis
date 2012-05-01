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
package edu.jhu.cs.cisst.vent.structures.processing;

import javax.vecmath.Point3f;

import processing.core.PApplet;
import processing.core.PConstants;
import edu.jhu.ece.iacl.jist.structures.geom.CurveCollection;

// TODO: Auto-generated Javadoc
/**
 * The Class PCurveCollection wraps a curve collection for rendering.
 */
public class PCurveCollection {

	/** The points. */
	Point3f[][] points;

	/** The wrap. */
	protected boolean wrap = false, smooth = true;

	/**
	 * Instantiates a new p curve collection.
	 * 
	 * @param curves
	 *            the curves
	 * @param wrap
	 *            the wrap
	 */
	public PCurveCollection(CurveCollection curves, boolean wrap) {
		this.points = curves.getCurves();
		this.wrap = wrap;
	}

	/**
	 * Instantiates a new p curve collection.
	 *
	 * @param curves the curves
	 * @param wrap the wrap
	 * @param smooth the smooth
	 */
	public PCurveCollection(CurveCollection curves, boolean wrap, boolean smooth) {
		this.points = curves.getCurves();
		this.wrap = wrap;
		this.smooth = smooth;
	}

	/**
	 * Draw in 2D.
	 *
	 * @param applet the applet
	 */
	public void draw2D(PApplet applet) {

		for (int i = 0; i < points.length; i++) {
			Point3f[] line = points[i];
			applet.beginShape();
			Point3f pt1;
			for (int j = 0; j < line.length; j++) {
				pt1 = line[j];
				applet.vertex(pt1.x, pt1.y);
			}
			if (wrap) {
				applet.endShape(PConstants.CLOSE);
			} else {
				applet.endShape();
			}
		}
	}

	/**
	 * Draw in 3D.
	 *
	 * @param applet the applet
	 */
	public void draw3D(PApplet applet) {
		Point3f pt1;
		Point3f pt2;
		Point3f pt3;
		Point3f pt4;
		for (int i = 0; i < points.length; i++) {
			Point3f[] line = points[i];
			for (int j = 0; j < line.length; j++) {
				if (wrap) {
					pt1 = line[j];
					pt2 = line[(j + 1) % line.length];
					pt3 = line[(j + 2) % line.length];
					pt4 = line[(j + 3) % line.length];

				} else {
					pt1 = line[j];
					pt2 = line[Math.min(j + 1, line.length - 1)];
					pt3 = line[Math.min(j + 2, line.length - 1)];
					pt4 = line[Math.min(j + 3, line.length - 1)];

				}
				if (smooth) {
					applet.curve(pt1.x, pt1.y, pt1.z, pt2.x, pt2.y, pt2.z,
							pt3.x, pt3.y, pt3.z, pt4.x, pt4.y, pt4.z);
				} else {

					applet.line(pt1.x, pt1.y, pt1.z, pt2.x, pt2.y, pt2.z);
				}
			}
		}
	}

	/**
	 * Gets the points.
	 *
	 * @return the points
	 */
	public Point3f[][] getPoints() {
		return points;
	}
}
