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

package org.imagesci.springls;

import org.imagesci.springls.ActiveContour3D.Task;

import com.jogamp.opencl.CLKernel;


// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsAdvectTest advects particles with analytical test
 * functions.
 */
public class SpringlsAdvectEnright3D extends SpringlsAdvect3D {

	/** The active contour. */
	protected ActiveContour3D activeContour;
	/** The commons. */
	protected SpringlsCommon3D commons;
	
	/** The enright period. */
	protected int enrightPeriod = 600;
	
	/** The task. */
	protected ActiveContour3D.Task task;

	/**
	 * Instantiates a new springls advect test.
	 *
	 * @param commons the commons
	 * @param activeContour the active contour
	 * @param task the task
	 * @param enrightPeriod the enright period
	 */
	public SpringlsAdvectEnright3D(SpringlsCommon3D commons,
			ActiveContour3D activeContour, ActiveContour3D.Task task,
			int enrightPeriod) {
		super(commons);
		this.commons = commons;
		this.enrightPeriod = enrightPeriod;
		this.task = task;
		this.activeContour = activeContour;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsAdvect3D#advect(double)
	 */
	@Override
	public double advect(double timeStep) {
		if (task == ActiveContour3D.Task.ZALESAK) {
			return advectZalesak(timeStep);
		} else if (task == Task.ENRIGHT) {
			return advectEnright(activeContour.getAdvectionWeight(),
					activeContour.getTime(), enrightPeriod);
		} else {
			return 0;
		}
	}

	/**
	 * Advect zalesak.
	 * 
	 * @param timeStep
	 *            the time step
	 * @return the double
	 */
	public double advectZalesak(double timeStep) {
		CLKernel advect = commons.kernelMap
				.get(SpringlsCommon3D.ADVECT_ZALESAK);
		advect.putArgs(commons.capsuleBuffer).putArg((float) timeStep)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(advect, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		return timeStep;
	}

	/**
	 * Advect enright.
	 *
	 * @param timeStep the time step
	 * @param time the time
	 * @param period the period
	 * @return the double
	 */
	public double advectEnright(double timeStep, double time, double period) {
		CLKernel advect = commons.kernelMap
				.get(SpringlsCommon3D.ADVECT_ENRIGHT);
		advect.putArgs(commons.capsuleBuffer).putArg((float) timeStep)
				.putArg((float) time).putArg((float) period)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(advect, 0, commons.arrayLength,
				SpringlsCommon3D.WORKGROUP_SIZE);
		return timeStep;
	}
}
