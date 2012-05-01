/**
 * ImageSci Toolkit
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
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * A collection input view that displays parameters in outlined panes instead of
 * tabbed panes.
 * 
 * @author Blake Lucas
 */
public class ParamCollectionPaneInputView extends ParamCollectionInputView {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6343651882334374546L;

	/**
	 * Default constructor with default layout.
	 * 
	 * @param params
	 *            parameters
	 */
	public ParamCollectionPaneInputView(ParamCollection params) {
		this(params, null);
	}

	/**
	 * Default constructor.
	 * 
	 * @param params
	 *            parameters
	 * @param layout
	 *            custom layout
	 */
	public ParamCollectionPaneInputView(ParamCollection params,
			LayoutManager layout) {
		super((ParamModel) params);
		// Default to BoxLayout
		if (layout == null) {
			layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		}
		this.setLayout(layout);
		for (ParamModel param : params.getChildren()) {
			if ((param instanceof ParamCollection)
					&& (param.getInputView() instanceof ParamCollectionInputView)) {
				// Get parameter group view
				ParamCollectionInputView group = (ParamCollectionInputView) param
						.getInputView();
				// Make this an observer of the collection
				group.addObserver(this);
				group.setPreferredSize(group.getMinimumSize());
				// Add group to pane
				JPanel smallPane = new JPanel(new BorderLayout(5, 5));
				smallPane.add(group, BorderLayout.NORTH);
				// Add border around parameter group
				smallPane.setBorder(BorderFactory.createTitledBorder(param
						.getLabel()));
				this.add(smallPane);
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
}
