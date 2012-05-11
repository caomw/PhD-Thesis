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
package org.imagesci.utility;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point2d;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

// TODO: Auto-generated Javadoc
/**
 * The Class GeomUtil.
 */
public class GeomUtil {

	/**
	 * Unwrap.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * 
	 * @return the point2d[]
	 */
	public static Point2d[] unwrap(Point3f p1, Point3f p2, Point3f p3) {
		Point2d s1 = toUnitSpherical(p1);
		Matrix A = rotateInverseMatrix(s1.x, s1.y);
		s1 = toUnitSpherical(multMatrix(A, p1));
		Point2d s2 = toUnitSpherical(multMatrix(A, p2));
		Point2d s3 = toUnitSpherical(multMatrix(A, p3));
		return new Point2d[] { s1, s2, s3 };
	}

	/**
	 * To unit spherical.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the point2d
	 */
	public static Point2d toUnitSpherical(Tuple3f p) {
		return new Point2d(Math.atan2(p.y, p.x), Math.acos(p.z));
	}

	/**
	 * Rotate inverse matrix.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the matrix
	 */
	public static Matrix rotateInverseMatrix(Point3f p) {
		return rotateInverseMatrix(Math.atan2(p.y, p.x), Math.acos(p.z));
	}

	/**
	 * Rotate inverse matrix.
	 * 
	 * @param theta
	 *            the theta
	 * @param phi
	 *            the phi
	 * 
	 * @return the matrix
	 */
	public static Matrix rotateInverseMatrix(double theta, double phi) {
		double cost = Math.cos(-theta);
		double sint = Math.sin(-theta);
		double cosp = Math.cos(0.5 * Math.PI - phi);
		double sinp = Math.sin(0.5 * Math.PI - phi);
		Matrix rotPhi = new Matrix(new double[][] { { cosp, 0, sinp },
				{ 0, 1, 0 }, { -sinp, 0, cosp } });
		Matrix rotTheta = new Matrix(new double[][] { { cost, -sint, 0 },
				{ sint, cost, 0 }, { 0, 0, 1 } });
		return rotPhi.times(rotTheta);
	}

	/*
	 * public void rewrap(Point2d p){ if(p.y<0){ p.y=-p.y;
	 * if(p.x<0)p.x+=Math.PI; else p.x-=Math.PI; } if(p.y>Math.PI){
	 * p.y=2*Math.PI-p.y; if(p.x<0)p.x+=Math.PI; else p.x-=Math.PI; }
	 * if(p.x>Math.PI)p.x-=2*Math.PI; if(p.x<-Math.PI)p.x+=2*Math.PI; }
	 */
	/**
	 * Mult matrix.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the point3f
	 */
	public static Point3f multMatrix(Matrix A, Point3f p) {
		if (p == null) {
			return new Point3f();
		} else {
			Matrix v = new Matrix(3, 1);
			v.set(0, 0, p.x);
			v.set(1, 0, p.y);
			v.set(2, 0, p.z);
			Matrix result = A.times(v);
			return new Point3f((float) result.get(0, 0), (float) result.get(1,
					0), (float) result.get(2, 0));
		}
	}

	/**
	 * Jacobian cartesian to sphere.
	 * 
	 * @param q1
	 *            the q1
	 * @param q2
	 *            the q2
	 * @param q3
	 *            the q3
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * @param p
	 *            the p
	 * 
	 * @return the matrix
	 */
	public static Matrix jacobianCartesianToSphere(Point3f q1, Point3f q2,
			Point3d q3, Point2d p1, Point2d p2, Point2d p3, Point2d p) {
		Matrix J1 = jacobianCartesianToStereo(q1, q2, q3, p1, p2, p3);
		Matrix J2 = jacobianStereoToSphere(p);
		Matrix J3 = J2.times(J1);
		return J3;
	}

	/**
	 * Jacobian cartesian to stereo.
	 * 
	 * @param q1
	 *            the q1
	 * @param q2
	 *            the q2
	 * @param q3
	 *            the q3
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * 
	 * @return the matrix
	 */
	public static Matrix jacobianCartesianToStereo(Point3f q1, Point3f q2,
			Point3d q3, Point2d p1, Point2d p2, Point2d p3) {
		Point3f jacx = new Point3f();
		float A = (float) (1.0 / ((p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x)
				* (p2.y - p1.y)));
		jacx.x = A
				* (float) (q1.x * (p2.y - p3.y) + q2.x * (p3.y - p1.y) + q3.x
						* (p1.y - p2.y));
		jacx.y = A
				* (float) (q1.y * (p2.y - p3.y) + q2.y * (p3.y - p1.y) + q3.y
						* (p1.y - p2.y));
		jacx.z = A
				* (float) (q1.z * (p2.y - p3.y) + q2.z * (p3.y - p1.y) + q3.z
						* (p1.y - p2.y));

		Point3f jacy = new Point3f();
		jacy.x = A
				* (float) (q1.x * (p3.x - p2.x) + q2.x * (p1.x - p3.x) + q3.x
						* (p2.x - p1.x));
		jacy.y = A
				* (float) (q1.y * (p3.x - p2.x) + q2.y * (p1.x - p3.x) + q3.y
						* (p2.x - p1.x));
		jacy.z = A
				* (float) (q1.z * (p3.x - p2.x) + q2.z * (p1.x - p3.x) + q3.z
						* (p2.x - p1.x));
		Matrix m = new Matrix(2, 3);
		m.set(0, 0, 1 / jacx.x);
		m.set(0, 1, 1 / jacx.y);
		m.set(0, 2, 1 / jacx.z);

		m.set(1, 0, 1 / jacy.x);
		m.set(1, 1, 1 / jacy.y);
		m.set(1, 2, 1 / jacy.z);
		return m;
	}

	/**
	 * Jacobian stereo to sphere.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the matrix
	 */
	public static Matrix jacobianStereoToSphere(Point2d p) {
		Matrix J = new Matrix(3, 2);
		double r = (1 + p.x * p.x + p.y * p.y);
		r = 1 / (r * r);
		J.set(0, 0, -2 * (p.x * p.x - p.y * p.y - 1) * r);
		J.set(1, 0, -4 * p.x * p.y * r);
		J.set(2, 0, 4 * p.x * r);

		J.set(0, 1, -4 * p.x * p.y * r);
		J.set(1, 1, -2 * (p.y * p.y - p.x * p.x - 1) * r);
		J.set(2, 1, 4 * p.y * r);
		return J;
	}

	/**
	 * Slerp.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * @param t
	 *            the t
	 * 
	 * @return the vector3f
	 */
	public static Vector3f slerp(Tuple3f v1, Tuple3f v2, double t) {
		double l1 = Math.abs(GeomUtil.length(v1) - 1);
		double l2 = Math.abs(GeomUtil.length(v2) - 1);
		if (l1 > 0.01) {
			return new Vector3f(v2);
		} else if (l2 > 0.01) {
			return new Vector3f(v1);
		}
		double ang = angle(v1, v2);
		double sina = Math.sin(ang);
		double w1 = (Math.abs(sina) < 1E-10) ? (1 - t) : Math
				.sin(ang * (1 - t)) / sina;
		double w2 = (Math.abs(sina) < 1E-10) ? t : Math.sin(ang * (t)) / sina;
		Vector3f v3 = new Vector3f((float) (w1 * v1.x + w2 * v2.x), (float) (w1
				* v1.y + w2 * v2.y), (float) (w1 * v1.z + w2 * v2.z));
		normalize(v3);
		return v3;
	}

	/**
	 * Angle.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * 
	 * @return the double
	 */
	public static double angle(Tuple3f p1, Tuple3f p2) {
		double l1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y + p1.z * p1.z);
		double l2 = Math.sqrt(p2.x * p2.x + p2.y * p2.y + p2.z * p2.z);
		return Math.acos(Math.min(
				1,
				Math.max(-1, (p1.x * p2.x + p1.y * p2.y + p1.z * p2.z)
						/ (l1 * l2))));
	}

	/**
	 * Normalize.
	 * 
	 * @param p1
	 *            the p1
	 */
	public static void normalize(Tuple3f p1) {
		double l1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y + p1.z * p1.z);
		p1.scale((float) ((l1 > 1E-6) ? 1 / l1 : 0));
	}

	/**
	 * Rotate inverse matrix3d.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the matrix3d
	 */
	public static Matrix3d rotateInverseMatrix3d(Point3f p) {
		return rotateInverseMatrix3d(Math.atan2(p.y, p.x), Math.acos(p.z));
	}

	/**
	 * Rotate inverse matrix3d.
	 * 
	 * @param theta
	 *            the theta
	 * @param phi
	 *            the phi
	 * 
	 * @return the matrix3d
	 */
	public static Matrix3d rotateInverseMatrix3d(double theta, double phi) {
		double cost = Math.cos(-theta);
		double sint = Math.sin(-theta);
		double cosp = Math.cos(0.5 * Math.PI - phi);
		double sinp = Math.sin(0.5 * Math.PI - phi);
		Matrix3d rotPhi = new Matrix3d(new double[] { cosp, 0, sinp, 0, 1, 0,
				-sinp, 0, cosp });
		Matrix3d rotTheta = new Matrix3d(new double[] { cost, -sint, 0, sint,
				cost, 0, 0, 0, 1 });
		rotPhi.mul(rotTheta);
		return rotPhi;
	}

	/**
	 * Slerp.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * @param t
	 *            the t
	 * 
	 * @return the vector2d
	 */
	public static Vector2d slerp(Tuple2d v1, Tuple2d v2, double t) {
		double ang = angle(v1, v2);
		double sina = Math.sin(ang);
		double w1 = (Math.abs(sina) < 1E-10) ? (1 - t) : Math
				.sin(ang * (1 - t)) / sina;
		double w2 = (Math.abs(sina) < 1E-10) ? t : Math.sin(ang * (t)) / sina;
		Vector2d v3 = new Vector2d((float) (w1 * v1.x + w2 * v2.x), (float) (w1
				* v1.y + w2 * v2.y));
		v3.normalize();
		return v3;
	}

	/**
	 * Angle.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * 
	 * @return the double
	 */
	public static double angle(Tuple2d p1, Tuple2d p2) {
		double l1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y);
		double l2 = Math.sqrt(p2.x * p2.x + p2.y * p2.y);
		return Math.acos(Math.min(1,
				Math.max(-1, (p1.x * p2.x + p1.y * p2.y) / (l1 * l2))));
	}

	/**
	 * To spherical.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the point3d
	 */
	public static Point3d toSpherical(Tuple3f p) {
		double r = length(p);
		if (r < 1E-10) {
			return new Point3d(0, 0, 0);
		} else {
			return new Point3d(r, Math.atan2(p.y / r, p.x / r), Math.acos(p.z
					/ r));
		}
	}

	/**
	 * Length.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the double
	 */
	public static double length(Tuple3f p) {
		return Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
	}

	/**
	 * Angle.
	 * 
	 * @param v0
	 *            the v0
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * 
	 * @return the double
	 */
	public static/* synchronized */double angle(Point3f v0, Point3f v1,
			Point3f v2) {
		Vector3f v = new Vector3f();
		Vector3f w = new Vector3f();
		v.sub(v0, v2);
		w.sub(v1, v2);
		return Math.acos(v.dot(w) / (v.length() * w.length()));
	}

	// Bisect angle at point 3
	/**
	 * Angle bisect.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * 
	 * @return the point3f
	 */
	public static Point3f angleBisect(Point3f p1, Point3f p2, Point3f p3) {
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Vector3f v3 = new Vector3f();
		Vector3f v4 = new Vector3f();
		v1.sub(p1, p3);
		v3.sub(p1, p3);

		v2.sub(p2, p3);
		v4.sub(p1, p2);

		v1.normalize();
		v2.normalize();

		v2.sub(v2, v1);
		double t = v3.dot(v2) / v4.dot(v2);

		v4.scale(-(float) t);

		Point3f p = new Point3f(p1);
		p.add(v4);
		return p;
	}

	/**
	 * Cos angle.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * 
	 * @return the double
	 */
	public static double cosAngle(Vector2d v1, Vector2d v2) {
		return v1.dot(v2) / (v1.length() * v2.length());
	}

	/**
	 * Cos angle.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * 
	 * @return the double
	 */
	public static double cosAngle(Vector3f v1, Vector3f v2) {
		return v1.dot(v2) / (v1.length() * v2.length());
	}

	/**
	 * Cross product.
	 * 
	 * @param u
	 *            the u
	 * @param v
	 *            the v
	 * 
	 * @return the double
	 */
	public static double crossProduct(Tuple2d u, Tuple2d v) {
		return u.x * v.y - v.x * u.y;
	}

	/**
	 * Determinant.
	 * 
	 * @param fX0
	 *            the f x0
	 * @param fY0
	 *            the f y0
	 * @param fZ0
	 *            the f z0
	 * @param fX1
	 *            the f x1
	 * @param fY1
	 *            the f y1
	 * @param fZ1
	 *            the f z1
	 * @param fX2
	 *            the f x2
	 * @param fY2
	 *            the f y2
	 * @param fZ2
	 *            the f z2
	 * 
	 * @return the double
	 */
	public static double determinant(double fX0, double fY0, double fZ0,
			double fX1, double fY1, double fZ1, double fX2, double fY2,
			double fZ2) {
		double fC00 = fY1 * fZ2 - fY2 * fZ1;
		double fC01 = fY2 * fZ0 - fY0 * fZ2;
		double fC02 = fY0 * fZ1 - fY1 * fZ0;
		return fX0 * fC00 + fX1 * fC01 + fX2 * fC02;
	}

	/**
	 * Determinant.
	 * 
	 * @param fX0
	 *            the f x0
	 * @param fY0
	 *            the f y0
	 * @param fZ0
	 *            the f z0
	 * @param fW0
	 *            the f w0
	 * @param fX1
	 *            the f x1
	 * @param fY1
	 *            the f y1
	 * @param fZ1
	 *            the f z1
	 * @param fW1
	 *            the f w1
	 * @param fX2
	 *            the f x2
	 * @param fY2
	 *            the f y2
	 * @param fZ2
	 *            the f z2
	 * @param fW2
	 *            the f w2
	 * @param fX3
	 *            the f x3
	 * @param fY3
	 *            the f y3
	 * @param fZ3
	 *            the f z3
	 * @param fW3
	 *            the f w3
	 * 
	 * @return the double
	 */
	public static double determinant(double fX0, double fY0, double fZ0,
			double fW0, double fX1, double fY1, double fZ1, double fW1,
			double fX2, double fY2, double fZ2, double fW2, double fX3,
			double fY3, double fZ3, double fW3) {
		double fA0 = fX0 * fY1 - fX1 * fY0;
		double fA1 = fX0 * fY2 - fX2 * fY0;
		double fA2 = fX0 * fY3 - fX3 * fY0;
		double fA3 = fX1 * fY2 - fX2 * fY1;
		double fA4 = fX1 * fY3 - fX3 * fY1;
		double fA5 = fX2 * fY3 - fX3 * fY2;
		double fB0 = fZ0 * fW1 - fZ1 * fW0;
		double fB1 = fZ0 * fW2 - fZ2 * fW0;
		double fB2 = fZ0 * fW3 - fZ3 * fW0;
		double fB3 = fZ1 * fW2 - fZ2 * fW1;
		double fB4 = fZ1 * fW3 - fZ3 * fW1;
		double fB5 = fZ2 * fW3 - fZ3 * fW2;
		return fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5
				* fB0;
	}

	/**
	 * Distance.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * 
	 * @return the double
	 */
	public static double distance(Tuple3d p1, Tuple3d p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
				* (p1.y - p2.y) + (p1.z - p2.z) * (p1.z - p2.z));
	}

	/**
	 * Distance.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * 
	 * @return the double
	 */
	public static double distance(Tuple3f p1, Tuple3d p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
				* (p1.y - p2.y) + (p1.z - p2.z) * (p1.z - p2.z));
	}

	/**
	 * Distance.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * 
	 * @return the double
	 */
	public static double distance(Tuple3f p1, Tuple3f p2) {
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
				* (p1.y - p2.y) + (p1.z - p2.z) * (p1.z - p2.z));
	}

	/**
	 * Dot.
	 * 
	 * @param u
	 *            the u
	 * @param v
	 *            the v
	 * 
	 * @return the double
	 */
	public static double dot(Tuple3d u, Tuple3d v) {
		return u.x * v.x + u.y * v.y + u.z * v.z;
	}

	/**
	 * Dot.
	 * 
	 * @param u
	 *            the u
	 * @param v
	 *            the v
	 * 
	 * @return the double
	 */
	public static double dot(Tuple3f u, Tuple3f v) {
		return u.x * v.x + u.y * v.y + u.z * v.z;
	}

	/**
	 * Fractional anisotropy.
	 * 
	 * @param m
	 *            the m
	 * 
	 * @return the double
	 */
	public static double fractionalAnisotropy(Matrix m) {
		EigenvalueDecomposition ed = new EigenvalueDecomposition(m);
		Matrix D = ed.getD();
		double l1 = Math.abs(D.get(0, 0));
		double l2 = Math.abs(D.get(1, 1));
		double l3 = Math.abs(D.get(2, 2));
		double lm = (l1 + l2 + l3) * 0.3333333333;
		return Math.sqrt((1.5)
				* ((l1 - lm) * (l1 - lm) + (l2 - lm) * (l2 - lm) + (l3 - lm)
						* (l3 - lm)) / (l1 * l1 + l2 * l2 + l3 * l3));
	}

	/**
	 * Interp.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * @param t
	 *            the t
	 * 
	 * @return the vector3f
	 */
	public static Vector3f interp(Tuple3f v1, Tuple3f v2, double t) {
		double w1 = (1 - t);
		double w2 = t;
		Vector3f v3 = new Vector3f((float) (w1 * v1.x + w2 * v2.x), (float) (w1
				* v1.y + w2 * v2.y), (float) (w1 * v1.z + w2 * v2.z));
		return v3;
	}

	/**
	 * Intersection angle.
	 * 
	 * @param x1
	 *            the x1
	 * @param x2
	 *            the x2
	 * @param x3
	 *            the x3
	 * @param x4
	 *            the x4
	 * 
	 * @return the double
	 */
	public static double intersectionAngle(Vector3f x1, Vector3f x2,
			Vector3f x3, Vector3f x4) {
		Vector3f n1 = new Vector3f();
		n1.cross(x1, x2);
		Vector3f n2 = new Vector3f();
		n2.cross(x3, x4);
		Vector3f x0 = new Vector3f();
		x0.cross(n1, n2);
		x0.normalize();
		if (x0.dot(x1) < 0) {
			x0.negate();
		}
		n2.cross(x1, x0);
		return Math.signum(n1.dot(n2)) * GeomUtil.angle(x1, x0);
	}

	/**
	 * Intersection time.
	 * 
	 * @param x1
	 *            the x1
	 * @param v1
	 *            the v1
	 * @param x2
	 *            the x2
	 * @param v2
	 *            the v2
	 * 
	 * @return the double
	 */
	public static double intersectionTime(Tuple2d x1, Tuple2d v1, Tuple2d x2,
			Tuple2d v2) {
		Vector2d v3 = new Vector2d();
		v3.sub(x2, x1);
		double denom = (v1.x * v2.y - v2.x * v1.y);
		return (denom != 0) ? (v3.x * v2.y - v2.x * v3.y) / denom : 1E30;
	}

	/**
	 * Jacobian sphere to stereo.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the matrix
	 */
	public static Matrix jacobianSphereToStereo(Point2d p) {
		Matrix J = new Matrix(2, 3);
		double r = (1 + p.x * p.x + p.y * p.y);
		r = (r * r);
		J.set(0, 0, r / (-2 * (p.x * p.x - p.y * p.y - 1)));
		J.set(0, 1, r / (-4 * p.x * p.y));
		J.set(0, 2, r / (4 * p.x));

		J.set(1, 0, r / (-4 * p.x * p.y));
		J.set(1, 1, r / (-2 * (p.y * p.y - p.x * p.x - 1)));
		J.set(1, 2, r / (4 * p.y));
		return J;
	}

	/**
	 * Jacobian stereo to cartesian.
	 * 
	 * @param q1
	 *            the q1
	 * @param q2
	 *            the q2
	 * @param q3
	 *            the q3
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * 
	 * @return the matrix
	 */
	public static Matrix jacobianStereoToCartesian(Point3f q1, Point3f q2,
			Point3d q3, Point2d p1, Point2d p2, Point2d p3) {
		Point3f jacx = new Point3f();
		float A = (float) (1.0 / ((p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x)
				* (p2.y - p1.y)));
		jacx.x = A
				* (float) (q1.x * (p2.y - p3.y) + q2.x * (p3.y - p1.y) + q3.x
						* (p1.y - p2.y));
		jacx.y = A
				* (float) (q1.y * (p2.y - p3.y) + q2.y * (p3.y - p1.y) + q3.y
						* (p1.y - p2.y));
		jacx.z = A
				* (float) (q1.z * (p2.y - p3.y) + q2.z * (p3.y - p1.y) + q3.z
						* (p1.y - p2.y));

		Point3f jacy = new Point3f();
		jacy.x = A
				* (float) (q1.x * (p3.x - p2.x) + q2.x * (p1.x - p3.x) + q3.x
						* (p2.x - p1.x));
		jacy.y = A
				* (float) (q1.y * (p3.x - p2.x) + q2.y * (p1.x - p3.x) + q3.y
						* (p2.x - p1.x));
		jacy.z = A
				* (float) (q1.z * (p3.x - p2.x) + q2.z * (p1.x - p3.x) + q3.z
						* (p2.x - p1.x));
		Matrix m = new Matrix(3, 2);
		m.set(0, 0, jacx.x);
		m.set(1, 0, jacx.y);
		m.set(2, 0, jacx.z);

		m.set(0, 1, jacy.x);
		m.set(1, 1, jacy.y);
		m.set(2, 1, jacy.z);
		return m;
	}

	/**
	 * Length.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the double
	 */
	public static double length(Tuple3d p) {
		return Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
	}

	/**
	 * Max stretch.
	 * 
	 * @param m
	 *            the m
	 * 
	 * @return the double
	 */
	public static double maxStretch(Matrix m) {
		EigenvalueDecomposition ed = new EigenvalueDecomposition(m);
		Matrix D = ed.getD();
		double l1 = Math.abs(D.get(0, 0));
		double l2 = Math.abs(D.get(1, 1));
		double l3 = Math.abs(D.get(2, 2));
		return Math.max(Math.max(l1, l2), l3);
	}

	/**
	 * Mean diffusivity.
	 * 
	 * @param m
	 *            the m
	 * 
	 * @return the double
	 */
	public static double meanDiffusivity(Matrix m) {
		EigenvalueDecomposition ed = new EigenvalueDecomposition(m);
		Matrix D = ed.getD();
		double l1 = Math.abs(D.get(0, 0));
		double l2 = Math.abs(D.get(1, 1));
		double l3 = Math.abs(D.get(2, 2));
		double lm = (l1 + l2 + l3) * 0.3333333333;
		return lm;
	}

	/**
	 * Mean stretch.
	 * 
	 * @param m
	 *            the m
	 * 
	 * @return the double
	 */
	public static double meanStretch(Matrix m) {
		EigenvalueDecomposition ed = new EigenvalueDecomposition(m);
		Matrix D = ed.getD();
		double l1 = D.get(0, 0);
		double l2 = D.get(1, 1);
		double l3 = D.get(2, 2);
		return Math.sqrt((l1 * l1 + l2 * l2 + l3 * l3) * 0.3333333333);
	}

	/**
	 * Mult matrix.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the point3f
	 */
	public static Point3f multMatrix(Matrix A, double[] p) {
		Matrix v = new Matrix(3, 1);
		v.set(0, 0, p[0]);
		v.set(1, 0, p[1]);
		v.set(2, 0, p[2]);
		Matrix result = A.times(v);
		return new Point3f((float) result.get(0, 0), (float) result.get(1, 0),
				(float) result.get(2, 0));
	}

	/**
	 * Mult matrix.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the point3f
	 */
	public static Point3f multMatrix(Matrix3d A, double[] p) {
		double px = p[0];
		double py = p[1];
		double pz = p[2];
		return new Point3f((float) (px * A.m00 + py * A.m01 + pz * A.m02),
				(float) (px * A.m10 + py * A.m11 + pz * A.m12), (float) (px
						* A.m20 + py * A.m21 + pz * A.m22));
	}

	/**
	 * Mult matrix.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the point3d
	 */
	public static Point3d multMatrix(Matrix3d A, Point3d p) {
		double px = p.x;
		double py = p.y;
		double pz = p.z;
		return new Point3d((px * A.m00 + py * A.m01 + pz * A.m02), (px * A.m10
				+ py * A.m11 + pz * A.m12), (px * A.m20 + py * A.m21 + pz
				* A.m22));
	}

	/**
	 * Mult matrix.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the point3f
	 */
	public static Point3f multMatrix(Matrix3d A, Point3f p) {
		double px = p.x;
		double py = p.y;
		double pz = p.z;
		return new Point3f((float) (px * A.m00 + py * A.m01 + pz * A.m02),
				(float) (px * A.m10 + py * A.m11 + pz * A.m12), (float) (px
						* A.m20 + py * A.m21 + pz * A.m22));
	}

	// -

	/**
	 * Mult matrix.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the vector3f
	 */
	public static Vector3f multMatrix(Matrix3d A, Vector3f p) {
		double px = p.x;
		double py = p.y;
		double pz = p.z;
		return new Vector3f((float) (px * A.m00 + py * A.m01 + pz * A.m02),
				(float) (px * A.m10 + py * A.m11 + pz * A.m12), (float) (px
						* A.m20 + py * A.m21 + pz * A.m22));
	}

	/**
	 * Mult matrix3d.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the point3d
	 */
	public static Point3d multMatrix3d(Matrix3d A, Point3d p) {
		double px = p.x;
		double py = p.y;
		double pz = p.z;
		return new Point3d((px * A.m00 + py * A.m01 + pz * A.m02), (px * A.m10
				+ py * A.m11 + pz * A.m12), (px * A.m20 + py * A.m21 + pz
				* A.m22));
	}

	/**
	 * Mult matrix3d.
	 * 
	 * @param A
	 *            the a
	 * @param p
	 *            the p
	 * 
	 * @return the point3d
	 */
	public static Point3d multMatrix3d(Matrix3d A, Point3f p) {
		double px = p.x;
		double py = p.y;
		double pz = p.z;
		return new Point3d((px * A.m00 + py * A.m01 + pz * A.m02), (px * A.m10
				+ py * A.m11 + pz * A.m12), (px * A.m20 + py * A.m21 + pz
				* A.m22));
	}

	/**
	 * Normalize.
	 * 
	 * @param p1
	 *            the p1
	 */
	public static void normalize(Tuple3d p1) {
		double l1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y + p1.z * p1.z);
		p1.scale(((l1 > 1E-6) ? 1 / l1 : 0));
	}

	/**
	 * Normalized angle.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * 
	 * @return the double
	 */
	public static double normalizedAngle(Tuple3f p1, Tuple3f p2) {
		return Math.acos(Math.max(-1,
				Math.min(1, p1.x * p2.x + p1.y * p2.y + p1.z * p2.z)));
	}

	/**
	 * Normalize sqr.
	 * 
	 * @param p1
	 *            the p1
	 */
	public static void normalizeSqr(Tuple3d p1) {
		double l1 = p1.x * p1.x + p1.y * p1.y + p1.z * p1.z;
		p1.scale(((l1 > 1E-6) ? 1 / l1 : 0));
	}

	/**
	 * Normalize sqr.
	 * 
	 * @param p1
	 *            the p1
	 */
	public static void normalizeSqr(Tuple3f p1) {
		double l1 = p1.x * p1.x + p1.y * p1.y + p1.z * p1.z;
		p1.scale((float) ((l1 > 1E-6) ? 1 / l1 : 0));
	}

	/**
	 * Relation to circumsphere.
	 * 
	 * @param P
	 *            the p
	 * @param V0
	 *            the v0
	 * @param V1
	 *            the v1
	 * @param V2
	 *            the v2
	 * @param V3
	 *            the v3
	 * 
	 * @return the int
	 */
	public static int relationToCircumsphere(Point3f P, Point3f V0, Point3f V1,
			Point3f V2, Point3f V3) {
		double fS0x = V0.x + P.x;
		double fD0x = V0.x - P.x;
		double fS0y = V0.y + P.y;
		double fD0y = V0.y - P.y;
		double fS0z = V0.z + P.z;
		double fD0z = V0.z - P.z;
		double fS1x = V1.x + P.x;
		double fD1x = V1.x - P.x;
		double fS1y = V1.y + P.y;
		double fD1y = V1.y - P.y;
		double fS1z = V1.z + P.z;
		double fD1z = V1.z - P.z;
		double fS2x = V2.x + P.x;
		double fD2x = V2.x - P.x;
		double fS2y = V2.y + P.y;
		double fD2y = V2.y - P.y;
		double fS2z = V2.z + P.z;
		double fD2z = V2.z - P.z;
		double fS3x = V3.x + P.x;
		double fD3x = V3.x - P.x;
		double fS3y = V3.y + P.y;
		double fD3y = V3.y - P.y;
		double fS3z = V3.z + P.z;
		double fD3z = V3.z - P.z;
		double fW0 = fS0x * fD0x + fS0y * fD0y + fS0z * fD0z;
		double fW1 = fS1x * fD1x + fS1y * fD1y + fS1z * fD1z;
		double fW2 = fS2x * fD2x + fS2y * fD2y + fS2z * fD2z;
		double fW3 = fS3x * fD3x + fS3y * fD3y + fS3z * fD3z;
		double fDet4 = GeomUtil.determinant(fD0x, fD0y, fD0z, fW0, fD1x, fD1y,
				fD1z, fW1, fD2x, fD2y, fD2z, fW2, fD3x, fD3y, fD3z, fW3);
		return ((fDet4 > 0.0) ? 1 : ((fDet4 < 0.0) ? -1 : 0));
	}

	/**
	 * Relation to plane.
	 * 
	 * @param rkP
	 *            the rk p
	 * @param rkV0
	 *            the rk v0
	 * @param rkV1
	 *            the rk v1
	 * @param rkV2
	 *            the rk v2
	 * 
	 * @return the int
	 */
	public static int relationToPlane(Point3f rkP, Point3f rkV0, Point3f rkV1,
			Point3f rkV2) {
		double fX0 = rkP.x - rkV0.x;
		double fY0 = rkP.y - rkV0.y;
		double fZ0 = rkP.z - rkV0.z;
		double fX1 = rkV1.x - rkV0.x;
		double fY1 = rkV1.y - rkV0.y;
		double fZ1 = rkV1.z - rkV0.z;
		double fX2 = rkV2.x - rkV0.x;
		double fY2 = rkV2.y - rkV0.y;
		double fZ2 = rkV2.z - rkV0.z;
		double fDet3 = GeomUtil.determinant(fX0, fY0, fZ0, fX1, fY1, fZ1, fX2,
				fY2, fZ2);
		return ((fDet3 > 0.0) ? +1 : ((fDet3 < 0.0) ? -1 : 0));
	}

	/**
	 * Rotate matrix.
	 * 
	 * @param theta
	 *            the theta
	 * @param phi
	 *            the phi
	 * 
	 * @return the matrix
	 */
	public static Matrix rotateMatrix(double theta, double phi) {
		double cost = Math.cos(theta);
		double sint = Math.sin(theta);
		double cosp = Math.cos(phi - 0.5 * Math.PI);
		double sinp = Math.sin(phi - 0.5 * Math.PI);
		Matrix rotPhi = new Matrix(new double[][] { { cosp, 0, sinp },
				{ 0, 1, 0 }, { -sinp, 0, cosp } });
		Matrix rotTheta = new Matrix(new double[][] { { cost, -sint, 0 },
				{ sint, cost, 0 }, { 0, 0, 1 } });
		return rotTheta.times(rotPhi);
	}

	/**
	 * Rotate matrix3d.
	 * 
	 * @param theta
	 *            the theta
	 * @param phi
	 *            the phi
	 * 
	 * @return the matrix3d
	 */
	public static Matrix3d rotateMatrix3d(double theta, double phi) {
		double cost = Math.cos(theta);
		double sint = Math.sin(theta);
		double cosp = Math.cos(phi - 0.5 * Math.PI);
		double sinp = Math.sin(phi - 0.5 * Math.PI);
		Matrix3d rotPhi = new Matrix3d(new double[] { cosp, 0, sinp, 0, 1, 0,
				-sinp, 0, cosp });
		Matrix3d rotTheta = new Matrix3d(new double[] { cost, -sint, 0, sint,
				cost, 0, 0, 0, 1 });
		Matrix3d m = new Matrix3d();
		m.mul(rotTheta, rotPhi);
		return m;
	}

	/**
	 * Rotate matrix3f.
	 * 
	 * @param theta
	 *            the theta
	 * @param phi
	 *            the phi
	 * 
	 * @return the matrix3f
	 */
	public static Matrix3f rotateMatrix3f(double theta, double phi) {
		float cost = (float) Math.cos(theta);
		float sint = (float) Math.sin(theta);
		float cosp = (float) Math.cos(phi - 0.5 * Math.PI);
		float sinp = (float) Math.sin(phi - 0.5 * Math.PI);
		Matrix3f rotPhi = new Matrix3f(new float[] { cosp, 0, sinp, 0, 1, 0,
				-sinp, 0, cosp });
		Matrix3f rotTheta = new Matrix3f(new float[] { cost, -sint, 0, sint,
				cost, 0, 0, 0, 1 });
		Matrix3f m = new Matrix3f();
		m.mul(rotTheta, rotPhi);
		return m;
	}

	/**
	 * Sphere to stereo.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the point2d
	 */
	public static Point2d sphereToStereo(Tuple3f p) {
		return new Point2d((p.z != 1) ? p.x / (1 - p.z) : p.x * 1E20,
				(p.z != 1) ? p.y / (1 - p.z) : p.y * 1E20);
	}

	/**
	 * Spherical angle.
	 * 
	 * @param v2
	 *            the v2
	 * @param v1
	 *            the v1
	 * @param v3
	 *            the v3
	 * 
	 * @return the double
	 */
	public static double sphericalAngle(Vector3d v2, Vector3d v1, Vector3d v3) {
		Vector3d v12 = new Vector3d();
		Vector3d v23 = new Vector3d();
		v12.cross(v1, v2);
		v23.cross(v3, v2);
		double ang = -Math.atan2(v2.length() * v1.dot(v23), v12.dot(v23));
		if (ang < -1E-5) {
			return 2 * Math.PI + ang;
		} else {
			return ang;
		}
	}

	/**
	 * Spherical angle.
	 * 
	 * @param v2
	 *            the v2
	 * @param v1
	 *            the v1
	 * @param v3
	 *            the v3
	 * 
	 * @return the double
	 */
	public static double sphericalAngle(Vector3f v2, Vector3f v1, Vector3f v3) {
		Vector3f v12 = new Vector3f();
		Vector3f v23 = new Vector3f();
		v12.cross(v1, v2);
		v23.cross(v3, v2);
		double ang = -Math.atan2(v2.length() * v1.dot(v23), v12.dot(v23));
		if (ang < -1E-5) {
			return 2 * Math.PI + ang;
		} else {
			return ang;
		}
	}

	/**
	 * Stereo to sphere.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the point3f
	 */
	public static Point3f stereoToSphere(Tuple2d p) {
		double r = (1 + p.x * p.x + p.y * p.y);
		if (Math.abs(r) > 1E-10) {
			return new Point3f((float) (2 * p.x / r), (float) (2 * p.y / r),
					(float) ((p.x * p.x + p.y * p.y - 1) / r));
		} else {
			return new Point3f(0, 0, 1);
		}
	}

	/**
	 * Tan angle.
	 * 
	 * @param v0
	 *            the v0
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * 
	 * @return the double
	 */
	public static double tanAngle(Tuple3f v0, Tuple3f v1, Tuple3f v2) {
		Vector3f v = new Vector3f();
		Vector3f w = new Vector3f();
		Vector3f z = new Vector3f();
		v.sub(v0, v2);
		w.sub(v1, v2);
		z.cross(v, w);
		double dot = v.dot(w);
		if (Math.abs(dot) > 0) {
			return z.length() / dot;
		} else {
			return 0;
		}
	}

	/**
	 * To cartesian.
	 * 
	 * @param r
	 *            the r
	 * 
	 * @return the point3f
	 */
	public static Point3f toCartesian(Point2d r) {
		return new Point3f((float) (Math.cos(r.x) * Math.sin(r.y)),
				(float) (Math.sin(r.x) * Math.sin(r.y)), (float) Math.cos(r.y));
	}

	/**
	 * To cartesian.
	 * 
	 * @param r
	 *            the r
	 * 
	 * @return the point3f
	 */
	public static Point3f toCartesian(Point2f r) {
		return new Point3f((float) (Math.cos(r.x) * Math.sin(r.y)),
				(float) (Math.sin(r.x) * Math.sin(r.y)), (float) Math.cos(r.y));
	}

	/**
	 * To cartesian.
	 * 
	 * @param r
	 *            the r
	 * 
	 * @return the point3f
	 */
	public static Point3f toCartesian(Point3d r) {
		return new Point3f((float) (r.x * Math.cos(r.y) * Math.sin(r.z)),
				(float) (r.x * Math.sin(r.y) * Math.sin(r.z)),
				(float) (r.x * Math.cos(r.z)));
	}

	/**
	 * To cartesian vector.
	 * 
	 * @param r
	 *            the r
	 * 
	 * @return the vector3f
	 */
	public static Vector3f toCartesianVector(Point2d r) {
		return new Vector3f((float) (Math.cos(r.x) * Math.sin(r.y)),
				(float) (Math.sin(r.x) * Math.sin(r.y)), (float) Math.cos(r.y));
	}

	/**
	 * Triangle area.
	 * 
	 * @param v0
	 *            the v0
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * 
	 * @return the double
	 */
	public static double triangleArea(Point3f v0, Point3f v1, Point3f v2) {
		Vector3f v = new Vector3f();
		Vector3f w = new Vector3f();
		Vector3f z = new Vector3f();
		v.sub(v0, v2);
		w.sub(v1, v2);
		z.cross(v, w);
		return 0.5 * z.length();
	}
}
