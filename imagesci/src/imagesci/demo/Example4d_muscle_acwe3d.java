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
package imagesci.demo;

import imagesci.muscle.MuscleACWE3D;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.SurfaceVtkReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example4d_muscle_acwe3d.
 */
public class Example4d_muscle_acwe3d {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File flabel = new File(PlaceHolder.class.getResource(
					"ufo_labels.nii").toURI());
			File fdistfield = new File(PlaceHolder.class.getResource(
					"ufo_distfield.nii").toURI());
			File fimg = new File(PlaceHolder.class.getResource("metacube.nii")
					.toURI());
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat initDistfield = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdistfield));
			ImageDataFloat refImage = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fimg));
			MuscleACWE3D activeContour = new MuscleACWE3D();
			activeContour.setPressure(refImage, 1.0f);
			activeContour.setCurvatureWeight(0.1f);
			activeContour.setTargetPressure(0.5f);
			activeContour.setMaxIterations(300);
			activeContour.setReferenceImage(refImage);
			activeContour.setInitialLabelImage(initLabels);
			activeContour.setInitialDistanceFieldImage(initDistfield);
			if (showGUI) {
				try {
					activeContour.init(new double[] { 0, 2, 1 });
					VisualizationMUSCLE3D visual = new VisualizationMUSCLE3D(
							512, 512, activeContour);
					VisualizationApplication app = new VisualizationApplication(
							visual);
					app.setPreferredSize(new Dimension(920, 650));
					app.setShowToolBar(true);
					app.addListener(visual);
					app.runAndWait();

					SurfaceVtkReaderWriter
							.getInstance()
							.write(activeContour.getSpringlsSurface(),
									new File(
											"C:\\Users\\Blake\\Desktop\\macwe\\macwe.vtk"));
					visual.dispose();
					System.exit(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				activeContour.solve(new double[] { 0, 2, 1 });
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
