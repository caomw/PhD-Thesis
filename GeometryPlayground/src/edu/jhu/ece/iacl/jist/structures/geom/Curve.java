/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
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
