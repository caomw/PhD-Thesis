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
package edu.jhu.ece.iacl.jist.structures.data;

import java.util.PriorityQueue;

// TODO: Auto-generated Javadoc
/**
 * Binary Max Heap uses Java implemention with reverse comparator.
 * 
 * @param <E>
 *            Comparable class
 * 
 * @author Blake Lucas
 */
public class BinaryMaxHeap<E extends Comparable<? super E>> extends
		PriorityQueue<E> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new binary max heap.
	 */
	public BinaryMaxHeap() {
		super(0, new ReverseComparator<E>());
	}
}
