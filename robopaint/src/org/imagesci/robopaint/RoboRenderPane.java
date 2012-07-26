package org.imagesci.robopaint;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point3i;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.imagesci.mogac.MOGAC3D;
import org.imagesci.robopaint.graphics.RoboRenderWidget;
import org.imagesci.utility.PhantomMetasphere;
import org.imagesci.utility.RandomSphereCollection;

import com.jogamp.opencl.CLDevice;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationImage2D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class RoboRenderPane {
	private Frame frame;
	private VisualizationProcessing visual;

	public RoboRenderPane(Composite parent) {
		final Display display = parent.getDisplay();
		final Shell shell = parent.getShell();
		frame = SWT_AWT.new_Frame(parent);
		// main(null);
		Rectangle bounds = parent.getBounds();
		visual = createVisual(800, 600);
		ParamCollection visualizationParameters = visual.create();
		visual.updateVisualizationParameters();
		ParamInputView inputView = visualizationParameters.getInputView();
		visual.updateVisualizationParameters();
		inputView.addObserver(visual);
		inputView.update();

	}

	public void launch() {
		Panel p = new Panel(new BorderLayout());
		p.add(visual.getComponent(), BorderLayout.CENTER);
		frame.add(p);
		frame.pack();
		frame.setVisible(true);
		((VisualizationProcessing) visual).init();
	}

	private static VisualizationProcessing createVisual(int width, int height) {
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
		MOGAC3D activeContour = new MOGAC3D(refImage, CLDevice.Type.GPU);
		activeContour.setPressure(refImage, 0.5f);
		activeContour.setCurvatureWeight(1.0f);
		activeContour.setTargetPressure(0.5f);
		activeContour.setMaxIterations(620);
		activeContour.setClampSpeed(true);

		try {
			activeContour.init(initDistField, initLabels, false);

			VisualizationMOGAC3D visual = new VisualizationMOGAC3D(width,
					height, activeContour);
			return visual;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
}
