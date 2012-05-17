/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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
package edu.jhu.cs.cisst.vent.renderer.processing;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;


import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLPointerFunc;

import org.imagesci.gac.WEGAC2D;
import org.imagesci.springls.ActiveContour2D;
import org.imagesci.springls.PrefixScanCPU;
import org.imagesci.springls.SpringlsConstants;

import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opengl.util.awt.TextRenderer;

import edu.jhu.cs.cisst.vent.VisualizationProcessing2D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsMeshRenderer.
 */
public class ActiveContourRenderer2D extends VolumeSliceRenderer2D implements
		ActiveContour2D.FrameUpdateListener {

	/** The Constant WORKGROUP_SIZE. */
	protected static final int WORKGROUP_SIZE = 256;
	/** The commons. */
	protected WEGAC2D activeContour;
	/** The applet. */
	protected VisualizationProcessing2D applet = null;

	/** The color param. */
	protected ParamColor colorParam;

	/** The compute fps. */
	protected double computeFPS = -1;

	/** The context. */
	protected CLContext context;

	/** The copy iso surface to mesh. */
	protected CLKernel copyCapsulesToMesh, springlsContourRenderer,
			isoSurfCount, isoSurfGen;

	/** The diffuse color. */
	protected Color diffuseColor = new Color(0, 102, 204);

	/** Instantiates a new renderer. */
	protected int elements = 0;

	/** The iso surface vertex buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceVertexBuffer;
	/** The last element count. */
	int lastElementCount = 0;
	/** The vertex buffer. */
	protected CLBuffer<FloatBuffer> pixelBuffer;
	/** The queue. */
	protected CLCommandQueue queue;
	/** The render fps. */
	protected double renderFPS = -1;
	/** The show iso surface. */
	protected boolean showIsoSurface = true;
	
	/** The show region. */
	protected boolean showRegion = true;
	/** The show iso surface param. */
	protected ParamBoolean showTextParam, showIsoSurfaceParam;
	
	/** The signed level set copy. */
	protected CLBuffer<FloatBuffer> signedLevelSetCopy;

	/** The start time. */
	protected long startTime = System.nanoTime();

	/** The stroke weight param. */
	protected ParamFloat strokeWeightParam;

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

	/**
	 * Instantiates a new active contour renderer2 d.
	 *
	 * @param applet the applet
	 * @param activeContour the active contour
	 */
	public ActiveContourRenderer2D(VisualizationProcessing2D applet,
			WEGAC2D activeContour) {
		super(activeContour.getSignedLevelSet(), "Springls Contour", applet);
		this.applet = applet;
		boolean usingGPU = false;
		CLDevice device = null;
		this.activeContour = activeContour;
		activeContour.addListener(this);
		try {
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

			System.out.println("Springls renderer using device: "
					+ device.getVendor() + " " + device.getVersion() + " "
					+ device.getName());

			CLProgram program = context
					.createProgram(
							getClass().getResourceAsStream(
									"SpringlsContourRenderer.cl"))
					.build(define("ROWS", activeContour.getRows()),
							define("COLS", activeContour.getCols()),
							define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
							define("SCALE_DOWN", SpringlsConstants.scaleDown
									+ "f"), define("GPU", (usingGPU) ? 1 : 0));
			copyCapsulesToMesh = program.createCLKernel("copyCapsulesToMesh");
			springlsContourRenderer = program
					.createCLKernel("springlsContourRenderer");
			isoSurfCount = program.createCLKernel("isoSurfCount");
			isoSurfGen = program.createCLKernel("isoSurfGen");
			pixelBuffer = context.createFloatBuffer(activeContour.getRows()
					* activeContour.getCols() * 4, READ_WRITE, USE_BUFFER);
			signedLevelSetCopy = context.createFloatBuffer(
					activeContour.getRows() * activeContour.getCols(),
					READ_WRITE, USE_BUFFER);
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
			activeContour.queue.putReadBuffer(
					activeContour.signedLevelSetBuffer, true);
			signedLevelSetCopy.getBuffer()
					.put(activeContour.signedLevelSetBuffer.getBuffer())
					.rewind();
			activeContour.signedLevelSetBuffer.getBuffer().rewind();
			queue.putWriteBuffer(signedLevelSetCopy, true);
			queue.finish();
			updateColor();
			updatingFrame = false;
			if (showIsoSurface) {
				updateIsoSurface();
			}
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
			showRegion = visibleParam.getValue();
		} else if (model == showIsoSurfaceParam) {
			showIsoSurface = showIsoSurfaceParam.getValue();
		} else if (model == colorParam) {
			diffuseColor = colorParam.getValue();
			updateColor();
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
		showRegion = visibleParam.getValue();
		showIsoSurface = showIsoSurfaceParam.getValue();
		diffuseColor = colorParam.getValue();
		transparency = transparencyParam.getFloat();
		updateColor();
	}

	/**
	 * Update color.
	 */
	private void updateColor() {
		final float scale = 0.0039215f;
		if (activeContour.queue.getDevice().getType() == CLDevice.Type.CPU) {
			springlsContourRenderer.putArgs(signedLevelSetCopy, pixelBuffer)
					.putArg(scale * diffuseColor.getRed())
					.putArg(scale * diffuseColor.getGreen())
					.putArg(scale * diffuseColor.getBlue())
					.putArg(transparency).rewind();
		} else {
			springlsContourRenderer
					.putArgs(activeContour.signedLevelSetBuffer, pixelBuffer)
					.putArg(scale * diffuseColor.getRed())
					.putArg(scale * diffuseColor.getGreen())
					.putArg(scale * diffuseColor.getBlue())
					.putArg(transparency).rewind();
		}
		queue.put1DRangeKernel(springlsContourRenderer, 0,
				activeContour.getRows() * activeContour.getCols(),
				WORKGROUP_SIZE);
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
		visualizationParameters.add(strokeWeightParam = new ParamFloat(
				"Stroke Weight", 0, 100000, 2));
		visualizationParameters.add(colorParam = new ParamColor(
				"Surface Color", diffuseColor));
		visualizationParameters.add(transparencyParam = new ParamDouble(
				"Transparency", 0, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(visibleParam = new ParamBoolean(
				"Show Segmentation", showRegion));
		visualizationParameters.add(showIsoSurfaceParam = new ParamBoolean(
				"Show Iso-Surface", showIsoSurface));
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
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,
						activeContour.getRows(), activeContour.getCols(), 0,
						GL.GL_RGBA, GL.GL_FLOAT, pixelBuffer.getBuffer());

				gl.glDisable(GL.GL_DEPTH_TEST);
				gl.glBegin(GL2.GL_QUADS);
				gl.glTexCoord2f(0, 1);
				gl.glVertex2f(0, activeContour.getCols());

				gl.glTexCoord2f(1, 1);
				gl.glVertex2f(activeContour.getRows(), activeContour.getCols());

				gl.glTexCoord2f(1, 0);
				gl.glVertex2f(activeContour.getRows(), 0);

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
				gl.glLineWidth(strokeWeightParam.getFloat());
				final float scale = 0.0039215f;
				gl.glColor4f(scale * diffuseColor.getRed(), scale
						* diffuseColor.getGreen(),
						scale * diffuseColor.getBlue(), 1.0f);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
				gl.glDrawArrays(GL.GL_LINES, 0, isoSurfaceVertexBuffer
						.getBuffer().capacity() / 2);
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
			textRenderer.draw(String.format("Grid Size: %d x %d",
					activeContour.getRows(), activeContour.getCols()), 10,
					applet.height - 20);
			textRenderer.draw(String.format("Iteration: %d", timeStep), 10,
					applet.height - 40);
			textRenderer.draw(
					String.format("Render Frame Rate: %4.1f", renderFPS), 10,
					applet.height - 60);
			if (computeFPS > 0) {
				textRenderer.draw(
						String.format("Compute Frame Rate: %6.3f", computeFPS),
						10, applet.height - 80);
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
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,
				activeContour.getRows(), activeContour.getCols(), 0,
				GL.GL_RGBA, GL.GL_FLOAT, pixelBuffer.getBuffer());
		gl.glDisable(GL.GL_TEXTURE_2D);
		((PGraphicsOpenGL2) applet.g).endGL();
	}
}
