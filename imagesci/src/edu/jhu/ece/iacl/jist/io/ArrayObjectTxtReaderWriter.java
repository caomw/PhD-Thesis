/**
 * ImageSci Toolkit
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

// TODO: Auto-generated Javadoc
/**
 * The Class ArrayObjectTxtReaderWriter.
 */
public class ArrayObjectTxtReaderWriter extends FileReaderWriter<Object[][]> {

	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/**
	 * Instantiates a new array object txt reader writer.
	 */
	public ArrayObjectTxtReaderWriter() {
		super(new FileExtensionFilter(new String[] { "csv", "txt" }));
	}

	/**
	 * Gets the single instance of ArrayObjectTxtReaderWriter.
	 * 
	 * @return single instance of ArrayObjectTxtReaderWriter
	 */
	public static ArrayObjectTxtReaderWriter getInstance() {
		return readerWriter;
	}

	/** The Constant readerWriter. */
	protected static final ArrayObjectTxtReaderWriter readerWriter = new ArrayObjectTxtReaderWriter();

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
	protected Object[][] readObject(File f) {
		BufferedReader in;
		if (f == null) {
			return null;
		}
		ArrayList<ArrayList> datArray = new ArrayList<ArrayList>();
		ArrayList array;
		try {
			CSVReader reader = new CSVReader(in = new BufferedReader(
					new InputStreamReader(new FileInputStream(f))));
			// Read file as string
			String[] strs;
			while ((strs = reader.readNext()) != null) {
				array = new ArrayList();
				for (String s : strs) {
					try {
						array.add(Integer.parseInt(s));
						continue;
					} catch (NumberFormatException e) {
					}
					try {
						array.add(Double.parseDouble(s));
						continue;
					} catch (NumberFormatException e) {
					}
					array.add(s);
				}
				datArray.add(array);
			}
			in.close();
			Object[][] dat = new Object[datArray.size()][0];
			for (int i = 0; i < datArray.size(); i++) {
				array = datArray.get(i);
				dat[i] = new Object[array.size()];
				for (int j = 0; j < array.size(); j++) {
					dat[i][j] = array.get(j);
				}
			}
			return dat;
		} catch (Exception e) {
			System.err.println(getClass().getCanonicalName()
					+ "Error occured while reading parameter file:\n"
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObject(java.lang.Object,
	 * java.io.File)
	 */
	@Override
	protected File writeObject(Object[][] dat, File f) {
		try {
			BufferedWriter data = new BufferedWriter(new FileWriter(f));
			String delim;
			for (int i = 0; i < dat.length; i++) {
				for (int j = 0; j < dat[i].length; j++) {
					delim = (j == dat[i].length - 1) ? "" : ",";
					if (dat[i][j] instanceof Number) {
						data.append(dat[i][j].toString() + delim);
					} else {
						data.append("\"" + dat[i][j].toString() + "\"" + delim);
					}
				}
				data.append("\n");
			}
			data.close();
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
			return null;
		}
	}

}
