/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
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
 * The Class PhantomSphere.
 */
public class PhantomSphere extends PhantomSimulator3D {

	/** The center. */
	protected Point3d center = new Point3d(0, 0, 0);

	/** The radius. */
	protected double radius = 0.5;

	/**
	 * Instantiates a new phantom sphere.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomSphere(Point3i dims) {
		super(dims);
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
					double r = Math.sqrt((x - center.x) * (x - center.x)
							+ (y - center.y) * (y - center.y) + (z - center.z)
							* (z - center.z));
					levelset.set(i, j, k, (r - radius) / scale);
				}
			}
		}
		levelset.setName("sphere_level");
		image.setName("sphere");
		finish();
	}

	/**
	 * Finish.
	 */
	@Override
	protected void finish() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					double l = levelset.getDouble(i, j, k);
					double noise = 0;
					switch (noiseType) {
					case Uniform:
						noise = noiseLevel * (2 * randn.nextDouble() - 1);
						break;
					case Gaussian:
						noise = noiseLevel * randn.nextGaussian();
						break;
					}
					double v = noise
							+ PhantomSimulator3D.heaviside(l, fuzziness,
									heaviside);
					if (invertImage) {
						image.set(i, j, k, 1 - v);
					} else {
						image.set(i, j, k, v);
					}
				}
			}
		}
		IsoSurfaceGenerator isosurf = new IsoSurfaceGenerator();
		surf = isosurf.solve(levelset, 0);
		surf.setName(image.getName());
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
	 * Sets the radius.
	 * 
	 * @param radius
	 *            the new radius
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}
}
