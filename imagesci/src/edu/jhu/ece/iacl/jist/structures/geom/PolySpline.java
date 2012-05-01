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
package edu.jhu.ece.iacl.jist.structures.geom;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import Jama.Matrix;

// TODO: Auto-generated Javadoc
/**
 * The Class PolySpline.
 */
public class PolySpline implements Curve {

	/** The Constant lengthDelta. */
	private static final double lengthDelta = 1E-4;

	/** The coeff. */
	private double[][] coeff;

	/** The degree. */
	private int degree;

	/** The length. */
	private double length;

	/** The value. */
	private double value;

	/**
	 * Instantiates a new poly spline.
	 * 
	 * @param degree
	 *            the degree
	 */
	public PolySpline(int degree) {
		this.degree = degree;

	}

	/**
	 * Compute coefficients.
	 * 
	 * @param controlPoints
	 *            the control points
	 * @param s
	 *            the s
	 */
	public void computeCoefficients(Point3f[] controlPoints, double[] s) {
		ArrayList<Point3f> controls = new ArrayList<Point3f>();
		ArrayList<Double> times = new ArrayList<Double>();
		Point3f last = controlPoints[0];
		for (int i = 1; i < controlPoints.length; i++) {
			Point3f current = controlPoints[i];
			if (last.distance(current) > 1E-4) {
				controls.add(controlPoints[i]);
				times.add(s[i]);
				last = current;
			}
		}
		degree = times.size() - 1;
		coeff = new double[degree + 1][3];
		Matrix M = new Matrix(times.size(), degree + 1);
		Matrix Vx = new Matrix(times.size(), 1);
		Matrix Vy = new Matrix(times.size(), 1);
		Matrix Vz = new Matrix(times.size(), 1);
		for (int i = 0; i <= degree; i++) {
			for (int j = 0; j < times.size(); j++) {
				M.set(j, i, Math.pow(times.get(j), i));
				Vx.set(j, 0, controls.get(j).x);
				Vy.set(j, 0, controls.get(j).y);
				Vz.set(j, 0, controls.get(j).z);
			}
		}
		Matrix inv = M.inverse();
		Matrix coeffX = inv.times(Vx);
		Matrix coeffY = inv.times(Vy);
		Matrix coeffZ = inv.times(Vz);
		for (int i = 0; i < coeff.length; i++) {
			coeff[i][0] = coeffX.get(i, 0);
			coeff[i][1] = coeffY.get(i, 0);
			coeff[i][2] = coeffZ.get(i, 0);
		}
		approxLength();
	}

	/**
	 * Approx length.
	 */
	private void approxLength() {
		double s = 0;
		Point3f lastPoint = null;
		Point3f p;
		length = 0;
		for (s = 0; s <= 1; s += lengthDelta) {
			p = interpolate(s);
			if (lastPoint != null) {
				length += p.distance(lastPoint);
			}
			lastPoint = p;
		}
		value = length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Curve#getCurve()
	 */
	@Override
	public Point3f[] getCurve() {
		return getCurve(0.25);
	}

	/**
	 * Gets the curve.
	 * 
	 * @param spacing
	 *            the spacing
	 * 
	 * @return the curve
	 */
	public Point3f[] getCurve(double spacing) {
		int steps = (int) Math.max(1, Math.ceil(length / spacing));
		if (steps < 1) {
			steps = 1;
		}
		double stepSize = 1.0 / steps;
		Point3f[] points = new Point3f[steps + 1];
		for (int i = 0; i < steps; i++) {
			points[i] = interpolate(i * stepSize);
		}
		points[steps] = interpolate(1);
		return points;
	}

	/**
	 * Gets the curve.
	 * 
	 * @param steps
	 *            the steps
	 * 
	 * @return the curve
	 */
	public Point3f[] getCurve(int steps) {
		double stepSize = 1.0 / steps;
		Point3f[] points = new Point3f[steps + 1];
		for (int i = 0; i < steps; i++) {
			points[i] = interpolate(i * stepSize);
		}
		points[steps] = interpolate(1);
		return points;
	}

	/**
	 * Interpolate.
	 * 
	 * @param s
	 *            the s
	 * 
	 * @return the point3f
	 */
	public Point3f interpolate(double s) {
		Point3f p = new Point3f();
		for (int i = 0; i <= degree; i++) {
			p.x += coeff[i][0] * Math.pow(s, i);
			p.y += coeff[i][1] * Math.pow(s, i);
			p.z += coeff[i][2] * Math.pow(s, i);
		}
		return p;
	}

	/**
	 * Gets the length.
	 * 
	 * @return the length
	 */
	public double getLength() {
		return length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.geom.Curve#getValue()
	 */
	@Override
	public double getValue() {
		return value;
	}

	/**
	 * Sets the length.
	 * 
	 * @param length
	 *            the new length
	 */
	public void setLength(double length) {
		this.length = length;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(double value) {
		this.value = value;
	}
}
