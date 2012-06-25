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

import java.awt.Color;

import javax.media.j3d.BoundingBox;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.converter.processing.ConvertEmbeddedSurfaceToPTriangleMesh;
import edu.jhu.cs.cisst.vent.structures.processing.PTriangleMesh;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceRenderer.
 */
public class SurfaceRenderer extends RendererProcessing3D {

	/** The applet. */
	protected VisualizationProcessing3D applet = null;

	/** The diffuse color. */
	protected Color diffuseColor = new Color(Color.white.getRGB());

	/** The diffuse color param. */
	protected ParamColor diffuseColorParam;

	/** The flip normals. */
	protected boolean flipNormals = false;

	/** The gouraud. */
	protected boolean gouraud = false;

	/** The mesh. */
	protected PTriangleMesh mesh;

	/** The surf. */
	protected EmbeddedSurface surf;

	/** The transparency. */
	float transparency = 1.0f;

	/** The transparency param. */
	protected ParamDouble transparencyParam;

	/** The visible. */
	protected boolean visible = true;

	/** The visible param. */
	protected ParamBoolean visibleParam, gouraudParam, flipNormalsParam;

	/**
	 * Instantiates a new surface renderer.
	 * 
	 * @param applet
	 *            the applet
	 * @param surf
	 *            the surf
	 */
	public SurfaceRenderer(VisualizationProcessing3D applet,
			EmbeddedSurface surf) {
		this.applet = applet;
		this.surf = surf;
		ConvertEmbeddedSurfaceToPTriangleMesh converter = new ConvertEmbeddedSurfaceToPTriangleMesh();
		mesh = converter.convert(surf);
		bbox = new BoundingBox();
		for (Point3f pt : mesh.getPoints()) {
			bbox.combine(new Point3d(pt));
		}
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		if (model == visibleParam) {
			setVisible(visibleParam.getValue());
		} else if (model == diffuseColorParam) {
			diffuseColor = diffuseColorParam.getValue();
		} else if (model == flipNormalsParam) {
			flipNormals = flipNormalsParam.getValue();
		} else if (model == gouraudParam) {
			gouraud = gouraudParam.getValue();
		} else if (model == transparencyParam) {
			transparency = transparencyParam.getFloat();
		}
	}

	/**
	 * Update visualization parameters.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		setVisible(visibleParam.getValue());
		diffuseColor = diffuseColorParam.getValue();
		flipNormals = flipNormalsParam.getValue();
		transparency = transparencyParam.getFloat();
		gouraud = gouraudParam.getValue();
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
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		// TODO Auto-generated method stub
		visualizationParameters.setName("Surface - " + surf.getName());
		visualizationParameters.add(diffuseColorParam = new ParamColor(
				"Diffuse Color", diffuseColor));
		visualizationParameters.add(transparencyParam = new ParamDouble(
				"Transparency", 0, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(gouraudParam = new ParamBoolean(
				"Gouraud Shading", gouraud));
		visualizationParameters.add(flipNormalsParam = new ParamBoolean(
				"Flip Normals", flipNormals));
		visualizationParameters.add(visibleParam = new ParamBoolean("Visible",
				visible));
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
			applet.noStroke();
			applet.smooth();
			applet.fill(diffuseColor.getRed(), diffuseColor.getGreen(),
					diffuseColor.getBlue(), transparency * 255.0f);
			mesh.draw(applet, gouraud, flipNormals);
		}
		applet.popStyle();
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
