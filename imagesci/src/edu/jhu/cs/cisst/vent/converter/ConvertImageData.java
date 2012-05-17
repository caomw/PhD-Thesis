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
package edu.jhu.cs.cisst.vent.converter;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Interface ConvertImageData.
 *
 * @param <DestinationType> the generic type
 */
public interface ConvertImageData<DestinationType> extends
		Converter<ImageData, DestinationType> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.converter.Converter#convert(java.lang.Object)
	 */
	@Override
	public abstract DestinationType convert(ImageData source);
}
