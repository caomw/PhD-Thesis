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

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;
import static java.lang.Math.sqrt;


import java.awt.Color;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.j3d.BoundingBox;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.imagesci.springls.Springl3D;
import org.imagesci.springls.SpringlsCommon3D;
import org.imagesci.springls.SpringlsConstants;
import org.imagesci.springls.ActiveContour3D.FrameUpdateListener;

import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.common.nio.Buffers;
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
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFloat;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamInteger;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamIntegerSliderInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsRaycastRenderer.
 */
public class SpringlsRaycastRenderer extends RendererProcessing3D implements
		FrameUpdateListener {
	/** The Constant WORKGROUP_SIZE. */
	protected static final int WORKGROUP_SIZE = 64;

	/** The active list buffer copy. */
	protected CLBuffer<IntBuffer> activeListBufferCopy;

	/** The applet. */
	protected VisualizationProcessing3D applet = null;

	/** The bg color. */
	protected Color bgColor = Color.WHITE;

	/** The brightness. */
	protected float brightness = 0;

	/** The brightness param. */
	protected ParamFloat brightnessParam;

	/** The capsule buffer copy. */
	protected CLBuffer<ByteBuffer> capsuleBufferCopy;

	/** The col. */
	protected float col = 0;

	/** The color. */
	protected Color4f color = new Color4f(0, 0.4f, 0.8f, 1.0f);

	/** The color param. */
	protected ParamColor colorParam;

	/** The column param. */
	protected ParamInteger colParam;
	/** The commons. */
	protected SpringlsCommon3D commons;
	/** The compute fps. */
	protected double computeFPS = -1;
	/** The config. */
	protected RenderingConfig config;

	/** The config buffer. */
	protected CLBuffer<ByteBuffer> configBuffer;
	/** The context. */
	protected CLContext context;

	/** The contrast. */
	protected float contrast = 1;

	/** The contrast param. */
	protected ParamFloat contrastParam;

	/** The copy level set. */
	protected CLKernel copyLevelSet;

	/** The CUB e_ dim. */
	protected final int CUBE_DIM = 8;

	/** The dirty. */
	protected boolean dirty = false;

	/** The enable anti alias. */
	protected boolean enableAntiAlias = true;

	/** The enable fast rendering. */
	protected boolean enableFastRendering = true;

	/** The enable shadows. */
	protected boolean enableShadows = false;

	/** The index buffer copy. */
	protected CLBuffer<IntBuffer> indexBufferCopy;
	/** The key buffer copy. */
	protected CLBuffer<IntBuffer> keyBufferCopy;

	/** The label buffer copy. */
	protected CLBuffer<IntBuffer> labelBufferCopy;

	/** The last active list array size. */
	protected int lastActiveListArraySize = -1;

	/** The last active list size. */
	protected int lastActiveListSize = -1;

	/** The last array length. */
	protected int lastArrayLength = -1;
	/** The last element count. */
	protected int lastElementCount = -1;

	/** The max image value. */
	protected float minImageValue, maxImageValue;
	/** The model view. */
	protected Matrix4f modelView;
	/** The model view inverse matrix buffer. */
	protected CLBuffer<FloatBuffer> modelViewInverseMatrixBuffer;
	/** The model view matrix buffer. */
	protected CLBuffer<FloatBuffer> modelViewMatrixBuffer;

	/** The copy level set image. */
	protected CLKernel multiply, computeCapsuleColor, copyLevelSetImage,
			bilateralFilter;

	/** The pixel buffer. */
	protected CLBuffer<FloatBuffer> pixelBuffer;

	/** The program. */
	protected CLProgram program;

	/** The queue. */
	protected CLCommandQueue queue;

	/** The reduce normals. */
	protected CLKernel reduceNormals;

	/** The ref image buffer. */
	protected CLImage3d<FloatBuffer> refImageBuffer;

	/** The refresh rate. */
	protected int refreshRate = 1;

	/** The render fps. */
	protected double renderFPS = -1;

	/** The row. */
	protected float row = 0;
	/** The col param. */
	protected ParamInteger rowParam;

	/** The show iso surf. */
	protected boolean showIsoSurf = true;

	/** The show iso surf param. */
	protected ParamBoolean showIsoSurfParam;
	/** The show triangles. */
	protected boolean showSpringls = false;
	/** The show text param. */
	protected ParamBoolean showTrianglesParam, enableAntiAliasParam,
			enableShadowsParam, showTextParam, enableSmoothingParam;

	/** The show vertex tracking. */
	protected boolean showVertexTracking = false;

	/** The show vertex tracking param. */
	protected ParamBoolean showVertexTrackingParam;
	/** The show xplane. */
	protected boolean showXplane = false;

	/** The show xplane param. */
	protected ParamBoolean showXplaneParam;

	/** The show yplane. */
	protected boolean showYplane = false;

	/** The show yplane param. */
	protected ParamBoolean showYplaneParam;

	/** The show zplane. */
	protected boolean showZplane = false;

	/** The show zplane param. */
	protected ParamBoolean showZplaneParam;

	/** The signed level set buffer copy. */
	protected CLBuffer<FloatBuffer> signedLevelSetBufferCopy;

	/** The slice. */
	protected float slice = 0;

	/** The slice param. */
	protected ParamInteger sliceParam;

	/** The spatial look up copy. */
	protected CLBuffer<IntBuffer> spatialLookUpCopy;

	/** The task. */
	protected TimerTask task = null;

	/** The tex buffer. */
	protected IntBuffer texBuffer = IntBuffer.allocate(1);

	/** The text renderer. */
	protected TextRenderer textRenderer;

	/** The timer. */
	protected Timer timer;

	/** The time step. */
	protected long timeStep = 0;

	/** The transparency param. */
	protected ParamFloat transparencyParam;
	/** The updating frame. */
	protected boolean updatingFrame;

	/** The use springl normals. */
	protected ParamBoolean useSpringlNormals;

	/** The value buffer copy. */
	protected CLBuffer<IntBuffer> valueBufferCopy;

	/** The color buffer. */
	protected CLBuffer<FloatBuffer> vertexBuffer, colorBuffer;

	/** The volume color buffer. */
	protected CLBuffer<FloatBuffer> volumeColorBuffer;

	/** The color texture. */
	protected CLImage3d<FloatBuffer> volumeTexture, colorTexture;

	/** The volume tmp color buffer. */
	protected CLBuffer<FloatBuffer> volumeTmpColorBuffer;

	/**
	 * Instantiates a new springls raycast renderer.
	 * 
	 * @param applet
	 *            the applet
	 * @param commons
	 *            the commons
	 * @param rasterWidth
	 *            the raster width
	 * @param rasterHeight
	 *            the raster height
	 * @param refreshRate
	 *            the refresh rate
	 */
	public SpringlsRaycastRenderer(VisualizationProcessing3D applet,
			SpringlsCommon3D commons, int rasterWidth, int rasterHeight,
			int refreshRate) {
		super();
		this.applet = applet;
		this.commons = commons;
		this.refreshRate = refreshRate;
		this.bbox = new BoundingBox(new Point3d(0, 0, 0), new Point3d(
				0.5 * commons.rows, 0.5 * commons.cols, 0.5 * commons.slices));
		timer = new Timer();
		try {
			if (commons.getType() == CLDevice.Type.GPU) {
				context = commons.context;
				queue = commons.queue;

			} else {
				CLPlatform[] platforms = CLPlatform.listCLPlatforms();
				CLDevice device = null;
				for (CLPlatform p : platforms) {
					device = p.getMaxFlopsDevice(CLDevice.Type.GPU);
					if (device != null) {
						break;
					}
				}
				if (device == null) {
					device = CLPlatform.getDefault().getMaxFlopsDevice();
				}
				System.out.println("Springls renderer using device: "
						+ device.getVendor() + " " + device.getVersion() + " "
						+ device.getName());
				context = CLContext.create(device);
				queue = device.createCommandQueue();
			}

			program = context
					.createProgram(
							getClass().getResourceAsStream(
									"SpringlsSurfaceRenderer.cl"))
					.build(define("ROWS", commons.rows),
							define("COLS", commons.cols),
							define("SLICES", commons.slices),
							define("MAX_BIN_SIZE",
									SpringlsCommon3D.MAX_BIN_SIZE),
							define("CUBE_DIM", CUBE_DIM),
							define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
							define("SCALE_DOWN", SpringlsConstants.scaleDown
									+ "f"), define("ATI", 0), define("GPU", 1),
							ENABLE_MAD);

			config = RenderingConfig
					.create()
					.setWidth(rasterWidth)
					.setHeight(rasterHeight)
					.setEnableShadow(1)
					.setSuperSamplingSize(2)
					.setActvateFastRendering(1)
					.setMaxIterations(
							4 * Math.max(Math.max(commons.rows, commons.cols),
									commons.slices)).setEpsilon(0.1f)
					.setLight(new float[] { 5, 10, 15 })
					.setBackgroundColor(new float[] { 0.8f, 0.2f, 0.1f, 1.0f });
			Point3f cameraCenter = new Point3f(0, 1, 4.0f);
			Point3f modelCenter = new Point3f(0, 0, 0);
			config.getCamera().getOrig().setX(cameraCenter.x)
					.setY(cameraCenter.y).setZ(cameraCenter.z);
			config.getCamera().getTarget().setX(modelCenter.x)
					.setY(modelCenter.y).setZ(modelCenter.z);
			modelView = new Matrix4f();
			modelView.setIdentity();
			modelViewMatrixBuffer = context.createFloatBuffer(16, READ_ONLY);
			modelViewInverseMatrixBuffer = context.createFloatBuffer(16,
					READ_ONLY);
			configBuffer = context.createBuffer(config.getBuffer(), READ_ONLY);
			CLImageFormat iformat = new CLImageFormat(
					CLImageFormat.ChannelOrder.RGBA,
					CLImageFormat.ChannelType.FLOAT);
			volumeColorBuffer = context
					.createFloatBuffer(commons.rows * commons.cols
							* commons.slices * 4, READ_WRITE, USE_BUFFER);
			signedLevelSetBufferCopy = context.createFloatBuffer(commons.rows
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
			colorTexture = context.createImage3d(buff, CUBE_DIM, CUBE_DIM,
					CUBE_DIM, iformat, READ_WRITE, CLMemory.Mem.COPY_BUFFER);
			int bufferSize = config.getWidth() * config.getHeight() * 3;
			pixelBuffer = context.createFloatBuffer(bufferSize, READ_WRITE,
					USE_BUFFER);
			colorBuffer = context.createFloatBuffer(4 * commons.elements,
					READ_ONLY, USE_BUFFER);
			reduceNormals = program.createCLKernel("reduceNormals");
			copyLevelSet = program.createCLKernel("copyLevelSet");
			copyLevelSetImage = commons.kernelMap.get("copyLevelSetImage");
			multiply = program.createCLKernel("multiply");
			bilateralFilter = program.createCLKernel("bilateralFilterVolume");
			computeCapsuleColor = program.createCLKernel("computeCapsuleColor");
			multiply.putArg(pixelBuffer).putArg(bufferSize).rewind();
			ImageData refImage = commons.getReferenceImage();
			if (refImage != null) {
				iformat = new CLImageFormat(
						CLImageFormat.ChannelOrder.INTENSITY,
						CLImageFormat.ChannelType.FLOAT);
				FloatBuffer buff2 = Buffers.newDirectFloatBuffer(commons.rows
						* commons.cols * commons.slices);

				minImageValue = 0;
				maxImageValue = 0;
				for (int k = 0; k < commons.slices; k++) {
					for (int j = 0; j < commons.cols; j++) {
						for (int i = 0; i < commons.rows; i++) {
							float val = refImage.getFloat(i, j, k);
							minImageValue = Math.min(val, minImageValue);
							maxImageValue = Math.max(val, maxImageValue);
							buff2.put(val);
						}
					}
				}

				buff2.rewind();
				refImageBuffer = context.createImage3d(buff2, commons.rows,
						commons.cols, commons.slices, iformat, READ_ONLY,
						CLMemory.Mem.COPY_BUFFER);
				queue.putWriteImage(refImageBuffer, true);
			}
			frameUpdate(0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new springls raycast renderer.
	 */
	public SpringlsRaycastRenderer() {

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
		if (model == showTrianglesParam) {
			showSpringls = showTrianglesParam.getValue();
		} else if (model == enableAntiAliasParam) {
			enableAntiAlias = enableAntiAliasParam.getValue();
			setFastRendering(!enableAntiAlias);
		} else if (model == enableShadowsParam) {
			enableShadows = enableShadowsParam.getValue();
		} else if (model == colorParam) {
			color = new Color4f(colorParam.getValue());
		} else if (model == enableSmoothingParam || model == useSpringlNormals) {
			frameUpdate(-1, -1);
		} else if (model == contrastParam) {
			contrast = contrastParam.getFloat();
		} else if (model == brightnessParam) {
			brightness = brightnessParam.getFloat();
		} else if (model == showXplaneParam) {
			showXplane = showXplaneParam.getValue();
		} else if (model == showYplaneParam) {
			showYplane = showYplaneParam.getValue();
		} else if (model == showZplaneParam) {
			showZplane = showZplaneParam.getValue();
		} else if (model == showIsoSurfParam) {
			showIsoSurf = showIsoSurfParam.getValue();
		} else if (model == showVertexTrackingParam) {
			showVertexTracking = showVertexTrackingParam.getValue();
		} else if (model == rowParam) {
			row = rowParam.getInt() - 1;
		} else if (model == colParam) {
			col = colParam.getInt() - 1;
		} else if (model == sliceParam) {
			slice = sliceParam.getInt() - 1;
		}
		refresh();
	}

	/**
	 * Update visualization parameters.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		showSpringls = showTrianglesParam.getValue();
		enableAntiAlias = enableAntiAliasParam.getValue();
		enableShadows = enableShadowsParam.getValue();
		color = new Color4f(colorParam.getValue());
		row = rowParam.getInt() - 1;
		col = colParam.getInt() - 1;
		slice = sliceParam.getInt() - 1;
		contrast = contrastParam.getFloat();
		brightness = brightnessParam.getFloat();
		showIsoSurf = showIsoSurfParam.getValue();
		showVertexTracking = showVertexTrackingParam.getValue();
		showXplane = showXplaneParam.getValue();
		showYplane = showYplaneParam.getValue();
		showZplane = showZplaneParam.getValue();
		setFastRendering(!enableAntiAliasParam.getValue());
		refresh();
		frameUpdate(-1, 0);
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.ActiveContour3D.FrameUpdateListener#frameUpdate(long, double)
	 */
	@Override
	public void frameUpdate(long time, double fps) {
		this.computeFPS = fps;
		if (time >= 0) {
			this.timeStep = time;
		}
		synchronized (this) {

			System.nanoTime();
			int global_size = SpringlsCommon3D.roundToWorkgroupPower(
					commons.rows * commons.cols * commons.slices,
					WORKGROUP_SIZE);

			commons.queue.putReadBuffer(commons.signedLevelSetBuffer, true);
			signedLevelSetBufferCopy.getBuffer()
					.put(commons.signedLevelSetBuffer.getBuffer()).rewind();
			commons.signedLevelSetBuffer.getBuffer().rewind();
			queue.putWriteBuffer(signedLevelSetBufferCopy, true);

			commons.queue.putReadBuffer(commons.capsuleBuffer, true);
			commons.queue.putReadBuffer(commons.indexBuffer, true);
			commons.queue.putReadBuffer((commons).spatialLookUp, true);
			commons.queue.putReadBuffer(commons.springlLabelBuffer, true);

			if (activeListBufferCopy == null
					|| lastActiveListArraySize != (commons).activeListArraySize) {
				if (activeListBufferCopy != null) {
					activeListBufferCopy.release();
					spatialLookUpCopy.release();
				}
				lastActiveListArraySize = (commons).activeListArraySize;
				lastActiveListSize = (commons).activeListSize;
				activeListBufferCopy = context.createIntBuffer(
						(commons).activeListArraySize, READ_WRITE);
				spatialLookUpCopy = context.createIntBuffer(
						SpringlsCommon3D.MAX_BIN_SIZE
								* (commons).activeListArraySize, READ_WRITE,
						USE_BUFFER);
			}

			if (capsuleBufferCopy == null
					|| lastArrayLength != commons.arrayLength) {
				if (labelBufferCopy != null) {
					labelBufferCopy.release();
				}
				if (capsuleBufferCopy != null) {
					capsuleBufferCopy.release();
				}
				if (colorBuffer != null) {
					colorBuffer.release();
				}
				lastArrayLength = commons.arrayLength;
				capsuleBufferCopy = context.createByteBuffer(
						Springl3D.BYTE_SIZE * commons.arrayLength, READ_WRITE,
						USE_BUFFER);
				labelBufferCopy = context.createIntBuffer(commons.arrayLength,
						READ_WRITE, USE_BUFFER);
				colorBuffer = context.createFloatBuffer(
						4 * commons.arrayLength, READ_WRITE, USE_BUFFER);
			}
			if (indexBufferCopy == null) {
				indexBufferCopy = context
						.createIntBuffer(commons.rows * commons.cols
								* commons.slices, READ_WRITE, USE_BUFFER);
			}
			labelBufferCopy.getBuffer()
					.put(commons.springlLabelBuffer.getBuffer()).rewind();
			commons.springlLabelBuffer.getBuffer().rewind();
			spatialLookUpCopy.getBuffer()
					.put((commons).spatialLookUp.getBuffer()).rewind();
			(commons).spatialLookUp.getBuffer().rewind();
			indexBufferCopy.getBuffer().put(commons.indexBuffer.getBuffer())
					.rewind();
			commons.indexBuffer.getBuffer().rewind();
			capsuleBufferCopy.getBuffer()
					.put(commons.capsuleBuffer.getBuffer()).rewind();
			commons.capsuleBuffer.getBuffer().rewind();
			queue.putWriteBuffer(capsuleBufferCopy, true)
					.putWriteBuffer(indexBufferCopy, true)
					.putWriteBuffer(spatialLookUpCopy, true)
					.putWriteBuffer(labelBufferCopy, true);

			copyLevelSet.putArgs(volumeColorBuffer, signedLevelSetBufferCopy)
					.rewind();
			queue.put1DRangeKernel(copyLevelSet, 0, global_size, WORKGROUP_SIZE);

			commons.queue.putReadBuffer((commons).activeListBuffer, true);
			activeListBufferCopy.getBuffer()
					.put((commons).activeListBuffer.getBuffer()).rewind();
			(commons).activeListBuffer.getBuffer().rewind();
			queue.putWriteBuffer(activeListBufferCopy, true);

			if (useSpringlNormals == null || useSpringlNormals.getValue()) {
				int narrowband_global_size = SpringlsCommon3D
						.roundToWorkgroupPower((commons).activeListSize,
								WORKGROUP_SIZE);
				reduceNormals
						.putArgs(activeListBufferCopy, spatialLookUpCopy,
								capsuleBufferCopy, volumeColorBuffer)
						.putArg((commons).activeListSize).rewind();
				queue.put1DRangeKernel(reduceNormals, 0,
						narrowband_global_size, WORKGROUP_SIZE);
			}
			if (enableSmoothingParam == null || enableSmoothingParam.getValue()) {
				final int SMOOTH_ITERATIONS = 8;
				float smoothing = 0.8f;
				bilateralFilter.putArgs(volumeColorBuffer).putArg(smoothing)
						.putArg(1.0f).rewind();
				for (int k = 0; k < SMOOTH_ITERATIONS; k++) {
					queue.put1DRangeKernel(bilateralFilter, 0, global_size,
							WORKGROUP_SIZE);
				}
			}
			computeCapsuleColor
					.putArgs(capsuleBufferCopy, labelBufferCopy, colorBuffer,
							colorTexture).putArg(commons.elements).rewind();
			queue.put1DRangeKernel(computeCapsuleColor, 0, commons.arrayLength,
					WORKGROUP_SIZE);
			queue.putReadBuffer(volumeColorBuffer, true);
			if (volumeTexture == null) {
				CLImageFormat iformat = new CLImageFormat(
						CLImageFormat.ChannelOrder.RGBA,
						CLImageFormat.ChannelType.FLOAT);
				volumeTexture = context.createImage3d(
						volumeColorBuffer.getBuffer(), commons.rows,
						commons.cols, commons.slices, iformat, READ_ONLY,
						CLMemory.Mem.COPY_BUFFER);
			}

			queue.putWriteImage(volumeTexture, false);
			lastElementCount = commons.elements;
			System.nanoTime();
		}

		dirty = true;
	}

	/**
	 * Sets the fast rendering.
	 * 
	 * @param render
	 *            the new fast rendering
	 */
	public void setFastRendering(boolean render) {
		this.enableFastRendering = render;
	}

	/**
	 * Refresh.
	 */
	public void refresh() {
		dirty = true;
	}

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {
		GL2 gl = (GL2) ((PGraphicsOpenGL2) applet.g).beginGL();
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[0]);

		if (dirty) {
			if (task != null) {
				task.cancel();
			}
			updateCamera();
			synchronized (this) {

				long startTime = System.nanoTime();
				compute(enableAntiAlias && !enableFastRendering);
				long endTime = System.nanoTime();
				renderFPS = 1E9 / (endTime - startTime);
			}
			if (enableAntiAlias && enableFastRendering) {
				task = new TimerTask() {
					@Override
					public void run() {
						synchronized (this) {
							compute(enableAntiAlias);
						}
					}
				};
				timer.schedule(task, 1000);
			}
		}
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F,
				config.getWidth(), config.getHeight(), 0, GL.GL_RGB,
				GL.GL_FLOAT, pixelBuffer.getBuffer());

		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2f(0, 0);

		gl.glTexCoord2f(1, 1);
		gl.glVertex2f(applet.width, 0);

		gl.glTexCoord2f(1, 0);
		gl.glVertex2f(applet.width, applet.height);

		gl.glTexCoord2f(0, 0);
		gl.glVertex2f(0, applet.height);

		gl.glEnd();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_TEXTURE_2D);

		if (renderFPS > 0 && showTextParam.getValue()) {
			textRenderer.beginRendering(applet.width, applet.height);
			textRenderer.draw(String.format("Grid Size: %d x %d x %d",
					commons.rows, commons.cols, commons.slices), 10,
					applet.height - 20);
			textRenderer.draw(String.format("Springls: %d", commons.elements),
					10, applet.height - 40);
			if (timeStep >= 0) {
				textRenderer.draw(String.format("Iteration: %d", timeStep), 10,
						applet.height - 60);
			}
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
		((PGraphicsOpenGL2) applet.g).endGL();

	}

	/**
	 * Update camera.
	 */
	private synchronized void updateCamera() {
		Camera camera = config.getCamera();
		Vec dir = camera.getDir();
		Vec target = camera.getTarget();
		Vec camX = camera.getX();
		Vec camY = camera.getY();
		Vec orig = camera.getOrig();

		vsub(dir, target, orig);
		vnorm(dir);

		Vec up = Vec.create().setX(0).setY(1).setZ(0);
		vxcross(camX, dir, up);
		vnorm(camX);
		vmul(camX, config.getWidth() * .5135f / config.getHeight(), camX);

		vxcross(camY, camX, dir);
		vnorm(camY);
		vmul(camY, .5135f, camY);
		config.setEnableShadow(enableShadows ? 1 : 0);
		config.setActvateFastRendering(enableAntiAlias ? 1 : 0);
		queue.putWriteBuffer(configBuffer, true);
		FloatBuffer buff = modelViewMatrixBuffer.getBuffer();
		buff.rewind();
		Matrix4f tmp = new Matrix4f(modelView);
		buff.put(tmp.m00);
		buff.put(tmp.m01);
		buff.put(tmp.m02);
		buff.put(tmp.m03);
		buff.put(tmp.m10);
		buff.put(tmp.m11);
		buff.put(tmp.m12);
		buff.put(tmp.m13);
		buff.put(tmp.m20);
		buff.put(tmp.m21);
		buff.put(tmp.m22);
		buff.put(tmp.m23);
		buff.put(tmp.m30);
		buff.put(tmp.m31);
		buff.put(tmp.m32);
		buff.put(tmp.m33);
		buff.rewind();
		buff = modelViewInverseMatrixBuffer.getBuffer();
		tmp.invert();
		buff.put(tmp.m00);
		buff.put(tmp.m01);
		buff.put(tmp.m02);
		buff.put(tmp.m03);
		buff.put(tmp.m10);
		buff.put(tmp.m11);
		buff.put(tmp.m12);
		buff.put(tmp.m13);
		buff.put(tmp.m20);
		buff.put(tmp.m21);
		buff.put(tmp.m22);
		buff.put(tmp.m23);
		buff.put(tmp.m30);
		buff.put(tmp.m31);
		buff.put(tmp.m32);
		buff.put(tmp.m33);
		buff.rewind();
		queue.putWriteBuffer(modelViewMatrixBuffer, true);
		queue.putWriteBuffer(modelViewInverseMatrixBuffer, true);
	}

	/**
	 * Vsub.
	 * 
	 * @param v
	 *            the v
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public final static void vsub(Vec v, Vec a, Vec b) {
		v.setX(a.getX() - b.getX());
		v.setY(a.getY() - b.getY());
		v.setZ(a.getZ() - b.getZ());
	}

	/**
	 * Vnorm.
	 * 
	 * @param v
	 *            the v
	 */
	public final static void vnorm(Vec v) {
		float s = (float) (1.0f / sqrt(vdot(v, v)));
		vmul(v, s, v);
	}

	/**
	 * Vdot.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the float
	 */
	public final static float vdot(Vec a, Vec b) {
		return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
	}

	/**
	 * Vmul.
	 * 
	 * @param v
	 *            the v
	 * @param s
	 *            the s
	 * @param b
	 *            the b
	 */
	public final static void vmul(Vec v, float s, Vec b) {
		v.setX(s * b.getX());
		v.setY(s * b.getY());
		v.setZ(s * b.getZ());
	}

	/**
	 * Vxcross.
	 * 
	 * @param v
	 *            the v
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public final static void vxcross(Vec v, Vec a, Vec b) {
		v.setX(a.getY() * b.getZ() - a.getZ() * b.getY());
		v.setY(a.getZ() * b.getX() - a.getX() * b.getZ());
		v.setZ(a.getX() * b.getY() - a.getY() * b.getX());
	}

	/**
	 * Compute.
	 * 
	 * @param antiAlias
	 *            the anti alias
	 */
	public void compute(boolean antiAlias) {
		synchronized (this) {
			int globalThreads = config.getWidth() * config.getHeight();
			if (globalThreads % WORKGROUP_SIZE != 0) {
				globalThreads = (globalThreads / WORKGROUP_SIZE + 1)
						* WORKGROUP_SIZE;
			}
			int localThreads = WORKGROUP_SIZE;
			int superSamplingSize = config.getSuperSamplingSize();
			if (antiAlias && superSamplingSize > 1) {
				for (int y = 0; y < superSamplingSize; ++y) {
					for (int x = 0; x < superSamplingSize; ++x) {

						float sampleX = (x + 0.5f) / superSamplingSize;
						float sampleY = (y + 0.5f) / superSamplingSize;

						if (x == 0 && y == 0) {
							queue.put1DRangeKernel(render(0, 0, 0), 0,
									globalThreads, localThreads);
						} else if (x == (superSamplingSize - 1)
								&& y == (superSamplingSize - 1)) {
							// normalize the values we accumulated
							multiply.setArg(
									2,
									1.0f / (superSamplingSize * superSamplingSize));
							queue.put1DRangeKernel(render(1, sampleX, sampleY),
									0, globalThreads, localThreads);
							queue.put1DRangeKernel(multiply, 0,
									globalThreads * 3, localThreads);
						} else {
							queue.put1DRangeKernel(render(1, sampleX, sampleY),
									0, globalThreads, localThreads);
						}
					}
				}

				queue.putBarrier().putReadBuffer(pixelBuffer, true);
			} else {
				queue.put1DRangeKernel(render(0, 0, 0), 0, globalThreads,
						localThreads);
				queue.putBarrier().putReadBuffer(pixelBuffer, true);
			}
		}
		dirty = false;
	}

	/**
	 * Render.
	 * 
	 * @param fastRender
	 *            the fast render
	 * @param sampleX
	 *            the sample x
	 * @param sampleY
	 *            the sample y
	 * @return the cL kernel
	 */
	protected CLKernel render(int fastRender, float sampleX, float sampleY) {

		final CLKernel isoSurfRender = program.createCLKernel("IsoSurfRender");
		final CLKernel springlsRender = program
				.createCLKernel("NBSpringlsRender");
		if (showSpringls) {
			springlsRender
					.putArgs(pixelBuffer, refImageBuffer, volumeTexture,
							capsuleBufferCopy, colorBuffer, indexBufferCopy,
							spatialLookUpCopy, configBuffer,
							modelViewMatrixBuffer, modelViewInverseMatrixBuffer)
					.putArg(commons.mapLength).putArg(commons.elements)
					.putArg(fastRender).putArg(sampleX).putArg(sampleY)
					.putArg(color.x).putArg(color.y).putArg(color.z)
					.putArg(row).putArg(col).putArg(slice)
					.putArg(showXplane ? 1 : 0).putArg(showYplane ? 1 : 0)
					.putArg(showZplane ? 1 : 0).putArg(showIsoSurf ? 1 : 0)
					.putArg(showVertexTracking ? 1 : 0).putArg(minImageValue)
					.putArg(maxImageValue).putArg(brightness).putArg(contrast)
					.putArg(transparencyParam.getFloat()).rewind();
			return springlsRender;
		} else {
			isoSurfRender
					.putArg(pixelBuffer)
					.putArgs(refImageBuffer, volumeTexture, colorBuffer,
							indexBufferCopy, spatialLookUpCopy,
							capsuleBufferCopy, configBuffer,
							modelViewMatrixBuffer, modelViewInverseMatrixBuffer)
					.putArg(fastRender).putArg(sampleX).putArg(sampleY)
					.putArg(color.x).putArg(color.y).putArg(color.z)
					.putArg(row).putArg(col).putArg(slice)
					.putArg(showXplane ? 1 : 0).putArg(showYplane ? 1 : 0)
					.putArg(showZplane ? 1 : 0).putArg(showIsoSurf ? 1 : 0)
					.putArg(showVertexTracking ? 1 : 0).putArg(minImageValue)
					.putArg(maxImageValue).putArg(brightness).putArg(contrast)
					.putArg(transparencyParam.getFloat()).rewind();

			return isoSurfRender;
		}
	}

	/**
	 * Vadd.
	 * 
	 * @param v
	 *            the v
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public final static void vadd(Vec v, Vec a, Vec b) {
		v.setX(a.getX() + b.getX());
		v.setY(a.getY() + b.getY());
		v.setZ(a.getZ() + b.getZ());
	}

	/**
	 * Apply transform.
	 * 
	 * @param rotx
	 *            the rotx
	 * @param roty
	 *            the roty
	 * @param rotz
	 *            the rotz
	 * @param tx
	 *            the tx
	 * @param ty
	 *            the ty
	 * @param tz
	 *            the tz
	 */
	public void applyTransform(float rotx, float roty, float rotz, float tx,
			float ty, float tz) {
		Matrix4f R = new Matrix4f();
		Matrix4f T = new Matrix4f();
		T.setIdentity();
		T.setTranslation(new Vector3f(tx, ty, tz));
		R.rotX(rotx);
		modelView.mul(R);
		R.rotY(roty);
		modelView.mul(R);
		R.rotZ(rotz);
		modelView.mul(R);
		modelView.mul(T);
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
		visualizationParameters.setName("Springls Raycast");
		visualizationParameters.add(colorParam = new ParamColor("Color",
				new Color(color.x, color.y, color.z)));
		visualizationParameters.add(rowParam = new ParamInteger("Row", 1,
				commons.rows, commons.rows / 2));
		rowParam.setInputView(new ParamIntegerSliderInputView(rowParam, 4));

		visualizationParameters.add(colParam = new ParamInteger("Column", 1,
				commons.cols, commons.cols / 2));
		colParam.setInputView(new ParamIntegerSliderInputView(colParam, 4));

		visualizationParameters.add(sliceParam = new ParamInteger("Slice", 1,
				commons.slices, commons.slices / 2));
		sliceParam.setInputView(new ParamIntegerSliderInputView(sliceParam, 4));

		visualizationParameters.add(contrastParam = new ParamFloat("Contrast",
				-5, 5, contrast));
		contrastParam.setInputView(new ParamDoubleSliderInputView(
				contrastParam, 4, false));
		visualizationParameters.add(brightnessParam = new ParamFloat(
				"Brightness", -5, 5, brightness));
		visualizationParameters.add(transparencyParam = new ParamFloat(
				"Iso-Contour Transparency", 0, 1, 0.5f));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		brightnessParam.setInputView(new ParamDoubleSliderInputView(
				brightnessParam, 4, false));

		visualizationParameters.add(showIsoSurfParam = new ParamBoolean(
				"Show Iso-Surface", showIsoSurf));
		visualizationParameters.add(showTrianglesParam = new ParamBoolean(
				"Show Springls", showSpringls));
		visualizationParameters.add(showVertexTrackingParam = new ParamBoolean(
				"Show Vertex Tracking", showVertexTracking));
		visualizationParameters.add(showXplaneParam = new ParamBoolean(
				"Show X Plane", showXplane));
		visualizationParameters.add(showYplaneParam = new ParamBoolean(
				"Show Y Plane", showYplane));
		visualizationParameters.add(showZplaneParam = new ParamBoolean(
				"Show Z Plane", showZplane));
		visualizationParameters.add(enableAntiAliasParam = new ParamBoolean(
				"Enable Anti-Aliasing", enableAntiAlias));
		visualizationParameters.add(enableSmoothingParam = new ParamBoolean(
				"Enable Smoothing", true));
		visualizationParameters.add(useSpringlNormals = new ParamBoolean(
				"Use Springl Normals", true));
		visualizationParameters.add(enableShadowsParam = new ParamBoolean(
				"Enable Shadows", enableShadows));
		visualizationParameters.add(showTextParam = new ParamBoolean(
				"Show Text", true));
	}

	/**
	 * Gets the model view.
	 * 
	 * @return the model view
	 */
	public Matrix4f getModelView() {
		return modelView;
	}

	/**
	 * Sets the background color.
	 * 
	 * @param c
	 *            the new background color
	 */
	public void setBackgroundColor(Color c) {
		float[] cArray = new float[4];
		bgColor = c;
		c.getComponents(cArray);
		config.setBackgroundColor(cArray);
	}

	/**
	 * Sets the camera center.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	public void setCameraCenter(float x, float y, float z) {
		config.getCamera().getOrig().setX(x);
		config.getCamera().getOrig().setY(y);
		config.getCamera().getOrig().setZ(z);
	}

	/**
	 * Sets the model view.
	 * 
	 * @param modelView
	 *            the new model view
	 */
	public void setModelView(Matrix4f modelView) {
		this.modelView.set(modelView);
	}

	/**
	 * Sets the target center.
	 * 
	 * @param center
	 *            the new target center
	 */
	public void setTargetCenter(Point3f center) {
		config.getCamera().getTarget().setX(center.x);
		config.getCamera().getTarget().setY(center.y);
		config.getCamera().getTarget().setZ(center.z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {
		applet.ortho();
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
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F,
				config.getWidth(), config.getHeight(), 0, GL.GL_RGB,
				GL.GL_FLOAT, pixelBuffer.getBuffer());
		gl.glDisable(GL.GL_TEXTURE_2D);
		((PGraphicsOpenGL2) applet.g).endGL();
		textRenderer = new TextRenderer(applet.getFont().deriveFont(Font.BOLD,
				14), true, true, null, false);
		textRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
	}
}
