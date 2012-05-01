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
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamMultiOption;

// TODO: Auto-generated Javadoc
/**
 * Input view to select multiple options.
 * 
 * @author Blake Lucas
 */
public class ParamMultiOptionInputView extends ParamInputView implements
		ListSelectionListener, ListCellRenderer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4483721450186968746L;

	/** The field. */
	protected JList field;

	/** The list box entries. */
	protected DefaultListModel listBoxEntries;

	/** The list pane. */
	protected JPanel listPane;

	/** The list size. */
	protected int listSize = -1;

	/** The refresher. */
	private Refresher refresher;

	/** The scroll pane. */
	protected JScrollPane scrollPane;

	/**
	 * Default constructor.
	 * 
	 * @param param
	 *            multi-option parameter
	 */
	public ParamMultiOptionInputView(ParamMultiOption param) {
		super(param);
		// Create list of entries that contain ParamVolume
		listBoxEntries = new DefaultListModel();
		// Create listbox entry field
		field = new JList(listBoxEntries);
		// Use custom entry renderer
		field.setCellRenderer(this);
		field.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// Create scroll pane to display entries
		scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(100, 30));
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// Create list pane to layout listbox entries
		listPane = new JPanel(new BorderLayout());
		listPane.add(field, BorderLayout.CENTER);
		// Create pane to layout list pane
		JPanel smallPane = new JPanel(new BorderLayout());
		smallPane.add(listPane, BorderLayout.NORTH);
		scrollPane.setViewportView(smallPane);
		update();
		// Create pane to layout scroll pane and browse button
		smallPane = new JPanel(new BorderLayout(5, 5));
		smallPane.add(scrollPane, BorderLayout.CENTER);
		smallPane.setPreferredSize(new Dimension(150, 150));
		setMinimumSize(new Dimension(100, 100));
		buildLabelAndParam(smallPane);
		field.addListSelectionListener(this);
	}

	/**
	 * Update pane with new value from parameter.
	 */
	@Override
	public void update() {
		field.removeListSelectionListener(this);
		refresh();
		ArrayList<Integer> items = getParameter().getSelection();
		int[] selected = new int[items.size()];
		int i = 0;
		for (Integer select : items) {
			selected[i++] = select;
		}
		field.setSelectedIndices(selected);
		field.addListSelectionListener(this);
	}

	/**
	 * Refresh view by rebuilding list box.
	 */
	public void refresh() {
		field.removeListSelectionListener(this);
		boolean equals = false;
		if (listBoxEntries.size() == getParameter().getOptions().size()) {
			equals = true;
			for (int i = 0; i < listBoxEntries.size(); i++) {
				if (!listBoxEntries.get(i).equals(
						getParameter().getOptions().get(i))) {
					equals = false;
				}
			}
		}
		if (!equals) {
			listBoxEntries.clear();
			for (String option : getParameter().getOptions()) {
				listBoxEntries.addElement(option);
			}
		}
		field.addListSelectionListener(this);
	}

	/**
	 * Commit changes to this parameter.
	 */
	@Override
	public void commit() {
		updateParameter();
	}

	/**
	 * Update parameter when the list box selection changes.
	 * 
	 * @param event
	 *            selection changed
	 */
	@Override
	public void valueChanged(ListSelectionEvent event) {
		updateParameter();
	}

	/**
	 * Update parameter value with selected items from list box.
	 */
	protected void updateParameter() {
		int[] items = field.getSelectedIndices();
		ParamMultiOption p = getParameter();
		ArrayList<Integer> selection = new ArrayList<Integer>();
		for (int item : items) {
			selection.add(item);
		}
		p.setSelection(selection);
		notifyObservers(p, this);
	}

	/**
	 * Get multi-option parameter.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamMultiOption getParameter() {
		return (ParamMultiOption) param;
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

	/**
	 * Use a custom cell renderer that can interpret option.
	 * 
	 * @param list
	 *            list box
	 * @param value
	 *            list box entry
	 * @param index
	 *            selected index
	 * @param isSelected
	 *            is selected
	 * @param cellHasFocus
	 *            has focus
	 * @return the list cell renderer component
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// Get the selected index. (The index parameter isn't
		// always valid, so just use the value.)
		JLabel listLabel = new JLabel(value.toString());
		JPanel pane = new JPanel();
		if (isSelected) {
			pane.setBackground(list.getSelectionBackground());
			pane.setForeground(list.getSelectionForeground());
		} else {
			pane.setBackground(list.getBackground());
			pane.setForeground(list.getForeground());
		}
		pane.add(listLabel);
		return pane;
	}

	/**
	 * Get refresher for this view.
	 * 
	 * @return refresher
	 */
	public Refresher getRefresher() {
		return refresher;
	}

	/**
	 * Get index of list box entry.
	 * 
	 * @param name
	 *            image name
	 * @return list box index
	 */
	protected int getIndexOf(String name) {
		int index = -1;
		for (int j = 0; j < listBoxEntries.size(); j++) {
			if (listBoxEntries.get(j).equals(name)) {
				index = j;
				break;
			}
		}
		return index;
	}
}
