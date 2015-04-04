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
package edu.jhu.ece.iacl.jist.pipeline.factory;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumberCollection;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamNumberCollectionInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamNumberCollectionOutputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * Number Collection Parameter Factory.
 * 
 * @author Blake Lucas
 */
public class ParamNumberCollectionFactory extends ParamFactory {

	/** The param. */
	private ParamNumberCollection param;

	/**
	 * Instantiates a new param number collection factory.
	 */
	public ParamNumberCollectionFactory() {
	}

	/**
	 * Instantiates a new param number collection factory.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamNumberCollectionFactory(ParamNumberCollection param) {
		this.param = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#equals(java.lang.
	 * Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof ParamFactory) {
			return this.equals(((ParamFactory) obj).getParameter());
		} else if (obj instanceof ParamNumberCollection) {
			return this.getParameter().getValue()
					.equals(((ParamNumberCollection) obj).getValue());
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getInputView()
	 */
	@Override
	public ParamInputView getInputView() {
		if (inputView == null) {
			inputView = new ParamNumberCollectionInputView(param);
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
			outputView = new ParamNumberCollectionOutputView(param);
		}
		return outputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getParameter()
	 */
	@Override
	public ParamNumberCollection getParameter() {
		return param;
	}

}
