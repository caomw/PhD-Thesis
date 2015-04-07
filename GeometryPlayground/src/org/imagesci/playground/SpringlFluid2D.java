package org.imagesci.playground;
import java.io.File; 

import javax.vecmath.Point2f;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.Transform;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix2D;
import processing.core.PVector;
import processing.event.MouseEvent;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
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
ImageDataFloat particleLevelSet;
PMatrix2D Pose;
float lastMouseX=0,lastMouseY=0;
ContourArray contour;
public void setup() {
  size(1440,800);
  int sample=16;
  background(102);
  	backgroundImage=PImageReaderWriter
			.getInstance().read(new File("source.png"));
  	int w=backgroundImage.width;
  	int h=backgroundImage.height;
  	backgroundImage.resize(w/sample,h/sample);
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
	Pose=new PMatrix2D();
	Pose.reset();
	Pose.translate(width*0.25f-backgroundImage.width*0.5f,0);
	sample=2;
	
	NIFTIReaderWriter.getInstance().write(particleGrid.createUnsignedLevelSet(sample), new File("unsigned.nii"));
	NIFTIReaderWriter.getInstance().write(particleGrid.createParticleLevelSet(sample), new File("signed.nii"));
	NIFTIReaderWriter.getInstance().write(particleLevelSet=particleGrid.createSignedLevelSet(sample), new File("newsigned.nii"));
	
	
	IsoContourGenerator gen=new IsoContourGenerator(true);
	contour=gen.solve(particleLevelSet);
	for(Point2f pt:contour.points){
		pt.x/=sample;
		pt.y/=sample;
	}
}
public void mousePressed(){
	if(mouseButton==LEFT){
		PMatrix2D PoseInv=Pose.get();
		PoseInv.invert();
		PVector target=new PVector();
		PoseInv.mult(new PVector(mouseX,mouseY), target);
		lastMouseX=target.x;
		lastMouseY=target.y;
	}
}
public void mouseDragged(){
	if(mouseButton==RIGHT){
		float oldScale=Pose.m00;
		Pose.translate((mouseX-pmouseX)/oldScale,(mouseY-pmouseY)/oldScale);
	}
	if(mouseButton==LEFT){
		float scale=1.0f+10.0f*(mouseY-pmouseY)/(float)height;
		Pose.translate(lastMouseX, lastMouseY);
		Pose.scale(scale,scale);
		Pose.translate(-lastMouseX, -lastMouseY);
	}
			
}

public void mouseWheel(MouseEvent event) {
	float e = event.getCount();
	float scale=1.0f+30.0f*Math.signum(e)/(float)height;	
	PMatrix2D PoseInv=Pose.get();
	PoseInv.invert();
	PVector target=new PVector();
	PoseInv.mult(new PVector(mouseX,mouseY), target);
	lastMouseX=target.x;
	lastMouseY=target.y;
	Pose.translate(lastMouseX, lastMouseY);
	Pose.scale(scale,scale);
	Pose.translate(-lastMouseX, -lastMouseY);
}
public void draw() {

  fill(255);
  noStroke();
  rect(0,0,width,height);
  pushMatrix();
  applyMatrix(Pose);
  float scaleY=height/(float)backgroundImage.height;
  float scaleX=scaleY;
  imageMode(CORNER);
  image(backgroundImage,
		  -0.5f*scaleX,-0.5f*scaleY,//half pixel shift to fix centering
		  scaleX*backgroundImage.width,scaleY*backgroundImage.height);
  ellipseMode(RADIUS); 
  //float scaleX=width/(float)backgroundImage.width;

  noFill();
  strokeWeight(1.0f);
  stroke(128,128,128,128);
  for (int i = 0; i < backgroundImage.width+1; i++) {
	  line(scaleX*i,0,scaleX*i,scaleY*backgroundImage.height);
  }
  
  for (int j = 0; j < backgroundImage.height+1; j++) {
	  line(0,scaleY*j,scaleX*backgroundImage.width,scaleY*j);
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
  
  noFill();
  stroke(64,64,255,255);
  strokeWeight(2.0f);
  for(int i=0;i<contour.indexes.length;i+=2){
	  Point2f pt1=contour.points[contour.indexes[i]];
	  Point2f pt2=contour.points[contour.indexes[i+1]];
	  line(scaleX*pt1.x,scaleY*pt1.y,scaleX*pt2.x,scaleY*pt2.y);
	  
  }
  
  popMatrix();
  
  /*
  stroke(0);
  fill(0);
  text(" ("+Math.round(lastMouseX)+","+Math.round(lastMouseY)+")",5,15,0);
  */
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
