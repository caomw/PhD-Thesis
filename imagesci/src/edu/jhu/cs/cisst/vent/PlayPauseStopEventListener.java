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
