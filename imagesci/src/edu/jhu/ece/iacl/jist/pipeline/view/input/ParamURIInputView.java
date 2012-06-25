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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.jhu.ece.iacl.jist.io.MipavController;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile;

// TODO: Auto-generated Javadoc
/**
 * File Parameter Input View creates a text field to enter a file and a browse
 * button to select a file. The file name can be mandatory or not mandatory. The
 * file extension filter can be set to only permit files with specific
 * extensions.
 * 
 * @author Blake Lucas
 */
public class ParamURIInputView extends ParamInputView implements
		ActionListener, CaretListener {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8584156964249536461L;
	/** The browse button. */
	private JButton browseButton;
	/** The field. */
	private JTextField field;

	/**
	 * Construct text field to enter file name and browse button to select file.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamURIInputView(ParamFile param) {
		super(param);
		field = new JTextField();
		field.setPreferredSize(defaultTextFieldDimension);
		browseButton = new JButton("Browse");
		browseButton.addActionListener(this);
		browseButton.setPreferredSize(defaultNumberFieldDimension);
		JPanel smallPane = new JPanel(new BorderLayout());
		smallPane.add(field, BorderLayout.CENTER);
		JPanel myPanel = new JPanel(new BorderLayout());
		myPanel.add(browseButton, BorderLayout.EAST);
		smallPane.add(myPanel, BorderLayout.SOUTH);
		field.addCaretListener(this);
		buildLabelAndParam(smallPane);
		update();
	}

	/**
	 * Update the textfield with the current parameter file name.
	 */
	@Override
	public void update() {
		URI uri = getParameter().getURI();
		if (uri != null) {
			field.setText(uri.toString());
		}
	}

	/**
	 * Open file dialog box when browse button is pressed.
	 * 
	 * @param event
	 *            browse button clicked
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(browseButton)) {
			String text = openFileChooser();
			if (text != null) {
				// Set the field text to the selected file name
				field.setText(text);
			}
		}
	}

	/**
	 * Open file chooser to select file with specific extension.
	 * 
	 * @return absolute path of the file
	 */
	private String openFileChooser() {
		JFileChooser openDialog = new JFileChooser();
		openDialog
				.setSelectedFile(MipavController.getDefaultWorkingDirectory());
		if (getParameter().getDialogType() == ParamFile.DialogType.FILE) {
			openDialog.setDialogTitle("Select File");
			openDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			openDialog.setFileFilter(getParameter().getExtensionFilter());
			openDialog.setAcceptAllFileFilterUsed(false);
		} else if (getParameter().getDialogType() == ParamFile.DialogType.DIRECTORY) {
			openDialog.setDialogTitle("Select Directory");
			openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		File oldFile = getParameter().getValue();
		openDialog.setSelectedFile(oldFile);

		openDialog.setDialogType(JFileChooser.OPEN_DIALOG);
		int returnVal = openDialog.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return openDialog.getSelectedFile().getAbsolutePath();
		} else {
			return null;
		}
	}

	/**
	 * Update parameter when text field is changed.
	 * 
	 * @param event
	 *            field text changed
	 */
	@Override
	public void caretUpdate(CaretEvent event) {
		if (event.getSource().equals(field)) {
			URI uri = null;
			File f = null;
			try {
				uri = new URI(field.getText());
				f = new File(uri);
			} catch (URISyntaxException e) {
				// System.err.println(getClass().getCanonicalName()+e.getMessage());
			} catch (IllegalArgumentException e) {

			}
			if (f == null) {
				f = new File(field.getText());
				uri = f.toURI();
			}
			if (f.exists()) {
				getParameter().setValue(uri);
				notifyObservers(param, this);
			}
		}
	}

	/**
	 * Commit changes to this parameter.
	 */
	@Override
	public void commit() {
		File f = new File(field.getText());
		if (f.exists()) {
			getParameter().setValue(f);
			notifyObservers(param, this);
		}
	}

	/**
	 * Get file parameter.
	 * 
	 * @return the parameter
	 */
	@Override
	public ParamFile getParameter() {
		return (ParamFile) param;
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
}
