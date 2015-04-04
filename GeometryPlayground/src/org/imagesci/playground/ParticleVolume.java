package org.imagesci.playground;

import java.util.Vector;

import javax.vecmath.Point2f;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class ParticleVolume {
	public Vector<FluidParticle> particles;
	public Vector<FluidInterfaceParticle> interfaceParticles;
	public ParticleVolume(ImageDataFloat levelSet){
		particles=new Vector<FluidParticle>();
		float[][] img = levelSet.toArray2d();
		float radius=0.25f;
		int r = img.length;
		int c = img[0].length;
		for (int i = 0; i < 2*r; i++) {
			for (int j = 0; j < 2*c; j++) {
				float x=0.5f*i+radius;
				float y=0.5f*j+radius;
				double l=DataOperations.interpolate(x, y, img, r, c);
				if(l<-radius){
					particles.add(new FluidParticle(x,y,radius));
				}
			}
		}
		interfaceParticles=new Vector<FluidInterfaceParticle>();
		IsoContourGenerator gen=new IsoContourGenerator(true);
		ContourArray isoContour=gen.solve(levelSet);
		  for(int i=0;i<isoContour.indexes.length;i+=2){
			  Point2f pt1=isoContour.points[isoContour.indexes[i]];
			  Point2f pt2=isoContour.points[isoContour.indexes[i+1]];
			  interfaceParticles.add(new FluidInterfaceParticle(pt1.x,pt1.y, pt2.x, pt2.y, radius));
		  }
	}
}
