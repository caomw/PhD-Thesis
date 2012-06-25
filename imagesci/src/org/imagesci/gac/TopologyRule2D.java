/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.gac;

// TODO: Auto-generated Javadoc
/**
 * The Class TopologyRule2D.
 */
public abstract class TopologyRule2D {

	/**
	 * The topology rule.
	 */
	public enum Rule {

		/** The CONNEC t_4. */
		CONNECT_4,
		/** The CONNEC t_8. */
		CONNECT_8
	};

	/** The columns. */
	protected int cols;

	/** The rows. */
	protected int rows;

	/** The rule. */
	protected Rule rule;

	/**
	 * Instantiates a new topology rule2 d.
	 * 
	 * @param rule
	 *            the rule
	 */
	public TopologyRule2D(Rule rule) {
		this.rule = rule;
	}

}
