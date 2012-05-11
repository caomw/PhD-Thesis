package edu.jhu.ece.iacl.jist.io;


import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.imagesci.utility.GeomUtil;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.geom.NormalGenerator;

public class SurfaceSTLReaderWriter extends SurfaceReaderWriter {

	/** The Constant readerWriter. */
	protected static final SurfaceSTLReaderWriter readerWriter = new SurfaceSTLReaderWriter();

	/**
	 * Gets the single instance of SurfaceVrmlReaderWriter.
	 * 
	 * @return single instance of SurfaceVrmlReaderWriter
	 */
	public static SurfaceSTLReaderWriter getInstance() {
		return readerWriter;
	}

	/**
	 * Instantiates a new surface vrml reader writer.
	 */
	public SurfaceSTLReaderWriter() {
		super(new FileExtensionFilter(new String[] { "stl" }));
	}

	protected static class ComparePoint3f extends Point3f implements
			Comparable<ComparePoint3f> {
		protected int id;

		public ComparePoint3f(Point3f pt, int id) {
			super(pt);
			this.id = id;
		}

		public ComparePoint3f(float x, float y, float z, int id) {
			super(x, y, z);
			this.id = id;
		}

		public boolean equals(ComparePoint3f pt2) {
			Point3f pt1 = this;
			return (pt1.x == pt2.x && pt1.y == pt2.y && pt1.z == pt2.z);
		}

		@Override
		public int compareTo(ComparePoint3f pt2) {
			Point3f pt1 = this;
			return (pt1.x == pt2.x && pt1.y == pt2.y && pt1.z == pt2.z) ? 0
					: pt2.id - this.id;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter#readObject(java.io.File)
	 */
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
		Pattern header = Pattern.compile("vertex.+\n");
		Matcher m = header.matcher(buff);

		TreeSet<ComparePoint3f> map = new TreeSet<ComparePoint3f>();
		LinkedList<ComparePoint3f> pointList = new LinkedList<ComparePoint3f>();

		int l = 0;
		while (m.find()) {
			String head = buff.substring(m.start(), m.end());
			String[] vals = head.split("\\s+");
			ComparePoint3f pt = new ComparePoint3f(Float.parseFloat(vals[1]),
					Float.parseFloat(vals[2]), Float.parseFloat(vals[3]), l++);
			pointList.add(pt);
			map.add(pt);
		}
		l = 0;
		Hashtable<ComparePoint3f, Integer> hash = new Hashtable<ComparePoint3f, Integer>();

		int vertexCount = map.size();
		ComparePoint3f[] points = new ComparePoint3f[vertexCount];
		for (ComparePoint3f val : map) {
			points[l] = val;
			hash.put(val, l++);
		}
		int indexCount = pointList.size();
		int[] indices = new int[indexCount];
		l = 0;
		for (ComparePoint3f pt : pointList) {
			indices[l++] = hash.get(pt);
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
	protected File writeObject(EmbeddedSurface mesh, File f) {
		f = new File(f.getParent(), FileReaderWriter.getFileName(f) + ".stl");
		try {
			BufferedWriter data = new BufferedWriter(new FileWriter(f));
			Point3f[] points = mesh.getVertexCopy();
			int[] indexes = mesh.getIndexCopy();
			data.append("solid ascii\n");
			Vector3f norm = new Vector3f();
			Vector3f e1 = new Vector3f();
			Vector3f e2 = new Vector3f();
			for (int i = 0; i < indexes.length; i += 3) {
				Vector3f v1 = new Vector3f(points[indexes[i]]);
				Vector3f v2 = new Vector3f(points[indexes[i + 1]]);
				Vector3f v3 = new Vector3f(points[indexes[i + 2]]);
				e1.sub(v3, v2);
				e2.sub(v1, v2);
				norm.cross(e1, e2);
				norm.normalize();
				data.append(String.format("facet normal %f %f %f\n"
						+ "outer loop\n" + "vertex %f %f %f\n"
						+ "vertex %f %f %f\n" + "vertex %f %f %f\n"
						+ "endloop\n" + "endfacet\n", norm.x, norm.y, norm.z,
						v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, v3.x, v3.y, v3.z));
			}
			data.close();
			return f;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
