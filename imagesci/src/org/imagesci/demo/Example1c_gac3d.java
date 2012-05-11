/**
 * ImageSci Toolkit
 *
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 * @author Blake Lucas (blake@cs.jhu.edu)
 */
package org.imagesci.demo;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import org.imagesci.gac.WEGAC3D;
import org.imagesci.utility.PhantomBubbles;
import org.imagesci.utility.PhantomCube;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationGAC3D;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class Example1c_gac3d.
 */
public class Example1c_gac3d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static final void main(String[] args) {
		(new Example1c_gac3d()).launch(args);
	}

	@Override
	public String getDescription() {
		return "Classic level set segmentation with a 3D geodesic active contour.";
	}

	@Override
	public String getName() {
		return "Active Contour 3D";
	}

	@Override
	public void launch(File workingDirectory, String[] args) {
		new Point3i(128, 128, 128);
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
			VisualizationGAC3D vis = new VisualizationGAC3D(600, 600, gac);
			VisualizationApplication app = new VisualizationApplication(vis);
			app.setPreferredSize(new Dimension(1024, 768));
			app.setShowToolBar(true);
			app.addListener(vis);
			app.runAndWait();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
