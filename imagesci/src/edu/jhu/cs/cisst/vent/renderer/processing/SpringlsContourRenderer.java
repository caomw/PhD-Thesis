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
package edu.jhu.cs.cisst.vent.renderer.processing;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;


import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLPointerFunc;

import org.imagesci.springls.ActiveContour2D;
import org.imagesci.springls.PrefixScanCPU;
import org.imagesci.springls.Springl2D;
import org.imagesci.springls.SpringlsActiveContour2D;
import org.imagesci.springls.SpringlsCommon2D;
import org.imagesci.springls.SpringlsConstants;

import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLPlatform;
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
public class SpringlsContourRenderer extends VolumeSliceRenderer2D implements
		ActiveContour2D.FrameUpdateListener {

	/** The Constant WORKGROUP_SIZE. */
	protected static final int WORKGROUP_SIZE = 256;
	/** The applet. */
	protected VisualizationProcessing2D applet = null;
	/** The capsule buffer copy. */
	protected CLBuffer<ByteBuffer> capsuleBufferCopy;
	
	/** The color param. */
	protected ParamColor colorParam;
	/** The commons. */
	protected SpringlsCommon2D commons;

	/** The compute fps. */
	protected double computeFPS = -1;
	/** The context. */
	protected CLContext context;

	/** The copy iso surface to mesh. */
	protected CLKernel copyCapsulesToMesh, springlsContourRenderer,
			isoSurfCount, isoSurfGen;

	/** The diffuse color. */
	protected Color diffuseColor = new Color(0, 102, 102);
	
	/** Instantiates a new renderer. */
	protected int elements = 0;
	
	/** The iso surface correspondence buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceCorrespondenceBuffer;

	/** The iso surface vertex buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceVertexBuffer;

	/** The label buffer copy. */
	protected CLBuffer<IntBuffer> labelBufferCopy;

	/** The last element count. */
	int lastElementCount = 0;

	/** The map point buffer. */
	protected CLBuffer<FloatBuffer> mapPointBuffer;

	/** The normal segment buffer. */
	protected CLBuffer<FloatBuffer> normalSegmentBuffer;

	/** The particle buffer. */
	protected CLBuffer<FloatBuffer> particleBuffer;

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
	
	/** The signed level set copy. */
	protected CLBuffer<FloatBuffer> signedLevelSetCopy;
	/** The start time. */
	protected long startTime = System.nanoTime();

	/** The tex buffer. */
	protected IntBuffer texBuffer = IntBuffer.allocate(1);

	/** The text renderer. */
	protected TextRenderer textRenderer;

	/** The time step. */
	protected long timeStep = 0;

	/** The transparency. */
	protected float transparency = 1.0f;

	/** The transparency param. */
	protected ParamDouble transparencyParam;

	/** The updating frame. */
	protected boolean updatingFrame = true;

	/** The vertex buffer. */
	protected CLBuffer<FloatBuffer> vertexBuffer;

	/** The show iso surface param. */
	protected ParamBoolean visibleParam, showVertexesParam, showNormalsParam,
			showColorsParam, showParticlesParam, showTextParam,
			showIsoSurfaceParam, segmentationParam, showMappingParam;

	/**
	 * Instantiates a new springls contour renderer.
	 *
	 * @param applet the applet
	 * @param activeContour the active contour
	 */
	public SpringlsContourRenderer(VisualizationProcessing2D applet,
			SpringlsActiveContour2D activeContour) {
		super(activeContour.getSignedLevelSet(), "Springls Contour", applet);
		this.applet = applet;
		this.commons = activeContour.getCommons();
		boolean usingGPU = false;
		CLDevice device = null;
		activeContour.addListener(this);
		try {
			if (commons.getType() == CLDevice.Type.GPU) {
				context = commons.context;
				queue = commons.queue;
				device = commons.queue.getDevice();
				usingGPU = true;
			} else {
				usingGPU = true;
				CLPlatform[] platforms = CLPlatform.listCLPlatforms();

				for (CLPlatform p : platforms) {
					device = p.getMaxFlopsDevice(CLDevice.Type.GPU);
					if (device != null) {
						break;
					}
				}
				if (device == null) {
					usingGPU = false;
					device = CLPlatform.getDefault().getMaxFlopsDevice();
				}

				context = CLContext.create(device);

				queue = device.createCommandQueue();

			}
			System.out.println("Springls renderer using device: "
					+ device.getVendor() + " " + device.getVersion() + " "
					+ device.getName());

			CLProgram program = context
					.createProgram(
							getClass().getResourceAsStream(
									"SpringlsContourRenderer.cl"))
					.build(define("ROWS", commons.rows),
							define("COLS", commons.cols),
							define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
							define("SCALE_DOWN", SpringlsConstants.scaleDown
									+ "f"), define("GPU", (usingGPU) ? 1 : 0));
			copyCapsulesToMesh = program.createCLKernel("copyCapsulesToMesh");
			springlsContourRenderer = program
					.createCLKernel("springlsContourRenderer");
			isoSurfCount = program.createCLKernel("isoSurfCount");
			isoSurfGen = program.createCLKernel("isoSurfGen");
			pixelBuffer = context.createFloatBuffer(commons.rows * commons.cols
					* 4, READ_WRITE, USE_BUFFER);
			signedLevelSetCopy = context.createFloatBuffer(commons.rows
					* commons.cols, READ_WRITE, USE_BUFFER);
			frameUpdate(0, 30);

		} catch (Exception e) {
			e.printStackTrace();
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
		updatingFrame = true;

		synchronized (this) {
			elements = commons.elements;
			if (lastElementCount != elements) {
				if (vertexBuffer != null) {
					vertexBuffer.release();
				}
				if (particleBuffer != null) {
					particleBuffer.release();
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
				normalSegmentBuffer = context.createFloatBuffer(
						2 * 2 * elements, CLMemory.Mem.READ_WRITE,
						CLMemory.Mem.USE_BUFFER);
				mapPointBuffer = context.createFloatBuffer(elements * 2 * 2,
						CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
			}
			if (commons.queue.getDevice().getType() == CLDevice.Type.CPU) {

				if (lastElementCount != elements) {
					if (capsuleBufferCopy != null) {
						capsuleBufferCopy.release();
					}
					if (labelBufferCopy != null) {
						labelBufferCopy.release();
					}

					capsuleBufferCopy = context.createByteBuffer(
							Springl2D.BYTE_SIZE * commons.arrayLength,
							READ_WRITE, USE_BUFFER);

					labelBufferCopy = context.createIntBuffer(
							commons.arrayLength, READ_WRITE, USE_BUFFER);
				}

				lastElementCount = elements;

				commons.queue.putReadBuffer(commons.signedLevelSetBuffer, true);
				signedLevelSetCopy.getBuffer()
						.put(commons.signedLevelSetBuffer.getBuffer()).rewind();
				commons.signedLevelSetBuffer.getBuffer().rewind();
				commons.queue.putReadBuffer(commons.capsuleBuffer, true);
				commons.queue.putReadBuffer(commons.springlLabelBuffer, true);
				capsuleBufferCopy.getBuffer()
						.put(commons.capsuleBuffer.getBuffer()).rewind();
				commons.capsuleBuffer.getBuffer().rewind();
				labelBufferCopy.getBuffer()
						.put(commons.springlLabelBuffer.getBuffer()).rewind();
				commons.springlLabelBuffer.getBuffer().rewind();
				queue.putWriteBuffer(capsuleBufferCopy, true)
						.putWriteBuffer(labelBufferCopy, true)
						.putWriteBuffer(signedLevelSetCopy, true);
				copyCapsulesToMesh
						.putArgs(labelBufferCopy, capsuleBufferCopy,
								vertexBuffer, particleBuffer,
								normalSegmentBuffer, mapPointBuffer)
						.putArg(elements).rewind();
				queue.put1DRangeKernel(copyCapsulesToMesh, 0,
						commons.arrayLength, WORKGROUP_SIZE);

			} else {
				copyCapsulesToMesh
						.putArgs(commons.springlLabelBuffer,
								commons.capsuleBuffer, vertexBuffer,
								particleBuffer, normalSegmentBuffer,
								mapPointBuffer).putArg(elements).rewind();
				queue.put1DRangeKernel(copyCapsulesToMesh, 0,
						commons.arrayLength, WORKGROUP_SIZE);
			}

			queue.finish();
			updateColor();
			queue.putBarrier().putReadBuffer(vertexBuffer, true)
					.putReadBuffer(particleBuffer, true)
					.putReadBuffer(normalSegmentBuffer, true)
					.putReadBuffer(mapPointBuffer, true);

			updatingFrame = false;

		}
		if (showIsoSurface) {
			updateIsoSurface();
		}
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
		} else if (model == colorParam) {
			diffuseColor = colorParam.getValue();
			updateColor();
		} else if (model == segmentationParam) {
			showRegion = segmentationParam.getValue();
		} else if (model == transparencyParam) {
			transparency = transparencyParam.getFloat();
			updateColor();
		}
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
		diffuseColor = colorParam.getValue();
		showRegion = segmentationParam.getValue();
		transparency = transparencyParam.getFloat();
		updateColor();
	}

	/**
	 * Update color.
	 */
	private void updateColor() {
		final float scale = 0.0039215f;
		if (commons.queue.getDevice().getType() == CLDevice.Type.CPU) {
			springlsContourRenderer.putArgs(signedLevelSetCopy, pixelBuffer)
					.putArg(scale * diffuseColor.getRed())
					.putArg(scale * diffuseColor.getGreen())
					.putArg(scale * diffuseColor.getBlue())
					.putArg(transparency).rewind();
		} else {
			springlsContourRenderer
					.putArgs(commons.signedLevelSetBuffer, pixelBuffer)
					.putArg(scale * diffuseColor.getRed())
					.putArg(scale * diffuseColor.getGreen())
					.putArg(scale * diffuseColor.getBlue())
					.putArg(transparency).rewind();
		}
		queue.put1DRangeKernel(springlsContourRenderer, 0, commons.rows
				* commons.cols, WORKGROUP_SIZE);
		queue.putReadBuffer(pixelBuffer, true);
	}

	/**
	 * Update iso surface.
	 */
	public void updateIsoSurface() {

		if (isoSurfaceVertexBuffer != null) {
			isoSurfaceVertexBuffer.release();
		}
		CLBuffer<IntBuffer> counts = context.createIntBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);
		isoSurfCount.putArgs(signedLevelSetCopy, counts).putArg(1).rewind();
		queue.put1DRangeKernel(isoSurfCount, 0, rows * cols, WORKGROUP_SIZE);
		queue.finish();
		try {
			final PrefixScanCPU scan = new PrefixScanCPU(queue, WORKGROUP_SIZE,
					32);
			int vertexCount = scan.scan(counts, rows * cols);
			isoSurfaceVertexBuffer = context.createFloatBuffer(2 * vertexCount,
					READ_WRITE, USE_BUFFER);
			isoSurfGen
					.putArgs(isoSurfaceVertexBuffer, signedLevelSetCopy, counts)
					.putArg(1).rewind();
			queue.put1DRangeKernel(isoSurfGen, 0, rows * cols, WORKGROUP_SIZE);
			queue.finish();
			queue.putReadBuffer(isoSurfaceVertexBuffer, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		counts.release();
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
		visualizationParameters.setName("Active Contour");
		visualizationParameters.add(colorParam = new ParamColor(
				"Surface Color", diffuseColor));
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
			gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			gl.glShadeModel(GLLightingFunc.GL_FLAT);
			gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);

			gl.glDisable(GLLightingFunc.GL_LIGHTING);

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
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						isoSurfaceVertexBuffer.getBuffer());

				gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);

				gl.glShadeModel(GLLightingFunc.GL_FLAT);
				gl.glLineWidth(3.0f);
				final float scale = 0.0039215f;
				gl.glColor4f(scale * diffuseColor.getRed(), scale
						* diffuseColor.getGreen(),
						scale * diffuseColor.getBlue(), 1.0f);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
				gl.glDrawArrays(GL.GL_LINES, 0, isoSurfaceVertexBuffer
						.getBuffer().capacity() / 2);
			}
			if (showMappingParam.getValue()) {
				gl.glLineWidth(1.0f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						mapPointBuffer.getBuffer());
				gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
				gl.glDrawArrays(GL.GL_LINES, 0, 2 * elements);

				gl.glColor4f(0.6f, 0f, 0f, 1.0f);
				gl.glPointSize(2.0f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						mapPointBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, 2 * elements);

			}
			if (showSegments) {
				gl.glLineWidth(1.5f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer.getBuffer());
				gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
				gl.glDrawArrays(GL.GL_LINES, 0, 2 * elements);
			}
			if (showNormals) {
				gl.glLineWidth(2.0f);
				gl.glColor4f(0, 0.8f, 0, 0.5f);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						normalSegmentBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_LINES, 0, 2 * elements);
			}
			if (showVertexes) {
				gl.glColor4f(1.0f, 0.8f, 0f, 1.0f);
				gl.glPointSize(2.0f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, 2 * elements);
			}

			if (showParticles) {
				gl.glColor4f(0.6f, 0f, 0f, 1.0f);
				gl.glPointSize(4.0f);
				gl.glVertexPointer(2, GL.GL_FLOAT, 0,
						particleBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, elements);

			}

			gl.glEnable(GLLightingFunc.GL_LIGHTING);
			gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {

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
	}
}
