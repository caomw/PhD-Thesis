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
package org.imagesci.playground;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class DataOperations defines basic operations to perform on raw data.
 */
public final class DataOperations {

	/**
	 * Curl.
	 *
	 * @param image the image
	 * @param levelset the levelset
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public static double curl(float[][][] image, float[][] levelset, int i,
			int j) {
		int rows = image.length;
		int cols = image[0].length;
		double l21 = getImageValue(levelset, i + 1, j, rows, cols);
		double l01 = getImageValue(levelset, i - 1, j, rows, cols);
		double l12 = getImageValue(levelset, i, j + 1, rows, cols);
		double l10 = getImageValue(levelset, i, j - 1, rows, cols);

		double yx21 = (l21 > 0) ? getImageValue(image, i + 1, j, 1, rows, cols,
				2) : image[i][j][1];
		double yx01 = (l01 > 0) ? getImageValue(image, i - 1, j, 1, rows, cols,
				2) : image[i][j][1];
		double xy12 = (l12 > 0) ? getImageValue(image, i, j + 1, 0, rows, cols,
				2) : image[i][j][0];
		double xy10 = (l10 > 0) ? getImageValue(image, i, j - 1, 0, rows, cols,
				2) : image[i][j][0];

		double dy = ((yx21 - yx01) * 0.5);
		double dx = ((xy12 - xy10) * 0.5);
		return dy - dx;
	}

	/**
	 * Divergence.
	 *
	 * @param image the image
	 * @param levelset the levelset
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public static double divergence(float[][][] image, float[][] levelset,
			int i, int j) {
		int rows = image.length;
		int cols = image[0].length;
		double l21 = getImageValue(levelset, i + 1, j, rows, cols);
		double l01 = getImageValue(levelset, i - 1, j, rows, cols);
		double l12 = getImageValue(levelset, i, j + 1, rows, cols);
		double l10 = getImageValue(levelset, i, j - 1, rows, cols);

		double v21 = (l21 > 0) ? getImageValue(image, i + 1, j, 0, rows, cols,
				2) : image[i][j][0];
		double v01 = (l01 > 0) ? getImageValue(image, i - 1, j, 0, rows, cols,
				2) : image[i][j][0];
		double v12 = (l12 > 0) ? getImageValue(image, i, j + 1, 1, rows, cols,
				2) : image[i][j][1];
		double v10 = (l10 > 0) ? getImageValue(image, i, j - 1, 1, rows, cols,
				2) : image[i][j][1];

		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return dx + dy;
	}

	/**
	 * Blur.
	 * 
	 * @param image
	 *            the image
	 * @param stddev
	 *            the stddev
	 * 
	 * @return the float[][]
	 */
	public static final float[][] blur(float[][] image, double stddev) {
		double f = 0.5 * stddev * stddev;
		double timeStep = 0.999;
		int iterations = (int) Math.ceil(f / timeStep);
		// Insure that time step will achieve exactly the target standard
		// deviation
		timeStep = f / iterations;
		int rows = image.length;
		int cols = image[0].length;
		float[][] blurImage = new float[rows][cols];
		// Copy image
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				blurImage[i][j] = image[i][j];
			}
		}
		// Smooth image
		for (int iter = 0; iter < iterations; iter++) {
			for (int l = 0; l < 2; l++) {
				for (int m = 0; m < 2; m++) {
					for (int i = l; i < rows; i += 2) {
						for (int j = m; j < cols; j += 2) {
							double v21 = getImageValue(blurImage, i + 1, j,
									rows, cols);
							double v12 = getImageValue(blurImage, i, j + 1,
									rows, cols);
							double v11 = blurImage[i][j];
							double v10 = getImageValue(blurImage, i, j - 1,
									rows, cols);
							double v01 = getImageValue(blurImage, i - 1, j,
									rows, cols);
							double Dxx = v21 - v11 - v11 + v01;
							double Dyy = v12 - v11 - v11 + v10;
							blurImage[i][j] = (float) (v11 + 0.25 * timeStep
									* (Dxx + Dyy));
						}
					}
				}
			}
		}

		return blurImage;
	}

	/**
	 * Gradient.
	 * 
	 * @param image
	 *            the image
	 * @param componentsFirst
	 *            indicate components come first
	 * 
	 * @return the float[][][]
	 */
	public static float[][][] gradient(float[][] image, boolean componentsFirst) {
		int rows = image.length;
		int cols = image[0].length;
		if (componentsFirst) {
			float[][][] gradient = new float[2][rows][cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					double v21 = getImageValue(image, i + 1, j, rows, cols);
					double v12 = getImageValue(image, i, j + 1, rows, cols);
					double v10 = getImageValue(image, i, j - 1, rows, cols);
					double v01 = getImageValue(image, i - 1, j, rows, cols);
					gradient[0][i][j] = (float) ((v21 - v01) * 0.5);
					gradient[1][i][j] = (float) ((v12 - v10) * 0.5);
				}
			}
			return gradient;
		} else {
			float[][][] gradient = new float[rows][cols][2];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					double v21 = getImageValue(image, i + 1, j, rows, cols);
					double v12 = getImageValue(image, i, j + 1, rows, cols);
					double v10 = getImageValue(image, i, j - 1, rows, cols);
					double v01 = getImageValue(image, i - 1, j, rows, cols);
					gradient[i][j][0] = (float) ((v21 - v01) * 0.5);
					gradient[i][j][1] = (float) ((v12 - v10) * 0.5);
				}
			}
			return gradient;
		}
	}

	/**
	 * Gradient.
	 *
	 * @param image the image
	 * @param x the x
	 * @param y the y
	 * @return the vector2d
	 */
	public static Vector2d gradient(float[][] image, double x, double y) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = interpolate(x + 1, y, image, rows, cols);
		double v12 = interpolate(x, y + 1, image, rows, cols);
		double v10 = interpolate(x, y - 1, image, rows, cols);
		double v01 = interpolate(x - 1, y, image, rows, cols);
		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return new Vector2d(dx, dy);
	}

	/**
	 * Gradient.
	 *
	 * @param image the image
	 * @param x the x
	 * @param y the y
	 * @return the vector2f
	 */
	public static Vector2f gradient(float[][] image, float x, float y) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = interpolate(x + 1, y, image, rows, cols);
		double v12 = interpolate(x, y + 1, image, rows, cols);
		double v10 = interpolate(x, y - 1, image, rows, cols);
		double v01 = interpolate(x - 1, y, image, rows, cols);
		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return new Vector2f((float) dx, (float) dy);
	}

	/**
	 * Upwind gradient.
	 *
	 * @param image the image
	 * @param x the x
	 * @param y the y
	 * @return the vector2f
	 */
	public static Vector2f upwindGradient(float[][] image, float x, float y) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = interpolate(x + 1, y, image, rows, cols);
		double v12 = interpolate(x, y + 1, image, rows, cols);
		double v10 = interpolate(x, y - 1, image, rows, cols);
		double v01 = interpolate(x - 1, y, image, rows, cols);
		double v11 = interpolate(x, y, image, rows, cols);
		double DxNeg = v11 - v01;
		double DxPos = v21 - v11;
		double DyNeg = v11 - v10;
		double DyPos = v12 - v11;
		double DxNegMin = Math.min(DxNeg, 0);
		double DxNegMax = Math.max(DxNeg, 0);
		double DxPosMin = Math.min(DxPos, 0);
		double DxPosMax = Math.max(DxPos, 0);
		double DyNegMin = Math.min(DyNeg, 0);
		double DyNegMax = Math.max(DyNeg, 0);
		double DyPosMin = Math.min(DyPos, 0);
		double DyPosMax = Math.max(DyPos, 0);
		double dx, dy;
		if (v11 < 0) {
			dx = DxNegMax + DxPosMin;
			dy = DyNegMax + DyPosMin;
		} else {
			dx = DxNegMin + DxPosMax;
			dy = DyNegMin + DyPosMax;
		}
		return new Vector2f((float) dx, (float) dy);
	}

	/**
	 * Interpolate.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param data
	 *            the data
	 * @param sx
	 *            the sx
	 * @param sy
	 *            the sy
	 * 
	 * @return the double
	 */
	public static final double interpolate(double x, double y, float[][] data,
			int sx, int sy) {
		int y0, x0, y1, x1;
		double dx, dy, hx, hy;
		if (x < 0 || x > (sx - 1) || y < 0 || y > (sy - 1)) {
			return getImageValue(data, (int) x, (int) y, sx, sy);
		} else {
			x1 = (int) Math.ceil(x);
			y1 = (int) Math.ceil(y);
			x0 = (int) Math.floor(x);
			y0 = (int) Math.floor(y);
			dx = x - x0;
			dy = y - y0;

			// Introduce more variables to reduce computation
			hx = 1.0f - dx;
			hy = 1.0f - dy;
			// Optimized below
			return (((data[x0][y0] * hx + data[x1][y0] * dx) * hy + (data[x0][y1]
					* hx + data[x1][y1] * dx)
					* dy));
		}
	}

	/**
	 * Gradient.
	 *
	 * @param image the image
	 * @param levelset the levelset
	 * @param positive the positive
	 * @param componentsFirst indicate components come first
	 * @return the float[][][]
	 */
	public static float[][][] gradient(float[][] image, float[][] levelset,
			boolean positive, boolean componentsFirst) {
		int rows = image.length;
		int cols = image[0].length;
		if (componentsFirst) {
			float[][][] gradient = new float[2][rows][cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {

					double v11 = image[i][j];
					double l21 = getImageValue(levelset, i + 1, j, rows, cols);
					double l12 = getImageValue(levelset, i, j + 1, rows, cols);
					double l10 = getImageValue(levelset, i, j - 1, rows, cols);
					double l01 = getImageValue(levelset, i - 1, j, rows, cols);

					double v21 = (l21 > 0 && positive || l21 < 0 && !positive) ? getImageValue(
							image, i + 1, j, rows, cols) : v11;
					double v12 = (l12 > 0 && positive || l12 < 0 && !positive) ? getImageValue(
							image, i, j + 1, rows, cols) : v11;
					double v10 = (l10 > 0 && positive || l10 < 0 && !positive) ? getImageValue(
							image, i, j - 1, rows, cols) : v11;
					double v01 = (l01 > 0 && positive || l01 < 0 && !positive) ? getImageValue(
							image, i - 1, j, rows, cols) : v11;

					gradient[0][i][j] = (float) ((v21 - v01) * 0.5);
					gradient[1][i][j] = (float) ((v12 - v10) * 0.5);
				}
			}
			return gradient;
		} else {
			float[][][] gradient = new float[rows][cols][2];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					double v11 = image[i][j];
					double l21 = getImageValue(levelset, i + 1, j, rows, cols);
					double l12 = getImageValue(levelset, i, j + 1, rows, cols);
					double l10 = getImageValue(levelset, i, j - 1, rows, cols);
					double l01 = getImageValue(levelset, i - 1, j, rows, cols);

					double v21 = (l21 > 0 && positive || l21 < 0 && !positive) ? getImageValue(
							image, i + 1, j, rows, cols) : v11;
					double v12 = (l12 > 0 && positive || l12 < 0 && !positive) ? getImageValue(
							image, i, j + 1, rows, cols) : v11;
					double v10 = (l10 > 0 && positive || l10 < 0 && !positive) ? getImageValue(
							image, i, j - 1, rows, cols) : v11;
					double v01 = (l01 > 0 && positive || l01 < 0 && !positive) ? getImageValue(
							image, i - 1, j, rows, cols) : v11;
					gradient[i][j][0] = (float) ((v21 - v01) * 0.5);
					gradient[i][j][1] = (float) ((v12 - v10) * 0.5);
				}
			}
			return gradient;
		}
	}

	/**
	 * Gradient.
	 * 
	 * @param image
	 *            the image
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the double
	 */
	public static Vector2d gradient(float[][] image, int i, int j) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = getImageValue(image, i + 1, j, rows, cols);
		double v12 = getImageValue(image, i, j + 1, rows, cols);
		double v10 = getImageValue(image, i, j - 1, rows, cols);
		double v01 = getImageValue(image, i - 1, j, rows, cols);
		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return new Vector2d(dx, dy);
	}

	/**
	 * Gradient magnitude.
	 * 
	 * @param image
	 *            the image
	 * 
	 * @return the float[][]
	 */
	public static float[][] gradientMagnitude(float[][] image) {
		int rows = image.length;
		int cols = image[0].length;
		float[][] gradient = new float[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double v21 = getImageValue(image, i + 1, j, rows, cols);
				double v12 = getImageValue(image, i, j + 1, rows, cols);
				double v10 = getImageValue(image, i, j - 1, rows, cols);
				double v01 = getImageValue(image, i - 1, j, rows, cols);
				double dx = ((v21 - v01) * 0.5);
				double dy = ((v12 - v10) * 0.5);
				gradient[i][j] = (float) Math.sqrt(dx * dx + dy * dy);
			}
		}
		return gradient;
	}

	/**
	 * Gradient magnitude.
	 * 
	 * @param image
	 *            the image
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the double
	 */
	public static double gradientMagnitude(float[][] image, int i, int j) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = getImageValue(image, i + 1, j, rows, cols);
		double v12 = getImageValue(image, i, j + 1, rows, cols);
		double v10 = getImageValue(image, i, j - 1, rows, cols);
		double v01 = getImageValue(image, i - 1, j, rows, cols);
		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Gradient magnitude squared.
	 * 
	 * @param image
	 *            the image
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the double
	 */
	public static double gradientMagnitudeSquared(float[][] image, int i, int j) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = getImageValue(image, i + 1, j, rows, cols);
		double v12 = getImageValue(image, i, j + 1, rows, cols);
		double v10 = getImageValue(image, i, j - 1, rows, cols);
		double v01 = getImageValue(image, i - 1, j, rows, cols);
		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return (dx * dx + dy * dy);
	}

	/**
	 * Gets the image value.
	 * 
	 * @param image
	 *            the image
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * 
	 * @return the image value
	 */
	public static double getImageValue(float[][] image, int i, int j, int rows,
			int cols) {

		int r = Math.max(Math.min(i, rows - 1), 0);
		int c = Math.max(Math.min(j, cols - 1), 0);
		return image[r][c];
	}

	/**
	 * Curl.
	 *
	 * @param image the image
	 * @param levelset the levelset
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 * @return the vector3f
	 */
	public static Vector3f curl(float[][][][] image, float[][][] levelset,
			int i, int j, int k) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		double l211 = getImageValue(levelset, i + 1, j, k, rows, cols, slices);
		double l011 = getImageValue(levelset, i - 1, j, k, rows, cols, slices);
		double l121 = getImageValue(levelset, i, j + 1, k, rows, cols, slices);
		double l101 = getImageValue(levelset, i, j - 1, k, rows, cols, slices);
		double l112 = getImageValue(levelset, i, j, k + 1, rows, cols, slices);
		double l110 = getImageValue(levelset, i, j, k - 1, rows, cols, slices);

		double yx211 = (l211 > 0) ? getImageValue(image, i + 1, j, k, 1, rows,
				cols, slices, 3) : image[i][j][k][0];
		double yx011 = (l011 > 0) ? getImageValue(image, i - 1, j, k, 1, rows,
				cols, slices, 3) : image[i][j][k][0];

		double zx211 = (l211 > 0) ? getImageValue(image, i + 1, j, k, 2, rows,
				cols, slices, 3) : image[i][j][k][0];
		double zx011 = (l011 > 0) ? getImageValue(image, i - 1, j, k, 2, rows,
				cols, slices, 3) : image[i][j][k][0];

		double xy121 = (l121 > 0) ? getImageValue(image, i, j + 1, k, 0, rows,
				cols, slices, 3) : image[i][j][k][1];
		double xy101 = (l101 > 0) ? getImageValue(image, i, j - 1, k, 0, rows,
				cols, slices, 3) : image[i][j][k][1];

		double zy121 = (l121 > 0) ? getImageValue(image, i, j + 1, k, 2, rows,
				cols, slices, 3) : image[i][j][k][1];
		double zy101 = (l101 > 0) ? getImageValue(image, i, j - 1, k, 2, rows,
				cols, slices, 3) : image[i][j][k][1];

		double xz112 = (l112 > 0) ? getImageValue(image, i, j, k + 1, 0, rows,
				cols, slices, 3) : image[i][j][k][2];
		double xz110 = (l110 > 0) ? getImageValue(image, i, j, k - 1, 0, rows,
				cols, slices, 3) : image[i][j][k][2];

		double yz112 = (l112 > 0) ? getImageValue(image, i, j, k + 1, 1, rows,
				cols, slices, 3) : image[i][j][k][2];
		double yz110 = (l110 > 0) ? getImageValue(image, i, j, k - 1, 1, rows,
				cols, slices, 3) : image[i][j][k][2];

		double dyx = ((yx211 - yx011) * 0.5);
		double dzx = ((zx211 - zx011) * 0.5);

		double dxy = ((xy121 - xy101) * 0.5);
		double dzy = ((zy121 - zy101) * 0.5);

		double dxz = ((xz112 - xz110) * 0.5);
		double dyz = ((yz112 - yz110) * 0.5);

		return new Vector3f((float) (dzy - dyz), (float) (dxz - dzx),
				(float) (dyx - dxy));
	}

	/**
	 * Divergence.
	 *
	 * @param image the image
	 * @param levelset the levelset
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 * @return the double
	 */
	public static double divergence(float[][][][] image, float[][][] levelset,
			int i, int j, int k) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		double l211 = getImageValue(levelset, i + 1, j, k, rows, cols, slices);
		double l011 = getImageValue(levelset, i - 1, j, k, rows, cols, slices);
		double l121 = getImageValue(levelset, i, j + 1, k, rows, cols, slices);
		double l101 = getImageValue(levelset, i, j - 1, k, rows, cols, slices);
		double l112 = getImageValue(levelset, i, j, k + 1, rows, cols, slices);
		double l110 = getImageValue(levelset, i, j, k - 1, rows, cols, slices);

		double v211 = (l211 > 0) ? getImageValue(image, i + 1, j, k, 0, rows,
				cols, slices, 3) : image[i][j][k][0];
		double v011 = (l011 > 0) ? getImageValue(image, i - 1, j, k, 0, rows,
				cols, slices, 3) : image[i][j][k][0];
		double v121 = (l121 > 0) ? getImageValue(image, i, j + 1, k, 1, rows,
				cols, slices, 3) : image[i][j][k][1];
		double v101 = (l101 > 0) ? getImageValue(image, i, j - 1, k, 1, rows,
				cols, slices, 3) : image[i][j][k][1];
		double v112 = (l112 > 0) ? getImageValue(image, i, j, k + 1, 2, rows,
				cols, slices, 3) : image[i][j][k][2];
		double v110 = (l110 > 0) ? getImageValue(image, i, j, k - 1, 2, rows,
				cols, slices, 3) : image[i][j][k][2];

		double dx = ((v211 - v011) * 0.5);
		double dy = ((v121 - v101) * 0.5);
		double dz = ((v112 - v110) * 0.5);
		return dx + dy + dz;
	}

	/**
	 * Blur.
	 * 
	 * @param image
	 *            the image
	 * @param stddev
	 *            the stddev
	 * 
	 * @return the float[][]
	 */
	public static final float[][][] blur(float[][][] image, double stddev) {
		double f = 0.5 * stddev * stddev;
		double timeStep = 0.999;
		int iterations = (int) Math.ceil(f / timeStep);
		// Insure that time step will achieve exactly the target standard
		// deviation
		timeStep = f / iterations;
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		float[][][] blurImage = new float[rows][cols][slices];
		// Copy image
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					blurImage[i][j][k] = image[i][j][k];
				}
			}
		}
		// Smooth image
		for (int iter = 0; iter < iterations; iter++) {
			for (int l = 0; l < 2; l++) {
				for (int m = 0; m < 2; m++) {
					for (int n = 0; n < 2; n++) {
						for (int i = l; i < rows; i += 2) {
							for (int j = m; j < cols; j += 2) {
								for (int k = n; k < slices; k += 2) {
									double v211 = getImageValue(blurImage,
											i + 1, j, k, rows, cols, slices);
									double v121 = getImageValue(blurImage, i,
											j + 1, k, rows, cols, slices);
									double v111 = blurImage[i][j][k];
									double v101 = getImageValue(blurImage, i,
											j - 1, k, rows, cols, slices);
									double v011 = getImageValue(blurImage,
											i - 1, j, k, rows, cols, slices);
									double v110 = getImageValue(blurImage, i,
											j, k - 1, rows, cols, slices);
									double v112 = getImageValue(blurImage, i,
											j, k + 1, rows, cols, slices);
									double Dxx = v211 - v111 - v111 + v011;
									double Dyy = v121 - v111 - v111 + v101;
									double Dzz = v112 - v111 - v111 + v110;
									blurImage[i][j][k] = (float) (v111 + (1.0 / 6)
											* timeStep * (Dxx + Dyy + Dzz));
								}
							}
						}
					}
				}
			}
		}
		return blurImage;
	}

	/**
	 * Curl.
	 *
	 * @param image the image
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public static double curl(float[][][] image, int i, int j) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = getImageValue(image, i + 1, j, 1, rows, cols, 2);
		double v01 = getImageValue(image, i - 1, j, 1, rows, cols, 2);
		double v12 = getImageValue(image, i, j + 1, 0, rows, cols, 2);
		double v10 = getImageValue(image, i, j - 1, 0, rows, cols, 2);
		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return dy - dx;
	}

	/**
	 * Divergence.
	 *
	 * @param image the image
	 * @param i the i
	 * @param j the j
	 * @return the double
	 */
	public static double divergence(float[][][] image, int i, int j) {
		int rows = image.length;
		int cols = image[0].length;
		double v21 = getImageValue(image, i + 1, j, 0, rows, cols, 2);
		double v01 = getImageValue(image, i - 1, j, 0, rows, cols, 2);
		double v12 = getImageValue(image, i, j + 1, 1, rows, cols, 2);
		double v10 = getImageValue(image, i, j - 1, 1, rows, cols, 2);
		double dx = ((v21 - v01) * 0.5);
		double dy = ((v12 - v10) * 0.5);
		return dx + dy;
	}

	/**
	 * Gradient.
	 *
	 * @param image the image
	 * @param levelset the levelset
	 * @param positive the positive
	 * @param componentsFirst the components first
	 * @return the float[][][][]
	 */
	public static float[][][][] gradient(float[][][] image,
			float[][][] levelset, boolean positive, boolean componentsFirst) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		if (componentsFirst) {
			float[][][][] gradient = new float[3][rows][cols][slices];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						double v111 = image[i][j][k];
						double l211 = getImageValue(levelset, i + 1, j, k,
								rows, cols, slices);
						double l121 = getImageValue(levelset, i, j + 1, k,
								rows, cols, slices);
						double l112 = getImageValue(levelset, i, j, k + 1,
								rows, cols, slices);
						double l101 = getImageValue(levelset, i, j - 1, k,
								rows, cols, slices);
						double l011 = getImageValue(levelset, i - 1, j, k,
								rows, cols, slices);
						double l110 = getImageValue(levelset, i, j, k - 1,
								rows, cols, slices);

						double v211 = (l211 > 0 && positive || l211 < 0
								&& !positive) ? getImageValue(image, i + 1, j,
								k, rows, cols, slices) : v111;
						double v121 = (l121 > 0 && positive || l121 < 0
								&& !positive) ? getImageValue(image, i, j + 1,
								k, rows, cols, slices) : v111;
						double v101 = (l101 > 0 && positive || l101 < 0
								&& !positive) ? getImageValue(image, i, j - 1,
								k, rows, cols, slices) : v111;
						double v011 = (l011 > 0 && positive || l011 < 0
								&& !positive) ? getImageValue(image, i - 1, j,
								k, rows, cols, slices) : v111;
						double v112 = (l112 > 0 && positive || l112 < 0
								&& !positive) ? getImageValue(image, i, j,
								k + 1, rows, cols, slices) : v111;
						double v110 = (l110 > 0 && positive || l110 < 0
								&& !positive) ? getImageValue(image, i, j,
								k - 1, rows, cols, slices) : v111;
						gradient[0][i][j][k] = (float) ((v211 - v011) * 0.5);
						gradient[1][i][j][k] = (float) ((v121 - v101) * 0.5);
						gradient[2][i][j][k] = (float) ((v112 - v110) * 0.5);
					}
				}
			}
			return gradient;
		} else {
			float[][][][] gradient = new float[rows][cols][slices][3];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						double v111 = image[i][j][k];
						double l211 = getImageValue(levelset, i + 1, j, k,
								rows, cols, slices);
						double l121 = getImageValue(levelset, i, j + 1, k,
								rows, cols, slices);
						double l112 = getImageValue(levelset, i, j, k + 1,
								rows, cols, slices);
						double l101 = getImageValue(levelset, i, j - 1, k,
								rows, cols, slices);
						double l011 = getImageValue(levelset, i - 1, j, k,
								rows, cols, slices);
						double l110 = getImageValue(levelset, i, j, k - 1,
								rows, cols, slices);

						double v211 = (l211 > 0 && positive || l211 < 0
								&& !positive) ? getImageValue(image, i + 1, j,
								k, rows, cols, slices) : v111;
						double v121 = (l121 > 0 && positive || l121 < 0
								&& !positive) ? getImageValue(image, i, j + 1,
								k, rows, cols, slices) : v111;
						double v101 = (l101 > 0 && positive || l101 < 0
								&& !positive) ? getImageValue(image, i, j - 1,
								k, rows, cols, slices) : v111;
						double v011 = (l011 > 0 && positive || l011 < 0
								&& !positive) ? getImageValue(image, i - 1, j,
								k, rows, cols, slices) : v111;
						double v112 = (l112 > 0 && positive || l112 < 0
								&& !positive) ? getImageValue(image, i, j,
								k + 1, rows, cols, slices) : v111;
						double v110 = (l110 > 0 && positive || l110 < 0
								&& !positive) ? getImageValue(image, i, j,
								k - 1, rows, cols, slices) : v111;
						gradient[i][j][k][0] = (float) ((v211 - v011) * 0.5);
						gradient[i][j][k][1] = (float) ((v121 - v101) * 0.5);
						gradient[i][j][k][2] = (float) ((v112 - v110) * 0.5);
					}
				}
			}
			return gradient;
		}
	}

	/**
	 * Gradient.
	 * 
	 * @param image
	 *            the image
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the double
	 */
	public static Vector3d gradient(float[][][] image, int i, int j, int k) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		double v211 = getImageValue(image, i + 1, j, k, rows, cols, slices);
		double v121 = getImageValue(image, i, j + 1, k, rows, cols, slices);
		double v101 = getImageValue(image, i, j - 1, k, rows, cols, slices);
		double v011 = getImageValue(image, i - 1, j, k, rows, cols, slices);
		double v110 = getImageValue(image, i, j, k - 1, rows, cols, slices);
		double v112 = getImageValue(image, i, j, k + 1, rows, cols, slices);
		double dx = ((v211 - v011) * 0.5);
		double dy = ((v121 - v101) * 0.5);
		double dz = ((v112 - v110) * 0.5);
		return new Vector3d(dx, dy, dz);
	}

	/**
	 * Gradient magnitude.
	 * 
	 * @param image
	 *            the image
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the double
	 */
	public static double gradientMagnitude(float[][][] image, int i, int j,
			int k) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		double v211 = getImageValue(image, i + 1, j, k, rows, cols, slices);
		double v121 = getImageValue(image, i, j + 1, k, rows, cols, slices);
		double v101 = getImageValue(image, i, j - 1, k, rows, cols, slices);
		double v011 = getImageValue(image, i - 1, j, k, rows, cols, slices);
		double v110 = getImageValue(image, i, j, k - 1, rows, cols, slices);
		double v112 = getImageValue(image, i, j, k + 1, rows, cols, slices);
		double dx = ((v211 - v011) * 0.5);
		double dy = ((v121 - v101) * 0.5);
		double dz = ((v112 - v110) * 0.5);
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Gradient magnitude squared.
	 *
	 * @param image the image
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 * @return the double
	 */
	public static double gradientMagnitudeSquared(float[][][] image, int i,
			int j, int k) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		double v211 = getImageValue(image, i + 1, j, k, rows, cols, slices);
		double v121 = getImageValue(image, i, j + 1, k, rows, cols, slices);
		double v101 = getImageValue(image, i, j - 1, k, rows, cols, slices);
		double v011 = getImageValue(image, i - 1, j, k, rows, cols, slices);
		double v110 = getImageValue(image, i, j, k - 1, rows, cols, slices);
		double v112 = getImageValue(image, i, j, k + 1, rows, cols, slices);
		double dx = ((v211 - v011) * 0.5);
		double dy = ((v121 - v101) * 0.5);
		double dz = ((v112 - v110) * 0.5);

		return (dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Interpolate.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param data
	 *            the data
	 * @param sx
	 *            the sx
	 * @param sy
	 *            the sy
	 * 
	 * @return the double
	 */
	public static final Vector2f interpolateVector(double x, double y,
			float[][][] data, int sx, int sy) {
		int y0, x0, y1, x1;
		double dx, dy, hx, hy;
		if (x < 0 || x > (sx - 1) || y < 0 || y > (sy - 1)) {
			return new Vector2f((float) getImageValue(data, (int) x, (int) y,
					0, sx, sy, 2), (float) getImageValue(data, (int) x,
					(int) y, 1, sx, sy, 2));
		} else {
			x1 = (int) Math.ceil(x);
			y1 = (int) Math.ceil(y);
			x0 = (int) Math.floor(x);
			y0 = (int) Math.floor(y);
			dx = x - x0;
			dy = y - y0;

			// Introduce more variables to reduce computation
			hx = 1.0f - dx;
			hy = 1.0f - dy;
			// Optimized below
			return new Vector2f(
					(float) (((data[x0][y0][0] * hx + data[x1][y0][0] * dx)
							* hy + (data[x0][y1][0] * hx + data[x1][y1][0] * dx)
							* dy)),
					(float) (((data[x0][y0][1] * hx + data[x1][y0][1] * dx)
							* hy + (data[x0][y1][1] * hx + data[x1][y1][1] * dx)
							* dy)));
		}
	}

	/**
	 * Upwind gradient.
	 *
	 * @param image the image
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 * @return the vector3f
	 */
	public static Vector3f upwindGradient(float[][][] image, float i, float j,
			float k) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		double v011 = interpolate(i - 1, j, k, image, rows, cols, slices);
		double v121 = interpolate(i, j + 1, k, image, rows, cols, slices);
		double v111 = interpolate(i, j, k, image, rows, cols, slices);
		double v101 = interpolate(i, j - 1, k, image, rows, cols, slices);
		double v211 = interpolate(i + 1, j, k, image, rows, cols, slices);
		double v110 = interpolate(i, j, k - 1, image, rows, cols, slices);
		double v112 = interpolate(i, j, k + 1, image, rows, cols, slices);
		double DxNeg = v111 - v011;
		double DxPos = v211 - v111;
		double DyNeg = v111 - v101;
		double DyPos = v121 - v111;
		double DzNeg = v111 - v110;
		double DzPos = v112 - v111;
		double DxNegMin = Math.min(DxNeg, 0);
		double DxNegMax = Math.max(DxNeg, 0);
		double DxPosMin = Math.min(DxPos, 0);
		double DxPosMax = Math.max(DxPos, 0);
		double DyNegMin = Math.min(DyNeg, 0);
		double DyNegMax = Math.max(DyNeg, 0);
		double DyPosMin = Math.min(DyPos, 0);
		double DyPosMax = Math.max(DyPos, 0);
		double DzNegMin = Math.min(DzNeg, 0);
		double DzNegMax = Math.max(DzNeg, 0);
		double DzPosMin = Math.min(DzPos, 0);
		double DzPosMax = Math.max(DzPos, 0);
		double dx, dy, dz;
		if (v111 < 0) {
			dx = DxNegMax + DxPosMin;
			dy = DyNegMax + DyPosMin;
			dz = DzNegMax + DzPosMin;
		} else {
			dx = DxNegMin + DxPosMax;
			dy = DyNegMin + DyPosMax;
			dz = DzNegMin + DzPosMax;
		}
		return new Vector3f((float) dx, (float) dy, (float) dz);
	}

	/**
	 * Interpolate.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @param data
	 *            the data
	 * @param sx
	 *            the sx
	 * @param sy
	 *            the sy
	 * @param sz
	 *            the sz
	 * 
	 * @return the double
	 */
	public static final double interpolate(double x, double y, double z,
			float[][][] data, int sx, int sy, int sz) {
		int y0, x0, z0, y1, x1, z1;
		double dx, dy, dz, hx, hy, hz;
		if (x < 0 || x > (sx - 1) || y < 0 || y > (sy - 1) || z < 0
				|| z > (sz - 1)) {
			return getImageValue(data, (int) x, (int) y, (int) z, sx, sy, sz);
		} else {
			x1 = (int) Math.ceil(x);
			y1 = (int) Math.ceil(y);
			z1 = (int) Math.ceil(z);
			x0 = (int) Math.floor(x);
			y0 = (int) Math.floor(y);
			z0 = (int) Math.floor(z);
			dx = x - x0;
			dy = y - y0;
			dz = z - z0;

			// Introduce more variables to reduce computation
			hx = 1.0f - dx;
			hy = 1.0f - dy;
			hz = 1.0f - dz;
			// Optimized below
			return (((data[x0][y0][z0] * hx + data[x1][y0][z0] * dx) * hy + (data[x0][y1][z0]
					* hx + data[x1][y1][z0] * dx)
					* dy)
					* hz + ((data[x0][y0][z1] * hx + data[x1][y0][z1] * dx)
					* hy + (data[x0][y1][z1] * hx + data[x1][y1][z1] * dx) * dy)
					* dz);
		}
	}

	/**
	 * Gets the image value.
	 *
	 * @param image the image
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @return the image value
	 */
	public static double getImageValue(float[][][] image, int i, int j, int k,
			int rows, int cols, int slices) {

		int r = Math.max(Math.min(i, rows - 1), 0);
		int c = Math.max(Math.min(j, cols - 1), 0);
		int s = Math.max(Math.min(k, slices - 1), 0);
		return image[r][c][s];
	}

	/**
	 * Gradient magnitude squared.
	 * 
	 * @param image
	 *            the image
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the double
	 */
	public static double divergence(float[][][][] image, int i, int j, int k) {
		int rows = image.length;
		int cols = image[0].length;
		int slices = image[0][0].length;
		double v211 = getImageValue(image, i + 1, j, k, 0, rows, cols, slices,
				3);
		double v011 = getImageValue(image, i - 1, j, k, 0, rows, cols, slices,
				3);
		double v121 = getImageValue(image, i, j + 1, k, 1, rows, cols, slices,
				3);
		double v101 = getImageValue(image, i, j - 1, k, 1, rows, cols, slices,
				3);
		double v110 = getImageValue(image, i, j, k - 1, 2, rows, cols, slices,
				3);
		double v112 = getImageValue(image, i, j, k + 1, 2, rows, cols, slices,
				3);
		double dx = ((v211 - v011) * 0.5);
		double dy = ((v121 - v101) * 0.5);
		double dz = ((v112 - v110) * 0.5);
		return dx + dy + dz;
	}

	/**
	 * Interpolate.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param comp the comp
	 * @param data the data
	 * @param sx the sx
	 * @param sy the sy
	 * @param sz the sz
	 * @param comps the comps
	 * @return the double
	 */
	public static final double interpolate(double x, double y, double z,
			int comp, float[][][][] data, int sx, int sy, int sz, int comps) {
		int y0, x0, z0, y1, x1, z1;
		double dx, dy, dz, hx, hy, hz;
		if (x < 0 || x > (sx - 1) || y < 0 || y > (sy - 1) || z < 0
				|| z > (sz - 1)) {
			return getImageValue(data, (int) x, (int) y, (int) z, comp, sx, sy,
					sz, comps);
		} else {
			x1 = (int) Math.ceil(x);
			y1 = (int) Math.ceil(y);
			z1 = (int) Math.ceil(z);
			x0 = (int) Math.floor(x);
			y0 = (int) Math.floor(y);
			z0 = (int) Math.floor(z);
			dx = x - x0;
			dy = y - y0;
			dz = z - z0;

			// Introduce more variables to reduce computation
			hx = 1.0f - dx;
			hy = 1.0f - dy;
			hz = 1.0f - dz;
			// Optimized below
			return (((data[x0][y0][z0][comp] * hx + data[x1][y0][z0][comp] * dx)
					* hy + (data[x0][y1][z0][comp] * hx + data[x1][y1][z0][comp]
					* dx)
					* dy)
					* hz + ((data[x0][y0][z1][comp] * hx + data[x1][y0][z1][comp]
					* dx)
					* hy + (data[x0][y1][z1][comp] * hx + data[x1][y1][z1][comp]
					* dx)
					* dy)
					* dz);
		}
	}

	/**
	 * Interpolate.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param data the data
	 * @param sx the sx
	 * @param sy the sy
	 * @param sz the sz
	 * @return the double
	 */
	public static final Vector3f interpolateVector(double x, double y,
			double z, float[][][][] data, int sx, int sy, int sz) {
		double dx, dy, hx, hy, dz, hz;
		int y0, x0, z0, y1, x1, z1;
		Vector3f v = new Vector3f();
		if (x < 0 || x > (sx - 1) || y < 0 || y > (sy - 1) || z < 0
				|| z > (sz - 1)) {
			v.x = (float) getImageValue(data, (int) x, (int) y, (int) z, 0, sx,
					sy, sz, 3);
			v.y = (float) getImageValue(data, (int) x, (int) y, (int) z, 1, sx,
					sy, sz, 3);
			v.z = (float) getImageValue(data, (int) x, (int) y, (int) z, 2, sx,
					sy, sz, 3);
			return v;
		} else {
			x1 = (int) Math.ceil(x);
			y1 = (int) Math.ceil(y);
			z1 = (int) Math.ceil(z);
			x0 = (int) Math.floor(x);
			y0 = (int) Math.floor(y);
			z0 = (int) Math.floor(z);
			dx = x - x0;
			dy = y - y0;
			dz = z - z0;

			// Introduce more variables to reduce computation
			hx = 1.0f - dx;
			hy = 1.0f - dy;
			hz = 1.0f - dz;
			// Optimized below
			v.x = (float) (((data[x0][y0][z0][0] * hx + data[x1][y0][z0][0]
					* dx)
					* hy + (data[x0][y1][z0][0] * hx + data[x1][y1][z0][0] * dx)
					* dy)
					* hz + ((data[x0][y0][z1][0] * hx + data[x1][y0][z1][0]
					* dx)
					* hy + (data[x0][y1][z1][0] * hx + data[x1][y1][z1][0] * dx)
					* dy)
					* dz);
			v.y = (float) (((data[x0][y0][z0][1] * hx + data[x1][y0][z0][1]
					* dx)
					* hy + (data[x0][y1][z0][1] * hx + data[x1][y1][z0][1] * dx)
					* dy)
					* hz + ((data[x0][y0][z1][1] * hx + data[x1][y0][z1][1]
					* dx)
					* hy + (data[x0][y1][z1][1] * hx + data[x1][y1][z1][1] * dx)
					* dy)
					* dz);
			v.z = (float) (((data[x0][y0][z0][2] * hx + data[x1][y0][z0][2]
					* dx)
					* hy + (data[x0][y1][z0][2] * hx + data[x1][y1][z0][2] * dx)
					* dy)
					* hz + ((data[x0][y0][z1][2] * hx + data[x1][y0][z1][2]
					* dx)
					* hy + (data[x0][y1][z1][2] * hx + data[x1][y1][z1][2] * dx)
					* dy)
					* dz);
			return v;
		}

	}

	/**
	 * Gets the image value.
	 *
	 * @param image the image
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 * @param l the l
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param comps the comps
	 * @return the image value
	 */
	protected static double getImageValue(float[][][][] image, int i, int j,
			int k, int l, int rows, int cols, int slices, int comps) {

		int r = Math.max(Math.min(i, rows - 1), 0);
		int c = Math.max(Math.min(j, cols - 1), 0);
		int s = Math.max(Math.min(k, slices - 1), 0);
		int m = Math.max(Math.min(l, comps - 1), 0);
		return image[r][c][s][m];
	}

	/**
	 * Gaussian curvature.
	 * 
	 * @param image
	 *            the image
	 * 
	 * @return the float[][]
	 */
	public static final float[][] gaussianCurvature(float[][] image) {
		int rows = image.length;
		int cols = image[0].length;
		float[][] curvImage = new float[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				float v11 = getValue(i, j, rows, cols, image);
				float v01 = getValue(i - 1, j, rows, cols, image);
				float v21 = getValue(i + 1, j, rows, cols, image);
				float v10 = getValue(i, j - 1, rows, cols, image);
				float v12 = getValue(i, j + 1, rows, cols, image);
				float v22 = getValue(i + 1, j + 1, rows, cols, image);
				float v02 = getValue(i - 1, j + 1, rows, cols, image);
				float v00 = getValue(i - 1, j - 1, rows, cols, image);
				float v20 = getValue(i + 1, j - 1, rows, cols, image);
				double DxCtr = 0.5 * (v21 - v01);
				double DyCtr = 0.5 * (v12 - v10);
				double DxxCtr = v21 - v11 - v11 + v01;
				double DyyCtr = v12 - v11 - v11 + v10;
				double DxyCtr = (v22 - v02 - v20 + v00) * 0.25;
				double numer = DxxCtr * DyyCtr - DxyCtr * DxyCtr;
				double denom = DxCtr * DxCtr + DyCtr * DyCtr;
				double kappa;
				if (Math.abs(denom) > 1E-5) {
					kappa = numer / denom;
				} else {
					kappa = numer * Math.signum(denom) * 1E5;
				}
				curvImage[i][j] = (float) kappa;
			}
		}
		return curvImage;
	}

	/**
	 * Mean curvature.
	 * 
	 * @param image
	 *            the image
	 * 
	 * @return the float[][]
	 */
	public static final float[][] meanCurvature(float[][] image) {
		int rows = image.length;
		int cols = image[0].length;
		float[][] curvImage = new float[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				float v11 = getValue(i, j, rows, cols, image);
				float v01 = getValue(i - 1, j, rows, cols, image);
				float v21 = getValue(i + 1, j, rows, cols, image);
				float v10 = getValue(i, j - 1, rows, cols, image);
				float v12 = getValue(i, j + 1, rows, cols, image);
				float v22 = getValue(i + 1, j + 1, rows, cols, image);
				float v02 = getValue(i - 1, j + 1, rows, cols, image);
				float v00 = getValue(i - 1, j - 1, rows, cols, image);
				float v20 = getValue(i + 1, j - 1, rows, cols, image);
				double DxCtr = 0.5 * (v21 - v01);
				double DyCtr = 0.5 * (v12 - v10);
				double DxxCtr = v21 - v11 - v11 + v01;
				double DyyCtr = v12 - v11 - v11 + v10;
				double DxyCtr = (v22 - v02 - v20 + v00) * 0.25;
				double numer = 0.5 * (DyCtr * DyCtr * DxxCtr - 2 * DxCtr
						* DyCtr * DxyCtr + DxCtr * DxCtr * DyyCtr);
				double denom = DxCtr * DxCtr + DyCtr * DyCtr;
				double kappa;
				if (Math.abs(denom) > 1E-5) {
					kappa = numer / denom;
				} else {
					kappa = numer * Math.signum(denom) * 1E5;
				}
				curvImage[i][j] = (float) kappa;
			}
		}
		return curvImage;
	}

	/**
	 * Gets the value.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param imgMat
	 *            the img mat
	 * 
	 * @return the value
	 */
	protected static final float getValue(int i, int j, int rows, int cols,
			float[][] imgMat) {
		int x = Math.max(Math.min(rows - 1, i), 0);
		int y = Math.max(Math.min(cols - 1, j), 0);
		return imgMat[x][y];
	}

	/**
	 * Upsample2 d.
	 *
	 * @param image the image
	 * @param samplingRate the sampling rate
	 * @return the image data float
	 */
	public static ImageDataFloat upsample2D(ImageDataFloat image,
			int samplingRate) {
		float[][] img2d = image.toArray2d();
		float[][][] img3d = image.toArray3d();
		ImageDataFloat upImage = null;
		int rows = image.getRows();
		int cols = image.getCols();
		int slices = image.getSlices();
		if (img2d != null) {
			upImage = new ImageDataFloat(rows * samplingRate, cols
					* samplingRate);
			upImage.setName(image.getName() + "_up");
			for (int i = 0; i < rows * samplingRate; i++) {
				for (int j = 0; j < cols * samplingRate; j++) {
					upImage.set(i, j, img2d[i / samplingRate][j / samplingRate]);
				}
			}
		} else if (img3d != null) {
			// Upsample vector field
			upImage = new ImageDataFloat(rows * samplingRate, cols
					* samplingRate, slices);
			upImage.setName(image.getName() + "_up");
			for (int i = 0; i < rows * samplingRate; i++) {
				for (int j = 0; j < cols * samplingRate; j++) {
					for (int k = 0; k < slices; k++) {
						upImage.set(i, j, k, img3d[i / samplingRate][j
								/ samplingRate][k]);
					}
				}
			}
		}
		return upImage;
	}

	/**
	 * Upsample2 d.
	 *
	 * @param image the image
	 * @param up_rows the up_rows
	 * @param up_cols the up_cols
	 * @return the image data float
	 */
	public static ImageDataFloat upsample2D(ImageDataFloat image, int up_rows,
			int up_cols) {
		if (image.getRows() == up_rows && image.getCols() == up_cols) {
			ImageDataFloat upImage = new ImageDataFloat(image);
			upImage.setName(image.getName() + "_up");
			return upImage;
		} else {
			float[][] img2d = image.toArray2d();
			float[][][] img3d = image.toArray3d();
			ImageDataFloat upImage = null;
			int rows = image.getRows();
			int cols = image.getCols();
			int slices = image.getSlices();
			if (img2d != null) {
				upImage = new ImageDataFloat(up_rows, up_cols);
				upImage.setName(image.getName() + "_up");
				double rowDelta = rows / (double) up_rows;
				double colDelta = cols / (double) up_cols;
				for (int i = 0; i < up_rows; i++) {
					for (int j = 0; j < up_cols; j++) {
						upImage.set(
								i,
								j,
								DataOperations.interpolate(i * rowDelta, j
										* colDelta, img2d, rows, cols));
					}
				}
			} else if (img3d != null) {
				// Upsample vector field
				upImage = new ImageDataFloat(up_rows, up_cols, slices);
				upImage.setName(image.getName() + "_up");
				double rowDelta = rows / (double) up_rows;
				double colDelta = cols / (double) up_cols;
				for (int i = 0; i < up_rows; i++) {
					for (int j = 0; j < up_cols; j++) {
						for (int c = 0; c < slices; c++) {
							upImage.set(i, j, c, DataOperations.interpolate(i
									* rowDelta, j * colDelta, c, img3d, rows,
									cols, slices));
						}
					}
				}
			}
			return upImage;
		}
	}

	/**
	 * Upsample3 d.
	 *
	 * @param image the image
	 * @param samplingRate the sampling rate
	 * @return the image data float
	 */
	public static ImageDataFloat upsample3D(ImageDataFloat image,
			int samplingRate) {
		float[][][] img2d = image.toArray3d();
		float[][][][] img3d = image.toArray4d();
		ImageDataFloat upImage = null;
		int rows = image.getRows();
		int cols = image.getCols();
		int slices = image.getSlices();
		int comps = image.getComponents();
		if (img2d != null) {
			upImage = new ImageDataFloat(rows * samplingRate, cols
					* samplingRate, slices * samplingRate);
			upImage.setName(image.getName() + "_up");
			for (int i = 0; i < rows * samplingRate; i++) {
				for (int j = 0; j < cols * samplingRate; j++) {
					for (int k = 0; k < slices * samplingRate; k++) {
						upImage.set(i, j, k, img2d[i / samplingRate][j
								/ samplingRate][k / samplingRate]);
					}
				}
			}
		} else if (img3d != null) {
			// Upsample vector field
			upImage = new ImageDataFloat(rows * samplingRate, cols
					* samplingRate, slices * samplingRate, comps);
			upImage.setName(image.getName() + "_up");
			for (int i = 0; i < rows * samplingRate; i++) {
				for (int j = 0; j < cols * samplingRate; j++) {
					for (int k = 0; k < slices * samplingRate; k++) {
						for (int c = 0; c < comps; c++) {
							upImage.set(i, j, k, img3d[i / samplingRate][j
									/ samplingRate][k / samplingRate][c]);
						}
					}
				}
			}
		}
		return upImage;
	}

	/**
	 * Upsample3 d.
	 *
	 * @param image the image
	 * @param up_rows the up_rows
	 * @param up_cols the up_cols
	 * @param up_slices the up_slices
	 * @return the image data float
	 */
	public static ImageDataFloat upsample3D(ImageDataFloat image, int up_rows,
			int up_cols, int up_slices) {
		if (image.getRows() == up_rows && image.getCols() == up_cols
				&& image.getSlices() == up_slices) {
			ImageDataFloat upImage = new ImageDataFloat(image);
			upImage.setName(image.getName() + "_up");
			return upImage;
		} else {
			float[][][] img3d = image.toArray3d();
			float[][][][] img4d = image.toArray4d();
			ImageDataFloat upImage = null;
			int rows = image.getRows();
			int cols = image.getCols();
			int slices = image.getSlices();
			int comps = image.getComponents();
			if (img3d != null) {
				upImage = new ImageDataFloat(up_rows, up_cols, up_slices);
				upImage.setName(image.getName() + "_up");
				double rowDelta = rows / (double) up_rows;
				double colDelta = cols / (double) up_cols;
				double sliceDelta = slices / (double) up_slices;
				for (int i = 0; i < up_rows; i++) {
					for (int j = 0; j < up_cols; j++) {
						for (int k = 0; k < up_slices; k++) {
							upImage.set(i, j, k, DataOperations.interpolate(i
									* rowDelta, j * colDelta, k * sliceDelta,
									img3d, rows, cols, slices));
						}
					}
				}
			} else if (img4d != null) {
				// Upsample vector field
				upImage = new ImageDataFloat(up_rows, up_cols, up_slices, comps);
				upImage.setName(image.getName() + "_up");
				double rowDelta = rows / (double) up_rows;
				double colDelta = cols / (double) up_cols;
				double sliceDelta = slices / (double) up_slices;
				for (int i = 0; i < up_rows; i++) {
					for (int j = 0; j < up_cols; j++) {
						for (int k = 0; k < up_slices; k++) {
							for (int c = 0; c < comps; c++) {
								upImage.set(i, j, k, DataOperations
										.interpolate(i * rowDelta,
												j * colDelta, k * sliceDelta,
												c, img4d, rows, cols, slices,
												comps));
							}
						}
					}
				}
			}
			return upImage;
		}
	}

}
