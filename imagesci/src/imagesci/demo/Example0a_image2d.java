package imagesci.demo;

import java.awt.Dimension;
import java.io.File;
import java.net.URISyntaxException;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationImage2D;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

public class Example0a_image2d {
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
