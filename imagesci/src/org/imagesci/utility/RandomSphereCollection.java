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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.vecmath.Point3f;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class RandomSphereCollection.
 */
public class RandomSphereCollection {
	
	/** The distfield image. */
	protected ImageDataFloat distfieldImage;
	
	/** The label image. */
	protected ImageDataInt labelImage;

	/**
	 * Instantiates a new random sphere collection.
	 *
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param numDots the num dots
	 * @param radius the radius
	 */
	public RandomSphereCollection(int rows, int cols, int slices, int numDots,
			float radius) {

		int gridRows = (int) Math.floor(rows / (2 * radius + 3));
		int gridCols = (int) Math.floor(cols / (2 * radius + 3));
		int gridSlices = (int) Math.floor(slices / (2 * radius + 3));
		float deltaX = rows / (float) gridRows;
		float deltaY = cols / (float) gridCols;
		float deltaZ = slices / (float) gridSlices;
		numDots = Math.min(numDots, gridRows * gridCols * gridSlices);
		distfieldImage = new ImageDataFloat(rows, cols, slices);
		labelImage = new ImageDataInt(rows, cols, slices);
		distfieldImage.setName("dots_distfield");
		labelImage.setName("dots_labels");

		ArrayList<Point3f> gridPoints = new ArrayList<Point3f>(gridRows
				* gridCols);
		for (int k = 0; k < gridSlices; k++) {
			for (int j = 0; j < gridCols; j++) {
				for (int i = 0; i < gridRows; i++) {
					gridPoints.add(new Point3f((i + 0.5f) * deltaX, (j + 0.5f)
							* deltaY, (k + 0.5f) * deltaZ));
				}
			}
		}
		Random randn = new Random(738957323l);
		Collections.shuffle(gridPoints, randn);

		float[][][] unsigned = distfieldImage.toArray3d();
		int[][][] labels = labelImage.toArray3d();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					float val = 10.0f;
					int label = 0;
					for (int l = 0; l < numDots; l++) {
						Point3f pt = gridPoints.get(l);
						float dist = (pt.x - i) * (pt.x - i) + (pt.y - j)
								* (pt.y - j) + (pt.z - k) * (pt.z - k);
						if (dist < radius * radius) {
							label = l + 1;
						}
						val = (float) Math.min(val,
								Math.abs(Math.sqrt(dist) - radius));
					}
					labels[i][j][k] = label;
					unsigned[i][j][k] = val;
				}
			}
		}
	}

	/**
	 * Gets the distance field.
	 *
	 * @return the distance field
	 */
	public ImageDataFloat getDistanceField() {
		return distfieldImage;
	}

	/**
	 * Gets the label image.
	 *
	 * @return the label image
	 */
	public ImageDataInt getLabelImage() {
		return labelImage;
	}
}
