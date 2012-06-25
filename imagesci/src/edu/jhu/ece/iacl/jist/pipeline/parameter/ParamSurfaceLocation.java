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

import edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamSurfaceLocation.
 */
public class ParamSurfaceLocation extends ParamFile {
	/**
	 * Default constructor.
	 */
	public ParamSurfaceLocation() {
		super(DialogType.FILE);
		setExtensionFilter(SurfaceReaderWriter.getInstance()
				.getExtensionFilter());
		this.factory = new ParamFileFactory(this);
	}

	/**
	 * Instantiates a new param surface location.
	 *
	 * @param name the name
	 */
	public ParamSurfaceLocation(String name) {
		this();
		setName(name);
	}
}
