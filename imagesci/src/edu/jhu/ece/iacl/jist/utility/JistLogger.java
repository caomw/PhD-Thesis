/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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

// TODO: Auto-generated Javadoc
/**
 * The Class JistLogger.
 */
public class JistLogger {

	/** The Constant CONFIG. */
	public final static int CONFIG = 3;
	
	/** The Constant FINE. */
	public final static int FINE = 4;
	
	/** The Constant FINER. */
	public final static int FINER = 5;
	
	/** The Constant FINEST. */
	public final static int FINEST = 6;
	
	/** The Constant INFO. */
	public final static int INFO = 2;
	
	/** The Constant INTENSIVE_I. */
	public final static int INTENSIVE_I = 7;
	
	/** The Constant INTENSIVE_II. */
	public final static int INTENSIVE_II = 8;
	
	/** The Constant INTENSIVE_III. */
	public final static int INTENSIVE_III = 9;
	
	/** The Constant PAD_MESSAGE_LENGTH. */
	public final static int PAD_MESSAGE_LENGTH = 100;
	
	/** The Constant SEVERE. */
	public final static int SEVERE = 0;
	
	/** The Constant WARNING. */
	public final static int WARNING = 1;

	/**
	 * Log error.
	 *
	 * @param debugLevel the debug level
	 * @param msg the msg
	 */
	public static void logError(int debugLevel, String msg) {
		System.err.println(msg);
	}

	/**
	 * Log flush.
	 */
	public static void logFlush() {
		System.out.flush();
		System.err.flush();
	}

	/**
	 * Log mipav registry.
	 */
	public static void logMIPAVRegistry() {

	}

	/**
	 * Log output.
	 *
	 * @param debugLevel the debug level
	 * @param msg the msg
	 */
	public static void logOutput(int debugLevel, String msg) {
		// System.out.println(msg);
	}

	/**
	 * Augment message.
	 *
	 * @param debugLevel the debug level
	 * @param msg the msg
	 * @return the string
	 */
	private static String augmentMessage(int debugLevel, String msg) {
		msg = msg.replaceAll("\t", "     ").trim();
		if (debugLevel > INFO) {
			if (msg.length() < PAD_MESSAGE_LENGTH) {
				msg = padRight(msg, PAD_MESSAGE_LENGTH - msg.length());
			}

			final Throwable throwable = new IllegalArgumentException("Blah");
			StackTraceElement[] trace = throwable.getStackTrace();
			if (trace.length > 2) {
				msg = msg + "\t(" + trace[2].getFileName() + ":"
						+ trace[2].getLineNumber() + ")";
			} else {
				msg = msg + "\t(" + trace[1].getFileName() + ":"
						+ trace[1].getLineNumber() + ")";
			}
		}
		if (debugLevel >= INTENSIVE_I) {
			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();

			long totalFreeMemory = (freeMemory + (maxMemory - allocatedMemory));
			long totalUsedMemory = maxMemory - totalFreeMemory;
			final long MB = (1 << 20);
			String memoryReport = (totalFreeMemory / MB) + "\t"
					+ (totalUsedMemory / MB) + "\t" + (allocatedMemory / MB)
					+ "\t" + (maxMemory / MB);
			msg += "\t" + memoryReport;
		}
		return msg;
	}

	/**
	 * Pad right.
	 *
	 * @param s the s
	 * @param n the n
	 * @return the string
	 */
	public static String padRight(String s, int n) {
		while (n > 0) {
			s += " ";
			n--;
		}
		return s;
		// return String.format("%1$-" + n + "s"+n, s);
	}

}
