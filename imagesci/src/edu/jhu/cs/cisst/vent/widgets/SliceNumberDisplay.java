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
package edu.jhu.cs.cisst.vent.widgets;

import processing.core.PApplet;

// TODO: Auto-generated Javadoc
/**
 * The Interface SliceNumberDisplay.
 */
public interface SliceNumberDisplay {

	/**
	 * Draw.
	 * 
	 * @param applet
	 *            the applet
	 * @param sliceIndex
	 *            the slice index
	 * @param totalSlices
	 *            the total slices
	 * @param height
	 *            the height
	 */
	public void draw(PApplet applet, int sliceIndex, int totalSlices, int height);
}
