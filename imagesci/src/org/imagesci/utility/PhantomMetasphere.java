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
 * The Class PhantomMetasphere.
 */
public class PhantomMetasphere extends PhantomSimulator3D {

	/** The center. */
	protected Point3d center = new Point3d(0, 0, 0);

	/** The frequency. */
	protected double frequency = 6;

	/** The max amplitude. */
	protected double maxAmplitude = 0.9;

	/** The min amplitude. */
	protected double minAmplitude = 0.7;

	/**
	 * Instantiates a new phantom metasphere.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomMetasphere(Point3i dims) {
		super(dims);
		// TODO Auto-generated constructor stub
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
	 * @param frequency
	 *            the new frequency
	 */
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	/**
	 * Sets the max amplitude.
	 * 
	 * @param maxAmplitude
	 *            the new max amplitude
	 */
	public void setMaxAmplitude(double maxAmplitude) {
		this.maxAmplitude = maxAmplitude;
	}

	/**
	 * Sets the min amplitude.
	 * 
	 * @param minAmplitude
	 *            the new min amplitude
	 */
	public void setMinAmplitude(double minAmplitude) {
		this.minAmplitude = minAmplitude;
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

					double rXY = Math.sqrt((x - center.x) * (x - center.x)
							+ (y - center.y) * (y - center.y));

					double rXYZ = Math.sqrt((x - center.x) * (x - center.x)
							+ (y - center.y) * (y - center.y) + (z - center.z)
							* (z - center.z));
					double alpha = Math.atan2(y, x);
					double r1 = Math.sqrt(maxAmplitude * maxAmplitude
							- ((z - center.z) * (z - center.z)))
							/ maxAmplitude;
					double beta = Math.atan2(z, rXY);
					double d = (minAmplitude + (maxAmplitude - minAmplitude)
							* (Math.cos(alpha * frequency)));
					double r2 = (minAmplitude + (maxAmplitude - minAmplitude)
							* (Math.cos(2 * beta * frequency)));
					levelset.set(i, j, k, 0.5 * ((rXY - r1 * d) + (rXYZ - r2)));
				}
			}
		}
		levelset.setName("metasphere_level");
		image.setName("metasphere");
		finish();
	}
}
