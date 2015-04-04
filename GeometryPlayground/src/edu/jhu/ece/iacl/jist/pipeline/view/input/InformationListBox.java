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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

// TODO: Auto-generated Javadoc
/**
 * A customized list box for displaying and editing algorithm information.
 * 
 * @param <T>
 *            *
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public abstract class InformationListBox<T> extends JPanel implements
		ActionListener, ListCellRenderer {

	/** The add button. */
	private JButton addButton;

	/** The down button. */
	private JButton downButton;

	/**
	 * Editable.
	 */
	protected boolean editable;

	/** The edit button. */
	private JButton editButton;

	/** The field. */
	protected JList field;

	/** The list box entries. */
	protected DefaultListModel listBoxEntries;

	/** The list pane. */
	protected JPanel listPane;

	/** The remove button. */
	private JButton removeButton;

	/** The scroll pane. */
	protected JScrollPane scrollPane;

	/** The up button. */
	private JButton upButton;

	/**
	 * Default constructor.
	 *
	 * @param title the title
	 * @param editable the editable
	 */
	public InformationListBox(String title, boolean editable) {
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.editable = editable;
		// Create list of entries that contain ParamFile
		listBoxEntries = new DefaultListModel();
		// Create listbox entry field
		field = new JList(listBoxEntries);
		// Use custom entry renderer
		field.setCellRenderer(this);
		field.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// Create scroll pane to display entries
		scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(200, 30));
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// Create list pane to layout listbox entries
		listPane = new JPanel(new BorderLayout());
		listPane.add(field, BorderLayout.CENTER);

		editButton = new JButton((editable) ? "Edit" : "View");
		editButton.addActionListener(this);
		// Create pane to layout list pane
		JPanel smallPane = new JPanel(new BorderLayout());
		smallPane.add(listPane, BorderLayout.NORTH);
		scrollPane.setViewportView(smallPane);
		// Create list box
		// Create pane to layout scroll pane and browse button
		smallPane = new JPanel(new BorderLayout(5, 5));
		smallPane.add(scrollPane, BorderLayout.CENTER);
		JPanel optButtons = new JPanel(new GridLayout(1, 0));
		if (editable) {
			addButton = new JButton("Add");
			addButton.addActionListener(this);
			optButtons.add(addButton);
			removeButton = new JButton("Remove");
			removeButton.addActionListener(this);

			optButtons.add(removeButton);
		}
		optButtons.add(editButton);
		smallPane.add(optButtons, BorderLayout.SOUTH);
		smallPane.setPreferredSize(new Dimension(300, 150));
		add(smallPane, BorderLayout.CENTER);
		setMinimumSize(new Dimension(100, 150));

	}

	/**
	 * Select files to load when the browse button is clicked.
	 * 
	 * @param event
	 *            browse button clicked
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == addButton) {
			T obj = addItem();
			if (obj != null) {
				listBoxEntries.addElement(obj);
			}
		} else if (event.getSource() == removeButton) {
			Object[] selected = field.getSelectedValues();
			for (Object obj : selected) {
				listBoxEntries.removeElement(obj);
			}
		} else if (event.getSource() == upButton) {
			int index1 = field.getSelectedIndex();
			int index2 = (index1 - 1 + listBoxEntries.size())
					% listBoxEntries.size();
			Object obj1 = listBoxEntries.elementAt(index1);
			Object obj2 = listBoxEntries.elementAt(index2);
			listBoxEntries.setElementAt(obj1, index2);
			listBoxEntries.setElementAt(obj2, index1);
			field.setSelectedIndex(index2);
		} else if (event.getSource() == downButton) {
			int index1 = field.getSelectedIndex();
			int index2 = (index1 + 1) % listBoxEntries.size();
			Object obj1 = listBoxEntries.elementAt(index1);
			Object obj2 = listBoxEntries.elementAt(index2);
			listBoxEntries.setElementAt(obj1, index2);
			listBoxEntries.setElementAt(obj2, index1);
			field.setSelectedIndex(index2);
		} else if (event.getSource() == editButton) {
			T selected = (T) field.getSelectedValue();
			int in = field.getSelectedIndex();
			if (in >= 0) {
				T changed = editItem(selected);
				if (changed != null) {
					listBoxEntries.set(in, changed);
				}
			}
		}
	}

	/**
	 * Copy list box entries into existing list.
	 *
	 * @param list the list
	 * @return the list
	 */
	public void getList(List<T> list) {
		Enumeration en = listBoxEntries.elements();
		list.clear();
		while (en.hasMoreElements()) {
			Object obj = en.nextElement();
			list.add((T) obj);
		}
	}

	/**
	 * Renderer for list box items.
	 * 
	 * @param list
	 *            the list
	 * @param value
	 *            the value
	 * @param index
	 *            the index
	 * @param isSelected
	 *            the is selected
	 * @param cellHasFocus
	 *            the cell has focus
	 * @return the list cell renderer component
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// Get the selected index. (The index parameter isn't
		// always valid, so just use the value.)
		JLabel listLabel = new JLabel(value.toString());
		JPanel pane = new JPanel(new BorderLayout());
		if (isSelected) {
			pane.setBackground(list.getSelectionBackground());
			pane.setForeground(list.getSelectionForeground());
		} else {
			pane.setBackground(list.getBackground());
			pane.setForeground(list.getForeground());
		}
		pane.add(listLabel, BorderLayout.WEST);
		return pane;
	}

	/**
	 * Set list box entries from existing list.
	 * 
	 * @param list
	 *            list
	 */
	public void setList(List<T> list) {
		listBoxEntries.clear();
		for (T vol : list) {
			listBoxEntries.addElement(vol);
		}
	}

	/**
	 * Called when "ADD" button is clicked.
	 * 
	 * @return the added item
	 */
	protected abstract T addItem();

	/**
	 * Called when "EDIT" button is clicked.
	 * 
	 * @param obj
	 *            the item to edit
	 * @return the edited item
	 */
	protected abstract T editItem(T obj);
}
