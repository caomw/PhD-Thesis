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
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsFillGaps will fill gaps that form between springls.
 */
public class SpringlsFillGaps3D {
	/** The STRIDE. */
	protected static int STRIDE = 256;
	/** The commons. */
	protected SpringlsCommon3D commons;

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
	public SpringlsFillGaps3D(SpringlsCommon3D commons) {
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
				.get(SpringlsCommon3D.FILL_GAP_COUNT);
		final CLKernel expandGaps = commons.kernelMap
				.get(SpringlsCommon3D.EXPAND_GAPS);
		final CLKernel copyElements = commons.kernelMap
				.get(SpringlsCommon3D.COPY_ELEMENTS);
		fillGapCount
				.putArgs(commons.signedLevelSetBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets, commons.aiCubeEdgeFlagsBuffer,
						commons.a2iTriangleConnectionTableBuffer)
				.putArg((commons.flip) ? -1 : 1).putArg(activeListSize)
				.rewind();

		commons.queue.put1DRangeKernel(fillGapCount, 0,
				SpringlsCommon3D.roundToWorkgroupPower(activeListSize),
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();

		int oldElements = commons.elements;
		int sum = (commons).scan.scan(offsets, activeListSize);
		commons.elements += sum;
		int oldArrayLength = commons.arrayLength;

		if (commons.elements >= commons.arrayLength) {
			commons.arrayLength = commons.roundArrayLength(commons.elements);
		}
		CLBuffer<IntBuffer> labelBuffer2;
		CLBuffer<ByteBuffer> capsuleBuffer2;
		if (commons.arrayLength != oldArrayLength) {
			labelBuffer2 = commons.context.createIntBuffer(commons.arrayLength,
					READ_WRITE, USE_BUFFER);

			capsuleBuffer2 = commons.context.createByteBuffer(
					Springl3D.BYTE_SIZE * commons.arrayLength, READ_WRITE,
					USE_BUFFER);
			copyElements
					.putArgs(commons.capsuleBuffer, commons.springlLabelBuffer,
							capsuleBuffer2, labelBuffer2).putArg(oldElements)
					.rewind();
			commons.queue.put1DRangeKernel(copyElements, 0, oldArrayLength,
					SpringlsCommon3D.WORKGROUP_SIZE);
		} else {
			labelBuffer2 = commons.springlLabelBuffer;
			capsuleBuffer2 = commons.capsuleBuffer;
		}
		commons.queue.finish();
		expandGaps
				.putArgs(capsuleBuffer2, labelBuffer2,
						commons.signedLevelSetBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets, commons.aiCubeEdgeFlagsBuffer,
						commons.a2iTriangleConnectionTableBuffer)
				.putArg((commons.flip) ? -1 : 1).putArg(oldElements)
				.putArg(activeListSize).rewind();
		commons.queue.put1DRangeKernel(expandGaps, 0,
				SpringlsCommon3D.roundToWorkgroupPower(activeListSize),
				SpringlsCommon3D.WORKGROUP_SIZE);
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
		CLKernel fixLabels = commons.kernelMap.get(SpringlsCommon3D.FIX_LABELS);
		fixLabels
				.putArgs(commons.capsuleBuffer, commons.capsuleNeighborBuffer,
						commons.originalUnsignedLevelSetBuffer,
						commons.springlLabelBuffer).putArg(commons.elements)
				.rewind();
		commons.queue.put1DRangeKernel(fixLabels, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
	}
}
