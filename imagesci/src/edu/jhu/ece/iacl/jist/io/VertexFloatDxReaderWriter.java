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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point3f;

// TODO: Auto-generated Javadoc
/**
 * The Class VertexFloatDxReaderWriter.
 */
public class VertexFloatDxReaderWriter extends FileReaderWriter<Point3f[]> {

	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/**
	 * Instantiates a new vertex float dx reader writer.
	 */
	public VertexFloatDxReaderWriter() {
		super(new FileExtensionFilter(new String[] { "dx" }));
	}
	/** The Constant readerWriter. */
	protected static final VertexFloatDxReaderWriter readerWriter = new VertexFloatDxReaderWriter();

	/**
	 * Gets the single instance of VertexFloatDxReaderWriter.
	 * 
	 * @return single instance of VertexFloatDxReaderWriter
	 */
	public static VertexFloatDxReaderWriter getInstance() {
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
	protected Point3f[] readObject(File f) {
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
		Pattern header = Pattern
				.compile("rank\\s\\d+\\sshape\\s\\d+\\sitems\\s\\d+\\sdata\\sfollows");
		Matcher m = header.matcher(buff);
		int vertexCount = 0;
		Point3f[] points;
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length >= 4) {
				try {
					vertexCount = Integer.parseInt(vals[3]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE VERTEX COUNT");
					return null;
				}
			}
			points = new Point3f[vertexCount];
			System.out.println("jist.io" + "\t" + "VERTS " + vertexCount);
			String[] strs = buff.substring(m.end(), buff.length()).split(
					"\\s+", vertexCount * 3 + 2);

			for (int i = 1; i < strs.length - 1; i += 3) {
				try {
					Point3f p = new Point3f();
					p.x = Float.parseFloat(strs[i]);
					p.y = Float.parseFloat(strs[i + 1]);
					p.z = Float.parseFloat(strs[i + 2]);
					points[(i - 1) / 3] = p;
					// System.out.println("jist.io"+"\t"+i/3+")"+p);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT VERTS");
					return null;
				}
			}
		} else {
			System.err.println(getClass().getCanonicalName()
					+ "Cannot find header string");
			return null;
		}
		return points;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObject(java.lang.Object,
	 * java.io.File)
	 */
	@Override
	protected File writeObject(Point3f[] results, File f) {
		try {
			BufferedWriter stream = new BufferedWriter(new FileWriter(f));
			stream.append("object \"data\" class array type float rank 1 shape 3 items "
					+ results.length + " data follows\n");
			for (int i = 0; i < results.length; i++) {
				Point3f p = results[i];
				stream.append(String.format("%f %3f %3f\n", p.x, p.y, p.z));
			}
			stream.close();
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
		}
		return null;

	}

}
