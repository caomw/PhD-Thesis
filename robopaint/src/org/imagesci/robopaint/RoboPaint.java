package org.imagesci.robopaint;

import java.io.File;
import java.util.Collection;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class RoboPaint {
	protected final Display display = new Display();
	protected final Shell shell = new Shell(display,SWT.DIALOG_TRIM );
	
	public static enum Tools {
		
		PAINT, AUTOSEG, SCULPT
	};
	protected static Tools tool = Tools.PAINT;
	
	public static Tools getTool() {
		
		return tool;
	}
	
	public static void setTool(Tools newTool) {
		
		tool = newTool;
	}

	public static void main(String[] args) {
		int i = 0;
		
		ObjectDescription label1 = new ObjectDescription("Label 1", i++);
		ObjectDescription label2 = new ObjectDescription("Label 2", i++);
		
		GeometryViewDescription.getInstance().getObjectDescriptions().add(label1);
		GeometryViewDescription.getInstance().getObjectDescriptions().add(label2);
		
		SculptDescription creaseTool = new SculptDescription("Crease");
		SculptDescription rotateTool = new SculptDescription("Rotate");
		SculptDescription scaleTool = new SculptDescription("Scale");
		SculptDescription drawTool = new SculptDescription("Draw");
		SculptDescription flattenTool = new SculptDescription("Flatten");
		SculptDescription grabTool = new SculptDescription("Grab");
		SculptDescription inflateTool = new SculptDescription("Inflate");
		SculptDescription pinchTool = new SculptDescription("Pinch");
		SculptDescription smoothTool = new SculptDescription("Smooth");
		
		SculptViewDescription.getInstance().getSculptDescriptions().add(creaseTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(rotateTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(scaleTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(drawTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(flattenTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(grabTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(inflateTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(pinchTool);
		SculptViewDescription.getInstance().getSculptDescriptions().add(smoothTool);
	
		int n=GeometryViewDescription.getInstance().getObjectDescriptions().indexOf(label1);
		RoboPaint robo = new RoboPaint();
	}

	public RoboPaint() {
		shell.setText("RoboPaint");
		BorderLayout blayout = new BorderLayout();
		shell.setLayout(blayout);
		RoboToolbar toolbar = new RoboToolbar(shell);

		SashForm form = new SashForm(shell, SWT.HORIZONTAL);
		form.setLayoutData(new BorderLayout.BorderData(BorderLayout.CENTER));
		form.setLayout(new FillLayout());
		Composite controlComp = new Composite(form, SWT.BORDER);
		Composite renderComp = new Composite(form, SWT.BORDER|SWT.EMBEDDED | SWT.NO_BACKGROUND);
		renderComp.setLayout(new FillLayout());
		controlComp.setLayout(new FillLayout());
		form.setWeights(new int[] { 25, 75 });
		RoboControlPane controlPane = new RoboControlPane(controlComp);
		RoboRenderPane renderPane = new RoboRenderPane(renderComp);
		RoboMenubar menu = new RoboMenubar(shell);

		// shell.setSize(1600,1000);
		shell.setMaximized(true);
		shell.open();
		renderPane.launch();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
