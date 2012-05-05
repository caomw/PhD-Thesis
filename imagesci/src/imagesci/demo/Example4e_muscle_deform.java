package imagesci.demo;

import imagesci.muscle.CompressLevelSets;
import imagesci.muscle.MuscleApplyDisplacementField3D;
import imagesci.utility.PhantomMetasphere;
import imagesci.utility.PhantomSphere;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.vecmath.Point3i;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMUSCLE3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class Example4e_muscle_deform extends AbstractExample {
	public static void main(String[] args) {
		(new Example4e_muscle_deform()).launch(args);
	}

	@Override
	public String getDescription() {
		return "Applies a deformation field to a MUSCLE.";
	}

	@Override
	public String getName() {
		return "Apply Deformation Field";
	}

	@Override
	public void launch(File workingDirectory, String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		File fvecfield = new File(workingDirectory, "demons_vecfield.nii");

		ImageDataFloat vecfield = new ImageDataFloat(NIFTIReaderWriter
				.getInstance().read(fvecfield));

		PhantomSphere sphere = new PhantomSphere(new Point3i(128, 128, 128));
		sphere.setNoiseLevel(0.1);
		sphere.setRadius(0.8);
		sphere.solve();
		ImageDataFloat initDistfield = sphere.getLevelset();
		CompressLevelSets compress = new CompressLevelSets(
				new ImageDataFloat[] { initDistfield });

		PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(128,
				128, 128));
		metasphere.setNoiseLevel(0.1);
		metasphere.setFuzziness(0.5f);
		metasphere.setInvertImage(true);
		metasphere.solve();
		ImageDataFloat refImage = metasphere.getImage();

		MuscleApplyDisplacementField3D activeContour = new MuscleApplyDisplacementField3D();
		activeContour.setCurvatureWeight(0.1f);
		activeContour.setTargetPressure(0.5f);
		activeContour.setMaxIterations(130);
		activeContour.setReferenceImage(refImage);
		activeContour.setInitialLabelImage(compress.getLabelImage());
		activeContour.setInitialDistanceFieldImage(compress
				.getDistanceFieldImage());
		if (showGUI) {
			try {
				activeContour.init(vecfield);
				VisualizationMUSCLE3D visual = new VisualizationMUSCLE3D(600,
						600, activeContour);
				VisualizationApplication app = new VisualizationApplication(
						visual);
				app.setPreferredSize(new Dimension(1024, 768));
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
			activeContour.solve(vecfield);
		}

	}

}
