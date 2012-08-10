package org.imagesci.robopaint;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.imagesci.mogac.MOGAC3D;
import org.imagesci.mogac.WEMOGAC3D;
import org.imagesci.robopaint.GeometryViewDescription.GeometryViewListener;
import org.imagesci.robopaint.ImageViewDescription.ImageViewListener;
import org.imagesci.robopaint.ImageViewDescription.ParameterName;
import org.imagesci.robopaint.graphics.RoboRenderWidget;

import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class RoboRenderPane implements ImageViewListener, GeometryViewListener {
	private static RoboRenderWidget createVisual(int width, int height) {
		WEMOGAC3D activeContour = new WEMOGAC3D();
		RoboRenderWidget visual = new RoboRenderWidget(width, height,
				activeContour);
		return visual;

	}

	private MOGAC3D activeContour;
	private Frame frame;

	private RoboRenderWidget visual;

	public RoboRenderPane(Composite parent) {
		parent.getDisplay();
		parent.getShell();
		frame = SWT_AWT.new_Frame(parent);
		parent.getBounds();
		try {
			visual = createVisual(840, 700);
			this.activeContour = visual.getActiveContour();
			ParamCollection visualizationParameters = visual.create();
			visual.updateVisualizationParameters();
			ParamInputView inputView = visualizationParameters.getInputView();
			visual.updateVisualizationParameters();
			inputView.addObserver(visual);
			inputView.update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void launch() {
		if (visual != null) {
			Panel p = new Panel(new BorderLayout());
			if (visual != null) {
				p.add(visual.getComponent(), BorderLayout.CENTER);
				frame.add(p);
				frame.pack();
				frame.setVisible(true);
			}
		}
	}

	@Override
	public void updateParameter(GeometryViewDescription g,
			org.imagesci.robopaint.GeometryViewDescription.ParameterName p) {
		switch (p) {
		case OPEN_LABEL_IMAGE:
			activeContour.setLabelImage(new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(g.getLabelImageFile())));

			visual.updateImageSegmentation();
			break;
		case OPEN_DISTFIELD_IMAGE:
			activeContour.setDistanceFieldImage(new ImageDataFloat(
					NIFTIReaderWriter.getInstance().read(
							g.getDistanceFieldImageFile())));

			visual.updateImageSegmentation();
			break;
		case OPEN_IMAGE_SEGMENTATION:
			activeContour.setImageSegmentation(
					new ImageDataInt(NIFTIReaderWriter.getInstance().read(
							g.getLabelImageFile())),
					new ImageDataFloat(NIFTIReaderWriter.getInstance().read(
							g.getDistanceFieldImageFile())));

			visual.updateImageSegmentation();

			visual.updateVisualizationParameters();
			break;

		case ADD_OBJECT:
		case REMOVE_ALL_OBJECTS:
			break;
		default:
			visual.updateVisualizationParameters();
			break;
		}
	}

	@Override
	public void updateParameter(ImageViewDescription g, ParameterName p) {
		switch (p) {
		case OPEN_REFERENCE_IMAGE:
			edu.jhu.ece.iacl.jist.structures.image.ImageData img = NIFTIReaderWriter
					.getInstance().read(g.getImageFile());
			activeContour.setReferenceImage(img);
			ImageViewDescription.getInstance().setReferenceImage(img);
			visual.updateReferenceImage();

			visual.updateVisualizationParameters();
			break;
		default:
			visual.updateVisualizationParameters();
			break;
		}
	}

	public void dispose() {
		if (visual != null)
			visual.dispose();
		activeContour.dispose();
	}
}
