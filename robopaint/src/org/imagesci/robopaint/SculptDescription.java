package org.imagesci.robopaint;

/**
 *
 * The SculptDescription class provides an object with which to store the parameters for
 * individual Sculpting tools.
 * 
 * @author TYung
 */
public class SculptDescription {

	protected String name = "";
	protected int size = 0;
	protected int strength = 0;
	
	/**
	 * Creates an instance of a SculptDescription.
	 * @param name the name of the Sculpting tool to be associated with this SculptDescription.
	 * @param size the size of the tool.
	 * @param strength the strength of the tool.
	 */
	public SculptDescription(String name) {
		
		this.name = name;
	}
	
	/**
	 * Returns the name of the Sculpting tool associated with the SculptDescription.
	 * @return the name of the tool.
	 */
	public String getName() {
		
		return name;
	}
	
	/**
	 * Sets the name of the Sculpting tool associated with the SculptDescription.
	 * @param name the new name of the tool.
	 */
	public void setName(String name) {
		
		this.name = name;
	}
	
	/**
	 * Returns the size of the Sculpting tool associated with the SculptDescription.
	 * @return the size of the tool.
	 */
	public int getSize() {
		
		return size;
	}
	
	/**
	 * Sets the size of the Sculpting tool associated with the SculptDescription.
	 * @param size the new size of the tool.
	 */
	public void setSize(int size) {
		
		this.size = size;
	}
	
	/**
	 * Returns the strength of the Sculpting tool associated with the SculptDescription.
	 * @return the strength of the tool.
	 */
	public int getStrength() {
		
		return strength;
	}
	
	/**
	 * Sets the strength of the Sculpting tool associated with the SculptDescription.
	 * @param strength the new strength of the tool.
	 */
	public void setStrength(int strength) {
		
		this.strength = strength;
	}
}
