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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class FreeSurferAsciiReader.
 */
public class FreeSurferAsciiReader {

	/** The vertices end index. */
	static int verticesEndIndex;

	/**
	 * Gets the faces.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the faces
	 */
	public static String[] getFaces(File f) {
		ArrayList surfaceData = readSurfaceFile(f);
		int numOfFaces = Integer.parseInt((String) surfaceData.get(5));
		int facesStartIndex = verticesEndIndex + 1;
		int facesEndIndex = (facesStartIndex + (numOfFaces * 4) - 1);
		String[] faces = new String[numOfFaces * 4];
		int facesCounter = 0;
		for (int i = facesStartIndex; i <= facesEndIndex; i++) {
			faces[facesCounter] = (String) surfaceData.get(i);
			facesCounter++;
		}
		return faces;
	}

	/**
	 * Gets the vertices.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the vertices
	 */
	public static String[] getVertices(File f) {
		ArrayList surfaceData = readSurfaceFile(f);
		int numOfVertices = Integer.parseInt((String) surfaceData.get(4));
		int verticesStartIndex = 6;
		verticesEndIndex = (verticesStartIndex + (numOfVertices * 4) - 1);
		String[] vertices = new String[numOfVertices * 4];
		int verticesCounter = 0;
		for (int i = verticesStartIndex; i <= verticesEndIndex; i++) {
			vertices[verticesCounter] = (String) surfaceData.get(i);
			verticesCounter++;
		}
		return vertices;
	}

	/**
	 * Read surface file.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the array list
	 */
	public static ArrayList readSurfaceFile(File f) {
		ArrayList<String> str = new ArrayList<String>();
		try {
			// Create input stream from file
			FileReader reader = new FileReader(f);
			BufferedReader in = new BufferedReader(reader);
			String line;
			// Read file and store the string in a array
			while ((line = in.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					str.add(token);
				}
			}
			in.close();
			reader.close();
		} catch (IOException e) {
			System.out.println("jist.io" + "\t"
					+ "Error occured while reading parameter file:\n"
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		return str;
	}

}

// SAMPLE FREESURFER ASCII FILE FORMAT
/*
 * #!ascii version of rh.white 144081 288158 // vertices and Faces (indices 4 &
 * 5 of ArrayList str) 16.091122 -102.305519 4.174082 0 15.775849 -102.404739
 * 3.938211 0 14.936373 -102.353081 3.933048 0 14.253302 -102.252464 3.901379 0
 * ..............Truncated............ 0 1 5 0 6 5 1 0 0 98 99 0 0 99 1 0 0 5 98
 * 0 107 98 5 0 1 2 6 0 7 6 2 0 ....Truncated......
 */