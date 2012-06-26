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
package org.imagesci.muscle;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class CompressLevelSets compresses level sets into a label mask and
 * distance field.
 */
public class CompressLevelSets {

	/** The label image. */
	protected ImageDataInt labelImage;

	/** The level set image. */
	protected ImageDataFloat levelSetImage;

	/**
	 * Instantiates a new compress level sets.
	 * 
	 * @param images
	 *            the images
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
