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
package org.imagesci.muscle;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import javax.vecmath.Point3i;

import org.imagesci.gac.TopologyRule3D;
import org.imagesci.mogac.MuscleEvolveLevelSet3D;
import org.imagesci.mogac.WEMOGAC3D;
import org.imagesci.springls.SpringlsActiveContour3D;
import org.imagesci.springls.SpringlsAdvect3D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.springls.SpringlsContract3D;
import org.imagesci.springls.SpringlsExpand3D;
import org.imagesci.springls.SpringlsRelax3D;
import org.imagesci.springls.SpringlsStatisticsLog;
import org.imagesci.springls.SpringlsSurface;
import org.imagesci.utility.IsoSurfaceGeneratorMogac;
import org.imagesci.utility.PhantomMetasphere;

import com.jogamp.opencl.CLDevice.Type;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.SurfaceVtkReaderWriter;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleActiveContour3D.
 */
public class MuscleActiveContour3D extends SpringlsActiveContour3D {

	/** The evolve. */
	public MuscleEvolveLevelSet3D evolve;

	/** The copy mesh to capsules. */
	protected ImageDataInt labelImage;

	/**
	 * Instantiates a new springls active contour3 d.
	 */
	public MuscleActiveContour3D() {
		super();
		setLabel("Springls Active Contour");
		listeners = new LinkedList<FrameUpdateListener>();
	}

	/**
	 * .
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
	 * Smooth.
	 */
	public void smooth() {
		try {
			init();
			cleanup();
			for (int i = 0; i < maxIterations; i++) {
				evolve.evolve();
				time++;
			}
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
		init(rows, cols, slices, new MuscleCommon3D(Type.CPU));
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
	@Override
	public void init(int rows, int cols, int slices, SpringlsCommon3D commons)
			throws IOException {
		this.rows = rows;
		this.cols = cols;
		this.slices = slices;
		this.commons = commons;
		((MuscleCommon3D) commons).setActiveContour(this);
		commons.setResampling(resamplingEnabled);
		commons.initialize(rows, cols, slices, preserveTopology);
		evolve = new MuscleEvolveLevelSet3D(commons, referenceImage,
				curvatureWeight);

		evolve.setPreserveTopology(preserveTopology);
		hash = new MuscleSpatialHash3D(commons);
		contract = new SpringlsContract3D(commons);
		expand = new SpringlsExpand3D(commons);

		fillGaps = new MuscleFillGaps3D(commons, evolve);
		if (initialSurface == null) {
			evolve.init(this.initialDistanceFieldImage, this.labelImage, false);
			if (initialDistanceFieldImage == null) {
				initialDistanceFieldImage = evolve.getDistanceField();
			}
			IsoSurfaceGeneratorMogac isoGen = new IsoSurfaceGeneratorMogac(
					TopologyRule3D.Rule.CONNECT_6_26);

			isoGen.setUseResolutions(false);
			int r = initialDistanceFieldImage.getRows();
			int c = initialDistanceFieldImage.getCols();
			int s = initialDistanceFieldImage.getSlices();
			float[][][] img = initialDistanceFieldImage.toArray3d();
			int[][][] lab = labelImage.toArray3d();
			ImageDataFloat imgDown = new ImageDataFloat(r / 2 + 1, c / 2 + 1,
					s / 2 + 1);
			ImageDataInt labDown = new ImageDataInt(r / 2 + 1, c / 2 + 1,
					s / 2 + 1);
			float[][][] imageDown = imgDown.toArray3d();
			int[][][] labelsDown = labDown.toArray3d();
			for (int i = 0; i < r; i += 2) {
				for (int j = 0; j < c; j += 2) {
					for (int k = 0; k < s; k += 2) {
						imageDown[i / 2][j / 2][k / 2] = img[i][j][k];
						labelsDown[i / 2][j / 2][k / 2] = lab[i][j][k];
					}
				}
			}
			EmbeddedSurface[] contours = isoGen.solve(imgDown, labDown,
					evolve.getLabelMasks(), evolve.containsOverlaps());
			SpringlsSurface[] springls = new SpringlsSurface[contours.length];
			int index = 0;
			for (EmbeddedSurface contour : contours) {
				contour.scaleVertices(2.0f);
				springls[index++] = new SpringlsSurface(contour);
			}
			commons.setSpringls(springls, evolve.getLabelMasks());
			imgDown.dispose();
			labDown.dispose();
		} else {
			SpringlsSurface springls = new SpringlsSurface(initialSurface);
			commons.setSpringls(springls);
			if (labelImage == null) {
				MuscleIsoSurface3D iso = new MuscleIsoSurface3D(commons, evolve);
				hash.updateSpatialHash();
				this.labelImage = iso
						.createLabelImage(springls.getLabelMasks());
			}
			System.out.println("Initialize with existing constellation");
			evolve.init(this.initialDistanceFieldImage, this.labelImage, false);

		}
		commons.signedLevelSetBuffer = evolve.distanceFieldBuffer;
		relax = new SpringlsRelax3D(commons, SpringlsConstants.relaxTimeStep);
		// WARNING: RESCALING WILL MODIFY ORIGINAL PRESSURE IMAGE
		advect = createAdvect();
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
		hash.updateSpatialHash();
		hash.updateNearestNeighbors();
		hash.updateUnsignedLevelSet();
		evolve.evolve();

		hash.updateSpatialHash();
		for (FrameUpdateListener listener : listeners) {
			listener.frameUpdate(0, getFrameRate());
		}

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsActiveContour3D#createAdvect()
	 */
	@Override
	protected SpringlsAdvect3D createAdvect() {
		SpringlsAdvect3D advect = null;
		if (resamplingEnabled) {
			if (pressureImage != null) {
				rescale(pressureImage.toArray3d());
				if (vecFieldImage != null) {
					advect = new MuscleAdvect3D(commons, evolve,
							pressureImage.toArray3d(),
							vecFieldImage.toArray4d(), pressureWeight,
							advectionWeight);
				} else {
					advect = new MuscleAdvect3D(commons, evolve,
							pressureImage.toArray3d(), pressureWeight);
				}
			} else if (vecFieldImage != null) {
				advect = new MuscleAdvect3D(commons, evolve,
						vecFieldImage.toArray4d(), advectionWeight);
			}
		} else {
			if (pressureImage != null) {
				rescale(pressureImage.toArray3d());
				if (vecFieldImage != null) {
					advect = new MuscleAdvectNoResample3D(commons, evolve,
							pressureImage.toArray3d(),
							vecFieldImage.toArray4d(), pressureWeight,
							advectionWeight);
				} else {
					advect = new MuscleAdvectNoResample3D(commons, evolve,
							pressureImage.toArray3d(), pressureWeight);
				}
			} else if (vecFieldImage != null) {
				advect = new MuscleAdvectNoResample3D(commons, evolve,
						vecFieldImage.toArray4d(), advectionWeight);
			}
		}
		return advect;
	}

	/**
	 * Gets the unsigned level set.
	 * 
	 * @return the unsigned level set
	 */
	@Override
	public ImageDataFloat getUnsignedLevelSet() {
		ImageDataFloat unsigned = new ImageDataFloat(
				commons.getUnsignedLevelSet());
		unsigned.setName("unsigned_levelset");
		return unsigned;
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

	/**
	 * Step.
	 * 
	 * @return true, if successful
	 */
	@Override
	public boolean step() {
		if (maxIterations == 0) {
			return false;
		}

		long updateSpatialLookUpTime = 0, advectTime = 0, relaxTime = 0, contractTime = 0, expandTime = 0, updateLevelSetTime = 0, fillGapsTime = 0, resampleTime = 0;
		int contractCount = 0, expandCount = 0, fillCount = 0;
		long computeTime = 0;
		long initTime = System.nanoTime();
		long startTime = System.nanoTime();
		startTime = System.nanoTime();
		double displacement = 0;
		advect.advect(advectTimeStep);
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
				hash.updateSpatialHash();
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
				if ((commons).activeListSize == 0) {
					return false;
				}
				updateLevelSetTime = System.nanoTime() - startTime;
			}
		} else {
			startTime = System.nanoTime();
			hash.updateUnsignedLevelSet();
			evolve.evolve();
			if ((commons).activeListSize == 0) {
				return false;
			}
			evolve.extendDistanceField(5);
			updateLevelSetTime = System.nanoTime() - startTime;
		}

		computeTime = System.nanoTime() - initTime;
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
		if ((time - 1) % resamplingInterval == 0 && time != 1) {
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
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File flabel = new File(PlaceHolder.class.getResource(
					"ufo_labels.nii").toURI());
			File fdistfield = new File(PlaceHolder.class.getResource(
					"ufo_distfield.nii").toURI());
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat initDistfield = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdistfield));
			PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(
					128, 128, 128));
			metasphere.setNoiseLevel(0.1);
			metasphere.setFuzziness(0.5f);
			metasphere.setInvertImage(true);
			metasphere.solve();
			ImageDataFloat refImage = metasphere.getImage();

			MuscleActiveContour3D activeContour = new MuscleActiveContour3D();
			activeContour.setPressure(refImage, 1.0f);
			activeContour.setCurvatureWeight(0.1f);
			activeContour.setTargetPressure(0.5f);
			activeContour.setResampling(false);
			activeContour.setMaxIterations(130);
			activeContour.setReferenceImage(refImage);
			activeContour.setInitialLabelImage(initLabels);
			activeContour.setInitialDistanceFieldImage(initDistfield);
			if (showGUI) {
				try {
					activeContour.init();
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
				activeContour.solve();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Adds the listener.
	 * 
	 * @param springlsRender
	 *            the springls render
	 */
	@Override
	public void addListener(FrameUpdateListener springlsRender) {
		listeners.add(springlsRender);
	}

	/**
	 * Gets the computation time.
	 * 
	 * @return the computation time
	 */
	public double getComputationTime() {
		return totalTime;
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
	 * Gets the signed level set.
	 * 
	 * @return the signed level set
	 */
	@Override
	public ImageDataFloat getSignedLevelSet() {
		ImageDataFloat signed = new ImageDataFloat(commons.getSignedLevelSet());
		signed.setName("signed_levelset");
		return signed;
	}

	/**
	 * Sets the initial level set image.
	 * 
	 * @param labelImage
	 *            the new initial label image
	 */
	public void setInitialLabelImage(ImageDataInt labelImage) {
		this.labelImage = labelImage;
		updateDimensions(labelImage);
	}
}
