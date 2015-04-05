package org.imagesci.playground;

public class FluidParticle implements Comparable<FluidParticle>{
	public float radius;
	public float x;
	public float y;
	public int id;
	public FluidParticle(float x, float y, float r){
		this.x=x;
		this.y=y;
		radius=r;
	}
	public int compareTo(FluidParticle other) {
		return (id-other.id);
	}
}
