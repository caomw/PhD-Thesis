package org.imagesci.robopaint.graphics;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.imagesci.mogac.MOGAC3D;

import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;

public class RoboRenderWidget extends VisualizationMOGAC3D {

	public RoboRenderWidget(int width, int height, MOGAC3D activeContour) {
		super(width, height, activeContour);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#create()
	 */
	@Override
	public ParamCollection create() {
		renderers = new ArrayList<RendererProcessing3D>();

		renderers.add(renderer = new RoboRenderer(this, activeContour,
				preferredWidth / 2, preferredHeight / 2, activeContour
						.getResamplingRate()));

		renderer.init();
		init();
		visualizationParameters = new ParamCollection(name);
		createVisualizationParameters(visualizationParameters);
		return visualizationParameters;
	}

	public MOGAC3D getActiveContour() {
		return activeContour;
	}

	public void updateImageSegmentation() {
		((RoboRenderer) renderer).updateImageSegmentation();

	}

	public void updateReferenceImage() {
		center = new Point3f(activeContour.rows * 0.5f,
				activeContour.cols * 0.5f, activeContour.slices * 0.5f);
		centerParam.setValue(center);

		((RoboRenderer) renderer).updateReferenceImage();
	}
}
