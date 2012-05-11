/**
 * ImageSci Toolkit
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
 * @author Blake Lucas (blake@cs.jhu.edu)
 */
package edu.jhu.cs.cisst.vent.widgets;


import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;

import org.imagesci.mogac.MOGAC2D;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.PlayPauseStopEventListener;
import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.cs.cisst.vent.VisualizationProcessing2D;
import edu.jhu.cs.cisst.vent.renderer.processing.ImageRenderer2D;
import edu.jhu.cs.cisst.vent.renderer.processing.MOGACRenderer2D;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing2D;
import edu.jhu.cs.cisst.vent.renderer.processing.VectorFieldRenderer2D;
import edu.jhu.cs.cisst.vent.renderer.processing.VolumeIsoContourRenderer;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.utility.VersionUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationMultiActiveContour2D.
 */
public class VisualizationMOGAC2D extends VisualizationImage2D implements
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
				lastTime = -1;
				running = true;
				int simulationDuration = activeContour.getMaxIterations();
				while (running) {
					if (!pause) {
						if (!activeContour.step()) {
							break;
						}
						if (captureParam.getValue() && time % sampleRate == 0) {
							screenshot = null;
							requestVideoFrame = true;
							while (requestVideoFrame) {
								Thread.sleep(4 * updateInterval);
							}
						}
						Thread.sleep(updateInterval);
						time++;
						if (time > simulationDuration) {
							break;
						}
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

	/** The simulator. */
	protected MOGAC2D activeContour;

	/** The boundary renderer. */
	protected ImageRenderer2D imageRenderer;
	/** The last time. */
	protected int lastTime = -1;
	/** The model renderer. */
	protected MOGACRenderer2D levelsetRenderer;

	/** The model renderer. */
	protected VolumeIsoContourRenderer modelRenderer, boundaryRenderer;
	/** The pause. */
	protected boolean pause = true;

	/** The boundary renderer. */
	protected ImageRenderer2D pressureRenderer;

	/** The boundary renderer. */
	protected ImageRenderer2D refRenderer;

	/** The request video frame. */
	protected boolean requestVideoFrame = false;

	/** The running. */
	protected boolean running = false;

	/** The sample rate. */
	protected int sampleRate = 5;

	/** The thread. */

	protected Thread thread;

	/** The time. */
	protected int time;

	/** The update interval. */
	protected long updateInterval = 10;

	/** The vec field renderer. */
	VectorFieldRenderer2D vecFieldRenderer = null;

	/** The visualization. */
	protected VisualizationProcessing2D visualization;

	/**
	 * Instantiates a new visualization active contour.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param activeContour
	 *            the active contour
	 */
	public VisualizationMOGAC2D(int width, int height, MOGAC2D activeContour) {
		super(width, height, activeContour.getReferenceImage().getRows(),
				activeContour.getReferenceImage().getCols());

		this.activeContour = activeContour;

		this.showCursorText = false;
		setName("Visualization - Springls 2D");

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
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		return VersionUtil.parseRevisionNumber("$Revision: 1.3 $");
	}

	/**
	 * Creates the.
	 * 
	 * @return the param collection
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#create()
	 */
	@Override
	public ParamCollection create() {
		renderers = new ArrayList<RendererProcessing2D>();

		if (activeContour.getPressureImage() != null) {
			pressureRenderer = new ImageRenderer2D(
					activeContour.getPressureImage(), "Pressure Image", this);
			renderers.add(pressureRenderer);
		}
		if (activeContour.getReferenceImage() != null) {
			refRenderer = new ImageRenderer2D(
					activeContour.getReferenceImage(), "Reference Image", this);
			renderers.add(refRenderer);
		}
		if (activeContour.getVectorFieldImage() != null) {
			renderers.add(vecFieldRenderer = new VectorFieldRenderer2D(
					activeContour.getVectorFieldImage(), "Advection Field",
					this));
			vecFieldRenderer.setTransparency(0.25f);
			vecFieldRenderer.setSampleRate(4);
			vecFieldRenderer.setArrowColor(new Color(102, 153, 0));
		}
		levelsetRenderer = new MOGACRenderer2D(
				activeContour.getDistanceField(), this);
		levelsetRenderer.setSimulator(activeContour);
		renderers.add(levelsetRenderer);
		ParamCollection params = super.create();
		return params;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationImage2D#dispose()
	 */
	@Override
	public void dispose() {
		this.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.widgets.VisualizationImage2D#draw()
	 */
	@Override
	public void draw() {
		super.draw();
		if (requestVideoFrame) {
			PImage img = captureScreenshot();
			String f = (new File(outputFileParam.getValue(), String.format(
					"screenshot_%05d.png", time))).getAbsolutePath();
			System.out.println("Saving " + f);
			img.save(f);
			img.delete();
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
	public Image[] getVideoFrames(long sampleInterval, long duration) {
		/*
		 * LinkedList<PImage> pimages = simulator.getImages(); Image[] images =
		 * new Image[pimages.size()]; int index = 0; for (PImage img : pimages)
		 * { images[index] = img.getImage(); index++; } return images;
		 */
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#keyPressed()
	 */
	@Override
	public void keyPressed() {
		/*
		 * if (simulator != null) { if (key == ' ') {
		 * simulator.setPause(!simulator.getPause()); } else if (key == 'r') {
		 * simulator.reset(); simulator.start(); } else if (keyCode ==
		 * KeyEvent.VK_PAGE_DOWN) {
		 * simulator.setUpdateInterval(simulator.getUpdateInterval() + 50); }
		 * else if (keyCode == KeyEvent.VK_PAGE_UP) {
		 * simulator.setUpdateInterval(Math.max(0, simulator.getUpdateInterval()
		 * - 50)); } }
		 */
	}

	/**
	 * Sets the visualization.
	 * 
	 * @param vis
	 *            the new visualization
	 */
	public void setVisualization(VisualizationProcessing vis) {
		this.visualization = (VisualizationProcessing2D) vis;
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

}
