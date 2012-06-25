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
package org.imagesci.utility;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

// TODO: Auto-generated Javadoc
/**
 * The Class PhantomTorus.
 */
public class PhantomTorus extends PhantomSimulator3D {
	/** The center. */
	protected Point3d center = new Point3d(0, 0, 0);

	/** The inner radius. */
	protected double innerRadius = 0.2;

	/** The outer radius. */
	protected double outerRadius = 0.6;

	/**
	 * Instantiates a new phantom torus.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomTorus(Point3i dims) {
		super(dims);
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
	 * Sets the inner radius.
	 *
	 * @param innerRadius the new inner radius
	 */
	public void setInnerRadius(double innerRadius) {
		this.innerRadius = innerRadius;
	}

	/**
	 * Sets the outer radius.
	 *
	 * @param outerRadius the new outer radius
	 */
	public void setOuterRadius(double outerRadius) {
		this.outerRadius = outerRadius;
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
					double xp = (x - center.x);
					double yp = (y - center.y);
					double zp = (z - center.z);
					double tmp = (outerRadius - Math.sqrt(zp * zp + yp * yp));
					levelset.set(i, j, k, tmp * tmp + xp * xp - innerRadius
							* innerRadius);
				}
			}
		}
		levelset.setName("torus_level");
		image.setName("torus");
		finish();
	}
}
