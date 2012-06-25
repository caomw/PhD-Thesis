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
package org.imagesci.springls;

import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsLog.
 */
public class SpringlsStatisticsLog {

	/** The log. */
	public static SpringlsStatisticsLog log = new SpringlsStatisticsLog();

	/** The column headers. */
	protected String[] columnHeaders = new String[] { "Springls", "Fill Count",
			"Contract Count", "Update Neighbors", "Advection", "Relaxation",
			"Re-sampling", "Level Set Update", "Total Time", "DICE",
			"Displacement", "Iteration" };

	/** The log entries. */
	protected LinkedList<Number[]> logEntries = new LinkedList<Number[]>();

	/**
	 * Gets the average advection time.
	 *
	 * @param data the data
	 * @return the average advection time
	 */
	public static double getAverageAdvectionTime(Object[][] data) {
		return 1E-9 * talley(data, 4) / data.length;
	}

	/**
	 * Gets the average level set time.
	 *
	 * @param data the data
	 * @return the average level set time
	 */
	public static double getAverageLevelSetTime(Object[][] data) {
		return 1E-9 * talley(data, 7) / data.length;
	}

	/**
	 * Gets the average relaxation time.
	 *
	 * @param data the data
	 * @return the average relaxation time
	 */
	public static double getAverageRelaxationTime(Object[][] data) {
		return 1E-9 * talley(data, 5) / data.length;
	}

	/**
	 * Gets the average resample time.
	 *
	 * @param data the data
	 * @return the average resample time
	 */
	public static double getAverageResampleTime(Object[][] data) {
		return 1E-9 * talley(data, 6) / data.length;
	}

	/**
	 * Gets the average spatial look up time.
	 *
	 * @param data the data
	 * @return the average spatial look up time
	 */
	public static double getAverageSpatialLookUpTime(Object[][] data) {
		return 1E-9 * talley(data, 3) / data.length;
	}

	/**
	 * Gets the average springls count.
	 *
	 * @param data the data
	 * @return the average springls count
	 */
	public static double getAverageSpringlsCount(Object[][] data) {
		return talley(data, 0) / data.length;
	}

	/**
	 * Gets the average time per iteration.
	 *
	 * @param data the data
	 * @return the average time per iteration
	 */
	public static double getAverageTimePerIteration(Object[][] data) {
		return 1E-9 * talley(data, 8) / data.length;
	}

	/**
	 * Gets the contract count.
	 *
	 * @param data the data
	 * @return the contract count
	 */
	public static int getContractCount(Object[][] data) {
		return (int) (talley(data, 2));
	}

	/**
	 * Gets the expand count.
	 *
	 * @param data the data
	 * @return the expand count
	 */
	public static int getExpandCount(Object[][] data) {
		return (int) (talley(data, 1));
	}

	/**
	 * Gets the total time per iteration.
	 *
	 * @param data the data
	 * @return the total time per iteration
	 */
	public static double getTotalTimePerIteration(Object[][] data) {
		return 1E-9 * talley(data, 8);
	}

	/**
	 * Talley.
	 *
	 * @param data the data
	 * @param index the index
	 * @return the double
	 */
	public static double talley(Object[][] data, int index) {
		double sum = 0;
		for (int i = 1; i < data.length; i++) {
			sum += ((Number) data[i][index]).doubleValue();
		}
		return sum;
	}

	/**
	 * Gets the max springls count.
	 *
	 * @param data the data
	 * @return the max springls count
	 */
	public static int getMaxSpringlsCount(Object[][] data) {
		return (int) max(data, 0);
	}

	/**
	 * Max.
	 *
	 * @param data the data
	 * @param index the index
	 * @return the double
	 */
	public static double max(Object[][] data, int index) {
		double max = -1E10;
		for (int i = 1; i < data.length; i++) {
			max = Math.max(max, ((Number) data[i][index]).doubleValue());
		}
		return max;
	}

	/**
	 * Gets the min springls count.
	 *
	 * @param data the data
	 * @return the min springls count
	 */
	public static int getMinSpringlsCount(Object[][] data) {
		return (int) min(data, 0);
	}

	/**
	 * Min.
	 *
	 * @param data the data
	 * @param index the index
	 * @return the double
	 */
	public static double min(Object[][] data, int index) {
		double min = 1E10;
		for (int i = 1; i < data.length; i++) {
			min = Math.min(min, ((Number) data[i][index]).doubleValue());
		}
		return min;
	}

	/**
	 * Gets the springls count.
	 *
	 * @param data the data
	 * @return the springls count
	 */
	public static int getSpringlsCount(Object[][] data) {
		return ((Number) data[data.length - 1][0]).intValue();
	}

	/**
	 * Adds the entry.
	 *
	 * @param springls the springls
	 * @param fillCount the fill count
	 * @param contractCount the contract count
	 * @param updateNeighborsTime the update neighbors time
	 * @param advectionTime the advection time
	 * @param relaxationTime the relaxation time
	 * @param resamplingTime the resampling time
	 * @param levelSetTime the level set time
	 * @param totalTime the total time
	 * @param dice the dice
	 * @param displacement the displacement
	 * @param iterations the iterations
	 */
	public void addEntry(long springls, long fillCount, long contractCount,
			long updateNeighborsTime, long advectionTime, long relaxationTime,
			long resamplingTime, long levelSetTime, long totalTime,
			double dice, double displacement, long iterations) {
		logEntries.add(new Number[] { springls, fillCount, contractCount,
				updateNeighborsTime, advectionTime, relaxationTime,
				resamplingTime, levelSetTime, totalTime, dice, displacement,
				iterations });
		/*
		 * System.out.println("[" + springls + "," + fillCount + "," +
		 * contractCount + "," + updateNeighborsTime + "," + advectionTime + ","
		 * + relaxationTime + "," + resamplingTime + "," + levelSetTime + "," +
		 * totalTime + "," + dice + "," + displacement + "," + iterations +
		 * "]");
		 */
	}

	/**
	 * Gets the log file.
	 * 
	 * @return the log file
	 */
	public Object[][] getLogFile() {
		Object[][] objectFiles = new Object[logEntries.size() + 1][columnHeaders.length];
		objectFiles[0] = columnHeaders;
		int index = 1;
		for (Number[] entry : logEntries) {
			objectFiles[index++] = entry;
		}
		return objectFiles;
	}
}
