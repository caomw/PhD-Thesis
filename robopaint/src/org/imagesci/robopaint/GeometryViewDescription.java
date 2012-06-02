package org.imagesci.robopaint;

import java.util.ArrayList;
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
	protected boolean showIsoSurface = true;
	protected static final GeometryViewDescription description = new GeometryViewDescription();
	protected ExpandBar bar;
	protected ExpandItem item;

	public static GeometryViewDescription getInstance() {
		return description;
	}

	public GeometryViewDescription() {
		objectDescriptions = new ArrayList<ObjectDescription>();
		bar = new ExpandBar(RoboPaint.shell, SWT.V_SCROLL);
		item = new ExpandItem(bar, SWT.NONE, 0);
		Composite composite = new Composite(bar, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		Text text;
		Scale scale;
		for (int i=0; i<objectDescriptions.size(); i++) {
			
			text = new Text(composite, SWT.NONE);
			text.setText(objectDescriptions.get(i).getName());
			scale = new Scale(composite, SWT.NONE);
		}
		
		item.setText("Geometry");
	}

	public List<ObjectDescription> getObjectDescriptions() {
		return objectDescriptions;
	}

	public void setObjectDescriptions(List<ObjectDescription> objectDescriptions) {
		this.objectDescriptions = objectDescriptions;
	}

	public boolean isShowIsoSurface() {
		return showIsoSurface;
	}

	public void setShowIsoSurface(boolean showIsoSurface) {
		this.showIsoSurface = showIsoSurface;
	}
}
