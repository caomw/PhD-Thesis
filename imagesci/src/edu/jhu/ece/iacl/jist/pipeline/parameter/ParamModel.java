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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.tree.MutableTreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver;
import edu.jhu.ece.iacl.jist.pipeline.view.output.ParamOutputView;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Generic Parameter with a bridge to interface with methods in the parameter
 * getFactory(). The parameter factory is not serialized when exporting the
 * parameter to XML
 *
 * @param <T> the generic type
 * @author Blake Lucas
 */
public abstract class ParamModel<T> implements Comparable<ParamModel>,
		Cloneable {
	
	/** The cli tag. */
	protected String cliTag = null;

	/** Indicates if this port can be connected to. */
	transient protected boolean connectible = true;

	/** Description of parameter. */
	protected String description;

	/** The factory. */
	protected transient ParamFactory factory = null;

	/** The hidden. */
	protected boolean hidden = false;

	/** The label. */
	protected String label = null;

	/** Should the parameter be validated on load. True = default behavior. **/
	protected transient boolean loadAndSaveOnValidate = true;

	/** The mandatory. */
	protected boolean mandatory = true;

	/** The name. */
	private String name = "";

	/** The shortLabel --- "command line flag". */
	protected String shortLabel = null;

	/**
	 * Abstract xml decode param.
	 *
	 * @param document the document
	 * @param parent the parent
	 * @return the param model
	 */
	public static ParamModel abstractXmlDecodeParam(Document document,
			Element parent) {
		String className = JistXMLUtil.xmlReadTag(parent, "classname");
		ParamModel param;
		try {
			param = (ParamModel) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
		param.xmlDecodeParam(document, parent);
		return param;
	}

	/**
	 * Clean resources that maybe out-of-date with the system.
	 */
	public void clean() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.PipePort#clone()
	 */
	@Override
	public abstract ParamModel<T> clone();

	/**
	 * Compare specificity of parameters. Zero indicates both parameters have
	 * the same level of specificity. Negative one indicates this parameter is
	 * less specific than the foreign one. Positive one indicates this parameter
	 * is more specific than the foreign one.
	 * 
	 * @param mod
	 *            the mod
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel mod) {
		return (this.getClass().equals(mod.getClass())) ? 0 : 1;
	}

	/**
	 * Create tree node that represents this parameter.
	 * 
	 * @return the mutable tree node
	 */
	public final MutableTreeNode createTreeNode() {
		return getFactory().createTreeNode();
	}

	/**
	 * Release as much memory as possible from this object without setting the
	 * entire object to null. If this is an input parameter, the data should be
	 * able to be retrieved. By default, does nothing.
	 */
	public void dispose() {

	}

	/**
	 * Equals.
	 *
	 * @param model the model
	 * @return true, if successful
	 */
	public boolean equals(ParamModel<T> model) {
		T value1 = this.getValue();
		T value2 = model.getValue();
		if (value1 == null && value2 == null) {
			return true;
		}
		if (value1 == null || value2 == null) {
			return false;
		}
		if (value1.equals(value2)) {
			return true;
		} else {
			// System.err.println(getClass().getCanonicalName()+this.getLabel()+": "+value1+" NOT EQUAL TO "+model.getLabel()+": "+value2);
			return false;
		}
	}

	/**
	 * Gets the cli tag.
	 *
	 * @return the cli tag
	 */
	public String getCliTag() {
		return cliTag;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		if (description == null) {
			return getLabel();
		} else {
			return description;
		}
	}

	/**
	 * Get parameter factory.
	 * 
	 * @return parameter factory
	 */
	public ParamFactory getFactory() {
		if (factory == null) {
			init();
		}
		return factory;
	}

	/**
	 * Gets the human readable data type.
	 *
	 * @return the human readable data type
	 */
	public abstract String getHumanReadableDataType();

	/**
	 * Get the input view to specify the parameter value.
	 * 
	 * @return input view
	 */
	public final ParamInputView getInputView() {
		return getFactory().getInputView();
	}

	/**
	 * Get the text label for this parameter. If none is specified, the text
	 * name is used
	 * 
	 * @return label
	 */
	public String getLabel() {
		if (label == null) {
			if (name == null) {
				return "NULL-NO-NAME";
			}
			return name;
		} else {
			return label;
		}
	}

	/**
	 * Get the parameter's name, which should be unique.
	 * 
	 * @return name
	 */
	public String getName() {
		if ((name == null) && (label != null)) {
			return label;
		}
		return name;
	}

	/**
	 * Get the output view to specify the output parameter.
	 * 
	 * @return output view
	 */
	public final ParamOutputView getOutputView() {
		return getFactory().getOutputView();
	}

	/**
	 * Get the short text label for this parameter. Useful for command line
	 * flag.
	 * 
	 * @return suggested command line flag for parameter
	 */
	public String getShortLabel() {
		if (shortLabel == null) {
			return getLabel();
		} else {
			return shortLabel;
		}
	}

	/**
	 * Get the value stored in the parameter.
	 * 
	 * @return this the default value stored by the parameter. Although,
	 *         parameters could potentially store more than one type of value.
	 */
	public abstract T getValue();

	/**
	 * Get panel that represents this parameter, depending on whether it is an
	 * input or output parameter.
	 * 
	 * @return the view
	 */
	public JPanel getView() {
		return getFactory().getInputView();
	}

	/**
	 * Gets the xML value.
	 *
	 * @return the xML value
	 */
	abstract public String getXMLValue();

	/**
	 * Hide this parameter.
	 */
	public void hide() {
		hidden = true;
	}

	/**
	 * Import the contents of a foreign parameter into this parameter.
	 * 
	 * @param model
	 *            foreign parameter
	 * @return true if success
	 */
	public final boolean importParameter(ParamModel model) {
		return getFactory().importParameter(model);
	}

	/**
	 * Initialized data that could not be deserialized.
	 */
	public abstract void init();

	/**
	 * returns true if connector can connect to this parameter.
	 * 
	 * @return true, if checks if is connectible
	 */
	public boolean isConnectible() {
		return (connectible && !hidden);
	}

	/**
	 * Returns true if parameter is hidden.
	 * 
	 * @return true if hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * File field is mandatory.
	 * 
	 * @return true, if checks if is mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Load resources.
	 * 
	 * @param param
	 *            the param
	 * @return true, if successful
	 */
	public final boolean loadResources(ParamModel param) {
		return getFactory().loadResources(param, null);
	}

	/**
	 * Load external resources specified in foreign parameter.
	 * 
	 * @param param
	 *            foreign parameter
	 * @param monitor
	 *            TODO
	 * @return true if resources successfully loaded or not specified
	 */
	public final boolean loadResources(ParamModel param, ProgressMonitor monitor) {
		return getFactory().loadResources(param, monitor);
	}

	/**
	 * Probe default value.
	 *
	 * @return the string
	 */
	abstract public String probeDefaultValue();

	/**
	 * Read parameter information from file.
	 * 
	 * @param f
	 *            XML file
	 * @return true if parameter information imported correctly
	 */
	public final boolean read(File f) {
		return getFactory().read(f);
	}

	/**
	 * Read parameter information from string.
	 * 
	 * @param text
	 *            the text
	 * @param loadResources
	 *            the load resources
	 * @return true if parameter information imported correctly
	 */
	public final boolean read(String text, boolean loadResources) {
		return getFactory().read(text, loadResources);
	}

	/**
	 * Replace path in all file type objects with this path. This is used for
	 * JUnit testing
	 *
	 * @param originalDir the original dir
	 * @param replaceDir the replace dir
	 */
	public void replacePath(File originalDir, File replaceDir) {

	}

	/**
	 * Save external resources associated with the parameter to the specified
	 * directory.
	 *
	 * @param dir save directory
	 * @param saveSubDirectoryOverride the save sub directory override
	 * @return true if success
	 */
	public final boolean saveResources(File dir,
			boolean saveSubDirectoryOverride) {
		if (loadAndSaveOnValidate) {
			return getFactory().saveResources(dir, saveSubDirectoryOverride);
		} else {
			return true;
		}
	}

	/**
	 * Sets the cli tag.
	 *
	 * @param tag the new cli tag
	 */
	public void setCliTag(String tag) {
		cliTag = tag;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Use a different factory for manipulating this parameter model.
	 * 
	 * @param factory
	 *            the factory
	 */
	public void setFactory(ParamFactory factory) {
		this.factory = factory;
	}

	/**
	 * Set hidden status.
	 * 
	 * @param b
	 *            true if hidden
	 */
	public void setHidden(boolean b) {
		hidden = b;
	}

	/**
	 * Set the input view used to specify the parameter value.
	 * 
	 * @param view
	 *            the view
	 */
	public final void setInputView(ParamInputView view) {
		for (ParamViewObserver obs : getFactory().getInputView().getObservers()) {
			view.addObserver(obs);
		}
		getFactory().setInputView(view);

	}

	/**
	 * Set the label to be displayed in the input and output views.
	 * 
	 * @param label
	 *            the label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Allow memory saving operations by reducing the level of validation
	 * performed.
	 * 
	 * @param flag
	 *            - false = do not perform extra validation, true (default) =
	 *            perform standard valdiation
	 */
	public void setLoadAndSaveOnValidate(boolean flag) {
		loadAndSaveOnValidate = flag;
	}

	/**
	 * Set the mandatory field. The default is true.
	 * 
	 * @param mandatory
	 *            the mandatory
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * Set the name of the parameter, which will appear as the label if the
	 * label is not specified.
	 * 
	 * @param name
	 *            the name
	 */
	public void setName(String name) {
		// System.out.println(getClass().getCanonicalName()+"\t"+"***" +
		// "SETNAME:"+name);
		// try {
		// throw new IOException();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		this.name = name;
	}

	/**
	 * Set the output view used to display the parameter value.
	 * 
	 * @param view
	 *            the view
	 */
	public final void setOutputView(ParamOutputView view) {
		getFactory().setOutputView(view);
	}

	/**
	 * Set the shortLabel to be used as a command line switch.
	 * 
	 * @param shortLabel
	 *            the shortLabel
	 */
	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	};

	/**
	 * Set the parameter value. A parameter maybe able to support more than one
	 * type of value
	 *
	 * @param value the value
	 * @throws InvalidParameterValueException object type is not an acceptable value for this parameter
	 */
	public abstract void setValue(T value)
			throws InvalidParameterValueException;

	/**
	 * Sets the xML value.
	 *
	 * @param arg the new xML value
	 */
	abstract public void setXMLValue(String arg);

	/**
	 * Get description of parameter.
	 * 
	 * @return the string
	 */
	@Override
	public abstract String toString();

	/**
	 * Serialize parameter as XML.
	 * 
	 * @return the string
	 */
	public final String toXML() {
		return getFactory().toXML();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.PipePort#validate()
	 */
	/**
	 * Validate.
	 *
	 * @throws InvalidParameterException the invalid parameter exception
	 */
	public abstract void validate() throws InvalidParameterException;

	/**
	 * Write this parameter to an XML file.
	 * 
	 * @param f
	 *            file
	 * @return true if success
	 */
	public final boolean write(File f) {
		return getFactory().write(f);
	}

	/**
	 * Write parameter to byte serialized output stream.
	 * 
	 * @param out
	 *            the out
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final void write(ObjectOutputStream out) throws IOException {
		getFactory().write(out);
	}

	/**
	 * Xml decode param.
	 *
	 * @param document the document
	 * @param el the el
	 */
	public void xmlDecodeParam(Document document, Element el) {
		label = JistXMLUtil.xmlReadTag(el, "label");
		name = JistXMLUtil.xmlReadTag(el, "name");
		description = JistXMLUtil.xmlReadTag(el, "description");
		shortLabel = JistXMLUtil.xmlReadTag(el, "shortLabel");
		hidden = Boolean.valueOf(JistXMLUtil.xmlReadTag(el, "hidden"));
		mandatory = Boolean.valueOf(JistXMLUtil.xmlReadTag(el, "mandatory"));

	}

	/**
	 * Xml encode param.
	 *
	 * @param document the document
	 * @param parent the parent
	 * @return true, if successful
	 */
	public boolean xmlEncodeParam(Document document, Element parent) {

		Element em;

		/** Label to display for this parameter. */
		em = document.createElement("classname");
		em.appendChild(document.createTextNode(this.getClass()
				.getCanonicalName()));
		parent.appendChild(em);

		/** Label to display for this parameter. */
		em = document.createElement("label");
		em.appendChild(document.createTextNode(getLabel()));
		parent.appendChild(em);

		/** Unique name to identify this parameter. */
		em = document.createElement("name");
		em.appendChild(document.createTextNode(getName()));
		parent.appendChild(em);

		/** The shortLabel --- "command line flag" */
		em = document.createElement("shortLabel");
		em.appendChild(document.createTextNode(getShortLabel()));
		parent.appendChild(em);

		/** The hidden. */
		em = document.createElement("hidden");
		em.appendChild(document.createTextNode("" + hidden));
		parent.appendChild(em);

		/** The mandatory. */
		em = document.createElement("mandatory");
		em.appendChild(document.createTextNode("" + mandatory));
		parent.appendChild(em);

		/** Description of parameter. */
		em = document.createElement("description");
		em.appendChild(document.createTextNode("" + description));
		parent.appendChild(em);

		return true;

	}

}
