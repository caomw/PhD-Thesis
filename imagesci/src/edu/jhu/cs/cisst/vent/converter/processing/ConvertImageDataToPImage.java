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
package edu.jhu.cs.cisst.vent.converter.processing;

import java.awt.Color;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.converter.ConvertImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.VoxelType;

// TODO: Auto-generated Javadoc
/**
 * The Class ConvertImageDataToPImage.
 */
public class ConvertImageDataToPImage implements ConvertImageData<PImage> {

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * @return the p image
	 * @see edu.jhu.cs.cisst.vent.converter.ConvertImageData#convert(edu.jhu.ece.iacl.jist.structures.image.ImageData)
	 */
	@Override
	public PImage convert(ImageData source) {
		return convert(source, 0, 1.0, 0.0);
	}

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * @param slice
	 *            the slice
	 * 
	 * @return the p image
	 */
	public PImage convert(ImageData source, int slice) {
		return convert(source, slice, 1, 0);
	}

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * @param slice
	 *            the slice
	 * @param contrast
	 *            the contrast
	 * @param brightness
	 *            the brightness
	 * 
	 * @return the p image
	 */
	public PImage convert(ImageData source, int slice, double contrast,
			double brightness) {
		return convert(source, slice, 0, contrast, brightness);
	}

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * @param slice
	 *            the slice
	 * @param component
	 *            the component
	 * @param contrast
	 *            the contrast
	 * @param brightness
	 *            the brightness
	 * 
	 * @return the p image
	 */
	public PImage convert(ImageData source, int slice, int component,
			double contrast, double brightness) {
		int rows = source.getRows();
		int cols = source.getCols();
		PImage img = new PImage(rows, cols);
		float[] hsb = new float[4];
		if (source.getType() == VoxelType.COLOR
				|| source.getType() == VoxelType.COLOR_FLOAT
				|| source.getType() == VoxelType.COLOR_USHORT) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					Color color = source.getColor(r, c, slice, component);
					Color.RGBtoHSB(color.getRed(), color.getGreen(),
							color.getBlue(), hsb);
					hsb[2] = (float) Math.max(
							Math.min(hsb[2] * contrast + brightness, 1.0f),
							0.0f);
					img.set(r, c, Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
				}
			}
		} else {
			double min = 1E10f;
			double max = -1E10f;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					min = Math.min(min, source.getDouble(i, j, slice));
					max = Math.max(max, source.getDouble(i, j, slice));
				}
			}
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					double val = (source.getDouble(r, c, slice, component) - min)
							/ (max - min);
					val = Math.min(1, Math.max(0, val * contrast + brightness));
					img.set(r, c, (new Color((float) val, (float) val,
							(float) val)).getRGB());
				}
			}
		}
		return img;
	}

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * @param slice
	 *            the slice
	 * @param component
	 *            the component
	 * @param contrast
	 *            the contrast
	 * @param brightness
	 *            the brightness
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the p image
	 */
	public PImage convertSlice(ImageData source, int slice, int component,
			double contrast, double brightness, double min, double max) {
		return convert(source, slice, component, contrast, brightness, min, max);
	}

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * @param slice
	 *            the slice
	 * @param component
	 *            the component
	 * @param contrast
	 *            the contrast
	 * @param brightness
	 *            the brightness
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the p image
	 */
	public PImage convert(ImageData source, int slice, int component,
			double contrast, double brightness, double min, double max) {
		int rows = source.getRows();
		int cols = source.getCols();
		PImage img = new PImage(rows, cols);
		if (source.getType() == VoxelType.COLOR
				|| source.getType() == VoxelType.COLOR_FLOAT
				|| source.getType() == VoxelType.COLOR_USHORT) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					img.set(r, c, source.getColor(r, c, slice, component)
							.getRGB());
				}
			}
		} else {

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					double val = (source.getDouble(r, c, slice, component) - min)
							/ (max - min);
					val = Math.min(1, Math.max(0, val * contrast + brightness));
					img.set(r, c, (new Color((float) val, (float) val,
							(float) val)).getRGB());
				}
			}
		}
		return img;
	}

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * @param col
	 *            the col
	 * @param component
	 *            the component
	 * @param contrast
	 *            the contrast
	 * @param brightness
	 *            the brightness
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the p image
	 */
	public PImage convertColumn(ImageData source, int col, int component,
			double contrast, double brightness, double min, double max) {
		int rows = source.getRows();
		int slices = source.getSlices();
		PImage img = new PImage(rows, slices);
		if (source.getType() == VoxelType.COLOR
				|| source.getType() == VoxelType.COLOR_FLOAT
				|| source.getType() == VoxelType.COLOR_USHORT) {
			for (int r = 0; r < rows; r++) {
				for (int s = 0; s < slices; s++) {
					img.set(r, col, source.getColor(r, col, s, component)
							.getRGB());
				}
			}
		} else {

			for (int r = 0; r < rows; r++) {
				for (int s = 0; s < slices; s++) {
					double val = (source.getDouble(r, col, s, component) - min)
							/ (max - min);
					val = Math.min(1, Math.max(0, val * contrast + brightness));
					img.set(r, s, (new Color((float) val, (float) val,
							(float) val)).getRGB());
				}
			}
		}
		return img;
	}

	/**
	 * Convert Row.
	 * 
	 * @param source
	 *            the source
	 * @param row
	 *            the row
	 * @param component
	 *            the component
	 * @param contrast
	 *            the contrast
	 * @param brightness
	 *            the brightness
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the p image
	 */
	public PImage convertRow(ImageData source, int row, int component,
			double contrast, double brightness, double min, double max) {
		int cols = source.getCols();
		int slices = source.getSlices();
		PImage img = new PImage(cols, slices);
		if (source.getType() == VoxelType.COLOR
				|| source.getType() == VoxelType.COLOR_FLOAT
				|| source.getType() == VoxelType.COLOR_USHORT) {
			for (int c = 0; c < cols; c++) {
				for (int s = 0; s < slices; s++) {
					img.set(c, s, source.getColor(row, c, s, component)
							.getRGB());
				}
			}
		} else {
			for (int c = 0; c < cols; c++) {
				for (int s = 0; s < slices; s++) {
					double val = (source.getDouble(row, c, s, component) - min)
							/ (max - min);
					val = Math.min(1, Math.max(0, val * contrast + brightness));
					img.set(c, s, (new Color((float) val, (float) val,
							(float) val)).getRGB());
				}
			}
		}
		return img;
	}
}
