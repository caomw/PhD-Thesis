/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
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

import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsNeighborHash creates a spatial look-up table for locating
 * springls.
 */
public class SpringlsSpatialHash3D {
	/** The Constant DISTANCE_FIELD_EXTENT. */
	protected static final int DISTANCE_FIELD_EXTENT = 4;

	/** The commons. */
	protected SpringlsCommon3D commons;

	/** The last active list array length. */
	protected int lastActiveListArrayLength = -1;

	/** The last array count. */
	protected int lastArrayLength = 0;

	/** The last list size. */
	protected int lastListSize = -1;

	/** The nbr lists. */
	protected CLBuffer<IntBuffer> nbrLists = null;

	/**
	 * Instantiates a new springls spatial hash.
	 * 
	 * @param commons
	 *            the commons
	 */
	public SpringlsSpatialHash3D(SpringlsCommon3D commons) {
		this.commons = commons;
	}

	/**
	 * Update nearest neighbors.
	 */
	public void updateNearestNeighbors() {
		final CLKernel mapNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon3D.MAP_NEAREST_NEIGHBORS);
		final CLKernel sortNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon3D.SORT_NEAREST_NEIGHBORS);
		final CLKernel reduceNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon3D.REDUCE_NEAREST_NEIGHBORS);
		commons.queue.finish();
		int listSize = SpringlsConstants.maxNearestBins * commons.elements * 3;
		if (lastArrayLength != commons.arrayLength) {
			if (commons.capsuleNeighborBuffer != null) {
				commons.capsuleNeighborBuffer.release();
			}
			commons.capsuleNeighborBuffer = commons.context.createByteBuffer(
					((2 * Integer.SIZE) / 8) * SpringlsConstants.maxNeighbors
							* commons.arrayLength * 3, READ_WRITE, USE_BUFFER);
			lastArrayLength = commons.arrayLength;
		}
		if (nbrLists == null || lastListSize < listSize) {
			if (nbrLists != null) {
				nbrLists.release();
			}
			nbrLists = commons.context.createIntBuffer(listSize, READ_WRITE,
					CLMemory.Mem.COPY_BUFFER);
			lastListSize = listSize;
		}
		mapNearestNeighbors
				.putArgs(nbrLists, commons.indexBuffer,
						(commons).spatialLookUp, commons.capsuleBuffer)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(mapNearestNeighbors, 0,
				commons.arrayLength * 3, SpringlsCommon3D.WORKGROUP_SIZE);
		sortNearestNeighbors.putArgs(nbrLists).putArg(commons.elements * 3)
				.rewind();
		commons.queue.put1DRangeKernel(sortNearestNeighbors, 0,
				commons.arrayLength * 3, SpringlsCommon3D.WORKGROUP_SIZE);
		reduceNearestNeighbors
				.putArgs(commons.capsuleBuffer, commons.capsuleNeighborBuffer,
						nbrLists).putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(reduceNearestNeighbors, 0,
				commons.arrayLength * 3, SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
	}

	/**
	 * Update spatial hash.
	 */
	public void updateSpatialHash() {
		int activeListArrayLength = (commons).activeListArraySize;
		if (lastActiveListArrayLength != activeListArrayLength) {
			commons.mapLength = activeListArrayLength
					* SpringlsCommon3D.MAX_BIN_SIZE;
			lastActiveListArrayLength = activeListArrayLength;
			if ((commons).spatialLookUp != null) {
				(commons).spatialLookUp.release();
			}
			(commons).spatialLookUp = commons.context.createIntBuffer(
					commons.mapLength, READ_WRITE, USE_BUFFER);
		}

		final CLKernel buildLUT = commons.kernelMap.get("buildLUT");
		final CLKernel updateIndexMap = commons.kernelMap.get("updateIndexMap");
		final CLKernel initLUT = commons.kernelMap.get("initLUT");
		int global_size = SpringlsCommon3D
				.roundToWorkgroupPower((commons).activeListSize);
		initLUT.putArg((commons).spatialLookUp)
				.putArg((commons).activeListSize).rewind();
		commons.queue.put1DRangeKernel(initLUT, 0, global_size,
				SpringlsCommon3D.WORKGROUP_SIZE);
		updateIndexMap.putArgs(commons.indexBuffer, (commons).activeListBuffer)
				.putArg((commons).activeListSize).rewind();
		commons.queue.put1DRangeKernel(updateIndexMap, 0, global_size,
				SpringlsCommon3D.WORKGROUP_SIZE);
		buildLUT.putArgs(commons.capsuleBuffer, commons.indexBuffer,
				(commons).spatialLookUp).putArg(commons.elements)
				.putArg((commons).activeListSize).rewind();
		commons.queue.put1DRangeKernel(buildLUT, 0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.elements),
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
		commons.setActiveSetValid(true);
	}

	/**
	 * Update unsigned level set.
	 */
	public void updateUnsignedLevelSet() {
		final CLKernel reduceLevelSet = commons.kernelMap
				.get(SpringlsCommon3D.REDUCE_LEVEL_SET);

		reduceLevelSet
				.putArgs((commons).activeListBuffer, (commons).spatialLookUp,
						commons.capsuleBuffer, commons.unsignedLevelSetBuffer)
				.putArg((commons).activeListSize).rewind();
		commons.queue.put1DRangeKernel(reduceLevelSet, 0, SpringlsCommon3D
				.roundToWorkgroupPower((commons).activeListSize),
				SpringlsCommon3D.WORKGROUP_SIZE);
		commons.queue.finish();
	}

}
