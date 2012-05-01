package imagesci.demo;

import imagesci.mogac.MOGAC2D;
import imagesci.mogac.WEMOGAC2D;

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
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class Example2d_wemogac2d {
	public static void main(String[] args) {
		try {
			File fdist = new File(PlaceHolder.class.getResource(
					"shapes_overlap_distfield.nii").toURI());

			File flabel = new File(PlaceHolder.class.getResource(
					"shapes_overlap_labels.nii").toURI());
			File fimg = new File(PlaceHolder.class.getResource("x.png").toURI());
			ImageDataFloat initDistField = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdist));
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat refImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(fimg));
			MOGAC2D activeContour = new WEMOGAC2D(refImage, CLDevice.Type.CPU);
			activeContour.setPressure(refImage, -0.5f);
			activeContour.setCurvatureWeight(1.0f);
			activeContour.setTargetPressure(128.0f);
			activeContour.setMaxIterations(601);
			activeContour.setPreserveTopology(true);
			activeContour.setClampSpeed(true);
			try {
				activeContour.init(initDistField, initLabels, true);

				VisualizationMOGAC2D visual = new VisualizationMOGAC2D(512,
						512, activeContour);
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

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
