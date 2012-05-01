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
package edu.jhu.cs.cisst.vent.converter.processing;

import edu.jhu.cs.cisst.vent.converter.ConvertCurveCollection;
import edu.jhu.cs.cisst.vent.structures.processing.PCurveCollection;
import edu.jhu.ece.iacl.jist.structures.geom.CurveCollection;

// TODO: Auto-generated Javadoc
/**
 * The Class CovertCurveCollectionToPCurveCollection.
 */
public class CovertCurveCollectionToPCurveCollection implements
		ConvertCurveCollection<PCurveCollection> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.converter.Converter#convert(java.lang.Object)
	 */
	@Override
	public PCurveCollection convert(CurveCollection source) {
		return new PCurveCollection(source, false);
	}

}
