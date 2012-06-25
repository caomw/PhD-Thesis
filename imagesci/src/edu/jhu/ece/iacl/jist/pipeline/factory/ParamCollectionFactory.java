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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ProgressMonitor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamCollectionInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamCollectionOutputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * Parameter Collection Factory.
 * 
 * @author Blake Lucas
 */
public class ParamCollectionFactory extends ParamFactory {

	/** The params. */
	private ParamCollection params;

	/**
	 * Instantiates a new param collection factory.
	 * 
	 * @param params
	 *            the params
	 */
	public ParamCollectionFactory(ParamCollection params) {
		this.params = params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#createTreeNode()
	 */
	@Override
	public MutableTreeNode createTreeNode() {
		if (getParameter().isHidden()) {
			return null;
		}
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				"<HTML><B><I><font color='#009900'>"
						+ getParameter().getLabel() + "</font></I></B></HTML>");
		for (ParamModel model : getParameter().getChildren()) {
			if (!model.isHidden()) {
				root.add(model.createTreeNode());
			}
		}
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#equals(java.lang.
	 * Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof ParamFactory) {
			return this.equals(((ParamFactory) obj).getParameter());
		} else if (obj instanceof ParamCollection) {
			Vector<ParamModel> children = ((ParamCollection) obj).getValue();
			Vector<ParamModel> mychildren = getParameter().getValue();
			boolean equals = false;
			if (children.size() == mychildren.size()) {
				equals = true;
				for (int i = 0; i < children.size(); i++) {
					if (!children.get(i).getFactory().equals(mychildren.get(i))) {
						equals = false;
					}
				}
			}
			if (!equals) {
			}
			return equals;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getParameter()
	 */
	@Override
	public ParamCollection getParameter() {
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getInputView()
	 */
	@Override
	public ParamInputView getInputView() {
		if (inputView == null) {
			inputView = new ParamCollectionInputView(params);
		}
		return inputView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#getOutputView()
	 */
	@Override
	public ParamOutputView getOutputView() {
		if (outputView == null) {
			outputView = new ParamCollectionOutputView(params);
		}
		return outputView;
	}

	/**
	 * Gets the parameter.
	 * 
	 * @param name
	 *            the name
	 * @return the parameter
	 */
	public ParamModel getParameter(String name) {
		for (ParamModel param : params.getChildren()) {
			if (param.getName().equals(name)) {
				return param;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#importParameter(edu
	 * .jhu.ece.iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public boolean importParameter(ParamModel model) {
		boolean ret = true;
		if (model instanceof ParamCollection) {
			Hashtable<String, ParamModel> hash = ((ParamCollection) model)
					.getChildrenHash();
			Hashtable<String, ParamModel> targetHash = params.getChildrenHash();
			Enumeration<String> keys = targetHash.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();

				ParamModel mod = targetHash.get(key);
				ParamModel otherMod = hash.get(key);
				if (otherMod != null) {
					if (!mod.importParameter(otherMod)) {
						ret = false;
					}
				} else {
					System.err.println(getClass().getCanonicalName()
							+ "Could not find parameter matching " + key + " "
							+ mod.getName());
				}
			}
		} else {
			return false;
		}
		return ret;
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
		boolean ret = false;
		if (foreign instanceof ParamCollection) {
			ret = true;
			Hashtable<String, ParamModel> hash = ((ParamCollection) foreign)
					.getChildrenHash();
			Hashtable<String, ParamModel> targetHash = params.getChildrenHash();
			Enumeration<String> keys = targetHash.keys();
			if (monitor != null) {
				monitor.setMaximum(targetHash.keySet().size());
			}
			int progress = 0;
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				ParamModel mod = targetHash.get(key);
				if (!mod.isHidden()) {
					ParamModel fmod = hash.get(key);
					if (monitor != null) {
						monitor.setNote(params.getLabel() + ":"
								+ fmod.getLabel());
						if (monitor.isCanceled()) {
							break;
						}
					}
					if (fmod != null) {
						if (!mod.loadResources(fmod, monitor)) {
							ret = false;
						}
					} else {
						System.err.println(getClass().getCanonicalName()
								+ "Could not find parameter matching "
								+ mod.getName());
					}
					if (monitor != null) {
						monitor.setProgress(++progress);
					}
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory#saveResources(java
	 * .io.File)
	 */
	@Override
	public boolean saveResources(File dir, boolean saveSubDirectoryOverride) {
		boolean ret = true;
		String name = params.getName();
		if (name != null && !saveSubDirectoryOverride) {
			File cur = new File(dir,
					File.separatorChar
							+ edu.jhu.ece.iacl.jist.utility.FileUtil
									.forceSafeFilename(name));
			if (!cur.exists()) {
				cur.mkdir();
			}
			dir = cur.getAbsoluteFile();
		}
		for (ParamModel param : params.getChildren()) {
			if (!param.saveResources(dir, saveSubDirectoryOverride)) {
				System.out.println(getClass().getCanonicalName() + "\t"
						+ "ParamCollectionFactory: Resource Save Failed.");
				return false;
			}
		}
		return ret;
	}
}
