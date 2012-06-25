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

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.widgets.SliceNumberDisplay;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamIntegerSliderInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class VectorFieldSliceRenderer2D.
 */
public class VectorFieldSliceRenderer2D extends VolumeSliceRenderer2D {

	/** The arrow color. */
	protected Color arrowColor = new Color(Color.blue.getRGB());

	/** The arrow color param. */
	protected ParamColor arrowColorParam;

	/** The arrow head height. */
	protected float arrowHeadHeight = 0.5f;

	/** The arrow head height param. */
	protected ParamFloat arrowHeadHeightParam;

	/** The arrow head width. */
	protected float arrowHeadWidth = 0.35f;

	/** The arrow head width param. */
	protected ParamFloat arrowHeadWidthParam;

	/** The arrow width. */
	protected float arrowWidth = 0.2f;

	/** The arrow width param. */
	protected ParamFloat arrowWidthParam;

	/** The magnitude image. */
	protected float[][][] magImage = null;

	/** The max magnitude. */
	protected float maxMagnitude;

	/** The sample rate. */
	protected int sampleRate = 4;

	/** The sample rate param. */
	protected ParamInteger sampleRateParam;

	/** The scale. */
	protected float scale = 1;

	/** The transparency param. */
	protected ParamFloat transparencyParam;

	/** The vector field. */
	protected Vector2f[][][] vectorField = null;

	/** The x offset. */
	protected float xOffset = 0;

	/** The y offset. */
	protected float yOffset = 0;

	/**
	 * Instantiates a new vector field slice renderer2 d.
	 * 
	 * @param img
	 *            the img
	 * @param applet
	 *            the applet
	 */
	public VectorFieldSliceRenderer2D(ImageData img,
			VisualizationProcessing applet) {
		super(img, applet);
		if (img.getComponents() < 2) {
			slices = 1;
		} else {
			slice = slices / 2;
		}
		// TODO Auto-generated constructor stub
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
		}
		if (model == sampleRateParam) {
			setSampleRate(sampleRateParam.getInt());
		} else if (model == transparencyParam) {
			setTransparency(transparencyParam.getFloat());
		} else if (model == visibleParam) {
			setVisible(visibleParam.getValue());
		} else if (model == arrowHeadWidthParam) {
			setArrowHeadWidth(arrowHeadWidthParam.getFloat());
		} else if (model == arrowHeadHeightParam) {
			setArrowHeadHeight(arrowHeadHeightParam.getFloat());
		} else if (model == arrowColorParam) {
			setArrowColor(arrowColorParam.getValue());
		} else if (model == arrowWidthParam) {
			setArrowWidth(arrowWidthParam.getFloat());
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
		setSampleRate(sampleRateParam.getInt());
		setTransparency(transparencyParam.getFloat());
		setVisible(visibleParam.getValue());
		setArrowHeadWidth(arrowHeadWidthParam.getFloat());
		setArrowHeadHeight(arrowHeadHeightParam.getFloat());
		setArrowColor(arrowColorParam.getValue());
		setArrowWidth(arrowWidthParam.getFloat());
		setSlice(sliceParam.getInt() - 1);
	}

	/**
	 * Sets the sample rate.
	 * 
	 * @param sampleRate
	 *            the new sample rate
	 */
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * Sets the transparency.
	 * 
	 * @param transparency
	 *            the new transparency
	 */
	@Override
	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	/**
	 * Sets the visible.
	 * 
	 * @param visible
	 *            the new visible
	 */
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Sets the arrow head width.
	 * 
	 * @param arrowHeadWidth
	 *            the new arrow head width
	 */
	public void setArrowHeadWidth(float arrowHeadWidth) {
		this.arrowHeadWidth = arrowHeadWidth;
	}

	/**
	 * Sets the arrow head height.
	 * 
	 * @param arrowHeadHeight
	 *            the new arrow head height
	 */
	public void setArrowHeadHeight(float arrowHeadHeight) {
		this.arrowHeadHeight = arrowHeadHeight;
	}

	/**
	 * Sets the arrow color.
	 * 
	 * @param arrowColor
	 *            the new arrow color
	 */
	public void setArrowColor(Color arrowColor) {
		this.arrowColor = arrowColor;
	}

	/**
	 * Sets the arrow width.
	 * 
	 * @param arrowWidth
	 *            the new arrow width
	 */
	private void setArrowWidth(float arrowWidth) {
		this.arrowWidth = arrowWidth;
	}

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderers.RendererProcessing#draw()
	 */
	@Override
	public void draw() {
		applet.pushStyle();
		if (visible) {
			applet.pushMatrix();
			applet.scale(scale);
			applet.translate(xOffset, yOffset);
			Vector2f[][][] vectorField = getVectorField();
			int r = vectorField.length;
			int c = vectorField[0].length;
			int sample = sampleRate;
			applet.strokeWeight(sample * arrowWidth);
			for (int i = (int) Math.max(-xOffset * sample, 0); i < r
					- Math.max(0, xOffset * sample); i += sample) {
				for (int j = (int) Math.max(-yOffset * sample, 0); j < c
						- Math.max(0, yOffset * sample); j += sample) {
					int is = Math.min(Math.round(i + sample * 0.5f), r - 1);
					int js = Math.min(Math.round(j + sample * 0.5f), c - 1);
					Vector2f v = vectorField[is][js][slice];
					float len = magImage[is][js][slice];
					Vector2f pt = new Vector2f(v.x / len, v.y / len);
					// Added 1 pixel shift to account for translation
					// discrepancy
					// Adding pixel shift looks better for GVF, but is incorrect
					// for rendering purposes.
					float offx = (0.5f * sample + i);
					float offy = (0.5f * sample + j);
					float basex = sample * pt.x * (0.5f - arrowHeadHeight);
					float basey = sample * pt.y * (0.5f - arrowHeadHeight);
					float tipx = sample * pt.x * 0.5f;
					float tipy = sample * pt.y * 0.5f;
					float lcornerx = sample
							* (pt.x * (0.5f - arrowHeadHeight) + arrowHeadWidth
									* pt.y * 0.5f);
					float lcornery = sample
							* (pt.y * (0.5f - arrowHeadHeight) - arrowHeadWidth
									* pt.x * 0.5f);
					float rcornerx = sample
							* (pt.x * (0.5f - arrowHeadHeight) - arrowHeadWidth
									* pt.y * 0.5f);
					float rcornery = sample
							* (pt.y * (0.5f - arrowHeadHeight) + arrowHeadWidth
									* pt.x * 0.5f);

					applet.stroke(
							arrowColor.getRed(),
							arrowColor.getGreen(),
							arrowColor.getBlue(),
							255 * Math.max(
									0,
									Math.min(1, transparency + len
											/ maxMagnitude)));

					applet.line(offx + basex, offy + basey, offx - tipx, offy
							- tipy);
					applet.noStroke();
					applet.fill(
							arrowColor.getRed(),
							arrowColor.getGreen(),
							arrowColor.getBlue(),
							255 * Math.max(
									0,
									Math.min(1, transparency + len
											/ maxMagnitude)));
					applet.triangle(offx + lcornerx, offy + lcornery, offx
							+ tipx, offy + tipy, offx + rcornerx, offy
							+ rcornery);
				}
			}
			if (slices > 1 && showSliceNumber) {
				if (visualization instanceof SliceNumberDisplay) {
					((SliceNumberDisplay) visualization).draw(applet, slice,
							slices + 1, cols);
				}
			}
			applet.popMatrix();
		}
		applet.popStyle();

	}

	/**
	 * Gets the vector field.
	 * 
	 * @return the vector field
	 */
	public Vector2f[][][] getVectorField() {

		if (vectorField == null) {
			maxMagnitude = 0;
			Vector2f v;

			if (image.getComponents() > 1) {
				vectorField = new Vector2f[rows][cols][slices];
				magImage = new float[rows][cols][slices];
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						for (int k = 0; k < slices; k++) {
							vectorField[i][j][k] = v = new Vector2f(
									image.getFloat(i, j, k, 0), image.getFloat(
											i, j, k, 1));
							float len = v.length();
							magImage[i][j][k] = len;
							maxMagnitude = Math.max(maxMagnitude, len);
						}
					}
				}
			} else {
				vectorField = new Vector2f[rows][cols][1];
				magImage = new float[rows][cols][1];
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						vectorField[i][j][0] = v = new Vector2f(image.getFloat(
								i, j, 0), image.getFloat(i, j, 1));
						float len = v.length();
						magImage[i][j][0] = len;
						maxMagnitude = Math.max(maxMagnitude, len);
					}
				}
			}
		}
		return vectorField;
	}

	/**
	 * Sets the brightness.
	 * 
	 * @param brightness
	 *            the new brightness
	 */
	@Override
	public void setBrightness(float brightness) {
		if (brightness != this.brightness) {
			clearCache();
		}
		this.brightness = brightness;
	}

	/**
	 * Sets the contrast.
	 * 
	 * @param contrast
	 *            the new contrast
	 */
	@Override
	public void setContrast(float contrast) {
		if (contrast != this.contrast) {
			clearCache();
		}
		this.contrast = contrast;

	}

	/**
	 * Clear cache.
	 */
	@Override
	protected void clearCache() {
		vectorField = null;
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
		visualizationParameters.setName("Vector Field");
		visualizationParameters.add(sliceParam = new ParamInteger("Slice", 1,
				slices, slice + 1));
		sliceParam.setInputView(new ParamIntegerSliderInputView(sliceParam, 4));
		visualizationParameters.add(sampleRateParam = new ParamInteger(
				"Sample Rate", 1, 50, sampleRate));
		sampleRateParam.setInputView(new ParamIntegerSliderInputView(
				sampleRateParam, 4));
		visualizationParameters.add(transparencyParam = new ParamFloat(
				"Transparency", -1, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(arrowColorParam = new ParamColor(
				"Arrow Color", arrowColor));
		visualizationParameters.add(arrowWidthParam = new ParamFloat(
				"Arrow Width", 0, 1, arrowWidth));
		arrowWidthParam.setInputView(new ParamDoubleSliderInputView(
				arrowWidthParam, 4, false));
		visualizationParameters.add(arrowHeadWidthParam = new ParamFloat(
				"Arrow Head Width", 0, 1, arrowHeadWidth));
		arrowHeadWidthParam.setInputView(new ParamDoubleSliderInputView(
				arrowHeadWidthParam, 4, false));

		visualizationParameters.add(arrowHeadHeightParam = new ParamFloat(
				"Arrow Head Height", 0, 1, arrowHeadHeight));
		arrowHeadHeightParam.setInputView(new ParamDoubleSliderInputView(
				arrowHeadHeightParam, 4, false));
		visualizationParameters.add(visibleParam = new ParamBoolean("Visible",
				visible));
	}

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * Sets the offset.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	public void setOffset(double x, double y) {
		this.xOffset = (float) x;
		this.yOffset = (float) y;
	}

	/**
	 * Sets the scale.
	 * 
	 * @param scale
	 *            the new scale
	 */
	@Override
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * Setup.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderers.RendererProcessing#setup()
	 */
	@Override
	public void setup() {
	}

	/**
	 * Sets the vector field.
	 * 
	 * @param image
	 *            the new vector field
	 */
	public void setVectorField(ImageDataFloat image) {
		this.image = image;
		this.rows = image.getRows();
		this.cols = image.getCols();
		vectorField = null;
	}

	/**
	 * Draw.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param v
	 *            the v
	 */
	protected void draw(int x, int y, Vector3f v) {

	}

}
