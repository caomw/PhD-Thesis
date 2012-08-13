package org.imagesci.robopaint;

import javax.vecmath.Color4f;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.imagesci.robopaint.GeometryViewDescription.GeometryViewListener;
import org.imagesci.robopaint.ImageViewDescription.ParameterName;
import org.imagesci.robopaint.ObjectDescription.Status;

public class RoboControlPane implements ImageViewDescription.ImageViewListener,
		GeometryViewDescription.GeometryViewListener {
	final Scale rowScale, colScale, sliceScale;
	final Label rowScaleLabel, colScaleLabel, sliceScaleLabel, colorLabel,
			transparencyScaleLabel;
	final Combo nameCombo, labelStatusCombo, paintNameCombo,
			autosegmentNameCombo;
	final Display display;
	final Label colorDisplayLabel, filenameLabel;
	final Text pressureText, intensityText, curvatureText;// advectionText,
	final Button visibilityButton, autoUpdateButton;

	public RoboControlPane(Composite parent) {
		ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
		display = parent.getDisplay();
		final Shell shell = parent.getShell();
		Composite outerComposite = new Composite(bar, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.wrap = true;
		rowLayout.pack = true;
		rowLayout.justify = true;
		rowLayout.type = SWT.VERTICAL;
		rowLayout.marginLeft = 5;
		rowLayout.marginTop = 5;
		rowLayout.marginRight = 0;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 0;

		outerComposite.setLayout(rowLayout);
		
		// File name label.
		filenameLabel = new Label(outerComposite, SWT.SHADOW_OUT);
		filenameLabel.setText("File: None");
		filenameLabel.setFont(new Font(display,"Arial",10,SWT.BOLD));
		// IMAGE SUBPANEL

		Composite imageComposite = new Composite(outerComposite, SWT.NONE);
		GridLayout imageLayout = new GridLayout(3, false);
		imageLayout.marginLeft = imageLayout.marginTop = 10;
		imageLayout.marginRight = imageLayout.marginBottom = 10;
		imageLayout.verticalSpacing = 10;
		imageComposite.setLayout(imageLayout);

		(new Label(imageComposite, SWT.NONE)).setText("Show Slice");
		Composite showComposite = new Composite(imageComposite, SWT.NONE);
		rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.wrap = false;
		rowLayout.pack = true;
		rowLayout.justify = false;
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 10;
		showComposite.setLayout(rowLayout);
		Label showXLabel = new Label(showComposite, SWT.NONE);
		showXLabel.setText("Row");
		final Button showXButton = new Button(showComposite, SWT.CHECK);
		showXButton.setSelection(true);
		showXButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				ImageViewDescription.getInstance().setShowRow(
						showXButton.getSelection());
			}
		});

		Label showYLabel = new Label(showComposite, SWT.NONE);
		showYLabel.setText("Column");
		final Button showYButton = new Button(showComposite, SWT.CHECK);
		showYButton.setSelection(true);
		showYButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				ImageViewDescription.getInstance().setShowColumn(
						showYButton.getSelection());
			}
		});
		Label showZLabel = new Label(showComposite, SWT.NONE);
		showZLabel.setText("Slice");
		final Button showZButton = new Button(showComposite, SWT.CHECK);
		showZButton.setSelection(true);
		showZButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				ImageViewDescription.getInstance().setShowSlice(
						showZButton.getSelection());
			}
		});
		(new Label(imageComposite, SWT.NONE)).setText(" ");

		// Row slider.
		Label rowLabel = new Label(imageComposite, SWT.NONE);
		rowLabel.setText("Row");
		rowScale = new Scale(imageComposite, SWT.NONE);
		rowScale.setMinimum(1);
		rowScale.setMaximum(100);
		rowScale.setIncrement(10);
		rowScale.setPageIncrement(10);
		rowScaleLabel = new Label(imageComposite, SWT.READ_ONLY);
		rowScaleLabel.setText(Integer.toString(rowScale.getSelection()));
		rowScaleLabel.setAlignment(SWT.RIGHT);
		rowScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				int rowValue = rowScale.getSelection();
				ImageViewDescription.getInstance().setRow(rowValue);
				rowScaleLabel.setText(rowValue + "");
				rowScaleLabel.pack();
			}

		});

		// Column slider.
		Label colLabel = new Label(imageComposite, SWT.NONE);
		colLabel.setText("Column");
		colScale = new Scale(imageComposite, SWT.NONE);
		colScale.setMinimum(1);
		colScale.setMaximum(100);
		colScale.setIncrement(10);
		colScale.setPageIncrement(10);
		colScaleLabel = new Label(imageComposite, SWT.NONE);
		colScaleLabel.setText(Integer.toString(colScale.getSelection()));
		colScaleLabel.setAlignment(SWT.RIGHT);
		colScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				int colValue = colScale.getSelection();
				ImageViewDescription.getInstance().setCol(colValue);
				colScaleLabel.setText(colValue + "");
				colScaleLabel.pack();
			}
		});

		// Slice slider.
		Label sliceLabel = new Label(imageComposite, SWT.NONE);
		sliceLabel.setText("Slice");
		sliceScale = new Scale(imageComposite, SWT.NONE);
		sliceScale.setMinimum(1);
		sliceScale.setMaximum(100);
		sliceScale.setIncrement(10);
		sliceScale.setPageIncrement(10);
		sliceScaleLabel = new Label(imageComposite, SWT.NONE);
		sliceScaleLabel.setText(Integer.toString(sliceScale.getSelection()));
		sliceScaleLabel.setAlignment(SWT.RIGHT);
		sliceScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				int sliceValue = sliceScale.getSelection();
				ImageViewDescription.getInstance().setSlice(sliceValue);
				sliceScaleLabel.setText(sliceValue + "");
				sliceScaleLabel.pack();
			}
		});

		// Contrast slider.
		Label contrastLabel = new Label(imageComposite, SWT.NONE);
		contrastLabel.setText("Contrast");
		final Scale contrastScale = new Scale(imageComposite, SWT.NONE);
		contrastScale.setMinimum(0);
		contrastScale.setMaximum(100);
		contrastScale.setSelection(50);
		contrastScale.setIncrement(10);
		contrastScale.setPageIncrement(10);
		final Label contrastScaleLabel = new Label(imageComposite, SWT.NONE);
		contrastScaleLabel.setText(Integer.toString(contrastScale
				.getSelection()));
		contrastScaleLabel.setAlignment(SWT.RIGHT);
		contrastScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				int contrastValue = contrastScale.getSelection();
				ImageViewDescription.getInstance().setContrast(
						contrastValue / 50.0f);
				contrastScaleLabel.setText(Integer.toString(contrastValue));
				contrastScaleLabel.pack();
			}
		});

		// Brightness slider.
		Label brightnessLabel = new Label(imageComposite, SWT.NONE);
		brightnessLabel.setText("Brightness");
		final Scale brightnessScale = new Scale(imageComposite, SWT.NONE);
		brightnessScale.setMinimum(0);
		brightnessScale.setMaximum(100);
		brightnessScale.setIncrement(10);
		brightnessScale.setSelection(50);
		brightnessScale.setPageIncrement(10);
		final Label brightnessScaleLabel = new Label(imageComposite, SWT.NONE);
		brightnessScaleLabel.setText(Integer.toString(brightnessScale
				.getSelection()));
		brightnessScaleLabel.setAlignment(SWT.RIGHT);
		brightnessScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				int brightnessValue = brightnessScale.getSelection();
				ImageViewDescription.getInstance().setBrightness(
						0.1f * (brightnessValue - 50));
				brightnessScaleLabel.setText(Integer.toString(brightnessValue));
				brightnessScaleLabel.pack();
			}
		});

		// Transparency slider.
		Label transparencyLabel = new Label(imageComposite, SWT.NONE);
		transparencyLabel.setText("Transparency");
		final Scale transparencyScale = new Scale(imageComposite, SWT.NONE);
		transparencyScale.setMinimum(0);
		transparencyScale.setMaximum(100);
		transparencyScale.setIncrement(10);
		transparencyScale.setSelection(50);
		transparencyScale.setPageIncrement(10);
		transparencyScaleLabel = new Label(imageComposite, SWT.NONE);
		transparencyScaleLabel.setText(Integer.toString(transparencyScale
				.getSelection()));
		transparencyScaleLabel.setAlignment(SWT.RIGHT);
		transparencyScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				int transparencyValue = transparencyScale.getSelection();
				ImageViewDescription.getInstance().setTransparency(
						transparencyValue / 100.0f);
				transparencyScaleLabel.setText(Integer
						.toString(transparencyValue));
				transparencyScaleLabel.pack();
			}
		});

		ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
		item0.setText("Image");
		outerComposite.pack();
		item0.setHeight(outerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(outerComposite);

		// GEOMETRY SUBPANEL
		Composite geoComposite = new Composite(bar, SWT.NONE);
		GridLayout geoLayout = new GridLayout(2, false);
		geoLayout.marginLeft = geoLayout.marginTop = geoLayout.marginRight = geoLayout.marginBottom = 10;
		geoLayout.verticalSpacing = 10;
		geoComposite.setLayout(geoLayout);

		// "Slice View" button.
		Label isoLabel = new Label(geoComposite, SWT.NONE);
		isoLabel.setText("Slice View");
		Button isoButton = new Button(geoComposite, SWT.CHECK);
		isoButton.setSelection(false);
		isoButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				if (GeometryViewDescription.getInstance().isSliceView()) {

					GeometryViewDescription.getInstance().setHideAll(false);
				} else {

					GeometryViewDescription.getInstance().setHideAll(true);
				}
			}
		});

		// Object label name dropdown.
		Label geoNameLabel = new Label(geoComposite, SWT.NONE);
		geoNameLabel.setText("Name");
		nameCombo = new Combo(geoComposite, SWT.NONE);
		// *visibilityButton listener to update visible property of current
		// object.
		// **end for nameCombo listener to update nameCombo and paintNameCombo
		// names
		// for current object.
		// ***end for nameCombo listener to update current object and properties
		// displayed when nameCombo selection changes.

		// Color selection button and dialog.
		colorLabel = new Label(geoComposite, SWT.NONE);
		colorLabel.setText("Color");
		Composite colorDialogComposite = new Composite(geoComposite, SWT.NONE);
		GridLayout colorDialogLayout = new GridLayout(2, false);
		colorDialogComposite.setLayout(colorDialogLayout);
		colorDisplayLabel = new Label(colorDialogComposite, SWT.BORDER);
		colorDisplayLabel.setText("     ");
		Button colorButton = new Button(colorDialogComposite, SWT.PUSH);
		colorButton.setText("Change color");
		colorButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				ColorDialog colorDialog = new ColorDialog(shell);
				colorDialog.setText("Change color...");
				RGB newColor = colorDialog.open();
				if (newColor == null) {
					return;
				}
				colorDisplayLabel.setBackground(new Color(display, newColor));
				Color4f passColor = new Color4f(newColor.red / 255.0f,
						newColor.green / 255.0f, newColor.blue / 255.0f, 0);
				GeometryViewDescription.getInstance().getCurrentObject()
						.setColor(passColor);
			}
		});

		// "Visible" button.
		Label geoVisiblityLabel = new Label(geoComposite, SWT.NONE);
		geoVisiblityLabel.setText("Visible");
		visibilityButton = new Button(geoComposite, SWT.CHECK);
		visibilityButton.setSelection(true);
		// *nameCombo section for visibilityButton listener to update visible
		// property of current object.
		visibilityButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				GeometryViewDescription.getInstance().getCurrentObject()
						.setVisible(visibilityButton.getSelection());
			}
		});

		/*
		Label geoTransparencyLabel = new Label(geoComposite, SWT.NONE);
		geoTransparencyLabel.setText("Transparency");
		final Composite geoTransparencyComposite = new Composite(geoComposite,
				SWT.NONE);
		GridLayout geoTransparencyLayout = new GridLayout(2, true);
		geoTransparencyComposite.setLayout(geoTransparencyLayout);
		geoTransparencyScale = new Scale(geoTransparencyComposite, SWT.NONE);
		geoTransparencyScale.setMinimum(0);
		geoTransparencyScale.setMaximum(100);
		geoTransparencyScale.setIncrement(10);
		geoTransparencyScale.setPageIncrement(10);
		geoTransparencyScaleLabel = new Label(geoTransparencyComposite,
				SWT.NONE);
		geoTransparencyScaleLabel.setText(Integer.toString(geoTransparencyScale
				.getSelection()));
		geoTransparencyScaleLabel.setAlignment(SWT.RIGHT);
		geoTransparencyScale.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int geoTransparencyValue = geoTransparencyScale.getSelection();
				GeometryViewDescription.getInstance().getCurrentObject()
						.setTransparency((geoTransparencyValue) / 100.0f);
				geoTransparencyScaleLabel.setText(Integer
						.toString(geoTransparencyValue));
				geoTransparencyScaleLabel.pack();
				geoTransparencyComposite.pack();
			}
		});
		*/

		ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 1);
		item1.setText("Geometry");
		item1.setHeight(geoComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(geoComposite);

		// PAINT SUBPANEL
		Composite paintComposite = new Composite(bar, SWT.NONE);
		GridLayout paintLayout = new GridLayout(2, false);
		paintLayout.marginLeft = paintLayout.marginTop = paintLayout.marginRight = paintLayout.marginBottom = 10;
		paintLayout.verticalSpacing = 10;
		paintComposite.setLayout(paintLayout);

		// Current object selection dropdown.
		Label currentObjectLabel = new Label(paintComposite, SWT.NONE);
		currentObjectLabel.setText("Current object");
		currentObjectLabel.pack();
		paintNameCombo = new Combo(paintComposite, SWT.READ_ONLY);
		String labelArray[] = nameCombo.getItems();
		for (int i = 0; i < labelArray.length - 1; i++) {
			paintNameCombo.add(labelArray[i]);
		}
		paintNameCombo.select(0);
		// ****end for paintNameCombo listener to update current object and
		// properties displayed when
		// paintNameCombo selection changes.
		paintNameCombo.pack();

		// Paint brush size slider.
		Label brushSizeLabel = new Label(paintComposite, SWT.NONE);
		brushSizeLabel.setText("Brush size");
		final Composite brushSizeComposite = new Composite(paintComposite,
				SWT.NONE);
		GridLayout brushSizeLayout = new GridLayout(2, false);
		brushSizeComposite.setLayout(brushSizeLayout);
		final Scale brushSizeScale = new Scale(brushSizeComposite, SWT.NONE);
		brushSizeScale.setMinimum(0);
		brushSizeScale.setMaximum(50);
		brushSizeScale.setPageIncrement(5);
		brushSizeScale.setIncrement(5);
		final Label brushSizeScaleLabel = new Label(brushSizeComposite,
				SWT.NONE);
		brushSizeScaleLabel.setText(Integer.toString(brushSizeScale
				.getSelection()));
		brushSizeScaleLabel.setAlignment(SWT.RIGHT);
		brushSizeScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				brushSizeScaleLabel.setText(Integer.toString(brushSizeScale
						.getSelection()));
				PaintViewDescription.getInstance().setPaintBrushSize(
						brushSizeScale.getSelection());
				brushSizeScaleLabel.pack();
				brushSizeComposite.pack();
			}
		});

		// Paint mask transparency slider.
		Label paintMaskLabel = new Label(paintComposite, SWT.NONE);
		paintMaskLabel.setText("Paint Transparency");
		final Composite paintMaskComposite = new Composite(paintComposite,
				SWT.NONE);
		GridLayout paintMaskLayout = new GridLayout(2, false);
		paintMaskComposite.setLayout(paintMaskLayout);
		final Scale paintMaskScale = new Scale(paintMaskComposite, SWT.NONE);
		paintMaskScale.setMinimum(0);
		paintMaskScale.setMaximum(100);
		paintMaskScale.setIncrement(10);
		paintMaskScale.setPageIncrement(10);
		final Label paintMaskScaleLabel = new Label(paintMaskComposite,
				SWT.NONE);
		paintMaskScaleLabel.setText(Integer.toString(paintMaskScale
				.getSelection()));
		paintMaskScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				paintMaskScaleLabel.setText(Integer.toString(paintMaskScale
						.getSelection()));
				PaintViewDescription.getInstance().setTransparency(
						paintMaskScale.getSelection());
				paintMaskScaleLabel.pack();
				paintMaskComposite.pack();
			}
		});

		// "3D Brush" button.
		Label brush3DLabel = new Label(paintComposite, SWT.NONE);
		brush3DLabel.setText("3D Brush");
		final Button brush3DButton = new Button(paintComposite, SWT.CHECK);
		brush3DButton.setSelection(false);
		brush3DButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				PaintViewDescription.getInstance().setBrush3D(
						brush3DButton.getSelection());
			}
		});

		ExpandItem item2 = new ExpandItem(bar, SWT.NONE, 2);
		item2.setText("Paint");
		item2.setHeight(paintComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item2.setControl(paintComposite);

		// AUTO-SEGMENT SUBPANEL
		Composite segComposite = new Composite(bar, SWT.NONE);
		GridLayout segLayout = new GridLayout(2, false);
		segLayout.marginLeft = segLayout.marginTop = segLayout.marginRight = segLayout.marginBottom = 10;
		segLayout.verticalSpacing = 10;
		segComposite.setLayout(segLayout);

		// Current object and status selection dropdowns.
		autosegmentNameCombo = new Combo(segComposite, SWT.READ_ONLY);
		String statusLabelArray[] = nameCombo.getItems();
		for (int i = 0; i < statusLabelArray.length; i++) {

			autosegmentNameCombo.add(statusLabelArray[i]);
		}
		autosegmentNameCombo.select(0);
		// *****end for statusNameCombo listener to update current object and
		// properties displayed
		// when statusNameCombo selection changes.
		labelStatusCombo = new Combo(segComposite, SWT.READ_ONLY);
		labelStatusCombo.add("Active");
		labelStatusCombo.add("Passive");
		labelStatusCombo.add("Static");
		labelStatusCombo.select(0);
		labelStatusCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				int index = labelStatusCombo.getSelectionIndex();

				if (index == 0) {

					GeometryViewDescription.getInstance().currentObject
							.setStatus(Status.ACTIVE);
				}

				else if (index == 1) {

					GeometryViewDescription.getInstance().getCurrentObject()
							.setStatus(Status.PASSIVE);
				}

				else if (index == 2) {

					GeometryViewDescription.getInstance().getCurrentObject()
							.setStatus(Status.STATIC);
				}
			}
		});

		// Pressure weight editable text box.
		Label pressureLabel = new Label(segComposite, SWT.NONE);
		pressureLabel.setText("Pressure weight");
		pressureText = new Text(segComposite, SWT.BORDER);
		/*
		pressureText.setText(Float.toString(PaintViewDescription.getInstance()
				.getCurrentObject().getPressureWeight()));
		*/
		pressureText.addKeyListener(new KeyListener() {

			String currentText;
			float newValue;

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.CR) {

					currentText = pressureText.getText();

					try {

						newValue = Float.parseFloat(currentText);
					}

					catch (NumberFormatException err) {

						return;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.keyCode == SWT.CR) {

					GeometryViewDescription.getInstance().getCurrentObject()
							.setPressureWeight(newValue);
				}
			}
		});

		// Target intensity editable text box.
		Label intensityLabel = new Label(segComposite, SWT.NONE);
		intensityLabel.setText("Target intensity");
		intensityText = new Text(segComposite, SWT.BORDER);

		intensityText.addKeyListener(new KeyListener() {

			String currentText;
			float newValue;

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.CR) {

					currentText = intensityText.getText();

					try {

						newValue = Float.parseFloat(currentText);
					}

					catch (NumberFormatException err) {

						return;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.keyCode == SWT.CR) {

					GeometryViewDescription.getInstance().getCurrentObject()
							.setTargetIntensity(newValue);
				}
			}
		});
		Label autoUpdate = new Label(segComposite, SWT.NONE);
		autoUpdate.setText("Auto-update intensity");
		autoUpdateButton = new Button(segComposite, SWT.CHECK);
		autoUpdateButton.setSelection(false);
		autoUpdateButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				GeometryViewDescription
						.getInstance()
						.getCurrentObject()
						.setAutoUpdateIntensity(autoUpdateButton.getSelection());

			}
		});
		/*
		// Advection weight editable text box.
		Label advectionLabel = new Label(segComposite, SWT.NONE);
		advectionLabel.setText("Advection weight");
		advectionText = new Text(segComposite, SWT.BORDER);

		advectionText.addKeyListener(new KeyListener() {

			String currentText;
			float newValue;

			@Override
			public void keyPressed(KeyEvent e) {
				
				currentText = advectionText.getText();

				if (e.keyCode == SWT.CR) {

					try {

						newValue = Float.parseFloat(currentText);
					}

					catch (NumberFormatException err) {

						return;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.keyCode == SWT.CR) {

					GeometryViewDescription.getInstance().getCurrentObject()
							.setAdvectionWeight(newValue);
				}
			}
		});
		*/
		// Curvature weight editable text box.
		Label curvatureLabel = new Label(segComposite, SWT.NONE);
		curvatureLabel.setText("Curvature weight");
		curvatureText = new Text(segComposite, SWT.BORDER);

		curvatureText.addKeyListener(new KeyListener() {

			String currentText;
			float newValue;

			@Override
			public void keyPressed(KeyEvent e) {

				currentText = curvatureText.getText();

				if (e.keyCode == SWT.CR) {

					try {

						newValue = Float.parseFloat(currentText);
					}

					catch (NumberFormatException err) {

						return;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.keyCode == SWT.CR) {

					GeometryViewDescription.getInstance().getCurrentObject()
							.setCurvatureWeight(newValue);
				}
			}
		});

		ExpandItem item3 = new ExpandItem(bar, SWT.NONE, 3);
		item3.setText("Automated Paint");
		item3.setHeight(segComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item3.setControl(segComposite);

		// SCULPT SUBPANEL
		Composite sculptComposite = new Composite(bar, SWT.NONE);
		GridLayout sculptLayout = new GridLayout(3, false);
		sculptLayout.marginLeft = sculptLayout.marginTop = sculptLayout.marginRight = sculptLayout.marginBottom = 10;
		sculptLayout.verticalSpacing = 10;
		sculptComposite.setLayout(sculptLayout);

		Label sculptLabel = new Label(sculptComposite, SWT.NONE);
		sculptLabel.setText("Sculpting tool: ");

		// Sculpt tool selection dropdown.
		final Combo sculptCombo = new Combo(sculptComposite, SWT.READ_ONLY);
		sculptCombo.add("Crease");
		sculptCombo.add("Draw");
		sculptCombo.add("Flatten");
		sculptCombo.add("Grab");
		sculptCombo.add("Inflate");
		sculptCombo.add("Pinch");
		sculptCombo.add("Rotate");
		sculptCombo.add("Scale");
		sculptCombo.add("Smooth");
		sculptCombo.select(0);
		SculptViewDescription.getInstance().setCurrentSculpt(
				SculptViewDescription.getInstance().getSculptDescriptions()
						.get(0));

		new Label(sculptComposite, SWT.NONE);

		// Sculpt tool size slider.
		Label sizeLabel = new Label(sculptComposite, SWT.NONE);
		sizeLabel.setText("Size");
		final Scale sizeScale = new Scale(sculptComposite, SWT.NONE);
		sizeScale.setMinimum(0);
		sizeScale.setMaximum(100);
		sizeScale.setIncrement(10);
		sizeScale.setPageIncrement(10);
		final Label sizeScaleLabel = new Label(sculptComposite, SWT.NONE);
		sizeScaleLabel.setText(Integer.toString(sizeScale.getSelection()));
		sizeScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				int sizeValue = sizeScale.getSelection();
				SculptViewDescription.getInstance().getCurrentSculpt()
						.setSize(sizeValue);
				sizeScaleLabel.setText(Integer.toString(sizeValue));
				sizeScaleLabel.pack();
			}
		});

		// Sculpt tool strength slider.
		Label strengthLabel = new Label(sculptComposite, SWT.NONE);
		strengthLabel.setText("Strength");
		final Scale strengthScale = new Scale(sculptComposite, SWT.NONE);
		strengthScale.setMinimum(0);
		strengthScale.setMaximum(100);
		strengthScale.setIncrement(10);
		strengthScale.setPageIncrement(10);
		final Label strengthScaleLabel = new Label(sculptComposite, SWT.NONE);
		strengthScaleLabel.setText(Integer.toString(strengthScale
				.getSelection()));
		strengthScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				int strengthValue = strengthScale.getSelection();
				SculptViewDescription.getInstance().getCurrentSculpt()
						.setStrength(strengthValue);
				strengthScaleLabel.setText(Integer.toString(strengthValue));
				strengthScaleLabel.pack();
			}
		});
		sculptComposite.pack();

		ExpandItem item4 = new ExpandItem(bar, SWT.NONE, 4);
		item4.setText("Sculpt");
		item4.setHeight(sculptComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item4.setControl(sculptComposite);

		// Listeners
		// **nameCombo listener to update nameCombo and paintNameCombo names for
		// current object.
		nameCombo.addKeyListener(new KeyListener() {

			String currentText;

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.CR) {

					currentText = nameCombo.getText();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

				int currentIndex = GeometryViewDescription.getInstance()
						.getCurrentObjectIndex();
				if (e.keyCode == SWT.CR && currentIndex >= 0) {
					nameCombo.remove(currentIndex);
					nameCombo.add(currentText, currentIndex);
					paintNameCombo.remove(currentIndex);
					paintNameCombo.add(currentText, currentIndex);
					paintNameCombo.select(currentIndex);
					autosegmentNameCombo.remove(currentIndex);
					autosegmentNameCombo.add(currentText, currentIndex);
					autosegmentNameCombo.select(currentIndex);
					GeometryViewDescription.getInstance().getCurrentObject()
							.setName(currentText);
				}
			}
		});
		// ***nameCombo listener to update current object and properties
		// displayed when nameCombo
		// selection changes.
		nameCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				GeometryViewDescription.getInstance().setCurrentObject(
						GeometryViewDescription.getInstance()
								.getObjectDescriptions()
								.get(nameCombo.getSelectionIndex()));
				updateCurrentGeometryLabel();
				updateCurrentSegmentParameters();
				paintNameCombo.select(nameCombo.getSelectionIndex());
				autosegmentNameCombo.select(nameCombo.getSelectionIndex());
			}
		});
		// ****paintNameCombo listener to update current object and properties
		// displayed when
		// paintNameCombo selection changes.
		paintNameCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				GeometryViewDescription.getInstance().setCurrentObject(
						GeometryViewDescription.getInstance()
								.getObjectDescriptions()
								.get(paintNameCombo.getSelectionIndex()));
				updateCurrentGeometryLabel();
				updateCurrentSegmentParameters();
				nameCombo.select(paintNameCombo.getSelectionIndex());
				autosegmentNameCombo.select(paintNameCombo.getSelectionIndex());
			}
		});
		// *****statusNameCombo listener to update current object and properties
		// displayed when
		// statusNameCombo selection changes.
		autosegmentNameCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				ObjectDescription obj = GeometryViewDescription.getInstance()
						.getObjectDescriptions()
						.get(autosegmentNameCombo.getSelectionIndex());
				GeometryViewDescription.getInstance().setCurrentObject(obj);
				updateCurrentGeometryLabel();
				updateCurrentSegmentParameters();
				if (obj.getId() != 0) {
					nameCombo.select(autosegmentNameCombo.getSelectionIndex());
					paintNameCombo.select(autosegmentNameCombo
							.getSelectionIndex());
				}
			}
		});

		sculptCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				int idx = sculptCombo.getSelectionIndex();

				SculptViewDescription.getInstance().setCurrentSculpt(
						SculptViewDescription.getInstance()
								.getSculptDescriptions().get(idx));
				sizeScale.setSelection(SculptViewDescription.getInstance()
						.getCurrentSculpt().getSize());
				sizeScaleLabel.setText(Integer.toString(SculptViewDescription
						.getInstance().getCurrentSculpt().getSize()));
				strengthScale.setSelection(SculptViewDescription.getInstance()
						.getCurrentSculpt().getStrength());
				strengthScaleLabel.setText(Integer
						.toString(SculptViewDescription.getInstance()
								.getCurrentSculpt().getStrength()));
			}
		});
	}

	@Override
	public void updateParameter(ImageViewDescription g, ParameterName p) {
		if (p == ParameterName.OPEN_REFERENCE_IMAGE) {
			rowScale.setMaximum(g.getImageRows());
			colScale.setMaximum(g.getImageCols());
			sliceScale.setMaximum(g.getImageSlices());
			rowScale.setSelection(g.getImageRows() / 2);
			colScale.setSelection(g.getImageCols() / 2);
			sliceScale.setSelection(g.getImageSlices() / 2);
			ImageViewDescription.getInstance().setRow(g.getImageRows() / 2);
			ImageViewDescription.getInstance().setCol(g.getImageCols() / 2);
			ImageViewDescription.getInstance().setSlice(g.getImageSlices() / 2);
			rowScaleLabel.setText(rowScale.getSelection() + "");
			colScaleLabel.setText(colScale.getSelection() + "");
			sliceScaleLabel.setText(sliceScale.getSelection() + "");
			filenameLabel.setText(ImageViewDescription.getInstance()
					.getImageFile().getAbsolutePath());
			rowScaleLabel.pack();
			colScaleLabel.pack();
			sliceScaleLabel.pack();
			filenameLabel.pack();
		}
	}

	@Override
	public void updateParameter(GeometryViewDescription g,
			org.imagesci.robopaint.GeometryViewDescription.ParameterName p) {
		switch (p) {
		case REMOVE_ALL_OBJECTS:
			nameCombo.deselectAll();
			nameCombo.removeAll();
			paintNameCombo.deselectAll();
			paintNameCombo.removeAll();
			autosegmentNameCombo.deselectAll();
			autosegmentNameCombo.removeAll();

			break;
		case ADD_OBJECT:
			ObjectDescription obj = GeometryViewDescription
					.getInstance()
					.getObjectDescriptions()
					.get(GeometryViewDescription.getInstance()
							.getObjectDescriptions().size() - 1);
			String name = obj.getName();

			if (obj.getId() != 0) {
				nameCombo.add(name);
				nameCombo.select(0);
				nameCombo.pack();

				paintNameCombo.add(name);
				paintNameCombo.select(0);
				paintNameCombo.pack();
			}

			autosegmentNameCombo.add(name);
			autosegmentNameCombo.select(0);
			autosegmentNameCombo.pack();

			GeometryViewDescription.getInstance().setCurrentObject(
					nameCombo.getSelectionIndex());

			updateCurrentGeometryLabel();
			updateCurrentSegmentParameters();
			break;
		}
	}

	protected void updateCurrentGeometryLabel() {
		ObjectDescription currentObject = GeometryViewDescription.getInstance()
				.getCurrentObject();
		if (currentObject != null && currentObject.getId() != 0) {
			visibilityButton.setSelection(currentObject.isVisible());
			Color4f passColor4f = currentObject.getColor4f();
			RGB passRGB = new RGB((int) (255.0f * passColor4f.x),
					(int) (255.0f * passColor4f.y),
					(int) (255.0f * passColor4f.z));
			Color passColor = new Color(display, passRGB);
			colorDisplayLabel.setBackground(passColor);
			int trans = Math.min(100,
					Math.max(0, (int) (currentObject.getTransparency() * 100)));
			// geoTransparencyScale.setSelection(trans);
			// geoTransparencyScaleLabel.setText(trans + "");
			// geoTransparencyScaleLabel.pack();
		}
	}

	protected void updateCurrentSegmentParameters() {
		ObjectDescription currentObject = GeometryViewDescription.getInstance()
				.getCurrentObject();
		if (currentObject != null) {
			if (GeometryViewDescription.getInstance().getCurrentObject()
					.getStatus() == Status.ACTIVE) {

				labelStatusCombo.select(0);
			} else if (GeometryViewDescription.getInstance().getCurrentObject()
					.getStatus() == Status.PASSIVE) {

				labelStatusCombo.select(1);
			} else if (GeometryViewDescription.getInstance().getCurrentObject()
					.getStatus() == Status.STATIC) {

				labelStatusCombo.select(2);
			}
			pressureText.setText(Float.toString(currentObject
					.getPressureWeight()));
			intensityText.setText(Float.toString(currentObject
					.getTargetIntensity()));
			autoUpdateButton
					.setSelection(currentObject.isAutoUpdateIntensity());
			// advectionText.setText(Float.toString(currentObject.getAdvectionWeight()));
			curvatureText.setText(Float.toString(currentObject
					.getCurvatureWeight()));
		}
	}
}
