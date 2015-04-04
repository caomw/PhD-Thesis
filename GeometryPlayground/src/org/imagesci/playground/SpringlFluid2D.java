package org.imagesci.playground;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

import javax.vecmath.Point2f;

import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class SpringlFluid2D extends PApplet {

/**
 * Continuous Lines. 
 * 
 * Click and drag the mouse to draw a line. 
 */

PImage backgroundImage;
ContourArray isoContour;
IsoContourGenerator gen;
ParticleVolume particleGrid;
public void setup() {
  size(800,800);
  background(102);
  	backgroundImage=PImageReaderWriter
			.getInstance().read(new File("source.png"));
  	backgroundImage.resize(32,32);
  	System.out.println("Size "+backgroundImage.width+" "+backgroundImage.height);
	ImageDataFloat sourceImage = PImageReaderWriter.convertToGray(backgroundImage);
	gen=new IsoContourGenerator(true);
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
	isoContour=gen.solve(levelSet);
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
  
  fill(255,64,64,128);
  stroke(0,0,0,255);
  strokeWeight(1.0f);
  for(FluidParticle p:particleGrid.particles){
	  ellipse(scaleX*p.x,scaleY*p.y,scaleX*p.radius,scaleY*p.radius);
  }
  stroke(192,192,192);
  strokeWeight(3.0f);
  for(int i=0;i<isoContour.indexes.length;i+=2){
	  Point2f pt1=isoContour.points[isoContour.indexes[i]];
	  Point2f pt2=isoContour.points[isoContour.indexes[i+1]];
	  line(scaleX*pt1.x,scaleY*pt1.y,scaleX*pt2.x,scaleY*pt2.y);
  }
  
  strokeWeight(3.0f);
  stroke(255,64,64,128);
  for(int i=0;i<isoContour.indexes.length;i++){
	  Point2f pt=isoContour.points[isoContour.indexes[i]];
	  point(scaleX*pt.x,scaleY*pt.y);
  }
  
  strokeWeight(6.0f);
  stroke(192,255,192);
  
  for(int i=0;i<isoContour.indexes.length;i+=2){
	  Point2f pt1=isoContour.points[isoContour.indexes[i]];
	  Point2f pt2=isoContour.points[isoContour.indexes[i+1]];
	  point(scaleX*0.5f*(pt1.x+pt2.x),scaleY*0.5f*(pt1.y+pt2.y));
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
