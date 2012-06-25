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
package edu.jhu.cs.cisst.vent;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver;

// TODO: Auto-generated Javadoc
/**
 * The Interface VisualizationParameters.
 */
public interface VisualizationParameters extends ParamViewObserver {

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 */
	public void createVisualizationParameters(
			ParamCollection visualizationParameters);

	/**
	 * Update all visualization parameters.
	 */
	public void updateVisualizationParameters();

}
