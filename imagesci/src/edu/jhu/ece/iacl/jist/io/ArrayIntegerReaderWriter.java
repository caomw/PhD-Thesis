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
package edu.jhu.ece.iacl.jist.io;

import java.io.File;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class ArrayIntegerReaderWriter.
 */
public class ArrayIntegerReaderWriter extends FileReaderWriter<int[][]> {

	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/**
	 * Instantiates a new array integer reader writer.
	 */
	public ArrayIntegerReaderWriter() {
		super(new FileExtensionFilter());
		Vector<String> exts = new Vector<String>();
		for (ArrayIntegerReaderWriter reader : arrayReaderWriters) {
			exts.addAll(reader.extensionFilter.getExtensions());
		}
		extensionFilter.setExtensions(exts);
	}

	/** The Constant arrayReaderWriters. */
	private static final ArrayIntegerReaderWriter[] arrayReaderWriters = new ArrayIntegerReaderWriter[] { new ArrayIntegerTxtReaderWriter() };

	/** The Constant readerWriter. */
	protected static final ArrayIntegerReaderWriter readerWriter = new ArrayIntegerReaderWriter();

	/**
	 * Instantiates a new array integer reader writer.
	 * 
	 * @param filter
	 *            the filter
	 */
	public ArrayIntegerReaderWriter(FileExtensionFilter filter) {
		super(filter);
	}

	/**
	 * Gets the single instance of ArrayIntegerReaderWriter.
	 * 
	 * @return single instance of ArrayIntegerReaderWriter
	 */
	public static ArrayIntegerReaderWriter getInstance() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected int[][] readObject(File f) {
		for (ArrayIntegerReaderWriter reader : arrayReaderWriters) {
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
	protected File writeObject(int[][] obj, File f) {
		for (ArrayIntegerReaderWriter writer : arrayReaderWriters) {
			if (writer.accept(f)) {
				return writer.writeObject(obj, f);
			}
		}
		return null;
	}
}
