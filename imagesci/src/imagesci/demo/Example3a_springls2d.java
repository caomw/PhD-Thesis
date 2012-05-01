package imagesci.demo;

import imagesci.gac.DistanceField2D;
import imagesci.springls.SpringlsActiveContour2D;

import java.awt.Dimension;
import java.io.File;
import java.net.URISyntaxException;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour2D;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class Example3a_springls2d {
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
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

			SpringlsActiveContour2D simulator = new SpringlsActiveContour2D();
			simulator.setPreserveTopology(false);
			simulator.setTargetPressure(230.0f);
			simulator.setAdvectionWeight(0.0f);
			simulator.setCurvatureWeight(0.1f);
			simulator.setPressureWeight(-1.0f);
			simulator.setResamplingInterval(5);
			simulator.setMaxIterations(450);
			simulator.setPressureImage(refImage);
			simulator.setInitialDistanceFieldImage(initImage);
			if (showGUI) {
				try {
					simulator.init();
					VisualizationSpringlsActiveContour2D visual = new VisualizationSpringlsActiveContour2D(
							600, 600, simulator);
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
			} else {
				simulator.solve();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
