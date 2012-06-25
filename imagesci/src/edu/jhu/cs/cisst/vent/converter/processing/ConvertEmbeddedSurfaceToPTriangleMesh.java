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
