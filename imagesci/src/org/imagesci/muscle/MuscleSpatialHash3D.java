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
package org.imagesci.muscle;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.springls.SpringlsSpatialHash3D;

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
