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

import static com.jogamp.opencl.CLProgram.define;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;

import imagesci.mogac.MOGAC2D;
import imagesci.mogac.WEMOGAC2D;
import imagesci.springls.SpringlsCommon2D;
import imagesci.springls.SpringlsConstants;

import java.io.IOException;

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
