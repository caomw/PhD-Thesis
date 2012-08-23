package org.imagesci.robopaint;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import edu.jhu.ece.iacl.jist.io.FileReaderWriter;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

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
	public final RoboRenderPane renderPane;
	public final RoboControlPane controlPane;
	public final RoboToolbar roboToolbar;
	public final RoboMenubar roboMenubar;

	public RoboPaint() {
		shell.setText("RoboPaint");
		BorderLayout blayout = new BorderLayout();
		shell.setLayout(blayout);
		roboToolbar = new RoboToolbar(this, shell);

		SashForm form = new SashForm(shell, SWT.HORIZONTAL);
		form.setLayoutData(new BorderLayout.BorderData(BorderLayout.CENTER));
		form.setLayout(new FillLayout());
		Composite controlComp = new Composite(form, SWT.BORDER);
		Composite renderComp = new Composite(form, SWT.BORDER | SWT.EMBEDDED
				| SWT.NO_BACKGROUND);
		renderComp.setLayout(new FillLayout());
		controlComp.setLayout(new FillLayout());
		form.setWeights(new int[] { 30, 70 });
		controlPane = new RoboControlPane(this, controlComp);
		renderPane = new RoboRenderPane(this, renderComp);
		roboMenubar = new RoboMenubar(this, shell);
		ImageViewDescription.getInstance().listeners.add(renderPane);
		ImageViewDescription.getInstance().listeners.add(controlPane);
		GeometryViewDescription.getInstance().listeners.add(renderPane);
		GeometryViewDescription.getInstance().listeners.add(controlPane);
		shell.setSize(1200, 768);
		shell.setLocation(20, 20);
		// shell.setMaximized(true);
		shell.open();
		renderPane.launch();

		// try {
		/*

		ImageViewDescription.getInstance().setFile(
				new File("C:\\Users\\Blake\\Desktop\\metacube.nii"));
		GeometryViewDescription.getInstance().setLabelImageFile(
				new File("C:\\Users\\Blake\\Desktop\\ufo_labels.nii"));
	
		GeometryViewDescription.getInstance().setDistanceFieldFile(
				new File("C:\\Users\\Blake\\Desktop\\ufo_distfield.nii"));
			*/
		// } catch (Exception e) {
		// System.err.println("Could not find file.");
		// }

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		renderPane.dispose();
		System.exit(1);
	}

	public boolean openReferenceImage() {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Open");
		fileDialog.setFilterPath("C:/");
		String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
		fileDialog.setFilterExtensions(filterExtensions);
		fileDialog.open();
		File f = new File(fileDialog.getFilterPath(), fileDialog.getFileName());
		if (f.exists() && !f.isDirectory()) {
			ImageViewDescription.getInstance().setFile(f);
			return true;
		} else
			return false;
	}

	public boolean openLabelImage() {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Open Label Image");
		fileDialog.setFilterPath("C:/");
		String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
		fileDialog.setFilterExtensions(filterExtensions);
		fileDialog.open();
		File f = new File(fileDialog.getFilterPath(), fileDialog.getFileName());
		if (f.exists() && !f.isDirectory()) {
			GeometryViewDescription.getInstance().setLabelImageFile(f);
			return true;
		} else
			return false;
	}

	public boolean openDistanceFieldImage() {

		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Open Distance Field");
		fileDialog.setFilterPath("C:/");
		String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
		fileDialog.setFilterExtensions(filterExtensions);
		fileDialog.open();
		File f = new File(fileDialog.getFilterPath(), fileDialog.getFileName());
		if (f.exists() && !f.isDirectory()) {
			GeometryViewDescription.getInstance().setDistanceFieldFile(f);
			return true;
		} else {
			return false;
		}
	}

	public boolean saveSegmentation() {
		File f = ImageViewDescription.getInstance().getImageFile();
		if (f == null)
			return false;
		String fileName = FileReaderWriter.getFileName(f);
		String fileExt = FileReaderWriter.getFileExtension(f);
		if (fileExt == null)
			renderPane.getWidget().getRenderer().syncPaint();
		ImageDataInt labels = renderPane.getWidget().getActiveContour()
				.getImageLabels();

		if (GeometryViewDescription.getInstance().getLabelImageFile() != null) {
			NIFTIReaderWriter.getInstance().write(labels,
					GeometryViewDescription.getInstance().getLabelImageFile());
		} else {
			File labelFile = new File(f.getParent(), fileName + "_labels."
					+ fileExt);
			NIFTIReaderWriter.getInstance().write(labels, labelFile);
		}
		ImageDataFloat distfield = renderPane.getWidget().getActiveContour()
				.getDistanceField();
		if (GeometryViewDescription.getInstance().getDistanceFieldImageFile() != null) {
			NIFTIReaderWriter.getInstance().write(
					distfield,
					GeometryViewDescription.getInstance()
							.getDistanceFieldImageFile());
		} else {
			File distFile = new File(f.getParent(), fileName + "_distfield."
					+ fileExt);
			NIFTIReaderWriter.getInstance().write(distfield, distFile);
		}
		return true;
	}

	public boolean saveAsSegmentation() {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setText("Save As");
		fileDialog.setFilterPath(ImageViewDescription.getInstance()
				.getImageFile().getParent());
		String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
		fileDialog.setFilterExtensions(filterExtensions);
		fileDialog.open();
		if (fileDialog.getFileName() != null) {
			File f = new File(fileDialog.getFilterPath(),
					fileDialog.getFileName());
			renderPane.getWidget().getRenderer().syncPaint();
			String fileName = FileReaderWriter.getFileName(f);
			String fileExt = FileReaderWriter.getFileExtension(f);
			if (fileExt == null)
				fileExt = "nii";
			ImageDataInt labels = renderPane.getWidget().getActiveContour()
					.getImageLabels();
			ImageDataFloat distfield = renderPane.getWidget()
					.getActiveContour().getDistanceField();
			File labelFile = new File(f.getParent(), fileName + "_labels."
					+ fileExt);
			NIFTIReaderWriter.getInstance().write(labels, labelFile);
			File distFile = new File(f.getParent(), fileName + "_distfield."
					+ fileExt);
			NIFTIReaderWriter.getInstance().write(distfield, distFile);
			GeometryViewDescription.getInstance().labelFile = labelFile;
			GeometryViewDescription.getInstance().distfieldFile = distFile;
			return true;
		} else {
			return false;
		}
	}
}
