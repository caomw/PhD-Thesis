package org.imagesci.robopaint;

import javax.vecmath.Color4f;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.*;

public class RoboControlPane {
	public RoboControlPane(Composite parent) {
		ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
		final Display display = parent.getDisplay();
		final Shell shell = parent.getShell();

		// Image item
		Composite imageComposite = new Composite(bar, SWT.NONE);
		GridLayout imageLayout = new GridLayout(3, false);
		imageLayout.marginLeft = imageLayout.marginTop = imageLayout.marginRight = imageLayout.marginBottom = 10;
		imageLayout.verticalSpacing = 10;
		imageComposite.setLayout(imageLayout);
		
		Label filenameLabel = new Label(imageComposite, SWT.NONE);
		filenameLabel.setText("Filename.img");
		Label blankLabel = new Label(imageComposite, SWT.NONE);
		Label blankLabel2 = new Label(imageComposite, SWT.NONE);
		
		Label rowLabel = new Label(imageComposite, SWT.NONE);
		rowLabel.setText("Row");
		final Scale rowScale = new Scale(imageComposite, SWT.NONE);
		rowScale.setMinimum(0);
		rowScale.setMaximum(100);
		rowScale.setIncrement(10);
		rowScale.setPageIncrement(10);
		final Label rowScaleLabel = new Label(imageComposite, SWT.READ_ONLY);
		rowScaleLabel.setText(Integer.toString(rowScale.getSelection()));
		rowScaleLabel.setAlignment(SWT.RIGHT);
		rowScale.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				
				int rowValue = rowScale.getSelection();
				ImageViewDescription.getInstance().setRow(rowValue);
				rowScaleLabel.setText(Integer.toString(rowValue));
				rowScaleLabel.pack();
			}
		
		});
		
		Label colLabel = new Label(imageComposite, SWT.NONE);
		colLabel.setText("Column");
		final Scale colScale = new Scale(imageComposite, SWT.NONE);
		colScale.setMinimum(0);
		colScale.setMaximum(100);
		colScale.setIncrement(10);
		colScale.setPageIncrement(10);
		final Label colScaleLabel = new Label(imageComposite, SWT.NONE);
		colScaleLabel.setText(Integer.toString(colScale.getSelection()));
		colScaleLabel.setAlignment(SWT.RIGHT);
		colScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				int colValue = colScale.getSelection();
				ImageViewDescription.getInstance().setCol(colValue);
				colScaleLabel.setText(Integer.toString(colValue));
				colScaleLabel.pack();
			}
		});
		
		Label sliceLabel = new Label(imageComposite, SWT.NONE);
		sliceLabel.setText("Slice");
		final Scale sliceScale = new Scale(imageComposite, SWT.NONE);
		sliceScale.setMinimum(0);
		sliceScale.setMaximum(100);
		sliceScale.setIncrement(10);
		sliceScale.setPageIncrement(10);
		final Label sliceScaleLabel = new Label(imageComposite, SWT.NONE);
		sliceScaleLabel.setText(Integer.toString(sliceScale.getSelection()));
		sliceScaleLabel.setAlignment(SWT.RIGHT);
		sliceScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				int sliceValue = sliceScale.getSelection();
				ImageViewDescription.getInstance().setSlice(sliceValue);
				sliceScaleLabel.setText(Integer.toString(sliceValue));
				sliceScaleLabel.pack();
			}
		});
		
		Label contrastLabel = new Label(imageComposite, SWT.NONE);
		contrastLabel.setText("Contrast");
		final Scale contrastScale = new Scale(imageComposite, SWT.NONE);
		contrastScale.setMinimum(0);
		contrastScale.setMaximum(100);
		contrastScale.setIncrement(10);
		contrastScale.setPageIncrement(10);
		final Label contrastScaleLabel = new Label(imageComposite, SWT.NONE);
		contrastScaleLabel.setText(Integer.toString(contrastScale.getSelection()));
		contrastScaleLabel.setAlignment(SWT.RIGHT);
		contrastScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				int contrastValue = contrastScale.getSelection();
				ImageViewDescription.getInstance().setContrast(contrastValue);
				contrastScaleLabel.setText(Integer.toString(contrastValue));
				contrastScaleLabel.pack();
			}
		});
		
		Label brightnessLabel = new Label(imageComposite, SWT.NONE);
		brightnessLabel.setText("Brightness");
		final Scale brightnessScale = new Scale(imageComposite, SWT.NONE);
		brightnessScale.setMinimum(0);
		brightnessScale.setMaximum(100);
		brightnessScale.setIncrement(10);
		brightnessScale.setPageIncrement(10);
		final Label brightnessScaleLabel = new Label(imageComposite, SWT.NONE);
		brightnessScaleLabel.setText(Integer.toString(brightnessScale.getSelection()));
		brightnessScaleLabel.setAlignment(SWT.RIGHT);
		brightnessScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				int brightnessValue = brightnessScale.getSelection();
				ImageViewDescription.getInstance().setBrightness(brightnessValue);
				brightnessScaleLabel.setText(Integer.toString(brightnessValue));
				brightnessScaleLabel.pack();
			}
		});
		
		Label transparencyLabel = new Label(imageComposite, SWT.NONE);
		transparencyLabel.setText("Transparency");
		final Scale transparencyScale = new Scale(imageComposite, SWT.NONE);
		transparencyScale.setMinimum(0);
		transparencyScale.setMaximum(100);
		transparencyScale.setIncrement(10);
		transparencyScale.setPageIncrement(10);
		final Label transparencyScaleLabel = new Label(imageComposite, SWT.NONE);
		transparencyScaleLabel.setText(Integer.toString(transparencyScale.getSelection()));
		transparencyScaleLabel.setAlignment(SWT.RIGHT);
		transparencyScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				int transparencyValue = transparencyScale.getSelection();
				ImageViewDescription.getInstance().setTransparency(transparencyValue);
				transparencyScaleLabel.setText(Integer.toString(transparencyValue));
				transparencyScaleLabel.pack();
			}
		});
		
		ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
		item0.setText("Image");
		item0.setHeight(imageComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(imageComposite);

		// Geometry item
		Composite geoComposite = new Composite(bar, SWT.NONE);
		GridLayout geoLayout = new GridLayout(2, false);
		geoLayout.marginLeft = geoLayout.marginTop = geoLayout.marginRight = geoLayout.marginBottom = 10;
		geoLayout.verticalSpacing = 10;
		geoComposite.setLayout(geoLayout);
		
		Label isoLabel = new Label(geoComposite, SWT.NONE);
		isoLabel.setText("Show Iso-surface");
		Button isoButton = new Button(geoComposite, SWT.CHECK);
		isoButton.setSelection(true);
		isoButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				if (GeometryViewDescription.getInstance().isShowIsoSurface()) {
					
					GeometryViewDescription.getInstance().setShowIsoSurface(false);
				} else {
					
					GeometryViewDescription.getInstance().setShowIsoSurface(true);
				}
			}
		});
		
		Label geoVisiblityLabel = new Label(geoComposite, SWT.NONE);
		geoVisiblityLabel.setText("Visible");
		Button visibilityButton = new Button(geoComposite, SWT.CHECK);
		visibilityButton.setSelection(true);
		visibilityButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				/*if (GeometryViewDescription.getInstance().isVisible()) {
					
					GeometryViewDescription.getInstance().setVisible(false);
				} else {
					
					GeometryViewDescription.getInstance().setVisible(true);
				}*/
			}
		});
		
		Label geoNameLabel = new Label(geoComposite, SWT.NONE);
		geoNameLabel.setText("Name");
		final Combo nameCombo = new Combo(geoComposite, SWT.NONE);
		nameCombo.add("Label [#1]");
		nameCombo.add("Label [#2]");
		nameCombo.select(0);
		nameCombo.addKeyListener(new KeyListener() {
			
			int currentIndex = nameCombo.getSelectionIndex();
			String currentText;
			
			public void keyPressed(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					currentText = nameCombo.getText();
				}
			}
			
			public void keyReleased(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					nameCombo.remove(currentIndex);
					nameCombo.add(currentText, currentIndex);
					// update name
				}
			}
		});
		nameCombo.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				// update displayed 4 traits.
			}
		});
		
		Label colorLabel = new Label(geoComposite, SWT.NONE);
		colorLabel.setText("Color");
		Composite colorDialogComposite = new Composite(geoComposite, SWT.NONE);
		GridLayout colorDialogLayout = new GridLayout(2, false);
		colorDialogComposite.setLayout(colorDialogLayout);
		Color color = new Color(display, new RGB(0, 255, 0));
		final Label colorDisplayLabel = new Label(colorDialogComposite, SWT.BORDER);
		colorDisplayLabel.setText("     ");
		colorDisplayLabel.setBackground(color);
		Button colorButton = new Button(colorDialogComposite, SWT.PUSH);
		colorButton.setText("Change color");
		colorButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				ColorDialog colorDialog = new ColorDialog(shell);
				colorDialog.setText("Change color...");
				colorDialog.setRGB(new RGB(0, 255, 0));
				RGB newColor = colorDialog.open();
				if (newColor == null) {
					
					return;
				}
				
				colorDisplayLabel.setBackground(new Color(display, newColor));
				
				// update color.
			}
		});
		
		Label geoTransparencyLabel = new Label(geoComposite, SWT.NONE);
		geoTransparencyLabel.setText("Transparency");
		final Composite geoTransparencyComposite = new Composite(geoComposite, SWT.NONE);
		GridLayout geoTransparencyLayout = new GridLayout(2, true);
		geoTransparencyComposite.setLayout(geoTransparencyLayout);
		final Scale geoTransparencyScale = new Scale(geoTransparencyComposite, SWT.NONE);
		geoTransparencyScale.setMinimum(0);
		geoTransparencyScale.setMaximum(100);
		geoTransparencyScale.setIncrement(10);
		geoTransparencyScale.setPageIncrement(10);
		final Label geoTransparencyScaleLabel = new Label(geoTransparencyComposite, SWT.NONE);
		geoTransparencyScaleLabel.setText(Integer.toString(geoTransparencyScale.getSelection()));
		geoTransparencyScaleLabel.setAlignment(SWT.RIGHT);
		geoTransparencyScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				int geoTransparencyValue = geoTransparencyScale.getSelection();
				// update transparency.
				geoTransparencyScaleLabel.setText(Integer.toString(geoTransparencyValue));
				geoTransparencyScaleLabel.pack();
				geoTransparencyComposite.pack();
			}
		});
		
		ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 1);
		item1.setText("Geometry");
		item1.setHeight(geoComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(geoComposite);
		
		// Paint item
		Composite paintComposite = new Composite(bar, SWT.NONE);
		GridLayout paintLayout = new GridLayout(2, false);
		paintLayout.marginLeft = paintLayout.marginTop = paintLayout.marginRight = paintLayout.marginBottom = 10;
		paintLayout.verticalSpacing = 10;
		paintComposite.setLayout(paintLayout);
		
		Label currentObjectLabel = new Label(paintComposite, SWT.NONE);
		currentObjectLabel.setText("Current object");
		currentObjectLabel.pack();
		final Combo paintNameCombo = new Combo(paintComposite, SWT.READ_ONLY);
		String labelArray[] = nameCombo.getItems();
		for (int i=0; i < labelArray.length; i++) {
			
			paintNameCombo.add(labelArray[i]);
		}
		paintNameCombo.select(0);
		nameCombo.addKeyListener(new KeyListener() {
			
			int currentIndex = nameCombo.getSelectionIndex();
			String currentText;
			
			public void keyPressed(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					currentText = nameCombo.getText();
				}
			}
			
			public void keyReleased(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					paintNameCombo.remove(currentIndex);
					paintNameCombo.add(currentText, currentIndex);
				}
			}
		});
		paintNameCombo.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				// set current object.
			}
		});
		paintNameCombo.pack();
		
		Label brushSizeLabel = new Label(paintComposite, SWT.NONE);
		brushSizeLabel.setText("Brush size");
		final Composite brushSizeComposite = new Composite(paintComposite, SWT.NONE);
		GridLayout brushSizeLayout = new GridLayout(2, false);
		brushSizeComposite.setLayout(brushSizeLayout);
		final Scale brushSizeScale = new Scale(brushSizeComposite, SWT.NONE);
		brushSizeScale.setMinimum(0);
		brushSizeScale.setMaximum(50);
		brushSizeScale.setPageIncrement(5);
		brushSizeScale.setIncrement(5);
		final Label brushSizeScaleLabel = new Label(brushSizeComposite, SWT.NONE);
		brushSizeScaleLabel.setText(Integer.toString(brushSizeScale.getSelection()));
		brushSizeScaleLabel.setAlignment(SWT.RIGHT);
		brushSizeScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				brushSizeScaleLabel.setText(Integer.toString(brushSizeScale.getSelection()));
				PaintViewDescription.getInstance().setPaintBrushSize(brushSizeScale.getSelection());
				brushSizeScaleLabel.pack();
				brushSizeComposite.pack();
			}
		});
		
		Label paintMaskLabel = new Label(paintComposite, SWT.NONE);
		paintMaskLabel.setText("Paint Transparency");
		final Composite paintMaskComposite = new Composite(paintComposite, SWT.NONE);
		GridLayout paintMaskLayout = new GridLayout(2, false);
		paintMaskComposite.setLayout(paintMaskLayout);
		final Scale paintMaskScale = new Scale(paintMaskComposite, SWT.NONE);
		paintMaskScale.setMinimum(0);
		paintMaskScale.setMaximum(100);
		paintMaskScale.setIncrement(10);
		paintMaskScale.setPageIncrement(10);
		final Label paintMaskScaleLabel = new Label(paintMaskComposite, SWT.NONE);
		paintMaskScaleLabel.setText(Integer.toString(paintMaskScale.getSelection()));
		paintMaskScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				paintMaskScaleLabel.setText(Integer.toString(paintMaskScale.getSelection()));
				PaintViewDescription.getInstance().setTransparency(paintMaskScale.getSelection());
				paintMaskScaleLabel.pack();
				paintMaskComposite.pack();
			}
		});
		
		Label brush3DLabel = new Label(paintComposite, SWT.NONE);
		brush3DLabel.setText("3D Brush");
		Button brush3DButton = new Button(paintComposite, SWT.CHECK);
		brush3DButton.setSelection(true);
		brush3DButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				if(PaintViewDescription.getInstance().isBrush3D()) {
					
					PaintViewDescription.getInstance().setBrush3D(false);
				}
				
				else {
					
					PaintViewDescription.getInstance().setBrush3D(true);
				}
			}
		});
		
		ExpandItem item2 = new ExpandItem(bar, SWT.NONE, 2);
		item2.setText("Paint");
		item2.setHeight(paintComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item2.setControl(paintComposite);
		
		// Auto-segment item
		Composite segComposite = new Composite(bar, SWT.NONE);
		GridLayout segLayout = new GridLayout();
		segLayout.marginLeft = segLayout.marginTop = segLayout.marginRight = segLayout.marginBottom = 10;
		segLayout.verticalSpacing = 10;
		segComposite.setLayout(segLayout);
		
		ExpandItem item3 = new ExpandItem(bar, SWT.NONE, 3);
		item3.setText("Sculpt");
		item3.setHeight(segComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item3.setControl(segComposite);
		
		// Sculpt item
		Composite sculptComposite = new Composite(bar, SWT.NONE);
		GridLayout sculptLayout = new GridLayout();
		sculptLayout.marginLeft = sculptLayout.marginTop = sculptLayout.marginRight = sculptLayout.marginBottom = 10;
		sculptLayout.verticalSpacing = 10;
		sculptComposite.setLayout(sculptLayout);
		
		ExpandItem item4 = new ExpandItem(bar, SWT.NONE, 3);
		item4.setText("Auto-segment");
		item4.setHeight(sculptComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item4.setControl(sculptComposite);
	}
}
