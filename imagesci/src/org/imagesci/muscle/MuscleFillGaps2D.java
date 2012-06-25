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
package org.imagesci.muscle;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.imagesci.mogac.WEMOGAC2D;
import org.imagesci.springls.Springl2D;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsFillGaps2D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;


// TODO: Auto-generated Javadoc
/**
 * The Class MuscleFillGaps2D.
 */
public class MuscleFillGaps2D extends SpringlsFillGaps2D {
	
	/** The mogac. */
	protected WEMOGAC2D mogac;

	/**
	 * Instantiates a new muscle fill gaps2 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 */
	public MuscleFillGaps2D(SpringlsCommon2D commons, WEMOGAC2D mogac) {
		super(commons);
		this.mogac = mogac;
	}

	/**
	 * Fill gaps.
	 * 
	 * @return the int
	 */

	@Override
	public int fillGaps() {
		int oldElements = commons.elements;
		for (int nn = 0; nn < mogac.getNumObjects(); nn++) {
			int label = mogac.getLabelMasks()[nn + 1];
			updateUnsignedLevelSet(label);
			fillGaps(label);
		}
		return commons.elements - oldElements;
	}

	/**
	 * Update unsigned level set.
	 *
	 * @param label the label
	 */
	private void updateUnsignedLevelSet(int label) {
		// Has unsigned level set been cleared before this?
		CLKernel reduceLevelSet = commons.kernelMap
				.get(SpringlsCommon2D.REDUCE_LEVEL_SET + "Mogac");
		reduceLevelSet
				.putArgs((commons).activeListBuffer, (commons).spatialLookUp,
						commons.capsuleBuffer, commons.unsignedLevelSetBuffer,
						commons.springlLabelBuffer)
				.putArg((commons).activeListSize).putArg(label).rewind();
		commons.queue
				.put1DRangeKernel(reduceLevelSet, 0, SpringlsCommon2D
						.roundToWorgroupPower((commons).activeListSize),
						SpringlsCommon2D.WORKGROUP_SIZE);
		commons.queue.finish();
	}

	/**
	 * Fill gaps.
	 *
	 * @param label the label
	 * @return the int
	 */
	protected int fillGaps(int label) {
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

		CLKernel fillGapCount = commons.kernelMap
				.get(SpringlsCommon2D.FILL_GAP_COUNT + "Mogac");
		CLKernel expandGaps = commons.kernelMap
				.get(SpringlsCommon2D.EXPAND_GAPS + "Mogac");
		CLKernel copyElements = commons.kernelMap
				.get(SpringlsCommon2D.COPY_ELEMENTS);
		fillGapCount
				.putArgs(mogac.distanceFieldBuffer, mogac.imageLabelBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets).putArg(1).putArg(activeListSize).putArg(label)
				.rewind();

		commons.queue.put1DRangeKernel(fillGapCount, 0,
				SpringlsCommon2D.roundToWorgroupPower(activeListSize),
				SpringlsCommon2D.WORKGROUP_SIZE);
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
						mogac.distanceFieldBuffer, mogac.imageLabelBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets).putArg(1).putArg(oldElements)
				.putArg(activeListSize).putArg(label).rewind();
		commons.queue.put1DRangeKernel(expandGaps, 0,
				SpringlsCommon2D.roundToWorgroupPower(activeListSize),
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

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsFillGaps2D#fillLabels()
	 */
	@Override
	public void fillLabels() {
		CLKernel fixLabels = commons.kernelMap.get(SpringlsCommon2D.FIX_LABELS
				+ "Mogac");
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
