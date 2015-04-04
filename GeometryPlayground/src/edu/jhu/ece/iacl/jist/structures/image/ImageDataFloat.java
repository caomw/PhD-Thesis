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

import edu.jhu.ece.iacl.jist.utility.JistLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageDataFloat.
 * 
 * @author Blake Lucas
 */
public class ImageDataFloat extends ImageData {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8205138120433256897L;

	/** The vol2d. */
	protected float vol2d[][] = null;

	/** The vol3d. */
	protected float vol3d[][][] = null;

	/** The vol4d. */
	protected float vol4d[][][][] = null;

	/**
	 * Instantiates a new image data float.
	 * 
	 * @param data
	 *            the data
	 */
	public ImageDataFloat(float[][] data) {

		this.type = VoxelType.FLOAT;
		this.rows = data.length;
		this.cols = data[0].length;
		this.slices = 1;
		this.components = 0;
		vol2d = data;
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data float.
	 * 
	 * @param data
	 *            the data
	 */
	public ImageDataFloat(float[][][] data) {

		this.type = VoxelType.FLOAT;
		this.rows = data.length;
		this.cols = data[0].length;
		this.slices = data[0][0].length;
		this.components = 0;
		vol3d = data;
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data float.
	 * 
	 * @param data
	 *            the data
	 */
	public ImageDataFloat(float[][][][] data) {
		this.type = VoxelType.FLOAT;
		this.rows = data.length;
		this.cols = data[0].length;
		this.slices = data[0][0].length;
		this.components = data[0][0][0].length;
		vol4d = data;
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data float.
	 * 
	 * @param vol
	 *            the vol
	 */
	public ImageDataFloat(ImageData vol) {
		JistLogger.logOutput(JistLogger.INFO,
				"New ImageData Copy from Existing: " + vol.getName());

		this.type = VoxelType.FLOAT;
		this.rows = vol.getRows();
		this.cols = vol.getCols();
		this.slices = vol.getSlices();
		this.components = vol.getComponents();
		this.setName(vol.getName());
		this.setHeader(vol.getHeader());
		if ((vol.slices < 2) && (vol.components < 2)) {
			vol2d = new float[rows][cols];
			vol3d = null;
			vol4d = null;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					vol2d[i][j] = vol.getFloat(i, j);
				}
			}
		} else {
			if (vol.components < 2) {
				vol2d = null;
				vol3d = new float[rows][cols][slices];
				vol4d = null;
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						for (int k = 0; k < slices; k++) {
							vol3d[i][j][k] = vol.getFloat(i, j, k);
						}
					}
				}
			} else {
				vol2d = null;
				vol3d = null;
				vol4d = new float[rows][cols][slices][components];
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						for (int k = 0; k < slices; k++) {
							for (int l = 0; l < components; l++) {
								vol4d[i][j][k][l] = vol.get(i, j, k, l)
										.floatValue();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Instantiates a new image data float.
	 *
	 * @param rows the rows
	 * @param cols the cols
	 */
	public ImageDataFloat(int rows, int cols) {
		this(rows, cols, 1, 1);
	}

	/**
	 * Instantiates a new image data float.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 */
	public ImageDataFloat(int rows, int cols, int slices) {
		this(rows, cols, slices, 1);
	}

	/**
	 * Instantiates a new image data float.
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
	public ImageDataFloat(int rows, int cols, int slices, int components) {
		this.type = VoxelType.FLOAT;
		this.rows = rows;
		this.cols = cols;
		this.slices = slices;
		this.components = components;

		if (components < 2) {
			if (slices < 2) {
				vol2d = new float[rows][cols];
				vol3d = null;
				vol4d = null;
			} else {
				vol2d = null;
				vol3d = new float[rows][cols][slices];
				vol4d = null;
			}
		} else {
			vol2d = null;
			vol3d = null;
			vol4d = new float[rows][cols][slices][components];
		}

		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data float.
	 *
	 * @param name the name
	 * @param rows the rows
	 * @param cols the cols
	 */
	public ImageDataFloat(String name, int rows, int cols) {
		this(rows, cols, 1, 1);
		setName(name);
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data float.
	 * 
	 * @param name
	 *            the name
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 */
	public ImageDataFloat(String name, int rows, int cols, int slices) {
		this(rows, cols, slices, 1);
		setName(name);
		setHeader(new ImageHeader());
	}

	/**
	 * Instantiates a new image data float.
	 * 
	 * @param name
	 *            the name
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @param components
	 *            the components
	 */
	public ImageDataFloat(String name, int rows, int cols, int slices,
			int components) {
		this(rows, cols, slices, components);
		setName(name);
		setHeader(new ImageHeader());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#clone()
	 */
	@Override
	public ImageDataFloat clone() {
		ImageDataFloat v = this.mimic();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					for (int l = 0; l < components; l++) {
						v.set(i, j, k, l, getFloat(i, j, k, l));
					}
				}
			}
		}

		return v;
	}

	// public void add(ImageData m) {
	// if (m.components < 2) {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// vol3d[i][j][k] += m.getFloat(i, j, k);
	// }
	// }
	// }
	// } else {
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// for (int l = 0; l < components; l++) {
	// vol4d[i][j][k][l] += m.get(i, j, k, l).floatValue();
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
		if (vol4d != null) {
			return vol4d[i][j][k][l];
		}
		if (vol3d != null) {
			return vol3d[i][j][k];
		}
		if (vol2d != null) {
			return vol2d[i][j];
		}
		return Float.NaN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getUByte(int, int,
	 * int, int)
	 */
	@Override
	public short getUByte(int i, int j, int k, int l) {
		short val = getShort(i, j, k, l);
		return (val < 0 ? 0 : val > 255 ? 255 : val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getShort(int, int,
	 * int, int)
	 */
	@Override
	public short getShort(int i, int j, int k, int l) {
		if (vol4d != null) {
			return (short) vol4d[i][j][k][l];
		}
		if (vol3d != null) {
			return (short) vol3d[i][j][k];
		}
		if (vol2d != null) {
			return (short) vol2d[i][j];
		}
		return 0;
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
		if (vol4d != null) {
			return vol4d[i][j][k][l];
		}
		if (vol3d != null) {
			return vol3d[i][j][k];
		}
		if (vol2d != null) {
			return vol2d[i][j];
		}
		return null;
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

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getByte(int, int, int, int)
	 */
	@Override
	public byte getByte(int i, int j, int k, int l) {
		if (vol4d != null) {
			return (byte) vol4d[i][j][k][l];
		}
		if (vol3d != null) {
			return (byte) vol3d[i][j][k];
		}
		if (vol2d != null) {
			return (byte) vol2d[i][j];
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getColor(int, int,
	 * int, int)
	 */
	@Override
	public Color getColor(int i, int j, int k, int l) {
		if (vol4d != null) {
			return new Color((int) vol4d[i][j][k][l]);
		}
		if (vol3d != null) {
			return new Color((int) vol3d[i][j][k]);
		}
		if (vol2d != null) {
			return new Color((int) vol2d[i][j]);
		}
		return null;

		// return new Color((float) vol4d[i][j][k][0], (float)
		// vol4d[i][j][k][1], (float) vol4d[i][j][k][2],
		// (float) vol4d[i][j][k][3]);

	}

	//
	// public ImageDataFloat mag() {
	// ImageDataFloat M = new ImageDataFloat(rows, cols, slices);
	// double sum = 0;
	// for (int i = 0; i < rows; i++) {
	// for (int j = 0; j < cols; j++) {
	// for (int k = 0; k < slices; k++) {
	// sum = 0;
	// for (int l = 0; l < components; l++) {
	// sum += Math.pow(get(i, j, k, l).floatValue(), 2);
	// }
	// M.vol3d[i][j][k] = (float) Math.sqrt(sum);
	// }
	// }
	// }
	// return M;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getDouble(int, int,
	 * int, int)
	 */
	@Override
	public double getDouble(int i, int j, int k, int l) {
		if (vol4d != null) {
			return vol4d[i][j][k][l];
		}
		if (vol3d != null) {
			return vol3d[i][j][k];
		}
		if (vol2d != null) {
			return vol2d[i][j];
		}
		return Double.NaN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#getInt(int, int,
	 * int, int)
	 */
	@Override
	public int getInt(int i, int j, int k, int l) {
		if (vol4d != null) {
			return (int) vol4d[i][j][k][l];
		}
		if (vol3d != null) {
			return (int) vol3d[i][j][k];
		}
		if (vol2d != null) {
			return (int) vol2d[i][j];
		}
		return 0;
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
	public ImageDataFloat mimic() {
		ImageDataFloat vol = new ImageDataFloat(rows, cols, slices, components);
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
	public ImageDataFloat mimic(int rows, int cols, int slices, int components) {
		ImageDataFloat vol = new ImageDataFloat(rows, cols, slices, components);
		vol.setHeader(this.getHeader());
		return vol;
	}

	/**
	 * Normalize.
	 */
	public void normalize() {
		if (vol3d != null) {
			// Normalize 3d volume
			float min = 1E30f;
			float max = -1E30f;
			float val;
			float scale;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						val = getFloat(i, j, k);
						min = Math.min(min, val);
						max = Math.max(max, val);
					}
				}
			}
			scale = ((max - min) > 0.0f) ? (1.0f / (max - min)) : 1.0f;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						vol3d[i][j][k] = (vol3d[i][j][k] - min) * scale;
					}
				}
			}
		} else {
			double sum;
			// Normalize 4d vector volume
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						sum = 0;
						for (int l = 0; l < components; l++) {
							sum += vol4d[i][j][k][l] * vol4d[i][j][k][l];
						}
						if (sum != 0) {
							sum = Math.sqrt(sum);
							for (int l = 0; l < components; l++) {
								vol4d[i][j][k][l] /= sum;
							}
						}
					}
				}
			}
		}
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
			vol4d[i][j][k][l] = a ? 1 : 0;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = a ? 1 : 0;
		}
		if (vol2d != null) {
			vol2d[i][j] = a ? 1 : 0;
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
			vol4d[i][j][k][l] = a.getRGB();
		}
		if (vol3d != null) {
			vol3d[i][j][k] = a.getRGB();
		}
		if (vol2d != null) {
			vol2d[i][j] = a.getRGB();
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
		if (vol4d != null) {
			vol4d[i][j][k][l] = (float) a;
		}
		if (vol3d != null) {
			vol3d[i][j][k] = (float) a;
		}
		if (vol2d != null) {
			vol2d[i][j] = (float) a;
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
	 * int, int)
	 */
	@Override
	public void set(int i, int j, int k, int l, int a) {
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

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.structures.image.ImageData#set(int, int, int, int, java.lang.Number)
	 */
	@Override
	public void set(int i, int j, int k, int l, Number a) {
		if (vol4d != null) {
			vol4d[i][j][k][l] = a.floatValue();
		}
		if (vol3d != null) {
			vol3d[i][j][k] = a.floatValue();
		}
		if (vol2d != null) {
			vol2d[i][j] = a.floatValue();
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

	/**
	 * To array2d.
	 * 
	 * @return the float[][]
	 */
	public float[][] toArray2d() {
		return vol2d;
	}

	/**
	 * To array3d.
	 * 
	 * @return the float[][][]
	 */
	public float[][][] toArray3d() {
		return vol3d;
	}

	/**
	 * To array4d.
	 * 
	 * @return the float[][][][]
	 */
	public float[][][][] toArray4d() {
		return vol4d;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			vol2d = null;
			vol3d = null;
			vol4d = null;
		} finally {
			super.finalize();
		}
	}
}
