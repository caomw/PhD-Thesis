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
 * The Class Camera32.
 */
class Camera32 extends Camera {

	/**
	 * Instantiates a new camera32.
	 * 
	 * @param buf
	 *            the buf
	 */
	Camera32(java.nio.ByteBuffer buf) {
		super(buf);
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public static int size() {
		return 76;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getDir()
	 */
	@Override
	public Vec getDir() {
		return Vec.create(accessor.slice(32, 12));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getOrig()
	 */
	@Override
	public Vec getOrig() {
		return Vec.create(accessor.slice(0, 12));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getTarget()
	 */
	@Override
	public Vec getTarget() {
		return Vec.create(accessor.slice(16, 12));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getX()
	 */
	@Override
	public Vec getX() {
		return Vec.create(accessor.slice(48, 12));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Camera#getY()
	 */
	@Override
	public Vec getY() {
		return Vec.create(accessor.slice(64, 12));
	}
}
