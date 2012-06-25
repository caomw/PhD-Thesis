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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

// TODO: Auto-generated Javadoc
/**
 * The Class ArrayDoubleMtxReaderWriter.
 */
public class ArrayDoubleMtxReaderWriter extends ArrayDoubleReaderWriter {


	/**
	 * Instantiates a new array double mtx reader writer.
	 */
	public ArrayDoubleMtxReaderWriter() {
		super(new FileExtensionFilter(new String[] { "mtx" }));
	}

	/**
	 * Gets the single instance of ArrayDoubleMtxReaderWriter.
	 * 
	 * @return single instance of ArrayDoubleMtxReaderWriter
	 */
	public static ArrayDoubleMtxReaderWriter getInstance() {
		return readerWriter;
	}
	/** The Constant readerWriter. */
	protected static final ArrayDoubleMtxReaderWriter readerWriter = new ArrayDoubleMtxReaderWriter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.ArrayDoubleReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected double[][] readObject(File f) {
		BufferedReader in;
		// ArrayList<ArrayList<Double>> datArray=new
		// ArrayList<ArrayList<Double>>();
		// ArrayList<Double> array;
		try {
			// Create input stream from file
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String str;
			// Read file as string
			int rows = Integer.parseInt(in.readLine());
			int cols = Integer.parseInt(in.readLine());
			double[][] dat = new double[rows][cols];
			int i = 0;
			while ((str = in.readLine()) != null && i < rows) {
				// array=new ArrayList<Double>();
				String[] strs = str.split(" ");
				int j = 0;
				for (String s : strs) {
					try {
						// array.add(Double.parseDouble(s));
						dat[i][j] = Double.parseDouble(s);
					} catch (NumberFormatException e) {
					}
					j++;
				}
				// datArray.add(array);
				i++;
			}
			in.close();
			// double[][] dat=new double[datArray.size()][0];
			// for(int i=0;i<datArray.size();i++){
			// array=datArray.get(i);
			// dat[i]=new double[array.size()];
			// for(int j=0;j<array.size();j++){
			// dat[i][j]=array.get(j);
			// }
			// }
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
			data.append(dat.length + "\n");
			data.append(dat[0].length + "\n");
			for (int i = 0; i < dat.length; i++) {
				for (int j = 0; j < dat[i].length; j++) {
					data.append(dat[i][j] + " ");
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
