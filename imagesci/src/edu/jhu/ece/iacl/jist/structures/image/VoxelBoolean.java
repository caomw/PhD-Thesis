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
package edu.jhu.ece.iacl.jist.structures.image;

import java.awt.Color;

// TODO: Auto-generated Javadoc
/**
 * Boolean Voxel Type.
 * 
 * @author Blake Lucas
 */
public class VoxelBoolean extends Voxel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6843914692644177564L;

	/** The vox. */
	private boolean vox;

	/**
	 * Instantiates a new voxel boolean.
	 * 
	 * @param vox
	 *            the vox
	 */
	public VoxelBoolean(boolean vox) {
		set(vox);
	}

	/**
	 * Instantiates a new voxel boolean.
	 * 
	 * @param v
	 *            the v
	 */
	public VoxelBoolean(Voxel v) {
		set(v);
	}

	/**
	 * Instantiates a new voxel boolean.
	 */
	public VoxelBoolean() {
		this.vox = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(boolean)
	 */
	@Override
	public void set(boolean vox) {
		this.vox = vox;
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
		this.vox = v.getBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#add(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelBoolean add(Voxel v) {
		return new VoxelBoolean(v.getBoolean() ^ getBoolean());
	}

	/**
	 * And.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel boolean
	 */
	public VoxelBoolean and(Voxel v) {
		return new VoxelBoolean(v.getBoolean() & getBoolean());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#mul(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelBoolean mul(Voxel v) {
		return new VoxelBoolean(v.getBoolean() & getBoolean());
	}

	/**
	 * Nand.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel boolean
	 */
	public VoxelBoolean nand(Voxel v) {
		return new VoxelBoolean(!(v.getBoolean() & getBoolean()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#neg()
	 */
	@Override
	public VoxelBoolean neg() {
		return new VoxelBoolean(!getBoolean());
	}

	/**
	 * Nor.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel boolean
	 */
	public VoxelBoolean nor(Voxel v) {
		return new VoxelBoolean(!(v.getBoolean() | getBoolean()));
	}

	/**
	 * Or.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel boolean
	 */
	public VoxelBoolean or(Voxel v) {
		return new VoxelBoolean(v.getBoolean() | getBoolean());
	}

	/**
	 * Xor.
	 * 
	 * @param v
	 *            the v
	 * 
	 * @return the voxel boolean
	 */
	public VoxelBoolean xor(Voxel v) {
		return new VoxelBoolean(v.getBoolean() ^ getBoolean());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getBoolean()
	 */
	@Override
	public boolean getBoolean() {
		return vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#clone()
	 */
	@Override
	public Voxel clone() {
		return new VoxelBoolean(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Voxel obj) {
		return (this.getShort() - (obj).getShort());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#div(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelBoolean div(Voxel v) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getColor()
	 */
	@Override
	public Color getColor() {
		return (vox) ? Color.WHITE : Color.BLACK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getDouble()
	 */
	@Override
	public double getDouble() {
		return ((vox) ? 1.0 : 0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getInt()
	 */
	@Override
	public int getInt() {
		return (vox) ? 1 : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getShort()
	 */
	@Override
	public short getShort() {
		return (byte) ((vox) ? 1 : 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getType()
	 */
	@Override
	public VoxelType getType() {
		return VoxelType.BOOLEAN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(java.awt.Color)
	 */
	@Override
	public void set(Color vox) {
		this.vox = !vox.equals(Color.BLACK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(double)
	 */
	@Override
	public void set(double vox) {
		this.vox = (vox != 0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(int)
	 */
	@Override
	public void set(int vox) {
		this.vox = (vox != 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(short)
	 */
	@Override
	public void set(short vox) {
		this.vox = (vox != 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#sub(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelBoolean sub(Voxel v) {
		return null;
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
