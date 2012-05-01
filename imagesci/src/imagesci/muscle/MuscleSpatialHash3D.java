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
package imagesci.muscle;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import imagesci.springls.SpringlsCommon3D;
import imagesci.springls.SpringlsConstants;
import imagesci.springls.SpringlsSpatialHash3D;

import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;


// TODO: Auto-generated Javadoc
/**
 * The Class MuscleSpatialHash3D.
 */
public class MuscleSpatialHash3D extends SpringlsSpatialHash3D {

	/**
	 * Instantiates a new muscle spatial hash3 d.
	 *
	 * @param commons the commons
	 */
	public MuscleSpatialHash3D(SpringlsCommon3D commons) {
		super(commons);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsSpatialHash3D#updateNearestNeighbors()
	 */
	@Override
	public void updateNearestNeighbors() {
		CLKernel mapNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon3D.MAP_NEAREST_NEIGHBORS + "Mogac");
		CLKernel sortNearestNeighbors = commons.kernelMap
				.get(SpringlsCommon3D.SORT_NEAREST_NEIGHBORS);
		CLKernel reduceNearestNeighbors = commons.kernelMap
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
					CLMemory.Mem.USE_BUFFER);
			lastListSize = listSize;
		}
		mapNearestNeighbors
				.putArgs(nbrLists, commons.indexBuffer,
						(commons).spatialLookUp, commons.capsuleBuffer,
						commons.springlLabelBuffer).putArg(commons.elements)
				.rewind();
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
}
