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
package edu.jhu.cs.cisst.vent.renderer.processing;

import java.awt.Color;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.converter.processing.ConvertImageDataToPImage;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.VoxelType;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageRenderer.
 */
public class ImageRenderer2D extends RendererProcessing2D {
	/** The applet. */
	protected VisualizationProcessing applet;
	/** The brightness. */
	protected float brightness = 0;

	/** The brightness param. */
	protected ParamFloat brightnessParam;

	/** The contrast. */
	protected float contrast = 1;

	/** The contrast param. */
	protected ParamFloat contrastParam;

	/** The image. */
	protected ImageData image;

	/** The images. */
	protected PImage images = null;

	/** The img name. */
	protected String imgName = "Image";

	/** The cols. */
	protected int rows, cols;

	/** The tint color. */
	protected Color tintColor;

	/** The tint param. */
	protected ParamColor tintParam;

	/** The transparency. */
	protected float transparency = 1;

	/** The transparency param. */
	protected ParamFloat transparencyParam;

	/** The visible. */
	protected boolean visible = true;

	/** The visible param. */
	protected ParamBoolean visibleParam;

	/**
	 * Instantiates a new image renderer.
	 *
	 * @param img the img
	 * @param name the name
	 * @param applet the applet
	 */
	public ImageRenderer2D(ImageData img, String name,
			VisualizationProcessing applet) {
		this.image = img;
		imgName = name;
		rows = image.getRows();
		cols = image.getCols();
		this.applet = applet;
	}

	/**
	 * Instantiates a new image renderer.
	 * 
	 * @param img
	 *            the img
	 * @param applet
	 *            the applet
	 */
	public ImageRenderer2D(ImageData img, VisualizationProcessing applet) {
		this.image = img;
		if (img.getName() != null) {
			imgName = img.getName();
		}
		rows = image.getRows();
		cols = image.getCols();
		this.applet = applet;
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
		if (model == contrastParam) {
			setContrast(contrastParam.getFloat());
		} else if (model == brightnessParam) {
			setBrightness(brightnessParam.getFloat());
		} else if (model == transparencyParam) {
			setTransparency(transparencyParam.getFloat());
		} else if (model == visibleParam) {
			setVisible(visibleParam.getValue());
		} else if (model == tintParam) {
			setTintColor(tintParam.getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters
	 * ()
	 */
	@Override
	public void updateVisualizationParameters() {
		setContrast(contrastParam.getFloat());
		setBrightness(brightnessParam.getFloat());
		setTransparency(transparencyParam.getFloat());
		setVisible(visibleParam.getValue());
		setTintColor(tintParam.getValue());
	}

	/**
	 * Sets the contrast.
	 * 
	 * @param contrast
	 *            the new contrast
	 */
	public void setContrast(float contrast) {
		if (contrast != this.contrast) {
			clearCache();
		}
		this.contrast = contrast;

	}

	/**
	 * Sets the brightness.
	 * 
	 * @param brightness
	 *            the new brightness
	 */
	public void setBrightness(float brightness) {
		if (brightness != this.brightness) {
			clearCache();
		}
		this.brightness = brightness;
	}

	/**
	 * Clear cache.
	 */
	protected void clearCache() {
		images = null;
	}

	/**
	 * Sets the transparency.
	 * 
	 * @param transparency
	 *            the new transparency
	 */
	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	/**
	 * Sets the visible.
	 * 
	 * @param visible
	 *            the new visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Sets the tint color.
	 * 
	 * @param c
	 *            color
	 */
	public void setTintColor(Color c) {
		this.tintColor = c;
	}

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {

		applet.pushStyle();
		if (visible) {
			PImage img = getImage();
			float w = img.width;
			float h = img.height;
			img.setModified(false);
			applet.tint(tintColor.getRed(), tintColor.getGreen(),
					tintColor.getBlue(), transparency * 255);
			applet.image(img, 0, 0, w, h);
			applet.noStroke();
			applet.tint(255);
			applet.fill(255, 255, 0);
		}
		applet.popStyle();

	}

	/**
	 * Gets the image.
	 * 
	 * @return the image
	 */
	public PImage getImage() {
		if (images == null) {
			ConvertImageDataToPImage converter = new ConvertImageDataToPImage();
			images = converter.convert(image, 0, contrast, brightness);
			images.parent = applet;
		}
		return images;
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
		visualizationParameters.setName(imgName);
		visualizationParameters.add(contrastParam = new ParamFloat("Contrast",
				-5, 5, contrast));
		contrastParam.setInputView(new ParamDoubleSliderInputView(
				contrastParam, 4, false));
		visualizationParameters.add(brightnessParam = new ParamFloat(
				"Brightness", -5, 5, brightness));
		brightnessParam.setInputView(new ParamDoubleSliderInputView(
				brightnessParam, 4, false));
		visualizationParameters.add(transparencyParam = new ParamFloat(
				"Transparency", 0, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(visibleParam = new ParamBoolean("Visible",
				visible));
		visualizationParameters.add(tintParam = new ParamColor("Tint",
				Color.white));
	}

	/**
	 * Gets the value string.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * 
	 * @return the value string
	 */
	public String getValueString(int x, int y) {
		if (x < 0 || x >= rows || y < 0 || y >= cols) {
			return "";
		}
		VoxelType type = image.getType();
		switch (type) {
		case COLOR:
		case COLOR_FLOAT:
		case COLOR_USHORT:
			Color c = image.getColor(x, y);
			return String.format("(%d,%d,%d,%d)", c.getRed(), c.getGreen(),
					c.getBlue(), c.getAlpha());
		default:
			return String.format("%4.3f", image.getFloat(x, y));
		}
	}

	/**
	 * Checks if is visible.
	 * 
	 * @return true, if is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Setup.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {
	}
}
