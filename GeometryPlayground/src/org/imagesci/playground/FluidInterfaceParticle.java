package org.imagesci.playground;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class FluidInterfaceParticle extends FluidParticle{
	public float x1,y1;
	public float x2,y2;
	public Point2f[] boundaryPoints;
	public FluidInterfaceParticle(float x1,float y1,float x2,float y2,float r){
		super(0.5f*(x1+x2),0.5f*(y1+y2),r);
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
		update();
	}
	public float distance(float px,float py) {
		Vector2f dir = new Vector2f();
		Point2f pt1 = new Point2f(x1,y1);
		Point2f pt2 = new Point2f(x2,y2);
		Point2f pt=new Point2f(px,py);
		dir.sub(pt2, pt1);
		double len = dir.length();
		dir.scale(1.0f / ((float) len));
		Vector2f diff = new Vector2f();
		diff.sub(pt, pt1);
		double mSegmentParameter = dir.dot(diff);
		Point2f lastClosestSegmentPoint=new Point2f();
		if (0 < mSegmentParameter) {
			if (mSegmentParameter < len) {
				lastClosestSegmentPoint.x = dir.x;
				lastClosestSegmentPoint.y = dir.y;
				lastClosestSegmentPoint.scale((float) mSegmentParameter);
				lastClosestSegmentPoint.add(pt1);
			} else {
				lastClosestSegmentPoint.x = pt2.x;
				lastClosestSegmentPoint.y = pt2.y;
			}
		} else {
			lastClosestSegmentPoint.x = pt1.x;
			lastClosestSegmentPoint.y = pt1.y;
		}
		diff.sub(pt, lastClosestSegmentPoint);
		Vector2f norm=new Vector2f(y1-y2,x2-x1);
		float sign=Math.signum(diff.dot(norm));
		return pt.distance(lastClosestSegmentPoint)*sign;
	}
	public void update() {
		
		Point2f startPoint = new Point2f(x1,y1);
		Point2f endPoint = new Point2f(x2,y2);
		Point2f tanget = new Point2f();
		Point2f point = new Point2f(x,y);
		float tangetPositiveExtent = endPoint.distance(point);
		float tangetNegativeExtent = startPoint.distance(point);
		tanget.sub(endPoint, startPoint);
		tanget.scale(1.0f / (float) Math.sqrt(tanget.x * tanget.x + tanget.y
				* tanget.y));
		Point2f normal = new Point2f(-tanget.y, tanget.x);
		double u, v;
		final float spacing = 0.05f;
		double l = radius;
		double hemiLength = Math.PI * l;
		int N = (int) Math
				.ceil((2 * hemiLength + 2 * (tangetNegativeExtent + tangetPositiveExtent))
						/ (spacing));
		double effectiveSpacing = (2 * hemiLength + 2 * (tangetNegativeExtent + tangetPositiveExtent))
				/ N;
		boundaryPoints = new Point2f[N];
		u = 0;
		v = 0;
		double t = 0;
		for (int i = 0; i < N; i++) {
			if (t < tangetPositiveExtent) {
				u = t;
				v = l;
			} else if (t >= tangetPositiveExtent
					&& t < tangetPositiveExtent + hemiLength) {
				double r = t - tangetPositiveExtent;
				u = tangetPositiveExtent + l * Math.cos(0.5 * Math.PI - r / l);
				v = l * Math.sin(0.5 * Math.PI - r / l);
			} else if (t >= hemiLength + tangetPositiveExtent
					&& t < 2 * tangetPositiveExtent + tangetNegativeExtent
							+ hemiLength) {
				double r = t - hemiLength - tangetPositiveExtent;
				u = tangetPositiveExtent - r;
				v = -l;
			} else if (t >= 2 * tangetPositiveExtent + tangetNegativeExtent
					+ hemiLength
					&& t < 2 * tangetPositiveExtent + tangetNegativeExtent + 2
							* hemiLength) {
				double r = t
						- (2 * tangetPositiveExtent + tangetNegativeExtent)
						- hemiLength;
				u = -tangetNegativeExtent + l
						* Math.cos(-0.5 * Math.PI - r / l);
				v = l * Math.sin(-0.5 * Math.PI - r / l);
			} else {
				u = t
						- (2 * (tangetPositiveExtent + tangetNegativeExtent) + 2 * hemiLength);
				v = l;
			}
			boundaryPoints[i] = new Point2f((float) (point.x + u * tanget.x + v
					* normal.x),
					(float) (point.y + u * tanget.y + v * normal.y));
			t += effectiveSpacing;
		}
	}
}
