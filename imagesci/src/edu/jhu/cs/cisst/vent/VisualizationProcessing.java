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

import java.awt.Dimension;
import java.awt.Image;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl2.PGraphicsOpenGL2;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationProcessing.
 */
public abstract class VisualizationProcessing extends PApplet implements
		Visualization {

	/** The RENDE r_ method. */
	public static String RENDER_METHOD = "processing.opengl2.PGraphicsOpenGL2";

	/** The duration param. */
	protected ParamDouble durationParam;

	/** The frame rate param. */
	protected ParamInteger frameRateParam;;

	/** The movie height param. */
	protected ParamInteger movieHeightParam;

	/** The movie width param. */
	protected ParamInteger movieWidthParam;
	/** The name. */
	protected String name;

	/** The preferred height. */
	protected int preferredHeight;

	/** The preferred width. */
	protected int preferredWidth;

	/** The request screen shot. */
	protected boolean requestScreenShot = false;

	/** The screenshot. */
	protected PImage screenshot = null;

	/** The visualization parameters. */
	protected ParamCollection visualizationParameters;

	/**
	 * Instantiates a new visualization processing.
	 */
	public VisualizationProcessing() {
		this(VentPreferences.getInstance().getDefaultCanvasWidth(),
				VentPreferences.getInstance().getDefaultCanvasHeight());

	}

	/**
	 * Instantiates a new visualization processing.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public VisualizationProcessing(int width, int height) {
		this.preferredWidth = width;
		this.preferredHeight = height;
		setPreferredSize(new Dimension(width, height));
		setSize(new Dimension(width, height));
	}

	/**
	 * Creates the.
	 * 
	 * @return the param collection
	 * 
	 * @see edu.jhu.cs.cisst.vent.Visualization#create()
	 */
	@Override
	public ParamCollection create() {
		init();
		visualizationParameters = new ParamCollection(name);
		createVisualizationParameters(visualizationParameters);
		return visualizationParameters;
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
		/*
		 * ParamCollection prefsParam = new ParamCollection("Preferences");
		 * prefsParam.add(durationParam = new ParamDouble("Duration", 0, 1E6,
		 * 1)); prefsParam.add(frameRateParam = new ParamInteger("Frame Rate",
		 * 1, 10000000, 15)); prefsParam.add(movieWidthParam = new
		 * ParamInteger("Movie Width", 0, 1000000, 640));
		 * prefsParam.add(movieHeightParam = new ParamInteger("Movie Height", 0,
		 * 1000000, 480)); visualizationParameters.add(prefsParam);
		 */
	}

	/**
	 * Capture screenshot.
	 * 
	 * @return the p image
	 */
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
	 * Dispose.
	 * 
	 * @see edu.jhu.cs.cisst.vent.Visualization#dispose()
	 */
	@Override
	public void dispose() {
		super.destroy();
	}

	/**
	 * Gets the component.
	 * 
	 * @return the component
	 * 
	 * @see edu.jhu.cs.cisst.vent.Visualization#getComponent()
	 */
	@Override
	public PApplet getComponent() {
		return this;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.Visualization#getDuration()
	 */
	@Override
	public double getDuration() {
		return durationParam.getDouble();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.Visualization#getFrameRate()
	 */
	@Override
	public int getFrameRate() {
		return frameRateParam.getInt();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.Visualization#getMovieDimensions()
	 */
	@Override
	public Dimension getMovieDimensions() {
		return new Dimension(movieWidthParam.getInt(),
				movieHeightParam.getInt());
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 * 
	 * @see java.awt.Component#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the video frames.
	 * 
	 * @param frameRate
	 *            the frame rate
	 * @param duration
	 *            the duration
	 * @return the video frames
	 * @see edu.jhu.cs.cisst.vent.Visualization#getVideoFrames(long, long)
	 */
	public abstract Image[] getVideoFrames(long frameRate, long duration);

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @see java.awt.Component#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	@Override
	public void setup() {
		size(preferredWidth, preferredHeight, RENDER_METHOD);
	}

	protected void resizeRenderer(int iwidth, int iheight) {
	}
}