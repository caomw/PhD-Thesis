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

import imagesci.springls.ActiveContour3D;
import imagesci.springls.MeshToSpringls;
import imagesci.springls.SpringlsActiveContour3D;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;
import edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class Example0d_mesh2springls.
 */
public class Example0d_mesh2springls extends AbstractExample {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		(new Example0d_mesh2springls()).launch(args);
	}

	@Override
	public String getDescription() {
		return "Converts a triangle mesh to a level set.";
	}

	@Override
	public String getName() {
		return "Convert Mesh to SpringLS";
	}

	@Override
	public void launch(File workingDirectory, String[] args) {
		try {
			File f = new File(workingDirectory, "cow.stl");
			EmbeddedSurface mesh = SurfaceReaderWriter.getInstance().read(f);
			MeshToSpringls mtos = new MeshToSpringls(256, 192, 128);
			ImageDataFloat levelSet = mtos.solve(mesh);

			SpringlsActiveContour3D simulator = new SpringlsActiveContour3D();
			simulator.setTask(ActiveContour3D.Task.ACTIVE_CONTOUR);
			simulator.setReferenceImage(levelSet);
			simulator.setInitialDistanceFieldImage(levelSet);
			simulator.setInitialSurface(mesh);
			try {
				simulator.init();
				VisualizationSpringlsActiveContour3D vis = new VisualizationSpringlsActiveContourVolume3D(
						600, 600, simulator);
				VisualizationApplication app = new VisualizationApplication(vis);
				app.setPreferredSize(new Dimension(1024, 768));
				app.setShowToolBar(false);
				app.addListener(vis);
				app.runAndWait();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
