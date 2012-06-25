/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 */
package edu.jhu.cs.cisst.vent.widgets;


import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.imagesci.gac.WEGAC3D;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.PlayPauseStopEventListener;
import edu.jhu.cs.cisst.vent.renderer.processing.ActiveContourRenderer3D;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationTriangleMesh.
 */
public class VisualizationGAC3D extends VisualizationTriangleMesh implements
		PlayPauseStopEventListener {

	/**
	 * The Class AnimationLoop.
	 */
	protected class AnimationLoop extends Thread {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				running = true;
				int simulationDuration = activeContour.getMaxIterations();
				while (activeContour.getTime() < simulationDuration && running) {
					if (!pause) {
						if (captureParam.getValue()
								&& activeContour.getTime()
										% activeContour.getResamplingRate() == 0) {
							screenshot = null;
							requestVideoFrame = true;
							while (requestVideoFrame) {
								Thread.sleep(4 * updateInterval);
							}
						}
						if (!activeContour.step()) {
							break;
						}

						Thread.sleep(updateInterval);
					} else {
						Thread.sleep(12 * updateInterval);
					}
				}
				running = false;
			} catch (Exception e) {
				e.printStackTrace();
				System.err.flush();
			}
		}
	}

	/** The active contour. */
	protected WEGAC3D activeContour;
	/** The count cycle. */
	protected int countCycle = 0;

	/** The last time. */
	protected int lastTime = -1;

	/** The pause. */
	protected boolean pause = true;

	/** The renderer. */
	protected ActiveContourRenderer3D renderer;

	/** The request video frame. */
	protected boolean requestVideoFrame = false;

	/** The running. */
	protected boolean running = false;

	/** The thread. */

	protected Thread thread;

	/** The time. */
	protected int time;

	/** The update interval. */
	protected long updateInterval = 10;

	/** The view update. */
	boolean viewUpdate = false;

	/**
	 * Instantiates a new visualization triangle mesh.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param activeContour
	 *            the active contour
	 */
	public VisualizationGAC3D(int width, int height, WEGAC3D activeContour) {
		super(width, height);
		pose = new Matrix4f();
		pose.setIdentity();
		this.activeContour = activeContour;
		setName("Visualize - Multi-Object Level Set");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#dispose()
	 */
	@Override
	public void dispose() {
		stopEvent();
		refresher.stop();
		// super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.PlayPauseStopEventListener#stopEvent()
	 */
	@Override
	public void stopEvent() {
		running = false;
		try {
			if (thread != null) {
				thread.join();
			}
			thread = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#draw()
	 */
	@Override
	public void draw() {
		if ((requestVideoFrame && captureParam.getValue()) || running
				|| requestScreenShot) {
			renderer.setFastRendering(false);
			renderer.refresh();
		} else {
			renderer.setFastRendering(true);
		}

		for (RendererProcessing renderer : renderers) {
			renderer.draw();
		}
		if (requestScreenShot) {
			screenshot = captureScreenshot();
			requestScreenShot = false;
		} else if (running && captureParam.getValue()) {
			long t = activeContour.getTime();

			if (requestVideoFrame) {
				PImage img = captureScreenshot();
				String f = (new File(outputFileParam.getValue(), String.format(
						"screenshot_%05d.png", t))).getAbsolutePath();
				System.out.println("Saving " + f);
				img.save(f);
				img.delete();
				requestVideoFrame = false;
				Point3f rot = autoRotateParam.getValue();
				if (Math.abs(rot.x) > 1E-3f || Math.abs(rot.y) > 1E-3f
						|| Math.abs(rot.z) > 1E-3f) {
					applyRotation(rot.x, rot.y, rot.z);
					updatePoseParam();
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D#refresh
	 * ()
	 */
	@Override
	public void refresh() {
		if (requestUpdate) {
			requestUpdate = false;
			refreshLock = true;
			if (viewUpdate) {
				sceneParams.getInputView().update();
			}
			refreshLock = false;
			renderer.refresh();

		} else if (enableAutoRotate.getValue()
				&& !mousePressed
				&& ((!captureParam.getValue() && !running)
						|| (captureParam.getValue() && !running) || (!captureParam
						.getValue() && running))) {
			Point3f rot = autoRotateParam.getValue();
			if (Math.abs(rot.x) > 1E-3f || Math.abs(rot.y) > 1E-3f
					|| Math.abs(rot.z) > 1E-3f) {

				if (enableAutoRotate.getValue()) {
					applyRotation(rot.x, rot.y, rot.z);
				}
				updatePoseParam();
				if (countCycle == 0) {
					refreshLock = true;
					sceneParams.getInputView().update();
					refreshLock = false;
				}
				renderer.refresh();
				countCycle++;
				if (countCycle == 100) {
					countCycle = 0;
				}
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#applyRotation
	 * (float, float, float)
	 */
	@Override
	public void applyRotation(float rotx, float roty, float rotz) {
		renderer.applyTransform(-rotx, roty, rotz, 0, 0, 0);
		pose = renderer.getModelView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.PlayPauseStopEventListener#pauseEvent()
	 */
	@Override
	public void pauseEvent() {
		setPause(!pause);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.PlayPauseStopEventListener#playEvent()
	 */
	@Override
	public void playEvent() {
		setPause(false);
		if (!running) {
			if (thread == null) {
				thread = new AnimationLoop();
			}
			thread.start();
		}
	}

	/**
	 * Sets the pause.
	 * 
	 * @param pause
	 *            the new pause
	 */
	public void setPause(boolean pause) {
		this.pause = pause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#create()
	 */
	@Override
	public ParamCollection create() {
		renderers = new ArrayList<RendererProcessing3D>();

		renderers.add(renderer = new ActiveContourRenderer3D(this,
				activeContour, preferredWidth / 2, preferredHeight / 2,
				activeContour.getResamplingRate()));

		ParamCollection params = super.create();
		return params;
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */

	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		super.createVisualizationParameters(visualizationParameters);
		activeContour.addListener(renderer);
		centerParam.setValue(center = new Point3f(0.5f * activeContour
				.getRows(), 0.5f * activeContour.getCols(),
				0.5f * activeContour.getSlices()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#getVideoFrames(long,
	 * long)
	 */
	@Override
	public Image[] getVideoFrames(long frameRate, long duration) {
		// Need to create new axis to rotate around that is in plane with the
		// view plane.
		/*
		 * Image[] images = new Image[frames / 10]; for (int i = 0; i < frames;
		 * i++) { springlsRender.setTimeIndex(i); Image im = getScreenshot();
		 * 
		 * PImage img = new PImage(im); img.parent = this; img.save((new
		 * File(String.format("screenshot_%05d", i))) .getAbsolutePath()); if (i
		 * % 10 == 0) { images[i / 10] = im; } else { im.flush(); }
		 * img.delete(); }
		 * 
		 * return images;
		 */
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#mouseDragged()
	 */
	@Override
	public void mouseDragged() {
		super.mouseDragged();
		if (requestUpdate) {
			renderer.setCameraCenter(tx / width, -ty / height, 4.0f / scale);
			renderer.setTargetCenter(new Point3f(tx / width + 2 * center.x
					/ activeContour.getRows() - 1, -ty / height + 2 * center.y
					/ activeContour.getCols() - 1, 2 * center.z
					/ activeContour.getSlices() - 1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#mouseWheelMoved
	 * (java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scale = Math
				.max(0.01f, scale - wheelScaleRate * (e.getWheelRotation()));
		scaleParam.setValue(scale);
		renderer.setCameraCenter(0, 0, 4.0f / scale);
		requestUpdate = true;
		viewUpdate = true;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#setup()
	 */
	@Override
	public void setup() {
		super.setup();
		refresher.setRefreshInterval(10);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.PlayPauseStopEventListener#stepEvent()
	 */
	@Override
	public void stepEvent() {
		activeContour.step();
		time++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#update(edu.jhu
	 * .ece.iacl.jist.pipeline.parameter.ParamModel,
	 * edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		super.update(model, view);
		renderer.setBackgroundColor(bgColor);
		renderer.setModelView(pose);
		renderer.setCameraCenter(tx / width, -ty / height, 4.0f / scale);
		renderer.setTargetCenter(new Point3f(tx / width + 2 * center.x
				/ activeContour.getRows() - 1, -ty / height + 2 * center.y
				/ activeContour.getCols() - 1, 2 * center.z
				/ activeContour.getSlices() - 1));

		requestUpdate = true;
		viewUpdate = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#
	 * updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		super.updateVisualizationParameters();
		renderer.setBackgroundColor(bgColor);
		renderer.setModelView(pose);
		renderer.setCameraCenter(tx / width, -ty / height, 4.0f / scale);
		renderer.setTargetCenter(new Point3f(tx / width + 2 * center.x
				/ activeContour.getRows() - 1, -ty / height + 2 * center.y
				/ activeContour.getCols() - 1, 2 * center.z
				/ activeContour.getSlices() - 1));

		requestUpdate = true;
		viewUpdate = true;
	}
}
