/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
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

import org.imagesci.mogac.MACWE2D;

import com.jogamp.opencl.CLDevice;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example2i_macwe2d.
 */
public class Example2i_macwe2d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example2i_macwe2d()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Mumford-Shah segmentation with multi-object active contours 2D.";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "MACWE 2D";
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
		File fdepth = new File(workingDirectory, "kinect_depth.nii");

		File flabel = new File(workingDirectory, "kinect_labels.nii");
		File frgb = new File(workingDirectory, "kinect_rgb.png");
		ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
				.getInstance().read(flabel));
		ImageDataFloat pressureImage = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fdepth));

		ImageData refImage = PImageReaderWriter.convertToRGB(PImageReaderWriter
				.getInstance().read(frgb));

		MACWE2D activeContour = new MACWE2D(refImage, CLDevice.Type.GPU);
		activeContour.setPressure(pressureImage, 1E-5f);
		activeContour.setCurvatureWeight(0.5f);
		activeContour.setMaxIterations(500);
		activeContour.setClampSpeed(true);
		activeContour.setIntensityEstimation(true);
		activeContour.setIntensityEstimationInterval(50);
		if (showGUI) {
			try {
				activeContour.init(null, initLabels, false);

				VisualizationMOGAC2D visual = new VisualizationMOGAC2D(640,
						480, activeContour);
				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setPreferredSize(new Dimension(1050, 640));
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
			activeContour.solve(null, initLabels, false);
		}
	}

}
