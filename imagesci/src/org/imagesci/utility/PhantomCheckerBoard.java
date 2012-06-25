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
 * The Class PhantomCube.
 */
public class PhantomCheckerBoard extends PhantomSimulator3D {

	/** The center. */
	protected Point3d center;

	/** The width. */
	protected double width;

	/** The x frequency. */
	protected double xFrequency = 1;

	/** The y frequency. */
	protected double yFrequency = 1;

	/** The z frequency. */
	protected double zFrequency = 1;

	/**
	 * Instantiates a new phantom cube.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomCheckerBoard(Point3i dims) {
		super(dims);
	}

	/**
	 * Gets the x frequency.
	 * 
	 * @return the x frequency
	 */
	public double getxFrequency() {
		return xFrequency;
	}

	/**
	 * Gets the y frequency.
	 * 
	 * @return the y frequency
	 */
	public double getyFrequency() {
		return yFrequency;
	}

	/**
	 * Gets the z frequency.
	 * 
	 * @return the z frequency
	 */
	public double getzFrequency() {
		return zFrequency;
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
	 * Sets the frequency.
	 *
	 * @param value the new frequency
	 */
	public void setFrequency(Point3d value) {
		this.xFrequency = value.x;
		this.yFrequency = value.y;
		this.zFrequency = value.z;
	}

	/**
	 * Sets the width.
	 * 
	 * @param width
	 *            the new width
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * Sets the x frequency.
	 * 
	 * @param xFrequency
	 *            the new x frequency
	 */
	public void setxFrequency(double xFrequency) {
		this.xFrequency = xFrequency;
	}

	/**
	 * Sets the y frequency.
	 * 
	 * @param yFrequency
	 *            the new y frequency
	 */
	public void setyFrequency(double yFrequency) {
		this.yFrequency = yFrequency;
	}

	/**
	 * Sets the z frequency.
	 * 
	 * @param zFrequency
	 *            the new z frequency
	 */
	public void setzFrequency(double zFrequency) {
		this.zFrequency = zFrequency;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.algorithms.util.phantom.PhantomSimulator3D#solve()
	 */
	@Override
	public void solve() {
		double scale = 2.0 / Math.min(rows, Math.min(cols, slices));
		double f = 2 * Math.PI / width;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					double x = (i - 0.5 * rows) * scale;
					double y = (j - 0.5 * cols) * scale;
					double z = (k - 0.5 * slices) * scale;
					levelset.set(
							i,
							j,
							k,
							(((x - center.x) > -0.5 * width
									&& (x - center.x) < 0.5 * width
									&& (y - center.y) > -0.5 * width
									&& (y - center.y) < 0.5 * width
									&& (z - center.z) > -0.5 * width && (z - center.z) < 0.5 * width) ? Math
									.signum(Math.sin(f * xFrequency * x)
											* Math.sin(f * yFrequency * y)
											* Math.sin(f * zFrequency * z)) : 1));
				}
			}
		}
		levelset.setName("checkerboard_level");
		image.setName("checkerboard");
		finish();
	}
}
