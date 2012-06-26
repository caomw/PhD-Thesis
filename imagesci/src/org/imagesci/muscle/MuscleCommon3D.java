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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Point3f;

import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.springls.SpringlsEvolveLevelSet3D;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLProgram;

import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.MUSCLERenderer3D;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * The Class MuscleCommon3D.
 */
public class MuscleCommon3D extends SpringlsCommon3D {

	/** The active contour. */
	protected MuscleActiveContour3D activeContour;

	/**
	 * Instantiates a new muscle common3 d.
	 *
	 * @param context the context
	 * @param queue the queue
	 * @param workgroupSize the workgroup size
	 * @param type the type
	 */
	public MuscleCommon3D(CLContext context, CLCommandQueue queue,
			int workgroupSize, Type type) {
		super(context, queue, workgroupSize, type);
	}

	/**
	 * Instantiates a new muscle common3 d.
	 *
	 * @param type the type
	 */
	public MuscleCommon3D(Type type) {
		super(type);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsCommon3D#createSpringlsRayCastRenderer(edu.jhu.cs.cisst.vent.VisualizationProcessing3D, int, int, int)
	 */
	@Override
	public RendererProcessing3D createSpringlsRayCastRenderer(
			VisualizationProcessing3D applet, int rasterWidth,
			int rasterHeight, int refreshRate) {
		return new MUSCLERenderer3D(applet, activeContour, rasterWidth,
				rasterHeight, refreshRate);
	}

	/**
	 * Gets the springls surface.
	 * 
	 * @return the springls surface
	 */
	@Override
	public EmbeddedSurface getSpringlsSurface() {
		queue.putReadBuffer(capsuleBuffer, true).putReadBuffer(
				springlLabelBuffer, true);
		queue.putReadBuffer(springlLabelBuffer, true);
		IntBuffer lbuffer = springlLabelBuffer.getBuffer();
		ByteBuffer buffer = capsuleBuffer.getBuffer();
		Point3f[] points = new Point3f[3 * elements];
		int[] indices = new int[3 * elements];
		double[][] data = new double[elements][7];
		int index = 0;
		float scale = SpringlsConstants.scaleUp;
		for (int n = 0; n < elements; n++) {
			float px = buffer.getFloat();
			float py = buffer.getFloat();
			float pz = buffer.getFloat();
			buffer.getFloat();
			float[] a = new float[3];
			for (int i = 0; i < 3; i++) {
				indices[index] = index;
				points[index++] = new Point3f(scale * buffer.getFloat(), scale
						* buffer.getFloat(), scale * buffer.getFloat());
				a[i] = buffer.getFloat();
			}
			int l = Math.abs(lbuffer.get());
			data[n] = new double[] { scale * px, scale * py, scale * pz,
					scale * a[0], scale * a[1], scale * a[2], l };
		}
		lbuffer.rewind();
		EmbeddedSurface surf = new EmbeddedSurface(points, indices);
		surf.setCellData(data);
		surf.setName("springls");
		buffer.rewind();
		return surf;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsCommon3D#initKernels(com.jogamp.opencl.CLCommandQueue)
	 */
	@Override
	public void initKernels(CLCommandQueue queue) throws IOException {
		super.initKernels(queue);
		CLProgram program = context
				.createProgram(
						MuscleCommon3D.class.getResourceAsStream("Muscle3D.cl"))
				.build(define("ROWS", rows),
						define("COLS", cols),
						define("SLICES", slices),
						define("STRIDE", STRIDE),
						define("MAX_BIN_SIZE", MAX_BIN_SIZE),
						define("MAX_DISTANCE",
								SpringlsEvolveLevelSet3D.MAX_DISTANCE + "f"),
						define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
						define("SCALE_DOWN", SpringlsConstants.scaleDown + "f"),
						define("IMAGE_SIZE", rows * cols * slices),
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

	/**
	 * Sets the active contour.
	 *
	 * @param activeContour the new active contour
	 */
	public void setActiveContour(MuscleActiveContour3D activeContour) {
		this.activeContour = activeContour;
	}
}
