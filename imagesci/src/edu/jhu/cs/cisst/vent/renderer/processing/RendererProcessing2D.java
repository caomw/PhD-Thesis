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
package edu.jhu.cs.cisst.vent.renderer.processing;

import java.awt.Rectangle;

import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.VisualizationProcessing2D;

// TODO: Auto-generated Javadoc
/**
 * The Class RendererProcessing2D.
 */
public abstract class RendererProcessing2D extends RendererProcessing {

	/** The bounds. */
	protected Rectangle.Float bounds = new Rectangle.Float(0, 0, 100, 100);

	/** The visualization. */
	protected VisualizationProcessing2D visualization;

	/**
	 * Contains.
	 * 
	 * @param px
	 *            the px
	 * @param py
	 *            the py
	 * 
	 * @return true, if successful
	 */
	public boolean contains(double px, double py) {
		return bounds.contains(px, py);
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public float getHeight() {
		return (float) bounds.getHeight();
	}

	/**
	 * Gets the position x.
	 * 
	 * @return the position x
	 */
	public float getPositionX() {
		return (float) bounds.getX();
	}

	/**
	 * Gets the position y.
	 * 
	 * @return the position y
	 */
	public float getPositionY() {
		return (float) bounds.getY();
	}

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public float getWidth() {
		return (float) bounds.getWidth();
	}

	/**
	 * Sets the bounds.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void setBounds(double x, double y, double width, double height) {
		bounds.setRect(x, y, width, height);
	}

	/**
	 * Sets the visualization.
	 * 
	 * @param vis
	 *            the new visualization
	 */
	@Override
	public void setVisualization(VisualizationProcessing vis) {
		this.visualization = (VisualizationProcessing2D) vis;
	}
}
