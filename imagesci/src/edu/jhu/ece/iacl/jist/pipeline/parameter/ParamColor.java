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

import java.awt.Color;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamColorFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamColor.
 */
public class ParamColor extends ParamModel<Color> {

	/** The color. */
	protected Color color;

	/**
	 * Default constructor.
	 */
	public ParamColor() {
		super();
		color = new Color(0, 0, 0);
		this.factory = new ParamColorFactory(this);
	}

	/**
	 * Instantiates a new param color.
	 * 
	 * @param p
	 *            the p
	 */
	public ParamColor(Color p) {
		this();
		color = new Color(p.getRGB());
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamColor(String name) {
		this();
		this.setName(name);
	}

	/**
	 * Instantiates a new parameter color.
	 * 
	 * @param name
	 *            the name
	 * @param p
	 *            the p
	 */
	public ParamColor(String name, Color p) {
		this();
		this.setName(name);
		color = new Color(p.getRGB());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		return getXMLValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		Color c = getValue();
		return c.getRed() + ";" + c.getGreen() + ";" + c.getBlue() + ";"
				+ c.getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getValue()
	 */
	@Override
	public Color getValue() {
		return color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.
	 * lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		String[] args = arg.trim().split("[;]+");
		if (args.length != 4) {
			throw new InvalidParameterValueException(this,
					"Cannot find four entries:" + arg);
		}
		setValue(new Color(Integer.valueOf(args[0]), Integer.valueOf(args[1]),
				Integer.valueOf(args[2]), Integer.valueOf(args[3])));
	}

	/**
	 * Set color value.
	 * 
	 * @param value
	 *            the color
	 * 
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	@Override
	public void setValue(Color value) throws InvalidParameterValueException {
		if (value != null) {
			color = new Color(value.getRGB());
		} else {
			throw new InvalidParameterValueException(this, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#clone()
	 */
	@Override
	public ParamColor clone() {
		ParamColor param = new ParamColor();
		param.color = new Color(color.getRGB());
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Compare to.
	 * 
	 * @param model
	 *            the model
	 * 
	 * @return the int
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#compareTo(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public int compareTo(ParamModel model) {
		return (model instanceof ParamColor) ? 0 : 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType
	 * ()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "color: semi-colon separated list of 4 color components (red,green,blue,alpha) which are integers in the range [0,255]";
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamColorFactory(this);
	}

	/**
	 * Get description of color.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return "(" + color.getRed() + "," + color.getGreen() + ","
				+ color.getBlue() + "," + color.getAlpha() + ")";
	}

	/**
	 * Validate color.
	 * 
	 * @throws InvalidParameterException
	 *             the invalid parameter exception
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (color == null) {
			throw new InvalidParameterException(this);
		}
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		color = Color.decode(JistXMLUtil.xmlReadTag(parent, "color"));
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("color");
		em.appendChild(document.createTextNode(color.toString() + ""));
		parent.appendChild(em);
		return true;
	}
}
