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

// TODO: Auto-generated Javadoc
/**
 * The Class ArrayDoubleListTxtReaderWriter.
 */
public class ArrayDoubleListTxtReaderWriter extends
		FileReaderWriter<ArrayList<double[][]>> {


	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/**
	 * Instantiates a new array double list txt reader writer.
	 */
	public ArrayDoubleListTxtReaderWriter() {
		super(new FileExtensionFilter(new String[] { "txt", "csv", "xfm" }));
	}

	/**
	 * Gets the single instance of ArrayDoubleListTxtReaderWriter.
	 * 
	 * @return single instance of ArrayDoubleListTxtReaderWriter
	 */
	public static ArrayDoubleTxtReaderWriter getInstance() {
		return readerWriter;
	}
	/** The Constant readerWriter. */
	protected static final ArrayDoubleTxtReaderWriter readerWriter = new ArrayDoubleTxtReaderWriter();

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
	protected ArrayList<double[][]> readObject(File f) {
		ArrayList<double[][]> alltrans = new ArrayList<double[][]>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));

			String str;
			ArrayList<ArrayList<Double>> datArray = new ArrayList<ArrayList<Double>>();
			ArrayList<Double> array = new ArrayList<Double>();
			while ((str = in.readLine()) != null) {
				// Read file as string
				if (!str.contains("*****")) {
					String[] strs = str.split(" ");
					for (String s : strs) {
						try {
							array.add(Double.parseDouble(s));
						} catch (NumberFormatException e) {
						}
					}
					datArray.add((ArrayList<Double>) array.clone());
					array.clear();
				} else {
					double[][] dat = new double[datArray.get(0).size()][datArray
							.size()];
					for (int i = 0; i < datArray.size(); i++) {
						array = datArray.get(i);
						for (int j = 0; j < array.size(); j++) {
							dat[i][j] = array.get(j);
						}
					}
					alltrans.add(dat);
					datArray.clear();
					array.clear();
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return alltrans;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObject(java.lang.Object,
	 * java.io.File)
	 */
	@Override
	protected File writeObject(ArrayList<double[][]> dat, File f) {
		String alltrans = "";
		int k = 1;
		for (double[][] a : dat) {
			for (int i = 0; i < a.length; i++) {
				for (int j = 0; j < a[i].length; j++) {
					alltrans = alltrans + a[i][j] + " ";
				}
				alltrans = alltrans + "\n";
			}
			alltrans = alltrans + "***** Matrix " + k + "*****\n";
			k++;
		}
		try {
			BufferedWriter data = new BufferedWriter(new FileWriter(f));
			data.append(alltrans);
			data.close();
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
			return null;
		}

	}

}
