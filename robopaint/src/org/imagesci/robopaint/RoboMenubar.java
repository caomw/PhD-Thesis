package org.imagesci.robopaint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class RoboMenubar {
	public RoboMenubar(Shell parent) {
		Menu mbar = new Menu(parent, SWT.BAR);

		MenuItem fileMenu = new MenuItem(mbar, SWT.CASCADE);
		fileMenu.setText("&File");
		Menu fileSubMenu = new Menu(parent, SWT.DROP_DOWN);
		fileMenu.setMenu(fileSubMenu);
		
		MenuItem openItem = new MenuItem(fileSubMenu, SWT.PUSH);
		openItem.setText("&Open\tCtrl+O");
		openItem.setAccelerator(SWT.MOD1 + 'O');
		openItem.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent event) {
				
				
			}
			
			public void widgetDefaultSelected(SelectionEvent event) {
				
			}
		});
		
		MenuItem saveItem = new MenuItem(fileSubMenu, SWT.PUSH);
		saveItem.setText("&Save\tCtrl+S");
		saveItem.setAccelerator(SWT.MOD1 + 'S');
		saveItem.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				System.out.println("SAVE!");
			}
		});
		
		MenuItem saveAsItem = new MenuItem(fileSubMenu, SWT.PUSH);
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
