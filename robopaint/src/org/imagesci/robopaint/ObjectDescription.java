package org.imagesci.robopaint;

import javax.vecmath.Color4f;

public class ObjectDescription implements Comparable<ObjectDescription> {
	protected String name = "";
	protected boolean visible = false;
	protected Color4f color = new Color4f();
	protected int id = -1;
	protected float pressureWeight=0;
	protected float targetIntensity=0;
	protected float advectionWeight=0;
	protected float curvatureWeight=0;
	public enum Status {
		STATIC, ACTIVE, PASSIVE
	};

	protected Status status = Status.ACTIVE;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ObjectDescription(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ObjectDescription) {
			return (((ObjectDescription) obj).id == id);
		} else {
			return false;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Color4f getColor() {
		return color;
	}

	public void setColor(Color4f color) {
		this.color = color;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setColor(int r, int g, int b) {
		color.x = r / 255.0f;
		color.y = g / 255.0f;
		color.z = b / 255.0f;
	}

	public void setTransparency(float t) {
		color.w = t;
	}

	public float getTransparency() {
		return color.w;
	}

	public void setPressureWeight(float pressureWeight) {
		
		this.pressureWeight = pressureWeight;
	}
	
	public float getPressureWeight() {
		
		return pressureWeight;
	}
	
	public void setTargetIntensity(float targetIntensity) {
		
		this.targetIntensity = targetIntensity;
	}
	
	public float getTargetIntensity() {
		
		return targetIntensity;
	}
	
	public void setAdvectionWeight(float advectionWeight) {
		
		this.advectionWeight = advectionWeight;
	}
	
	public float getAdvectionWeight() {
		
		return advectionWeight;
	}
	
	public void setCurvatureWeight(float curvatureWeight) {
		
		this.curvatureWeight = curvatureWeight;
	}
	
	public float getCurvatureWeight() {
		
		return curvatureWeight;
	}
	
	@Override
	public int compareTo(ObjectDescription obj) {
		return (int) Math.signum(this.id - obj.id);
	}
}
