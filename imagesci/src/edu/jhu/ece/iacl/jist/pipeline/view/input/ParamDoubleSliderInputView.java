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
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamDoubleSliderInputView.
 */
public class ParamDoubleSliderInputView extends ParamInputView implements
		ChangeListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The field. */
	private JSlider field;

	/** The log scale. */
	protected boolean logScale;

	/** The max. */
	protected double min, max;

	/**
	 * Instantiates a new param double slider input view.
	 * 
	 * @param param
	 *            the param
	 * @param ticks
	 *            the ticks
	 * @param logScale
	 *            the log scale
	 */
	public ParamDoubleSliderInputView(ParamNumber param, int ticks,
			boolean logScale) {
		super(param);
		field = new JSlider(0, 100);
		field.setMajorTickSpacing(20);
		field.setMinorTickSpacing(10);
		field.setPaintTicks(true);
		field.setPaintLabels(true);
		field.setSnapToTicks(true);
		field.setAlignmentY(1);
		field.setAlignmentX(0);
		field.setMinimumSize(new Dimension(100, 50));

		this.min = param.getMin().doubleValue();
		this.max = param.getMax().doubleValue();
		this.logScale = logScale;

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		double logMin = Math.log10(min);
		double logMax = Math.log10(max);
		for (int i = 0; i < ticks; i++) {
			double t = i / (double) (ticks - 1);
			if (logScale) {

				labelTable.put(
						(int) Math.round(t * 100),
						new JLabel(String.format("%4.2g",
								Math.pow(10, logMin * (1 - t) + logMax * t))));
			} else {
				labelTable.put(
						(int) Math.round(t * 100),
						new JLabel(String
								.format("%4.2g", min + (max - min) * t)));
			}
		}

		field.setLabelTable(labelTable);
		update();
		buildLabelAndParam(field);

		field.setSnapToTicks(false);
		field.addChangeListener(this);
	}

	/**
	 * Update slider with current parameter value.
	 */
	@Override
	public void update() {
		double t;
		double value = getParameter().getDouble();
		if (logScale) {
			t = (Math.log10(value) - Math.log10(min))
					/ (Math.log10(max) - Math.log10(min));
		} else {
			t = (value - min) / (max - min);
		}
		((JSlider) getField()).setValue((int) Math.round(100 * t));
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
		if (logScale) {
			double t = ((JSlider) getField()).getValue() * 0.01;
			double logMin = Math.log10(min);
			double logMax = Math.log10(max);
			getParameter()
					.setValue(Math.pow(10, logMin * (1 - t) + logMax * t));
		} else {
			getParameter().setValue(
					min + (max - min) * ((JSlider) getField()).getValue()
							* 0.01);
		}
		notifyObservers(param, this);
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
