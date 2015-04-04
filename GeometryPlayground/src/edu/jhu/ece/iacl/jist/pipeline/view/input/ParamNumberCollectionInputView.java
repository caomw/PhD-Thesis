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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamLong;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumberCollection;

// TODO: Auto-generated Javadoc
/**
 * Input view to select multiple numbers.
 * 
 * @author Blake Lucas
 */
public class ParamNumberCollectionInputView extends ParamInputView implements
		ListSelectionListener, ActionListener, ListCellRenderer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4483721450186968746L;

	/** The add button. */
	private JButton addButton;

	/** The down button. */
	private JButton downButton;

	/** The field. */
	protected JList field;

	/** The image list size. */
	protected int imageListSize = -1;

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
	 * @param param
	 *            parameters
	 */
	public ParamNumberCollectionInputView(ParamNumberCollection param) {
		super(param);
		buildLabel(BorderLayout.NORTH);
		// Create list of entries that contain ParamNumber
		listBoxEntries = new DefaultListModel();
		// Create listbox entry field
		field = new JList(listBoxEntries);
		// Use custom entry renderer
		field.setCellRenderer(this);
		field.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// Create browse button to select images
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		upButton = new JButton("Up");
		upButton.addActionListener(this);
		downButton = new JButton("Down");
		downButton.addActionListener(this);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
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
		// Create list box
		// Create pane to layout scroll pane and browse button
		smallPane = new JPanel(new BorderLayout(5, 5));
		smallPane.add(scrollPane, BorderLayout.CENTER);
		JPanel optButtons = new JPanel(new GridLayout(0, 2));
		optButtons.add(addButton);
		optButtons.add(removeButton);
		optButtons.add(upButton);
		optButtons.add(downButton);
		smallPane.add(optButtons, BorderLayout.SOUTH);
		smallPane.setPreferredSize(new Dimension(200, 150));
		add(smallPane, BorderLayout.CENTER);
		setMinimumSize(new Dimension(100, 150));
	}

	/**
	 * Select numbers to load when the browse button is clicked.
	 * 
	 * @param event
	 *            browse button clicked
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == addButton) {
			String s = (String) JOptionPane.showInputDialog((new JFrame()),
					"Enter a value:", null, JOptionPane.INFORMATION_MESSAGE,
					null, null, "0");
			if (s != null) {
				Number max = getParameter().getMax();
				Number min = getParameter().getMin();

				if (max instanceof Double) {
					listBoxEntries.addElement(new ParamDouble((listBoxEntries
							.size() + 1) + ")", min.doubleValue(), max
							.doubleValue(), Double.parseDouble(s)));
				} else if (max instanceof Float) {
					listBoxEntries.addElement(new ParamFloat((listBoxEntries
							.size() + 1) + ")", min.floatValue(), max
							.floatValue(), Float.parseFloat(s)));
				} else if (max instanceof Integer) {
					listBoxEntries.addElement(new ParamInteger((listBoxEntries
							.size() + 1) + ")", min.intValue(), max.intValue(),
							Integer.parseInt(s)));
				} else if (max instanceof Long) {
					listBoxEntries.addElement(new ParamLong((listBoxEntries
							.size() + 1) + ")", min.longValue(), max
							.longValue(), Long.parseLong(s)));
				}
				updateParameter();
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
		}
	}

	/**
	 * Commit changes to this parameter view.
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
		ParamNumberCollection p = getParameter();
		p.clear();
		Enumeration en = listBoxEntries.elements();
		while (en.hasMoreElements()) {
			Object obj = en.nextElement();
			if (obj instanceof ParamNumber) {
				p.add(((ParamNumber) obj).getValue());
			} else {
				p.add((Number) obj);
			}
		}
		notifyObservers(p, this);
	}

	/**
	 * Update pane with new value from parameter.
	 */
	@Override
	public void update() {
		ParamNumberCollection p = getParameter();
		// Get current volumes in volume collection
		List<ParamNumber> vols = p.getParameterList();
		listBoxEntries.clear();
		for (ParamNumber vol : vols) {
			listBoxEntries.addElement(vol);
		}
	}

	/**
	 * Get file parameter.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamNumberCollection getParameter() {
		return (ParamNumberCollection) param;
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
	 * Use a custom cell renderer that can interpret number parameters.
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
		ParamNumber param = (ParamNumber) value;
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(new JLabel((index + 1) + ")"), BorderLayout.WEST);
		pane.add(new JLabel(param.toString()), BorderLayout.EAST);
		if (isSelected) {
			pane.setBackground(list.getSelectionBackground());
			pane.setForeground(list.getSelectionForeground());
		} else {
			pane.setBackground(list.getBackground());
			pane.setForeground(list.getForeground());
		}
		return pane;
	}
}
