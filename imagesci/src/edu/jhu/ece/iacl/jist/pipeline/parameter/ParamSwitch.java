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

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamHiddenFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Command line switch parameter.
 * 
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public class ParamSwitch extends ParamHidden<String> implements
		JISTInternalParam {

	/** The switch spaced. */
	protected boolean switchSpaced;

	/** The switch string. */
	protected String switchString;

	/**
	 * Default constructor.
	 */
	public ParamSwitch() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 * @param switchString
	 *            switch string
	 */
	public ParamSwitch(String name, String switchString) {
		this(name, switchString, true);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 * @param switchString
	 *            switch string
	 * @param switchSpaced
	 *            flag to indicate whether to put a space after switch
	 */
	public ParamSwitch(String name, String switchString, boolean switchSpaced) {
		super();
		this.setName(name);
		this.switchSpaced = switchSpaced;
		this.switchString = switchString;
	}

	/**
	 * Get switch description.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return switchString + ((isSwitchSpaced()) ? " " : "");
	}

	/**
	 * Returns true if there is a space after the switch.
	 * 
	 * @return true if spaced
	 */
	public boolean isSwitchSpaced() {
		return switchSpaced;
	}

	/**
	 * Clone object.
	 * 
	 * @return the param switch
	 */
	@Override
	public ParamSwitch clone() {
		ParamSwitch param = new ParamSwitch(getName(), this.switchString,
				this.switchSpaced);
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
	public int compareTo(ParamModel obj) {
		if (obj instanceof ParamSwitch) {
			if (((ParamSwitch) obj).switchString.equals(this.switchString)) {
				return 0;
			} else {
				return 1;
			}
		}
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "deprecated: DO NOT USE. REPORT THIS MESSAGE IF SEEN";
	}

	/**
	 * Get switch string.
	 * 
	 * @return the value
	 */
	@Override
	public String getValue() {
		return switchString;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		throw new RuntimeException("INTERNAL: Not Serializable");
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamHiddenFactory(this);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		return null;
	}

	/**
	 * Indicates whether there should be a space after the switch.
	 * 
	 * @param switchSpaced
	 *            true if spaced
	 */
	public void setSwitchSpaced(boolean switchSpaced) {
		this.switchSpaced = switchSpaced;
	}

	/**
	 * Set switch string.
	 * 
	 * @param value
	 *            the value
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	@Override
	public void setValue(String value) throws InvalidParameterValueException {
		this.switchString = value;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		throw new RuntimeException("INTERNAL: Not Serializable");

	}

	/**
	 * Unimplemented.
	 * 
	 * @throws InvalidParameterException
	 *             the invalid parameter exception
	 */
	@Override
	public void validate() throws InvalidParameterException {
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		switchString = JistXMLUtil.xmlReadTag(parent, "switchString");
		switchSpaced = Boolean.valueOf(JistXMLUtil.xmlReadTag(parent,
				"switchSpaced"));
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("switchString");
		em.appendChild(document.createTextNode(switchString + ""));
		parent.appendChild(em);
		em = document.createElement("switchSpaced");
		em.appendChild(document.createTextNode(switchSpaced + ""));
		parent.appendChild(em);
		return true;
	}
}
