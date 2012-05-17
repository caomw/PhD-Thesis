/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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
package org.imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsContract removes springls pased on quality.
 */
public class SpringlsContract2D {

	/** The Constant atlasThreshold. */
	protected static final float atlasThreshold = 0.5f;

	/** The atlas bias. */
	protected CLBuffer<FloatBuffer> atlasBias = null;

	/** The commons. */
	protected SpringlsCommon2D commons;

	/**
	 * Instantiates a new springls contract.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsContract2D(SpringlsCommon2D commons) {
		this.commons = commons;
	}

	/**
	 * Instantiates a new springls contract.
	 * 
	 * @param commons
	 *            the commons
	 * @param atlasBias
	 *            the atlas bias
	 */
	public SpringlsContract2D(SpringlsCommon2D commons,
			CLBuffer<FloatBuffer> atlasBias) {
		this.commons = commons;
		this.atlasBias = atlasBias;
	}

	/**
	 * Contract.
	 * 
	 * @return the int
	 */
	public int contract() {
		return contract(false);
	}

	/**
	 * Contract.
	 * 
	 * @param outliersOnly
	 *            the outliers only
	 * @return the int
	 */
	public int contract(boolean outliersOnly) {
		if (commons.elements == 0) {
			return 0;
		}
		final CLBuffer<IntBuffer> offsets = commons.context.createIntBuffer(
				commons.elements, READ_WRITE, USE_BUFFER);
		final CLBuffer<IntBuffer> labelBuffer2 = commons.context
				.createIntBuffer(commons.arrayLength, READ_WRITE, USE_BUFFER);
		final CLBuffer<ByteBuffer> capsuleBuffer2 = commons.context
				.createByteBuffer(Springl2D.BYTE_SIZE * commons.arrayLength,
						READ_WRITE, USE_BUFFER);
		final CLKernel contractCount = commons.kernelMap
				.get((outliersOnly) ? SpringlsCommon2D.CONTRACT_OUTLIERS_COUNT
						: ((atlasBias == null) ? SpringlsCommon2D.CONTRACT_COUNT
								: SpringlsCommon2D.CONTRACT_COUNT_WITH_ATLAS));
		final CLKernel contractArray = commons.kernelMap
				.get(SpringlsCommon2D.CONTRACT_ARRAY);
		if (atlasBias == null) {
			contractCount.putArgs(commons.capsuleBuffer, offsets)
					.putArg(commons.elements).rewind();
		} else {
			contractCount.putArgs(commons.capsuleBuffer, offsets, atlasBias)
					.putArg(commons.elements).putArg(atlasThreshold).rewind();
		}
		commons.queue.put1DRangeKernel(contractCount, 0, commons.arrayLength,
				SpringlsCommon2D.WORKGROUP_SIZE);
		int total = commons.scan.scan(offsets, commons.elements);
		contractArray
				.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
						capsuleBuffer2, labelBuffer2, offsets)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(contractArray, 0, commons.arrayLength,
				SpringlsCommon2D.WORKGROUP_SIZE);
		int oldElements = commons.elements;
		commons.elements = total;
		commons.springlLabelBuffer.release();
		commons.springlLabelBuffer = labelBuffer2;
		commons.capsuleBuffer.release();
		commons.capsuleBuffer = capsuleBuffer2;
		offsets.release();
		commons.queue.finish();
		return oldElements - commons.elements;
	}

	/**
	 * Sets the atlas bias.
	 * 
	 * @param atlasBias
	 *            the new atlas bias
	 */
	public void setAtlasBias(CLBuffer<FloatBuffer> atlasBias) {
		this.atlasBias = atlasBias;
	}
}
