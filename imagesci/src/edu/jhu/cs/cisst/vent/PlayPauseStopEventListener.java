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

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving playPauseStopEvent events.
 * The class that is interested in processing a playPauseStopEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addPlayPauseStopEventListener<code> method. When
 * the playPauseStopEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see PlayPauseStopEventEvent
 */
public interface PlayPauseStopEventListener {
	
	/**
	 * Pause event.
	 */
	public void pauseEvent();

	/**
	 * Play event.
	 */
	public void playEvent();

	/**
	 * Step event.
	 */
	public void stepEvent();

	/**
	 * Stop event.
	 */
	public void stopEvent();

}
