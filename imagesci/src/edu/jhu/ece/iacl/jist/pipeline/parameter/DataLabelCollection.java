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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;

// TODO: Auto-generated Javadoc
/**
 * Collection data labels.
 * 
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public class DataLabelCollection extends ArrayList<DataLabel> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3203176698544823885L;

	/** The file. */
	private File file;

	/**
	 * Read labels from XML file.
	 * 
	 * @param f
	 *            XML file
	 * @return label collection
	 */
	public static DataLabelCollection read(File f) {
		BufferedReader in;
		XStream stream = new XStream();
		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String text;
			StringBuffer buff = new StringBuffer();
			String str;
			while ((str = in.readLine()) != null) {
				buff.append(str + "\n");
			}
			text = buff.toString();
			Object o = stream.fromXML(text);
			if (o instanceof DataLabelCollection) {
				return (DataLabelCollection) o;
			} else {
				return null;
			}
		} catch (IOException e) {
			System.err.println("jist.base" + e.getMessage());
			return null;
		}
	}

	/**
	 * Get location of XML file.
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Set location of XML file.
	 * 
	 * @param file
	 *            the file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Write labels to XML file.
	 * 
	 * @param f
	 *            XML file
	 * @return true if successful
	 */
	public boolean write(File f) {
		PrintWriter out;
		XStream stream = new XStream();
		try {
			// System.out.println(getClass().getCanonicalName()+"\t"+"Writing "+f);
			out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			String text = stream.toXML(this);
			// System.out.println(getClass().getCanonicalName()+"\t"+text);
			out.print(text);
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			// System.err.println("jist.base"+e.getMessage());
			return false;
		}
	}
}
