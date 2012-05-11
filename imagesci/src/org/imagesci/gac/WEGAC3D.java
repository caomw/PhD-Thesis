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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import org.imagesci.springls.ActiveContour3D;
import org.imagesci.utility.PhantomBubbles;
import org.imagesci.utility.PhantomCube;
import org.imagesci.utility.PhantomMetasphere;
import org.imagesci.utility.PhantomTorus;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationGAC3D;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class MGACOpenCL3D.
 */
public class WEGAC3D extends ActiveContour3D {

	/** The Constant STRIDE. */
	protected static final int STRIDE = 128;

	/** The WORKGROUP SIZE. */
	public static int WORKGROUP_SIZE = 256;

	/** The active list array size. */
	public int activeListArraySize;

	/** The active list buffer. */
	public CLBuffer<IntBuffer> activeListBuffer;

	/** The active list size. */
	public int activeListSize;

	/** The max tmp buffer. */
	protected boolean adaptiveConvergence = false;
	/** The adaptive convergence sampling interval. */
	protected int adaptiveConvergenceSamplingInterval = 16;
	/** The clamp speed. */
	protected boolean clampSpeed = false;
	/** The context. */
	public CLContext context;
	/** The delta level set buffer. */
	public CLBuffer<FloatBuffer> deltaLevelSetBuffer = null;
	/** The dirty. */
	boolean dirty = true;
	/** The dist field. */
	protected float[][][] distField;
	/** The dist field image. */
	protected ImageDataFloat distFieldImage;
	/** The history buffer. */
	protected CLBuffer<ByteBuffer> historyBuffer = null;

	/** The kernel map. */
	public Map<String, CLKernel> kernelMap;
	/** The last start time. */
	protected long lastStartTime = 0;
	/** The MA x_ distance. */
	final float MAX_DISTANCE = 3.5f;

	/** The max tmp buffer. */
	protected CLBuffer<FloatBuffer> maxTmpBuffer = null;

	/** The max value buffer. */
	protected CLBuffer<IntBuffer> maxValueBuffer = null;

	/** The offset buffer. */
	protected CLBuffer<IntBuffer> offsetBuffer = null;

	/** The old unsigned level set buffer. */
	public CLBuffer<FloatBuffer> oldSignedLevelSetBuffer = null;

	/** The pressure buffer. */
	public CLBuffer<FloatBuffer> pressureBuffer = null;

	/** The queue. */
	public CLCommandQueue queue;

	/** The unsigned level set buffer. */
	public CLBuffer<FloatBuffer> signedLevelSetBuffer = null;

	/** The tmp active buffer. */
	protected CLBuffer<IntBuffer> tmpActiveBuffer = null;

	/** The topology rule buffer buffer. */
	public CLBuffer<ByteBuffer> topologyRuleBuffer;

	/** The vec field buffer. */
	public CLBuffer<FloatBuffer> vecFieldBuffer = null;

	/**
	 * Instantiates a new wEGA c3 d.
	 */
	public WEGAC3D() {
		this(CLDevice.Type.CPU);
	}

	/**
	 * Instantiates a new mGAC open c l3 d.
	 * 
	 * @param type
	 *            the type
	 */
	public WEGAC3D(CLDevice.Type type) {
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
			WORKGROUP_SIZE = 256;
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
	 * Instantiates a new mGAC open c l3 d.
	 * 
	 * @param refImage
	 *            the ref image
	 * @param context
	 *            the context
	 * @param queue
	 *            the queue
	 */
	public WEGAC3D(ImageData refImage, CLContext context, CLCommandQueue queue) {
		super();
		this.referenceImage = refImage;
		this.context = context;
		this.queue = queue;
	}

	/**
	 * Inits the.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void init(int rows, int cols, int slices) throws IOException {
		rows = this.rows;
		cols = this.cols;
		slices = this.slices;
		CLProgram program = context.createProgram(
				getClass().getResourceAsStream("WEEvolveLevelSet3D.cl")).build(
				define("ROWS", rows), define("COLS", cols),
				define("SLICES", slices),
				define("CLAMP_SPEED", clampSpeed ? 1 : 0),
				define("STRIDE", STRIDE), define("MAX_DISTANCE", MAX_DISTANCE));

		kernelMap = program.createCLKernels();

		signedLevelSetBuffer = context.createFloatBuffer(rows * cols * slices,
				READ_WRITE, USE_BUFFER);
		oldSignedLevelSetBuffer = context.createFloatBuffer(rows * cols
				* slices, READ_WRITE, USE_BUFFER);

		FloatBuffer signedLevelSet = signedLevelSetBuffer.getBuffer();
		FloatBuffer oldSignedLevelSet = oldSignedLevelSetBuffer.getBuffer();
		this.distField = initialDistanceFieldImage.toArray3d();
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					float val = Math.min(
							Math.max(distField[i][j][k], -(maxLayers + 1)),
							(maxLayers + 1));
					signedLevelSet.put(val);
					oldSignedLevelSet.put(val);
				}
			}
		}
		signedLevelSet.rewind();
		oldSignedLevelSet.rewind();
		queue.putWriteBuffer(signedLevelSetBuffer, true).putWriteBuffer(
				oldSignedLevelSetBuffer, true);

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

		this.distFieldImage = initialDistanceFieldImage;
		if (preserveTopology) {
			loadLUT626();
		}
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
	 * Load lu t626.
	 * 
	 * @return true, if successful
	 */
	protected boolean loadLUT626() {
		return loadLUT(TopologyPreservationRule3D.class
				.getResourceAsStream("connectivity6_26.zip"));
	}

	/**
	 * Load lut.
	 * 
	 * @param f
	 *            the f
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
	 * Solve.
	 * 
	 * @return the image data float
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
					.printf("Time Steps: %d\nElapsed Time: %6.4f sec\nFrame Rate: %6.2f fps\n",
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
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					distField[i][j][k] = buff.get();
				}
			}
		}
		buff.rewind();
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
				for (int nn = 0; nn < 8; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
									deltaLevelSetBuffer, signedLevelSetBuffer,
									maxTmpBuffer, topologyRuleBuffer)
							.putArg(activeListSize).putArg(nn).rewind();

					queue.put1DRangeKernel(applyForces, 0, global_size,
							WORKGROUP_SIZE);
				}
			} else {
				for (int nn = 0; nn < 8; nn++) {
					applyForces
							.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
									deltaLevelSetBuffer, signedLevelSetBuffer,
									topologyRuleBuffer).putArg(0.5f)
							.putArg(activeListSize).putArg(nn).rewind();
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
		if (!preserveTopology) {
			final CLKernel plugLevelSet = kernelMap.get("plugLevelSet");
			plugLevelSet.putArgs(activeListBuffer, signedLevelSetBuffer)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(plugLevelSet, 0, global_size, WORKGROUP_SIZE);
		}
		if (adaptiveConvergence) {
			copyBuffers
					.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
							signedLevelSetBuffer).putArg(activeListSize)
					.rewind();
			queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
			final CLKernel rememberImageLabels = kernelMap
					.get("rememberImageLabels");
			final CLKernel diffImageLabels = kernelMap.get("diffImageLabels");
			if ((time) % adaptiveConvergenceSamplingInterval == 0) {
				rememberImageLabels
						.putArgs(signedLevelSetBuffer, historyBuffer).rewind();
				queue.put1DRangeKernel(rememberImageLabels, 0,
						roundToWorkgroupPower(rows * cols * slices),
						WORKGROUP_SIZE);
			} else if ((time) % adaptiveConvergenceSamplingInterval == adaptiveConvergenceSamplingInterval - 1) {

				diffImageLabels.putArgs(signedLevelSetBuffer, historyBuffer)
						.rewind();

				queue.put1DRangeKernel(diffImageLabels, 0,
						roundToWorkgroupPower(rows * cols * slices),
						WORKGROUP_SIZE);
				final CLKernel dilateLabels = kernelMap.get("dilateLabels");

				for (int cycle = 0; cycle < 4; cycle++) {
					for (int kk = 0; kk < 8; kk++) {
						dilateLabels.putArgs(activeListBuffer, historyBuffer)
								.putArg(activeListSize).putArg(kk).rewind();
						queue.put1DRangeKernel(dilateLabels, 0, global_size,
								WORKGROUP_SIZE);
					}
				}
			}
		} else {
			copyBuffers
					.putArgs(activeListBuffer, oldSignedLevelSetBuffer,
							signedLevelSetBuffer).putArg(activeListSize)
					.rewind();
			queue.put1DRangeKernel(copyBuffers, 0, global_size, WORKGROUP_SIZE);
		}
		queue.finish();

		dirty = true;
		deleteElements();
		addElements();
		if (activeListSize == 0) {
			return false;
		}
		if (time % getResamplingRate() == 0) {
			// saveLevelSet();
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

		return true;
	}

	/**
	 * Adds the elements.
	 * 
	 * @return the int
	 */
	protected int addElements() {
		final CLKernel addCountActiveList = kernelMap.get("addCountActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel expandActiveList = kernelMap.get("expandActiveList");
		for (int nn = 0; nn < 6; nn++) {
			addCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							signedLevelSetBuffer).putArg(activeListSize)
					.putArg(nn).rewind();
			queue.put1DRangeKernel(addCountActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		}
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(6 * (1 + (activeListSize / STRIDE))).rewind();
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

			for (int nn = 0; nn < 6; nn++) {
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
		CLBuffer<IntBuffer> rebuildOffsetBuffer = context.createIntBuffer(
				slices, READ_WRITE, USE_BUFFER);
		countActiveList.putArgs(rebuildOffsetBuffer, oldSignedLevelSetBuffer,
				signedLevelSetBuffer).rewind();

		queue.put1DRangeKernel(countActiveList, 0,
				roundToWorkgroupPower(slices, WORKGROUP_SIZE / 4),
				WORKGROUP_SIZE / 4);
		prefixScanList.putArgs(rebuildOffsetBuffer, maxValueBuffer)
				.putArg(slices).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);

		queue.finish();
		if (historyBuffer == null && adaptiveConvergence) {
			historyBuffer = context.createByteBuffer(rows * cols * slices,
					READ_WRITE, USE_BUFFER);
		}
		queue.putReadBuffer(maxValueBuffer, true);
		activeListSize = maxValueBuffer.getBuffer().get(0);
		activeListArraySize = (int) Math.min(
				rows * cols * slices,
				Math.max(activeListSize * 1.25f,
						Math.ceil(rows * cols * slices * 0.1)));
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
		queue.put1DRangeKernel(buildActiveList, 0, slices, 2);

		queue.finish();

		rebuildOffsetBuffer.release();

		if (offsetBuffer != null) {
			offsetBuffer.release();
		}
		offsetBuffer = context.createIntBuffer(
				6 * (1 + (activeListArraySize / STRIDE)), USE_BUFFER,
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
	 * Delete elements.
	 * 
	 * @return the int
	 */
	protected int deleteElements() {
		final CLKernel deleteCountActiveList = kernelMap
				.get("deleteCountActiveList");
		final CLKernel prefixScanList = kernelMap.get("prefixScanList");
		final CLKernel compactActiveList = kernelMap.get("compactActiveList");
		final CLKernel deleteCountActiveListHistory = kernelMap
				.get("deleteCountActiveListHistory");
		final CLKernel compactActiveListHistory = kernelMap
				.get("compactActiveListHistory");

		if (adaptiveConvergence
				&& time % adaptiveConvergenceSamplingInterval == adaptiveConvergenceSamplingInterval - 1) {
			deleteCountActiveListHistory
					.putArgs(offsetBuffer, activeListBuffer,
							oldSignedLevelSetBuffer, historyBuffer)
					.putArg(activeListSize).rewind();
			queue.put1DRangeKernel(deleteCountActiveListHistory, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		} else {

			deleteCountActiveList
					.putArgs(offsetBuffer, activeListBuffer,
							oldSignedLevelSetBuffer).putArg(activeListSize)
					.rewind();
			queue.put1DRangeKernel(deleteCountActiveList, 0,
					roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1), 1);
		}
		queue.finish();
		prefixScanList.putArgs(offsetBuffer, maxValueBuffer)
				.putArg(1 + (activeListSize / STRIDE)).rewind();
		queue.put1DRangeKernel(prefixScanList, 0, 1, 1);
		queue.finish();
		queue.putReadBuffer(maxValueBuffer, true);
		int newElements = maxValueBuffer.getBuffer().get(0);
		int delete = activeListSize - newElements;

		if (newElements != activeListSize) {

			if (adaptiveConvergence
					&& time > adaptiveConvergenceSamplingInterval
					&& time % adaptiveConvergenceSamplingInterval == adaptiveConvergenceSamplingInterval - 1) {
				compactActiveListHistory
						.putArgs(offsetBuffer, activeListBuffer,
								tmpActiveBuffer, oldSignedLevelSetBuffer,
								signedLevelSetBuffer, historyBuffer)
						.putArg(activeListSize).rewind();

				queue.put1DRangeKernel(
						compactActiveListHistory,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1),
						1);
			} else {

				compactActiveList
						.putArgs(offsetBuffer, activeListBuffer,
								tmpActiveBuffer, oldSignedLevelSetBuffer,
								signedLevelSetBuffer).putArg(activeListSize)
						.rewind();

				queue.put1DRangeKernel(
						compactActiveList,
						0,
						roundToWorkgroupPower(1 + (activeListSize / STRIDE), 1),
						1);

			}
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
	public static final void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0 && args[0].equalsIgnoreCase("-nogui")) {
			showGUI = false;
		}
		System.out.println("Starting Geodesic Active Contour test ...");
		System.out
				.println("Did you remember to install the latest OpenCL drivers for your GPU and CPU?");
		Point3i dims = new Point3i(128, 128, 128);
		/*
		PhantomTorus phantom = new PhantomTorus(dims);
		phantom.solve();
		ImageDataFloat levelset = phantom.getLevelset();
		PhantomMetasphere metasphere = new PhantomMetasphere(dims);
		metasphere.setNoiseLevel(0.1);
		metasphere.setFuzziness(0.5f);
		metasphere.setInvertImage(true);
		metasphere.solve();
		ImageDataFloat pressureImage = metasphere.getImage();
		*/

		PhantomCube phantom = new PhantomCube(new Point3i(128, 128, 128));
		phantom.setCenter(new Point3d(0, 0, 0));
		phantom.setWidth(1.21);
		phantom.solve();
		ImageDataFloat levelset = phantom.getLevelset();

		PhantomBubbles bubbles = new PhantomBubbles(new Point3i(128, 128, 128));
		bubbles.setNoiseLevel(0);
		bubbles.setNumberOfBubbles(12);
		bubbles.setFuzziness(0.5f);
		bubbles.setMinRadius(0.2);
		bubbles.setMaxRadius(0.3);
		bubbles.setInvertImage(true);
		bubbles.solve();
		ImageDataFloat pressureImage = bubbles.getImage();

		WEGAC3D gac = new WEGAC3D();
		gac.setPreserveTopology(false);// Turn off adaptive convergence if you
										// use topology preservation
		gac.setPressure(pressureImage, 0.5f);
		gac.setReferenceImage(pressureImage);
		gac.setInitialDistanceFieldImage(levelset);
		gac.setCurvatureWeight(0.5f);
		gac.setMaxIterations(200);
		gac.setTargetPressure(0.5f);
		gac.setClampSpeed(true);// This is for speed enhancement only, it may
								// cause bad things to happen if not used
								// correctly.
		gac.setAdaptiveConvergence(true);
		if (showGUI) {
			try {
				gac.init();
				VisualizationGAC3D vis = new VisualizationGAC3D(512, 512, gac);
				VisualizationApplication app = new VisualizationApplication(vis);
				app.setPreferredSize(new Dimension(920, 650));
				app.setShowToolBar(true);
				app.addListener(vis);
				app.runAndWait();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			gac.solve();
		}

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.ActiveContour3D#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.cisst.algorithms.springls.ActiveContour3D#dispose()
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
	 * Sets the adaptive convergence.
	 * 
	 * @param adaptive
	 *            the new adaptive convergence
	 */
	public void setAdaptiveConvergence(boolean adaptive) {
		this.adaptiveConvergence = adaptive;
	}

	/**
	 * Sets the adaptive convergence sampling interval.
	 * 
	 * @param interval
	 *            the new adaptive convergence sampling interval
	 */
	public void setAdaptiveConvergenceSamplingInterval(int interval) {
		this.adaptiveConvergenceSamplingInterval = interval;
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

	/**
	 * Sets the curvature weight.
	 * 
	 * @param weight
	 *            the new curvature weight
	 */
	@Override
	public void setCurvatureWeight(float weight) {
		this.curvatureWeight = weight;
	}

	/**
	 * Sets the outer iterations.
	 * 
	 * the new outer iterations
	 * 
	 * @param outerIterations
	 *            the new max iterations
	 */
	@Override
	public void setMaxIterations(int outerIterations) {
		this.maxIterations = outerIterations;
	}

	/**
	 * Sets the pressure.
	 * 
	 * @param pressureImage
	 *            the pressure image
	 * @param weight
	 *            the weight
	 */
	@Override
	public void setPressure(ImageDataFloat pressureImage, float weight) {
		this.pressureImage = pressureImage;
		this.pressureWeight = weight;
	}

	/**
	 * Sets the target pressure.
	 * 
	 * @param pressure
	 *            the new target pressure
	 */
	@Override
	public void setTargetPressure(float pressure) {
		this.targetPressure = pressure;
	}
}
