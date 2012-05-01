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
package edu.jhu.ece.iacl.jist.structures.image;

import java.awt.Color;

import edu.jhu.ece.iacl.jist.utility.JistLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageDataUByte.
 * 
 * @author Blake Lucas
 */
public class ImageDataUByte extends ImageData {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8205138120433256897L;

	/** The val. */
	private short val;

	/** The vol2d. */
	protected byte vol2d[][] = null;

	/** The vol3d. */
	protected byte vol3d[][][] = null;

	/** The vol4d. */
	protected byte vol4d[][][][] = null;

	/**
	 * Instantiates a new image data u byte.
	 * 
	 * @param data
	 *            the data
	 */
	public ImageDataUByte(byte[][] data) {
		this.type = VoxelType.UBYTE;
		this.rows = data.length;
		this.cols = data[0].length;
		this.slices = 0;
		this.components = 0;
		vol2d = data;
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data u byte.
	 * 
	 * @param data
	 *            the data
	 */
	public ImageDataUByte(byte[][][] data) {
		this.type = VoxelType.UBYTE;
		this.rows = data.length;
		this.cols = data[0].length;
		this.slices = data[0][0].length;
		this.components = 0;
		vol3d = data;
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data u byte.
	 * 
	 * @param data
	 *            the data
	 */
	public ImageDataUByte(byte[][][][] data) {
		this.type = VoxelType.UBYTE;
		this.rows = data.length;
		this.cols = data[0].length;
		this.slices = data[0][0].length;
		this.components = data[0][0][0].length;
		vol4d = data;
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data u byte.
	 * 
	 * @param vol
	 *            the vol
	 */
	public ImageDataUByte(ImageData vol) {
		JistLogger.logOutput(JistLogger.INFO,
				"New ImageData Copy from Existing: " + vol.getName());
		this.type = VoxelType.UBYTE;
		this.rows = vol.getRows();
		this.cols = vol.getCols();
		this.slices = vol.getSlices();
		this.components = vol.getComponents();
		this.setName(vol.getName());
		this.setHeader(vol.getHeader());
		if (vol.slices < 2) {
			vol2d = new byte[rows][cols];
			vol3d = null;
			vol4d = null;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					int a = vol.getUByte(i, j);
					a = (byte) (a > 127 ? a - 256 : a);
					vol2d[i][j] = (byte) a;
				}
			}
		} else {
			if (vol.components < 2) {
				vol2d = null;
				vol3d = new byte[rows][cols][slices];
				vol4d = null;
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						for (int k = 0; k < slices; k++) {
							int a = vol.getUByte(i, j, k);
							;
							a = (byte) (a > 127 ? a - 256 : a);
							vol3d[i][j][k] = (byte) a;
						}
					}
				}
			} else {
				vol2d = null;
				vol3d = null;
				vol4d = new byte[rows][cols][slices][components];
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						for (int k = 0; k < slices; k++) {
							for (int l = 0; l < components; l++) {
								int a = vol.getUByte(i, j, k, l);
								;
								a = (byte) (a > 127 ? a - 256 : a);
								vol4d[i][j][k][l] = (byte) a;
							}
						}
					}
				}
			}
		}
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data u byte.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 */
	public ImageDataUByte(int rows, int cols) {
		this(rows, cols, 1, 1);
	}

	/**
	 * Instantiates a new image data u byte.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 */
	public ImageDataUByte(int rows, int cols, int slices) {
		this(rows, cols, slices, 1);
	}

	/**
	 * Instantiates a new image data u byte.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @param components
	 *            the components
	 */
	public ImageDataUByte(int rows, int cols, int slices, int components) {
		this.rows = rows;
		this.cols = cols;
		this.slices = slices;
		this.components = components;
		this.type = VoxelType.UBYTE;
		if (components < 2) {
			if (slices < 2) {
				vol2d = new byte[rows][cols];
				vol3d = null;
				vol4d = null;
			} else {
				vol2d = null;
				vol3d = new byte[rows][cols][slices];
				vol4d = null;
			}
		} else {
			vol2d = null;
			vol3d = null;
			vol4d = new byte[rows][cols][slices][components];
		}
		setHeader(new ImageHeader());
	}

	//
	// public void add(ImageData m) {
	// if (m.components < 2) {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// vol3d[i][j][k] += m.getUByte(i, j, k);
	// }
	// }
	// }
	// } else {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// for (int l = 0; l < components; l++) {
	// vol4d[i][j][k][l] += m.get(i, j, k, l).shortValue();
	// }
	// }
	// }
	// }
	// }
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#clone()
	 */
	@Override
	public ImageDataUByte clone() {
		ImageDataUByte v = this.mimic();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					for (int l = 0; l < components; l++) {
						v.set(i, j, k, l, getUByte(i, j, k, l));
					}
				}
			}
		}

		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getUByte(int, int,
	 * int, int)
	 */
	@Override
	public short getUByte(int i, int j, int k, int l) {
		return getShort(i, j, k, l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getShort(int, int,
	 * int, int)
	 */
	@Override
	public short getShort(int i, int j, int k, int l) {
		short val = 0;
		if (vol2d == null && vol3d == null && vol4d == null) {
			return 0;
		}
		if (vol2d != null) {
			val = vol2d[i][j];
		}
		if (vol3d != null) {
			val = vol3d[i][j][k];
		}
		if (vol4d != null) {
			val = vol4d[i][j][k][l];
		}

		return (short) (val < 0 ? val + 256 : val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getColor(int, int,
	 * int, int)
	 */
	@Override
	public Color getColor(int i, int j, int k, int l) {
		return new Color(getInt(i, j, k, l));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getInt(int, int,
	 * int, int)
	 */
	@Override
	public int getInt(int i, int j, int k, int l) {
		int val = 0;

		if (vol2d == null && vol3d == null && vol4d == null) {
			return 0;
		}
		if (vol2d != null) {
			val = vol2d[i][j];
		}
		if (vol3d != null) {
			val = vol3d[i][j][k];
		}
		if (vol4d != null) {
			val = vol4d[i][j][k][l];
		}

		return (val < 0 ? val + 256 : val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.ImageData#set(edu.jhu.ece.iacl
	 * .jist.structures.image.Voxel)
	 */
	@Override
	public void set(Voxel a) {
		Object[] vals = toArray();
		double val = a.doubleValue();
		for (int i = 0; i < vals.length; i++) {
			vals[i] = val;
		}
	}

	/**
	 * To array.
	 * 
	 * @return the object[]
	 */
	public Object[] toArray() {
		if (vol2d != null) {
			return vol2d;
		}
		if (vol3d != null) {
			return vol3d;
		}
		if (vol4d != null) {
			return vol4d;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#dispose()
	 */
	@Override
	public void dispose() {
		vol2d = null;
		vol3d = null;
		vol4d = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#get(int, int, int,
	 * int)
	 */
	@Override
	public Number get(int i, int j, int k, int l) {
		Number val = 0;

		if (vol2d == null && vol3d == null && vol4d == null) {
			return null;
		}
		if (vol2d != null) {
			val = vol2d[i][j];
		}
		if (vol3d != null) {
			val = vol3d[i][j][k];
		}
		if (vol4d != null) {
			val = vol4d[i][j][k][l];
		}

		if (val.intValue() < 0) {
			val = (val.intValue() + 256);
			return val;
		} else {
			return val;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getBoolean(int,
	 * int, int, int)
	 */
	@Override
	public boolean getBoolean(int i, int j, int k, int l) {
		if (vol4d != null) {
			return vol4d[i][j][k][l] != 0;
		}
		if (vol3d != null) {
			return vol3d[i][j][k] != 0;
		}
		if (vol2d != null) {
			return vol2d[i][j] != 0;
		}
		return false;
	}

	//
	// public ImageDataUByte mag() {
	// ImageDataUByte M = new ImageDataUByte(rows, cols, slices);
	// double sum = 0;
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// sum = 0;
	// for (int l = 0; l < components; l++) {
	// sum += Math.pow(get(i, j, k, l).shortValue(), 2);
	// }
	// M.vol3d[i][j][k] = (byte) Math.sqrt(sum);
	// }
	// }
	// }
	// return M;
	// }

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getByte(int, int, int, int)
	 */
	@Override
	public byte getByte(int i, int j, int k, int l) {
		if (vol4d != null) {
			return vol4d[i][j][k][l];
		}
		if (vol3d != null) {
			return vol3d[i][j][k];
		}
		if (vol2d != null) {
			return vol2d[i][j];
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getDouble(int, int,
	 * int, int)
	 */
	@Override
	public double getDouble(int i, int j, int k, int l) {
		double val = 0;

		if (vol2d == null && vol3d == null && vol4d == null) {
			return Double.NaN;
		}
		if (vol2d != null) {
			val = vol2d[i][j];
		}
		if (vol3d != null) {
			val = vol3d[i][j][k];
		}
		if (vol4d != null) {
			val = vol4d[i][j][k][l];
		}

		return (val < 0 ? val + 256 : val);

	}

	// public void normalize() {
	// if (vol3d != null) {
	// // Normalize 3d volume
	// float min = 1E30f;
	// float max = -1E30f;
	// int val;
	// float scale;
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// val = getUByte(i, j, k);
	// min = Math.min(min, val);
	// max = Math.max(max, val);
	// }
	// }
	// }
	// scale = ((max - min) > 0.0f) ? (1.0f / (max - min)) : 1.0f;
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// vol3d[i][j][k] = (byte) Math.round((vol3d[i][j][k] - min) * scale);
	// }
	// }
	// }
	// } else {
	// double sum;
	// // Normalize 4d vector volume
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// sum = 0;
	// for (int l = 0; l < components; l++) {
	// sum += Math.pow(vol4d[i][j][k][l], 2);
	// }
	// sum = Math.sqrt(sum);
	// if (sum > 0) {
	// for (int l = 0; l < components; l++) {
	// vol4d[i][j][k][l] /= sum;
	// }
	// }
	// }
	// }
	// }
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getFloat(int, int,
	 * int, int)
	 */
	@Override
	public float getFloat(int i, int j, int k, int l) {
		float val = 0;

		if (vol2d == null && vol3d == null && vol4d == null) {
			return Float.NaN;
		}
		if (vol2d != null) {
			val = vol2d[i][j];
		}
		if (vol3d != null) {
			val = vol3d[i][j][k];
		}
		if (vol4d != null) {
			val = vol4d[i][j][k][l];
		}

		return (val < 0 ? val + 256 : val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#isNotAvailable()
	 */
	@Override
	public boolean isNotAvailable() {
		return (vol2d == null) && (vol3d == null) && (vol4d == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#mimic()
	 */
	@Override
	public ImageDataUByte mimic() {
		ImageDataUByte vol = new ImageDataUByte(rows, cols, slices, components);
		vol.setHeader(this.getHeader());
		return vol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#mimic(int, int,
	 * int, int)
	 */
	@Override
	public ImageDataUByte mimic(int rows, int cols, int slices, int components) {
		ImageDataUByte vol = new ImageDataUByte(rows, cols, slices, components);
		vol.setHeader(this.getHeader());
		return vol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int,
	 * int, boolean)
	 */
	@Override
	public void set(int i, int j, int k, int l, boolean a) {
		if (vol4d != null) {
			vol4d[i][j][k][l] = a ? (byte) 1 : (byte) 0;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = a ? (byte) 1 : (byte) 0;
		}
		if (vol2d != null) {
			vol2d[i][j] = a ? (byte) 1 : (byte) 0;
		}
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
	public void set(int i, int j, int k, int l, byte a) {
		if (vol4d != null) {
			vol4d[i][j][k][l] = a;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = a;
		}
		if (vol2d != null) {
			vol2d[i][j] = a;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int,
	 * int, java.awt.Color)
	 */
	@Override
	public void set(int i, int j, int k, int l, Color a) {
		if (vol4d != null) {
			vol4d[i][j][k][l] = (byte) a.getRed();
		}
		if (vol3d != null) {
			vol3d[i][j][k] = (byte) a.getRed();
		}
		if (vol2d != null) {
			vol2d[i][j] = (byte) a.getRed();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int,
	 * int, double)
	 */
	@Override
	public void set(int i, int j, int k, int l, double a) {
		a = (a > 255 ? 255 : a < 0 ? 0 : a);
		byte ba = (byte) (a > 127 ? a - 256 : a);
		if (vol4d != null) {
			vol4d[i][j][k][l] = ba;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = ba;
		}
		if (vol2d != null) {
			vol2d[i][j] = ba;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int,
	 * int, float)
	 */
	@Override
	public void set(int i, int j, int k, int l, float a) {
		a = (a > 255 ? 255 : a < 0 ? 0 : a);
		byte ba = (byte) (a > 127 ? a - 256 : a);
		if (vol4d != null) {
			vol4d[i][j][k][l] = ba;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = ba;
		}
		if (vol2d != null) {
			vol2d[i][j] = ba;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int,
	 * int, int)
	 */
	@Override
	public void set(int i, int j, int k, int l, int a) {
		a = (a > 255 ? 255 : a < 0 ? 0 : a);
		byte ba = (byte) (a > 127 ? a - 256 : a);
		if (vol4d != null) {
			vol4d[i][j][k][l] = ba;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = ba;
		}
		if (vol2d != null) {
			vol2d[i][j] = ba;
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int, int, java.lang.Number)
	 */
	@Override
	public void set(int i, int j, int k, int l, Number av) {

		int a = av.intValue();
		a = (a > 255 ? 255 : a < 0 ? 0 : a);
		byte ba = (byte) (a > 127 ? a - 256 : a);
		if (vol4d != null) {
			vol4d[i][j][k][l] = ba;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = ba;
		}
		if (vol2d != null) {
			vol2d[i][j] = ba;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int,
	 * int, short)
	 */
	@Override
	public void set(int i, int j, int k, int l, short a) {
		a = (a > 255 ? 255 : a < 0 ? 0 : a);
		byte ba = (byte) (a > 127 ? a - 256 : a);
		if (vol4d != null) {
			vol4d[i][j][k][l] = ba;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = ba;
		}
		if (vol2d != null) {
			vol2d[i][j] = ba;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.ImageData#setName(java.lang.String
	 * )
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * To array2d.
	 * 
	 * @return the byte[][]
	 */
	public byte[][] toArray2d() {
		return vol2d;
	}

	/**
	 * To array3d.
	 * 
	 * @return the byte[][][]
	 */
	public byte[][][] toArray3d() {
		return vol3d;
	}

	/**
	 * To array4d.
	 * 
	 * @return the byte[][][][]
	 */
	public byte[][][][] toArray4d() {
		return vol4d;
	}
}
