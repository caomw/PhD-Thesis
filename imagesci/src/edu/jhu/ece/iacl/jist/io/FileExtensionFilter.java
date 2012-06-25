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
package edu.jhu.ece.iacl.jist.io;

import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * File filter based on extensions.
 * 
 * @author Blake Lucas
 */
public class FileExtensionFilter extends FileFilter {

	/** The exts. */
	private Vector<String> exts;

	/** The preferred extension. */
	private int preferredExtension;

	/**
	 * Instantiates a new file extension filter.
	 */
	public FileExtensionFilter() {
		preferredExtension = 0;
		this.exts = new Vector<String>();
	}

	/**
	 * Instantiates a new file extension filter.
	 * 
	 * @param exts
	 *            the exts
	 */
	public FileExtensionFilter(String exts[]) {
		this();
		for (String ext : exts) {
			this.exts.add(ext);
		}
	}

	/**
	 * Instantiates a new file extension filter.
	 * 
	 * @param exts
	 *            the exts
	 */
	public FileExtensionFilter(Vector<String> exts) {
		this.exts = exts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		if (f == null) {
			return false;
		}
		if (exts.size() == 0) {
			return true;
		}
		if (f.isDirectory()) {
			return true;
		}
		String fileExt = FileReaderWriter.getFileExtension(f);
		for (String extension : exts) {
			if (extension.equalsIgnoreCase(fileExt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Accept.
	 * 
	 * @param foreignExts
	 *            the foreign exts
	 * 
	 * @return true, if successful
	 */
	public boolean accept(List<String> foreignExts) {
		if (exts.size() == 0) {
			return true;
		}
		for (String fext : foreignExts) {
			boolean test = false;
			for (String ext : exts) {

				if (fext.equalsIgnoreCase(ext)) {
					test = true;
					break;
				}
			}
			if (!test) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FileExtensionFilter) {
			Vector<String> exts2 = ((FileExtensionFilter) obj).getExtensions();
			if (exts.size() != exts2.size()) {
				return false;
			}
			for (int i = 0; i < exts.size(); i++) {
				if (exts.get(i) == null) {
					if (exts2.get(i) != null) {
						return false;
					}
				} else if (exts.get(i).equalsIgnoreCase(exts2.get(i))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		if (exts.size() == 0) {
			return "All Files";
		}
		String text = "";
		int count = 0;
		for (String extension : exts) {
			text += extension
					+ ((count < exts.size() - 1) ? (((++count) % 12 == 0) ? ",<BR>"
							: ", ")
							: "");
		}
		if (text.length() == 0) {
			return "All Files";
		}
		// trim last comma
		text = "<HTML>" + text + "</HTML>";
		return text;
	}

	/**
	 * Gets the extensions.
	 * 
	 * @return the extensions
	 */
	public Vector<String> getExtensions() {
		return exts;
	}

	/**
	 * Gets the preferred extension.
	 * 
	 * @return the preferred extension
	 */
	public String getPreferredExtension() {
		return (preferredExtension < exts.size() && preferredExtension >= 0) ? exts
				.get(preferredExtension) : null;
	}

	/**
	 * Checks if is compatible.
	 * 
	 * @param filter
	 *            the filter
	 * 
	 * @return true, if is compatible
	 */
	public boolean isCompatible(FileExtensionFilter filter) {
		// Wild card file, do not check compatibility until runtime
		if (filter == null) {
			return false;
		}
		if (filter.getExtensions() == null) {
			return true;
		}
		if (filter.getExtensions().size() == 0) {
			return true;
		}
		if (exts.size() == 0) {
			return true;
		}
		// See if this plug-in accepts the preferred extension
		String first = filter.getPreferredExtension();

		if (first != null) {
			for (String ext : exts) {
				if (ext.equalsIgnoreCase(first)) {
					return true;
				}
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Sets the extensions.
	 * 
	 * @param exts
	 *            the new extensions
	 */
	public void setExtensions(Vector<String> exts) {
		this.exts = exts;
	}

	/**
	 * Sets the preferred extension.
	 * 
	 * @param i
	 *            the new preferred extension
	 */
	public void setPreferredExtension(int i) {
		this.preferredExtension = i;
	}

	/**
	 * Sets the preferred extension.
	 * 
	 * @param ext
	 *            the new preferred extension
	 */
	public void setPreferredExtension(String ext) {
		preferredExtension = exts.indexOf(ext);
	}

	/**
	 * Xml decode param.
	 *
	 * @param document the document
	 * @param item the item
	 */
	public void xmlDecodeParam(Document document, Element item) {
		exts = new Vector<String>();
		// Element el=;
		String str = JistXMLUtil.xmlReadTag(item, "exts");
		if (str != null) {
			String[] validexts = str.split(" ");
			if (validexts != null) {
				for (String e : validexts) {
					exts.add(e);
				}
			}
		}

		preferredExtension = Integer.valueOf(JistXMLUtil.xmlReadTag(item,
				"preferredExtension"));

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
		// /** Algorithm authors. */
		// protected LinkedList<AlgorithmAuthor> authors;
		boolean val = false;

		em = document.createElement("exts");
		for (String c : exts) {
			em.appendChild(document.createTextNode(c + " "));
			val = true;
		}
		if (val) {
			parent.appendChild(em);
		}

		em = document.createElement("preferredExtension");
		em.appendChild(document.createTextNode(preferredExtension + ""));
		parent.appendChild(em);

		return true;
	}

}
