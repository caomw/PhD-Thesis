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
package edu.jhu.cs.cisst.vent;

import java.awt.Dimension;

// TODO: Auto-generated Javadoc
/**
 * The Class VentPreferences.
 */
public class VentPreferences {
	
	/** The default canvas size. */
	protected static Dimension defaultCanvasSize = new Dimension(600, 600);

	/** The preferences. */
	protected static VentPreferences preferences;

	/**
	 * Gets the single instance of VentPreferences.
	 *
	 * @return single instance of VentPreferences
	 */
	public static VentPreferences getInstance() {
		if (preferences == null) {
			preferences = new VentPreferences();
		}
		return preferences;
	}

	/**
	 * Gets the default canvas height.
	 *
	 * @return the default canvas height
	 */
	public int getDefaultCanvasHeight() {
		return defaultCanvasSize.height;
	}

	/**
	 * Gets the default canvas size.
	 *
	 * @return the default canvas size
	 */
	public Dimension getDefaultCanvasSize() {
		return defaultCanvasSize;
	}

	/**
	 * Gets the default canvas width.
	 *
	 * @return the default canvas width
	 */
	public int getDefaultCanvasWidth() {
		return defaultCanvasSize.width;
	}

	/**
	 * Sets the default canvas size.
	 *
	 * @param defaultCanvasSize the new default canvas size
	 */
	public void setDefaultCanvasSize(Dimension defaultCanvasSize) {
		VentPreferences.defaultCanvasSize = defaultCanvasSize;
	}
}
