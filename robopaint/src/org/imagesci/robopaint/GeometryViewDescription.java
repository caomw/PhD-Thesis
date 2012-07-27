package org.imagesci.robopaint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 * The GeometryViewDescription class provides an object with which to store the parameters for
 * viewing the segmentation of the original images.
 * description is a global GeometryViewDescription instance containing the setting for displaying
 * or hiding all objects/labels.
 * The global instance description stores parameters for the entire segmentation project, not
 * only for a single object/label.
 * 
 * @author TYung
 */
public class GeometryViewDescription {
	protected List<ObjectDescription> objectDescriptions;
	protected boolean hideAll = false;
	protected static final GeometryViewDescription description = new GeometryViewDescription();
	protected ExpandBar bar;
	protected ExpandItem item;
	public enum ParameterName{HIDE_ALL};
	public static interface GeometryViewListener{
		public void updateParameter(GeometryViewDescription g,ParameterName p);
	}
	protected List<GeometryViewListener> listeners=new LinkedList<GeometryViewListener>();
	
	/**
	 * Updates a specified GeometryViewDescription parameter.
	 * @param param the parameter to be updated.
	 */
	public void fireUpdate(ParameterName param){
		for(GeometryViewListener g:listeners){
			g.updateParameter(this,param);
		}
	}
	
	/**
	 * Returns a global instance of a GeometryViewDescription to store parameters for a
	 * segmentation project.
	 * @return a global instance of a GeometryViewDescription.
	 */
	public static GeometryViewDescription getInstance() {
		return description;
	}

	/**
	 * Constructs an instance of GeometryViewDescription.
	 */
	public GeometryViewDescription() {
		objectDescriptions = new ArrayList<ObjectDescription>();
	}

	/**
	 * Returns a list of ObjectDescriptions instances associated with the existing objects.
	 * @return a list of ObjectDescriptions.
	 */
	public List<ObjectDescription> getObjectDescriptions() {
		return objectDescriptions;
	}

	/**
	 * Sets the list of ObjectDescription instances.
	 * @param objectDescriptions a list of ObjectDescriptions.
	 */
	public void setObjectDescriptions(List<ObjectDescription> objectDescriptions) {
		this.objectDescriptions = objectDescriptions;
	}

	/**
	 * Determines whether all segmentations are hidden in the rendering pane.
	 * @return true if all segmentations are hidden.
	 */
	public boolean isHideAll() {
		return hideAll;
	}

	/**
	 * Sets whether all segmentations are hidden in the rendering pane.
	 * @param hideAll true if all segmentations are hidden.
	 */
	public void setHideAll(boolean hideAll) {
		this.hideAll = hideAll;
		fireUpdate(ParameterName.HIDE_ALL);
	}
}
