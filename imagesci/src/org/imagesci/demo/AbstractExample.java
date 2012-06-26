/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
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
