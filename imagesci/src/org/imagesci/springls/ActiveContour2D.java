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


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.imagesci.springls.ActiveContour3D.Task;
import org.imagesci.utility.ContourArray;

import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class ActiveContour2D.
 */
public abstract class ActiveContour2D extends AbstractCalculation {
	/**
	 * The listener interface for receiving frameUpdate events. The class that
	 * is interested in processing a frameUpdate event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addFrameUpdateListener<code> method. When
	 * the frameUpdate event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see FrameUpdateEvent
	 */
	public static interface FrameUpdateListener {

		/**
		 * Frame update.
		 * 
		 * @param time
		 *            the time
		 * @param fps
		 *            the fps
		 */
		public void frameUpdate(long time, double fps);
	}

	/** The advection weight. */
	protected float advectionWeight = 1.0f;

	/** The curvature weight. */
	protected float curvatureWeight = 0.1f;
	/** The initial surface. */
	protected ContourArray initialContour = null;
	/** The initial level set image. */
	protected ImageDataFloat initialLevelSetImage = null;
	/** The listeners. */
	protected List<FrameUpdateListener> listeners = new LinkedList<FrameUpdateListener>();
	/** The max iterations. */
	protected int maxIterations = 300;
	/** The max layers. */
	protected int maxLayers = 3;

	/** The preserve topology. */
	protected boolean preserveTopology = false;

	/** The pressure image. */
	protected ImageDataFloat pressureImage = null;

	/** The pressure weight. */
	protected float pressureWeight = 1.0f;

	/** The reference image. */
	protected ImageData referenceImage;

	/** The referencelevel set image. */
	protected ImageDataFloat referencelevelSetImage = null;

	/** The re-sampling interval. */
	protected int resamplingInterval = 5;

	/** The slices. */
	protected int rows = 256, cols = 256;

	/** The target pressure. */
	protected float targetPressure = 0.5f;

	/** The task. */
	protected Task task = Task.ACTIVE_CONTOUR;

	/** The time. */
	protected long time = 0;

	/** The total time. */
	protected double totalTime = 0;
	/** The update iso surface. */
	protected boolean updateIsoSurface = true;
	/** The vec field image. */
	protected ImageDataFloat vecFieldImage = null;

	/**
	 * Sets the pressure.
	 *
	 * @param refImage the ref image
	 * @param f the f
	 */
	public void setPressure(ImageDataFloat refImage, float f) {
		setPressureImage(refImage);
		setPressureWeight(f);
	}

	/**
	 * Sets the pressure image.
	 * 
	 * @param pressureImage
	 *            the new pressure image
	 */
	public void setPressureImage(ImageDataFloat pressureImage) {
		this.pressureImage = pressureImage;
		updateDimensions(pressureImage);
	}

	/**
	 * Sets the vector field.
	 *
	 * @param refImage the ref image
	 * @param f the f
	 */
	public void setVectorField(ImageDataFloat refImage, float f) {
		setAdvectionWeight(f);
		setVecFieldImage(refImage);
	}

	/**
	 * Sets the vec field image.
	 * 
	 * @param vecFieldImage
	 *            the new vec field image
	 */
	public void setVecFieldImage(ImageDataFloat vecFieldImage) {
		this.vecFieldImage = vecFieldImage;
		updateDimensions(vecFieldImage);
	}

	/**
	 * Sets the initial level set image.
	 * 
	 * @param initialLevelSetImage
	 *            the new initial level set image
	 */
	public void setInitialDistanceFieldImage(ImageDataFloat initialLevelSetImage) {
		this.initialLevelSetImage = initialLevelSetImage;
		updateDimensions(initialLevelSetImage);
	}

	/**
	 * Sets the referencelevel set image.
	 * 
	 * @param referencelevelSetImage
	 *            the new referencelevel set image
	 */
	public void setReferencelevelSetImage(ImageDataFloat referencelevelSetImage) {
		this.referencelevelSetImage = referencelevelSetImage;
		updateDimensions(referencelevelSetImage);
	}

	/**
	 * Update dimensions.
	 * 
	 * @param ref
	 *            the ref
	 */
	protected void updateDimensions(ImageData ref) {
		if (ref != null) {
			this.rows = ref.getRows();
			this.cols = ref.getCols();
		}
	}

	/**
	 * Sets the pressure weight.
	 * 
	 * @param pressureWeight
	 *            the new pressure weight
	 */
	public void setPressureWeight(float pressureWeight) {
		this.pressureWeight = pressureWeight;
	}

	/**
	 * Sets the advection weight.
	 * 
	 * @param advectionWeight
	 *            the new advection weight
	 */
	public void setAdvectionWeight(float advectionWeight) {
		this.advectionWeight = advectionWeight;
	}

	/**
	 * Inits the.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init() throws IOException {
		init(rows, cols);
	}

	/**
	 * Inits the.
	 *
	 * @param rows the rows
	 * @param cols the cols
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void init(int rows, int cols) throws IOException;

	/**
	 * Adds the listener.
	 * 
	 * @param springlsRender
	 *            the springls render
	 */
	public void addListener(FrameUpdateListener springlsRender) {
		listeners.add(springlsRender);
	}

	/**
	 * Cleanup.
	 */
	public abstract void cleanup();

	/**
	 * Dispose.
	 */
	public abstract void dispose();

	/**
	 * Gets the cols.
	 *
	 * @return the cols
	 */
	public int getCols() {
		return cols;
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
	 * Gets the max iterations.
	 * 
	 * @return the max iterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Gets the pressure image.
	 *
	 * @return the pressure image
	 */
	public ImageDataFloat getPressureImage() {
		return pressureImage;
	}

	/**
	 * Gets the reference image.
	 * 
	 * @return the reference image
	 */
	public ImageData getReferenceImage() {
		return referenceImage;
	}

	/**
	 * Gets the reference level set.
	 *
	 * @return the reference level set
	 */
	public ImageDataFloat getReferenceLevelSet() {
		return referencelevelSetImage;
	}

	/**
	 * Gets the resampling interval.
	 * 
	 * @return the resampling interval
	 */
	public int getResamplingInterval() {
		return resamplingInterval;
	}

	/**
	 * Gets the rows.
	 *
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Gets the signed level set.
	 * 
	 * @return the signed level set
	 */
	public abstract ImageDataFloat getSignedLevelSet();

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Gets the vector field image.
	 *
	 * @return the vector field image
	 */
	public ImageDataFloat getVectorFieldImage() {
		return vecFieldImage;
	}

	/**
	 * Reset time.
	 */
	public void resetTime() {
		time = 0;
	}

	/**
	 * Sets the curvature weight.
	 * 
	 * @param curvatureWeight
	 *            the new curvature weight
	 */
	public void setCurvatureWeight(float curvatureWeight) {
		this.curvatureWeight = curvatureWeight;
	}

	/**
	 * Sets the initial surface.
	 * 
	 * @param initialSurface
	 *            the new initial surface
	 */
	public void setInitialSurface(ContourArray initialSurface) {
		this.initialContour = initialSurface;
	}

	/**
	 * Sets the max iterations.
	 * 
	 * @param maxIterations
	 *            the new max iterations
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * Sets the preserve topology.
	 *
	 * @param preserve the new preserve topology
	 */
	public void setPreserveTopology(boolean preserve) {
		this.preserveTopology = preserve;
	}

	/**
	 * Sets the reference image.
	 *
	 * @param refImage the new reference image
	 */
	public void setReferenceImage(ImageData refImage) {
		this.referenceImage = refImage;
	}

	/**
	 * Sets the resampling interval.
	 * 
	 * @param resamplingInterval
	 *            the new resampling interval
	 */
	public void setResamplingInterval(int resamplingInterval) {
		this.resamplingInterval = resamplingInterval;
	}

	/**
	 * Sets the target pressure.
	 * 
	 * @param targetPressure
	 *            the new target pressure
	 */
	public void setTargetPressure(float targetPressure) {
		this.targetPressure = targetPressure;
	}

	/**
	 * Sets the task.
	 * 
	 * @param task
	 *            the new task
	 */
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * Solve.
	 */
	public abstract void solve();

	/**
	 * Step.
	 *
	 * @return true, if successful
	 */
	public abstract boolean step();

	/**
	 * Rescale.
	 * 
	 * @param pressureForce
	 *            the pressure force
	 */
	protected void rescale(float[][] pressureForce) {
		int index = 0;
		double min = Float.MAX_VALUE;
		double max = Float.MIN_VALUE;
		if (!Float.isNaN(targetPressure)) {
			for (int i = 0; i < pressureForce.length; i++) {
				for (int j = 0; j < pressureForce[0].length; j++) {
					double val = pressureForce[i][j] - targetPressure;
					min = Math.min(val, min);
					max = Math.max(val, max);
					index++;
				}
			}
		}
		double normMin = (Math.abs(min) > 1E-4) ? 1 / Math.abs(min) : 1;
		double normMax = (Math.abs(max) > 1E-4) ? 1 / Math.abs(max) : 1;
		for (int i = 0; i < pressureForce.length; i++) {
			for (int j = 0; j < pressureForce[0].length; j++) {
				double val = pressureForce[i][j] - targetPressure;
				if (val < 0) {
					pressureForce[i][j] = (float) (val * normMin);
				} else {
					pressureForce[i][j] = (float) (val * normMax);
				}
			}
		}
	}

}
