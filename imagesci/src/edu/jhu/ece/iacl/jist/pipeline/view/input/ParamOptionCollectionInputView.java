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
package edu.jhu.ece.iacl.jist.pipeline.view.input;

import java.util.Vector;

import javax.swing.JComponent;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamOption;

// TODO: Auto-generated Javadoc
/**
 * Input View to select multiple options from list.
 * 
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public class ParamOptionCollectionInputView extends ParamInputView implements
		ParamViewObserver {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1089191586873675113L;

	/** The models. */
	private Vector<ParamModel> models;

	/**
	 * Construct combobox with options from ParamOption.
	 * 
	 * @param param
	 *            the parameter
	 * @param collection
	 *            the collection
	 */
	public ParamOptionCollectionInputView(ParamOption param,
			ParamCollection collection) {
		super(collection);
		models = collection.getChildren();
		param.getInputView().addObserver(this);
		this.add(models.get(param.getIndex()).getInputView());
	}

	/**
	 * Commit changes.
	 */
	@Override
	public void commit() {
		for (ParamModel param : models) {
			param.getInputView().commit();
		}
	}

	/**
	 * Get field used to enter this value.
	 *
	 * @return the field
	 */
	@Override
	public JComponent getField() {
		return null;
	}

	/**
	 * Update view to reflect parameter value.
	 */
	@Override
	public void update() {
		for (ParamModel param : models) {
			param.getInputView().update();
		}
	}

	/**
	 * Update particular option parameter.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		// TODO Auto-generated method stub
		ParamOption options = (ParamOption) model;
		ParamInputView newView = models.get(options.getIndex()).getInputView();
		this.removeAll();
		this.add(newView);
		notifyObservers(model, view);
	}
}
