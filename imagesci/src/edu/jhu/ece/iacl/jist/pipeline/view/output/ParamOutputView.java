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
package edu.jhu.ece.iacl.jist.pipeline.view.output;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * Generic Output View to display an output value. By default, the value
 * displayed as its toString() representation Extending classes can also add
 * event handlers to further manipulate the output
 * 
 * @author Blake Lucas <br>
 *         Muqun Li(muqun.li@vanderbilt.edu)
 */
public class ParamOutputView extends JPanel {

	// default label dimensions
	/** The Constant labelDimension. */
	protected static final Dimension labelDimension = new Dimension(150, 20);
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5143890134588768529L;
	// layout constraints
	/** The constraints. */
	protected Object constraints = null;
	// Field to describe the parameter
	/** The field. */
	protected JLabel field;
	// Label for output parameter
	/** The label. */
	protected JLabel label;
	// Parameter to be displayed
	/** The param. */
	protected ParamModel param;

	/**
	 * Constructor does nothing. Output view construction is left to extending
	 * class.
	 */
	public ParamOutputView() {
	}

	/**
	 * Construct output view as string representation of parameter.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamOutputView(ParamModel param) {
		this.param = param;
		BorderLayout layout = new BorderLayout();
		layout.setHgap(5);
		layout.setVgap(5);
		setLayout(layout);
		// Create label
		label = new JLabel("<HTML><B>" + param.getLabel() + "</B></HTML>");
		label.setAlignmentX(0);
		label.setAlignmentY(0);
		label.setVerticalTextPosition(SwingConstants.TOP);
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		// Create parameter description field
		field = new JLabel(param.toString());
		field.setAlignmentX(0);
		field.setAlignmentY(0);
		field.setVerticalTextPosition(SwingConstants.TOP);
		field.setHorizontalTextPosition(SwingConstants.LEFT);
		add(label, BorderLayout.WEST);
		add(field, BorderLayout.CENTER);
	}

	/**
	 * Get layout constraints.
	 * 
	 * @return layout constraints
	 */
	public Object getLayoutConstraints() {
		return constraints;
	}

	/**
	 * Set layout constraints.
	 * 
	 * @param obj
	 *            layout constraints
	 */
	public void setLayoutConstraints(Object obj) {
		this.constraints = obj;
	}

	/**
	 * An update has occurred to the output parameter. Populate field with new
	 * output value
	 */
	public void update() {
		field.setText(param.toString());
	}
}
