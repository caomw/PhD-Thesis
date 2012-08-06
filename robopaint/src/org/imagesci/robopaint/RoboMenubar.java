package org.imagesci.robopaint;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class RoboMenubar {
	protected MenuItem openItem, importLabelsItem, importDistfieldItem,
			importMeshItem;

	public RoboMenubar(Shell parent) {

		final Shell shell = parent.getShell();
		Menu mbar = new Menu(parent, SWT.BAR);
		MenuItem fileMenu = new MenuItem(mbar, SWT.CASCADE);
		fileMenu.setText("&File");
		Menu fileSubMenu = new Menu(parent, SWT.DROP_DOWN);
		fileMenu.setMenu(fileSubMenu);
		MenuItem openAllItem = new MenuItem(fileSubMenu, SWT.PUSH);
		openAllItem.setText("&Open All ...\tCtrl+O");
		openAllItem.setAccelerator(SWT.MOD1 + 'O');
		openAllItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();

			}
		});
		openItem = new MenuItem(fileSubMenu, SWT.PUSH);
		openItem.setText("&(1) Open Reference Image\tCtrl+R");
		openItem.setAccelerator(SWT.MOD1 + 'R');
		openItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();
				File f = new File(fileDialog.getFilterPath(), fileDialog
						.getFileName());
				if (f.exists() && !f.isDirectory()) {
					ImageViewDescription.getInstance().setFile(f);
					importLabelsItem.setEnabled(true);
				}
			}
		});
		importLabelsItem = new MenuItem(fileSubMenu, SWT.PUSH);
		importLabelsItem.setText("&(2) Open Label Image\tCtrl+L");
		importLabelsItem.setAccelerator(SWT.MOD1 + 'L');
		importLabelsItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open Label Image");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();
				File f = new File(fileDialog.getFilterPath(), fileDialog
						.getFileName());
				if (f.exists() && !f.isDirectory()) {
					GeometryViewDescription.getInstance().setLabelImageFile(f);
					importDistfieldItem.setEnabled(true);
				}
			}
		});
		importDistfieldItem = new MenuItem(fileSubMenu, SWT.PUSH);
		importDistfieldItem.setText("&(3) Open Distance Field\tCtrl+D");
		importDistfieldItem.setAccelerator(SWT.MOD1 + 'D');
		importDistfieldItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open Distance Field");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();
				File f = new File(fileDialog.getFilterPath(), fileDialog
						.getFileName());
				if (f.exists() && !f.isDirectory()) {
					GeometryViewDescription.getInstance().setDistanceFieldFile(
							f);
					importDistfieldItem.setEnabled(true);
				}
			}
		});
		importMeshItem = new MenuItem(fileSubMenu, SWT.PUSH);
		importMeshItem.setText("&(4) Open Triangle Mesh\tCtrl+T");
		importMeshItem.setAccelerator(SWT.MOD1 + 'T');
		importMeshItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setText("Open Triangle Mesh");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
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
		saveAsItem.setText("&Save As\tCtrl+Shift+S");
		saveAsItem.setAccelerator(SWT.MOD1 + SWT.MOD2 + 'S');
		saveAsItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setText("Save As");
				fileDialog.setFilterPath("C:/");
				String[] filterExtensions = { "*.nii", "*.img", "*.hdr" };
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.open();
			}
		});
		importLabelsItem.setEnabled(false);
		importDistfieldItem.setEnabled(false);
		importMeshItem.setEnabled(false);
		new MenuItem(fileSubMenu, SWT.SEPARATOR);

		MenuItem quitItem = new MenuItem(fileSubMenu, SWT.PUSH);
		quitItem.setText("&Quit\tCtrl+Q");
		quitItem.setAccelerator(SWT.MOD1 + 'Q');
		quitItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				System.exit(1);
			}
		});

		parent.setMenuBar(mbar);
	}
}
