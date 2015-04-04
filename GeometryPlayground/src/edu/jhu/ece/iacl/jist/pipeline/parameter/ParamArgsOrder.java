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

import java.util.ArrayList;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamHiddenFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Argument order for command line calls.
 * 
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public class ParamArgsOrder extends ParamHidden<ArrayList<String>> implements
		JISTInternalParam {

	/** The args order. */
	protected ArrayList<String> argsOrder;

	/**
	 * Instantiates a new param args order.
	 */
	public ParamArgsOrder() {
		super();
	}

	/**
	 * Instantiates a new param args order.
	 * 
	 * @param name
	 *            the name
	 * @param argsOrder
	 *            the args order
	 */
	public ParamArgsOrder(String name, ArrayList<String> argsOrder) {
		super();
		this.setName(name);
		this.argsOrder = argsOrder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#clone()
	 */
	@Override
	public ParamArgsOrder clone() {
		ParamArgsOrder param = new ParamArgsOrder(getName(),
				(ArrayList) argsOrder.clone());
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
		if (obj instanceof ParamArgsOrder) {
			if (((ParamArgsOrder) obj).argsOrder.equals(this.argsOrder)) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getValue()
	 */
	@Override
	public ArrayList<String> getValue() {
		return argsOrder;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		throw new RuntimeException("INTERNAL: Not Serializable");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamHidden#init()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setValue(java.lang
	 * .Object)
	 */
	@Override
	public void setValue(ArrayList<String> value)
			throws InvalidParameterValueException {
		this.argsOrder = value;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		throw new RuntimeException("INTERNAL: Not Serializable");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#toString()
	 */
	@Override
	public String toString() {
		return argsOrder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#validate()
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
		argsOrder = new ArrayList<String>();
		Vector<Element> nl = JistXMLUtil
				.xmlReadElementList(parent, "argsOrder");
		for (Element e : nl) {
			String val = e.getFirstChild().getNodeValue();
			argsOrder.add(val);
		}

	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		for (String s : argsOrder) {
			em = document.createElement("argsOrder");
			em.appendChild(document.createTextNode(s + ""));
			parent.appendChild(em);
		}
		return true;
	}
}
