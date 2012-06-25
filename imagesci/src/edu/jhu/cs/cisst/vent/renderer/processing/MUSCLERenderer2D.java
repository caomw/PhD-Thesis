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
import java.awt.Font;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLPointerFunc;
import javax.vecmath.Color4f;

import org.imagesci.mogac.WEMOGAC2D;
import org.imagesci.muscle.MuscleActiveContour2D;
import org.imagesci.muscle.MuscleCommon2D;
import org.imagesci.springls.ActiveContour2D;
import org.imagesci.springls.PrefixScanCPU;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsConstants;

import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opengl.util.awt.TextRenderer;

import edu.jhu.cs.cisst.vent.VisualizationProcessing2D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsMeshRenderer.
 */
public class MUSCLERenderer2D extends VolumeSliceRenderer2D implements
		ActiveContour2D.FrameUpdateListener {
	/** The Constant WORKGROUP_SIZE. */
	protected static final int WORKGROUP_SIZE = 256;
	
	/** The active contour. */
	protected MuscleActiveContour2D activeContour;
	/** The applet. */
	protected VisualizationProcessing2D applet = null;
	
	/** The color buffer. */
	protected CLBuffer<FloatBuffer> colorBuffer;
	
	/** The color param. */
	protected ParamColor colorParam;
	
	/** The colors. */
	protected Color4f colors[];
	/** The commons. */
	protected MuscleCommon2D commons;
	/** The compute fps. */
	protected double computeFPS = -1;

	/** The context. */
	protected CLContext context;
	
	/** The contour colors param. */
	protected ParamColor[] contourColorsParam;
	/** The copy iso surface to mesh. */
	protected CLKernel copyCapsulesToMesh;
	
	/** The count elements. */
	CLKernel countElements;

	/** Instantiates a new springls mesh renderer. */
	protected int elements = 0;

	/** The iso contour renderer. */
	CLKernel isoContourRenderer;
	
	/** The iso surface color buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceColorBuffer;
	/** The iso surface vertex buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceVertexBuffer;
	
	/** The iso surf count. */
	CLKernel isoSurfCount;
	
	/** The iso surf gen. */
	CLKernel isoSurfGen;
	/** The last element count. */
	int lastElementCount = 0;
	
	/** The map point buffer. */
	protected CLBuffer<FloatBuffer> mapPointBuffer;
	
	/** The mogac. */
	protected WEMOGAC2D mogac;

	/** The normal segment buffer. */
	protected CLBuffer<FloatBuffer> normalSegmentBuffer;

	// protected CLBuffer<FloatBuffer> isoSurfaceCorrespondenceBuffer;
	/** The particle buffer. */
	protected CLBuffer<FloatBuffer> particleBuffer;

	/** The particle color buffer. */
	protected CLBuffer<FloatBuffer> particleColorBuffer;

	/** The pixel buffer. */
	protected CLBuffer<FloatBuffer> pixelBuffer;

	/** The queue. */
	protected CLCommandQueue queue;

	/** The render fps. */
	protected double renderFPS = -1;
	/** The show iso surface. */
	protected boolean showIsoSurface = true;

	/** The show normals. */
	protected boolean showNormals = true;
	/** The show particles. */
	protected boolean showParticles = true;
	
	/** The show region. */
	protected boolean showRegion = true;
	/** The show triangles. */
	protected boolean showSegments = true;
	/** The show vertexes. */
	protected boolean showVertexes = true;
	/** The start time. */
	protected long startTime = System.nanoTime();
	
	/** The STRIDE. */
	protected final int STRIDE = 1024;

	/** The stroke weight param. */
	protected ParamDouble strokeWeightParam;

	/** The tex buffer. */
	protected IntBuffer texBuffer = IntBuffer.allocate(1);

	/** The text renderer. */
	protected TextRenderer textRenderer;

	/** The updating frame. */
	protected long timeStep = 0;

	/** The diffuse color. */
	/** The transparency. */
	protected float transparency = 0.65f;

	/** The transparency param. */
	protected ParamDouble transparencyParam;

	/** The vertex buffer. */
	protected CLBuffer<FloatBuffer> vertexBuffer;

	/** The show iso surface param. */
	protected ParamBoolean visibleParam, showVertexesParam, showNormalsParam,
			showColorsParam, showParticlesParam, showTextParam,
			showIsoSurfaceParam, segmentationParam, showMappingParam;

	/**
	 * Instantiates a new mUSCLE renderer2 d.
	 *
	 * @param applet the applet
	 * @param activeContour the active contour
	 */
	public MUSCLERenderer2D(VisualizationProcessing2D applet,
			MuscleActiveContour2D activeContour) {
		super(activeContour.getReferenceImage(), "Springls Contour", applet);
		this.applet = applet;
		this.commons = (MuscleCommon2D) activeContour.getCommons();
		this.activeContour = activeContour;
		mogac = activeContour.evolve;
		activeContour.addListener(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {
		context = commons.context;
		queue = commons.queue;
		try {
			CLProgram program = context
					.createProgram(
							getClass().getResourceAsStream(
									"MogacContourRenderer.cl"))
					.build(define("ROWS", commons.rows),
							define("COLS", commons.rows),
							define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
							define("SCALE_DOWN", SpringlsConstants.scaleDown
									+ "f"),
							define("NUM_OBJECTS",
									activeContour.evolve.getNumObjects()),
							define("CONTAINS_OVERLAPS",
									(mogac.containsOverlaps() ? 1 : 0)),
							ENABLE_MAD);
			copyCapsulesToMesh = program
					.createCLKernel("copyCapsulesToMeshMogac");
			isoContourRenderer = program.createCLKernel("IsoContourRenderer");
			isoSurfCount = program
					.createCLKernel(SpringlsCommon2D.ISO_SURF_COUNT);
			isoSurfGen = program.createCLKernel(SpringlsCommon2D.ISO_SURF_GEN);
			countElements = program
					.createCLKernel(SpringlsCommon2D.COUNT_ELEMENTS);
			pixelBuffer = context.createFloatBuffer(commons.rows * commons.cols
					* 4, READ_WRITE, USE_BUFFER);
			colorBuffer = context.createFloatBuffer(4 * mogac.getNumObjects(),
					READ_WRITE);
			colors = new Color4f[mogac.getNumObjects()];

		} catch (Exception e) {
			e.printStackTrace();
		}
		textRenderer = new TextRenderer(applet.getFont().deriveFont(Font.BOLD,
				14), true, true, null, false);
		textRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
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
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, commons.rows,
				commons.cols, 0, GL.GL_RGBA, GL.GL_FLOAT,
				pixelBuffer.getBuffer());
		gl.glDisable(GL.GL_TEXTURE_2D);
		((PGraphicsOpenGL2) applet.g).endGL();
		updateColors();
		frameUpdate(0, 0);
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		if (model == visibleParam) {
			showSegments = visibleParam.getValue();
		} else if (model == showVertexesParam) {
			showVertexes = showVertexesParam.getValue();
		} else if (model == showParticlesParam) {
			showParticles = showParticlesParam.getValue();
		} else if (model == showNormalsParam) {
			showNormals = showNormalsParam.getValue();
		} else if (model == showIsoSurfaceParam) {
			showIsoSurface = showIsoSurfaceParam.getValue();
		} else if (model instanceof ParamColor) {
			updateColors();
		} else if (model == segmentationParam) {
			showRegion = segmentationParam.getValue();
		} else if (model == transparencyParam) {
			transparency = transparencyParam.getFloat();
			updateRender();
		}
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		// TODO Auto-generated method stub
		visualizationParameters.setName("Springls Contour");
		visualizationParameters.add(strokeWeightParam = new ParamDouble(
				"Stroke Weight", 0, 20, 2));
		visualizationParameters.add(transparencyParam = new ParamDouble(
				"Transparency", 0, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(showIsoSurfaceParam = new ParamBoolean(
				"Show Iso-Surface", showIsoSurface));
		visualizationParameters.add(segmentationParam = new ParamBoolean(
				"Show Segmentation", showRegion));
		visualizationParameters.add(visibleParam = new ParamBoolean(
				"Show Surface Elements", showSegments));
		visualizationParameters.add(showVertexesParam = new ParamBoolean(
				"Show Vertexes", showVertexes));
		visualizationParameters.add(showParticlesParam = new ParamBoolean(
				"Show Particles", showParticles));
		visualizationParameters.add(showNormalsParam = new ParamBoolean(
				"Show Normals", showNormals));
		visualizationParameters.add(showMappingParam = new ParamBoolean(
				"Show Mapping", true));
		visualizationParameters.add(showTextParam = new ParamBoolean(
				"Show Text", true));
		final long seed = 5437897311l;
		Random randn = new Random(seed);
		contourColorsParam = new ParamColor[mogac.getNumObjects()];

		for (int i = 0; i < contourColorsParam.length; i++) {
			visualizationParameters.add(contourColorsParam[i] = new ParamColor(
					"Contour Color [" + (i + 1) + "]",
					(new Color(randn.nextFloat(), randn.nextFloat(), randn
							.nextFloat()))));
		}
		updateColors();
	}

	/**
	 * Update visualization parameters.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		showVertexes = showVertexesParam.getValue();
		showParticles = showParticlesParam.getValue();
		showNormals = showNormalsParam.getValue();
		showSegments = visibleParam.getValue();
		showIsoSurface = showIsoSurfaceParam.getValue();
		showRegion = segmentationParam.getValue();
		transparency = transparencyParam.getFloat();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.algorithms.segmentation.SpringlsActiveContour3D.
	 * FrameUpdateListener#frameUpdate(int, double,
	 * edu.jhu.cs.cisst.algorithms.springls.SpringlsCommon)
	 */
	@Override
	public void frameUpdate(long time, double fps) {
		this.computeFPS = fps;
		this.timeStep = time;
		synchronized (this) {
			updateCapsules();
			updateRender();
			if (showIsoSurfaceParam.getValue()) {
				updateIsoSurface();
			}
		}
	}

	/**
	 * Update capsules.
	 */
	private void updateCapsules() {
		elements = commons.elements;
		if (lastElementCount != elements) {
			queue.finish();
			if (vertexBuffer != null) {
				vertexBuffer.release();
			}
			if (particleBuffer != null) {
				particleBuffer.release();
			}
			if (particleColorBuffer != null) {
				particleColorBuffer.release();
			}
			if (normalSegmentBuffer != null) {
				normalSegmentBuffer.release();
			}
			if (mapPointBuffer != null) {
				mapPointBuffer.release();
			}
			vertexBuffer = context.createFloatBuffer(2 * 2 * elements,
					CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
			particleBuffer = context.createFloatBuffer(2 * elements,
					CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
			particleColorBuffer = context.createFloatBuffer(4 * elements,
					CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
			normalSegmentBuffer = context.createFloatBuffer(2 * 2 * elements,
					CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
			mapPointBuffer = context.createFloatBuffer(elements * 2 * 2,
					CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
		}
		copyCapsulesToMesh
				.putArgs(mogac.imageLabelBuffer, commons.springlLabelBuffer,
						colorBuffer, commons.capsuleBuffer, vertexBuffer,
						particleBuffer, particleColorBuffer,
						normalSegmentBuffer, mapPointBuffer).putArg(elements)
				.rewind();
		queue.put1DRangeKernel(copyCapsulesToMesh, 0, commons.arrayLength,
				WORKGROUP_SIZE);
		queue.finish();
		queue.putBarrier().putReadBuffer(vertexBuffer, true)
				.putReadBuffer(particleBuffer, true)
				.putReadBuffer(particleColorBuffer, true)
				.putReadBuffer(normalSegmentBuffer, true)
				.putReadBuffer(mapPointBuffer, true);
	}

	/**
	 * Update render.
	 */
	private void updateRender() {
		if (isoContourRenderer != null) {
			isoContourRenderer
					.putArgs(mogac.imageLabelBuffer, mogac.distanceFieldBuffer,
							pixelBuffer, colorBuffer)
					.putArg(transparencyParam.getFloat()).rewind();
			queue.put1DRangeKernel(isoContourRenderer, 0, commons.rows
					* commons.cols, WORKGROUP_SIZE);
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
		isoSurfCount.putArgs(mogac.distanceFieldBuffer, mogac.imageLabelBuffer,
				counts).rewind();
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
					mogac.distanceFieldBuffer, mogac.imageLabelBuffer,
					isoSurfaceColorBuffer, colorBuffer, counts).rewind();
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

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {
		synchronized (this) {
			GL2 gl = (GL2) (((PGraphicsOpenGL2) applet.g).beginGL());
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glEnable(GL2GL3.GL_POLYGON_SMOOTH);
			gl.glEnable(GL2ES1.GL_POINT_SMOOTH);
			gl.glDisable(GLLightingFunc.GL_LIGHTING);
			gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
			if (showRegion) {
				gl.glEnable(GL.GL_TEXTURE_2D);
				gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[0]);
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, commons.rows,
						commons.cols, 0, GL.GL_RGBA, GL.GL_FLOAT,
						pixelBuffer.getBuffer());

				gl.glDisable(GL.GL_DEPTH_TEST);
				gl.glBegin(GL2.GL_QUADS);
				gl.glTexCoord2f(0, 1);
				gl.glVertex2f(0, commons.cols);

				gl.glTexCoord2f(1, 1);
				gl.glVertex2f(commons.rows, commons.cols);

				gl.glTexCoord2f(1, 0);
				gl.glVertex2f(commons.rows, 0);

				gl.glTexCoord2f(0, 0);
				gl.glVertex2f(0, 0);

				gl.glEnd();
				gl.glEnable(GL.GL_DEPTH_TEST);
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			if (isoSurfaceVertexBuffer != null && showIsoSurface) {
				gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						isoSurfaceVertexBuffer.getBuffer());

				gl.glColorPointer(4, GL.GL_FLOAT, 0,
						isoSurfaceColorBuffer.getBuffer());
				gl.glLineWidth(strokeWeightParam.getFloat());
				gl.glDrawArrays(GL.GL_LINES, 0, isoSurfaceVertexBuffer
						.getBuffer().capacity() / 2);
				gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);

			}
			if (mapPointBuffer != null && showMappingParam.getValue()) {
				gl.glLineWidth(1.0f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						mapPointBuffer.getBuffer());
				gl.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
				gl.glDrawArrays(GL.GL_LINES, 0, 2 * elements);
				gl.glColor4f(0.6f, 0f, 0f, 1.0f);
				gl.glPointSize(2.0f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						mapPointBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, 2 * elements);

			}
			if (vertexBuffer != null && showSegments) {
				gl.glLineWidth(1.5f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer.getBuffer());
				gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
				gl.glDrawArrays(GL.GL_LINES, 0, 2 * elements);
			}
			if (normalSegmentBuffer != null && showNormals) {
				gl.glLineWidth(2.0f);
				gl.glColor4f(0, 0.8f, 0, 0.5f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						normalSegmentBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_LINES, 0, 2 * elements);
			}
			if (vertexBuffer != null && showVertexes) {
				gl.glColor4f(1.0f, 0.8f, 0f, 1.0f);
				gl.glPointSize(2.0f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, 2 * elements);
			}

			if (particleBuffer != null && showParticles) {
				gl.glPointSize(4.0f);
				gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				gl.glColorPointer(4, GL.GL_FLOAT, 0,
						particleColorBuffer.getBuffer());
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						particleBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, elements);
				gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);

			}

			gl.glEnable(GLLightingFunc.GL_LIGHTING);
			gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
			gl.glDisable(GL2ES1.GL_POINT_SMOOTH);
			((PGraphicsOpenGL2) applet.g).endGL();
			renderFPS = 1E9 / (System.nanoTime() - startTime);
			startTime = System.nanoTime();
		}
		if (renderFPS > 0 && showTextParam.getValue()) {
			textRenderer.beginRendering(applet.width, applet.height);
			textRenderer.draw(String.format("Grid Size: %d x %d", commons.rows,
					commons.cols), 10, applet.height - 20);
			textRenderer.draw(String.format("Springls: %d", commons.elements),
					10, applet.height - 40);
			textRenderer.draw(String.format("Iteration: %d", timeStep), 10,
					applet.height - 60);
			textRenderer.draw(
					String.format("Render Frame Rate: %4.1f", renderFPS), 10,
					applet.height - 80);
			if (computeFPS > 0) {
				textRenderer.draw(
						String.format("Compute Frame Rate: %6.3f", computeFPS),
						10, applet.height - 100);
			}
			textRenderer.endRendering();
		}
	}

}
