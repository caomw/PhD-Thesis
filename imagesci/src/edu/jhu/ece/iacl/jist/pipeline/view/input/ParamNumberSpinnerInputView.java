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
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import java.awt.BorderLayout;
import java.text.ParseException;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamLong;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber;

// TODO: Auto-generated Javadoc
/**
 * Input View creates a spinner box to enter an integer value between the
 * minimum and maximum value for the parameter.
 * 
 * @author Blake Lucas
 */
public class ParamNumberSpinnerInputView extends ParamInputView implements
		ChangeListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8704969540392449742L;

	/** The field. */
	private JSpinner field;

	/**
	 * Construct spinner to select a double value.
	 * 
	 * @param param
	 *            double parameter
	 * @param inc
	 *            spinner increment
	 */
	public ParamNumberSpinnerInputView(ParamDouble param, double inc) {
		super(param);
		field = new JSpinner(new SpinnerNumberModel(param.getValue(), param
				.getMin().doubleValue(), param.getMax().doubleValue(), inc));
		field.setAlignmentY(1);
		field.setAlignmentX(0);
		field.setPreferredSize(defaultNumberFieldDimension);
		buildLabelAndParam(field);
		field.addChangeListener(this);
	}

	/**
	 * Construct spinner to select an integer value.
	 * 
	 * @param param
	 *            integer parameter
	 */
	public ParamNumberSpinnerInputView(ParamInteger param) {
		super(param);
		field = new JSpinner(new SpinnerNumberModel(
				param.getValue().intValue(), param.getMin().intValue(), param
						.getMax().intValue(), 1));
		field.setAlignmentY(1);
		field.setAlignmentX(0);
		field.setPreferredSize(defaultNumberFieldDimension);
		add(field, BorderLayout.EAST);
		buildLabelAndParam(field);
		field.addChangeListener(this);
	}

	/**
	 * Construct spinner to select an long value.
	 * 
	 * @param param
	 *            long parameter
	 */
	public ParamNumberSpinnerInputView(ParamLong param) {
		super(param);
		field = new JSpinner(new SpinnerNumberModel(param.getValue()
				.longValue(), param.getMin().longValue(), param.getMax()
				.longValue(), 1l));
		field.setAlignmentY(1);
		field.setAlignmentX(0);
		field.setPreferredSize(defaultNumberFieldDimension);
		add(field, BorderLayout.EAST);
		buildLabelAndParam(field);
		field.addChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#commit()
	 */
	@Override
	public void commit() {
		try {
			field.commitEdit();
			getParameter().setValue(
					Double.parseDouble(field.getValue().toString()));
			notifyObservers(param, this);
		} catch (ParseException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
		}
	}

	/**
	 * The spinner value has changed.
	 * 
	 * @param event
	 *            spinner value changed
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		updateParameter(event);
	}

	/**
	 * Update parameter from view value.
	 * 
	 * @param event
	 *            the event
	 */
	private void updateParameter(EventObject event) {
		if (event.getSource().equals(field)) {
			try {
				// Update parameter with current spinner value
				getParameter().setValue((Number) field.getValue());
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				System.err.println(getClass().getCanonicalName() + "UPDATE "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Update the spinner with the current parameter field value.
	 */
	@Override
	public void update() {
		field.setValue(getParameter().getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#getParameter()
	 */
	@Override
	public ParamNumber getParameter() {
		return (ParamNumber) param;
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
