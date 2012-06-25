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
package org.imagesci.muscle;

import org.imagesci.springls.SpringlsActiveContour3D;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsResample.
 */
public class SpringlsResample extends SpringlsActiveContour3D {
	
	/** The constellation stability theshold. */
	protected float constellationStabilityTheshold = 0.1f;

	/**
	 * Instantiates a new springls resample.
	 */
	public SpringlsResample() {
		super();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.SpringlsActiveContour3D#resample()
	 */
	@Override
	public void resample() {
		System.out.println("Evolving Level Set for " + maxIterations
				+ " iterations");
		for (int t = 0; t < maxIterations; t++) {
			evolve.evolve();
		}
		System.out.println("Start re-sampling.");
		int startSpringls = commons.elements;
		int fillCount, contractCount;
		int iter = 0;
		// Re-sampling cycle
		do {
			fillCount = fillGaps.fillGaps();
			hash.updateSpatialHash();
			hash.updateNearestNeighbors();
			fillGaps.fillLabels();
			relax.relax();
			contractCount = contract.contract();

			System.out.println("Re-sample Iteration " + iter + ") Fill: "
					+ (100 * fillCount / (float) startSpringls)
					+ "% Contract: "
					+ (100 * contractCount / (float) startSpringls) + "%");

			expand.expand();
			hash.updateSpatialHash();
			hash.updateUnsignedLevelSet();
			iter++;
		} while ((fillCount > startSpringls * constellationStabilityTheshold || contractCount > startSpringls
				* constellationStabilityTheshold));
		for (int t = 0; t < maxIterations; t++) {
			evolve.evolve();
		}
	}

	/**
	 * Sets the constellation stability.
	 *
	 * @param stab the new constellation stability
	 */
	public void setConstellationStability(float stab) {
		this.constellationStabilityTheshold = stab;
	}

}