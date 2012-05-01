package imagesci.demo;

import imagesci.springls.EnrightDemo;
import imagesci.springls.SpringlsActiveContour3D;

import java.awt.Dimension;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourMesh3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;

public class Example3g_enright256 {
	public static void main(String[] args) {
		int rows = 256;
		int cols = 256;
		int slices = 256;

		boolean volRender = false;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("-volume")) {
				volRender = true;
			} else if (args[0].equalsIgnoreCase("-mesh")) {
				volRender = false;
			} else {
				System.err
						.println("Usage: EnrightDemo -[volume|mesh] [DIMENSION] ");
			}
			if (args.length > 1) {
				try {
					int dim = Integer.parseInt(args[1]);
					rows = cols = slices = dim;
				} catch (NumberFormatException e) {
					System.err
							.println("Argument should be a number [32 512] representing the dimension of the volume. Default is 128.");
				}
			}
		}

		SpringlsActiveContour3D simulator = EnrightDemo.createEnrightTest(
				volRender, rows, cols, slices);
		try {
			boolean show = true;
			if (show) {
				simulator.init();

				simulator.cleanup();

				VisualizationSpringlsActiveContour3D visual = (volRender) ? new VisualizationSpringlsActiveContourVolume3D(
						600, 600, simulator)
						: new VisualizationSpringlsActiveContourMesh3D(600,
								600, simulator);

				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setMinimumSize(new Dimension(640, 640));

				app.setShowToolBar(true);
				app.addListener(visual);

				app.setPreferredSize(new Dimension(1024, 768));
				app.runAndWait();
			} else {
				simulator.solve();
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
