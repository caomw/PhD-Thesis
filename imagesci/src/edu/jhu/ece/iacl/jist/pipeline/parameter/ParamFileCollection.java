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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.io.FileExtensionFilter;
import edu.jhu.ece.iacl.jist.io.FileReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileCollectionFactory;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile.DialogType;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * File collection stores a collection of files. The restrictions on the files
 * are set to be the same as the collection.
 * 
 * @author Blake Lucas
 */
public class ParamFileCollection extends ParamModel<List> implements
		ObjectCollection<File> {

	/** The dialog type. */
	protected DialogType dialogType = DialogType.DIRECTORY;
	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/** The file params. */
	protected Vector<ParamFile> fileParams;

	/** The port index. */
	private Vector<Integer> portIndex = null;

	/** The reader writer. */
	protected FileReaderWriter readerWriter;

	/**
	 * Default constructor.
	 */
	public ParamFileCollection() {
		mandatory = true;
		fileParams = new Vector<ParamFile>();
		this.factory = new ParamFileCollectionFactory(this);
	}

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            collection name
	 */
	public ParamFileCollection(String name) {
		this();
		this.setName(name);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            name
	 * @param filter
	 *            extension filter
	 */
	public ParamFileCollection(String name, FileExtensionFilter filter) {
		this();
		this.setName(name);
		this.setExtensionFilter(filter);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            name
	 * @param readerWriter
	 *            the reader writer
	 */
	public ParamFileCollection(String name, FileReaderWriter readerWriter) {
		this();
		this.setName(name);
		this.setReaderWriter(readerWriter);
	}

	/**
	 * Sets the.
	 *
	 * @param i the i
	 * @param value the value
	 * @return the param file
	 */
	public ParamFile set(int i, Object value) {
		while (i >= size()) {
			this.add((File) null);
		}
		ParamFile param;
		if (value instanceof ParamFile) {
			fileParams.set(i, param = (ParamFile) value);
		} else {
			param = create(value);
			fileParams.set(i, param);
		}
		return param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#size()
	 */
	@Override
	public int size() {
		return fileParams.size();
	}

	/**
	 * Add a new value to the collection.
	 * 
	 * @param value
	 *            the value
	 * @return the param file
	 */
	@Override
	public void add(Object value) {
		ParamFile param;
		if (value instanceof ParamFile) {
			fileParams.add(param = (ParamFile) value);
		} else {
			if (fileParams.size() > 0) {
				param = fileParams.get(fileParams.size() - 1);
				File file = param.getValue();
				// Volume is null, so we don't have to create a new parameter,
				// we can use the last one
				if (file == null) {
					if (value instanceof String) {
						param.setValue((String) value);
					} else if (value instanceof File) {
						param.setValue((File) value);
					}
				} else {
					param = create(value);
					fileParams.add(param);
				}
			} else {
				param = create(value);
				fileParams.add(param);
			}
		}
		// return param;
	}

	/**
	 * Create a new ParamFile with the same restrictions as the collection and
	 * the specified value.
	 * 
	 * @param value
	 *            the value
	 * @return the param file
	 */
	protected ParamFile create(Object value) {
		ParamFile param = new ParamFile(getName());
		param.setMandatory(mandatory);
		param.setExtensionFilter(this.extensionFilter);
		if (value instanceof String) {
			param.setValue((String) value);
		} else if (value instanceof File) {
			param.setValue((File) value);
		}
		return param;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		if (fileParams == null) {
			return null;
		}
		return getXMLValue();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		StringWriter sw = new StringWriter();
		List<File> val = getValue();

		try {
			for (int i = 0; i < val.size(); i++) {
				File f = val.get(i);
				if (i > 0) {
					sw.append(";");
				}
				sw.append(f.getCanonicalPath());
			}

			return sw.toString();
		} catch (IOException e) {
			throw new InvalidParameterValueException(this,
					"unable to realize canonical path");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getValue()
	 */
	@Override
	public List<File> getValue() {
		ArrayList<File> list = new ArrayList<File>();
		for (ParamFile f : fileParams) {
			if (f != null) {
				list.add(f.getValue());
			}
		}
		return list;
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
	 * Set the file collection. This method accepts ArrayLists with any of the
	 * valid types of ParamFile
	 * 
	 * @param value
	 *            parameter value
	 */
	@Override
	public void setValue(List value) {
		List list = value;
		clear();
		for (Object obj : list) {
			this.add(obj);
		}
	}

	/**
	 * Remove all files from collection.
	 */
	@Override
	public void clear() {
		fileParams.clear();
		portIndex = null;
	}

	/**
	 * Add file to collection.
	 * 
	 * @param val
	 *            the val
	 */
	public void add(File val) {
		this.add((Object) val);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#clean()
	 */
	@Override
	public void clean() {
		for (ParamFile f : fileParams) {
			f.clean();
		}
	}

	/**
	 * Clone object.
	 * 
	 * @return the param file collection
	 */
	@Override
	public ParamFileCollection clone() {
		ParamFileCollection param = new ParamFileCollection();
		param.setName(this.getName());
		param.label = this.label;
		param.fileParams = new Vector<ParamFile>(fileParams.size());
		for (ParamFile p : fileParams) {
			param.fileParams.add(p.clone());
		}

		param.mandatory = mandatory;
		param.readerWriter = readerWriter;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
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
		return (model instanceof ParamFileCollection) ? 0 : 1;
	}

	/**
	 * Get extension filter.
	 * 
	 * @return filter
	 */
	public FileExtensionFilter getExtensionFilter() {
		return this.extensionFilter;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "file collection: semi-colon delimited list";
	}

	/**
	 * Get list of parameter files.
	 * 
	 * @return the parameters
	 */
	public List<ParamFile> getParameters() {
		return fileParams;
	}

	/**
	 * Get File reader writer.
	 * 
	 * @return the reader writer
	 */
	public FileReaderWriter getReaderWriter() {
		return readerWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#getValue(int)
	 */
	@Override
	public File getValue(int i) {
		if (i < fileParams.size()) {
			return fileParams.get(i).getValue();
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
		factory = new ParamFileCollectionFactory(this);
	}

	/**
	 * File field is mandatory.
	 * 
	 * @return true, if checks if is mandatory
	 */
	@Override
	public boolean isMandatory() {
		return mandatory;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#replacePath(java.io.File, java.io.File)
	 */
	@Override
	public void replacePath(File originalPath, File replacePath) {
		for (ParamFile f : fileParams) {
			f.replacePath(originalPath, replacePath);
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ObjectCollection#set(int, java.lang.Object)
	 */
	@Override
	public void set(int i, File val) {
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

	}

	/**
	 * Set extension filter.
	 * 
	 * @param filter
	 *            filter
	 */
	public void setExtensionFilter(FileExtensionFilter filter) {
		this.extensionFilter = filter;
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
	 * Set extension filter. This adds an extra restriction to the file
	 * parameter
	 * 
	 * @param readerWriter
	 *            the reader writer
	 */
	public void setReaderWriter(FileReaderWriter readerWriter) {
		this.readerWriter = readerWriter;
		this.extensionFilter = readerWriter.getExtensionFilter();
	}

	/**
	 * Get description of volumes.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		String text = "<HTML><OL>";
		for (ParamFile file : fileParams) {
			text += "<LI>" + file.toString() + "</LI>";
		}
		text += "</OL></HTML>";
		return text;
	}

	/**
	 * Validate that the files meet all restrictions.
	 * 
	 * @throws InvalidParameterException
	 *             parameter does not meet value restrictions
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (mandatory && (fileParams.size() == 0)) {
			throw new InvalidParameterException(this);
		}
		for (ParamFile fparam : fileParams) {
			fparam.validate();
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		Vector<Element> nv;
		String rw = (JistXMLUtil.xmlReadTag(parent, "readerWriter"));
		try {
			if (rw == null || "null".equalsIgnoreCase(rw)) {
				readerWriter = null;
			} else {
				readerWriter = (FileReaderWriter) Class.forName(rw)
						.newInstance();
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		extensionFilter = new FileExtensionFilter();
		extensionFilter.xmlDecodeParam(document,
				JistXMLUtil.xmlReadElement(parent, "extensionFilter"));

		fileParams = new Vector<ParamFile>();
		Element eFp = JistXMLUtil.xmlReadElement(parent, "fileParams");
		if (eFp != null) {
			nv = JistXMLUtil.xmlReadElementList(eFp, "file");
			;
			for (Element e : nv) {
				String classname = JistXMLUtil.xmlReadTag(e, "classname");
				try {
					ParamFile p = (ParamFile) Class.forName(classname)
							.newInstance();
					p.xmlDecodeParam(document, e);
					fileParams.add(p);
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
		nv = JistXMLUtil.xmlReadElementList(parent, "portIndex");
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

		em = document.createElement("extensionFilter");
		if (extensionFilter.xmlEncodeParam(document, em)) {
			parent.appendChild(em);
		}

		em = document.createElement("readerWriter");
		if (readerWriter != null) {
			em.appendChild(document.createTextNode(readerWriter.getClass()
					.getCanonicalName()));
		} else {
			em.appendChild(document.createTextNode("null"));
		}
		parent.appendChild(em);

		em = document.createElement("fileParams");
		boolean val = false;
		for (ParamFile pm : fileParams) {
			Element em2 = document.createElement("file");
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
		return true;
	}

}
