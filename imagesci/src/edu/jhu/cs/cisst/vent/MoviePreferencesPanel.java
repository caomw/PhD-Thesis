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
package edu.jhu.cs.cisst.vent;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

// TODO: Auto-generated Javadoc
/**
 * The Class MoviePreferencesPanel.
 */
public class MoviePreferencesPanel extends JPanel {

	/** The duration. */
	int duration = 1;

	/** The duration field. */
	private JSpinner durationField;

	/** The frame rate. */
	int frameRate = 15;

	/** The frame rate field. */
	private JSpinner frameRateField;

	/** The height field. */
	private JSpinner heightField;

	/** The movie height. */
	int movieHeight = 480;

	/** The movie width. */
	int movieWidth = 640;

	/** The width field. */
	private JSpinner widthField;

	/**
	 * Instantiates a new movie preferences panel.
	 */
	public MoviePreferencesPanel() {
		super();
		createPane();
	}

	/**
	 * Create panel to display preferences.
	 */
	protected void createPane() {
		this.setLayout(new BorderLayout());
		JPanel small = new JPanel();
		BoxLayout layout = new BoxLayout(small, BoxLayout.PAGE_AXIS);
		small.setLayout(layout);
		this.add(small, BorderLayout.NORTH);
		JPanel durationPane = new JPanel(new BorderLayout());
		durationPane.add(new JLabel("Duration"), BorderLayout.WEST);
		durationPane.add(durationField = new JSpinner(new SpinnerNumberModel(
				duration, 1, 100000, 1l)), BorderLayout.EAST);
		small.add(durationPane);
		JPanel frameRatePane = new JPanel(new BorderLayout());
		frameRatePane.add(new JLabel("Frame Rate"), BorderLayout.WEST);
		frameRatePane.add(frameRateField = new JSpinner(new SpinnerNumberModel(
				frameRate, 1, 60, 1l)), BorderLayout.EAST);
		small.add(frameRatePane);

		JPanel widthPane = new JPanel(new BorderLayout());
		widthPane.add(new JLabel("Width"), BorderLayout.WEST);
		widthPane.add(widthField = new JSpinner(new SpinnerNumberModel(
				movieWidth, 1, 5000, 1l)), BorderLayout.EAST);
		small.add(widthPane);

		JPanel heightPane = new JPanel(new BorderLayout());
		heightPane.add(new JLabel("Height"), BorderLayout.WEST);
		heightPane.add(heightField = new JSpinner(new SpinnerNumberModel(
				movieHeight, 1, 5000, 1l)), BorderLayout.EAST);
		small.add(heightPane);
	}

	/**
	 * Show dialog.
	 * 
	 * @param comp
	 *            the comp
	 * 
	 * @return true, if successful
	 */
	public static MoviePreferencesPanel showDialog(Component comp) {
		MoviePreferencesPanel panel = new MoviePreferencesPanel();
		while (true) {
			int n = JOptionPane.showConfirmDialog(comp, panel,
					"Movie Preferences", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (n == 0) {
				if (panel.update()) {
					return panel;
				} else {
					JOptionPane.showMessageDialog(comp, "Invalid parameter.",
							"Run Parameter Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				return null;
			}
		}
	}

	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Gets the frame rate.
	 * 
	 * @return the frame rate
	 */
	public int getFrameRate() {
		return frameRate;
	}

	/**
	 * Gets the movie height.
	 * 
	 * @return the movie height
	 */
	public int getMovieHeight() {
		return movieHeight;
	}

	/**
	 * Gets the movie width.
	 * 
	 * @return the movie width
	 */
	public int getMovieWidth() {
		return movieWidth;
	}

	/**
	 * Sets the duration.
	 * 
	 * @param duration
	 *            the new duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * Sets the frame rate.
	 * 
	 * @param frameRate
	 *            the new frame rate
	 */
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	/**
	 * Sets the movie height.
	 * 
	 * @param movieHeight
	 *            the new movie height
	 */
	public void setMovieHeight(int movieHeight) {
		this.movieHeight = movieHeight;
	}

	/**
	 * Sets the movie width.
	 * 
	 * @param movieWidth
	 *            the new movie width
	 */
	public void setMovieWidth(int movieWidth) {
		this.movieWidth = movieWidth;
	}

	/**
	 * Update.
	 * 
	 * @return true, if successful
	 */
	protected boolean update() {
		duration = (int) Double
				.parseDouble(durationField.getValue().toString());
		movieWidth = (int) Double.parseDouble(widthField.getValue().toString());
		movieHeight = (int) Double.parseDouble(heightField.getValue()
				.toString());
		frameRate = (int) Double.parseDouble(frameRateField.getValue()
				.toString());
		return true;
	}

}
