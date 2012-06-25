/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */

package edu.jhu.cs.cisst.vent.renderer.processing;

import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.StructAccessor;

// TODO: Auto-generated Javadoc

/**
 * The Class Vec.
 */
public abstract class Vec {

	/** The accessor. */
	StructAccessor accessor;

	/**
	 * Instantiates a new vec.
	 * 
	 * @param buf
	 *            the buf
	 */
	Vec(java.nio.ByteBuffer buf) {
		accessor = new StructAccessor(buf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + getX() + "," + getY() + "," + getZ() + ")";
	}

	/**
	 * Gets the x.
	 * 
	 * @return the x
	 */
	public abstract float getX();

	/**
	 * Gets the y.
	 * 
	 * @return the y
	 */
	public abstract float getY();

	/**
	 * Gets the z.
	 * 
	 * @return the z
	 */
	public abstract float getZ();

	/**
	 * Creates the.
	 * 
	 * @return the vec
	 */
	public static Vec create() {
		return create(Buffers.newDirectByteBuffer(size()));
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public static int size() {
		// if (CPU.is32Bit()) {
		// return Vec32.size();
		// } else {
		return Vec64.size();
		// }
	}

	/**
	 * Creates the.
	 * 
	 * @param buf
	 *            the buf
	 * @return the vec
	 */
	public static Vec create(java.nio.ByteBuffer buf) {
		// if (CPU.is32Bit()) {
		// return new Vec32(buf);
		// } else {
		return new Vec64(buf);
		// }
	}

	/**
	 * Gets the buffer.
	 * 
	 * @return the buffer
	 */
	public java.nio.ByteBuffer getBuffer() {
		return accessor.getBuffer();
	}

	/**
	 * Sets the x.
	 * 
	 * @param val
	 *            the val
	 * @return the vec
	 */
	public abstract Vec setX(float val);

	/**
	 * Sets the y.
	 * 
	 * @param val
	 *            the val
	 * @return the vec
	 */
	public abstract Vec setY(float val);

	/**
	 * Sets the z.
	 * 
	 * @param val
	 *            the val
	 * @return the vec
	 */
	public abstract Vec setZ(float val);
}
