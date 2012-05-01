package imagesci.demo;

import imagesci.mogac.MACWE2D;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.jogamp.opencl.CLDevice;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class Example2i_macwe2d {
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File fdepth = new File(PlaceHolder.class.getResource(
					"kinect_depth.nii").toURI());

			File flabel = new File(PlaceHolder.class.getResource(
					"kinect_labels.nii").toURI());
			File frgb = new File(PlaceHolder.class
					.getResource("kinect_rgb.png").toURI());
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat pressureImage = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdepth));

			ImageData refImage = PImageReaderWriter
					.convertToRGB(PImageReaderWriter.getInstance().read(frgb));

			MACWE2D activeContour = new MACWE2D(refImage, CLDevice.Type.GPU);
			activeContour.setPressure(pressureImage, 1E-5f);
			activeContour.setCurvatureWeight(0.5f);
			activeContour.setMaxIterations(500);
			activeContour.setClampSpeed(true);
			activeContour.setIntensityEstimation(true);
			activeContour.setIntensityEstimationInterval(50);
			if (showGUI) {
				try {
					activeContour.init(null, initLabels, false);

					VisualizationMOGAC2D visual = new VisualizationMOGAC2D(640,
							480, activeContour);
					VisualizationApplication app = new VisualizationApplication(
							visual);
					app.setPreferredSize(new Dimension(1050, 640));
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
				activeContour.solve(null, initLabels, false);
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
