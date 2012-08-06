package org.imagesci.robopaint;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;

/**
 * 
 * The ImageViewDescription class provides an object with which to store the
 * parameters for viewing the original images. description is a global
 * ImageViewDescription instance containing the settings for displaying the row,
 * column and slice, as well as settings for the values of the transparency,
 * brightness, and contrast of the original images. The global instance
 * description stores parameters for the entire segmentation project, not only
 * for a single object/label.
 * 
 * @author TYung
 */
public class ImageViewDescription {

	/**
	 * @author TYung
	 * 
	 *         Returns a global instance of an ImageViewListener to update a
	 *         parameter.
	 */
	public static interface ImageViewListener {
		public void updateParameter(ImageViewDescription g, ParameterName p);
	}

	public enum ParameterName {
		CHANGE_BRIGHTNESS, CHANGE_COL, CHANGE_CONTRAST, CHANGE_ROW, CHANGE_SLICE, CHANGE_TRANSPARENCY, CLOSE_IMAGE, OPEN_REFERENCE_IMAGE
	}

	protected static final ImageViewDescription description = new ImageViewDescription();

	/**
	 * Returns a global instance of an ImageViewDescription to store parameters
	 * for a segmentation project.
	 * 
	 * @return a global instance of an ImageViewDescription.
	 */
	public static ImageViewDescription getInstance() {
		return description;
	}

	protected float brightness = 0;
	protected int col = 0;
	protected float contrast = 1;
	protected File file;
	protected ImageData image;
	protected List<ImageViewListener> listeners = new LinkedList<ImageViewListener>();
	protected int row = 0;

	protected boolean showColumn = true;

	protected boolean showRow = true;;

	protected boolean showSlice = true;

	protected int slice = 0;

	protected float transparency = 0.5f;

	public void addListener(ImageViewListener listener) {
		listeners.add(listener);
	}

	/**
	 * Updates a parameter.
	 * 
	 * @param param
	 *            the parameter to be updated.
	 */
	public void fireUpdate(ParameterName param) {
		for (ImageViewListener g : listeners) {
			g.updateParameter(this, param);
		}
	}

	/**
	 * Returns the brightness of the image.
	 * 
	 * @return the current brightness of the image.
	 */
	public float getBrightness() {
		return brightness;
	}

	/**
	 * Returns the current column.
	 * 
	 * @return the current column.
	 */
	public int getCol() {
		return col;
	}

	/**
	 * Returns the contrast of the image.
	 * 
	 * @return the current contrast of the image.
	 */
	public float getContrast() {
		return contrast;
	}

	/**
	 * Returns the image data for the original image.
	 * 
	 * @return the image data.
	 */
	public ImageData getImage() {
		return image;
	}

	/**
	 * Returns the columns of the image.
	 * 
	 * @return the columns of the image.
	 */
	public int getImageCols() {
		return image.getCols();
	}

	/**
	 * Returns the File of the image.
	 * 
	 * @return the File of the image.
	 */
	public File getImageFile() {
		return file;
	}

	/**
	 * Returns the rows of the image.
	 * 
	 * @return the rows of the image.
	 */
	public int getImageRows() {
		return image.getRows();
	}

	/**
	 * Returns the slices of the image.
	 * 
	 * @return the slices of the image.
	 */
	public int getImageSlices() {
		return image.getSlices();
	}

	/**
	 * Returns the current row.
	 * 
	 * @return the current row.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Returns the current slice.
	 * 
	 * @return the current slice.
	 */
	public int getSlice() {
		return slice;
	}

	/**
	 * Returns the transparency of the image.
	 * 
	 * @return the current transparency of the image.
	 */
	public float getTransparency() {
		return transparency;
	}

	/**
	 * Determines whether the column is being shown.
	 * 
	 * @return true if the column is being shown.
	 */
	public boolean isShowColumn() {
		return showColumn;
	}

	/**
	 * Determines if the row is being shown.
	 * 
	 * @return true if the row is being shown.
	 */
	public boolean isShowRow() {
		return showRow;
	}

	/**
	 * Determines whether the slice is being shown.
	 * 
	 * @return true if the slice is being shown.
	 */
	public boolean isShowSlice() {
		return showSlice;
	}

	/**
	 * Sets the brightness of the image.
	 * 
	 * @param brightness
	 *            the new brightness of the image.
	 */
	public void setBrightness(float brightness) {
		this.brightness = brightness;
		fireUpdate(ParameterName.CHANGE_BRIGHTNESS);
	}

	/**
	 * Sets the current column.
	 * 
	 * @param col
	 *            the new column.
	 */
	public void setCol(int col) {
		this.col = col;
		fireUpdate(ParameterName.CHANGE_COL);

	}

	/**
	 * Sets the contrast of the image.
	 * 
	 * @param contrast
	 *            the new contrast of the image.
	 */
	public void setContrast(float contrast) {
		this.contrast = contrast;
		fireUpdate(ParameterName.CHANGE_CONTRAST);
	}

	/**
	 * Sets the File of the image.
	 * 
	 * @param file
	 *            the new File of the image.
	 */
	public void setFile(File file) {
		this.file = file;
		if (file != null) {
			fireUpdate(ParameterName.OPEN_REFERENCE_IMAGE);
		} else {
			fireUpdate(ParameterName.CLOSE_IMAGE);
		}
	}

	public void setReferenceImage(ImageData img) {
		this.image = img;
	}

	/**
	 * Sets the current row.
	 * 
	 * @param row
	 *            the new row.
	 */
	public void setRow(int row) {
		this.row = row;
		fireUpdate(ParameterName.CHANGE_ROW);
	}

	/**
	 * Sets whether the column is being shown.
	 * 
	 * @param showColumn
	 *            true if the column is being shown.
	 */
	public void setShowColumn(boolean showColumn) {
		this.showColumn = showColumn;
	}

	/**
	 * Sets whether the row is being shown.
	 * 
	 * @param showRow
	 *            true if the row is being shown.
	 */
	public void setShowRow(boolean showRow) {
		this.showRow = showRow;
	}

	/**
	 * Sets whether the slice is being shown.
	 * 
	 * @param showSlice
	 *            true if the slice is being shown.
	 */
	public void setShowSlice(boolean showSlice) {
		this.showSlice = showSlice;
	}

	/**
	 * Sets the current slice.
	 * 
	 * @param slice
	 *            the new slice.
	 */
	public void setSlice(int slice) {
		this.slice = slice;
		fireUpdate(ParameterName.CHANGE_SLICE);
	}

	/**
	 * Sets the transparency of the image.
	 * 
	 * @param transparency
	 *            the new transparency of the image.
	 */
	public void setTransparency(float transparency) {
		this.transparency = transparency;
		fireUpdate(ParameterName.CHANGE_TRANSPARENCY);
	}
}
