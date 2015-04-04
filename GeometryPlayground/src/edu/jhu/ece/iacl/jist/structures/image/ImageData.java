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
package edu.jhu.ece.iacl.jist.structures.image;

import java.awt.Color;
import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * Structure used for storing all volumetric data.
 * 
 * @author Blake Lucas
 */
public abstract class ImageData implements Cloneable, Serializable {

	/** The cols. */
	protected int cols;

	/** The components. */
	protected int components;

	/* BL: These need to be simplified and made consistent for world-space. */
	/** The image header. */
	protected ImageHeader imageHeader;

	/** The name. */
	protected String name;
	// protected CropParameters cropParams = null;

	/*
	 * public static double talley(byte[][] vol) { double count = 0; int rows =
	 * vol.length; int cols = vol[0].length; for (int i = 0; i < rows; i++) {
	 * for (int j = 0; j < cols; j++) { count += vol[i][j]; } } return count; }
	 * 
	 * public static double talley(byte[][][] vol) { double count = 0; int rows
	 * = vol.length; int cols = vol[0].length; int slices = vol[0][0].length;
	 * for (int i = 0; i < rows; i++) { for (int j = 0; j < cols; j++) { for
	 * (int k = 0; k < slices; k++) { count += vol[i][j][k]; } } } return count;
	 * }
	 * 
	 * public static double talley(byte[][][][] vol) { double count = 0; int
	 * rows = vol.length; int cols = vol[0].length; int slices =
	 * vol[0][0].length; int components = vol[0][0][0].length; for (int i = 0; i
	 * < rows; i++) { for (int j = 0; j < cols; j++) { for (int k = 0; k <
	 * slices; k++) { for (int l = 0; l < components; l++) { count +=
	 * vol[i][j][k][l]; } } } } return count; }
	 * 
	 * public static double talley(double[][] vol) { double sum = 0; int rows =
	 * vol.length; int cols = vol[0].length; for (int i = 0; i < rows; i++) {
	 * for (int j = 0; j < cols; j++) { sum += vol[i][j]; } } return sum; }
	 * 
	 * public static double talley(double[][][] vol) { double sum = 0; int rows
	 * = vol.length; int cols = vol[0].length; int slices = vol[0][0].length;
	 * for (int i = 0; i < rows; i++) { for (int j = 0; j < cols; j++) { for
	 * (int k = 0; k < slices; k++) { sum += vol[i][j][k]; } } } return sum; }
	 * 
	 * public static double talley(double[][][][] vol) { double count = 0; int
	 * rows = vol.length; int cols = vol[0].length; int slices =
	 * vol[0][0].length; int components = vol[0][0][0].length; for (int i = 0; i
	 * < rows; i++) { for (int j = 0; j < cols; j++) { for (int k = 0; k <
	 * slices; k++) { for (int l = 0; l < components; l++) { count +=
	 * vol[i][j][k][l]; } } } } return count; }
	 * 
	 * public static double talley(float[][] vol) { double count = 0; int rows =
	 * vol.length; int cols = vol[0].length; for (int i = 0; i < rows; i++) {
	 * for (int j = 0; j < cols; j++) { count += vol[i][j]; } } return count; }
	 * 
	 * public static double talley(float[][][] vol) { double count = 0; int rows
	 * = vol.length; int cols = vol[0].length; int slices = vol[0][0].length;
	 * for (int i = 0; i < rows; i++) { for (int j = 0; j < cols; j++) { for
	 * (int k = 0; k < slices; k++) { count += vol[i][j][k]; } } } return count;
	 * }
	 * 
	 * public static double talley(float[][][][] vol) { double count = 0; int
	 * rows = vol.length; int cols = vol[0].length; int slices =
	 * vol[0][0].length; int components = vol[0][0][0].length; for (int i = 0; i
	 * < rows; i++) { for (int j = 0; j < cols; j++) { for (int k = 0; k <
	 * slices; k++) { for (int l = 0; l < components; l++) { count +=
	 * vol[i][j][k][l]; } } } } return count; }
	 * 
	 * public static double talley(int[] vol) { long count = 0; double sum = 0;
	 * int rows = vol.length; for (int i = 0; i < rows; i++) { if (count % 2 ==
	 * 0) { sum += vol[i]; } count++; } return sum; }
	 * 
	 * public static long talley(int[][] vol) { long sum1 = 0, sum2 = 0; long
	 * count = 0; int rows = vol.length; int cols = vol[0].length; for (int i =
	 * 0; i < rows; i++) { for (int j = 0; j < cols; j++) { if (count % 2 == 0)
	 * { sum1 += vol[i][j]; } else { sum2 += vol[i][j]; } count++; } } return
	 * sum1 * sum2; }
	 * 
	 * public static long talley(int[][][] vol) { long sum1 = 0, sum2 = 0; long
	 * count = 0; int rows = vol.length; int cols = vol[0].length; int slices =
	 * vol[0][0].length; for (int i = 0; i < rows; i++) { for (int j = 0; j <
	 * cols; j++) { for (int k = 0; k < slices; k++) { if (count % 2 == 0) {
	 * sum1 += vol[i][j][k]; } else { sum2 += vol[i][j][k]; } count++; } } }
	 * return sum1 * sum2; }
	 * 
	 * public static double talley(int[][][][] vol) { int sum = 0; int rows =
	 * vol.length; int cols = vol[0].length; int slices = vol[0][0].length; int
	 * components = vol[0][0][0].length; for (int i = 0; i < rows; i++) { for
	 * (int j = 0; j < cols; j++) { for (int k = 0; k < slices; k++) { for (int
	 * l = 0; l < components; l++) { sum ^= vol[i][j][k][l]; } } } } return sum;
	 * }
	 */
	/** The rows. */
	protected int rows;

	/** The slices. */
	protected int slices;

	/** The type. */
	protected VoxelType type;

	/**
	 * Checks for comparable extents.
	 *
	 * @param labelVol the label vol
	 * @return true, if successful
	 */
	public boolean hasComparableExtents(ImageData labelVol) {
		if (getRows() != labelVol.getRows()) {
			return false;
		}
		if (getCols() != labelVol.getCols()) {
			return false;
		}
		if (getSlices() != labelVol.getSlices()) {
			return false;
		}
		if (getComponents() != labelVol.getComponents()) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the rows.
	 * 
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Gets the cols.
	 * 
	 * @return the cols
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * Gets the slices.
	 * 
	 * @return the slices
	 */
	public int getSlices() {
		if (slices == 0) {
			return 1;
		} else {
			return slices;
		}
	}

	/**
	 * Gets the components.
	 * 
	 * @return the components
	 */
	public int getComponents() {
		if ((components == 0) && (slices != 0)) {
			return 1;
		} else {
			return components;
		}
	}

	// public void add(ImageData m) {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// for(int l=0;l<components;l++) {
	// set(i, j, k, l,getFloat(i, j, k,l) + m.getFloat(i, j, k,l));
	//
	// }
	// }
	// }
	// }
	// }
	//
	//
	// public void add(double val) {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// for (int l = 0; l < components; l++) {
	// set(i, j, k, l, get(i, j, k, l).doubleValue() + val);
	// }
	// }
	// }
	// }
	// }

	/**
	 * Gets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the number
	 */
	public Number get(int i, int j) {
		return get(i, j, 0, 0);
	}

	/*
	 * public ImageData crop(CropParameters params) { CubicVolumeCropper cropper
	 * = new CubicVolumeCropper(); return cropper.crop(this, params); }
	 * 
	 * public ImageData crop(double threshold, int padding) { CubicVolumeCropper
	 * cropper = new CubicVolumeCropper(); return cropper.crop(this, threshold,
	 * padding); }
	 */

	/**
	 * Get voxel.
	 * 
	 * @param i
	 *            row
	 * @param j
	 *            column
	 * @param k
	 *            slice
	 * 
	 * @return Voxel
	 */
	public Number get(int i, int j, int k) {
		return get(i, j, k, 0);
	};

	/**
	 * Get vector component for volume.
	 * 
	 * @param i
	 *            row
	 * @param j
	 *            column
	 * @param k
	 *            slice
	 * @param l
	 *            component
	 * 
	 * @return the number
	 */
	public abstract Number get(int i, int j, int k, int l);;

	/**
	 * Gets the boolean.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the boolean
	 */
	public boolean getBoolean(int i, int j) {
		return getBoolean(i, j, 0, 0);
	}

	/**
	 * Gets the boolean.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the boolean
	 */
	public boolean getBoolean(int i, int j, int k) {
		return getBoolean(i, j, k, 0);
	};

	/**
	 * Gets the boolean.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the boolean
	 */
	public abstract boolean getBoolean(int i, int j, int k, int l);;

	/**
	 * Gets the u byte.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the u byte
	 */
	public byte getByte(int i, int j) {
		return getByte(i, j, 0, 0);
	}

	/**
	 * Gets the u byte.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the u byte
	 */
	public byte getByte(int i, int j, int k) {
		return getByte(i, j, k, 0);
	};

	/**
	 * Gets the u byte.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the u byte
	 */
	public abstract byte getByte(int i, int j, int k, int l);;

	/**
	 * Gets the color.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the color
	 */
	public Color getColor(int i, int j) {
		return getColor(i, j, 0, 0);
	}

	/**
	 * Gets the color.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the color
	 */
	public Color getColor(int i, int j, int k) {
		return getColor(i, j, k, 0);
	}

	/**
	 * Gets the color.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the color
	 */
	public abstract Color getColor(int i, int j, int k, int l);

	/*
	 * public CropParameters getCropParameters() { return cropParams; }
	 */

	/**
	 * Gets the double.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the double
	 */
	public double getDouble(int i, int j) {
		return getDouble(i, j, 0, 0);
	};

	/**
	 * Gets the double.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the double
	 */
	public double getDouble(int i, int j, int k) {
		return getDouble(i, j, k, 0);
	};

	/**
	 * Gets the double.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the double
	 */
	public abstract double getDouble(int i, int j, int k, int l);

	/**
	 * Gets the float.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the float
	 */
	public float getFloat(int i, int j) {
		return getFloat(i, j, 0, 0);
	};

	/**
	 * Gets the float.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the float
	 */
	public float getFloat(int i, int j, int k) {
		return getFloat(i, j, k, 0);
	};

	/**
	 * Gets the float.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the float
	 */
	public abstract float getFloat(int i, int j, int k, int l);

	/**
	 * Gets the short.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the short
	 */
	public short getShort(int i, int j) {
		return getShort(i, j, 0, 0);
	};

	/**
	 * Gets the short.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the short
	 */
	public short getShort(int i, int j, int k) {
		return getShort(i, j, k, 0);
	};

	/**
	 * Gets the short.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the short
	 */
	public abstract short getShort(int i, int j, int k, int l);

	/**
	 * Gets the u byte.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the u byte
	 */
	public short getUByte(int i, int j) {
		return getUByte(i, j, 0, 0);
	}

	/**
	 * Gets the u byte.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the u byte
	 */
	public short getUByte(int i, int j, int k) {
		return getUByte(i, j, k, 0);
	}

	/**
	 * Gets the u byte.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the u byte
	 */
	public abstract short getUByte(int i, int j, int k, int l);;

	/**
	 * Gets the u short.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the u short
	 */
	public int getUShort(int i, int j) {
		return getInt(i, j);
	};

	/**
	 * Gets the int.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * 
	 * @return the int
	 */
	public int getInt(int i, int j) {
		return getInt(i, j, 0, 0);
	}

	/**
	 * Gets the u short.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the u short
	 */
	public int getUShort(int i, int j, int k) {
		return getInt(i, j, k);
	}

	/**
	 * Gets the int.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * 
	 * @return the int
	 */
	public int getInt(int i, int j, int k) {
		return getInt(i, j, k, 0);
	}

	/**
	 * Gets the u short.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the u short
	 */
	public int getUShort(int i, int j, int k, int l) {
		return getInt(i, j, k, l);
	};

	/**
	 * Gets the int.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * 
	 * @return the int
	 */
	public abstract int getInt(int i, int j, int k, int l);;

	/**
	 * Debugging routine for printing non-zero entries in matrix.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	/*
	 * public void printNonZeroString(int max) {
	 * System.out.println(getClass().getCanonicalName()+"\t"+"{rows=" + rows +
	 * " cols=" + cols + " slices=" + slices + "}"); int count = 0; if
	 * (components < 2) { for (int i = 0; i < rows; i++) { for (int j = 0; j <
	 * cols; j++) { for (int k = 0; k < slices; k++) { if (Math.abs(get(i, j,
	 * k).doubleValue()) > 0.00001) { Number v = get(i, j, k);
	 * System.out.println(getClass().getCanonicalName()+"\t"+"(" + i + "," + j +
	 * "," + k + ") " + v); count++; if (count >= max) { return; } } } } } }
	 * else { for (int i = 0; i < rows; i++) { for (int j = 0; j < cols; j++) {
	 * for (int k = 0; k < slices; k++) { VectorX v = getVector(i, j, k); if
	 * (v.mag().doubleValue() > 0.00001) {
	 * System.out.println(getClass().getCanonicalName()+"\t"+"(" + i + "," + j +
	 * "," + k + ") " + v.toString()); count++; if (count >= max) { return; } }
	 * } } } } }
	 */

	// public void scale(double scale) {
	// if (components < 2) {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// set(i, j, k, getDouble(i, j, k) * scale);
	// }
	// }
	// }
	// } else {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// for (int l = 0; l < components; l++) {
	// set(i, j, k, l, get(i, j, k, l).doubleValue() * scale);
	// }
	// }
	// }
	// }
	// }
	// }

	public void set(int i, int j, boolean a) {
		set(i, j, 0, 0, a);
	}

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, boolean a) {
		set(i, j, k, 0, a);
	}

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param a
	 *            the a
	 */
	public abstract void set(int i, int j, int k, int l, boolean a);

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, byte a) {
		set(i, j, 0, 0, a);
	}

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, byte a) {
		set(i, j, k, 0, a);
	}

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, short a) {
		set(i, j, k, 0, a);
	}

	// public void negate() {
	// if (components < 2) {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// set(i, j, k, -getDouble(i, j, k));
	// }
	// }
	// }
	// } else {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// for (int l = 0; l < components; l++) {
	// set(i, j, k, l, -get(i, j, k, l).doubleValue());
	// }
	// }
	// }
	// }
	// }
	// }

	// public void normalize() {
	// if (components < 2) {
	// float min = 1E30f;
	// float max = -1E30f;
	// float val;
	// float scale;
	// Matrix3 M = new Matrix3(rows, cols, slices);
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// val = getFloat(i, j, k);
	// min = Math.min(min, val);
	// max = Math.max(max, val);
	// }
	// }
	// }
	// scale = ((max - min) > 0.0f) ? (1.0f / (max - min)) : 1.0f;
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// set(i, j, k, (getFloat(i, j, k) - min) * scale);
	// }
	// }
	// }
	// } else {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// double sum = 0;
	// for (int l = 0; l < components; l++) {
	// sum += Math.pow(get(i, j, k, l).doubleValue(), 2);
	// }
	// sum = Math.sqrt(sum);
	// for (int l = 0; l < components; l++) {
	// set(i, j, k, l, get(i, j, k, l).doubleValue() / sum);
	// }
	// }
	// }
	// }
	// }
	// }

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, short a) {
		set(i, j, 0, 0, a);
	};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param a
	 *            the a
	 */
	public abstract void set(int i, int j, int k, int l, short a);;

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, Color a) {
		set(i, j, 0, 0, a);
	};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, Color a) {
		set(i, j, k, 0, a);
	};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param a
	 *            the a
	 */
	public abstract void set(int i, int j, int k, int l, Color a);;

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, double a) {
		set(i, j, 0, 0, a);
	};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, double a) {
		set(i, j, k, 0, a);
	};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param a
	 *            the a
	 */
	public abstract void set(int i, int j, int k, int l, double a);;

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, float a) {
		set(i, j, 0, 0, a);
	};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, float a) {
		set(i, j, k, 0, a);
	};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param a
	 *            the a
	 */
	public abstract void set(int i, int j, int k, int l, float a);;

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int a) {
		set(i, j, 0, 0, a);
	};

	// public void set(int i, int j, int k, Number a){set(i,j,k,0,a);};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, int a) {
		set(i, j, k, 0, a);
	};

	// public void set(int i, int j, Number a) {set(i,j,0,0,a);};

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param a
	 *            the a
	 */
	public abstract void set(int i, int j, int k, int l, int a);;

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, int k, Number a) {
		set(i, j, k, 0, a);
	}

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param a
	 *            the a
	 */
	public void set(int i, int j, Number a) {
		set(i, j, 0, 0, a);
	}

	/**
	 * Sets the.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param l
	 *            the l
	 * @param a
	 *            the a
	 */
	public abstract void set(int i, int j, int k, int l, Number a);

	/*
	 * public long talley() { long sum1 = 0, sum2 = 0; long count = 0; if
	 * (components < 2) { for (int i = 0; i < rows; i++) { for (int j = 0; j <
	 * cols; j++) { for (int k = 0; k < slices; k++) { if (count % 2 == 0) {
	 * sum1 += getUByte(i, j, k); } else { sum2 += getUByte(i, j, k); } count++;
	 * } } } } System.out.format("SUM 1 %d SUM 2 %d\n", sum1, sum2); return sum1
	 * + sum2; }
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String text = getName() + " {type=" + type + " rows=" + rows + " cols="
				+ cols + " slices=" + slices + " components=" + components
				+ "}";
		/*
		 * text+="{"; for(int k=0;k<slices;k++){ text+="\n["; for(int
		 * i=0;i<rows;i++){ for(int j=0;j<cols;j++){
		 * text+=get(i,j,k).toString()+" "; } if(i!=cols-1)text+=";\n"; }
		 * text+="]\n"; } text+="}\n"; return text;
		 */
		return text;
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
	 * Clone.
	 * 
	 * @param vol
	 *            the vol
	 * 
	 * @return the int[][]
	 */
	public static int[][] clone(int[][] vol) {
		int rows = vol.length;
		int cols = vol[0].length;
		int[][] copy = new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				copy[i][j] = vol[i][j];
			}
		}
		return copy;
	}

	/*
	 * public void printNonZeroString(int max,double val){
	 * System.out.println(getClass
	 * ().getCanonicalName()+"\t"+"{rows="+rows+" cols="
	 * +cols+" slices="+slices+"}"); int count=0; for(int i=0;i<rows;i++){
	 * for(int j=0;j<cols;j++){ for(int k=0;k<slices;k++){
	 * if(get(i,j,k).getDouble()!=val&&get(i,j,k).getDouble()!=0){ Voxel
	 * v=get(i,j,k);
	 * System.out.println(getClass().getCanonicalName()+"\t"+"("+i+
	 * ","+j+","+k+") "+v); count++; if(count>=max)return; } } } } }
	 */
	/**
	 * Clone.
	 * 
	 * @param vol
	 *            the vol
	 * 
	 * @return the int[][][]
	 */
	public static int[][][] clone(int[][][] vol) {
		int rows = vol.length;
		int cols = vol[0].length;
		int slices = vol[0][0].length;
		int[][][] copy = new int[rows][cols][slices];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					copy[i][j][k] = vol[i][j][k];
				}
			}
		}
		return copy;
	}

	/**
	 * Clone.
	 * 
	 * @param vol
	 *            the vol
	 * 
	 * @return the int[][][][]
	 */
	public static int[][][][] clone(int[][][][] vol) {
		int rows = vol.length;
		int cols = vol[0].length;
		int slices = vol[0][0].length;
		int components = vol[0][0][0].length;
		int[][][][] copy = new int[rows][cols][slices][components];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					for (int l = 0; l < components; l++) {
						copy[i][j][k][l] = vol[i][j][k][l];
					}
				}
			}
		}
		return copy;
	};

	// public void set(int i, int j, Number a) {set(i,j,0,0,a);};

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract ImageData clone();;

	// public abstract void set(int i, int j, int k, int l, Number x);

	/**
	 * Free any resources associated with an ImageData object. This is NOT
	 * necessary. It might encourage the garbage collector to collect resources
	 * more efficiently.
	 */
	abstract public void dispose();

	/*
	 * public void setCropParameters(CropParameters cropParams) {
	 * this.cropParams = cropParams; }
	 */

	/**
	 * Gets the header.
	 * 
	 * @return the header
	 */
	public ImageHeader getHeader() {
		return imageHeader;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public VoxelType getType() {
		return type;
	}

	/**
	 * Checks if is not available.
	 *
	 * @return true, if is not available
	 */
	public boolean isNotAvailable() {
		return false;
	}

	/*
	 * public ImageData mag() { ImageData M = new ImageDataMipav(this.getName()
	 * + "_mag", VoxelType.FLOAT, rows, cols, slices); for (int i = 0; i < rows;
	 * i++) { for (int j = 0; j < cols; j++) { for (int k = 0; k < slices; k++)
	 * { double sum = 0; for (int l = 0; l < components; l++) { sum +=
	 * Math.pow(get(i, j, k, l).doubleValue(), 2); } M.set(i, j, k,
	 * Math.sqrt(sum)); } } } return M; }
	 */
	/**
	 * Create new cubic volume of the same subclass type with the same
	 * dimensions Does not copy the values in the volume.
	 * 
	 * @return the image data
	 */
	public abstract ImageData mimic();

	// public ImageData uncrop(CropParameters params) {
	// CubicVolumeCropper cropper = new CubicVolumeCropper();
	// return cropper.uncrop(this, params);
	// }

	/**
	 * Mimic.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @param components
	 *            the components
	 * 
	 * @return the image data
	 */
	public abstract ImageData mimic(int rows, int cols, int slices,
			int components);;

	/**
	 * Sets the.
	 * 
	 * @param a
	 *            the a
	 */
	public abstract void set(Voxel a);;

	/**
	 * Sets the header.
	 * 
	 * @param header
	 *            the new header
	 */
	public void setHeader(ImageHeader header) {
		this.imageHeader = header.clone();
	}

	/*
	 * public void sub(double val) { if (components < 2) { for (int i = 0; i <
	 * rows; i++) { for (int j = 0; j < cols; j++) { for (int k = 0; k < slices;
	 * k++) { set(i, j, k, getDouble(i, j, k) - val); } } } } else { for (int i
	 * = 0; i < rows; i++) { for (int j = 0; j < cols; j++) { for (int k = 0; k
	 * < slices; k++) { for (int l = 0; l < components; l++) { set(i, j, k, l,
	 * get(i, j, k, l).doubleValue() - val); } } } } } }
	 */

	/**
	 * Volume name used my MIPAV images when saving.
	 * 
	 * @param name
	 *            the name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
