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
package edu.jhu.ece.iacl.jist.structures.geom;

import edu.jhu.ece.iacl.jist.structures.data.Indexable;

// TODO: Auto-generated Javadoc
/**
 * The Class VertexIndexed.
 */
public class VertexIndexed implements Indexable<Double> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The chain. */
	private int chain;

	/** The i. */
	private int i;

	/** The index. */
	private int index;

	/** The value. */
	private double value;

	/**
	 * Instantiates a new vertex indexed.
	 * 
	 * @param value
	 *            the value
	 */
	public VertexIndexed(double value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Double val) {
		return (int) Math.signum(value - val);
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
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getColumn()
	 */
	@Override
	public int getColumn() {
		return 0;
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
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getRow()
	 */
	@Override
	public int getRow() {
		return i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getSlice()
	 */
	@Override
	public int getSlice() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#getValue()
	 */
	@Override
	public Double getValue() {
		return value;
	}

	/**
	 * Gets the vertex id.
	 * 
	 * @return the vertex id
	 */
	public int getVertexId() {
		return i;
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

	/**
	 * Sets the position.
	 * 
	 * @param i
	 *            the new position
	 */
	public void setPosition(int i) {
		this.i = i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#setRefPosition(int,
	 * int, int)
	 */
	@Override
	public void setRefPosition(int i, int j, int k) {
		this.i = 0;
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
		value = (Double) obj;
	}

	/**
	 * Sets the value.
	 * 
	 * @param val
	 *            the new value
	 */
	public void setValue(double val) {
		this.value = val;
	}

}
