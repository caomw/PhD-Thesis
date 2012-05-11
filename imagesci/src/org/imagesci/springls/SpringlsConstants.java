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
package org.imagesci.springls;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsConstants contains all tunable constants in Springls. Some
 * parameters are scaled at runtime by scaleUp and scaleDown.
 */
public class SpringlsConstants {

	/**
	 * The Enum ThresholdKernel.
	 */
	public enum ThresholdKernel {

		/** The LINEAR. */
		LINEAR,
		/** The TANH. */
		TANH,
		/** The ATAN. */
		ATAN
	};
	/**
	 * The Enum WeightingKernel.
	 */
	public enum WeightingKernel {

		/** The LINEAR. */
		LINEAR,
		/** The ATANH. */
		ATANH,
		/** The BELL. */
		BELL,
		/** The SPLINE. */
		SPLINE
	};

	/** The Constant extentThreshold. */
	public static double extentThreshold = 1.5;

	/** The Constant maxAngleTolerance. */
	public static final double maxAngleTolerance = Math.PI * 160 / 180.0;

	/** The Constant maxAreaThreshold. */
	public static double maxAreaThreshold = 1.5;

	/** The Constant maxNearestBins. */
	public static final int maxNearestBins = 64;

	/** The Constant maxNeighbors. */
	public static final int maxNeighbors = 8;

	/** The max tries. */
	public static int maxTries = 3;

	/** The Constant minAngleTolerance. */
	public static final double minAngleTolerance = Math.PI * 20 / 180.0;

	/** The Constant nearestNeighborDistance. */
	public static double nearestNeighborDistance = 0.6;

	/** The Constant particleRadius. */
	public static double particleRadius = 0.05;

	/** The Constant refreshInterval. */
	public static int refreshInterval = 5;

	/** The relax iterations. */
	public static int relaxIterations = 5;

	/** The relax time step. */
	public static float relaxTimeStep = 0.1f;

	/** The Constant restRadius. */
	public static double restRadius = 0.05;

	/** The scale up. */
	public static float scaleUp = 2.0f;
	/** The scale down. */
	public static float scaleDown = 1.0f / scaleUp;


	// Optimal sharpness value !
	/** The sharpness. */

	public static float sharpness = 5.0f;

	// Optimal spring value !
	/** The Constant springConstant. */
	public static double springConstant = 0.3;


	/** The threshold kernel. */
	public static ThresholdKernel thresholdKernel = ThresholdKernel.TANH;

	/** The Constant vExtent. */
	public static double vExtent = 0.5;

	/** The Constant THRESHOLD_FUNCTION. */
	public static final String[] THRESHOLD_FUNCTION = new String[] { "(t)",
			"(tanh(t))", "(atan(t))" };

	/** The Constant WEIGHT_FUNCTION. */
	public static final String[] WEIGHT_FUNCTION = new String[] {
			"(w)",
			"(atanh(w))",
			"((max(w-0.3333f,0.0f)==0)?(-9.0f*w*w*w+3.0f*w*w+w):(5.33333*w*w*w-9.3333*w*w+4.4444*w-0.30864))",
			"((max(w-0.6666f,0.0f)==0)?(-2.25f*w*w*w+1.5f*w*w+w):(288.0f*w*w*w-648.0f*w*w+480.0f*w-116.6666f))" };

	/** The weighting kernel. */
	public static WeightingKernel weightingKernel = WeightingKernel.LINEAR;

	/**
	 * Evaluate threshold.
	 * 
	 * @param t
	 *            the t
	 * @return the double
	 */
	public static double evaluateThreshold(double t) {
		switch (thresholdKernel) {
		case LINEAR:
			return t;
		case TANH:
			return Math.tanh(t);
		case ATAN:
			return Math.atan(t);
		default:
			return 0;
		}
	}

	/**
	 * Evaluate weight.
	 * 
	 * @param w
	 *            the w
	 * @return the double
	 */
	public static double evaluateWeight(double w) {
		switch (weightingKernel) {
		case LINEAR:
			return (w);
		case ATANH:
			return 0.5 * Math.log((1 + w) / (1 - w));
		case BELL:
			return ((Math.max(w - 0.3333f, 0) == 0) ? (-9.0f * w * w * w + 3.0f
					* w * w + w) : (5.33333 * w * w * w - 9.3333 * w * w
					+ 4.4444 * w - 0.30864));
		case SPLINE:
			return ((Math.max(w - 0.6666f, 0) == 0) ? (-2.25f * w * w * w
					+ 1.5f * w * w + w) : (288.0f * w * w * w - 648.0f * w * w
					+ 480.0f * w - 116.6666f));
		default:
			return 0;
		}
	}

}
