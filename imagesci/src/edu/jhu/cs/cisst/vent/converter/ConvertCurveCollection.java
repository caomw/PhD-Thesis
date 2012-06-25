/**
 * JIST Extensions for Computer-Integrated Surgery
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
 * @author Blake Lucas
 */
package edu.jhu.cs.cisst.vent.converter;

import edu.jhu.ece.iacl.jist.structures.geom.CurveCollection;

/**
 * The Interface ConvertCurveCollection.
 *
 * @param <DestinationType> the generic type
 */
public interface ConvertCurveCollection<DestinationType> extends
		Converter<CurveCollection, DestinationType> {

}
