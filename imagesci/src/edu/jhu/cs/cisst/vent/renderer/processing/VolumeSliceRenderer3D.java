/**
 * ImageSci Toolkit
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
package edu.jhu.cs.cisst.vent.renderer.processing;

import java.awt.Color;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.media.j3d.BoundingBox;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.vecmath.Point3d;

import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.converter.processing.ConvertImageDataToPImage;
import edu.jhu.cs.cisst.vent.resources.PlaceHolder;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamIntegerSliderInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.VoxelType;

// TODO: Auto-generated Javadoc
/**
 * The Class VolumeSliceRenderer3D renders volume by slice in 3D.
 */
public class VolumeSliceRenderer3D extends RendererProcessing3D {

	/** The applet. */
	protected VisualizationProcessing3D applet;

	/** The brightness. */
	protected float brightness = 0;

	/** The brightness param. */
	protected ParamFloat brightnessParam;

	/** The col. */
	protected int col = 0;

	/** The column param. */
	protected ParamInteger colParam;

	/** The component. */
	protected int component = 0;

	/** The slice param. */
	protected ParamInteger componentParam;

	/** The contrast. */
	protected float contrast = 1;

	/** The contrast param. */
	protected ParamFloat contrastParam;

	/** The image. */
	protected ImageData image;
	/** The images. */
	protected PImage[] imagesX = null;

	/** The images. */
	protected PImage[] imagesY = null;

	/** The images. */
	protected PImage[] imagesZ = null;

	/** The max. */
	protected double min, max;

	/** The row. */
	protected int row = 0;

	/** The col param. */
	protected ParamInteger rowParam;

	/** The slices. */
	protected int rows, cols, slices, components;

	/** The scale. */
	protected float scaleX = 1;

	/** The scale y. */
	protected float scaleY = 1;

	/** The scale z. */
	protected float scaleZ = 1;

	/** The show xplane. */
	protected boolean showXplane = true;

	/** The show xplane param. */
	protected ParamBoolean showXplaneParam;

	/** The show yplane. */
	protected boolean showYplane = true;

	/** The show yplane param. */
	protected ParamBoolean showYplaneParam;

	/** The show zplane. */
	protected boolean showZplane = true;

	/** The show zplane param. */
	protected ParamBoolean showZplaneParam;

	/** The slice. */
	protected int slice = 0;

	/** The slice param. */
	protected ParamInteger sliceParam;

	/** The transparency. */
	protected float transparency = 1;

	/** The transparency param. */
	protected ParamFloat transparencyParam;

	/** The vol to image transform. */
	protected PMatrix3D volToImageTransform;

	/**
	 * Instantiates a new volume slice renderer.
	 * 
	 * @param img
	 *            the img
	 * @param volToImageTransform
	 *            the vol to image transform
	 * @param applet
	 *            the applet
	 */
	public VolumeSliceRenderer3D(ImageData img, PMatrix3D volToImageTransform,
			VisualizationProcessing3D applet) {
		this.image = img;
		rows = image.getRows();
		cols = image.getCols();
		slices = image.getSlices();
		this.row = rows / 2;
		this.col = cols / 2;
		this.slice = slices / 2;
		components = image.getComponents();
		min = 1E10f;
		max = -1E10f;
		this.volToImageTransform = volToImageTransform;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					for (int l = 0; l < components; l++) {
						double val = img.getDouble(i, j, k, l);
						min = Math.min(min, val);
						max = Math.max(max, val);
					}
				}
			}
		}
		imagesX = new PImage[rows];
		imagesY = new PImage[cols];
		imagesZ = new PImage[slices];
		this.applet = applet;
	}

	/**
	 * Instantiates a new volume slice renderer3 d.
	 * 
	 * @param img
	 *            the img
	 * @param applet
	 *            the applet
	 */
	public VolumeSliceRenderer3D(ImageData img, VisualizationProcessing3D applet) {
		this(img, null, applet);
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		if (model == sliceParam) {
			setSlice(sliceParam.getInt() - 1);
		} else if (model == componentParam) {
			setComponent(componentParam.getInt() - 1);
		} else if (model == contrastParam) {
			setContrast(contrastParam.getFloat());
		} else if (model == brightnessParam) {
			setBrightness(brightnessParam.getFloat());
		} else if (model == transparencyParam) {
			setTransparency(transparencyParam.getFloat());
		} else if (model == showXplaneParam) {
			setShowXplane(showXplaneParam.getValue());
		} else if (model == showYplaneParam) {
			setShowYplane(showYplaneParam.getValue());
		} else if (model == showZplaneParam) {
			setShowZplane(showZplaneParam.getValue());
		} else if (model == rowParam) {
			setRow(rowParam.getInt() - 1);
		} else if (model == colParam) {
			setCol(colParam.getInt() - 1);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters
	 * ()
	 */
	@Override
	public void updateVisualizationParameters() {
		setRow(rowParam.getInt() - 1);
		setCol(colParam.getInt() - 1);
		setSlice(sliceParam.getInt() - 1);
		setContrast(contrastParam.getFloat());
		setBrightness(brightnessParam.getFloat());
		setTransparency(transparencyParam.getFloat());
		setComponent(componentParam.getInt() - 1);
		setShowXplane(showXplaneParam.getValue());
		setShowYplane(showYplaneParam.getValue());
		setShowZplane(showZplaneParam.getValue());

	}

	/**
	 * Sets the slice.
	 * 
	 * @param slice
	 *            the new slice
	 */
	public void setSlice(int slice) {
		this.slice = slice;
	}

	/**
	 * Sets the component.
	 * 
	 * @param component
	 *            the new component
	 */
	public void setComponent(int component) {
		this.component = component;
		clearCache();
	}

	/**
	 * Sets the contrast.
	 * 
	 * @param contrast
	 *            the new contrast
	 */
	public void setContrast(float contrast) {
		if (contrast != this.contrast) {
			clearCache();
		}
		this.contrast = contrast;

	}

	/**
	 * Sets the brightness.
	 * 
	 * @param brightness
	 *            the new brightness
	 */
	public void setBrightness(float brightness) {
		if (brightness != this.brightness) {
			clearCache();
		}
		this.brightness = brightness;
	}

	/**
	 * Clear cache.
	 */
	protected void clearCache() {
		for (int i = 0; i < imagesZ.length; i++) {
			imagesZ[i] = null;
		}
		for (int i = 0; i < imagesY.length; i++) {
			imagesY[i] = null;
		}
		for (int i = 0; i < imagesX.length; i++) {
			imagesX[i] = null;
		}
	}

	/**
	 * Sets the transparency.
	 * 
	 * @param transparency
	 *            the new transparency
	 */
	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	/**
	 * Sets the show xplane.
	 * 
	 * @param showXplane
	 *            the new show xplane
	 */
	public void setShowXplane(boolean showXplane) {
		this.showXplane = showXplane;
	}

	/**
	 * Sets the show yplane.
	 * 
	 * @param showYplane
	 *            the new show yplane
	 */
	public void setShowYplane(boolean showYplane) {
		this.showYplane = showYplane;
	}

	/**
	 * Sets the show zplane.
	 * 
	 * @param showZplane
	 *            the new show zplane
	 */
	public void setShowZplane(boolean showZplane) {
		this.showZplane = showZplane;
	}

	/**
	 * Sets the row.
	 * 
	 * @param row
	 *            the new row
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Sets the col.
	 * 
	 * @param col
	 *            the new col
	 */
	public void setCol(int col) {
		this.col = col;
	}

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {

		applet.pushStyle();
		((processing.opengl2.PGraphicsOpenGL2) applet.g).gl
				.glDisable(GLLightingFunc.GL_LIGHTING);
		applet.pushMatrix();
		if (volToImageTransform != null) {
			applet.applyMatrix(volToImageTransform);
		}
		applet.scale(scaleX, scaleY, scaleZ);
		applet.tint(255, 255, 255, transparency * 255);
		applet.fill(255, 255, 255);
		applet.stroke(255, 153, 0, transparency * 255);
		if (showZplane) {
			PImage img = getImageZ(slice);
			float w = img.width;
			float h = img.height;
			PImage pimage = img;
			img.setModified(false);
			applet.beginShape(PConstants.QUADS);

			applet.texture(pimage);
			applet.vertex(0, 0, slice + 0.5f, 0, 0);
			applet.vertex(w, 0, slice + 0.5f, 1, 0);
			applet.vertex(w, h, slice + 0.5f, 1, 1);
			applet.vertex(0, h, slice + 0.5f, 0, 1);
			applet.endShape();
		}
		if (showYplane) {
			PImage img = getImageY(col);
			float w = img.width;
			float h = img.height;
			PImage pimage = img;
			img.setModified(false);
			applet.beginShape(PConstants.QUADS);

			applet.texture(pimage);
			applet.vertex(0, col + 0.5f, 0, 0, 0);
			applet.vertex(w, col + 0.5f, 0, 1, 0);
			applet.vertex(w, col + 0.5f, h, 1, 1);
			applet.vertex(0, col + 0.5f, h, 0, 1);
			applet.endShape();

		}
		if (showXplane) {
			PImage img = getImageX(row);
			float w = img.width;
			float h = img.height;
			PImage pimage = img;
			img.setModified(false);
			applet.beginShape(PConstants.QUADS);

			applet.texture(pimage);
			applet.vertex(row + 0.5f, 0, 0, 0, 0);
			applet.vertex(row + 0.5f, w, 0, 1, 0);
			applet.vertex(row + 0.5f, w, h, 1, 1);
			applet.vertex(row + 0.5f, 0, h, 0, 1);
			applet.endShape();

		}
		applet.popMatrix();
		((processing.opengl2.PGraphicsOpenGL2) applet.g).gl
				.glEnable(GLLightingFunc.GL_LIGHTING);
		applet.popStyle();

	}

	/**
	 * Gets the image.
	 * 
	 * @param index
	 *            the index
	 * 
	 * @return the image
	 */
	public PImage getImageZ(int index) {
		if (imagesZ[index] == null) {
			ConvertImageDataToPImage converter = new ConvertImageDataToPImage();
			imagesZ[index] = converter.convertSlice(image, index, component,
					contrast, brightness, min, max);
			imagesZ[index].parent = applet;
		}
		return imagesZ[index];
	}

	/**
	 * Gets the image.
	 * 
	 * @param index
	 *            the index
	 * 
	 * @return the image
	 */
	public PImage getImageY(int index) {
		if (imagesY[index] == null) {
			ConvertImageDataToPImage converter = new ConvertImageDataToPImage();
			imagesY[index] = converter.convertColumn(image, index, component,
					contrast, brightness, min, max);
			imagesY[index].parent = applet;
		}
		return imagesY[index];
	}

	/**
	 * Gets the image.
	 * 
	 * @param index
	 *            the index
	 * 
	 * @return the image
	 */
	public PImage getImageX(int index) {
		if (imagesX[index] == null) {
			ConvertImageDataToPImage converter = new ConvertImageDataToPImage();
			imagesX[index] = converter.convertRow(image, index, component,
					contrast, brightness, min, max);
			imagesX[index].parent = applet;
		}
		return imagesX[index];
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		visualizationParameters.setName("Image");
		visualizationParameters.add(rowParam = new ParamInteger("Row", 1, rows,
				row + 1));
		rowParam.setInputView(new ParamIntegerSliderInputView(rowParam, 4));

		visualizationParameters.add(colParam = new ParamInteger("Column", 1,
				cols, col + 1));
		colParam.setInputView(new ParamIntegerSliderInputView(colParam, 4));

		visualizationParameters.add(sliceParam = new ParamInteger("Slice", 1,
				slices, slice + 1));
		sliceParam.setInputView(new ParamIntegerSliderInputView(sliceParam, 4));
		visualizationParameters.add(componentParam = new ParamInteger(
				"Component", 1, Math.max(1, components), component));
		componentParam.setInputView(new ParamIntegerSliderInputView(
				componentParam, 4));
		visualizationParameters.add(contrastParam = new ParamFloat("Contrast",
				-5, 5, contrast));
		contrastParam.setInputView(new ParamDoubleSliderInputView(
				contrastParam, 4, false));
		visualizationParameters.add(brightnessParam = new ParamFloat(
				"Brightness", -5, 5, brightness));
		brightnessParam.setInputView(new ParamDoubleSliderInputView(
				brightnessParam, 4, false));
		visualizationParameters.add(transparencyParam = new ParamFloat(
				"Transparency", 0, 1, 1));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(showXplaneParam = new ParamBoolean(
				"Show X Plane", showXplane));
		visualizationParameters.add(showYplaneParam = new ParamBoolean(
				"Show Y Plane", showYplane));
		visualizationParameters.add(showZplaneParam = new ParamBoolean(
				"Show Z Plane", showZplane));
	}

	/**
	 * Gets the col.
	 * 
	 * @return the col
	 */
	public int getCol() {
		return col;
	}

	/**
	 * Gets the col parameter.
	 * 
	 * @return the col parameter
	 */
	public ParamInteger getColParameter() {
		return colParam;
	}

	/**
	 * Gets the cols.
	 * 
	 * @return the cols
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * Gets the component.
	 * 
	 * @return the component
	 */
	public int getComponent() {
		return component;
	}

	/**
	 * Gets the components.
	 * 
	 * @return the components
	 */
	public int getComponents() {
		return components;
	}

	/**
	 * Gets the row.
	 * 
	 * @return the row
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Gets the row parameter.
	 * 
	 * @return the row parameter
	 */
	public ParamInteger getRowParameter() {
		return rowParam;
	}

	/**
	 * Gets the rows.
	 * 
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	public float getScaleX() {
		return scaleX;
	}

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	public float getScaleY() {
		return scaleY;
	}

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	public float getScaleZ() {
		return scaleZ;
	}

	/**
	 * Gets the slice.
	 * 
	 * @return the slice
	 */
	public int getSlice() {
		return slice;
	}

	/**
	 * Gets the slice parameter.
	 * 
	 * @return the slice parameter
	 */
	public ParamInteger getSliceParameter() {
		return sliceParam;
	}

	/**
	 * Gets the slices.
	 * 
	 * @return the slices
	 */
	public int getSlices() {
		return slices;
	}

	/**
	 * Gets the value string.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * 
	 * @return the value string
	 */
	public String getValueString(int x, int y) {
		if (x < 0 || x >= rows || y < 0 || y >= cols) {
			return "";
		}
		VoxelType type = image.getType();
		switch (type) {
		case COLOR:
		case COLOR_FLOAT:
		case COLOR_USHORT:
			Color c = image.getColor(x, y, slice, component);
			return String.format("(%d,%d,%d,%d)", c.getRed(), c.getGreen(),
					c.getBlue(), c.getAlpha());
		default:
			return String.format("%4.3f",
					image.getFloat(x, y, slice, component));
		}
	}

	/**
	 * Checks if is show xplane.
	 * 
	 * @return true, if is show xplane
	 */
	public boolean isShowXplane() {
		return showXplane;
	}

	/**
	 * Checks if is show yplane.
	 * 
	 * @return true, if is show yplane
	 */
	public boolean isShowYplane() {
		return showYplane;
	}

	/**
	 * Checks if is show zplane.
	 * 
	 * @return true, if is show zplane
	 */
	public boolean isShowZplane() {
		return showZplane;
	}

	/**
	 * Match scale.
	 * 
	 * @param img
	 *            the img
	 */
	public void matchScale(ImageData img) {
		this.scaleX = img.getRows() / (float) image.getRows();
		this.scaleY = img.getCols() / (float) image.getCols();
		this.scaleZ = img.getSlices() / (float) image.getSlices();
	}

	/**
	 * Sets the cols.
	 * 
	 * @param cols
	 *            the new cols
	 */
	public void setCols(int cols) {
		this.cols = cols;
	}

	/**
	 * Sets the images.
	 * 
	 * @param images
	 *            the new images
	 */
	public void setImages(PImage[] images) {
		this.imagesZ = images;
	}

	/**
	 * Sets the rows.
	 * 
	 * @param rows
	 *            the new rows
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * Sets the scale.
	 * 
	 * @param scale
	 *            the new scale
	 */
	public void setScale(float scale) {
		this.scaleX = this.scaleY = this.scaleZ = scale;
	}

	/**
	 * Sets the scale.
	 * 
	 * @param scaleX
	 *            the scale x
	 * @param scaleY
	 *            the scale y
	 * @param scaleZ
	 *            the scale z
	 */
	public void setScale(float scaleX, float scaleY, float scaleZ) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}

	/**
	 * Sets the slices.
	 * 
	 * @param slices
	 *            the new slices
	 */
	public void setSlices(int slices) {
		this.slices = slices;
	}

	/**
	 * Setup.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {

		if (volToImageTransform != null) {
			bbox = new BoundingBox();
			PVector corner = new PVector();
			PVector result = new PVector();
			corner.x = 0;
			corner.y = cols * scaleY;
			corner.z = rows * scaleY;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
			corner.x = 0;
			corner.y = cols * scaleY;
			corner.z = 0;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
			corner.x = 0;
			corner.y = 0;
			corner.z = rows * scaleY;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
			corner.x = 0;
			corner.y = 0;
			corner.z = 0;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
			corner.x = rows * scaleY;
			corner.y = cols * scaleY;
			corner.z = rows * scaleY;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
			corner.x = rows * scaleY;
			corner.y = cols * scaleY;
			corner.z = 0;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
			corner.x = rows * scaleY;
			corner.y = 0;
			corner.z = rows * scaleY;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
			corner.x = rows * scaleY;
			corner.y = 0;
			corner.z = 0;

			volToImageTransform.mult(corner, result);
			bbox.combine(new Point3d(result.x, result.y, result.z));
		} else {
			bbox.combine(new Point3d(0, 0, 0));
			bbox.combine(new Point3d(0, 0, slices * scaleZ));
			bbox.combine(new Point3d(0, cols * scaleY, 0));
			bbox.combine(new Point3d(0, cols * scaleY, slices * scaleZ));
			bbox.combine(new Point3d(rows * scaleX, 0, 0));
			bbox.combine(new Point3d(rows * scaleX, 0, slices * scaleZ));
			bbox.combine(new Point3d(rows * scaleX, cols * scaleY, 0));
			bbox.combine(new Point3d(rows * scaleX, cols * scaleY, slices
					* scaleZ));
		}
		try {
			URL url = PlaceHolder.class.getResource("./TheSans-Plain-12.vlw");
			if (url != null) {
				File f = new File(url.toURI());
				if (f.exists()) {
					String fontFile = (f.getAbsolutePath());
					applet.textFont(applet.loadFont(fontFile));
				}
			}
		} catch (URISyntaxException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		applet.textureMode(PConstants.NORMALIZED);
	}
}
