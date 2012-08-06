package org.imagesci.robopaint;

import javax.vecmath.Color4f;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
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
	final Combo nameCombo, statusCombo, paintNameCombo, statusNameCombo;
	final Display display;
	final Label colorDisplayLabel;
	final Text pressureText, intensityText, advectionText, curvatureText;
	final Button visibilityButton;

	public RoboControlPane(Composite parent) {
		ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
		display = parent.getDisplay();
		final Shell shell = parent.getShell();

		// Image item
		Composite imageComposite = new Composite(bar, SWT.NONE);
		GridLayout imageLayout = new GridLayout(3, false);
		imageLayout.marginLeft = imageLayout.marginTop = imageLayout.marginRight = imageLayout.marginBottom = 10;
		imageLayout.verticalSpacing = 10;
		imageComposite.setLayout(imageLayout);

		Label filenameLabel = new Label(imageComposite, SWT.NONE);
		filenameLabel.setText("Filename.img");
		new Label(imageComposite, SWT.NONE);
		new Label(imageComposite, SWT.NONE);

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
		item0.setHeight(imageComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(imageComposite);

		// Geometry item
		Composite geoComposite = new Composite(bar, SWT.NONE);
		GridLayout geoLayout = new GridLayout(2, false);
		geoLayout.marginLeft = geoLayout.marginTop = geoLayout.marginRight = geoLayout.marginBottom = 10;
		geoLayout.verticalSpacing = 10;
		geoComposite.setLayout(geoLayout);

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

		// Paint item
		Composite paintComposite = new Composite(bar, SWT.NONE);
		GridLayout paintLayout = new GridLayout(2, false);
		paintLayout.marginLeft = paintLayout.marginTop = paintLayout.marginRight = paintLayout.marginBottom = 10;
		paintLayout.verticalSpacing = 10;
		paintComposite.setLayout(paintLayout);

		Label currentObjectLabel = new Label(paintComposite, SWT.NONE);
		currentObjectLabel.setText("Current object");
		currentObjectLabel.pack();
		paintNameCombo = new Combo(paintComposite, SWT.READ_ONLY);
		String labelArray[] = nameCombo.getItems();
		for (int i = 0; i < labelArray.length; i++) {
			paintNameCombo.add(labelArray[i]);
		}
		paintNameCombo.select(0);
		// ****end for paintNameCombo listener to update current object and
		// properties displayed when
		// paintNameCombo selection changes.
		paintNameCombo.pack();

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

		Label brush3DLabel = new Label(paintComposite, SWT.NONE);
		brush3DLabel.setText("3D Brush");
		Button brush3DButton = new Button(paintComposite, SWT.CHECK);
		brush3DButton.setSelection(false);
		brush3DButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				if (PaintViewDescription.getInstance().isBrush3D()) {

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

		statusNameCombo = new Combo(segComposite, SWT.READ_ONLY);
		String statusLabelArray[] = nameCombo.getItems();
		for (int i = 0; i < statusLabelArray.length; i++) {

			statusNameCombo.add(statusLabelArray[i]);
		}
		statusNameCombo.select(0);
		// *****end for statusNameCombo listener to update current object and
		// properties displayed
		// when statusNameCombo selection changes.
		statusCombo = new Combo(segComposite, SWT.READ_ONLY);
		statusCombo.add("Active");
		statusCombo.add("Passive");
		statusCombo.add("Static");
		statusCombo.select(0);
		statusCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				int index = statusCombo.getSelectionIndex();

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
		item3.setText("Auto-segment");
		item3.setHeight(segComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item3.setControl(segComposite);

		// Sculpt item
		Composite sculptComposite = new Composite(bar, SWT.NONE);
		GridLayout sculptLayout = new GridLayout(3, false);
		sculptLayout.marginLeft = sculptLayout.marginTop = sculptLayout.marginRight = sculptLayout.marginBottom = 10;
		sculptLayout.verticalSpacing = 10;
		sculptComposite.setLayout(sculptLayout);

		Label sculptLabel = new Label(sculptComposite, SWT.NONE);
		sculptLabel.setText("Sculpting tool: ");

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
				if (e.keyCode == SWT.CR&&currentIndex>=0) {
					nameCombo.remove(currentIndex);
					nameCombo.add(currentText, currentIndex);
					paintNameCombo.remove(currentIndex);
					paintNameCombo.add(currentText, currentIndex);
					paintNameCombo.select(currentIndex);
					statusNameCombo.remove(currentIndex);
					statusNameCombo.add(currentText, currentIndex);
					statusNameCombo.select(currentIndex);
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
				statusNameCombo.select(nameCombo.getSelectionIndex());
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
				statusNameCombo.select(paintNameCombo.getSelectionIndex());
			}
		});
		// *****statusNameCombo listener to update current object and properties
		// displayed when
		// statusNameCombo selection changes.
		statusNameCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {

				GeometryViewDescription.getInstance().setCurrentObject(
						GeometryViewDescription.getInstance()
								.getObjectDescriptions()
								.get(statusNameCombo.getSelectionIndex()));
				updateCurrentGeometryLabel();
				updateCurrentSegmentParameters();

				nameCombo.select(statusNameCombo.getSelectionIndex());
				paintNameCombo.select(statusNameCombo.getSelectionIndex());
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
			rowScaleLabel.pack();
			colScaleLabel.pack();
			sliceScaleLabel.pack();
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
			statusNameCombo.deselectAll();
			statusNameCombo.removeAll();

			break;
		case ADD_OBJECT:
			String name = GeometryViewDescription
					.getInstance()
					.getObjectDescriptions()
					.get(GeometryViewDescription.getInstance()
							.getObjectDescriptions().size() - 1).getName();
			nameCombo.add(name);
			nameCombo.select(0);
			nameCombo.pack();

			paintNameCombo.add(name);
			paintNameCombo.select(0);
			paintNameCombo.pack();

			statusNameCombo.add(name);
			statusNameCombo.select(0);
			statusNameCombo.pack();

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
		if (currentObject != null) {
			visibilityButton.setSelection(currentObject.isVisible());
			Color4f passColor4f = currentObject.getColor4f();
			RGB passRGB = new RGB((int) (255.0f * passColor4f.x),
					(int) (255.0f * passColor4f.y),
					(int) (255.0f * passColor4f.z));
			Color passColor = new Color(display, passRGB);
			colorDisplayLabel.setBackground(passColor);
			int trans = Math.min(100,
					Math.max(0, (int) (currentObject.getTransparency() * 100)));
			//geoTransparencyScale.setSelection(trans);
			//geoTransparencyScaleLabel.setText(trans + "");
			//geoTransparencyScaleLabel.pack();
		}
	}

	protected void updateCurrentSegmentParameters() {
		ObjectDescription currentObject = GeometryViewDescription.getInstance()
				.getCurrentObject();
		if (currentObject != null) {
			if (GeometryViewDescription.getInstance().getCurrentObject()
					.getStatus() == Status.ACTIVE) {

				statusCombo.select(0);
			} else if (GeometryViewDescription.getInstance().getCurrentObject()
					.getStatus() == Status.PASSIVE) {

				statusCombo.select(1);
			} else if (GeometryViewDescription.getInstance().getCurrentObject()
					.getStatus() == Status.STATIC) {

				statusCombo.select(2);
			}
			pressureText.setText(Float.toString(currentObject
					.getPressureWeight()));
			intensityText.setText(Float.toString(currentObject
					.getTargetIntensity()));
			advectionText.setText(Float.toString(currentObject
					.getAdvectionWeight()));
			curvatureText.setText(Float.toString(currentObject
					.getCurvatureWeight()));
		}
	}
}
