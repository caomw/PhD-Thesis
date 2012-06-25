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

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;


import java.nio.FloatBuffer;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsConstants;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleAdvectACWE3D.
 */
public class MuscleAdvectACWE3D extends MuscleAdvect3D {
	
	/** The areas. */
	public CLBuffer<FloatBuffer> areas;
	
	/** The averages. */
	public CLBuffer<FloatBuffer> averages;
	
	/** The estimation interval. */
	public int estimationInterval = 5;
	
	/** The intensity estimation. */
	protected boolean intensityEstimation = false;
	
	/** The stddev. */
	public CLBuffer<FloatBuffer> stddev;

	/** The time. */
	int time = 0;

	/**
	 * Instantiates a new muscle advect acw e3 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 * @param pressureImage the pressure image
	 * @param pressureWeight the pressure weight
	 */
	public MuscleAdvectACWE3D(SpringlsCommon3D commons, MOGAC3D mogac,
			float[][][] pressureImage, float pressureWeight) {
		super(commons, mogac, pressureImage, pressureWeight);
		int numLabels = mogac.getNumLabels();
		averages = commons.context.createFloatBuffer(SpringlsCommon3D
				.roundToWorkgroupPower(numLabels * mogac.slices), READ_WRITE,
				USE_BUFFER);
		areas = commons.context.createFloatBuffer(SpringlsCommon3D
				.roundToWorkgroupPower(numLabels * mogac.slices), READ_WRITE,
				USE_BUFFER);
		stddev = commons.context.createFloatBuffer(SpringlsCommon3D
				.roundToWorkgroupPower(numLabels * mogac.slices), READ_WRITE,
				USE_BUFFER);
	}

	/**
	 * Advect.
	 * 
	 * @param timeStep
	 *            the time step
	 * @return the double
	 */

	@Override
	public double advect(double timeStep) {
		final CLKernel regionAverage = mogac.kernelMap.get("regionAverage");
		final CLKernel sumAverages = mogac.kernelMap.get("sumAverages");
		if (intensityEstimation && time % estimationInterval == 0) {
			regionAverage.putArgs(mogac.imageLabelBuffer,
					mogac.distanceFieldBuffer, pressureImageBuffer,
					mogac.labelMaskBuffer, averages, areas, stddev).rewind();
			commons.queue.put1DRangeKernel(regionAverage, 0,
					SpringlsCommon3D.roundToWorkgroupPower(commons.slices),
					SpringlsCommon3D.WORKGROUP_SIZE);
			commons.queue.finish();
			sumAverages.putArgs(averages, areas, stddev).rewind();
			commons.queue.put1DRangeKernel(sumAverages, 0, SpringlsCommon3D
					.roundToWorkgroupPower(mogac.getNumLabels()),
					SpringlsCommon3D.WORKGROUP_SIZE);
		}
		time++;
		CLBuffer<FloatBuffer> pointUpdates = commons.context.createFloatBuffer(
				commons.elements * 3, READ_WRITE, USE_BUFFER);
		CLBuffer<FloatBuffer> maxForces = commons.context.createFloatBuffer(
				(commons.arrayLength / SpringlsCommon3D.STRIDE), READ_WRITE,
				USE_BUFFER);
		final CLKernel applyForces = (commons.topologyRuleBuffer == null) ? commons.kernelMap
				.get(SpringlsCommon3D.APPLY_FORCES + "Mogac")
				: commons.kernelMap.get(SpringlsCommon3D.APPLY_FORCES_TOPO_RULE
						+ "Mogac");

		final CLKernel computeMaxForces = commons.kernelMap
				.get(SpringlsCommon3D.COMPUTE_MAX_FORCES);

		// Compute forces applied to each particle and store them in an
		// array.

		final CLKernel computeForces = commons.kernelMap
				.get("computeAdvectionForcesACWE");
		computeForces
				.putArgs(commons.capsuleBuffer, pressureImageBuffer,
						commons.springlLabelBuffer, mogac.imageLabelBuffer,
						pointUpdates, averages).putArg(pressureWeight)
				.putArg(commons.elements).rewind();

		commons.queue.put1DRangeKernel(computeForces, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		computeMaxForces.putArgs(pointUpdates, maxForces)
				.putArg(SpringlsCommon3D.STRIDE).putArg(commons.elements)
				.rewind();
		// Find the maximum force in order to choose an appropriate step
		// size.
		commons.queue.put1DRangeKernel(
				computeMaxForces,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.arrayLength
						/ SpringlsCommon3D.STRIDE),
				SpringlsCommon3D.WORKGROUP_SIZE);

		commons.queue.putReadBuffer(maxForces, true);
		FloatBuffer buff = maxForces.getBuffer();
		float maxForce = 0;
		while (buff.hasRemaining()) {
			maxForce = Math.max(maxForce, buff.get());
		}
		if (maxForce < 1E-3) {
			pointUpdates.release();
			maxForces.release();
			return 0;
		}
		// Rescale forces and update the position of all paritcles.
		double displacement = (Math.sqrt(maxForce));
		maxForce = (float) (0.5f * SpringlsConstants.scaleDown
				* SpringlsConstants.vExtent / displacement);
		applyForces
				.putArgs(commons.capsuleBuffer, mogac.distanceFieldBuffer,
						mogac.imageLabelBuffer, commons.springlLabelBuffer,
						pointUpdates).putArg(maxForce).putArg(commons.elements)
				.rewind();
		commons.queue.put1DRangeKernel(applyForces, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
		maxForces.release();
		pointUpdates.release();
		return displacement;

	}

	/**
	 * Prints the averages.
	 */
	public void printAverages() {
		commons.queue.putReadBuffer(averages, true).putReadBuffer(areas, true)
				.putReadBuffer(stddev, true);
		FloatBuffer avgs = averages.getBuffer();
		FloatBuffer area = areas.getBuffer();
		FloatBuffer std = stddev.getBuffer();
		int count = 0;
		int numLabels = mogac.getNumLabels();
		System.out.println("Printing averages:");
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
		commons.queue.putWriteBuffer(averages, true);
	}

	/**
	 * Sets the averages.
	 *
	 * @param referenceImage the new averages
	 */
	public void setAverages(ImageData referenceImage) {
		int numLabels = mogac.getNumLabels();
		double averagesArray[] = new double[numLabels];
		int areasArray[] = new int[numLabels];
		double sqrsArray[] = new double[numLabels];
		ImageDataInt imageLabels = mogac.getImageLabels();
		System.out.println("LABELS " + mogac.rows + "," + mogac.cols + " "
				+ mogac.rows);
		for (int k = 0; k < mogac.slices; k++) {
			for (int j = 0; j < mogac.cols; j++) {
				for (int i = 0; i < mogac.rows; i++) {
					double val = referenceImage.getDouble(i, j, k);
					int label = imageLabels.getInt(i, j, k);
					averagesArray[label] += val;
					areasArray[label]++;
					sqrsArray[label] += val * val;
				}
			}
		}
		FloatBuffer avgs = averages.getBuffer();
		FloatBuffer area = areas.getBuffer();
		FloatBuffer stds = stddev.getBuffer();
		int count = 0;
		while (avgs.hasRemaining()) {
			float avg = (float) (averagesArray[count] / areasArray[count]);
			float std = (float) Math.sqrt((1.0 / (areasArray[count] - 1))
					* (sqrsArray[count] - areasArray[count] * avg * avg));
			avgs.put(avg);
			area.put(areasArray[count]);
			stds.put(std);
			System.out.println(count + ": " + avg + ": " + areasArray[count]
					+ " : " + std);
			count++;
			if (count >= numLabels) {
				break;
			}
		}
		avgs.rewind();
		area.rewind();
		stds.rewind();
		commons.queue.putWriteBuffer(averages, true)
				.putWriteBuffer(areas, true).putWriteBuffer(stddev, true);
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
		this.estimationInterval = interval;
	}
}
