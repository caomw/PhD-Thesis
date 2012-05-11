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
package org.imagesci.gac;

// TODO: Auto-generated Javadoc
/**
 * The Class TopologyRule3D.
 */
public abstract class TopologyRule3D {

	/**
	 * The topology rule.
	 */
	public enum Rule {

		/** The CONNEC t_18_6. */
		CONNECT_18_6, /** The CONNEC t_26_6. */
		CONNECT_26_6, /** The CONNEC t_6_18. */
		CONNECT_6_18, /** The CONNEC t_6_26. */
		CONNECT_6_26
	};

	/** The columns. */
	protected int cols;

	/** The rows. */
	protected int rows;

	/** The rule. */
	protected Rule rule;

	/** The slices. */
	protected int slices;

	/**
	 * Instantiates a new topology rule 3d.
	 * 
	 * @param rule
	 *            the rule
	 */
	public TopologyRule3D(Rule rule) {
		this.rule = rule;
	}

}
