/**
 * JIST Extensions for Computer-Integrated Surgery
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
 * @author Blake Lucas
 */
package edu.jhu.cs.cisst.vent;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;

// TODO: Auto-generated Javadoc
/**
 * The Interface Visualization.
 */
public interface Visualization extends VisualizationParameters {

	/**
	 * Creates the visualization and returns parameters used to control the
	 * visualziation.
	 * 
	 * @return the param collection
	 */
	public ParamCollection create();

	/**
	 * Dispose.
	 */
	public void dispose();

	/**
	 * Gets the component used to display the visualization.
	 * 
	 * @return the component
	 */
	public Component getComponent();

	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public double getDuration();

	/**
	 * Gets the frame rate.
	 * 
	 * @return the frame rate
	 */
	public int getFrameRate();

	/**
	 * Gets the movie dimensions.
	 * 
	 * @return the movie dimensions
	 */
	public Dimension getMovieDimensions();

	/**
	 * Gets the video frames.
	 *
	 * @return the video frames
	 */
	// public Image[] getVideoFrames(long frameRate, long duration);

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the screenshot.
	 * 
	 * @return the screenshot
	 */
	public Image getScreenshot();

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name);
}
