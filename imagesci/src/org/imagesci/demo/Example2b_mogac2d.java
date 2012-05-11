/**
 * ImageSci Toolkit
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

import org.imagesci.mogac.MOGAC2D;

import com.jogamp.opencl.CLDevice;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example2b_mogac2d.
 */
public class Example2b_mogac2d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example2b_mogac2d()).launch(args);
	}

	@Override
	public String getDescription() {
		return "Multi-object segmentation in 2D with topology-preserving MOGAC.";
	}

	@Override
	public String getName() {
		return "MOGAC 2D with Topology Constraint";
	}

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
		MOGAC2D activeContour = new MOGAC2D(refImage, CLDevice.Type.GPU);
		activeContour.setPressure(refImage, -0.5f);
		activeContour.setCurvatureWeight(1.0f);
		activeContour.setTargetPressure(128.0f);
		activeContour.setMaxIterations(1000);
		activeContour.setPreserveTopology(true);
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
