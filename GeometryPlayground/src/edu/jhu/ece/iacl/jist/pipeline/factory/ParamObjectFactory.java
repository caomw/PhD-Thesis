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
package edu.jhu.ece.iacl.jist.pipeline.factory;

import java.io.File;

import javax.swing.ProgressMonitor;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamObject;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamFileInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refreshable;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refresher;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * Object Parameter Factory.
 * 
 * @author Blake Lucas
 */
public class ParamObjectFactory extends ParamFileFactory {

	/** The param. */
	private ParamObject param;

	/**
	 * Construct factory for specified parameter.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamObjectFactory(ParamObject param) {
		super();
		this.param = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#loadResources(edu
	 * .jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 * javax.swing.ProgressMonitor)
	 */
	@Override
	public boolean loadResources(ParamModel foreign, ProgressMonitor monitor) {
		if (foreign instanceof ParamFile) {
			if (!param.isHidden()) {
				ParamInputView view = getInputView();
				if (view instanceof Refreshable) {
					Refresher.getInstance().remove(((Refreshable) view));
				}
				param.setValue(((ParamFile) foreign).getValue());
				view.update();
				if (view instanceof Refreshable) {
					Refresher.getInstance().add(((Refreshable) view));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get parameter input view.
	 * 
	 * @return input view
	 */
	@Override
	public ParamInputView getInputView() {
		if (inputView == null) {
			inputView = new ParamFileInputView(param);
		}
		return inputView;
	}

	/**
	 * Get parameter output view.
	 * 
	 * @return output view
	 */
	@Override
	public ParamOutputView getOutputView() {
		if (outputView == null) {
			outputView = new ParamOutputView(param);
		}
		return outputView;
	}

	/**
	 * Get factory's parameter.
	 * 
	 * @return file parameter
	 */
	@Override
	public ParamFile getParameter() {
		return param;
	}

	/**
	 * Save Resource to specified directory.
	 *
	 * @param dir save directory
	 * @param overRidesubDirectory the over ridesub directory
	 * @return resources saved correctly
	 */
	@Override
	public boolean saveResources(File dir, boolean overRidesubDirectory) {
		Object resource = param.getObject();
		if ((resource != null) && (param.getReaderWriter() != null)) {
			File f = null;
			if ((f = param.getReaderWriter().write(resource, dir)) != null) {
				param.setValue(f);
				return true;
			} else {
				System.out.println(getClass().getCanonicalName() + "\t"
						+ "ParamObjectFactory: Resource Save Failed.");
				return false;
			}
		} else {
			return true;
		}
	}
}
