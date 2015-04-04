/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.playground;

import java.util.BitSet;

// TODO: Auto-generated Javadoc
/**
 * The Class TopologyRule2DLookUpTable.
 */
public class TopologyPreservationRule2D extends TopologyRule2D {

	/** The Constant lut. */
	protected static final BitSet lut;

	/** The Constant lut4_8. */
	private static final byte[] lut4_8 = new byte[] { 123, -13, -5, -13, -69,
			51, -69, 51, -128, -13, -128, -13, 0, 51, 0, 51, -128, -13, -128,
			-13, -69, -52, -69, -52, -128, -13, -128, -13, -69, -52, -69, -52,
			-128, 0, -128, 0, -69, 51, -69, 51, 0, 0, 0, 0, 0, 51, 0, 51, -128,
			-13, -128, -13, -69, -52, -69, -52, -128, -13, -128, -13, -69, -52,
			-69, -52, 123, -13, -5, -13, -69, 51, -69, 51, -128, -13, -128,
			-13, 0, 51, 0, 51, -128, -13, -128, -13, -69, -52, -69, -52, -128,
			-13, -128, -13, -69, -52, -69, -52, -128, 0, -128, 0, -69, 51, -69,
			51, 0, 0, 0, 0, 0, 51, 0, 51, -128, -13, -128, -13, -69, -52, -69,
			-52, -128, -13, -128, -13, -69, -52, -69, -52 };

	static {
		lut = fromByteArray(lut4_8);
	}

	/** The cube background. */
	protected boolean[][] cubeBackground = new boolean[3][3];

	/** The cube foreground. */
	protected boolean[][] cubeForeground = new boolean[3][3];

	/**
	 * Instantiates a new topology rule2 d look up table.
	 * 
	 * @param rule
	 *            the rule
	 */
	public TopologyPreservationRule2D(Rule rule) {
		super(rule);
	}

	/**
	 * From byte array.
	 * 
	 * @param bytes
	 *            the bytes
	 * 
	 * @return the bit set
	 */
	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

}
