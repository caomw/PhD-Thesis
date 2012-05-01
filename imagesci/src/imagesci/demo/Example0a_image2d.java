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
import java.net.URISyntaxException;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationImage2D;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Class Example0a_image2d.
 */
public class Example0a_image2d {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			File imgFile = (args.length > 0) ? new File(args[0]) : new File(
					PlaceHolder.class.getResource("kinect_rgb.png").toURI());
			ImageData img = PImageReaderWriter.convertToRGB(PImageReaderWriter
					.getInstance().read(imgFile));
			VisualizationImage2D vis = new VisualizationImage2D(600, 600);
			vis.addImage(img);
			VisualizationApplication app = new VisualizationApplication(vis);
			app.setPreferredSize(new Dimension(1024, 768));
			app.runAndWait();
			System.exit(0);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
