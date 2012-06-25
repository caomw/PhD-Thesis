/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */
package edu.jhu.ece.iacl.jist.pipeline.factory;

import java.io.File;

import javax.swing.ProgressMonitor;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamSurface;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamURIInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refreshable;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refresher;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * Surface Parameter Factory.
 * 
 * @author Blake Lucas
 */
public class ParamSurfaceFactory extends ParamFileFactory {

	/** The param. */
	private ParamSurface param;

	/**
	 * Instantiates a new param surface factory.
	 * 
	 * @param param
	 *            the param
	 */
	public ParamSurfaceFactory(ParamSurface param) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileFactory#getInputView()
	 */
	@Override
	public ParamInputView getInputView() {
		if (inputView == null) {
			inputView = new ParamURIInputView(param);
		}
		return inputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileFactory#getParameter()
	 */
	@Override
	public ParamSurface getParameter() {
		return param;
	}

	/**
	 * Save Model Mesh to specified directory.
	 *
	 * @param dir save directory
	 * @param overRidesubDirectory the over ridesub directory
	 * @return resources saved correctly
	 */
	@Override
	public boolean saveResources(File dir, boolean overRidesubDirectory) {
		EmbeddedSurface resource = param.getSurface();
		if (resource != null) {
			File f = null;
			if ((f = param.getReaderWriter().write(resource, dir)) != null) {
				param.setValue(f);
				return true;
			} else {
				System.out.println(getClass().getCanonicalName() + "\t"
						+ "ParamSurfaceFactory: Resource Save Failed.");
				return false;
			}
		} else {
			return true;
		}
	}
}
