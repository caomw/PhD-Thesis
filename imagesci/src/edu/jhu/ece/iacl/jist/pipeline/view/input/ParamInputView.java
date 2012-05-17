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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * Generic input view to enter a parameter value. The input view can be extended
 * to support more types of data input. This class should not store any
 * parameter information because this class is not exported as part of the XML.
 * Input views should be atomic in respect that they are only responsible for
 * manipulating one type of parameter. The input view should also not
 * communicate with any other input views and only respond to events generated
 * by GUI components for this input view.
 * 
 * @author Blake Lucas
 */
public abstract class ParamInputView extends JPanel {

	/** The Constant defaultLabelDimension. */
	public static final Dimension defaultLabelDimension = new Dimension(150, 20);

	/** The Constant defaultNumberFieldDimension. */
	public static final Dimension defaultNumberFieldDimension = new Dimension(
			100, 20);

	/** The Constant defaultTextFieldDimension. */
	public static final Dimension defaultTextFieldDimension = new Dimension(
			200, 20);

	/** The constraints. */
	protected Object constraints = null;

	/** The observers. */
	private ArrayList<ParamViewObserver> observers = new ArrayList<ParamViewObserver>();

	/** The parameter. */
	protected ParamModel param;

	/**
	 * Instantiates a new parameter input view.
	 * 
	 * @param param
	 *            the parameter
	 */
	public ParamInputView(ParamModel param) {
		this.param = param;
	}

	/**
	 * Add observer to listen for changes to parameter values. This allows the
	 * processing dialog to be aware of input changes without knowledge of how
	 * the parameter is viewed.
	 * 
	 * @param observer
	 *            view observer
	 */
	public void addObserver(ParamViewObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	/**
	 * Force commitment of GUI input changes to parameter value.
	 */
	public abstract void commit();

	/**
	 * Gets the field.
	 *
	 * @return the field
	 */
	public abstract JComponent getField();

	/**
	 * Get layout constraints.
	 * 
	 * @return the layout constraints
	 */
	public Object getLayoutConstraints() {
		return constraints;
	}

	/**
	 * Get observers.
	 *
	 * @return observers
	 */
	public ArrayList<ParamViewObserver> getObservers() {
		return observers;
	}

	/**
	 * Get the parameter being viewed.
	 * 
	 * @return the parameter
	 */
	public ParamModel getParameter() {
		return param;
	}

	/**
	 * Notify all observers that this parameter has changed in this input view.
	 * 
	 * @param param
	 *            parameter
	 * @param view
	 *            input view
	 */
	public void notifyObservers(ParamModel param, ParamInputView view) {
		for (ParamViewObserver observer : observers) {
			observer.update(param, view);
		}
	}

	/**
	 * Remove observer from list of observers.
	 *
	 * @param observer view observer
	 */
	public void removeObserver(ParamViewObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Set layout constraints.
	 * 
	 * @param obj
	 *            the object
	 */
	public void setLayoutConstraints(Object obj) {
		this.constraints = obj;
	}

	/**
	 * Update the input view with the current parameter value.
	 */
	public abstract void update();

	/**
	 * Initialize label for parameter input. A lot of input views have the same
	 * look-and-feel for the label, so this functionality was implemented here.
	 * However, extending input views can choose their own look-and-feel for the
	 * parameter input view.
	 * 
	 * @param location
	 *            the location
	 */
	protected void buildLabel(String location) {
		BorderLayout layout = new BorderLayout();
		layout.setHgap(10);
		layout.setVgap(5);
		setLayout(layout);
		ToolTipManager.sharedInstance().registerComponent(this);
		ToolTipManager.sharedInstance().setInitialDelay(100);
		JLabel label = new JLabel(param.getLabel());
		label.setMaximumSize(defaultLabelDimension);
		String desc = param.getDescription();

		label.setAlignmentX(0);
		label.setAlignmentY(0);
		JPanel smallPane = new JPanel(new BorderLayout());
		smallPane.add(label, BorderLayout.NORTH);
		if (desc != null) {
			this.setToolTipText(desc);
		}
		add(smallPane, location);
	}

	/**
	 * Build panel that contains the label and parameter formatted in a standard
	 * way.
	 * 
	 * @param field
	 *            the field
	 */
	protected void buildLabelAndParam(JComponent field) {
		JPanel myPanel = new JPanel();
		BorderLayout layout;
		ToolTipManager.sharedInstance().registerComponent(this);
		ToolTipManager.sharedInstance().setInitialDelay(100);
		myPanel.setLayout(layout = new BorderLayout());
		JPanel labelPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(param.getLabel());
		labelPanel.add(label, BorderLayout.NORTH);
		String desc = param.getDescription();
		if (desc != null) {
			this.setToolTipText(desc);
		}

		myPanel.add(labelPanel, BorderLayout.CENTER);
		myPanel.add(field, BorderLayout.EAST);
		layout.setHgap(5);
		setLayout(new BorderLayout());
		add(myPanel, BorderLayout.CENTER);
	}
}
