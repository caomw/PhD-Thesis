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

import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.StructAccessor;

// TODO: Auto-generated Javadoc
/**
 * The Class Camera.
 */
public abstract class Camera {

	/** The accessor. */
	StructAccessor accessor;

	/**
	 * Instantiates a new camera.
	 * 
	 * @param buf
	 *            the buf
	 */
	Camera(java.nio.ByteBuffer buf) {
		accessor = new StructAccessor(buf);
	}

	/**
	 * Creates the.
	 * 
	 * @return the camera
	 */
	public static Camera create() {
		return create(Buffers.newDirectByteBuffer(size()));
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public static int size() {
		// if (CPU.is32Bit()) {
		// return Camera32.size();
		// } else {
		return Camera64.size();
		// }
	}

	/**
	 * Creates the.
	 * 
	 * @param buf
	 *            the buf
	 * @return the camera
	 */
	public static Camera create(java.nio.ByteBuffer buf) {
		// if (CPU.is32Bit()) {
		// return new Camera32(buf);
		// } else {
		return new Camera64(buf);
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
	 * Gets the dir.
	 * 
	 * @return the dir
	 */
	public abstract Vec getDir();

	/**
	 * Gets the orig.
	 * 
	 * @return the orig
	 */
	public abstract Vec getOrig();

	/**
	 * Gets the target.
	 * 
	 * @return the target
	 */
	public abstract Vec getTarget();

	/**
	 * Gets the x.
	 * 
	 * @return the x
	 */
	public abstract Vec getX();

	/**
	 * Gets the y.
	 * 
	 * @return the y
	 */
	public abstract Vec getY();
}
