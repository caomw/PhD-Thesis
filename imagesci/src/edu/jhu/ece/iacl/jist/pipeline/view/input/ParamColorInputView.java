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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamColorInputView.
 */
public class ParamColorInputView extends ParamInputView implements
		ChangeListener, ActionListener {

	/**
	 * The Class ColorIcon.
	 */
	private static class ColorIcon implements Icon {

		/** The color. */
		protected Color color = null;

		/** The height. */
		protected int width, height;

		/**
		 * Instantiates a new color icon.
		 * 
		 * @param w
		 *            the w
		 * @param h
		 *            the h
		 */
		public ColorIcon(int w, int h) {
			this.width = w;
			this.height = h;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return height;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return width;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#paintIcon(java.awt.Component,
		 * java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component pane, Graphics g, int x, int y) {
			if (color != null) {
				g.setColor(color);
				g.fillRect(x, y, width, height);
				g.setColor(Color.black);
				g.drawRect(x, y, width, height);
			}
		}

		/**
		 * Sets the color.
		 * 
		 * @param c
		 *            the new color
		 */
		public void setColor(Color c) {
			this.color = c;
		}

	}

	/** The Constant defaultPointDimension. */
	protected static final Dimension defaultPointDimension = new Dimension(50,
			25);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5522230314641027357L;

	/** The choose color button. */
	private JButton chooseColorButton;

	/** The color icon. */
	private ColorIcon colorIcon;

	/** The point panel. */
	private JPanel colorPane;

	/** The field alpha. */
	private JSpinner fieldAlpha;

	/** The field blue. */
	private JSpinner fieldBlue;

	/** The field green. */
	private JSpinner fieldGreen;

	/** The field red. */
	private JSpinner fieldRed;

	/** The show alpha componenet. */
	private boolean showAlpha = false;

	/**
	 * Construct text field to enter numerical value.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamColorInputView(ParamColor param) {
		this(param, false);
	}

	/**
	 * Construct text field to enter numerical value.
	 *
	 * @param param the param
	 * @param showAlpha the show alpha
	 */
	public ParamColorInputView(ParamColor param, boolean showAlpha) {
		super(param);
		JPanel smallPane = new JPanel(new BorderLayout());
		this.showAlpha = showAlpha;
		colorPane = new JPanel();
		colorPane.setLayout(new BoxLayout(colorPane, BoxLayout.X_AXIS));
		Color p = param.getValue();
		fieldRed = new JSpinner(new SpinnerNumberModel(p.getRed(), 0, 255, 1));
		fieldRed.setAlignmentY(1);
		fieldRed.setAlignmentX(0);
		fieldRed.setPreferredSize(defaultPointDimension);
		fieldRed.addChangeListener(this);
		colorPane.add(fieldRed);
		fieldGreen = new JSpinner(new SpinnerNumberModel(p.getGreen(), 0, 255,
				1));
		fieldGreen.setAlignmentY(1);
		fieldGreen.setAlignmentX(0);
		fieldGreen.setPreferredSize(defaultPointDimension);
		fieldGreen.addChangeListener(this);
		colorPane.add(fieldGreen);
		fieldBlue = new JSpinner(new SpinnerNumberModel(p.getBlue(), 0, 255, 1));
		fieldBlue.setAlignmentY(1);
		fieldBlue.setAlignmentX(0);
		fieldBlue.setPreferredSize(defaultPointDimension);
		fieldBlue.addChangeListener(this);
		colorPane.add(fieldBlue);
		if (showAlpha) {
			fieldAlpha = new JSpinner(new SpinnerNumberModel(p.getAlpha(), 0,
					255, 1));
			fieldAlpha.setAlignmentY(1);
			fieldAlpha.setAlignmentX(0);
			fieldAlpha.setPreferredSize(defaultPointDimension);
			fieldAlpha.addChangeListener(this);
			colorPane.add(fieldAlpha);
		}
		smallPane.add(colorPane, BorderLayout.NORTH);
		smallPane.add(chooseColorButton = new JButton("Select Color",
				colorIcon = new ColorIcon(15, 15)), BorderLayout.CENTER);
		chooseColorButton.setIconTextGap(10);
		chooseColorButton.addActionListener(this);
		buildLabelAndParam(smallPane);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == chooseColorButton) {
			Color c = JColorChooser.showDialog(this, "Select Color",
					getParameter().getValue());
			if (c != null) {
				getParameter().setValue(c);
				update();
			}
		}
	}

	/**
	 * Update field with parameter value.
	 */
	@Override
	public void update() {
		Color p = getParameter().getValue();
		colorIcon.setColor(p);
		chooseColorButton.repaint();
		fieldRed.setValue(p.getRed());
		fieldGreen.setValue(p.getGreen());
		fieldBlue.setValue(p.getBlue());
		if (showAlpha) {
			fieldAlpha.setValue(p.getAlpha());
		}
	}

	/**
	 * Commit changes to point.
	 */
	@Override
	public void commit() {
		if (showAlpha) {
			getParameter()
					.setValue(
							new Color(Integer.parseInt(fieldRed.getValue()
									.toString()), Integer.parseInt(fieldGreen
									.getValue().toString()), Integer
									.parseInt(fieldBlue.getValue().toString()),
									Integer.parseInt(fieldAlpha.getValue()
											.toString())));
		} else {
			getParameter().setValue(
					new Color(Integer.parseInt(fieldRed.getValue().toString()),
							Integer.parseInt(fieldGreen.getValue().toString()),
							Integer.parseInt(fieldBlue.getValue().toString()),
							255));
		}
		notifyObservers(param, this);
	}

	/**
	 * State changed.
	 * 
	 * @param event
	 *            the event
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 *      )
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource().equals(fieldRed)
				|| event.getSource().equals(fieldGreen)
				|| event.getSource().equals(fieldBlue)
				|| event.getSource().equals(fieldAlpha)) {
			try {
				Color c;
				if (showAlpha) {
					getParameter().setValue(
							c = new Color(Integer.parseInt(fieldRed.getValue()
									.toString()), Integer.parseInt(fieldGreen
									.getValue().toString()), Integer
									.parseInt(fieldBlue.getValue().toString()),
									Integer.parseInt(fieldAlpha.getValue()
											.toString())));
				} else {
					getParameter().setValue(
							c = new Color(Integer.parseInt(fieldRed.getValue()
									.toString()), Integer.parseInt(fieldGreen
									.getValue().toString()), Integer
									.parseInt(fieldBlue.getValue().toString()),
									255));
				}
				colorIcon.setColor(c);
				chooseColorButton.repaint();
				notifyObservers(param, this);
			} catch (NumberFormatException e) {
				// System.err.println(getClass().getCanonicalName()+"TEXTBOX PARSE ERROR "+e.getMessage());
			}
		}
	}

	/**
	 * Gets the parameter.
	 * 
	 * @return the parameter
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView#getParameter()
	 */
	@Override
	public ParamColor getParameter() {
		return (ParamColor) param;
	}

	/**
	 * Get field used to enter this value.
	 * 
	 * @return the field
	 */
	@Override
	public JComponent getField() {
		return colorPane;
	}

}
