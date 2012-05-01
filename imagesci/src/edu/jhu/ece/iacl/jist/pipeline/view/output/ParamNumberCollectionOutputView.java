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
package edu.jhu.ece.iacl.jist.pipeline.view.output;

import java.awt.LayoutManager;

import javax.swing.BoxLayout;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumberCollection;

// TODO: Auto-generated Javadoc
/**
 * Output view for a collection of number parameters.
 * 
 * @author Blake Lucas <br>
 *         Muqun Li(muqun.li@vanderbilt.edu)
 */
public class ParamNumberCollectionOutputView extends ParamOutputView {

	/**
	 * Default constructor.
	 * 
	 * @param param
	 *            parameters
	 */
	public ParamNumberCollectionOutputView(ParamNumberCollection param) {
		this.param = param;
		for (ParamNumber vol : param.getParameterList()) {
			LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			this.setLayout(layout);
			add(vol.getOutputView());
		}
	}

	/**
	 * Update child parameter views.
	 */
	@Override
	public void update() {
		super.update();
		for (ParamNumber p : ((ParamNumberCollection) param).getParameterList()) {
			p.getOutputView().update();
		}
	}
}
