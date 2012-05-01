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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

// TODO: Auto-generated Javadoc
/**
 * Data label for TOADS.
 * 
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public class DataLabel implements Comparable<DataLabel> {

	/** The Constant MAX_LABELS. */
	public static final int MAX_LABELS = 50;

	/** The Constant types. */
	public static final String[] types = { "obj", "mask", "out", "bg", "outb",
			"maskb" };

	/** The id. */
	private int id = 0;

	/** The intensity. */
	private float intensity = 0;

	/** The name. */
	public String name = "";

	/** The segment. */
	private boolean segment = false;

	/** The type. */
	private int type = 0;

	/**
	 * Default constructor.
	 * 
	 * @param id
	 *            the id
	 */
	public DataLabel(float id) {
		this.id = (short) id;
		name = "class_" + id;
		type = 0;
		intensity = id;
	}

	/**
	 * Compare data labels by intensity value.
	 * 
	 * @param label
	 *            the label
	 * @return the int
	 */
	@Override
	public int compareTo(DataLabel label) {
		return (int) Math.signum(this.intensity - label.intensity);
	}

	/**
	 * Returns true if two data labels are equal.
	 * 
	 * @param o
	 *            the o
	 * @return true, if equals
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof DataLabel) {
			return (((DataLabel) o).id == id);
		} else {
			return false;
		}
	}

	/**
	 * Get id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get intensity.
	 * 
	 * @return the intensity
	 */
	public float getIntensity() {
		return intensity;
	}

	/**
	 * Get name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get object type.
	 * 
	 * @return object type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns true if segmented.
	 * 
	 * @return true, if checks if is segment
	 */
	public boolean isSegment() {
		return segment;
	}

	/**
	 * Set id.
	 * 
	 * @param id
	 *            the id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Set intensity.
	 * 
	 * @param intensity
	 *            the intensity
	 */
	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}

	/**
	 * Set name.
	 * 
	 * @param name
	 *            the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set segment flag.
	 * 
	 * @param segment
	 *            segment flag
	 */
	public void setSegment(boolean segment) {
		this.segment = segment;
	}

	/**
	 * Set object type.
	 * 
	 * @param type
	 *            the type
	 */
	public void setType(int type) {
		this.type = type;
	}
}
