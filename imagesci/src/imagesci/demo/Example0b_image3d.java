package imagesci.demo;

import java.awt.Dimension;
import java.io.File;
import java.net.URISyntaxException;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationImage3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

public class Example0b_image3d {
	public static final void main(String[] args) {
		try {
			File imgFile = (args.length > 0) ? new File(args[0]) : new File(
					PlaceHolder.class.getResource("metacube.nii").toURI());
			ImageData img = NIFTIReaderWriter.getInstance().read(imgFile);

			VisualizationImage3D vis = new VisualizationImage3D(512, 512);
			vis.addImage(img);
			VisualizationApplication app = new VisualizationApplication(vis);
			app.setPreferredSize(new Dimension(920, 600));
			app.runAndWait();
			System.exit(0);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
