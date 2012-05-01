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

import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.StructAccessor;

// TODO: Auto-generated Javadoc
/**
 * The Class RenderingConfig.
 */
public abstract class RenderingConfig {

	/** The accessor. */
	StructAccessor accessor;

	/**
	 * Instantiates a new rendering config.
	 * 
	 * @param buf
	 *            the buf
	 */
	RenderingConfig(java.nio.ByteBuffer buf) {
		accessor = new StructAccessor(buf);
	}

	/**
	 * Creates the.
	 * 
	 * @return the rendering config
	 */
	public static RenderingConfig create() {
		return create(Buffers.newDirectByteBuffer(size()));
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public static int size() {
		// if (CPU.is32Bit()) {
		// return RenderingConfig32.size();
		// } else {
		return RenderingConfig64.size();
		// }
	}

	/**
	 * Creates the.
	 * 
	 * @param buf
	 *            the buf
	 * @return the rendering config
	 */
	public static RenderingConfig create(java.nio.ByteBuffer buf) {
		// if (CPU.is32Bit()) {
		// return new RenderingConfig32(buf);
		// } else {
		return new RenderingConfig64(buf);
		// }
	}

	/**
	 * Gets the actvate fast rendering.
	 * 
	 * @return the actvate fast rendering
	 */
	public abstract int getActvateFastRendering();

	/**
	 * Gets the background color.
	 * 
	 * @return the background color
	 */
	public abstract float[] getBackgroundColor();

	/**
	 * Gets the buffer.
	 * 
	 * @return the buffer
	 */
	public java.nio.ByteBuffer getBuffer() {
		return accessor.getBuffer();
	}

	/**
	 * Gets the camera.
	 * 
	 * @return the camera
	 */
	public abstract Camera getCamera();

	/**
	 * Gets the enable shadow.
	 * 
	 * @return the enable shadow
	 */
	public abstract int getEnableShadow();

	/**
	 * Gets the epsilon.
	 * 
	 * @return the epsilon
	 */
	public abstract float getEpsilon();

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public abstract int getHeight();

	/**
	 * Gets the light.
	 * 
	 * @return the light
	 */
	public abstract float[] getLight();

	/**
	 * Gets the max iterations.
	 * 
	 * @return the max iterations
	 */
	public abstract int getMaxIterations();

	/**
	 * Gets the super sampling size.
	 * 
	 * @return the super sampling size
	 */
	public abstract int getSuperSamplingSize();

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public abstract int getWidth();

	/**
	 * Sets the actvate fast rendering.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setActvateFastRendering(int val);

	/**
	 * Sets the background color.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setBackgroundColor(float[] val);

	/**
	 * Sets the enable shadow.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setEnableShadow(int val);

	/**
	 * Sets the epsilon.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setEpsilon(float val);

	/**
	 * Sets the height.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setHeight(int val);

	/**
	 * Sets the light.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setLight(float[] val);

	/**
	 * Sets the max iterations.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setMaxIterations(int val);

	/**
	 * Sets the super sampling size.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setSuperSamplingSize(int val);

	/**
	 * Sets the width.
	 * 
	 * @param val
	 *            the val
	 * @return the rendering config
	 */
	public abstract RenderingConfig setWidth(int val);
}
