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
package edu.jhu.ece.iacl.jist.structures.image;

// TODO: Auto-generated Javadoc
//Unsigned Byte
/**
 * The Class USbyte.
 */
public class USbyte {

	/** The signed. */
	byte signed;

	/** The unsigned. */
	int unsigned;

	/**
	 * Instantiates a new u sbyte.
	 * 
	 * @param b
	 *            the b
	 */
	public USbyte(byte b) {
		signed = b;
		unsigned = signed2unsigned(b);
	}

	/**
	 * Instantiates a new u sbyte.
	 * 
	 * @param i
	 *            the i
	 */
	public USbyte(int i) {
		unsigned = i;
		// try{
		signed = unsigned2signed(i);
		// }catch(Exception e){ e.printStackTrace(); }
	}

	/**
	 * Instantiates a new u sbyte.
	 */
	public USbyte() {
		signed = 0;
		unsigned = 0;
	}

	/**
	 * Signed2unsigned.
	 * 
	 * @param signed
	 *            the signed
	 * 
	 * @return the int
	 */
	public int signed2unsigned(byte signed) {
		if (signed < 0) {
			unsigned = 256 + signed;
		} else {
			unsigned = signed;
		}
		return unsigned;
	}

	/**
	 * Unsigned2signed.
	 * 
	 * @param uns
	 *            the uns
	 * 
	 * @return the byte
	 */
	public byte unsigned2signed(int uns) {// throws Exception{
		byte snd = 0;
		if (uns > 255 | uns < 0) {
			// throw new Exception("Unsigned out of bounds: 0<uns<255");
			System.out.println(getClass().getCanonicalName() + "\t"
					+ "Unsigned out of bounds: 0<uns<255 ... snd = 0");
			return snd;
		}
		if (uns <= 127) {
			snd = (byte) uns;
		} else {
			snd = (byte) (uns - 256);
		}

		return snd;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		// byte s = -5;
		// USbyte usb = new USbyte(s);
		// System.out.println(getClass().getCanonicalName()+"\t"+"unsigned is "
		// + usb.unsigned);
		// try{
		// byte ns = usb.unsigned2signed(128);
		// System.out.println(getClass().getCanonicalName()+"\t"+"new signed byte is: "
		// +ns);
		// }catch(Exception e){ e.printStackTrace(); }

		int a = 255;
		USbyte usb = new USbyte(a);
		System.out.println("USByte" + "\t" + usb.signed);

	}

}
