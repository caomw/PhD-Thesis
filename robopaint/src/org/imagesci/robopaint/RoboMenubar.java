package org.imagesci.robopaint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class RoboMenubar {
	public RoboMenubar(Shell parent) {
		
		final Shell shell = parent.getShell();
		Menu mbar = new Menu(parent, SWT.BAR);

		MenuItem fileMenu = new MenuItem(mbar, SWT.CASCADE);
		fileMenu.setText("&File");
		Menu fileSubMenu = new Menu(parent, SWT.DROP_DOWN);
		fileMenu.setMenu(fileSubMenu);
		
		MenuItem openItem = new MenuItem(fileSubMenu, SWT.PUSH);
		openItem.setText("&Open\tCtrl+O");
		openItem.setAccelerator(SWT.MOD1 + 'O');
		openItem.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent event) {
				
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = {"*.img", "*.hdr", "*.nii"};
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();
			}
		});
		
		MenuItem saveItem = new MenuItem(fileSubMenu, SWT.PUSH);
		saveItem.setText("&Save\tCtrl+S");
		saveItem.setAccelerator(SWT.MOD1 + 'S');
		saveItem.addSelectionListener(new SelectionAdapter() {
			
		});
		
		MenuItem saveAsItem = new MenuItem(fileSubMenu, SWT.PUSH);
		saveAsItem.setText("&Save As\tCtrl+SHIFT+S");
		saveAsItem.setAccelerator(SWT.MOD1 + SWT.MOD2 +'S');
		saveAsItem.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent event) {
				
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Save As");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = {"*.img", "*.hdr", "*.nii"};
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();
			}
		});
		
		MenuItem separator = new MenuItem(fileSubMenu, SWT.SEPARATOR);
		
		MenuItem quitItem = new MenuItem(fileSubMenu, SWT.PUSH);
		quitItem.setText("&Quit\tCtrl+Q");
		quitItem.setAccelerator(SWT.MOD1 + 'Q');
		quitItem.addSelectionListener(new SelectionAdapter() {
			
		});
		
		parent.setMenuBar(mbar);
	}
}
