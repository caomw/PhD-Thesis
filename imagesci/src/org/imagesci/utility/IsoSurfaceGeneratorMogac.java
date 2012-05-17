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
package org.imagesci.utility;


import java.util.Hashtable;
import java.util.LinkedList;

import javax.vecmath.Point3f;

import org.imagesci.gac.TopologyPreservationRule3D;

import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class IsoSurfaceGeneratorMogac.
 */
public class IsoSurfaceGeneratorMogac extends IsoSurfaceGenerator {
	
	/** The Constant LEVEL_SET_TOLERANCE. */
	public static final float LEVEL_SET_TOLERANCE = 1E-3f;
	
	/** The contains overlap. */
	protected boolean containsOverlap = false;

	/** The current label. */
	protected int currentLabel;

	/** The labels. */
	protected int[][][] labels;

	/**
	 * Instantiates a new iso surface generator mogac.
	 *
	 * @param parent the parent
	 * @param rule the rule
	 */
	public IsoSurfaceGeneratorMogac(AbstractCalculation parent,
			TopologyPreservationRule3D.Rule rule) {
		super(parent);
		this.connectivityRule = rule;
		setLabel("Iso-Surface");
	}

	/**
	 * Instantiates a new iso surface generator mogac.
	 *
	 * @param rule the rule
	 */
	public IsoSurfaceGeneratorMogac(TopologyPreservationRule3D.Rule rule) {
		super();
		this.connectivityRule = rule;
		setLabel("Iso-Surface");
	}

	/**
	 * Solve.
	 *
	 * @param levelset the levelset
	 * @param labelImage the label image
	 * @param labelMasks the label masks
	 * @param containsOverlap the contains overlap
	 * @return the embedded surface[]
	 */
	public EmbeddedSurface[] solve(ImageDataFloat levelset,
			ImageDataInt labelImage, int[] labelMasks, boolean containsOverlap) {
		int numObjects = labelMasks.length - 1;
		EmbeddedSurface[] contours = new EmbeddedSurface[numObjects];
		this.labels = labelImage.toArray3d();
		this.volMat = levelset.toArray3d();
		this.containsOverlap = containsOverlap;
		rows = levelset.getRows();
		cols = levelset.getCols();
		slices = levelset.getSlices();
		for (int nn = 0; nn < contours.length; nn++) {
			vertCount = 0;
			this.isoLevel = 0;
			if (useResolutions) {
				float[] res = levelset.getHeader().getDimResolutions();
				resX = res[0];
				resY = res[1];
				resZ = res[2];
			} else {
				resX = resY = resZ = 1;
			}
			this.currentLabel = labelMasks[nn + 1];
			Hashtable<Long, EdgeSplit> splits = new Hashtable<Long, EdgeSplit>();
			LinkedList<Triangle> triangles = new LinkedList<Triangle>();
			if (!silent) {
				setTotalUnits(rows);
			}
			// Solve for iso-surface
			if (method == Method.MARCHING_CUBES || connectivityRule != null) {
				if (connectivityRule == null) {
					for (int i = 0; i < rows; i++) {
						for (int j = 0; j < cols; j++) {
							for (int k = 0; k < slices; k++) {
								triangulateUsingMarchingCubes(splits,
										triangles, i, j, k);
							}
						}
						if (!silent) {
							incrementCompletedUnits();
						}
					}
				} else {
					for (int i = 0; i < rows; i++) {
						for (int j = 0; j < cols; j++) {
							for (int k = 0; k < slices; k++) {
								triangulateUsingMarchingCubesConnectivityConsistent(
										splits, triangles, i, j, k);
							}
						}
						if (!silent) {
							incrementCompletedUnits();
						}
					}
				}
			} else if (method == Method.MARCHING_TETRAHEDRALS) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						for (int k = 0; k < slices; k++) {
							triangulateUsingMarchingTetrahedrals(splits,
									triangles, i, j, k);
						}
					}
					if (!silent) {
						incrementCompletedUnits();
					}
				}
			}
			// Generate iso-surface from list of triangles
			int[] indexes = new int[triangles.size() * 3];
			Point3f[] points = new Point3f[splits.size()];
			int index = 0;
			if (winding == Winding.CLOCKWISE) {
				for (Triangle tri : triangles) {
					indexes[index++] = tri.vids[0];
					indexes[index++] = tri.vids[1];
					indexes[index++] = tri.vids[2];
				}
			} else if (winding == Winding.COUNTER_CLOCKWISE) {
				for (Triangle tri : triangles) {
					indexes[index++] = tri.vids[2];
					indexes[index++] = tri.vids[1];
					indexes[index++] = tri.vids[0];
				}
			}
			index = 0;
			for (EdgeSplit split : splits.values()) {
				index = split.vid;
				points[index] = split.pt3d;
			}
			// Create surface
			System.out.println(currentLabel + " POINTS " + points.length + " "
					+ indexes.length);
			EmbeddedSurface surf = new EmbeddedSurface(points, indexes);
			surf.setName(levelset.getName() + "_" + currentLabel);
			contours[nn] = surf;
		}
		volMat = null;
		labels = null;
		return contours;
	}

	/**
	 * Get level set value.
	 *
	 * @param i row
	 * @param j column
	 * @param k the k
	 * @return the value
	 */
	@Override
	protected float getValue(int i, int j, int k) {
		float val = 0;
		int x = Math.max(Math.min(rows - 1, i), 0);
		int y = Math.max(Math.min(cols - 1, j), 0);
		int z = Math.max(Math.min(slices - 1, k), 0);
		float sign = 0;
		// if (containsOverlap) {
		// sign = ((labels[x][y][z] & currentLabel) != 0) ? -1 : 1;
		// } else {
		sign = (labels[x][y][z] == currentLabel) ? -1 : 1;
		// }
		val = sign * volMat[x][y][z] - isoLevel;
		if (nudgeLevelSet) {
			// Push iso-level away from zero level set
			if (val < 0) {
				val = Math.min(val, -LEVEL_SET_TOLERANCE);
			} else {
				val = Math.max(val, LEVEL_SET_TOLERANCE);
			}
		}
		return val;
	}
}
