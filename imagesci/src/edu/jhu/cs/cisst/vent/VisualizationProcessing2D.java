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

import java.awt.Image;
import java.util.ArrayList;

import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing2D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationProcessing2D.
 */
public abstract class VisualizationProcessing2D extends VisualizationProcessing {

	/** The renderers. */
	protected ArrayList<RendererProcessing2D> renderers;

	/**
	 * Instantiates a new visualization processing 2d.
	 */
	public VisualizationProcessing2D() {
		super();
		renderers = new ArrayList<RendererProcessing2D>();
	}

	/**
	 * Instantiates a new visualization processing 2d.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public VisualizationProcessing2D(int width, int height) {
		super(width, height);
		renderers = new ArrayList<RendererProcessing2D>();
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		super.createVisualizationParameters(visualizationParameters);
		for (RendererProcessing renderer : renderers) {
			ParamCollection pane = new ParamCollection();
			renderer.createVisualizationParameters(pane);
			visualizationParameters.add(pane);
		}
	}

	/**
	 * Draw.
	 * 
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		for (RendererProcessing renderer : renderers) {
			renderer.draw();
		}

		if (requestScreenShot) {
			screenshot = captureScreenshot();
			screenshot.parent = this;
			requestScreenShot = false;
		}

	}

	/**
	 * Gets the screenshot.
	 * 
	 * @return the screenshot
	 * 
	 * @see edu.jhu.cs.cisst.vent.Visualization#getScreenshot()
	 */
	@Override
	public Image getScreenshot() {
		screenshot = null;
		requestScreenShot = true;

		while (requestScreenShot) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return screenshot.getImage();
	}

	/**
	 * Setup.
	 * 
	 * @see processing.core.PApplet#setup()
	 */
	@Override
	public void setup() {
		super.setup();
		for (RendererProcessing renderer : renderers) {
			renderer.setVisualization(this);
			renderer.setup();

		}
		loop();
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		for (RendererProcessing renderer : renderers) {
			renderer.update(model, view);
		}
	}

	/**
	 * Update visualization parameters.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		for (RendererProcessing renderer : renderers) {
			renderer.updateVisualizationParameters();
		}
	}

}
