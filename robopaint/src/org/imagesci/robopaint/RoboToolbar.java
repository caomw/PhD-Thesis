package org.imagesci.robopaint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Shell;

public class RoboToolbar {
	public RoboToolbar(Shell parent) {
		CoolBar bar = new CoolBar(parent, SWT.BORDER);
		
		CoolItem openItem = new CoolItem(bar, SWT.NONE);
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
		
		Rectangle clientArea = parent.getClientArea();
		bar.setLocation(clientArea.x, clientArea.y);
		bar.setLayoutData(new BorderLayout.BorderData(BorderLayout.NORTH));
	}
}