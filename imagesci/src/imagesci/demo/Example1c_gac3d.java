package imagesci.demo;

import imagesci.gac.WEGAC3D;
import imagesci.utility.PhantomBubbles;
import imagesci.utility.PhantomCube;

import java.awt.Dimension;
import java.io.IOException;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationGAC3D;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class Example1c_gac3d {
	public static final void main(String[] args) {
		System.out.println("Starting Geodesic Active Contour test ...");
		System.out
				.println("Did you remember to install the latest OpenCL drivers for your GPU and CPU?");
		Point3i dims = new Point3i(128, 128, 128);
		PhantomCube phantom = new PhantomCube(new Point3i(128, 128, 128));
		phantom.setCenter(new Point3d(0, 0, 0));
		phantom.setWidth(1.21);
		phantom.solve();
		ImageDataFloat levelset = phantom.getLevelset();

		PhantomBubbles bubbles = new PhantomBubbles(new Point3i(128, 128, 128));
		bubbles.setNoiseLevel(0);
		bubbles.setNumberOfBubbles(12);
		bubbles.setFuzziness(0.5f);
		bubbles.setMinRadius(0.2);
		bubbles.setMaxRadius(0.3);
		bubbles.setInvertImage(true);
		bubbles.solve();
		ImageDataFloat pressureImage = bubbles.getImage();

		WEGAC3D gac = new WEGAC3D();
		gac.setPreserveTopology(false);// Turn off adaptive convergence if you
										// use topology preservation
		gac.setPressure(pressureImage, 0.5f);
		gac.setReferenceImage(pressureImage);
		gac.setInitialDistanceFieldImage(levelset);
		gac.setCurvatureWeight(0.5f);
		gac.setMaxIterations(200);
		gac.setTargetPressure(0.5f);
		gac.setClampSpeed(true);// This is for speed enhancement only, it may
								// cause bad things to happen if not used
								// correctly.
		gac.setAdaptiveConvergence(true);
		try {
			gac.init();
			VisualizationGAC3D vis = new VisualizationGAC3D(512, 512, gac);
			VisualizationApplication app = new VisualizationApplication(vis);
			app.setPreferredSize(new Dimension(920, 650));
			app.setShowToolBar(true);
			app.addListener(vis);
			app.runAndWait();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
