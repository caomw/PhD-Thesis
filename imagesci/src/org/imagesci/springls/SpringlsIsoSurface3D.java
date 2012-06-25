/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
package org.imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsIsoSurface will convert a level set to iso-surface or vice
 * versa.
 */
public class SpringlsIsoSurface3D {
	/**
	 * The Class IndexedVoxel.
	 */
	protected static class IndexedVoxel implements Comparable<IndexedVoxel> {

		/** The k. */
		public int i, j, k;

		/** The value. */
		float val;

		/**
		 * Instantiates a new indexed voxel.
		 * 
		 * @param i
		 *            the i
		 * @param j
		 *            the j
		 * @param k
		 *            the k
		 * @param val
		 *            the value
		 */
		public IndexedVoxel(int i, int j, int k, float val) {
			this.val = val;
			this.i = i;
			this.j = j;
			this.k = k;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(IndexedVoxel voxel) {
			return (int) Math.signum(voxel.val - this.val);
		}
	}

	/** The Constant DISTANCE_FIELD_EXTENT. */
	protected static final int DISTANCE_FIELD_EXTENT = 2;

	/** The STRIDE. */
	protected static int STRIDE = 256;

	/** The commons. */
	protected SpringlsCommon3D commons;

	/** The image index. */
	protected int imageIndex = 0;

	/** The MA x_ cycles. */
	protected final int MAX_CYCLES = 16;

	/**
	 * Instantiates a new springls iso surface.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsIsoSurface3D(SpringlsCommon3D commons) {
		this.commons = commons;
	}

	/**
	 * Convert unsigned to signed.
	 */
	public void convertUnsignedToSigned() {
		// Init to unlabeled
		final CLKernel initSignedLevelSet = commons.kernelMap
				.get("initSignedLevelSet");
		final CLKernel erodeLevelSet = commons.kernelMap.get("erodeLevelSet");
		final CLKernel multiplyLevelSets = commons.kernelMap
				.get("multiplyLevelSets");
		initSignedLevelSet.putArg(commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(initSignedLevelSet, 0, commons.rows
				* commons.cols * commons.slices,
				SpringlsCommon3D.WORKGROUP_SIZE);
		int blockSize = commons.rows / (MAX_CYCLES);
		CLBuffer<FloatBuffer> tmpSignedLevelSet = commons.context
				.createFloatBuffer(
						commons.rows * commons.cols * commons.slices,
						USE_BUFFER, READ_WRITE);
		CLBuffer<FloatBuffer> buffIn = commons.signedLevelSetBuffer;
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
				commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(multiplyLevelSets, 0, commons.rows
				* commons.cols * commons.slices,
				SpringlsCommon3D.WORKGROUP_SIZE);

	}

}
