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
package edu.jhu.ece.iacl.jist.structures.geom;

import java.awt.Color;
import java.util.Arrays;
import java.util.Hashtable;

import javax.media.j3d.IndexedTriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import Jama.Matrix;

// TODO: Auto-generated Javadoc
/**
 * The Class EmbeddedSurface.
 */
public class EmbeddedSurface extends IndexedTriangleArray implements Cloneable,
		Comparable<EmbeddedSurface> {

	/**
	 * The Enum Direction.
	 */
	public enum Direction {

		/** The CLOCKWISE. */
		CLOCKWISE,
		/** The COUNTE r_ clockwise. */
		COUNTER_CLOCKWISE
	}

	/**
	 * The Class Edge.
	 */
	public static class Edge implements Comparable<Edge> {

		/** The f2. */
		public int f1, f2;

		/** The id. */
		public int id;

		/** The v2. */
		public int v1, v2;

		/**
		 * Instantiates a new edge.
		 * 
		 * @param v1
		 *            the v1
		 * @param v2
		 *            the v2
		 * @param id
		 *            the id
		 */
		public Edge(int v1, int v2, int id) {
			this.v1 = v1;
			this.v2 = v2;
			this.f1 = -1;
			this.f2 = -1;
			this.id = id;
		}

		/**
		 * Hash code long.
		 * 
		 * @param v1
		 *            the v1
		 * @param v2
		 *            the v2
		 * 
		 * @return the long
		 */
		public static long hashCodeLong(int v1, int v2) {
			return v1 + v2 * 10000000l;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Edge e) {
			return (int) Math.signum(this.hashCodeLong() - e.hashCodeLong());
		}

		/**
		 * Contains.
		 * 
		 * @param v
		 *            the v
		 * 
		 * @return true, if successful
		 */
		public boolean contains(int v) {
			return (v == v1 || v == v2);
		}

		/**
		 * Equals.
		 * 
		 * @param v1
		 *            the v1
		 * @param v2
		 *            the v2
		 * 
		 * @return true, if successful
		 */
		public boolean equals(int v1, int v2) {
			return ((this.v1 == v1 && this.v2 == v2) || (this.v1 == v2 && this.v2 == v1));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Edge) {
				return (v1 == ((Edge) obj).v1 && v2 == ((Edge) obj).v2);
			}
			return false;
		}

		/**
		 * Gets the vector.
		 * 
		 * @param surf
		 *            the surf
		 * 
		 * @return the vector
		 */
		public Vector3f getVector(EmbeddedSurface surf) {
			Vector3f v = new Vector3f();
			v.sub(surf.getVertex(v2), surf.getVertex(v1));
			return v;
		}

		/**
		 * Hash code long.
		 * 
		 * @return the long
		 */
		public long hashCodeLong() {
			return v1 + v2 * 10000000l;
		}

		/**
		 * Opposite.
		 * 
		 * @param v
		 *            the v
		 * 
		 * @return the int
		 */
		public int opposite(int v) {
			return (v == v1) ? v2 : v1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "<" + v1 + "," + v2 + ">";
		}
	}

	/**
	 * The Class EdgeSplit.
	 */
	public static class EdgeSplit extends Edge {

		/** The mid. */
		public int mid;

		/**
		 * Instantiates a new edge split.
		 * 
		 * @param e
		 *            the e
		 * @param mid
		 *            the mid
		 */
		public EdgeSplit(Edge e, int mid) {
			super(e.v1, e.v2, e.id);
			this.mid = mid;
		}

		/**
		 * Instantiates a new edge split.
		 * 
		 * @param v1
		 *            the v1
		 * @param v2
		 *            the v2
		 * @param mid
		 *            the mid
		 * @param id
		 *            the id
		 */
		public EdgeSplit(int v1, int v2, int mid, int id) {
			super(v1, v2, id);
			this.mid = mid;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface.Edge#toString()
		 */
		@Override
		public String toString() {
			return "<" + v1 + "," + mid + "," + v2 + ">";
		}
	}

	/**
	 * The Class Face.
	 */
	public static class Face {

		/** The edges. */
		public Edge[] edges;

		/** The id. */
		public int id;

		/**
		 * Instantiates a new face.
		 * 
		 * @param e1
		 *            the e1
		 * @param e2
		 *            the e2
		 * @param e3
		 *            the e3
		 * @param id
		 *            the id
		 */
		public Face(Edge e1, Edge e2, Edge e3, int id) {
			edges = new Edge[] { e1, e2, e3 };

			this.id = id;
			if (e1.f1 == -1) {
				e1.f2 = id;
			} else {
				e1.f1 = id;
			}
			if (e2.f1 == -1) {
				e2.f2 = id;
			} else {
				e2.f1 = id;
			}
			if (e3.f1 == -1) {
				e3.f2 = id;
			} else {
				e3.f1 = id;
			}
		}

		/**
		 * Instantiates a new face.
		 * 
		 * @param id
		 *            the id
		 */
		public Face(int id) {
			edges = new Edge[3];
			this.id = id;
		}

		/**
		 * Instantiates a new face.
		 * 
		 * @param v1
		 *            the v1
		 * @param v2
		 *            the v2
		 * @param v3
		 *            the v3
		 * @param id
		 *            the id
		 */
		public Face(int v1, int v2, int v3, int id) {
			this(new Edge(v1, v2, -1), new Edge(v2, v3, -1), new Edge(v3, v1,
					-1), id);
		}

		/**
		 * Gets the area.
		 * 
		 * @param mesh
		 *            the mesh
		 * 
		 * @return the area
		 */
		public double getArea(EmbeddedSurface mesh) {
			Vector3f v1 = edges[0].getVector(mesh);
			Vector3f v2 = edges[1].getVector(mesh);
			Vector3f mag = new Vector3f();
			mag.cross(v1, v2);
			return 0.5 * mag.length();
		}

		/**
		 * Gets the vertexes.
		 * 
		 * @return the vertexes
		 */
		public int[] getVertexes() {
			int v1 = edges[0].v1;
			int v2;
			int v3;
			if (edges[1].contains(v1)) {
				v2 = v1;
				v1 = edges[0].v2;
			} else {
				v2 = edges[0].v2;
			}
			v3 = edges[1].opposite(v2);
			return new int[] { v1, v2, v3 };
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "FACE " + id + ") " + edges[0] + " " + edges[1] + " "
					+ edges[2];
		}
	}

	/** The cell data. */
	protected double[][] cellData;

	/** The edges. */
	protected EmbeddedSurface.Edge[] edges;

	/** The edge table. */
	protected int[][] edgeTable;

	/** The faces. */
	protected EmbeddedSurface.Face[] faces;

	/** The m_k e0. */
	private javax.vecmath.Vector3f m_kE0;

	/** The m_k e1. */
	private javax.vecmath.Vector3f m_kE1;

	/** The m_k n. */
	private javax.vecmath.Vector3f m_kN;

	/** The m_k v0. */
	private Point3f m_kV0;

	/** The m_k v1. */
	private Point3f m_kV1;

	/** The m_k v2. */
	private Point3f m_kV2;

	/** The m_k v3. */
	private Point3f m_kV3;

	/** The neighbor edge face table. */
	protected EmbeddedSurface.Face[][] neighborEdgeFaceTable;

	/** The neighbor face face table. */
	protected EmbeddedSurface.Face[][] neighborFaceFaceTable;

	/** The neighbor vertex edge table. */
	protected EmbeddedSurface.Edge[][] neighborVertexEdgeTable;

	/** The neighbor vertex face table. */
	protected EmbeddedSurface.Face[][] neighborVertexFaceTable;

	/** The neighbor vertex vertex table. */
	protected int[][] neighborVertexVertexTable;

	/** The origin. */
	protected Point3f origin = new Point3f(0, 0, 0);

	/** The scale. */
	protected Vector3f scale = new Vector3f(1, 1, 1);

	/** The texture coords. */
	protected double[][] textureCoords = null;

	/** The vertex data. */
	protected double[][] vertexData;

	/**
	 * Instantiates a new embedded surface.
	 *
	 * @param akVertex the ak vertex
	 * @param aiConnect the ai connect
	 */
	public EmbeddedSurface(Point3f[] akVertex, int[] aiConnect) {
		super(akVertex.length, 79, 2, new int[] { 0, 0 }, aiConnect.length);
		init();
		setCoordinates(0, akVertex);
		setCoordinateIndices(0, aiConnect);
		computeNormals();
		setNormalIndices(0, aiConnect);

	}

	/**
	 * Instantiates a new embedded surface.
	 *
	 * @param akVertex the ak vertex
	 * @param akNormal the ak normal
	 * @param aiConnect the ai connect
	 */
	public EmbeddedSurface(Point3f[] akVertex,
			javax.vecmath.Vector3f[] akNormal, int[] aiConnect) {
		super(akVertex.length, 79, 2, new int[] { 0, 0 }, aiConnect.length);

		init();

		setCoordinates(0, akVertex);
		setCoordinateIndices(0, aiConnect);
		setNormals(0, akNormal);
		setNormalIndices(0, aiConnect);
	}

	/**
	 * Instantiates a new embedded surface.
	 *
	 * @param points the points
	 * @param indices the indices
	 * @param vertData the vert data
	 */
	public EmbeddedSurface(Point3f[] points, int[] indices, double[][] vertData) {
		this(points, indices);
		setVertexData(vertData);
	}

	/**
	 * Instantiates a new embedded surface.
	 *
	 * @param kMesh the k mesh
	 */
	public EmbeddedSurface(EmbeddedSurface kMesh) {
		this(kMesh.getVertexCopy(), kMesh.getNormalCopy(), kMesh.getIndexCopy());
	}

	/**
	 * Instantiates a new embedded surface.
	 *
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 * @param arg2 the arg2
	 */
	public EmbeddedSurface(int arg0, int arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new embedded surface.
	 *
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 * @param arg2 the arg2
	 * @param arg3 the arg3
	 * @param arg4 the arg4
	 */
	public EmbeddedSurface(int arg0, int arg1, int arg2, int[] arg3, int arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new embedded surface.
	 *
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 * @param arg2 the arg2
	 * @param arg3 the arg3
	 * @param arg4 the arg4
	 * @param arg5 the arg5
	 * @param arg6 the arg6
	 */
	public EmbeddedSurface(int arg0, int arg1, int arg2, int[] arg3, int arg4,
			int[] arg5, int arg6) {
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Inits the.
	 */
	private void init() {
		setCapability(18);
		setCapability(17);
		setCapability(8);
		setCapability(20);
		setCapability(10);
		setCapability(9);
		setCapability(4);
		setCapability(5);
		setCapability(0);
		setCapability(1);
		setCapability(6);
		setCapability(7);
		setCapability(16);
		setCapability(2);
		setCapability(3);

		this.m_kV0 = new Point3f();
		this.m_kV1 = new Point3f();
		this.m_kV2 = new Point3f();
		this.m_kV3 = new Point3f();
		this.m_kE0 = new javax.vecmath.Vector3f();
		this.m_kE1 = new javax.vecmath.Vector3f();
		this.m_kN = new javax.vecmath.Vector3f();
	}

	/**
	 * Compute normals.
	 */
	public void computeNormals() {
		javax.vecmath.Vector3f[] akSum = new javax.vecmath.Vector3f[getVertexCount()];

		for (int i = 0; i < getVertexCount(); ++i) {
			akSum[i] = new javax.vecmath.Vector3f(0.0F, 0.0F, 0.0F);
		}

		for (int i = 0; i < getIndexCount();) {
			int iV0 = getCoordinateIndex(i++);
			int iV1 = getCoordinateIndex(i++);
			int iV2 = getCoordinateIndex(i++);

			getCoordinate(iV0, this.m_kV0);
			getCoordinate(iV1, this.m_kV1);
			getCoordinate(iV2, this.m_kV2);

			this.m_kE0.sub(this.m_kV1, this.m_kV0);
			this.m_kE1.sub(this.m_kV2, this.m_kV0);
			this.m_kN.cross(this.m_kE0, this.m_kE1);

			float fLength = this.m_kN.length();

			if (fLength > 1.0E-006D) {
				this.m_kN.scale(1.0F / fLength);
			} else {
				this.m_kN.x = 0.0F;
				this.m_kN.y = 0.0F;
				this.m_kN.z = 0.0F;
			}

			akSum[iV0].add(this.m_kN);
			akSum[iV1].add(this.m_kN);
			akSum[iV2].add(this.m_kN);
		}

		for (int i = 0; i < getVertexCount(); ++i) {
			akSum[i].normalize();
			setNormal(i, akSum[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * setVertexData(double[][])
	 */
	/**
	 * Sets the vertex data.
	 *
	 * @param data the new vertex data
	 */
	public void setVertexData(double[][] data) {
		if (data != null && getVertexCount() != data.length) {
			System.err.println("jist.base"
					+ "VERTEX DATA IS NOT CORRECT LENGTH! Expected: "
					+ getVertexCount() + " Actual: " + data.length);
			// System.exit(1);
		} else {
			this.vertexData = data;
		}
	}

	/**
	 * Affine transform vertices.
	 * 
	 * @param m
	 *            the m
	 * @param t
	 *            the t
	 */
	public void affineTransformVertices(Matrix3f m, Point3f t) {
		int vertexCount = this.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			Point3f p = getVertex(i);
			m.transform(p);
			p.add(t);
			this.setVertex(i, p);
		}
	}

	/**
	 * Gets the center of mass.
	 * 
	 * @return the center of mass
	 */
	public Point3f getCenterOfMass() {
		int indexCount = getIndexCount();
		// Compute center of mass
		Point3f p1, p2, p3;
		Vector3f edge1 = new Vector3f();
		Vector3f edge2 = new Vector3f();
		Vector3f norm = new Vector3f();
		Point3f centroid = new Point3f();
		Point3f massCenter = new Point3f();
		float areaSum = 0;
		float area;
		for (int in = 0; in < indexCount; in += 3) {
			p1 = getVertex(getCoordinateIndex(in));
			p2 = getVertex(getCoordinateIndex(in + 1));
			p3 = getVertex(getCoordinateIndex(in + 2));
			edge1.sub(p2, p1);
			edge2.sub(p3, p1);
			norm.cross(edge2, edge1);
			area = 0.5f * norm.length();
			centroid = new Point3f(p1);
			centroid.add(p2);
			centroid.add(p3);
			centroid.scale(0.333333f * area);
			massCenter.add(centroid);
			areaSum += area;
		}
		massCenter.scale(1.0f / areaSum);
		return massCenter;
	}

	/**
	 * Gets the centroid.
	 * 
	 * @param fid
	 *            the fid
	 * 
	 * @return the centroid
	 */
	public Point3f getCentroid(int fid) {
		fid *= 3;
		Point3f p1 = getVertex(getCoordinateIndex(fid));
		Point3f p2 = getVertex(getCoordinateIndex(fid + 1));
		Point3f p3 = getVertex(getCoordinateIndex(fid + 2));
		p1.add(p2);
		p1.add(p3);
		p1.scale(0.333333333333f);
		return p1;
	}

	/**
	 * Gets the face area.
	 * 
	 * @param fid
	 *            the fid
	 * 
	 * @return the face area
	 */
	public float getFaceArea(int fid) {
		fid *= 3;
		Point3f p1 = getVertex(getCoordinateIndex(fid));
		Point3f p2 = getVertex(getCoordinateIndex(fid + 1));
		Point3f p3 = getVertex(getCoordinateIndex(fid + 2));
		Vector3f edge1 = new Vector3f();
		Vector3f edge2 = new Vector3f();
		Vector3f cross = new Vector3f();
		edge1.sub(p2, p1);
		edge2.sub(p3, p1);
		cross.cross(edge1, edge2);
		return cross.length();
	}

	/**
	 * Gets the face points.
	 * 
	 * @param fid
	 *            the fid
	 * 
	 * @return the face points
	 */
	public Point3f[] getFacePoints(int fid) {
		fid *= 3;
		Point3f[] pts = new Point3f[3];
		pts[0] = getVertex(getCoordinateIndex(fid));
		pts[1] = getVertex(getCoordinateIndex(fid + 1));
		pts[2] = getVertex(getCoordinateIndex(fid + 2));
		return pts;
	}

	/**
	 * Gets the max.
	 * 
	 * @return the max
	 */
	public Point3f getMax() {
		Point3f max = new Point3f(Float.MIN_VALUE, Float.MIN_VALUE,
				Float.MIN_VALUE);
		for (int i = 0; i < getVertexCount(); i++) {
			Point3f p = getVertex(i);
			max.x = Math.max(p.x, max.x);
			max.y = Math.max(p.y, max.y);
			max.z = Math.max(p.z, max.z);
		}
		return max;
	}

	/**
	 * Gets the max angle.
	 * 
	 * @param fid
	 *            the fid
	 * 
	 * @return the max angle
	 */
	public float getMaxAngle(int fid) {
		fid *= 3;
		Point3f p1 = getVertex(getCoordinateIndex(fid));
		Point3f p2 = getVertex(getCoordinateIndex(fid + 1));
		Point3f p3 = getVertex(getCoordinateIndex(fid + 2));
		Vector3f edge1 = new Vector3f();
		Vector3f edge2 = new Vector3f();
		Vector3f edge3 = new Vector3f();
		edge1.sub(p2, p1);
		edge2.sub(p3, p1);
		edge3.sub(p3, p2);
		float angle = Math.max(
				Math.max(edge1.angle(edge2), edge2.angle(edge3)),
				edge1.angle(edge3));
		return angle;
	}

	/**
	 * Gets the min.
	 * 
	 * @return the min
	 */
	public Point3f getMin() {
		Point3f min = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE,
				Float.MAX_VALUE);
		for (int i = 0; i < getVertexCount(); i++) {
			Point3f p = getVertex(i);
			min.x = Math.min(p.x, min.x);
			min.y = Math.min(p.y, min.y);
			min.z = Math.min(p.z, min.z);
		}
		return min;
	}

	/**
	 * Gets the min angle.
	 * 
	 * @param fid
	 *            the fid
	 * 
	 * @return the min angle
	 */
	public float getMinAngle(int fid) {
		fid *= 3;
		Point3f p1 = getVertex(getCoordinateIndex(fid));
		Point3f p2 = getVertex(getCoordinateIndex(fid + 1));
		Point3f p3 = getVertex(getCoordinateIndex(fid + 2));
		Vector3f edge1 = new Vector3f();
		Vector3f edge2 = new Vector3f();
		Vector3f edge3 = new Vector3f();
		edge1.sub(p2, p1);
		edge2.sub(p3, p1);
		edge3.sub(p3, p2);
		float angle = Math.min(
				Math.min(edge1.angle(edge2), edge2.angle(edge3)),
				edge1.angle(edge3));
		return angle;
	}

	/**
	 * Mid point.
	 * 
	 * @param e
	 *            the e
	 * 
	 * @return the point3f
	 */
	public Point3f midPoint(Edge e) {
		Point3f p1 = getVertex(e.v1);
		Point3f p2 = getVertex(e.v2);
		return new Point3f((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f,
				(p1.z + p2.z) * 0.5f);
	}

	/**
	 * Scale vertices.
	 * 
	 * @param scalar
	 *            the scalar
	 */
	public void scaleVertices(float scalar) {
		int vertexCount = this.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			Point3f p = getVertex(i);
			p.scale(scalar);
			this.setVertex(i, p);
		}
	}

	/**
	 * Scale vertices.
	 * 
	 * @param scalar
	 *            the scalar
	 */
	public void scaleVertices(float[] scalar) {
		int vertexCount = this.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			Point3f p = getVertex(i);
			p.x *= scalar[0];
			p.y *= scalar[1];
			p.z *= scalar[2];
			this.setVertex(i, p);
		}
	}

	/**
	 * Transform.
	 * 
	 * @param m
	 *            the m
	 */
	public void transform(Matrix m) {
		int vertexCount = this.getVertexCount();
		Matrix vec = new Matrix(4, 1);
		vec.set(3, 0, 1);
		for (int i = 0; i < vertexCount; i++) {
			Point3f p = getVertex(i);
			vec.set(0, 0, p.x);
			vec.set(1, 0, p.y);
			vec.set(2, 0, p.z);
			Matrix r = m.times(vec);
			double w = r.get(3, 0);
			this.setVertex(i,
					new Point3f((float) (r.get(0, 0) / w),
							(float) (r.get(1, 0) / w),
							(float) (r.get(2, 0) / w)));
		}
	}

	/**
	 * Translate.
	 * 
	 * @param offset
	 *            the offset
	 */
	public void translate(Point3f offset) {
		int vertexCount = this.getVertexCount();
		for (int i = 0; i < vertexCount; i++) {
			Point3f p = getVertex(i);
			p.add(offset);
			this.setVertex(i, p);
		}
	}

	/**
	 * Gets the vertex.
	 * 
	 * @param i
	 *            the i
	 * 
	 * @return the vertex
	 */
	public Point3f getVertex(int i) {
		Point3f p = new Point3f();
		this.getCoordinate(i, p);
		return p;
	}

	/**
	 * Gets the centroid at offset.
	 * 
	 * @param fid
	 *            the fid
	 * @param offset
	 *            the offset
	 * 
	 * @return the centroid at offset
	 */
	public Point3f getCentroidAtOffset(int fid, int offset) {
		fid *= 3;
		Point3f p1 = getPointAtOffset(getCoordinateIndex(fid), offset);
		Point3f p2 = getPointAtOffset(getCoordinateIndex(fid + 1), offset);
		Point3f p3 = getPointAtOffset(getCoordinateIndex(fid + 2), offset);
		p1.add(p2);
		p1.add(p3);
		p1.scale(0.333333333333f);
		return p1;
	}

	/**
	 * Gets the point at offset.
	 * 
	 * @param i
	 *            the i
	 * @param offset
	 *            the offset
	 * 
	 * @return the point at offset
	 */
	public Point3f getPointAtOffset(int i, int offset) {
		return new Point3f((float) vertexData[i][offset],
				(float) vertexData[i][offset + 1],
				(float) vertexData[i][offset + 2]);
	}

	/**
	 * Gets the number of holes.
	 * 
	 * @return the number of holes
	 */
	public long getNumberOfHoles() {
		return getGenus(this);
	}

	/**
	 * Gets the genus.
	 * 
	 * @param mesh
	 *            the mesh
	 * 
	 * @return the genus
	 */
	public static int getGenus(EmbeddedSurface mesh) {
		int vertCount = mesh.getVertexCount();
		int indexCount = mesh.getIndexCount();
		int faceCount = indexCount / 3;
		// Edge[] edges=new Edge[indexCount];
		long edgeHash[] = new long[indexCount];
		for (int i = 0; i < indexCount; i += 3) {
			int i1 = mesh.getCoordinateIndex(i);
			int i2 = mesh.getCoordinateIndex(i + 1);
			int i3 = mesh.getCoordinateIndex(i + 2);
			if (i1 < i2) {
				edgeHash[i] = i1 + i2 * 10000000l;
			} else {
				edgeHash[i] = i2 + i1 * 10000000l;
			}
			if (i2 < i3) {
				edgeHash[i + 1] = i2 + i3 * 10000000l;
			} else {
				edgeHash[i + 1] = i3 + i2 * 10000000l;
			}
			if (i3 < i1) {
				edgeHash[i + 2] = i3 + i1 * 10000000l;
			} else {
				edgeHash[i + 2] = i1 + i3 * 10000000l;
			}
		}
		Arrays.sort(edgeHash);
		long lastIndex = -1;
		int edgeCount = 1;
		for (long index : edgeHash) {
			if (index != lastIndex) {
				edgeCount++;
			}
			lastIndex = index;
		}
		return -(vertCount - edgeCount + faceCount - 2) / 2;
	}

	/**
	 * Resize data.
	 * 
	 * @param dim
	 *            the dim
	 */
	public void resizeData(int dim) {
		double[] tmp;
		for (int i = 0; i < vertexData.length; i++) {
			tmp = new double[dim];
			for (int j = 0; j < tmp.length && j < vertexData[i].length; j++) {
				tmp[j] = vertexData[i][j];
			}
			setVertexData(i, tmp);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * setVertexData(int, double[])
	 */
	/**
	 * Sets the vertex data.
	 *
	 * @param i the i
	 * @param array the array
	 */
	public void setVertexData(int i, double[] array) {
		vertexData[i] = array;
	}

	/**
	 * Builds the edge hash.
	 * 
	 * @param edges
	 *            the edges
	 * 
	 * @return the hashtable< long, edge>
	 */
	public static Hashtable<Long, Edge> buildEdgeHash(Edge[] edges) {
		Hashtable<Long, Edge> edgeHash = new Hashtable<Long, Edge>(edges.length);
		for (Edge e : edges) {
			edgeHash.put(e.hashCodeLong(), e);
		}
		return edgeHash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EmbeddedSurface surf) {
		return (int) Math.signum(this.getVertexCount() - surf.getVertexCount());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * getCellData()
	 */
	/**
	 * Gets the cell data.
	 *
	 * @return the cell data
	 */
	public double[][] getCellData() {
		return cellData;
	}

	/**
	 * Gets the edges.
	 * 
	 * @return the edges
	 */
	public EmbeddedSurface.Edge[] getEdges() {
		return edges;
	}

	/**
	 * Gets the edge table.
	 * 
	 * @return the edge table
	 */
	public int[][] getEdgeTable() {
		return edgeTable;
	}

	/**
	 * Gets the face count.
	 * 
	 * @return the face count
	 */
	public int getFaceCount() {
		return getIndexCount() / 3;
	}

	/**
	 * Gets the faces.
	 * 
	 * @return the faces
	 */
	public EmbeddedSurface.Face[] getFaces() {
		return faces;
	}

	/**
	 * Gets the face vertex ids.
	 * 
	 * @param fid
	 *            the fid
	 * 
	 * @return the face vertex ids
	 */
	public int[] getFaceVertexIds(int fid) {
		fid *= 3;
		int[] pts = new int[3];
		pts[0] = (getCoordinateIndex(fid));
		pts[1] = (getCoordinateIndex(fid + 1));
		pts[2] = (getCoordinateIndex(fid + 2));
		return pts;
	}

	/**
	 * Gets the index copy.
	 *
	 * @return the index copy
	 */
	public int[] getIndexCopy() {
		int[] aiConnect = new int[getIndexCount()];

		getCoordinateIndices(0, aiConnect);

		return aiConnect;
	}

	/**
	 * Gets the inner point.
	 * 
	 * @param i
	 *            the i
	 * 
	 * @return the inner point
	 */
	public Point3f getInnerPoint(int i) {
		return new Point3f((float) vertexData[i][1], (float) vertexData[i][2],
				(float) vertexData[i][3]);
	}

	/**
	 * Gets the neighbor edge face table.
	 * 
	 * @return the neighbor edge face table
	 */
	public EmbeddedSurface.Face[][] getNeighborEdgeFaceTable() {
		return neighborEdgeFaceTable;
	}

	/**
	 * Gets the neighbor face face table.
	 * 
	 * @return the neighbor face face table
	 */
	public EmbeddedSurface.Face[][] getNeighborFaceFaceTable() {
		return neighborFaceFaceTable;
	}

	/**
	 * Gets the neighbor vertex edge table.
	 * 
	 * @return the neighbor vertex edge table
	 */
	public EmbeddedSurface.Edge[][] getNeighborVertexEdgeTable() {
		return neighborVertexEdgeTable;
	}

	/**
	 * Gets the neighbor vertex face table.
	 * 
	 * @return the neighbor vertex face table
	 */
	public EmbeddedSurface.Face[][] getNeighborVertexFaceTable() {
		return neighborVertexFaceTable;
	}

	/**
	 * Gets the neighbor vertex vertex table.
	 * 
	 * @return the neighbor vertex vertex table
	 */
	public int[][] getNeighborVertexVertexTable() {
		return neighborVertexVertexTable;
	}

	/**
	 * Gets the normal.
	 * 
	 * @param i
	 *            the i
	 * 
	 * @return the normal
	 */
	public Vector3f getNormal(int i) {
		Vector3f p = new Vector3f();
		this.getNormal(i, p);
		return p;
	}

	/**
	 * Gets the normal copy.
	 *
	 * @return the normal copy
	 */
	public javax.vecmath.Vector3f[] getNormalCopy() {
		javax.vecmath.Vector3f[] akNormal = new javax.vecmath.Vector3f[getVertexCount()];

		for (int i = 0; i < getVertexCount(); ++i) {
			akNormal[i] = new javax.vecmath.Vector3f();
		}

		getNormals(0, akNormal);

		return akNormal;
	}

	/**
	 * Gets the origin.
	 * 
	 * @return the origin
	 */
	public Point3f getOrigin() {
		return origin;
	}

	/**
	 * Gets the outer point.
	 * 
	 * @param i
	 *            the i
	 * 
	 * @return the outer point
	 */
	public Point3f getOuterPoint(int i) {
		return new Point3f((float) vertexData[i][4], (float) vertexData[i][5],
				(float) vertexData[i][6]);
	}

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	public Vector3f getScale() {
		return scale;
	}

	/**
	 * Gets the stats.
	 * 
	 * @return the stats
	 */
	public double[] getStats() {
		double sqrs = 0;
		double sum = 0;
		double count = 0;
		double maxThickness = 0;
		double minThickness = 1E10;
		double thick = 0;
		for (int i = 0; i < vertexData.length; i++) {
			thick = (vertexData[i].length > 0) ? vertexData[i][0] : 0;
			maxThickness = Math.max(thick, maxThickness);
			minThickness = Math.min(thick, minThickness);
			sqrs += (thick) * (thick);
			sum += thick;
			count++;
		}
		return new double[] { (sum / count),
				Math.sqrt((sqrs - sum * sum / count) / (count - 1)),
				minThickness, maxThickness };
	}

	/**
	 * Gets the texture coordinates.
	 * 
	 * @return the texture coordinates
	 */
	public double[][] getTextureCoordinates() {
		return textureCoords;
	}

	/**
	 * Gets the thickness.
	 * 
	 * @param i
	 *            the i
	 * 
	 * @return the thickness
	 */
	public double getThickness(int i) {
		return vertexData[i][0];
	}

	/*
	 * public IntersectorTriangle getTriangle(int i) { IntersectorTriangle tri =
	 * new IntersectorTriangle(this.getCoordinateIndex(3 * i),
	 * this.getCoordinateIndex(3 * i + 1), this .getCoordinateIndex(3 * i + 2),
	 * this); return tri; }
	 */
	/**
	 * Gets the vector at offset.
	 * 
	 * @param i
	 *            the i
	 * @param offset
	 *            the offset
	 * 
	 * @return the vector at offset
	 */
	public Vector3f getVectorAtOffset(int i, int offset) {
		return new Vector3f((float) vertexData[i][offset],
				(float) vertexData[i][offset + 1],
				(float) vertexData[i][offset + 2]);
	}

	/**
	 * Gets the vertex copy.
	 *
	 * @return the vertex copy
	 */
	public Point3f[] getVertexCopy() {
		Point3f[] akVertex = new Point3f[getVertexCount()];

		for (int i = 0; i < getVertexCount(); ++i) {
			akVertex[i] = new Point3f();
		}

		getCoordinates(0, akVertex);

		return akVertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * getVertexData()
	 */
	/**
	 * Gets the vertex data.
	 *
	 * @return the vertex data
	 */
	public double[][] getVertexData() {
		return vertexData;
	}

	/**
	 * Gets the vertex data at offset.
	 * 
	 * @param i
	 *            the i
	 * @param offset
	 *            the offset
	 * 
	 * @return the vertex data at offset
	 */
	public double getVertexDataAtOffset(int i, int offset) {
		return vertexData[i][offset];
	}

	/**
	 * Gets the vertex double copy.
	 * 
	 * @return the vertex double copy
	 */
	public double[] getVertexDoubleCopy() {
		int vertCount = this.getVertexCount();
		double[] pts = new double[vertCount * 3];
		for (int i = 0; i < vertCount; i++) {
			Point3d p = new Point3d();
			this.getCoordinate(i, p);
			pts[i * 3] = p.x;
			pts[i * 3 + 1] = p.y;
			pts[i * 3 + 2] = p.z;
		}
		return pts;
	}

	/**
	 * Checks for embedded data.
	 * 
	 * @return true, if successful
	 */
	public boolean hasEmbeddedData() {
		return (vertexData != null && vertexData.length > 0 && vertexData[0].length > 0);
	}

	/**
	 * Mid values.
	 * 
	 * @param e
	 *            the e
	 * 
	 * @return the double[]
	 */
	public double[] midValues(Edge e) {
		double[] ret = new double[vertexData[0].length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = 0.5 * (vertexData[e.v1][i] + vertexData[e.v2][i]);
		}
		return ret;
	}

	/**
	 * Scale data.
	 * 
	 * @param scalar
	 *            the scalar
	 */
	public void scaleData(float scalar) {
		for (int i = 0; i < vertexData.length; i++) {
			for (int j = 0; j < vertexData[0].length; j++) {
				vertexData[i][j] *= scalar;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * setCellData(double[][])
	 */
	/**
	 * Sets the cell data.
	 *
	 * @param cellData the new cell data
	 */
	public void setCellData(double[][] cellData) {
		this.cellData = cellData;
	}

	/**
	 * Sets the color.
	 * 
	 * @param i
	 *            the i
	 * @param c
	 *            the c
	 */
	public void setColor(int i, Color c) {
		this.setColor(i, new Color4f(c));
	}

	/**
	 * Sets the inner point.
	 * 
	 * @param i
	 *            the i
	 * @param p
	 *            the p
	 */
	public void setInnerPoint(int i, Point3f p) {
		vertexData[i][1] = p.x;
		vertexData[i][2] = p.y;
		vertexData[i][3] = p.z;
	}

	/**
	 * Sets the origin.
	 * 
	 * @param scale
	 *            the new origin
	 */
	public void setOrigin(Point3f scale) {
		this.origin = scale;
	}

	/**
	 * Sets the outer point.
	 * 
	 * @param i
	 *            the i
	 * @param p
	 *            the p
	 */
	public void setOuterPoint(int i, Point3f p) {
		vertexData[i][4] = p.x;
		vertexData[i][5] = p.y;
		vertexData[i][6] = p.z;
	}

	/**
	 * Sets the scale.
	 * 
	 * @param scale
	 *            the new scale
	 */
	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

	/**
	 * Sets the texture coordinates.
	 * 
	 * @param textureCoords
	 *            the new texture coordinates
	 */
	public void setTextureCoordinates(double[][] textureCoords) {
		this.textureCoords = textureCoords;
	}

	/**
	 * Sets the thickness.
	 * 
	 * @param i
	 *            the i
	 * @param thick
	 *            the thick
	 */
	public void setThickness(int i, double thick) {
		vertexData[i][0] = thick;
	}

	/**
	 * Sets the vertex.
	 * 
	 * @param i
	 *            the i
	 * @param p
	 *            the p
	 */
	public void setVertex(int i, Tuple3f p) {
		this.setCoordinate(i, new float[] { p.x, p.y, p.z });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * setVertexData(int, double)
	 */
	/**
	 * Sets the vertex data.
	 *
	 * @param i the i
	 * @param val the val
	 */
	public void setVertexData(int i, double val) {
		vertexData[i] = new double[] { val };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * setVertexData(int, int, double)
	 */
	/**
	 * Sets the vertex data.
	 *
	 * @param i the i
	 * @param j the j
	 * @param val the val
	 */
	public void setVertexData(int i, int j, double val) {
		vertexData[i][j] = val;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.media.j3d.SceneGraphObject#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Gets the statistics.
	 * 
	 * @param i
	 *            the i
	 * 
	 * @return the statistics
	 */
	protected double[] getStatistics(int i) {
		int count = getVertexCount();
		double sum = 0;
		double sqrs = 0;
		double val;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int n = 0; n < count; n++) {
			val = getVertexData(n)[i];
			sum += val;
			sqrs += val * val;
			min = Math.min(min, val);
			max = Math.max(max, val);
		}
		double mean = (sum / count);
		double stdev = Math.sqrt((sqrs - sum * sum / count) / (count - 1));
		return new double[] { mean, stdev, min, max };
	}

	/**
	 * Uncrop.
	 *
	 * @param i the i
	 * @return the vertex data
	 */
	/*
	 * public void uncrop(CropParameters params) { Point3f offset = new
	 * Point3f(params.xmin, params.ymin, params.zmin); translate(offset); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.mipav.view.renderer.J3D.model.structures.ModelTriangleMesh#
	 * getVertexData(int)
	 */
	public double[] getVertexData(int i) {
		if (vertexData == null) {
			return null;
		} else {
			return vertexData[i];
		}
	}

	/**
	 * Convert colors.
	 * 
	 * @param colors
	 *            the colors
	 * 
	 * @return the color4f[]
	 */
	static private Color4f[] convertColors(Color3f[] colors) {
		Color4f[] colors4 = new Color4f[colors.length];
		for (int i = 0; i < colors.length; i++) {
			colors4[i] = new Color4f(colors[i].get());
		}
		return colors4;
	}
}