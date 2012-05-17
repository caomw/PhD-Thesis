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
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamString;

// TODO: Auto-generated Javadoc
/**
 * Input View creates a text field to enter a numerical value. The value is
 * validated against the specified min and max value. This view is default for
 * double values.
 * 
 * @author Blake Lucas
 */
public class ParamTextInputView extends ParamInputView implements
		ActionListener, CaretListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The field. */
	private JTextField field;

	/**
	 * Construct text field to enter numerical value.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamTextInputView(ParamString param) {
		super(param);
		field = new JTextField();
		field.setText(param.getValue());
		field.setPreferredSize(defaultTextFieldDimension);
		add(field, BorderLayout.EAST);
		field.addActionListener(this);
		field.addCaretListener(this);
		buildLabelAndParam(field);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(field)) {
			try {
				getParameter().setValue(field.getText());
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				System.err.println(getClass().getCanonicalName()
						+ e.getMessage());
			}
		}
	}

	/**
	 * A change has occurred to the input field.
	 * 
	 * @param event
	 *            text input changed
	 */
	@Override
	public void caretUpdate(CaretEvent event) {
		if (event.getSource().equals(field)) {
			// Set parameter value to textfield value
			getParameter().setValue(field.getText());
			notifyObservers(param, this);
		}
	}

	/**
	 * Commit changes to text field.
	 */
	@Override
	public void commit() {
		// Set parameter value to textfield value
		getParameter().setValue(field.getText());
		notifyObservers(param, this);
	}

	/**
	 * Update field with parameter value.
	 */
	@Override
	public void update() {
		field.setText(getParameter().getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#getParameter()
	 */
	@Override
	public ParamString getParameter() {
		return (ParamString) param;
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
