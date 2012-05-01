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
package edu.jhu.ece.iacl.jist.io;

import java.io.File;
import java.util.Vector;

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceReaderWriter.
 */
public class SurfaceReaderWriter extends FileReaderWriter<EmbeddedSurface> {

	/** The Constant surfReaderWriters. */
	private static final SurfaceReaderWriter[] surfReaderWriters = new SurfaceReaderWriter[] {
			new SurfaceVtkReaderWriter(), new SurfaceDxReaderWriter(),
			new SurfaceSTLReaderWriter(), new SurfaceFreeSurferReaderWriter(),
			new SurfaceMipavReaderWriter(), new SurfaceBrainSuiteReaderWriter() };

	/** The extension filter. */
	protected FileExtensionFilter extensionFilter;

	/**
	 * Instantiates a new surface reader writer.
	 */
	public SurfaceReaderWriter() {
		super(new FileExtensionFilter());
		Vector<String> exts = new Vector<String>();
		for (SurfaceReaderWriter reader : surfReaderWriters) {
			exts.addAll(reader.extensionFilter.getExtensions());
		}
		extensionFilter.setExtensions(exts);
	}

	/** The Constant readerWriter. */
	protected static final SurfaceReaderWriter readerWriter = new SurfaceReaderWriter();

	/**
	 * Instantiates a new surface reader writer.
	 * 
	 * @param filter
	 *            the filter
	 */
	public SurfaceReaderWriter(FileExtensionFilter filter) {
		super(filter);
	}

	/**
	 * Gets the single instance of SurfaceReaderWriter.
	 * 
	 * @return single instance of SurfaceReaderWriter
	 */
	public static SurfaceReaderWriter getInstance() {
		return readerWriter;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#getExtensionFilter()
	 */
	@Override
	public FileExtensionFilter getExtensionFilter() {
		return extensionFilter;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#setExtensionFilter(edu.jhu.ece.iacl.jist.io.FileExtensionFilter)
	 */
	@Override
	public void setExtensionFilter(FileExtensionFilter extensionFilter) {
		this.extensionFilter = extensionFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObjectToDirectory(java
	 * .lang.Object, java.io.File)
	 */
	@Override
	protected File writeObjectToDirectory(EmbeddedSurface surf, File dir) {
		String name = surf.getName();
		if (name == null || name.length() == 0) {
			name = "surface";
		}
		File f = new File(dir, name + "."
				+ extensionFilter.getExtensions().firstElement());
		if ((f = writeObject(surf, f)) != null) {
			return f;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.ece.iacl.jist.io.FileReaderWriter#writeObject(java.lang.Object,
	 * java.io.File)
	 */
	@Override
	protected File writeObject(EmbeddedSurface obj, File f) {
		for (SurfaceReaderWriter writer : surfReaderWriters) {
			if (writer.accept(f)) {
				return writer.writeObject(obj, f);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.io.FileReaderWriter#readObject(java.io.File)
	 */
	@Override
	protected EmbeddedSurface readObject(File f) {
		for (SurfaceReaderWriter reader : surfReaderWriters) {
			if (reader.accept(f)) {
				return reader.readObject(f);
			}
		}
		return null;
	}
}
