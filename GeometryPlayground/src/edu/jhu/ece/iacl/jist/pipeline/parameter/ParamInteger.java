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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamIntegerFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Integer Parameter.
 * 
 * @author Blake Lucas
 */
public class ParamInteger extends ParamNumber {

	/**
	 * Construct integer parameter with no restrictions on value.
	 */
	public ParamInteger() {
		this(MIN_INT_VALUE, MAX_INT_VALUE);
	}

	/**
	 * Construct integer parameter with restrictions on min and max value.
	 * 
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamInteger(int min, int max) {
		this.min = min;
		this.max = max;
		value = 0;
		this.factory = new ParamIntegerFactory(this);
	}

	/**
	 * Construct integer parameter with restrictions on min and max value.
	 * 
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 * @param val
	 *            the val
	 */
	public ParamInteger(int min, int max, int val) {
		this.min = min;
		this.max = max;
		this.setValue(val);
		this.factory = new ParamIntegerFactory(this);
	}

	/**
	 * Construct integer parameter with no restrictions on value.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamInteger(String name) {
		this(name, MIN_INT_VALUE, MAX_INT_VALUE);
	}

	/**
	 * Construct integer parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            value
	 */
	public ParamInteger(String name, int value) {
		this(MIN_INT_VALUE, MAX_INT_VALUE, value);
		setName(name);
	}

	/**
	 * Construct integer parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamInteger(String name, int min, int max) {
		this(min, max);
		setName(name);
	}

	/**
	 * Construct integer parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 * @param value
	 *            the value
	 */
	public ParamInteger(String name, int min, int max, int value) {
		this(min, max, value);
		setName(name);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(Integer.valueOf(arg));
	}

	// in response to issue [#3930] Float vs. Double comparison fails
	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber#setValue(java.lang.Number)
	 */
	@Override
	public void setValue(Number value) {
		if (value.intValue() > max.intValue()) {
			value = max.intValue();
		} else if (value.intValue() < min.intValue()) {
			value = min.intValue();
		}
		this.value = value.intValue();
	}

	/**
	 * Clone object.
	 * 
	 * @return the param integer
	 */
	@Override
	public ParamInteger clone() {
		ParamInteger param = new ParamInteger(min.intValue(), max.intValue());
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
		if (model instanceof ParamInteger) {
			ParamInteger num = (ParamInteger) model;
			// this number range is more restrictive than the model's number
			// range
			if ((min.intValue() < num.min.intValue())
					|| (max.intValue() > num.max.intValue())) {
				return 1;
				// this number range is equivalent to the model's number range
			} else if ((min.intValue() == num.min.intValue())
					|| (max.intValue() == num.max.intValue())) {
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
		return "integer";
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		return getValue().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.PipePort#init()
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamIntegerFactory(this);
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
			this.value = new Integer(str);
		} catch (NumberFormatException e) {
			System.err.println(getClass().getCanonicalName()
					+ "Error: Could not assign value " + str + " to "
					+ this.getLabel());
		}
	}

	/**
	 * Get description.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return String.format("%d", value.intValue());
	}

	/**
	 * Validate that the number is within the minimum and maximum restrictions.
	 * 
	 * @throws InvalidParameterException
	 *             parameter value does not meet value restriction
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if ((this.value.intValue() < min.intValue())
				|| (this.value.intValue() > max.intValue())) {
			throw new InvalidParameterException(this);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		value = Integer.valueOf(JistXMLUtil.xmlReadTag(parent, "value"));
		min = Integer.valueOf(JistXMLUtil.xmlReadTag(parent, "min"));
		max = Integer.valueOf(JistXMLUtil.xmlReadTag(parent, "max"));
	}
}
