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

import javax.vecmath.Point3f;

// TODO: Auto-generated Javadoc
/**
 * The Interface Curve.
 */
public interface Curve {

	/**
	 * Gets the curve.
	 * 
	 * @return the curve
	 */
	public Point3f[] getCurve();

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public double getValue();

}
