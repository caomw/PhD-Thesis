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
package edu.jhu.ece.iacl.jist.io;

import java.awt.Color;
import java.io.File;

import processing.core.PApplet;
import processing.core.PImage;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataColor;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.VoxelType;

// TODO: Auto-generated Javadoc
/**
 * The Class PImageReaderWriter.
 */
public class PImageReaderWriter extends FileReaderWriter<PImage> {
	
	/** The Constant applet. */
	final static PApplet applet = new PApplet();

	/** The Constant readerWriter. */
	private static final PImageReaderWriter readerWriter = new PImageReaderWriter();

	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/**
	 * Instantiates a new p image reader writer.
	 */
	public PImageReaderWriter() {
		super(new FileExtensionFilter(new String[] { "png", "tif" }));
	}

	/**
	 * Convert to gray.
	 *
	 * @param img the img
	 * @return the image data float
	 */
	public static ImageDataFloat convertToGray(PImage img) {
		ImageDataFloat gimg = new ImageDataFloat(img.width, img.height);
		float[][] lum = gimg.toArray2d();
		int index = 0;
		for (int j = 0; j < img.height; j++) {
			for (int i = 0; i < img.width; i++) {
				Color c = new Color(img.pixels[index++]);
				lum[i][j] = (c.getRed() + c.getGreen() + c.getBlue()) / 3.0f;
			}
		}
		return gimg;
	}

	/**
	 * Convert to rgb.
	 *
	 * @param img the img
	 * @return the image data color
	 */
	public static ImageDataColor convertToRGB(PImage img) {
		ImageDataColor gimg = new ImageDataColor(VoxelType.COLOR, img.width,
				img.height);
		Color[][] lum = gimg.toArray2d();
		int index = 0;
		for (int j = 0; j < img.height; j++) {
			for (int i = 0; i < img.width; i++) {
				Color c = new Color(img.pixels[index++]);
				lum[i][j] = c;
			}
		}
		return gimg;
	}

	/**
	 * Gets the single instance of PImageReaderWriter.
	 *
	 * @return single instance of PImageReaderWriter
	 */
	public static PImageReaderWriter getInstance() {
		return readerWriter;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#getExtensionFilter()
	 */
	@Override
	public FileExtensionFilter getExtensionFilter() {
		return extensionFilter;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#setExtensionFilter(edu.jhu.ece.iacl.jist.io.FileExtensionFilter)
	 */
	@Override
	public void setExtensionFilter(FileExtensionFilter extensionFilter) {
		this.extensionFilter = extensionFilter;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected PImage readObject(File f) {
		PImage img = applet.loadImage(f.getAbsolutePath());
		return img;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObject(java.lang.Object, java.io.File)
	 */
	@Override
	protected File writeObject(PImage img, File f) {
		try {
			if (img.parent == null) {
				img.parent = applet;
			}
			img.save(f.getAbsolutePath());
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
