/**
 * ImageSci Toolkit
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

import imagesci.gac.TopologyRule2D;
import imagesci.utility.IsoContourGenerator;

import java.awt.Color;

import javax.swing.SwingWorker;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.structures.processing.PCurveCollection;
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
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class VolumeIsoContourRenderer displays slices from a volume and overlays
 * a specified iso-contour.
 */
public class VolumeIsoContourRenderer extends VolumeSliceRenderer2D {

	/**
	 * The Class IsoContourWorker creates iso-contours for all slices.
	 */
	protected class IsoContourWorker extends SwingWorker<Void, Void> {

		/**
		 * Do in background.
		 * 
		 * @return the void
		 * 
		 * @throws Exception
		 *             the exception
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground() throws Exception {

			for (int i = 0; i < curves.length; i++) {
				curves[i] = null;
				if (this.isCancelled()) {
					break;
				}
				IsoContourGenerator isogen = new IsoContourGenerator(rule);
				curves[i] = new PCurveCollection(isogen.solve(
						(ImageDataFloat) image, isoLevel, i, component), false);
			}

			return null;

		}

	}

	/** The contour color. */
	protected Color contourColor = new Color(0, 132, 68);

	/** The contour color param. */
	protected ParamColor contourColorParam;

	/** The contour worker. */
	protected SwingWorker<Void, Void> contourWorker = null;

	/** The curves. */
	protected PCurveCollection[] curves = null;

	/** The fill color. */
	protected Color fillColor = new Color(Color.white.getRGB());

	/** The fill color param. */
	protected ParamColor fillColorParam;

	/** The fill contour. */
	protected boolean fillContour;

	/** The fill param. */
	protected ParamBoolean fillParam;

	/** The iso level. */
	protected float isoLevel = 0;

	/** The iso-level parameter. */
	protected ParamFloat isolevelParam;

	/** The name. */
	protected String name = "Iso-Contour";

	/** The rule. */
	protected TopologyRule2D.Rule rule = null;

	/** The show contour parameter. */
	protected ParamBoolean showContourParam;

	/** The stroke weight. */
	protected float strokeWeight = 2;

	/** The stroke weight param. */
	protected ParamFloat strokeWeightParam;

	/** The visible contour. */
	protected boolean visibleContour = true;

	/**
	 * Instantiates a new volume iso contour renderer.
	 * 
	 * @param img
	 *            the img
	 * @param rule
	 *            the rule
	 * @param applet
	 *            the applet
	 */
	public VolumeIsoContourRenderer(ImageDataFloat img,
			TopologyRule2D.Rule rule, VisualizationProcessing applet) {
		super(img, applet);
		curves = new PCurveCollection[slices];
		this.rule = rule;
		contourWorker = createWorker();
		contourWorker.execute();
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		super.update(model, view);
		if (model == isolevelParam) {
			setIsoLevel(isolevelParam.getFloat());
		} else if (model == showContourParam) {
			setContourVisible(showContourParam.getValue());
		} else if (model == contourColorParam) {
			setContourColor(contourColorParam.getValue());
		} else if (model == strokeWeightParam) {
			setStrokeWeight(strokeWeightParam.getFloat());
		} else if (model == fillColorParam) {
			setFillColor(fillColorParam.getValue());
		} else if (model == fillParam) {
			setFillContour(fillParam.getValue());
		}
	}

	/**
	 * Update visualization parameters.
	 * 
	 * @seeedu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer# 
	 *                                                                    updateVisualizationParameters
	 *                                                                    ()
	 */
	@Override
	public void updateVisualizationParameters() {
		super.updateVisualizationParameters();
		setIsoLevel(isolevelParam.getFloat());
		setContourVisible(showContourParam.getValue());
		setContourColor(contourColorParam.getValue());
		setStrokeWeight(strokeWeightParam.getFloat());
		setFillColor(fillColorParam.getValue());
		setFillContour(fillParam.getValue());
	}

	/**
	 * Sets the iso level.
	 * 
	 * @param isoLevel
	 *            the new iso level
	 */
	public void setIsoLevel(float isoLevel) {
		if (isoLevel != this.isoLevel) {
			this.isoLevel = isoLevel;
			if (contourWorker != null && !contourWorker.isDone()) {
				contourWorker.cancel(false);
			}
			contourWorker = createWorker();
			contourWorker.execute();

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer#setComponent
	 * (int)
	 */
	@Override
	public void setComponent(int component) {
		if (this.component != component) {
			this.component = component;
			clearCache();
			if (contourWorker != null && !contourWorker.isDone()) {
				contourWorker.cancel(false);
			}
			contourWorker = createWorker();
			contourWorker.execute();
		}
	}

	/**
	 * Creates the worker.
	 * 
	 * @return the swing worker
	 */
	protected SwingWorker<Void, Void> createWorker() {
		return new IsoContourWorker();
	}

	/**
	 * Sets the contour visible.
	 * 
	 * @param visible
	 *            the new contour visible
	 */
	public void setContourVisible(boolean visible) {
		this.visibleContour = visible;
	}

	/**
	 * Sets the contour color.
	 * 
	 * @param contourColor
	 *            the new contour color
	 */
	public void setContourColor(Color contourColor) {
		this.contourColor = contourColor;
	}

	/**
	 * Sets the stroke weight.
	 * 
	 * @param strokeWeight
	 *            the new stroke weight
	 */
	public void setStrokeWeight(float strokeWeight) {
		this.strokeWeight = strokeWeight;
	}

	/**
	 * Sets the fill color.
	 * 
	 * @param fillColor
	 *            the new fill color
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * Sets the fill contour.
	 * 
	 * @param fillContour
	 *            the new fill contour
	 */
	public void setFillContour(boolean fillContour) {
		this.fillContour = fillContour;
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		visualizationParameters.setName(name);
		visualizationParameters.add(sliceParam = new ParamInteger("Slice", 1,
				slices, 1));
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
				"Transparency", 0, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(contourColorParam = new ParamColor(
				"Contour Color", contourColor));
		visualizationParameters.add(strokeWeightParam = new ParamFloat(
				"Stroke Weight", 0, 10000, strokeWeight));
		visualizationParameters.add(fillColorParam = new ParamColor(
				"Fill Color", fillColor));
		visualizationParameters.add(fillParam = new ParamBoolean("Fill",
				fillContour));

		visualizationParameters.add(isolevelParam = new ParamFloat("Iso-Level",
				-1E6f, 1E6f, isoLevel));
		visualizationParameters.add(showSliceNumberParam = new ParamBoolean(
				"Show Slice Number", showSliceNumber));
		visualizationParameters.add(showContourParam = new ParamBoolean(
				"Contour Visible", visibleContour));
		visualizationParameters.add(visibleParam = new ParamBoolean(
				"Image Visible", visible));
	}

	/**
	 * (non-Javadoc).
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D#draw()
	 */
	@Override
	public void draw() {
		applet.pushStyle();
		applet.noSmooth();
		applet.scale(scaleX, scaleY);
		PImage img = getImage(slice);
		if (visible && img != null) {
			applet.tint(255, 255, 255, transparency * 255);
			img.setModified(false);
			applet.image(img, 0, 0, rows, cols);

		}
		PCurveCollection curve = curves[slice];
		if (curve != null && visibleContour) {
			applet.pushMatrix();

			if (fillContour) {
				applet.fill(fillColor.getRGB());
			} else {
				applet.noFill();
			}

			applet.smooth();

			applet.stroke(contourColor.getRed(), contourColor.getGreen(),
					contourColor.getBlue(), contourColor.getAlpha());
			applet.strokeWeight(strokeWeight);
			applet.translate(0.5f, 0.5f);
			curve.draw2D(applet);
			applet.popMatrix();
		}
		if (showSliceNumber && slices > 1 && (visibleContour || visible)) {
			if (visualization instanceof SliceNumberDisplay) {
				((SliceNumberDisplay) visualization).draw(applet, slice,
						slices + 1, cols);
			}
		}
		applet.strokeWeight(1.0f);
		applet.popStyle();
	}

	/**
	 * Gets the fill color.
	 * 
	 * @return the fill color
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Checks if is fill contour.
	 * 
	 * @return true, if is fill contour
	 */
	public boolean isFillContour() {
		return fillContour;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
