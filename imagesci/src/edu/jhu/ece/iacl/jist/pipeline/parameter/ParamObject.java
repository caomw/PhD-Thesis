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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.io.FileReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamObjectFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Parameter for generic object which uses specified reader/writer.
 * 
 * @param <T>
 *            *
 * @author Blake Lucas (bclucas@jhu.edu)
 */
public class ParamObject<T> extends ParamFile {

	/** The obj. */
	protected transient T obj;

	/** The reader writer. */
	protected FileReaderWriter<T> readerWriter;

	/** The valid. */
	private transient boolean valid = false;

	/**
	 * Instantiates a new param object.
	 */
	public ParamObject() {
		this("invalid");
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            dialog type
	 */
	public ParamObject(DialogType type) {
		super(type);
		init();
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamObject(String name) {
		super(name);
		init();
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 * @param type
	 *            dialog type
	 */
	public ParamObject(String name, DialogType type) {
		super(name, type);
		init();
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            parameter name
	 * @param readerWriter
	 *            reader/writer
	 */
	public ParamObject(String name, FileReaderWriter<T> readerWriter) {
		super(name);
		init();
		setReaderWriter(readerWriter);
	};

	/**
	 * Clone object.
	 * 
	 * @return the param object
	 */
	@Override
	public ParamObject clone() {
		ParamObject<T> param = new ParamObject<T>(this.getName());
		param.dialogType = dialogType;
		param.extensionFilter = extensionFilter;
		param.readerWriter = this.readerWriter;
		try {
			param.setValue(getURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/**
	 * Get object.
	 * 
	 * @return object
	 */
	public T getObject() {
		if ((obj == null) || !valid) {
			obj = ((getReaderWriter() != null) ? getReaderWriter().read(
					getValue()) : null);
			valid = true;
		}
		return obj;
	}

	/**
	 * Get reader and writer for object.
	 * 
	 * @return reader/writer
	 */
	public FileReaderWriter<T> getReaderWriter() {
		return readerWriter;
	}

	/**
	 * Initialize parameter.
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamObjectFactory(this);
	}

	/**
	 * Set filename to store object.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public void setFileName(String fileName) {
		getReaderWriter().setFileName(fileName);
	}

	/**
	 * Set object.
	 * 
	 * @param obj
	 *            object
	 */
	public void setObject(T obj) {
		this.obj = obj;
		valid = true;
	}

	/**
	 * Set reader writer. This adds an extra restriction to the file parameter
	 * 
	 * @param readerWriter
	 *            the reader writer
	 */
	public void setReaderWriter(FileReaderWriter<T> readerWriter) {
		this.readerWriter = readerWriter;
		this.extensionFilter = readerWriter.getExtensionFilter();
	}

	/**
	 * Set file to store object.
	 * 
	 * @param f
	 *            the f
	 */
	@Override
	public void setValue(File f) {
		super.setValue(f);
		valid = false;
	}

	/**
	 * Set filename as string.
	 * 
	 * @param s
	 *            the s
	 */
	@Override
	public void setValue(String s) {
		super.setValue(s);
		valid = false;
	}

	/**
	 * Get description of object.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		if ((getReaderWriter() != null)
				&& (getReaderWriter().getFileName() != null)) {
			return getReaderWriter().getFileName();
		} else if (this.obj != null) {
			return obj.toString();
		} else {
			return super.toString();
		}
	}

	/**
	 * Validate filename.
	 * 
	 * @throws InvalidParameterException
	 *             the invalid parameter exception
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (obj == null) {
			super.validate();
			// if(getObject()==null)throw new InvalidParameterException(this);
		}
	}

	/**
	 * Write stored object to file.
	 * 
	 * @param f
	 *            filename
	 * @return true if writer is successful
	 */
	public boolean writeObject(File f) {
		T resource = getObject();
		if ((resource != null) && (getReaderWriter() != null)) {
			if ((f = getReaderWriter().write(resource, f)) != null) {
				setValue(f);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		String rw = (JistXMLUtil.xmlReadTag(parent, "readerWriter"));
		try {
			readerWriter = (FileReaderWriter) Class.forName(rw).newInstance();
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
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("readerWriter");
		em.appendChild(document.createTextNode(readerWriter.getClass()
				.getCanonicalName() + ""));
		parent.appendChild(em);
		return true;
	}
}
