package org.imagesci.robopaint;

import java.io.File;

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

	protected File file;

	protected static final ImageViewDescription description = new ImageViewDescription();

	public static ImageViewDescription getInstance() {
		return description;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getSlice() {
		return slice;
	}

	public void setSlice(int slice) {
		this.slice = slice;
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

}
