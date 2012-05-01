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

import java.util.Vector;

import edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileCollectionFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamSurfaceLocationCollection.
 */
public class ParamSurfaceLocationCollection extends ParamFileCollection {
	
	/**
	 * Instantiates a new param surface location collection.
	 */
	public ParamSurfaceLocationCollection() {
		mandatory = true;
		this.setReaderWriter(SurfaceReaderWriter.getInstance());
		fileParams = new Vector<ParamFile>();
		this.factory = new ParamFileCollectionFactory(this);
	}

	/**
	 * Instantiates a new param surface location collection.
	 *
	 * @param name the name
	 */
	public ParamSurfaceLocationCollection(String name) {
		this();
		setName(name);
	}
}
