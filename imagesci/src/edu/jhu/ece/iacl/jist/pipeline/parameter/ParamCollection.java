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

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.jhu.ece.iacl.jist.pipeline.factory.ParamCollectionFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Parameter Collection.
 * 
 * @author Blake Lucas
 */
public class ParamCollection extends ParamModel<Vector<ParamModel>> implements
		ObjectCollection, JISTInternalParam {

	/** The Category. */
	protected String category;

	/** The children. */
	private Vector<ParamModel> children;
	
	/** The currently number of set and/or added components *. */
	private int connectionCount;

	/** The hash. */
	transient private Hashtable<String, ParamModel> hash = null;
	
	/** The Category. */
	protected String pkg = "Base";

	/** The port index. */
	private Vector<Integer> portIndex = null;

	/**
	 * Construct parameter collection.
	 */
	public ParamCollection() {
		connectionCount = 0;
		children = new Vector<ParamModel>();
		factory = new ParamCollectionFactory(this);
	}

	/**
	 * Construct parameter collection.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamCollection(String name) {
		this();
		setName(name);
	}

	/**
	 * Add object.
	 * 
	 * @param val
	 *            the val
	 */
	@Override
	public void add(Object val) {
		if (val instanceof ParamModel) {
			add((ParamModel) val);
		}
	}

	/**
	 * Add parameter to children.
	 * 
	 * @param model
	 *            the model
	 */
	public void add(ParamModel model) {
		if (model != null) {
			children.add(model);
			connectionCount++;
		} else {
			System.err.println(getClass().getCanonicalName() + "Collection "
					+ getName() + " added a null child parameter!");
		}
	}

	/**
	 * Get all children, including hidden ones.
	 * 
	 * @return descendants
	 */
	public Vector<ParamModel> getAllDescendants() {
		return getAllDescedants(true);
	}

	/**
	 * Get all visible descendants.
	 * 
	 * @return descendants
	 */
	public Vector<ParamModel> getAllVisibleDescendants() {
		return getAllDescedants(false);
	}

	/**
	 * Get all descendants.
	 * 
	 * @param acceptHidden
	 *            true indicates hidden children will be added to the list
	 * @return list of children parameters
	 */
	protected Vector<ParamModel> getAllDescedants(boolean acceptHidden) {
		Vector<ParamModel> all = new Vector<ParamModel>();

		for (ParamModel child : children) {
			if (acceptHidden || !child.isHidden()) {
				if (child instanceof ParamCollection) {
					all.addAll(((ParamCollection) child)
							.getAllDescedants(acceptHidden));
				} else {
					all.add(child);
				}
			}
		}
		return all;
	}

	/**
	 * Get all descendants by class.
	 * 
	 * @param cls
	 *            class
	 * @return all children that are from a particular class
	 */
	public LinkedList<ParamModel> getAllDescendantsByClass(Class cls) {
		LinkedList<ParamModel> list = new LinkedList<ParamModel>();
		getAllDescendantsByClass(cls, list);
		return list;
	}

	/**
	 * Get all descendants and append them to the list.
	 *
	 * @param cls class
	 * @param list list
	 * @return the all descendants by class
	 */
	protected void getAllDescendantsByClass(Class cls, List<ParamModel> list) {
		for (ParamModel child : children) {
			if (cls.isInstance(child)) {
				list.add(child);
			}
			if (child instanceof ParamCollection) {
				((ParamCollection) child).getAllDescendantsByClass(cls, list);
			}
		}
	}

	/**
	 * Get the parameter's children.
	 * 
	 * @return list of child parameters
	 */
	@Override
	public Vector<ParamModel> getValue() {
		return getChildren();
	}

	/**
	 * Get description of parameters.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		String str = "<HTML>";
		for (ParamModel param : getChildren()) {
			if (!param.getName().equals(this.getName())) {
				str += param.toString() + "<BR>\n";
			}
		}
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
		}
		str += "</HTML>";
		return str;
	}

	/**
	 * Validate the collection my validating all the children.
	 * 
	 * @throws InvalidParameterException
	 *             parameter does not meet value restrictions
	 */
	@Override
	public void validate() throws InvalidParameterException {
		for (ParamModel param : getChildren()) {
			if (!param.isHidden() && param.isMandatory()) {
				param.validate();
			}
		}
	}

	/**
	 * Get child parameters.
	 * 
	 * @return the children
	 */
	public Vector<ParamModel> getChildren() {
		return children;
	}

	/**
	 * Clean collection and its children.
	 */
	@Override
	public void clean() {
		for (ParamModel child : children) {
			child.clean();
		}
	}

	/**
	 * Clear children.
	 */
	@Override
	public void clear() {
		connectionCount = 0;
		children.clear();
		portIndex = null;
	}

	/**
	 * Clone collections.
	 * 
	 * @return the param collection
	 */
	@Override
	public ParamCollection clone() {
		ParamCollection param = new ParamCollection();
		param.setName(this.getName());
		param.label = this.label;
		param.setCategory(this.getCategory());
		for (ParamModel child : children) {
			param.add(child.clone());
		}
		param.connectionCount = connectionCount;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Compare the names of two collections to see if they are the same.
	 * 
	 * @param model
	 *            the model
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		if (model instanceof ParamCollection) {
			if (model.getName().equals(this.getName())) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#equals(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public boolean equals(ParamModel<Vector<ParamModel>> collection) {
		Vector<ParamModel> children = collection.getValue();
		if (children == null || this.children == null
				|| this.children.size() != children.size()) {
			return false;
		}
		int sz = children.size();
		for (int i = 0; i < sz; i++) {
			if (!this.children.get(i).equals(children.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Get hash that maps parameter names to parameters. This should only be
	 * created once.
	 * 
	 * @return the children hash
	 */
	public Hashtable<String, ParamModel> getChildrenHash() {
		if (hash == null) {
			hash = new Hashtable<String, ParamModel>();
			for (ParamModel mod : children) {
				if (hash.put(this.getName() + "_" + mod.getName(), mod) != null) {
					System.err.println(getClass().getCanonicalName()
							+ "Duplicate parameter name " + this.getName()
							+ "_" + mod.getName());
				}
			}
		}
		return hash;
	}

	/**
	 * Get hash that maps parameter names to parameters. This should only be
	 * created once.
	 * 
	 * @return the descendant children hash
	 */
	public Hashtable<String, ParamModel> getDescendantChildrenHash() {
		Hashtable<String, ParamModel> hash = new Hashtable<String, ParamModel>();
		for (ParamModel mod : children) {
			if (mod instanceof ParamCollection) {
				Hashtable<String, ParamModel> tmpHash = ((ParamCollection) mod)
						.getDescendantChildrenHash();
				for (String key : tmpHash.keySet()) {
					hash.put(key, tmpHash.get(key));
				}
			} else {
				if (hash.put(this.getName() + "_" + mod.getName(), mod) != null) {
					System.err.println(getClass().getCanonicalName()
							+ "Duplicate parameter name " + this.getName()
							+ "_" + mod.getName());
				}
			}
		}
		return hash;
	}

	/**
	 * Get first child that is of the specified class type.
	 * 
	 * @param cls
	 *            class
	 * @return parameter
	 */
	public ParamModel getFirstChildByClass(Class cls) {
		if (cls.isInstance(this)) {
			return this;
		} else {
			for (ParamModel child : children) {
				if (cls.isInstance(child)) {
					return child;
				}
				if (child instanceof ParamCollection) {
					ParamModel ret = ((ParamCollection) child)
							.getFirstChildByClass(cls);
					if (ret != null) {
						return ret;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get first child that has the specified label.
	 * 
	 * @param name
	 *            label
	 * @return parameter
	 */
	public ParamModel getFirstChildByLabel(String name) {
		if (this.getLabel().equals(name)) {
			return this;
		} else {
			for (ParamModel child : children) {
				if (child.getLabel().equals(name)) {
					return child;
				}
				if (child instanceof ParamCollection) {
					ParamModel ret = ((ParamCollection) child)
							.getFirstChildByLabel(name);
					if (ret != null) {
						return ret;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get first child that has the specified name.
	 * 
	 * @param name
	 *            child name
	 * @return parameter
	 */
	public ParamModel getFirstChildByName(String name) {
		if (this.getName().equals(name)) {
			return this;
		} else {
			for (ParamModel child : children) {
				if (child.getName().equals(name)) {
					return child;
				}
				if (child instanceof ParamCollection) {
					ParamModel ret = ((ParamCollection) child)
							.getFirstChildByName(name);
					if (ret != null) {
						return ret;
					}
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "JIST internal collection of parameters. REPORT if seen";
	}

	/**
	 * Get all children labels.
	 * 
	 * @return labels
	 */
	public ArrayList<String> getLabels() {
		ArrayList<String> strs = new ArrayList<String>(children.size());
		for (ParamModel child : children) {
			strs.add(child.getLabel());
		}
		return strs;
	}

	/**
	 * Gets the number of added connections.
	 *
	 * @return number of times add has been called
	 */
	public int getNumberOfAddedConnections() {
		return connectionCount;
	}

	/**
	 * Gets the package.
	 *
	 * @return the package
	 */
	public String getPackage() {
		return pkg;
	}

	/**
	 * Get child value.
	 * 
	 * @param i
	 *            the i
	 * @return the value
	 */
	@Override
	public ParamModel getValue(int i) {
		return children.get(i);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		throw new RuntimeException("INTERNAL: Not Serializable");
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		factory = new ParamCollectionFactory(this);
		connectible = true;
		if (children == null) {
			return;
		}
		for (ParamModel child : children) {
			child.init();
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		return null;
	}

	/**
	 * Add parameter to children.
	 * 
	 * @param model
	 *            the model
	 * @return true, if removes the
	 */
	public boolean remove(ParamModel model) {
		boolean ret = children.remove(model);
		this.getInputView().update();
		return ret;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#replacePath(java.io.File, java.io.File)
	 */
	@Override
	public void replacePath(File originalPath, File replacePath) {
		for (ParamModel child : children) {
			child.replacePath(originalPath, replacePath);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#set(int,
	 * java.lang.Object)
	 */
	@Override
	public void set(int i, Object val) {
		if (val instanceof ParamModel) {
			children.set(i, (ParamModel) val);
		}
	}

	/**
	 * Sets the category.
	 *
	 * @param category the new category
	 */
	public void setCategory(String category) {
		this.category = category;
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

	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setLoadAndSaveOnValidate(boolean)
	 */
	@Override
	public void setLoadAndSaveOnValidate(boolean flag) {
		loadAndSaveOnValidate = flag;
		for (ParamModel child : children) {
			child.setLoadAndSaveOnValidate(flag);
		}
	}

	/**
	 * Sets the package.
	 *
	 * @param pkg the new package
	 */
	public void setPackage(String pkg) {
		this.pkg = pkg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setValue(java.lang
	 * .Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	/**
	 * Set the collection of children parameters for this collection. The
	 * children must be specified as type Vector<ParamModel>.
	 */
	public void setValue(Vector<ParamModel> obj) {
		connectionCount = 1;
		children = obj;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		throw new RuntimeException("INTERNAL: Not Serializable");
	};

	/**
	 * Get size of collection.
	 * 
	 * @return the int
	 */
	@Override
	public int size() {
		return children.size();
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		connectionCount = Integer.valueOf(JistXMLUtil.xmlReadTag(parent,
				"connectionCount"));

		category = (JistXMLUtil.xmlReadTag(parent, "category"));

		pkg = (JistXMLUtil.xmlReadTag(parent, "pkg"));

		children = new Vector<ParamModel>();
		Element el = JistXMLUtil.xmlReadElement(parent, "children");
		if (el != null) {
			NodeList nl = el.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				if (e == null) {
					continue;
				}
				String classname = JistXMLUtil.xmlReadTag(e, "classname");
				try {
					ParamModel p = (ParamModel) Class.forName(classname)
							.newInstance();
					p.xmlDecodeParam(document, e);
					children.add(p);
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
		}

		portIndex = new Vector<Integer>();
		Vector<Element> nv = JistXMLUtil
				.xmlReadElementList(parent, "portIndex");
		for (Element e : nv) {
			String val = e.getFirstChild().getNodeValue();
			portIndex.add(Integer.valueOf(val));
		}

	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;

		em = document.createElement("connectionCount");
		em.appendChild(document.createTextNode(connectionCount + ""));
		parent.appendChild(em);

		em = document.createElement("category");
		em.appendChild(document.createTextNode(category + ""));
		parent.appendChild(em);

		em = document.createElement("pkg");
		em.appendChild(document.createTextNode(pkg + ""));
		parent.appendChild(em);

		em = document.createElement("children");
		boolean val = false;
		for (ParamModel pm : children) {
			Element em2 = document.createElement("child");
			if (pm.xmlEncodeParam(document, em2)) {
				em.appendChild(em2);
				val = true;
			}
		}
		if (val) {
			parent.appendChild(em);
		}
		if (portIndex != null) {
			em = document.createElement("portIndex");
			val = false;
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
		;

		return true;
	}

}
