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

import org.imagesci.mogac.MOGAC2D;
import org.imagesci.mogac.WEMOGAC2D;

import com.jogamp.opencl.CLDevice;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example2c_wemogac2d.
 */
public class Example2c_wemogac2d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example2c_wemogac2d()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Multi-object segmentation in 2D with Work-Efficient MOGAC";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "Work-Efficient MOGAC 2D";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#launch(java.io.File, java.lang.String[])
	 */
	@Override
	public void launch(File workingDirectory, String[] args) {
		File fdist = new File(workingDirectory, "shapes_overlap_distfield.nii");

		File flabel = new File(workingDirectory, "shapes_overlap_labels.nii");
		File fimg = new File(workingDirectory, "x.png");
		ImageDataFloat initDistField = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fdist));
		ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
				.getInstance().read(flabel));
		ImageDataFloat refImage = PImageReaderWriter
				.convertToGray(PImageReaderWriter.getInstance().read(fimg));
		MOGAC2D activeContour = new WEMOGAC2D(refImage, CLDevice.Type.CPU);
		activeContour.setPressure(refImage, -0.5f);
		activeContour.setCurvatureWeight(1.0f);
		activeContour.setTargetPressure(128.0f);
		activeContour.setMaxIterations(601);
		activeContour.setClampSpeed(true);
		try {
			activeContour.init(initDistField, initLabels, true);

			VisualizationMOGAC2D visual = new VisualizationMOGAC2D(512, 512,
					activeContour);
			VisualizationApplication app = new VisualizationApplication(visual);
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
	}

}
