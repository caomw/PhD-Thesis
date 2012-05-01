package imagesci.demo;

import imagesci.muscle.MuscleActiveContour2D;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class Example4a_muscle2d {
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File fdist = new File(PlaceHolder.class.getResource(
					"shapes_distfield.nii").toURI());

			File flabel = new File(PlaceHolder.class.getResource(
					"shapes_labels.nii").toURI());
			File fimg = new File(PlaceHolder.class.getResource("x.png").toURI());
			ImageDataFloat initDistField = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdist));
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat refImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(fimg));
			MuscleActiveContour2D activeContour = new MuscleActiveContour2D();
			activeContour.setPressure(refImage, -1.0f);
			activeContour.setCurvatureWeight(0.1f);
			activeContour.setTargetPressure(128.0f);
			activeContour.setMaxIterations(300);
			activeContour.setInitialLabelImage(initLabels);
			activeContour.setInitialDistanceFieldImage(initDistField);
			activeContour.setReferenceImage(refImage);
			if (showGUI) {
				try {
					activeContour.init();
					VisualizationMUSCLE2D visual = new VisualizationMUSCLE2D(
							512, 512, activeContour);
					VisualizationApplication app = new VisualizationApplication(
							visual);
					app.setPreferredSize(new Dimension(920, 650));
					app.setShowToolBar(true);
					app.addListener(visual);
					app.runAndWait();
					visual.dispose();
					System.exit(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				activeContour.solve();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
