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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

// TODO: Auto-generated Javadoc
/**
 * A data collection is a parameter that contains a collection of parameters.
 * 
 * @param <T>
 *            Data type
 * @author Blake Lucas
 */
public interface ObjectCollection<T> {
	/**
	 * Add item to collection.
	 * 
	 * @param val
	 *            the val
	 */
	public void add(Object val);

	/**
	 * Remove all values from collection.
	 */
	public void clear();

	/**
	 * Get value at specified index.
	 * 
	 * @param i
	 *            index
	 * @return value
	 */
	public T getValue(int i);

	/**
	 * Set item value in list.
	 *
	 * @param i the index
	 * @param val the val
	 */
	public void set(int i, T val);

	/**
	 * Set item value in list to the first element of the colleciton. Safely
	 * adds all remaining elements
	 *
	 * @param index the index
	 * @param src the src
	 */
	public void setCollection(int index, ObjectCollection<T> src);

	/**
	 * Returns size of collection.
	 * 
	 * @return size
	 */
	public int size();

}
