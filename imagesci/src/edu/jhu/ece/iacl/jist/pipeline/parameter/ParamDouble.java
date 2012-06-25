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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamDoubleFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Double Parameter.
 * 
 * @author Blake Lucas
 */
public class ParamDouble extends ParamNumber {

	/**
	 * Construct double parameter with no restrictions on value.
	 */
	public ParamDouble() {
		this(MIN_DOUBLE_VALUE, MAX_DOUBLE_VALUE);
	}

	/**
	 * Construct double parameter with restrictions on min and max value.
	 * 
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamDouble(double min, double max) {
		this.min = min;
		this.max = max;
		value = new Double(0);
		this.factory = new ParamDoubleFactory(this);
	}

	/**
	 * Construct double parameter with no restrictions on value.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamDouble(String name) {
		this(name, MIN_DOUBLE_VALUE, MAX_DOUBLE_VALUE);
	}

	/**
	 * Construct double parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param val
	 *            value
	 */
	public ParamDouble(String name, double val) {
		this(name, MIN_DOUBLE_VALUE, MAX_DOUBLE_VALUE, val);
	}

	/**
	 * Construct double parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamDouble(String name, double min, double max) {
		this(min, max);
		setName(name);
	}

	/**
	 * Construct double parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 * @param val
	 *            the val
	 */
	public ParamDouble(String name, double min, double max, double val) {
		this(min, max);
		this.setValue(val);
		setName(name);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(Double.valueOf(arg));
	}

	// in response to issue [#3930] Float vs. Double comparison fails
	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber#setValue(java.lang.Number)
	 */
	@Override
	public void setValue(Number value) {
		if (value.doubleValue() > max.doubleValue()) {
			value = max.doubleValue();
		} else if (value.doubleValue() < min.doubleValue()) {
			value = min.doubleValue();
		}
		this.value = value.doubleValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber#clone()
	 */
	@Override
	public ParamDouble clone() {
		ParamDouble param = new ParamDouble(min.doubleValue(),
				max.doubleValue());
		param.setValue(value);
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Compare the min and max value restrictions of two parameters.
	 * 
	 * @param model
	 *            the model
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		if (model instanceof ParamDouble) {
			ParamDouble num = (ParamDouble) model;
			// this number range is more restrictive than the model's number
			// range
			if ((min.doubleValue() < num.min.doubleValue())
					|| (max.doubleValue() > num.max.doubleValue())) {
				return 1;
			} else if ((min.doubleValue() == num.min.doubleValue())
					|| (max.doubleValue() == num.max.doubleValue())) {
				// this number range is equivalent to the model's number range
				return 0;
				// this number range is less restrictive than the model's number
				// range
			} else {
				return -1;
			}
		}
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "double";
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		return getValue().toString();
	}

	/**
	 * Initialize this object.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamDoubleFactory(this);
	}

	/**
	 * Set value from string.
	 * 
	 * @param str
	 *            the str
	 */
	@Override
	public void setValue(String str) {
		try {
			this.value = new Double(str);
		} catch (NumberFormatException e) {
			System.err.println(getClass().getCanonicalName()
					+ "Error: Could not assign value " + str + " to "
					+ this.getLabel());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber#toString()
	 */
	@Override
	public String toString() {
		if (value.doubleValue() == value.intValue()) {
			return "" + value.intValue();
		} else {
			return value.toString();
		}
	}

	/**
	 * Validate that the number is within the minimum and maximum restrictions.
	 * 
	 * @throws InvalidParameterException
	 *             parameter does not meet value restrictions
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if ((this.value.doubleValue() < min.doubleValue())
				|| (this.value.doubleValue() > max.doubleValue())) {
			throw new InvalidParameterException(this);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		value = Double.valueOf(JistXMLUtil.xmlReadTag(parent, "value"));
		min = Double.valueOf(JistXMLUtil.xmlReadTag(parent, "min"));
		max = Double.valueOf(JistXMLUtil.xmlReadTag(parent, "max"));
	}

}
