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
package edu.jhu.ece.iacl.jist.utility;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class JistXMLUtil.
 */
public class JistXMLUtil {
	
	/**
	 * Xml read element.
	 *
	 * @param parent the parent
	 * @param tag the tag
	 * @return the element
	 */
	public static Element xmlReadElement(Element parent, String tag) {
		return xmlReadElement(parent, tag, false);
	}

	/**
	 * Xml read element.
	 *
	 * @param parent the parent
	 * @param tag the tag
	 * @param optional the optional
	 * @return the element
	 */
	public static Element xmlReadElement(Element parent, String tag,
			boolean optional) {
		if (parent == null) {
			return null;
		}
		Vector<Node> nl = new Vector<Node>();
		NodeList nl0 = parent.getChildNodes();
		for (int i = 0; i < nl0.getLength(); i++) {
			Node node = nl0.item(i);

			if (node.getNodeName().equalsIgnoreCase(tag)) {
				nl.add(node);
			}

		}

		if (nl.size() < 1) {
			if (!optional) {
				JistLogger.logOutput(JistLogger.WARNING,
						"Tag not found (using default): " + tag);
			}
			return null;
		}
		if (nl.size() > 1) {
			JistLogger.logOutput(JistLogger.WARNING,
					"Found multiple tags (using first): " + tag);
		}

		return (Element) nl.get(0);

	}

	/**
	 * Xml read tag.
	 *
	 * @param parent the parent
	 * @param tag the tag
	 * @return the string
	 */
	public static String xmlReadTag(Element parent, String tag) {
		return xmlReadTag(parent, tag, false);
	}

	/**
	 * Xml read tag.
	 *
	 * @param parent the parent
	 * @param tag the tag
	 * @param optional the optional
	 * @return the string
	 */
	public static String xmlReadTag(Element parent, String tag, boolean optional) {
		if (parent == null) {
			if (!optional) {
				JistLogger.logOutput(JistLogger.WARNING,
						"Tag not found (using default): " + tag);
			}
			return null;
		}
		Vector<Node> nl = new Vector<Node>();
		NodeList nl0 = parent.getChildNodes();
		if (nl0 == null) {
			if (!optional) {
				JistLogger.logOutput(JistLogger.WARNING,
						"Tag not found (using default): " + tag);
			}
			return null;
		}

		for (int i = 0; i < nl0.getLength(); i++) {
			Node node = nl0.item(i);

			if (node.getNodeName().equalsIgnoreCase(tag)) {
				nl.add(node);
			}

		}

		if (nl.size() < 1) {
			if (!optional) {
				JistLogger.logOutput(JistLogger.WARNING,
						"Tag not found (using default): " + tag);
			}
			return null;
		}
		if (nl.size() > 1) {
			JistLogger.logOutput(JistLogger.WARNING,
					"Found multiple tags (using first): " + tag);
		}
		if (nl.get(0).getFirstChild() != null) {
			return nl.get(0).getFirstChild().getNodeValue();
		} else {
			return null;
		}

	}

	/**
	 * Xml read element list.
	 *
	 * @param parent the parent
	 * @param tag the tag
	 * @return the vector
	 */
	public static Vector<Element> xmlReadElementList(Element parent, String tag) {
		Vector<Element> nl = new Vector<Element>();
		NodeList nl0 = parent.getChildNodes();
		for (int i = 0; i < nl0.getLength(); i++) {
			Node node = nl0.item(i);

			if (node.getNodeName().equalsIgnoreCase(tag)) {
				nl.add((Element) node);
			}

		}
		return nl;

	}

}
