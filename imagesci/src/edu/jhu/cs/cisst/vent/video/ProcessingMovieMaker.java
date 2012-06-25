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
package edu.jhu.cs.cisst.vent.video;

import java.awt.Image;
import java.io.File;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.MovieMaker;
import edu.jhu.ece.iacl.jist.io.FileReaderWriter;

// TODO: Auto-generated Javadoc
/**
 * The Class ProcessingMovieMaker.
 */
public class ProcessingMovieMaker extends PApplet implements GenericMovieMaker {

	/**
	 * Instantiates a new processing movie maker.
	 */
	public ProcessingMovieMaker() {
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.video.GenericMovieMaker#save(java.io.File,
	 * java.awt.Image[], int, int, int)
	 */
	@Override
	public void save(File f, Image[] images, int frameRate, int width,
			int height) {
		for (int i = 0; i < images.length; i++) {
			PImage pimg = new PImage(images[i]);
			File imageFile = new File(f.getParent(), String.format(
					"%s_%04d.png", FileReaderWriter.getFileName(f), i));
			System.out.println("SAVING FRAME (" + (i + 1) + "/" + images.length
					+ ") " + imageFile);
			pimg.save(imageFile.getAbsolutePath());
		}
		try {
			MovieMaker movieMaker = new MovieMaker(this, width, height,
					f.getAbsolutePath(), frameRate);
			System.out.println("SAVING VIDEO Dimensions: [" + width + ","
					+ height + "] Frame Rate: " + frameRate + " Frame Count: "
					+ images.length);
			for (int i = 0; i < images.length; i++) {
				PImage pimg = new PImage(images[i]);
				pimg.resize(width, height);
				movieMaker.addFrame(pimg.pixels, pimg.width, pimg.height);
			}
			movieMaker.finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
