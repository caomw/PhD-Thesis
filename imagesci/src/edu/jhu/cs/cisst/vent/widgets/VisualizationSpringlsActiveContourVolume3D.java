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
package edu.jhu.cs.cisst.vent.widgets;


import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.imagesci.springls.SpringlsActiveContour3D;
import org.imagesci.springls.SpringlsCommon3D;

import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.SpringlsRaycastRenderer;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationSpringlsActiveContourVolume3D.
 */
public class VisualizationSpringlsActiveContourVolume3D extends
		VisualizationSpringlsActiveContour3D {

	/** The springls raycast render. */
	protected SpringlsRaycastRenderer springlsRaycastRender;

	/** The view update. */
	boolean viewUpdate = false;

	/**
	 * Instantiates a new visualization springls active contour volume3 d.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param activeContour
	 *            the active contour
	 */
	public VisualizationSpringlsActiveContourVolume3D(int width, int height,
			SpringlsActiveContour3D activeContour) {
		super(width, height, activeContour);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D#refresh
	 * ()
	 */
	@Override
	public void refresh() {
		if (requestUpdate) {
			requestUpdate = false;
			refreshLock = true;
			if (viewUpdate) {
				sceneParams.getInputView().update();
			}
			refreshLock = false;
			springlsRaycastRender.refresh();

		} else if (enableAutoRotate.getValue()
				&& !mousePressed
				&& ((!captureParam.getValue() && !running)
						|| (captureParam.getValue() && !running) || (!captureParam
						.getValue() && running))) {
			Point3f rot = autoRotateParam.getValue();
			if (Math.abs(rot.x) > 1E-3f || Math.abs(rot.y) > 1E-3f
					|| Math.abs(rot.z) > 1E-3f) {

				if (enableAutoRotate.getValue()) {
					applyRotation(rot.x, rot.y, rot.z);
				}
				updatePoseParam();
				if (countCycle == 0) {
					refreshLock = true;
					sceneParams.getInputView().update();
					refreshLock = false;
				}
				springlsRaycastRender.refresh();
				countCycle++;
				if (countCycle == 100) {
					countCycle = 0;
				}
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#applyRotation
	 * (float, float, float)
	 */
	@Override
	public void applyRotation(float rotx, float roty, float rotz) {
		springlsRaycastRender.applyTransform(-rotx, roty, rotz, 0, 0, 0);
		pose = springlsRaycastRender.getModelView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#create()
	 */
	@Override
	public ParamCollection create() {
		renderers = new ArrayList<RendererProcessing3D>();

		renderers
				.add(springlsRaycastRender = (SpringlsRaycastRenderer) activeContour
						.getCommons().createSpringlsRayCastRenderer(this,
								preferredWidth / 2, preferredHeight / 2,
								activeContour.getResamplingInterval()));

		ParamCollection params = super.create();
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D#
	 * createVisualizationParameters
	 * (edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		activeContour.addListener(springlsRaycastRender);
		super.createVisualizationParameters(visualizationParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D#draw()
	 */
	@Override
	public void draw() {
		if (captureParam.getValue() || running) {
			springlsRaycastRender.setFastRendering(false);
			springlsRaycastRender.refresh();
		} else {
			springlsRaycastRender.setFastRendering(true);
		}
		super.draw();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#mouseDragged()
	 */
	@Override
	public void mouseDragged() {
		super.mouseDragged();
		if (requestUpdate) {
			springlsRaycastRender.setCameraCenter(tx / width, -ty / height,
					4.0f / scale);
			SpringlsCommon3D commons = activeContour.getCommons();
			springlsRaycastRender.setTargetCenter(new Point3f(tx / width + 2
					* center.x / commons.rows - 1, -ty / height + 2 * center.y
					/ commons.cols - 1, 2 * center.z / commons.slices - 1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#mouseWheelMoved
	 * (java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scale = Math
				.max(0.01f, scale - wheelScaleRate * (e.getWheelRotation()));
		scaleParam.setValue(scale);
		springlsRaycastRender.setCameraCenter(0, 0, 4.0f / scale);
		requestUpdate = true;
		viewUpdate = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#update(edu.jhu
	 * .ece.iacl.jist.pipeline.parameter.ParamModel,
	 * edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		super.update(model, view);
		springlsRaycastRender.setBackgroundColor(bgColor);
		springlsRaycastRender.setModelView(pose);
		springlsRaycastRender.setCameraCenter(tx / width, -ty / height,
				4.0f / scale);
		SpringlsCommon3D commons = activeContour.getCommons();
		springlsRaycastRender.setTargetCenter(new Point3f(tx / width + 2
				* center.x / commons.rows - 1, -ty / height + 2 * center.y
				/ commons.cols - 1, 2 * center.z / commons.slices - 1));

		requestUpdate = true;
		viewUpdate = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#
	 * updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		super.updateVisualizationParameters();
		springlsRaycastRender.setBackgroundColor(bgColor);
		springlsRaycastRender.setModelView(pose);
		springlsRaycastRender.setCameraCenter(tx / width, -ty / height,
				4.0f / scale);
		SpringlsCommon3D commons = activeContour.getCommons();
		springlsRaycastRender.setTargetCenter(new Point3f(tx / width + 2
				* center.x / commons.rows - 1, -ty / height + 2 * center.y
				/ commons.cols - 1, 2 * center.z / commons.slices - 1));

		requestUpdate = true;
		viewUpdate = true;
	}
}
