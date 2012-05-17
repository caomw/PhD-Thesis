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

import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamURL;

// TODO: Auto-generated Javadoc
/**
 * URL Parameter Input View creates a text field to enter a file and a browse
 * button to select a file. The file name can be mandatory or not mandatory. The
 * file extension filter can be set to only permit files with specific
 * extensions.
 * 
 * @author Blake Lucas
 */
public class ParamURLInputView extends ParamInputView implements CaretListener {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8584156964249536461L;
	/** The field. */
	private JTextField field;

	/**
	 * Construct text field to enter file name and browse button to select file.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamURLInputView(ParamURL param) {
		super(param);
		field = new JTextField("http://");
		field.setPreferredSize(defaultTextFieldDimension);
		field.addCaretListener(this);
		buildLabelAndParam(field);
		update();
	}

	/**
	 * Update the textfield with the current parameter file name.
	 */
	@Override
	public void update() {
		URL url = getParameter().getValue();
		if (url != null) {
			field.setText(url.toString());
		}
	}

	/**
	 * Update parameter when text field is changed.
	 * 
	 * @param event
	 *            field text changed
	 */
	@Override
	public void caretUpdate(CaretEvent event) {
		if (event.getSource().equals(field)) {
			getParameter().setValue(field.getText());
		}
	}

	/**
	 * Commit changes to this parameter.
	 */
	@Override
	public void commit() {
		getParameter().setValue(field.getText());
		notifyObservers(param, this);
	}

	/**
	 * Get file parameter.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamURL getParameter() {
		return (ParamURL) param;
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
