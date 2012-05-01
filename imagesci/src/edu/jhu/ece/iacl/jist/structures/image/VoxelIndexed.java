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

import edu.jhu.ece.iacl.jist.structures.data.Indexable;

// TODO: Auto-generated Javadoc
/**
 * Indexed Voxel Type used for Binary Heap This class provides an adapter to
 * existing Voxel types.
 *
 * @param <V> the value type
 * @author Blake Lucas
 */
public class VoxelIndexed<V extends Voxel> extends Voxel implements
		Indexable<Voxel> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The chain. */
	private int chain;

	/** The i. */
	private int i;

	/** The index. */
	private int index;

	/** The j. */
	private int j;

	/** The k. */
	private int k;

	/** The v. */
	Voxel v;

	/**
	 * Instantiates a new voxel indexed.
	 * 
	 * @param v
	 *            the v
	 */
	public VoxelIndexed(V v) {
		this.v = v;
		i = j = k = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.image.Voxel#add(edu.jhu.ece.iacl.jist
	 * .structures.image.Voxel)
	 */
	@Override
	public Voxel add(Voxel v) {
		return this.v.add(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#clone()
	 */
	@Override
	public Voxel clone() {
		return new VoxelIndexed<V>((V) v.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Voxel arg0) {
		return v.compareTo(arg0);
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
		return this.v.div(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getBoolean()
	 */
	@Override
	public boolean getBoolean() {
		return v.getBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getChainIndex()
	 */
	@Override
	public int getChainIndex() {
		return chain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getColor()
	 */
	@Override
	public Color getColor() {
		return v.getColor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getColumn()
	 */
	@Override
	public int getColumn() {
		return j;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getDouble()
	 */
	@Override
	public double getDouble() {
		return v.getDouble();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getFloat()
	 */
	@Override
	public float getFloat() {
		return v.getFloat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getIndex()
	 */
	@Override
	public int getIndex() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getInt()
	 */
	@Override
	public int getInt() {
		return v.getInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getRow()
	 */
	@Override
	public int getRow() {
		return i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getShort()
	 */
	@Override
	public short getShort() {
		return v.getShort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getSlice()
	 */
	@Override
	public int getSlice() {
		return k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#getType()
	 */
	@Override
	public VoxelType getType() {
		return v.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getValue()
	 */
	@Override
	public Comparable getValue() {
		return this.getDouble();
	}

	/**
	 * Gets the voxel.
	 *
	 * @return the voxel
	 */
	public V getVoxel() {
		return (V) v;
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
		return this.v.mul(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#neg()
	 */
	@Override
	public Voxel neg() {
		return this.v.neg();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(boolean)
	 */
	@Override
	public void set(boolean vox) {
		v.set(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(java.awt.Color)
	 */
	@Override
	public void set(Color vox) {
		v.set(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(double)
	 */
	@Override
	public void set(double vox) {
		v.set(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(int)
	 */
	@Override
	public void set(int vox) {
		v.set(vox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#set(short)
	 */
	@Override
	public void set(short vox) {
		v.set(vox);
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
		v.set(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#setChainIndex(int)
	 */
	@Override
	public void setChainIndex(int chainIndex) {
		chain = chainIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#setIndex(int)
	 */
	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#setRefPosition(int,
	 * int, int)
	 */
	@Override
	public void setRefPosition(int i, int j, int k) {
		this.i = i;
		this.j = j;
		this.k = k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.structures.data.Indexable#setValue(java.lang.Comparable
	 * )
	 */
	@Override
	public void setValue(Comparable obj) {
		if (obj instanceof Double) {
			set((Double) obj);
		} else if (obj instanceof Integer) {
			set((Integer) obj);
		} else if (obj instanceof Byte) {
			set((Byte) obj);
		} else if (obj instanceof Color) {
			set((Color) obj);
		} else if (obj instanceof Voxel) {
			set((Voxel) obj);
		} else if (obj instanceof Short) {
			set((Short) obj);
		}
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
		return this.v.sub(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.image.Voxel#toString()
	 */
	@Override
	public String toString() {
		return this.v.toString();
	}

}
