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
import java.net.URISyntaxException;

import org.imagesci.gac.DistanceField2D;
import org.imagesci.gac.WEGAC2D;

import com.jogamp.opencl.CLDevice;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationGAC2D;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class Example1a_gac2d.
 */
public class Example1a_gac2d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static final void main(String[] args) {
		(new Example1a_gac2d()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Classic level set segmentation with a 2D geodesic active contour.";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "Active Contour 2D";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#launch(java.io.File, java.lang.String[])
	 */
	@Override
	public void launch(File workingDirectory, String[] args) {
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
			simulator.setPreserveTopology(false);
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
