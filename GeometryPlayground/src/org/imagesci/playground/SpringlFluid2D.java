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

public class SpringlFluid2D extends PApplet {

/**
 * Continuous Lines. 
 * 
 * Click and drag the mouse to draw a line. 
 */

public void setup() {
  size(640, 360);
  background(102);
}

public void draw() {
  stroke(255);
  if (mousePressed == true) {
    line(mouseX, mouseY, pmouseX, pmouseY);
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
