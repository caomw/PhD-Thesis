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
