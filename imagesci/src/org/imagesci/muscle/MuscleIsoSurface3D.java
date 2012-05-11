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


import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.springls.SpringlsCommon3D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleIsoSurface3D.
 */
public class MuscleIsoSurface3D {
	/** The Constant DISTANCE_FIELD_EXTENT. */
	protected static final int DISTANCE_FIELD_EXTENT = 2;
	/** The STRIDE. */
	protected static int STRIDE = 256;

	/** The commons. */
	protected SpringlsCommon3D commons;

	/** The distance field buffer. */
	protected CLBuffer<FloatBuffer> distanceFieldBuffer;

	/** The image label buffer. */
	protected CLBuffer<IntBuffer> imageLabelBuffer;
	/** The MA x_ cycles. */
	protected final int MAX_CYCLES = 16;
	
	/** The mogac. */
	protected MOGAC3D mogac;

	/**
	 * Instantiates a new muscle iso surface3 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 */
	public MuscleIsoSurface3D(SpringlsCommon3D commons, MOGAC3D mogac) {
		this.mogac = mogac;
		this.commons = commons;
	}

	/**
	 * Creates the label image.
	 *
	 * @param labelMasks the label masks
	 * @return the image data int
	 */
	public ImageDataInt createLabelImage(int[] labelMasks) {
		imageLabelBuffer = commons.context.createIntBuffer(commons.rows
				* commons.cols * commons.slices, USE_BUFFER, READ_WRITE);
		distanceFieldBuffer = commons.context.createFloatBuffer(commons.rows
				* commons.cols * commons.slices, USE_BUFFER, READ_WRITE);
		final CLKernel combineLabelImages = commons.kernelMap
				.get("combineLabelImages");

		// for (int nn = labelMasks.length - 1; nn >= 0; nn--) {
		for (int nn = 0; nn < labelMasks.length; nn++) {
			int label = labelMasks[nn];
			updateUnsignedLevelSet(label);
			convertUnsignedToSigned();
			combineLabelImages.putArgs(imageLabelBuffer, distanceFieldBuffer)
					.putArg(label).rewind();
			commons.queue.put1DRangeKernel(combineLabelImages, 0, commons.rows
					* commons.cols * commons.slices,
					SpringlsCommon3D.WORKGROUP_SIZE);
		}
		commons.queue.finish();
		int[][][] imageMat;
		commons.queue.putReadBuffer(imageLabelBuffer, true);
		IntBuffer levelSet = imageLabelBuffer.getBuffer();
		imageMat = new int[commons.rows][commons.cols][commons.slices];
		for (int k = 0; k < commons.slices; k++) {
			for (int j = 0; j < commons.cols; j++) {
				for (int i = 0; i < commons.rows; i++) {
					imageMat[i][j][k] = levelSet.get();
				}
			}
		}
		levelSet.rewind();
		ImageDataInt labelImage = new ImageDataInt(imageMat);
		labelImage.setName("labels");
		imageLabelBuffer.release();
		distanceFieldBuffer.release();
		return labelImage;
	}

	/**
	 * Update unsigned level set.
	 *
	 * @param label the label
	 */
	private void updateUnsignedLevelSet(int label) {
		CLKernel reduceLevelSet = commons.kernelMap
				.get(SpringlsCommon3D.REDUCE_LEVEL_SET + "Mogac");
		reduceLevelSet
				.putArgs(commons.indexBuffer, commons.keyBuffer,
						commons.valueBuffer, commons.capsuleBuffer,
						commons.unsignedLevelSetBuffer,
						commons.springlLabelBuffer).putArg(commons.mapLength)
				.putArg(label).rewind();
		commons.queue.put1DRangeKernel(reduceLevelSet, 0, commons.rows
				* commons.cols * commons.slices,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
	}

	/**
	 * Convert unsigned to signed.
	 */
	private void convertUnsignedToSigned() {
		// Init to unlabeled
		final CLKernel initSignedLevelSet = commons.kernelMap
				.get("initSignedLevelSet");
		final CLKernel erodeLevelSet = commons.kernelMap.get("erodeLevelSet");
		final CLKernel multiplyLevelSets = commons.kernelMap
				.get("multiplyLevelSets");
		initSignedLevelSet.putArg(distanceFieldBuffer).rewind();
		commons.queue.put1DRangeKernel(initSignedLevelSet, 0, commons.rows
				* commons.cols * commons.slices,
				SpringlsCommon3D.WORKGROUP_SIZE);
		int blockSize = commons.rows / (MAX_CYCLES);
		CLBuffer<FloatBuffer> tmpSignedLevelSet = commons.context
				.createFloatBuffer(
						commons.rows * commons.cols * commons.slices,
						USE_BUFFER, READ_WRITE);
		CLBuffer<FloatBuffer> buffIn = distanceFieldBuffer;
		CLBuffer<FloatBuffer> buffOut = tmpSignedLevelSet;
		int cycles = 0;
		int lastBlockSize = commons.rows;
		while (blockSize >= 1) {
			cycles = lastBlockSize / (blockSize);
			int globalSize = commons.rows * commons.cols * commons.slices
					/ (blockSize * blockSize * blockSize);
			for (int i = 0; i < cycles; i++) {
				erodeLevelSet
						.putArgs(commons.unsignedLevelSetBuffer, buffIn,
								buffOut).putArg(blockSize).rewind();

				commons.queue.put1DRangeKernel(erodeLevelSet, 0, globalSize,
						Math.min(globalSize, SpringlsCommon3D.WORKGROUP_SIZE));
				CLBuffer<FloatBuffer> tmp = buffOut;
				buffOut = buffIn;
				buffIn = tmp;
			}
			lastBlockSize = blockSize;
			blockSize /= 2;

		}
		tmpSignedLevelSet.release();
		multiplyLevelSets.putArgs(commons.unsignedLevelSetBuffer,
				distanceFieldBuffer).rewind();
		commons.queue.put1DRangeKernel(multiplyLevelSets, 0, commons.rows
				* commons.cols * commons.slices,
				SpringlsCommon3D.WORKGROUP_SIZE);

	}
}