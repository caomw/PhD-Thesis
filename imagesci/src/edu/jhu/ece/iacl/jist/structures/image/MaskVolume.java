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
package edu.jhu.ece.iacl.jist.structures.image;

// TODO: Auto-generated Javadoc

/**
 * Mask Interface.
 * 
 * @author Blake Lucas
 */
public interface MaskVolume {

	/**
	 * Gets the neighbors x.
	 * 
	 * @return the neighbors x
	 */
	public byte[] getNeighborsX();

	/**
	 * Gets the neighbors y.
	 * 
	 * @return the neighbors y
	 */
	public byte[] getNeighborsY();

	/**
	 * Gets the neighbors z.
	 * 
	 * @return the neighbors z
	 */
	public byte[] getNeighborsZ();
}
