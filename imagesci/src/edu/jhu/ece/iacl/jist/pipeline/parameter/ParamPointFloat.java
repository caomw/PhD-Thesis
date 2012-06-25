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

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamPointFloatFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * 3D Point parameter for floating point.
 * 
 * @author Blake Lucas
 */
public class ParamPointFloat extends ParamModel<Point3f> implements ParamPoint {

	/** The px. */
	protected ParamFloat px;

	/** The py. */
	protected ParamFloat py;

	/** The pz. */
	protected ParamFloat pz;

	/**
	 * Default constructor.
	 */
	public ParamPointFloat() {
		super();
		px = new ParamFloat("x");
		py = new ParamFloat("y");
		pz = new ParamFloat("z");
		this.factory = new ParamPointFloatFactory(this);
	}

	/**
	 * Constructor.
	 * 
	 * @param p
	 *            point
	 */
	public ParamPointFloat(Point3f p) {
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
	public ParamPointFloat(String name) {
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
	public ParamPointFloat(String name, Point3f p) {
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
		Point3f pt = getValue();
		return pt.x + ";" + pt.y + ";" + pt.z;
	}

	/**
	 * Get float point.
	 * 
	 * @return the value
	 */
	@Override
	public Point3f getValue() {
		return new Point3f(px.getFloat(), py.getFloat(), pz.getFloat());
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
		setValue(new Point3f(Float.valueOf(args[0]), Float.valueOf(args[1]),
				Float.valueOf(args[2])));
	}

	/**
	 * Set float point value.
	 * 
	 * @param value
	 *            point
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	@Override
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
	 * Clone object.
	 * 
	 * @return the param point float
	 */
	@Override
	public ParamPointFloat clone() {
		ParamPointFloat param = new ParamPointFloat();
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
		return "float point: semi-colon separated list of 3 coordinates";
	}

	/**
	 * Get X coordinate.
	 * 
	 * @return X coordinate
	 */
	@Override
	public ParamFloat getParamX() {
		return px;
	}

	/**
	 * Get Y coordinate.
	 * 
	 * @return Y coordinate
	 */
	@Override
	public ParamFloat getParamY() {
		return py;
	}

	/**
	 * Get Z coordinate.
	 * 
	 * @return Z coordinate
	 */
	@Override
	public ParamFloat getParamZ() {
		return pz;
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamPointFloatFactory(this);
	}

	/**
	 * Set double point value.
	 * 
	 * @param value
	 *            the value
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
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
	 * Get description of point.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return "(" + px.getFloat() + "," + py.getFloat() + "," + pz.getFloat()
				+ ")";
	};

	/**
	 * Validate point coordinates.
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
		px = new ParamFloat();
		px.xmlDecodeParam(document, JistXMLUtil.xmlReadElement(parent, "px"));
		py = new ParamFloat();
		py.xmlDecodeParam(document, JistXMLUtil.xmlReadElement(parent, "py"));
		pz = new ParamFloat();
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
