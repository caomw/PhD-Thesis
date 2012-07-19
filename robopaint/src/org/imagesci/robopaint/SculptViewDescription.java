package org.imagesci.robopaint;

/**
 * @author TYung
 * 
 * The SculptViewDescription class provides an object with which to store the parameters for 
 * the Sculpting tool.
 * description is a global SculptViewDescription instance with boolean variables indicating
 * which sculpting tool is active. A true boolean indicates the active tool; all other tools
 * yield false and are inactive.
 * The global instance description stores parameters for the entire segmentation project, not
 * only for a single object/label.
 */
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
	
	/**
	 * Determines whether the Crease tool is active.
	 * @return true if Crease is active.
	 */
	public boolean isCrease() {
		
		return isCrease;
	}
	
	/**
	 * Determines whether the Rotate tool is active.
	 * @return true if Rotate is active.
	 */
	public boolean isRotate() {
		
		return isRotate;
	}
	
	/**
	 * Determines whether the Scale tool is active.
	 * @return true if Scale is active.
	 */
	public boolean isScale() {
		
		return isScale;
	}
	
	/**
	 * Determines whether the Draw tool is active.
	 * @return true if Draw is active.
	 */
	public boolean isDraw() {
		
		return isDraw;
	}
	
	/**
	 * Determines whether the Flatten tool is active.
	 * @return true if Flatten is active.
	 */
	public boolean isFlatten() {
		
		return isFlatten;
	}
	
	/**
	 * Determines whether the Grab tool is active.
	 * @return true if Grab is active.
	 */
	public boolean isGrab() {
		
		return isGrab;
	}
	
	/**
	 * Determines whether the Inflate tool is active.
	 * @return true if Inflate is active.
	 */
	public boolean isInflate() {
		
		return isInflate;
	}
	
	/**
	 * Determines whether the Pinch tool is active.
	 * @return true if Pinch is active.
	 */
	public boolean isPinch() {
		
		return isPinch;
	}
	
	/**
	 * Determines whether the Smooth tool is active.
	 * @return true if Smooth is active.
	 */
	public boolean isSmooth() {
		
		return isSmooth;
	}
	
	/**
	 * Returns the size of the sculpting tool.
	 * @return the size of the sculpting tool.
	 */
	public int getSculptSize() {
		
		return sculptSize;
	}
	
	/**
	 * Returns the strength of the sculpting tool.
	 * @return the strength of the sculpting tool.
	 */
	public int getSculptStrength() {
		
		return sculptStrength;
	}
	
	/**
	 * Returns the global instance of SculptViewDescription containing the Sculpting tool
	 * parameter settings.
	 * @return the global instance of SculptViewDescription.
	 */
	public static SculptViewDescription getInstance() {
		
		return description;
	}
	
	/**
	 * Sets whether the Crease tool is active.
	 * @param isCrease true if Crease is active.
	 */
	public void setCrease(boolean isCrease) {
		
		this.isCrease = isCrease;
	}
	
	/**
	 * Sets whether the Rotate tool is active.
	 * @param isRotate true if Rotate is active.
	 */
	public void setRotate(boolean isRotate) {
		
		this.isRotate = isRotate;
	}
	
	/**
	 * Sets whether the Scale tool is active.
	 * @param isScale true if Scale is active.
	 */
	public void setScale(boolean isScale) {
		
		this.isScale = isScale;
	}
	
	/**
	 * Sets whether the Draw tool is active.
	 * @param isDraw true if Draw is active.
	 */
	public void setDraw(boolean isDraw) {
		
		this.isDraw = isDraw;
	}
	
	/**
	 * Sets whether the Flatten tool is active.
	 * @param isFlatten true if Flatten is active.
	 */
	public void setFlatten(boolean isFlatten) {
		
		this.isFlatten = isFlatten;
	}
	
	/**
	 * Sets whether the Grab tool is active.
	 * @param isGrab true if Grab is active.
	 */
	public void setGrab(boolean isGrab) {
		
		this.isGrab = isGrab;
	}
	
	/**
	 * Sets whether the Inflate tool is active.
	 * @param isInflate true if Inflate is active.
	 */
	public void setInflate(boolean isInflate) {
		
		this.isInflate = isInflate;
	}
	
	/**
	 * Sets whether the Pinch tool is active.
	 * @param isPinch true if Pinch is active.
	 */
	public void setPinch(boolean isPinch) {
		
		this.isPinch = isPinch;
	}
	
	/**
	 * Sets whether the Smooth tool is active.
	 * @param isSmooth true if Smooth is active.
	 */
	public void setSmooth(boolean isSmooth) {
		
		this.isSmooth = isSmooth;
	}
	
	/**
	 * Sets the size of the sculpting tool.
	 * @param sculptSize the new size of the sculpting tool.
	 */
	public void setSculptSize(int sculptSize) {
		
		this.sculptSize = sculptSize;
	}
	
	/**
	 * Sets the strength of the sculpting tool.
	 * @param sculptStrength the new strength of the sculpting tool.
	 */
	public void setSculptStrength(int sculptStrength) {
		
		this.sculptStrength = sculptStrength;
	}
	
	/**
	 * Sets all sculpting tools to inactive (false).
	 */
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
