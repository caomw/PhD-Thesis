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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamSurfaceCollectionFactory;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * Surface collection stores a collection of files. The restrictions on the
 * files are set to be the same as the collection.
 * 
 * @author Blake Lucas
 */
public class ParamSurfaceCollection extends ParamSurfaceLocationCollection {

	/**
	 * Default constructor.
	 */
	public ParamSurfaceCollection() {
		super();
		this.factory = new ParamSurfaceCollectionFactory(this);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamSurfaceCollection(String name) {
		this();
		this.setName(name);
	}

	/**
	 * Add a new value to the collection.
	 * 
	 * @param value
	 *            the value
	 * @return surface parameter
	 */
	@Override
	public void add(Object value) {
		ParamSurface param;
		if (value instanceof ParamSurface) {
			fileParams.add(param = (ParamSurface) value);
		} else {
			param = create(value);
			fileParams.add(param);
		}
		// return param;
	}

	/**
	 * Create a new ParamSurface with the same restrictions as the collection
	 * and the specified value.
	 * 
	 * @param value
	 *            the value
	 * @return surface parameter
	 */
	@Override
	protected ParamSurface create(Object value) {
		ParamSurface param = new ParamSurface(getName());
		if (value instanceof String) {
			param.setValue((String) value);
			param.setName((String) value);
		} else if (value instanceof File) {
			param.setValue((File) value);
			param.setName(((File) value).getName());
		} else if (value instanceof EmbeddedSurface) {
			param.setValue((EmbeddedSurface) value);
			// param.setName(((EmbeddedSurface) value).getName());
		}
		param.setReaderWriter(readerWriter);
		param.setMandatory(mandatory);
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Set the volume collection. This method accepts ArrayLists with any of the
	 * valid types of ParamFile
	 * 
	 * @param value
	 *            parameter value
	 */
	@Override
	public void setValue(List value) {
		clear();
		for (Object obj : value) {
			this.add(obj);
		}
	}

	/**
	 * Remove all files from collection.
	 */
	@Override
	public void clear() {
		this.fileParams.clear();
	}

	/**
	 * Clone object.
	 * 
	 * @return the param surface collection
	 */
	@Override
	public ParamSurfaceCollection clone() {
		ParamSurfaceCollection param = new ParamSurfaceCollection();
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.fileParams = new Vector<ParamFile>(fileParams.size());
		for (ParamFile p : fileParams) {
			param.fileParams.add(p.clone());
		}
		param.mandatory = mandatory;
		param.readerWriter = readerWriter;
		return param;
	}

	/**
	 * Compare restriction of one volume collection to another.
	 * 
	 * @param model
	 *            the model
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		return (model instanceof ParamSurfaceCollection) ? 0 : 1;
	}

	/**
	 * Get surfaces.
	 * 
	 * @return list of surface
	 */
	public List<EmbeddedSurface> getSurfaceList() {
		ArrayList<EmbeddedSurface> surfs = new ArrayList<EmbeddedSurface>();
		for (ParamFile param : fileParams) {
			surfs.add(((ParamSurface) param).getSurface());
		}
		return surfs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFileCollection#size()
	 */
	@Override
	public int size() {
		return this.fileParams.size();
	}
}
