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
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamNumberCollectionFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Number collection stores a collection of numbers. The restrictions on the
 * numbers are set to be the same as the collection.
 * 
 * Note: The type of number in the collection is defined by the type of the
 * maximum range. By default this is a double.
 * 
 * As of 12/23/2008, the min/max are not updated until the module library is
 * completely rebuilt (new library directory).
 * 
 * @author Blake Lucas
 */
public class ParamNumberCollection extends ParamModel<List> implements
		ObjectCollection<Number> {

	/** The max. */
	protected Number max;
	/** The min. */
	protected Number min;

	/** The num params. */
	protected Vector<ParamNumber> numParams;

	/** The port index. */
	private Vector<Integer> portIndex = null;

	/**
	 * Construct double parameter with no restrictions on value.
	 */
	public ParamNumberCollection() {
		mandatory = true;
		numParams = new Vector<ParamNumber>();
		this.factory = new ParamNumberCollectionFactory(this);
		this.min = ParamNumber.MIN_DOUBLE_VALUE;
		this.max = ParamNumber.MAX_DOUBLE_VALUE;
		// System.out.println(getClass().getCanonicalName()+"\t"+"ParamNumberCollection(): "+max.getClass().getName());
	}

	/**
	 * Construct double parameter with restrictions on min and max value.
	 * 
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamNumberCollection(Number min, Number max) {
		this();
		this.min = min;
		this.max = max;
		// System.out.println(getClass().getCanonicalName()+"\t"+"ParamNumberCollection(min,max): "+max.getClass().getName());
	}

	/**
	 * Construct double parameter with no restrictions on value.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamNumberCollection(String name) {
		this(name, ParamNumber.MIN_DOUBLE_VALUE, ParamNumber.MAX_DOUBLE_VALUE);
	}

	/**
	 * Construct double parameter with restrictions on min and max value.
	 * 
	 * @param name
	 *            parameter name
	 * @param min
	 *            minimum value restriction
	 * @param max
	 *            maximum value restriction
	 */
	public ParamNumberCollection(String name, Number min, Number max) {
		this(min, max);
		setName(name);
		// System.out.println(getClass().getCanonicalName()+"\t"+"ParamNumberCollection(name,min,max): "+max.getClass().getName());
	}

	/**
	 * Set value.
	 *
	 * @param i index
	 * @param value value
	 * @return the param number
	 */
	public ParamNumber set(int i, Object value) {
		// System.out.println(getClass().getCanonicalName()+"\t"+"setobj: "+max.getClass().getName());
		while (i >= size()) {
			this.add((Number) null);
		}
		ParamNumber param;
		if (value instanceof ParamNumber) {
			numParams.add(param = (ParamNumber) value);
		} else {
			param = create(value);
			numParams.add(param);
		}
		return param;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		String[] args = arg.trim().split("[;]+");
		ArrayList<ParamNumber> newval = new ArrayList<ParamNumber>();
		for (int i = 0; i < args.length; i++) {
			newval.add(create(args[i]));
		}
		setValue(newval);
	}

	/**
	 * Add a new value to the collection.
	 * 
	 * @param value
	 *            the value
	 * @return the param number
	 */
	@Override
	public void add(Object value) {
		// System.out.println(getClass().getCanonicalName()+"\t"+"addobj: "+max.getClass().getName());
		ParamNumber param;
		if (value instanceof ParamNumber) {
			numParams.add(param = (ParamNumber) value);
		} else {
			param = create(value);
			numParams.add(param);
		}
		// return param;
	}

	/**
	 * Create a new ParamNumber with the same restrictions as the collection and
	 * the specified value.
	 * 
	 * @param value
	 *            the value
	 * @return the param number
	 */
	protected ParamNumber create(Object value) {
		// System.out.println(getClass().getCanonicalName()+"\t"+"create: "+max.getClass().getName());
		if (value instanceof Double) {
			return new ParamDouble((size() + 1) + ")", min.doubleValue(),
					max.doubleValue(), (Double) value);
		} else if (value instanceof Float) {
			return new ParamFloat((size() + 1) + ")", min.floatValue(),
					max.floatValue(), (Float) value);
		} else if (value instanceof Integer) {
			return new ParamInteger((size() + 1) + ")", min.intValue(),
					max.intValue(), (Integer) value);
		} else if (value instanceof Long) {
			return new ParamLong((size() + 1) + ")", min.longValue(),
					max.longValue(), (Long) value);
		} else if (value instanceof String) {
			return new ParamDouble((size() + 1) + ")", min.doubleValue(),
					max.doubleValue(), Double.parseDouble(value.toString()));
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#size()
	 */
	@Override
	public int size() {
		return numParams.size();
	}

	/**
	 * Set the file collection. This method accepts ArrayLists with any of the
	 * valid types of ParamNumber
	 * 
	 * @param value
	 *            parameter value
	 */
	@Override
	public void setValue(List value) {
		List list = value;
		numParams.clear();
		for (Object obj : list) {
			this.add(obj);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		if (numParams != null) {
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
		List<Number> pt = getValue();
		StringWriter sw = new StringWriter();
		for (int i = 0; i < pt.size(); i++) {
			if (i > 0) {
				sw.append(";");
			}
			sw.append(pt.get(i).toString());
		}
		return sw.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getValue()
	 */
	@Override
	public List<Number> getValue() {
		ArrayList<Number> list = new ArrayList<Number>();
		for (ParamNumber f : numParams) {
			if (f != null) {
				list.add(f.getValue());
			}
		}
		return list;
	}

	/**
	 * Add file to collection.
	 * 
	 * @param val
	 *            the val
	 */
	public void add(Number val) {
		// System.out.println(getClass().getCanonicalName()+"\t"+"add: "+max.getClass().getName());
		this.add((Object) val);
	}

	/**
	 * Remove all numbers from collection.
	 */
	@Override
	public void clear() {
		// System.out.println(getClass().getCanonicalName()+"\t"+"clear: "+max.getClass().getName());
		numParams.clear();
	}

	/**
	 * Clone object.
	 * 
	 * @return the param number collection
	 */
	@Override
	public ParamNumberCollection clone() {
		// System.out.println(getClass().getCanonicalName()+"\t"+"clone: "+max.getClass().getName());
		ParamNumberCollection param = new ParamNumberCollection();
		param.setName(this.getName());
		param.label = this.label;
		param.numParams = new Vector<ParamNumber>(numParams.size());
		for (ParamNumber p : numParams) {
			param.numParams.add(p.clone());
		}
		param.mandatory = mandatory;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		// System.out.println(getClass().getCanonicalName()+"\t"+"cloneB: "+param.max.getClass().getName());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Compare restriction of one file collection to another.
	 * 
	 * @param model
	 *            the model
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		// System.out.println(getClass().getCanonicalName()+"\t"+"compareTo: "+max.getClass().getName());
		return (model instanceof ParamNumberCollection) ? 0 : 1;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "semi-colon separated list of numbers. ";
	}

	/**
	 * Gets the max.
	 * 
	 * @return the max
	 */
	public Number getMax() {
		// //System.out.println(getClass().getCanonicalName()+"\t"+"getMax: "+max.getClass().getName());
		return max;
	}

	/**
	 * Gets the min.
	 * 
	 * @return the min
	 */
	public Number getMin() {
		return min;
	}

	/**
	 * Get list of parameter numbers.
	 * 
	 * @return the parameters
	 */
	public List<ParamNumber> getParameterList() {
		// System.out.println(getClass().getCanonicalName()+"\t"+"List: "+max.getClass().getName());
		return numParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#getValue(int)
	 */
	@Override
	public Number getValue(int i) {
		if (i < numParams.size()) {
			return numParams.get(i).getValue();
		} else {
			return null;
		}
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamNumberCollectionFactory(this);
	}

	/**
	 * Number field is mandatory.
	 * 
	 * @return true, if checks if is mandatory
	 */
	@Override
	public boolean isMandatory() {
		return mandatory;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#set(int, java.lang.Object)
	 */
	@Override
	public void set(int i, Number val) {
		// System.out.println(getClass().getCanonicalName()+"\t"+"set: "+max.getClass().getName());
		this.set(i, (Object) val);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#setCollection(int, edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection)
	 */
	@Override
	public void setCollection(int index, ObjectCollection src) {
		if (src.size() < 1) {
			return;
		}

		// Set the indicated index to the first element of the collection
		Object val = src.getValue(0);

		this.set(index, val);

		if (portIndex == null) {
			portIndex = new Vector<Integer>();
		}

	}

	/**
	 * Set the mandatory field. The default is true.
	 * 
	 * @param mandatory
	 *            the mandatory
	 */
	@Override
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * Sets the max.
	 * 
	 * @param max
	 *            the new max
	 */
	public void setMax(Number max) {
		// //System.out.println(getClass().getCanonicalName()+"\t"+"setMax: "+max.getClass().getName());
		this.max = max;
	}

	/**
	 * Sets the min.
	 * 
	 * @param min
	 *            the new min
	 */
	public void setMin(Number min) {
		this.min = min;
	}

	/**
	 * Get description of numbers.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		String text = "";
		for (ParamNumber file : numParams) {
			if (file != null) {
				text += file.toString() + " ";
			} else {
				text += "null" + " ";
			}
		}
		return text;
	}

	/**
	 * Validate that the numbers meet all restrictions.
	 * 
	 * @throws InvalidParameterException
	 *             parameter does not meet value restrictions
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (mandatory && (numParams.size() == 0)) {
			throw new InvalidParameterException(this);
		}
		for (ParamNumber fparam : numParams) {
			if (fparam == null) {
				throw new InvalidParameterException(this, "Null param number");
			}

			fparam.validate();
		}
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);

		min = Double.valueOf(JistXMLUtil.xmlReadTag(parent, "min"));
		max = Double.valueOf(JistXMLUtil.xmlReadTag(parent, "max"));

		numParams = new Vector<ParamNumber>();
		Vector<Element> nl = JistXMLUtil
				.xmlReadElementList(parent, "numParams");
		for (Element e : nl) {
			e = JistXMLUtil.xmlReadElement(e, "num");
			String classname = JistXMLUtil.xmlReadTag(e, "classname");
			try {
				ParamNumber p = (ParamNumber) Class.forName(classname)
						.newInstance();
				p.xmlDecodeParam(document, e);
				numParams.add(p);
			} catch (InstantiationException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			} catch (IllegalAccessException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			} catch (ClassNotFoundException ee) {
				// TODO Auto-generated catch block
				ee.printStackTrace();
			}
		}

		portIndex = new Vector<Integer>();
		nl = JistXMLUtil.xmlReadElementList(parent, "portIndex");
		for (Element e : nl) {

			String val = e.getFirstChild().getNodeValue();
			portIndex.add(Integer.valueOf(val));
		}

	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;

		em = document.createElement("min");
		em.appendChild(document.createTextNode(min + ""));
		parent.appendChild(em);

		em = document.createElement("max");
		em.appendChild(document.createTextNode(max + ""));
		parent.appendChild(em);

		em = document.createElement("numParams");
		boolean val = false;
		for (ParamNumber pm : numParams) {
			Element em2 = document.createElement("num");
			if (pm.xmlEncodeParam(document, em2)) {
				em.appendChild(em2);
				val = true;
			}
		}
		if (val) {
			parent.appendChild(em);
		}

		em = document.createElement("portIndex");
		val = false;
		if (portIndex != null) {
			for (Integer pi : portIndex) {
				Element em2 = document.createElement("port");
				em2.appendChild(document.createTextNode(pi.toString()));
				em.appendChild(em2);
				val = true;

			}
			if (val) {
				parent.appendChild(em);
			}
		}
		return true;
	}
}
