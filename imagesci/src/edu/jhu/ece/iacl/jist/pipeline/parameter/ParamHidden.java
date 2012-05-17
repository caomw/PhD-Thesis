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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamHiddenFactory;

// TODO: Auto-generated Javadoc
/**
 * Hidden parameter.
 * 
 * @param <T>
 *            *
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public abstract class ParamHidden<T> extends ParamModel<T> {

	/**
	 * Default constructor.
	 */
	public ParamHidden() {
		super();
		hidden = true;
		this.factory = new ParamHiddenFactory(this);
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = false;
		factory = new ParamHiddenFactory(this);
	}

	/**
	 * Always returns false.
	 * 
	 * @return true, if checks if is hidden
	 */
	@Override
	public boolean isHidden() {
		return true;
	}
}
