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
package edu.jhu.cs.cisst.vent.converter.processing;

import edu.jhu.cs.cisst.vent.converter.ConvertEmbeddedSurface;
import edu.jhu.cs.cisst.vent.structures.processing.PTriangleMesh;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * The Class ConvertEmbeddedSurfaceToPTriangleMesh.
 */
public class ConvertEmbeddedSurfaceToPTriangleMesh implements
		ConvertEmbeddedSurface<PTriangleMesh> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.converter.Converter#convert(java.lang.Object)
	 */
	@Override
	public PTriangleMesh convert(EmbeddedSurface surf) {
		return new PTriangleMesh(surf.getVertexCopy(), surf.getNormalCopy(),
				surf.getIndexCopy());
	}

}
