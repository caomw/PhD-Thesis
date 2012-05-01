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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/**
 * Number Parameter storage.
 * 
 * @author Blake Lucas
 */
public abstract class ParamNumber extends ParamModel<Number> {

	/** The Constant MAX_DOUBLE_VALUE. */
	public static final double MAX_DOUBLE_VALUE = 1E20;

	/** The Constant MAX_FLOAT_VALUE. */
	public static final float MAX_FLOAT_VALUE = 1E20f;

	/** The Constant MAX_INT_VALUE. */
	public static final int MAX_INT_VALUE = 1000000000;

	/** The Constant MAX_LONG_VALUE. */
	public static final long MAX_LONG_VALUE = 1000000000;

	/** The Constant MIN_DOUBLE_VALUE. */
	public static final double MIN_DOUBLE_VALUE = -1E20;

	/** The Constant MIN_FLOAT_VALUE. */
	public static final float MIN_FLOAT_VALUE = -1E20f;

	/** The Constant MIN_INT_VALUE. */
	public static final int MIN_INT_VALUE = -1000000000;

	/** The Constant MIN_LONG_VALUE. */
	public static final long MIN_LONG_VALUE = -1000000000;

	/** The max. */
	protected Number max;

	/** The min. */
	protected Number min;

	/** The value. */
	protected Number value;

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		return getValue().toString();
	}

	/**
	 * Get the number value.
	 * 
	 * @return number value
	 */
	@Override
	public Number getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#clone()
	 */
	@Override
	public abstract ParamNumber clone();

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#equals(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public boolean equals(ParamModel<Number> model) {
		Number value1 = this.getValue();
		Number value2 = model.getValue();
		if (value1 == null && value2 == null) {
			return true;
		}
		if (value1 == null || value2 == null) {
			return false;
		}
		if (value1.doubleValue() == value2.doubleValue()) {
			return true;
		} else {
			System.err.println(getClass().getCanonicalName() + "NUMBER "
					+ this.getLabel() + ": " + value1 + " NOT EQUAL TO "
					+ model.getLabel() + ": " + value2);
			return false;
		}
	}

	/**
	 * Get the number value as double.
	 * 
	 * @return double value
	 */
	public double getDouble() {
		return value.doubleValue();
	}

	/**
	 * Get the number value as float.
	 * 
	 * @return float value
	 */
	public float getFloat() {
		return value.floatValue();
	}

	/**
	 * Get the number value as int.
	 * 
	 * @return integer value
	 */
	public int getInt() {
		return value.intValue();
	}

	/**
	 * Get the number value as long.
	 * 
	 * @return long value
	 */
	public long getLong() {
		return value.longValue();
	}

	/**
	 * Get maximum possible value.
	 * 
	 * @return the max
	 */
	public Number getMax() {
		return max;
	}

	/**
	 * Get minimum possible value.
	 * 
	 * @return the min
	 */
	public Number getMin() {
		return min;
	}

	/**
	 * Set the parameter. The value must be of Number type.
	 * 
	 * Now institutes type-safe censoring (clamping).(12/2008 bl)
	 * 
	 * @param value
	 *            number value
	 */
	@Override
	public void setValue(Number value) {
		if (value.doubleValue() > max.doubleValue()) {
			value = max;
		} else if (value.doubleValue() < min.doubleValue()) {
			value = min;
		}
		this.value = value;
	}

	/**
	 * Set the parameter. The value can be a String
	 * 
	 * @param str
	 *            the str
	 */
	public abstract void setValue(String str);

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#toString()
	 */
	@Override
	public String toString() {
		return String.format("%6f", value.doubleValue());
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("value");
		em.appendChild(document.createTextNode(value + ""));
		parent.appendChild(em);
		em = document.createElement("min");
		em.appendChild(document.createTextNode(min + ""));
		parent.appendChild(em);
		em = document.createElement("max");
		em.appendChild(document.createTextNode(max + ""));
		parent.appendChild(em);
		return true;
	}

}
