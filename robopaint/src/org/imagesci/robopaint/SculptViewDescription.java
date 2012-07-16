package org.imagesci.robopaint;

public class SculptViewDescription {

	protected boolean isCrease = false;
	protected boolean isRotate = false;
	protected boolean isScale = false;
	protected boolean isDraw = true;
	protected boolean isFlatten = false;
	protected boolean isGrab = false;
	protected boolean isInflate = false;
	protected boolean isPinch = false;
	protected boolean isSmooth = false;
	
	protected int sculptSize = 0;
	protected int sculptStrength = 0;
	
	protected static final SculptViewDescription description = new SculptViewDescription();
	
	public boolean isCrease() {
		
		return isCrease;
	}
	
	public boolean isRotate() {
		
		return isRotate;
	}
	
	public boolean isScale() {
		
		return isScale;
	}
	
	public boolean isDraw() {
		
		return isDraw;
	}
	
	public boolean isFlatten() {
		
		return isFlatten;
	}
	
	public boolean isGrab() {
		
		return isGrab;
	}
	
	public boolean isInflate() {
		
		return isInflate;
	}
	
	public boolean isPinch() {
		
		return isPinch;
	}
	
	public boolean isSmooth() {
		
		return isSmooth;
	}
	
	public int getSculptSize() {
		
		return sculptSize;
	}
	
	public int getSculptStrength() {
		
		return sculptStrength;
	}
	
	public static SculptViewDescription getInstance() {
		
		return description;
	}
	
	public void setCrease(boolean isCrease) {
		
		this.isCrease = isCrease;
	}
	
	public void setRotate(boolean isRotate) {
		
		this.isRotate = isRotate;
	}
	
	public void setScale(boolean isScale) {
		
		this.isScale = isScale;
	}
	
	public void setDraw(boolean isDraw) {
		
		this.isDraw = isDraw;
	}
	
	public void setFlatten(boolean isFlatten) {
		
		this.isFlatten = isFlatten;
	}
	
	public void setGrab(boolean isGrab) {
		
		this.isGrab = isGrab;
	}
	
	public void setInflate(boolean isInflate) {
		
		this.isInflate = isInflate;
	}
	
	public void setPinch(boolean isPinch) {
		
		this.isPinch = isPinch;
	}
	
	public void setSmooth(boolean isSmooth) {
		
		this.isSmooth = isSmooth;
	}
	
	public void setSculptSize(int sculptSize) {
		
		this.sculptSize = sculptSize;
	}
	
	public void setSculptStrength(int sculptStrength) {
		
		this.sculptStrength = sculptStrength;
	}
	
	public void setAllFalse() {
		
		this.isCrease = false;
		this.isRotate = false;
		this.isScale = false;
		this.isDraw = false;
		this.isFlatten = false;
		this.isGrab = false;
		this.isInflate = false;
		this.isPinch = false;
		this.isSmooth = false;
	}
}
