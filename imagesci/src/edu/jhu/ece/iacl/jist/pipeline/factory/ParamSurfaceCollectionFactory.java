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
package edu.jhu.ece.iacl.jist.pipeline.factory;

import java.io.File;
import java.util.List;

import javax.swing.ProgressMonitor;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamSurfaceCollection;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamFileCollectionInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamFileCollectionOutputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * Surface Collection Parameter Factory.
 * 
 * @author Blake Lucas
 */
public class ParamSurfaceCollectionFactory extends ParamFileCollectionFactory {

	/** The param. */
	private ParamSurfaceCollection param;

	/**
	 * Instantiates a new param surface collection factory.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamSurfaceCollectionFactory(ParamSurfaceCollection param) {
		super();
		this.param = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileCollectionFactory#equals
	 * (java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof ParamFactory) {
			return this.equals(((ParamFactory) obj).getParameter());
		} else if (obj instanceof ParamSurfaceCollection) {
			return this.getParameter().getValue()
					.equals(((ParamSurfaceCollection) obj).getValue());
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileCollectionFactory#
	 * getInputView()
	 */
	@Override
	public ParamInputView getInputView() {
		if (inputView == null) {
			inputView = new ParamFileCollectionInputView(param);
		}
		return inputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileCollectionFactory#
	 * getOutputView()
	 */
	@Override
	public ParamOutputView getOutputView() {
		if (outputView == null) {
			outputView = new ParamFileCollectionOutputView(param);
		}
		return outputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileCollectionFactory#
	 * getParameter()
	 */
	@Override
	public ParamSurfaceCollection getParameter() {
		return param;
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
		super.loadResources(foreign, monitor);
		boolean ret = true;
		List<ParamFile> surfs = param.getParameters();
		if (foreign instanceof ParamSurfaceCollection) {
			List<ParamFile> foreignSurfs = ((ParamSurfaceCollection) foreign)
					.getParameters();
			for (int i = 0; i < surfs.size(); i++) {
				if (!surfs.get(i).loadResources(foreignSurfs.get(i), null)) {
					ret = false;
				}
			}
			return ret;
		} else {
			return false;
		}
	}

	/**
	 * Save Surfaces to specified directory.
	 *
	 * @param dir save directory
	 * @param saveSubDirectoryOverride the save sub directory override
	 * @return resources saved correctly
	 */
	@Override
	public boolean saveResources(File dir, boolean saveSubDirectoryOverride) {
		super.saveResources(dir, saveSubDirectoryOverride);
		boolean ret = true;
		List<ParamFile> surfs = param.getParameters();
		for (int i = 0; i < surfs.size(); i++) {
			if (!surfs.get(i).saveResources(dir, saveSubDirectoryOverride)) {
				System.out
						.println(getClass().getCanonicalName()
								+ "\t"
								+ "ParamSurfaceCollectionFactory: Resource Save Failed.");
				ret = false;
			}
		}
		return ret;
	}
}
