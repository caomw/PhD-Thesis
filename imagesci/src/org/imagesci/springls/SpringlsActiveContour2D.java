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
package org.imagesci.springls;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import javax.vecmath.Point2f;

import org.imagesci.gac.DistanceField2D;
import org.imagesci.gac.TopologyPreservationRule2D;
import org.imagesci.utility.ContourArray;
import org.imagesci.utility.IsoContourGenerator;

import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLKernel;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour2D;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsActiveContour2D.
 */
public class SpringlsActiveContour2D extends ActiveContour2D {

	/** The advect. */
	protected SpringlsAdvect2D advect;

	/** The advect. */
	protected SpringlsAdvectEnright3D advectTest;

	/** The commons. */
	protected SpringlsCommon2D commons;

	/** The contract. */
	protected SpringlsContract2D contract;

	/** The copy mesh to capsules. */
	protected CLKernel copyMeshToCapsules;

	/** The elapsed time. */
	protected long elapsedTime = 0;

	/** The evolve. */
	protected SpringlsEvolveLevelSet2D evolve;

	/** The expand. */
	protected SpringlsExpand2D expand;

	/** The fill gaps. */
	protected SpringlsFillGaps2D fillGaps;

	/** The hash. */
	protected SpringlsSpatialHash2D hash;

	/** The iso surf. */
	protected SpringlsIsoSurface2D isoSurf;

	/** The relax. */
	protected SpringlsRelax2D relax;

	/** The signed img. */
	ImageDataFloat signedImg;

	/**
	 * Instantiates a new springls active contour3 d.
	 */
	public SpringlsActiveContour2D() {
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
			commons.dispose();
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
		init(rows, cols, new SpringlsCommon2D(Type.CPU));
	}

	/**
	 * Inits the.
	 *
	 * @param rows the rows
	 * @param cols the cols
	 * @param commons the commons
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void init(int rows, int cols, SpringlsCommon2D commons)
			throws IOException {
		this.rows = rows;
		this.cols = cols;
		this.commons = commons;
		SpringlsContour surf = null;
		if (initialContour == null) {
			IsoContourGenerator isoGen = new IsoContourGenerator(
					TopologyPreservationRule2D.Rule.CONNECT_4);
			isoGen.setUseResolutions(false);

			int r = initialLevelSetImage.getRows();
			int c = initialLevelSetImage.getCols();
			float[][] img = initialLevelSetImage.toArray2d();
			ImageDataFloat imgDown = new ImageDataFloat(r / 2 + 1, c / 2 + 1);
			float[][] imageDown = imgDown.toArray2d();
			for (int i = 0; i < r; i += 2) {
				for (int j = 0; j < c; j += 2) {
					imageDown[i / 2][j / 2] = img[i][j];
				}
			}
			ContourArray contour = isoGen.solve(imgDown);
			contour.orient(imgDown);
			for (Point2f pt : contour.points) {
				pt.scale(2.0f);
			}
			surf = new SpringlsContour(contour);

		}
		commons.initialize(surf, rows, cols, preserveTopology);
		contract = new SpringlsContract2D(commons);
		expand = new SpringlsExpand2D(commons);
		evolve = new SpringlsEvolveLevelSet2D(commons, curvatureWeight);
		fillGaps = new SpringlsFillGaps2D(commons);
		hash = new SpringlsSpatialHash2D(commons);
		isoSurf = new SpringlsIsoSurface2D(commons);
		relax = new SpringlsRelax2D(commons, SpringlsConstants.relaxTimeStep);
		// WARNING: RESCALING WILL MODIFY ORIGINAL PRESSURE IMAGE
		if (pressureImage != null) {
			rescale(pressureImage.toArray2d());
			if (vecFieldImage != null) {
				advect = new SpringlsAdvect2D(commons,
						pressureImage.toArray2d(), vecFieldImage.toArray3d(),
						pressureWeight, advectionWeight);
			} else {
				advect = new SpringlsAdvect2D(commons,
						pressureImage.toArray2d(), pressureWeight);
			}
		} else if (vecFieldImage != null) {
			advect = new SpringlsAdvect2D(commons, vecFieldImage.toArray3d(),
					advectionWeight);
		}
		if (initialLevelSetImage != null) {
			commons.setInitialSignedLevelSet(initialLevelSetImage.toArray2d());
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
		evolve.rebuildNarrowBand();
		if (advect != null) {
			advect.advect(0.0f);
		}

		hash.updateSpatialHash();

		hash.updateNearestNeighbors();
		hash.updateUnsignedLevelSet();
		evolve.evolve();
		if (updateIsoSurface & listeners.size() > 0) {
			isoSurf.updateIsoSurface();
		}
		for (FrameUpdateListener listener : listeners) {
			listener.frameUpdate(time - 1, getFrameRate());
		}

	}

	/**
	 * Gets the unsigned level set.
	 * 
	 * @return the unsigned level set
	 */
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

		if (updateIsoSurface && listeners.size() > 0
				&& time % resamplingInterval == 0) {
			isoSurf.updateIsoSurface();
		}

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

			SpringlsActiveContour2D simulator = new SpringlsActiveContour2D();
			simulator.setPreserveTopology(false);
			simulator.setTargetPressure(230.0f);
			simulator.setAdvectionWeight(0.0f);
			simulator.setCurvatureWeight(0.1f);
			simulator.setPressureWeight(-1.0f);
			simulator.setResamplingInterval(5);
			simulator.setMaxIterations(450);
			simulator.setPressureImage(refImage);
			simulator.setInitialDistanceFieldImage(initImage);
			if (showGUI) {
				try {
					simulator.init();
					VisualizationSpringlsActiveContour2D visual = new VisualizationSpringlsActiveContour2D(
							600, 600, simulator);
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
			} else {
				simulator.solve();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
	 * Gets the time.
	 * 
	 * @return the time
	 */
	@Override
	public long getTime() {
		return time;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.ActiveContour2D#init(int, int)
	 */
	@Override
	public void init(int rows, int cols) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * Reset time.
	 */
	@Override
	public void resetTime() {
		time = 0;
	}

	/**
	 * Resample.
	 */
	protected void resample() {
		contract.contract();
		expand.expand();
		hash.updateSpatialHash();
		hash.updateNearestNeighbors();
		relax.relax();
		hash.updateUnsignedLevelSet();
		evolve.evolve();
		fillGaps.fillGaps();
		hash.updateSpatialHash();
		hash.updateNearestNeighbors();
		fillGaps.fillLabels();
		for (FrameUpdateListener listener : listeners) {
			listener.frameUpdate(time - 1, getFrameRate());
		}
	}
}
