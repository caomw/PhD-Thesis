package org.imagesci.robopaint;

public class PaintViewDescription {
	protected ObjectDescription currentObject;
	protected int paintBrushSize = 10;
	protected float transparency = 0.5f;
	protected boolean isBrush3D = true;
	protected static final PaintViewDescription description = new PaintViewDescription();

	public static PaintViewDescription getInstance() {
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
	
	public boolean isBrush3D() {
		
		return isBrush3D;
	}
	
	public void setBrush3D(boolean isBrush3D) {
		
		this.isBrush3D = isBrush3D;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

}
