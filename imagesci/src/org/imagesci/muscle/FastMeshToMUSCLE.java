/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
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

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.imagesci.mogac.MuscleEvolveLevelSet3D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsSurface;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MeshToMUSCLE.
 */
public class FastMeshToMUSCLE extends AbstractCalculation {

	/** The Constant DISTANCE_FIELD_EXTENT. */
	protected static final int DISTANCE_FIELD_EXTENT = 2;

	/** The commons. */
	protected MuscleCommon3D commons;

	/** The dist field. */
	ImageDataFloat distField;

	/** The label image. */
	ImageDataInt labelImage;

	/** The MA x_ cycles. */
	protected final int MAX_CYCLES = 16;

	/**
	 * Instantiates a new mesh to muscle.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public FastMeshToMUSCLE(int rows, int cols, int slices) throws IOException {
		super();
		this.commons = new MuscleCommon3D(CLDevice.Type.CPU);
		commons.initialize(rows, cols, slices, true);
		commons.signedLevelSetBuffer = commons.context.createFloatBuffer(rows
				* cols * slices, USE_BUFFER, READ_WRITE);

	}

	/**
	 * Solve.
	 * 
	 * @param initialSurface
	 *            the initial surface
	 * @param order
	 *            the order
	 * @param smoothWeight
	 *            the smooth weight
	 * @param smoothingIters
	 *            the smoothing iters
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void solve(EmbeddedSurface initialSurface, int[] order,
			float smoothWeight, int smoothingIters) throws IOException {
		setTotalUnits(order.length);
		commons.setSpringls(new SpringlsSurface(initialSurface));
		long startTime = System.nanoTime();
		int numLabels = 0;
		for (int i = 0; i < order.length; i++) {
			numLabels = Math.max(order[i], numLabels);
		}
		int padLabelSize = ((numLabels % 32 == 0) ? numLabels
				: 32 * ((numLabels / 32) + 1));

		System.out
				.println("Labels " + numLabels + " padded to " + padLabelSize);
		CLBuffer<IntBuffer> orderBuffer = commons.context.createIntBuffer(
				order.length, USE_BUFFER, READ_WRITE);
		orderBuffer.getBuffer().put(order).rewind();
		commons.queue.putWriteBuffer(orderBuffer, true);

		CLBuffer<IntBuffer> bitMaskBuffer = commons.context
				.createIntBuffer(SpringlsCommon3D.roundToWorkgroupPower(
						commons.rows * commons.cols * commons.slices
								* padLabelSize, 32) / 32, USE_BUFFER,
						READ_WRITE);

		CLKernel buildBoundaryLabels = commons.kernelMap
				.get("buildBoundaryLabels");
		buildBoundaryLabels
				.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
						bitMaskBuffer).putArg(padLabelSize)
				.putArg(commons.elements).rewind();

		commons.queue.put1DRangeKernel(buildBoundaryLabels, 0,
				commons.arrayLength, SpringlsCommon3D.WORKGROUP_SIZE);
		// Init to unlabeled
		final CLKernel erodeLevelSet = commons.kernelMap.get("erodeBitMask");

		CLBuffer<IntBuffer> tmp1BitMaskBuffer = commons.context
				.createIntBuffer(SpringlsCommon3D.roundToWorkgroupPower(
						commons.rows * commons.cols * commons.slices
								* padLabelSize, 32) / 32, USE_BUFFER,
						READ_WRITE);
		CLBuffer<IntBuffer> tmp2BitMaskBuffer = commons.context
				.createIntBuffer(SpringlsCommon3D.roundToWorkgroupPower(
						commons.rows * commons.cols * commons.slices
								* padLabelSize, 32) / 32, USE_BUFFER,
						READ_WRITE);

		int blockSize = Math.max(Math.max(commons.rows, commons.cols),
				commons.slices) / (MAX_CYCLES);
		CLBuffer<IntBuffer> buffIn = tmp1BitMaskBuffer;
		CLBuffer<IntBuffer> buffOut = tmp2BitMaskBuffer;
		int cycles = 0;
		int lastBlockSize = Math.max(Math.max(commons.rows, commons.cols),
				commons.slices);

		while (blockSize >= 1) {
			cycles = lastBlockSize / (blockSize);
			int globalSize = commons.rows * commons.cols * commons.slices
					/ (blockSize * blockSize * blockSize);
			for (int i = 0; i < 2 * cycles; i++) {
				erodeLevelSet.putArgs(bitMaskBuffer, buffIn, buffOut)
						.putArg(padLabelSize >> 5).putArg(blockSize).rewind();

				commons.queue.put1DRangeKernel(erodeLevelSet, 0,
						SpringlsCommon3D.roundToWorkgroupPower(globalSize),
						Math.min(globalSize, SpringlsCommon3D.WORKGROUP_SIZE));
				CLBuffer<IntBuffer> tmp = buffOut;
				buffOut = buffIn;
				buffIn = tmp;
			}

			lastBlockSize = blockSize;
			blockSize /= 2;

		}
		final CLKernel combineLabelImages = commons.kernelMap
				.get("combineBitLabelImages");
		CLBuffer<IntBuffer> labelImageBuffer = commons.context.createIntBuffer(
				commons.rows * commons.cols * commons.slices, USE_BUFFER,
				READ_WRITE);
		combineLabelImages.putArgs(labelImageBuffer, buffOut, orderBuffer)
				.putArg(order.length).putArg(numLabels).putArg(padLabelSize)
				.rewind();
		commons.queue.put1DRangeKernel(
				combineLabelImages,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
		tmp1BitMaskBuffer.release();
		tmp2BitMaskBuffer.release();
		bitMaskBuffer.release();
		orderBuffer.release();
		CLKernel buildDistanceField = commons.kernelMap
				.get("buildDistanceField");
		CLKernel initDistanceField = commons.kernelMap.get("initDistanceField");
		initDistanceField.putArgs(commons.unsignedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(
				initDistanceField,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
		buildDistanceField
				.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
						commons.unsignedLevelSetBuffer).putArg(-1)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(buildDistanceField, 0,
				commons.arrayLength, SpringlsCommon3D.WORKGROUP_SIZE);

		distField = new ImageDataFloat(commons.rows, commons.cols,
				commons.slices);
		distField.setName(initialSurface.getName() + "_distfield");

		labelImage = new ImageDataInt(commons.rows, commons.cols,
				commons.slices);
		labelImage.setName(initialSurface.getName() + "_labels");

		commons.queue.putReadBuffer(labelImageBuffer, true).putReadBuffer(
				commons.unsignedLevelSetBuffer, true);
		IntBuffer labelsBuffer = labelImageBuffer.getBuffer();
		FloatBuffer distFieldBuffer = commons.unsignedLevelSetBuffer
				.getBuffer();
		for (int k = 0; k < commons.slices; k++) {
			for (int j = 0; j < commons.cols; j++) {
				for (int i = 0; i < commons.rows; i++) {
					labelImage.set(i, j, k, labelsBuffer.get());
					distField.set(i, j, k, distFieldBuffer.get());
				}
			}
		}
		labelsBuffer.rewind();
		distFieldBuffer.rewind();

		if (smoothingIters > 0) {
			System.out.println("Smoothing ...");
			MuscleEvolveLevelSet3D mogac = new MuscleEvolveLevelSet3D(commons,
					labelImage, smoothWeight);
			mogac.setPreserveTopology(false);
			mogac.setMaxIterations(smoothingIters);
			mogac.init(distField, labelImage, false);
			mogac.evolve();
			distField = mogac.getDistanceField();
			labelImage = mogac.getImageLabels();
			labelImage.setName(initialSurface.getName() + "_labels");
			distField.setName(initialSurface.getName() + "_distfield");
		}

		long stopTime = System.nanoTime();
		System.out.println("Elapsed Time: " + ((stopTime - startTime) * 1E-9)
				+ " sec");
		commons.dispose();
		markCompleted();
	}

	/**
	 * Gets the distance field.
	 * 
	 * @return the distance field
	 */
	public ImageDataFloat getDistanceField() {
		// TODO Auto-generated method stub
		return distField;
	}

	/**
	 * Gets the label image.
	 * 
	 * @return the label image
	 */
	public ImageDataInt getLabelImage() {
		// TODO Auto-generated method stub
		return labelImage;
	}
}
