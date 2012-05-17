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

import java.awt.Color;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import edu.jhu.cs.cisst.vent.VisualizationProcessing;
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
 * The Class VectorField2DRenderer.
 */
public class VectorFieldRenderer2D extends RendererProcessing2D {

	/** The applet. */
	protected VisualizationProcessing applet;

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

	/** The brightness. */
	protected float brightness = 0;

	/** The contrast. */
	protected float contrast = 1;

	/** The image. */
	protected ImageDataFloat image;

	/** The img name. */
	protected String imgName = "Vector Field";

	/** The magnitude image. */
	protected float[][] magImage = null;

	/** The max magnitude. */
	protected float maxMagnitude;

	/** The cols. */
	protected int rows, cols;

	/** The sample rate. */
	protected int sampleRate = 4;

	/** The sample rate param. */
	protected ParamInteger sampleRateParam;

	/** The scale. */
	protected float scaleX = 1, scaleY = 1;

	/** The transparency. */
	protected float transparency = 0;

	/** The transparency param. */
	protected ParamFloat transparencyParam;

	/** The vector field. */
	protected Vector2f[][] vectorField = null;

	/** The visible. */
	protected boolean visible = true;

	/** The visible param. */
	protected ParamBoolean visibleParam;

	/** The x offset. */
	protected float xOffset = 0;

	/** The y offset. */
	protected float yOffset = 0;

	/**
	 * Instantiates a new vector field2 d renderer.
	 *
	 * @param img the img
	 * @param name the name
	 * @param applet the applet
	 */
	public VectorFieldRenderer2D(ImageDataFloat img, String name,
			VisualizationProcessing applet) {
		this.image = img;
		this.imgName = name;
		rows = image.getRows();
		cols = image.getCols();
		this.applet = applet;
	}

	/**
	 * Instantiates a new vector field2 d renderer.
	 * 
	 * @param img
	 *            the img
	 * @param applet
	 *            the applet
	 */
	public VectorFieldRenderer2D(ImageDataFloat img,
			VisualizationProcessing applet) {
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
			applet.scale(scaleX, scaleY);
			applet.translate(xOffset, yOffset);
			Vector2f[][] vectorField = getVectorField();
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
					Vector2f v = vectorField[is][js];
					float len = magImage[is][js];
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
			applet.popMatrix();
		}
		applet.popStyle();

	}

	/**
	 * Gets the vector field.
	 * 
	 * @return the vector field
	 */
	public Vector2f[][] getVectorField() {

		if (vectorField == null) {
			maxMagnitude = 0;
			Vector2f v;
			vectorField = new Vector2f[rows][cols];
			magImage = new float[rows][cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					vectorField[i][j] = v = new Vector2f(
							image.getFloat(i, j, 0), image.getFloat(i, j, 1));
					float len = v.length();
					magImage[i][j] = len;
					maxMagnitude = Math.max(maxMagnitude, len);
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
	public void setContrast(float contrast) {
		if (contrast != this.contrast) {
			clearCache();
		}
		this.contrast = contrast;

	}

	/**
	 * Clear cache.
	 */
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
		visualizationParameters.setName(imgName);
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
	 * Match scale.
	 *
	 * @param img the img
	 */
	public void matchScale(ImageData img) {
		this.scaleX = img.getRows() / (float) image.getRows();
		this.scaleY = img.getCols() / (float) image.getCols();
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
