package org.imagesci.robopaint;

import javax.vecmath.Color4f;

public class ObjectDescription implements Comparable<ObjectDescription> {
	protected String name = "";
	protected boolean visible = true;
	protected Color4f color = new Color4f();
	protected int id = -1;

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

	@Override
	public int compareTo(ObjectDescription obj) {
		return (int) Math.signum(this.id - obj.id);
	}
}
