package org.imagesci.robopaint.graphics;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.glu.GLU;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.imagesci.mogac.MOGAC3D;

import processing.opengl2.PGraphicsOpenGL2;

import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;

public class RoboRenderWidget extends VisualizationMOGAC3D implements Listener {
	public RoboRenderWidget(int width, int height, MOGAC3D activeContour) {
		super(width, height, activeContour);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleEvent(Event event) {
		/*
		Rectangle bounds = canvas.getBounds();
		float fAspect = (float) bounds.width / (float) bounds.height;
		canvas.setCurrent();
		context.makeCurrent();
		GL2 gl = (GL2) context.getGL();
		gl.glViewport(0, 0, bounds.width, bounds.height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU glu = new GLU();
		glu.gluPerspective(45.0f, fAspect, 0.5f, 400.0f);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		context.release();
		*/
	}

}
