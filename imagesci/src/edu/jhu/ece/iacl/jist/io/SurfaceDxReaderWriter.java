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

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.geom.NormalGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceDxReaderWriter.
 */
public class SurfaceDxReaderWriter extends SurfaceReaderWriter {


	/**
	 * Instantiates a new surface dx reader writer.
	 */
	public SurfaceDxReaderWriter() {
		super(new FileExtensionFilter(new String[] { "dx" }));
	}
	/** The Constant readerWriter. */
	protected static final SurfaceDxReaderWriter readerWriter = new SurfaceDxReaderWriter();

	/**
	 * Gets the single instance of SurfaceDxReaderWriter.
	 * 
	 * @return single instance of SurfaceDxReaderWriter
	 */
	public static SurfaceDxReaderWriter getInstance() {
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
		Pattern header = Pattern
				.compile("rank\\s\\d+\\sshape\\s\\d+\\sitems\\s\\d+\\sdata\\sfollows");
		Matcher m = header.matcher(buff);
		int vertexCount = 0;
		int indexCount = 0;
		Point3f[] points;
		int[] indices;
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
			return null;
		}
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length > 0) {
				try {
					indexCount = Integer.parseInt(vals[3]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE INDEX COUNT");
					return null;
				}
			}
			indices = new int[indexCount * 3];
			System.out.println("jist.io" + "\t" + "INDICES " + indexCount);
			String[] strs = buff.substring(m.end(), buff.length()).split(
					"\\s+", indexCount * 3 + 2);
			for (int i = 1; i < strs.length - 1; i += 3) {
				try {
					indices[i - 1] = Integer.parseInt(strs[i + 2]);
					indices[i] = Integer.parseInt(strs[i + 1]);
					indices[i + 1] = Integer.parseInt(strs[i]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT INDICES");
					return null;
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
			int[] indices = new int[mesh.getIndexCount()];
			Point3d[] points = new Point3d[mesh.getVertexCount()];
			for (int i = 0; i < points.length; i++) {
				points[i] = new Point3d();
				mesh.getCoordinate(i, points[i]);
			}
			mesh.getCoordinateIndices(0, indices);
			int i;
			stream.append(String
					.format("object 1 class array type float rank 1 shape 3 items %d data follows\n",
							points.length));
			for (i = 0; i < points.length; i++) {
				Point3d p = points[i];
				stream.append(String.format("%9.4f %9.4f %9.4f\n", p.x, p.y,
						p.z));
			}
			stream.append(String.format("\n"));
			stream.append(String
					.format("attribute \"dep\" string \"positions\"\n"));

			stream.append(String
					.format("object 2 class array type int rank 1 shape 3 items %d data follows\n",
							indices.length / 3));

			for (i = 0; i < indices.length; i += 3) {
				stream.append(String.format("%9d %9d %9d\n", indices[i],
						indices[i + 1], indices[i + 2]));
			}

			stream.append(String.format("\n"));
			stream.append(String
					.format("attribute \"ref\" string \"positions\"\n"));
			stream.append(String
					.format("attribute \"element type\" string \"triangles\"\n"));
			stream.append(String
					.format("attribute \"dep\" string \"connections\"\n"));

			stream.append(String
					.format("object \"origin\" class array type float rank 1 shape 3 items 1 data follows\n"));
			stream.append(String
					.format("%9.4f %9.4f %9.4f\n", 0.0f, 0.0f, 0.0f));

			stream.append(String.format("#\n"));
			stream.append(String.format("object \"default\" field\n"));
			stream.append(String.format("    component \"positions\"  1\n"));
			stream.append(String.format("    component \"connections\"  2\n"));
			stream.append(String
					.format("    component \"origin\"  \"origin\"\n"));
			stream.append(String.format("end\n"));

			stream.close();
			if (mesh.getVertexData() != null
					&& mesh.getVertexData()[0].length > 0) {
				ArrayDoubleDxReaderWriter.getInstance().write(
						mesh.getVertexData(),
						new File(f.getParentFile(), FileReaderWriter
								.getFileName(f) + "_verts.dx"));
			}
			if (mesh.getCellData() != null && mesh.getCellData()[0].length > 0) {
				ArrayDoubleDxReaderWriter.getInstance().write(
						mesh.getCellData(),
						new File(f.getParentFile(), FileReaderWriter
								.getFileName(f) + "_cells.dx"));
			}
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
		}

		return null;
	}

}
