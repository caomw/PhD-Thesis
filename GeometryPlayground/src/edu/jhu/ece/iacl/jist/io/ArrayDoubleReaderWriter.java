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
package edu.jhu.ece.iacl.jist.io;

import java.io.File;
import java.text.NumberFormat;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class ArrayDoubleReaderWriter.
 */
public class ArrayDoubleReaderWriter extends FileReaderWriter<double[][]> {

	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/** The numformat. */
	private NumberFormat numformat;

	/**
	 * Instantiates a new array double reader writer.
	 */
	public ArrayDoubleReaderWriter() {
		super(new FileExtensionFilter());
		Vector<String> exts = new Vector<String>();
		for (ArrayDoubleReaderWriter reader : arrayReaderWriters) {
			exts.addAll(reader.extensionFilter.getExtensions());
		}
		extensionFilter.setExtensions(exts);
	}

	/** The Constant arrayReaderWriters. */
	private static final ArrayDoubleReaderWriter[] arrayReaderWriters = new ArrayDoubleReaderWriter[] {
			new ArrayDoubleTxtReaderWriter(), new ArrayDoubleDxReaderWriter() };

	/** The Constant readerWriter. */
	protected static final ArrayDoubleReaderWriter readerWriter = new ArrayDoubleReaderWriter();

	/**
	 * Instantiates a new array double reader writer.
	 * 
	 * @param filter
	 *            the filter
	 */
	public ArrayDoubleReaderWriter(FileExtensionFilter filter) {
		super(filter);
	}

	/**
	 * Gets the single instance of ArrayDoubleReaderWriter.
	 * 
	 * @return single instance of ArrayDoubleReaderWriter
	 */
	public static ArrayDoubleReaderWriter getInstance() {
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

	/**
	 * Sets the number format.
	 * 
	 * @param numformat
	 *            the new number format
	 */
	public void setNumberFormat(NumberFormat numformat) {
		this.numformat = numformat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected double[][] readObject(File f) {
		for (ArrayDoubleReaderWriter reader : arrayReaderWriters) {
			if (reader.accept(f)) {
				return reader.readObject(f);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObject(java.lang.Object,
	 * java.io.File)
	 */
	@Override
	protected File writeObject(double[][] obj, File f) {
		for (ArrayDoubleReaderWriter writer : arrayReaderWriters) {
			if (writer.accept(f)) {
				return writer.writeObject(obj, f);
			}
		}
		return null;
	}
}
