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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsContract removes springls pased on quality.
 */
public class SpringlsContract3D {

	/** The Constant atlasThreshold. */
	protected static final float atlasThreshold = 0.5f;

	/** The atlas bias. */
	protected CLBuffer<FloatBuffer> atlasBias = null;

	/** The commons. */
	protected SpringlsCommon3D commons;

	/**
	 * Instantiates a new springls contract.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsContract3D(SpringlsCommon3D commons) {
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
	public SpringlsContract3D(SpringlsCommon3D commons,
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
	 * @param outliersOnly the outliers only
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
				.createByteBuffer(Springl3D.BYTE_SIZE * commons.arrayLength,
						READ_WRITE, USE_BUFFER);
		final CLKernel contractCount = commons.kernelMap
				.get((outliersOnly) ? SpringlsCommon3D.CONTRACT_OUTLIERS_COUNT
						: ((atlasBias == null) ? SpringlsCommon3D.CONTRACT_COUNT
								: SpringlsCommon3D.CONTRACT_COUNT_WITH_ATLAS));
		final CLKernel contractArray = commons.kernelMap
				.get(SpringlsCommon3D.CONTRACT_ARRAY);
		if (atlasBias == null) {
			contractCount.putArgs(commons.capsuleBuffer, offsets)
					.putArg(commons.elements).rewind();
		} else {
			contractCount.putArgs(commons.capsuleBuffer, offsets, atlasBias)
					.putArg(commons.elements).putArg(atlasThreshold).rewind();
		}
		commons.queue.put1DRangeKernel(contractCount, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		int total = (commons).scan.scan(offsets, commons.elements);
		contractArray
				.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
						capsuleBuffer2, labelBuffer2, offsets)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(contractArray, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
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
