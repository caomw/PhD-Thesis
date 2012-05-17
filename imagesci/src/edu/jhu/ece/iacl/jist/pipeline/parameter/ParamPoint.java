/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

// TODO: Auto-generated Javadoc
/**
 * Parameter that stores a 3d point.
 * 
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public interface ParamPoint {

	/**
	 * Get X coordinate parameter.
	 * 
	 * @return X coordinate
	 */
	public abstract ParamNumber getParamX();

	/**
	 * Get Y coordinate parameter.
	 * 
	 * @return Y coordinate
	 */
	public abstract ParamNumber getParamY();

	/**
	 * Get Z coordinate parameter.
	 * 
	 * @return Z coordinate
	 */
	public abstract ParamNumber getParamZ();
}
