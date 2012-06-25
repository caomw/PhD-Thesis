/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.springls;

import java.awt.Dimension;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import org.imagesci.utility.PhantomSphere;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourMesh3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsEnrightDemo executes the enright test with either mesh or
 * volume visualization.
 */
public class EnrightDemo {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		int rows = 128;
		int cols = 128;
		int slices = 128;

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

		SpringlsActiveContour3D simulator = createEnrightTest(volRender, rows,
				cols, slices);
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

	/**
	 * Creates the enright test.
	 * 
	 * @param volRender
	 *            the vol render
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @return the springls active contour3 d
	 */
	public static final SpringlsActiveContour3D createEnrightTest(
			boolean volRender, int rows, int cols, int slices) {
		return createEnrightTest(volRender, rows, cols, slices, 500);
	}

	/**
	 * Creates the enright test.
	 * 
	 * @param volRender
	 *            the vol render
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @return the springls active contour3 d
	 */
	public static final SpringlsActiveContour3D createEnrightTest(
			boolean volRender, int rows, int cols, int slices, int timeSteps) {
		PhantomSphere phantom = new PhantomSphere(new Point3i(rows / 2,
				cols / 2, slices / 2));
		phantom.setCenter(new Point3d(-0.3, -0.3, -0.3));
		phantom.setRadius(0.3f);
		phantom.solve();
		EmbeddedSurface initSurface = phantom.getSurface();
		initSurface.scaleVertices(2.0f);
		phantom = new PhantomSphere(new Point3i(rows, cols, slices));
		phantom.setCenter(new Point3d(-0.3, -0.3, -0.3));
		phantom.setRadius(0.3f);
		phantom.solve();
		ImageDataFloat initImage = phantom.getLevelset();
		SpringlsActiveContour3D simulator = new SpringlsActiveContour3D();
		simulator.setTask(ActiveContour3D.Task.ENRIGHT);
		simulator.setPreserveTopology(false);
		simulator.setTargetPressure(0.0f);
		simulator.setAdvectionWeight(0.5f);
		simulator.setCurvatureWeight(0.01f);
		simulator.setPressureWeight(1.0f);
		if (volRender)
			simulator.setReferenceImage(phantom.getImage());
		simulator.setResamplingInterval(5);
		simulator.setMaxIterations((timeSteps * rows) / 128);
		simulator.setEnrightPeriod((timeSteps * rows) / 128);
		simulator.setInitialDistanceFieldImage(initImage);
		simulator.setInitialSurface(initSurface);
		return simulator;
	}
}
