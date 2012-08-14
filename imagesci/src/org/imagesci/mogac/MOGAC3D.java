/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.mogac;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;

import javax.vecmath.Point3i;

import org.imagesci.gac.TopologyPreservationRule3D;
import org.imagesci.utility.PhantomMetasphere;
import org.imagesci.utility.RandomSphereCollection;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationMOGAC3D;
import edu.jhu.ece.iacl.jist.io.NIFTIReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class MOGAC3D is an implementation of Multi-Object Geodesic Active
 * Contours 3D.
 */
public class MOGAC3D extends AbstractCalculation {
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
		public void frameUpdate(long time, double fps);
	}

	/** The WORKGROUP size. */
	public static int WORKGROUP_SIZE = 256;

	/** The clamp speed. */
	protected boolean clampSpeed = false;

	/** The contains overlaps. */
	protected boolean containsOverlaps = false;

	/** The context. */
	public CLContext context;

	/** The curvature weight. */
	protected float curvatureWeight = 0;
	/** The delta level set buffer. */
	public CLBuffer<FloatBuffer> deltaLevelSetBuffer = null;

	/** The dice threshold. */
	protected double diceThreshold = 0.995;
	/** The dirty bit. */
	protected boolean dirty = true;
	/** The unsigned level set buffer. */
	public CLBuffer<FloatBuffer> distanceFieldBuffer = null;

	/** The distance field. */
	protected float[][][] distField;
	/** The distance field image. */
	protected ImageDataFloat distFieldImage;
	/** The elapsed time. */
	protected long elapsedTime = 0;

	/** The force indexes. */
	protected int[] forceIndexes;

	/** The label offset buffer. */
	public CLBuffer<IntBuffer> forceIndexesBuffer = null;

	/** The id buffer. */
	public CLBuffer<IntBuffer> idBuffer = null;

	/** The reference image. */
	protected ImageData image = null;

	/** The label buffer. */
	public CLBuffer<IntBuffer> imageLabelBuffer = null;

	// public int final
	/** The kernel map. */
	public Map<String, CLKernel> kernelMap;

	/** The label image. */
	protected ImageDataInt labelImage;

	/** The label mask buffer. */
	public CLBuffer<IntBuffer> labelMaskBuffer = null;

	/** The label masks. */
	protected int[] labelMasks;

	/** The labels. */
	protected int[][][] labels;
	/** The last start time. */
	protected long lastStartTime = 0;

	/** The listeners. */
	protected LinkedList<FrameUpdateListener> listeners = new LinkedList<FrameUpdateListener>();

	/** The maximum number of overlapping objects. */
	protected final int MAX_OBJECTS = 32;

	/** The outer iterations. */
	protected int maxIterations = 100;

	/** The max layers. */
	protected final int MAX_LAYERS = 3;

	/** The max speed. */
	protected float maxSpeed = 0.999f;;

	/** The max temporary buffer. */
	CLBuffer<FloatBuffer> maxTmpBuffer = null;

	/** The STRIDE. */
	protected int numLabels;

	/** The number of objects. */
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

	/** The resampling rate. */
	private int resamplingRate = 8;

	/** The slices. */
	public int rows, cols, slices;

	/** The step elapsed time. */
	protected long stepElapsedTime = 0;

	/** The target pressure. */
	protected float targetPressure = 0;

	/** The time. */
	protected long time;

	/** The topology rule. */
	protected boolean topologyPreservation = false;

	/** The topology rule buffer buffer. */
	public CLBuffer<ByteBuffer> topologyRuleBuffer;

	/** The vector field buffer. */
	public CLBuffer<FloatBuffer> vecFieldBuffer = null;

	/** The vector field image. */
	protected ImageDataFloat vecFieldImage = null;

	/** The vector field weight. */
	protected float vecFieldWeight = 0;

	/**
	 * Instantiates a new mOGA c3 d.
	 * 
	 * @param refImage
	 *            the reference image
	 */
	public MOGAC3D(ImageData refImage) {
		this(refImage, CLDevice.Type.GPU);
	}

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 3D.
	 * 
	 * @param refImage
	 *            the reference image
	 * @param context
	 *            the context
	 * @param queue
	 *            the queue
	 */
	public MOGAC3D(ImageData refImage, CLContext context, CLCommandQueue queue) {
		this.image = refImage;
		this.context = context;
		this.queue = queue;
	}

	/**
	 * Instantiates a new Multi-Object Geodesic Active Contour 3D.
	 * 
	 * @param refImage
	 *            the reference image
	 * @param type
	 *            the type
	 */
	public MOGAC3D(ImageData refImage, CLDevice.Type type) {
		this(type);
		this.image = refImage;
	}

	public MOGAC3D(CLDevice.Type type) {
		CLPlatform[] platforms = CLPlatform.listCLPlatforms();
		CLDevice device = null;
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
		System.out.println("MOGAC 3D using device: " + device.getVendor() + " "
				+ device.getVersion() + " " + device.getName());

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
			time = 0;
			long startTime = lastStartTime = System.nanoTime();

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
			context = null;
			markCompleted();
			return distFieldImage;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Initialize the OpenCL device.
	 * 
	 * @param labelImage
	 *            the label image
	 * @param containsOverlaps
	 *            the contains overlaps
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void init(ImageDataInt labelImage, boolean containsOverlaps)
			throws IOException {
		init(null, labelImage, containsOverlaps);
	}

	/**
	 * Initialize the OpenCL device.
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
		slices = labelImage.getSlices();
		this.containsOverlaps = containsOverlaps;
		int mask = 0x00000001;
		int l = 0;
		this.labels = labelImage.toArray3d();
		if (containsOverlaps) {
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			int totalMask = 0;
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						l = labels[i][j][k];
						totalMask |= l;
						labelHash.add(l);
					}
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
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						l = labels[i][j][k];
						labelHash.add(l);
					}
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
				forceIndexes[i] = i - 1;
			}
		}
		CLProgram program = context.createProgram(
				getClass().getResourceAsStream("MogacEvolveLevelSet3D.cl"))
				.build(define("ROWS", rows), define("COLS", cols),
						define("CONTAINS_OVERLAPS", containsOverlaps),
						define("CLAMP_SPEED", clampSpeed ? 1 : 0),
						define("SLICES", slices),
						define("NUM_LABELS", numLabels));

		kernelMap = program.createCLKernels();
		imageLabelBuffer = context.createIntBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);
		oldImageLabelBuffer = context.createIntBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);
		IntBuffer label = imageLabelBuffer.getBuffer();
		IntBuffer oldLabel = oldImageLabelBuffer.getBuffer();
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					int lab = labels[i][j][k];
					if (i == 0 || j == 0 || k == 0 || i == rows - 1
							|| j == cols - 1 || k == slices - 1) {
						lab = 0;
					}
					label.put(lab);
					oldLabel.put(lab);
				}
			}
		}
		label.rewind();
		oldLabel.rewind();
		queue.putWriteBuffer(imageLabelBuffer, true).putWriteBuffer(
				oldImageLabelBuffer, true);
		oldDistanceFieldBuffer = context.createFloatBuffer(
				rows * cols * slices, READ_WRITE, USE_BUFFER);
		if (unsignedImage == null) {

			convertLabelsToLevelSet();
			unsignedImage = new ImageDataFloat(rows, cols, slices);
			unsignedImage.setName(image.getName() + "_distfield");
			queue.putReadBuffer(distanceFieldBuffer, true);
			FloatBuffer buff = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray3d();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						float val = distField[i][j][k];
						if (i == 0 || j == 0 || k == 0 || i == rows - 1
								|| j == cols - 1 || k == slices - 1) {
							val = Math.max(val, 3);
							distField[i][j][k] = Math.max(buff.get(), 0);
						} else {
							distField[i][j][k] = buff.get();
						}
						oldUnsignedLevelSet.put(val);
					}
				}
			}
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(oldDistanceFieldBuffer, true);
			buff.rewind();
		} else {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols
					* slices, READ_WRITE, USE_BUFFER);

			FloatBuffer unsignedLevelSet = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray3d();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						float val = distField[i][j][k];
						if (i == 0 || j == 0 || k == 0 || i == rows - 1
								|| j == cols - 1 || k == slices - 1) {
							val = Math.max(val, 3);
						}
						unsignedLevelSet.put(val);
						oldUnsignedLevelSet.put(val);
					}
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
			rescale(pressureImage.toArray3d());
			float[][][] pressure = pressureImage.toArray3d();
			pressureBuffer = context.createFloatBuffer(rows * cols * slices,
					READ_ONLY);
			FloatBuffer buff = pressureBuffer.getBuffer();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						buff.put(pressure[i][j][k]);
					}
				}
			}
			buff.rewind();
			queue.putWriteBuffer(pressureBuffer, true);
		}
		if (vecFieldImage != null) {
			float[][][][] vecField = vecFieldImage.toArray4d();
			vecFieldBuffer = context.createFloatBuffer(
					rows * cols * slices * 3, READ_ONLY);
			FloatBuffer advectBuff = vecFieldBuffer.getBuffer();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						advectBuff.put(vecField[i][j][k][0]);
						advectBuff.put(vecField[i][j][k][1]);
						advectBuff.put(vecField[i][j][k][2]);
					}
				}
			}
			advectBuff.rewind();
			queue.putWriteBuffer(vecFieldBuffer, true);
		}
		idBuffer = context.createIntBuffer(rows * cols * slices * 7,
				READ_WRITE, USE_BUFFER);
		deltaLevelSetBuffer = context.createFloatBuffer(rows * cols * slices
				* 7, READ_WRITE, USE_BUFFER);
		queue.putWriteBuffer(labelMaskBuffer, true).putWriteBuffer(
				forceIndexesBuffer, true);
		this.distFieldImage = unsignedImage;
		this.labelImage = labelImage;
		if (image != null) {
			this.labelImage.setName(image.getName() + "_labels");
			distFieldImage.setName(image.getName() + "_distfield");
		} else {
			this.labelImage.setName("labels");
			distFieldImage.setName("distfield");

		}
		setTotalUnits(maxIterations / getResamplingRate());

		if (topologyPreservation) {
			loadLUT626();
		}
		finish();
	}

	/**
	 * Convert labels to level set.
	 */
	public void convertLabelsToLevelSet() {
		final CLKernel labelsToLevelSet = kernelMap.get("labelsToLevelSet");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		if (distanceFieldBuffer == null) {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols
					* slices, USE_BUFFER, READ_WRITE);
		}

		labelsToLevelSet.putArgs(imageLabelBuffer, oldImageLabelBuffer,
				distanceFieldBuffer, oldDistanceFieldBuffer).rewind();
		queue.put1DRangeKernel(labelsToLevelSet, 0, roundToWorkgroupPower(rows
				* cols * slices), WORKGROUP_SIZE);
		for (int i = 1; i <= 2 * MAX_LAYERS; i++) {
			extendDistanceField
					.putArgs(oldDistanceFieldBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(i).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0,
					roundToWorkgroupPower(rows * cols * slices), WORKGROUP_SIZE);
		}
		queue.putReadBuffer(distanceFieldBuffer, true);
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
		final CLKernel maxTimeStep = kernelMap.get("maxTimeStep");
		final CLKernel applyForces = (!topologyPreservation) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel extendDistanceField = kernelMap
				.get("extendDistanceField");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");
		final int global_size = roundToWorkgroupPower(rows * cols * slices);
		if (pressureBuffer != null) {
			if (vecFieldBuffer != null) {
				pressureVecFieldSpeedKernel
						.putArgs(pressureBuffer, vecFieldBuffer,
								oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer, labelMaskBuffer,
								forceIndexesBuffer).putArg(pressureWeight)
						.putArg(vecFieldWeight).putArg(curvatureWeight)
						.rewind();
				queue.put1DRangeKernel(pressureVecFieldSpeedKernel, 0,
						global_size, WORKGROUP_SIZE);
			} else {
				pressureSpeedKernel
						.putArgs(pressureBuffer, oldDistanceFieldBuffer,
								oldImageLabelBuffer, deltaLevelSetBuffer,
								idBuffer, labelMaskBuffer, forceIndexesBuffer)
						.putArg(pressureWeight).putArg(curvatureWeight)
						.rewind();
				queue.put1DRangeKernel(pressureSpeedKernel, 0, global_size,
						WORKGROUP_SIZE);
			}
		} else {
			vecFieldSpeedKernel
					.putArgs(vecFieldBuffer, oldDistanceFieldBuffer,
							oldImageLabelBuffer, deltaLevelSetBuffer, idBuffer,
							labelMaskBuffer, forceIndexesBuffer)
					.putArg(vecFieldWeight).putArg(curvatureWeight).rewind();
			queue.put1DRangeKernel(vecFieldSpeedKernel, 0, global_size,
					WORKGROUP_SIZE);
		}
		if (topologyPreservation) {
			if (!clampSpeed) {
				// Find max
				if (maxTmpBuffer == null) {
					maxTmpBuffer = context.createFloatBuffer(rows * cols,
							READ_WRITE, USE_BUFFER);
				}
				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.rewind();
				queue.put1DRangeKernel(maxImageValue, 0,
						roundToWorkgroupPower(rows * cols), WORKGROUP_SIZE);
				maxTimeStep.putArg(maxTmpBuffer).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 8, 8);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(0)
						.putArg(0).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(0)
						.putArg(1).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(1)
						.putArg(1).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(1)
						.putArg(0).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(0)
						.putArg(0).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(0)
						.putArg(1).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(1)
						.putArg(1).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								maxTmpBuffer, topologyRuleBuffer).putArg(1)
						.putArg(0).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

			} else {
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(0)
						.putArg(0).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(0)
						.putArg(1).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(1)
						.putArg(1).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(1)
						.putArg(0).putArg(0).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(0)
						.putArg(0).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(0)
						.putArg(1).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(1)
						.putArg(1).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);

				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer,
								topologyRuleBuffer).putArg(0.5f).putArg(1)
						.putArg(0).putArg(1).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size / 8,
						WORKGROUP_SIZE);
			}
		} else {
			if (!clampSpeed) {
				// Find max
				if (maxTmpBuffer == null) {
					maxTmpBuffer = context.createFloatBuffer(rows * cols,
							READ_WRITE, USE_BUFFER);
				}
				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.rewind();

				queue.put1DRangeKernel(maxImageValue, 0,
						roundToWorkgroupPower(rows * cols), WORKGROUP_SIZE);
				maxTimeStep.putArg(maxTmpBuffer).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 1, 1);
				applyForces.putArgs(oldDistanceFieldBuffer,
						oldImageLabelBuffer, deltaLevelSetBuffer, idBuffer,
						distanceFieldBuffer, imageLabelBuffer, maxTmpBuffer)
						.rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size,
						WORKGROUP_SIZE);
			} else {
				applyForces
						.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
								deltaLevelSetBuffer, idBuffer,
								distanceFieldBuffer, imageLabelBuffer)
						.putArg(0.5f).rewind();
				queue.put1DRangeKernel(applyForces, 0, global_size,
						WORKGROUP_SIZE);
			}
		}
		for (int i = 1; i <= MAX_LAYERS; i++) {
			extendDistanceField
					.putArgs(oldDistanceFieldBuffer, distanceFieldBuffer,
							imageLabelBuffer).putArg(i).rewind();
			queue.put1DRangeKernel(extendDistanceField, 0, global_size,
					WORKGROUP_SIZE);
		}

		queue.finish();
		// saveLevelSetAndLabelImages();
		copyBuffers.putArgs(oldDistanceFieldBuffer, oldImageLabelBuffer,
				distanceFieldBuffer, imageLabelBuffer).rewind();
		queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
		// contours = null;
		dirty = true;
		if (time % getResamplingRate() == 0) {
			queue.finish();
			long tmp = System.nanoTime();
			for (FrameUpdateListener updater : listeners) {
				updater.frameUpdate(time, 1E9 * getResamplingRate()
						/ (tmp - lastStartTime));
			}
			lastStartTime = tmp;
			incrementCompletedUnits();
		}
		time++;
		// rebuild
		return true;
	}

	/**
	 * Round to workgroup power.
	 * 
	 * @param length
	 *            the length
	 * @return the workgroup size
	 */
	public static int roundToWorkgroupPower(int length) {
		if (length % WORKGROUP_SIZE == 0) {
			return length;
		} else {
			return WORKGROUP_SIZE * (length / WORKGROUP_SIZE + 1);
		}
	}

	/**
	 * Rescale.
	 * 
	 * @param pressureForce
	 *            the pressure force
	 */
	protected void rescale(float[][][] pressureForce) {
		int index = 0;
		double min = Float.MAX_VALUE;
		double max = Float.MIN_VALUE;
		if (!Float.isNaN(targetPressure)) {
			for (int i = 0; i < pressureForce.length; i++) {
				for (int j = 0; j < pressureForce[0].length; j++) {
					for (int k = 0; k < pressureForce[0][0].length; k++) {
						double val = pressureForce[i][j][k] - targetPressure;
						min = Math.min(val, min);
						max = Math.max(val, max);
						index++;
					}
				}
			}
			double normMin = (Math.abs(min) > 1E-4) ? 1 / Math.abs(min) : 1;
			double normMax = (Math.abs(max) > 1E-4) ? 1 / Math.abs(max) : 1;
			for (int i = 0; i < pressureForce.length; i++) {
				for (int j = 0; j < pressureForce[0].length; j++) {
					for (int k = 0; k < pressureForce[0][0].length; k++) {
						double val = pressureForce[i][j][k] - targetPressure;
						if (val < 0) {
							pressureForce[i][j][k] = (float) (val * normMin);
						} else {
							pressureForce[i][j][k] = (float) (val * normMax);
						}
					}
				}
			}
		}
	}

	/**
	 * Gets the resampling rate.
	 * 
	 * @return the resampling rate
	 */
	public int getResamplingRate() {
		return resamplingRate;
	}

	/**
	 * Load lu t626.
	 * 
	 * @return true, if successful
	 */
	protected boolean loadLUT626() {
		return loadLUT(TopologyPreservationRule3D.class
				.getResourceAsStream("connectivity6_26.zip"));
	}

	/**
	 * Load the look-up table.
	 * 
	 * @param fis
	 *            the fis
	 * @return true, if successful
	 */
	private boolean loadLUT(InputStream fis) {
		final int BUFFER = 4096;
		try {
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			if ((zis.getNextEntry()) != null) {
				int index = 0;
				int count = 0;
				byte[] buff = new byte[(2 << 24)];
				while ((count = zis.read(buff, index,
						Math.min(BUFFER, buff.length - index))) > 0) {
					index += count;
				}
				topologyRuleBuffer = context.createByteBuffer(buff.length,
						READ_ONLY);
				topologyRuleBuffer.getBuffer().put(buff).rewind();
				queue.putWriteBuffer(topologyRuleBuffer, true);
				return true;
			}
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					distField[i][j][k] = buff.get();
					labels[i][j][k] = buff2.get();
				}
			}
		}
		buff.rewind();
		buff2.rewind();
		dirty = false;
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
		RandomSphereCollection rando = new RandomSphereCollection(128, 128,
				128, 27, 15);
		PhantomMetasphere metasphere = new PhantomMetasphere(new Point3i(128,
				128, 128));
		metasphere.setNoiseLevel(0.1);
		metasphere.setFuzziness(0.5f);
		metasphere.setInvertImage(true);
		metasphere.solve();
		ImageDataFloat refImage = metasphere.getImage();

		ImageDataFloat initDistField = rando.getDistanceField();
		ImageDataInt initLabels = rando.getLabelImage();
		MOGAC3D activeContour = new MOGAC3D(refImage, CLDevice.Type.GPU);
		activeContour.setPressure(refImage, 0.5f);
		activeContour.setCurvatureWeight(1.0f);
		activeContour.setTargetPressure(0.5f);
		activeContour.setMaxIterations(620);
		activeContour.setClampSpeed(true);
		if (showGUI) {
			try {
				activeContour.init(initDistField, initLabels, false);

				VisualizationMOGAC3D visual = new VisualizationMOGAC3D(512,
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
				e.printStackTrace();
			}
		} else {
			activeContour.solve(initDistField, initLabels, true);
		}
	}

	/**
	 * Round to workgroup power.
	 * 
	 * @param length
	 *            the length
	 * @param workgroup
	 *            the workgroup size
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
		image = null;
		labelImage = null;

	}

	/**
	 * Evolve.
	 * 
	 * @return the double
	 */
	public double evolve() {
		return 0;
	}

	/**
	 * Evolve.
	 * 
	 * @param check
	 *            the check
	 * @return the double
	 */
	public double evolve(boolean check) {
		return 0;
	}

	/**
	 * Gets the elapsed time.
	 * 
	 * @return the elapsed time
	 */
	public double getElapsedTime() {
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
	 * Gets the number of colors.
	 * 
	 * @return the number of colors
	 */
	public int getNumColors() {
		return (labelMasks == null) ? 0 : labelMasks[labelMasks.length - 1] + 1;
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
	 * Gets the number of objects.
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
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public long getTime() {
		return time;
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
	 * Save delta image.
	 * 
	 * @param clamp
	 *            the new clamp speed
	 */

	public void setClampSpeed(boolean clamp) {
		this.clampSpeed = clamp;
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
		this.resamplingRate = rate;
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
	 *            the vector field image
	 * @param weight
	 *            the weight
	 */
	public void setVectorField(ImageDataFloat vecFieldImage, float weight) {
		this.vecFieldImage = vecFieldImage;
		this.vecFieldWeight = weight;
	}

	public void setReferenceImage(ImageData image) {
		this.image = image;
		this.rows = image.getRows();
		this.cols = image.getCols();
		this.slices = image.getSlices();

	}

	public void setLabelImage(ImageDataInt labelImage) {
		setImageSegmentation(labelImage, null);
	}

	public void setDistanceFieldImage(ImageDataFloat unsignedImage) {
		if (distanceFieldBuffer != null)
			distanceFieldBuffer.release();
		if (oldDistanceFieldBuffer != null)
			oldDistanceFieldBuffer.release();
		distanceFieldBuffer = context.createFloatBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);

		FloatBuffer unsignedLevelSet = distanceFieldBuffer.getBuffer();
		FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer.getBuffer();
		this.distField = unsignedImage.toArray3d();
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					float val = distField[i][j][k];
					if (i == 0 || j == 0 || k == 0 || i == rows - 1
							|| j == cols - 1 || k == slices - 1) {
						val = Math.max(val, 3);
					}
					unsignedLevelSet.put(val);
					oldUnsignedLevelSet.put(val);
				}
			}
		}
		unsignedLevelSet.rewind();
		oldUnsignedLevelSet.rewind();
		queue.putWriteBuffer(distanceFieldBuffer, true).putWriteBuffer(
				oldDistanceFieldBuffer, true);
		this.distFieldImage = unsignedImage;
		if (image != null) {
			distFieldImage.setName(image.getName() + "_distfield");
		} else {
			distFieldImage.setName("distfield");
		}
	}

	public void setImageSegmentation(ImageDataInt labelImage,
			ImageDataFloat unsignedImage) {
		this.rows = image.getRows();
		this.cols = image.getCols();
		this.slices = image.getSlices();
		this.labelImage = labelImage;
		int mask = 0x00000001;
		int l = 0;
		this.labels = labelImage.toArray3d();
		if (containsOverlaps) {
			TreeSet<Integer> labelHash = new TreeSet<Integer>();
			int totalMask = 0;
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						l = labels[i][j][k];
						totalMask |= l;
						labelHash.add(l);
					}
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
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						l = labels[i][j][k];
						labelHash.add(l);
					}
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
				forceIndexes[i] = i - 1;
			}
		}
		if (imageLabelBuffer != null)
			imageLabelBuffer.release();
		if (oldImageLabelBuffer != null)
			oldImageLabelBuffer.release();
		imageLabelBuffer = context.createIntBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);
		oldImageLabelBuffer = context.createIntBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);
		IntBuffer label = imageLabelBuffer.getBuffer();
		IntBuffer oldLabel = oldImageLabelBuffer.getBuffer();
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					int lab = labels[i][j][k];
					if (i == 0 || j == 0 || k == 0 || i == rows - 1
							|| j == cols - 1 || k == slices - 1) {
						lab = 0;
					}
					label.put(lab);
					oldLabel.put(lab);
				}
			}
		}
		label.rewind();
		oldLabel.rewind();
		queue.putWriteBuffer(imageLabelBuffer, true).putWriteBuffer(
				oldImageLabelBuffer, true);
		if (distanceFieldBuffer != null)
			distanceFieldBuffer.release();
		if (oldDistanceFieldBuffer != null)
			oldDistanceFieldBuffer.release();
		oldDistanceFieldBuffer = context.createFloatBuffer(
				rows * cols * slices, READ_WRITE, USE_BUFFER);
		init();
		if (unsignedImage == null) {
			convertLabelsToLevelSet();
			unsignedImage = new ImageDataFloat(rows, cols, slices);
			unsignedImage.setName(image.getName() + "_distfield");
			queue.putReadBuffer(distanceFieldBuffer, true);
			FloatBuffer buff = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray3d();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						float val = distField[i][j][k];
						if (i == 0 || j == 0 || k == 0 || i == rows - 1
								|| j == cols - 1 || k == slices - 1) {
							val = Math.max(val, 3);
							distField[i][j][k] = Math.max(buff.get(), 0);
						} else {
							distField[i][j][k] = buff.get();
						}
						oldUnsignedLevelSet.put(val);
					}
				}
			}
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(oldDistanceFieldBuffer, true);
			buff.rewind();
		} else {
			distanceFieldBuffer = context.createFloatBuffer(rows * cols
					* slices, READ_WRITE, USE_BUFFER);

			FloatBuffer unsignedLevelSet = distanceFieldBuffer.getBuffer();
			FloatBuffer oldUnsignedLevelSet = oldDistanceFieldBuffer
					.getBuffer();
			this.distField = unsignedImage.toArray3d();
			for (int k = 0; k < slices; k++) {
				for (int j = 0; j < cols; j++) {
					for (int i = 0; i < rows; i++) {
						float val = distField[i][j][k];
						if (i == 0 || j == 0 || k == 0 || i == rows - 1
								|| j == cols - 1 || k == slices - 1) {
							val = Math.max(val, 3);
						}
						unsignedLevelSet.put(val);
						oldUnsignedLevelSet.put(val);
					}
				}
			}
			unsignedLevelSet.rewind();
			oldUnsignedLevelSet.rewind();
			queue.putWriteBuffer(distanceFieldBuffer, true).putWriteBuffer(
					oldDistanceFieldBuffer, true);
		}
		if (labelMaskBuffer != null)
			labelMaskBuffer.release();
		if (forceIndexesBuffer != null)
			forceIndexesBuffer.release();
		if (deltaLevelSetBuffer != null)
			deltaLevelSetBuffer.release();
		if (idBuffer != null)
			idBuffer.release();

		labelMaskBuffer = context.createIntBuffer(labelMasks.length, READ_ONLY,
				USE_BUFFER);
		labelMaskBuffer.getBuffer().put(labelMasks).rewind();
		forceIndexesBuffer = context.createIntBuffer(forceIndexes.length,
				READ_ONLY, USE_BUFFER);
		forceIndexesBuffer.getBuffer().put(forceIndexes).rewind();

		idBuffer = context.createIntBuffer(rows * cols * slices * 7,
				READ_WRITE, USE_BUFFER);
		deltaLevelSetBuffer = context.createFloatBuffer(rows * cols * slices
				* 7, READ_WRITE, USE_BUFFER);
		queue.putWriteBuffer(labelMaskBuffer, true).putWriteBuffer(
				forceIndexesBuffer, true);
		System.out.println("SET LABEL IMAGE " + getNumColors());
		this.distFieldImage = unsignedImage;
		this.labelImage = labelImage;

		if (image != null) {
			this.labelImage.setName(image.getName() + "_labels");
			distFieldImage.setName(image.getName() + "_distfield");
		} else {
			this.labelImage.setName("labels");
			distFieldImage.setName("distfield");
		}
	}

	public void init() {
		CLProgram program;
		try {
			program = context.createProgram(
					getClass().getResourceAsStream("MogacEvolveLevelSet3D.cl"))
					.build(define("ROWS", rows), define("COLS", cols),
							define("CONTAINS_OVERLAPS", containsOverlaps),
							define("CLAMP_SPEED", clampSpeed ? 1 : 0),
							define("SLICES", slices),
							define("NUM_LABELS", numLabels));

			kernelMap = program.createCLKernels();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setVectorFieldWeight(float f) {
		this.vecFieldWeight = f;
	}

	public void setPressureWeight(float f) {
		this.pressureWeight = f;
	}
}
