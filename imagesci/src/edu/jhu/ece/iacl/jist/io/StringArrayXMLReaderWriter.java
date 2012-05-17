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

import java.util.ArrayList;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class StringArrayXMLReaderWriter.
 */
public class StringArrayXMLReaderWriter {

	/** The Constant closeElement. */
	static final String closeElement = "</Element>\n";

	/** The Constant closeOption. */
	static final String closeOption = "</Option>\n";

	/** The Constant closeString. */
	static final String closeString = "</String>\n";

	/** The Constant closeStringLine. */
	static final String closeStringLine = "\t</StringLine>\n";

	/** The Constant openElement. */
	static final String openElement = "<Element>";

	/** The Constant openOption. */
	static final String openOption = "<Option>\t";

	/** The Constant openString. */
	static final String openString = "<String>";

	/** The Constant openStringLine. */
	static final String openStringLine = "\t<StringLine>\t";

	/** The Constant TAG_1. */
	static final String TAG_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	/** The Constant TAG_1a. */
	static final String TAG_1a = "<!-- CATNAP Parameters -->";

	/** The Constant TAG_2. */
	static final String TAG_2 = "<Surface xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";

	/**
	 * Instantiates a new string array xml reader writer.
	 */
	public StringArrayXMLReaderWriter() {
	}

	/**
	 * Write strings.
	 * 
	 * @param s
	 *            the s
	 * 
	 * @return the string
	 */
	public String writeStrings(String[] s) {
		String out = "";
		for (String el : s) {
			out = out + openElement + "\n";
			out = out + writeString(el);
			out = out + closeElement;
		}
		return out;
	}

	/**
	 * Write string.
	 * 
	 * @param s
	 *            the s
	 * 
	 * @return the string
	 */
	public String writeString(String s) {
		String out = "";
		if (s.contains("\n")) {
			out = out + openString + "\n";
			StringTokenizer tokenizer = new StringTokenizer(s, "\n");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				// System.out.println("jist.io"+"\t"+token);
				out = out + openStringLine + token + closeStringLine;
			}
			out = out + closeString;
		} else {
			out = out + openString;
			out = out + "\t" + s + "\t" + closeString;
		}
		return out;
	}

	/**
	 * Parses the vector.
	 * 
	 * @param svec
	 *            the svec
	 * 
	 * @return the double[]
	 */
	public double[] parseVector(String svec) {
		StringTokenizer vectokenizer = new StringTokenizer(svec, "\t");
		double[] vec = new double[vectokenizer.countTokens()];
		int i = 0;
		while (vectokenizer.hasMoreTokens()) {
			vec[i] = Double.parseDouble(vectokenizer.nextToken());
			i++;
		}
		return vec;
	}

	/**
	 * Read strings.
	 * 
	 * @param xml
	 *            the xml
	 * 
	 * @return the array list< string>
	 */
	public ArrayList<String> readStrings(String xml) {
		ArrayList<String> output = new ArrayList<String>();
		if (xml.contains("<Element>")) {
			StringTokenizer elemtokenizer = new StringTokenizer(xml,
					"\t,<,>,\n, ");
			// boolean inelem = false;
			boolean instr = false;
			boolean inln = false;
			String addme = "";
			while (elemtokenizer.hasMoreTokens()) {
				String token = elemtokenizer.nextToken();
				if (token.equals("Element")) {

				} else if (token.equals("/Element")) {
					output.add(addme);
					addme = "";
				} else if (token.equals("String")) {
					instr = true;
				} else if (token.equals("/String")) {
					instr = false;
				} else if (token.equals("StringLine") && instr) {
					inln = true;
				} else if (token.equals("/StringLine") && instr) {
					inln = false;
				} else {
					// System.out.println("jist.io"+"\t"+token);
					if (instr && !inln) {
						if (addme.length() == 0) {
							addme = token;
						} else {
							addme = addme + "\t" + token;
						}
					} else if (instr && inln) {
						addme = addme + token + "\n";
					}
				}
			}
		} else if (xml.contains("<String>")) {
			StringTokenizer elemtokenizer = new StringTokenizer(xml,
					"\t,<,>,\n");
			boolean instr = false;
			boolean inln = false;
			String addme = "";
			while (elemtokenizer.hasMoreTokens()) {
				String token = elemtokenizer.nextToken();
				if (token.equals("String")) {
					instr = true;
				} else if (token.equals("/String")) {
					instr = false;
					output.add(addme);
					addme = "";
				} else if (token.equals("StringLine") && instr) {
					inln = true;
				} else if (token.equals("/StringLine") && instr) {
					inln = false;
				} else {
					if (instr && !inln) {
						addme = token;
					} else if (instr && inln) {
						addme = addme + token + "\n";
					}
				}
			}
		} else {
			output = new ArrayList<String>();
			output.add(xml);
		}

		return output;
	}

	/**
	 * Vector to string.
	 * 
	 * @param vec
	 *            the vec
	 * 
	 * @return the string
	 */
	public String vectorToString(double[] vec) {
		String out = "";
		for (double d : vec) {
			out = out + "\t" + d;
		}
		return out;
	}

}
