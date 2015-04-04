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

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3i;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamPointInteger;

// TODO: Auto-generated Javadoc
/**
 * Input View creates a text field to enter a point value. The value is
 * validated against the specified min and max value. This view is default for
 * point values.
 * 
 * @author Blake Lucas
 */
public class ParamPointSpinnerInputView extends ParamInputView implements
		ChangeListener {

	/** The Constant defaultPointDimension. */
	protected static final Dimension defaultPointDimension = new Dimension(50,
			25);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The field x. */
	private JSpinner fieldX;

	/** The field y. */
	private JSpinner fieldY;

	/** The field z. */
	private JSpinner fieldZ;

	/** The point panel. */
	private JPanel pointPane;

	/**
	 * Construct text field to enter numerical value.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamPointSpinnerInputView(ParamPointInteger param) {
		super(param);
		pointPane = new JPanel();
		pointPane.setLayout(new BoxLayout(pointPane, BoxLayout.X_AXIS));
		param.getValue();
		fieldX = new JSpinner(new SpinnerNumberModel(
				param.getParamX().getInt(), param.getParamX().getMin()
						.intValue(), param.getParamX().getMax().intValue(), 1));
		fieldX.setAlignmentY(1);
		fieldX.setAlignmentX(0);
		fieldX.setPreferredSize(defaultPointDimension);
		fieldX.addChangeListener(this);
		pointPane.add(fieldX);
		fieldY = new JSpinner(new SpinnerNumberModel(
				param.getParamY().getInt(), param.getParamY().getMin()
						.intValue(), param.getParamY().getMax().intValue(), 1));
		fieldY.setAlignmentY(1);
		fieldY.setAlignmentX(0);
		fieldY.setPreferredSize(defaultPointDimension);
		fieldY.addChangeListener(this);
		pointPane.add(fieldY);
		fieldZ = new JSpinner(new SpinnerNumberModel(
				param.getParamZ().getInt(), param.getParamZ().getMin()
						.intValue(), param.getParamZ().getMax().intValue(), 1));
		fieldZ.setAlignmentY(1);
		fieldZ.setAlignmentX(0);
		fieldZ.setPreferredSize(defaultPointDimension);
		fieldZ.addChangeListener(this);
		pointPane.add(fieldZ);
		buildLabelAndParam(pointPane);
	}

	/**
	 * Commit changes to point.
	 */
	@Override
	public void commit() {
		getParameter().setValue(
				new Point3i(Integer.parseInt(fieldX.getValue().toString()),
						Integer.parseInt(fieldY.getValue().toString()), Integer
								.parseInt(fieldZ.getValue().toString())));
		notifyObservers(param, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource().equals(fieldX)
				|| event.getSource().equals(fieldY)
				|| event.getSource().equals(fieldZ)) {
			try {
				getParameter().setValue(
						new Point3i(Integer.parseInt(fieldX.getValue()
								.toString()), Integer.parseInt(fieldY
								.getValue().toString()), Integer
								.parseInt(fieldZ.getValue().toString())));
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				// System.err.println(getClass().getCanonicalName()+"TEXTBOX PARSE ERROR "+e.getMessage());
			}
		}
	}

	/**
	 * Update field with parameter value.
	 */
	@Override
	public void update() {
		Point3i p = getParameter().getValue();
		fieldX.setValue(p.x);
		fieldY.setValue(p.y);
		fieldZ.setValue(p.z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#getParameter()
	 */
	@Override
	public ParamPointInteger getParameter() {
		return (ParamPointInteger) param;
	}

	/**
	 * A change has occurred to the input field.
	 * 
	 * @param event
	 *            text input changed
	 */
	public void caretUpdate(CaretEvent event) {
	}

	/**
	 * Get field used to enter this value.
	 *
	 * @return the field
	 */
	@Override
	public JComponent getField() {
		return pointPane;
	}
}
