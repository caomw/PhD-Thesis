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
package edu.jhu.ece.iacl.jist.pipeline.view.output;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * Output view for a collection of parameters.
 * 
 * @author Blake Lucas <br>
 *         Muqun Li(muqun.li@vanderbilt.edu)
 */
public class ParamCollectionOutputView extends ParamOutputView {

	/**
	 * Default constructor.
	 * 
	 * @param params
	 *            parameters
	 */
	public ParamCollectionOutputView(ParamCollection params) {
		this(params, null);
	}

	/**
	 * Build output view as tabbed pane.
	 * 
	 * @param params
	 *            parameters
	 * @param layout
	 *            the layout
	 */
	public ParamCollectionOutputView(ParamCollection params,
			LayoutManager layout) {
		this.param = params;
		JTabbedPane tabPane = null;
		// Default to BoxLayout
		JPanel mainPane = new JPanel();
		if (layout == null) {
			layout = new BoxLayout(mainPane, BoxLayout.Y_AXIS);
		}
		this.setLayout(new GridLayout(1, 0));
		mainPane.setLayout(layout);
		for (ParamModel param : params.getChildren()) {
			if (param.isHidden()) {
				continue;
			}
			if ((param instanceof ParamCollection)
					&& (param.getOutputView() instanceof ParamCollectionOutputView)) {
				// If tab pane does not exist, create one
				if (tabPane == null) {
					tabPane = new JTabbedPane(SwingConstants.TOP,
							JTabbedPane.WRAP_TAB_LAYOUT);
					mainPane.add(tabPane);
				}
				// Get parameter group view
				ParamCollectionOutputView group = (ParamCollectionOutputView) param
						.getOutputView();
				group.setPreferredSize(group.getMinimumSize());
				// Add group to tab pane
				JPanel smallPane = new JPanel(new BorderLayout(5, 5));
				smallPane.add(group, BorderLayout.NORTH);
				tabPane.add(param.getLabel(), smallPane);
			} else {
				// Add parameter to pane
				ParamOutputView view = param.getOutputView();
				if (view.getLayoutConstraints() != null) {
					// Use layout constraints if available
					mainPane.add(view, view.getLayoutConstraints());
				} else {
					view.setBorder(BorderFactory.createEtchedBorder());
					mainPane.add(view);
				}
			}
		}
		this.add(mainPane);
	}

	/**
	 * Update all views with new parameter values.
	 */
	@Override
	public void update() {
		for (ParamModel p : ((ParamCollection) param).getChildren()) {
			p.getOutputView().update();
		}
	}
}
