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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class ArrayDoubleDxReaderWriter.
 */
public class ArrayDoubleDxReaderWriter extends ArrayDoubleReaderWriter {

	/** The numformat. */
	NumberFormat numformat;

	/**
	 * Instantiates a new array double dx reader writer.
	 */
	public ArrayDoubleDxReaderWriter() {
		super(new FileExtensionFilter(new String[] { "dx" }));
	}

	/**
	 * Gets the single instance of ArrayDoubleDxReaderWriter.
	 * 
	 * @return single instance of ArrayDoubleDxReaderWriter
	 */
	public static ArrayDoubleDxReaderWriter getInstance() {
		return readerWriter;
	}

	/** The Constant readerWriter. */
	protected static final ArrayDoubleDxReaderWriter readerWriter = new ArrayDoubleDxReaderWriter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.ArrayDoubleReaderWriter#setNumberFormat(java
	 * .text.NumberFormat)
	 */
	@Override
	public void setNumberFormat(NumberFormat numformat) {
		this.numformat = numformat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.ArrayDoubleReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected double[][] readObject(File f) {
		BufferedReader in;
		StringBuffer buff = new StringBuffer();
		try {
			// Create input stream from file
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));

			String str;
			// Read file as string
			while ((str = in.readLine()) != null) {
				buff.append(str + "\n");
			}
		} catch (Exception e) {
			System.err.println(getClass().getCanonicalName()
					+ "Error occured while reading parameter file:\n"
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		Pattern header = Pattern.compile("items\\s+\\d+\\s+data\\s+follows");
		Matcher m = header.matcher(buff);
		double[][] dat;
		int count = 0;
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length > 0) {
				try {
					count = Integer.parseInt(vals[vals.length - 1]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE DATA POINTS");
					return null;
				}
			}
			dat = new double[count][1];
			System.out.println("jist.io" + "\t" + "DATA POINTS " + count);
			String[] strs = buff.substring(m.end(), buff.length()).split(
					"\\s+", count + 2);

			for (int i = 1; i < strs.length - 1 && i <= dat.length; i++) {
				try {
					dat[i - 1][0] = Double.parseDouble(strs[i]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT DATA");
					return null;
				}
			}
			return dat;
		} else {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.ArrayDoubleReaderWriter#writeObject(double[][],
	 * java.io.File)
	 */
	@Override
	protected File writeObject(double[][] dat, File f) {

		if (dat.length == 0 || dat[0].length == 0) {
			return null;
		}
		if (numformat == null) {
			try {
				BufferedWriter data = new BufferedWriter(new FileWriter(f));
				data.append(String
						.format("object 1 class array type float rank 0 shape %d items %d data follows\n",
								dat[0].length, dat.length));
				for (int i = 0; i < dat.length; i++) {
					for (int j = 0; j < dat[i].length; j++) {
						data.append(dat[i][j] + " ");
					}
					data.append("\n");
				}
				data.close();
				return f;
			} catch (IOException e) {
				System.err.println(getClass().getCanonicalName()
						+ e.getMessage());
				return null;
			}
		} else {
			try {
				BufferedWriter data = new BufferedWriter(new FileWriter(f));
				data.append(String
						.format("object 1 class array type float rank 0 shape %d items %d data follows\n",
								dat[0].length, dat.length));
				for (int i = 0; i < dat.length; i++) {
					for (int j = 0; j < dat[i].length; j++) {
						data.append(numformat.format(dat[i][j]) + " ");
					}
					data.append("\n");
				}
				data.close();
				return f;
			} catch (IOException e) {
				System.err.println(getClass().getCanonicalName()
						+ e.getMessage());
				return null;
			}
		}
	}

}
