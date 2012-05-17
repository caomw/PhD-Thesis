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

// TODO: Auto-generated Javadoc
/**
 * Invalid Parameter Value Exception is thrown when the setValue() method is
 * used for an invalid data type.
 * 
 * @author Blake Lucas
 */
public class InvalidParameterValueException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7837548622346555554L;

	/**
	 * Instantiates a new invalid parameter value exception.
	 * 
	 * @param model
	 *            the model
	 * @param o
	 *            the o
	 */
	public InvalidParameterValueException(ParamModel model, Object o) {
		super("Parameter " + model.getName() + " cannot accept type "
				+ ((o != null) ? o.getClass() : "NULL") + " with value "
				+ ((o != null) ? o.toString() : "NULL"));
	}
}
