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
package edu.jhu.ece.iacl.jist.pipeline.factory;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamURIInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * File Parameter Factory.
 * 
 * @author Blake Lucas
 */
public class ParamFileFactory extends ParamFactory {

	/** The param. */
	private ParamFile param;

	/**
	 * Instantiates a new param file factory.
	 */
	public ParamFileFactory() {
	}

	/**
	 * Construct factory for specified parameter.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamFileFactory(ParamFile param) {
		this.param = param;
	}

	/**
	 * Get parameter input view.
	 * 
	 * @return input view
	 */
	@Override
	public ParamInputView getInputView() {
		if (inputView == null) {
			inputView = new ParamURIInputView(param);
		}
		return inputView;
	}

	/**
	 * Get parameter output view.
	 * 
	 * @return output view
	 */
	@Override
	public ParamOutputView getOutputView() {
		if (outputView == null) {
			outputView = new ParamOutputView(param);
		}
		return outputView;
	}

	/**
	 * Get factory's parameter.
	 * 
	 * @return file parameter
	 */
	@Override
	public ParamFile getParameter() {
		return param;
	}

}
