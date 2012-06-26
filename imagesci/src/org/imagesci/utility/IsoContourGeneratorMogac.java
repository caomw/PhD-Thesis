/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.utility;


import java.util.Hashtable;
import java.util.LinkedList;

import javax.vecmath.Point2f;

import org.imagesci.gac.TopologyPreservationRule2D;
import org.imagesci.mogac.MOGAC2D;

import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class IsoContourGeneratorMGAC.
 */
public class IsoContourGeneratorMogac extends IsoContourGenerator {

	/** The contains overlap. */
	boolean containsOverlap = false;

	/** The current label. */
	protected int currentLabel;

	/** The labels. */
	protected int[][] labels;

	/**
	 * Instantiates a new iso contour generator mgac.
	 * 
	 * @param parent
	 *            the parent
	 * @param rule
	 *            the rule
	 */
	public IsoContourGeneratorMogac(AbstractCalculation parent,
			TopologyPreservationRule2D.Rule rule) {
		super(parent, rule);
	}

	/**
	 * Instantiates a new iso contour generator mgac.
	 * 
	 * @param rule
	 *            the rule
	 */
	public IsoContourGeneratorMogac(TopologyPreservationRule2D.Rule rule) {
		super(rule);

	}

	/**
	 * Solve.
	 * 
	 * @param vol
	 *            the vol
	 * @param labelImage
	 *            the label image
	 * @param label
	 *            the label
	 * @return the contour array
	 */
	public ContourArray solve(ImageDataFloat vol, ImageDataInt labelImage,
			int label) {
		vertCount = 0;
		this.isoLevel = 0;
		this.currentLabel = label;
		this.labels = labelImage.toArray2d();
		this.imgMat = vol.toArray2d();
		rows = vol.getRows();
		cols = vol.getCols();

		resX = resY = 1;
		setTotalUnits(rows);
		Hashtable<Long, EdgeSplit> splits = new Hashtable<Long, EdgeSplit>();
		LinkedList<Edge> edges = new LinkedList<Edge>();
		// Set mesh resolutions
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				// Get the image values at the corners of the square.
				processSquare(i, j, splits, edges);
			}
			if (!silent) {
				incrementCompletedUnits();
			}
		}
		EdgeSplit[] pts = new EdgeSplit[splits.size()];
		for (EdgeSplit split : splits.values()) {
			pts[split.vid] = split;
		}
		// Generate iso-surface from list of triangles
		int[] indexes = new int[edges.size() * 2];
		Point2f[] points = new Point2f[splits.size()];
		int index = 0;

		if (winding == Winding.CLOCKWISE) {
			for (Edge edge : edges) {
				indexes[index++] = edge.vids[0];
				indexes[index++] = edge.vids[1];
			}
		} else if (winding == Winding.COUNTER_CLOCKWISE) {
			for (Edge edge : edges) {
				indexes[index++] = edge.vids[1];
				indexes[index++] = edge.vids[0];
			}
		}
		index = 0;
		for (EdgeSplit split : splits.values()) {
			index = split.vid;
			points[index] = split.pt2d;
		}
		volMat = null;
		// Create surface
		ContourArray contour = new ContourArray(points, indexes);

		return contour;
	}

	/**
	 * Solve.
	 *
	 * @param levelset the levelset
	 * @param labelImage the label image
	 * @param labelMasks the label masks
	 * @param containsOverlap the contains overlap
	 * @return the contour array[]
	 */
	public ContourArray[] solve(ImageDataFloat levelset,
			ImageDataInt labelImage, int[] labelMasks, boolean containsOverlap) {
		int numObjects = labelMasks.length - 1;
		ContourArray[] contours = new ContourArray[numObjects];
		this.labels = labelImage.toArray2d();
		this.imgMat = levelset.toArray2d();
		this.containsOverlap = containsOverlap;
		this.rows = imgMat.length;
		this.cols = imgMat[0].length;
		for (int nn = 0; nn < contours.length; nn++) {
			vertCount = 0;
			this.isoLevel = 0;
			if (useResolutions) {
				float[] res = levelset.getHeader().getDimResolutions();
				resX = res[0];
				resY = res[1];
			} else {
				resX = resY = 1;
			}
			this.currentLabel = (containsOverlap) ? (0x1 << nn)
					: labelMasks[nn + 1];
			setTotalUnits(rows);
			Hashtable<Long, EdgeSplit> splits = new Hashtable<Long, EdgeSplit>();
			LinkedList<Edge> edges = new LinkedList<Edge>();
			// Set mesh resolutions
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					processSquare(i, j, splits, edges);
					if (!silent) {
						incrementCompletedUnits();
					}
				}
			}

			EdgeSplit[] pts = new EdgeSplit[splits.size()];
			for (EdgeSplit split : splits.values()) {
				pts[split.vid] = split;
			}
			// Generate iso-surface from list of triangles
			int[] indexes = new int[edges.size() * 2];
			Point2f[] points = new Point2f[splits.size()];
			int index = 0;
			if (winding == Winding.CLOCKWISE) {
				for (Edge edge : edges) {
					indexes[index++] = edge.vids[0];
					indexes[index++] = edge.vids[1];
				}
			} else if (winding == Winding.COUNTER_CLOCKWISE) {
				for (Edge edge : edges) {
					indexes[index++] = edge.vids[1];
					indexes[index++] = edge.vids[0];
				}
			}
			index = 0;
			for (EdgeSplit split : splits.values()) {
				index = split.vid;
				points[index] = split.pt2d;
			}
			volMat = null;
			// Create surface
			contours[nn] = new ContourArray(points, indexes);
		}
		return contours;
	}

	/**
	 * Solve.
	 *
	 * @param gac the gac
	 * @param containsOverlap the contains overlap
	 * @return the contour array[]
	 */
	public ContourArray[] solve(MOGAC2D gac, boolean containsOverlap) {
		int numObjects = gac.getNumObjects();
		ContourArray[] contours = new ContourArray[numObjects];
		this.labels = gac.getImageLabels().toArray2d();
		this.imgMat = gac.getDistanceField().toArray2d();
		this.containsOverlap = containsOverlap;
		for (int nn = 0; nn < contours.length; nn++) {
			vertCount = 0;
			this.isoLevel = 0;
			resX = resY = 1;
			this.currentLabel = (containsOverlap) ? (0x1 << nn) : gac
					.getLabelMasks()[nn + 1];
			this.rows = imgMat.length;
			this.cols = imgMat[0].length;

			setTotalUnits(rows);
			Hashtable<Long, EdgeSplit> splits = new Hashtable<Long, EdgeSplit>();
			LinkedList<Edge> edges = new LinkedList<Edge>();
			// Set mesh resolutions
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					processSquare(i, j, splits, edges);
					if (!silent) {
						incrementCompletedUnits();
					}
				}
			}

			EdgeSplit[] pts = new EdgeSplit[splits.size()];
			for (EdgeSplit split : splits.values()) {
				pts[split.vid] = split;
			}
			// Generate iso-surface from list of triangles
			int[] indexes = new int[edges.size() * 2];
			Point2f[] points = new Point2f[splits.size()];
			int index = 0;
			if (winding == Winding.CLOCKWISE) {
				for (Edge edge : edges) {
					indexes[index++] = edge.vids[0];
					indexes[index++] = edge.vids[1];
				}
			} else if (winding == Winding.COUNTER_CLOCKWISE) {
				for (Edge edge : edges) {
					indexes[index++] = edge.vids[1];
					indexes[index++] = edge.vids[0];
				}
			}
			index = 0;
			for (EdgeSplit split : splits.values()) {
				index = split.vid;
				points[index] = split.pt2d;
			}
			volMat = null;
			// Create surface
			contours[nn] = new ContourArray(points, indexes);
		}
		return contours;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.algorithms.geometry.surface.IsoContourGenerator#getValue
	 * (int, int)
	 */
	@Override
	protected float getValue(int i, int j) {
		int x = Math.max(Math.min(rows - 1, i), 0);
		int y = Math.max(Math.min(cols - 1, j), 0);
		float val;
		if (x == 0 || y == 0 || x == rows - 1 || y == cols - 1) {
			val = 1;
		} else {
			float sign = 0;
			if (containsOverlap) {
				sign = ((labels[x][y] & currentLabel) != 0) ? -1 : 1;
			} else {
				sign = (labels[x][y] == currentLabel) ? -1 : 1;
			}
			if (fieldMat != null) {
				val = sign * fieldMat[x][y][slice][component] - isoLevel;
			} else if (volMat != null) {
				val = sign * volMat[x][y][slice] - isoLevel;
			} else {
				val = sign * imgMat[x][y] - isoLevel;
			}
			if (nudgeLevelSet) {
				// Push iso-level away from zero level set
				if (val < 0) {
					val = Math.min(val, -LEVEL_SET_TOLERANCE);
				} else {
					val = Math.max(val, LEVEL_SET_TOLERANCE);
				}
			}
		}
		return val;
	}
}
