/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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
 * The Class Vec64.
 */
class Vec64 extends Vec {

	/**
	 * Instantiates a new vec64.
	 * 
	 * @param buf
	 *            the buf
	 */
	Vec64(java.nio.ByteBuffer buf) {
		super(buf);
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public static int size() {
		return 12;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Vec#getX()
	 */
	@Override
	public float getX() {
		return accessor.getFloatAt(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Vec#getY()
	 */
	@Override
	public float getY() {
		return accessor.getFloatAt(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Vec#getZ()
	 */
	@Override
	public float getZ() {
		return accessor.getFloatAt(2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Vec#setX(float)
	 */
	@Override
	public Vec setX(float val) {
		accessor.setFloatAt(0, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Vec#setY(float)
	 */
	@Override
	public Vec setY(float val) {
		accessor.setFloatAt(1, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.Vec#setZ(float)
	 */
	@Override
	public Vec setZ(float val) {
		accessor.setFloatAt(2, val);
		return this;
	}
}
