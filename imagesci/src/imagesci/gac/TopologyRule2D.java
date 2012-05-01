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
package imagesci.gac;

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
