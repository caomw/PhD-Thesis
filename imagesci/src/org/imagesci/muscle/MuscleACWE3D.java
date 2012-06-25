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
package org.imagesci.muscle;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.imagesci.springls.SpringlsAdvect3D;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.SurfaceVtkReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleACWE3D.
 */
public class MuscleACWE3D extends MuscleActiveContour3D {

	/** The estimation interval. */
	protected int estimationInterval = 10;

	/** The intensity estimation. */
	protected boolean intensityEstimation = false;

	/**
	 * Solve.
	 * 
	 * @param averages
	 *            the averages
	 */
	public void solve(double[] averages) {
		try {
			setTotalUnits(maxIterations);
			init();
			setAverages(averages);
			cleanup();
			while (step()) {
				incrementCompletedUnits();
			}
			markCompleted();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Solve.
	 * 
	 * @param referenceImage
	 *            the reference image
	 */
	public void solve(ImageData referenceImage) {
		try {
			setTotalUnits(maxIterations);
			init();
			setAverages(referenceImage);
			cleanup();
			while (step()) {
				incrementCompletedUnits();
			}
			markCompleted();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.muscle.MuscleActiveContour3D#init()
	 */
	@Override
	public void init() throws IOException {
		super.init();
		((MuscleAdvectACWE3D) advect)
				.setIntensityEstimation(intensityEstimation);
		((MuscleAdvectACWE3D) advect)
				.setIntensityEstimationInterval(estimationInterval);
	}

	/**
	 * Inits the.
	 * 
	 * @param averages
	 *            the averages
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init(double[] averages) throws IOException {
		super.init();
		setAverages(averages);
		((MuscleAdvectACWE3D) advect)
				.setIntensityEstimation(intensityEstimation);
		((MuscleAdvectACWE3D) advect)
				.setIntensityEstimationInterval(estimationInterval);
	}

	/**
	 * Sets the averages.
	 * 
	 * @param data
	 *            the new averages
	 */
	public void setAverages(double[] data) {
		((MuscleAdvectACWE3D) advect).setAverages(data);
	}

	/**
	 * Sets the averages.
	 * 
	 * @param referenceImage
	 *            the new averages
	 */
	public void setAverages(ImageData referenceImage) {
		((MuscleAdvectACWE3D) advect).setAverages(referenceImage);
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File flabel = new File(PlaceHolder.class.getResource(
					"ufo_labels.nii").toURI());
			File fdistfield = new File(PlaceHolder.class.getResource(
					"ufo_distfield.nii").toURI());
			File fimg = new File(PlaceHolder.class.getResource("metacube.nii")
					.toURI());
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
					VisualizationMUSCLE3D visual = new VisualizationMUSCLE3D(
							512, 512, activeContour);
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
				activeContour.solve(new double[] { 0, 2, 1 });
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Sets the intensity estimation.
	 * 
	 * @param dynamic
	 *            the new intensity estimation
	 */
	public void setIntensityEstimation(boolean dynamic) {
		this.intensityEstimation = dynamic;
	}

	/**
	 * Sets the intensity estimation interval.
	 * 
	 * @param interval
	 *            the new intensity estimation interval
	 */
	public void setIntensityEstimationInterval(int interval) {
		this.estimationInterval = interval;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.muscle.MuscleActiveContour3D#createAdvect()
	 */
	@Override
	protected SpringlsAdvect3D createAdvect() {
		return (resamplingEnabled) ? new MuscleAdvectACWE3D(commons, evolve,
				pressureImage.toArray3d(), pressureWeight)
				: new MuscleAdvectNoResampleACWE3D(commons, evolve,
						pressureImage.toArray3d(), pressureWeight);
	}
}
