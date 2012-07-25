package org.imagesci.robopaint;

import java.util.ArrayList;
import java.util.List;

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

	protected static final SculptViewDescription description = new SculptViewDescription();
	protected List<SculptDescription> sculptDescriptions;
	protected SculptDescription currentSculpt;
	
	public SculptViewDescription() {
		
		sculptDescriptions = new ArrayList<SculptDescription>();
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
	 * Returns a List of SculptDescriptions for the Sculpting tools in SculptViewDescription.
	 * @return a List of SculptDescription instances.
	 */
	public List<SculptDescription> getSculptDescriptions() {
		
		return sculptDescriptions;
	}
	
	/**
	 * Sets the List of SculptDescription instances.
	 * @param sculptDescriptions the new List of SculptDescription instances.
	 */
	public void setSculptDescriptions(List<SculptDescription> sculptDescriptions) {
		
		this.sculptDescriptions = sculptDescriptions;
	}
	
	/**
	 * Returns the SculptDescription associated with the Sculpting tool currently in use.
	 * @return the SculptDescription instance of the tool currently in use.
	 */
	public SculptDescription getCurrentSculpt() {
		
		return currentSculpt;
	}
	
	/**
	 * Sets the SculptDescription associated with the Sculpting tool currently in use.
	 * @param currentSculpt the SculptDescription of the new tool in use.
	 */
	public void setCurrentSculpt(SculptDescription currentSculpt) {
		
		this.currentSculpt = currentSculpt;
	}
}
