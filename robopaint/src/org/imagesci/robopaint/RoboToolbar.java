package org.imagesci.robopaint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Shell;

public class RoboToolbar {
	public RoboToolbar(Shell parent) {
		CoolBar bar = new CoolBar(parent, SWT.BORDER);		
		
		CoolItem fileItems = new CoolItem(bar, SWT.NONE);
		Composite fileComposite = new Composite(bar, SWT.NONE);
		GridLayout fileLayout = new GridLayout(2, true);
		fileComposite.setLayout(fileLayout);
		Button openButton = new Button(fileComposite, SWT.PUSH);
		openButton.setText("     ");
		Button saveButton = new Button(fileComposite, SWT.PUSH);
		saveButton.setText("     ");
		Point fileSize = fileComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		fileItems.setControl(fileComposite);
		fileItems.setPreferredSize(fileItems.computeSize(fileSize.x, fileSize.y));
		
		CoolItem segItems = new CoolItem(bar, SWT.NONE);
		Composite segComposite = new Composite(bar, SWT.NONE);
		GridLayout segLayout = new GridLayout(2, true);
		segComposite.setLayout(segLayout);
		Button playButton = new Button(segComposite, SWT.PUSH);
		playButton.setText("     ");
		Button stopButton = new Button(segComposite, SWT.PUSH);
		stopButton.setText("     ");
		Point segSize = segComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		segItems.setControl(segComposite);
		segItems.setPreferredSize(segItems.computeSize(segSize.x, segSize.y));
		
		CoolItem blankItem = new CoolItem(bar, SWT.NONE);
		
		/*CoolItem openItem = new CoolItem(bar, SWT.NONE);
		Button openButton = new Button(bar, SWT.PUSH);
		openButton.setText("Open");
		Point openSize = openButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		openItem.setControl(openButton);
		openItem.setPreferredSize(openItem.computeSize(openSize.x, openSize.y));
		
		CoolItem saveItem = new CoolItem(bar, SWT.NONE);
		Button saveButton = new Button(bar, SWT.PUSH);
		saveButton.setText("Save");
		Point saveSize = saveButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		saveItem.setControl(saveButton);
		saveItem.setPreferredSize(saveItem.computeSize(saveSize.x, saveSize.y));
		
		CoolItem playItem = new CoolItem(bar, SWT.NONE);
		Button playButton = new Button(bar, SWT.NONE);
		playButton.setText("Play");
		Point playSize = playButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		playItem.setControl(playButton);
		playItem.setPreferredSize(playItem.computeSize(playSize.x, playSize.y));
		
		CoolItem blankItem = new CoolItem(bar, SWT.NONE);
		*/
		
		Rectangle clientArea = parent.getClientArea();
		bar.setLocation(clientArea.x, clientArea.y);
		bar.setLayoutData(new BorderLayout.BorderData(BorderLayout.NORTH));
	}
}