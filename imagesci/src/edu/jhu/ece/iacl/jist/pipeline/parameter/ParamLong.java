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

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamLongFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Long Parameter.
 * 
 * @author Blake Lucas
 */
public class ParamLong extends ParamNumber {

	/**
	 * Construct long parameter with no restrictions on value.
	 */
	public ParamLong() {
		this(MIN_LONG_VALUE, MAX_LONG_VALUE);
	}

	/**
	 * Construct long parameter with restrictions on min and max value.
	 * 
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamLong(long min, long max) {
		this.min = min;
		this.max = max;
		value = 0;
		this.factory = new ParamLongFactory(this);
	}

	/**
	 * Construct long parameter with restrictions on min and max value.
	 * 
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 * @param val
	 *            the val
	 */
	public ParamLong(long min, long max, long val) {
		this.min = min;
		this.max = max;
		this.setValue(val);
		this.factory = new ParamLongFactory(this);
	}

	/**
	 * Construct long parameter with no restrictions on value.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamLong(String name) {
		this(name, MIN_LONG_VALUE, MAX_LONG_VALUE);
	}

	/**
	 * Construct long parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            value
	 */
	public ParamLong(String name, long value) {
		this(MIN_LONG_VALUE, MAX_LONG_VALUE, value);
		setName(name);
	}

	/**
	 * Construct long parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamLong(String name, long min, long max) {
		this(min, max);
		setName(name);
	}

	/**
	 * Construct long parameter with restrictions on min and max value.
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
	public ParamLong(String name, long min, long max, long value) {
		this(min, max, value);
		setName(name);
	}

	/**
	 * Clone object.
	 * 
	 * @return the param long
	 */
	@Override
	public ParamLong clone() {
		ParamLong param = new ParamLong(min.longValue(), max.longValue());
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
		if (model instanceof ParamLong) {
			ParamLong num = (ParamLong) model;
			// this number range is more restrictive than the model's number
			// range
			if ((min.longValue() < num.min.longValue())
					|| (max.longValue() > num.max.longValue())) {
				return 1;
				// this number range is equivalent to the model's number range
			} else if ((min.longValue() == num.min.longValue())
					|| (max.longValue() == num.max.longValue())) {
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
		return "long";
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
		factory = new ParamLongFactory(this);
	}

	/**
	 * Set value from string.
	 * 
	 * @param str
	 *            the str
	 */
	@Override
	public void setValue(String str) {
		this.value = new Long(str);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(Long.valueOf(arg));
	}

	/**
	 * Get description.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return String.format("%d", value.longValue());
	}

	/**
	 * Validate that the number is within the minimum and maximum restrictions.
	 * 
	 * @throws InvalidParameterException
	 *             parameter value does not meet value restriction
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if ((this.value.longValue() < min.longValue())
				|| (this.value.longValue() > max.longValue())) {
			throw new InvalidParameterException(this);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		value = Long.valueOf(JistXMLUtil.xmlReadTag(parent, "value"));
		min = Long.valueOf(JistXMLUtil.xmlReadTag(parent, "min"));
		max = Long.valueOf(JistXMLUtil.xmlReadTag(parent, "max"));
	}
}
