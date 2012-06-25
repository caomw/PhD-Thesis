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
 * The Class SpringlsFillGaps will fill gaps that form between springls.
 */
public class SpringlsFillGaps2D {

	/** The STRIDE. */
	protected static int STRIDE = 256;

	/** The commons. */
	protected SpringlsCommon2D commons;

	/** The last active list array size. */
	protected int lastActiveListArraySize = -1;

	/** The offsets. */
	protected CLBuffer<IntBuffer> offsets = null;

	/**
	 * Instantiates a new springls fill gaps.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsFillGaps2D(SpringlsCommon2D commons) {
		this.commons = commons;

	}

	/**
	 * Fill gaps.
	 * 
	 * @return the int
	 */
	public int fillGaps() {
		int activeListSize = (commons).activeListSize;
		int activeListArraySize = (commons).activeListArraySize;
		CLBuffer<IntBuffer> activeListBuffer = (commons).activeListBuffer;

		if (lastActiveListArraySize != activeListArraySize) {
			if (offsets != null) {
				offsets.release();
			}
			offsets = commons.context.createIntBuffer(activeListArraySize,
					READ_WRITE, USE_BUFFER);
			lastActiveListArraySize = activeListArraySize;
		}

		final CLKernel fillGapCount = commons.kernelMap
				.get(SpringlsCommon2D.FILL_GAP_COUNT);
		final CLKernel expandGaps = commons.kernelMap
				.get(SpringlsCommon2D.EXPAND_GAPS);
		final CLKernel copyElements = commons.kernelMap
				.get(SpringlsCommon2D.COPY_ELEMENTS);
		fillGapCount
				.putArgs(commons.signedLevelSetBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets).putArg(1).putArg(activeListSize).rewind();

		commons.queue.put1DRangeKernel(fillGapCount, 0,
				SpringlsCommon2D.roundToWorkgroupPower(activeListSize),
				SpringlsCommon2D.WORKGROUP_SIZE);
		commons.queue.finish();
		int oldElements = commons.elements;
		int sum = commons.scan.scan(offsets, activeListSize);
		commons.elements += sum;
		int oldArrayLength = commons.arrayLength;
		// Allow for 20% extra space in array to estimate maximum amount of hole
		// filling
		if (commons.elements >= commons.arrayLength) {
			commons.arrayLength = commons.roundArrayLength(commons.elements);
		}
		CLBuffer<IntBuffer> labelBuffer2;
		CLBuffer<ByteBuffer> capsuleBuffer2;
		if (commons.arrayLength != oldArrayLength) {
			labelBuffer2 = commons.context.createIntBuffer(commons.arrayLength,
					READ_WRITE, USE_BUFFER);

			capsuleBuffer2 = commons.context.createByteBuffer(
					Springl2D.BYTE_SIZE * commons.arrayLength, READ_WRITE,
					USE_BUFFER);
			copyElements
					.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
							capsuleBuffer2, labelBuffer2).putArg(oldElements)
					.rewind();
			commons.queue.put1DRangeKernel(copyElements, 0, oldArrayLength,
					SpringlsCommon2D.WORKGROUP_SIZE);
		} else {
			labelBuffer2 = commons.springlLabelBuffer;
			capsuleBuffer2 = commons.capsuleBuffer;
		}
		commons.queue.finish();
		expandGaps
				.putArgs(capsuleBuffer2, labelBuffer2,
						commons.signedLevelSetBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets).putArg(1).putArg(oldElements)
				.putArg(activeListSize).rewind();
		commons.queue.put1DRangeKernel(expandGaps, 0,
				SpringlsCommon2D.roundToWorkgroupPower(activeListSize),
				SpringlsCommon2D.WORKGROUP_SIZE);
		commons.queue.finish();
		if (commons.springlLabelBuffer != labelBuffer2) {
			commons.springlLabelBuffer.release();
		}
		commons.springlLabelBuffer = labelBuffer2;
		if (commons.capsuleBuffer != capsuleBuffer2) {
			commons.capsuleBuffer.release();
		}
		commons.capsuleBuffer = capsuleBuffer2;
		commons.queue.finish();
		return commons.elements - oldElements;
	}

	/**
	 * Fill labels.
	 */
	public void fillLabels() {
		CLKernel fixLabels = commons.kernelMap.get(SpringlsCommon2D.FIX_LABELS);
		fixLabels
				.putArgs(commons.capsuleBuffer, commons.capsuleNeighborBuffer,
						commons.originalUnsignedLevelSetBuffer,
						commons.springlLabelBuffer).putArg(commons.elements)
				.rewind();
		commons.queue.put1DRangeKernel(fixLabels, 0, commons.arrayLength,
				SpringlsCommon2D.WORKGROUP_SIZE);
		commons.queue.finish();
	}
}
