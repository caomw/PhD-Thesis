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
package edu.jhu.ece.iacl.jist.io;


import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.vecmath.Point3i;

import org.imagesci.utility.PhantomMetasphere;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataByte;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataUByte;
import edu.jhu.ece.iacl.jist.utility.JistLogger;
import edu.washington.biostr.sig.nifti.AnalyzeNiftiSpmHeader;
import edu.washington.biostr.sig.nifti.NiftiFile;

// TODO: Auto-generated Javadoc
/**
 * The Class CubicVolumeReaderWriter.
 */
public class NIFTIReaderWriter extends FileReaderWriter<ImageData> {
	
	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/** The Constant readerWriter. */
	protected static final NIFTIReaderWriter readerWriter = new NIFTIReaderWriter();

	/**
	 * Gets the single instance of CubicVolumeReaderWriter.
	 * 
	 * @return single instance of CubicVolumeReaderWriter
	 */
	public static NIFTIReaderWriter getInstance() {
		return readerWriter;
	}

	/**
	 * Instantiates a new cubic volume reader writer.
	 */
	public NIFTIReaderWriter() {
		super(new FileExtensionFilter(new String[] { "nii", "hdr", "img" }));
	}

	/**
	 * Buffer to byte matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @return the byte[][]
	 */
	public final byte[][] bufferToByteMatrix(byte[] buffer, int rows, int cols) {
		byte mat[][] = new byte[rows][cols];
		int index = 0;
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				mat[i][j] = buffer[index++];
			}
		}
		return mat;
	}

	/**
	 * Buffer to byte matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @return the byte[][][]
	 */
	public final byte[][][] bufferToByteMatrix(byte[] buffer, int rows,
			int cols, int slices) {
		byte mat[][][] = new byte[rows][cols][slices];
		int index = 0;
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					mat[i][j][k] = buffer[index++];
				}
			}
		}
		return mat;
	}

	/**
	 * Buffer to byte matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param comps the comps
	 * @return the byte[][][][]
	 */
	public final byte[][][][] bufferToByteMatrix(byte[] buffer, int rows,
			int cols, int slices, int comps) {
		byte mat[][][][] = new byte[rows][cols][slices][comps];
		int index = 0;
		for (int c = 0; c < comps; c++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						mat[i][j][k][c] = buffer[index++];
					}
				}
			}
		}
		return mat;
	}

	/**
	 * Buffer to float matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param bigEndian the big endian
	 * @return the float[][]
	 */
	public final float[][] bufferToFloatMatrix(byte[] buffer, int rows,
			int cols, boolean bigEndian) {
		float mat[][] = new float[rows][cols];
		int index = 0;
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				mat[i][j] = getBufferFloat(buffer, index, bigEndian);
				index += 4;
			}
		}
		return mat;
	}

	/**
	 * Buffer to float matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param bigEndian the big endian
	 * @return the float[][][]
	 */
	public final float[][][] bufferToFloatMatrix(byte[] buffer, int rows,
			int cols, int slices, boolean bigEndian) {
		float mat[][][] = new float[rows][cols][slices];
		int index = 0;
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					mat[i][j][k] = getBufferFloat(buffer, index, bigEndian);
					index += 4;
				}
			}
		}
		return mat;
	}

	/**
	 * Buffer to float matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param comps the comps
	 * @param bigEndian the big endian
	 * @return the float[][][][]
	 */
	public final float[][][][] bufferToFloatMatrix(byte[] buffer, int rows,
			int cols, int slices, int comps, boolean bigEndian) {
		float mat[][][][] = new float[rows][cols][slices][comps];
		int index = 0;

		for (int c = 0; c < comps; c++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						mat[i][j][k][c] = getBufferFloat(buffer, index,
								bigEndian);
						index += 4;
					}
				}
			}
		}
		return mat;
	}

	/**
	 * Buffer to int matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param bigEndian the big endian
	 * @return the int[][]
	 */
	public final int[][] bufferToIntMatrix(byte[] buffer, int rows, int cols,
			boolean bigEndian) {
		int mat[][] = new int[rows][cols];
		int index = 0;
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				mat[i][j] = getBufferInt(buffer, index, bigEndian);
				index += 4;
			}
		}

		return mat;
	}

	/**
	 * Buffer to int matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param bigEndian the big endian
	 * @return the int[][][]
	 */
	public final int[][][] bufferToIntMatrix(byte[] buffer, int rows, int cols,
			int slices, boolean bigEndian) {
		int mat[][][] = new int[rows][cols][slices];
		int index = 0;
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					mat[i][j][k] = getBufferInt(buffer, index, bigEndian);
					index += 4;
				}
			}
		}
		return mat;
	}

	/**
	 * Buffer to int matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param comps the comps
	 * @param bigEndian the big endian
	 * @return the int[][][][]
	 */
	public final int[][][][] bufferToIntMatrix(byte[] buffer, int rows,
			int cols, int slices, int comps, boolean bigEndian) {
		int mat[][][][] = new int[rows][cols][slices][comps];
		int index = 0;
		for (int c = 0; c < comps; c++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						mat[i][j][k][c] = getBufferInt(buffer, index, bigEndian);
						index += 4;
					}
				}
			}
		}
		return mat;
	}

	/**
	 * Buffer to short matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param bigEndian the big endian
	 * @return the int[][]
	 */
	public final int[][] bufferToShortMatrix(byte[] buffer, int rows, int cols,
			boolean bigEndian) {
		int mat[][] = new int[rows][cols];
		int index = 0;
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				mat[i][j] = getBufferShort(buffer, index, bigEndian);
				index += 2;
			}
		}
		return mat;
	}

	/**
	 * Buffer to short matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param bigEndian the big endian
	 * @return the int[][][]
	 */
	public final int[][][] bufferToShortMatrix(byte[] buffer, int rows,
			int cols, int slices, boolean bigEndian) {
		int mat[][][] = new int[rows][cols][slices];
		int index = 0;
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					mat[i][j][k] = getBufferShort(buffer, index, bigEndian);
					index += 2;
				}
			}
		}
		return mat;
	}

	/**
	 * Buffer to short matrix.
	 *
	 * @param buffer the buffer
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @param comps the comps
	 * @param bigEndian the big endian
	 * @return the int[][][][]
	 */
	public final int[][][][] bufferToShortMatrix(byte[] buffer, int rows,
			int cols, int slices, int comps, boolean bigEndian) {
		int mat[][][][] = new int[rows][cols][slices][comps];
		int index = 0;
		for (int c = 0; c < comps; c++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						mat[i][j][k][c] = getBufferShort(buffer, index,
								bigEndian);
						index += 2;
					}
				}
			}
		}
		return mat;
	}

	/**
	 * Gets the buffer float.
	 *
	 * @param buffer the buffer
	 * @param index the index
	 * @param bigEndian the big endian
	 * @return the buffer float
	 */
	public final float getBufferFloat(byte[] buffer, int index,
			boolean bigEndian) {
		if (bigEndian) {
			int tmpInt = (buffer[index] & 0xFF) << 24
					| (buffer[(index + 1)] & 0xFF) << 16
					| (buffer[(index + 2)] & 0xFF) << 8 | buffer[(index + 3)]
					& 0xFF;

			return Float.intBitsToFloat(tmpInt);
		}
		int tmpInt = (buffer[(index + 3)] & 0xFF) << 24
				| (buffer[(index + 2)] & 0xFF) << 16
				| (buffer[(index + 1)] & 0xFF) << 8 | buffer[index] & 0xFF;

		return Float.intBitsToFloat(tmpInt);
	}

	/**
	 * Gets the buffer int.
	 *
	 * @param buffer the buffer
	 * @param index the index
	 * @param bigEndian the big endian
	 * @return the buffer int
	 */
	public final int getBufferInt(byte[] buffer, int index, boolean bigEndian) {
		if (bigEndian) {
			return ((buffer[index] & 0xFF) << 24
					| (buffer[(index + 1)] & 0xFF) << 16
					| (buffer[(index + 2)] & 0xFF) << 8 | buffer[(index + 3)] & 0xFF);
		}

		return ((buffer[(index + 3)] & 0xFF) << 24
				| (buffer[(index + 2)] & 0xFF) << 16
				| (buffer[(index + 1)] & 0xFF) << 8 | buffer[index] & 0xFF);
	}

	/**
	 * Gets the buffer short.
	 *
	 * @param buffer the buffer
	 * @param index the index
	 * @param bigEndian the big endian
	 * @return the buffer short
	 */
	public final short getBufferShort(byte[] buffer, int index,
			boolean bigEndian) {
		if (bigEndian) {
			return (short) ((buffer[index] & 0xFF) << 8 | buffer[(index + 1)] & 0xFF);
		}
		return (short) ((buffer[(index + 1)] & 0xFF) << 8 | buffer[index] & 0xFF);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#getExtensionFilter()
	 */
	@Override
	public FileExtensionFilter getExtensionFilter() {
		return extensionFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected ImageData readObject(File f) {
		try {
			NiftiFile nimage = new NiftiFile(f);
			AnalyzeNiftiSpmHeader ahead = nimage.getHeader();
			short[] dims = ahead.getDim();
			int rows = -1, cols = -1, slices = -1, comps = -1;
			int numDims = dims[0];
			if (numDims > 0) {
				rows = dims[1];

				if (numDims > 1) {
					cols = dims[2];
					if (numDims > 2) {
						slices = dims[3];
						if (numDims > 3) {
							comps = dims[4];
						}
					}
				}
			}

			byte[] buffer = nimage.getDataBytes();
			ImageData img = null;
			boolean bigEndian = (ahead.getEndian() == ByteOrder.BIG_ENDIAN);
			if (comps != -1) {
				switch (ahead.getDatatype()) {
				case AnalyzeNiftiSpmHeader.DT_FLOAT:
					img = new ImageDataFloat(bufferToFloatMatrix(buffer, rows,
							cols, slices, comps, bigEndian));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT32:
				case AnalyzeNiftiSpmHeader.DT_UINT32:
					img = new ImageDataInt(bufferToIntMatrix(buffer, rows,
							cols, slices, comps, bigEndian));
					break;
				case AnalyzeNiftiSpmHeader.DT_UINT8:
					img = new ImageDataUByte(bufferToByteMatrix(buffer, rows,
							cols, slices, comps));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT8:
					img = new ImageDataByte(bufferToByteMatrix(buffer, rows,
							cols, slices, comps));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT16:
				case AnalyzeNiftiSpmHeader.DT_UINT16:
					img = new ImageDataInt(bufferToShortMatrix(buffer, rows,
							cols, slices, comps, bigEndian));
					break;

				}
			} else if (slices != -1) {
				switch (ahead.getDatatype()) {
				case AnalyzeNiftiSpmHeader.DT_FLOAT:
					img = new ImageDataFloat(bufferToFloatMatrix(buffer, rows,
							cols, slices, bigEndian));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT32:
				case AnalyzeNiftiSpmHeader.DT_UINT32:
					img = new ImageDataInt(bufferToIntMatrix(buffer, rows,
							cols, slices, bigEndian));
					break;
				case AnalyzeNiftiSpmHeader.DT_UINT8:
					img = new ImageDataUByte(bufferToByteMatrix(buffer, rows,
							cols, slices));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT8:
					img = new ImageDataByte(bufferToByteMatrix(buffer, rows,
							cols, slices));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT16:
				case AnalyzeNiftiSpmHeader.DT_UINT16:
					img = new ImageDataInt(bufferToShortMatrix(buffer, rows,
							cols, slices, bigEndian));
					break;

				}
			} else if (cols != -1) {
				switch (ahead.getDatatype()) {
				case AnalyzeNiftiSpmHeader.DT_FLOAT:
					img = new ImageDataFloat(bufferToFloatMatrix(buffer, rows,
							cols, bigEndian));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT32:
				case AnalyzeNiftiSpmHeader.DT_UINT32:
					img = new ImageDataInt(bufferToIntMatrix(buffer, rows,
							cols, bigEndian));
					break;
				case AnalyzeNiftiSpmHeader.DT_UINT8:
					img = new ImageDataUByte(bufferToByteMatrix(buffer, rows,
							cols));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT8:
					img = new ImageDataByte(bufferToByteMatrix(buffer, rows,
							cols));
					break;
				case AnalyzeNiftiSpmHeader.DT_INT16:
				case AnalyzeNiftiSpmHeader.DT_UINT16:
					img = new ImageDataInt(bufferToShortMatrix(buffer, rows,
							cols, bigEndian));
					break;

				}
			}
			float[] pixelRes = ahead.getPixdim();
			float[] headerRes = new float[numDims];
			for (int k = 0; k < headerRes.length; k++) {
				headerRes[k] = pixelRes[k + 1];
			}
			short[] origin = ahead.getOrigin();
			float[] forigin = new float[origin.length];
			for (int i = 0; i < forigin.length; i++)
				forigin[i] = origin[i];
			img.getHeader().setDimResolutions(pixelRes);
			img.getHeader().setOrigin(forigin);
			return img;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * To float buffer.
	 *
	 * @param img the img
	 * @param bigEndian the big endian
	 * @return the byte[]
	 */
	public final byte[] toFloatBuffer(ImageData img, boolean bigEndian) {
		int rows = img.getRows();
		int cols = img.getCols();
		int slices = Math.max(1, img.getSlices());
		int comps = Math.max(1, img.getComponents());
		byte[] data = new byte[4 * comps * rows * cols * slices];
		int index = 0;
		for (int l = 0; l < comps; l++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						setBufferFloat(data, img.getFloat(i, j, k, l), index,
								bigEndian);
						index += 4;
					}
				}
			}
		}
		return data;
	}

	/**
	 * To int buffer.
	 *
	 * @param img the img
	 * @param bigEndian the big endian
	 * @return the byte[]
	 */
	public final byte[] toIntBuffer(ImageData img, boolean bigEndian) {
		int rows = img.getRows();
		int cols = img.getCols();
		int slices = Math.max(1, img.getSlices());
		int comps = Math.max(1, img.getComponents());
		byte[] data = new byte[4 * comps * rows * cols * slices];
		int index = 0;
		for (int l = 0; l < comps; l++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						setBufferInt(data, img.getInt(i, j, k, l), index,
								bigEndian);
						index += 4;
					}
				}
			}
		}
		return data;
	}

	/**
	 * To byte buffer.
	 *
	 * @param img the img
	 * @param bigEndian the big endian
	 * @return the byte[]
	 */
	public final byte[] toByteBuffer(ImageData img, boolean bigEndian) {
		int rows = img.getRows();
		int cols = img.getCols();
		int slices = Math.max(1, img.getSlices());
		int comps = Math.max(1, img.getComponents());
		byte[] data = new byte[comps * rows * cols * slices];
		int index = 0;
		for (int l = 0; l < comps; l++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						data[index++] = img.getByte(i, j, k, l);
					}
				}
			}
		}
		return data;
	}

	/**
	 * To short buffer.
	 *
	 * @param img the img
	 * @param bigEndian the big endian
	 * @return the byte[]
	 */
	public final byte[] toShortBuffer(ImageData img, boolean bigEndian) {
		int rows = img.getRows();
		int cols = img.getCols();
		int slices = Math.max(1, img.getSlices());
		int comps = Math.max(1, img.getComponents());
		byte[] data = new byte[2 * comps * rows * cols * slices];
		int index = 0;
		for (int l = 0; l < comps; l++) {
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						setBufferShort(data, img.getShort(i, j, k, l), index,
								bigEndian);
						index += 2;
					}
				}
			}
		}
		return data;
	}

	/**
	 * Sets the buffer float.
	 *
	 * @param buffer the buffer
	 * @param data the data
	 * @param i the i
	 * @param bigEndian the big endian
	 */
	public final void setBufferFloat(byte[] buffer, float data, int i,
			boolean bigEndian) {
		int tmpInt = Float.floatToIntBits(data);
		setBufferInt(buffer, tmpInt, i, bigEndian);
	}

	/**
	 * Sets the buffer int.
	 *
	 * @param buffer the buffer
	 * @param data the data
	 * @param i the i
	 * @param bigEndian the big endian
	 */
	public final void setBufferInt(byte[] buffer, int data, int i,
			boolean bigEndian) {
		if (bigEndian) {
			buffer[i] = (byte) (data >>> 24);
			buffer[(i + 1)] = (byte) (data >>> 16);
			buffer[(i + 2)] = (byte) (data >>> 8);
			buffer[(i + 3)] = (byte) (data & 0xFF);
		} else {
			buffer[i] = (byte) (data & 0xFF);
			buffer[(i + 1)] = (byte) (data >>> 8);
			buffer[(i + 2)] = (byte) (data >>> 16);
			buffer[(i + 3)] = (byte) (data >>> 24);
		}
	}

	/**
	 * Sets the buffer long.
	 *
	 * @param buffer the buffer
	 * @param data the data
	 * @param i the i
	 * @param bigEndian the big endian
	 */
	public final void setBufferLong(byte[] buffer, long data, int i,
			boolean bigEndian) {
		if (bigEndian) {
			buffer[i] = (byte) (int) (data >>> 56);
			buffer[(i + 1)] = (byte) (int) (data >>> 48);
			buffer[(i + 2)] = (byte) (int) (data >>> 40);
			buffer[(i + 3)] = (byte) (int) (data >>> 32);
			buffer[(i + 4)] = (byte) (int) (data >>> 24);
			buffer[(i + 5)] = (byte) (int) (data >>> 16);
			buffer[(i + 6)] = (byte) (int) (data >>> 8);
			buffer[(i + 7)] = (byte) (int) (data & 0xFF);
		} else {
			buffer[i] = (byte) (int) (data & 0xFF);
			buffer[(i + 1)] = (byte) (int) (data >>> 8);
			buffer[(i + 2)] = (byte) (int) (data >>> 16);
			buffer[(i + 3)] = (byte) (int) (data >>> 24);
			buffer[(i + 4)] = (byte) (int) (data >>> 32);
			buffer[(i + 5)] = (byte) (int) (data >>> 40);
			buffer[(i + 6)] = (byte) (int) (data >>> 48);
			buffer[(i + 7)] = (byte) (int) (data >>> 56);
		}
	}

	/**
	 * Sets the buffer short.
	 *
	 * @param buffer the buffer
	 * @param data the data
	 * @param i the i
	 * @param bigEndian the big endian
	 */
	public final void setBufferShort(byte[] buffer, short data, int i,
			boolean bigEndian) {
		if (bigEndian) {
			buffer[i] = (byte) (data >>> 8);
			buffer[(i + 1)] = (byte) (data & 0xFF);
		} else {
			buffer[i] = (byte) (data & 0xFF);
			buffer[(i + 1)] = (byte) (data >>> 8);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#setExtensionFilter(edu.jhu.ece.iacl.jist.io.FileExtensionFilter)
	 */
	@Override
	public void setExtensionFilter(FileExtensionFilter extensionFilter) {
		this.extensionFilter = extensionFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObject(java.lang.Object,
	 * java.io.File)
	 */
	@Override
	protected File writeObject(ImageData img, File f) {

		AnalyzeNiftiSpmHeader newHeader = new AnalyzeNiftiSpmHeader(
				ByteOrder.BIG_ENDIAN);
		try {

			short numDims = 2;
			if (img.getSlices() > 1)
				numDims++;
			if (img.getComponents() > 1)
				numDims++;
			short[] dims = new short[numDims + 1];
			float[] pixelRes = new float[8];
			float[] headerRes = img.getHeader().getDimResolutions();
			dims[0] = numDims;
			dims[1] = (short) img.getRows();
			dims[2] = (short) img.getCols();
			pixelRes[1] = headerRes[0];
			pixelRes[2] = headerRes[1];
			if (numDims > 2) {
				dims[3] = (short) img.getSlices();
				pixelRes[3] = headerRes[2];
				if (numDims > 3) {
					dims[4] = (short) img.getComponents();
					pixelRes[4] = headerRes[3];
				}
			}
			newHeader.setDim(dims);
			newHeader.setPixdim(pixelRes);
			byte[] buffer = null;
			switch (img.getType()) {
			case DOUBLE:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_DOUBLE);
				break;
			case FLOAT:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_FLOAT);
				buffer = toFloatBuffer(img, true);
				break;
			case UBYTE:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_UINT8);
				buffer = toByteBuffer(img, true);
				break;
			case BYTE:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_INT8);
				buffer = toByteBuffer(img, true);
				break;
			case INT:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_INT32);
				buffer = toIntBuffer(img, true);
				break;
			case UINT:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_UINT32);
				buffer = toIntBuffer(img, true);
				break;
			case SHORT:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_INT16);
				buffer = toShortBuffer(img, true);
				break;
			case USHORT:
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_UINT16);
				buffer = toShortBuffer(img, true);
				break;
			case COLOR:
				// not implemented yet!
				newHeader.setDatatype(AnalyzeNiftiSpmHeader.DT_RGB);
				break;
			}
			String ext = FileReaderWriter.getFileExtension(f);

			if (buffer != null) {
				if (ext.equals("nii")) {

					DataOutputStream outputStream = new DataOutputStream(
							new FileOutputStream(f));
					newHeader.write(outputStream);
					outputStream.write(buffer);
					outputStream.close();
				} else if (ext.equals("hdr")) {
					DataOutputStream outputStream = new DataOutputStream(
							new FileOutputStream(f));
					newHeader.write(outputStream);
					outputStream.close();
					outputStream = new DataOutputStream(new FileOutputStream(
							new File(f.getParentFile(), FileReaderWriter
									.getFileName(f) + ".img")));
					outputStream.write(buffer);
					outputStream.close();
				} else if (ext.equals("img")) {
					DataOutputStream outputStream = new DataOutputStream(
							new FileOutputStream(new File(f.getParentFile(),
									FileReaderWriter.getFileName(f) + ".hdr")));
					newHeader.write(outputStream);
					outputStream.close();
					outputStream = new DataOutputStream(new FileOutputStream(f));
					outputStream.write(buffer);
					outputStream.close();
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return f;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		File outputDir = new File(args[0]);
		Point3i dims = new Point3i(128, 100, 87);
		PhantomMetasphere metasphere = new PhantomMetasphere(dims);
		metasphere.solve();

		NIFTIReaderWriter.getInstance().write(metasphere.getImage(),
				new File(outputDir, "metasphere.img"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObjectToDirectory(java
	 * .lang.Object, java.io.File)
	 */
	@Override
	protected File writeObjectToDirectory(ImageData img, File dir) {
		File f = new File(dir, img.getName() + "."
				+ extensionFilter.getPreferredExtension());
		if ((f = writeObject(img, f)) != null) {
			JistLogger
					.logOutput(JistLogger.FINE, "SUCCESS - Wrote file : " + f);
			return f;
		} else {
			JistLogger.logError(JistLogger.FINE,
					"ERROR - Failed to write file : " + f);
			return null;
		}
	}

}
