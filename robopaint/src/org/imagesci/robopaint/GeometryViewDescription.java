package org.imagesci.robopaint;

import java.util.ArrayList;
import java.util.List;

public class GeometryViewDescription {
	protected List<ObjectDescription> objectDescriptions;
	protected boolean showIsoSurface = true;
	protected static final GeometryViewDescription description = new GeometryViewDescription();

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

	public boolean isShowIsoSurface() {
		return showIsoSurface;
	}

	public void setShowIsoSurface(boolean showIsoSurface) {
		this.showIsoSurface = showIsoSurface;
	}
}
