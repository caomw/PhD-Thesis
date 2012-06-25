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
 * The Class PhantomBubbles.
 */
public class PhantomBubbles extends PhantomSimulator3D {

	/** The max radius. */
	protected double maxRadius;

	/** The min radius. */
	protected double minRadius;

	/** The num bubbles. */
	protected int numBubbles;

	/**
	 * Instantiates a new phantom bubbles.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomBubbles(Point3i dims) {
		super(dims);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets the max radius.
	 * 
	 * @param maxRadius
	 *            the new max radius
	 */
	public void setMaxRadius(double maxRadius) {
		this.maxRadius = maxRadius;
	}

	/**
	 * Sets the min radius.
	 * 
	 * @param minRadius
	 *            the new min radius
	 */
	public void setMinRadius(double minRadius) {
		this.minRadius = minRadius;
	}

	/**
	 * Sets the number of bubbles.
	 * 
	 * @param numBubbles
	 *            the new number of bubbles
	 */
	public void setNumberOfBubbles(int numBubbles) {
		this.numBubbles = numBubbles;
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
					levelset.set(i, j, k, 1E10);
				}
			}
		}
		for (int n = 0; n < numBubbles; n++) {

			double v = randn.nextDouble();
			double ra = (1 - v) * minRadius + v * maxRadius;
			Point3d center = new Point3d((2 * randn.nextDouble() - 1)
					* (1 - ra - 4.0 / rows), (2 * randn.nextDouble() - 1)
					* (1 - ra - 4.0 / cols), (2 * randn.nextDouble() - 1)
					* (1 - ra - 4.0 / slices));

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						double x = (i - 0.5 * rows) * scale;
						double y = (j - 0.5 * cols) * scale;
						double z = (k - 0.5 * slices) * scale;
						double r = Math.sqrt((x - center.x) * (x - center.x)
								+ (y - center.y) * (y - center.y)
								+ (z - center.z) * (z - center.z));
						levelset.set(i, j, k,
								Math.min(levelset.getFloat(i, j, k), r - ra));
					}
				}
			}
		}
		levelset.setName("bubbles_level");
		image.setName("bubbles");
		finish();
	}
}
