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
package org.imagesci.muscle;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.imagesci.mogac.WEMOGAC3D;
import org.imagesci.springls.Springl3D;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsFillGaps3D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;


// TODO: Auto-generated Javadoc
/**
 * The Class MuscleFillGaps3D.
 */
public class MuscleFillGaps3D extends SpringlsFillGaps3D {
	
	/** The last active list array size. */
	protected int lastActiveListArraySize = -1;

	/** The mogac. */
	protected WEMOGAC3D mogac;
	
	/** The offsets. */
	protected CLBuffer<IntBuffer> offsets = null;

	/**
	 * Instantiates a new muscle fill gaps3 d.
	 *
	 * @param commons the commons
	 * @param mogac the mogac
	 */
	public MuscleFillGaps3D(SpringlsCommon3D commons, WEMOGAC3D mogac) {
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
	protected void updateUnsignedLevelSet(int label) {

		CLKernel reduceLevelSet = commons.kernelMap
				.get(SpringlsCommon3D.REDUCE_LEVEL_SET + "Mogac");
		reduceLevelSet
				.putArgs(((MuscleCommon3D) commons).activeListBuffer,
						((MuscleCommon3D) commons).spatialLookUp,
						commons.capsuleBuffer, commons.unsignedLevelSetBuffer,
						commons.springlLabelBuffer)
				.putArg(((MuscleCommon3D) commons).activeListSize)
				.putArg(label).rewind();
		commons.queue
				.put1DRangeKernel(
						reduceLevelSet,
						0,
						SpringlsCommon2D
								.roundToWorgroupPower(((MuscleCommon3D) commons).activeListSize),
						SpringlsCommon3D.WORKGROUP_SIZE);
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
				.get(SpringlsCommon3D.FILL_GAP_COUNT + "Mogac");
		CLKernel expandGaps = commons.kernelMap
				.get(SpringlsCommon3D.EXPAND_GAPS + "Mogac");
		CLKernel copyElements = commons.kernelMap
				.get(SpringlsCommon3D.COPY_ELEMENTS);
		fillGapCount
				.putArgs(mogac.distanceFieldBuffer, mogac.imageLabelBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets, commons.aiCubeEdgeFlagsBuffer,
						commons.a2iTriangleConnectionTableBuffer)
				.putArg((commons.flip) ? -1 : 1).putArg(activeListSize)
				.putArg(label).rewind();

		commons.queue.put1DRangeKernel(fillGapCount, 0,
				SpringlsCommon2D.roundToWorgroupPower(activeListSize),
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
						mogac.distanceFieldBuffer, mogac.imageLabelBuffer,
						commons.unsignedLevelSetBuffer, activeListBuffer,
						offsets, commons.aiCubeEdgeFlagsBuffer,
						commons.a2iTriangleConnectionTableBuffer)
				.putArg((commons.flip) ? -1 : 1).putArg(oldElements)
				.putArg(activeListSize).putArg(label).rewind();
		commons.queue.put1DRangeKernel(expandGaps, 0,
				SpringlsCommon2D.roundToWorgroupPower(activeListSize),
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
	@Override
	public void fillLabels() {
		CLKernel fixLabels = commons.kernelMap.get(SpringlsCommon3D.FIX_LABELS
				+ "Mogac");
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
