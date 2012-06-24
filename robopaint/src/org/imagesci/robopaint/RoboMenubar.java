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
		
		MenuItem openItem = new MenuItem(submenu, SWT.PUSH);
		openItem.setText("&Open\tCtrl+O");
		openItem.setAccelerator(SWT.MOD1 + 'O');
		openItem.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				System.out.println("OPEN!");
			}
		});
		
		MenuItem saveItem = new MenuItem(submenu, SWT.PUSH);
		saveItem.setText("&Save\tCtrl+S");
		saveItem.setAccelerator(SWT.MOD1 + 'S');
		saveItem.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				System.out.println("SAVE!");
			}
		});
		
		MenuItem saveAsItem = new MenuItem(submenu, SWT.PUSH);
		saveAsItem.setText("&Save As\tCtrl+SHIFT+S");
		saveAsItem.setAccelerator(SWT.MOD1 + SWT.MOD2 +'S');
		saveAsItem.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				System.out.println("SAVE AS!");
			}
		});
		
		parent.setMenuBar(mbar);
	}
}
