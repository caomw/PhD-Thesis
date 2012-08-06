package org.imagesci.robopaint.graphics;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.TimerTask;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.robopaint.GeometryViewDescription;
import org.imagesci.robopaint.ImageViewDescription;
import org.imagesci.robopaint.ObjectDescription;
import org.imagesci.springls.SpringlsConstants;

import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLImageFormat;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.MOGACRenderer3D;
import edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

public class RoboRenderer extends MOGACRenderer3D {
	public RoboRenderer(VisualizationProcessing3D applet, MOGAC3D simulator,
			int rasterWidth, int rasterHeight, int refreshRate) {
		super(applet, simulator, rasterWidth, rasterHeight, refreshRate);
		// TODO Auto-generated constructor stub
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
	}

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {
		if (isoSurfRender == null) {
			applet.strokeWeight(0);
			applet.fill(255, 255, 255);
			applet.rect(0, 0, applet.width, applet.height);
			return;
		}
		GL2 gl = (GL2) ((PGraphicsOpenGL2) applet.g).beginGL();
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[0]);

		if (dirty) {
			if (task != null) {
				task.cancel();
			}
			updateCamera();

			long startTime = System.nanoTime();
			compute(enableAntiAlias && !enableFastRendering);
			long endTime = System.nanoTime();
			renderFPS = 1E9 / (endTime - startTime);
			dirty = false;
			if (enableAntiAlias && enableFastRendering) {
				task = new TimerTask() {
					@Override
					public void run() {
						compute(enableAntiAlias);
						dirty = false;
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

		((PGraphicsOpenGL2) applet.g).endGL();

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
		if (refImageBuffer == null) {
			return;
		}
		synchronized (this) {
			System.nanoTime();
			int global_size = MOGAC3D.roundToWorkgroupPower(simulator.rows
					* simulator.cols * simulator.slices, WORKGROUP_SIZE);
			if (time >= 0) {
				if (simulator.imageLabelBuffer != null) {
					simulator.queue.putReadBuffer(simulator.imageLabelBuffer,
							true).putReadBuffer(simulator.distanceFieldBuffer,
							true);
					imageLabelBufferCopy.getBuffer()
							.put(simulator.imageLabelBuffer.getBuffer())
							.rewind();
					distanceFieldBufferCopy.getBuffer()
							.put(simulator.distanceFieldBuffer.getBuffer())
							.rewind();
					simulator.imageLabelBuffer.getBuffer().rewind();
					simulator.distanceFieldBuffer.getBuffer().rewind();
					queue.putWriteBuffer(imageLabelBufferCopy, true)
							.putWriteBuffer(distanceFieldBufferCopy, true);
				}
			}
			maskLabels.setArgs(imageLabelBufferCopy, volumeColorBuffer,
					gpuColorLUT).rewind();
			queue.put1DRangeKernel(maskLabels, 0, global_size, WORKGROUP_SIZE);
			copyLevelSetImage.putArgs(distanceFieldBufferCopy,
					imageLabelBufferCopy, gpuColorLUT, volumeColorBuffer)
					.rewind();
			queue.put1DRangeKernel(copyLevelSetImage, 0, global_size,
					WORKGROUP_SIZE);
			if (enableSmoothingParam != null && enableSmoothingParam.getValue()) {
				final int SMOOTH_ITERATIONS = 8;
				float smoothing = 0.8f;
				bilateralFilter.putArgs(volumeColorBuffer).putArg(smoothing)
						.putArg(1.0f).rewind();
				for (int k = 0; k < SMOOTH_ITERATIONS; k++) {
					queue.put1DRangeKernel(bilateralFilter, 0, global_size,
							WORKGROUP_SIZE);
				}
			}
			queue.putReadBuffer(volumeColorBuffer, false);
			if (distanceFieldTexture == null) {
				CLImageFormat iformat = new CLImageFormat(
						CLImageFormat.ChannelOrder.RGBA,
						CLImageFormat.ChannelType.FLOAT);
				distanceFieldTexture = context.createImage3d(
						volumeColorBuffer.getBuffer(), simulator.rows,
						simulator.cols, simulator.slices, iformat, READ_WRITE,
						CLMemory.Mem.COPY_BUFFER);
			}
			queue.putWriteImage(distanceFieldTexture, false);
			System.nanoTime();
		}
		dirty = true;
	}

	@Override
	public void init() {
		try {
			if (simulator.queue.getDevice().getType() == CLDevice.Type.GPU) {
				context = simulator.context;
				queue = simulator.queue;
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
				System.out.println("Volume renderer using device: "
						+ device.getVendor() + " " + device.getVersion() + " "
						+ device.getName());
				context = CLContext.create(device);
				queue = device.createCommandQueue();

			}
			config = RenderingConfig.create().setWidth(rasterWidth)
					.setHeight(rasterHeight).setEnableShadow(1)
					.setSuperSamplingSize(2).setActvateFastRendering(1)
					.setMaxIterations(4 * simulator.rows).setEpsilon(0.1f)
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
			int bufferSize = config.getWidth() * config.getHeight() * 3;
			pixelBuffer = context.createFloatBuffer(bufferSize, READ_WRITE,
					USE_BUFFER);
			this.showXplane = true;
			this.showYplane = true;
			this.showZplane = true;
			this.showIsoSurf = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected CLKernel render(int fastRender, float sampleX, float sampleY) {

		if (isoSurfRender != null && distanceFieldTexture != null) {
			isoSurfRender.putArg(pixelBuffer).putArg(configBuffer)
					.putArg(refImageBuffer).putArg(distanceFieldTexture)
					.putArg(imageLabelBufferCopy).putArg(modelViewMatrixBuffer)
					.putArg(modelViewInverseMatrixBuffer).putArg(gpuColorLUT)
					.putArg(fastRender).putArg(sampleX).putArg(sampleY)
					.putArg(row).putArg(col).putArg(slice)
					.putArg(showXplane ? 1 : 0).putArg(showYplane ? 1 : 0)
					.putArg(showZplane ? 1 : 0).putArg((showIsoSurf) ? 1 : 0)
					.putArg(minImageValue).putArg(maxImageValue)
					.putArg(brightness).putArg(contrast).putArg(transparency)
					.rewind();
			return isoSurfRender;
		} else {
			return null;
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
		refresh();
	}

	public void updateImageSegmentation() {
		if (gpuColorLUT != null) {
			gpuColorLUT.release();
		}

		enableFastRendering = true;
		gpuColorLUT = context.createFloatBuffer(4 * simulator.getNumColors(),
				READ_WRITE);
		colors = new Color4f[simulator.getNumColors()];
		showIsoSurf = true;
		transparency = 0.5f;
		// task.cancel();
		dirty = true;

		int[] masks = simulator.getLabelMasks();
		contourColorsParam = new ParamColor[masks.length - 1];
		contoursVisibleParam = new ParamBoolean[masks.length - 1];

		final long seed = 5437897311l;
		Random randn = new Random(seed);
		GeometryViewDescription.getInstance().removeAllObjectDescriptions();
		for (int i = 0; i < contourColorsParam.length; i++) {
			Color c = new Color(randn.nextFloat(), randn.nextFloat(),
					randn.nextFloat());
			ObjectDescription label = new ObjectDescription("Label " + (i + 1),
					masks[i + 1]);
			label.setColor(c.getRed(), c.getGreen(), c.getBlue(), 255);
			contourColorsParam[i] = new ParamColor("Object Color ["
					+ masks[i + 1] + "]", c);
			contoursVisibleParam[i] = new ParamBoolean("Visibility ["
					+ masks[i + 1] + "]", true);
			GeometryViewDescription.getInstance().addObjectDescription(label);
		}
		frameUpdate(0, -1);
		updateColors();

	}

	public void updateReferenceImage() {
		ImageData refImage = simulator.getReferenceImage();
		if (refImage != null) {
			if (refImageBuffer != null) {
				refImageBuffer.release();
			}
			this.row = simulator.rows / 2;
			this.col = simulator.cols / 2;
			this.slice = simulator.slices / 2;

			FloatBuffer buff = Buffers.newDirectFloatBuffer(simulator.rows
					* simulator.cols * simulator.slices);
			CLImageFormat iformat = new CLImageFormat(
					CLImageFormat.ChannelOrder.INTENSITY,
					CLImageFormat.ChannelType.FLOAT);
			refImageBuffer = context.createImage3d(buff, simulator.rows,
					simulator.cols, simulator.slices, iformat, READ_ONLY,
					CLMemory.Mem.COPY_BUFFER);
			minImageValue = 0;
			maxImageValue = 0;
			for (int k = 0; k < simulator.slices; k++) {
				for (int j = 0; j < simulator.cols; j++) {
					for (int i = 0; i < simulator.rows; i++) {
						float val = refImage.getFloat(i, j, k);
						minImageValue = Math.min(val, minImageValue);
						maxImageValue = Math.max(val, maxImageValue);
						buff.put(val);
					}
				}
			}
			buff.rewind();
			queue.putWriteImage(refImageBuffer, true);
			if (distanceFieldBufferCopy != null) {
				distanceFieldBufferCopy.release();
			}
			if (imageLabelBufferCopy != null) {
				imageLabelBufferCopy.release();
			}
			if (volumeColorBuffer != null) {
				volumeColorBuffer.release();
			}
			if (gpuColorLUT != null) {
				gpuColorLUT.release();
			}
			gpuColorLUT = context.createFloatBuffer(
					4 * Math.max(1, simulator.getNumColors()), READ_WRITE);
			volumeColorBuffer = context.createFloatBuffer(simulator.rows
					* simulator.cols * simulator.slices * 4, READ_WRITE,
					USE_BUFFER);
			distanceFieldBufferCopy = context
					.createFloatBuffer(simulator.rows * simulator.cols
							* simulator.slices, READ_WRITE, USE_BUFFER);
			imageLabelBufferCopy = context
					.createIntBuffer(simulator.rows * simulator.cols
							* simulator.slices, READ_WRITE, USE_BUFFER);
			buff = distanceFieldBufferCopy.getBuffer();
			IntBuffer ibuff = imageLabelBufferCopy.getBuffer();
			for (int k = 0; k < simulator.slices; k++) {
				for (int j = 0; j < simulator.cols; j++) {
					for (int i = 0; i < simulator.rows; i++) {
						buff.put(10);
						ibuff.put(0);
					}
				}
			}
			buff.rewind();
			ibuff.rewind();
			queue.putWriteBuffer(distanceFieldBufferCopy, true);
			queue.putWriteBuffer(imageLabelBufferCopy, true);
			CLProgram program;
			if (isoSurfRender == null) {
				try {
					program = context.createProgram(
							getClass().getResourceAsStream(
									"MogacSurfaceRenderer.cl"))
							.build(define("ROWS", simulator.rows),
									define("COLS", simulator.cols),
									define("SLICES", simulator.slices),
									define("NUM_OBJECTS",
											simulator.getNumObjects()),
									define("SCALE_UP",
											SpringlsConstants.scaleUp + "f"),
									define("SCALE_DOWN",
											SpringlsConstants.scaleDown + "f"),
									define("ATI", 0),
									define("GPU", 1),
									define("CONTAINS_OVERLAPS", (simulator
											.containsOverlaps() ? 1 : 0)),
									ENABLE_MAD);
					bilateralFilter = program
							.createCLKernel("bilateralFilterVolume");
					copyLevelSetImage = program
							.createCLKernel("copyLevelSetImage");
					multiply = program.createCLKernel("multiply");
					setDistance = program.createCLKernel("setDistance");
					maskLabels = program.createCLKernel("maskLabels");
					isoSurfRender = program.createCLKernel("RoboRender");
					copyNarrowBandToImage = program
							.createCLKernel("copyNarrowBandToImage");
					clearNarrowBandToImage = program
							.createCLKernel("clearNarrowBandToImage");
					int bufferSize = config.getWidth() * config.getHeight() * 3;
					multiply.putArg(pixelBuffer).putArg(bufferSize).rewind();
					config.setMaxIterations(4 * simulator.rows);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if (refImageBuffer != null) {
				refImageBuffer.release();
				refImageBuffer = null;
			}
		}
		enableFastRendering = true;
		// task.cancel();
		dirty = true;
		frameUpdate(0, -1);

	}

	/**
	 * Update visualization parameters.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		row = ImageViewDescription.getInstance().getRow() - 1;
		col = ImageViewDescription.getInstance().getCol() - 1;
		slice = ImageViewDescription.getInstance().getSlice() - 1;
		contrast = ImageViewDescription.getInstance().getContrast();
		brightness = ImageViewDescription.getInstance().getBrightness();
		transparency = ImageViewDescription.getInstance().getTransparency();
		showXplane = ImageViewDescription.getInstance().isShowRow();
		showYplane = ImageViewDescription.getInstance().isShowColumn();
		showZplane = ImageViewDescription.getInstance().isShowSlice();
		showIsoSurf = !GeometryViewDescription.getInstance().isSliceView();
		int index = 0;
		for (ObjectDescription obj : GeometryViewDescription.getInstance()
				.getObjectDescriptions()) {
			contourColorsParam[index].setValue(obj.getColor());
			contoursVisibleParam[index].setValue(obj.isVisible());
			index++;
		}
		updateColors(true);
		frameUpdate(0, 0);
		refresh();
	}
}
