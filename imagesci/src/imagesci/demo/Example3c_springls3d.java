package imagesci.demo;

import imagesci.springls.ActiveContour3D;
import imagesci.springls.SpringlsActiveContour3D;
import imagesci.utility.PhantomBubbles;
import imagesci.utility.PhantomCube;

import java.awt.Dimension;
import java.io.IOException;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourMesh3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class Example3c_springls3d {
	public static void main(String[] args) {
		boolean showGUI = true;
		boolean volRender = true;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("-volume")) {
				volRender = true;
			} else if (args[0].equalsIgnoreCase("-mesh")) {
				volRender = false;
			} else if (args[0].equalsIgnoreCase("-nogui")) {
				showGUI = false;
			}
		}
		PhantomCube phantom = new PhantomCube(new Point3i(64, 64, 64));
		phantom.setCenter(new Point3d(0, 0, 0));
		phantom.setWidth(1.21);
		phantom.solve();
		EmbeddedSurface initSurface = phantom.getSurface();
		initSurface.scaleVertices(2.0f);
		phantom = new PhantomCube(new Point3i(128, 128, 128));
		phantom.setCenter(new Point3d(0, 0, 0));
		phantom.setWidth(1.21);
		phantom.solve();
		ImageDataFloat initImage = phantom.getLevelset();

		PhantomBubbles bubbles = new PhantomBubbles(new Point3i(128, 128, 128));
		bubbles.setNoiseLevel(0);
		bubbles.setNumberOfBubbles(12);
		bubbles.setFuzziness(0.5f);
		bubbles.setMinRadius(0.2);
		bubbles.setMaxRadius(0.3);
		bubbles.setInvertImage(true);
		bubbles.solve();
		ImageDataFloat pressureImage = bubbles.getImage();

		SpringlsActiveContour3D simulator = new SpringlsActiveContour3D();
		simulator.setTask(ActiveContour3D.Task.ACTIVE_CONTOUR);
		simulator.setPressureImage(pressureImage);
		simulator.setReferenceImage(pressureImage);
		simulator.setPreserveTopology(false);
		simulator.setTargetPressure(0.5f);
		simulator.setAdvectionWeight(0.0f);
		simulator.setCurvatureWeight(0.01f);
		simulator.setPressureWeight(1.0f);
		simulator.setResamplingInterval(5);
		simulator.setMaxIterations(200);
		simulator.setInitialDistanceFieldImage(initImage);
		simulator.setInitialSurface(initSurface);
		if (showGUI) {
			try {
				simulator.init();
				VisualizationSpringlsActiveContour3D vis = (volRender) ? new VisualizationSpringlsActiveContourVolume3D(
						512, 512, simulator)
						: new VisualizationSpringlsActiveContourMesh3D(512,
								512, simulator);
				VisualizationApplication app = new VisualizationApplication(vis);
				app.setPreferredSize(new Dimension(920, 650));
				app.setShowToolBar(true);
				app.addListener(vis);
				app.runAndWait();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			simulator.solve();
		}
	}

}
