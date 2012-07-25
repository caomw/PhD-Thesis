package org.imagesci.robopaint;

import javax.swing.ImageIcon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.imagesci.robopaint.icons.PlaceHolder;
/**
 * The RoboToolbar constructs a GUI layout for toolbar functionality.
 * @author TYung
 *
 */
public class RoboToolbar {
	/**
	 * 
	 * @param parent parent shell object
	 */
	public RoboToolbar(Shell parent) {
		
		final Shell shell = parent;
		Display display = parent.getDisplay();

		Image openImage = new Image(display, PlaceHolder.class.getResourceAsStream("./toolbarButtonGraphics/general/Open24.gif"));
		Image saveImage = new Image(display, PlaceHolder.class.getResourceAsStream("./toolbarButtonGraphics/general/Save24.gif"));
		final Image playImage = new Image(display, PlaceHolder.class.getResourceAsStream("./toolbarButtonGraphics/media/Play24.gif"));
		final Image stopImage = new Image(display, PlaceHolder.class.getResourceAsStream("./toolbarButtonGraphics/media/Stop24.gif"));
		
		CoolBar bar = new CoolBar(parent, SWT.BORDER);
		
		CoolItem fileItems = new CoolItem(bar, SWT.NONE);
		Composite fileComposite = new Composite(bar, SWT.NONE);
		GridLayout fileLayout = new GridLayout(2, true);
		fileComposite.setLayout(fileLayout);
		Button openButton = new Button(fileComposite, SWT.PUSH);
		openButton.setImage(openImage);
		openButton.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent event) {
				
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = {"*.img", "*.hdr", "*.nii"};
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();
			}
		});
		Button saveButton = new Button(fileComposite, SWT.PUSH);
		saveButton.setImage(saveImage);
		Point fileSize = fileComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		fileItems.setControl(fileComposite);
		fileItems.setPreferredSize(fileItems.computeSize(fileSize.x, fileSize.y));
		
		CoolItem segItems = new CoolItem(bar, SWT.NONE);
		final Button playButton = new Button(bar, SWT.PUSH);
		playButton.setImage(playImage);
		playButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				if (PaintViewDescription.getInstance().getCurrentObject().getPlaying()) {
					
					PaintViewDescription.getInstance().getCurrentObject().setPlaying(false);
					playButton.setImage(playImage);
				}
				
				else {
					
					PaintViewDescription.getInstance().getCurrentObject().setPlaying(true);
					playButton.setImage(stopImage);
				}
			}
		});
		Point segSize = playButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		segItems.setControl(playButton);
		segItems.setPreferredSize(segItems.computeSize(segSize.x, segSize.y));
		
		CoolItem toolItems = new CoolItem(bar, SWT.NONE);
		Combo toolCombo = new Combo(bar, SWT.READ_ONLY);
		toolCombo.add("Paint");
		toolCombo.add("Auto-segment");
		toolCombo.add("Sculpt");
		toolCombo.select(0);
		Point toolSize = toolCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		toolItems.setControl(toolCombo);
		toolItems.setPreferredSize(toolItems.computeSize(toolSize.x, toolSize.y));
		
		CoolItem blankItem = new CoolItem(bar, SWT.NONE);
		
		Rectangle clientArea = parent.getClientArea();
		bar.setLocation(clientArea.x, clientArea.y);
		bar.setLayoutData(new BorderLayout.BorderData(BorderLayout.NORTH));
	}
}