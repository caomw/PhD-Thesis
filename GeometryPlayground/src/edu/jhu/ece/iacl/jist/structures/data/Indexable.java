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
package edu.jhu.ece.iacl.jist.structures.data;

// TODO: Auto-generated Javadoc
/**
 * Indexable object used for heap.
 * 
 * @param <E>
 *            Must be comparable
 * 
 * @author Blake Lucas
 */
public interface Indexable<E extends Comparable<? super E>> extends
		Comparable<E> {

	/**
	 * Gets the chain index.
	 * 
	 * @return the chain index
	 */
	public int getChainIndex();

	/**
	 * Gets the column.
	 * 
	 * @return the column
	 */
	public int getColumn();

	/**
	 * Get heap index.
	 * 
	 * @return the index
	 */
	public int getIndex();

	/**
	 * Gets the row.
	 * 
	 * @return the row
	 */
	public int getRow();

	/**
	 * Gets the slice.
	 * 
	 * @return the slice
	 */
	public int getSlice();

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public Comparable getValue();

	/**
	 * Sets the chain index.
	 * 
	 * @param chainIndex
	 *            the new chain index
	 */
	public void setChainIndex(int chainIndex);

	/**
	 * Set index into heap.
	 * 
	 * @param index
	 *            the index
	 */
	public void setIndex(int index);

	/**
	 * Set position in volume.
	 * 
	 * @param i
	 *            row
	 * @param j
	 *            column
	 * @param k
	 *            slice
	 */
	public void setRefPosition(int i, int j, int k);

	/**
	 * Sets the value.
	 * 
	 * @param obj
	 *            the new value
	 */
	public void setValue(Comparable obj);
}
