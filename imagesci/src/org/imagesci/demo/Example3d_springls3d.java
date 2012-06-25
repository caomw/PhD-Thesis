/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.demo;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import org.imagesci.springls.ActiveContour3D;
import org.imagesci.springls.SpringlsActiveContour3D;
import org.imagesci.utility.PhantomBubbles;
import org.imagesci.utility.PhantomCube;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourMesh3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class Example3d_springls3d.
 */
public class Example3d_springls3d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example3d_springls3d()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Parametric active contour segmentation with 3D topology-preserving Spring Level Sets.";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "SpringLS 3D with Topology Constraint";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#launch(java.io.File, java.lang.String[])
	 */
	@Override
	public void launch(File workingdirectory, String[] args) {
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
		simulator.setPreserveTopology(true);
		simulator.setTargetPressure(0.5f);
		simulator.setAdvectionWeight(0.0f);
		simulator.setCurvatureWeight(0.01f);
		simulator.setPressureWeight(1.0f);
		simulator.setResamplingInterval(5);
		simulator.setMaxIterations(210);
		simulator.setInitialDistanceFieldImage(initImage);
		simulator.setInitialSurface(initSurface);
		if (showGUI) {
			try {
				simulator.init();
				VisualizationSpringlsActiveContour3D vis = (volRender) ? new VisualizationSpringlsActiveContourVolume3D(
						600, 600, simulator)
						: new VisualizationSpringlsActiveContourMesh3D(600,
								600, simulator);
				VisualizationApplication app = new VisualizationApplication(vis);
				app.setPreferredSize(new Dimension(1024, 768));
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
