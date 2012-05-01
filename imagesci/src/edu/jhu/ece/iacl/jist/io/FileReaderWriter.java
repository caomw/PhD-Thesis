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

// TODO: Auto-generated Javadoc
/**
 * FileReaderWriter is the abstract class for reading/writing various files
 * 
 * Extensions exist for various object types (view type hierarchy)
 * 
 * Every extension of FileReaderWriter should overwrite the following protected
 * methods (1) readObject(File) (2) writeObject(T, File) (3)
 * writeObjectToDirectory(T, File).
 * 
 * @param <T>
 *            The object type - this will be specified in extensions
 */
public abstract class FileReaderWriter<T> {

	/** The file name. */
	transient protected String fileName = "unknown";

	/**
	 * Instantiates a new file reader writer.
	 */
	public FileReaderWriter() {

	}

	/**
	 * Instantiates a new file reader writer.
	 * 
	 * @param filter
	 *            the filter
	 */
	public FileReaderWriter(FileExtensionFilter filter) {
		setExtensionFilter(filter);
	}

	/**
	 * Get file extension for file.
	 * 
	 * @param file
	 *            file
	 * 
	 * @return file extension
	 */
	public static String getFileExtension(File file) {
		if (file == null) {
			return null;
		} else {
			return getFileExtension(file.getName());
		}
	}

	/**
	 * Get file extension for file string.
	 * 
	 * @param name
	 *            file name
	 * 
	 * @return file extension
	 */
	public static String getFileExtension(String name) {
		int index = name.lastIndexOf(".");
		if (index >= 0) {
			return name.substring(index + 1, name.length());
		} else {
			return "";
		}
	}

	/**
	 * Get image name based on file name.
	 * 
	 * @param file
	 *            file
	 * 
	 * @return image name
	 */
	public static String getFileName(File file) {
		return getFileName(file.getName());
	}

	/**
	 * Get image name from file name.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return image name
	 */
	public static String getFileName(String name) {
		int index = name.lastIndexOf(".");
		if (index >= 0) {
			return name.substring(0, index);
		} else {
			return name;
		}
	}

	/**
	 * Accept.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return true, if successful
	 */
	public boolean accept(File f) {
		return getExtensionFilter().accept(f);
	}

	/**
	 * Gets the extension filter.
	 * 
	 * @return the extension filter
	 */
	abstract public FileExtensionFilter getExtensionFilter();

	/**
	 * Gets the file name.
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * The public method for reading files. Protected method readObject(File) is
	 * called from here Extensions of FileReaderWriter should overwrite
	 * readObject
	 * 
	 * @param f
	 *            The file being read
	 * 
	 * @return A pointer to the object in memory
	 */
	public T read(File f) {
		if (!getExtensionFilter().accept(f) || !f.exists()) {
			return null;
		}
		MipavController.setDefaultWorkingDirectory(f.getParentFile());
		try {
			return readObject(f);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the extension filter.
	 * 
	 * @param extensionFilter
	 *            the new extension filter
	 */
	abstract public void setExtensionFilter(FileExtensionFilter extensionFilter);

	/**
	 * Sets the file name.
	 * 
	 * @param fileName
	 *            the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * This is the method users should call to write a file to disk.
	 * 
	 * @param obj
	 *            The object being written
	 * @param f
	 *            The file/directory the object will be written to
	 * 
	 * @return the file
	 */
	public File write(T obj, File f) {
		// System.out.println("jist.io"+"\t"+"FileReaderWriter: write file:"+f.getName());

		if (f.isDirectory()) {
			MipavController.setDefaultWorkingDirectory(f);
			File ret = writeObjectToDirectory(obj, f);
			fileName = (ret != null) ? ret.getName() : null;
			if (null == ret) {
				System.out.println("jist.io" + "\t"
						+ "FileReaderWriter: Failed directory write.");
			}
			return ret;
		} else {
			MipavController.setDefaultWorkingDirectory(f.getParentFile());
			fileName = f.getName();
			if (f.exists()) {
				f.delete();
			}
			f = writeObject(obj, f);
			if (f != null) {
				return f;
			} else {
				System.out.println("jist.io" + "\t"
						+ "FileReaderWriter: Failed object write.");
				return null;
			}
		}

	}

	/**
	 * Read object.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the t
	 */
	protected abstract T readObject(File f);

	/**
	 * writeObject should be overwritten by all extending classes This method is
	 * protected because users should call write() From there, other
	 * reader/writer methods (including this one) are called as needed.
	 * 
	 * @param obj
	 *            The object being written
	 * @param f
	 *            Where the file should be written
	 * 
	 * @return File actually writen if successful, null otherwise
	 */
	protected abstract File writeObject(T obj, File f);

	/**
	 * Write object to directory.
	 * 
	 * @param obj
	 *            the obj
	 * @param f
	 *            the f
	 * 
	 * @return the file
	 */
	protected File writeObjectToDirectory(T obj, File f) {
		System.out.println("jist.io" + "\t"
				+ "FileReaderWriter: WriteObjectToDirectory");
		String ext = getExtensionFilter().getPreferredExtension();
		File newFile;
		if (ext == null) {
			newFile = new File(f,
					edu.jhu.ece.iacl.jist.utility.FileUtil
							.forceSafeFilename(getFileName()));
		} else {
			newFile = new File(f,
					edu.jhu.ece.iacl.jist.utility.FileUtil
							.forceSafeFilename(getFileName()) + "." + ext);
		}
		if (newFile.exists()) {
			newFile.delete();
		}
		System.out.println("jist.io.FileReaderWriter : newFile" + "\t"
				+ newFile);
		return (writeObject(obj, newFile)); // ?newFile:null;
	}
}
