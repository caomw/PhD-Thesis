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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber;

// TODO: Auto-generated Javadoc
/**
 * Input View creates a text field to enter a numerical value. The value is
 * validated against the specified min and max value. This view is default for
 * double values.
 * 
 * @author Blake Lucas
 */
public class ParamNumberTextInputView extends ParamInputView implements
		ActionListener, CaretListener {

	/**
	 * The Class NumericDocument.
	 */
	private class NumericDocument extends PlainDocument {
		// Variables
		/** The max. */
		protected double min, max;

		// Constructor
		/**
		 * Instantiates a new numeric document.
		 *
		 * @param mn the mn
		 * @param mx the mx
		 */
		public NumericDocument(double mn, double mx) {
			super();
			this.min = mn;
			this.max = mx;
		}

		// Insert string method
		/* (non-Javadoc)
		 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
		 */
		@Override
		public void insertString(int offset, String str, AttributeSet attr)
				throws BadLocationException {
			if (str != null) {
				try {
					String SVAL = getText(0, offset) + str
							+ getText(offset, getLength() - offset);
					double val = Double.parseDouble(SVAL);
					val = Double.parseDouble(getText(0, offset) + str
							+ getText(offset, getLength() - offset));
					if (val < min) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
					if (val > max) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
				} catch (NumberFormatException e) {
					return;
				}

				// All is fine, so add the character to the text box
				super.insertString(offset, str, attr);
			}
			return;
		}
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The field. */
	private JTextField field;

	/**
	 * Construct text field to enter numerical value.
	 * 
	 * @param param
	 *            the parameter
	 */
	public ParamNumberTextInputView(ParamNumber param) {
		super(param);
		field = new JTextField();
		field.setDocument(new NumericDocument(param.getMin().doubleValue(),
				param.getMax().doubleValue()));
		field.setText(param.getValue().toString());
		field.setHorizontalAlignment(SwingConstants.RIGHT);
		field.setPreferredSize(defaultNumberFieldDimension);
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
			double val = Double.parseDouble(field.getText());
			getParameter().setValue(val);
			notifyObservers(param, this);

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
			try {
				// Set parameter value to textfield value
				double val = Double.parseDouble(field.getText());
				getParameter().setValue(val);

				notifyObservers(param, this);

				// double val = Double.parseDouble(field.getText());
				// getParameter().setValue(val);
				// if(getParameter().getValue().doubleValue()!=val) {
				// field.setText(getParameter().getValue().doubleValue()+"");
				// }
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				// System.err.println(getClass().getCanonicalName()+e.getMessage());
			}
		}
	}

	/**
	 * Commit changes to parameter.
	 */
	@Override
	public void commit() {
		try {
			// Set parameter value to textfield value
			// getParameter().setValue(Double.parseDouble(field.getText()));
			double val = Double.parseDouble(field.getText());
			getParameter().setValue(val);

			notifyObservers(param, this);
		} catch (NumberFormatException e) {
			// System.err.println(getClass().getCanonicalName()+e.getMessage());
		}
	}

	/**
	 * Update field with parameter value.
	 */
	@Override
	public void update() {
		field.setText(getParameter().getValue().toString());
	}

	/**
	 * Get number parameter.
	 * 
	 * @return the parameter
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
