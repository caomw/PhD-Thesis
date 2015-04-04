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
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import javax.swing.JComponent;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamHidden;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * A hidden input field. Nothing will be displayed.
 * 
 * @author Blake Lucas
 */
public class ParamHiddenInputView extends ParamInputView {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/**
	 * Default constructor.
	 * 
	 * @param param
	 *            parameter
	 */
	public ParamHiddenInputView(ParamModel param) {
		super(param);
	}

	/**
	 * Unimplemented.
	 */
	@Override
	public void commit() {
		// TODO Auto-generated method stub
	}

	/**
	 * Get field used to enter this value.
	 *
	 * @return the field
	 */
	@Override
	public JComponent getField() {
		return null;
	}

	/**
	 * Get parameter.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamHidden getParameter() {
		return (ParamHidden) param;
	}

	/**
	 * Unimplemented.
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub
	}
}
