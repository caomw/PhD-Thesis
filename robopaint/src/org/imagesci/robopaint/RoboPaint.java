package org.imagesci.robopaint;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RoboPaint {
	public static enum Tools {

		AUTOSEG, PAINT, SCULPT
	}

	protected static Tools tool = Tools.PAINT;

	public static Tools getTool() {

		return tool;
	};

	public static void main(String[] args) {
		int i = 0;

		SculptDescription creaseTool = new SculptDescription("Crease");
		SculptDescription rotateTool = new SculptDescription("Rotate");
		SculptDescription scaleTool = new SculptDescription("Scale");
		SculptDescription drawTool = new SculptDescription("Draw");
		SculptDescription flattenTool = new SculptDescription("Flatten");
		SculptDescription grabTool = new SculptDescription("Grab");
		SculptDescription inflateTool = new SculptDescription("Inflate");
		SculptDescription pinchTool = new SculptDescription("Pinch");
		SculptDescription smoothTool = new SculptDescription("Smooth");

		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(creaseTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(rotateTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(scaleTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(drawTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(flattenTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(grabTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(inflateTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(pinchTool);
		SculptViewDescription.getInstance().getSculptDescriptions()
				.add(smoothTool);

		new RoboPaint();
	}

	public static void setTool(Tools newTool) {

		tool = newTool;
	}

	protected final Display display = new Display();

	protected final Shell shell = new Shell(display, SWT.DIALOG_TRIM);

	public RoboPaint() {
		shell.setText("RoboPaint");
		BorderLayout blayout = new BorderLayout();
		shell.setLayout(blayout);
		new RoboToolbar(shell);

		SashForm form = new SashForm(shell, SWT.HORIZONTAL);
		form.setLayoutData(new BorderLayout.BorderData(BorderLayout.CENTER));
		form.setLayout(new FillLayout());
		Composite controlComp = new Composite(form, SWT.BORDER);
		Composite renderComp = new Composite(form, SWT.BORDER | SWT.EMBEDDED
				| SWT.NO_BACKGROUND);
		renderComp.setLayout(new FillLayout());
		controlComp.setLayout(new FillLayout());
		form.setWeights(new int[] { 30, 70 });
		RoboControlPane controlPane = new RoboControlPane(controlComp);
		RoboRenderPane renderPane = new RoboRenderPane(renderComp);
		new RoboMenubar(shell);
		ImageViewDescription.getInstance().listeners.add(renderPane);
		ImageViewDescription.getInstance().listeners.add(controlPane);
		GeometryViewDescription.getInstance().listeners.add(renderPane);
		GeometryViewDescription.getInstance().listeners.add(controlPane);
		shell.setSize(1200, 768);
		shell.setLocation(20, 20);
		// shell.setMaximized(true);
		shell.open();
		renderPane.launch();
		/*
		ImageViewDescription.getInstance().setFile(
				new File("C:\\Users\\Blake\\Desktop\\metacube.nii"));
		GeometryViewDescription.getInstance().setLabelImageFile(
				new File("C:\\Users\\Blake\\Desktop\\ufo_labels.nii"));
		GeometryViewDescription.getInstance().setDistanceFieldFile(
				new File("C:\\Users\\Blake\\Desktop\\ufo_distfield.nii"));
*/
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		renderPane.dispose();
		System.exit(1);
	}
}
