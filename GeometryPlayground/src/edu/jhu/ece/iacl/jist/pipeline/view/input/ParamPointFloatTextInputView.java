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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.vecmath.Point3f;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamPointFloat;

// TODO: Auto-generated Javadoc
/**
 * Input View creates a text field to enter a point value. The value is
 * validated against the specified min and max value. This view is default for
 * point values.
 * 
 * @author Blake Lucas
 */
public class ParamPointFloatTextInputView extends ParamInputView implements
		ActionListener, CaretListener {

	/** The Constant defaultPointDimension. */
	protected static final Dimension defaultPointDimension = new Dimension(50,
			25);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The field x. */
	private JTextField fieldX;

	/** The field y. */
	private JTextField fieldY;

	/** The field z. */
	private JTextField fieldZ;

	/** The point panel. */
	private JPanel pointPane;

	/**
	 * Construct text field to enter numerical value.
	 * 
	 * @param param
	 *            the parameter
	 */
	public ParamPointFloatTextInputView(ParamPointFloat param) {
		super(param);
		pointPane = new JPanel();
		pointPane.setLayout(new BoxLayout(pointPane, BoxLayout.X_AXIS));
		Point3f p = param.getValue();
		fieldX = new JTextField();
		fieldX.setText("" + p.x);
		fieldX.setAlignmentY(1);
		fieldX.setAlignmentX(0);
		fieldX.setPreferredSize(defaultPointDimension);
		fieldX.setHorizontalAlignment(SwingConstants.RIGHT);
		fieldX.addActionListener(this);
		fieldX.addCaretListener(this);
		pointPane.add(fieldX);
		fieldY = new JTextField();
		fieldY.setText("" + p.y);
		fieldY.setAlignmentY(1);
		fieldY.setAlignmentX(0);
		fieldY.setPreferredSize(defaultPointDimension);
		fieldY.setHorizontalAlignment(SwingConstants.RIGHT);
		fieldY.addActionListener(this);
		fieldY.addCaretListener(this);
		pointPane.add(fieldY);
		fieldZ = new JTextField();
		fieldZ.setText("" + p.z);
		fieldZ.setAlignmentY(1);
		fieldZ.setAlignmentX(0);
		fieldZ.setHorizontalAlignment(SwingConstants.RIGHT);
		fieldZ.setPreferredSize(defaultPointDimension);
		fieldZ.addActionListener(this);
		fieldZ.addCaretListener(this);
		pointPane.add(fieldZ);
		buildLabelAndParam(pointPane);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(fieldX)) {
			try {
				getParameter().setValue(
						new Point3f(Float.parseFloat(fieldX.getText()), Float
								.parseFloat(fieldY.getText()), Float
								.parseFloat(fieldZ.getText())));
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				// System.err.println(getClass().getCanonicalName()+"TEXTBOX PARSE ERROR "+e.getMessage());
			}
		} else if (event.getSource().equals(fieldY)) {
			try {
				getParameter().setValue(
						new Point3f(Float.parseFloat(fieldX.getText()), Float
								.parseFloat(fieldY.getText()), Float
								.parseFloat(fieldZ.getText())));
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				// System.err.println(getClass().getCanonicalName()+"TEXTBOX PARSE ERROR "+e.getMessage());
			}
		} else if (event.getSource().equals(fieldZ)) {
			try {
				getParameter().setValue(
						new Point3f(Float.parseFloat(fieldX.getText()), Float
								.parseFloat(fieldY.getText()), Float
								.parseFloat(fieldZ.getText())));
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				// System.err.println(getClass().getCanonicalName()+"TEXTBOX PARSE ERROR "+e.getMessage());
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
		if (event.getSource().equals(fieldX)
				|| event.getSource().equals(fieldY)
				|| event.getSource().equals(fieldZ)) {
			try {
				getParameter().setValue(
						new Point3f(Float.parseFloat(fieldX.getText()), Float
								.parseFloat(fieldY.getText()), Float
								.parseFloat(fieldZ.getText())));
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				// System.err.println(getClass().getCanonicalName()+"TEXTBOX PARSE ERROR "+e.getMessage());
			}
		}
	}

	/**
	 * Commit changes to this parameter.
	 */
	@Override
	public void commit() {
		getParameter().setValue(
				new Point3f(Float.parseFloat(fieldX.getText()), Float
						.parseFloat(fieldY.getText()), Float.parseFloat(fieldZ
						.getText())));
		notifyObservers(param, this);
	}

	/**
	 * Update field with parameter value.
	 */
	@Override
	public void update() {
		Point3f p = getParameter().getValue();
		fieldX.setText("" + p.x);
		fieldY.setText("" + p.y);
		fieldZ.setText("" + p.z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#getParameter()
	 */
	@Override
	public ParamPointFloat getParameter() {
		return (ParamPointFloat) param;
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
