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

package org.imagesci.utility;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

// TODO: Auto-generated Javadoc

/**
 * The Class PhantomCube.
 */
public class PhantomCube extends PhantomSimulator3D {

	/** The center. */
	protected Point3d center;

	/** The width. */
	protected double width;

	/**
	 * Instantiates a new phantom cube.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomCube(Point3i dims) {
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
	 * Sets the width.
	 * 
	 * @param width
	 *            the new width
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
		// double scale=2.0/Math.max(rows,Math.max(cols,slices));

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					double x = (i - 0.5 * rows) / (float) (0.5 * rows);
					double y = (j - 0.5 * cols) / (float) (0.5 * cols);
					double z = (k - 0.5 * slices) / (float) (0.5 * slices);
					levelset.set(
							i,
							j,
							k,
							((x - center.x) > -0.5 * width
									&& (x - center.x) < 0.5 * width
									&& (y - center.y) > -0.5 * width
									&& (y - center.y) < 0.5 * width
									&& (z - center.z) > -0.5 * width && (z - center.z) < 0.5 * width) ? -1
									: 1);
				}
			}
		}
		levelset.setName("cube_level");
		image.setName("cube");
		finish();
	}
}
