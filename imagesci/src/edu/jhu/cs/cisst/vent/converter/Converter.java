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
package edu.jhu.cs.cisst.vent.converter;

// TODO: Auto-generated Javadoc
/**
 * The Interface Converter.
 *
 * @param <SourceType> the generic type
 * @param <DestinationType> the generic type
 */
public interface Converter<SourceType, DestinationType> {

	/**
	 * Convert.
	 * 
	 * @param source
	 *            the source
	 * 
	 * @return the destination type
	 */
	public DestinationType convert(SourceType source);
}
