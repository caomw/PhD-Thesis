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

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamURLFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamURL.
 */
public class ParamURL extends ParamModel<URL> {

	/** The url. */
	protected URL url;

	/**
	 * Instantiates a new param url.
	 */
	public ParamURL() {
		init();
	}

	/**
	 * Instantiates a new param url.
	 * 
	 * @param name
	 *            the name
	 * @param url
	 *            the url
	 */
	public ParamURL(String name, URL url) {
		this(name);
		setName(name);
		setValue(url);
	}

	/**
	 * Instantiates a new param url.
	 * 
	 * @param name
	 *            the name
	 */
	public ParamURL(String name) {
		this();
		setName(name);
	}

	/**
	 * Inits the.
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.PipePort#init()
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamURLFactory(this);
	}

	/**
	 * Set the url location for this parameter.
	 * 
	 * @param value
	 *            parameter value
	 */
	@Override
	public void setValue(URL value) {
		this.url = value;
	}

	/**
	 * Probe default value.
	 *
	 * @return the string
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		if (url == null) {
			return null;
		}
		return getXMLValue();
	}

	/**
	 * Gets the xML value.
	 *
	 * @return the xML value
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		if (getValue() == null) {
			return null;
		}
		return getValue().toString();
	}

	/**
	 * Validate that the specified url exists and meets all restrictions.
	 * 
	 * @throws InvalidParameterException
	 *             parameter value does not meet value restrictions
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (!mandatory) {
			return;
		}
		URL url = getValue();
		if (url == null) {
			throw new InvalidParameterException(this);
		}
	}

	/**
	 * Get url location.
	 * 
	 * @return url location
	 */
	@Override
	public URL getValue() {
		return url;
	}

	/**
	 * Sets the xML value.
	 *
	 * @param arg the new xML value
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(arg);
	}

	/**
	 * Set url location.
	 * 
	 * @param value
	 *            url location as string
	 */
	public void setValue(String value) {
		this.url = null;
		try {
			this.url = new URL(value);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	/**
	 * Clean.
	 *
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#clean()
	 */
	@Override
	public void clean() {
		url = null;
	}

	/**
	 * Clone object.
	 * 
	 * @return the param url
	 */
	@Override
	public ParamURL clone() {
		ParamURL param = new ParamURL(this.getName());
		param.setValue(url);
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Compare two parameters based on their mandatory fields.
	 * 
	 * @param model
	 *            the model
	 * 
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		if (model instanceof ParamURL) {
			ParamURL f = (ParamURL) model;
			if (this.mandatory && !f.isMandatory()) {
				return 1;
			} else {
				return 0;
			}
		}
		return 1;
	}

	/**
	 * Gets the human readable data type.
	 *
	 * @return the human readable data type
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "url";
	}

	/**
	 * Get description of parameter.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return (url != null) ? url.toString() : "None";
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		try {
			url = new URL(JistXMLUtil.xmlReadTag(parent, "url"));
		} catch (MalformedURLException e) {
			url = null;
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("url");
		em.appendChild(document.createTextNode(url + ""));
		parent.appendChild(em);
		return true;

	}
}
