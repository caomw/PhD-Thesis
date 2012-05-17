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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamFloatFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Float Parameter.
 * 
 * @author Blake Lucas
 */
public class ParamFloat extends ParamNumber {

	/**
	 * Construct float parameter with no restrictions on value.
	 */
	public ParamFloat() {
		this(MIN_FLOAT_VALUE, MAX_FLOAT_VALUE);
	}

	/**
	 * Construct float parameter with restrictions on min and max value.
	 * 
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamFloat(float min, float max) {
		this.min = min;
		this.max = max;
		value = new Float(0);
		this.factory = new ParamFloatFactory(this);
	}

	/**
	 * Construct float parameter with no restrictions on value.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamFloat(String name) {
		this(name, MIN_FLOAT_VALUE, MAX_FLOAT_VALUE);
	}

	/**
	 * Construct float parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param val
	 *            value
	 */
	public ParamFloat(String name, float val) {
		this(name, MIN_FLOAT_VALUE, MAX_FLOAT_VALUE, val);
	}

	/**
	 * Construct float parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamFloat(String name, float min, float max) {
		this(min, max);
		setName(name);
	}

	/**
	 * Construct float parameter with restrictions on min and max value.
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
	public ParamFloat(String name, float min, float max, float val) {
		this(min, max);
		this.setValue(val);
		setName(name);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(Float.valueOf(arg));
	}

	// in response to issue [#3930] Float vs. Double comparison fails
	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber#setValue(java.lang.Number)
	 */
	@Override
	public void setValue(Number value) {
		if (value.floatValue() > max.floatValue()) {
			value = max.floatValue();
		} else if (value.floatValue() < min.floatValue()) {
			value = min.floatValue();
		}
		this.value = value.floatValue();
	}

	/**
	 * Clone parameter.
	 * 
	 * @return the param float
	 */
	@Override
	public ParamFloat clone() {
		ParamFloat param = new ParamFloat(min.floatValue(), max.floatValue());
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
		if (model instanceof ParamFloat) {
			ParamFloat num = (ParamFloat) model;
			// this number range is more restrictive than the model's number
			// range
			if ((min.floatValue() < num.min.floatValue())
					|| (max.floatValue() > num.max.floatValue())) {
				return 1;
			} else if ((min.floatValue() == num.min.floatValue())
					|| (max.floatValue() == num.max.floatValue())) {
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
		return "float";
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
		factory = new ParamFloatFactory(this);
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
			this.value = new Float(str);
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
		if ((this.value.floatValue() < min.floatValue())
				|| (this.value.floatValue() > max.floatValue())) {
			throw new InvalidParameterException(this);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		value = Float.valueOf(JistXMLUtil.xmlReadTag(parent, "value"));
		min = Float.valueOf(JistXMLUtil.xmlReadTag(parent, "min"));
		max = Float.valueOf(JistXMLUtil.xmlReadTag(parent, "max"));
	}
}
