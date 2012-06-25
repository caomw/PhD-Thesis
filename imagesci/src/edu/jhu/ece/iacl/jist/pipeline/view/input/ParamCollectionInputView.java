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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * Collection Parameter Input View creates a vertical list of child parameters.
 * If a child parameter is a ParamCollection, that parameter is turned into a
 * tab pane. This will build GUI dialogs recursively since a
 * ParamCollectionInputView is also a ParamInputView
 * 
 * @author Blake Lucas
 */
public class ParamCollectionInputView extends ParamInputView implements
		ParamViewObserver, ChangeListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4236640173087293194L;
	/** Customized layout for this collection. */
	protected LayoutManager layout;

	/**
	 * Default constructor.
	 * 
	 * @param params
	 *            parameters
	 * @param layout
	 *            custom layout
	 */
	public ParamCollectionInputView(ParamCollection params, LayoutManager layout) {
		super(params);
		init(params, layout);
	}

	/**
	 * Default constructor that uses default layout.
	 * 
	 * @param params
	 *            parameters
	 */
	public ParamCollectionInputView(ParamCollection params) {
		this(params, null);
	}

	/**
	 * Default constructor.
	 * 
	 * @param params
	 *            parameters
	 */
	public ParamCollectionInputView(ParamModel params) {
		super(params);
	}

	/**
	 * Initialize view with custom layout.
	 */
	public void init() {
		init(this.getParameter(), layout);
	}

	/**
	 * Initialize view.
	 * 
	 * @param params
	 *            parameters
	 * @param layout
	 *            layout
	 */
	public void init(ParamCollection params, LayoutManager layout) {
		this.removeAll();
		JTabbedPane tabPane = null;
		// Default to BoxLayout
		JPanel mainPane = new JPanel();
		JPanel myPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		myPanel.add(mainPane, BorderLayout.NORTH);
		scrollPane.setViewportView(myPanel);
		if (layout == null) {
			this.layout = new BoxLayout(mainPane, BoxLayout.Y_AXIS);
		} else {
			this.layout = layout;
		}
		this.setLayout(new GridLayout(1, 0));
		mainPane.setLayout(this.layout);
		for (ParamModel param : params.getChildren()) {
			if (param.isHidden()) {
				continue;
			}
			if ((param instanceof ParamCollection)
					&& (param.getInputView() instanceof ParamCollectionInputView)) {
				// If tab pane does not exist, create one
				if (tabPane == null) {
					tabPane = new JTabbedPane(SwingConstants.TOP,
							JTabbedPane.WRAP_TAB_LAYOUT);
					mainPane.add(tabPane);
				}
				// Get parameter group view
				ParamCollectionInputView group = (ParamCollectionInputView) param
						.getInputView();
				// Make this an observer of the collection
				group.addObserver(this);
				group.setPreferredSize(group.getMinimumSize());
				// Add group to tab pane
				JPanel smallPane = new JPanel(new BorderLayout(5, 5));
				smallPane.add(group, BorderLayout.NORTH);
				tabPane.add(param.getLabel(), smallPane);
			} else {
				// Add parameter to pane
				ParamInputView view = param.getInputView();
				if (view.getLayoutConstraints() != null) {
					// Use layout constraints if available
					mainPane.add(view, view.getLayoutConstraints());
				} else {
					view.setBorder(BorderFactory.createEtchedBorder());
					mainPane.add(view);
				}
				// Observe changes to child
				view.addObserver(this);
			}
		}
		scrollPane.setMinimumSize(new Dimension(0, 600));
		this.add(scrollPane);
	}

	/**
	 * Commit changes to this view and its children.
	 */
	@Override
	public void commit() {
		for (ParamModel param : getParameter().getChildren()) {
			// Only commit changes if input view is used
			param.getInputView().commit();
		}
	}

	/**
	 * Set visibility of this view and its children.
	 * 
	 * @param visible
	 *            the visible
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		for (ParamModel param : getParameter().getChildren()) {
			param.getInputView().setVisible(visible);
		}
	}

	/**
	 * An update to the parent will invoke updates to the children.
	 */
	@Override
	public void update() {
		for (ParamModel param : getParameter().getChildren()) {
			param.getInputView().update();
		}
	}

	/**
	 * Get parameter collection.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamCollection getParameter() {
		return (ParamCollection) param;
	}

	/**
	 * Get field used to enter this value.
	 *
	 * @return the field
	 */
	@Override
	public JComponent getField() {
		return null;
	}

	/**
	 * The selected tab pane has changed so notify observers.
	 * 
	 * @param event
	 *            tab pane changed
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		notifyObservers(param, this);
	}

	/**
	 * Notify parent observers.
	 * 
	 * @param model
	 *            parameter
	 * @param view
	 *            input view
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		notifyObservers(model, view);
	}
}
