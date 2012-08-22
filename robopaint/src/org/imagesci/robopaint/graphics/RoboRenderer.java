package org.imagesci.robopaint.graphics;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.TimerTask;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.imagesci.mogac.MOGAC3D;
import org.imagesci.mogac.WEMOGAC3D;
import org.imagesci.mogac.MACWE3D;
import org.imagesci.robopaint.GeometryViewDescription;
import org.imagesci.robopaint.ImageViewDescription;
import org.imagesci.robopaint.ObjectDescription;
import org.imagesci.robopaint.PaintViewDescription;
import org.imagesci.robopaint.RoboControlPane;
import org.imagesci.robopaint.segmentation.RoboSegment;
import org.imagesci.springls.SpringlsConstants;

import processing.core.PConstants;
import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLImageFormat;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opengl.util.awt.TextRenderer;

import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.MOGACRenderer3D;
import edu.jhu.cs.cisst.vent.renderer.processing.RenderingConfig;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class RoboRenderer extends MOGACRenderer3D {
	CLBuffer<FloatBuffer> referenceDepthmapBuffer = null, depthmapBuffer;
	CLBuffer<FloatBuffer> xyRadarBuffer = null;
	CLBuffer<FloatBuffer> yzRadarSweepBuffer = null;
	CLBuffer<FloatBuffer> xzRadarSweepBuffer = null;
	CLBuffer<FloatBuffer> paintOverlayBuffer = null;
	CLBuffer<FloatBuffer> lineSegmentBuffer;
	CLKernel radarOverlayRender;
	CLKernel paintOverlayRender;
	CLKernel copyPaint;
	protected int insetWidth, insetHeight;

	public RoboRenderer(VisualizationProcessing3D applet, MOGAC3D simulator,
			int rasterWidth, int rasterHeight, int refreshRate) {
		super(applet, simulator, rasterWidth, rasterHeight, refreshRate);
		enableAntiAlias = false;
		insetWidth = 320;
		insetHeight = 320;
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

	private void saveDepthData() {
		queue.putReadBuffer(referenceDepthmapBuffer, true);
		FloatBuffer buff = referenceDepthmapBuffer.getBuffer();
		ImageDataFloat image = new ImageDataFloat(config.getWidth(),
				config.getHeight(), 8);
		for (int j = 0; j < config.getHeight(); j++) {
			for (int i = 0; i < config.getWidth(); i++) {
				for (int k = 0; k < 8; k++) {
					image.set(i, j, k, buff.get());
				}
			}
		}
		buff.rewind();
		System.out.println("SAVING ...");
		NIFTIReaderWriter.getInstance().write(image,
				new File("C:\\Users\\Blake\\Desktop\\depth.nii"));
	}

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {
		applet.strokeWeight(0);
		applet.fill(255, 255, 255);
		applet.rect(0, 0, applet.width, applet.height);
		GL2 gl = (GL2) ((PGraphicsOpenGL2) applet.g).beginGL();
		if (isoSurfRender != null) {
			gl.glEnable(GL.GL_TEXTURE_2D);

			if (!applet.mousePressed) {
				sweepAngle -= sweepStepSize;
				if (sweepAngle < 0)
					sweepAngle += Math.PI * 2;
			}
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
							// saveDepthData();
							dirty = false;
						}
					};
					timer.schedule(task, 1000);
				}
			} else {
				if (!(applet.mouseButton == PConstants.LEFT && applet.mousePressed)
						|| (applet.keyPressed && applet.keyCode == KeyEvent.VK_SHIFT)) {
					computeRadar();
					computePaintOverlay();
				}
			}

			gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[0]);
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
			if (!applet.mousePressed) {
				gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[4]);
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F,
						config.getWidth(), config.getHeight(), 0, GL.GL_RGBA,
						GL.GL_FLOAT, paintOverlayBuffer.getBuffer());

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
			}
			gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[1]);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F, insetWidth,
					insetHeight, 0, GL.GL_RGBA, GL.GL_FLOAT,
					xyRadarBuffer.getBuffer());

			final int SPACING = 10;
			gl.glBegin(GL2.GL_QUADS);
			gl.glTexCoord2f(0, 1);
			gl.glVertex2f(applet.width - insetWidth / 2, SPACING);

			gl.glTexCoord2f(1, 1);
			gl.glVertex2f(applet.width, SPACING);

			gl.glTexCoord2f(1, 0);
			gl.glVertex2f(applet.width, insetHeight / 2 + SPACING);

			gl.glTexCoord2f(0, 0);
			gl.glVertex2f(applet.width - insetWidth / 2, insetHeight / 2
					+ SPACING);

			gl.glEnd();

			gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[2]);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F, insetWidth,
					insetHeight, 0, GL.GL_RGBA, GL.GL_FLOAT,
					yzRadarSweepBuffer.getBuffer());

			gl.glBegin(GL2.GL_QUADS);
			gl.glTexCoord2f(0, 1);
			gl.glVertex2f(applet.width - insetWidth / 2, insetHeight / 2 + 2
					* SPACING);

			gl.glTexCoord2f(1, 1);
			gl.glVertex2f(applet.width, insetHeight / 2 + 2 * SPACING);

			gl.glTexCoord2f(1, 0);
			gl.glVertex2f(applet.width, insetHeight + 2 * SPACING);

			gl.glTexCoord2f(0, 0);
			gl.glVertex2f(applet.width - insetWidth / 2, insetHeight + 2
					* SPACING);

			gl.glEnd();

			gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[3]);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F, insetWidth,
					insetHeight, 0, GL.GL_RGBA, GL.GL_FLOAT,
					xzRadarSweepBuffer.getBuffer());

			gl.glBegin(GL2.GL_QUADS);
			gl.glTexCoord2f(0, 1);
			gl.glVertex2f(applet.width - insetWidth / 2, insetHeight + 3
					* SPACING);

			gl.glTexCoord2f(1, 1);
			gl.glVertex2f(applet.width, insetHeight + 3 * SPACING);

			gl.glTexCoord2f(1, 0);
			gl.glVertex2f(applet.width, (3 * insetHeight) / 2 + 3 * SPACING);

			gl.glTexCoord2f(0, 0);
			gl.glVertex2f(applet.width - insetWidth / 2, (3 * insetHeight) / 2
					+ 2 * SPACING);

			gl.glEnd();

			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glDisable(GL.GL_TEXTURE_2D);

		}

		if (ImageViewDescription.getInstance().getImage() != null) {
			textRenderer.beginRendering(applet.width, applet.height);
			if (GeometryViewDescription.getInstance().getLabelImageFile() == null) {
				textRenderer
						.draw(String
								.format("Next, open a label image (Ctrl+L) that has dimensions %d x %d x %d and integer / short data type.",
										simulator.rows, simulator.cols,
										simulator.slices), 10,
								applet.height - 20);
			} else if (GeometryViewDescription.getInstance()
					.getDistanceFieldImageFile() == null) {
				textRenderer
						.draw(String
								.format("If available, open a distance field image (Ctrl+D) that has dimensions %d x %d x %d and float data type.",
										simulator.rows, simulator.cols,
										simulator.slices), 10,
								applet.height - 20);

				textRenderer.draw(
						"To paint, hold down Shift and Left Click + Drag.", 10,
						applet.height - 40);
			} else {
				textRenderer
						.draw("Remember to periodically save (Ctrl+S) the image segmentation!",
								10, applet.height - 20);
				textRenderer.draw(
						"To paint, hold down Shift and Left Click + Drag.", 10,
						applet.height - 40);
			}
			/*
			textRenderer.draw(
					String.format("Render Frame Rate: %4.1f", renderFPS), 10,
					applet.height - 60);
			*/
			textRenderer.endRendering();
		} else {
			textRenderer.beginRendering(applet.width, applet.height);
			textRenderer.draw(
					"To get started, open a reference image (Ctrl+R).", 10,
					applet.height - 20);
			textRenderer.endRendering();
		}

		((PGraphicsOpenGL2) applet.g).endGL();
	}

	int lastMouseX, lastMouseY;
	float sweepAngle = 0.0f;
	float sweepStepSize = (float) (Math.PI / 128.0f);

	protected void computeRadar() {
		// && (lastMouseX != applet.mouseX || lastMouseY != applet.mouseY)
		if (refImageBuffer != null) {
			synchronized (this) {

				int globalThreads = insetWidth * insetHeight;
				if (globalThreads % WORKGROUP_SIZE != 0) {
					globalThreads = (globalThreads / WORKGROUP_SIZE + 1)
							* WORKGROUP_SIZE;
				}
				int localThreads = WORKGROUP_SIZE;
				radarOverlayRender
						.putArg(pixelBuffer)
						.putArg(xyRadarBuffer)
						.putArg(yzRadarSweepBuffer)
						.putArg(xzRadarSweepBuffer)
						.putArg(referenceDepthmapBuffer)
						.putArg(refImageBuffer)
						.putArg(distanceFieldTexture)
						.putArg(imageLabelBufferCopy)
						.putArg(gpuColorLUT)
						.putArg(minImageValue)
						.putArg(maxImageValue)
						.putArg(brightness)
						.putArg(contrast)
						.putArg(transparency)
						.putArg(sweepAngle)
						.putArg(config.getCamera().getOrig().getZ())
						.putArg((applet.mouseX * config.getHeight())
								/ applet.height)
						.putArg(((applet.height - applet.mouseY) * config
								.getHeight()) / applet.height).rewind();
				queue.put1DRangeKernel(radarOverlayRender, 0, globalThreads,
						localThreads);
				queue.putBarrier().putReadBuffer(xyRadarBuffer, true)
						.putReadBuffer(yzRadarSweepBuffer, true)
						.putReadBuffer(xzRadarSweepBuffer, true);

			}
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
		if (refImageBuffer == null) {
			return;
		}
		synchronized (this) {
			System.nanoTime();
			int global_size = MOGAC3D.roundToWorkgroupPower(simulator.rows
					* simulator.cols * simulator.slices, WORKGROUP_SIZE);
			if (time > 0) {
				if (simulator.imageLabelBuffer != null) {
					syncPaint();
					FloatBuffer averages = ((RoboSegment) simulator)
							.getCurrentAverages();
					for (ObjectDescription obj : GeometryViewDescription
							.getInstance().getObjectDescriptions()) {
						obj.setTargetIntensity(averages.get(obj.getId()));
						RoboControlPane.updateTargetIntensity(obj);
					}
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
					System.err
							.println("Could not find GPU! Disabling Anti-aliasing...");
					if (enableAntiAliasParam != null)
						enableAntiAliasParam.setValue(false);
					enableAntiAlias = false;

					device = CLPlatform.getDefault().getMaxFlopsDevice();
				} else {
					// if (enableAntiAliasParam != null)
					// enableAntiAliasParam.setValue(true);
					// enableAntiAlias = true;
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

			paintOverlayBuffer = context.createFloatBuffer(config.getWidth()
					* config.getHeight() * 4, READ_WRITE, USE_BUFFER);

			final int MAX_NUMBER_SEGMENTS = 3;
			lineSegmentBuffer = context.createFloatBuffer(
					MAX_NUMBER_SEGMENTS * 4, READ_WRITE, USE_BUFFER);

			referenceDepthmapBuffer = context.createFloatBuffer(
					config.getWidth() * config.getHeight() * 4 * 2, READ_WRITE,
					USE_BUFFER);
			depthmapBuffer = context.createFloatBuffer(config.getWidth()
					* config.getHeight() * 4 * 2, READ_WRITE, USE_BUFFER);
			xyRadarBuffer = context.createFloatBuffer(insetWidth * insetHeight
					* 4, READ_WRITE, USE_BUFFER);
			yzRadarSweepBuffer = context.createFloatBuffer(insetWidth
					* insetHeight * 4, READ_WRITE, USE_BUFFER);
			xzRadarSweepBuffer = context.createFloatBuffer(insetWidth
					* insetHeight * 4, READ_WRITE, USE_BUFFER);
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
			boolean isPainting = (applet.keyPressed && applet.keyCode == KeyEvent.VK_SHIFT);

			isoSurfRender.putArg(pixelBuffer).putArg(referenceDepthmapBuffer)
					.putArg(depthmapBuffer).putArg(configBuffer)
					.putArg(refImageBuffer).putArg(distanceFieldTexture)
					.putArg(imageLabelBufferCopy).putArg(modelViewMatrixBuffer)
					.putArg(modelViewInverseMatrixBuffer).putArg(gpuColorLUT)
					.putArg(fastRender).putArg(sampleX).putArg(sampleY)
					.putArg(row).putArg(col).putArg(slice)
					.putArg(showXplane ? 1 : 0).putArg(showYplane ? 1 : 0)
					.putArg(showZplane ? 1 : 0).putArg((showIsoSurf) ? 1 : 0)
					.putArg(isPainting ? 1 : 0).putArg(minImageValue)
					.putArg(maxImageValue).putArg(brightness).putArg(contrast)
					.putArg(transparency).rewind();
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
		System.out.println("NUMBER OF COLORS " + simulator.getNumColors()+" "+simulator.rows+" "+simulator.cols+" "+simulator.slices);
		gpuColorLUT = context.createFloatBuffer(4 * simulator.getNumColors(),
				READ_WRITE);
		colors = new Color4f[simulator.getNumColors()];
		transparency = 0.5f;
		// task.cancel();
		dirty = true;

		int[] masks = simulator.getLabelMasks();
		contourColorsParam = new ParamColor[masks.length - 1];
		contoursVisibleParam = new ParamBoolean[masks.length - 1];

		final long seed = 5437897311l;
		Random randn = new Random(seed);
		GeometryViewDescription.getInstance().removeAllObjectDescriptions();
		Color c = Color.BLACK;
		ObjectDescription label;
		for (int i = 1; i < contourColorsParam.length + 1; i++) {
			c = new Color(randn.nextFloat(), randn.nextFloat(),
					randn.nextFloat());
			label = new ObjectDescription("Label " + (i), masks[i]);
			label.setColor(c.getRed(), c.getGreen(), c.getBlue(), 255);
			GeometryViewDescription.getInstance().addObjectDescription(label);

			label.setTargetIntensity(((MACWE3D) simulator).getCurrentAverage(i));
			label.setPressureWeight(1.0f);
			contourColorsParam[i - 1] = new ParamColor("Object Color ["
					+ masks[i] + "]", c);
			contoursVisibleParam[i - 1] = new ParamBoolean("Visibility ["
					+ masks[i] + "]", true);
		}
		frameUpdate(1, -1);
		updateColors();
		label = new ObjectDescription("Background", 0);

		GeometryViewDescription.getInstance().addObjectDescription(label);
		label.setColor(c.getRed(), c.getGreen(), c.getBlue(), 255);
		label.setTargetIntensity(((MACWE3D) simulator).getCurrentAverage(0));
		label.setPressureWeight(0f);
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
									define("INSET_WIDTH", insetWidth),
									define("INSET_HEIGHT", insetHeight),
									define("WIDTH", config.getWidth()),
									define("HEIGHT", config.getHeight()),
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
					copyPaint = program.createCLKernel("copyPaint");
					radarOverlayRender = program
							.createCLKernel("radarOverlayRender");
					paintOverlayRender = program
							.createCLKernel("paintOverlayRender");
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
		frameUpdate(1, -1);

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
			if (index >= contourColorsParam.length)
				break;
		}
		updateColors(true);
		frameUpdate(0, 0);
		refresh();
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
		texBuffer = IntBuffer.allocate(5);
		gl.glGenTextures(5, texBuffer);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[0]);
		gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F,
				config.getWidth(), config.getHeight(), 0, GL.GL_RGB,
				GL.GL_FLOAT, pixelBuffer.getBuffer());

		gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[1]);
		gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F, insetWidth,
				insetHeight, 0, GL.GL_RGBA, GL.GL_FLOAT,
				xyRadarBuffer.getBuffer());

		gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[2]);
		gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F, insetWidth,
				insetHeight, 0, GL.GL_RGBA, GL.GL_FLOAT,
				yzRadarSweepBuffer.getBuffer());

		gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[3]);
		gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F, insetWidth,
				insetHeight, 0, GL.GL_RGBA, GL.GL_FLOAT,
				xzRadarSweepBuffer.getBuffer());

		gl.glBindTexture(GL.GL_TEXTURE_2D, texBuffer.array()[4]);
		gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
				GL2GL3.GL_CLAMP_TO_BORDER);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2GL3.GL_RGBA16F,
				config.getWidth(), config.getHeight(), 0, GL.GL_RGBA,
				GL.GL_FLOAT, paintOverlayBuffer.getBuffer());
		gl.glDisable(GL.GL_TEXTURE_2D);
		((PGraphicsOpenGL2) applet.g).endGL();
		textRenderer = new TextRenderer(applet.getFont().deriveFont(Font.BOLD,
				14), true, true, null, false);
		textRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
	}

	protected int lineSegmentOffset = 0;
	protected boolean dirtyPaint = false;

	public void computePaintOverlay() {
		ObjectDescription current = GeometryViewDescription.getInstance()
				.getCurrentObject();
		if (current == null)
			return;
		boolean isPainting = (lastMouseX != applet.mouseX || lastMouseY != applet.mouseY)
				&& (applet.keyPressed && applet.keyCode == KeyEvent.VK_SHIFT
						&& applet.mousePressed && applet.mouseButton == PConstants.LEFT);

		synchronized (this) {
			int globalThreads = config.getWidth() * config.getHeight();
			if (globalThreads % WORKGROUP_SIZE != 0) {
				globalThreads = (globalThreads / WORKGROUP_SIZE + 1)
						* WORKGROUP_SIZE;
			}
			int localThreads = WORKGROUP_SIZE;
			int currentMouseX = applet.mouseX;
			int currentMouseY = applet.mouseY;
			paintOverlayRender
					.putArg(pixelBuffer)
					.putArg(paintOverlayBuffer)
					.putArg(referenceDepthmapBuffer)
					.putArg(depthmapBuffer)
					.putArg(lineSegmentBuffer)
					.putArg(configBuffer)
					.putArgs(refImageBuffer, distanceFieldTexture,
							imageLabelBufferCopy, modelViewMatrixBuffer,
							modelViewInverseMatrixBuffer, gpuColorLUT)

					.putArg(minImageValue)
					.putArg(maxImageValue)
					.putArg(brightness)
					.putArg(contrast)
					.putArg(transparency)
					.putArg(lineSegmentOffset)
					.putArg((lastMouseX * config.getHeight()) / applet.height)
					.putArg(((applet.height - lastMouseY) * config.getHeight())
							/ applet.height)
					.putArg((currentMouseX * config.getHeight())
							/ applet.height)
					.putArg(((applet.height - currentMouseY) * config
							.getHeight()) / applet.height)
					.putArg(PaintViewDescription.getInstance()
							.getPaintBrushSize())
					.putArg(current.getId())
					.putArg(PaintViewDescription.getInstance().isBrush3D() ? 1
							: 0)
					.putArg(GeometryViewDescription.getInstance().isSliceView() ? 0
							: 1).putArg(isPainting ? 1 : 0).rewind();
			queue.put1DRangeKernel(paintOverlayRender, 0, globalThreads,
					localThreads);
			queue.putBarrier().putReadBuffer(paintOverlayBuffer, true)
					.putReadBuffer(pixelBuffer, true);

			if (isPainting) {
				renderPaintDirty = true;
				dirtyPaint = true;
				int global_size = MOGAC3D.roundToWorkgroupPower(simulator.rows
						* simulator.cols * simulator.slices, WORKGROUP_SIZE);
				copyPaint
						.putArgs(imageLabelBufferCopy, distanceFieldBufferCopy,
								lineSegmentBuffer)
						.putArg(lineSegmentOffset)
						.putArg(PaintViewDescription.getInstance()
								.getPaintBrushSize())
						.putArg(current.getId())
						.putArg(PaintViewDescription.getInstance().isBrush3D() ? 1
								: 0)
						.putArg(GeometryViewDescription.getInstance()
								.isSliceView() ? 0 : 1)
						.putArg(isPainting ? 1 : 0).rewind();
				queue.put1DRangeKernel(copyPaint, 0, global_size,
						WORKGROUP_SIZE);

				maskLabels.setArgs(imageLabelBufferCopy, volumeColorBuffer,
						gpuColorLUT).rewind();
				queue.put1DRangeKernel(maskLabels, 0, global_size,
						WORKGROUP_SIZE);
				copyLevelSetImage.putArgs(distanceFieldBufferCopy,
						imageLabelBufferCopy, gpuColorLUT, volumeColorBuffer)
						.rewind();
				queue.put1DRangeKernel(copyLevelSetImage, 0, global_size,
						WORKGROUP_SIZE);

			} else {
				if (dirtyPaint) {
					queue.putReadBuffer(volumeColorBuffer, false);
					queue.putWriteImage(distanceFieldTexture, false);
					dirtyPaint = false;
					refresh();
				}
			}
			lastMouseX = currentMouseX;
			lastMouseY = currentMouseY;

		}
		// if (isPainting)
	}

	protected boolean renderPaintDirty = false;

	public void syncPaint() {
		if (renderPaintDirty) {
			queue.putReadBuffer(imageLabelBufferCopy, true).putReadBuffer(
					distanceFieldBufferCopy, true);
			simulator.imageLabelBuffer.getBuffer()
					.put(imageLabelBufferCopy.getBuffer()).rewind();
			simulator.distanceFieldBuffer.getBuffer()
					.put(distanceFieldBufferCopy.getBuffer()).rewind();
			imageLabelBufferCopy.getBuffer().rewind();
			distanceFieldBufferCopy.getBuffer().rewind();
			simulator.queue.putWriteBuffer(simulator.imageLabelBuffer, true)
					.putWriteBuffer(simulator.distanceFieldBuffer, true);
			renderPaintDirty = false;
		} else {
			if (simulator.queue != null && simulator.imageLabelBuffer != null) {
				simulator.queue.putReadBuffer(simulator.imageLabelBuffer, true)
						.putReadBuffer(simulator.distanceFieldBuffer, true);
				imageLabelBufferCopy.getBuffer()
						.put(simulator.imageLabelBuffer.getBuffer()).rewind();
				distanceFieldBufferCopy.getBuffer()
						.put(simulator.distanceFieldBuffer.getBuffer())
						.rewind();
				simulator.imageLabelBuffer.getBuffer().rewind();
				simulator.distanceFieldBuffer.getBuffer().rewind();
				queue.putWriteBuffer(imageLabelBufferCopy, true)
						.putWriteBuffer(distanceFieldBufferCopy, true);
			}
		}
	}

	protected void savePaint() {
		queue.putReadBuffer(imageLabelBufferCopy, true);
		IntBuffer levelSet = imageLabelBufferCopy.getBuffer();
		ImageDataInt labelImage = new ImageDataInt(simulator.rows,
				simulator.cols, simulator.slices);
		int[][][] imageMat = labelImage.toArray3d();
		for (int k = 0; k < simulator.slices; k++) {
			for (int j = 0; j < simulator.cols; j++) {
				for (int i = 0; i < simulator.rows; i++) {
					imageMat[i][j][k] = levelSet.get();
				}
			}
		}
		levelSet.rewind();
		NIFTIReaderWriter.getInstance().write(labelImage,
				new File("C:\\Users\\Blake\\Desktop\\image_labels.nii"));

	}
}
