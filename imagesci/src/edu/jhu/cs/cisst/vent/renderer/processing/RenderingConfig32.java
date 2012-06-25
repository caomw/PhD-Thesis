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

// TODO: Auto-generated Javadoc
/**
 * The Class RenderingConfig32.
 */
class RenderingConfig32 extends RenderingConfig {

	/**
	 * Instantiates a new rendering config32.
	 * 
	 * @param buf
	 *            the buf
	 */
	RenderingConfig32(java.nio.ByteBuffer buf) {
		super(buf);
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public static int size() {
		return 140;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#
	 * getActvateFastRendering()
	 */
	@Override
	public int getActvateFastRendering() {
		return accessor.getIntAt(3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getBackgroundColor
	 * ()
	 */
	@Override
	public float[] getBackgroundColor() {
		return accessor.getFloatsAt(8, new float[4]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getCamera()
	 */
	@Override
	public Camera getCamera() {
		return Camera.create(accessor.slice(64, 76));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getEnableShadow
	 * ()
	 */
	@Override
	public int getEnableShadow() {
		return accessor.getIntAt(4);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getEpsilon()
	 */
	@Override
	public float getEpsilon() {
		return accessor.getFloatAt(6);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getHeight()
	 */
	@Override
	public int getHeight() {
		return accessor.getIntAt(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getLight()
	 */
	@Override
	public float[] getLight() {
		return accessor.getFloatsAt(12, new float[3]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getMaxIterations
	 * ()
	 */
	@Override
	public int getMaxIterations() {
		return accessor.getIntAt(5);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#
	 * getSuperSamplingSize()
	 */
	@Override
	public int getSuperSamplingSize() {
		return accessor.getIntAt(2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#getWidth()
	 */
	@Override
	public int getWidth() {
		return accessor.getIntAt(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#
	 * setActvateFastRendering(int)
	 */
	@Override
	public RenderingConfig setActvateFastRendering(int val) {
		accessor.setIntAt(3, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#setBackgroundColor
	 * (float[])
	 */
	@Override
	public RenderingConfig setBackgroundColor(float[] val) {
		accessor.setFloatsAt(8, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#setEnableShadow
	 * (int)
	 */
	@Override
	public RenderingConfig setEnableShadow(int val) {
		accessor.setIntAt(4, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#setEpsilon(
	 * float)
	 */
	@Override
	public RenderingConfig setEpsilon(float val) {
		accessor.setFloatAt(6, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#setHeight(int)
	 */
	@Override
	public RenderingConfig setHeight(int val) {
		accessor.setIntAt(1, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#setLight(float
	 * [])
	 */
	@Override
	public RenderingConfig setLight(float[] val) {
		accessor.setFloatsAt(12, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#setMaxIterations
	 * (int)
	 */
	@Override
	public RenderingConfig setMaxIterations(int val) {
		accessor.setIntAt(5, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#
	 * setSuperSamplingSize(int)
	 */
	@Override
	public RenderingConfig setSuperSamplingSize(int val) {
		accessor.setIntAt(2, val);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig#setWidth(int)
	 */
	@Override
	public RenderingConfig setWidth(int val) {
		accessor.setIntAt(0, val);
		return this;
	}
}
