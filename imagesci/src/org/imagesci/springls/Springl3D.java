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
package org.imagesci.springls;


import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.imagesci.utility.GeomUtil;


// TODO: Auto-generated Javadoc
/**
 * The Class Springl3D represents a Springl and all its metadata.
 */
public class Springl3D implements Comparable<Springl3D> {

	/**
	 * The Interface Neighbor.
	 */
	public static interface Neighbor {

		/**
		 * Equals.
		 * 
		 * @param nbr
		 *            the nbr
		 * 
		 * @return true, if successful
		 */
		public boolean equals(Neighbor nbr);

		/**
		 * Gets the capsule.
		 * 
		 * @return the capsule
		 */
		public Springl3D getCapsule();

		/**
		 * Gets the neighbor point.
		 * 
		 * @return the neighbor point
		 */
		public Point3f getNeighborPoint();
	}

	/**
	 * The Class Neighbor.
	 */
	public static class NeighborEdge implements Neighbor {

		/** The Constant BYTE_SIZE. */
		public static final int BYTE_SIZE = (3 * Float.SIZE + 2 * Integer.SIZE) / 8;
		/** The capsule. */
		public Springl3D capsule;

		/** The index. */
		public int index = -1;

		/** The pt. */
		protected Point3f pt;

		/**
		 * Instantiates a new neighbor.
		 */
		public NeighborEdge() {

		}

		/**
		 * Instantiates a new neighbor.
		 * 
		 * @param capsule
		 *            the capsule
		 * @param pt
		 *            the pt
		 * @param index
		 *            the index
		 */
		public NeighborEdge(Springl3D capsule, Point3f pt, int index) {
			this.capsule = capsule;
			this.index = index;
			this.pt = pt;
		}

		/**
		 * Equals.
		 * 
		 * @param nbr
		 *            the nbr
		 * 
		 * @return true, if successful
		 */
		@Override
		public boolean equals(Neighbor nbr) {
			return (capsule == ((NeighborEdge) nbr).capsule && ((NeighborEdge) nbr).index == index);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * edu.jhu.cs.cisst.algorithms.springls.Springl3D.Neighbor#getCapsule()
		 */
		@Override
		public Springl3D getCapsule() {
			return capsule;
		}

		/**
		 * Gets the neighbor point.
		 * 
		 * @return the neighbor point
		 */
		@Override
		public Point3f getNeighborPoint() {
			Point3f tmp = new Point3f();
			Springl3D.distanceSquared(pt, capsule.vertexes[index],
					capsule.vertexes[(index + 1) % 3], tmp);
			return tmp;
		}

	}

	/** The Constant BYTE_SIZE. */
	public static final int BYTE_SIZE = (16 * Float.SIZE) / 8;

	/** The id. */
	public int id = 0;

	/** The level set value. */
	public float levelSetValue = 0;

	/** The neighbors. */

	protected HashSet<Springl3D> neighborHashSet;

	/** The particle. */
	public Point3f particle;

	/** The reference id. */
	public int referenceId = -1;

	/** The reference point. */
	public Point3f referencePoint;

	/** The vertexes. */
	public Point3f[] vertexes;

	/** The vertex neighbors. */
	LinkedList<Springl3D.Neighbor>[] vertexNeighbors;

	/**
	 * Instantiates a new capsule3 d.
	 * 
	 * @param pt1
	 *            the pt1
	 * @param pt2
	 *            the pt2
	 * @param pt3
	 *            the pt3
	 */
	public Springl3D(Point3f pt1, Point3f pt2, Point3f pt3) {
		this.particle = new Point3f();
		particle.add(pt1);
		particle.add(pt2);
		particle.add(pt3);
		particle.scale(0.333333f);
		this.referencePoint = new Point3f(particle);
		this.referenceId = -1;
		this.vertexes = new Point3f[3];
		vertexes[0] = pt1;
		vertexes[1] = pt2;
		vertexes[2] = pt3;
	}

	/**
	 * Instantiates a new capsule3 d.
	 * 
	 * @param pt1
	 *            the pt1
	 * @param pt2
	 *            the pt2
	 * @param pt3
	 *            the pt3
	 * @param particle
	 *            the particle
	 */
	public Springl3D(Point3f pt1, Point3f pt2, Point3f pt3, Point3f particle) {
		this.particle = particle;

		this.referencePoint = new Point3f(particle);
		this.vertexes = new Point3f[3];
		vertexes[0] = pt1;
		vertexes[1] = pt2;
		vertexes[2] = pt3;
	}

	/**
	 * Instantiates a new capsule3 d.
	 * 
	 * @param pt1
	 *            the pt1
	 * @param pt2
	 *            the pt2
	 * @param pt3
	 *            the pt3
	 * @param particle
	 *            the particle
	 * @param referencePoint
	 *            the reference point
	 */
	public Springl3D(Point3f pt1, Point3f pt2, Point3f pt3, Point3f particle,
			Point3f referencePoint) {
		this.particle = particle;
		this.referencePoint = referencePoint;
		this.vertexes = new Point3f[3];
		vertexes[0] = pt1;
		vertexes[1] = pt2;
		vertexes[2] = pt3;
	}

	/**
	 * Distance to a point using parametric calculations.
	 * 
	 * @param p
	 *            the p
	 * @param capsule
	 *            the capsule
	 * @return the double
	 */
	public static double distanceSquared(Point3f p, Springl3D capsule) {
		double distanceSquared = 0;

		Vector3f v0 = new Vector3f(capsule.vertexes[0]);
		Vector3f v1 = new Vector3f(capsule.vertexes[1]);
		Vector3f v2 = new Vector3f(capsule.vertexes[2]);

		Point3f P = p;
		Vector3f B = v0;

		Vector3f e0 = new Vector3f();
		e0.sub(v1, v0);

		Vector3f e1 = new Vector3f();
		e1.sub(v2, v0);

		float a = e0.dot(e0);
		float b = e0.dot(e1);
		float c = e1.dot(e1);

		Vector3f dv = new Vector3f();
		dv.sub(B, P);
		float d = e0.dot(dv);
		float e = e1.dot(dv);
		dv.dot(dv);

		// Determine which region contains s, t
		int region = 0;

		float det = a * c - b * b;
		float s = b * e - c * d;
		float t = b * d - a * e;

		if (s + t <= det) {
			if (s < 0) {
				if (t < 0) {
					region = 4;
				} else {
					region = 3;
				}
			} else if (t < 0) {
				region = 5;
			} else {
				region = 0;
			}
		} else {
			if (s < 0) {
				region = 2;
			} else if (t < 0) {
				region = 6;
			} else {
				region = 1;
			}
		}

		/* Region Cases ------------------------------------------------------ */
		// System.out.println("\nThe Point is in Region: " + region + "\n");
		// Parametric Triangle Point
		Point3f T = new Point3f();

		if (region == 0) {// Region 0
			float invDet = 1 / det;
			s *= invDet;
			t *= invDet;

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			distanceSquared = P.distanceSquared(T);

		} else if (region == 1) {// Region 1
			float numer = c + e - b - d;

			if (numer < +0) {
				s = 0;
			} else {
				float denom = a - 2 * b + c;
				s = (numer >= denom ? 1 : numer / denom);
			}
			t = 1 - s;

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			distanceSquared = P.distanceSquared(T);

		} else if (region == 2) {// Region 2
			float tmp0 = b + d;
			float tmp1 = c + e;

			if (tmp1 > tmp0) {
				float numer = tmp1 - tmp0;
				float denom = a - 2 * b + c;
				s = (numer >= denom ? 1 : numer / denom);
				t = 1 - s;
			} else {
				s = 0;
				t = (tmp1 <= 0 ? 1 : (e >= 0 ? 0 : -e / c));
			}

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			distanceSquared = P.distanceSquared(T);

		} else if (region == 3) {// Region 3
			s = 0;
			t = (e >= 0 ? 0 : (-e >= c ? 1 : -e / c));

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			distanceSquared = P.distanceSquared(T);

		} else if (region == 4) {// Region 4
			float tmp0 = c + e;
			float tmp1 = a + d;

			if (tmp0 > tmp1) {
				s = 0;
				t = (tmp1 <= 0 ? 1 : (e >= 0 ? 0 : -e / c));
			} else {
				t = 0;
				s = (tmp1 <= 0 ? 1 : (d >= 0 ? 0 : -d / a));
			}

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			distanceSquared = P.distanceSquared(T);

		} else if (region == 5) {// Region 5
			t = 0;
			s = (d >= 0 ? 0 : (-d >= a ? 1 : -d / a));

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			distanceSquared = P.distanceSquared(T);

		} else {// Region 6
			float tmp0 = b + e;
			float tmp1 = a + d;

			if (tmp1 > tmp0) {
				float numer = tmp1 - tmp0;
				float denom = c - 2 * b + a;
				t = (numer >= denom ? 1 : numer / denom);
				s = 1 - t;
			} else {
				t = 0;
				s = (tmp1 <= 0 ? 1 : (d >= 0 ? 0 : -d / a));
			}

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			distanceSquared = P.distanceSquared(T);

		}
		return distanceSquared;
	}

	/**
	 * Create a point on a parametric triangle T(s,t).
	 * 
	 * @param e0
	 *            vector e0
	 * @param e1
	 *            vector e1
	 * @param s
	 *            the s parameter
	 * @param t
	 *            the t parameter
	 * @param B
	 *            the base vector
	 * @return T The point on the triangle
	 */
	public static final Point3f parametricTriangle(Vector3f e0, Vector3f e1,
			float s, float t, Vector3f B) {
		Vector3f Bsum = new Vector3f();

		Vector3f se0 = new Vector3f();
		se0.scale(s, e0);

		Vector3f te1 = new Vector3f();
		te1.scale(t, e1);

		Bsum.add(B, se0);
		Bsum.add(te1);

		Point3f T = new Point3f(Bsum);

		return T;
	}

	/**
	 * Gets the neighbors.
	 * 
	 * @return the neighbors
	 */
	public HashSet<Springl3D> getNeighbors() {
		if (neighborHashSet == null) {
			neighborHashSet = new HashSet<Springl3D>();
			for (int i = 0; i < 3; i++) {
				for (Springl3D.Neighbor nbr : getVertexNeighbors()[i]) {
					neighborHashSet.add(nbr.getCapsule());
				}
			}
		}
		return neighborHashSet;
	}

	/**
	 * Gets the vertex neighbors.
	 * 
	 * @return the vertex neighbors
	 */
	public List<Springl3D.Neighbor>[] getVertexNeighbors() {
		if (vertexNeighbors == null) {
			vertexNeighbors = new LinkedList[3];
			vertexNeighbors[0] = new LinkedList<Neighbor>();
			vertexNeighbors[1] = new LinkedList<Neighbor>();
			vertexNeighbors[2] = new LinkedList<Neighbor>();
		}
		return vertexNeighbors;
	}

	/**
	 * Gets the normal.
	 * 
	 * @return the normal
	 */
	public Vector3f getNormal() {
		Vector3f normal = cross(vertexes[0], vertexes[1], vertexes[2]);
		GeomUtil.normalize(normal);
		return normal;
	}

	/**
	 * Cross.
	 * 
	 * @param pa
	 *            the pa
	 * @param pb
	 *            the pb
	 * @param pc
	 *            the pc
	 * @return the vector3f
	 */
	public static Vector3f cross(Point3f pa, Point3f pb, Point3f pc) {
		Point3f p1 = new Point3f((pb.x - pa.x), (pb.y - pa.y), (pb.z - pa.z));
		Point3f p2 = new Point3f((pc.x - pa.x), (pc.y - pa.y), (pc.z - pa.z));
		Vector3f p3 = new Vector3f(p1.y * p2.z - p1.z * p2.y, p1.z * p2.x
				- p1.x * p2.z, p1.x * p2.y - p1.y * p2.x);
		return p3;
	}

	/**
	 * Distance squared.
	 * 
	 * @param pt
	 *            the pt
	 * @param pt1
	 *            the pt1
	 * @param pt2
	 *            the pt2
	 * @param lastClosestSegmentPoint
	 *            the last closest segment point
	 * @return the double
	 */
	public static double distanceSquared(Point3f pt, Point3f pt1, Point3f pt2,
			Point3f lastClosestSegmentPoint) {
		Vector3f dir = new Vector3f();
		dir.sub(pt2, pt1);
		double len = dir.length();
		dir.scale(1.0f / ((float) len));
		Vector3f diff = new Vector3f();
		diff.sub(pt, pt1);

		double mSegmentParameter = dir.dot(diff);
		if (0 < mSegmentParameter) {
			if (mSegmentParameter < len) {
				lastClosestSegmentPoint.x = dir.x;
				lastClosestSegmentPoint.y = dir.y;
				lastClosestSegmentPoint.z = dir.z;
				lastClosestSegmentPoint.scale((float) mSegmentParameter);
				lastClosestSegmentPoint.add(pt1);
			} else {
				lastClosestSegmentPoint.x = pt2.x;
				lastClosestSegmentPoint.y = pt2.y;
				lastClosestSegmentPoint.z = pt2.z;
			}
		} else {
			lastClosestSegmentPoint.x = pt1.x;
			lastClosestSegmentPoint.y = pt1.y;
			lastClosestSegmentPoint.z = pt1.z;
		}
		return pt.distanceSquared(lastClosestSegmentPoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Springl3D capsule) {
		return (this.hashCode() - capsule.hashCode());
	}

	/**
	 * Deserialize.
	 * 
	 * @param buffer
	 *            the buffer
	 */
	public void deserialize(ByteBuffer buffer) {
		particle.x = buffer.getFloat();
		particle.y = buffer.getFloat();
		particle.z = buffer.getFloat();
		levelSetValue = buffer.getFloat();
		vertexes[0].x = buffer.getFloat();
		vertexes[0].y = buffer.getFloat();
		vertexes[0].z = buffer.getFloat();
		referencePoint.x = buffer.getFloat();
		vertexes[1].x = buffer.getFloat();
		vertexes[1].y = buffer.getFloat();
		vertexes[1].z = buffer.getFloat();
		referencePoint.y = buffer.getFloat();
		vertexes[2].x = buffer.getFloat();
		vertexes[2].y = buffer.getFloat();
		vertexes[2].z = buffer.getFloat();
		referencePoint.z = buffer.getFloat();
	}

	/**
	 * Serialize.
	 * 
	 * @param buffer
	 *            the buffer
	 */
	public void serialize(ByteBuffer buffer) {
		buffer.putFloat(particle.x);
		buffer.putFloat(particle.y);
		buffer.putFloat(particle.z);
		buffer.putFloat(0);
		buffer.putFloat(vertexes[0].x);
		buffer.putFloat(vertexes[0].y);
		buffer.putFloat(vertexes[0].z);
		buffer.putFloat(referencePoint.x);
		buffer.putFloat(vertexes[1].x);
		buffer.putFloat(vertexes[1].y);
		buffer.putFloat(vertexes[1].z);
		buffer.putFloat(referencePoint.y);
		buffer.putFloat(vertexes[2].x);
		buffer.putFloat(vertexes[2].y);
		buffer.putFloat(vertexes[2].z);
		buffer.putFloat(referencePoint.z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "(" + particle.x + "," + particle.y + "," + particle.z
				+ ") ";
		for (int i = 0; i < 3; i++) {
			str += "(" + vertexes[i].x + "," + vertexes[i].y + ","
					+ vertexes[i].z + ") ";
		}
		return str;
	}

}
