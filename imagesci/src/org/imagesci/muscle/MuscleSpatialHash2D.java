/**
 * ImageSci Toolkit
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

import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.springls.SpringlsSpatialHash2D;

import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;


// TODO: Auto-generated Javadoc
/**
 * The Class MuscleSpatialHash2D.
 */
public class MuscleSpatialHash2D extends SpringlsSpatialHash2D {

	/**
	 * Instantiates a new muscle spatial hash2 d.
	 *
	 * @param commons the commons
	 */
	public MuscleSpatialHash2D(SpringlsCommon2D commons) {
		super(commons);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsSpatialHash2D#updateNearestNeighbors()
	 */
	@Override
	public void updateNearestNeighbors() {
		final CLKernel mapNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon2D.MAP_NEAREST_NEIGHBORS + "Mogac");
		final CLKernel sortNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon2D.SORT_NEAREST_NEIGHBORS);
		final CLKernel reduceNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon2D.REDUCE_NEAREST_NEIGHBORS);
		commons.queue.finish();
		int listSize = SpringlsConstants.maxNearestBins * commons.elements * 2;
		if (lastArrayLength != commons.arrayLength) {
			if (commons.capsuleNeighborBuffer != null) {
				commons.capsuleNeighborBuffer.release();
			}
			commons.capsuleNeighborBuffer = commons.context.createByteBuffer(
					((2 * Integer.SIZE) / 8) * SpringlsConstants.maxNeighbors
							* commons.arrayLength * 2, READ_WRITE, USE_BUFFER);
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
						(commons).spatialLookUp, commons.capsuleBuffer,
						commons.springlLabelBuffer).putArg(commons.elements)
				.rewind();
		commons.queue.put1DRangeKernel(mapNearestNeighbors, 0,
				commons.arrayLength * 2, SpringlsCommon2D.WORKGROUP_SIZE);
		sortNearestNeighbors.putArgs(nbrLists).putArg(commons.elements * 2)
				.rewind();
		commons.queue.put1DRangeKernel(sortNearestNeighbors, 0,
				commons.arrayLength * 2, SpringlsCommon2D.WORKGROUP_SIZE);
		reduceNearestNeighbors
				.putArgs(commons.capsuleBuffer, commons.capsuleNeighborBuffer,
						nbrLists).putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(reduceNearestNeighbors, 0,
				commons.arrayLength * 2, SpringlsCommon2D.WORKGROUP_SIZE);
		commons.queue.finish();
	}
}
