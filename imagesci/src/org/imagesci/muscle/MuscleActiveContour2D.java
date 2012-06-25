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
package org.imagesci.muscle;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import javax.vecmath.Point2f;

import org.imagesci.gac.TopologyRule2D;
import org.imagesci.mogac.MuscleEvolveLevelSet2D;
import org.imagesci.springls.SpringlsActiveContour2D;
import org.imagesci.springls.SpringlsAdvect2D;
import org.imagesci.springls.SpringlsAdvectEnright3D;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.springls.SpringlsContour;
import org.imagesci.springls.SpringlsContract2D;
import org.imagesci.springls.SpringlsExpand2D;
import org.imagesci.springls.SpringlsRelax2D;
import org.imagesci.springls.SpringlsSpatialHash2D;
import org.imagesci.springls.SpringlsStatisticsLog;
import org.imagesci.utility.ContourArray;
import org.imagesci.utility.IsoContourGeneratorMogac;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLDevice.Type;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsActiveContour2D.
 */
public class MuscleActiveContour2D extends SpringlsActiveContour2D {

	/** The advect. */
	protected SpringlsAdvect2D advect;

	/** The advect. */
	protected SpringlsAdvectEnright3D advectTest;

	/** The commons. */
	protected SpringlsCommon2D commons;

	/** The contract. */
	protected SpringlsContract2D contract;

	/** The evolve. */
	public MuscleEvolveLevelSet2D evolve;

	/** The expand. */
	protected SpringlsExpand2D expand;

	/** The fill gaps. */
	protected MuscleFillGaps2D fillGaps;

	/** The hash. */
	protected SpringlsSpatialHash2D hash;

	/** The copy mesh to capsules. */
	protected ImageDataInt labelImage;

	/** The iso surf. */
	// protected SpringlsIsoSurface2D isoSurf;

	/** The relax. */
	protected SpringlsRelax2D relax;

	/**
	 * Instantiates a new springls active contour3 d.
	 */
	public MuscleActiveContour2D() {
		super();
		setLabel("Springls Active Contour");
		listeners = new LinkedList<FrameUpdateListener>();
	}

	/**
	 * Solve.
	 */
	@Override
	public void solve() {
		try {
			setTotalUnits(maxIterations);
			init();
			cleanup();
			long startTime = System.nanoTime();
			while (step()) {
				incrementCompletedUnits();
			}
			long endTime = System.nanoTime();
			this.elapsedTime = endTime - startTime;
			System.out
					.printf("Time Steps: %d\nElapsed Time: %6.4f sec\nFrame Rate: %6.2f fps\n",
							time, 1E-9 * (endTime - startTime), 1E9 * time
									/ (endTime - startTime));

			markCompleted();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inits the.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void init() throws IOException {
		init(rows, cols, new MuscleCommon2D(Type.CPU));
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsActiveContour2D#init(int, int)
	 */
	@Override
	public void init(int rows, int cols) throws IOException {
		init(rows, cols, new SpringlsCommon2D(CLDevice.Type.CPU));
	}

	/**
	 * Inits the.
	 *
	 * @param rows the rows
	 * @param cols the cols
	 * @param commons the commons
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void init(int rows, int cols, SpringlsCommon2D commons)
			throws IOException {
		this.rows = rows;
		this.cols = cols;
		this.commons = commons;
		commons.initialize(rows, cols, preserveTopology);
		evolve = new MuscleEvolveLevelSet2D(commons, referenceImage,
				curvatureWeight);
		evolve.setPreserveTopology(preserveTopology);
		contract = new SpringlsContract2D(commons);
		expand = new SpringlsExpand2D(commons);
		fillGaps = new MuscleFillGaps2D(commons, evolve);

		evolve.init(this.initialLevelSetImage, this.labelImage, true);
		commons.signedLevelSetBuffer = evolve.distanceFieldBuffer;
		if (initialLevelSetImage == null) {
			initialLevelSetImage = evolve.getDistanceField();
		}
		IsoContourGeneratorMogac isoGen = new IsoContourGeneratorMogac(
				TopologyRule2D.Rule.CONNECT_4);
		isoGen.setUseResolutions(false);
		int r = initialLevelSetImage.getRows();
		int c = initialLevelSetImage.getCols();
		float[][] img = initialLevelSetImage.toArray2d();
		int[][] lab = labelImage.toArray2d();
		ImageDataFloat imgDown = new ImageDataFloat(r / 2 + 1, c / 2 + 1);
		ImageDataInt labDown = new ImageDataInt(r / 2 + 1, c / 2 + 1);
		float[][] imageDown = imgDown.toArray2d();
		int[][] labelsDown = labDown.toArray2d();
		for (int i = 0; i < r; i += 2) {
			for (int j = 0; j < c; j += 2) {
				imageDown[i / 2][j / 2] = img[i][j];
				labelsDown[i / 2][j / 2] = lab[i][j];
			}
		}

		ContourArray[] contours = isoGen.solve(imgDown, labDown,
				evolve.getLabelMasks(), evolve.containsOverlaps());
		SpringlsContour[] springls = new SpringlsContour[contours.length];
		int index = 0;
		for (ContourArray contour : contours) {
			for (Point2f pt : contour.points) {
				pt.scale(2.0f);
			}
			springls[index++] = new SpringlsContour(contour);

		}
		commons.setSpringls(springls, evolve.getLabelMasks());
		imgDown.dispose();
		labDown.dispose();
		hash = new MuscleSpatialHash2D(commons);
		relax = new SpringlsRelax2D(commons, SpringlsConstants.relaxTimeStep);
		// WARNING: RESCALING WILL MODIFY ORIGINAL PRESSURE IMAGE
		if (pressureImage != null) {
			rescale(pressureImage.toArray2d());
			if (vecFieldImage != null) {
				advect = new MuscleAdvect2D(commons, evolve,
						pressureImage.toArray2d(), vecFieldImage.toArray3d(),
						pressureWeight, advectionWeight);
			} else {
				advect = new MuscleAdvect2D(commons, evolve,
						pressureImage.toArray2d(), pressureWeight);
			}
		} else if (vecFieldImage != null) {
			advect = new MuscleAdvect2D(commons, evolve,
					vecFieldImage.toArray3d(), advectionWeight);
		}
		if (referencelevelSetImage != null) {
			commons.setReferenceLevelSet(referencelevelSetImage.toArray2d());
		} else {
			if (initialLevelSetImage != null) {
				commons.setReferenceLevelSet(initialLevelSetImage.toArray2d());
			} else {
				commons.setReferenceLevelSet(getUnsignedLevelSet().toArray2d());
			}
		}
		time = 0;
		hash.updateSpatialHash();
		hash.updateNearestNeighbors();
		hash.updateUnsignedLevelSet();
		evolve.evolve();
		hash.updateSpatialHash();
		for (FrameUpdateListener listener : listeners) {
			listener.frameUpdate(time - 1, -1);
		}

	}

	/**
	 * Gets the unsigned level set.
	 * 
	 * @return the unsigned level set
	 */
	@Override
	public ImageDataFloat getUnsignedLevelSet() {
		return new ImageDataFloat(commons.getUnsignedLevelSet());
	}

	/**
	 * Cleanup.
	 */
	@Override
	public void cleanup() {
		if (pressureImage != null) {
			pressureImage.dispose();
		}
		if (vecFieldImage != null) {
			vecFieldImage.dispose();
		}
		if (initialLevelSetImage != null) {
			initialLevelSetImage.dispose();
		}
		if (referencelevelSetImage != null) {
			referencelevelSetImage.dispose();
		}
		initialContour = null;
		pressureImage = null;
		vecFieldImage = null;
		initialLevelSetImage = null;
		referencelevelSetImage = null;
		System.gc();
	}

	/**
	 * Step.
	 * 
	 * @return true, if successful
	 */
	@Override
	public boolean step() {
		long updateNeighborsTime = 0, advectTime = 0, relaxTime = 0, contractTime = 0, expandTime = 0, updateContoursTime = 0, fillGapsTime = 0, resampleTime = 0;
		int contractCount = 0, fillCount = 0;
		long initTime = System.nanoTime();
		long startTime = System.nanoTime();
		startTime = System.nanoTime();
		double displacement = 0;

		advect.advect(1.0f);
		advectTime = System.nanoTime() - startTime;
		startTime = System.nanoTime();
		hash.updateSpatialHash();
		updateNeighborsTime = System.nanoTime() - startTime;
		startTime = System.nanoTime();
		relax.relax();
		relaxTime = System.nanoTime() - startTime;
		double dice = 0;

		if (time % resamplingInterval == 0 && time != 0) {
			hash.updateUnsignedLevelSet();
			startTime = System.nanoTime();
			contractCount = contract.contract();
			contractTime = System.nanoTime() - startTime;
			startTime = System.nanoTime();
			expand.expand();
			expandTime = System.nanoTime() - startTime;
			dice = evolve.evolve(false);
			hash.updateSpatialHash();
			updateContoursTime = System.nanoTime() - startTime;
			startTime = System.nanoTime();
			fillCount = fillGaps.fillGaps();
			fillGapsTime = System.nanoTime() - startTime;
			startTime = System.nanoTime();
			hash.updateSpatialHash();
			hash.updateNearestNeighbors();
			updateNeighborsTime += System.nanoTime() - startTime;
			startTime = System.nanoTime();
			fillGaps.fillLabels();
			fillGapsTime += System.nanoTime() - startTime;
			resampleTime = contractTime + expandTime + fillGapsTime;
			System.gc();
		} else {

			startTime = System.nanoTime();
			hash.updateUnsignedLevelSet();
			evolve.evolve();
			updateContoursTime = System.nanoTime() - startTime;
		}

		long computeTime = System.nanoTime() - initTime;
		SpringlsStatisticsLog.log.addEntry(commons.elements, fillCount,
				contractCount, updateNeighborsTime, advectTime, relaxTime,
				resampleTime, updateContoursTime, computeTime, dice,
				displacement, time);

		time++;
		totalTime += computeTime * 1E-9;
		if ((time - 1) % resamplingInterval == 0) {
			for (FrameUpdateListener listener : listeners) {
				listener.frameUpdate(time - 1, getFrameRate());
			}
		}

		return (time < maxIterations);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.algorithms.segmentation.SpringlsActiveContour#dispose()
	 */
	@Override
	public void dispose() {
		getCommons().dispose();
	}

	/**
	 * Gets the commons.
	 * 
	 * @return the commons
	 */
	@Override
	public SpringlsCommon2D getCommons() {
		return commons;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File fdist = new File(PlaceHolder.class.getResource(
					"shapes_distfield.nii").toURI());

			File flabel = new File(PlaceHolder.class.getResource(
					"shapes_labels.nii").toURI());
			File fimg = new File(PlaceHolder.class.getResource("x.png").toURI());
			ImageDataFloat initDistField = new ImageDataFloat(
					NIFTIReaderWriter.getInstance().read(fdist));
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
					VisualizationMUSCLE2D visual = new VisualizationMUSCLE2D(
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
				activeContour.solve();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Gets the distance field.
	 *
	 * @return the distance field
	 */
	public ImageDataFloat getDistanceField() {
		return evolve.getDistanceField();
	}

	/**
	 * Gets the image labels.
	 *
	 * @return the image labels
	 */
	public ImageDataInt getImageLabels() {
		return evolve.getImageLabels();
	}

	/**
	 * Gets the level set.
	 *
	 * @return the level set
	 */
	public ImageDataFloat getLevelSet() {
		return evolve.getDistanceField();
	}

	/**
	 * Gets the signed level set.
	 * 
	 * @return the signed level set
	 */
	@Override
	public ImageDataFloat getSignedLevelSet() {
		return new ImageDataFloat(commons.getSignedLevelSet());
	}

	/**
	 * Sets the initial level set image.
	 *
	 * @param labelImage the new initial label image
	 */
	public void setInitialLabelImage(ImageDataInt labelImage) {
		this.labelImage = labelImage;
		updateDimensions(labelImage);
	}

}
