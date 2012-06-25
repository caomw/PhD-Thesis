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
package edu.jhu.ece.iacl.jist.utility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

// TODO: Auto-generated Javadoc
/**
 * The Class FileUtil.
 */
public class FileUtil {

	/** Saved STDERR filehandle. */
	private static PrintStream stderr = System.err;

	/** Saved STDOUT filehandle. */
	private static PrintStream stdout = System.out;

	/**
	 * Replace any potentially unsafe file system characters with "_".
	 * 
	 * @param filenameNoPath
	 *            the filename no path
	 * 
	 * @return the string
	 */
	public static String forceSafeFilename(String filenameNoPath) {
		filenameNoPath = recoverUnsafeFilename(filenameNoPath); // Allow for
																// symmetry -
																// prevent % ->
																// %25 -> %2525,
																// etc.
		try {
			filenameNoPath = filenameNoPath.replaceAll("\\*", "%47"); // * is
																		// safe
																		// for
																		// URLS,
																		// but
																		// not
																		// for
																		// file
																		// names
			return java.net.URLEncoder.encode(filenameNoPath, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("UTF-8 not supported!");
		}
		// return filenameNoPath.replaceAll("^[a-zA-Z0-9\\.-_]", "_");
	}

	/**
	 * Recover unsafe filename.
	 * 
	 * @param filenameNoPath
	 *            the filename no path
	 * 
	 * @return the string
	 */
	public static String recoverUnsafeFilename(String filenameNoPath) {
		try {
			return java.net.URLDecoder.decode(filenameNoPath, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("UTF-8 not supported!");
		}
	}

	/**
	 * Redirect stdout and stderr to files.
	 * 
	 * @param fout
	 *            file for stdout
	 * @param ferr
	 *            file for stderr
	 * 
	 * @return the prints the stream[]
	 */
	public static PrintStream[] redirect(File fout, File ferr) {
		if (stdout == null) {
			stderr = System.err;

			stdout = System.out;
		}

		PrintStream out = null;
		PrintStream err = null;
		// stdout.println("Starting redirect");
		try {
			out = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(fout)));
			err = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(ferr)));
			System.out.flush();
			System.setOut(out);
			System.err.flush();
			System.setErr(err);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// stdout.println("Finished redirect");
		return new PrintStream[] { out, err };
	}

	/**
	 * Restore redirect.
	 */
	public static void restoreRedirect() {
		// if(cachedOutErr!=null) {
		// stdout.println("Un-redirect");
		System.setOut(stdout);
		System.setErr(stderr);
		// stdout.println("Finished Un-redirect");
		stdout.flush();
		// }
	}

}
