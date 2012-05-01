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

import java.io.File;

import edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamSurfaceFactory;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * Surface parameter file.
 * 
 * @author Blake Lucas
 */
public class ParamSurface extends ParamObject<EmbeddedSurface> {

	/**
	 * Construct parameter for a mandatory file type.
	 *
	 */
	public ParamSurface() {
		this("invalid");
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            the type
	 */
	public ParamSurface(DialogType type) {
		super(type);
		this.setReaderWriter(new SurfaceReaderWriter());
		this.factory = new ParamSurfaceFactory(this);
	};

	/**
	 * Instantiates a new param surface.
	 *
	 * @param name the name
	 */
	public ParamSurface(String name) {
		super(name);
		this.setReaderWriter(new SurfaceReaderWriter());
		this.factory = new ParamSurfaceFactory(this);
	}

	/**
	 * Construct parameter with specified restrictions.
	 * 
	 * @param name
	 *            parameter name
	 * @param type
	 *            directory or file dialog
	 */
	public ParamSurface(String name, DialogType type) {
		super(name, type);
		this.setReaderWriter(SurfaceReaderWriter.getInstance());
		this.factory = new ParamSurfaceFactory(this);
	}

	/**
	 * Get Surface.
	 * 
	 * @return the object
	 */
	@Override
	public EmbeddedSurface getObject() {
		return getSurface();
	}

	/**
	 * Get surface.
	 * 
	 * @return surface
	 */
	public EmbeddedSurface getSurface() {
		File f = getValue();
		if ((obj == null) && (f != null)) {
			obj = getReaderWriter().read(f);
		}
		return obj;
	}

	/**
	 * Set surface.
	 * 
	 * @param obj
	 *            the obj
	 */
	@Override
	public void setObject(EmbeddedSurface obj) {
		setValue(obj);
	}

	/**
	 * Set surface.
	 * 
	 * @param value
	 *            the value
	 */
	public void setValue(EmbeddedSurface value) {
		this.obj = value;
	}

	/**
	 * Clone object.
	 * 
	 * @return the param surface
	 */
	@Override
	public ParamSurface clone() {
		ParamSurface param = new ParamSurface(this.getName());
		param.dialogType = dialogType;
		param.extensionFilter = extensionFilter;
		param.readerWriter = this.readerWriter;
		param.file = this.file;
		param.uri = this.uri;
		if (obj != null) {
			param.setValue(obj);
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
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile#compareTo(edu.jhu.
	 * ece.iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public int compareTo(ParamModel model) {
		if (model instanceof ParamSurface) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamSurfaceFactory(this);
	}

	/**
	 * Get description of surface.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		File f = getValue();
		if (f != null) {
			return f.getAbsolutePath();
		}
		if (obj != null) {
			return obj.toString();
		}
		return null;
	}

	/**
	 * validate surface.
	 * 
	 * @throws InvalidParameterException
	 *             the invalid parameter exception
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (obj == null) {
			super.validate();
		}
	}
}
