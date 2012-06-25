/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */
package edu.jhu.cs.cisst.vent.renderer.processing;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;


import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.fixedfunc.GLPointerFunc;
import javax.vecmath.Color4f;
import javax.vecmath.Point2f;

import org.imagesci.mogac.MOGAC2D;
import org.imagesci.springls.PrefixScanCPU;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.utility.ContourArray;

import processing.core.PConstants;
import processing.core.PImage;
import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

import edu.jhu.cs.cisst.vent.VisualizationProcessing;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class ActiveContourRenderer2D.
 */
public class MOGACRenderer2D extends VolumeSliceRenderer2D implements
		MOGAC2D.FrameUpdateListener {
	
	/** The Constant WORKGROUP_SIZE. */
	private static final int WORKGROUP_SIZE = 128;
	
	/** The color buffer. */
	protected CLBuffer<FloatBuffer> colorBuffer;
	
	/** The colors. */
	protected Color4f colors[];
	/** The context. */
	protected CLContext context;
	/** The contour colors param. */
	protected ParamColor[] contourColorsParam;
	
	/** The contours. */
	protected ContourArray[] contours;
	
	/** The count elements. */
	CLKernel countElements;
	
	/** The iso contour renderer. */
	protected CLKernel isoContourRenderer;

	/** The iso surface color buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceColorBuffer;

	/** The iso surface vertex buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceVertexBuffer;

	/** The iso surf count. */
	CLKernel isoSurfCount;

	/** The iso surf gen. */
	CLKernel isoSurfGen;

	/** The labels. */
	protected int[][] labels;

	/** The mask image. */
	protected PImage maskImage;

	/** The pixel buffer. */
	protected CLBuffer<FloatBuffer> pixelBuffer;

	/** The queue. */
	protected CLCommandQueue queue;

	/** The show contour. */
	protected boolean showContour = true;

	/** The show contour param. */
	private ParamBoolean showContourParam;

	/** The show mask param. */
	protected ParamBoolean showMaskParam;

	/** The simulator. */
	protected MOGAC2D simulator;

	/** The STRIDE. */
	protected final int STRIDE = 1024;

	/** The stroke weight param. */
	protected ParamDouble strokeWeightParam;

	/** The tex buffer. */
	protected IntBuffer texBuffer = IntBuffer.allocate(1);

	/** The visible. */
	protected boolean visible = true;

	/**
	 * Instantiates a new springls2 d renderer.
	 * 
	 * @param img
	 *            the img
	 * @param applet
	 *            the applet
	 */
	public MOGACRenderer2D(ImageDataFloat img, VisualizationProcessing applet) {
		super(img, applet);

	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		if (model == showContourParam) {
			this.showContour = showContourParam.getValue();
			frameUpdate(0, 30.0f);
		} else if (model instanceof ParamColor) {
			updateColors();
		} else if (model == transparencyParam) {
			updateRender();
		} else if (model == showMaskParam) {
			frameUpdate(0, 30.0f);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D#
	 * createVisualizationParameters
	 * (edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		visualizationParameters.setName("Active Contour");
		contourColorsParam = new ParamColor[simulator.getNumColors()];
		final long seed = 5437897311l;
		Random randn = new Random(seed);

		visualizationParameters.add(strokeWeightParam = new ParamDouble(
				"Stroke Weight", 0, 20, 2));
		if (simulator instanceof MOGAC2D) {
			visualizationParameters.add(transparencyParam = new ParamFloat(
					"Mask Transparency", 0, 1, 0.65f));
			transparencyParam.setInputView(new ParamDoubleSliderInputView(
					transparencyParam, 4, false));
			visualizationParameters.add(showMaskParam = new ParamBoolean(
					"Show Mask", true));
		}
		visualizationParameters.add(showContourParam = new ParamBoolean(
				"Show Iso-Contours", showContour));
		for (int i = 0; i < contourColorsParam.length; i++) {
			visualizationParameters.add(contourColorsParam[i] = new ParamColor(
					"Contour Color [" + (i + 1) + "]",
					(new Color(randn.nextFloat(), randn.nextFloat(), randn
							.nextFloat()))));
		}
		updateColors();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D#setup()
	 */
	@Override
	public void setup() {
		try {
			context = (simulator).context;
			queue = (simulator).queue;
			pixelBuffer = context.createFloatBuffer(simulator.getRows()
					* simulator.getCols() * 4, READ_WRITE, USE_BUFFER);
			CLProgram program = context
					.createProgram(
							getClass().getResourceAsStream(
									"MogacContourRenderer.cl"))
					.build(define("ROWS", simulator.getRows()),
							define("COLS", simulator.getCols()),
							define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
							define("SCALE_DOWN", SpringlsConstants.scaleDown
									+ "f"),
							define("NUM_OBJECTS", simulator.getNumObjects()),
							define("CONTAINS_OVERLAPS",
									(simulator.containsOverlaps() ? 1 : 0)),
							ENABLE_MAD);

			isoContourRenderer = program.createCLKernel("IsoContourRenderer");
			colorBuffer = context.createFloatBuffer(
					4 * simulator.getNumColors(), READ_WRITE);
			colors = new Color4f[simulator.getNumColors()];
			isoSurfCount = program
					.createCLKernel(SpringlsCommon2D.ISO_SURF_COUNT);
			isoSurfGen = program.createCLKernel(SpringlsCommon2D.ISO_SURF_GEN);
			countElements = program
					.createCLKernel(SpringlsCommon2D.COUNT_ELEMENTS);

			GL2 gl = (GL2) ((PGraphicsOpenGL2) applet.g).beginGL();
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glGenTextures(1, texBuffer);
			gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[0]);
			gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE,
					GL.GL_REPLACE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
					GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
					GL2GL3.GL_CLAMP_TO_BORDER);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
					GL2GL3.GL_CLAMP_TO_BORDER);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,
					simulator.getRows(), simulator.getCols(), 0, GL.GL_RGBA,
					GL.GL_FLOAT, pixelBuffer.getBuffer());
			gl.glDisable(GL.GL_TEXTURE_2D);
			((PGraphicsOpenGL2) applet.g).endGL();
			updateColors();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D#
	 * updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		maskImage = null;
		this.showContour = showContourParam.getValue();
		updateColors();
	}

	/**
	 * Update colors.
	 */
	private void updateColors() {
		if (colorBuffer != null) {
			int index = 0;
			FloatBuffer buff = colorBuffer.getBuffer();
			for (ParamColor param : contourColorsParam) {
				Color4f c = colors[index++] = new Color4f(param.getValue());
				buff.put(new float[] { c.x, c.y, c.z, 1.0f });
			}
			buff.rewind();
			queue.putWriteBuffer(colorBuffer, true);
			frameUpdate(0, 30.0f);
		}
	}

	/**
	 * Sets the simulator.
	 * 
	 * @param simulator
	 *            the new simulator
	 */
	public void setSimulator(MOGAC2D simulator) {
		this.simulator = simulator;
		ImageDataInt labelImage = simulator.getImageLabels();
		this.labels = labelImage.toArray2d();
		simulator.addListener(this);
		frameUpdate(0, 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.algorithms.segmentation.gac.MultiGeodesicActiveContour2D
	 * .FrameUpdateListener#frameUpdate(int, double)
	 */
	@Override
	public void frameUpdate(int time, double fps) {
		synchronized (this) {
			if (queue != null) {

				if (showMaskParam.getValue()) {
					updateRender();
				}
				if (showContourParam.getValue()) {
					updateIsoSurface();
				}
			}

		}
	}

	/**
	 * Update render.
	 */
	public void updateRender() {
		if (isoContourRenderer != null) {
			isoContourRenderer
					.putArgs((simulator).imageLabelBuffer,
							(simulator).distanceFieldBuffer, pixelBuffer,
							colorBuffer).putArg(transparencyParam.getFloat())
					.rewind();
			queue.put1DRangeKernel(isoContourRenderer, 0, simulator.getRows()
					* simulator.getCols(), WORKGROUP_SIZE);
			queue.putReadBuffer(pixelBuffer, true);
		}
	}

	/**
	 * Update iso surface.
	 */
	public void updateIsoSurface() {

		if (isoSurfaceVertexBuffer != null) {
			isoSurfaceVertexBuffer.release();
		}
		if (isoSurfaceColorBuffer != null) {
			isoSurfaceColorBuffer.release();
		}
		CLBuffer<IntBuffer> counts = context.createIntBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);
		isoSurfCount.putArgs((simulator).distanceFieldBuffer,
				(simulator).imageLabelBuffer, counts).rewind();
		queue.put1DRangeKernel(isoSurfCount, 0, rows * cols, WORKGROUP_SIZE);
		queue.finish();
		try {
			final PrefixScanCPU scan = new PrefixScanCPU(queue, WORKGROUP_SIZE,
					32);
			int vertexCount = scan.scan(counts, rows * cols);
			isoSurfaceVertexBuffer = context.createFloatBuffer(2 * vertexCount,
					READ_WRITE, USE_BUFFER);
			isoSurfaceColorBuffer = context.createFloatBuffer(8 * vertexCount,
					READ_WRITE, USE_BUFFER);
			isoSurfGen.putArgs(isoSurfaceVertexBuffer,
					(simulator).distanceFieldBuffer,
					(simulator).imageLabelBuffer, isoSurfaceColorBuffer,
					colorBuffer, counts).rewind();
			queue.put1DRangeKernel(isoSurfGen, 0, rows * cols, WORKGROUP_SIZE);
			queue.finish();
			queue.putReadBuffer(isoSurfaceVertexBuffer, true);
			queue.putReadBuffer(isoSurfaceColorBuffer, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		counts.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.renderer.processing.VolumeSliceRenderer2D#draw()
	 */
	@Override
	public void draw() {
		synchronized (this) {
			GL2 gl = (GL2) ((PGraphicsOpenGL2) applet.g).beginGL();
			if (showMaskParam != null && showMaskParam.getValue()) {
				gl.glEnable(GL.GL_TEXTURE_2D);
				gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[0]);
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,
						simulator.getRows(), simulator.getCols(), 0,
						GL.GL_RGBA, GL.GL_FLOAT, pixelBuffer.getBuffer());

				gl.glDisable(GL.GL_DEPTH_TEST);
				gl.glBegin(GL2.GL_QUADS);
				gl.glTexCoord2f(0, 1);
				gl.glVertex2f(0, simulator.getCols());

				gl.glTexCoord2f(1, 1);
				gl.glVertex2f(simulator.getRows(), simulator.getCols());

				gl.glTexCoord2f(1, 0);
				gl.glVertex2f(simulator.getRows(), 0);

				gl.glTexCoord2f(0, 0);
				gl.glVertex2f(0, 0);

				gl.glEnd();
				gl.glEnable(GL.GL_DEPTH_TEST);
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			if (showContour) {
				if (isoSurfaceVertexBuffer != null) {

					gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
					gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
					gl.glVertexPointer(2, GL.GL_FLOAT, 0,
							isoSurfaceVertexBuffer.getBuffer());
					gl.glColorPointer(4, GL.GL_FLOAT, 0,
							isoSurfaceColorBuffer.getBuffer());
					gl.glEnable(GL.GL_LINE_SMOOTH);
					gl.glLineWidth(strokeWeightParam.getFloat());
					// gl.glColor4f(1.0f,0.0f,0.0f,1.0f);
					gl.glDrawArrays(GL.GL_LINES, 0, isoSurfaceVertexBuffer
							.getBuffer().capacity() / 2);
					gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
					gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);

				}

			}

			((PGraphicsOpenGL2) applet.g).endGL();
		}
	}

	/**
	 * Sets the visible.
	 * 
	 * @param visible
	 *            the new visible
	 */
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Draw circle.
	 * 
	 * @param resolution
	 *            the resolution
	 * @param center
	 *            the center
	 * @param radius
	 *            the radius
	 */
	private void drawCircle(int resolution, Point2f center, double radius) {
		applet.beginShape(PConstants.POLYGON);
		double dTheta = 2 * Math.PI / resolution;
		for (int i = 0; i < resolution; i++) {
			applet.vertex((float) (center.x + radius * Math.cos(i * dTheta)),
					(float) (center.y + radius * Math.sin(i * dTheta)));
		}
		applet.endShape(PConstants.CLOSE);
	}

	/**
	 * Sets the slice number visible.
	 * 
	 * @param showSliceNumber
	 *            the new slice number visible
	 */
	private void setSliceNumberVisible(boolean showSliceNumber) {
		this.showSliceNumber = showSliceNumber;
	}
}