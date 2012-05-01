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
package imagesci.springls;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

// TODO: Auto-generated Javadoc
/**
 * The Class Springl2D represents a Springl and all its metadata.
 */
public class Springl2D implements Comparable<Springl2D> {

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
		public Springl2D getCapsule();

		/**
		 * Gets the neighbor point.
		 * 
		 * @return the neighbor point
		 */
		public Point2f getNeighborPoint();
	}

	/**
	 * The Class Neighbor.
	 */
	public static class NeighborEdge implements Neighbor {

		/** The Constant BYTE_SIZE. */
		public static final int BYTE_SIZE = (3 * Float.SIZE + 2 * Integer.SIZE) / 8;
		/** The capsule. */
		public Springl2D capsule;

		/** The index. */
		public int index = -1;

		/** The pt. */
		protected Point2f pt;

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
		public NeighborEdge(Springl2D capsule, Point2f pt, int index) {
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
		 * edu.jhu.cs.cisst.algorithms.springls.Springl2D.Neighbor#getCapsule()
		 */
		@Override
		public Springl2D getCapsule() {
			return capsule;
		}

		/**
		 * Gets the neighbor point.
		 * 
		 * @return the neighbor point
		 */
		@Override
		public Point2f getNeighborPoint() {
			return new Point2f(capsule.vertexes[index]);
		}

	}

	/** The Constant BYTE_SIZE. */
	public static final int BYTE_SIZE = (10 * Float.SIZE) / 8;

	/** The boundary points. */
	public Point2f[] boundaryPoints;

	/** The id. */
	public int id = 0;

	/** The level set value. */
	public float levelSetValue = 0;

	/** The neighbors. */

	protected HashSet<Springl2D> neighborHashSet;

	/** The particle. */
	public Point2f particle;

	/** The reference id. */
	public int referenceId = -1;

	/** The reference point. */
	public Point2f referencePoint;

	/** The vertexes. */
	public Point2f[] vertexes;

	/** The vertex neighbors. */
	LinkedList<Springl2D.Neighbor>[] vertexNeighbors;

	/**
	 * Instantiates a new capsule3 d.
	 * 
	 * @param pt1
	 *            the pt1
	 * @param pt2
	 *            the pt2
	 */
	public Springl2D(Point2f pt1, Point2f pt2) {
		this.particle = new Point2f();
		particle.add(pt1);
		particle.add(pt2);
		particle.scale(0.5f);
		this.referencePoint = new Point2f(particle);
		this.referenceId = -1;
		this.vertexes = new Point2f[2];
		vertexes[0] = pt1;
		vertexes[1] = pt2;
	}

	/**
	 * Instantiates a new capsule3 d.
	 * 
	 * @param pt1
	 *            the pt1
	 * @param pt2
	 *            the pt2
	 * @param particle
	 *            the particle
	 */
	public Springl2D(Point2f pt1, Point2f pt2, Point2f particle) {
		this.particle = particle;

		this.referencePoint = new Point2f(particle);
		this.vertexes = new Point2f[2];
		vertexes[0] = pt1;
		vertexes[1] = pt2;
	}

	/**
	 * Instantiates a new capsule3d.
	 * 
	 * @param pt1
	 *            the pt1
	 * @param pt2
	 *            the pt2
	 * @param particle
	 *            the particle
	 * @param referencePoint
	 *            the reference point
	 */
	public Springl2D(Point2f pt1, Point2f pt2, Point2f particle,
			Point2f referencePoint) {
		this.particle = particle;
		this.referencePoint = referencePoint;
		this.vertexes = new Point2f[2];
		vertexes[0] = pt1;
		vertexes[1] = pt2;
	}

	/**
	 * Distance squared.
	 * 
	 * @param pt
	 *            the pt
	 * @param capsule
	 *            the capsule
	 * @return the double
	 */
	public static double distanceSquared(Point2f pt, Springl2D capsule) {

		Point2f lastClosestSegmentPoint = new Point2f();
		return distanceSquared(pt, capsule, lastClosestSegmentPoint);
	}

	/**
	 * Distance squared.
	 * 
	 * @param pt
	 *            the pt
	 * @param capsule
	 *            the capsule
	 * @param lastClosestSegmentPoint
	 *            the last closest segment point
	 * @return the double
	 */
	public static double distanceSquared(Point2f pt, Springl2D capsule,
			Point2f lastClosestSegmentPoint) {
		Vector2f dir = new Vector2f();
		Point2f pt1 = capsule.vertexes[0];
		Point2f pt2 = capsule.vertexes[1];
		dir.sub(pt2, pt1);
		double len = dir.length();
		dir.scale(1.0f / ((float) len));
		Vector2f diff = new Vector2f();
		diff.sub(pt, pt1);
		double mSegmentParameter = dir.dot(diff);
		if (0 < mSegmentParameter) {
			if (mSegmentParameter < len) {
				lastClosestSegmentPoint.x = dir.x;
				lastClosestSegmentPoint.y = dir.y;
				lastClosestSegmentPoint.scale((float) mSegmentParameter);
				lastClosestSegmentPoint.add(pt1);
			} else {
				lastClosestSegmentPoint.x = pt2.x;
				lastClosestSegmentPoint.y = pt2.y;
			}
		} else {
			lastClosestSegmentPoint.x = pt1.x;
			lastClosestSegmentPoint.y = pt1.y;
		}
		return pt.distanceSquared(lastClosestSegmentPoint);
	}

	/**
	 * Gets the neighbors.
	 * 
	 * @return the neighbors
	 */
	public HashSet<Springl2D> getNeighbors() {
		if (neighborHashSet == null) {
			neighborHashSet = new HashSet<Springl2D>();
			for (int i = 0; i < 2; i++) {
				for (Springl2D.Neighbor nbr : getVertexNeighbors()[i]) {
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
	public List<Springl2D.Neighbor>[] getVertexNeighbors() {
		if (vertexNeighbors == null) {
			vertexNeighbors = new LinkedList[2];
			vertexNeighbors[0] = new LinkedList<Neighbor>();
			vertexNeighbors[1] = new LinkedList<Neighbor>();
		}
		return vertexNeighbors;
	}

	/**
	 * Gets the normal.
	 * 
	 * @return the normal
	 */
	public Vector2f getNormal() {
		Vector2f normal = cross(vertexes[0], vertexes[1]);
		normal.normalize();
		return normal;
	}

	/**
	 * Cross.
	 * 
	 * @param pa
	 *            the pa
	 * @param pb
	 *            the pb
	 * @return the vector2f
	 */
	public static Vector2f cross(Point2f pa, Point2f pb) {
		Vector2f p3 = new Vector2f(pa.y - pb.y, pb.x - pa.x);
		return p3;
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
	public static final Point2f parametricTriangle(Vector2f e0, Vector2f e1,
			float s, float t, Vector2f B) {
		Vector2f Bsum = new Vector2f();

		Vector2f se0 = new Vector2f();
		se0.scale(s, e0);

		Vector2f te1 = new Vector2f();
		te1.scale(t, e1);

		Bsum.add(B, se0);
		Bsum.add(te1);

		Point2f T = new Point2f(Bsum);

		return T;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Springl2D capsule) {
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
		levelSetValue = buffer.getFloat();
		vertexes[0].x = buffer.getFloat();
		vertexes[0].y = buffer.getFloat();
		referencePoint.x = buffer.getFloat();
		vertexes[1].x = buffer.getFloat();
		vertexes[1].y = buffer.getFloat();
		referencePoint.y = buffer.getFloat();
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
		buffer.putFloat(referencePoint.x);
		buffer.putFloat(referencePoint.y);
		buffer.putFloat(vertexes[0].x);
		buffer.putFloat(vertexes[0].y);
		buffer.putFloat(vertexes[1].x);
		buffer.putFloat(vertexes[1].y);
		buffer.putFloat(0);
		buffer.putFloat(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "(" + particle.x + "," + particle.y + ") ";
		for (int i = 0; i < 3; i++) {
			str += "(" + vertexes[i].x + "," + vertexes[i].y + ") ";
		}
		return str;
	}

	/**
	 * Update.
	 *
	 */
	public void update() {
		Point2f startPoint = vertexes[0];
		Point2f endPoint = vertexes[1];
		Point2f tanget = new Point2f();
		Point2f point = particle;
		float tangetPositiveExtent = endPoint.distance(particle);
		float tangetNegativeExtent = startPoint.distance(particle);
		/** The tanget negative extent. */
		tanget.sub(endPoint, startPoint);
		tanget.scale(1.0f / (float) Math.sqrt(tanget.x * tanget.x + tanget.y
				* tanget.y));
		Point2f normal = new Point2f(-tanget.y, tanget.x);
		double u, v;
		final float spacing = 0.2f;
		final float normalExtent = 0.5f;
		double l = normalExtent;
		double hemiLength = Math.PI * l;
		int N = (int) Math
				.ceil((2 * hemiLength + 2 * (tangetNegativeExtent + tangetPositiveExtent))
						/ (spacing));
		double effectiveSpacing = (2 * hemiLength + 2 * (tangetNegativeExtent + tangetPositiveExtent))
				/ N;
		boundaryPoints = new Point2f[N];
		u = 0;
		v = 0;
		double t = 0;
		for (int i = 0; i < N; i++) {
			if (t < tangetPositiveExtent) {
				u = t;
				v = l;
			} else if (t >= tangetPositiveExtent
					&& t < tangetPositiveExtent + hemiLength) {
				double r = t - tangetPositiveExtent;
				u = tangetPositiveExtent + l * Math.cos(0.5 * Math.PI - r / l);
				v = l * Math.sin(0.5 * Math.PI - r / l);
			} else if (t >= hemiLength + tangetPositiveExtent
					&& t < 2 * tangetPositiveExtent + tangetNegativeExtent
							+ hemiLength) {
				double r = t - hemiLength - tangetPositiveExtent;
				u = tangetPositiveExtent - r;
				v = -l;
			} else if (t >= 2 * tangetPositiveExtent + tangetNegativeExtent
					+ hemiLength
					&& t < 2 * tangetPositiveExtent + tangetNegativeExtent + 2
							* hemiLength) {
				double r = t
						- (2 * tangetPositiveExtent + tangetNegativeExtent)
						- hemiLength;
				u = -tangetNegativeExtent + l
						* Math.cos(-0.5 * Math.PI - r / l);
				v = l * Math.sin(-0.5 * Math.PI - r / l);
			} else {
				u = t
						- (2 * (tangetPositiveExtent + tangetNegativeExtent) + 2 * hemiLength);
				v = l;
			}
			boundaryPoints[i] = new Point2f((float) (point.x + u * tanget.x + v
					* normal.x),
					(float) (point.y + u * tanget.y + v * normal.y));
			t += effectiveSpacing;
		}
	}

}
