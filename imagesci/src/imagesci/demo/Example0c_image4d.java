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

import imagesci.utility.PhantomMetasphere;

import java.awt.Dimension;
import java.io.File;

import javax.vecmath.Point3i;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationImage2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Class Example0c_image4d.
 */
public class Example0c_image4d extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example0c_image4d()).launch(args);
	}

	@Override
	public String getDescription() {
		return "Displays a 4D image as a 3D vector field.";
	}

	@Override
	public String getName() {
		return "View 3D Vector Field";
	}

	@Override
	public void launch(File workingDirectory, String[] args) {
		ImageData vecfield = NIFTIReaderWriter.getInstance().read(
				new File(workingDirectory, "demons_vecfield.nii"));
		PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(128,
				128, 128));
		metasphere.setInvertImage(true);
		metasphere.solve();
		VisualizationImage2D vis = new VisualizationImage2D(600, 600);
		vis.addVectorField(vecfield);
		VisualizationApplication app = new VisualizationApplication(vis);
		app.setPreferredSize(new Dimension(1024, 768));
		app.runAndWait();
		System.exit(0);
	}
}
