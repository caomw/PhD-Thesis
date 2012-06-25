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
package edu.jhu.ece.iacl.jist.structures.image;

// TODO: Auto-generated Javadoc
/**
 * The Class test.
 */
public class test {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int R = 100, C = 100, S = 100;
		ImageData foo = new ImageDataInt(R, C, S, 1);

		System.out.println("test" + "\t" + "Non-native:");
		long tic = System.currentTimeMillis();
		for (int q = 0; q < 10; q++) {
			for (int i = 0; i < R; i++) {
				for (int j = 0; j < C; j++) {
					for (int k = 0; k < S; k++) {
						foo.get(i, j, k, 1).doubleValue();
					}
				}
			}
		}
		long toc = System.currentTimeMillis();
		System.out.println("test" + "\t" + "get().doubleValue(): "
				+ (toc - tic));
		tic = System.currentTimeMillis();
		for (int q = 0; q < 10; q++) {
			for (int i = 0; i < R; i++) {
				for (int j = 0; j < C; j++) {
					for (int k = 0; k < S; k++) {
						foo.getDouble(i, j, k, 1);
					}
				}
			}
		}
		toc = System.currentTimeMillis();
		System.out.println("test" + "\t" + "getDouble(): " + (toc - tic));

		foo = new ImageDataDouble(R, C, S, 1);

		System.out.println("test" + "\t" + "Native conversion:");
		tic = System.currentTimeMillis();
		for (int q = 0; q < 10; q++) {
			for (int i = 0; i < R; i++) {
				for (int j = 0; j < C; j++) {
					for (int k = 0; k < S; k++) {
						foo.get(i, j, k, 1).doubleValue();
					}
				}
			}
		}
		toc = System.currentTimeMillis();
		System.out.println("test" + "\t" + "get().doubleValue(): "
				+ (toc - tic));
		tic = System.currentTimeMillis();
		for (int q = 0; q < 10; q++) {
			for (int i = 0; i < R; i++) {
				for (int j = 0; j < C; j++) {
					for (int k = 0; k < S; k++) {
						foo.getDouble(i, j, k, 1);
					}
				}
			}
		}
		toc = System.currentTimeMillis();
		System.out.println("test" + "\t" + "getDouble(): " + (toc - tic));

	}

}
