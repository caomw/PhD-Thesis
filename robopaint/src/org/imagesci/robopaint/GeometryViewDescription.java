package org.imagesci.robopaint;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

/**
 * 
 * The GeometryViewDescription class provides an object with which to store the
 * parameters for viewing the segmentation of the original images. description
 * is a global GeometryViewDescription instance containing the setting for
 * displaying or hiding all objects/labels. The global instance description
 * stores parameters for the entire segmentation project, not only for a single
 * object/label.
 * 
 * @author TYung
 */
public class GeometryViewDescription {
	public static interface GeometryViewListener {
		public void updateParameter(GeometryViewDescription g,
				ObjectDescription currentObject, ParameterName p);
	}

	public enum ParameterName {
		CHANGE_AUTO_UPDATE_INTENSITY, CHANGE_TARGET_INTENSITY, CHANGE_CURVATURE, CHANGE_PRESSURE, START_STOP_SEGMENTATION, ADD_OBJECT, REMOVE_ALL_OBJECTS, CLOSE_DISTFIELD_IMAGE, CLOSE_LABEL_IMAGE, HIDE_ALL, OPEN_DISTFIELD_IMAGE, OPEN_IMAGE_SEGMENTATION, OPEN_LABEL_IMAGE, CHANGE_OBJECT_COLOR, CHANGE_OBJECT_VISIBILITY, OPEN_MESH
	}

	protected static final GeometryViewDescription description = new GeometryViewDescription();

	/**
	 * Returns a global instance of a GeometryViewDescription to store
	 * parameters for a segmentation project.
	 * 
	 * @return a global instance of a GeometryViewDescription.
	 */
	public static GeometryViewDescription getInstance() {
		return description;
	}

	protected ExpandBar bar;
	protected File distfieldFile;
	protected boolean showSliceView = false;
	protected ObjectDescription currentObject;

	public ObjectDescription getCurrentObject() {
		return currentObject;
	}

	public int getCurrentObjectIndex() {
		return objectDescriptions.indexOf(currentObject);
	}

	public void setCurrentObject(ObjectDescription currentObject) {
		this.currentObject = currentObject;
	}

	public void setCurrentObject(int index) {
		this.currentObject = objectDescriptions.get(index);
	}

	protected ExpandItem item;;

	protected File labelFile;

	protected List<GeometryViewListener> listeners = new LinkedList<GeometryViewListener>();

	protected List<ObjectDescription> objectDescriptions;

	public void addObjectDescription(ObjectDescription obj) {
		objectDescriptions.add(obj);
		fireUpdate(ParameterName.ADD_OBJECT);
	}

	public void removeAllObjectDescriptions() {
		objectDescriptions.clear();
		fireUpdate(ParameterName.REMOVE_ALL_OBJECTS);
	}

	/**
	 * Constructs an instance of GeometryViewDescription.
	 */
	public GeometryViewDescription() {
		objectDescriptions = new ArrayList<ObjectDescription>();
	}

	public void addListener(GeometryViewListener listener) {
		listeners.add(listener);
	}

	/**
	 * Updates a specified GeometryViewDescription parameter.
	 * 
	 * @param param
	 *            the parameter to be updated.
	 */
	public void fireUpdate(ParameterName param) {
		for (GeometryViewListener g : listeners) {
			g.updateParameter(this, this.getCurrentObject(), param);
		}
	}

	/**
	 * Updates a specified GeometryViewDescription parameter.
	 * 
	 * @param param
	 *            the parameter to be updated.
	 */
	public void fireUpdate(ParameterName param, ObjectDescription obj) {
		for (GeometryViewListener g : listeners) {
			g.updateParameter(this, obj, param);
		}
	}

	public File getDistanceFieldImageFile() {
		return distfieldFile;
	}

	public File getLabelImageFile() {
		return labelFile;
	}

	/**
	 * Returns a list of ObjectDescriptions instances associated with the
	 * existing objects.
	 * 
	 * @return a list of ObjectDescriptions.
	 */
	public List<ObjectDescription> getObjectDescriptions() {
		return objectDescriptions;
	}

	/**
	 * Determines whether all segmentations are hidden in the rendering pane.
	 * 
	 * @return true if all segmentations are hidden.
	 */
	public boolean isSliceView() {
		return showSliceView;
	}

	public void setDistanceFieldFile(File f) {
		this.distfieldFile = f;
		if (f != null) {
			fireUpdate(ParameterName.OPEN_DISTFIELD_IMAGE);
		} else {
			fireUpdate(ParameterName.CLOSE_DISTFIELD_IMAGE);
		}
	}

	/**
	 * Sets whether all segmentations are hidden in the rendering pane.
	 * 
	 * @param hideAll
	 *            true if all segmentations are hidden.
	 */
	public void setSliceView(boolean hideAll) {
		this.showSliceView = hideAll;
		fireUpdate(ParameterName.HIDE_ALL);
	}

	public void setLabelImageFile(File f) {
		this.labelFile = f;
		if (f != null) {
			fireUpdate(ParameterName.OPEN_LABEL_IMAGE);
		} else {
			fireUpdate(ParameterName.CLOSE_LABEL_IMAGE);
		}
	}

	/**
	 * Sets the list of ObjectDescription instances.
	 * 
	 * @param objectDescriptions
	 *            a list of ObjectDescriptions.
	 */
	public void setObjectDescriptions(List<ObjectDescription> objectDescriptions) {
		this.objectDescriptions = objectDescriptions;
	}
}
