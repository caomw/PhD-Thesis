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

import imagesci.mogac.MOGAC3D;
import imagesci.mogac.WEMOGAC3D;
import imagesci.utility.PhantomMetasphere;
import imagesci.utility.RandomSphereCollection;

import java.awt.Dimension;
import java.io.IOException;

import javax.vecmath.Point3i;

import com.jogamp.opencl.CLDevice;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class Example2h_wemogac3d.
 */
public class Example2h_wemogac3d {
	
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
		RandomSphereCollection rando = new RandomSphereCollection(128, 128,
				128, 27, 15);
		PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(128,
				128, 128));
		metasphere.setNoiseLevel(0.1);
		metasphere.setFuzziness(0.5f);
		metasphere.setInvertImage(true);
		metasphere.solve();
		ImageDataFloat refImage = metasphere.getImage();

		ImageDataFloat initDistField = rando.getDistanceField();
		ImageDataInt initLabels = rando.getLabelImage();
		MOGAC3D activeContour = new WEMOGAC3D(refImage, CLDevice.Type.CPU);
		activeContour.setPressure(refImage, 0.5f);
		activeContour.setCurvatureWeight(1.0f);
		activeContour.setTargetPressure(0.5f);
		activeContour.setMaxIterations(620);
		activeContour.setClampSpeed(true);
		activeContour.setPreserveTopology(true);
		if (showGUI) {
			try {
				activeContour.init(initDistField, initLabels, false);

				VisualizationMOGAC3D visual = new VisualizationMOGAC3D(512,
						512, activeContour);
				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setPreferredSize(new Dimension(920, 650));
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
			activeContour.solve(initDistField, initLabels, true);
		}
	}

}
