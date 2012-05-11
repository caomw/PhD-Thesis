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

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;


import java.io.IOException;
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
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MeshToMUSCLE.
 */
public class MeshToMUSCLE extends AbstractCalculation {

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
	public MeshToMUSCLE(int rows, int cols, int slices) throws IOException {
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
		CLBuffer<IntBuffer> labelImageBuffer = commons.context.createIntBuffer(
				commons.rows * commons.cols * commons.slices, USE_BUFFER,
				READ_WRITE);

		final CLKernel combineLabelImages = commons.kernelMap
				.get("combineLabelImages");
		long startTime = System.nanoTime();
		for (int i = 0; i < order.length; i++) {
			setLabel("Object " + order[i]);
			System.out.println("Converting object " + (i + 1) + "/"
					+ order.length + " to level set");
			createUnsignedLevelSet(commons, order[i]);
			CLBuffer<FloatBuffer> tmpSignedLevelSet = commons.context
					.createFloatBuffer(commons.rows * commons.cols
							* commons.slices, USE_BUFFER, READ_WRITE);
			convertUnsignedToSigned(commons, tmpSignedLevelSet, false);
			commons.queue.finish();
			tmpSignedLevelSet.release();
			combineLabelImages
					.putArgs(labelImageBuffer, commons.signedLevelSetBuffer)
					.putArg(order[i]).rewind();
			commons.queue.put1DRangeKernel(
					combineLabelImages,
					0,
					SpringlsCommon3D.roundToWorkgroupPower(commons.rows
							* commons.cols * commons.slices),
					SpringlsCommon3D.WORKGROUP_SIZE);
			commons.queue.finish();
			incrementCompletedUnits();
			System.gc();
		}
		createUnsignedLevelSet(commons, -1);
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
		long endTime = System.nanoTime();
		System.out.println("Elapsed Time: " + 1E-9f * (endTime - startTime)
				+ " sec");
		commons.dispose();
		markCompleted();
	}

	/**
	 * Creates the unsigned level set.
	 * 
	 * @param commons
	 *            the commons
	 * @param label
	 *            the label
	 */
	private void createUnsignedLevelSet(MuscleCommon3D commons, int label) {
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
						commons.unsignedLevelSetBuffer).putArg(label)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(buildDistanceField, 0,
				commons.arrayLength, SpringlsCommon3D.WORKGROUP_SIZE);

	}

	/**
	 * Convert unsigned to signed.
	 * 
	 * @param commons
	 *            the commons
	 * @param tmpSignedLevelSet
	 *            the tmp signed level set
	 * @param extend
	 *            the extend
	 */
	private void convertUnsignedToSigned(MuscleCommon3D commons,
			CLBuffer<FloatBuffer> tmpSignedLevelSet, boolean extend) {
		CLKernel extendDistanceField = commons.kernelMap
				.get(SpringlsCommon3D.EXTEND_DISTANCE_FIELD);

		// Init to unlabeled
		final CLKernel initSignedLevelSet = commons.kernelMap
				.get("initSignedLevelSet");
		final CLKernel erodeLevelSet = commons.kernelMap.get("erodeLevelSet");
		final CLKernel multiplyLevelSets = commons.kernelMap
				.get("multiplyLevelSets");
		initSignedLevelSet.putArg(commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(
				initSignedLevelSet,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
		int blockSize = Math.max(Math.max(commons.rows, commons.cols),
				commons.slices) / (MAX_CYCLES);
		CLBuffer<FloatBuffer> buffIn = commons.signedLevelSetBuffer;
		CLBuffer<FloatBuffer> buffOut = tmpSignedLevelSet;
		int cycles = 0;
		int lastBlockSize = Math.max(Math.max(commons.rows, commons.cols),
				commons.slices);

		while (blockSize >= 1) {
			cycles = lastBlockSize / (blockSize);
			int globalSize = commons.rows * commons.cols * commons.slices
					/ (blockSize * blockSize * blockSize);
			for (int i = 0; i < 2 * cycles; i++) {
				erodeLevelSet
						.putArgs(commons.unsignedLevelSetBuffer, buffIn,
								buffOut).putArg(blockSize).rewind();

				commons.queue.put1DRangeKernel(erodeLevelSet, 0,
						SpringlsCommon3D.roundToWorkgroupPower(globalSize),
						Math.min(globalSize, SpringlsCommon3D.WORKGROUP_SIZE));
				CLBuffer<FloatBuffer> tmp = buffOut;
				buffOut = buffIn;
				buffIn = tmp;
			}
			lastBlockSize = blockSize;
			blockSize /= 2;

		}
		// End
		multiplyLevelSets.putArgs(commons.unsignedLevelSetBuffer,
				commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(
				multiplyLevelSets,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
		if (extend) {
			for (int i = DISTANCE_FIELD_EXTENT - 1; i < 4 * DISTANCE_FIELD_EXTENT; i++) {
				extendDistanceField.putArgs(commons.unsignedLevelSetBuffer)
						.putArg(i).rewind();
				commons.queue.put1DRangeKernel(extendDistanceField, 0,
						commons.rows * commons.cols * commons.slices,
						SpringlsCommon3D.WORKGROUP_SIZE);
			}
		}
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
