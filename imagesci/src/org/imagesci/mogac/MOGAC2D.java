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
package org.imagesci.mogac;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import org.imagesci.utility.ContourArray;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC2D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MOGAC2D is an implementation of Multi-Object Geodesic Active
 * Contours 2D.
 */
public class MOGAC2D extends AbstractCalculation {

	/**
	 * The listener interface for receiving frameUpdate events. The class that
	 * is interested in processing a frameUpdate event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addFrameUpdateListener<code> method. When
	 * the frameUpdate event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see FrameUpdateEvent
	 */
	public static interface FrameUpdateListener {

		/**
		 * Frame update.
		 * 
		 * @param time
		 *            the time
		 * @param fps
		 *            the fps
		 */
		public void frameUpdate(int time, double fps);
	}

	/** The STRIDE. */
	public final static int STRIDE = 128;

	/** The WORKGROU p_ size. */
	public static int WORKGROUP_SIZE = 128;

	/** The clamp speed. */
	protected boolean clampSpeed = false;

	/** The contains overlaps. */
	protected boolean containsOverlaps = true;

	/** The context. */
	public CLContext context;

	/** The contours. */
	protected ContourArray[] contours;
	/** The curvature weight. */
	protected float curvatureWeight = 0;

	/** The delta level set buffer. */
	public CLBuffer<FloatBuffer> deltaLevelSetBuffer = null;
	/** The dice threshold. */
	protected double diceThreshold = 0.995;

	/** The dirty bit. */
	boolean dirty = false;
	/** The unsigned level set buffer. */
	public CLBuffer<FloatBuffer> distanceFieldBuffer = null;
	/** The distance field. */
	protected float[][] distField;

	/** The distance field image. */
	protected ImageDataFloat distFieldImage;

	/** The elapsed time. */
	protected long elapsedTime = 0;

	/** The force indexes. */
	protected int[] forceIndexes;

	/** The label offset buffer. */
	public CLBuffer<IntBuffer> forceIndexesBuffer = null;

	/** The label buffer. */
	public CLBuffer<IntBuffer> idBuffer = null;

	/** The reference image. */
	protected ImageData image = null;

	/** The label buffer. */
	public CLBuffer<IntBuffer> imageLabelBuffer = null;

	/** The intensity estimation. */
	protected boolean intensityEstimation = true;

	/** The iso surface buffer. */
	public CLBuffer<FloatBuffer> isoSurfaceBuffer;
	// public int final
	/** The kernel map. */
	protected Map<String, CLKernel> kernelMap;

	/** The label image. */
	protected ImageDataInt labelImage;
	/** The label mask buffer. */
	public CLBuffer<IntBuffer> labelMaskBuffer = null;

	/** The label masks. */
	protected int[] labelMasks;

	/** The labels *. */
	protected int[][] labels;

	/** The last start time. */
	protected long lastStartTime = 0;

	/** The listeners. */
	protected LinkedList<FrameUpdateListener> listeners = new LinkedList<FrameUpdateListener>();

	/** The maximum number of overlapping objects. */
	protected final int MAX_OBJECTS = 32;

	/** The outer iterations. */
	protected int maxIterations = 100;

	/** The max layers. */
	protected int maxLayers = 3;

	/** The max speed. */
	protected float maxSpeed = 0.999f;

	/** The max tmp buffer. */
	CLBuffer<FloatBuffer> maxTmpBuffer = null;

	/** The STRIDE. */
	protected int numLabels;

	/** The num objects. */
	protected int numObjects = 0;

	/** The old unsigned level set buffer. */
	public CLBuffer<FloatBuffer> oldDistanceFieldBuffer = null;

	/** The old label buffer. */
	public CLBuffer<IntBuffer> oldImageLabelBuffer = null;

	/** The pressure buffer. */
	public CLBuffer<FloatBuffer> pressureBuffer = null;

	/** The pressure image. */
	protected ImageDataFloat pressureImage = null;

	/** The pressure weight. */
	protected float pressureWeight = 0;

	/** The queue. */
	public CLCommandQueue queue;

	/** The resampling interval. */
	protected int resamplingInterval = 10;

	/** The cols. */
	protected int rows, cols;

	/** The target pressure. */
	protected float targetPressure = 0;

	/** The count. */
	protected long time = 0;

	/** The topology rule. */
	protected boolean topologyPreservation = false;

	/** The vec field buffer. */
	public CLBuffer<FloatBuffer> vecFieldBuffer = null;

	/** The vec field image. */
	protected ImageDataFloat vecFieldImage = null;

	/** The vec field weight. */
	protected float vecFieldWeight = 0;

	/**
	 * Instantiates a new mGAC open c l2 d.
	 * 
	 * @param refImage
	 *            the reference image
	 */
	public MOGAC2D(ImageData refImage) {
		this(refImage, CLDevice.Type.GPU);
	}

	/**
	 * Instantiates a new Multi-object Geodesic Active Contour 2D.
	 *
	 * @param refImage the reference image
	 * @param type the type
	 */
	public MOGAC2D(ImageData refImage, CLDevice.Type type) {
		CLPlatform[] platforms = CLPlatform.listCLPlatforms();
		CLDevice device = null;
		this.image = refImage;
		for (CLPlatform p : platforms) {
			device = p.getMaxFlopsDevice(type);
			if (device != null) {
				break;
			}
		}
		if (device == null) {
			device = CLPlatform.getDefault().getMaxFlopsDevice();
		}
		if (type == CLDevice.Type.CPU) {
			WORKGROUP_SIZE = 256;
		} else if (type == CLDevice.Type.GPU) {
			WORKGROUP_SIZE = 128;

		}
		context = CLContext.create(device);
		queue = device.createCommandQueue();
		System.out.println("MOGAC 2D using device: " + device.getVendor() + " "
				+ device.getVersion() + " " + device.getName());

	}

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 2D.
	 *
	 * @param rows the rows
	 * @param cols the cols
	 * @param context the context
	 * @param queue the queue
	 */
	public MOGAC2D(int rows, int cols, CLContext context, CLCommandQueue queue) {
		this.context = context;
		this.queue = queue;
		this.rows = rows;
		this.cols = cols;

	}

	/**
	 * Solve.
	 * 
	 * @param unsignedImage
	 *            the unsigned image
	 * @param labelImage
	 *            the label image
	 * @param containsOverlaps
	 *            the contains overlaps
	 * @return the image data float
	 */
	public ImageDataFloat solve(ImageDataFloat unsignedImage,
			ImageDataInt labelImage, boolean containsOverlaps) {
		try {
			init(unsignedImage, labelImage, containsOverlaps);
			queue.finish();
			long startTime = System.nanoTime();

			for (int outerIter = 0; outerIter < maxIterations; outerIter++) {
				if (!step()) {
					break;
				}

			}
			queue.finish();
			long endTime = System.nanoTime();
			this.elapsedTime = endTime - startTime;
			System.out
					.printf("Time Steps: %d\nElapsed Time: %6.4f sec\nFrame Rate: %6.2f fps\n",
							time, 1E-9 * (endTime - startTime), 1E9 * time
									/ (endTime - startTime));
			finish();
			context.release();
			markCompleted();
			return distFieldImage;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Initializes the OpenCL device.
	 *
	 * @param labelImage the label image
	 * @param containsOverlaps the contains overlaps
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void init(ImageDataInt labelImage, boolean containsOverlaps)
			throws IOException {
		init(null, labelImage, containsOverlaps);
	}

	/**
	 * Initializes the OpenCL device.
	 * 
	 * @param unsignedImage
	 *            the unsigned image
	 * @param labelImage
	 *            the label image
	 * @param containsOverlaps
	 *            the contains overlaps
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init(ImageDataFloat unsignedImage, ImageDataInt labelImage,
			boolean containsOverlaps) throws IOException {

		rows = labelImage.getRows();
		cols = labelImage.getCols();
		this.containsOverlaps = containsOverlaps;
		int l;
		int mask = 0x00000001;
		this.labels = labelImage.toArray2d();
		if (containsOverlaps) {
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			int totalMask = 0;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					l = labels[i][j];
					totalMask |= l;
					labelHash.add(l);
				}
			}
			numObjects = 0;
			while (totalMask != 0) {
				if ((totalMask & 0x01) != 0) {
					numObjects++;
				}
				totalMask >>= 1;
			}
			numLabels = labelHash.size();
			l = 0;
			labelMasks = new int[numLabels];
			for (Integer val : labelHash) {
				labelMasks[l++] = val;
			}
			forceIndexes = new int[numLabels];
			for (int i = 0; i < numLabels; i++) {
				mask = labelMasks[i];
				forceIndexes[i] = -1;
				for (int b = 0; b < numObjects; b++) {
					if ((0x01 << b) == mask) {
						forceIndexes[i] = b;
						break;
					}
				}
			}
		} else {
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					l = labels[i][j];
					labelHash.add(l);
				}
			}
			numLabels = labelHash.size();
			numObjects = numLabels - 1;
			l = 0;

			labelMasks = new int[numLabels];
			for (Integer val : labelHash) {
				labelMasks[l++] = val;
			}
			forceIndexes = new int[numLabels];
			for (int i = 0; i < numLabels; i++) {
				mask = labelMasks[i];
				forceIndexes[i] = i - 1;
			}
		}
		CLProgram program = context.createProgram(
				getClass().getResourceAsStream("MogacEvolveLevelSet2D.cl"))
				.build(define("ROWS", rows), define("COLS", cols),
						define("CONTAINS_OVERLAPS", containsOverlaps),
						define("CLAMP_SPEED", clampSpeed ? 1 : 0),
						define("NUM_LABELS", numLabels));

		kernelMap = program.createCLKernels();
		imageLabelBuffer = context.createIntBuffer(rows * cols, READ_WRITE,
				USE_BUFFER);
		oldImageLabelBuffer = context.createIntBuffer(rows * cols, READ_WRITE,
				USE_BUFFER);
		IntBuffer label = imageLabelBuffer.getBuffer();
		IntBuffer oldLabel = oldImageLabelBuffer.getBuffer();
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				int lab = labels[i][j];
				label.put(lab);
				oldLabel.put(lab);
			}
		}
		label.rewind();
		oldLabel.rewind();
		oldDistanceFieldBuffer = context.createFloatBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);
		if (unsignedImage == null) {
			convertLabelsToLevelSet();
			unsignedImage = new ImageDataFloat(rows, cols);
			unsignedImage.setName(image.getName() + "_distfield");
			queue.putReadBuffer(distanceFieldBuffer, true);
			FloatBuffer buff = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			distField = unsignedImage.toArray2d();
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					float tmp = distField[i][j] = buff.get();
					oldUnsignedLevelSet.put(tmp);
				}
			}
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(oldDistanceFieldBuffer, true);
			buff.rewind();
		} else {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols,
					READ_WRITE, USE_BUFFER);
			FloatBuffer unsignedLevelSet = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray2d();
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					float val = distField[i][j];
					unsignedLevelSet.put(val);
					oldUnsignedLevelSet.put(val);
				}
			}
			unsignedLevelSet.rewind();
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(distanceFieldBuffer, true).putWriteBuffer(
					oldDistanceFieldBuffer, true);
		}

		labelMaskBuffer = context.createIntBuffer(labelMasks.length, READ_ONLY,
				USE_BUFFER);
		labelMaskBuffer.getBuffer().put(labelMasks).rewind();
		forceIndexesBuffer = context.createIntBuffer(forceIndexes.length,
				READ_ONLY, USE_BUFFER);
		forceIndexesBuffer.getBuffer().put(forceIndexes).rewind();

		if (pressureImage != null) {
			rescale(pressureImage.toArray2d());
			float[][] pressure = pressureImage.toArray2d();
			pressureBuffer = context.createFloatBuffer(rows * cols, READ_ONLY);
			FloatBuffer buff = pressureBuffer.getBuffer();
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					buff.put(pressure[i][j]);
				}
			}
			buff.rewind();
			queue.putWriteBuffer(pressureBuffer, true);
			if (vecFieldImage != null) {
				float[][][] vecField = vecFieldImage.toArray3d();
				vecFieldBuffer = context.createFloatBuffer(rows * cols * 2,
						READ_ONLY);
				FloatBuffer advectBuff = vecFieldBuffer.getBuffer();
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						advectBuff.put(vecField[i][j][0]);
						advectBuff.put(vecField[i][j][1]);
					}
				}
				advectBuff.rewind();
				queue.putWriteBuffer(vecFieldBuffer, true);
			}
		}
		idBuffer = context.createIntBuffer(rows * cols * 5, READ_WRITE,
				USE_BUFFER);
		deltaLevelSetBuffer = context.createFloatBuffer(rows * cols * 5,
				READ_WRITE, USE_BUFFER);

		queue.putWriteBuffer(labelMaskBuffer, true).putWriteBuffer(
				forceIndexesBuffer, true);

		this.distFieldImage = unsignedImage;
		this.labelImage = labelImage;
		this.labelImage.setName(image.getName() + "_labels");
		distFieldImage.setName(image.getName() + "_distfield");
		setTotalUnits(maxIterations / resamplingInterval);
		finish();
	}

	/**
	 * Initializes the OpenCL device.
	 * 
	 * @param labelBuffer
	 *            the label buffer
	 * @param numLabels
	 *            the number of clusters
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init(CLBuffer<IntBuffer> labelBuffer, int numLabels)
			throws IOException {
		init(null, labelBuffer, numLabels);
	}

	/**
	 * Inits the.
	 * 
	 * @param unsignedLevelSetBuffer
	 *            the unsigned level set buffer
	 * @param labelBuffer
	 *            the label buffer
	 * @param numLabels
	 *            the number of labels
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init(CLBuffer<FloatBuffer> unsignedLevelSetBuffer,
			CLBuffer<IntBuffer> labelBuffer, int numLabels)
			throws IOException {
		int l;
		this.numLabels = numLabels;
		numObjects = numLabels - 1;
		this.containsOverlaps = false;
		CLProgram program = context.createProgram(
				getClass().getResourceAsStream("MogacEvolveLevelSet2D.cl"))
				.build(define("ROWS", rows), define("COLS", cols),
						define("CONTAINS_OVERLAPS", containsOverlaps),
						define("CLAMP_SPEED", clampSpeed ? 1 : 0),
						define("NUM_LABELS", numLabels));

		kernelMap = program.createCLKernels();

		this.imageLabelBuffer = labelBuffer;

		this.distanceFieldBuffer = unsignedLevelSetBuffer;
		oldImageLabelBuffer = context.createIntBuffer(rows * cols, READ_WRITE,
				USE_BUFFER);
		oldDistanceFieldBuffer = context.createFloatBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);
		if (unsignedLevelSetBuffer == null) {
			convertLabelsToLevelSet();
		}

		FloatBuffer unsignedLevelSet = this.distanceFieldBuffer.getBuffer();
		FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer.getBuffer();
		IntBuffer label = labelBuffer.getBuffer();
		IntBuffer oldLabel = oldImageLabelBuffer.getBuffer();
		oldUnsignedLevelSet.put(unsignedLevelSet).rewind();
		oldLabel.put(label).rewind();
		queue.putWriteBuffer(oldDistanceFieldBuffer, true);
		queue.putWriteBuffer(oldImageLabelBuffer, true);

		l = 0;
		labelMasks = new int[numLabels];
		for (l = 0; l < numLabels; l++) {
			labelMasks[l] = l;
		}
		forceIndexes = new int[numLabels];
		for (l = 0; l < numLabels; l++) {
			forceIndexes[l] = l - 1;
		}
		labelMaskBuffer = context.createIntBuffer(labelMasks.length, READ_ONLY,
				USE_BUFFER);
		labelMaskBuffer.getBuffer().put(labelMasks).rewind();
		forceIndexesBuffer = context.createIntBuffer(forceIndexes.length,
				READ_ONLY, USE_BUFFER);
		forceIndexesBuffer.getBuffer().put(forceIndexes).rewind();
		unsignedLevelSet.rewind();
		oldUnsignedLevelSet.rewind();
		label.rewind();
		oldLabel.rewind();

		idBuffer = context.createIntBuffer(rows * cols * 5, READ_WRITE,
				USE_BUFFER);
		deltaLevelSetBuffer = context.createFloatBuffer(rows * cols * 5,
				READ_WRITE, USE_BUFFER);
		queue.putWriteBuffer(oldDistanceFieldBuffer, true)
				.putWriteBuffer(labelMaskBuffer, true)
				.putWriteBuffer(forceIndexesBuffer, true)
				.putWriteBuffer(oldImageLabelBuffer, true);
		setTotalUnits(maxIterations / resamplingInterval);

	}

	/**
	 * Convert labels to level set.
	 */
	public void convertLabelsToLevelSet() {
		final CLKernel labelsToLevelSet = kernelMap.get("labelsToLevelSet");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		if (distanceFieldBuffer == null) {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols,
					USE_BUFFER, READ_WRITE);
		}
		labelsToLevelSet.putArgs(imageLabelBuffer, oldImageLabelBuffer,
				distanceFieldBuffer, oldDistanceFieldBuffer).rewind();
		queue.put1DRangeKernel(labelsToLevelSet, 0, rows * cols, WORKGROUP_SIZE);
		for (int i = 1; i <= maxLayers; i++) {
			extendDistanceField
					.putArgs(oldDistanceFieldBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(i).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0, rows * cols,
					WORKGROUP_SIZE);
		}

		queue.putReadBuffer(distanceFieldBuffer, true);
	}

	/**
	 * Rescale.
	 * 
	 * @param pressureForce
	 *            the pressure force
	 */
	protected void rescale(float[][] pressureForce) {
		if (!Float.isNaN(targetPressure)) {
			int index = 0;
			double min = Float.MAX_VALUE;
			double max = Float.MIN_VALUE;

			for (int i = 0; i < pressureForce.length; i++) {
				for (int j = 0; j < pressureForce[0].length; j++) {
					double val = pressureForce[i][j] - targetPressure;
					min = Math.min(val, min);
					max = Math.max(val, max);
					index++;
				}
			}
			double normMin = (Math.abs(min) > 1E-4) ? 1 / Math.abs(min) : 1;
			double normMax = (Math.abs(max) > 1E-4) ? 1 / Math.abs(max) : 1;
			for (int i = 0; i < pressureForce.length; i++) {
				for (int j = 0; j < pressureForce[0].length; j++) {
					double val = pressureForce[i][j] - targetPressure;
					if (val < 0) {
						pressureForce[i][j] = (float) (val * normMin);
					} else {
						pressureForce[i][j] = (float) (val * normMax);
					}
				}
			}
		}
	}

	/**
	 * Gets the distance field.
	 * 
	 * @return the distance field
	 */
	public ImageDataFloat getDistanceField() {
		if (dirty) {
			finish();
		}
		return distFieldImage;
	}

	/**
	 * Gets the image labels.
	 * 
	 * @return the image labels
	 */
	public ImageDataInt getImageLabels() {
		if (dirty) {
			finish();
		}

		return labelImage;
	}

	/**
	 * Finish.
	 */
	public void finish() {
		queue.putReadBuffer(distanceFieldBuffer, true).putReadBuffer(
				imageLabelBuffer, true);
		FloatBuffer buff = distanceFieldBuffer.getBuffer();
		IntBuffer buff2 = imageLabelBuffer.getBuffer();

		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				distField[i][j] = buff.get();
				labels[i][j] = buff2.get();
			}
		}

		buff.rewind();
		buff2.rewind();
		dirty = false;
	}

	/**
	 * Step.
	 * 
	 * @return true, if successful
	 */
	public boolean step() {
		final CLKernel pressureSpeedKernel = kernelMap
				.get("pressureSpeedKernel");
		final CLKernel vecFieldSpeedKernel = kernelMap
				.get("vecFieldSpeedKernel");
		final CLKernel pressureVecFieldSpeedKernel = kernelMap
				.get("pressureVecFieldSpeedKernel");
		final CLKernel maxImageValue = kernelMap.get("maxImageValue");
		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel maxTimeStep = kernelMap.get("maxTimeStep");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");

		if (pressureBuffer != null) {
			if (vecFieldBuffer != null) {
				pressureVecFieldSpeedKernel
						.putArgs(pressureBuffer, vecFieldBuffer,
								oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
								forceIndexesBuffer).putArg(pressureWeight)
						.putArg(vecFieldWeight).putArg(curvatureWeight)
						.rewind();
				queue.put1DRangeKernel(pressureVecFieldSpeedKernel, 0, rows
						* cols, WORKGROUP_SIZE);
			} else {
				pressureSpeedKernel
						.putArgs(pressureBuffer, oldDistanceFieldBuffer,
								oldImageLabelBuffer, deltaLevelSetBuffer,
								idBuffer, labelMaskBuffer, forceIndexesBuffer)
						.putArg(pressureWeight).putArg(curvatureWeight)
						.rewind();
				queue.put1DRangeKernel(pressureSpeedKernel, 0, rows * cols,
						WORKGROUP_SIZE);
			}
		} else {
			vecFieldSpeedKernel
					.putArgs(vecFieldBuffer, oldDistanceFieldBuffer,
							oldImageLabelBuffer, deltaLevelSetBuffer, idBuffer,
							labelMaskBuffer, forceIndexesBuffer)
					.putArg(vecFieldWeight).putArg(curvatureWeight).rewind();
			queue.put1DRangeKernel(vecFieldSpeedKernel, 0, rows * cols,
					WORKGROUP_SIZE);
		}

		// queue.finish();
		// saveDeltaImage();

		// Find max
		if (topologyPreservation) {
			if (!clampSpeed) {
				if (maxTmpBuffer == null) {
					maxTmpBuffer = context.createFloatBuffer(rows, READ_WRITE,
							USE_BUFFER);
				}
				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.rewind();
				queue.put1DRangeKernel(maxImageValue, 0,
						roundToWorkgroupPower(rows), WORKGROUP_SIZE);
				maxTimeStep.putArg(maxTmpBuffer).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 8, 8);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer).putArg(0).putArg(0).rewind();

				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer).putArg(0).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer).putArg(1).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer).putArg(1).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);

			} else {
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer)
						.putArg(0.5f).putArg(0).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer)
						.putArg(0.5f).putArg(0).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer)
						.putArg(0.5f).putArg(1).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer)
						.putArg(0.5f).putArg(1).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, (rows * cols) / 4,
						WORKGROUP_SIZE);

			}
		} else {
			if (!clampSpeed) {
				if (maxTmpBuffer == null) {
					maxTmpBuffer = context.createFloatBuffer(rows, READ_WRITE,
							USE_BUFFER);
				}
				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.rewind();
				queue.put1DRangeKernel(maxImageValue, 0,
						roundToWorkgroupPower(rows), WORKGROUP_SIZE);
				maxTimeStep.putArg(maxTmpBuffer).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 8, 8);
				applyForces.putArgs(oldDistanceFieldBuffer,
						oldImageLabelBuffer, deltaLevelSetBuffer, idBuffer,
						distanceFieldBuffer, imageLabelBuffer, maxTmpBuffer)
						.rewind();
			} else {
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer)
						.putArg(0.5f).rewind();
			}

			queue.put1DRangeKernel(applyForces, 0, rows * cols, WORKGROUP_SIZE);
		}
		for (int i = 1; i <= maxLayers; i++) {
			extendDistanceField
					.putArgs(oldDistanceFieldBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(i).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0, rows * cols,
					WORKGROUP_SIZE);
		}
		final CLKernel plugLevelSet = kernelMap.get("plugLevelSet");
		plugLevelSet.putArgs(distanceFieldBuffer, imageLabelBuffer).rewind();
		queue.put1DRangeKernel(plugLevelSet, 0, rows * cols, WORKGROUP_SIZE);
		copyBuffers.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
				distanceFieldBuffer, imageLabelBuffer).rewind();
		queue.put1DRangeKernel(copyBuffers, 0, rows * cols, WORKGROUP_SIZE);
		contours = null;
		dirty = true;
		if (time % resamplingInterval == 0) {
			queue.finish();
			long tmp = System.nanoTime();
			for (FrameUpdateListener updater : listeners) {
				updater.frameUpdate((int) time, 1E9 * resamplingInterval
						/ (tmp - lastStartTime));
			}
			lastStartTime = tmp;
			incrementCompletedUnits();
		}
		time++;
		return true;
	}

	/**
	 * Round to workgroup power.
	 * 
	 * @param length
	 *            the length
	 * @return the int
	 */
	public static int roundToWorkgroupPower(int length) {
		if (length % WORKGROUP_SIZE == 0) {
			return length;
		} else {
			return WORKGROUP_SIZE * (length / WORKGROUP_SIZE + 1);
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		try {
			File fdist = new File(PlaceHolder.class.getResource(
					"shapes_overlap_distfield.nii").toURI());

			File flabel = new File(PlaceHolder.class.getResource(
					"shapes_overlap_labels.nii").toURI());
			File fimg = new File(PlaceHolder.class.getResource("x.png").toURI());
			ImageDataFloat initDistField = new ImageDataFloat(NIFTIReaderWriter
					.getInstance().read(fdist));
			ImageDataInt initLabels = new ImageDataInt(NIFTIReaderWriter
					.getInstance().read(flabel));
			ImageDataFloat refImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(fimg));
			MOGAC2D activeContour = new MOGAC2D(refImage, CLDevice.Type.GPU);
			activeContour.setPressure(refImage, -0.5f);
			activeContour.setCurvatureWeight(1.0f);
			activeContour.setTargetPressure(128.0f);
			activeContour.setMaxIterations(601);
			activeContour.setClampSpeed(true);
			if (showGUI) {
				try {
					activeContour.init(initDistField, initLabels, true);

					VisualizationMOGAC2D visual = new VisualizationMOGAC2D(512,
							512, activeContour);
					VisualizationApplication app = new VisualizationApplication(
							visual);
					app.setPreferredSize(new Dimension(920, 650));
					app.setShowToolBar(true);
					app.addListener(visual);
					app.runAndWait();
					visual.dispose();
					System.exit(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				activeContour.solve(initDistField, initLabels, true);
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Round to workgroup power.
	 * 
	 * @param length
	 *            the length
	 * @param workgroup
	 *            the workgroup
	 * @return the int
	 */
	public static int roundToWorkgroupPower(int length, int workgroup) {
		if (length % workgroup == 0) {
			return length;
		} else {
			return workgroup * (length / workgroup + 1);
		}
	}

	/**
	 * Adds the listener.
	 * 
	 * @param activeContourRenderer
	 *            the active contour renderer
	 */
	public void addListener(FrameUpdateListener activeContourRenderer) {
		listeners.add(activeContourRenderer);
	}

	/**
	 * Contains overlaps.
	 * 
	 * @return true, if successful
	 */
	public boolean containsOverlaps() {
		return containsOverlaps;
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		if (context != null) {
			context.release();
			context = null;
		}
	}

	/**
	 * Gets the cols.
	 * 
	 * @return the cols
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * Gets the elapsed time.
	 * 
	 * @return the elapsed time
	 */
	public double getElapsedTime() {
		// TODO Auto-generated method stub
		return elapsedTime * 1E-9;
	}

	/**
	 * Gets the label masks.
	 * 
	 * @return the label masks
	 */
	public int[] getLabelMasks() {
		return labelMasks;
	}

	/**
	 * Gets the max iterations.
	 * 
	 * @return the max iterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Gets the num colors.
	 * 
	 * @return the num colors
	 */
	public int getNumColors() {
		if (containsOverlaps) {
			return numObjects;
		} else {
			return numLabels;
		}
	}

	/**
	 * Gets the level set.
	 * 
	 * @return the level set
	 */

	public int getNumLabels() {
		return numLabels;
	}

	/**
	 * Gets the num objects.
	 * 
	 * @return the num objects
	 */
	public int getNumObjects() {
		return numObjects;
	}

	/**
	 * Gets the pressure image.
	 * 
	 * @return the pressure image
	 */
	public ImageDataFloat getPressureImage() {
		return pressureImage;
	}

	/**
	 * Gets the reference image.
	 * 
	 * @return the reference image
	 */
	public ImageData getReferenceImage() {
		return image;
	}

	/**
	 * Gets the rows.
	 * 
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Gets the vector field image.
	 * 
	 * @return the vector field image
	 */
	public ImageDataFloat getVectorFieldImage() {
		return vecFieldImage;
	}

	/**
	 * Checks if is dirty.
	 * 
	 * @return true, if is dirty
	 */
	public final boolean isDirty() {
		return dirty;
	}

	/**
	 * Save delta image.
	 * 
	 * @param clampSpeed
	 *            the new clamp speed
	 */

	public void setClampSpeed(boolean clampSpeed) {
		this.clampSpeed = clampSpeed;
	}

	/**
	 * Sets the curvature weight.
	 * 
	 * @param weight
	 *            the new curvature weight
	 */
	public void setCurvatureWeight(float weight) {
		this.curvatureWeight = weight;
	}

	/**
	 * Sets the dice threshold.
	 * 
	 * @param dice
	 *            the new dice threshold
	 */
	public void setDiceThreshold(double dice) {
		this.diceThreshold = dice;
	}

	/**
	 * Sets the dirty.
	 * 
	 * @param dirty
	 *            the new dirty
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * Sets the outer iterations.
	 * 
	 * @param outerIterations
	 *            the new outer iterations
	 */
	public void setMaxIterations(int outerIterations) {
		this.maxIterations = outerIterations;
	}

	/**
	 * Sets the preserve topology.
	 * 
	 * @param preserveTopology
	 *            the new preserve topology
	 */
	public void setPreserveTopology(boolean preserveTopology) {
		this.topologyPreservation = preserveTopology;
	}

	/**
	 * Sets the pressure.
	 * 
	 * @param pressureImage
	 *            the pressure image
	 * @param weight
	 *            the weight
	 */
	public void setPressure(ImageDataFloat pressureImage, float weight) {
		this.pressureImage = pressureImage;
		this.pressureWeight = weight;
	}

	/**
	 * Sets the resampling rate.
	 * 
	 * @param rate
	 *            the new resampling rate
	 */
	public void setResamplingRate(int rate) {
		this.resamplingInterval = rate;
	}

	/**
	 * Sets the target pressure.
	 * 
	 * @param pressure
	 *            the new target pressure
	 */
	public void setTargetPressure(float pressure) {
		this.targetPressure = pressure;
	}

	/**
	 * Sets the vector field.
	 * 
	 * @param vecFieldImage
	 *            the vec field image
	 * @param weight
	 *            the weight
	 */
	public void setVectorField(ImageDataFloat vecFieldImage, float weight) {
		this.vecFieldImage = vecFieldImage;
		this.vecFieldWeight = weight;
	}
}
