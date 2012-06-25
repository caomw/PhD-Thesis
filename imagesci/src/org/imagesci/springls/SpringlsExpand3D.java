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
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsExpand will split triangles that are too large.
 */
public class SpringlsExpand3D {

	/** The commons. */
	protected SpringlsCommon3D commons;

	/**
	 * Instantiates a new springls expand.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsExpand3D(SpringlsCommon3D commons) {
		this.commons = commons;
	}

	/**
	 * Expand.
	 *
	 * @return the int
	 */
	public int expand() {
		if (commons.elements == 0) {
			return 0;
		}
		CLBuffer<IntBuffer> offsets = commons.context.createIntBuffer(
				commons.elements, READ_WRITE, USE_BUFFER);

		CLKernel expandCount = commons.kernelMap
				.get(SpringlsCommon3D.EXPAND_COUNT);
		CLKernel expandArray = commons.kernelMap
				.get(SpringlsCommon3D.EXPAND_ARRAY);
		expandCount.putArgs(commons.capsuleBuffer, offsets)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(expandCount, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		int total = (commons).scan.scan(offsets, commons.elements);
		int oldElements = commons.elements;
		commons.elements = total;
		int oldArrayLength = commons.arrayLength;
		if (commons.elements > oldArrayLength) {
			commons.arrayLength = commons.roundArrayLength(commons.elements);
		}

		CLBuffer<IntBuffer> labelBuffer2 = commons.context.createIntBuffer(
				commons.arrayLength, READ_WRITE, USE_BUFFER);

		CLBuffer<ByteBuffer> capsuleBuffer2 = commons.context.createByteBuffer(
				Springl3D.BYTE_SIZE * commons.arrayLength, READ_WRITE,
				USE_BUFFER);
		expandArray
				.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
						capsuleBuffer2, labelBuffer2, offsets)
				.putArg(oldElements).rewind();
		commons.queue.put1DRangeKernel(expandArray, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);

		commons.queue.finish();
		commons.springlLabelBuffer.release();
		commons.springlLabelBuffer = labelBuffer2;
		commons.capsuleBuffer.release();
		commons.capsuleBuffer = capsuleBuffer2;
		offsets.release();

		return commons.elements - oldElements;
	}

}
