package imagesci.demo;

import imagesci.muscle.MuscleActiveContour3D;
import imagesci.utility.PhantomMetasphere;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.vecmath.Point3i;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class Example4b_muscle3d {
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File flabel = new File(PlaceHolder.class.getResource(
					"ufo_labels.nii").toURI());
			File fdistfield = new File(PlaceHolder.class.getResource(
					"ufo_distfield.nii").toURI());
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat initDistfield = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdistfield));
			PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(
					128, 128, 128));
			metasphere.setNoiseLevel(0.1);
			metasphere.setFuzziness(0.5f);
			metasphere.setInvertImage(true);
			metasphere.solve();
			ImageDataFloat refImage = metasphere.getImage();

			MuscleActiveContour3D activeContour = new MuscleActiveContour3D();
			activeContour.setPressure(refImage, 1.0f);
			activeContour.setCurvatureWeight(0.1f);
			activeContour.setTargetPressure(0.5f);
			activeContour.setMaxIterations(130);
			activeContour.setReferenceImage(refImage);
			activeContour.setInitialLabelImage(initLabels);
			activeContour.setInitialDistanceFieldImage(initDistfield);
			if (showGUI) {
				try {
					activeContour.init();
					VisualizationMUSCLE3D visual = new VisualizationMUSCLE3D(
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
