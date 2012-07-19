package org.imagesci.robopaint;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.imagesci.robopaint.GeometryViewDescription.GeometryViewListener;
import org.imagesci.robopaint.GeometryViewDescription.ParameterName;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;

/**
 * @author TYung
 *
 * The ImageViewDescription class provides an object with which to store the parameters for
 * viewing the original images.
 * description is a global ImageViewDescription instance containing the settings for displaying
 * the row, column and slice, as well as settings for the values of the transparency,
 * brightness, and contrast of the original images.
 * The global instance description stores parameters for the entire segmentation project, not
 * only for a single object/label.
 */
public class ImageViewDescription {

	protected int row = 0;
	protected int col = 0;
	protected int slice = 0;

	protected boolean showRow = true;
	protected boolean showColumn = true;
	protected boolean showSlice = true;
	protected float transparency = 0;
	protected float brightness = 0;
	protected float contrast = 0;
	protected ImageData image;
	protected File file;

	protected static final ImageViewDescription description = new ImageViewDescription();

	/**
	 * Returns the image data for the original image.
	 * @return the image data.
	 */
	public ImageData getImage() {
		return image;
	}

	public int getImageRows() {
		return image.getRows();
	}

	public int getImageCols() {
		return image.getCols();
	}

	public int getImageSlices() {
		return image.getSlices();
	}

	public static ImageViewDescription getInstance() {
		return description;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
		fireUpdate(ParameterName.CHANGE_ROW);
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
		fireUpdate(ParameterName.CHANGE_COL);

	}

	public int getSlice() {
		return slice;
	}

	public void setSlice(int slice) {
		this.slice = slice;
		fireUpdate(ParameterName.CHANGE_SLICE);
	}

	public boolean isShowRow() {
		return showRow;
	}

	public void setShowRow(boolean showRow) {
		this.showRow = showRow;
	}

	public boolean isShowColumn() {
		return showColumn;
	}

	public void setShowColumn(boolean showColumn) {
		this.showColumn = showColumn;
	}

	public boolean isShowSlice() {
		return showSlice;
	}

	public void setShowSlice(boolean showSlice) {
		this.showSlice = showSlice;
	}

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public float getContrast() {
		return contrast;
	}

	public void setContrast(float contrast) {
		this.contrast = contrast;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	public enum ParameterName{CHANGE_ROW,CHANGE_COL,CHANGE_SLICE};
	public static interface ImageViewListener{
		public void updateParameter(ImageViewDescription g,ParameterName p);
	}
	protected List<ImageViewListener> listeners=new LinkedList<ImageViewListener>();
	public void fireUpdate(ParameterName param){
		for(ImageViewListener g:listeners){
			g.updateParameter(this,param);
		}
	}
}
