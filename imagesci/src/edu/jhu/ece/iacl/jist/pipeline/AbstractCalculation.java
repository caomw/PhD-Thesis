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
package edu.jhu.ece.iacl.jist.pipeline;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * Observable Calculation allows a calculation to be monitored.
 * 
 * @author Blake Lucas
 */
public abstract class AbstractCalculation {

	/** The start time. */
	private long actualStartTime = 0;

	/** List of children calculations. */
	private ArrayList<AbstractCalculation> children;

	/** total completed computational units. */
	private long completedUnits = 0;

	/** The start time. */
	private long cpuStartTime = 0;

	/** Label for calculation to be displayed. */
	private String label = "";

	/** Parent calculation. */
	private AbstractCalculation parent;

	/** total computational units. */
	private long totalUnits = 0;

	/**
	 * Default constructor.
	 */
	public AbstractCalculation() {
		children = new ArrayList<AbstractCalculation>();
		parent = null;
		actualStartTime = cpuStartTime = System.currentTimeMillis();
	}

	/**
	 * Constructor with parent calculation.
	 * 
	 * @param parent
	 *            parent calculation
	 */
	public AbstractCalculation(AbstractCalculation parent) {
		children = new ArrayList<AbstractCalculation>();
		this.parent = parent;
		parent.children.add(this);
	}

	/**
	 * Get current label of calculation including any child calculations in
	 * progress.
	 * 
	 * @return the current label
	 */
	public String getCurrentLabel() {
		String str = getLabel() + " (" + Math.round(100 * getProgress()) + "%)";
		return str;
		// Rendering the progress as HTML occasionally causes a runtime error
		// because
		// HTML rendering is not thread safe
		/*
		 * if (children.size() > 0) { str += "<ul><li>" +
		 * children.get(0).getCurrentLabel() + "</li></ul>"; } if (parent ==
		 * null) str = "<HTML>" + str + "</HTML>";
		 * 
		 * return str;
		 */
	}

	/**
	 * Label for this computation.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Return normalized progress of this calculation.
	 * 
	 * @return the progress
	 */
	public double getProgress() {
		if (totalUnits == 0) {
			return 0;
		}
		if (totalUnits <= completedUnits) {
			return 1.0;
		}
		// System.out.println(getClass().getCanonicalName()+"\t"+getCurrentLabel()+":"+getCompletedUnits()+"/"+getTotalUnits());
		return completedUnits / (double) totalUnits;
	}

	/**
	 * Return normalized progress of deepest child calculation.
	 * 
	 * @return the current progress
	 */
	public double getCurrentProgress() {
		if (getTotalUnits() == 0) {
			return 0;
		}
		if (getTotalUnits() <= getCompletedUnits()) {
			return 1.0;
		}
		return getCompletedUnits() / (double) getTotalUnits();
	}

	/**
	 * True if completed units equals total computational units.
	 * 
	 * @return true, if checks if is completed
	 */
	public/* synchronized */boolean isCompleted() {
		return (getCompletedUnits() == getTotalUnits());
	}

	/**
	 * Total computational units for calculation.
	 * 
	 * @return the total units
	 */
	public synchronized long getTotalUnits() {
		if (children.size() > 0) {
			return children.get(0).getTotalUnits();
		} else {
			return totalUnits;
		}
	}

	/**
	 * Completed computational units for the calculation.
	 * 
	 * @return the completed units
	 */
	public synchronized long getCompletedUnits() {
		if (children.size() > 0) {
			return children.get(0).getCompletedUnits();
		} else {
			return completedUnits;
		}
	}

	/**
	 * Add a child calculation to this calculation.
	 * 
	 * @param child
	 *            the child
	 */
	public synchronized void add(AbstractCalculation child) {
		children.add(child);
	}

	/**
	 * Add extra computational units to the total computational units.
	 * 
	 * @param extra
	 *            the extra completed units
	 */
	public void addTotalUnits(long extra) {
		totalUnits += extra;
	}

	/**
	 * Decrement the number of completed units.
	 */
	public void decrementCompletedUnits() {
		completedUnits--;
		completedUnits = Math.max(0, Math.min(completedUnits, totalUnits));
	}

	/**
	 * Decrement the number of completed units.
	 * 
	 * @param inc
	 *            increment amount
	 */
	public void decrementCompletedUnits(int inc) {
		completedUnits -= inc;
		completedUnits = Math.max(0, Math.min(completedUnits, totalUnits));
	}

	/**
	 * Increment the number of completed units.
	 */
	public/* synchronized */void incrementCompletedUnits() {
		completedUnits++;
		completedUnits = Math.min(completedUnits, totalUnits);
	}

	/**
	 * Increment the number of completed units.
	 * 
	 * @param inc
	 *            increment
	 */
	public/* synchronized */void incrementCompletedUnits(int inc) {
		completedUnits += inc;
		completedUnits = Math.min(completedUnits, totalUnits);
	}

	/**
	 * Mark algorithm as completed at the end of calculation It is mandatory
	 * that this method be executed at the end of the calculation.
	 */
	public synchronized void markCompleted() {

	}

	/**
	 * Mark algorithm as completed at the end of calculation It is mandatory
	 * that this method be executed at the end of the calculation.
	 *
	 */
	/*
	 * public synchronized void markCompleted(String event) { if (monitor !=
	 * null) { summary.record(this.getLabel(), cpuStartTime,
	 * monitor.getTimeStamp(),actualStartTime,System.currentTimeMillis()); }
	 * else { summary.record(this.getLabel(), cpuStartTime,
	 * System.currentTimeMillis(),actualStartTime,System.currentTimeMillis());
	 * System.err.println(getClass().getCanonicalName()+this.getLabel() +
	 * ":No Calculation Monitor"); }
	 * System.out.println(getClass().getCanonicalName
	 * ()+"\t"+summary.getSummary()); if (parent != null) {
	 * parent.children.remove(this); } if (monitor != null) { cpuStartTime =
	 * monitor.getTimeStamp(); actualStartTime=System.currentTimeMillis(); }
	 * else { actualStartTime=cpuStartTime = System.currentTimeMillis(); } }
	 */

	/**
	 * Reset the completed units for this calculation and all child
	 * calculations.
	 */
	public synchronized void reset() {

	}

	/**
	 * Set the percent completed [0,1] Requires that the total number of units
	 * is non-zero, (i.e. setTotalUnits(100))
	 * 
	 * @param percent
	 *            the percent
	 */
	public/* synchronized */void setCompletedUnits(double percent) {
		completedUnits = Math.round(Math.min(1, Math.max(0, percent))
				* totalUnits);
	}

	/**
	 * Set the number of units completed.
	 * 
	 * @param units
	 *            the units
	 */
	public/* synchronized */void setCompletedUnits(int units) {
		completedUnits = Math.min(units, totalUnits);
	}

	/**
	 * Set label to display in progress bar for this calculation.
	 * 
	 * @param str
	 *            the string
	 */
	public void setLabel(String str) {
		label = str;
	}

	/**
	 * Set total computational units for this calculation. This method also
	 * resets the total completed units.
	 * 
	 * @param total
	 *            the total
	 */
	public void setTotalUnits(long total) {
		completedUnits = 0;
		totalUnits = total;
	}
}
