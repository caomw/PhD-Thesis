/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
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
