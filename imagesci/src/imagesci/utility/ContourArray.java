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
package imagesci.utility;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class ContourArray.
 */
public class ContourArray {
	
	/** The edge data. */
	public double[][] edgeData = null;
	
	/** The indexes. */
	public int[] indexes;
	
	/** The points. */
	public Point2f[] points;
	
	/** The vertex data. */
	public double[][] vertexData = null;

	/**
	 * Instantiates a new contour array.
	 *
	 * @param pts the pts
	 * @param indexes the indexes
	 */
	public ContourArray(Point2f[] pts, int[] indexes) {
		this.indexes = indexes;
		this.points = pts;
	}

	/**
	 * Gets the centroid.
	 *
	 * @param id the id
	 * @return the centroid
	 */
	public Point2f getCentroid(int id) {
		Point2f pt1 = points[indexes[id * 2]];
		Point2f pt2 = points[indexes[id * 2 + 1]];
		return new Point2f(0.5f * (pt1.x + pt2.x), 0.5f * (pt1.y + pt2.y));
	}

	/**
	 * Gets the coordinate index.
	 *
	 * @param i the i
	 * @return the coordinate index
	 */
	public int getCoordinateIndex(int i) {
		return indexes[i];
	}

	/**
	 * Gets the edge count.
	 *
	 * @return the edge count
	 */
	public int getEdgeCount() {
		return indexes.length / 2;
	}

	/**
	 * Gets the edge points.
	 *
	 * @param id the id
	 * @return the edge points
	 */
	public Point2f[] getEdgePoints(int id) {
		return new Point2f[] { new Point2f(points[indexes[id * 2]]),
				new Point2f(points[indexes[id * 2 + 1]]) };
	}

	/**
	 * Gets the index count.
	 *
	 * @return the index count
	 */
	public int getIndexCount() {
		return indexes.length;
	}

	/**
	 * Gets the vertex.
	 *
	 * @param id the id
	 * @return the vertex
	 */
	public Point2f getVertex(int id) {
		return points[id];
	}

	/**
	 * Orient.
	 *
	 * @param img the img
	 */
	public void orient(float[][] img) {
		for (int i = 0; i < indexes.length; i += 2) {
			Point2f pt1 = points[indexes[i]];
			Point2f pt2 = points[indexes[i + 1]];
			Vector2f norm = DataOperations.gradient(img,
					0.5f * (pt1.x + pt2.x), 0.5f * (pt2.y + pt2.y));
			if (norm.x * (pt2.y - pt1.y) + norm.y * (pt1.x - pt2.x) > 0) {
				int tmp = indexes[i];
				indexes[i] = indexes[i + 1];
				indexes[i + 1] = tmp;
			}
		}
	}

	/**
	 * Orient.
	 *
	 * @param img the img
	 */
	public void orient(ImageDataFloat img) {
		for (int i = 0; i < indexes.length; i += 2) {
			Point2f pt1 = points[indexes[i]];
			Point2f pt2 = points[indexes[i + 1]];
			Vector2f norm = DataOperations.gradient(img.toArray2d(),
					0.5f * (pt1.x + pt2.x), 0.5f * (pt2.y + pt2.y));
			if (norm.x * (pt2.y - pt1.y) + norm.y * (pt1.x - pt2.x) > 0) {
				int tmp = indexes[i];
				indexes[i] = indexes[i + 1];
				indexes[i + 1] = tmp;
			}
		}
	}
}
