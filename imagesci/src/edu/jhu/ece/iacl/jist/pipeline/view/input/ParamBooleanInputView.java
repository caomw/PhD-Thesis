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
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;

// TODO: Auto-generated Javadoc
/**
 * Boolean Parameter Input View creates a checkbox to enter the parameter.
 * 
 * @author Blake Lucas
 */
public class ParamBooleanInputView extends ParamInputView implements
		ChangeListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4321500604312102744L;

	/** A checkbox to denote TRUE or FALSE input. */
	private JCheckBox field;

	/**
	 * Construct new checkbox to represent parameter.
	 * 
	 * @param param
	 *            the parameter
	 */
	public ParamBooleanInputView(ParamBoolean param) {
		super(param);
		field = new JCheckBox("", param.getValue());
		field.setAlignmentY(1);
		field.setAlignmentX(0);
		field.setPreferredSize(defaultNumberFieldDimension);
		field.addChangeListener(this);
		buildLabelAndParam(field);
	}

	/**
	 * Update parameter with checkbox value.
	 * 
	 * @param event
	 *            checkbox change
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource().equals(field)) {
			commit();
		}
	}

	/**
	 * Unimplemented.
	 */
	@Override
	public void commit() {
		if (getParameter().getValue() != field.isSelected()) {
			getParameter().setValue(field.isSelected());
			notifyObservers(param, this);
		}
	}

	/**
	 * Update checkbox with parameter value.
	 */
	@Override
	public void update() {
		field.setSelected(getParameter().getValue());
	}

	/**
	 * Get boolean parameter.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamBoolean getParameter() {
		return (ParamBoolean) param;
	}

	/**
	 * Get field used to enter this value.
	 *
	 * @return the field
	 */
	@Override
	public JComponent getField() {
		return field;
	}
}
