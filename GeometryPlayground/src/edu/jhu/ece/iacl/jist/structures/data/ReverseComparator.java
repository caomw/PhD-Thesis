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

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * Reverse comparator used for Binary Max Heap.
 * 
 * @param <T>
 *            *
 * @author Blake Lucas
 */
public class ReverseComparator<T extends Comparable<? super T>> implements
		Comparator<T> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(T obj0, T obj1) {
		return obj1.compareTo(obj0);
	}
};