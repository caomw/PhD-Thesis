/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */
package edu.jhu.ece.iacl.jist.structures.image;

import java.awt.Color;
import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * Generic Voxel Type.
 * 
 * @author Blake Lucas
 */
public abstract class Voxel extends Number implements Cloneable,
		Comparable<Voxel>, Serializable {

	/**
	 * Instantiates a new voxel.
	 * 
	 * @param v
	 *            the v
	 */
	public Voxel(Voxel v) {
		set(v);
	}

	/**
	 * Instantiates a new voxel.
	 */
	public Voxel() {
	}

	/**
	 * Sets the.
	 * 
	 * @param v
	 *            the v
	 */
	public abstract void set(Voxel v);

	/**
	 * Sets the.
	 * 
	 * @param vox
	 *            the vox
	 */
	public void set(byte vox) {
		short b = vox;
		if (b < 0) {
			set(255 + b);
		} else {
			set(b);
		}
	}

	/**
	 * Sets the.
	 * 
	 * @param vox
	 *            the vox
	 */
	public abstract void set(int vox);

	/**
	 * Sets the.
	 * 
	 * @param vox
	 *            the vox
	 */
	public abstract void set(short vox);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#byteValue()
	 */
	@Override
	public byte byteValue() {
		return (byte) getUByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#shortValue()
	 */
	@Override
	public short shortValue() {
		return getUByte();
	}

	/**
	 * Gets the u byte.
	 * 
	 * @return the u byte
	 */
	public short getUByte() {
		return getShort();
	}

	/**
	 * Gets the short.
	 * 
	 * @return the short
	 */
	public abstract short getShort();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#doubleValue()
	 */
	@Override
	public double doubleValue() {
		return getDouble();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#floatValue()
	 */
	@Override
	public float floatValue() {
		return getFloat();
	}

	/**
	 * Gets the float.
	 * 
	 * @return the float
	 */
	public float getFloat() {
		return (float) getDouble();
	}

	/**
	 * Gets the double.
	 * 
	 * @return the double
	 */
	public abstract double getDouble();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#intValue()
	 */
	@Override
	public int intValue() {
		return getInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#longValue()
	 */
	@Override
	public long longValue() {
		return getInt();
	}

	/**
	 * Gets the int.
	 * 
	 * @return the int
	 */
	public abstract int getInt();

	/**
	 * Sets the.
	 * 
	 * @param a
	 *            the a
	 */
	public void set(float a) {
		set((double) a);
	}

	/**
	 * Sets the.
	 * 
	 * @param vox
	 *            the vox
	 */
	public abstract void set(double vox);

	/**
	 * Determine restriction level for a particular Voxel Data Type The higher
	 * the value, the more restrictive.
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return the restriction
	 */
	public static int getRestriction(VoxelType type) {
		if (type == null) {
			return -1;
		}
		switch (type) {
		case BOOLEAN:
			return 1;
		case UBYTE:
			return 2;
		case SHORT:
			return 3;
		case USHORT:
			return 3;
		case INT:
			return 4;
		case COLOR:
			return 4;
		case FLOAT:
			return 5;
		case DOUBLE:
			return 6;
		case VECTORX:
			return 7;
		default:
			return 0;
		}
	}

	/**
	 * Round.
	 * 
	 * @param v
	 *            the v
	 * @param prec
	 *            the prec
	 * 
	 * @return the voxel
	 */
	public static Voxel round(Voxel v, int prec) {
		double exp = Math.pow(10, prec);
		v.set(Math.round(v.getDouble() * exp) / exp);
		return v;
	}

	/**
	 * Adds the.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel
	 */
	public abstract Voxel add(Voxel v);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract Voxel clone();

	/**
	 * Div.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel
	 */
	public abstract Voxel div(Voxel v);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Voxel) {
			return (compareTo((Voxel) o) == 0);
		} else {
			return false;
		}
	}

	/**
	 * Gets the boolean.
	 * 
	 * @return the boolean
	 */
	public abstract boolean getBoolean();

	/**
	 * Gets the color.
	 * 
	 * @return the color
	 */
	public abstract Color getColor();

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public abstract VoxelType getType();

	/**
	 * Mul.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel
	 */
	public abstract Voxel mul(Voxel v);

	/**
	 * Neg.
	 * 
	 * @return the voxel
	 */
	public abstract Voxel neg();

	/**
	 * Sets the.
	 * 
	 * @param vox
	 *            the vox
	 */
	public abstract void set(boolean vox);

	/**
	 * Sets the.
	 * 
	 * @param vox
	 *            the vox
	 */
	public abstract void set(Color vox);

	/**
	 * Sub.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel
	 */
	public abstract Voxel sub(Voxel v);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public abstract String toString();
}
