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
package edu.jhu.ece.iacl.jist.pipeline;

// TODO: Auto-generated Javadoc
/**
 * Algorithm Runtime Exception for error that occurs when running an algorithm.
 * 
 * @author Blake Lucas
 */
public class AlgorithmRuntimeException extends Exception {

	/** Error message to display. */
	private String message = "";

	/**
	 * Default constructor.
	 * 
	 * @param message
	 *            the message
	 */
	public AlgorithmRuntimeException(String message) {
		this.message = message;
	}

	/**
	 * Get error message.
	 * 
	 * @return the message
	 */
	@Override
	public String getMessage() {
		return message;
	}
}
