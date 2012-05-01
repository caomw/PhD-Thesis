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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

// TODO: Auto-generated Javadoc
/**
 * Invalid Parameter Exception is thrown when a parameter value does not
 * validate.
 * 
 * @author Blake Lucas
 */
public class InvalidParameterException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The extra message. */
	private String extraMessage = "";

	/** The param. */
	private Object param;

	/**
	 * Default constructor.
	 * 
	 * @param param
	 *            invalid parameter
	 */
	public InvalidParameterException(Object param) {
		this.param = param;
	}

	/**
	 * Defautl constructor.
	 * 
	 * @param param
	 *            invalid parameter
	 * @param extraMessage
	 *            additional message
	 */
	public InvalidParameterException(Object param, String extraMessage) {
		this.param = param;
		this.extraMessage = extraMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "<HTML>: Value " + param.toString() + " is invalid.<BR>"
				+ extraMessage + "</HTML>";
	}

}
