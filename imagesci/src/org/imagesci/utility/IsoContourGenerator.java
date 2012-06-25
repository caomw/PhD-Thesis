/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
import javax.vecmath.Point2i;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;

import org.imagesci.gac.TopologyPreservationRule2D;
import org.imagesci.gac.TopologyRule2D;

import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.geom.CurveCollection;
import edu.jhu.ece.iacl.jist.structures.geom.CurvePath;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.utility.VersionUtil;

// TODO: Auto-generated Javadoc
/*
 * Geometric Tools, LLC Copyright (c) 1998-2010 Distributed under the Boost
 * Software License, Version 1.0. http://www.boost.org/LICENSE_1_0.txt
 * http://www.geometrictools.com/License/Boost/LICENSE_1_0.txt
 * 
 * File Version: 4.10.0 (2009/11/18)
 */
/**
 * The Iso-Contour generator generates 2D curves representing closed contours.
 * The algorithm has been modified from it's original implementation so that
 * contours are connected and do not self-intersect.
 * 
 * @author Blake Lucas
 */
public class IsoContourGenerator extends AbstractCalculation {
	/**
	 * The Class Edge stores vertex ids.
	 */
	protected class Edge {

		/** The vertex ids. */
		protected int[] vids;

		/**
		 * Instantiates a new edge.
		 */
		public Edge() {
			this.vids = new int[2];
		}

		/**
		 * Instantiates a new edge.
		 * 
		 * @param vid1
		 *            the vid1
		 * @param vid2
		 *            the vid2
		 */
		public Edge(int vid1, int vid2) {
			this.vids = new int[] { vid1, vid2 };
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return ("[" + vids[0] + "," + vids[1] + "]");
		}
	}

	/**
	 * The Class EdgeSplit is a container to describe a level set crossing
	 * point.
	 */
	protected class EdgeSplit {

		/** The e2. */
		public Edge e1 = null, e2 = null;

		/** The grid point reference less than the level set value. */
		Point2i pt1;

		/** The grid point reference greater than the level set value. */
		Point2i pt2;

		/** The interpolated point on the target level set. */
		Point2f pt2d;

		/** The vertex id for the interpolated point. */
		int vid;

		/**
		 * Instantiates a new edge split.
		 * 
		 * @param pt1
		 *            the lower grid point
		 * @param pt2
		 *            the upper grid point
		 */
		public EdgeSplit(Point2i pt1, Point2i pt2) {
			this.pt1 = pt1;
			this.pt2 = pt2;
		}

		/**
		 * Compare two edge splits.
		 * 
		 * @param split
		 *            the split
		 * 
		 * @return true, if successful
		 */
		public boolean equals(EdgeSplit split) {
			return ((pt1.equals(split.pt1) && pt2.equals(split.pt2)) || (pt1
					.equals(split.pt2) && pt2.equals(split.pt1)));
		}

		/**
		 * Hash value that uniquely identifies edge split.
		 * 
		 * @return the hash value
		 */
		public long hashValue() {
			long d = rows * cols;
			long h1 = hashValue(pt1);
			long h2 = hashValue(pt2);
			if (h1 < h2) {
				return h1 + d * h2;
			} else {
				return h2 + d * h1;
			}

		}

		/**
		 * Generate hash value for grid point.
		 * 
		 * @param pt
		 *            the grid point
		 * 
		 * @return the hash value
		 */
		public long hashValue(Point2i pt) {
			return rows * pt.y + pt.x;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return ("Vertex " + vid + ": " + e1 + " " + e2);
		}
	}

	/**
	 * The winding order for triangle vertices.
	 */
	public enum Winding {

		/** The CLOCKWISE. */
		CLOCKWISE,

		/** The COUNTER_CLOCKWISE. */
		COUNTER_CLOCKWISE

	}

	/** The Constant a2fVertex1Offset. */
	protected static final int a2fVertex1Offset[][] = { { 0, 0 }, { 1, 0 },
			{ 1, 1 }, { 0, 1 } };

	/** The Constant a2fVertex2Offset. */
	protected static final int a2fVertex2Offset[][] = { { 1, 0 }, { 1, 1 },
			{ 0, 1 }, { 0, 0 } };

	/** The Constant afSquareValue4. */
	protected static final int afSquareValue4[][] = new int[][] {
			{ 4, 4, 4, 4 },// 0000 0
			{ 3, 0, 4, 4 },// 0001 1
			{ 0, 1, 4, 4 },// 0010 2
			{ 3, 1, 4, 4 },// 0011 3
			{ 1, 2, 4, 4 },// 0100 4
			{ 0, 1, 2, 3 },// 0101 5
			{ 0, 2, 4, 4 },// 0110 6
			{ 3, 2, 4, 4 },// 0111 7
			{ 2, 3, 4, 4 },// 1000 8
			{ 2, 0, 4, 4 },// 1001 9
			{ 1, 2, 3, 0 },// 1010 10
			{ 2, 1, 4, 4 },// 1011 11
			{ 1, 3, 4, 4 },// 1100 12
			{ 1, 0, 4, 4 },// 1101 13
			{ 0, 3, 4, 4 },// 1110 14
			{ 4, 4, 4, 4 } // 1111 15
	};

	/** The Constant afSquareValue8. */
	protected static final int afSquareValue8[][] = new int[][] {
			{ 4, 4, 4, 4 },// 0000 0
			{ 3, 0, 4, 4 },// 0001 1
			{ 0, 1, 4, 4 },// 0010 2
			{ 3, 1, 4, 4 },// 0011 3
			{ 1, 2, 4, 4 },// 0100 4
			{ 2, 1, 0, 3 },// 0101 5
			{ 0, 2, 4, 4 },// 0110 6
			{ 3, 2, 4, 4 },// 0111 7
			{ 2, 3, 4, 4 },// 1000 8
			{ 2, 0, 4, 4 },// 1001 9
			{ 1, 0, 3, 2 },// 1010 10
			{ 2, 1, 4, 4 },// 1011 11
			{ 1, 3, 4, 4 },// 1100 12
			{ 1, 0, 4, 4 },// 1101 13
			{ 0, 3, 4, 4 },// 1110 14
			{ 4, 4, 4, 4 } // 1111 15
	};

	/** The Constant LEVEL_SET_TOLERANCE. */
	public static final float LEVEL_SET_TOLERANCE = 1E-3f;

	/** The component. */
	protected int component;

	/** The field mat. */
	protected float[][][][] fieldMat = null;

	/** The img mat. */
	protected float[][] imgMat = null;

	/** The iso level. */
	protected float isoLevel;

	/** The last split1. */
	protected EdgeSplit lastSplit1;

	/** The last split2. */
	protected EdgeSplit lastSplit2;

	/** The nudge level set. */
	protected boolean nudgeLevelSet = true;

	/** The res y. */
	protected float resX, resY;

	/** The cols. */
	protected int rows, cols;

	/** The rule. */
	protected TopologyRule2D.Rule rule = null;

	/** The silent. */
	protected boolean silent = false;

	/** The slice. */
	protected int slice;

	/** The use resolutions. */
	protected boolean useResolutions;

	/** The vert count. */
	protected int vertCount;

	/** The vol mat. */
	protected float[][][] volMat = null;

	/** The winding order. */
	protected Winding winding = Winding.COUNTER_CLOCKWISE;

	/**
	 * Instantiates a new iso contour generator mgac.
	 * 
	 * @param parent
	 *            the parent
	 * @param rule
	 *            the rule
	 */
	public IsoContourGenerator(AbstractCalculation parent,
			TopologyPreservationRule2D.Rule rule) {
		super(parent);
		setConnectivityRule(rule);
	}

	/**
	 * Instantiates a new iso contour generator.
	 */
	public IsoContourGenerator() {
		super();
		setLabel("Iso-Contour");
	}

	/**
	 * Instantiates a new iso contour generator.
	 * 
	 * @param parent
	 *            the parent
	 */
	public IsoContourGenerator(AbstractCalculation parent) {
		super(parent);
		setLabel("Iso-Contour");
	}

	/**
	 * Instantiates a new iso contour generator.
	 *
	 * @param silent the silent
	 */
	public IsoContourGenerator(boolean silent) {
		super();
		this.silent = silent;
		setLabel("Iso-Contour");
	}

	/**
	 * Instantiates a new iso contour generator.
	 *
	 * @param rule the rule
	 */
	public IsoContourGenerator(TopologyRule2D.Rule rule) {
		this();
		this.rule = rule;
	}

	/**
	 * Solve.
	 *
	 * @param vol the vol
	 * @return the contour array
	 */
	public ContourArray solve(ImageDataFloat vol) {
		vertCount = 0;
		this.isoLevel = 0;
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
	 * Solve for iso-contour.
	 * 
	 * @param vol
	 *            the level set
	 * @param isoLevel
	 *            the target iso-level
	 * 
	 * @return the iso-surface
	 */
	public CurveCollection solve(ImageDataFloat vol, float isoLevel) {
		return solve(vol, isoLevel, -1, -1);
	}

	/**
	 * Solve for iso-contour.
	 * 
	 * @param vol
	 *            the level set
	 * @param isoLevel
	 *            the target iso-level
	 * @param slice
	 *            the slice
	 * @param component
	 *            the component
	 * 
	 * @return the iso-surface
	 */
	public CurveCollection solve(ImageDataFloat vol, float isoLevel, int slice,
			int component) {
		this.slice = slice;
		this.component = component;
		fieldMat = vol.toArray4d();
		volMat = vol.toArray3d();
		imgMat = vol.toArray2d();
		if (imgMat == null && volMat == null && fieldMat == null) {
			return null;
		}
		rows = vol.getRows();
		cols = vol.getCols();
		vertCount = 0;
		this.isoLevel = isoLevel;
		setTotalUnits(rows);
		Hashtable<Long, EdgeSplit> splits = new Hashtable<Long, EdgeSplit>();
		LinkedList<Edge> edges = new LinkedList<Edge>();
		// Set mesh resolutions
		if (useResolutions) {
			float[] res = vol.getHeader().getDimResolutions();
			resX = res[0];
			resY = res[1];
		} else {
			resX = resY = 1;
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				// Get the image values at the corners of the square.
				processSquare(i, j, splits, edges);
			}
			if (!silent) {
				incrementCompletedUnits();
			}
		}
		CurveCollection curves = new CurveCollection();

		EdgeSplit[] pts = new EdgeSplit[splits.size()];
		for (EdgeSplit split : splits.values()) {
			pts[split.vid] = split;
		}
		int index = 1;
		EdgeSplit lastSplit = (pts.length > 0) ? pts[0] : null;
		CurvePath curvePath = new CurvePath();
		// Extract connected contours
		boolean firstPass = true;
		while (lastSplit != null) {
			Point2f pt1 = lastSplit.pt2d;
			curvePath.add(new Point3f(pt1.x, pt1.y, 0));
			Edge e1 = lastSplit.e1;
			Edge e2 = lastSplit.e2;
			float val = getValue(lastSplit.pt1.x, lastSplit.pt1.y);
			if ((val > 0 && winding == Winding.COUNTER_CLOCKWISE)
					|| (val <= 0 && winding == Winding.CLOCKWISE)) {
				// Swap edges so they are correctly ordered. This technique may
				// fail if a vertex lies exactly on the iso-level.
				Edge tmp = e1;
				e1 = e2;
				e2 = tmp;
			}
			// March around contour
			if (e1 != null) {
				if (!firstPass) {
					lastSplit.e1 = null;
				}

				if (e1.vids[0] == lastSplit.vid) {
					lastSplit = pts[e1.vids[1]];
				} else {
					lastSplit = pts[e1.vids[0]];
				}
				if (!firstPass) {
					if (lastSplit.e1 == e1) {
						lastSplit.e1 = null;
					}
					if (lastSplit.e2 == e1) {
						lastSplit.e2 = null;
					}
				}
				firstPass = false;
			} else if (e2 != null) {
				if (!firstPass) {
					lastSplit.e2 = null;
				}
				firstPass = false;
				if (e2.vids[0] == lastSplit.vid) {
					lastSplit = pts[e2.vids[1]];
				} else {
					lastSplit = pts[e2.vids[0]];
				}
				if (!firstPass) {
					if (lastSplit.e1 == e2) {
						lastSplit.e1 = null;
					}
					if (lastSplit.e2 == e2) {
						lastSplit.e2 = null;
					}
				}
				firstPass = false;
			} else {
				// Start new contour
				lastSplit = null;
				curves.add(curvePath);
				curvePath = new CurvePath();
				firstPass = true;
				while (index < pts.length) {
					EdgeSplit tmp = pts[index++];
					if (tmp.e1 != null && tmp.e2 != null) {
						lastSplit = tmp;
						break;
					}
				}
			}
		}
		volMat = null;
		fieldMat = null;
		imgMat = null;
		curves.setName(vol.getName() + "_contour");
		if (!silent) {
			markCompleted();
		}
		System.err.flush();
		return curves;
	}

	/**
	 * Process square.
	 *
	 * @param x the x
	 * @param y the y
	 * @param splits the splits
	 * @param edges the edges
	 */
	protected void processSquare(int x, int y,
			Hashtable<Long, EdgeSplit> splits, LinkedList<Edge> edges) {
		// processSquare1(x, y, splits, edges);
		processSquare2(x, y, splits, edges);
	}

	/**
	 * Process square.
	 *
	 * @param x the x
	 * @param y the y
	 * @param splits the splits
	 * @param edges the edges
	 */

	private void processSquare2(int x, int y,
			Hashtable<Long, EdgeSplit> splits, LinkedList<Edge> edges) {
		int iFlagIndex = 0;

		for (int iVertex = 0; iVertex < 4; iVertex++) {
			if (getValue(clampRow(x + a2fVertex1Offset[iVertex][0]),
					clampColumn(y + a2fVertex1Offset[iVertex][1])) > isoLevel) {
				iFlagIndex |= 1 << iVertex;
			}
		}
		int[] mask = (rule == TopologyRule2D.Rule.CONNECT_4) ? afSquareValue4[iFlagIndex]
				: afSquareValue8[iFlagIndex];
		if (mask[0] < 4) {
			EdgeSplit split1 = createSplit(splits, x
					+ a2fVertex1Offset[mask[0]][0], y
					+ a2fVertex1Offset[mask[0]][1], x
					+ a2fVertex2Offset[mask[0]][0], y
					+ a2fVertex2Offset[mask[0]][1]);

			EdgeSplit split2 = createSplit(splits, x
					+ a2fVertex1Offset[mask[1]][0], y
					+ a2fVertex1Offset[mask[1]][1], x
					+ a2fVertex2Offset[mask[1]][0], y
					+ a2fVertex2Offset[mask[1]][1]);
			Edge edge = new Edge(split1.vid, split2.vid);
			/*
			 * if(!orient(split1, split2, edge)){
			 * System.out.println("FLIP "+iFlagIndex); }
			 */
			if (split1.e1 == null) {
				split1.e1 = edge;
			} else {
				split1.e2 = edge;
			}
			if (split2.e1 == null) {
				split2.e1 = edge;
			} else {
				split2.e2 = edge;
			}
			edges.add(edge);

		}

		if (mask[2] < 4) {
			EdgeSplit split1 = createSplit(splits, x
					+ a2fVertex1Offset[mask[2]][0], y
					+ a2fVertex1Offset[mask[2]][1], x
					+ a2fVertex2Offset[mask[2]][0], y
					+ a2fVertex2Offset[mask[2]][1]);

			EdgeSplit split2 = createSplit(splits, x
					+ a2fVertex1Offset[mask[3]][0], y
					+ a2fVertex1Offset[mask[3]][1], x
					+ a2fVertex2Offset[mask[3]][0], y
					+ a2fVertex2Offset[mask[3]][1]);

			Edge edge = new Edge(split1.vid, split2.vid);
			// orient(split1, split2, edge);
			if (split1.e1 == null) {
				split1.e1 = edge;
			} else {
				split1.e2 = edge;
			}
			if (split2.e1 == null) {
				split2.e1 = edge;
			} else {
				split2.e2 = edge;
			}
			edges.add(edge);
		}

	}

	/**
	 * Process square.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param splits
	 *            the splits
	 * @param edges
	 *            the edges
	 */
	private void processSquare1(int i, int j,
			Hashtable<Long, EdgeSplit> splits, LinkedList<Edge> edges) {
		float iF00 = getValue(i, j);
		float iF10 = getValue(i + 1, j);
		float iF01 = getValue(i, j + 1);
		float iF11 = getValue(i + 1, j + 1);
		boolean signFlip = false;
		if (iF00 != 0) {
			// convert to case "+***"

			if (iF00 < 0) {
				iF00 = -iF00;
				iF10 = -iF10;
				iF11 = -iF11;
				iF01 = -iF01;
				signFlip = true;
			}

			if (iF10 > 0) {
				if (iF11 > 0) {
					if (iF01 > 0) {
						// ++++
						return;
					} else {
						// +++-
						addEdge(splits, edges, i, j + 1, i + 1, j + 1, i,
								j + 1, i, j);
					}
				} else if (iF11 < 0) {
					if (iF01 > 0) {
						// ++-+
						addEdge(splits, edges, i + 1, j, i + 1, j + 1, i + 1,
								j + 1, i, j + 1);
					} else if (iF01 < 0) {
						// ++--
						addEdge(splits, edges, i, j + 1, i, j, i + 1, j, i + 1,
								j + 1);
					} else {
						// ++-0
						addEdge(splits, edges, i, j + 1, i, j + 1, i + 1, j,
								i + 1, j + 1);
					}
				} else {
					if (iF01 > 0) {
						// ++0+
						return;
					} else if (iF01 < 0) {
						// ++0-
						addEdge(splits, edges, i + 1, j + 1, i + 1, j + 1, i,
								j, i, j + 1);
					} else {
						// ++00
						addEdge(splits, edges, i + 1, j + 1, i + 1, j + 1, i,
								j + 1, i, j + 1);
					}
				}
			} else if (iF10 < 0) {
				if (iF11 > 0) {
					if (iF01 > 0) {
						// +-++
						addEdge(splits, edges, i, j, i + 1, j, i + 1, j + 1,
								i + 1, j);
					} else if (iF01 < 0) {
						// +-+-
						// Ambiguous Case
						if (rule == null) {
							float iD0 = iF00 - iF10;
							float iXN0 = iF00 * (i + 1) - iF10 * i;
							float iD3 = iF11 - iF01;
							float iXN1 = iF11 * i - iF01 * (i + 1);
							float iDet = 0;
							if (iD0 * iD3 > 0) {
								iDet = iXN1 * iD0 - iXN0 * iD3;
							} else {
								iDet = iXN0 * iD3 - iXN1 * iD0;
							}
							if (iDet > 0) {
								addEdge(splits, edges, i + 1, j + 1, i, j + 1,
										i + 1, j + 1, i + 1, j);
								addEdge(splits, edges, i, j, i + 1, j, i, j, i,
										j + 1);
							} else {
								addEdge(splits, edges, i + 1, j + 1, i, j + 1,
										i, j, i, j + 1);
								addEdge(splits, edges, i, j, i + 1, j, i + 1,
										j + 1, i + 1, j);
							}
						} else if (rule == TopologyRule2D.Rule.CONNECT_4) {
							if (signFlip) {
								addEdge(splits, edges, i + 1, j + 1, i, j + 1,
										i + 1, j + 1, i + 1, j);
								addEdge(splits, edges, i, j, i + 1, j, i, j, i,
										j + 1);
							} else {
								addEdge(splits, edges, i + 1, j + 1, i, j + 1,
										i, j, i, j + 1);
								addEdge(splits, edges, i, j, i + 1, j, i + 1,
										j + 1, i + 1, j);
							}
						} else if (rule == TopologyRule2D.Rule.CONNECT_8) {
							if (signFlip) {
								addEdge(splits, edges, i + 1, j + 1, i, j + 1,
										i, j, i, j + 1);
								addEdge(splits, edges, i, j, i + 1, j, i + 1,
										j + 1, i + 1, j);
							} else {
								addEdge(splits, edges, i + 1, j + 1, i, j + 1,
										i + 1, j + 1, i + 1, j);
								addEdge(splits, edges, i, j, i + 1, j, i, j, i,
										j + 1);
							}
						}
					} else {
						// +-+0
						addEdge(splits, edges, i, j, i + 1, j, i + 1, j + 1,
								i + 1, j);
					}
				} else if (iF11 < 0) {
					if (iF01 > 0) {
						// +--+
						addEdge(splits, edges, i, j, i + 1, j, i + 1, j + 1, i,
								j + 1);
					} else if (iF01 < 0) {
						// +---
						addEdge(splits, edges, i, j + 1, i, j, i, j, i + 1, j);
					} else {
						// +--0
						addEdge(splits, edges, i, j + 1, i, j + 1, i, j, i + 1,
								j);
					}
				} else {
					if (iF01 > 0) {
						// +-0+
						addEdge(splits, edges, i + 1, j + 1, i + 1, j + 1, i,
								j, i + 1, j);
					} else if (iF01 < 0) {
						// +-0-
						addEdge(splits, edges, i, j + 1, i, j, i, j, i + 1, j);
					} else {
						// +-00
						addEdge(splits, edges, i + 1, j + 1, i + 1, j + 1, i,
								j + 1, i + 1, j + 1);
						addEdge(splits, edges, i, j + 1, i + 1, j + 1, i,
								j + 1, i, j + 1);
						addEdge(splits, edges, i, j + 1, i + 1, j + 1, i, j,
								i + 1, j);
					}
				}
			} else {
				if (iF11 > 0) {
					if (iF01 > 0) {
						// +0++
					} else if (iF01 < 0) {
						// +0+-
						addEdge(splits, edges, i, j + 1, i + 1, j + 1, i,
								j + 1, i, j);
					}
				} else if (iF11 < 0) {
					if (iF01 > 0) {
						// +0-+
						addEdge(splits, edges, i + 1, j, i + 1, j, i, j + 1,
								i + 1, j + 1);
					} else if (iF01 < 0) {
						// +0--
						addEdge(splits, edges, i + 1, j, i + 1, j, i, j, i,
								j + 1);
					} else {
						// +0-0
						addEdge(splits, edges, i + 1, j, i + 1, j, i, j + 1, i,
								j + 1);
					}
				} else {
					if (iF01 > 0) {
						// +00+
						addEdge(splits, edges, i + 1, j, i + 1, j, i + 1,
								j + 1, i + 1, j + 1);
					} else if (iF01 < 0) {
						// +00-
						addEdge(splits, edges, i + 1, j, i + 1, j, i + 1, j,
								i + 1, j + 1);
						addEdge(splits, edges, i + 1, j, i + 1, j + 1, i + 1,
								j + 1, i + 1, j + 1);
						addEdge(splits, edges, i + 1, j, i + 1, j + 1, i, j, i,
								j + 1);
					} else {
						// +000
						addEdge(splits, edges, i, j + 1, i, j + 1, i, j, i, j);
						addEdge(splits, edges, i, j, i, j, i + 1, j, i + 1, j);
					}
				}
			}
		} else if (iF10 != 0) {
			// convert to case 0+**
			if (iF10 < 0) {
				iF10 = -iF10;
				iF11 = -iF11;
				iF01 = -iF01;
			}

			if (iF11 > 0) {
				if (iF01 > 0) {
					// 0+++
				} else if (iF01 < 0) {
					// 0++-
					addEdge(splits, edges, i, j, i, j, i, j + 1, i + 1, j + 1);
				} else {
					// 0++0
					addEdge(splits, edges, i, j + 1, i, j + 1, i, j, i, j);
				}
			} else if (iF11 < 0) {
				if (iF01 > 0) {
					// 0+-+
					addEdge(splits, edges, i + 1, j, i + 1, j + 1, i + 1,
							j + 1, i, j + 1);
				} else if (iF01 < 0) {
					// 0+--
					addEdge(splits, edges, i, j, i, j, i + 1, j, i + 1, j + 1);
				} else {
					// 0+-0
					addEdge(splits, edges, i, j, i, j, i, j, i, j + 1);
					addEdge(splits, edges, i, j, i, j + 1, i, j + 1, i, j + 1);
					addEdge(splits, edges, i, j, i, j + 1, i + 1, j, i + 1,
							j + 1);
				}
			} else {
				if (iF01 > 0) {
					// 0+0+
				} else if (iF01 < 0) {
					// 0+0-
					addEdge(splits, edges, i, j, i, j, i + 1, j + 1, i + 1,
							j + 1);
				} else {
					// 0+00
					addEdge(splits, edges, i + 1, j + 1, i + 1, j + 1, i,
							j + 1, i, j + 1);
					addEdge(splits, edges, i, j + 1, i, j + 1, i, j, i, j);
				}
			}
		} else if (iF11 != 0) {
			// convert to case 00+*
			if (iF11 < 0) {
				iF11 = -iF11;
				iF01 = -iF01;
			}

			if (iF01 > 0) {
				// 00++
				addEdge(splits, edges, i, j, i, j, i + 1, j, i + 1, j);
			} else if (iF01 < 0) {
				// 00+-
				addEdge(splits, edges, i, j, i, j, i, j, i + 1, j);
				addEdge(splits, edges, i, j, i + 1, j, i + 1, j, i + 1, j);
				addEdge(splits, edges, i, j, i + 1, j, i, j + 1, i + 1, j + 1);
			} else {
				// 00+0
				addEdge(splits, edges, i + 1, j, i + 1, j, i + 1, j + 1, i + 1,
						j + 1);
				addEdge(splits, edges, i + 1, j + 1, i + 1, j + 1, i, j + 1, i,
						j + 1);
			}
		} else if (iF01 != 0) {
			// cases 000+ or 000-
			addEdge(splits, edges, i, j, i, j, i + 1, j, i + 1, j);
			addEdge(splits, edges, i + 1, j, i + 1, j, i + 1, j + 1, i + 1,
					j + 1);
		} else {
			// case 0000
			addEdge(splits, edges, i, j, i, j, i + 1, j, i + 1, j);
			addEdge(splits, edges, i + 1, j, i + 1, j, i + 1, j + 1, i + 1,
					j + 1);
			addEdge(splits, edges, i + 1, j + 1, i + 1, j + 1, i, j + 1, i,
					j + 1);
			addEdge(splits, edges, i, j + 1, i, j + 1, i, j, i, j);
		}
	}

	/**
	 * Adds the edge.
	 * 
	 * @param splits
	 *            the splits
	 * @param edges
	 *            the edges
	 * @param p1x
	 *            the p1x
	 * @param p1y
	 *            the p1y
	 * @param p2x
	 *            the p2x
	 * @param p2y
	 *            the p2y
	 * @param p3x
	 *            the p3x
	 * @param p3y
	 *            the p3y
	 * @param p4x
	 *            the p4x
	 * @param p4y
	 *            the p4y
	 */
	protected void addEdge(Hashtable<Long, EdgeSplit> splits,
			LinkedList<Edge> edges, int p1x, int p1y, int p2x, int p2y,
			int p3x, int p3y, int p4x, int p4y) {

		EdgeSplit split1 = createSplit(splits, p1x, p1y, p2x, p2y);
		EdgeSplit split2 = createSplit(splits, p3x, p3y, p4x, p4y);
		lastSplit1 = split1;
		lastSplit2 = split2;
		Edge edge = new Edge(split1.vid, split2.vid);
		if (split1.e1 == null) {
			split1.e1 = edge;
		} else {
			split1.e2 = edge;
		}
		if (split2.e1 == null) {
			split2.e1 = edge;
		} else {
			split2.e2 = edge;
		}
		edges.add(edge);
	}

	/**
	 * Creates the split.
	 * 
	 * @param splits
	 *            the splits
	 * @param p1x
	 *            the p1x
	 * @param p1y
	 *            the p1y
	 * @param p2x
	 *            the p2x
	 * @param p2y
	 *            the p2y
	 * 
	 * @return the edge split
	 */
	protected EdgeSplit createSplit(Hashtable<Long, EdgeSplit> splits, int p1x,
			int p1y, int p2x, int p2y) {
		EdgeSplit split = new EdgeSplit(new Point2i(p1x, p1y), new Point2i(p2x,
				p2y));
		EdgeSplit foundSplit = splits.get(split.hashValue());
		if (foundSplit == null) {
			split.vid = vertCount++;
			Point2f pt2d = new Point2f();
			float fOffset = fGetOffset(split.pt1, split.pt2);
			float fInvOffset = 1.0f - fOffset;
			pt2d.x = resX * (fInvOffset * p1x + fOffset * p2x);
			pt2d.y = resY * (fInvOffset * p1y + fOffset * p2y);
			split.pt2d = pt2d;
			splits.put(split.hashValue(), split);
			return split;
		} else {
			return foundSplit;
		}
	}

	/**
	 * Interpolate position along edge.
	 * 
	 * @param v1
	 *            the lower grid point
	 * @param v2
	 *            the upper grid point
	 * 
	 * @return the position
	 */
	protected float fGetOffset(Point2i v1, Point2i v2) {
		float fValue1 = getValue(v1.x, v1.y);
		float fValue2 = getValue(v2.x, v2.y);
		double fDelta = fValue2 - fValue1;
		if (fDelta == 0.0) {
			return 0.5f;
		}
		return (float) (-fValue1 / fDelta);
	}

	/**
	 * Gets the value.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the value
	 */
	protected float getValue(int i, int j) {
		int x = Math.max(Math.min(rows - 1, i), 0);
		int y = Math.max(Math.min(cols - 1, j), 0);
		float val;
		// if(x==0||y==0||x==rows-1||y==cols-1){
		// val=1;
		// } else {
		if (fieldMat != null) {
			val = fieldMat[x][y][slice][component] - isoLevel;
		} else if (volMat != null) {
			val = volMat[x][y][slice] - isoLevel;
		} else {
			val = imgMat[x][y] - isoLevel;
		}
		if (nudgeLevelSet) {
			// Push iso-level away from zero level set
			if (val < 0) {
				val = Math.min(val, -LEVEL_SET_TOLERANCE);
			} else {
				val = Math.max(val, LEVEL_SET_TOLERANCE);
			}
		}
		// }
		return val;
	}

	/**
	 * Clamp row.
	 * 
	 * @param r
	 *            the row
	 * 
	 * @return the row
	 */
	protected int clampRow(int r) {
		return Math.max(Math.min(r, rows - 1), 0);
	}

	/**
	 * Clamp column index.
	 * 
	 * @param c
	 *            the column
	 * 
	 * @return the column
	 */
	protected int clampColumn(int c) {
		return Math.max(Math.min(c, cols - 1), 0);
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		return VersionUtil.parseRevisionNumber("$Revision: 1.13 $");
	}

	/**
	 * Sets the connectivity rule.
	 *
	 * @param rule the new connectivity rule
	 */
	public void setConnectivityRule(TopologyRule2D.Rule rule) {
		this.rule = rule;
	}

	/**
	 * Sets the nudge level set.
	 * 
	 * @param nudgeLevelSet
	 *            the new nudge level set
	 */
	public void setNudgeLevelSet(boolean nudgeLevelSet) {
		this.nudgeLevelSet = nudgeLevelSet;
	}

	/**
	 * Sets the use resolutions.
	 * 
	 * @param useResolutions
	 *            the new use resolutions
	 */
	public void setUseResolutions(boolean useResolutions) {
		this.useResolutions = useResolutions;
	}

	/**
	 * Sets the winding.
	 * 
	 * @param winding
	 *            the new winding
	 */
	public void setWinding(Winding winding) {
		this.winding = winding;
	}

	/**
	 * Orient.
	 *
	 * @param split1 the split1
	 * @param split2 the split2
	 * @param edge the edge
	 * @return true, if successful
	 */
	private boolean orient(EdgeSplit split1, EdgeSplit split2, Edge edge) {
		Point2f pt1 = split1.pt2d;
		Point2f pt2 = split2.pt2d;
		Vector2f norm = DataOperations.gradient(imgMat, 0.5f * (pt1.x + pt2.x),
				0.5f * (pt2.y + pt2.y));
		if (norm.x * (pt2.y - pt1.y) + norm.y * (pt1.x - pt2.x) > 0) {
			int tmp = edge.vids[0];
			edge.vids[0] = edge.vids[1];
			edge.vids[1] = tmp;
			return false;
		}
		return true;
	}
}
