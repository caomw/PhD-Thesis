package org.imagesci.robopaint.graphics;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.robopaint.ImageViewDescription;
import org.imagesci.robopaint.RoboPaint;

import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;

public class RoboRenderWidget extends VisualizationMOGAC3D {
	public RoboRenderer getRenderer() {
		return (RoboRenderer) renderer;
	}

	public RoboRenderWidget(int width, int height, MOGAC3D activeContour) {
		super(width, height, activeContour);
		this.scale = 0.7f;
		this.tx = 180;
		// TODO Auto-generated constructor stub
	}

	public void keyTyped() {

		switch (key) {
		case 'a':
			ImageViewDescription.getInstance().setRow(
					Math.min(ImageViewDescription.getInstance().getRow() + 1,
							ImageViewDescription.getInstance().getImageRows()));
			break;
		case 'z':
			ImageViewDescription.getInstance()
					.setRow(Math.max(ImageViewDescription.getInstance()
							.getRow() - 1, 1));
			break;
		case 's':
			ImageViewDescription.getInstance().setCol(
					Math.min(ImageViewDescription.getInstance().getCol() + 1,
							ImageViewDescription.getInstance().getImageCols()));
			break;
		case 'x':
			ImageViewDescription.getInstance()
					.setCol(Math.max(ImageViewDescription.getInstance()
							.getCol() - 1, 1));
			break;
		case 'd':
			ImageViewDescription.getInstance()
					.setSlice(
							Math.min(ImageViewDescription.getInstance()
									.getSlice() + 1, ImageViewDescription
									.getInstance().getImageSlices()));
			break;
		case 'c':
			ImageViewDescription.getInstance().setSlice(
					Math.max(ImageViewDescription.getInstance().getSlice() - 1,
							1));
			break;
		default:
			super.keyTyped();
		}

	}

	public void mousePressed() {
		super.mousePressed();
		if (mouseEvent.getClickCount() == 2) {
			Point3f pt = getRenderer().getCurrentMouseLocation3D();
			ImageViewDescription.getInstance().setRow((int) (pt.x + 1));
			ImageViewDescription.getInstance().setCol((int) (pt.y + 1));
			ImageViewDescription.getInstance().setSlice((int) (pt.z + 1));
		}
	}

	public void playEvent() {
		getRenderer().syncPaint();
		super.playEvent();

	}

	public void stopEvent() {
		getRenderer().syncPaint();
		super.stopEvent();

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
		txParam.setValue(tx);
		tyParam.setValue(ty);
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
