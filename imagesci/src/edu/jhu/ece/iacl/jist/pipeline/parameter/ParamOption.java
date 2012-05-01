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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamOptionFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Option Parameter to select between different text options.
 * 
 * @author Blake Lucas
 */
public class ParamOption extends ParamModel<String> {

	/** The index. */
	protected int index;

	/** The options. */
	protected List<String> options;

	/**
	 * Instantiates a new param option.
	 */
	public ParamOption() {
		this("invalid", (String[]) null);
	}

	/**
	 * Construct a list of possible options.
	 * 
	 * @param options
	 *            the options
	 */
	public ParamOption(List<String> options) {
		this.options = options;
		factory = new ParamOptionFactory(this);
		index = 0;
	}

	/**
	 * Construct a list of possible options.
	 * 
	 * @param name
	 *            parameter name
	 * @param options
	 *            the options
	 */
	public ParamOption(String name, List<String> options) {
		this(options);
		setName(name);
	}

	/**
	 * Construct a list of possible options.
	 * 
	 * @param name
	 *            parameter name
	 * @param options
	 *            the options
	 */
	public ParamOption(String name, String[] options) {
		this(options);
		setName(name);
	}

	/**
	 * Construct a list of possible options.
	 * 
	 * @param options
	 *            the options
	 */
	public ParamOption(String[] options) {
		this.options = new ArrayList<String>();
		if (options != null) {
			for (String option : options) {
				this.options.add(option);
			}
		}
		factory = new ParamOptionFactory(this);
		index = 0;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		return getValue();
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		return getValue();
	}

	/**
	 * Get selected option name.
	 * 
	 * @return selected option string
	 */
	@Override
	public String getValue() {
		if (options.size() > 0 && index >= 0 && index < options.size()) {
			return new String(options.get(index));
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setValue(java.lang
	 * .Object)
	 */
	@Override
	public void setValue(String value) {
		if (value == null) {
			index = 0;
		} else {
			index = options.indexOf(value);
		}
		if (index < 0) {
			throw new InvalidParameterValueException(this, value);
		}
	}

	/**
	 * Add option to list.
	 * 
	 * @param opt
	 *            option name
	 */
	public void add(String opt) {
		if (options == null) {
			this.options = new ArrayList<String>();
		}
		if (!options.contains(opt)) {
			options.add(opt);
			if (getInputView() != null) {
				getInputView().update();
			}
		}
	}

	/**
	 * Clone object.
	 * 
	 * @return the param option
	 */
	@Override
	public ParamOption clone() {
		ParamOption param = new ParamOption(new ArrayList<String>(options));
		param.index = index;
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Compare the options of one parameter to another to determine which list
	 * of options is more restrictive.
	 * 
	 * @param model
	 *            the model
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		if (model instanceof ParamOption) {
			List<String> modelOptions = ((ParamOption) model).getOptions();
			return (int) Math.signum(modelOptions.size() - options.size());
		} else {
			return 1;
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		StringWriter sw = new StringWriter();
		sw.append("option:");
		for (int i = 0; i < options.size(); i++) {
			String opt = options.get(i);
			if (i > 0) {
				sw.append("|");
			}
			sw.append(opt);
		}
		return sw.toString();
	}

	/**
	 * Get selected option index.
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Get list of possible options.
	 * 
	 * @return the options
	 */
	public List<String> getOptions() {
		return options;
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamOptionFactory(this);
	}

	/**
	 * Set list of options.
	 * 
	 * @param options
	 *            options
	 */
	public void setOptions(List<String> options) {
		index = 0;
		if (options == null) {
			this.options = new ArrayList<String>();
		} else {
			this.options = options;
		}
		if (getInputView() != null) {
			getInputView().update();
		}
	}

	/**
	 * Set the selected option. This method will accept the string
	 * representation of the option or an integer index into the option array.
	 * 
	 * @param value
	 *            parameter value
	 */
	public void setValue(Integer value) {
		index = value;
		if ((index < 0) || ((index >= options.size()) && (options.size() > 0))) {
			throw new InvalidParameterValueException(this, value);
		}
	}

	/**
	 * Get description of selected option.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return (index >= 0 && index < options.size()) ? options.get(index)
				: null;
	}

	/**
	 * Validate that the selected index corresponds to a possible option.
	 * 
	 * @throws InvalidParameterException
	 *             parameter does not meet value restriction
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if ((index < 0) || (index >= options.size())) {
			throw new InvalidParameterException(this);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);

		options = new ArrayList<String>();
		Element el = JistXMLUtil.xmlReadElement(parent, "options");
		if (el != null) {
			for (Element opt : JistXMLUtil.xmlReadElementList(el, "option")) {
				options.add(opt.getFirstChild().getNodeValue());
			}
		}

		index = Integer.valueOf(JistXMLUtil.xmlReadTag(parent, "index"));

	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;

		em = document.createElement("options");
		boolean val = false;
		for (String str : options) {
			Element em2 = document.createElement("option");
			em2.appendChild(document.createTextNode(str));
			em.appendChild(em2);
			val = true;

		}
		if (val) {
			parent.appendChild(em);
		}

		em = document.createElement("index");
		em.appendChild(document.createTextNode(index + ""));
		parent.appendChild(em);
		return true;
	}
}
