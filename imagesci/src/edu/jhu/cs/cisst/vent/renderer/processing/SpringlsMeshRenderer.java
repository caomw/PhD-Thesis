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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.media.j3d.BoundingBox;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLPointerFunc;
import javax.vecmath.Point3d;

import org.imagesci.springls.PrefixScanCPU;
import org.imagesci.springls.Springl3D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.springls.ActiveContour3D.FrameUpdateListener;
import org.imagesci.utility.IsoSurfaceGenerator;

import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLImage3d;
import com.jogamp.opencl.CLImageFormat;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opengl.util.awt.TextRenderer;

import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
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
public class SpringlsMeshRenderer extends RendererProcessing3D implements
		FrameUpdateListener {
	/** The Constant WORKGROUP_SIZE. */
	protected static final int WORKGROUP_SIZE = 128;

	/** The a2i triangle connection table buffer. */
	public CLBuffer<IntBuffer> a2iTriangleConnectionTableBuffer;

	/** The active list buffer copy. */
	protected CLBuffer<IntBuffer> activeListBufferCopy;
	/** The ai cube edge flags buffer. */
	public CLBuffer<IntBuffer> aiCubeEdgeFlagsBuffer;
	/** The applet. */
	protected VisualizationProcessing3D applet = null;
	/** The capsule buffer copy. */
	protected CLBuffer<ByteBuffer> capsuleBufferCopy;

	/** The color buffer. */
	protected CLBuffer<FloatBuffer> colorBuffer;

	/** The color param. */
	protected ParamColor colorParam;

	/** The volume texture red. */
	protected CLImage3d<FloatBuffer> colorTexture;

	/** The commons. */
	protected SpringlsCommon3D commons;

	/** The compute fps. */
	protected double computeFPS = -1;

	/** The context. */
	protected CLContext context;

	/** The copy iso surface to mesh. */
	protected CLKernel copyCapsulesToMesh, copyIsoSurfaceToMesh;

	/** The CUB e_ dim. */
	protected final int CUBE_DIM = 8;
	/** The diffuse color. */
	protected Color diffuseColor = new Color(0, 153, 255);
	/** The iso surface vertex buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceBuffer;
	/** The iso surface color buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceColorBuffer;
	/** The iso surface normal buffer. */
	protected CLBuffer<FloatBuffer> isoSurfaceNormalBuffer;

	/** The iso surf gen. */
	CLKernel isoSurfCount, isoSurfGen, isoSurfGenSmooth;

	/** The label buffer copy. */
	protected CLBuffer<IntBuffer> labelBufferCopy;

	/** The last active list array size. */
	protected int lastActiveListArraySize = -1;

	/** The last active list size. */
	protected int lastActiveListSize = -1;

	/** The last array length. */
	protected int lastArrayLength = -1;

	/** The last element count. */
	int lastElementCount = 0;

	/** The normal buffer. */
	protected CLBuffer<FloatBuffer> normalBuffer;

	/** The normal segment buffer. */
	protected CLBuffer<FloatBuffer> normalSegmentBuffer;

	/** The particle buffer. */
	protected CLBuffer<FloatBuffer> particleBuffer;

	/** The queue. */
	protected CLCommandQueue queue;
	/** The render fps. */
	protected double renderFPS = -1;

	/** The scan. */
	protected PrefixScanCPU scan;

	/** The show colors. */
	protected boolean showColors = true;
	/** The show iso surface. */
	protected boolean showIsoSurface = false;
	/** The show normals. */
	protected boolean showNormals = false;
	/** The show particles. */
	protected boolean showParticles = false;
	/** The show triangles. */
	protected boolean showTriangles = true;
	/** The show vertexes. */
	protected boolean showVertexes = false;

	/** The signed level set buffer. */
	protected CLBuffer<FloatBuffer> signedLevelSetBuffer;

	/** The start time. */
	protected long startTime = System.nanoTime();

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
			showIsoSurfaceParam, wireframeParam, smoothNormalsParam;

	/** The wireframe. */
	protected boolean wireframe = true;

	/**
	 * Instantiates a new springls mesh renderer.
	 * 
	 * @param applet
	 *            the applet
	 * @param commons
	 *            the commons
	 */
	public SpringlsMeshRenderer(VisualizationProcessing3D applet,
			SpringlsCommon3D commons) {
		this.applet = applet;
		this.commons = commons;
		this.bbox = new BoundingBox(new Point3d(0, 0, 0), new Point3d(
				commons.rows, commons.cols, commons.slices));
		boolean usingGPU = false;
		CLDevice device = null;
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
			signedLevelSetBuffer = context.createFloatBuffer(commons.rows
					* commons.cols * commons.slices, READ_WRITE, USE_BUFFER);
			int[][][] colorCube = new int[CUBE_DIM][CUBE_DIM][CUBE_DIM];
			Random rand = new Random(21319321);
			for (int i = 0; i < CUBE_DIM; i++) {
				for (int j = 0; j < CUBE_DIM; j++) {
					for (int k = 0; k < CUBE_DIM; k++) {
						colorCube[i][j][k] = rand.nextInt();
					}
				}
			}
			for (int i = 0; i < CUBE_DIM; i++) {
				for (int j = 0; j < CUBE_DIM; j++) {
					for (int k = 0; k < CUBE_DIM; k++) {
						if (2 * i < CUBE_DIM || 2 * j < CUBE_DIM
								|| 2 * k < CUBE_DIM) {
							colorCube[i][j][k] = ~colorCube[CUBE_DIM - i - 1][CUBE_DIM
									- j - 1][CUBE_DIM - k - 1];
						}
					}
				}
			}
			FloatBuffer buff = FloatBuffer.allocate(CUBE_DIM * CUBE_DIM
					* CUBE_DIM * 4);
			for (int k = 0; k < CUBE_DIM; k++) {
				for (int j = 0; j < CUBE_DIM; j++) {
					for (int i = 0; i < CUBE_DIM; i++) {
						Color finalColor = (new Color(colorCube[i][j][k]));
						buff.put(finalColor.getRed() / 255.0f);
						buff.put(finalColor.getGreen() / 255.0f);
						buff.put(finalColor.getBlue() / 255.0f);
						buff.put(1.0f);
					}
				}
			}

			buff.rewind();
			CLImageFormat iformat = new CLImageFormat(
					CLImageFormat.ChannelOrder.RGBA,
					CLImageFormat.ChannelType.FLOAT);
			colorTexture = context.createImage3d(buff, CUBE_DIM, CUBE_DIM,
					CUBE_DIM, iformat, READ_WRITE, CLMemory.Mem.COPY_BUFFER);
			CLProgram program = context
					.createProgram(
							getClass().getResourceAsStream(
									"SpringlsSurfaceRenderer.cl"))
					.build(define("ROWS", commons.rows),
							define("COLS", commons.cols),
							define("SLICES", commons.slices),
							define("CUBE_DIM", CUBE_DIM),
							define("MAX_BIN_SIZE",
									SpringlsCommon3D.MAX_BIN_SIZE),
							define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
							define("SCALE_DOWN", SpringlsConstants.scaleDown
									+ "f"), define("GPU", (usingGPU) ? 1 : 0));
			isoSurfCount = program
					.createCLKernel(SpringlsCommon3D.ISO_SURF_COUNT);
			isoSurfGen = program.createCLKernel(SpringlsCommon3D.ISO_SURF_GEN);
			isoSurfGenSmooth = program.createCLKernel("isoSurfGenSmooth");
			copyCapsulesToMesh = program.createCLKernel("copyCapsulesToMesh");
			copyIsoSurfaceToMesh = program
					.createCLKernel("copyIsoSurfaceToMesh");
			loadLUT();
			scan = new PrefixScanCPU(queue, WORKGROUP_SIZE, 32);
			frameUpdate(0, 30);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the connectivity rule.
	 * 
	 */
	private void loadLUT() {
		int[] aiCubeEdgeFlags = null;
		int[][] a2iTriangleConnectionTable = null;
		aiCubeEdgeFlags = IsoSurfaceGenerator.aiCubeEdgeFlagsCC626;
		a2iTriangleConnectionTable = IsoSurfaceGenerator.a2iTriangleConnectionTableCC626;

		aiCubeEdgeFlagsBuffer = context.createIntBuffer(aiCubeEdgeFlags.length,
				CLMemory.Mem.READ_ONLY, CLMemory.Mem.USE_BUFFER);
		IntBuffer buff = aiCubeEdgeFlagsBuffer.getBuffer();
		for (int i = 0; i < aiCubeEdgeFlags.length; i++) {
			buff.put(aiCubeEdgeFlags[i]);
		}
		a2iTriangleConnectionTableBuffer = context.createIntBuffer(
				a2iTriangleConnectionTable.length * 16, CLMemory.Mem.READ_ONLY,
				CLMemory.Mem.USE_BUFFER);
		buff = a2iTriangleConnectionTableBuffer.getBuffer();
		for (int i = 0; i < a2iTriangleConnectionTable.length; i++) {
			int[] list = a2iTriangleConnectionTable[i];
			for (int j = 0; j < 16; j++) {
				if (j >= list.length) {
					buff.put(-1);
				} else {
					buff.put(a2iTriangleConnectionTable[i][j]);
				}
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
		boolean dirty = false;
		synchronized (this) {
			if (model == visibleParam) {
				showTriangles = visibleParam.getValue();
			} else if (model == showVertexesParam) {
				showVertexes = showVertexesParam.getValue();
			} else if (model == showParticlesParam) {
				showParticles = showParticlesParam.getValue();
			} else if (model == showNormalsParam) {
				showNormals = showNormalsParam.getValue();
			} else if (model == showColorsParam) {
				showColors = showColorsParam.getValue();
			} else if (model == showIsoSurfaceParam) {
				showIsoSurface = showIsoSurfaceParam.getValue();
				dirty = true;
			} else if (model == colorParam) {
				diffuseColor = colorParam.getValue();
			} else if (model == wireframeParam) {
				wireframe = wireframeParam.getValue();
			} else if (model == transparencyParam) {
				transparency = transparencyParam.getFloat();
				dirty = true;
			} else if (model == smoothNormalsParam) {
				dirty = true;
			}
		}
		if (dirty) {
			frameUpdate(-1, 0);
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
		if (time >= 0) {
			this.timeStep = time;
		}
		updatingFrame = true;
		synchronized (this) {
			int elements = commons.elements;

			if (lastElementCount != elements) {
				if (vertexBuffer != null) {
					vertexBuffer.release();
				}
				if (colorBuffer != null) {
					colorBuffer.release();
				}
				if (normalBuffer != null) {
					normalBuffer.release();
				}
				if (particleBuffer != null) {
					particleBuffer.release();
				}
				if (normalSegmentBuffer != null) {
					normalSegmentBuffer.release();
				}
				if (capsuleBufferCopy != null) {
					capsuleBufferCopy.release();
				}
				if (labelBufferCopy != null) {
					labelBufferCopy.release();
				}
				vertexBuffer = context.createFloatBuffer(4 * 3 * elements,
						CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
				colorBuffer = context.createFloatBuffer(4 * 3 * elements,
						CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
				normalBuffer = context.createFloatBuffer(3 * 3 * elements,
						CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
				particleBuffer = context.createFloatBuffer(4 * elements,
						CLMemory.Mem.READ_WRITE, CLMemory.Mem.USE_BUFFER);
				normalSegmentBuffer = context.createFloatBuffer(
						4 * 2 * elements, CLMemory.Mem.READ_WRITE,
						CLMemory.Mem.USE_BUFFER);
				capsuleBufferCopy = context.createByteBuffer(
						Springl3D.BYTE_SIZE * commons.arrayLength, READ_WRITE,
						USE_BUFFER);

				labelBufferCopy = context.createIntBuffer(commons.arrayLength,
						READ_WRITE, USE_BUFFER);
			}

			lastElementCount = elements;

			commons.queue.putReadBuffer(commons.capsuleBuffer, true);

			commons.queue.putReadBuffer(commons.springlLabelBuffer, true);
			capsuleBufferCopy.getBuffer()
					.put(commons.capsuleBuffer.getBuffer()).rewind();
			commons.capsuleBuffer.getBuffer().rewind();
			labelBufferCopy.getBuffer()
					.put(commons.springlLabelBuffer.getBuffer()).rewind();
			commons.springlLabelBuffer.getBuffer().rewind();
			queue.putWriteBuffer(capsuleBufferCopy, true)

			.putWriteBuffer(labelBufferCopy, true);
			copyCapsulesToMesh
					.putArgs(labelBufferCopy, capsuleBufferCopy, vertexBuffer,
							particleBuffer, normalBuffer, normalSegmentBuffer,
							colorTexture, colorBuffer).putArg(transparency)
					.putArg(elements).rewind();

			queue.put1DRangeKernel(copyCapsulesToMesh, 0, commons.arrayLength,
					WORKGROUP_SIZE);

			queue.putBarrier().putReadBuffer(normalBuffer, true)
					.putReadBuffer(vertexBuffer, true)
					.putReadBuffer(colorBuffer, true)
					.putReadBuffer(particleBuffer, true)
					.putReadBuffer(normalSegmentBuffer, true);
			queue.finish();
			updatingFrame = false;

			updateIsoSurface();

		}
	}

	/**
	 * Update iso surface.
	 */
	public void updateIsoSurface() {
		if (isoSurfaceBuffer != null) {
			isoSurfaceBuffer.release();
		}
		if (isoSurfaceNormalBuffer != null) {
			isoSurfaceNormalBuffer.release();
		}
		if (!showIsoSurface) {
			isoSurfaceBuffer = null;
			isoSurfaceNormalBuffer = null;
			return;
		}
		if (activeListBufferCopy == null
				|| lastActiveListArraySize != (commons).activeListArraySize) {
			if (activeListBufferCopy != null) {
				activeListBufferCopy.release();
			}
			lastActiveListArraySize = (commons).activeListArraySize;
			lastActiveListSize = (commons).activeListSize;
			activeListBufferCopy = context.createIntBuffer(
					(commons).activeListArraySize, READ_WRITE);

		}
		commons.queue.putReadBuffer((commons).activeListBuffer, true);
		activeListBufferCopy.getBuffer()
				.put((commons).activeListBuffer.getBuffer()).rewind();
		(commons).activeListBuffer.getBuffer().rewind();
		queue.putWriteBuffer(activeListBufferCopy, true);
		commons.queue.putReadBuffer(commons.signedLevelSetBuffer, true);
		signedLevelSetBuffer.getBuffer()
				.put(commons.signedLevelSetBuffer.getBuffer()).rewind();
		commons.signedLevelSetBuffer.getBuffer().rewind();
		queue.putWriteBuffer(signedLevelSetBuffer, true);
		int activeListSize = (commons).activeListSize;

		CLBuffer<IntBuffer> offsets = context.createIntBuffer(activeListSize,
				READ_WRITE, USE_BUFFER);
		isoSurfCount
				.putArgs(signedLevelSetBuffer, activeListBufferCopy, offsets,
						aiCubeEdgeFlagsBuffer, a2iTriangleConnectionTableBuffer)
				.putArg(1).putArg(activeListSize).rewind();
		queue.put1DRangeKernel(isoSurfCount, 0, SpringlsCommon3D
				.roundToWorkgroupPower(activeListSize, WORKGROUP_SIZE),
				WORKGROUP_SIZE);
		final int vertexCount = scan.scan(offsets, activeListSize);
		isoSurfaceBuffer = context.createFloatBuffer(4 * vertexCount,
				READ_WRITE, USE_BUFFER);
		isoSurfaceNormalBuffer = context.createFloatBuffer(4 * vertexCount,
				READ_WRITE, USE_BUFFER);
		if (smoothNormalsParam.getValue()) {
			isoSurfGenSmooth
					.putArgs(isoSurfaceBuffer, isoSurfaceNormalBuffer,
							signedLevelSetBuffer, activeListBufferCopy,
							offsets, aiCubeEdgeFlagsBuffer,
							a2iTriangleConnectionTableBuffer).putArg(1)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(isoSurfGenSmooth, 0, SpringlsCommon3D
					.roundToWorkgroupPower(activeListSize, WORKGROUP_SIZE),
					WORKGROUP_SIZE);
		} else {
			isoSurfGen
					.putArgs(isoSurfaceBuffer, isoSurfaceNormalBuffer,
							signedLevelSetBuffer, activeListBufferCopy,
							offsets, aiCubeEdgeFlagsBuffer,
							a2iTriangleConnectionTableBuffer).putArg(1)
					.putArg(activeListSize).rewind();

			queue.put1DRangeKernel(isoSurfGen, 0, SpringlsCommon3D
					.roundToWorkgroupPower(activeListSize, WORKGROUP_SIZE),
					WORKGROUP_SIZE);
		}
		queue.finish();
		queue.putReadBuffer(isoSurfaceBuffer, true);
		queue.putReadBuffer(isoSurfaceNormalBuffer, true);

		offsets.release();
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
		visualizationParameters.setName("Springls Mesh");
		visualizationParameters.add(colorParam = new ParamColor(
				"Surface Color", diffuseColor));
		visualizationParameters.add(transparencyParam = new ParamDouble(
				"Transparency", 0, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(visibleParam = new ParamBoolean(
				"Show Surface Elements", showTriangles));
		visualizationParameters.add(showIsoSurfaceParam = new ParamBoolean(
				"Show Iso-Surface", showIsoSurface));
		visualizationParameters.add(wireframeParam = new ParamBoolean(
				"Wireframe", wireframe));
		visualizationParameters.add(showVertexesParam = new ParamBoolean(
				"Show Vertexes", showVertexes));
		visualizationParameters.add(showParticlesParam = new ParamBoolean(
				"Show Particles", showParticles));
		visualizationParameters.add(showNormalsParam = new ParamBoolean(
				"Show Normals", showNormals));
		visualizationParameters.add(showColorsParam = new ParamBoolean("Color",
				showColors));
		visualizationParameters.add(showTextParam = new ParamBoolean(
				"Show Text", true));
		visualizationParameters.add(smoothNormalsParam = new ParamBoolean(
				"Smooth Normals", false));

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
			gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
			final float scale = 0.0039215f;

			if (isoSurfaceBuffer != null) {
				gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
				gl.glVertexPointer(4, GL.GL_FLOAT, 0,
						isoSurfaceBuffer.getBuffer());
				if (showIsoSurface) {
					if (transparency <= 0.99f) {
						gl.glDisable(GLLightingFunc.GL_LIGHTING);
						gl.glDisable(GL.GL_DEPTH_TEST);
					} else {
						gl.glEnable(GLLightingFunc.GL_LIGHTING);
					}
					if (isoSurfaceNormalBuffer != null && transparency > 0.99f) {
						gl.glNormalPointer(GL.GL_FLOAT, 0,
								isoSurfaceNormalBuffer.getBuffer());
					} else {
						gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
					}
					gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
					gl.glColor4f(scale * diffuseColor.getRed(), scale
							* diffuseColor.getGreen(),
							scale * diffuseColor.getBlue(), transparency);

					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
					gl.glDrawArrays(GL.GL_TRIANGLES, 0, isoSurfaceBuffer
							.getBuffer().capacity() / 4);

					gl.glDisable(GLLightingFunc.GL_LIGHTING);
					if (wireframe && !showTriangles) {
						if (isoSurfaceNormalBuffer != null) {
							gl.glNormalPointer(GL.GL_FLOAT, 0,
									isoSurfaceNormalBuffer.getBuffer());
						} else {
							gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
						}
						gl.glDisable(GLLightingFunc.GL_LIGHTING);
						gl.glShadeModel(GLLightingFunc.GL_FLAT);
						gl.glLineWidth(1.5f);
						gl.glColor4f(0, 0, 0, transparency);
						gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
						gl.glDrawArrays(GL.GL_TRIANGLES, 0, isoSurfaceBuffer
								.getBuffer().capacity() / 4);
					}
					gl.glEnable(GLLightingFunc.GL_LIGHTING);
					gl.glEnable(GL.GL_DEPTH_TEST);
				}

			}
			if (showTriangles) {
				gl.glLineWidth(1.5f);
				gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer.getBuffer());
				gl.glVertexPointer(4, GL.GL_FLOAT, 0, vertexBuffer.getBuffer());

				gl.glEnable(GLLightingFunc.GL_LIGHTING);
				if (showColors) {
					gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
					gl.glColorPointer(4, GL.GL_FLOAT, 0,
							colorBuffer.getBuffer());
				} else {
					gl.glColor4f(scale * diffuseColor.getRed(), scale
							* diffuseColor.getGreen(),
							scale * diffuseColor.getBlue(), transparency);
				}
				if (transparency <= 0.99f) {
					gl.glDisable(GLLightingFunc.GL_LIGHTING);
					gl.glDisable(GL.GL_DEPTH_TEST);
				}
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
				gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexBuffer.getBuffer()
						.capacity() / 4);
				gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
				if (wireframe) {
					gl.glColor4f(0, 0, 0, 0.5f);
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
					gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexBuffer
							.getBuffer().capacity() / 4);
				}

				gl.glEnable(GL.GL_DEPTH_TEST);
			}
			gl.glDisable(GLLightingFunc.GL_LIGHTING);
			gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
			if (showNormals) {
				gl.glLineWidth(2.0f);
				gl.glColor4f(0, 0.8f, 0, 0.5f);
				gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
				gl.glVertexPointer(4, GL.GL_FLOAT, 0,
						normalSegmentBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_LINES, 0, normalSegmentBuffer.getBuffer()
						.capacity() / 4);

			}

			if (showVertexes) {

				if (showColors) {

					gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
					gl.glColorPointer(4, GL.GL_FLOAT, 0,
							colorBuffer.getBuffer());
				} else {
					gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
					gl.glColor4f(1.0f, 0.8f, 0f, 1.0f);
				}
				gl.glPointSize(6.0f);
				gl.glVertexPointer(4, GL.GL_FLOAT, 0, vertexBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, vertexBuffer.getBuffer()
						.capacity() / 4);
				if (showColors) {
					gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				}

			}

			gl.glEnable(GLLightingFunc.GL_LIGHTING);

			if (showParticles) {
				gl.glDisable(GLLightingFunc.GL_LIGHTING);
				gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				if (showColors) {
					gl.glColor4f(0f, 0f, 0f, 1.0f);
				} else {
					gl.glColor4f(0.6f, 0f, 0f, 1.0f);
				}
				gl.glPointSize(10.0f);
				gl.glVertexPointer(4, GL.GL_FLOAT, 0,
						particleBuffer.getBuffer());
				gl.glDrawArrays(GL.GL_POINTS, 0, particleBuffer.getBuffer()
						.capacity() / 4);
				gl.glEnable(GLLightingFunc.GL_LIGHTING);

			}
			gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
			gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			gl.glDisable(GL2ES1.GL_POINT_SMOOTH);
			((PGraphicsOpenGL2) applet.g).endGL();
			renderFPS = 1E9 / (System.nanoTime() - startTime);
			startTime = System.nanoTime();
		}
		if (renderFPS > 0 && showTextParam.getValue()) {
			textRenderer.beginRendering(applet.width, applet.height);
			textRenderer.draw(String.format("Grid Size: %d x %d x %d",
					commons.rows, commons.cols, commons.slices), 10,
					applet.height - 20);
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
		showColors = showColorsParam.getValue();
		showTriangles = visibleParam.getValue();
		showIsoSurface = showIsoSurfaceParam.getValue();
		diffuseColor = colorParam.getValue();
		wireframe = wireframeParam.getValue();
		transparency = transparencyParam.getFloat();
	}
}
