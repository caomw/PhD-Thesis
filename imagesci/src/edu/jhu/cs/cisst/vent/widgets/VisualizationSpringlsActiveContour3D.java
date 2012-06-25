/**
 * JIST Extensions for Computer-Integrated Surgery
 *
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 * @author Blake Lucas
 */
package edu.jhu.cs.cisst.vent.widgets;


import java.awt.Image;
import java.io.File;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.imagesci.springls.SpringlsActiveContour3D;
import org.imagesci.springls.SpringlsCommon3D;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.PlayPauseStopEventListener;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationTriangleMesh.
 */
public abstract class VisualizationSpringlsActiveContour3D extends
		VisualizationTriangleMesh implements PlayPauseStopEventListener {

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
				for (time = 0; time < simulationDuration && running; time++) {
					if (!pause) {
						if (captureParam.getValue()
								&& time % activeContour.getResamplingInterval() == 0) {
							screenshot = null;
							requestVideoFrame = true;
							while (requestVideoFrame) {
								Thread.sleep(4 * updateInterval);
							}
						}
						if (!activeContour.step()) {
							time++;
							if (captureParam.getValue()) {
								screenshot = null;
								requestVideoFrame = true;
								while (requestVideoFrame) {
									Thread.sleep(4 * updateInterval);
								}
							}
							break;
						}
						Thread.sleep(updateInterval);
					} else {
						time--;
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
	protected SpringlsActiveContour3D activeContour;

	/** The count cycle. */
	protected int countCycle = 0;

	/** The image index. */
	protected long imageIndex = 0;

	/** The last time. */
	protected int lastTime = -1;

	/** The pause. */
	protected boolean pause = true;

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
	public VisualizationSpringlsActiveContour3D(int width, int height,
			SpringlsActiveContour3D activeContour) {
		super(width, height);
		pose = new Matrix4f();
		pose.setIdentity();
		this.activeContour = activeContour;
		setName("Visualize - Springls Mesh");
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
		SpringlsCommon3D commons = activeContour.getCommons();
		centerParam.setValue(center = new Point3f(0.5f * commons.rows,
				0.5f * commons.cols, 0.5f * commons.slices));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh#draw()
	 */
	@Override
	public void draw() {
		for (RendererProcessing renderer : renderers) {
			renderer.draw();
		}
		if (requestScreenShot) {
			screenshot = captureScreenshot();
			requestScreenShot = false;
		}

		if (requestVideoFrame) {
			long t = activeContour.getTime();
			PImage img = captureScreenshot();
			String f = (new File(outputFileParam.getValue(), String.format(
					"screenshot_%05d.png", t))).getAbsolutePath();
			System.out.println("Saving " + f);
			img.save(f);
			img.delete();
			requestVideoFrame = false;
		}
		if (running && captureParam.getValue()) {
			if (requestVideoFrame) {
				long t = activeContour.getTime();
				PImage img = captureScreenshot();
				String f = (new File(outputFileParam.getValue(), String.format(
						"screenshot_%05d.png", t))).getAbsolutePath();
				System.out.println("Saving " + f);
				img.save(f);
				img.delete();
				Point3f rot = autoRotateParam.getValue();
				if (Math.abs(rot.x) > 1E-3f || Math.abs(rot.y) > 1E-3f
						|| Math.abs(rot.z) > 1E-3f) {
					applyRotation(rot.x, rot.y, rot.z);
					updatePoseParam();
				}
				requestVideoFrame = false;
			}
		} else {
			requestVideoFrame = false;
		}
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
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.Refreshable#refresh()
	 */
	@Override
	public void refresh() {

		if (requestUpdate) {
			requestUpdate = false;
			refreshLock = true;
			sceneParams.getInputView().update();
			refreshLock = false;
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
	 * @see edu.jhu.cs.cisst.vent.PlayPauseStopEventListener#stepEvent()
	 */
	@Override
	public void stepEvent() {
		activeContour.step();
		time++;
	}
}
