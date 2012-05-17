/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 * @author Blake Lucas (blake@cs.jhu.edu)
 */
package edu.jhu.cs.cisst.vent.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import data.PlaceHolder;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.VisualizationProcessing2D;
import edu.jhu.cs.cisst.vent.renderer.processing.ImageRenderer2D;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing2D;
import edu.jhu.cs.cisst.vent.renderer.processing.VectorFieldSliceRenderer2D;
import edu.jhu.cs.cisst.vent.renderer.processing.VolumeIsoContourRenderer;
import edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile.DialogType;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refreshable;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refresher;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.utility.VersionUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationImage.
 */
public class VisualizationImage2D extends VisualizationProcessing2D implements
		Refreshable, MouseWheelListener, SliceNumberDisplay {

	/** The capture param. */
	ParamBoolean captureParam;

	/** The cursor refresh rate. */
	int cursorRefreshRate = 10;

	/** The cursor text color. */
	protected Color cursorTextColor = new Color(255, 255, 0);
	/** The cursor text color param. */
	protected ParamColor cursorTextColorParam;

	/** The maximize. */
	protected boolean maximize = false;

	/** The maximize param. */
	protected ParamBoolean maximizeParam;

	/** The mouse position. */
	PVector mousePosition = new PVector();

	/** The mouse value. */
	String mouseValue = "";

	/** The output file param. */
	ParamFile outputFileParam;
	/** The refresh count. */
	int refreshCount = 0;

	/** The refresher. */
	protected Refresher refresher;

	/** The refresh lock. */
	protected boolean refreshLock = false;

	/** The request update. */
	protected boolean requestUpdate = false;

	/** The rotation rate in radians. */
	protected float rotRate = 0.01f;

	/** The scale param. */
	protected ParamDouble scaleParam;

	/** The scale rate. */
	protected float scaleRate = 0.01f;

	/** The scene params. */
	protected ParamCollection sceneParams;

	/** The show cursor text. */
	protected boolean showCursorText = true;

	/** The show cursor text param. */
	protected ParamBoolean showCursorTextParam;

	/** The scale. */
	protected float tx = 0, ty = 0, scale = 1;

	/** The scale param. */
	protected ParamFloat txParam, tyParam;

	/** The scale rate. */
	protected float wheelScaleRate = 0.1f;

	/**
	 * Instantiates a new visualization image.
	 * 
	 * @param img
	 *            the img
	 */
	public VisualizationImage2D(ImageData img) {
		super();
		addImage(img);
		setName("Visualize Image - " + img.getName());
	}

	/**
	 * Instantiates a new visualization image.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param img
	 *            the img
	 */
	public VisualizationImage2D(int width, int height, ImageData img) {
		super(width, height);
		addImage(img);
		setName("Visualize Image - " + img.getName());
	}

	/**
	 * Instantiates a new visualization image2 d.
	 */
	public VisualizationImage2D() {
		super();
	}

	/**
	 * Instantiates a new visualization image.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 */
	public VisualizationImage2D(int rows, int cols) {
		super(rows, cols);
		setName("Visualize Image");
	}

	/**
	 * Instantiates a new visualization image.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 */
	public VisualizationImage2D(int width, int height, int rows, int cols) {
		super(width, height);
		setName("Visualize Image");
	}

	/**
	 * Adds the image.
	 * 
	 * @param img
	 *            the img
	 */
	public void addImage(ImageData img) {
		if (img.getSlices() <= 1) {
			renderers.add(new ImageRenderer2D(img, this));
		} else {
			renderers.add(new VolumeSliceRenderer2D(img, this));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.SliceNumberDisplay#draw(processing.core
	 * .PApplet, int, int, int)
	 */
	@Override
	public void draw(PApplet applet, int sliceIndex, int totalSlices, int h) {
		applet.noStroke();
		applet.tint(255);
		applet.fill(cursorTextColor.getRGB());
		applet.pushMatrix();
		float textScale = 1;
		textScale = getScale();
		applet.scale(1.0f / textScale);

		applet.text(String.format("%d/%d", sliceIndex, totalSlices), 5,
				textScale * h - 15);
		applet.popMatrix();
		applet.noFill();
	}

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	public float getScale() {

		int rows = 0;
		int cols = 0;

		int size = renderers.size();
		for (int i = size - 1; i >= 0; i--) {
			RendererProcessing2D renderer = renderers.get(i);
			if (renderer instanceof ImageRenderer2D) {
				if (!((ImageRenderer2D) renderer).isVisible()) {
					continue;
				}
				PImage img = ((ImageRenderer2D) renderer).getImage();
				rows = Math.max(rows, img.width);
				cols = Math.max(cols, img.height);
			} else if (renderer instanceof VolumeSliceRenderer2D) {
				if (!((VolumeSliceRenderer2D) renderer).isVisible()) {
					continue;
				}
				// PImage img = ((VolumeSliceRenderer2D)
				// renderer).getImage(((VolumeSliceRenderer2D)
				// renderer).getSliceParameter().getInt()-1);
				rows = Math.max(rows, ((VolumeSliceRenderer2D) renderer).rows);
				cols = Math.max(cols, ((VolumeSliceRenderer2D) renderer).cols);
			}
		}
		if (maximize) {
			return Math.min(width / (float) rows, height / (float) cols);
		} else {
			return scale
					* Math.min(width / (float) rows, height / (float) cols);
		}
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		return VersionUtil.parseRevisionNumber("$Revision: 1.15 $");
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		try {
			File imgFile = (args.length > 0) ? new File(args[0]) : new File(
					PlaceHolder.class.getResource("kinect_rgb.png").toURI());
			ImageData img = PImageReaderWriter.convertToRGB(PImageReaderWriter
					.getInstance().read(imgFile));
			VisualizationImage2D vis = new VisualizationImage2D(600, 600);
			vis.addImage(img);
			VisualizationApplication app = new VisualizationApplication(vis);
			app.setPreferredSize(new Dimension(1024, 768));
			app.runAndWait();
			System.exit(0);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Adds the image.
	 * 
	 * @param img
	 *            the img
	 */
	public void addVectorField(ImageData img) {
		renderers.add(new VectorFieldSliceRenderer2D(img, this));
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing2D#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		super.createVisualizationParameters(visualizationParameters);
		sceneParams = new ParamCollection("Scene Controls");
		sceneParams.add(scaleParam = new ParamDouble("Scale", 0.1, 100, scale));

		scaleParam.setInputView(new ParamDoubleSliderInputView(scaleParam, 4,
				true));

		sceneParams.add(txParam = new ParamFloat("Translation X", -10 * width,
				10 * width, tx));
		sceneParams.add(tyParam = new ParamFloat("Translation Y", -10 * height,
				10 * height, ty));
		sceneParams.add(captureParam = new ParamBoolean("Capture Screenshots",
				false));
		sceneParams.add(outputFileParam = new ParamFile("Output Directory",
				DialogType.DIRECTORY));
		outputFileParam.setValue(new File(""));
		sceneParams.add(maximizeParam = new ParamBoolean("Maximize", maximize));
		visualizationParameters.add(sceneParams);
	}

	/**
	 * Dispose.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#dispose()
	 */
	@Override
	public void dispose() {
		if (refresher != null) {
			refresher.stop();
		}
		super.dispose();
	}

	/**
	 * Draw.
	 * 
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {

		background(255, 255, 255);
		float rows = 0;
		float cols = 0;

		int size = renderers.size();
		for (int i = size - 1; i >= 0; i--) {
			RendererProcessing2D renderer = renderers.get(i);

			if (renderer instanceof ImageRenderer2D) {
				if (!((ImageRenderer2D) renderer).isVisible()) {
					continue;
				}
				PImage img = ((ImageRenderer2D) renderer).getImage();

				rows = Math.max(rows, img.width);
				cols = Math.max(cols, img.height);

			} else if (renderer instanceof VolumeIsoContourRenderer) {
				rows = Math.max(rows, ((VolumeSliceRenderer2D) renderer).rows
						* ((VolumeIsoContourRenderer) renderer).getScaleX());
				cols = Math.max(cols, ((VolumeSliceRenderer2D) renderer).cols
						* ((VolumeIsoContourRenderer) renderer).getScaleY());
			} else if (renderer instanceof VolumeSliceRenderer2D) {
				if (!((VolumeSliceRenderer2D) renderer).isVisible()) {
					continue;
				}
				rows = Math.max(rows, ((VolumeSliceRenderer2D) renderer).rows
						* ((VolumeSliceRenderer2D) renderer).getScaleX());
				cols = Math.max(cols, ((VolumeSliceRenderer2D) renderer).cols
						* ((VolumeSliceRenderer2D) renderer).getScaleY());
			}
		}
		pushMatrix();
		final float effectiveScale = Math.min(width / rows, height / cols);
		if (maximize) {
			scale(effectiveScale, effectiveScale);
		} else {
			translate(rows * 0.5f - tx, cols * 0.5f - ty);

			scale(effectiveScale * scale, effectiveScale * scale);
			translate(-rows * 0.5f / effectiveScale, -cols * 0.5f
					/ effectiveScale);

		}
		super.draw();
		stroke(0, 0, 0);
		noSmooth();
		noFill();
		rect(0, 0, rows, cols);
		popMatrix();
		fill(255);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#getVideoFrames(long,
	 * long)
	 */
	@Override
	public Image[] getVideoFrames(long sampleInterval, long duration) {
		int maxSlice = 0;
		LinkedList<ParamInteger> sliceParams = new LinkedList<ParamInteger>();
		LinkedList<Integer> currentSlice = new LinkedList<Integer>();
		for (RendererProcessing2D renderer : renderers) {
			if (renderer instanceof VolumeSliceRenderer2D) {
				ParamInteger sliceParam = ((VolumeSliceRenderer2D) renderer)
						.getSliceParameter();
				maxSlice = Math.max(maxSlice, sliceParam.getMax().intValue()
						- sliceParam.getMin().intValue() + 1);
				sliceParams.add(sliceParam);
				currentSlice.add(sliceParam.getInt());
				sliceParam.setValue(sliceParam.getMin());
				renderer.updateVisualizationParameters();
			}
		}
		Image[] images = new Image[maxSlice];
		for (int i = 0; i < maxSlice; i++) {
			// Acquire video frames
			images[i] = getScreenshot();
			for (ParamInteger param : sliceParams) {
				param.setValue(param.getValue().intValue() + 1);
			}
			for (RendererProcessing2D renderer : renderers) {
				if (renderer instanceof VolumeSliceRenderer2D) {
					renderer.updateVisualizationParameters();
				}
			}
		}
		int index = 0;
		for (ParamInteger param : sliceParams) {
			param.setValue(currentSlice.get(index++));
		}
		return images;
	}

	/**
	 * Mouse dragged.
	 * 
	 * @see processing.core.PApplet#mouseDragged()
	 */
	@Override
	public void mouseDragged() {
		if ((mouseButton == RIGHT || mouseButton == CENTER) && !keyPressed) {
			scale = Math.max(0, scale + scaleRate * (mouseY - pmouseY));
			scaleParam.setValue(scale);
			requestUpdate = true;
		} else if (mouseButton == LEFT && !keyPressed) {
			tx += (pmouseX - mouseX);
			ty += (pmouseY - mouseY);
			txParam.setValue(tx);
			tyParam.setValue(ty);
			requestUpdate = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
	 * MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scale = Math.max(0, scale - wheelScaleRate * (e.getWheelRotation()));
		scaleParam.setValue(scale);
		requestUpdate = true;
	}

	/**
	 * Refresh.
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.Refreshable#refresh()
	 */
	@Override
	public void refresh() {
		if (requestUpdate) {
			requestUpdate = false;
			refreshLock = true;
			sceneParams.getInputView().update();
			refreshLock = false;
		}
	}

	/**
	 * Setup.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing2D#setup()
	 */
	@Override
	public void setup() {
		super.setup();
		ortho(0, 0, width, height, 0, 256);
		this.addMouseWheelListener(this);
		refresher = new Refresher();
		refresher.add(this);
		refresher.setRefreshInterval(250);
		refresher.start();
		File f;
		try {
			URL url = PlaceHolder.class.getResource("./TheSans-Plain-12.vlw");
			if (url != null) {
				f = new File(url.toURI());
				if (f.exists()) {
					String fontFile = (f.getAbsolutePath());
					textFont(loadFont(fontFile));
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing2D#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		if (!refreshLock) {
			super.update(model, view);
			if (model == txParam) {
				tx = txParam.getFloat();
			} else if (model == tyParam) {
				ty = tyParam.getFloat();
			} else if (model == scaleParam) {
				scale = scaleParam.getFloat();
			} else if (model == maximizeParam) {
				maximize = maximizeParam.getValue();
			} else if (model == showCursorTextParam) {
				showCursorText = showCursorTextParam.getValue();
			} else if (model == cursorTextColorParam) {
				cursorTextColor = cursorTextColorParam.getValue();
			}
		}
		
	}

	/**
	 * Update visualization parameters.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing2D#updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		super.updateVisualizationParameters();
		tx = txParam.getFloat();
		ty = tyParam.getFloat();
		scale = scaleParam.getFloat();
		maximize = maximizeParam.getValue();
		// cursorTextColor = cursorTextColorParam.getValue();
		// showCursorText = showCursorTextParam.getValue();
	}

}
