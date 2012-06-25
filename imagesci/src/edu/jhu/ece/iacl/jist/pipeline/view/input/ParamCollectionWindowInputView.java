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

import java.awt.LayoutManager;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * Display collection of parameters in different windows.
 * 
 * @author Blake Lucas
 */
public class ParamCollectionWindowInputView extends ParamCollectionInputView {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7750323016860035660L;
	/** List of frames to display input parameters. */
	private ArrayList<JFrame> childWindows;

	/**
	 * Default constructor with default window layout.
	 * 
	 * @param params
	 *            parameters
	 */
	public ParamCollectionWindowInputView(ParamCollection params) {
		this(params, null);
	}

	/**
	 * Default constructor.
	 * 
	 * @param params
	 *            parameters
	 * @param layout
	 *            frame layout
	 */
	public ParamCollectionWindowInputView(ParamCollection params,
			LayoutManager layout) {
		super((ParamModel) params);
		childWindows = new ArrayList<JFrame>();
		// Default to BoxLayout
		if (layout == null) {
			layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		}
		this.setLayout(layout);
		for (ParamModel param : params.getChildren()) {
			if ((param instanceof ParamCollection)
					&& (param.getInputView() instanceof ParamCollectionInputView)) {
				// Create new window to display parameter group
				JFrame window = new JFrame();
				childWindows.add(window);
				// Get parameter group view
				ParamCollectionInputView group = (ParamCollectionInputView) param
						.getInputView();
				// Make this an observer of the collection
				group.addObserver(this);
				group.setPreferredSize(group.getMinimumSize());
				window.add(group);
				window.setTitle(param.getLabel());
				// Do not destroy window on close
				window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				window.setVisible(true);
				window.pack();
			} else {
				// Add parameter to pane
				ParamInputView view = param.getInputView();
				if (view.getLayoutConstraints() != null) {
					// Use layout constraints if available
					this.add(view, view.getLayoutConstraints());
				} else {
					this.add(view);
				}
				// Observe changes to child
				view.addObserver(this);
			}
		}
	}

	/**
	 * Set visibility for windows.
	 * 
	 * @param visible
	 *            the visible
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		for (JFrame windows : childWindows) {
			windows.setVisible(visible);
		}
		for (ParamModel param : getParameter().getChildren()) {
			param.getInputView().setVisible(visible);
		}
	}
}
