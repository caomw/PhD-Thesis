/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber;

// TODO: Auto-generated Javadoc
/**
 * Input view to select a number via a slider.
 * 
 * @author Blake Lucas
 */
public class ParamNumberSliderInputView extends ParamInputView implements
		ChangeListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The field. */
	private JSlider field;

	/**
	 * Default constructor.
	 * 
	 * @param param
	 *            number parameter
	 */
	public ParamNumberSliderInputView(ParamNumber param) {
		super(param);
		field = new JSlider(param.getMin().intValue(), param.getMax()
				.intValue(), param.getValue().intValue());
		field.setMajorTickSpacing((param.getMax().intValue() - param.getMin()
				.intValue()) / 10);
		field.setMinorTickSpacing((param.getMax().intValue() - param.getMin()
				.intValue()) / 20);
		field.setPaintTicks(true);
		field.setPaintLabels(true);
		field.setSnapToTicks(true);
		field.setAlignmentY(1);
		field.setAlignmentX(0);
		field.setMinimumSize(new Dimension(100, 50));
		buildLabelAndParam(field);
		field.addChangeListener(this);
	}

	/**
	 * The slider value has changed.
	 * 
	 * @param event
	 *            slider changed
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource().equals(field)) {
			// Update parameter with current slider value
			commit();
		}
	}

	/**
	 * Commit changes to this parameter.
	 */
	@Override
	public void commit() {
		getParameter().setValue(field.getValue());
		notifyObservers(param, this);
	}

	/**
	 * Update slider with current parameter value.
	 */
	@Override
	public void update() {
		field.setValue(getParameter().getValue().intValue());
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
