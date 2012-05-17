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
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamNumber;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamIntegerSliderInputView.
 */
public class ParamIntegerSliderInputView extends ParamNumberSliderInputView {

	/**
	 * Instantiates a new param integer slider input view.
	 * 
	 * @param param
	 *            the param
	 * @param ticks
	 *            the ticks
	 */
	public ParamIntegerSliderInputView(ParamNumber param, int ticks) {
		super(param);
		int min = param.getMin().intValue();
		int max = param.getMax().intValue();
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();

		for (int i = 0; i < ticks; i++) {
			double t = i / (double) (ticks - 1);
			int pos = (int) Math.round(min + (max - min) * t);
			labelTable.put(pos, new JLabel(String.format("%d", pos)));
		}
		update();
		((JSlider) getField()).setSnapToTicks(false);
		((JSlider) getField()).setLabelTable(labelTable);
	}

}
