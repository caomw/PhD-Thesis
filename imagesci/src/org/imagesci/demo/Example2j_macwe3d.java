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

import org.imagesci.mogac.MACWE3D;

import com.jogamp.opencl.CLDevice;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example2j_macwe3d.
 */
public class Example2j_macwe3d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example2j_macwe3d()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Mumford-Shah segmentation with multi-object active contours 3D.";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "MACWE 3D";
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
		File fimg = new File(workingDirectory, "metacube.nii");
		File flabel = new File(workingDirectory, "ufo_labels.nii");
		File fdistfield = new File(workingDirectory, "ufo_distfield.nii");
		ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
				.getInstance().read(flabel));
		ImageDataFloat initDistfield = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fdistfield));
		ImageDataFloat refImage = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fimg));

		MACWE3D activeContour = new MACWE3D(refImage, CLDevice.Type.CPU);
		activeContour.setPressure(refImage, 1.0f);
		activeContour.setCurvatureWeight(0.5f);
		activeContour.setMaxIterations(340);
		activeContour.setClampSpeed(true);
		if (showGUI) {
			try {
				activeContour.init(initDistfield, initLabels, new double[] { 0,
						2, 1 }, false);

				VisualizationMOGAC3D visual = new VisualizationMOGAC3D(512,
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
			activeContour.solve(initDistfield, initLabels, new double[] { 0, 2,
					1 }, false);
		}
	}

}
