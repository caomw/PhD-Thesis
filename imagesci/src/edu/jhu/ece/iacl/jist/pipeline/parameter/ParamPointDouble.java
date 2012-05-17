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

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamPointDoubleFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * 3D Point parameter for double floating point.
 * 
 * @author Blake Lucas
 */
public class ParamPointDouble extends ParamModel<Point3d> implements ParamPoint {

	/** The px. */
	protected ParamDouble px;

	/** The py. */
	protected ParamDouble py;

	/** The pz. */
	protected ParamDouble pz;

	/**
	 * Default constructor.
	 */
	public ParamPointDouble() {
		super();
		px = new ParamDouble("x");
		py = new ParamDouble("y");
		pz = new ParamDouble("z");
		this.factory = new ParamPointDoubleFactory(this);
	}

	/**
	 * Constructor.
	 * 
	 * @param p
	 *            point
	 */
	public ParamPointDouble(Point3d p) {
		this();
		px.setValue(p.x);
		py.setValue(p.y);
		pz.setValue(p.z);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamPointDouble(String name) {
		this();
		this.setName(name);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 * @param p
	 *            point
	 */
	public ParamPointDouble(String name, Point3d p) {
		this();
		this.setName(name);
		px.setValue(p.x);
		py.setValue(p.y);
		pz.setValue(p.z);
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
		Point3d pt = getValue();
		return pt.x + ";" + pt.y + ";" + pt.z;
	}

	/**
	 * Get point value.
	 * 
	 * @return the value
	 */
	@Override
	public Point3d getValue() {
		return new Point3d(px.getFloat(), py.getFloat(), pz.getFloat());
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		String[] args = arg.trim().split("[;]+");
		if (args.length != 3) {
			throw new InvalidParameterValueException(this,
					"Cannot find three entries:" + arg);
		}
		setValue(new Point3d(Double.valueOf(args[0]), Double.valueOf(args[1]),
				Double.valueOf(args[2])));

	}

	/**
	 * Set double point value.
	 * 
	 * @param value
	 *            the value
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	@Override
	public void setValue(Point3d value) throws InvalidParameterValueException {
		if (value != null) {
			px.setValue(value.x);
			py.setValue(value.y);
			pz.setValue(value.z);
		} else {
			throw new InvalidParameterValueException(this, value);
		}
	}

	/**
	 * Clone object.
	 * 
	 * @return the param point double
	 */
	@Override
	public ParamPointDouble clone() {
		ParamPointDouble param = new ParamPointDouble();
		param.px = px.clone();
		param.py = py.clone();
		param.pz = pz.clone();
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
		return (model instanceof ParamPoint) ? px
				.compareTo(((ParamPoint) model).getParamX()) : 1;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "double point: semi-colon separated list of 3 coordinates";
	}

	/**
	 * Get X coordinate parameter.
	 * 
	 * @return X coordinate
	 */
	@Override
	public ParamDouble getParamX() {
		return px;
	}

	/**
	 * Get Y coordinate parameter.
	 * 
	 * @return Y coordinate
	 */
	@Override
	public ParamDouble getParamY() {
		return py;
	}

	/**
	 * Get Z coordinate parameter.
	 * 
	 * @return Z coordinate
	 */
	@Override
	public ParamDouble getParamZ() {
		return pz;
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamPointDoubleFactory(this);
	}

	/**
	 * Set float point value.
	 * 
	 * @param value
	 *            point
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	public void setValue(Point3f value) throws InvalidParameterValueException {
		if (value != null) {
			px.setValue(value.x);
			py.setValue(value.y);
			pz.setValue(value.z);
		} else {
			throw new InvalidParameterValueException(this, value);
		}
	}

	/**
	 * Set integer point value.
	 * 
	 * @param value
	 *            point
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	public void setValue(Point3i value) throws InvalidParameterValueException {
		if (value != null) {
			px.setValue(value.x);
			py.setValue(value.y);
			pz.setValue(value.z);
		} else {
			throw new InvalidParameterValueException(this, value);
		}
	};

	/**
	 * Get point description.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return "(" + px.getDouble() + "," + py.getDouble() + ","
				+ pz.getDouble() + ")";
	};

	/**
	 * Validate coordinate values.
	 * 
	 * @throws InvalidParameterException
	 *             the invalid parameter exception
	 */
	@Override
	public void validate() throws InvalidParameterException {
		px.validate();
		py.validate();
		pz.validate();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		px = new ParamDouble();
		px.xmlDecodeParam(document, JistXMLUtil.xmlReadElement(parent, "px"));
		py = new ParamDouble();
		py.xmlDecodeParam(document, JistXMLUtil.xmlReadElement(parent, "py"));
		pz = new ParamDouble();
		pz.xmlDecodeParam(document, JistXMLUtil.xmlReadElement(parent, "pz"));
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("px");
		px.xmlEncodeParam(document, em);
		parent.appendChild(em);
		em = document.createElement("py");
		py.xmlEncodeParam(document, em);
		parent.appendChild(em);
		em = document.createElement("pz");
		pz.xmlEncodeParam(document, em);
		parent.appendChild(em);
		return true;
	}

}
