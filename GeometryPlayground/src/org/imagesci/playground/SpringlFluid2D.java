package org.imagesci.playground;
import java.io.File; 

import javax.vecmath.Point2f;

import processing.core.PApplet;
import processing.core.PImage;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class SpringlFluid2D extends PApplet {

/**
 * Continuous Lines. 
 * 
 * Click and drag the mouse to draw a line. 
 */

PImage backgroundImage;
ParticleVolume particleGrid;
public void setup() {
  size(800,800);
  background(102);
  	backgroundImage=PImageReaderWriter
			.getInstance().read(new File("source.png"));
  	backgroundImage.resize(32,32);
  	System.out.println("Size "+backgroundImage.width+" "+backgroundImage.height);
	ImageDataFloat sourceImage = PImageReaderWriter.convertToGray(backgroundImage);
	DistanceField2D df = new DistanceField2D();
	float[][] img = sourceImage.toArray2d();
	int r = img.length;
	int c = img[0].length;
	for (int i = 0; i < r; i++) {
		for (int j = 0; j < c; j++) {
			img[i][j] -= 127.5f;
		}
	}
	ImageDataFloat levelSet = df.solve(sourceImage, 15.0);
	particleGrid=new ParticleVolume(levelSet);
	
}

public void draw() {
 
  fill(255);
  noStroke();
  rect(0,0,width,height);
  
  image(backgroundImage,-0.5f*width/(float)backgroundImage.width,-0.5f*height/(float)backgroundImage.height, width,height);
  ellipseMode(RADIUS); 
  float scaleX=width/(float)backgroundImage.width;
  float scaleY=height/(float)backgroundImage.height;
  
  noFill();
  strokeWeight(1.0f);
  stroke(128,128,128,128);
  for (int i = 0; i < backgroundImage.width; i++) {
	  line(scaleX*i,0,scaleX*i,scaleY*height);
  }
  
  for (int j = 0; j < backgroundImage.height; j++) {
	  line(0,scaleY*j,scaleX*width,scaleY*j);
  }
  smooth(8);
  fill(255,64,64,128);
  stroke(0,0,0,255);
  strokeWeight(1.0f);
  for(FluidParticle p:particleGrid.particles){
	  ellipse(scaleX*p.x,scaleY*p.y,scaleX*p.radius,scaleY*p.radius);
	  
  }
  fill(64,255,64,128);
  stroke(0,0,0,255);
  for(FluidInterfaceParticle p:particleGrid.interfaceParticles){
	  beginShape();
	  for(Point2f pt:p.boundaryPoints){
		  vertex(scaleX*pt.x,scaleY*pt.y);
	  }
	  endShape(CLOSE);
	  
  }
  stroke(192,192,192);
  strokeWeight(3.0f);
  for(FluidInterfaceParticle ip:particleGrid.interfaceParticles){
	  line(scaleX*ip.x1,scaleY*ip.y1,scaleX*ip.x2,scaleY*ip.y2);
  }
  
  strokeWeight(3.0f);
  stroke(255,64,64,128);
  for(FluidInterfaceParticle ip:particleGrid.interfaceParticles){
	  point(scaleX*ip.x1,scaleY*ip.y1);
	  point(scaleX*ip.x2,scaleY*ip.y2);
  }
  
  strokeWeight(6.0f);
  stroke(192,255,192);
  
  for(FluidInterfaceParticle ip:particleGrid.interfaceParticles){
	  point(scaleX*ip.x,scaleY*ip.y);
  }
  

}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "org.imagesci.playground.SpringlFluid2D" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
