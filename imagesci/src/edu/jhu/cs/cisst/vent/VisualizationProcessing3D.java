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
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import processing.core.PImage;
import processing.opengl2.PGraphicsOpenGL2;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationProcessing3D.
 */
public abstract class VisualizationProcessing3D extends VisualizationProcessing {
	
	/** The adjusting. */
	protected boolean adjusting = false;

	/** The renderers. */
	protected ArrayList<RendererProcessing3D> renderers;

	/**
	 * Instantiates a new visualization processing 3d.
	 */
	public VisualizationProcessing3D() {
		super();
		renderers = new ArrayList<RendererProcessing3D>();
	}

	/**
	 * Instantiates a new visualization processing 3d.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public VisualizationProcessing3D(int width, int height) {
		super(width, height);
		renderers = new ArrayList<RendererProcessing3D>();
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

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#captureScreenshot()
	 */
	@Override
	public PImage captureScreenshot() {
		GL2 gl = (GL2) ((PGraphicsOpenGL2) this.g).gl;
		PImage img = new PImage(width, height);
		img.parent = this;
		int[] tmp = new int[width * height];
		IntBuffer buff = IntBuffer.wrap(tmp);
		gl.glReadPixels(0, 0, width, height, GL2GL3.GL_BGRA,
				GL.GL_UNSIGNED_BYTE, buff);
		int index = 0;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				img.set(i, height - j - 1, tmp[index++]);
			}
		}
		return img;
	}

	/**
	 * Adds a new processing renderer to the rendering pipeline.
	 * 
	 * @param renderer
	 *            the renderer
	 */
	public void add(RendererProcessing3D renderer) {
		renderers.add(renderer);
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
	 * Checks if is adjusting.
	 *
	 * @return true, if is adjusting
	 */
	public boolean isAdjusting() {
		return adjusting;
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
