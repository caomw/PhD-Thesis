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
package org.imagesci.muscle;

import static com.jogamp.opencl.CLProgram.define;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;


import java.io.IOException;

import org.imagesci.mogac.MOGAC2D;
import org.imagesci.mogac.WEMOGAC2D;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsConstants;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLProgram;


// TODO: Auto-generated Javadoc
/**
 * The Class MuscleCommon2D.
 */
public class MuscleCommon2D extends SpringlsCommon2D {

	/**
	 * Instantiates a new muscle common2 d.
	 *
	 * @param context the context
	 * @param queue the queue
	 * @param workgroupSize the workgroup size
	 * @param type the type
	 */
	public MuscleCommon2D(CLContext context, CLCommandQueue queue,
			int workgroupSize, Type type) {
		super(context, queue, workgroupSize, type);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new muscle common2 d.
	 *
	 * @param type the type
	 */
	public MuscleCommon2D(Type type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsCommon2D#initKernels(com.jogamp.opencl.CLCommandQueue)
	 */
	@Override
	public void initKernels(CLCommandQueue queue) throws IOException {
		super.initKernels(queue);
		CLProgram program = context
				.createProgram(getClass().getResourceAsStream("Muscle2D.cl"))
				.build(define("ROWS", rows),
						define("COLS", cols),
						define("STRIDE", MOGAC2D.STRIDE),
						define("MAX_DISTANCE", WEMOGAC2D.MAX_DISTANCE),
						define("MAX_BIN_SIZE", SpringlsCommon2D.MAX_BIN_SIZE),
						define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
						define("SCALE_DOWN", SpringlsConstants.scaleDown + "f"),
						define("IMAGE_SIZE", rows * cols),
						define("LOCAL_SIZE_LIMIT", WORKGROUP_SIZE * 2),
						define("MAX_VALUE", Integer.MAX_VALUE),
						define("MAX_NEIGHBORS", SpringlsConstants.maxNeighbors),
						define("MAX_RADIUS",
								SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						define("REST_LENGTH", SpringlsConstants.restRadius
								+ "f"),
						define("SPRING_CONSTANT",
								SpringlsConstants.springConstant + "f"),
						define("PARTICLE_RADIUS",
								SpringlsConstants.particleRadius
										* SpringlsConstants.scaleDown + "f"),
						define("MAX_NEAREST_BINS",
								SpringlsConstants.maxNearestBins),
						define("MAX_VEXT", (SpringlsConstants.vExtent) + "f"),
						define("vExtent", SpringlsConstants.scaleDown
								* SpringlsConstants.vExtent + "f"),

						define("SHARPNESS", SpringlsConstants.sharpness + "f"),
						define("nearestNeighborDistance",
								(float) SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						define("WEIGHT_FUNC",
								SpringlsConstants.WEIGHT_FUNCTION[SpringlsConstants.weightingKernel
										.ordinal()]),
						define("THRESHOLD_FUNC",
								SpringlsConstants.THRESHOLD_FUNCTION[SpringlsConstants.thresholdKernel
										.ordinal()]), ENABLE_MAD);
		kernelMap.putAll(program.createCLKernels());
	}
}
