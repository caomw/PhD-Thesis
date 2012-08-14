package org.imagesci.robopaint;

import java.awt.Color;

import javax.vecmath.Color4f;

import org.imagesci.robopaint.GeometryViewDescription.ParameterName;

/**
 * 
 * The ObjectDescription class provides an object with which to store the
 * parameters for individual objects/labels being used in the segmentation. An
 * ObjectDescription instance must be created for each individual object/label.
 * 
 * @author TYung
 */
public class ObjectDescription implements Comparable<ObjectDescription> {
	protected String name = "";
	protected boolean visible = true;
	protected Color4f color = new Color4f();
	protected int id = -1;
	protected static boolean isPlaying = false;
	protected float pressureWeight = 1;
	protected float targetIntensity = 0;
	protected float advectionWeight = 0;
	protected float curvatureWeight = 0.1f;
	protected boolean autoUpdateIntensity = false;

	public enum Status {
		STATIC, ACTIVE, PASSIVE
	};

	private static final int statusMasks[] = { 1, 2, 4 };

	protected Status status = Status.ACTIVE;

	public byte getObjectStatus() {
		return (byte) (((autoUpdateIntensity) ? 8 : 0) | statusMasks[status
				.ordinal()]);
	}

	/**
	 * Returns the status of an object/label.
	 * 
	 * @return the Status associated with an object/label.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status of an object/label.
	 * 
	 * @param status
	 *            the new Status to be associated with an object/label.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Creates an instance of an ObjectDescription.
	 * 
	 * @param name
	 *            the name of the object/label whose parameters are to be stored
	 *            in this instance of an ObjectDescription.
	 * @param id
	 *            the identification number of the object/label.
	 */
	public ObjectDescription(String name, int id) {
		this.name = name;
		this.id = id;
	}

	/**
	 * Determines whether two ObjectDescription instances are the same
	 * ObjectDescription instance using their id numbers.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ObjectDescription) {
			return (((ObjectDescription) obj).id == id);
		} else {
			return false;
		}
	}

	/**
	 * Returns the name of the ObjectViewDescription instance.
	 * 
	 * @return the name of the object/label.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the ObjectViewDescription instance.
	 * 
	 * @param name
	 *            the name of the object/label.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Determines whether the object/label associated with the ObjectDescription
	 * instance is visible in the rendering pane.
	 * 
	 * @return true if the object/label is visible.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets whether the object/label is visible in the rendering pane.
	 * 
	 * @param visible
	 *            true if the object/label is visible.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;

		GeometryViewDescription.getInstance().fireUpdate(
				ParameterName.CHANGE_OBJECT_VISIBILITY);
	}

	/**
	 * Returns the Color4f color description of the color of the object/label.
	 * 
	 * @return the Color4f of the object/label.
	 */
	public Color4f getColor4f() {
		return color;
	}

	/**
	 * Returns the Color4f color description of the color of the object/label.
	 * 
	 * @return the Color4f of the object/label.
	 */
	public Color getColor() {
		return new Color(color.x, color.y, color.z, color.w);
	}

	/**
	 * Sets the Color4f color description of the color of the object/label.
	 * 
	 * @param color
	 *            the Color4f of the object/label.
	 */
	public void setColor(Color4f color) {
		this.color = color;
		GeometryViewDescription.getInstance().fireUpdate(
				ParameterName.CHANGE_OBJECT_COLOR);
	}

	/**
	 * Returns the identification number of the object/label.
	 * 
	 * @return the identification number.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the identification number of the object/label.
	 * 
	 * @param id
	 *            the new identification number.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the RGB color description of the color of the object/label.
	 * 
	 * @param r
	 *            a value between 0 and 255 inclusive describing the red
	 *            component.
	 * @param g
	 *            a value between 0 and 255 inclusive describing the green
	 *            component.
	 * @param b
	 *            a value between 0 and 255 inclusive describing the blue
	 *            component.
	 */
	public void setColor(int r, int g, int b) {
		color.x = r / 255.0f;
		color.y = g / 255.0f;
		color.z = b / 255.0f;
	}

	/**
	 * Sets the RGB color description of the color of the object/label.
	 * 
	 * @param r
	 *            a value between 0 and 255 inclusive describing the red
	 *            component.
	 * @param g
	 *            a value between 0 and 255 inclusive describing the green
	 *            component.
	 * @param b
	 *            a value between 0 and 255 inclusive describing the blue
	 *            component.
	 * @param a
	 *            a value between 0 and 255 inclusive describing the alpha
	 *            component.
	 */
	public void setColor(int r, int g, int b, int a) {
		color.x = r / 255.0f;
		color.y = g / 255.0f;
		color.z = b / 255.0f;
		color.w = a / 255.0f;
	}

	/**
	 * Sets the transparency of the object/label.
	 * 
	 * @param t
	 *            the new transparency.
	 */
	public void setTransparency(float t) {
		color.w = t;
	}

	/**
	 * Returns the transparency of the object/label.
	 * 
	 * @return the transparency.
	 */
	public float getTransparency() {
		return color.w;
	}

	/**
	 * Sets the pressure weight of the object/label in the segmentation.
	 * 
	 * @param pressureWeight
	 *            the new pressure weight.
	 */
	public void setPressureWeight(float pressureWeight) {

		this.pressureWeight = pressureWeight;
		GeometryViewDescription.getInstance().fireUpdate(
				ParameterName.CHANGE_PRESSURE);

	}

	/**
	 * Returns the pressure weight of the object/label in the segmentation.
	 * 
	 * @return the pressure weight.
	 */
	public float getPressureWeight() {

		return pressureWeight;
	}

	/**
	 * Sets the target intensity of the object/label in the segmentation.
	 * 
	 * @param targetIntensity
	 *            the new target intensity.
	 */
	public void setTargetIntensity(float targetIntensity) {

		this.targetIntensity = targetIntensity;
		GeometryViewDescription.getInstance().fireUpdate(
				ParameterName.CHANGE_TARGET_INTENSITY);

	}

	/**
	 * Returns the target intensity of the object/label in the segmentation.
	 * 
	 * @return the target intensity.
	 */
	public float getTargetIntensity() {

		return targetIntensity;
	}

	/**
	 * Sets the advection weight of the object/label in the segmentation.
	 * 
	 * @param advectionWeight
	 *            the new advection weight.
	 */
	public void setAdvectionWeight(float advectionWeight) {

		this.advectionWeight = advectionWeight;
	}

	/**
	 * Returns the advection weight of the object/label in the segmentation.
	 * 
	 * @return the advection weight.
	 */
	public float getAdvectionWeight() {

		return advectionWeight;
	}

	/**
	 * Sets the curvature weight of the object/label in the segmentation.
	 * 
	 * @param curvatureWeight
	 *            the new curvature weight.
	 */
	public void setCurvatureWeight(float curvatureWeight) {

		this.curvatureWeight = curvatureWeight;

		GeometryViewDescription.getInstance().fireUpdate(
				ParameterName.CHANGE_CURVATURE);
	}

	/**
	 * Returns the curvature weight of the object/label in the segmentation.
	 * 
	 * @return the new curvature weight.
	 */
	public float getCurvatureWeight() {

		return curvatureWeight;
	}

	/**
	 * Determines whether the object/label segmentation is playing.
	 * 
	 * @return true if the segmentation is playing.
	 */
	public static boolean getPlaying() {

		return isPlaying;
	}

	/**
	 * Sets whether the object/label segmentation is playing.
	 * 
	 * @param isPlaying
	 *            true if the segmentation is playing.
	 */
	public static void setPlaying(boolean play) {

		isPlaying = play;
		GeometryViewDescription.getInstance().fireUpdate(
				ParameterName.START_STOP_SEGMENTATION);
	}

	public boolean isAutoUpdateIntensity() {
		return autoUpdateIntensity;
	}

	public void setAutoUpdateIntensity(boolean autoUpdateIntensity) {
		this.autoUpdateIntensity = autoUpdateIntensity;
	}

	@Override
	public int compareTo(ObjectDescription obj) {
		return (int) Math.signum(this.id - obj.id);
	}
}
