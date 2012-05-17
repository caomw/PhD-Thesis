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
package edu.jhu.cs.cisst.vent.renderer.processing;

import edu.jhu.cs.cisst.vent.VisualizationParameters;
import edu.jhu.cs.cisst.vent.VisualizationProcessing;

// TODO: Auto-generated Javadoc
/**
 * The Class RendererProcessing.
 */
public abstract class RendererProcessing implements VisualizationParameters {

	/**
	 * Draw.
	 */
	public abstract void draw();

	/**
	 * Setup.
	 */
	public abstract void setup();

	/**
	 * Set visualization.
	 * 
	 * @param vis
	 *            the vis
	 */
	public abstract void setVisualization(VisualizationProcessing vis);

}
