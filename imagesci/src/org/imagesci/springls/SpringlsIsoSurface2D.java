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
public class SpringlsIsoSurface2D {

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
	protected SpringlsCommon2D commons;

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
	public SpringlsIsoSurface2D(SpringlsCommon2D commons) {
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
				* commons.cols, SpringlsCommon2D.WORKGROUP_SIZE);
		int blockSize = commons.rows / (MAX_CYCLES);
		CLBuffer<FloatBuffer> tmpSignedLevelSet = commons.context
				.createFloatBuffer(commons.rows * commons.cols, USE_BUFFER,
						READ_WRITE);
		CLBuffer<FloatBuffer> buffIn = commons.signedLevelSetBuffer;
		CLBuffer<FloatBuffer> buffOut = tmpSignedLevelSet;
		int cycles = 0;
		int lastBlockSize = commons.rows;
		while (blockSize >= 1) {
			cycles = lastBlockSize / (blockSize);
			int globalSize = commons.rows * commons.cols
					/ (blockSize * blockSize * blockSize);
			for (int i = 0; i < cycles; i++) {
				erodeLevelSet
						.putArgs(commons.unsignedLevelSetBuffer, buffIn,
								buffOut).putArg(blockSize).rewind();

				commons.queue.put1DRangeKernel(erodeLevelSet, 0, globalSize,
						Math.min(globalSize, SpringlsCommon2D.WORKGROUP_SIZE));
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
				* commons.cols, SpringlsCommon2D.WORKGROUP_SIZE);

	}

	/**
	 * Update iso surface.
	 */
	public void updateIsoSurface() {
		// TODO Auto-generated method stub

	}

}
