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

import org.imagesci.springls.EnrightDemo;
import org.imagesci.springls.SpringlsActiveContour3D;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourMesh3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;

// TODO: Auto-generated Javadoc
/**
 * The Class Example3g_enright256.
 */
public class Example3h_enright384 extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example3h_enright384()).launch(args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Deforms a sphere into a disk and back again with an invicid flow.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "Enright Deformation Test at 384^3";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.imagesci.demo.AbstractExample#launch(java.io.File,
	 * java.lang.String[])
	 */
	@Override
	public void launch(File workingDirectory, String[] args) {
		int rows = 384;
		int cols = 384;
		int slices = 384;

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
				volRender, rows, cols, slices, 600);
		try {
			boolean show = true;
			if (show) {
				simulator.init();

				simulator.cleanup();

				VisualizationSpringlsActiveContour3D visual = (volRender) ? new VisualizationSpringlsActiveContourVolume3D(
						600, 600, simulator)
						: new VisualizationSpringlsActiveContourMesh3D(1024,
								768, simulator);

				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setMinimumSize(new Dimension(1024, 768));

				app.setShowToolBar(true);
				app.addListener(visual);

				app.setPreferredSize(new Dimension(1200, 800));
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
