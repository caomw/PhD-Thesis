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
import java.text.NumberFormat;

// TODO: Auto-generated Javadoc
/**
 * Double Voxel Type.
 * 
 * @author Blake Lucas
 */
public class VoxelDouble extends Voxel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -425550895608205185L;

	/** The vox. */
	private double vox;

	/**
	 * Instantiates a new voxel double.
	 * 
	 * @param v
	 *            the v
	 */
	public VoxelDouble(Voxel v) {
		set(v);
	}

	/**
	 * Instantiates a new voxel double.
	 */
	public VoxelDouble() {
		this.vox = 0;
	}

	/**
	 * Instantiates a new voxel double.
	 * 
	 * @param vox
	 *            the vox
	 */
	public VoxelDouble(double vox) {
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
		this.vox = v.getDouble();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#add(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelDouble add(Voxel v) {
		return new VoxelDouble(getDouble() + v.getDouble());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#div(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public Voxel div(Voxel v) {
		return new VoxelDouble(getDouble() / v.getDouble());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#mul(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelDouble mul(Voxel v) {
		return new VoxelDouble(getDouble() * v.getDouble());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#neg()
	 */
	@Override
	public Voxel neg() {
		return new VoxelDouble(-getDouble());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#sub(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelDouble sub(Voxel v) {
		return new VoxelDouble(getDouble() - v.getDouble());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getDouble()
	 */
	@Override
	public double getDouble() {
		return vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#clone()
	 */
	@Override
	public Voxel clone() {
		return new VoxelDouble(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Voxel obj) {
		return (int) Math.signum(this.getDouble() - (obj).getDouble());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getBoolean()
	 */
	@Override
	public boolean getBoolean() {
		return (vox != 0) ? true : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getColor()
	 */
	@Override
	public Color getColor() {
		return new Color((float) vox, (float) vox, (float) vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getInt()
	 */
	@Override
	public int getInt() {
		return (int) Math.round(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getShort()
	 */
	@Override
	public short getShort() {
		return (byte) Math.round(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getType()
	 */
	@Override
	public VoxelType getType() {
		return VoxelType.DOUBLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(boolean)
	 */
	@Override
	public void set(boolean vox) {
		this.vox = (vox) ? 1 : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(java.awt.Color)
	 */
	@Override
	public void set(Color vox) {
		float[] hsb = new float[3];
		hsb = Color.RGBtoHSB(vox.getRed(), vox.getBlue(), vox.getGreen(), hsb);
		this.vox = hsb[2];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(double)
	 */
	@Override
	public void set(double vox) {
		this.vox = vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(int)
	 */
	@Override
	public void set(int vox) {
		this.vox = vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(short)
	 */
	@Override
	public void set(short vox) {
		this.vox = vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#toString()
	 */
	@Override
	public String toString() {
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(5);
		format.setMinimumFractionDigits(5);
		return (format.format(vox));
	}
}
