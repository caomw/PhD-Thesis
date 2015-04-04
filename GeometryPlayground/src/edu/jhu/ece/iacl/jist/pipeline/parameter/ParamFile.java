/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 */
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.io.FileExtensionFilter;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamFileFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * File parameter used to store a file name with a particular file extension.
 * 
 * @author Blake Lucas
 */
public class ParamFile extends ParamModel<File> {

	/**
	 * The Enum DialogType.
	 */
	public enum DialogType {

		/** The DIRECTORY. */
		DIRECTORY,
		/** The FILE. */
		FILE
	};

	/** The dialog type. */
	protected DialogType dialogType;

	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/** The file. */
	protected File file;

	/** The URI. */
	protected URI uri;

	/**
	 * Instantiates a new param file.
	 */
	public ParamFile() {
		this("invalid", DialogType.FILE);
		setName("invalid");
		setValue("invalid");
	}

	/**
	 * Construct parameter with specified restrictions.
	 * 
	 * @param type
	 *            directory or file dialog
	 */
	public ParamFile(DialogType type) {
		this.dialogType = type;
		this.extensionFilter = new FileExtensionFilter();
		init();
	}

	/**
	 * Construct parameter with specified restrictions.
	 *
	 * @param name parameter name
	 * @param f the file
	 * @param type directory or file dialog
	 */
	public ParamFile(String name, File f, DialogType type) {
		this(name, type);
		setName(name);
		setValue(f);
	}

	/**
	 * Construct parameter for a mandatory file type.
	 * 
	 * @param name
	 *            parameter name
	 * @param filter
	 *            the filter
	 */
	public ParamFile(String name, FileExtensionFilter filter) {
		this(DialogType.FILE);
		setName(name);
		setExtensionFilter(filter);
	};

	/**
	 * Construct parameter with specified restrictions.
	 *
	 * @param name parameter name
	 * @param uri the uri
	 * @param type directory or file dialog
	 */
	public ParamFile(String name, URI uri, DialogType type) {
		this(name, type);
		setName(name);
		setValue(uri);
	}

	/**
	 * Construct parameter for a mandatory file type.
	 * 
	 * @param name
	 *            parameter name
	 */
	public ParamFile(String name) {
		this(DialogType.FILE);
		setName(name);
	}

	/**
	 * Construct parameter with specified restrictions.
	 * 
	 * @param name
	 *            parameter name
	 * @param type
	 *            directory or file dialog
	 */
	public ParamFile(String name, DialogType type) {
		this(type);
		setName(name);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		setValue(arg);
	}

	/**
	 * Set file location.
	 * 
	 * @param value
	 *            file location as string
	 */
	public void setValue(String value) {
		if (value == null || 0 == value.toString().compareToIgnoreCase("null")) {
			value = null;
		}
		if (value == null) {
			uri = null;
			file = null;
			return;
		}
		try {
			this.uri = new URI(value);
			this.file = new File(uri);
		} catch (URISyntaxException e) {
			this.file = new File(value);
			this.uri = file.toURI();
			// System.err.println(getClass().getCanonicalName()+"NOT A VALID URI "+value);
		} catch (IllegalArgumentException e) {
			this.file = new File(value);
			this.uri = file.toURI();
			// System.err.println(getClass().getCanonicalName()+"NOT A VALID URI "+value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.PipePort#init()
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamFileFactory(this);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#replacePath(java.io.File, java.io.File)
	 */
	@Override
	public void replacePath(File originalDir, File replaceDir) {
		File f = getValue();

		setValue(translatePath(f, originalDir, replaceDir));
	}

	/**
	 * Set the file location for this parameter. The file location can be
	 * specified as a File or a String.
	 * 
	 * @param value
	 *            parameter value
	 */
	@Override
	public void setValue(File value) {
		if (value != null) {
			if (0 == value.toString().compareToIgnoreCase("null")) {
				value = null;
			}
		}
		this.file = value;
		// this.uri = null;
		this.uri = (file != null) ? file.toURI() : null;
	}

	/**
	 * Sets the extension filter.
	 * 
	 * @param filter
	 *            the new extension filter
	 */
	public void setExtensionFilter(FileExtensionFilter filter) {
		this.extensionFilter = filter;
	}

	/**
	 * Set the uri location for this parameter. The uri location can be
	 * 
	 * @param value
	 *            parameter value
	 */
	public void setValue(URI value) {
		if (value == null || 0 == value.toString().compareToIgnoreCase("null")) {
			value = null;
		}
		this.uri = value;
		this.file = null;
	}

	/**
	 * Clone object.
	 * 
	 * @return the param file
	 */
	@Override
	public ParamFile clone() {
		ParamFile param = new ParamFile(this.getName());
		param.dialogType = dialogType;
		param.extensionFilter = extensionFilter;
		param.setValue(getValue());
		param.setName(this.getName());
		param.label = this.label;
		param.setHidden(this.isHidden());
		param.setMandatory(this.isMandatory());
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		if (file == null) {
			return null;
		}
		return getXMLValue();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		try {
			if (getValue() == null) {
				return null;
			}
			return getValue().getCanonicalPath();
		} catch (IOException e) {
			throw new InvalidParameterValueException(this,
					"unable to realize canonical path");
		}
	}

	/**
	 * Validate that the specified file exists and meets all restrictions.
	 * 
	 * @throws InvalidParameterException
	 *             parameter value does not meet value restrictions
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (!mandatory) {
			return;
		}
		file = getValue();
		if (((file == null) || ((!file.exists()) && mandatory))
				|| ((dialogType == DialogType.FILE)
						&& (extensionFilter != null) && !extensionFilter
						.accept(file))
				|| ((dialogType == DialogType.DIRECTORY) && !file.isDirectory())
				|| ((dialogType == DialogType.FILE) && !file.isFile())) {
			throw new InvalidParameterException(this);
		}
	}

	/**
	 * Get file location.
	 * 
	 * @return file location
	 */
	@Override
	public File getValue() {
		if (uri != null) {
			file = new File(uri);
		}
		return file;
	}

	/**
	 * Translate path.
	 *
	 * @param f the f
	 * @param originalDir the original dir
	 * @param replaceDir the replace dir
	 * @return the file
	 */
	public static File translatePath(File f, File originalDir, File replaceDir) {
		if (f == null) {
			return null;
		}
		if (replaceDir == null) {
			return f;
		}
		if (originalDir == null) {
			if (f.isFile()) {
				File newPath = new File(replaceDir, f.getName());
				System.out.println("jist.param" + "\t" + "Translated Path:"
						+ f.getAbsolutePath() + " -> " + newPath);
				return newPath;
			} else {
				return replaceDir;
			}
		}
		String orig = f.getAbsolutePath();
		String rel = replaceDir.getAbsolutePath();
		String tru = originalDir.getAbsolutePath();
		File newPath = f;
		if (orig.length() > rel.length()) {
			if (orig.substring(0, rel.length()).equalsIgnoreCase(rel)) {
				newPath = new File(tru, orig.substring(rel.length(),
						orig.length()));
				System.out.println("jist.param" + "\t" + "Translated Path:"
						+ orig + "->" + newPath);
			}
		}

		return newPath;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#clean()
	 */
	@Override
	public void clean() {
		uri = null;
		file = null;
	}

	/**
	 * Compare two parameters based on their dialog types, extensionFilters, and
	 * mandatory fields F *.
	 * 
	 * @param model
	 *            the model
	 * @return the int
	 */
	@Override
	public int compareTo(ParamModel model) {
		if (model instanceof ParamFile) {
			ParamFile f = (ParamFile) model;
			if (this.mandatory && !f.isMandatory()) {
				return 1;
			}
			if (f.getDialogType() == this.getDialogType()) {
				return 0;
			} else {
				return 1;
			}
		}
		return 1;
	}

	/**
	 * Get dialog type.
	 * 
	 * @return the dialog type
	 */
	public DialogType getDialogType() {
		return dialogType;
	}

	/**
	 * Gets the extension filter.
	 * 
	 * @return the extension filter
	 */
	public FileExtensionFilter getExtensionFilter() {
		return this.extensionFilter;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		switch (this.dialogType) {
		case DIRECTORY:
			return "directory";
		case FILE:
			return "file";
		}
		return "ERROR: invalid file type";
	}

	/**
	 * Get uri location.
	 * 
	 * @return file location
	 */
	public URI getURI() {
		if (uri == null) {
			if (file != null) {
				return file.toURI();
			} else {
				return null;
			}
		} else {
			return uri;
		}
	}

	/**
	 * Get description of parameter.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return (file != null) ? file.getAbsolutePath() : "None";
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element parent) {
		super.xmlDecodeParam(document, parent);
		dialogType = DialogType.valueOf(JistXMLUtil.xmlReadTag(parent,
				"dialogType"));
		file = new File(JistXMLUtil.xmlReadTag(parent, "file"));
		if (0 == file.toString().compareToIgnoreCase("null")) {
			file = null;
		}
		try {
			uri = new URI(JistXMLUtil.xmlReadTag(parent, "uri"));
			if (0 == uri.toString().compareToIgnoreCase("null")) {
				uri = null;
			}
		} catch (URISyntaxException e) {
			uri = null;
		}
		extensionFilter = new FileExtensionFilter();
		extensionFilter.xmlDecodeParam(document,
				JistXMLUtil.xmlReadElement(parent, "extensionFilter"));
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("dialogType");
		em.appendChild(document.createTextNode(dialogType + ""));
		parent.appendChild(em);

		em = document.createElement("file");
		em.appendChild(document.createTextNode(file + ""));
		parent.appendChild(em);

		em = document.createElement("uri");
		em.appendChild(document.createTextNode(uri + ""));
		parent.appendChild(em);

		em = document.createElement("extensionFilter");
		if (extensionFilter.xmlEncodeParam(document, em)) {
			parent.appendChild(em);
		}
		return true;
	}
}
