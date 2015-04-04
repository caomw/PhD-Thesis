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
package edu.jhu.ece.iacl.jist.utility;

// TODO: Auto-generated Javadoc
/**
 * The Class VersionUtil.
 */
public class VersionUtil {
	
	/**
	 * Parses the revision number.
	 *
	 * @param cvsversion the cvsversion
	 * @return the string
	 */
	public static String parseRevisionNumber(String cvsversion) {
		return cvsversion.replace("Revision: ", "").replace("$", "")
				.replace(" ", "");
	}
}
