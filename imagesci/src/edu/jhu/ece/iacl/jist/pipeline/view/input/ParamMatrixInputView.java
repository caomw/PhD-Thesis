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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import Jama.Matrix;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamMatrix;

// TODO: Auto-generated Javadoc
/**
 * Input View creates a text field to enter a numerical value. The value is
 * validated against the specified min and max value. This view is default for
 * double values.
 * 
 * @author Blake Lucas
 */
public class ParamMatrixInputView extends ParamInputView implements
		ActionListener, CaretListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The field. */
	private JTextField[][] field;
	/**
	 * The matrix panel.
	 */
	private JPanel matrixPane;

	/**
	 * Construct text field to enter numerical value.
	 * 
	 * @param param
	 *            the parameter
	 */
	public ParamMatrixInputView(ParamMatrix param) {
		super(param);
		matrixPane = new JPanel(
				new GridLayout(param.getRows(), param.getCols()));
		field = new JTextField[param.getRows()][param.getCols()];
		Matrix m = param.getValue();
		for (int i = 0; i < param.getRows(); i++) {
			for (int j = 0; j < param.getCols(); j++) {
				field[i][j] = new JTextField();
				field[i][j].setText(m.get(i, j) + "");
				field[i][j].setHorizontalAlignment(SwingConstants.RIGHT);
				field[i][j].setPreferredSize(new Dimension(50, 25));
				field[i][j].addActionListener(this);
				field[i][j].addCaretListener(this);
				matrixPane.add(field[i][j]);
			}
		}
		buildLabel(BorderLayout.NORTH);
		add(matrixPane, BorderLayout.CENTER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		ParamMatrix param = getParameter();
		for (int i = 0; i < param.getRows(); i++) {
			for (int j = 0; j < param.getCols(); j++) {
				if (event.getSource().equals(field[i][j])) {
					try {
						getParameter().setValue(i, j,
								Double.parseDouble(field[i][j].getText()));
						notifyObservers(param, this);
					} catch (NumberFormatException e) {
						System.err.println(getClass().getCanonicalName() + "("
								+ i + "," + j + ") " + e.getMessage());
					}
				}
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
		ParamMatrix param = getParameter();
		for (int i = 0; i < param.getRows(); i++) {
			for (int j = 0; j < param.getCols(); j++) {
				if (event.getSource().equals(field[i][j])) {
					try {
						getParameter().setValue(i, j,
								Double.parseDouble(field[i][j].getText()));
						notifyObservers(param, this);
					} catch (NumberFormatException e) {
						// System.err.println(getClass().getCanonicalName()+"("
						// + i + "," + j + ") " + e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Commit changes to parameter.
	 */
	@Override
	public void commit() {
		ParamMatrix param = getParameter();
		for (int i = 0; i < param.getRows(); i++) {
			for (int j = 0; j < param.getCols(); j++) {
				try {
					param.setValue(i, j,
							Double.parseDouble(field[i][j].getText()));
					notifyObservers(param, this);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName() + "(" + i
							+ "," + j + ") " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Update field with parameter value.
	 */
	@Override
	public void update() {
		Matrix m = getParameter().getValue();
		ParamMatrix param = getParameter();
		for (int i = 0; i < param.getRows(); i++) {
			for (int j = 0; j < param.getCols(); j++) {
				field[i][j].setText(m.get(i, j) + "");
			}
		}
	}

	/**
	 * Get number parameter.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamMatrix getParameter() {
		return (ParamMatrix) param;
	}

	/**
	 * Get field used to enter this value.
	 *
	 * @return the field
	 */
	@Override
	public JComponent getField() {
		return matrixPane;
	}
}
