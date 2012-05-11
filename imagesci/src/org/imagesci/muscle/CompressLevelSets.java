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
package org.imagesci.muscle;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class CompressLevelSets.
 */
public class CompressLevelSets {
	
	/** The label image. */
	protected ImageDataInt labelImage;
	
	/** The level set image. */
	protected ImageDataFloat levelSetImage;

	/**
	 * Instantiates a new compress level sets.
	 *
	 * @param images the images
	 */
	public CompressLevelSets(ImageDataFloat[] images) {
		ImageDataFloat first = images[0];
		if (first.toArray2d() != null) {
			int rows = first.getRows();
			int cols = first.getCols();
			int mask = 1;
			labelImage = new ImageDataInt(rows, cols);
			levelSetImage = new ImageDataFloat(rows, cols);
			int[][] labels = labelImage.toArray2d();
			float[][] levelset = levelSetImage.toArray2d();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					levelset[i][j] = 10.0f;
				}
			}
			int index = 0;
			for (ImageData img : images) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						float val = img.getFloat(i, j);
						levelset[i][j] = Math
								.min(levelset[i][j], Math.abs(val));
						if (val <= 0) {
							labels[i][j] |= mask;
						}
					}
				}
				mask <<= 1;
				index++;
			}
			labelImage.setName(first.getName() + "_labels");
			levelSetImage.setName(first.getName() + "_distfield");
		} else if (first.toArray3d() != null) {
			int rows = first.getRows();
			int cols = first.getCols();
			int slices = first.getSlices();
			int mask = 1;
			labelImage = new ImageDataInt(rows, cols, slices);
			levelSetImage = new ImageDataFloat(rows, cols, slices);
			int[][][] labels = labelImage.toArray3d();
			float[][][] levelset = levelSetImage.toArray3d();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						levelset[i][j][k] = 10.0f;
					}
				}
			}
			int index = 0;
			for (ImageData img : images) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						for (int k = 0; k < slices; k++) {
							float val = img.getFloat(i, j, k);
							levelset[i][j][k] = Math.min(levelset[i][j][k],
									Math.abs(val));
							if (val <= 0) {
								labels[i][j][k] |= mask;
							}
						}
					}
				}
				mask <<= 1;
				index++;
			}
			labelImage.setName(first.getName() + "_labels");
			levelSetImage.setName(first.getName() + "_distfield");
		}
	}

	/**
	 * Gets the distance field image.
	 *
	 * @return the distance field image
	 */
	public ImageDataFloat getDistanceFieldImage() {
		return levelSetImage;
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
