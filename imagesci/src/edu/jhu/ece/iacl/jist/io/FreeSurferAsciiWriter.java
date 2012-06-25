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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// TODO: Auto-generated Javadoc
/**
 * The Class FreeSurferAsciiWriter.
 */
public class FreeSurferAsciiWriter {

	/** The file name. */
	static String fileName;

	/** The num of faces. */
	static int numOfFaces;

	/** The num of vertices. */
	static int numOfVertices;

	/** The output. */
	static PrintWriter output;

	/** The write. */
	static FileWriter write;

	/**
	 * From dx writer.
	 * 
	 * @param vertices
	 *            the vertices
	 * @param faces
	 *            the faces
	 * @param f
	 *            the f
	 * @param X
	 *            the x
	 * @param Y
	 *            the y
	 * @param Z
	 *            the z
	 */
	public static void fromDxWriter(String[] vertices, String[] faces, File f,
			float X, float Y, float Z) {
		try {
			write = new FileWriter(f);
			PrintWriter output = new PrintWriter(write);

			fileName = f.getName();
			output.println("#!ascii version of " + fileName);

			numOfVertices = vertices.length / 3;
			System.out.println("jist.io" + "\t" + numOfVertices); // Checking
																	// numOfVertices
			numOfFaces = faces.length / 3;
			System.out.println("jist.io" + "\t" + numOfFaces); // Checking
																// numOfVertices
			output.println(numOfVertices + " " + numOfFaces);

			int verticesCounter = 1;
			for (int i = 0; i < vertices.length; i++) {
				if (verticesCounter < 4) {

					String verticesData;
					if (verticesCounter == 1) {
						float vertex_x = Float.parseFloat(vertices[i]);
						vertex_x = vertex_x - (X / 2);
						verticesData = String.valueOf(vertex_x);
						output.print(verticesData + " ");
						verticesCounter++;
					} else if (verticesCounter == 2) {
						float vertex_y = Float.parseFloat(vertices[i]);
						vertex_y = vertex_y - (Y / 2);
						verticesData = String.valueOf(vertex_y);
						output.print(verticesData + " ");
						verticesCounter++;
					} else {
						float vertex_z = Float.parseFloat(vertices[i]);
						vertex_z = vertex_z - (Z / 2);
						verticesData = String.valueOf(vertex_z);
						output.print(verticesData + " ");
						output.println("0");
						verticesCounter = 1;
					}
				}
			}

			int facesCounter = 1;
			for (int i = 0; i < faces.length; i++) {
				output.print(faces[i]);
				output.print("  ");
				facesCounter++;
				if (facesCounter == 4) {
					output.println("0");
					facesCounter = 1;
				}
			}

			output.close();
			write.close();
		} catch (FileNotFoundException e) {
			System.out.println("jist.io" + "\t" + e.getMessage());
		} catch (IOException e) {
			System.out.println("jist.io" + "\t" + e.getMessage());
		}
		return;
	}

	/**
	 * From mipav writer.
	 * 
	 * @param vertices
	 *            the vertices
	 * @param faces
	 *            the faces
	 * @param f
	 *            the f
	 * @param X
	 *            the x
	 * @param Y
	 *            the y
	 * @param Z
	 *            the z
	 */
	public static void fromMipavWriter(String[] vertices, String[] faces,
			File f, float X, float Y, float Z) {
		try {
			write = new FileWriter(f);
			PrintWriter output = new PrintWriter(write);

			fileName = f.getName();
			output.println("#!ascii version of " + fileName);

			numOfVertices = vertices.length / 3;
			System.out.println("jist.io" + "\t" + numOfVertices); // Checking
																	// numOfVertices
			numOfFaces = faces.length / 3;
			System.out.println("jist.io" + "\t" + numOfFaces); // Checking
																// numOfVertices
			output.println(numOfVertices + " " + numOfFaces);

			int verticesCounter = 1;
			for (int i = 0; i < vertices.length; i++) {

				String verticesData;
				if (verticesCounter == 1) {
					float vertex_x = Float.parseFloat(vertices[i]);
					vertex_x = vertex_x * (X / 2);
					verticesData = String.valueOf(vertex_x);
					output.print(verticesData + " ");
					verticesCounter++;
				} else if (verticesCounter == 2) {
					float vertex_y = Float.parseFloat(vertices[i]);
					vertex_y = vertex_y * (Y / 2);
					verticesData = String.valueOf(vertex_y);
					output.print(verticesData + " ");
					verticesCounter++;
				} else {
					float vertex_z = Float.parseFloat(vertices[i]);
					vertex_z = vertex_z * (Z / 2);
					verticesData = String.valueOf(vertex_z);
					output.print(verticesData + " ");
					output.println("0");
					verticesCounter = 1;
				}
			}

			int facesCounter = 1;
			for (int i = 0; i < faces.length; i++) {
				output.print(faces[i]);
				output.print("  ");
				facesCounter++;
				if (facesCounter == 4) {
					output.println("0");
					facesCounter = 1;
				}
			}

			output.close();
			write.close();
		} catch (FileNotFoundException e) {
			System.out.println("jist.io" + "\t" + e.getMessage());
		} catch (IOException e) {
			System.out.println("jist.io" + "\t" + e.getMessage());
		}
		return;
	}
}