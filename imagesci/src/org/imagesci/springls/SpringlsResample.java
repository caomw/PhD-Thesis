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
package org.imagesci.springls;


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