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

package org.imagesci.utility;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

// TODO: Auto-generated Javadoc
/**
 * The Class PhantomSphere.
 */
public class PhantomZalesakSphere extends PhantomSimulator3D {
	/** The center. */
	protected Point3d center;
	
	/** The depth. */
	protected double depth;

	/** The radius. */
	protected double radius;

	/** The width. */
	protected double width;

	/**
	 * Instantiates a new phantom sphere.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomZalesakSphere(Point3i dims) {
		super(dims);
	}

	/**
	 * Gets the depth.
	 *
	 * @return the depth
	 */
	public double getDepth() {
		return depth;
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Sets the center.
	 * 
	 * @param center
	 *            the new center
	 */
	public void setCenter(Point3d center) {
		this.center = center;
	}

	/**
	 * Sets the depth.
	 *
	 * @param depth the new depth
	 */
	public void setDepth(double depth) {
		this.depth = depth;
	}

	/**
	 * Sets the radius.
	 * 
	 * @param radius
	 *            the new radius
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * Sets the width.
	 *
	 * @param width the new width
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.algorithms.util.phantom.PhantomSimulator3D#solve()
	 */
	@Override
	public void solve() {
		double scale = 2.0 / Math.min(rows, Math.min(cols, slices));

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					double x = (i - 0.5 * rows) * scale;
					double y = (j - 0.5 * cols) * scale;
					double z = (k - 0.5 * slices) * scale;
					if (y > center.y - radius && y < center.y - radius + depth
							&& x > center.x - width * 0.5f
							&& x < center.x + width * 0.5f) {
						levelset.set(i, j, k, 5);
					} else {
						double r = Math.sqrt((x - center.x) * (x - center.x)
								+ (y - center.y) * (y - center.y)
								+ (z - center.z) * (z - center.z));
						levelset.set(i, j, k, r - radius);
					}
				}
			}
		}
		levelset.setName("milled_sphere_level");
		image.setName("milled_sphere");
		finish();
	}
}
