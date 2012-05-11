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
package org.imagesci.springls;


import java.awt.Dimension;
import java.io.IOException;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import org.imagesci.gac.TopologyRule3D;
import org.imagesci.utility.IsoSurfaceGenerator;
import org.imagesci.utility.PhantomBubbles;
import org.imagesci.utility.PhantomCube;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLKernel;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourMesh3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsActiveContour3D.
 */
public class SpringlsActiveContour3D extends ActiveContour3D {

	/** The adaptive convergence. */
	protected boolean adaptiveConvergence = false;

	/** The adaptive convergence sampling interval. */
	protected int adaptiveConvergenceSamplingInterval = 8;

	/** The advect. */
	protected SpringlsAdvect3D advect = null;

	/** The commons. */
	protected SpringlsCommon3D commons;

	/** The contract. */
	protected SpringlsContract3D contract;

	/** The copy mesh to capsules. */
	protected CLKernel copyMeshToCapsules;

	/** The evolve. */
	protected SpringlsEvolveLevelSet3D evolve;

	/** The expand. */
	protected SpringlsExpand3D expand;

	/** The fill gaps. */
	protected SpringlsFillGaps3D fillGaps;

	/** The hash. */
	protected SpringlsSpatialHash3D hash;

	/** The iso surf. */
	protected SpringlsIsoSurface3D isoSurf;

	/** The relax. */
	protected SpringlsRelax3D relax;

	/** The resampling enabled. */
	protected boolean resamplingEnabled = true;

	/** The total time. */
	protected double totalTime = 0;

	/**
	 * Instantiates a new springls active contour3 d.
	 */
	public SpringlsActiveContour3D() {
		super();
		setLabel("Springls Active Contour");
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
		init(rows, cols, slices, new SpringlsCommon3D(Type.CPU));
	}

	/**
	 * Inits the.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @param commons
	 *            the commons
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init(int rows, int cols, int slices, SpringlsCommon3D commons)
			throws IOException {
		this.rows = rows;
		this.cols = cols;
		this.slices = slices;
		this.commons = commons;

		commons.setReferenceImage(referenceImage);
		SpringlsSurface surf = null;
		if (initialSurface == null) {
			IsoSurfaceGenerator isoGen = new IsoSurfaceGenerator(
					TopologyRule3D.Rule.CONNECT_6_26);
			isoGen.setUseResolutions(false);
			int r = initialDistanceFieldImage.getRows();
			int c = initialDistanceFieldImage.getCols();
			int s = initialDistanceFieldImage.getSlices();
			float[][][] img = initialDistanceFieldImage.toArray3d();
			ImageDataFloat imgDown = new ImageDataFloat(r / 2 + 1, c / 2 + 1,
					s / 2 + 1);
			float[][][] imageDown = imgDown.toArray3d();
			for (int i = 0; i < r; i += 2) {
				for (int j = 0; j < c; j += 2) {
					for (int k = 0; k < s; k += 2) {
						imageDown[i / 2][j / 2][k / 2] = img[i][j][k];
					}
				}
			}
			isoGen.setUseResolutions(false);
			EmbeddedSurface surf2 = isoGen.solve(imgDown, 0);
			surf2.scaleVertices(2.0f);
			surf = new SpringlsSurface(surf2);
		} else {
			surf = new SpringlsSurface(initialSurface);
		}
		commons.initialize(surf, rows, cols, slices, preserveTopology);
		contract = new SpringlsContract3D(commons);
		expand = new SpringlsExpand3D(commons);
		evolve = new SpringlsEvolveLevelSet3D(commons, curvatureWeight);
		(evolve).setAdaptiveUpdate(adaptiveConvergence);
		(evolve).setAdaptiveUpdateInterval(adaptiveConvergenceSamplingInterval);
		fillGaps = new SpringlsFillGaps3D(commons);
		hash = new SpringlsSpatialHash3D(commons);
		isoSurf = new SpringlsIsoSurface3D(commons);
		relax = new SpringlsRelax3D(commons, SpringlsConstants.relaxTimeStep);

		// WARNING: RESCALING WILL MODIFY ORIGINAL PRESSURE IMAGE
		advect = createAdvect();
		commons.setInitialSignedLevelSet(initialDistanceFieldImage.toArray3d());

		if (referencelevelSetImage != null) {
			commons.setReferenceLevelSet(referencelevelSetImage.toArray3d());
		} else {
			if (initialDistanceFieldImage != null) {
				commons.setReferenceLevelSet(initialDistanceFieldImage
						.toArray3d());
			} else {
				commons.setReferenceLevelSet(getUnsignedLevelSet().toArray3d());
			}
		}
		time = 0;

		evolve.rebuildNarrowBand();
		hash.updateSpatialHash();
		hash.updateNearestNeighbors();
		for (FrameUpdateListener listener : listeners) {
			listener.frameUpdate(0, getFrameRate());
		}
	}

	/**
	 * Creates the advect.
	 * 
	 * @return the springls advect3 d
	 */
	protected SpringlsAdvect3D createAdvect() {
		SpringlsAdvect3D advect = null;
		// WARNING: RESCALING WILL MODIFY ORIGINAL PRESSURE IMAGE
		if (task == Task.ACTIVE_CONTOUR) {
			if (resamplingEnabled) {
				if (pressureImage != null) {
					rescale(pressureImage.toArray3d());
					if (vecFieldImage != null) {
						advect = new SpringlsAdvect3D(commons,
								pressureImage.toArray3d(),
								vecFieldImage.toArray4d(), pressureWeight,
								advectionWeight);
					} else {
						advect = new SpringlsAdvect3D(commons,
								pressureImage.toArray3d(), pressureWeight);
					}
				} else if (vecFieldImage != null) {
					advect = new SpringlsAdvect3D(commons,
							vecFieldImage.toArray4d(), advectionWeight);
				}
			} else {
				if (pressureImage != null) {
					rescale(pressureImage.toArray3d());
					if (vecFieldImage != null) {
						advect = new SpringlsAdvectNoResample3D(commons,
								pressureImage.toArray3d(),
								vecFieldImage.toArray4d(), pressureWeight,
								advectionWeight);
					} else {
						advect = new SpringlsAdvectNoResample3D(commons,
								pressureImage.toArray3d(), pressureWeight);
					}
				} else if (vecFieldImage != null) {
					advect = new SpringlsAdvectNoResample3D(commons,
							vecFieldImage.toArray4d(), advectionWeight);
				}
			}
		} else {
			if (resamplingEnabled) {
				advect = new SpringlsAdvectEnright3D(commons, this, task,
						enrightPeriod);

			} else {
				System.err
						.println("Enright test without re-sampling is not supported.");
				System.exit(1);
			}
		}
		return advect;
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
	 * Step.
	 * 
	 * @return true, if successful
	 */
	public boolean step() {
		long updateSpatialLookUpTime = 0, advectTime = 0, relaxTime = 0, contractTime = 0, expandTime = 0, updateLevelSetTime = 0, fillGapsTime = 0, resampleTime = 0;
		int contractCount = 0, expandCount = 0, fillCount = 0;
		long initTime = System.nanoTime();
		long startTime = System.nanoTime();
		startTime = System.nanoTime();
		double displacement = 0;
		if (maxIterations > 0) {
			advect.advect(advectTimeStep);
		}
		advectTime = System.nanoTime() - startTime;
		startTime = System.nanoTime();
		hash.updateSpatialHash();
		updateSpatialLookUpTime = System.nanoTime() - startTime;
		if (resamplingEnabled) {
			startTime = System.nanoTime();
			relax.relax();
			relaxTime = System.nanoTime() - startTime;
			if (time % resamplingInterval == 0 && time != 0) {
				hash.updateUnsignedLevelSet();
				startTime = System.nanoTime();
				int elems = commons.elements;
				contractCount = contract.contract();
				if (contractCount >= 0.9 * elems) {
					System.err
							.println("Almost ("
									+ ((100 * contractCount) / elems)
									+ "%) entire constellation was destroyed in contract step.");
					System.err.flush();
					System.exit(1);
				}
				contractTime = System.nanoTime() - startTime;
				startTime = System.nanoTime();
				expandCount = expand.expand();
				expandTime = System.nanoTime() - startTime;
				evolve.evolve(false);
				if ((commons).activeListSize == 0) {
					return false;
				}
				updateLevelSetTime = System.nanoTime() - startTime;
				startTime = System.nanoTime();
				fillCount = fillGaps.fillGaps();
				fillGapsTime = System.nanoTime() - startTime;
				startTime = System.nanoTime();
				hash.updateSpatialHash();
				hash.updateNearestNeighbors();
				updateSpatialLookUpTime += System.nanoTime() - startTime;
				startTime = System.nanoTime();
				fillGaps.fillLabels();
				fillGapsTime += System.nanoTime() - startTime;
				resampleTime = contractTime + expandTime + fillGapsTime;
				System.gc();
			} else {
				startTime = System.nanoTime();
				hash.updateUnsignedLevelSet();
				evolve.evolve();
				updateLevelSetTime = System.nanoTime() - startTime;
			}
		} else {
			startTime = System.nanoTime();
			hash.updateUnsignedLevelSet();
			evolve.evolve();
			if ((commons).activeListSize == 0) {
				return false;
			}
			evolve.extendSignedDistanceField(5);
			
			updateLevelSetTime = System.nanoTime() - startTime;
		}
		long computeTime = System.nanoTime() - initTime;
		if (time % resamplingInterval == 0 && time != 0) {
			System.out
					.println(String
							.format("--- UPDATING SPRINGLS (%d iteration | %d springls | %4.4f sec | %d) ---",
									time, commons.elements, computeTime * 1E-9,
									commons.arrayLength));
			System.out.printf("SPATIAL LOOK-UP: %8.3f sec [%d] \n",
					updateSpatialLookUpTime * 1E-9, updateSpatialLookUpTime
							* 100 / computeTime);
			System.out.printf("RELAXATION: %8.3f sec [%d] \n",
					relaxTime * 1E-9, relaxTime * 100 / computeTime);
			System.out.printf("ADVECTION: %8.3f sec [%d] \n",
					1E-9 * advectTime, advectTime * 100 / computeTime);
			System.out.printf("CONTRACTION (%d): %8.3f sec [%d] \n",
					contractCount, 1E-9 * contractTime, contractTime * 100
							/ computeTime);
			System.out.printf("EXPANSION (" + expandCount
					+ "): %8.3f sec [%d] \n", 1E-9 * expandTime, expandTime
					* 100 / computeTime);
			System.out.printf("SURFACE EVOLUTION: %8.3f sec [%d] \n",
					updateLevelSetTime * 1E-9, updateLevelSetTime * 100
							/ computeTime);
			System.out.printf("FILL GAPS (%d): %8.3f sec [%d] \n", fillCount,
					fillGapsTime * 1E-9, fillGapsTime * 100 / computeTime);
		}
		SpringlsStatisticsLog.log.addEntry(commons.elements, fillCount,
				contractCount, updateSpatialLookUpTime, advectTime, relaxTime,
				resampleTime, updateLevelSetTime, computeTime, 0, displacement,
				time);
		time++;
		totalTime += computeTime * 1E-9;
		if (task != Task.ACTIVE_CONTOUR
				|| (time % resamplingInterval == 0 && time != 0)) {
			if (listeners.size() > 0) {
				hash.updateSpatialHash();
			}
			for (FrameUpdateListener listener : listeners) {
				listener.frameUpdate(time - 1, getFrameRate());
			}
		}

		return (time < maxIterations);
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

	/**
	 * Gets the frame rate.
	 * 
	 * @return the frame rate
	 */
	public double getFrameRate() {
		return (time > 0) ? time / totalTime : -1;
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
		if (initialDistanceFieldImage != null) {
			initialDistanceFieldImage.dispose();
		}
		if (referencelevelSetImage != null) {
			referencelevelSetImage.dispose();
		}
		initialSurface = null;
		pressureImage = null;
		vecFieldImage = null;
		initialDistanceFieldImage = null;
		referencelevelSetImage = null;
		System.gc();
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
	public SpringlsCommon3D getCommons() {
		return commons;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		boolean showGUI = true;
		boolean volRender = true;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("-volume")) {
				volRender = true;
			} else if (args[0].equalsIgnoreCase("-mesh")) {
				volRender = false;
			} else if (args[0].equalsIgnoreCase("-nogui")) {
				showGUI = false;
			}
		}
		PhantomCube phantom = new PhantomCube(new Point3i(64, 64, 64));
		phantom.setCenter(new Point3d(0, 0, 0));
		phantom.setWidth(1.21);
		phantom.solve();
		EmbeddedSurface initSurface = phantom.getSurface();
		initSurface.scaleVertices(2.0f);
		phantom = new PhantomCube(new Point3i(128, 128, 128));
		phantom.setCenter(new Point3d(0, 0, 0));
		phantom.setWidth(1.21);
		phantom.solve();
		ImageDataFloat initImage = phantom.getLevelset();

		PhantomBubbles bubbles = new PhantomBubbles(new Point3i(128, 128, 128));
		bubbles.setNoiseLevel(0);
		bubbles.setNumberOfBubbles(12);
		bubbles.setFuzziness(0.5f);
		bubbles.setMinRadius(0.2);
		bubbles.setMaxRadius(0.3);
		bubbles.setInvertImage(true);
		bubbles.solve();
		ImageDataFloat pressureImage = bubbles.getImage();

		SpringlsActiveContour3D simulator = new SpringlsActiveContour3D();
		simulator.setTask(ActiveContour3D.Task.ACTIVE_CONTOUR);
		simulator.setPressureImage(pressureImage);
		simulator.setReferenceImage(pressureImage);
		simulator.setPreserveTopology(false);
		simulator.setTargetPressure(0.5f);
		simulator.setAdvectionWeight(0.0f);
		simulator.setCurvatureWeight(0.01f);
		simulator.setPressureWeight(1.0f);
		simulator.setResamplingInterval(5);
		simulator.setMaxIterations(200);
		simulator.setInitialDistanceFieldImage(initImage);
		simulator.setInitialSurface(initSurface);
		if (showGUI) {
			try {
				simulator.init();
				VisualizationSpringlsActiveContour3D vis = (volRender) ? new VisualizationSpringlsActiveContourVolume3D(
						512, 512, simulator)
						: new VisualizationSpringlsActiveContourMesh3D(512,
								512, simulator);
				VisualizationApplication app = new VisualizationApplication(vis);
				app.setPreferredSize(new Dimension(920, 650));
				app.setShowToolBar(true);
				app.addListener(vis);
				app.runAndWait();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			simulator.solve();
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
	 * Gets the springls surface.
	 * 
	 * @return the springls surface
	 */
	public EmbeddedSurface getSpringlsSurface() {
		return commons.getSpringlsSurface();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.ActiveContour3D#init(int, int, int)
	 */
	@Override
	public void init(int rows, int cols, int slices) throws IOException {
		this.init(rows, cols, slices, new SpringlsCommon3D(CLDevice.Type.CPU));
	}

	/**
	 * Sets the adaptive convergence.
	 * 
	 * @param adaptive
	 *            the new adaptive convergence
	 */
	public void setAdaptiveConvergence(boolean adaptive) {
		this.adaptiveConvergence = adaptive;
	}

	/**
	 * Sets the adaptive convergence sampling interval.
	 * 
	 * @param interval
	 *            the new adaptive convergence sampling interval
	 */
	public void setAdaptiveConvergenceSamplingInterval(int interval) {
		this.adaptiveConvergenceSamplingInterval = interval;
	}

	/**
	 * Sets the resampling.
	 * 
	 * @param resampling
	 *            the new resampling
	 */
	public void setResampling(boolean resampling) {
		this.resamplingEnabled = resampling;
	}

}
