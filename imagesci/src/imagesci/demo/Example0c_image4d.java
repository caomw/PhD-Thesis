package imagesci.demo;

import imagesci.utility.PhantomMetasphere;

import java.awt.Dimension;
import java.io.File;
import java.net.URISyntaxException;

import javax.vecmath.Point3i;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationImage2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

public class Example0c_image4d {
	public static void main(String[] args) {
		try {
			ImageData vecfield = NIFTIReaderWriter.getInstance().read(
					new File(PlaceHolder.class.getResource(
							"demons_vecfield.nii").toURI()));
			PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(
					128, 128, 128));
			metasphere.setInvertImage(true);
			metasphere.solve();
			VisualizationImage2D vis = new VisualizationImage2D(600, 600);
			vis.addVectorField(vecfield);
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
