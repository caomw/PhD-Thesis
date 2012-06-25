/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 */
package edu.jhu.ece.iacl.jist.structures.geom;

import java.util.LinkedList;

import javax.vecmath.Point3f;

// TODO: Auto-generated Javadoc
/**
 * The Class CurveCollection.
 */
public class CurveCollection extends LinkedList<Curve> {

	/** The line data. */
	protected double[][] lineData = null;

	/** The name. */
	protected String name;

	/**
	 * Instantiates a new curve collection.
	 */
	public CurveCollection() {
		super();
		name = null;
	}

	/**
	 * Gets the curves.
	 * 
	 * @return the curves
	 */
	public Point3f[][] getCurves() {
		Point3f[][] curves = new Point3f[size()][0];
		for (int i = 0; i < curves.length; i++) {
			curves[i] = get(i).getCurve();
		}
		return curves;
	}

	/**
	 * Gets the line data.
	 * 
	 * @return the line data
	 */
	public double[][] getLineData() {
		if (lineData == null) {
			double[][] data = new double[size()][1];
			for (int i = 0; i < data.length; i++) {
				data[i][0] = get(i).getValue();
			}
			return data;
		} else {
			return lineData;
		}
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the line data.
	 * 
	 * @param data
	 *            the new line data
	 */
	public void setLineData(double[][] data) {
		this.lineData = data;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		return (name != null) ? name : "lines";
	}
}
