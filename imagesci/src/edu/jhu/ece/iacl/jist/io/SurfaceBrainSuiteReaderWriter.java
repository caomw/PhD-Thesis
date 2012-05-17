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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.geom.NormalGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceBrainSuiteReaderWriter.
 */
public class SurfaceBrainSuiteReaderWriter extends SurfaceReaderWriter {


	/**
	 * Instantiates a new surface brain suite reader writer.
	 */
	public SurfaceBrainSuiteReaderWriter() {
		super(new FileExtensionFilter(new String[] { "ascii" }));
	}

	/**
	 * Gets the single instance of SurfaceBrainSuiteReaderWriter.
	 * 
	 * @return single instance of SurfaceBrainSuiteReaderWriter
	 */
	public static SurfaceBrainSuiteReaderWriter getInstance() {
		return readerWriter;
	}
	/** The Constant readerWriter. */
	protected static final SurfaceBrainSuiteReaderWriter readerWriter = new SurfaceBrainSuiteReaderWriter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected EmbeddedSurface readObject(File f) {
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
		Pattern header = Pattern.compile("\\sNVERTICES\\D+\\d+\\s");
		Matcher m = header.matcher(buff);
		int vertexCount = 0;
		int indexCount = 0;
		Point3f[] points;
		int[] indices;
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length >= 2) {
				try {
					vertexCount = Integer.parseInt(vals[1]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE VERTEX COUNT");
					return null;
				}
			}
			points = new Point3f[vertexCount];
			System.out.println("jist.io" + "\t" + "VERTS " + vertexCount);
			header = Pattern.compile("\\sVERTICES\\s+");
			m = header.matcher(buff);
			if (m.find()) {
				String[] strs = buff.substring(m.end(), buff.length()).split(
						"\\s+", vertexCount * 3 + 1);
				for (int i = 0; i < vertexCount * 3; i += 3) {
					try {
						Point3f p = new Point3f();
						p.x = Float.parseFloat(strs[i]);
						p.y = Float.parseFloat(strs[i + 1]);
						p.z = Float.parseFloat(strs[i + 2]);
						points[i / 3] = p;
					} catch (NumberFormatException e) {
						System.err.println(getClass().getCanonicalName()
								+ "CANNOT FORMAT VERTS");
						return null;
					}
				}
			}
		} else {
			return null;
		}
		header = Pattern.compile("\\sNFACES\\D+\\d+\\s");
		m = header.matcher(buff);
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length >= 2) {
				try {
					indexCount = Integer.parseInt(vals[1]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE INDEX COUNT");
					return null;
				}
			}
			indices = new int[indexCount * 3];
			System.out.println("jist.io" + "\t" + "FACES " + indexCount);
			header = Pattern.compile("\\sFACES\\s+");
			m = header.matcher(buff);
			if (m.find()) {
				String[] strs = buff.substring(m.end(), buff.length()).split(
						"\\s+", indexCount * 3 + 1);
				for (int i = 0; i < indexCount * 3; i++) {
					try {
						indices[i] = Integer.parseInt(strs[i]);
					} catch (NumberFormatException e) {
						System.err.println(getClass().getCanonicalName()
								+ "CANNOT FORMAT INDICES");
						return null;
					}
				}
			}
		} else {
			return null;
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
			int pointCount = mesh.getVertexCount();
			int indexCount = mesh.getIndexCount();
			// Write header for vertex locations
			stream.append("BRAINSUITE_SURFACE_FILE\n" + "NVERTICES\n "
					+ pointCount + "\n" + "NFACES\n " + indexCount / 3 + "\n"
					+ "ORIENTATION\n" + "1 0 0 0\n" + "0 1 0 0\n" + "0 0 1 0\n"
					+ "0 0 0 1\n");
			Point3f p = new Point3f();
			String tmp;
			// Write vertex locations
			stream.append("VERTICES\n");
			for (int i = 0; i < pointCount; i++) {
				mesh.getCoordinate(i, p);
				tmp = String.format("%.5f %.5f %.5f\n", p.x, p.y, p.z);
				stream.append(tmp);
			}
			// Write triangle indexes

			stream.append("FACES\n");
			for (int i = 0; i < indexCount; i += 3) {
				stream.append(mesh.getCoordinateIndex(i) + " "
						+ mesh.getCoordinateIndex(i + 1) + " "
						+ mesh.getCoordinateIndex(i + 2) + "\n");
			}
			stream.append("VERTEXNORMALS\n");
			Vector3f v = new Vector3f();
			for (int i = 0; i < pointCount; i++) {
				mesh.getNormal(i, v);
				tmp = String.format("%.5f %.5f %.5f\n", v.x, v.y, v.z);
				stream.append(tmp);
			}
			stream.close();
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
		}
		return null;
	}

}
