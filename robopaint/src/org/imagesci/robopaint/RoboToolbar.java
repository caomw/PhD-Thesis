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
		for (int i = 0; i < 2; i++) {
			CoolItem citem = new CoolItem(bar, SWT.NONE);
			Button button = new Button(bar, SWT.PUSH);
			button.setText("Button " + i);
			Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			citem.setControl(button);
			citem.setPreferredSize(citem.computeSize(size.x, size.y));
		}
		Rectangle clientArea = parent.getClientArea();
		bar.setLocation(clientArea.x, clientArea.y);
		bar.setLayoutData(new BorderLayout.BorderData(BorderLayout.NORTH));
	}
}