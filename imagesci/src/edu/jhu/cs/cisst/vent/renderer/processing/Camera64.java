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

package edu.jhu.cs.cisst.vent.renderer.processing;

// TODO: Auto-generated Javadoc
/**
 * The Class Camera64.
 */
class Camera64 extends Camera {

	/** The dir. */
	private final Vec dir;

	/** The orig. */
	private final Vec orig;

	/** The target. */
	private final Vec target;

	/** The x. */
	private final Vec x;

	/** The y. */
	private final Vec y;

	/**
	 * Instantiates a new camera64.
	 * 
	 * @param buf
	 *            the buf
	 */
	Camera64(java.nio.ByteBuffer buf) {
		super(buf);
		orig = Vec.create(accessor.slice(0, 12));
		target = Vec.create(accessor.slice(12, 12));
		dir = Vec.create(accessor.slice(24, 12));
		x = Vec.create(accessor.slice(36, 12));
		y = Vec.create(accessor.slice(48, 12));
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public static int size() {
		return 60;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getDir()
	 */
	@Override
	public Vec getDir() {
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getOrig()
	 */
	@Override
	public Vec getOrig() {
		return orig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getTarget()
	 */
	@Override
	public Vec getTarget() {
		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getX()
	 */
	@Override
	public Vec getX() {
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getY()
	 */
	@Override
	public Vec getY() {
		return y;
	}
}
