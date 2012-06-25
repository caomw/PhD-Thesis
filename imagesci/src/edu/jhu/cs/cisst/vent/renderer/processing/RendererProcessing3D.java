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
package edu.jhu.cs.cisst.vent.renderer.processing;

import javax.media.j3d.BoundingBox;

import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;

// TODO: Auto-generated Javadoc
/**
 * ImageSci Toolkit
 * 
 * Center for Computer-Integrated Surgical Systems and Technology & Johns
 * Hopkins Applied Physics Laboratory & The Johns Hopkins University
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 * 
 * @author Blake Lucas
 */
public abstract class RendererProcessing3D extends RendererProcessing {

	/** The bbox. */
	protected BoundingBox bbox = new BoundingBox();

	/** The visualization. */
	protected VisualizationProcessing3D visualization;

	/**
	 * Gets the bounding box.
	 * 
	 * @return the bounding box
	 */
	public BoundingBox getBoundingBox() {
		return bbox;
	}

	/**
	 * Sets the bounding box.
	 * 
	 * @param bbox
	 *            the new bounding box
	 */
	public void setBoundingBox(BoundingBox bbox) {
		this.bbox = bbox;
	}

	/**
	 * Sets the visualization.
	 * 
	 * @param vis
	 *            the new visualization
	 */
	@Override
	public void setVisualization(VisualizationProcessing vis) {
		this.visualization = (VisualizationProcessing3D) vis;
	}
}
