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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamOption;

// TODO: Auto-generated Javadoc
/**
 * Input view to select a particular text option from a combobox.
 * 
 * @author Blake Lucas
 */
public class ParamOptionInputView extends ParamInputView implements
		ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2897718521409400973L;

	/** The field. */
	private JComboBox field;

	/**
	 * Construct combobox with options from ParamOption.
	 * 
	 * @param param
	 *            the parameter
	 */
	public ParamOptionInputView(ParamOption param) {
		super(param);
		field = new JComboBox(param.getOptions().toArray());
		field.setMinimumSize(defaultNumberFieldDimension);
		if (param.getOptions().size() > 0) {
			field.setSelectedIndex(param.getIndex());
		}
		buildLabelAndParam(field);
		field.addActionListener(this);
	}

	/**
	 * Update parameter with current selected option.
	 * 
	 * @param event
	 *            selection event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(field)) {
			int selectedIndex = field.getSelectedIndex();
			if (selectedIndex >= 0) {
				getParameter().setValue(selectedIndex);
				notifyObservers(param, this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#commit()
	 */
	@Override
	public void commit() {
		int selectedIndex = field.getSelectedIndex();
		getParameter().setValue(selectedIndex);
		notifyObservers(param, this);
	}

	/**
	 * Update selected index with selected combobox index.
	 */
	@Override
	public void update() {
		field.removeAllItems();
		int index = getParameter().getIndex();
		for (String item : getParameter().getOptions()) {
			field.addItem(item);
		}
		if ((getParameter().getOptions().size() > 0)
				&& (field.getItemCount() > 0)) {
			field.setSelectedIndex(index);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#getParameter()
	 */
	@Override
	public ParamOption getParameter() {
		return (ParamOption) param;
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
