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
package edu.jhu.ece.iacl.jist.structures.data;

import java.util.Hashtable;

// TODO: Auto-generated Javadoc
/**
 * Implements a binary heap. Note that all "matching" is based on the compareTo
 * method.
 * 
 * @author Mark Allen Weiss
 */

public class BinaryMinFastHeap {

	/** The Constant DEFAULT_CAPACITY. */
	private static final int DEFAULT_CAPACITY = 100;

	/** The array. */
	private Indexable[] array; // The heap array

	/** The current size. */
	private int currentSize; // Number of elements in heap

	/** Construct the binary heap. */
	private Hashtable<Long, Integer> lookupTable;

	/** The ZN. */
	private long XN, YN, ZN;

	/**
	 * Construct the binary heap from an array.
	 * 
	 * @param items
	 *            the inital items in the binary heap.
	 */
	public BinaryMinFastHeap(Indexable[] items) {
		currentSize = items.length;
		array = new Indexable[items.length + 1];

		for (int i = 0; i < items.length; i++) {
			array[i + 1] = items[i];
		}
		buildHeap();
	}

	/**
	 * Instantiates a new binary min fast heap.
	 * 
	 * @param capacity
	 *            the capacity
	 * @param XN
	 *            the xN
	 * @param YN
	 *            the yN
	 * @param ZN
	 *            the zN
	 */
	public BinaryMinFastHeap(int capacity, int XN, int YN, int ZN) {
		currentSize = 0;
		array = new Indexable[capacity + 2];
		lookupTable = new Hashtable<Long, Integer>(XN * YN * ZN / 10);
		this.XN = XN;
		this.YN = YN;
		this.ZN = ZN;
	}

	/**
	 * Establish heap order property from an arbitrary arrangement of items.
	 * Runs in linear time.
	 */
	private void buildHeap() {
		for (int i = currentSize / 2; i > 0; i--) {
			percolateDown(i);
		}
	}

	/**
	 * Change.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param x
	 *            the x
	 */
	public void change(int i, int j, int k, Indexable x) {
		int index = lookup(i, j, k, x.getChainIndex());
		Indexable v = array[index];
		if (!x.equals(v)) {
			array[index] = x;
			if (x.compareTo(v) < 0) {
				percolateUp(index);
			} else {
				percolateDown(index);
			}
		}
	}

	/**
	 * Remove the smallest item from the priority queue.
	 *
	 * @return the smallest item.
	 */
	public Indexable remove() {
		Indexable minItem = peek();
		array[1] = array[currentSize--];
		percolateDown(1);

		return minItem;
	}

	/**
	 * Internal method to percolate down in the heap.
	 * 
	 * @param parent
	 *            the index at which the percolate begins.
	 */
	private void percolateDown(int parent) {
		int child;
		Indexable tmp = array[parent];
		if (tmp == null) {
			return;
		}
		for (; parent * 2 <= currentSize; parent = child) {
			child = parent * 2;
			if (array[child] == null) {
				parent = child;
				break;
			}
			if (array[child + 1] == null) {
				parent = child + 1;
				break;
			}
			if (child != currentSize
					&& array[child + 1].compareTo(array[child]) < 0) {
				child++;
			}
			if (array[child].compareTo(tmp) < 0) {
				array[parent] = array[child];
				updatePtr(array[parent], parent);
			} else {
				break;
			}
		}
		array[parent] = tmp;
		updatePtr(tmp, parent);
	}

	/**
	 * Insert into the priority queue. Duplicates are allowed.
	 * 
	 * @param x
	 *            the item to insert.
	 * 
	 * @return null, signifying that decreaseKey cannot be used.
	 */
	public void add(Indexable x) {
		if (currentSize + 1 == array.length) {
			resize();
		}

		// Percolate up
		int hole = ++currentSize;
		array[0] = x;

		for (; x.compareTo(array[hole / 2]) < 0; hole /= 2) {
			array[hole] = array[hole / 2];
			updatePtr(array[hole], hole);
		}
		int i = x.getRow();
		int j = x.getColumn();
		int k = x.getSlice();
		int chain = 0;
		while (lookup(i, j, k, chain) != -1) {
			chain++;
		}
		x.setChainIndex(chain);
		array[hole] = x;
		updatePtr(array[hole], hole);

	}

	/**
	 * Percolate up.
	 * 
	 * @param k
	 *            the k
	 */
	public void percolateUp(int k) {
		int k_father;
		Indexable v = array[k];
		k_father = k / 2; /* integer divsion to retrieve its parent */
		while (k_father > 0 && array[k_father].compareTo(v) > 0) {
			array[k] = array[k_father];
			updatePtr(array[k], k);
			k = k_father;
			k_father = k / 2;
		}
		array[k] = v;
		updatePtr(v, k);
	}

	/**
	 * Update ptr.
	 * 
	 * @param x
	 *            the x
	 * @param index
	 *            the index
	 */
	private void updatePtr(Indexable x, int index) {
		if (lookupTable != null) {
			lookupTable.put(
					hash(x.getRow(), x.getColumn(), x.getSlice(),
							x.getChainIndex()), index);
		}
		x.setIndex(index);
	}

	/**
	 * Lookup.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param c
	 *            the c
	 * 
	 * @return the int
	 */
	public int lookup(int i, int j, int k, int c) {
		Integer ind = lookupTable.get(hash(i, j, k, c));
		if (ind != null) {
			return ind.intValue();
		} else {
			return -1;
		}
	}

	/**
	 * Hash.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @param c
	 *            the c
	 * 
	 * @return the long
	 */
	public long hash(int i, int j, int k, int c) {
		return ((i + j * XN) + k * XN * YN) + c * XN * YN * ZN;
	}

	/**
	 * Internal method to extend array.
	 */
	private void resize() {
		Indexable[] newArray;

		newArray = new Indexable[array.length * 2];
		System.out.println(getClass().getCanonicalName() + "\t" + "RESIZE "
				+ array.length + " " + newArray.length);
		int i;
		for (i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		array = newArray;
	}

	/**
	 * Find the smallest item in the priority queue.
	 *
	 * @return the smallest item.
	 */
	public Indexable peek() {
		if (isEmpty()) {
			throw new RuntimeException("Empty binary heap");
		}
		return array[1];
	}

	/**
	 * Test if the priority queue is logically empty.
	 * 
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		return currentSize == 0;
	}

	/**
	 * Make the priority queue logically empty.
	 */
	public void makeEmpty() {
		currentSize = 0;
	}

	/**
	 * Returns size.
	 * 
	 * @return current size.
	 */
	public int size() {
		return currentSize;
	}

	// Test program

}

// PriorityQueue interface
//
// ******************PUBLIC OPERATIONS*********************
// Position insert( x ) --> Insert x
// Indexable deleteMin( )--> Return and remove smallest item
// Indexable findMin( ) --> Return smallest item
// boolean isEmpty( ) --> Return true if empty; else false
// void makeEmpty( ) --> Remove all items
// int size( ) --> Return size
// void decreaseKey( p, v)--> Decrease value in p to v
// ******************ERRORS********************************
// Throws UnderflowException for findMin and deleteMin when empty

/**
 * PriorityQueue interface. Some priority queues may support a decreaseKey
 * operation, but this is considered an advanced operation. If so, a Position is
 * returned by insert. Note that all "matching" is based on the compareTo
 * method.
 * 
 * @author Mark Allen Weiss
 */

/*
 * public BinaryMinHeap(){ super(); } public void change(AnyType src,AnyType
 * dest){ this.remove(dest); this.add(src); } /** Construct the binary heap.
 */
/*
 * private static final int DEFAULT_CAPACITY = 10; private int currentSize;
 * //private Indexable[] array; // The heap array private Vector<Indexable>
 * heap; public BinaryMinHeap() { this(DEFAULT_CAPACITY); }
 * 
 * /** Construct the binary heap.
 * 
 * @param capacity the capacity of the binary heap.
 */
/*
 * public BinaryMinHeap(int capacity) { heap=new Vector<Indexable>(capacity+1);
 * heap.setSize(capacity+1); currentSize=capacity; }
 * 
 * /** Construct the binary heap given an array of items.
 */
/*
 * public BinaryMinHeap(AnyType[] items) { this(items.length);
 * heap.setSize((currentSize + 2) * 11 / 10); int i = 1; for (AnyType item :
 * items) heap.set(i++,item); buildHeap(); }
 * 
 * /** Insert into the priority queue, maintaining heap order. Duplicates are
 * allowed.
 * 
 * @param x the item to insert.
 */
/*
 * public void offer(AnyType x) { if (currentSize == heap.size() - 1)
 * enlargeArray(heap.size() * 2 + 1);
 * 
 * // Percolate up int hole = ++currentSize; heap.set(hole,x);
 * percolateUp(hole); }
 * 
 * public void percolateUp(int hole) { Indexable x = heap.get(hole); for (; hole
 * > 1 && x.compareTo(heap.get(hole / 2)) < 0; hole /= 2){ Indexable
 * y=heap.get(hole / 2); y.setIndex(hole); heap.set(hole,y); } x.setIndex(hole);
 * heap.set(hole,x); }
 * 
 * private void enlargeArray(int newSize) { heap.setSize(newSize); }
 * 
 * /** Find the smallest item in the priority queue.
 * 
 * @return the smallest item, or throw an UnderflowException if empty.
 */
/*
 * public Indexable peek() { if (isEmpty()) throw new UnderflowException();
 * return heap.get(1); }
 * 
 * /** Remove the smallest item from the priority queue.
 * 
 * @return the smallest item, or throw an UnderflowException if empty.
 */
/*
 * public Indexable poll() { if (isEmpty()) throw new UnderflowException();
 * 
 * Indexable minItem = peek(); heap.set(1,heap.get(currentSize--));
 * percolateDown(1);
 * 
 * return minItem; }
 * 
 * /** Establish heap order property from an arbitrary arrangement of items.
 * Runs in linear time.
 */
/*
 * private void buildHeap() { for (int i = currentSize / 2; i > 0; i--)
 * percolateDown(i); }
 * 
 * /** Test if the priority queue is logically empty.
 * 
 * @return true if empty, false otherwise.
 */
/*
 * public boolean isEmpty() { return currentSize == 0; }
 * 
 * /** Make the priority queue logically empty.
 */
/*
 * public void reset() { currentSize = 0; }
 * 
 * 
 * 
 * /** Internal method to percolate down in the heap.
 * 
 * @param hole the index at which the percolate begins.
 */
/*
 * private void percolateDown(int hole) { int child; Indexable tmp =
 * heap.get(hole);
 * 
 * for (; hole * 2 <= currentSize; hole = child) { child = hole * 2;
 * if(heap.get(child)==null){ heap.set(hole,heap.get(child)); break; }
 * if(heap.get(child + 1)==null){ heap.set(hole,heap.get(child+1)); break; } if
 * (child != currentSize&& heap.get(child + 1).compareTo(heap.get(child)) < 0){
 * child++; } if (heap.get(child).compareTo(tmp) < 0){
 * heap.set(hole,heap.get(child)); } else { break; } } tmp.setIndex(hole);
 * heap.set(hole,tmp); }
 * 
 * public void change(AnyType b) { int index = b.getIndex();
 * if(heap.get(index)==null){ offer(b); } else { if (!heap.get(index).equals(b))
 * { if (heap.get(index).compareTo(b) < 0) { heap.set(index,b);
 * percolateUp(index); } else { heap.set(index,b); percolateDown(index); } } } }
 */

