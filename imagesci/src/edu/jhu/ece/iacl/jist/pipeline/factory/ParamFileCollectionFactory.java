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

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFileCollection;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamFileCollectionInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamFileCollectionOutputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * File Collection Parameter Factory.
 * 
 * @author Blake Lucas
 */
public class ParamFileCollectionFactory extends ParamFactory {

	/** The param. */
	private ParamFileCollection param;

	/**
	 * Instantiates a new param file collection factory.
	 */
	public ParamFileCollectionFactory() {
	}

	/**
	 * Instantiates a new param file collection factory.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamFileCollectionFactory(ParamFileCollection param) {
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
		} else if (obj instanceof ParamFileCollection) {
			return this.getParameter().getValue()
					.equals(((ParamFileCollection) obj).getValue());
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
			inputView = new ParamFileCollectionInputView(param);
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
			outputView = new ParamFileCollectionOutputView(param);
		}
		return outputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getParameter()
	 */
	@Override
	public ParamFileCollection getParameter() {
		return param;
	}

}
