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
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;

// TODO: Auto-generated Javadoc
/**
 * Observer to observe parameter changes.
 * 
 * @author Blake Lucas
 */
public interface ParamViewObserver {

	/**
	 * Indicate that an update has occurred to a particular parameter in a
	 * particular view pane.
	 * 
	 * @param model
	 *            parameter that was updated
	 * @param view
	 *            input view
	 */
	public abstract void update(ParamModel model, ParamInputView view);
}
