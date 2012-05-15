/**
 * ImageSci Toolkit
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
package org.imagesci.gac;

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

import org.imagesci.springls.ActiveContour2D;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

import data.PlaceHolder;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationGAC2D;
import edu.jhu.ece.iacl.jist.io.PImageReaderWriter;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class WEGAC2D is a Work-Efficient implementation of Geodesic Active
 * Contours 2D
 */
public class WEGAC2D extends ActiveContour2D {

	/** The Constant STRIDE. */
	protected static final int STRIDE = 128;

	/** The WORKGROUP SIZE. */
	public static int WORKGROUP_SIZE = STRIDE;

	/** The active list array size. */
	public int activeListArraySize;

	/** The active list buffer. */
	public CLBuffer<IntBuffer> activeListBuffer;

	/** The active list size. */
	public int activeListSize;

	/** The clamp speed flag. */
	protected boolean clampSpeed = false;

	/** The context. */
	public CLContext context;

	/** The delta level set buffer. */
	public CLBuffer<FloatBuffer> deltaLevelSetBuffer = null;
	/** The distance field. */
	protected float[][] distField;
	/** The distance field image. */
	protected ImageDataFloat distFieldImage;

	/** The elapsed time. */
	protected long elapsedTime = 0;
	/** The kernel map. */
	public Map<String, CLKernel> kernelMap;

	/** The last start time. */
	protected long lastStartTime = 0;
	/** The outer iterations. */

	/** The max distance. */
	final float MAX_DISTANCE = 3.5f;

	/** The max temporary buffer. */
	protected CLBuffer<FloatBuffer> maxTmpBuffer = null;

	/** The max value buffer. */
	protected CLBuffer<IntBuffer> maxValueBuffer = null;

	/** The offset buffer. */
	protected CLBuffer<IntBuffer> offsetBuffer = null;

	/** The old unsigned level set buffer. */
	public CLBuffer<FloatBuffer> oldSignedLevelSetBuffer = null;

	/** The pressure buffer. */
	public CLBuffer<FloatBuffer> pressureBuffer = null;

	/** The command queue. */
	public CLCommandQueue queue;

	/** The unsigned level set buffer. */
	public CLBuffer<FloatBuffer> signedLevelSetBuffer = null;

	/** The temporary active buffer. */
	protected CLBuffer<IntBuffer> tmpActiveBuffer = null;

	/** The vector field buffer. */
	public CLBuffer<FloatBuffer> vecFieldBuffer = null;

	/**
	 * Instantiates a new Work-Efficient Geodesic Active Contour 2D
	 */
	public WEGAC2D() {
		this(CLDevice.Type.CPU);
	}

	/**
	 * Instantiates a new Work-Efficient Geodesic Active Contour 2D
	 * 
	 * @param context
	 *            the context
	 * @param queue
	 *            the queue
	 */
	public WEGAC2D(CLContext context, CLCommandQueue queue) {
		super();
		this.context = context;
		this.queue = queue;
	}

	/**
	 * Instantiates a new Work-Efficient Geodesic Active Contour 2D
	 * 
	 * @param type
	 *            the device type
	 */
	public WEGAC2D(CLDevice.Type type) {
		super();
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
			WORKGROUP_SIZE = STRIDE;
		} else if (type == CLDevice.Type.GPU) {
			WORKGROUP_SIZE = 128;

		}
		context = CLContext.create(device);
		queue = device.createCommandQueue();
		System.out.println("Geodesic Active Contour using device: "
				+ device.getVendor() + " " + device.getVersion() + " "
				+ device.getName());

	}

	/**
	 * Initializes the OpenCL device.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void init(int rows, int cols) throws IOException {
		this.rows = rows;
		this.cols = cols;
		CLProgram program = context.createProgram(
				getClass().getResourceAsStream("WEEvolveLevelSet2D.cl")).build(
				define("ROWS", rows), define("COLS", cols),
				define("CLAMP_SPEED", clampSpeed ? 1 : 0),
				define("STRIDE", STRIDE),
				define("MAX_DISTANCE", MAX_DISTANCE + "f"));

		kernelMap = program.createCLKernels();

		signedLevelSetBuffer = context.createFloatBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);
		oldSignedLevelSetBuffer = context.createFloatBuffer(rows * cols,
				READ_WRITE, USE_BUFFER);

		FloatBuffer signedLevelSet = signedLevelSetBuffer.getBuffer();
		FloatBuffer oldSignedLevelSet = oldSignedLevelSetBuffer.getBuffer();
		this.distField = initialLevelSetImage.toArray2d();
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				float val = Math.min(
						Math.max(distField[i][j], -(maxLayers + 1)),
						(maxLayers + 1));
				signedLevelSet.put(val);
				oldSignedLevelSet.put(val);
			}
		}
		signedLevelSet.rewind();
		oldSignedLevelSet.rewind();
		queue.putWriteBuffer(signedLevelSetBuffer, true).putWriteBuffer(
				oldSignedLevelSetBuffer, true);

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
		}
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

		this.distFieldImage = initialLevelSetImage;
		finish();
		CLDevice.Type type = queue.getDevice().getType();
		if (type == CLDevice.Type.CPU) {
			WORKGROUP_SIZE = STRIDE;
		} else if (type == CLDevice.Type.GPU) {
			WORKGROUP_SIZE = 128;

		}
		rebuildNarrowBand();
	}

	/**
	 * Solve.
	 * 
	 */
	@Override
	public void solve() {
		try {
			init();
			queue.finish();
			time = 0;
			setTotalUnits(maxIterations / resamplingInterval);
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
					.printf("Time Steps: %d\nElapsed Time: %4.4f sec\nFrame Rate: %4.2f fps\n",
							time, 1E-9 * (endTime - startTime), 1E9 * time
									/ (endTime - startTime));
			finish();
			context.release();
			context = null;
			markCompleted();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finish.
	 */
	public void finish() {
		queue.putReadBuffer(signedLevelSetBuffer, true);
		FloatBuffer buff = signedLevelSetBuffer.getBuffer();
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				distField[i][j] = buff.get();
			}
		}
		buff.rewind();
	}

	/**
	 * Step.
	 * 
	 * @return true, if successful
	 */
	@Override
	public boolean step() {
		final CLKernel pressureSpeedKernel = kernelMap
				.get("pressureSpeedKernel");
		final CLKernel vecFieldSpeedKernel = kernelMap
				.get("vecFieldSpeedKernel");
		final CLKernel pressureVecFieldSpeedKernel = kernelMap
				.get("pressureVecFieldSpeedKernel");
		final CLKernel maxImageValue = kernelMap.get("maxImageValue");
		final CLKernel maxTimeStep = kernelMap.get("maxTimeStep");
		final CLKernel applyForces = (!preserveTopology) ? kernelMap
				.get("applyForces") : kernelMap.get("applyForcesTopoRule");
		final CLKernel updateDistanceField = kernelMap
				.get("updateDistanceField");
		final CLKernel copyBuffers = kernelMap.get("copyBuffers");
		int global_size = roundToWorkgroupPower(activeListSize);
		System.nanoTime();
		if (pressureBuffer != null) {
			if (vecFieldBuffer != null) {
				pressureVecFieldSpeedKernel
						.putArgs(activeListBuffer, pressureBuffer,
								vecFieldBuffer, oldSignedLevelSetBuffer,
								deltaLevelSetBuffer).putArg(pressureWeight)
						.putArg(advectionWeight).putArg(curvatureWeight)
						.putArg(activeListSize).rewind();
				queue.put1DRangeKernel(pressureVecFieldSpeedKernel, 0,
						global_size, WORKGROUP_SIZE);
			} else {
				pressureSpeedKernel
						.putArgs(activeListBuffer, pressureBuffer,
								oldSignedLevelSetBuffer, deltaLevelSetBuffer)
						.putArg(pressureWeight).putArg(curvatureWeight)
						.putArg(activeListSize).rewind();
				queue.put1DRangeKernel(pressureSpeedKernel, 0, global_size,
						WORKGROUP_SIZE);
			}
		} else {
			vecFieldSpeedKernel
					.putArgs(activeListBuffer, vecFieldBuffer,
							oldSignedLevelSetBuffer, deltaLevelSetBuffer)
					.putArg(advectionWeight).putArg(curvatureWeight)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(vecFieldSpeedKernel, 0, global_size,
					WORKGROUP_SIZE);
		}
		if (preserveTopology) {
			if (!clampSpeed) {
				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.putArg(activeListSize).rewind();
				queue.put1DRangeKernel(
						maxImageValue,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE),
								WORKGROUP_SIZE / 8), WORKGROUP_SIZE / 8);
				maxTimeStep.putArg(maxTmpBuffer)
						.putArg(1 + (activeListSize / STRIDE)).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 1, 1);
				for (int nn = 0; nn < 4; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
									deltaLevelSetBuffer, signedLevelSetBuffer,
									maxTmpBuffer).putArg(activeListSize)
							.putArg(nn).rewind();
					queue.put1DRangeKernel(applyForces, 0, global_size,
							WORKGROUP_SIZE);
				}
			} else {
				for (int nn = 0; nn < 4; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
									deltaLevelSetBuffer, signedLevelSetBuffer)
							.putArg(0.5f).putArg(activeListSize).putArg(nn)
							.rewind();
					queue.put1DRangeKernel(applyForces, 0, global_size,
							WORKGROUP_SIZE);
				}
			}
		} else {
			if (!clampSpeed) {
				maxImageValue.putArgs(deltaLevelSetBuffer, maxTmpBuffer)
						.putArg(activeListSize).rewind();
				queue.put1DRangeKernel(
						maxImageValue,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE),
								WORKGROUP_SIZE / 8), WORKGROUP_SIZE / 8);
				maxTimeStep.putArg(maxTmpBuffer)
						.putArg(1 + (activeListSize / STRIDE)).rewind();
				queue.put1DRangeKernel(maxTimeStep, 0, 1, 1);
				applyForces
						.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
								deltaLevelSetBuffer, signedLevelSetBuffer,
								maxTmpBuffer).putArg(activeListSize).rewind();
			} else {
				applyForces
						.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
								deltaLevelSetBuffer, signedLevelSetBuffer)
						.putArg(0.5f).putArg(activeListSize).rewind();
			}
			queue.put1DRangeKernel(applyForces, 0, global_size, WORKGROUP_SIZE);

		}

		for (int i = 1; i <= maxLayers; i++) {
			updateDistanceField
					.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
							signedLevelSetBuffer).putArg(i)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(updateDistanceField, 0, global_size,
					WORKGROUP_SIZE);
		}

		final CLKernel plugLevelSet = kernelMap.get("plugLevelSet");
		plugLevelSet.putArgs(activeListBuffer, signedLevelSetBuffer)
				.putArg(activeListSize).rewind();
		queue.put1DRangeKernel(plugLevelSet, 0, global_size, WORKGROUP_SIZE);
		copyBuffers
				.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
						signedLevelSetBuffer).putArg(activeListSize).rewind();
		queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
		queue.finish();
		deleteElements();
		addElements();
		if (activeListSize == 0) {
			return false;
		}
		if (time % getResamplingInterval() == 0) {
			queue.finish();
			long tmp = System.nanoTime();
			for (FrameUpdateListener updater : listeners) {
				updater.frameUpdate((int) time, 1E9 * getResamplingInterval()
						/ (tmp - lastStartTime));
			}
			lastStartTime = tmp;
			incrementCompletedUnits();
		}
		time++;
		return true;
	}

	/**
	 * Adds elements to the active list.
	 * 
	 * @return the number of added elements.
	 */
	protected int addElements() {
		final CLKernel addCountActiveList = kernelMap.get("addCountActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel expandActiveList = kernelMap.get("expandActiveList");
		for (int nn = 0; nn < 4; nn++) {
			addCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							signedLevelSetBuffer).putArg(activeListSize)
					.putArg(nn).rewind();
			queue.put1DRangeKernel(addCountActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		}
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(4 * (1 + (activeListSize / STRIDE))).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		int addElements = maxValueBuffer.getBuffer().get(0);
		int newElements = addElements + activeListSize;
		if (newElements != activeListSize) {
			if (newElements > activeListArraySize) {

				rebuildNarrowBand();
				return addElements;
			}

			for (int nn = 0; nn < 4; nn++) {
				expandActiveList
						.putArgs(offsetBuffer, activeListBuffer,
								oldSignedLevelSetBuffer, signedLevelSetBuffer)
						.putArg(activeListSize).putArg(nn).rewind();
				queue.put1DRangeKernel(
						expandActiveList,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1),
						1);
			}
		}
		activeListSize = newElements;
		queue.finish();
		return addElements;
	}

	/**
	 * Rebuild narrow band.
	 */
	public void rebuildNarrowBand() {
		final CLKernel countActiveList = kernelMap.get("countActiveList");
		final CLKernel buildActiveList = kernelMap.get("buildActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		if (maxValueBuffer == null) {
			maxValueBuffer = context.createIntBuffer(1, READ_WRITE, USE_BUFFER);
		}
		CLBuffer<IntBuffer> rebuildOffsetBuffer = context.createIntBuffer(cols,
				READ_WRITE, USE_BUFFER);
		countActiveList.putArgs(rebuildOffsetBuffer, oldSignedLevelSetBuffer,
				signedLevelSetBuffer).rewind();

		queue.put1DRangeKernel(countActiveList, 0,
				roundToWorkgroupPower(cols, WORKGROUP_SIZE / 4),
				WORKGROUP_SIZE / 4);
		prefixScanList.putArgs(rebuildOffsetBuffer, maxValueBuffer)
				.putArg(cols).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);

		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		activeListSize = maxValueBuffer.getBuffer().get(0);
		activeListArraySize = (int) Math.min(rows * cols,
				Math.max(activeListSize * 1.25f, Math.ceil(rows * cols * 0.1)));
		System.out.println("Building narrowband with " + activeListSize
				+ " active voxels out of " + activeListArraySize
				+ " total voxels. ");
		if (activeListBuffer != null) {
			activeListBuffer.release();
		}
		activeListBuffer = context.createIntBuffer(activeListArraySize,
				READ_WRITE, USE_BUFFER);

		buildActiveList.putArgs(rebuildOffsetBuffer, activeListBuffer,
				oldSignedLevelSetBuffer).rewind();
		queue.put1DRangeKernel(buildActiveList, 0, cols, 2);

		queue.finish();

		rebuildOffsetBuffer.release();

		if (offsetBuffer != null) {
			offsetBuffer.release();
		}
		offsetBuffer = context.createIntBuffer(
				4 * (1 + (activeListArraySize / STRIDE)), USE_BUFFER,
				READ_WRITE);

		if (maxTmpBuffer != null) {
			maxTmpBuffer.release();
		}
		maxTmpBuffer = context.createFloatBuffer(
				1 + (activeListArraySize / STRIDE), READ_WRITE, USE_BUFFER);

		if (tmpActiveBuffer != null) {
			tmpActiveBuffer.release();
		}

		tmpActiveBuffer = context.createIntBuffer(activeListArraySize,
				USE_BUFFER, READ_WRITE);

		if (deltaLevelSetBuffer != null) {
			deltaLevelSetBuffer.release();
		}
		deltaLevelSetBuffer = context.createFloatBuffer(activeListArraySize,
				USE_BUFFER, READ_WRITE);

	}

	/**
	 * Delete elements from the active list.
	 * 
	 * @return the number of deleted elements.
	 */
	protected int deleteElements() {
		final CLKernel deleteCountActiveList = kernelMap
				.get("deleteCountActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = kernelMap.get("compactActiveList");

		deleteCountActiveList
				.putArgs(offsetBuffer, activeListBuffer,
						oldSignedLevelSetBuffer).putArg(activeListSize)
				.rewind();
		queue.put1DRangeKernel(deleteCountActiveList, 0,
				roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		queue.finish();
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		int newElements = maxValueBuffer.getBuffer().get(0);
		int delete = activeListSize - newElements;

		if (newElements != activeListSize) {

			compactActiveList
					.putArgs(offsetBuffer, activeListBuffer, tmpActiveBuffer,
							oldSignedLevelSetBuffer, signedLevelSetBuffer)
					.putArg(activeListSize).rewind();

			queue.put1DRangeKernel(compactActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);

			activeListSize = newElements;
			queue.finish();
			CLBuffer<IntBuffer> tmp = activeListBuffer;
			activeListBuffer = tmpActiveBuffer;
			tmpActiveBuffer = tmp;

		}
		return delete;
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
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static final void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		File ftarget;
		try {
			ftarget = new File(PlaceHolder.class.getResource("target.png")
					.toURI());

			File fsource = new File(PlaceHolder.class.getResource("source.png")
					.toURI());

			ImageDataFloat sourceImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(
							fsource));

			ImageDataFloat refImage = PImageReaderWriter
					.convertToGray(PImageReaderWriter.getInstance().read(
							ftarget));

			DistanceField2D df = new DistanceField2D();
			float[][] img = sourceImage.toArray2d();
			int r = img.length;
			int c = img[0].length;
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < c; j++) {
					img[i][j] -= 127.5f;
				}
			}
			ImageDataFloat initImage = df.solve(sourceImage, 15.0);

			WEGAC2D simulator = new WEGAC2D(CLDevice.Type.CPU);
			simulator.setTargetPressure(230.0f);
			simulator.setPreserveTopology(false);
			simulator.setCurvatureWeight(0.2f);
			simulator.setMaxIterations(400);
			simulator.setClampSpeed(true);
			simulator.setReferenceImage(refImage);
			simulator.setPressure(refImage, -1.0f);
			simulator.setInitialDistanceFieldImage(initImage);
			simulator.setReferencelevelSetImage(refImage);
			if (showGUI) {
				try {
					simulator.init();
					VisualizationGAC2D visual = new VisualizationGAC2D(600,
							600, simulator);
					VisualizationApplication app = new VisualizationApplication(
							visual);
					app.setMinimumSize(new Dimension(1024, 768));
					app.setShowToolBar(true);
					app.addListener(visual);
					app.runAndWait();
					visual.dispose();
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				simulator.solve();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.ActiveContour2D#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.ActiveContour2D#dispose()
	 */
	@Override
	public void dispose() {
		context.release();
	}

	/**
	 * Gets the level set.
	 * 
	 * @return the level set
	 */
	@Override
	public ImageDataFloat getSignedLevelSet() {
		return distFieldImage;
	}

	/**
	 * Sets the clsamp speed.
	 * 
	 * @param clamp
	 *            the new clsamp speed
	 */
	public void setClampSpeed(boolean clamp) {
		this.clampSpeed = clamp;
	}

}
