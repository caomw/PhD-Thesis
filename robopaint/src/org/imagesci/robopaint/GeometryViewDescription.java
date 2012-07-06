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

public class GeometryViewDescription {
	protected List<ObjectDescription> objectDescriptions;
	protected boolean hideAll = true;
	protected static final GeometryViewDescription description = new GeometryViewDescription();
	protected ExpandBar bar;
	protected ExpandItem item;
	public enum ParameterName{HIDE_ALL};
	public static interface GeometryViewListener{
		public void updateParameter(GeometryViewDescription g,ParameterName p);
	}
	protected List<GeometryViewListener> listeners=new LinkedList<GeometryViewListener>();
	public void fireUpdate(ParameterName param){
		for(GeometryViewListener g:listeners){
			g.updateParameter(this,param);
		}
	}
	public static GeometryViewDescription getInstance() {
		return description;
	}

	public GeometryViewDescription() {
		objectDescriptions = new ArrayList<ObjectDescription>();
	}

	public List<ObjectDescription> getObjectDescriptions() {
		return objectDescriptions;
	}

	public void setObjectDescriptions(List<ObjectDescription> objectDescriptions) {
		this.objectDescriptions = objectDescriptions;
	}

	public boolean isHideAll() {
		return hideAll;
	}

	public void setHideAll(boolean hideAll) {
		this.hideAll = hideAll;
		fireUpdate(ParameterName.HIDE_ALL);
	}
}
