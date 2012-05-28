package org.imagesci.robopaint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class RoboMenubar {
	public RoboMenubar(Shell parent) {
		Menu mbar = new Menu(parent, SWT.BAR);

		MenuItem fileItem = new MenuItem(mbar, SWT.CASCADE);
		fileItem.setText("&File");
		Menu submenu = new Menu(parent, SWT.DROP_DOWN);
		fileItem.setMenu(submenu);
		MenuItem item = new MenuItem(submenu, SWT.PUSH);

		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				System.out.println("SAVE AS!");
			}
		});
		item.setText("&Save As\tCtrl+S");
		item.setAccelerator(SWT.MOD1 + 'S');
		parent.setMenuBar(mbar);
	}
}
