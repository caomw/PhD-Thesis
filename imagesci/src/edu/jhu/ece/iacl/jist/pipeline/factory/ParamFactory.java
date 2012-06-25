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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import javax.swing.ProgressMonitor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

import edu.jhu.ece.iacl.jist.io.MipavController;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;

// TODO: Auto-generated Javadoc
/**
 * Parameter Factory is responsible for manipulating Parameter Models, which
 * allows the factory to be substituted for a different factory if a customized
 * factory is required.
 * 
 * @author Blake Lucas
 */
public abstract class ParamFactory {

	/** Input view for parameter. */
	protected ParamInputView inputView = null;

	/** Output view for parameter. */
	protected ParamOutputView outputView = null;

	/**
	 * Read parameter from XML file. This method also loads resources associated
	 * with parameter if they exist
	 * 
	 * @param f
	 *            file
	 * @return read successful
	 */
	public boolean read(File f) {
		BufferedReader in;
		if ((f == null) || !f.exists()) {
			return false;
		}
		try {
			System.out.println(getClass().getCanonicalName() + "\t"
					+ "Reading " + f);
			// Create input stream from file
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String text = "";
			StringBuffer buff = new StringBuffer();
			String str;
			// Read file as string
			while ((str = in.readLine()) != null) {
				buff.append(str + "\n");
			}
			text = buff.toString();
			in.close();
			// Reconstruct class from XML
			ParamModel foreignParam = fromXML(text);
			if (foreignParam == null) {
				System.err.println("jist.base" + "Imported parameter is null");
				return false;
			}
			boolean ret = true;
			// Import foreign parameter into current parameter
			if (importParameter(foreignParam)) {
				// Load associated resources if they exist
				ParamModel param = getParameter();
				if (!param.isHidden()) {
					ret = param.loadResources(foreignParam, null);
					if (!ret) {
						System.err.println("jist.base"
								+ "Error occurred while loading resoruces for "
								+ param.getLabel() + " "
								+ foreignParam.getLabel() + ":"
								+ foreignParam.toString());
					}
				} else {
					ret = true;
				}
			}
			// Update GUI with parameter values
			MipavController.setDefaultWorkingDirectory(f.getParentFile());
			return ret;
		} catch (Exception e) {
			System.err.println("jist.base"
					+ "Error occured while reading parameter file:\n"
					+ e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Read parameter from String. This method also loads resources associated
	 * with parameter if they exist
	 * 
	 * @param text
	 *            string
	 * @param loadResources
	 *            the load resources
	 * @return read successful
	 */
	public boolean read(String text, boolean loadResources) {
		try {
			ParamModel foreignParam = fromXML(text);
			if (foreignParam == null) {
				return false;
			}
			boolean ret = true;
			// Import foreign parameter into current parameter
			if (importParameter(foreignParam)) {
				if (loadResources) {
					// Load associated resources if they exist
					ParamModel param = getParameter();
					if (!param.isHidden()) {
						ret = param.loadResources(foreignParam, null);
					} else {
						ret = true;
					}
				}
			}
			// Update GUI with parameter values
			return ret;
		} catch (Exception e) {
			System.err.println("jist.base"
					+ "Error occured while reading parameter string:\n"
					+ e.getMessage());
			return false;
		}
	}

	/**
	 * Read parameter from XML file. This method also loads resources associated
	 * with parameter if they exist
	 * 
	 * @param f
	 *            file
	 * @return read parameter
	 */
	public static ParamModel fromXML(File f) {
		BufferedReader in;
		if ((f == null) || !f.exists()) {
			return null;
		}
		try {
			// Create input stream from file
			FileInputStream fis;
			in = new BufferedReader(new InputStreamReader(
					fis = new FileInputStream(f)));
			String text = "";
			StringBuffer buff = new StringBuffer();
			String str;
			// Read file as string
			while ((str = in.readLine()) != null) {
				buff.append(str + "\n");
			}
			text = buff.toString();
			fis.close();
			in.close();
			// Reconstruct class from XML
			ParamModel foreignParam = fromXML(text);
			return foreignParam;
		} catch (IOException e) {
			System.err.println("jist.base"
					+ "Error occured while reading parameter file:\n"
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reconstruct parameter from XML string.
	 * 
	 * @param str
	 *            the str
	 * @return the param model
	 */
	public static ParamModel fromXML(String str) {
		try {
			XStream stream = new XStream();
			Object o = stream.fromXML(str);
			if (o == null) {
				return null;
			}
			if (o instanceof ParamModel) {
				return (ParamModel) o;
			} else {
				return null;
			}
		} catch (StreamException e) {
			System.err.println("jist.base" + e.getMessage());
		}
		return null;
	}

	/**
	 * Import parameter into existing parameter as long as parameter is not more
	 * restrictive than this parameter. This method copies value of foreign
	 * parameter into this parameter.
	 * 
	 * @param foreign
	 *            foreign parameter
	 * @return import successful
	 */
	public boolean importParameter(ParamModel foreign) {
		ParamModel param = getParameter();
		// this parameter is less than or as restrictive as model parameter
		if ((foreign != null) && isCompatible(foreign)) {
			if (foreign.getValue() != null) {
				param.setValue(foreign.getValue());
			}
			return true;
		} else {
			System.err.println("jist.base" + "COULD NOT COMPARE "
					+ param.getClass() + ":" + param.getName() + ":"
					+ param.toString() + " " + param.isMandatory());
			System.err.println("jist.base" + "TO " + foreign.getClass() + ":"
					+ foreign.getName() + ":" + foreign.toString() + " "
					+ foreign.isMandatory());
			return false;
		}
	}

	/**
	 * Import parameter into existing parameter as long as parameter is not more
	 * restrictive than this parameter. This method copies value of foreign
	 * parameter into this parameter.
	 * 
	 * @param foreign
	 *            foreign parameter
	 * @return import successful
	 */
	public boolean isCompatible(ParamModel foreign) {
		ParamModel param = getParameter();
		// this parameter is less than or as restrictive as model parameter
		// System.out.println(getClass().getCanonicalName()+"\t"+"COMPARE "+param.getClass()+" "+param.getLabel()+"
		// "+foreign.getClass()+" "+foreign.getLabel()+"
		// "+foreign.compareTo(param));
		return (foreign.compareTo(param) <= 0);
	}

	/**
	 * Creates a new Param object.
	 * 
	 * @return the mutable tree node
	 */
	public MutableTreeNode createTreeNode() {
		return (getParameter().isHidden()) ? null : new DefaultMutableTreeNode(
				"<HTML><B><font color='gray'>" + getParameter().getLabel()
						+ ": </font></B><CODE>" + getParameter().toString()
						+ "</HTML></CODE>");
	}

	/**
	 * Write parameter to file in XML.
	 * 
	 * @param f
	 *            file
	 * @return write successful
	 */
	public boolean write(File f) {
		PrintWriter out;
		if (f == null) {
			System.out.println(getClass().getCanonicalName() + "\t"
					+ "ParamFactory: Null file!");
			return false;
		}
		// Do not save resources by default
		// if (!saveResources(f.getParent()))return false;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			String text = toXML();
			out.print(text);
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			System.err.println("jist.base" + e.getMessage());
			return false;
		}
	}

	/**
	 * Serialize parameter as XML.
	 * 
	 * @return string representation of class
	 */
	public synchronized String toXML() {
		XStream stream = new XStream();
		return stream.toXML(getParameter());
	}

	/**
	 * Write object to serialization stream.
	 * 
	 * @param out
	 *            the out
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(ObjectOutputStream out) throws IOException {
		out.writeObject(getParameter());
	}

	/**
	 * Get the parameter.
	 * 
	 * @return the parameter
	 */
	public abstract ParamModel getParameter();

	/**
	 * Load external resources specified in foreign parameter into this
	 * parameter.
	 * 
	 * @param foreignParam
	 *            foreign parameter
	 * @param monitor
	 *            TODO
	 * @return true, if load resources
	 */
	public boolean loadResources(ParamModel foreignParam,
			ProgressMonitor monitor) {
		getInputView().update();
		return true;
	}

	/**
	 * Get the current input view.
	 * 
	 * @return the input view
	 */
	public abstract ParamInputView getInputView();

	/**
	 * Decode name.
	 * 
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String decodeName(String name) {
		return name.replaceAll("&nbsp;", " ");
	}

	/**
	 * Decode value.
	 * 
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String decodeValue(String name) {
		// return
		// edu.jhu.ece.iacl.jist.utility.FileUtil.recoverUnsafeFilename(name);
		return (name != null) ? name.replaceAll("___", " ") : null;
	}

	/**
	 * Encode name.
	 * 
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String encodeName(String name) {
		// return
		// edu.jhu.ece.iacl.jist.utility.FileUtil.forceSafeFilename(name);
		return name.replaceAll(" ", "___");
	}

	/**
	 * Encode value.
	 * 
	 * @param obj
	 *            the obj
	 * @return the string
	 */
	public static String encodeValue(Object obj) {
		return ((obj == null) ? "null" : obj.toString().replaceAll(" ", "___"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof ParamFactory) {
			return this.equals(((ParamFactory) obj).getParameter());
		} else if (obj instanceof ParamModel) {
			if ((this.getParameter().getValue() == null)
					|| (((ParamModel) obj).getValue() == null)) {
				return true;
			}
			return (this.getParameter().getValue().equals(((ParamModel) obj)
					.getValue()));
		} else {
			return false;
		}
	}

	/**
	 * Get the current output view.
	 * 
	 * @return the output view
	 */
	public abstract ParamOutputView getOutputView();

	/**
	 * Save external resources to specified directory.
	 *
	 * @param dir directory
	 * @param saveSubDirectoryOverride the save sub directory override
	 * @return true, if save resources
	 */
	public boolean saveResources(File dir, boolean saveSubDirectoryOverride) {
		return true;
	}

	/**
	 * Set the input view used to enter the parameter value.
	 * 
	 * @param view
	 *            the view
	 */
	public void setInputView(ParamInputView view) {
		this.inputView = view;
	}

	/**
	 * Set the output view used to display the parameter value.
	 * 
	 * @param view
	 *            the view
	 */
	public void setOutputView(ParamOutputView view) {
		this.outputView = view;
	}
}
