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


import java.awt.Color;

import javax.vecmath.Point3d;

import org.imagesci.utility.IsoContourGenerator;

import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.structures.processing.PCurveCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class VolumeSliceRenderer3D renders volume by slice in 3D.
 */
public class ImageRenderer3D extends RendererProcessing3D {

	/** The applet. */
	protected VisualizationProcessing3D applet;
	/** The brightness. */
	protected float brightness = 0;

	/** The brightness param. */
	protected ParamFloat brightnessParam;

	/** The component. */
	protected int component = 0;

	/** The contrast. */
	protected float contrast = 1;

	/** The contrast param. */
	protected ParamFloat contrastParam;

	/** The curve. */
	protected PCurveCollection curve;

	/** The depth. */
	protected float depth = 0;

	/** The depth param. */
	protected ParamFloat depthParam;

	/** The images. */
	protected PImage image;

	/** The max. */
	protected double min, max;

	/** The slices. */
	protected int rows, cols;

	/** The scale. */
	protected float scale = 1;

	/** The stroke color param. */
	protected ParamColor strokeColorParam;

	/** The stroke weight param. */
	protected ParamFloat strokeWeightParam;

	/** The transparency. */
	protected float transparency = 1;
	/** The transparency param. */
	protected ParamFloat transparencyParam;
	/** The vol to image transform. */
	protected PMatrix3D volToImageTransform;

	/**
	 * Instantiates a new volume slice renderer.
	 * 
	 * @param img
	 *            the img
	 * @param volToImageTransform
	 *            the vol to image transform
	 * @param applet
	 *            the applet
	 */
	public ImageRenderer3D(PImage img, PMatrix3D volToImageTransform,
			VisualizationProcessing3D applet) {
		this.image = img;
		rows = image.width;
		cols = image.height;
		min = 1E10f;
		max = -1E10f;
		float[] hsv = new float[3];
		this.volToImageTransform = volToImageTransform;
		for (int i = 0; i < img.pixels.length; i++) {
			Color val = new Color(img.pixels[i]);
			Color.RGBtoHSB(val.getRed(), val.getGreen(), val.getBlue(), hsv);
			min = Math.min(min, 255.0f * hsv[0]);
			max = Math.max(max, 255.0f * hsv[0]);
		}
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

	}

	/**
	 * Sets the contrast.
	 * 
	 * @param contrast
	 *            the new contrast
	 */
	public void setContrast(float contrast) {
		this.contrast = contrast;

	}

	/**
	 * Sets the brightness.
	 * 
	 * @param brightness
	 *            the new brightness
	 */
	public void setBrightness(float brightness) {
		this.brightness = brightness;
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
		visualizationParameters.setName("Image 3D");
		if (curve != null) {
			visualizationParameters.add(strokeColorParam = new ParamColor(
					"Contour Color", new Color(0, 128, 0)));
			visualizationParameters.add(strokeWeightParam = new ParamFloat(
					"Stroke Weight", 0, 10, 2));
		}
		visualizationParameters.add(depthParam = new ParamFloat("Depth", -cols,
				cols, -3 * cols / 4));
		depthParam.setInputView(new ParamDoubleSliderInputView(depthParam, 4,
				false));

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

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {
		applet.pushStyle();
		applet.pushMatrix();
		applet.applyMatrix(volToImageTransform);
		applet.scale(scale);
		applet.tint(255, 255, 255, transparency * 255);
		applet.fill(255, 255, 255);
		applet.stroke(255, 153, 0, transparency * 255);
		float w = image.width;
		float h = image.height;
		applet.pushMatrix();
		applet.translate(0, 0, depthParam.getFloat());

		applet.beginShape(PConstants.QUADS);

		applet.texture(image);
		applet.vertex(0, 0, 0, 0, 0);
		applet.vertex(w, 0, 0, 1, 0);
		applet.vertex(w, h, 0, 1, 1);
		applet.vertex(0, h, 0, 0, 1);
		applet.endShape();

		applet.strokeWeight(strokeWeightParam.getFloat());
		applet.stroke(strokeColorParam.getValue().getRGB());

		curve.draw3D(applet);
		applet.popMatrix();
		applet.popMatrix();
		applet.popStyle();
	}

	/**
	 * Sets the level set image.
	 *
	 * @param img the new level set image
	 */
	public void setLevelSetImage(ImageDataFloat img) {
		IsoContourGenerator isogen = new IsoContourGenerator();
		curve = new PCurveCollection(isogen.solve(img, 0, -1, -1), false, false);
	}

	/**
	 * Sets the scale.
	 * 
	 * @param scale
	 *            the new scale
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * Sets the scale.
	 *
	 * @param scaleX the scale x
	 * @param scaleY the scale y
	 * @param scaleZ the scale z
	 */
	public void setScale(float scaleX, float scaleY, float scaleZ) {
		this.scale = scaleX;

	}

	/**
	 * Setup.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {

		bbox.combine(new Point3d(0, 0, -cols));
		bbox.combine(new Point3d(0, 0, cols));
		bbox.combine(new Point3d(0, cols, 0));
		bbox.combine(new Point3d(rows, cols, 0));
		bbox.combine(new Point3d(rows, 0, 0));

		applet.textureMode(PConstants.NORMALIZED);
	}

}
