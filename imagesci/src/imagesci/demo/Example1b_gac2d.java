package imagesci.demo;

import imagesci.gac.DistanceField2D;
import imagesci.gac.WEGAC2D;

import java.awt.Dimension;
import java.io.File;
import java.net.URISyntaxException;

import com.jogamp.opencl.CLDevice;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationGAC2D;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class Example1b_gac2d {
	public static final void main(String[] args) {
		File ftarget;
		try {
			ftarget = new File(PlaceHolder.class.getResource("target.png")
					.toURI());

			File fsource = new File(PlaceHolder.class.getResource("source.png")
					.toURI());

			ImageDataFloat sourceImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(
							fsource));

			ImageDataFloat refImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(
							ftarget));

			DistanceField2D df = new DistanceField2D();
			float[][] img = sourceImage.toArray2d();
			int r = img.length;
			int c = img[0].length;
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < c; j++) {
					img[i][j] -= 127.5f;
				}
			}
			ImageDataFloat initImage = df.solve(sourceImage, 15.0);

			WEGAC2D simulator = new WEGAC2D(CLDevice.Type.CPU);
			simulator.setTargetPressure(230.0f);

			// Preserve topology this time to see the difference!
			simulator.setPreserveTopology(true);
			simulator.setCurvatureWeight(0.2f);
			simulator.setMaxIterations(400);
			simulator.setClampSpeed(true);
			simulator.setReferenceImage(refImage);
			simulator.setPressure(refImage, -1.0f);
			simulator.setInitialDistanceFieldImage(initImage);
			simulator.setReferencelevelSetImage(refImage);
			try {
				simulator.init();
				VisualizationGAC2D visual = new VisualizationGAC2D(600, 600,
						simulator);
				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setMinimumSize(new Dimension(1024, 768));
				app.setShowToolBar(true);
				app.addListener(visual);
				app.runAndWait();
				visual.dispose();
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
