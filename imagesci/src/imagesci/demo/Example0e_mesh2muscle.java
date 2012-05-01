package imagesci.demo;

import imagesci.mogac.WEMOGAC3D;
import imagesci.muscle.MeshToMUSCLE;
import imagesci.muscle.MuscleActiveContour3D;
import imagesci.springls.ActiveContour3D;
import imagesci.springls.MeshToSpringls;
import imagesci.springls.SpringlsActiveContour3D;
import imagesci.utility.SurfaceConnectedComponent;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;
import edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class Example0e_mesh2muscle {
	public static void main(String args[]) {
		try {
			File f = (args.length > 0) ? new File(args[0]) : new File(
					PlaceHolder.class.getResource("skeleton.vtk").toURI());

			EmbeddedSurface mesh = SurfaceReaderWriter.getInstance().read(f);
			mesh.scaleVertices(1.8f);
			int labelCount = SurfaceConnectedComponent.labelComponents(mesh);
			MeshToMUSCLE mtos = new MeshToMUSCLE(256, 256, 64);

			int order[] = new int[labelCount];
			for (int i = 0; i < labelCount; i++) {
				order[i] = i + 1;
			}
			mtos.solve(mesh, order, 0.1f, 0);
			ImageDataFloat levelSet = mtos.getDistanceField();
			ImageDataInt labelImage = mtos.getLabelImage();

			WEMOGAC3D simulator = new WEMOGAC3D(levelSet);
			simulator.setPressure(levelSet, 0.1f);
			simulator.setCurvatureWeight(0.5f);
			try {
				simulator.init(levelSet, labelImage, false);
				for (int k = 0; k < 5; k++)
					simulator.step();
				VisualizationMOGAC3D vis = new VisualizationMOGAC3D(600, 600,
						simulator);
				VisualizationApplication app = new VisualizationApplication(vis);
				app.setPreferredSize(new Dimension(1024, 768));
				app.setShowToolBar(false);
				app.addListener(vis);
				app.runAndWait();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
