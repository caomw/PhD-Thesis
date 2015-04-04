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

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamBooleanFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Boolean Parameter.
 * 
 * @author Blake Lucas
 */
public class ParamBoolean extends ParamModel<Boolean> {

	/** The value. */
	private boolean value;

	/**
	 * Default constructor.
	 */
	public ParamBoolean() {
		this(false);
	}

	/**
	 * Create a new boolean parameter with a default value.
	 * 
	 * @param value
	 *            default value
	 */
	public ParamBoolean(boolean value) {
		factory = new ParamBooleanFactory(this);
		this.value = value;
	}

	/**
	 * Construct new boolean parameter with a default value of false.
	 * 
	 * @param name
	 *            the name
	 */
	public ParamBoolean(String name) {
		this(name, false);
	}

	/**
	 * Create a new boolean parameter with a default value.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            default value
	 */
	public ParamBoolean(String name, boolean value) {
		this(value);
		setName(name);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		return getXMLValue();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		return getValue().toString();
	}

	/**
	 * Get value.
	 * 
	 * @return the value
	 */
	@Override
	public Boolean getValue() {
		return new Boolean(value);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(Boolean.valueOf(arg));
	}

	/**
	 * The parameter value must be a boolean.
	 * 
	 * @param value
	 *            parameter value
	 */
	@Override
	public void setValue(Boolean value) {
		this.value = value.booleanValue();
	}

	/**
	 * Clone parameter.
	 * 
	 * @return the param boolean
	 */
	@Override
	public ParamBoolean clone() {
		ParamBoolean param = new ParamBoolean();
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
	 * Not restriction comparison needed for boolean values.
	 * 
	 * @param model
	 *            the model
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		return (model instanceof ParamBoolean) ? 0 : 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#equals(edu.jhu.ece
	 * .iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public boolean equals(ParamModel<Boolean> model) {
		Boolean value1 = this.getValue().booleanValue();
		Boolean value2 = model.getValue().booleanValue();
		if (this.getValue() == null && model.getValue() == null) {
			return true;
		}
		if (this.getValue() == null || model.getValue() == null) {
			return false;
		}
		if (value1 == value2) {
			return true;
		} else {
			System.err.println(getClass().getCanonicalName() + "NUMBER "
					+ this.getLabel() + ": " + value1 + " NOT EQUAL TO "
					+ model.getLabel() + ": " + value2);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "boolean";
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamBooleanFactory(this);
	}

	/**
	 * Get description of parameter value.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return ((value) ? "true" : "false");
	}

	/**
	 * No validation necessary for boolean values.
	 * 
	 * @throws InvalidParameterException
	 *             parameter does not meet value restrictions
	 */
	@Override
	public void validate() throws InvalidParameterException {
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		value = Boolean.valueOf(JistXMLUtil.xmlReadTag(parent, "value"));
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
		return true;
	}
}
