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
package org.imagesci.demo;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import org.imagesci.muscle.MuscleActiveContour2D;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example4a_muscle2d.
 */
public class Example4a_muscle2d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example4a_muscle2d()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Parametric multi-object active contour segmentation with 2D MUSCLE.";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "MUSCLE 2D";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#launch(java.io.File, java.lang.String[])
	 */
	@Override
	public void launch(File workingDirectory, String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		File fdist = new File(workingDirectory, "shapes_distfield.nii");

		File flabel = new File(workingDirectory, "shapes_labels.nii");
		File fimg = new File(workingDirectory, "x.png");
		ImageDataFloat initDistField = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fdist));
		ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
				.getInstance().read(flabel));
		ImageDataFloat refImage = PImageReaderWriter
				.convertToGray(PImageReaderWriter.getInstance().read(fimg));
		MuscleActiveContour2D activeContour = new MuscleActiveContour2D();
		activeContour.setPressure(refImage, -1.0f);
		activeContour.setCurvatureWeight(0.1f);
		activeContour.setTargetPressure(128.0f);
		activeContour.setMaxIterations(300);
		activeContour.setInitialLabelImage(initLabels);
		activeContour.setInitialDistanceFieldImage(initDistField);
		activeContour.setReferenceImage(refImage);
		if (showGUI) {
			try {
				activeContour.init();
				VisualizationMUSCLE2D visual = new VisualizationMUSCLE2D(512,
						512, activeContour);
				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setPreferredSize(new Dimension(920, 650));
				app.setShowToolBar(true);
				app.addListener(visual);
				app.runAndWait();
				visual.dispose();
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			activeContour.solve();
		}

	}

}
