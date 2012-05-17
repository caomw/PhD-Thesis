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
 * Color Voxel Type.
 * 
 * @author Blake Lucas
 */
public class VoxelColor extends Voxel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3030644025869797985L;

	/** The hsb. */
	float[] hsb = new float[3];

	/** The vox. */
	private Color vox;

	/**
	 * Instantiates a new voxel color.
	 * 
	 * @param c
	 *            the c
	 */
	public VoxelColor(Color c) {
		set(c);
	}

	/**
	 * Instantiates a new voxel color.
	 * 
	 * @param v
	 *            the v
	 */
	public VoxelColor(Voxel v) {
		set(v);
	}

	/**
	 * Instantiates a new voxel color.
	 */
	public VoxelColor() {
		this.vox = Color.BLACK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(java.awt.Color)
	 */
	@Override
	public void set(Color vox) {
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
		vox = v.getColor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getDouble()
	 */
	@Override
	public double getDouble() {
		convert();
		return hsb[2];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getInt()
	 */
	@Override
	public int getInt() {
		convert();
		return Math.round(255 * hsb[2]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getShort()
	 */
	@Override
	public short getShort() {
		convert();
		return (short) Math.round(255.0 * hsb[2]);
	}

	/**
	 * Convert.
	 */
	private void convert() {
		hsb = Color.RGBtoHSB(vox.getRed(), vox.getGreen(), vox.getBlue(), hsb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#add(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public VoxelColor add(Voxel v) {
		Color c2 = v.getColor();
		return new VoxelColor(new Color(Math.min(255,
				vox.getRed() + c2.getRed()), Math.min(255,
				vox.getGreen() + c2.getGreen()), Math.min(255, vox.getBlue()
				+ c2.getBlue())));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#clone()
	 */
	@Override
	public Voxel clone() {
		return new VoxelColor(vox);
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
	public Voxel div(Voxel v) {
		Color c2 = v.getColor();
		return new VoxelColor(new Color(Math.max(0,
				(255 * vox.getRed()) / c2.getRed()), Math.max(0,
				(255 * vox.getGreen()) / c2.getGreen()), Math.max(0,
				(255 * vox.getBlue()) / c2.getBlue())));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getBoolean()
	 */
	@Override
	public boolean getBoolean() {
		return !vox.equals(Color.BLACK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getColor()
	 */
	@Override
	public Color getColor() {
		return vox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getType()
	 */
	@Override
	public VoxelType getType() {
		return VoxelType.COLOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#mul(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public Voxel mul(Voxel v) {
		Color c2 = v.getColor();
		return new VoxelColor(new Color(Math.min(255,
				vox.getRed() * c2.getRed() / 255), Math.min(255, vox.getGreen()
				* c2.getGreen() / 255), Math.min(255,
				vox.getBlue() * c2.getBlue() / 255)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#neg()
	 */
	@Override
	public Voxel neg() {
		return new VoxelColor(new Color(255 - vox.getRed(),
				255 - vox.getGreen(), 255 - vox.getBlue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(boolean)
	 */
	@Override
	public void set(boolean vox) {
		this.vox = (vox) ? Color.WHITE : Color.BLACK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(double)
	 */
	@Override
	public void set(double vox) {
		this.vox = Color.getHSBColor(1.0f, 1.0f, (float) vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(int)
	 */
	@Override
	public void set(int vox) {
		this.vox = new Color(vox, vox, vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(short)
	 */
	@Override
	public void set(short vox) {
		this.vox = new Color(vox, vox, vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#sub(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public Voxel sub(Voxel v) {
		Color c2 = v.getColor();
		return new VoxelColor(new Color(
				Math.max(0, vox.getRed() - c2.getRed()), Math.max(0,
						vox.getGreen() - c2.getGreen()), Math.max(0,
						vox.getBlue() - c2.getBlue())));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#toString()
	 */
	@Override
	public String toString() {
		return ("[" + vox.getRed() + "," + vox.getGreen() + "," + vox.getBlue() + "]");
	}
}
