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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class ArrayDoubleTxtReaderWriter.
 */
public class ArrayDoubleTxtReaderWriter extends ArrayDoubleReaderWriter {

	/**
	 * Instantiates a new array double txt reader writer.
	 */
	public ArrayDoubleTxtReaderWriter() {
		super(new FileExtensionFilter(new String[] { "txt", "csv" }));
	}

	/**
	 * Gets the single instance of ArrayDoubleTxtReaderWriter.
	 * 
	 * @return single instance of ArrayDoubleTxtReaderWriter
	 */
	public static ArrayDoubleTxtReaderWriter getInstance() {
		return readerWriter;
	}

	/** The Constant readerWriter. */
	protected static final ArrayDoubleTxtReaderWriter readerWriter = new ArrayDoubleTxtReaderWriter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.ArrayDoubleReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected double[][] readObject(File f) {
		BufferedReader in;
		ArrayList<ArrayList<Double>> datArray = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> array;
		try {
			// Create input stream from file
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String str;
			// Read file as string
			while ((str = in.readLine()) != null) {
				array = new ArrayList<Double>();
				String[] strs = str.split(" ");
				for (String s : strs) {
					try {
						array.add(Double.parseDouble(s));
					} catch (NumberFormatException e) {
					}
				}
				datArray.add(array);
			}
			in.close();
			double[][] dat = new double[datArray.size()][0];
			for (int i = 0; i < datArray.size(); i++) {
				array = datArray.get(i);
				dat[i] = new double[array.size()];
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
	 * edu.jhu.ece.iacl.jist.io.ArrayDoubleReaderWriter#writeObject(double[][],
	 * java.io.File)
	 */
	@Override
	protected File writeObject(double[][] dat, File f) {
		try {
			BufferedWriter data = new BufferedWriter(new FileWriter(f));
			int n = dat.length;

			for (int i = 0; i < n; i++) {
				int m = dat[i].length;
				for (int j = 0; j < (m - 1); j++) {
					data.append(dat[i][j] + " ");
				}
				data.append(dat[i][(m - 1)] + "");
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
