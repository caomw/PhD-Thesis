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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.geom.NormalGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceFreeSurferReaderWriter.
 */
public class SurfaceFreeSurferReaderWriter extends SurfaceReaderWriter {

	/** The Constant readerWriter. */
	protected static final SurfaceFreeSurferReaderWriter readerWriter = new SurfaceFreeSurferReaderWriter();

	/**
	 * Instantiates a new surface free surfer reader writer.
	 */
	public SurfaceFreeSurferReaderWriter() {
		super(new FileExtensionFilter(new String[] { "asc" }));
	}

	/**
	 * Gets the single instance of SurfaceFreeSurferReaderWriter.
	 * 
	 * @return single instance of SurfaceFreeSurferReaderWriter
	 */
	public static SurfaceFreeSurferReaderWriter getInstance() {
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
		Pattern header = Pattern.compile("\\d+\\s+\\d+");
		Matcher m = header.matcher(buff);
		int vertexCount = 0;
		int indexCount = 0;
		Point3f[] points;
		int[] indices;
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\s+");
			if (vals.length == 2) {
				// System.out.println("jist.io"+"\t"+vals.length);
				try {
					vertexCount = Integer.parseInt(vals[0]);
					indexCount = Integer.parseInt(vals[1]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE VERTEX AND INDEX COUNT");
					return null;
				}
			}
			String[] strs = buff.substring(m.end(), buff.length())
					.split("\\s+");
			// System.out.println("jist.io"+"\t"+strs.length);
			// System.out.println("jist.io"+"\t"+strs[1]);
			// System.out.println("jist.io"+"\t"+strs[vertexCount*4+1]);
			// System.out.println("jist.io"+"\t"+strs[vertexCount*4+2]);
			// System.out.println("jist.io"+"\t"+strs[vertexCount*4+3]);
			points = new Point3f[vertexCount];
			System.out.println("jist.io" + "\t" + "VERTS " + vertexCount);
			for (int i = 1; i < ((vertexCount * 4) + 1); i += 4) {
				try {
					Point3f p = new Point3f();
					p.x = Float.parseFloat(strs[i]);
					p.y = Float.parseFloat(strs[i + 1]);
					p.z = Float.parseFloat(strs[i + 2]);
					points[(i - 1) / 4] = p;
					// System.out.println("jist.io"+"\t"+i/3+")"+p);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT VERTS");
					return null;
				}
			}
			int j = 0;
			indices = new int[indexCount * 3];
			System.out.println("jist.io" + "\t" + "INDICES " + indexCount);
			for (int i = ((vertexCount * 4) + 1); i < strs.length; i += 4) {
				try {
					indices[j] = Integer.parseInt(strs[i + 2]);
					j++;
					indices[j] = Integer.parseInt(strs[i + 1]);
					j++;
					indices[j] = Integer.parseInt(strs[i]);
					j++;
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT INDICES");
					return null;
				}
			}
			// System.out.println("jist.io"+"\t"+j);
			// System.out.println("jist.io"+"\t"+indexCount*3);
			// System.out.println("jist.io"+"\t"+indices[0]);
			// System.out.println("jist.io"+"\t"+indices[1]);
			// System.out.println("jist.io"+"\t"+indices[2]);

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
			int[] indices = new int[mesh.getIndexCount()];
			Point3d[] points = new Point3d[mesh.getVertexCount()];
			for (int i = 0; i < points.length; i++) {
				points[i] = new Point3d();
				mesh.getCoordinate(i, points[i]);
			}
			mesh.getCoordinateIndices(0, indices);
			int i;
			stream.append(String.format("#!ascii version of %s\n", f.getName()
					.toString()));
			stream.append(String.format("%d %d\n", points.length,
					indices.length / 3));
			for (i = 0; i < points.length; i++) {
				Point3d p = points[i];
				stream.append(String
						.format("%.6f %.6f %.6f 0\n", p.x, p.y, p.z));
			}
			for (i = 0; i < indices.length; i += 3) {
				stream.append(String.format("%d %d %d 0\n", indices[i],
						indices[i + 1], indices[i + 2]));
			}
			stream.close();
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
		}
		return null;
	}

}
