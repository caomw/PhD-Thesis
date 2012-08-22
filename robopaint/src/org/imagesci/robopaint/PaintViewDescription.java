package org.imagesci.robopaint;

/**
 * 
 * The PaintViewDescription class provides an object with which to store the
 * parameters for the Paint tool. description is a global PaintViewDescription
 * instance containing the settings for paint brush size, paint transparency,
 * and whether the brush is 3-dimensional. description also holds the current,
 * active object/label being used. The global instance description stores
 * parameters for the entire segmentation project, not only for a single
 * object/label.
 * 
 * @author TYung
 */
public class PaintViewDescription {
	protected int paintBrushSize = 4;
	protected float transparency = 0.5f;
	protected boolean isBrush3D = true;
	protected static final PaintViewDescription description = new PaintViewDescription();

	/**
	 * Returns the global instance of PaintViewDescription containing the Paint
	 * tool parameter settings.
	 * 
	 * @return the global instance of PaintViewDescription.
	 */
	public static PaintViewDescription getInstance() {
		return description;
	}



	/**
	 * Returns the size of the Paint brush tool.
	 * 
	 * @return the size of the brush.
	 */
	public int getPaintBrushSize() {
		return paintBrushSize;
	}

	/**
	 * Sets the size of the Paint brush tool.
	 * 
	 * @param paintBrushSize
	 *            the new size of the brush.
	 */
	public void setPaintBrushSize(int paintBrushSize) {
		this.paintBrushSize = paintBrushSize;
	}

	/**
	 * Returns the transparency of the Paint tool's paint.
	 * 
	 * @return the transparency of the paint.
	 */
	public float getTransparency() {
		return transparency;
	}

	/**
	 * Determines whether the brush is displayed in 3D.
	 * 
	 * @return true if the brush is displayed in 3D.
	 */
	public boolean isBrush3D() {

		return isBrush3D;
	}

	/**
	 * Sets whether the brush is displayed in 3D.
	 * 
	 * @param isBrush3D
	 *            true if the brush is displayed in 3D.
	 */
	public void setBrush3D(boolean isBrush3D) {

		this.isBrush3D = isBrush3D;
	}

	/**
	 * Sets the transparency of the paint.
	 * 
	 * @param transparency
	 *            the new transparency of the paint.
	 */
	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

}
