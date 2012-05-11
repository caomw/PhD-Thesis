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


import java.util.ArrayList;

import javax.vecmath.Matrix4f;

import org.imagesci.springls.SpringlsActiveContour3D;

import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.SpringlsMeshRenderer;
import edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer3D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationSpringlsActiveContourMesh3D.
 */
public class VisualizationSpringlsActiveContourMesh3D extends
		VisualizationSpringlsActiveContour3D {

	/** The springls mesh render. */
	protected SpringlsMeshRenderer springlsMeshRender;

	/**
	 * Instantiates a new visualization springls active contour mesh3 d.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param activeContour
	 *            the active contour
	 */
	public VisualizationSpringlsActiveContourMesh3D(int width, int height,
			SpringlsActiveContour3D activeContour) {
		super(width, height, activeContour);
		// TODO Auto-generated constructor stub
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
		Matrix4f R = new Matrix4f();
		R.rotZ(rotz);
		pose.mul(R, pose);
		R.rotY(roty);
		pose.mul(R, pose);
		R.rotX(rotx);
		pose.mul(R, pose);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#create()
	 */
	@Override
	public ParamCollection create() {
		renderers = new ArrayList<RendererProcessing3D>();

		renderers.add(springlsMeshRender = new SpringlsMeshRenderer(this,
				activeContour.getCommons()));
		if (activeContour.getReferenceImage() != null) {
			renderers.add(new VolumeSliceRenderer3D(activeContour
					.getReferenceImage(), this));
		}
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
		activeContour.addListener(springlsMeshRender);
		super.createVisualizationParameters(visualizationParameters);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D#dispose()
	 */
	@Override
	public void dispose() {
		stopEvent();
		refresher.stop();
		// super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D#draw()
	 */
	@Override
	public void draw() {
		float scaleCamera = (float) ((height / 2.0) / Math
				.tan(PI * 60.0 / 360.0));
		float xp = width / 2;
		float yp = height / 2;
		resetMatrix();
		camera(xp, yp, scaleCamera, xp, yp, 0, 0, 1, 0);

		background(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(),
				bgColor.getAlpha());

		translate(width / 2.0f - tx, height / 2.0f - ty);

		rotateZ(PI);

		applyMatrix(pose.m00, pose.m01, pose.m02, pose.m03, pose.m10, pose.m11,
				pose.m12, pose.m13, pose.m20, pose.m21, pose.m22, pose.m23,
				pose.m30, pose.m31, pose.m32, pose.m33);

		scale(-1, 1);
		scale(scaleObject * scale);
		translate(-center.x, -center.y, -center.z);
		super.draw();
	}

}
