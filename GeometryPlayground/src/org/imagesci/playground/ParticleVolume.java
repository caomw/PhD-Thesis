package org.imagesci.playground;

import java.util.Vector;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class ParticleVolume {
	public Vector<FluidParticle> particles;
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
				if(l<0){
					particles.add(new FluidParticle(x,y,radius));
				}
			}
		}
	}
}
