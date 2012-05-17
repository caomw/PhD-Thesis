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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamMultiOptionFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * An parameter that allows the user to select more than one option.
 * 
 * @author Blake Lucas
 */
public class ParamMultiOption extends ParamModel<List<String>> {

	/** The options. */
	protected ArrayList<String> options;

	/** The selection. */
	protected ArrayList<Integer> selection;

	/**
	 * Instantiates a new param multi option.
	 */
	public ParamMultiOption() {
		this("invalid");
	}

	/**
	 * Construct a list of possible options.
	 * 
	 * @param options
	 *            the options
	 */
	public ParamMultiOption(ArrayList<String> options) {
		this.options = options;
		factory = new ParamMultiOptionFactory(this);
		selection = new ArrayList<Integer>();
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamMultiOption(String name) {
		this(name, new String[] {});
	}

	/**
	 * Construct a list of possible options.
	 * 
	 * @param name
	 *            parameter name
	 * @param options
	 *            the options
	 */
	public ParamMultiOption(String name, ArrayList<String> options) {
		this(options);
		setName(name);
	};

	/**
	 * Construct a list of possible options.
	 * 
	 * @param name
	 *            parameter name
	 * @param options
	 *            the options
	 */
	public ParamMultiOption(String name, String[] options) {
		this(options);
		setName(name);
	}

	/**
	 * Construct a list of possible options.
	 * 
	 * @param options
	 *            the options
	 */
	public ParamMultiOption(String[] options) {
		this.options = new ArrayList<String>();
		for (String option : options) {
			this.options.add(option);
		}
		factory = new ParamMultiOptionFactory(this);
		selection = new ArrayList<Integer>();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		if (selection != null) {
			return getXMLValue();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		StringWriter sw = new StringWriter();
		List<String> val = getValue();
		for (int i = 0; i < val.size(); i++) {
			String opt = val.get(i);
			if (i > 0) {
				sw.append(";");
			}
			sw.append(opt);
		}
		return sw.toString();
	}

	/**
	 * Get description.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		String txt = "{";
		for (Integer val : selection) {
			txt += options.get(val);
		}
		txt += "}";
		return getValue().toString();
	}

	/**
	 * Get selected option name.
	 * 
	 * @return selected option string
	 */
	@Override
	public List<String> getValue() {
		ArrayList<String> vals = new ArrayList<String>(selection.size());
		for (Integer index : selection) {
			vals.add(options.get(index));
		}
		return vals;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		String[] args = arg.trim().split("[;]+");
		setValue(Arrays.asList(args));
	}

	/**
	 * Set list of selected options.
	 * 
	 * @param value
	 *            the value
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	@Override
	public void setValue(List<String> value)
			throws InvalidParameterValueException {
		ArrayList<Integer> select = new ArrayList<Integer>(value.size());
		int index;
		for (String val : value) {
			index = options.indexOf(val);
			if (index < 0) {
				throw new InvalidParameterValueException(this, value);
			}
			select.add(index);
		}
		setSelection(select);
	}

	/**
	 * Set the selected option. This method will accept the string
	 * representation of the option or an integer index into the option array.
	 * 
	 * @param value
	 *            parameter value
	 * @throws InvalidParameterValueException
	 *             the invalid parameter value exception
	 */
	public void setSelection(ArrayList<Integer> value)
			throws InvalidParameterValueException {
		this.selection = value;
		for (Integer index : value) {
			if ((index < 0) || (index >= options.size())) {
				throw new InvalidParameterValueException(this, value);
			}
		}
	}

	/**
	 * Add string option.
	 * 
	 * @param opt
	 *            selected option
	 */
	public void add(String opt) {
		if (options == null) {
			this.options = new ArrayList<String>();
		}
		if (!options.contains(opt)) {
			options.add(opt);
			getInputView().update();
		}
	}

	/**
	 * Clone object.
	 * 
	 * @return the param multi option
	 */
	@Override
	public ParamMultiOption clone() {
		ParamMultiOption param = new ParamMultiOption(
				(ArrayList<String>) options.clone());
		param.selection = (ArrayList<Integer>) selection.clone();
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
		if (model instanceof ParamMultiOption) {
			ArrayList<String> modelOptions = ((ParamMultiOption) model)
					.getOptions();
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
		sw.append("option semi-colon delimited list:");
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
	 * Get list of possible options.
	 * 
	 * @return the options
	 */
	public ArrayList<String> getOptions() {
		return options;
	}

	/**
	 * Get selected option index.
	 * 
	 * @return the selection
	 */
	public ArrayList<Integer> getSelection() {
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.PipePort#init()
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamMultiOptionFactory(this);
	}

	/**
	 * Sets the options.
	 * 
	 * @param options
	 *            the new options
	 */
	public void setOptions(ArrayList<String> options) {
		selection = new ArrayList<Integer>();
		if (options == null) {
			this.options = new ArrayList<String>();
		} else {
			this.options = options;
		}
		getInputView().update();
	}

	/**
	 * Validate that the selected index corresponds to a possible option.
	 * 
	 * @throws InvalidParameterException
	 *             parameter does not meet value restriction
	 */
	@Override
	public void validate() throws InvalidParameterException {
		for (Integer index : selection) {
			if ((index < 0) || (index >= options.size())) {
				throw new InvalidParameterException(this);
			}
		}
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);

		options = new ArrayList<String>();

		Element tm = JistXMLUtil.xmlReadElement(parent, "options");
		if (tm != null) {
			Vector<Element> nl = JistXMLUtil.xmlReadElementList(tm, "option");
			for (Element el : nl) {
				options.add(el.getFirstChild().getNodeValue());
			}
		}

		selection = new ArrayList<Integer>();
		tm = JistXMLUtil.xmlReadElement(parent, "selections");
		if (tm != null) {
			Vector<Element> nl = JistXMLUtil
					.xmlReadElementList(tm, "selection");
			for (Element el : nl) {
				selection.add(Integer
						.valueOf(el.getFirstChild().getNodeValue()));
			}
		}

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

		em = document.createElement("selections");
		val = false;
		for (Integer pi : selection) {
			Element em2 = document.createElement("selection");
			em2.appendChild(document.createTextNode(pi.toString()));
			em.appendChild(em2);
			val = true;

		}
		if (val) {
			parent.appendChild(em);
		}

		return true;
	}
}
