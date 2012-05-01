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

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamStringFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * String parameter.
 * 
 * @author Blake Lucas
 */
public class ParamString extends ParamModel<String> {

	/** The string. */
	protected String string;

	/**
	 * Constructor.
	 */
	public ParamString() {
		this("", null);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamString(String name) {
		this(name, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 * @param str
	 *            string
	 */
	public ParamString(String name, String str) {
		setName(name);
		this.string = str;
		this.factory = new ParamStringFactory(this);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		return getValue();
	}

	/**
	 * Get string.
	 * 
	 * @return the value
	 */
	@Override
	public String getValue() {
		return string;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(arg);
	}

	/**
	 * Set string.
	 * 
	 * @param value
	 *            the value
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	@Override
	public void setValue(String value) throws InvalidParameterValueException {
		if (value == null) {
			throw new InvalidParameterValueException(this, "String is null");
		}
		this.string = value;
	}

	/**
	 * Clone object.
	 * 
	 * @return the param string
	 */
	@Override
	public ParamString clone() {
		ParamString param = new ParamString();
		if (string != null) {
			param.string = new String(string);
		}
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#compareTo(edu.jhu
	 * .ece.iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public int compareTo(ParamModel model) {
		return (model instanceof ParamString) ? 0 : 1;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "string";
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamStringFactory(this);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		return string;
	}

	/**
	 * Get string.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return string;
	}

	/**
	 * validate string. String cannot be null or zero length.
	 * 
	 * @throws InvalidParameterException
	 *             the invalid parameter exception
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (string == null) {
			throw new InvalidParameterException(this, "String is null");
		}
		if (string.length() == 0) {
			throw new InvalidParameterException(this, "String length is zero");
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		string = JistXMLUtil.xmlReadTag(parent, "string");
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("string");
		em.appendChild(document.createTextNode(string + ""));
		parent.appendChild(em);
		return true;
	}
}
