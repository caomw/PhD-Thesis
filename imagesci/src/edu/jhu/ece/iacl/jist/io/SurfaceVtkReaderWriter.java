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

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceVtkReaderWriter.
 */
public class SurfaceVtkReaderWriter extends SurfaceReaderWriter {

	/** The Constant readerWriter. */
	protected static final SurfaceVtkReaderWriter readerWriter = new SurfaceVtkReaderWriter();

	/**
	 * Instantiates a new surface vtk reader writer.
	 */
	public SurfaceVtkReaderWriter() {
		super(new FileExtensionFilter(new String[] { "vtk" }));
	}

	/**
	 * Gets the single instance of SurfaceVtkReaderWriter.
	 * 
	 * @return single instance of SurfaceVtkReaderWriter
	 */
	public static SurfaceVtkReaderWriter getInstance() {
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
		EmbeddedSurface surf = null;
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
		Pattern header = Pattern.compile("POINTS\\s\\d+\\sfloat");
		Matcher m = header.matcher(buff);
		int vertexCount = 0;
		int indexCount = 0;
		Point3f[] points;
		int[] indices;
		// Find vertex count
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length > 0) {
				try {
					vertexCount = Integer.parseInt(vals[vals.length - 1]);
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
			// Read in vertexes
			for (int i = 1; i < strs.length - 1; i += 3) {
				try {
					Point3f p = new Point3f();
					p.x = Float.parseFloat(strs[i]);
					p.y = Float.parseFloat(strs[i + 1]);
					p.z = Float.parseFloat(strs[i + 2]);
					points[(i - 1) / 3] = p;
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT VERTS");
					return null;
				}
			}
		} else {
			return null;
		}
		// Find cell count
		header = Pattern.compile("POLYGONS\\s+\\d+\\s+\\d+");
		m = header.matcher(buff);
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length > 1) {
				try {
					indexCount = Integer.parseInt(vals[1]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE INDEX COUNT");
					return null;
				}
			}
			indices = new int[indexCount * 3];
			System.out.println("jist.io" + "\t" + "INDICES " + indexCount);
			String[] strs = buff.substring(m.end(), buff.length()).split(
					"\\s+", indexCount * 4 + 2);
			int count = 0;
			// Read in indexes
			for (int i = 1; i < strs.length - 1; i += 4) {
				try {
					indices[count++] = Integer.parseInt(strs[i + 1]);
					indices[count++] = Integer.parseInt(strs[i + 2]);
					indices[count++] = Integer.parseInt(strs[i + 3]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT INDICES");
					return null;
				}
			}
		} else {
			return null;
		}
		header = Pattern
				.compile("POINT_DATA\\s+\\d+\\D+float\\s+\\d+\\nLOOKUP_TABLE\\s");
		m = header.matcher(buff);
		double[][] vertData = null;
		int count = 0;
		int dim = 0;
		// Find embedded vertex data count
		if (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length > 0) {
				try {
					count = Integer.parseInt(vals[1]);
					dim = Integer.parseInt(vals[2]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE DATA POINTS");
					return null;
				}
			}
			vertData = new double[count][dim];
			System.out.println("jist.io" + "\t" + "VERTEX DATA " + count
					+ " by " + dim);
			String[] strs = buff.substring(m.end(), buff.length()).split(
					"\\s+", count * dim + 2);
			int index = 0;
			// Read in vertex data
			for (int i = 1; i < strs.length && index < count * dim; i++) {
				try {
					vertData[index / dim][index % dim] = Double
							.parseDouble(strs[i]);
					index++;
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT DATA [" + strs[i] + "]");
					// return null;
				}
			}
			System.out.println("jist.io" + "\t" + index + " " + count);
		}
		header = Pattern
				.compile("CELL_DATA\\s+\\d+\\D+float\\s+\\d+\\nLOOKUP_TABLE\\s");
		m = header.matcher(buff);
		double[][] cellData = null;
		count = 0;
		dim = 0;
		// Find cell data count
		if (m.find()) {

			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\D+");
			if (vals.length > 0) {
				try {
					count = Integer.parseInt(vals[1]);
					dim = Integer.parseInt(vals[2]);
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT DETERMINE DATA POINTS");
					return null;
				}
			}
			cellData = new double[count][dim];
			System.out.println("jist.io" + "\t" + "CELL DATA " + count + " by "
					+ dim);
			String[] strs = buff.substring(m.end(), buff.length()).split(
					"\\s+", count * dim + 2);
			int index = 0;
			// Read in cell data
			for (int i = 1; i < strs.length && index < count * dim; i++) {
				try {
					cellData[index / dim][index % dim] = Double
							.parseDouble(strs[i]);
					index++;
				} catch (NumberFormatException e) {
					System.err.println(getClass().getCanonicalName()
							+ "CANNOT FORMAT DATA [" + strs[i] + "]");
					// return null;
				}
			}
			System.out.println("jist.io" + "\t" + index + " " + count);
		}
		surf = new EmbeddedSurface(points, indices);
		surf.setName(FileReaderWriter.getFileName(f));
		if (vertData != null) {
			surf.setVertexData(vertData);
		}
		if (cellData != null) {
			surf.setCellData(cellData);
		}
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
			stream.append("# vtk DataFile Version 3.0\n" + mesh.getName()
					+ "\n" + "ASCII\n" + "DATASET POLYDATA\n" + "POINTS "
					+ pointCount + " float\n");
			Point3f p = new Point3f();
			String tmp;
			// Write vertex locations
			for (int i = 0; i < pointCount; i++) {
				mesh.getCoordinate(i, p);
				tmp = String.format("%.5f %.5f %.5f\n", p.x, p.y, p.z);
				stream.append(tmp);
			}
			// Write triangle indexes
			stream.append("POLYGONS " + indexCount / 3 + " "
					+ (4 * indexCount / 3) + "\n");
			for (int i = 0; i < indexCount; i += 3) {
				stream.append(3 + " " + mesh.getCoordinateIndex(i) + " "
						+ mesh.getCoordinateIndex(i + 1) + " "
						+ mesh.getCoordinateIndex(i + 2) + "\n");
			}
			// Write scalar data
			double[][] scalars = mesh.getVertexData();
			if (scalars != null && scalars.length > 0 && scalars[0].length > 0) {
				stream.append("POINT_DATA " + scalars.length + "\n"
						+ "SCALARS EmbedVertex float " + scalars[0].length
						+ "\n" + "LOOKUP_TABLE default\n");
				for (int i = 0; i < scalars.length; i++) {
					for (int j = 0; j < scalars[i].length; j++) {
						stream.append(scalars[i][j] + " ");
					}
					stream.append("\n");
				}
			}
			// Write texture coordinates
			double[][] tex = mesh.getTextureCoordinates();
			if (tex != null && tex.length > 0 && tex[0].length > 0) {
				if (scalars == null || scalars.length == 0
						|| scalars[0].length == 0) {
					stream.append("POINT_DATA " + tex.length + "\n");
				}
				stream.append("TEXTURE_COORDINATES Texture%20Coordinates "
						+ tex[0].length + " float\n");
				for (int i = 0; i < tex.length; i++) {
					for (int j = 0; j < tex[i].length; j++) {
						stream.append(tex[i][j] + " ");
					}
					stream.append("\n");
				}
			}
			// Write cell data
			double[][] cells = mesh.getCellData();
			if (cells != null && cells.length > 0 && cells[0].length > 0) {
				stream.append("CELL_DATA " + cells.length + "\n"
						+ "SCALARS EmbedCell float " + cells[0].length + "\n"
						+ "LOOKUP_TABLE default\n");
				for (int i = 0; i < cells.length; i++) {
					for (int j = 0; j < cells[i].length; j++) {
						stream.append(cells[i][j] + " ");
					}
					stream.append("\n");
				}
			}
			stream.close();
			return f;
		} catch (IOException e) {
			System.err.println(getClass().getCanonicalName() + e.getMessage());
		}
		return null;
	}

}
