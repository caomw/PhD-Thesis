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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsExpand will split triangles that are too large.
 */
public class SpringlsExpand2D {

	/** The commons. */
	protected SpringlsCommon2D commons;

	/**
	 * Instantiates a new springls expand.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsExpand2D(SpringlsCommon2D commons) {
		this.commons = commons;
	}

	/**
	 * Expand.
	 * 
	 * @return the number of added elements.
	 */
	public int expand() {
		if (commons.elements == 0) {
			return 0;
		}
		CLBuffer<IntBuffer> offsets = commons.context.createIntBuffer(
				commons.elements, READ_WRITE, USE_BUFFER);

		final CLKernel expandCount = commons.kernelMap
				.get(SpringlsCommon2D.EXPAND_COUNT);
		final CLKernel expandArray = commons.kernelMap
				.get(SpringlsCommon2D.EXPAND_ARRAY);
		expandCount.putArgs(commons.capsuleBuffer, offsets)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(expandCount, 0, commons.arrayLength,
				SpringlsCommon2D.WORKGROUP_SIZE);
		int total = commons.scan.scan(offsets, commons.elements);
		int oldElements = commons.elements;
		commons.elements = total;
		int oldArrayLength = commons.arrayLength;
		if (commons.elements > oldArrayLength) {
			commons.arrayLength = commons.roundArrayLength(commons.elements);
		}

		CLBuffer<IntBuffer> labelBuffer2 = commons.context.createIntBuffer(
				commons.arrayLength, READ_WRITE, USE_BUFFER);

		CLBuffer<ByteBuffer> capsuleBuffer2 = commons.context.createByteBuffer(
				Springl2D.BYTE_SIZE * commons.arrayLength, READ_WRITE,
				USE_BUFFER);

		expandArray
				.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
						capsuleBuffer2, labelBuffer2, offsets)
				.putArg(oldElements).rewind();
		commons.queue.put1DRangeKernel(expandArray, 0, commons.arrayLength,
				SpringlsCommon2D.WORKGROUP_SIZE);

		commons.queue.finish();
		commons.springlLabelBuffer.release();
		commons.springlLabelBuffer = labelBuffer2;
		commons.capsuleBuffer.release();
		commons.capsuleBuffer = capsuleBuffer2;
		offsets.release();

		return commons.elements - oldElements;
	}

}
