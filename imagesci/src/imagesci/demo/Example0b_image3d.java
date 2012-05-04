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

import java.awt.Dimension;
import java.io.File;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationImage3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Class Example0b_image3d.
 */
public class Example0b_image3d extends AbstractExample {
	public static void main(String[] args) {
		(new Example0b_image3d()).launch(args);
	}

	@Override
	public String getDescription() {
		return "Displays a 3D image in tri-planar view.";
	}

	@Override
	public String getName() {
		return "View 3D Image";
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	@Override
	public void launch(File workingDirectory, String[] args) {
		File imgFile = new File(workingDirectory, "metacube.nii");
		ImageData img = NIFTIReaderWriter.getInstance().read(imgFile);

		VisualizationImage3D vis = new VisualizationImage3D(600, 600);
		vis.addImage(img);
		VisualizationApplication app = new VisualizationApplication(vis);
		app.setPreferredSize(new Dimension(1024, 768));
		app.runAndWait();
		System.exit(0);

	}
}
