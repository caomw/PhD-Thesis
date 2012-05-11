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

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3i;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.utility.PhantomMetasphere;
import org.imagesci.utility.PhantomSphere;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleApplyDisplacementField3D.
 */
public class MuscleApplyDisplacementField3D extends MuscleActiveContour3D {
	
	/**
	 * The Interface Stepper.
	 */
	public static interface Stepper {
		
		/**
		 * Gets the next deformation image.
		 *
		 * @param time the time
		 * @return the next deformation image
		 */
		public ImageDataFloat getNextDeformationImage(int time);

		/**
		 * Save deformation image.
		 *
		 * @param tracker the tracker
		 */
		public void saveDeformationImage(MuscleApplyDisplacementField3D tracker);
	}

	/** The Constant CONSTELLATION_STABILITY. */
	private static final float CONSTELLATION_STABILITY = 0.01f;

	/** The Constant MAX_RESAMPLE_CYCLES. */
	private static final int MAX_RESAMPLE_CYCLES = 20;

	/** The allow resampling. */
	protected boolean allowResampling = false;

	/**
	 * Instantiates a new muscle apply displacement field3 d.
	 */
	public MuscleApplyDisplacementField3D() {
		super();
	}

	/**
	 * Solve.
	 *
	 * @param deformationField the deformation field
	 */
	public void solve(ImageDataFloat deformationField) {
		try {
			init(deformationField);
			totalTime = 0;
			setTotalUnits(maxIterations);
			cleanup();
			while (step()) {
				incrementCompletedUnits();
			}
			System.out.println("Time Steps: " + maxIterations);
			System.out.println("Total Time: " + totalTime + " sec\n"
					+ "Frame Rate: " + time / totalTime + " fps");
			markCompleted();
		} catch (IOException e) {
			e.printStackTrace();
		}
		evolve.finish();
	}

	/**
	 * Inits the.
	 *
	 * @param deformationField the deformation field
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void init(ImageDataFloat deformationField) throws IOException {
		super.init();
		advect = new MuscleDeform3D(commons, evolve);
		float maxDisplacement = ((MuscleDeform3D) advect)
				.init(deformationField);
		final float MAX_TIME_STEP = 0.25f;
		int timeSteps = (int) Math.max(1,
				Math.ceil(maxDisplacement / MAX_TIME_STEP));
		advectTimeStep = (maxDisplacement / timeSteps);
		this.time = 0;
		this.maxIterations = timeSteps;

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.muscle.MuscleActiveContour3D#step()
	 */
	@Override
	public boolean step() {
		long initTime = System.nanoTime();
		if (advectTimeStep > 0) {
			advect.advect(advectTimeStep);
		}
		hash.updateSpatialHash();
		hash.updateUnsignedLevelSet();
		evolve.evolve();
		time++;
		if (allowResampling && time >= maxIterations) {
			hash.updateNearestNeighbors();
			relax.relax();
			int startSpringls = commons.elements;
			int fillCount, contractCount;
			int iter = 0;
			// Re-sampling cycle
			do {
				fillCount = fillGaps.fillGaps();
				hash.updateSpatialHash();
				hash.updateNearestNeighbors();
				fillGaps.fillLabels();
				relax.relax();
				contractCount = contract.contract();

				System.out.println("Re-sample Iteration " + iter + ") Fill: "
						+ (100 * fillCount / (float) startSpringls)
						+ "% Contract: "
						+ (100 * contractCount / (float) startSpringls) + "%");

				expand.expand();
				hash.updateSpatialHash();
				hash.updateUnsignedLevelSet();
				iter++;
			} while (iter < MAX_RESAMPLE_CYCLES
					&& (fillCount > startSpringls * CONSTELLATION_STABILITY || contractCount > startSpringls
							* CONSTELLATION_STABILITY));
			evolve.evolve();

		} else {
			hash.updateSpatialHash();
		}
		long computeTime = System.nanoTime() - initTime;
		totalTime += computeTime * 1E-9;

		for (FrameUpdateListener listener : listeners) {
			listener.frameUpdate(time, getFrameRate());
		}
		return (time < maxIterations);
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
			File fvecfield = new File(PlaceHolder.class.getResource(
					"demons_vecfield.nii").toURI());

			ImageDataFloat vecfield = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fvecfield));

			PhantomSphere sphere = new PhantomSphere(new Point3i(128, 128, 128));
			sphere.setNoiseLevel(0.1);
			sphere.setRadius(0.8);
			sphere.solve();
			ImageDataFloat initDistfield = sphere.getLevelset();
			CompressLevelSets compress = new CompressLevelSets(
					new ImageDataFloat[] { initDistfield });

			PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(
					128, 128, 128));
			metasphere.setNoiseLevel(0.1);
			metasphere.setFuzziness(0.5f);
			metasphere.setInvertImage(true);
			metasphere.solve();
			ImageDataFloat refImage = metasphere.getImage();

			MuscleApplyDisplacementField3D activeContour = new MuscleApplyDisplacementField3D();
			activeContour.setCurvatureWeight(0.1f);
			activeContour.setTargetPressure(0.5f);
			activeContour.setMaxIterations(130);
			activeContour.setReferenceImage(refImage);
			activeContour.setInitialLabelImage(compress.getLabelImage());
			activeContour.setInitialDistanceFieldImage(compress
					.getDistanceFieldImage());
			if (showGUI) {
				try {
					activeContour.init(vecfield);
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
				activeContour.solve(vecfield);
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.muscle.MuscleActiveContour3D#getComputationTime()
	 */
	@Override
	public double getComputationTime() {
		return totalTime;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsActiveContour3D#setResampling(boolean)
	 */
	@Override
	public void setResampling(boolean enable) {
		if (!enable) {
			this.resamplingInterval = -1;
		} else {
			this.resamplingInterval = 5;
		}
	}

	/**
	 * Transform.
	 *
	 * @param M the m
	 * @param smoothIters the smooth iters
	 */
	public void transform(Matrix4f M, int smoothIters) {

		final CLKernel copyBuffers = commons.kernelMap.get("copyFullBuffer");
		final CLKernel applyTransform = commons.kernelMap
				.get("affineTransformMogac");
		final CLKernel transformImage = commons.kernelMap
				.get("transformImageMogac");
		CLBuffer<FloatBuffer> modelViewMatrixBuffer = commons.context
				.createFloatBuffer(16, READ_ONLY, USE_BUFFER);
		CLBuffer<FloatBuffer> modelViewInverseMatrixBuffer = commons.context
				.createFloatBuffer(16, READ_ONLY, USE_BUFFER);
		M = new Matrix4f(M);
		FloatBuffer pbuff = modelViewMatrixBuffer.getBuffer();
		pbuff.put(M.m00);
		pbuff.put(M.m01);
		pbuff.put(M.m02);
		pbuff.put(M.m03);
		pbuff.put(M.m10);
		pbuff.put(M.m11);
		pbuff.put(M.m12);
		pbuff.put(M.m13);
		pbuff.put(M.m20);
		pbuff.put(M.m21);
		pbuff.put(M.m22);
		pbuff.put(M.m23);
		pbuff.put(M.m30);
		pbuff.put(M.m31);
		pbuff.put(M.m32);
		pbuff.put(M.m33);
		pbuff.rewind();
		commons.queue.putWriteBuffer(modelViewMatrixBuffer, true);
		M.invert();
		pbuff = modelViewInverseMatrixBuffer.getBuffer();
		pbuff.put(M.m00);
		pbuff.put(M.m01);
		pbuff.put(M.m02);
		pbuff.put(M.m03);
		pbuff.put(M.m10);
		pbuff.put(M.m11);
		pbuff.put(M.m12);
		pbuff.put(M.m13);
		pbuff.put(M.m20);
		pbuff.put(M.m21);
		pbuff.put(M.m22);
		pbuff.put(M.m23);
		pbuff.put(M.m30);
		pbuff.put(M.m31);
		pbuff.put(M.m32);
		pbuff.put(M.m33);
		pbuff.rewind();
		commons.queue.putWriteBuffer(modelViewInverseMatrixBuffer, true);
		long initTime = System.nanoTime();
		final int global_size = MOGAC3D.roundToWorkgroupPower(rows * cols
				* slices);
		applyTransform.putArgs(commons.capsuleBuffer, modelViewMatrixBuffer)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(applyTransform, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();

		System.out.println("Applying Inverse Transform");
		transformImage.putArgs(evolve.oldDistanceFieldBuffer,
				evolve.oldImageLabelBuffer, evolve.distanceFieldBuffer,
				evolve.imageLabelBuffer, modelViewInverseMatrixBuffer).rewind();
		commons.queue.put1DRangeKernel(transformImage, 0, global_size,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
		copyBuffers.putArgs(evolve.distanceFieldBuffer,
				evolve.imageLabelBuffer, evolve.oldDistanceFieldBuffer,
				evolve.oldImageLabelBuffer).rewind();
		commons.queue.put1DRangeKernel(copyBuffers, 0, global_size,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
		modelViewMatrixBuffer.release();
		modelViewInverseMatrixBuffer.release();
		(evolve).rebuildNarrowBand();
		hash.updateSpatialHash();
		hash.updateUnsignedLevelSet();
		for (int i = 0; i < smoothIters; i++) {
			evolve.evolve();
			time++;
		}
		long computeTime = System.nanoTime() - initTime;
		totalTime += computeTime * 1E-9;
	}
}
