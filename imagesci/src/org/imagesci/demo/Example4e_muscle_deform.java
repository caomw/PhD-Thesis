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
package org.imagesci.demo;


import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.vecmath.Point3i;

import org.imagesci.muscle.CompressLevelSets;
import org.imagesci.muscle.MuscleApplyDisplacementField3D;
import org.imagesci.utility.PhantomMetasphere;
import org.imagesci.utility.PhantomSphere;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class Example4e_muscle_deform.
 */
public class Example4e_muscle_deform extends AbstractExample {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		(new Example4e_muscle_deform()).launch(args);
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Applies a deformation field to a MUSCLE.";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#getName()
	 */
	@Override
	public String getName() {
		return "Apply Deformation Field";
	}

	/* (non-Javadoc)
	 * @see org.imagesci.demo.AbstractExample#launch(java.io.File, java.lang.String[])
	 */
	@Override
	public void launch(File workingDirectory, String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		File fvecfield = new File(workingDirectory, "demons_vecfield.nii");

		ImageDataFloat vecfield = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fvecfield));

		PhantomSphere sphere = new PhantomSphere(new Point3i(128, 128, 128));
		sphere.setNoiseLevel(0.1);
		sphere.setRadius(0.8);
		sphere.solve();
		ImageDataFloat initDistfield = sphere.getLevelset();
		CompressLevelSets compress = new CompressLevelSets(
				new ImageDataFloat[] { initDistfield });

		PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(128,
				128, 128));
		metasphere.setNoiseLevel(0.1);
		metasphere.setFuzziness(0.5f);
		metasphere.setInvertImage(true);
		metasphere.solve();
		ImageDataFloat refImage = metasphere.getImage();

		MuscleApplyDisplacementField3D activeContour = new MuscleApplyDisplacementField3D();
		activeContour.setCurvatureWeight(0.1f);
		activeContour.setTargetPressure(0.5f);
		activeContour.setMaxIterations(130);
		activeContour.setReferenceImage(refImage);
		activeContour.setInitialLabelImage(compress.getLabelImage());
		activeContour.setInitialDistanceFieldImage(compress
				.getDistanceFieldImage());
		if (showGUI) {
			try {
				activeContour.init(vecfield);
				VisualizationMUSCLE3D visual = new VisualizationMUSCLE3D(600,
						600, activeContour);
				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setPreferredSize(new Dimension(1024, 768));
				app.setShowToolBar(true);
				app.addListener(visual);
				app.runAndWait();
				visual.dispose();
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			activeContour.solve(vecfield);
		}

	}

}
