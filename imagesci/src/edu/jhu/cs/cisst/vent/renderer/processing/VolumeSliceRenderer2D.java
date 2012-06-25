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
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.converter.processing.ConvertImageDataToPImage;
import edu.jhu.cs.cisst.vent.resources.PlaceHolder;
import edu.jhu.cs.cisst.vent.widgets.SliceNumberDisplay;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamIntegerSliderInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.VoxelType;

// TODO: Auto-generated Javadoc
/**
 * The Class VolumeSliceRenderer2D renders volume by slice in 2D.
 */
public class VolumeSliceRenderer2D extends RendererProcessing2D {

	/** The applet. */
	protected VisualizationProcessing applet;

	/** The brightness. */
	protected float brightness = 0;

	/** The brightness param. */
	protected ParamFloat brightnessParam;

	/** The component. */
	protected int component = 0;

	/** The slice param. */
	protected ParamInteger componentParam;
	/** The contrast. */
	protected float contrast = 1;

	/** The contrast param. */
	protected ParamFloat contrastParam;

	/** The image. */
	protected ImageData image;

	/** The images. */
	protected PImage[] images = null;

	/** The max. */
	protected double min, max;

	/** The name. */
	protected String name;

	/** The slices. */
	public int rows, cols, slices, components;

	/** The scale y. */
	protected float scaleX = 1, scaleY = 1;

	/** The show slice number. */
	protected boolean showSliceNumber = true;

	/** The show slice number param. */
	protected ParamBoolean showSliceNumberParam;

	/** The slice. */
	protected int slice = 0;

	/** The slice param. */
	protected ParamInteger sliceParam;

	/** The transparency. */
	protected float transparency = 1;

	/** The transparency param. */
	protected ParamFloat transparencyParam;

	/** The visible. */
	protected boolean visible = true;

	/** The visible param. */
	protected ParamBoolean visibleParam;

	/**
	 * Instantiates a new volume slice renderer.
	 *
	 * @param img the img
	 * @param name the name
	 * @param applet the applet
	 */
	public VolumeSliceRenderer2D(ImageData img, String name,
			VisualizationProcessing applet) {
		this.image = img;
		rows = image.getRows();
		cols = image.getCols();
		slices = image.getSlices();
		slice = slices / 2;
		components = image.getComponents();
		this.name = name;
		min = 1E10f;
		max = -1E10f;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					for (int l = 0; l < components; l++) {
						double val = img.getDouble(i, j, k, l);
						min = Math.min(min, val);
						max = Math.max(max, val);
					}
				}
			}
		}
		images = new PImage[slices];
		this.applet = applet;

	}

	/**
	 * Instantiates a new volume slice renderer2 d.
	 *
	 * @param img the img
	 * @param applet the applet
	 */
	public VolumeSliceRenderer2D(ImageData img, VisualizationProcessing applet) {
		this(img, "Image 2D", applet);
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
		if (model == sliceParam) {
			setSlice(sliceParam.getInt() - 1);
		} else if (model == componentParam) {
			setComponent(componentParam.getInt() - 1);
		} else if (model == contrastParam) {
			setContrast(contrastParam.getFloat());
		} else if (model == brightnessParam) {
			setBrightness(brightnessParam.getFloat());
		} else if (model == transparencyParam) {
			setTransparency(transparencyParam.getFloat());
		} else if (model == visibleParam) {
			setVisible(visibleParam.getValue());
		} else if (model == showSliceNumberParam) {
			setSliceNumberVisible(showSliceNumberParam.getValue());
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
		setSlice(sliceParam.getInt() - 1);
		setContrast(contrastParam.getFloat());
		setBrightness(brightnessParam.getFloat());
		setTransparency(transparencyParam.getFloat());
		setVisible(visibleParam.getValue());
		setSliceNumberVisible(showSliceNumberParam.getValue());
		setComponent(componentParam.getInt() - 1);
	}

	/**
	 * Sets the slice.
	 * 
	 * @param slice
	 *            the new slice
	 */
	public void setSlice(int slice) {
		this.slice = slice;
	}

	/**
	 * Sets the component.
	 * 
	 * @param component
	 *            the new component
	 */
	public void setComponent(int component) {
		this.component = component;
		clearCache();
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
		for (int i = 0; i < images.length; i++) {
			images[i] = null;
		}
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
	 * Sets the slice number visible.
	 * 
	 * @param showSliceNumber
	 *            the new slice number visible
	 */
	private void setSliceNumberVisible(boolean showSliceNumber) {
		this.showSliceNumber = showSliceNumber;
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

			PImage img = getImage(slice);

			applet.tint(255, 255, 255, transparency * 255);
			applet.scale(scaleX, scaleY);
			img.setModified(false);
			applet.image(img, 0, 0, rows, cols);
			if (slices > 1 && showSliceNumber) {
				if (visualization instanceof SliceNumberDisplay) {
					((SliceNumberDisplay) visualization).draw(applet, slice,
							slices + 1, cols);
				}
			}
		}
		applet.popStyle();

	}

	/**
	 * Gets the image.
	 * 
	 * @param index
	 *            the index
	 * 
	 * @return the image
	 */
	public PImage getImage(int index) {
		if (images[index] == null) {
			ConvertImageDataToPImage converter = new ConvertImageDataToPImage();
			images[index] = converter.convert(image, index, component,
					contrast, brightness, min, max);
			images[index].parent = applet;
		}
		return images[index];
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
		visualizationParameters.setName(name);
		visualizationParameters.add(sliceParam = new ParamInteger("Slice", 1,
				slices, slice + 1));
		sliceParam.setInputView(new ParamIntegerSliderInputView(sliceParam, 4));
		visualizationParameters.add(componentParam = new ParamInteger(
				"Component", 1, Math.max(1, components), component));
		componentParam.setInputView(new ParamIntegerSliderInputView(
				componentParam, 4));

		visualizationParameters.add(contrastParam = new ParamFloat("Contrast",
				-5, 5, contrast));
		contrastParam.setInputView(new ParamDoubleSliderInputView(
				contrastParam, 4, false));
		visualizationParameters.add(brightnessParam = new ParamFloat(
				"Brightness", -5, 5, brightness));
		brightnessParam.setInputView(new ParamDoubleSliderInputView(
				brightnessParam, 4, false));
		visualizationParameters.add(transparencyParam = new ParamFloat(
				"Transparency", 0, 1, 1));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(showSliceNumberParam = new ParamBoolean(
				"Show Slice Number", showSliceNumber));
		visualizationParameters.add(visibleParam = new ParamBoolean("Visible",
				visible));
	}

	/**
	 * Gets the cols.
	 * 
	 * @return the cols
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * Gets the rows.
	 * 
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Gets the scale x.
	 *
	 * @return the scale x
	 */
	public float getScaleX() {
		return scaleX;
	}

	/**
	 * Gets the scale y.
	 *
	 * @return the scale y
	 */
	public float getScaleY() {
		return scaleY;
	}

	/**
	 * Gets the slice parameter.
	 * 
	 * @return the slice parameter
	 */
	public ParamInteger getSliceParameter() {
		return sliceParam;
	}

	/**
	 * Gets the slices.
	 * 
	 * @return the slices
	 */
	public int getSlices() {
		return slices;
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
			Color c = image.getColor(x, y, slice, component);
			return String.format("(%d,%d,%d,%d)", c.getRed(), c.getGreen(),
					c.getBlue(), c.getAlpha());
		default:
			return String.format("%4.3f",
					image.getFloat(x, y, slice, component));
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
	 * Match scale.
	 *
	 * @param img the img
	 */
	public void matchScale(ImageData img) {
		this.scaleX = img.getRows() / (float) image.getRows();
		this.scaleY = img.getCols() / (float) image.getCols();
	}

	/**
	 * Sets the images.
	 * 
	 * @param images
	 *            the new images
	 */
	public void setImages(PImage[] images) {
		this.images = images;
	}

	/**
	 * Sets the scale.
	 *
	 * @param scale the new scale
	 */
	public void setScale(float scale) {
		this.scaleX = scaleY = scale;
	}

	/**
	 * Sets the scale.
	 *
	 * @param scaleX the scale x
	 * @param scaleY the scale y
	 */
	public void setScale(float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/**
	 * Setup.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {
		File f;
		try {
			URL url = PlaceHolder.class.getResource("./TheSans-Plain-12.vlw");
			if (url != null) {
				f = new File(url.toURI());
				if (f.exists()) {
					String fontFile = (f.getAbsolutePath());
					applet.textFont(applet.loadFont(fontFile));
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
