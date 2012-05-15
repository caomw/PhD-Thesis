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
package org.imagesci.mogac;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MACWE2D is an implementation of Multi-object Active Contours Without Edges 2D
 */
public class MACWE2D extends MOGAC2D {
	
	/** The areas. */
	protected CLBuffer<FloatBuffer> areas;

	/** The averages. */
	protected CLBuffer<FloatBuffer> averages;
	
	/** The intensity estimation. */
	protected boolean intensityEstimation = false;

	/** The intensity estimation interval. */
	protected int intensityEstimationInterval = 10;

	/** The stddev. */
	protected CLBuffer<FloatBuffer> stddev;

	/**
	 * Instantiates a new mACW e2 d.
	 *
	 * @param refImage the ref image
	 * @param type the type
	 */
	public MACWE2D(ImageData refImage, CLDevice.Type type) {
		super(refImage, type);
	}

	/**
	 * Instantiates a new mACW e2 d.
	 *
	 * @param rows the rows
	 * @param cols the cols
	 * @param context the context
	 * @param queue the queue
	 */
	public MACWE2D(int rows, int cols, CLContext context, CLCommandQueue queue) {
		super(rows, cols, context, queue);

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#init(edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat, edu.jhu.ece.iacl.jist.structures.image.ImageDataInt, boolean)
	 */
	@Override
	public void init(ImageDataFloat unsignedImage, ImageDataInt labelImage,
			boolean containsOverlaps) throws IOException {
		// targetPressure = 0;
		targetPressure = Float.NaN;
		super.init(unsignedImage, labelImage, containsOverlaps);
		averages = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
		areas = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
		stddev = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
		updateAverages();

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#step()
	 */
	@Override
	public boolean step() {
		final CLKernel acwePressureSpeed = kernelMap.get("acwePressureSpeed");
		final CLKernel maxImageValue = kernelMap.get("maxImageValue");
		final CLKernel applyForces = kernelMap.get("applyForces");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");
		if (intensityEstimation && time % intensityEstimationInterval == 0) {
			updateAverages();
		}
		int global_size = roundToWorkgroupPower(rows * cols);
		acwePressureSpeed
				.putArgs(pressureBuffer, oldDistanceFieldBuffer,
						oldImageLabelBuffer, deltaLevelSetBuffer, idBuffer,
						labelMaskBuffer, averages).putArg(pressureWeight)
				.putArg(curvatureWeight).rewind();
		queue.put1DRangeKernel(acwePressureSpeed, 0, global_size,
				WORKGROUP_SIZE);
		// Find max
		if (!clampSpeed) {
			if (maxTmpBuffer == null) {
				maxTmpBuffer = context.createFloatBuffer(rows, READ_WRITE,
						USE_BUFFER);
			}
			maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer).rewind();
			queue.put1DRangeKernel(maxImageValue, 0,
					roundToWorkgroupPower(rows), WORKGROUP_SIZE);
			queue.putReadBuffer(maxTmpBuffer, true);
			FloatBuffer maxBuff = maxTmpBuffer.getBuffer();
			float maxDelta = 0;
			while (maxBuff.hasRemaining()) {
				maxDelta = Math.max(maxBuff.get(), maxDelta);
			}
			maxBuff.rewind();
			float timeStep = 0.5f * ((maxDelta > maxSpeed) ? (maxSpeed / maxDelta)
					: maxSpeed);
			applyForces
					.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
							deltaLevelSetBuffer, idBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(timeStep).rewind();
		} else {
			applyForces
					.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
							deltaLevelSetBuffer, idBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(0.5f).rewind();
		}
		queue.put1DRangeKernel(applyForces, 0, global_size, WORKGROUP_SIZE);

		for (int i = 1; i <= maxLayers; i++) {
			extendDistanceField
					.putArgs(oldDistanceFieldBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(i).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0, global_size,
					WORKGROUP_SIZE);
		}
		// Remove isolated points

		if (!topologyPreservation) {
			final CLKernel plugLevelSet = kernelMap.get("plugLevelSet");
			plugLevelSet.putArgs(distanceFieldBuffer, imageLabelBuffer)
					.rewind();
			queue.put1DRangeKernel(plugLevelSet, 0, global_size, WORKGROUP_SIZE);
		}
		copyBuffers.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
				distanceFieldBuffer, imageLabelBuffer).rewind();
		queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
		contours = null;
		dirty = true;
		if (time % resamplingInterval == 0) {
			queue.finish();
			long tmp = System.nanoTime();
			for (FrameUpdateListener updater : listeners) {
				updater.frameUpdate((int) time, 1E9 * resamplingInterval
						/ (tmp - lastStartTime));
			}
			lastStartTime = tmp;
			incrementCompletedUnits();
		}
		time++;
		return true;
	}

	/**
	 * Update averages.
	 */
	protected void updateAverages() {

		final CLKernel regionAverage = kernelMap.get("regionAverage");
		final CLKernel sumAverages = kernelMap.get("sumAverages");
		regionAverage.putArgs(imageLabelBuffer, distanceFieldBuffer,
				pressureBuffer, labelMaskBuffer, averages, areas, stddev)
				.rewind();
		queue.put1DRangeKernel(regionAverage, 0, roundToWorkgroupPower(rows),
				WORKGROUP_SIZE);
		queue.finish();
		sumAverages.putArgs(averages, areas, stddev).rewind();
		queue.put1DRangeKernel(sumAverages, 0,
				roundToWorkgroupPower(numLabels), WORKGROUP_SIZE);
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
			File fdepth = new File(PlaceHolder.class.getResource(
					"kinect_depth.nii").toURI());

			File flabel = new File(PlaceHolder.class.getResource(
					"kinect_labels.nii").toURI());
			File frgb = new File(PlaceHolder.class
					.getResource("kinect_rgb.png").toURI());
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat pressureImage = new ImageDataFloat(
					NIFTIReaderWriter.getInstance().read(fdepth));

			ImageData refImage = PImageReaderWriter
					.convertToRGB(PImageReaderWriter.getInstance().read(frgb));

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
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#init(com.jogamp.opencl.CLBuffer, com.jogamp.opencl.CLBuffer, int)
	 */
	@Override
	public void init(CLBuffer<FloatBuffer> unsignedLevelSetBuffer,
			CLBuffer<IntBuffer> labelBuffer, int numClusters)
			throws IOException {
		// targetPressure = 0;
		targetPressure = Float.NaN;
		super.init(unsignedLevelSetBuffer, labelBuffer, numClusters);
		averages = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
		areas = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
		stddev = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.mogac.MOGAC2D#init(com.jogamp.opencl.CLBuffer, int)
	 */
	@Override
	public void init(CLBuffer<IntBuffer> labelBuffer, int numClusters)
			throws IOException {
		// targetPressure = 0;
		targetPressure = Float.NaN;
		super.init(null, labelBuffer, numClusters);
		averages = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
		areas = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
		stddev = context.createFloatBuffer(roundToWorkgroupPower(numLabels
				* rows), READ_WRITE, USE_BUFFER);
	}

	/**
	 * Sets the averages.
	 *
	 * @param data the new averages
	 */
	public void setAverages(double[] data) {
		FloatBuffer avgs = averages.getBuffer();
		for (int i = 0; i < data.length; i++) {
			float avg = (float) data[i];
			avgs.put(avg);
			System.out.println(i + ": " + avg);

		}
		avgs.rewind();
		queue.putWriteBuffer(averages, true);
	}

	/**
	 * Sets the intensity estimation.
	 *
	 * @param dynamic the new intensity estimation
	 */
	public void setIntensityEstimation(boolean dynamic) {
		this.intensityEstimation = dynamic;
	}

	/**
	 * Sets the intensity estimation interval.
	 *
	 * @param interval the new intensity estimation interval
	 */
	public void setIntensityEstimationInterval(int interval) {
		this.intensityEstimationInterval = interval;
	}

	/**
	 * Prints the averages.
	 */
	private void printAverages() {
		queue.putReadBuffer(averages, true).putReadBuffer(areas, true)
				.putReadBuffer(stddev, true);
		FloatBuffer avgs = averages.getBuffer();
		FloatBuffer area = areas.getBuffer();
		FloatBuffer std = stddev.getBuffer();
		int count = 0;
		while (avgs.hasRemaining()) {
			System.out.println(count + ": " + avgs.get() + ": " + area.get()
					+ " : " + std.get());
			count++;
			if (count >= numLabels) {
				break;
			}

		}
		std.rewind();
		avgs.rewind();
		area.rewind();
	}
}
