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
package edu.jhu.ece.iacl.jist.pipeline.factory;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamPointFloat;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamPointFloatTextInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * Point Float Parameter Factory.
 * 
 * @author Blake Lucas
 */
public class ParamPointFloatFactory extends ParamFactory {

	/** The param. */
	protected ParamPointFloat param;

	/**
	 * Instantiates a new param point float factory.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamPointFloatFactory(ParamPointFloat param) {
		this.param = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getInputView()
	 */
	@Override
	public ParamInputView getInputView() {
		if (inputView == null) {
			inputView = new ParamPointFloatTextInputView(param);
		}
		return inputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getOutputView()
	 */
	@Override
	public ParamOutputView getOutputView() {
		if (outputView == null) {
			outputView = new ParamOutputView(param);
		}
		return outputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getParameter()
	 */
	@Override
	public ParamPointFloat getParameter() {
		return param;
	}

}
