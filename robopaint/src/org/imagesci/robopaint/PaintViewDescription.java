package org.imagesci.robopaint;

public class PaintViewDescription {
	protected ObjectDescription currentObject;
	protected int paintBrushSize = 10;
	protected float transparency = 0.5f;
	protected boolean isBrush3D = true;
	protected static final PaintViewDescription description = new PaintViewDescription();

	public PaintViewDescription getInstance() {
		return description;
	}

	public ObjectDescription getCurrentObject() {
		return currentObject;
	}

	public void setCurrentObject(ObjectDescription currentObject) {
		this.currentObject = currentObject;
	}

	public int getPaintBrushSize() {
		return paintBrushSize;
	}

	public void setPaintBrushSize(int paintBrushSize) {
		this.paintBrushSize = paintBrushSize;
	}

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

}
