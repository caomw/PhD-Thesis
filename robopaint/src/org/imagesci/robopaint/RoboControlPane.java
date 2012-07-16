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
import org.imagesci.robopaint.ObjectDescription.Status;
import org.imagesci.robopaint.icons.PlaceHolder;

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
				
				if (GeometryViewDescription.getInstance().isHideAll()) {
					
					GeometryViewDescription.getInstance().setHideAll(false);
				} else {
					
					GeometryViewDescription.getInstance().setHideAll(true);
				}
			}
		});
		
		Label geoVisiblityLabel = new Label(geoComposite, SWT.NONE);
		geoVisiblityLabel.setText("Hide all");
		final Button visibilityButton = new Button(geoComposite, SWT.CHECK);
		visibilityButton.setSelection(false);
		// *nameCombo section for visibilityButton listener to update visible property of current object.
		
		Label geoNameLabel = new Label(geoComposite, SWT.NONE);
		geoNameLabel.setText("Name");
		final Combo nameCombo = new Combo(geoComposite, SWT.NONE);
		nameCombo.add(GeometryViewDescription.getInstance().getObjectDescriptions().get(0).getName());
		nameCombo.add(GeometryViewDescription.getInstance().getObjectDescriptions().get(1).getName());
		nameCombo.select(0);
		PaintViewDescription.getInstance().setCurrentObject(GeometryViewDescription.getInstance().getObjectDescriptions().get(0));
		// *visibilityButton listener to update visible property of current object.
		visibilityButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				if (GeometryViewDescription.getInstance().getObjectDescriptions().get(nameCombo.getSelectionIndex()).isVisible()) {
					
					GeometryViewDescription.getInstance().getObjectDescriptions().get(nameCombo.getSelectionIndex()).setVisible(false);
				} else {
					
					GeometryViewDescription.getInstance().getObjectDescriptions().get(nameCombo.getSelectionIndex()).setVisible(true);
				}
			}
		});
		// **end for nameCombo listener to update nameCombo and paintNameCombo names
		//		for current object.
		// ***end for nameCombo listener to update current object and properties
		//		displayed when nameCombo selection changes.
		
		Label colorLabel = new Label(geoComposite, SWT.NONE);
		colorLabel.setText("Color");
		Composite colorDialogComposite = new Composite(geoComposite, SWT.NONE);
		GridLayout colorDialogLayout = new GridLayout(2, false);
		colorDialogComposite.setLayout(colorDialogLayout);
		final Label colorDisplayLabel = new Label(colorDialogComposite, SWT.BORDER);
		colorDisplayLabel.setText("     ");
		Color4f passColor4f = PaintViewDescription.getInstance().getCurrentObject().getColor();
		RGB passRGB = new RGB((int) passColor4f.x, (int) passColor4f.z, (int) passColor4f.y);
		Color passColor = new Color(display, passRGB);
		colorDisplayLabel.setBackground(passColor);
		Button colorButton = new Button(colorDialogComposite, SWT.PUSH);
		colorButton.setText("Change color");
		colorButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				
				ColorDialog colorDialog = new ColorDialog(shell);
				colorDialog.setText("Change color...");
				RGB newColor = colorDialog.open();
				if (newColor == null) {
					
					return;
				}
				
				colorDisplayLabel.setBackground(new Color(display, newColor));
				Color4f passColor = new Color4f(newColor.red, newColor.blue, newColor.green, 0);
				PaintViewDescription.getInstance().getCurrentObject().setColor(passColor);
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
				PaintViewDescription.getInstance().getCurrentObject().setTransparency(geoTransparencyValue);
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
		// ****end for paintNameCombo listener to update current object and properties displayed when 
		// 		paintNameCombo selection changes.
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
		GridLayout segLayout = new GridLayout(2, false);
		segLayout.marginLeft = segLayout.marginTop = segLayout.marginRight = segLayout.marginBottom = 10;
		segLayout.verticalSpacing = 10;
		segComposite.setLayout(segLayout);
		final Image playImage = new Image(display, PlaceHolder.class.getResourceAsStream("./toolbarButtonGraphics/media/Play24.gif"));
		final Image stopImage = new Image(display, PlaceHolder.class.getResourceAsStream("./toolbarButtonGraphics/media/Stop24.gif"));
		
		Label playLabel = new Label(segComposite, SWT.NONE);
		playLabel.setText("PLAY/STOP");
		final Button playButton = new Button(segComposite, SWT.PUSH);
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
		
		final Combo statusNameCombo = new Combo(segComposite, SWT.READ_ONLY);
		String statusLabelArray[] = nameCombo.getItems();
		for (int i=0; i < statusLabelArray.length; i++) {
			
			statusNameCombo.add(statusLabelArray[i]);
		}
		statusNameCombo.select(0);
		// *****end for statusNameCombo listener to update current object and properties displayed 
		//		when statusNameCombo selection changes.
		final Combo statusCombo = new Combo(segComposite, SWT.READ_ONLY);
		statusCombo.add("Active");
		statusCombo.add("Passive");
		statusCombo.add("Static");
		statusCombo.select(0);
		statusCombo.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				int index = statusCombo.getSelectionIndex();
				
				if (index == 0) {
					
					PaintViewDescription.getInstance().currentObject.setStatus(Status.ACTIVE);
				}
				
				else if (index == 1) {
					
					PaintViewDescription.getInstance().getCurrentObject().setStatus(Status.PASSIVE);
				}
				
				else if (index == 2) {
					
					PaintViewDescription.getInstance().getCurrentObject().setStatus(Status.STATIC);
				}
			}
		});
		
		Label pressureLabel = new Label(segComposite, SWT.NONE);
		pressureLabel.setText("Pressure weight");
		final Text pressureText = new Text(segComposite, SWT.BORDER);
		pressureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getPressureWeight()));
		pressureText.addKeyListener(new KeyListener() {
			
			float newValue;
			String currentText;
			
			public void keyPressed(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					 currentText = pressureText.getText();
					
					try {
						
						newValue = Float.parseFloat(currentText);
					}
					
					catch(NumberFormatException err) {
						
						return;
					}
				}
			}
			
			public void keyReleased(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					PaintViewDescription.getInstance().getCurrentObject().setPressureWeight(newValue);
				}
			}
		});
		
		Label intensityLabel = new Label(segComposite, SWT.NONE);
		intensityLabel.setText("Target intensity");
		final Text intensityText = new Text(segComposite, SWT.BORDER);
		intensityText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getTargetIntensity()));
		intensityText.addKeyListener(new KeyListener() {
			
			float newValue;
			String currentText;
			
			public void keyPressed(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					currentText = intensityText.getText();
					
					try {
						
						newValue = Float.parseFloat(currentText);
					}
					
					catch(NumberFormatException err) {
						
						return;
					}
				}
			}
			
			public void keyReleased(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					PaintViewDescription.getInstance().getCurrentObject().setTargetIntensity(newValue);
				}
			}
		});
		
		Label advectionLabel = new Label(segComposite, SWT.NONE);
		advectionLabel.setText("Advection weight");
		final Text advectionText = new Text(segComposite, SWT.BORDER);
		advectionText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getAdvectionWeight()));
		advectionText.addKeyListener(new KeyListener() {
			
			float newValue;
			String currentText;
			
			public void keyPressed(KeyEvent e) {
				
				currentText = advectionText.getText();
				
				if (e.keyCode == SWT.CR) {
					
					try {
						
						newValue = Float.parseFloat(currentText);
					}
					
					catch(NumberFormatException err) {
						
						return;
					}
				}
			}
			
			public void keyReleased(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					PaintViewDescription.getInstance().getCurrentObject().setAdvectionWeight(newValue);
				}
			}
		});
		
		Label curvatureLabel = new Label(segComposite, SWT.NONE);
		curvatureLabel.setText("Curvature weight");
		final Text curvatureText = new Text(segComposite, SWT.BORDER);
		curvatureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getCurvatureWeight()));
		curvatureText.addKeyListener(new KeyListener() {
			
			float newValue;
			String currentText;
			
			public void keyPressed(KeyEvent e) {
				
				currentText = curvatureText.getText();
				
				if (e.keyCode == SWT.CR) {
					
					try {
						
						newValue = Float.parseFloat(currentText);
					}
					
					catch(NumberFormatException err) {
						
						return;
					}
				}
			}
			
			public void keyReleased(KeyEvent e) {
				
				if (e.keyCode == SWT.CR) {
					
					PaintViewDescription.getInstance().getCurrentObject().setCurvatureWeight(newValue);
				}
			}
		});
		
		ExpandItem item3 = new ExpandItem(bar, SWT.NONE, 3);
		item3.setText("Auto-segment");
		item3.setHeight(segComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item3.setControl(segComposite);
		
		// Sculpt item
		Composite sculptComposite = new Composite(bar, SWT.NONE);
		GridLayout sculptLayout = new GridLayout();
		sculptLayout.marginLeft = sculptLayout.marginTop = sculptLayout.marginRight = sculptLayout.marginBottom = 10;
		sculptLayout.verticalSpacing = 10;
		sculptComposite.setLayout(sculptLayout);
		
		final Composite actionComposite = new Composite(sculptComposite, SWT.NONE);
		GridLayout actionLayout = new GridLayout(3, false);
		actionComposite.setLayout(actionLayout);
		Listener buttonListener = new Listener() {
			
			public void handleEvent(Event e) {
				
				Control buttonControl[] = actionComposite.getChildren();
				for (int i=0; i < buttonControl.length; i++) {
					
					Control buttonChild = buttonControl[i];
					if(e.widget != buttonChild) {
						
						((Button) buttonChild).setSelection(false);
					}
				}
				if (((Button)e.widget).getSelection()) {
					
					((Button)e.widget).setSelection(true);
				}
				else {
					
					((Button)e.widget).setSelection(false);
				}
			}
		};
		final Button creaseButton = new Button(actionComposite, SWT.TOGGLE);
		creaseButton.setText("     ");
		creaseButton.addListener(SWT.Selection, buttonListener);
		creaseButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = creaseButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setCrease(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setCrease(false);
				}
			}
		});
		final Button rotateButton = new Button(actionComposite, SWT.TOGGLE);
		rotateButton.setText("     ");
		rotateButton.addListener(SWT.Selection, buttonListener);
		rotateButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = rotateButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setRotate(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setRotate(false);
				}
			}
		});
		final Button scaleButton = new Button(actionComposite, SWT.TOGGLE);
		scaleButton.setText("     ");
		scaleButton.addListener(SWT.Selection, buttonListener);
		scaleButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = scaleButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setScale(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setScale(false);
				}
			}
		});
		final Button drawButton = new Button(actionComposite, SWT.TOGGLE);
		drawButton.setText("     ");
		drawButton.setSelection(true);
		drawButton.addListener(SWT.Selection, buttonListener);
		drawButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = drawButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setDraw(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setDraw(false);
				}
			}
		});
		final Button flattenButton = new Button(actionComposite, SWT.TOGGLE);
		flattenButton.setText("     ");
		flattenButton.addListener(SWT.Selection, buttonListener);
		flattenButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = flattenButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setFlatten(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setFlatten(false);
				}
			}
		});
		final Button grabButton = new Button(actionComposite, SWT.TOGGLE);
		grabButton.setText("     ");
		grabButton.addListener(SWT.Selection, buttonListener);
		grabButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = grabButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setGrab(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setGrab(false);
				}
			}
		});
		final Button inflateButton = new Button(actionComposite, SWT.TOGGLE);
		inflateButton.setText("     ");
		inflateButton.addListener(SWT.Selection, buttonListener);
		inflateButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = inflateButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setInflate(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setInflate(false);
				}
			}
		});
		final Button pinchButton = new Button(actionComposite, SWT.TOGGLE);
		pinchButton.setText("     ");
		pinchButton.addListener(SWT.Selection, buttonListener);
		pinchButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = pinchButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setPinch(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setPinch(false);
				}
			}
		});
		final Button smoothButton = new Button(actionComposite, SWT.TOGGLE);
		smoothButton.setText("     ");
		smoothButton.addListener(SWT.Selection, buttonListener);
		smoothButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				boolean isSelected = smoothButton.getSelection();
				SculptViewDescription.getInstance().setAllFalse();
				if (isSelected) {
					
					SculptViewDescription.getInstance().setSmooth(true);
				}
				
				else {
					
					SculptViewDescription.getInstance().setSmooth(false);
				}
			}
		});
		actionComposite.pack();
		
		Composite propertiesComposite = new Composite(sculptComposite, SWT.NONE);
		GridLayout propertiesLayout = new GridLayout(3, false);
		propertiesComposite.setLayout(propertiesLayout);
		Label sizeLabel = new Label(propertiesComposite, SWT.NONE);
		sizeLabel.setText("Size");
		final Scale sizeScale = new Scale(propertiesComposite, SWT.NONE);
		sizeScale.setMinimum(0);
		sizeScale.setMaximum(50);
		sizeScale.setIncrement(5);
		sizeScale.setPageIncrement(10);
		final Label sizeScaleLabel = new Label(propertiesComposite, SWT.NONE);
		sizeScaleLabel.setText(Integer.toString(sizeScale.getSelection()));
		sizeScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				int sizeValue = sizeScale.getSelection();
				SculptViewDescription.getInstance().setSculptStrength(sizeValue);
				sizeScaleLabel.setText(Integer.toString(sizeValue));
				sizeScaleLabel.pack();
			}
		});
		Label strengthLabel = new Label(propertiesComposite, SWT.NONE);
		strengthLabel.setText("Strength");
		final Scale strengthScale = new Scale(propertiesComposite, SWT.NONE);
		strengthScale.setMinimum(0);
		strengthScale.setMaximum(100);
		strengthScale.setIncrement(10);
		strengthScale.setPageIncrement(10);
		final Label strengthScaleLabel = new Label(propertiesComposite, SWT.NONE);
		strengthScaleLabel.setText(Integer.toString(strengthScale.getSelection()));
		strengthScale.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				int strengthValue = strengthScale.getSelection();
				SculptViewDescription.getInstance().setSculptStrength(strengthValue);
				strengthScaleLabel.setText(Integer.toString(strengthValue));
				strengthScaleLabel.pack();
			}
		});
		propertiesComposite.pack();
		sculptComposite.pack();
		
		ExpandItem item4 = new ExpandItem(bar, SWT.NONE, 4);
		item4.setText("Sculpt");
		item4.setHeight(sculptComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item4.setControl(sculptComposite);
		
		// Listeners
		// **nameCombo listener to update nameCombo and paintNameCombo names for current object.
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
					paintNameCombo.remove(currentIndex);
					paintNameCombo.add(currentText, currentIndex);
					paintNameCombo.select(currentIndex);
					statusNameCombo.remove(currentIndex);
					statusNameCombo.add(currentText, currentIndex);
					statusNameCombo.select(currentIndex);
					PaintViewDescription.getInstance().getCurrentObject().setName(currentText);
				}
			}
		});
		// ***nameCombo listener to update current object and properties displayed when nameCombo 
		//		selection changes.
		nameCombo.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				PaintViewDescription.getInstance().setCurrentObject(GeometryViewDescription.getInstance().getObjectDescriptions().get(nameCombo.getSelectionIndex()));
				visibilityButton.setSelection(PaintViewDescription.getInstance().getCurrentObject().isVisible());
				Color4f passColor4f = PaintViewDescription.getInstance().getCurrentObject().getColor();
				RGB passRGB = new RGB((int) passColor4f.x, (int) passColor4f.z, (int) passColor4f.y);
				Color passColor = new Color(display, passRGB);
				colorDisplayLabel.setBackground(passColor);
				geoTransparencyScale.setSelection((int) PaintViewDescription.getInstance().getCurrentObject().getTransparency());
				geoTransparencyScaleLabel.setText(Integer.toString(geoTransparencyScale.getSelection()));
				if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.ACTIVE) {
					
					statusCombo.select(0);
				}
				else if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.PASSIVE) {
					
					statusCombo.select(1);
				}
				else if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.STATIC) {
					
					statusCombo.select(2);
				}
				pressureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getPressureWeight()));
				intensityText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getTargetIntensity()));
				advectionText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getAdvectionWeight()));
				curvatureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getCurvatureWeight()));
				paintNameCombo.select(nameCombo.getSelectionIndex());
				statusNameCombo.select(nameCombo.getSelectionIndex());
			}
		});
		// ****paintNameCombo listener to update current object and properties displayed when 
		//		paintNameCombo selection changes.
		paintNameCombo.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				PaintViewDescription.getInstance().setCurrentObject(GeometryViewDescription.getInstance().getObjectDescriptions().get(paintNameCombo.getSelectionIndex()));
				visibilityButton.setSelection(PaintViewDescription.getInstance().getCurrentObject().isVisible());
				Color4f passColor4f = PaintViewDescription.getInstance().getCurrentObject().getColor();
				RGB passRGB = new RGB((int) passColor4f.x, (int) passColor4f.z, (int) passColor4f.y);
				Color passColor = new Color(display, passRGB);
				colorDisplayLabel.setBackground(passColor);
				geoTransparencyScale.setSelection((int) PaintViewDescription.getInstance().getCurrentObject().getTransparency());
				geoTransparencyScaleLabel.setText(Integer.toString(geoTransparencyScale.getSelection()));
				if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.ACTIVE) {
					
					statusCombo.select(0);
				}
				else if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.PASSIVE) {
					
					statusCombo.select(1);
				}
				else if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.STATIC) {
					
					statusCombo.select(2);
				}
				pressureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getPressureWeight()));
				intensityText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getTargetIntensity()));
				advectionText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getAdvectionWeight()));
				curvatureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getCurvatureWeight()));
				nameCombo.select(paintNameCombo.getSelectionIndex());
				statusNameCombo.select(paintNameCombo.getSelectionIndex());
			}
		});
		// *****statusNameCombo listener to update current object and properties displayed when 
		//		statusNameCombo selection changes.
		statusNameCombo.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event e) {
				
				PaintViewDescription.getInstance().setCurrentObject(GeometryViewDescription.getInstance().getObjectDescriptions().get(statusNameCombo.getSelectionIndex()));
				visibilityButton.setSelection(PaintViewDescription.getInstance().getCurrentObject().isVisible());
				Color4f passColor4f = PaintViewDescription.getInstance().getCurrentObject().getColor();
				RGB passRGB = new RGB((int) passColor4f.x, (int) passColor4f.z, (int) passColor4f.y);
				Color passColor = new Color(display, passRGB);
				colorDisplayLabel.setBackground(passColor);
				geoTransparencyScale.setSelection((int) PaintViewDescription.getInstance().getCurrentObject().getTransparency());
				geoTransparencyScaleLabel.setText(Integer.toString(geoTransparencyScale.getSelection()));
				if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.ACTIVE) {
					
					statusCombo.select(0);
				}
				else if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.PASSIVE) {
					
					statusCombo.select(1);
				}
				else if (PaintViewDescription.getInstance().getCurrentObject().getStatus() == Status.STATIC) {
					
					statusCombo.select(2);
				}
				pressureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getPressureWeight()));
				intensityText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getTargetIntensity()));
				advectionText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getAdvectionWeight()));
				curvatureText.setText(Float.toString(PaintViewDescription.getInstance().getCurrentObject().getCurvatureWeight()));
				nameCombo.select(statusNameCombo.getSelectionIndex());
				paintNameCombo.select(statusNameCombo.getSelectionIndex());
			}
		});
	}
}
