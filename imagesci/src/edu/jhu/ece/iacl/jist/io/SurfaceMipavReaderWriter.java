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
import java.util.StringTokenizer;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.geom.NormalGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceMipavReaderWriter.
 */
public class SurfaceMipavReaderWriter extends SurfaceReaderWriter {

	/** The Constant readerWriter. */
	protected static final SurfaceMipavReaderWriter readerWriter = new SurfaceMipavReaderWriter();

	/** The index close tag. */
	int indexCloseTag;

	/** The index count. */
	int indexCount;

	/** The index open tag. */
	int indexOpenTag;

	/** The vertex close tag. */
	int vertexCloseTag;

	/** The vertex count. */
	int vertexCount;

	/** The vertex open tag. */
	int vertexOpenTag;

	/**
	 * Instantiates a new surface mipav reader writer.
	 */
	public SurfaceMipavReaderWriter() {
		super(new FileExtensionFilter(new String[] { "xml" }));
	}

	/**
	 * Gets the single instance of SurfaceMipavReaderWriter.
	 * 
	 * @return single instance of SurfaceMipavReaderWriter
	 */
	public static SurfaceMipavReaderWriter getInstance() {
		return readerWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected EmbeddedSurface readObject(File f) {
		BufferedReader in;
		ArrayList<String> str = new ArrayList<String>();
		try {
			// Create input stream from file
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));

			String line;
			// Read file and store the string in a array
			while ((line = in.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line,
						" ,\t,\n,\r,<,>");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					// System.out.println("jist.io"+"\t"+token);
					str.add(token);
				}
			}

		} catch (Exception e) {
			System.err.println(getClass().getCanonicalName()
					+ "Error occured while reading parameter file:\n"
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
		// determine the location of XML tags for vertices and faces
		for (int index = 0; index < str.size(); index++) {
			String arrayListData = str.get(index);
			// System.out.println("jist.io"+"\t"+arrayListData);
			if (arrayListData.equalsIgnoreCase("Vertices")) {
				vertexOpenTag = index;
				// System.out.println("jist.io"+"\t"+vertexOpenTag);
			} else if (arrayListData.equalsIgnoreCase("/Vertices")) {
				vertexCloseTag = index;
				// System.out.println("jist.io"+"\t"+vertexCloseTag);
			} else if (arrayListData.equalsIgnoreCase("Connectivity")) {
				indexOpenTag = index;
				// System.out.println("jist.io"+"\t"+indexOpenTag);
			} else if (arrayListData.equalsIgnoreCase("/Connectivity")) {
				indexCloseTag = index;
				// System.out.println("jist.io"+"\t"+indexCloseTag);
			} else {
				System.out.print("");
			}
		}
		// get vertices
		Point3f[] points;
		int vertexBegin = vertexOpenTag + 1;
		int vertexEnd = vertexCloseTag - 1;
		if ((vertexEnd - vertexBegin) > 1) {
			try {
				vertexCount = ((vertexEnd - vertexBegin + 1) / 3);
			} catch (NumberFormatException e) {
				System.err.println(getClass().getCanonicalName()
						+ "CANNOT DETERMINE VERTEX COUNT");
				return null;
			}
		}
		System.out.println("jist.io" + "\t" + "VERTS " + vertexCount);
		String[] vertexStrs = new String[vertexCount * 3];
		int vertexCounter = 0;
		for (int i = vertexBegin; i <= vertexEnd; i++) {
			vertexStrs[vertexCounter] = str.get(i);
			vertexCounter++;
		}
		// System.out.println("jist.io"+"\t"+vertexStrs[0]);
		// System.out.println("jist.io"+"\t"+vertexStrs[1]);
		// System.out.println("jist.io"+"\t"+vertexStrs[2]);
		// System.out.println("jist.io"+"\t"+vertexStrs[vertexStrs.length-3]);
		// System.out.println("jist.io"+"\t"+vertexStrs[vertexStrs.length-2]);
		// System.out.println("jist.io"+"\t"+vertexStrs[vertexStrs.length-1]);
		points = new Point3f[vertexCount];
		for (int i = 0; i < vertexStrs.length; i += 3) {
			try {
				Point3f p = new Point3f();
				p.x = Float.parseFloat(vertexStrs[i]);
				p.y = Float.parseFloat(vertexStrs[i + 1]);
				p.z = Float.parseFloat(vertexStrs[i + 2]);
				points[i / 3] = p;
				// System.out.println("jist.io"+"\t"+i/3+")"+p);
			} catch (NumberFormatException e) {
				System.err.println(getClass().getCanonicalName()
						+ "CANNOT FORMAT VERTS");
				return null;
			}
		}
		// get indices
		int[] indices;
		int indexBegin = indexOpenTag + 1;
		int indexEnd = indexCloseTag - 1;
		if ((indexEnd - indexBegin > 1)) {
			try {
				indexCount = (indexEnd - indexBegin + 1);
			} catch (NumberFormatException e) {
				System.err.println(getClass().getCanonicalName()
						+ "CANNOT DETERMINE INDEX COUNT");
				return null;
			}
		}
		System.out.println("jist.io" + "\t" + "INDICES " + indexCount);
		String[] indexStrs = new String[indexCount];
		int indexCounter = 0;
		for (int i = indexBegin; i <= indexEnd; i++) {
			indexStrs[indexCounter] = str.get(i);
			indexCounter++;
		}
		// System.out.println("jist.io"+"\t"+indexStrs[0]);
		// System.out.println("jist.io"+"\t"+indexStrs[1]);
		// System.out.println("jist.io"+"\t"+indexStrs[2]);
		// System.out.println("jist.io"+"\t"+indexStrs[indexStrs.length-3]);
		// System.out.println("jist.io"+"\t"+indexStrs[indexStrs.length-2]);
		// System.out.println("jist.io"+"\t"+indexStrs[indexStrs.length-1]);
		indices = new int[indexCount];
		for (int i = 0; i < indexStrs.length; i += 3) {
			try {
				indices[i] = Integer.parseInt(indexStrs[i + 2]);
				indices[i + 1] = Integer.parseInt(indexStrs[i + 1]);
				indices[i + 2] = Integer.parseInt(indexStrs[i]);
			} catch (NumberFormatException e) {
				System.err.println(getClass().getCanonicalName()
						+ "CANNOT FORMAT INDICES");
				return null;
			}
		}

		EmbeddedSurface surf = new EmbeddedSurface(points,
				NormalGenerator.generate(points, indices), indices);
		surf.setName(FileReaderWriter.getFileName(f));
		return surf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter#writeObject(edu.jhu.ece.
	 * iacl.jist.structures.geom.EmbeddedSurface, java.io.File)
	 */
	@Override
	protected File writeObject(EmbeddedSurface mesh, File f) {
		try {
			BufferedWriter stream = new BufferedWriter(new FileWriter(f));
			int[] indices = new int[mesh.getIndexCount()];
			Point3d[] points = new Point3d[mesh.getVertexCount()];
			for (int i = 0; i < points.length; i++) {
				points[i] = new Point3d();
				mesh.getCoordinate(i, points[i]);
			}
			mesh.getCoordinateIndices(0, indices);

			stream.append(String
					.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"));
			stream.append(String.format("<!-- MIPAV header file -->\n"));
			stream.append(String
					.format("<Surface xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"));
			stream.append(String.format("\t<Unique-ID>22</Unique-ID>\n"));
			stream.append(String.format("\t<Material>\n"));
			stream.append(String.format("\t\t<Ambient>0.0 0.0 0.5</Ambient>\n"));
			stream.append(String.format("\t\t<Diffuse>0.0 0.0 0.5</Diffuse>\n"));
			stream.append(String
					.format("\t\t<Emissive>0.0 0.0 0.0</Emissive>\n"));
			stream.append(String
					.format("\t\t<Specular>0.0 0.0 0.0</Specular>\n"));
			stream.append(String.format("\t\t<Shininess>64.0</Shininess>\n"));
			stream.append(String.format("\t</Material>\n"));
			stream.append(String.format("\t<Type>TMesh</Type>\n"));
			stream.append(String.format("\t<Opacity>0.0</Opacity>\n"));
			stream.append(String.format("\t<LevelDetail>100</LevelDetail>\n"));
			stream.append(String.format("\t<Mesh>\n"));
			stream.append(String.format("\t\t\t<Vertices>"));

			Point3d p = new Point3d();
			for (int i = 0; i < points.length - 1; i++) {
				p = points[i];
				stream.append(String.format("%f %f %f ", p.x, p.y, p.z));
			}
			p = points[points.length - 1];
			stream.append(String.format("%f %f %f", p.x, p.y, p.z));
			stream.append(String.format("</Vertices>\n"));
			stream.append(String.format("\t\t\t<Connectivity>"));

			for (int i = 0; i < indices.length - 1; i++) {
				stream.append(String.format("%d ", indices[i]));
			}
			stream.append(String.format("%d", indices[indices.length - 1]));
			stream.append(String.format("</Connectivity>\n"));
			stream.append(String.format("\t</Mesh>\n"));
			stream.append(String.format("</Surface>\n"));

			stream.close();
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
		}
		return null;
	}
}
