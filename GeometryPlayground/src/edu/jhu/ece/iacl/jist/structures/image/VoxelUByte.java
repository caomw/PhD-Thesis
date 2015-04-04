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

// TODO: Auto-generated Javadoc
/**
 * Unsigned Byte Voxel Type Values are stored as signed byte, but recalled as
 * unsigned byte.
 * 
 * @author Blake Lucas
 */
public class VoxelUByte extends Voxel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The vox. */
	private byte vox;

	/**
	 * Instantiates a new voxel u byte.
	 * 
	 * @param b
	 *            the b
	 */
	public VoxelUByte(byte b) {
		set(b);
	}

	/**
	 * Instantiates a new voxel u byte.
	 * 
	 * @param b
	 *            the b
	 */
	public VoxelUByte(short b) {
		set(b);
	}

	/**
	 * Instantiates a new voxel u byte.
	 * 
	 * @param v
	 *            the v
	 */
	public VoxelUByte(Voxel v) {
		set(v);
	}

	/**
	 * Instantiates a new voxel u byte.
	 */
	public VoxelUByte() {
		this.vox = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(byte)
	 */
	@Override
	public void set(byte vox) {
		this.vox = vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(short)
	 */
	@Override
	public void set(short vox) {
		this.vox = (byte) vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#set(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public void set(Voxel v) {
		this.vox = (byte) v.getUByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#add(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelUByte add(Voxel v) {
		return new VoxelUByte((byte) (getUByte() + v.getUByte()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#div(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelUByte div(Voxel v) {
		return new VoxelUByte((byte) (getUByte() / v.getUByte()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getDouble()
	 */
	@Override
	public double getDouble() {
		return (getUByte());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getFloat()
	 */
	@Override
	public float getFloat() {
		return (getUByte());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getInt()
	 */
	@Override
	public int getInt() {
		return getUByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getShort()
	 */
	@Override
	public short getShort() {
		return getUByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#mul(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelUByte mul(Voxel v) {
		return new VoxelUByte((byte) (getUByte() * v.getUByte()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#neg()
	 */
	@Override
	public Voxel neg() {
		return new VoxelUByte((byte) (-getUByte()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#sub(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelUByte sub(Voxel v) {
		return new VoxelUByte((byte) (getUByte() - v.getUByte()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getUByte()
	 */
	@Override
	public short getUByte() {
		short b = vox;
		if (b < 0) {
			b = (short) (256 + b);
		}
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#clone()
	 */
	@Override
	public Voxel clone() {
		return new VoxelUByte(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Voxel obj) {
		return (this.getUByte() - (obj).getUByte());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getBoolean()
	 */
	@Override
	public boolean getBoolean() {
		return (vox != 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getColor()
	 */
	@Override
	public Color getColor() {
		return new Color(vox, vox, vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getType()
	 */
	@Override
	public VoxelType getType() {
		return VoxelType.UBYTE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(boolean)
	 */
	@Override
	public void set(boolean vox) {
		this.vox = (byte) ((vox) ? 1 : 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(java.awt.Color)
	 */
	@Override
	public void set(Color vox) {
		float[] hsb = new float[3];
		hsb = Color.RGBtoHSB(vox.getRed(), vox.getGreen(), vox.getBlue(), hsb);
		this.vox = (byte) Math.round(255.0 * hsb[2]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(double)
	 */
	@Override
	public void set(double vox) {
		this.vox = (byte) Math.round(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(int)
	 */
	@Override
	public void set(int vox) {
		this.vox = (byte) vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#toString()
	 */
	@Override
	public String toString() {
		return (vox + "");
	}
}
