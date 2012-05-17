/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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

import org.imagesci.muscle.MuscleACWE3D;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example4d_muscle_acwe3d.
 */
public class Example4d_muscle_acwe3d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example4d_muscle_acwe3d()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Mumford-shah segmentation with MUSCLE 3D.";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "MUSCLE MACWE 3D";
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
		File flabel = new File(workingDirectory, "ufo_labels.nii");
		File fdistfield = new File(workingDirectory, "ufo_distfield.nii");
		File fimg = new File(workingDirectory, "metacube.nii");
		ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
				.getInstance().read(flabel));
		ImageDataFloat initDistfield = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fdistfield));
		ImageDataFloat refImage = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fimg));
		MuscleACWE3D activeContour = new MuscleACWE3D();
		activeContour.setPressure(refImage, 1.0f);
		activeContour.setCurvatureWeight(0.1f);
		activeContour.setTargetPressure(0.5f);
		activeContour.setMaxIterations(300);
		activeContour.setReferenceImage(refImage);
		activeContour.setInitialLabelImage(initLabels);
		activeContour.setInitialDistanceFieldImage(initDistfield);
		if (showGUI) {
			try {
				activeContour.init(new double[] { 0, 2, 1 });
				VisualizationMUSCLE3D visual = new VisualizationMUSCLE3D(600,
						600, activeContour);
				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setPreferredSize(new Dimension(1024, 768));
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
			activeContour.solve(new double[] { 0, 2, 1 });
		}

	}

}
