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
package org.imagesci.demo;

import java.io.File;
import java.net.URISyntaxException;

import data.PlaceHolder;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractExample.
 */
public abstract class AbstractExample implements Runnable {
	
	/** The default working directory. */
	public static File defaultWorkingDirectory=new File("./data/");
	
	/** The working directory. */
	protected File workingDirectory;
	/*
	static {
		try {
			defaultWorkingDirectory = new File(PlaceHolder.class.getResource("./").toURI());
		} catch (URISyntaxException e) {
			defaultWorkingDirectory = new File("./");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 */
	/**
	 * Sets the working directory.
	 *
	 * @param workingDirectory the new working directory
	 */
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public abstract String getName();

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		this.launch((workingDirectory == null) ? defaultWorkingDirectory
				: workingDirectory, new String[] {});
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * Launch.
	 *
	 * @param workingDirectory the working directory
	 * @param args the args
	 */
	public abstract void launch(File workingDirectory, String args[]);

	/**
	 * Launch.
	 *
	 * @param args the args
	 */
	public void launch(String[] args) {
		launch(AbstractExample.defaultWorkingDirectory, args);
	}

}
