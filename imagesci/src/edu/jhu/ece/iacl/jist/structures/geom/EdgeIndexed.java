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
package edu.jhu.ece.iacl.jist.structures.geom;

import edu.jhu.ece.iacl.jist.structures.data.Indexable;

// TODO: Auto-generated Javadoc
/**
 * The Class EdgeIndexed.
 */
public class EdgeIndexed implements Indexable<Double> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The chain. */
	private int chain;

	/** The edge id. */
	private int edgeId;

	/** The index. */
	private int index;

	/** The v1. */
	private int v1;

	/** The v2. */
	private int v2;

	/** The value. */
	private double value;

	/**
	 * Instantiates a new edge indexed.
	 * 
	 * @param value
	 *            the value
	 */
	public EdgeIndexed(double value) {
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

	/**
	 * Gets the edge index.
	 * 
	 * @return the edge index
	 */
	public int getEdgeIndex() {
		return edgeId;
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
		return edgeId;
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

	/**
	 * Gets the v1.
	 * 
	 * @return the v1
	 */
	public int getV1() {
		return v1;
	}

	/**
	 * Gets the v2.
	 * 
	 * @return the v2
	 */
	public int getV2() {
		return v2;
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
	 * @param edgeId
	 *            the edge id
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 */
	public void setPosition(int edgeId, int v1, int v2) {
		this.edgeId = edgeId;
		this.v1 = v1;
		this.v2 = v2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.structures.data.Indexable#setRefPosition(int,
	 * int, int)
	 */
	@Override
	public void setRefPosition(int i, int j, int k) {
		this.edgeId = 0;
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
