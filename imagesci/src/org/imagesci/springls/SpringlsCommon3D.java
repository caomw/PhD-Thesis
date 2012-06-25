/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
package org.imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.define;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;
import static java.lang.System.out;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.vecmath.Point3f;

import org.imagesci.gac.TopologyPreservationRule3D;
import org.imagesci.utility.IsoSurfaceGenerator;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.SpringlsRaycastRenderer;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * The Class SpringlsCommon contains all global data structures that need to be
 * shared across OpenCL kernels.
 */
public class SpringlsCommon3D {
	/** The Constant ADD_TO_VOLUME. */
	public static final String ADD_TO_VOLUME = "addToVolume";
	/** The Constant ADVECT_ENRIGHT. */
	public static final String ADVECT_ENRIGHT = "advectEnright";
	/** The Constant ADVECT_ZALESAK. */
	public static final String ADVECT_ZALESAK = "advectZalesak";

	/** The Constant APPLY_FORCES. */
	public static final String APPLY_FORCES = "applyForces";
	/** The Constant APPLY_FORCES. */
	public static final String APPLY_FORCES_TOPO_RULE = "applyForcesTopoRule";

	/** The Constant APPLY_UPDATES. */
	public static final String APPLY_UPDATES = "applyUpdates";

	/** The Constant COMPUTE_FORCES_PRESSURE. */
	public static final String COMPUTE_FORCES_PRESSURE = "computeAdvectionForcesP";

	/** The Constant COMPUTE_FORCES_PRESSURE_VECFIELD. */
	public static final String COMPUTE_FORCES_PRESSURE_VECFIELD = "computeAdvectionForcesPandV";

	/** The Constant COMPUTE_FORCES_VECFIELD. */
	public static final String COMPUTE_FORCES_VECFIELD = "computeAdvectionForcesV";

	/** The Constant COMPUTE_GRADIENT. */
	public static final String COMPUTE_GRADIENT = "computeGradient";

	/** The Constant COMPUTE_MAX_FORCES. */
	public static final String COMPUTE_MAX_FORCES = "computeMaxForces";

	/** The Constant CONTRACT_ARRAY. */
	public static final String CONTRACT_ARRAY = "contractArray";

	/** The Constant CONTRACT_COUNT. */
	public static final String CONTRACT_COUNT = "contractCount";

	/** The Constant CONTRACT_COUNT_WITH_ATLAS. */
	public static final String CONTRACT_COUNT_WITH_ATLAS = "contractCountWithAtlas";

	/** The Constant CONTRACT_COUNT. */
	public static final String CONTRACT_OUTLIERS_COUNT = "contractOutliersCount";

	/** The Constant COPY_ELEMENTS. */
	public static final String COPY_ELEMENTS = "copyElements";

	/** The Constant COUNT_ELEMENTS. */
	public static final String COUNT_ELEMENTS = "countElements";

	/** The Constant CREATE_INDEX_MAP. */
	public static final String CREATE_INDEX_MAP = "createIndexMap";

	/** The Constant EVOLVE_LEVELSET. */
	public static final String EVOLVE_LEVELSET = "evolveLevelSet";

	/** The Constant EVOLVE_LEVELSET_TOPO. */
	public static final String EVOLVE_LEVELSET_TOPO = "evolveLevelSetTopoRule";

	/** The Constant EXPAND_ARRAY. */
	public static final String EXPAND_ARRAY = "expandArray";

	/** The Constant EXPAND_COUNT. */
	public static final String EXPAND_COUNT = "expandCount";

	/** The Constant EXPAND_GAPS. */
	public static final String EXPAND_GAPS = "expandGaps";

	/** The Constant EXTEND_DISTANCE_FIELD. */
	public static final String EXTEND_DISTANCE_FIELD = "extendDistanceField";

	/** The Constant FILL_GAP_COUNT. */
	public static final String FILL_GAP_COUNT = "fillGapCount";

	/** The Constant FIND_CLOSEST_POINTS. */
	public static final String FIND_CLOSEST_POINTS = "findClosestCorrespondencePoints";

	/** The Constant FIX_LABELS. */
	public static final String FIX_LABELS = "fixLabels";

	/** The Constant INIT_INDEX_MAP. */
	public static final String INIT_INDEX_MAP = "initIndexMap";

	/** The Constant ISO_SURF_COUNT. */
	public static final String ISO_SURF_COUNT = "isoSurfCount";

	/** The Constant ISO_SURF_GEN. */
	public static final String ISO_SURF_GEN = "isoSurfGen";

	/** The Constant MAP_NEAREST_NEIGHBORS. */
	public static final String MAP_NEAREST_NEIGHBORS = "mapNearestNeighbors";

	/** The Constant MAX_BIN_SIZE. */
	public static final int MAX_BIN_SIZE = 16;

	/** The Constant NEAREST_NEIGHBORS. */
	public static final String NEAREST_NEIGHBORS = "nearestNeighbors";

	/** The Constant REDUCE_LEVEL_SET. */
	public static final String REDUCE_LEVEL_SET = "reduceLevelSet";

	/** The Constant REDUCE_LEVEL_SET_MESH. */
	public static final String REDUCE_LEVEL_SET_MESH = "reduceLevelSetMesh";

	/** The Constant REDUCE_NEAREST_NEIGHBORS. */
	public static final String REDUCE_NEAREST_NEIGHBORS = "reduceNearestNeighbors";

	/** The Constant RELAX_NEIGHBORS. */
	public static final String RELAX_NEIGHBORS = "relaxNeighbors";

	/** The Constant SORT_NEAREST_NEIGHBORS. */
	public static final String SORT_NEAREST_NEIGHBORS = "sortNearestNeighbors";

	/** The Constant SPLAT_BBOX. */
	public static final String SPLAT_BBOX = "splatBBox";

	/** The Constant SPLAT_BBOX_COUNT. */
	public static final String SPLAT_BBOX_COUNT = "splatBBoxCount";

	/** The Constant SPLAT_BBOX_COUNT_MESH. */
	public static final String SPLAT_BBOX_COUNT_MESH = "splatBBoxCountMesh";

	/** The Constant SPLAT_BBOX_MESH. */
	public static final String SPLAT_BBOX_MESH = "splatBBoxMesh";

	/** The STRIDE. */
	public static final int STRIDE = 128;

	/** The Constant UNSIGNED_TO_SIGNED. */
	public static final String UNSIGNED_TO_SIGNED = "unsignedToSignedLevelSet";

	/** The Constant UPDATE_DISTANCE_FIELD. */
	public static final String UPDATE_DISTANCE_FIELD = "updateDistanceField";

	/** The WORKGROU p_ size. */
	public static int WORKGROUP_SIZE = 512;

	/** The a2i triangle connection table buffer. */
	public CLBuffer<IntBuffer> a2iTriangleConnectionTableBuffer;

	/** The active list array size. */
	public int activeListArraySize;

	/** The active list buffer. */
	public CLBuffer<IntBuffer> activeListBuffer;

	/** The active list size. */
	public int activeListSize;

	/** The ai cube edge flags buffer. */
	public CLBuffer<IntBuffer> aiCubeEdgeFlagsBuffer;

	/** The array length. */
	public int arrayLength;

	/** The capsule buffer. */
	public CLBuffer<ByteBuffer> capsuleBuffer;

	/** The capsule neighbor buffer. */
	public CLBuffer<ByteBuffer> capsuleNeighborBuffer;

	/** The context. */
	public CLContext context;

	/** The DIC e_ stride. */
	public int DICE_STRIDE = 1024;

	/** The elements. */
	public int elements;

	/** The flip. */
	public boolean flip = false;

	/** The index buffer. */
	public CLBuffer<IntBuffer> indexBuffer = null;

	/** The is active set valid. */
	protected volatile boolean isActiveSetValid = false;

	/** The kernel map. */
	public Map<String, CLKernel> kernelMap;

	/** The key buffer. */
	public CLBuffer<IntBuffer> keyBuffer = null;

	/** The map length. */
	public int mapLength;

	/** The original unsigned level set buffer. */
	public CLBuffer<FloatBuffer> originalUnsignedLevelSetBuffer;

	/** The preserve topology. */
	protected boolean preserveTopology = false;

	/** The queue. */
	public CLCommandQueue queue;

	/** The ref image. */
	protected ImageData refImage;

	/** The resampling enabled. */
	protected boolean resamplingEnabled = true;

	/** The slices. */
	public int rows, cols, slices;

	/** The scan. */

	public PrefixScanCPU scan;

	/** The signed level set buffer. */
	public CLBuffer<FloatBuffer> signedLevelSetBuffer;

	/** The spatial look up. */
	public CLBuffer<IntBuffer> spatialLookUp;

	/** The label buffer. */
	public CLBuffer<IntBuffer> springlLabelBuffer;

	/** The topology rule buffer buffer. */
	public CLBuffer<ByteBuffer> topologyRuleBuffer;

	/** The type. */
	protected CLDevice.Type type;

	/** The unsigned level set buffer. */
	public CLBuffer<FloatBuffer> unsignedLevelSetBuffer;

	/** The value buffer. */
	public CLBuffer<IntBuffer> valueBuffer = null;

	/**
	 * Instantiates a new springls common.
	 * 
	 * @param context
	 *            the context
	 * @param queue
	 *            the queue
	 * @param workgroupSize
	 *            the workgroup size
	 * @param type
	 *            the type
	 */
	public SpringlsCommon3D(CLContext context, CLCommandQueue queue,
			int workgroupSize, CLDevice.Type type) {
		this.context = context;
		this.queue = queue;
		this.type = type;
		WORKGROUP_SIZE = workgroupSize;

	}

	/**
	 * Instantiates a new springls common.
	 * 
	 * @param type
	 *            the type
	 */
	public SpringlsCommon3D(CLDevice.Type type) {
		this.type = type;
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
			if (rows < 128 || cols < 128 || slices < 128) {
				WORKGROUP_SIZE = 256;
			} else {
				WORKGROUP_SIZE = 512;
			}
		} else {
			WORKGROUP_SIZE = 128;
		}
		context = CLContext.create(device);
		queue = device.createCommandQueue();
		System.out.println("Springls common using device: "
				+ device.getVendor() + " " + device.getVersion() + " "
				+ device.getName());
	}

	/**
	 * Initialize.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @param preserve
	 *            the preserve
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void initialize(int rows, int cols, int slices, boolean preserve)
			throws IOException {
		initialize(null, rows, cols, slices, preserve);
	}

	/**
	 * Initialize.
	 * 
	 * @param surf
	 *            the surf
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @param preserveTopology
	 *            the preserve topology
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void initialize(SpringlsSurface surf, int rows, int cols,
			int slices, boolean preserveTopology) throws IOException {
		this.preserveTopology = preserveTopology;
		// Allow for 10% extra space in array to estimate maximum amount of hole
		// filling

		if (kernelMap == null) {
			this.rows = rows;
			this.cols = cols;
			this.slices = slices;

			initKernels(queue);
		}
		int IMAGE_SIZE = rows * cols * slices;
		unsignedLevelSetBuffer = context.createFloatBuffer(IMAGE_SIZE,
				READ_WRITE, USE_BUFFER);
		originalUnsignedLevelSetBuffer = context.createFloatBuffer(IMAGE_SIZE,
				READ_WRITE, USE_BUFFER);
		if (surf != null) {
			this.elements = surf.getCapsules().size();
			arrayLength = roundArrayLength(elements);
			mapLength = arrayLength * 32;
			System.out.println("Intializing Springls: " + elements
					+ " elements have been padded to " + arrayLength
					+ " elements");
			capsuleBuffer = context.createByteBuffer(Springl3D.BYTE_SIZE
					* arrayLength, READ_WRITE, USE_BUFFER);
			signedLevelSetBuffer = context.createFloatBuffer(IMAGE_SIZE,
					READ_WRITE, USE_BUFFER);
			springlLabelBuffer = context.createIntBuffer(arrayLength,
					READ_WRITE, USE_BUFFER);
			ByteBuffer buff = capsuleBuffer.getBuffer();
			IntBuffer buff2 = springlLabelBuffer.getBuffer();
			int index = 0;
			for (Springl3D capsule : surf.getCapsules()) {
				capsule.serialize(buff);
				buff2.put(index++);
			}
			buff.rewind();
			buff2.rewind();
			queue.putWriteBuffer(capsuleBuffer, true);
		}
		loadLUT();
	}

	/**
	 * Inits the kernels.
	 * 
	 * @param queue
	 *            the queue
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void initKernels(CLCommandQueue queue) throws IOException {
		CLContext context = queue.getContext();
		CLProgram program;
		program = context
				.createProgram(
						SpringlsCommon3D.class
								.getResourceAsStream("SpringlsNearestNeighbors3D.cl"))
				.build(define("ROWS", rows),
						define("COLS", cols),
						define("SLICES", slices),
						define("MAX_BIN_SIZE", MAX_BIN_SIZE),
						define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
						define("SCALE_DOWN", SpringlsConstants.scaleDown + "f"),
						define("IMAGE_SIZE", rows * cols * slices),
						define("LOCAL_SIZE_LIMIT", WORKGROUP_SIZE * 2),
						define("MAX_VALUE", Integer.MAX_VALUE),
						define("MAX_NEIGHBORS", SpringlsConstants.maxNeighbors),
						define("MAX_RADIUS",
								SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						define("MAX_NEAREST_BINS",
								SpringlsConstants.maxNearestBins),
						define("MAX_VEXT", (SpringlsConstants.vExtent) + "f"),
						define("vExtent", SpringlsConstants.scaleDown
								* SpringlsConstants.vExtent + "f"),
						define("nearestNeighborDistance",
								(float) SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						ENABLE_MAD);
		kernelMap = program.createCLKernels();

		program = context
				.createProgram(
						SpringlsCommon3D.class
								.getResourceAsStream("SpringlsEvolveLevelSet3D.cl"))
				.build(define("ROWS", rows),
						define("COLS", cols),
						define("SLICES", slices),

						define("MAX_BIN_SIZE", MAX_BIN_SIZE),
						define("STRIDE", SpringlsEvolveLevelSet3D.STRIDE),
						define("MAX_DISTANCE",
								SpringlsEvolveLevelSet3D.MAX_DISTANCE),
						define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
						define("SCALE_DOWN", SpringlsConstants.scaleDown + "f"),
						define("IMAGE_SIZE", rows * cols * slices),
						define("LOCAL_SIZE_LIMIT", WORKGROUP_SIZE * 2),
						define("MAX_VALUE", Integer.MAX_VALUE),
						define("MAX_NEIGHBORS", SpringlsConstants.maxNeighbors),
						define("MAX_RADIUS",
								SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						define("REST_LENGTH", SpringlsConstants.restRadius
								+ "f"),
						define("SPRING_CONSTANT",
								SpringlsConstants.springConstant + "f"),
						define("PARTICLE_RADIUS",
								SpringlsConstants.particleRadius
										* SpringlsConstants.scaleDown + "f"),
						define("MAX_NEAREST_BINS",
								SpringlsConstants.maxNearestBins),
						define("MAX_VEXT", (SpringlsConstants.vExtent) + "f"),
						define("vExtent", SpringlsConstants.scaleDown
								* SpringlsConstants.vExtent + "f"),
						define("SHARPNESS", SpringlsConstants.sharpness + "f"),
						define("nearestNeighborDistance",
								(float) SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						ENABLE_MAD);
		kernelMap.putAll(program.createCLKernels());
		/*
		System.out
				.println("WEIGHTING FUNCTION "
						+ SpringlsConstants.WEIGHT_FUNCTION[SpringlsConstants.weightingKernel
								.ordinal()]);
		System.out
				.println("THRESHOLD FUNCTION "
						+ SpringlsConstants.THRESHOLD_FUNCTION[SpringlsConstants.thresholdKernel
								.ordinal()]);
								*/
		program = context
				.createProgram(
						SpringlsCommon3D.class
								.getResourceAsStream("SpringlsUpdateParticles3D.cl"))
				.build(define("ROWS", rows),
						define("COLS", cols),
						define("SLICES", slices),

						define("MAX_BIN_SIZE", MAX_BIN_SIZE),
						define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
						define("SCALE_DOWN", SpringlsConstants.scaleDown + "f"),
						define("IMAGE_SIZE", rows * cols * slices),
						define("LOCAL_SIZE_LIMIT", WORKGROUP_SIZE * 2),
						define("MAX_VALUE", Integer.MAX_VALUE),
						define("MAX_NEIGHBORS", SpringlsConstants.maxNeighbors),
						define("MAX_RADIUS",
								SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						define("REST_LENGTH", SpringlsConstants.restRadius
								+ "f"),
						define("SPRING_CONSTANT",
								SpringlsConstants.springConstant + "f"),
						define("PARTICLE_RADIUS",
								SpringlsConstants.particleRadius
										* SpringlsConstants.scaleDown + "f"),
						define("MAX_NEAREST_BINS",
								SpringlsConstants.maxNearestBins),
						define("MAX_VEXT", (SpringlsConstants.vExtent) + "f"),
						define("vExtent", SpringlsConstants.scaleDown
								* SpringlsConstants.vExtent + "f"),

						define("SHARPNESS", SpringlsConstants.sharpness + "f"),
						define("nearestNeighborDistance",
								(float) SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						define("WEIGHT_FUNC",
								SpringlsConstants.WEIGHT_FUNCTION[SpringlsConstants.weightingKernel
										.ordinal()]),
						define("THRESHOLD_FUNC",
								SpringlsConstants.THRESHOLD_FUNCTION[SpringlsConstants.thresholdKernel
										.ordinal()]), ENABLE_MAD);
		kernelMap.putAll(program.createCLKernels());
		program = context
				.createProgram(
						SpringlsCommon3D.class
								.getResourceAsStream("SpringlsExpandContract3D.cl"))
				.build(define("ROWS", rows),
						define("COLS", cols),
						define("SLICES", slices),

						define("MAX_BIN_SIZE", MAX_BIN_SIZE),
						define("SCALE_UP", SpringlsConstants.scaleUp + "f"),
						define("SCALE_DOWN", SpringlsConstants.scaleDown + "f"),
						define("IMAGE_SIZE", rows * cols * slices),
						define("LOCAL_SIZE_LIMIT", WORKGROUP_SIZE * 2),
						define("MAX_VALUE", Integer.MAX_VALUE),
						define("MAX_NEIGHBORS", SpringlsConstants.maxNeighbors),
						define("MAX_RADIUS",
								SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"),
						define("REST_LENGTH", SpringlsConstants.restRadius
								+ "f"),
						define("SPRING_CONSTANT",
								SpringlsConstants.springConstant + "f"),
						define("PARTICLE_RADIUS",
								SpringlsConstants.particleRadius
										* SpringlsConstants.scaleDown + "f"),
						define("MAX_NEAREST_BINS",
								SpringlsConstants.maxNearestBins),

						define("maxAngleTolerance",
								SpringlsConstants.maxAngleTolerance + "f"),
						define("minAngleTolerance",
								SpringlsConstants.minAngleTolerance + "f"),
						define("maxAreaThreshold",
								SpringlsConstants.maxAreaThreshold + "f"),
						define("MAX_VEXT", (SpringlsConstants.vExtent) + "f"),
						define("vExtent", SpringlsConstants.scaleDown
								* SpringlsConstants.vExtent + "f"),
						define("nearestNeighborDistance",
								(float) SpringlsConstants.nearestNeighborDistance
										* SpringlsConstants.scaleDown + "f"));

		kernelMap.putAll(program.createCLKernels());
		scan = new PrefixScanCPU(queue, 32, 1024);
	}

	/**
	 * Sets the springls.
	 * 
	 * @param surface
	 *            the new springls
	 */
	public void setSpringls(SpringlsSurface surface) {
		this.elements = surface.getCapsules().size();

		arrayLength = roundArrayLength(elements);
		mapLength = arrayLength * 32;
		// System.out.println("Intializing Springls: " + elements+
		// " elements have been padded to " + arrayLength + " elements");
		capsuleBuffer = context.createByteBuffer(Springl3D.BYTE_SIZE
				* arrayLength, READ_WRITE, USE_BUFFER);
		springlLabelBuffer = context.createIntBuffer(arrayLength, READ_WRITE,
				USE_BUFFER);
		ByteBuffer buff = capsuleBuffer.getBuffer();
		IntBuffer buff2 = springlLabelBuffer.getBuffer();
		for (Springl3D capsule : surface.getCapsules()) {
			capsule.serialize(buff);
			buff2.put(capsule.referenceId);
		}
		buff.rewind();
		buff2.rewind();

		queue.putWriteBuffer(capsuleBuffer, true).putWriteBuffer(
				springlLabelBuffer, true);
		// System.out.println("SET SPRINGLS " + elements + " " + arrayLength +
		// " "+ mapLength);
	}

	/**
	 * Sets the springls.
	 * 
	 * @param surfaces
	 *            the surfaces
	 * @param labels
	 *            the labels
	 */
	public void setSpringls(SpringlsSurface[] surfaces, int[] labels) {
		this.elements = 0;
		for (SpringlsSurface surf : surfaces) {
			this.elements += surf.getCapsules().size();
		}
		arrayLength = roundArrayLength(elements);
		mapLength = arrayLength * 32;
		System.out.println("Intializing Springls: " + elements
				+ " elements have been padded to " + arrayLength + " elements");
		capsuleBuffer = context.createByteBuffer(Springl3D.BYTE_SIZE
				* arrayLength, READ_WRITE, USE_BUFFER);
		springlLabelBuffer = context.createIntBuffer(arrayLength, READ_WRITE,
				USE_BUFFER);
		ByteBuffer buff = capsuleBuffer.getBuffer();
		IntBuffer buff2 = springlLabelBuffer.getBuffer();
		int index = 1;
		for (SpringlsSurface surf : surfaces) {
			for (Springl3D capsule : surf.getCapsules()) {
				capsule.serialize(buff);
				buff2.put(labels[index]);
			}
			index++;
		}
		buff.rewind();
		buff2.rewind();

		queue.putWriteBuffer(capsuleBuffer, true).putWriteBuffer(
				springlLabelBuffer, true);
		// System.out.println("SET SPRINGLS " + elements + " " + arrayLength +
		// " "+ mapLength);
	}

	/**
	 * Round array length.
	 * 
	 * @param elements
	 *            the elements
	 * @return the int
	 */
	public int roundArrayLength(int elements) {
		return roundToWorkgroupPower((int) (1.25f * elements));
	}

	/**
	 * Round to worgroup power.
	 * 
	 * @param length
	 *            the length
	 * @return the int
	 */
	public static int roundToWorkgroupPower(int length) {
		if (length > 0 && length % WORKGROUP_SIZE == 0) {
			return length;
		} else {
			return WORKGROUP_SIZE * (length / WORKGROUP_SIZE + 1);
		}
	}

	/**
	 * Sets the connectivity rule.
	 * 
	 */
	private void loadLUT() {
		int[] aiCubeEdgeFlags = null;
		int[][] a2iTriangleConnectionTable = null;
		if (preserveTopology) {
			aiCubeEdgeFlags = IsoSurfaceGenerator.aiCubeEdgeFlagsCC626;
			a2iTriangleConnectionTable = IsoSurfaceGenerator.a2iTriangleConnectionTableCC626;
			loadLUT626();
		} else {
			aiCubeEdgeFlags = IsoSurfaceGenerator.aiCubeEdgeFlagsCC626;
			a2iTriangleConnectionTable = IsoSurfaceGenerator.a2iTriangleConnectionTableCC626;
		}

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
	 * Load LUT for 6/26 connectivity rule.
	 * 
	 * @return true, if successful
	 */
	private boolean loadLUT626() {
		return loadLUT(TopologyPreservationRule3D.class
				.getResourceAsStream("connectivity6_26.zip"));
	}

	/**
	 * Load LUT for 6/18 connectivity rule.
	 * 
	 * @return true, if successful
	 */
	private boolean loadLUT618() {
		return loadLUT(TopologyPreservationRule3D.class
				.getResourceAsStream("connectivity6_18.zip"));

	}

	/**
	 * Load lut.
	 *
	 * @param fis the fis
	 * @return the bit set
	 */
	private boolean loadLUT(InputStream fis) {
		final int BUFFER = 4096;
		try {
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry entry;
			if ((entry = zis.getNextEntry()) != null) {
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
	 * From byte array.
	 * 
	 * @param bytes
	 *            the bytes
	 * 
	 * @return the bit set
	 */
	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}

		return bits;
	}

	/**
	 * Prints the all.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public static void printAll(IntBuffer buffer, int snapshot) {
		int i = 0;
		buffer.rewind();
		while (buffer.hasRemaining()) {
			int val = buffer.get();
			if (i % snapshot == 0) {
				out.print(val + "\n");
			} else {
				out.print(val + " ");
			}
			i++;
		}
		buffer.rewind();
	}

	/**
	 * Prints the max.
	 * 
	 * @param buffer
	 *            the buffer
	 */
	public static void printMax(IntBuffer buffer) {
		int max = 0;
		while (buffer.hasRemaining()) {
			int val = buffer.get();
			if (val != Integer.MAX_VALUE) {
				max = Math.max(max, val);
			}
		}
		out.println("MAX " + max);
		buffer.rewind();
	}

	/**
	 * Prints the snapshot.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public static void printSnapshot(ByteBuffer buffer, int snapshot) {
		for (int i = 0; i < snapshot; i++) {
			out.print(buffer.getFloat() + ", ");
		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Prints the snapshot.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public static void printSnapshot(FloatBuffer buffer, int snapshot) {
		for (int i = 0; i < snapshot; i++) {
			float val = buffer.get();
			out.print(val + ", ");

			if (i % 4 == 3) {
				System.out.println();
			}

		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Prints the snapshot.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public static void printSnapshot(IntBuffer buffer, int snapshot) {
		for (int i = 0; i < snapshot; i++) {
			out.print(buffer.get() + ", ");
		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Prints the snapshot2.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public static void printSnapshot2(FloatBuffer buffer, int snapshot) {
		int i = 0;
		int index = 0;
		while (buffer.hasRemaining() && i < snapshot) {
			float val = buffer.get();
			if (val > 0) {
				out.print("[" + index + ":" + val + "], ");
				i++;
			}
			index++;
		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Prints the snapshot2.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public static void printSnapshot2(IntBuffer buffer, int snapshot) {
		int i = 0;
		int index = 0;
		while (buffer.hasRemaining() && i < snapshot) {
			int val = buffer.get();
			if (val > 0) {
				out.print("[" + index + ":" + val + "], ");
				i++;
			}
			index++;
		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Round to power of two.
	 * 
	 * @param length
	 *            the length
	 * @return the int
	 */
	public static int roundToPowerOfTwo(int length) {
		int padded = 1;
		while (padded < length) {
			padded <<= 1;
		}
		return padded;
	}

	/**
	 * Round to workgroup power.
	 * 
	 * @param length
	 *            the length
	 * @param WORKGROUP_SIZE
	 *            the wORKGROU p_ size
	 * @return the int
	 */
	public static int roundToWorkgroupPower(int length, int WORKGROUP_SIZE) {
		if (length % WORKGROUP_SIZE == 0) {
			return length;
		} else {
			return WORKGROUP_SIZE * (length / WORKGROUP_SIZE + 1);
		}
	}

	/**
	 * Creates the springls ray cast renderer.
	 * 
	 * @param applet
	 *            the applet
	 * @param rasterWidth
	 *            the raster width
	 * @param rasterHeight
	 *            the raster height
	 * @param refreshRate
	 *            the refresh rate
	 * @return the renderer processing3 d
	 */
	public RendererProcessing3D createSpringlsRayCastRenderer(
			VisualizationProcessing3D applet, int rasterWidth,
			int rasterHeight, int refreshRate) {
		return new SpringlsRaycastRenderer(applet, this, rasterWidth,
				rasterHeight, refreshRate);
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		// Please don't crash when cleaning up!
		capsuleBuffer = null;
		signedLevelSetBuffer = null;
		unsignedLevelSetBuffer = null;
		context.release();
		context = null;
	}

	/**
	 * Gets the neighbor references.
	 * 
	 * @return the neighbor references
	 */
	public int[][] getNeighborReferences() {
		int N = elements;
		int[][] capsuleNbrs = new int[N * 3][2 * SpringlsConstants.maxNeighbors];
		queue.putReadBuffer(capsuleNeighborBuffer, true);
		ByteBuffer buff = capsuleNeighborBuffer.getBuffer();
		int id = 0;
		int index = 0;
		boolean foundEnd = false;
		int maxCapsuleId = -1;
		while (buff.hasRemaining()) {
			if (id >= N) {
				break;
			}
			int capsuleId = buff.getInt();
			maxCapsuleId = Math.max(maxCapsuleId, capsuleId);
			int vertexId = buff.getInt();

			if (capsuleId == -1) {
				foundEnd = true;
			}
			if (!foundEnd) {
				capsuleNbrs[id][2 * (index % SpringlsConstants.maxNeighbors)] = capsuleId;
				capsuleNbrs[id][2 * (index % SpringlsConstants.maxNeighbors) + 1] = vertexId;

			}
			index++;
			if (index % SpringlsConstants.maxNeighbors == 0) {
				foundEnd = false;
				id++;
			}
		}
		buff.rewind();
		return capsuleNbrs;
	}

	/**
	 * Gets the reference image.
	 * 
	 * @return the reference image
	 */
	public ImageData getReferenceImage() {
		return refImage;
	}

	/**
	 * Gets the signed level set.
	 * 
	 * @return the signed level set
	 */
	public float[][][] getSignedLevelSet() {
		float[][][] imageMat;
		queue.putReadBuffer(signedLevelSetBuffer, true);
		FloatBuffer levelSet = signedLevelSetBuffer.getBuffer();
		imageMat = new float[rows][cols][slices];
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					imageMat[i][j][k] = levelSet.get();
				}
			}
		}
		levelSet.rewind();
		return imageMat;
	}

	/**
	 * Gets the springls surface.
	 * 
	 * @return the springls surface
	 */
	public EmbeddedSurface getSpringlsSurface() {
		if (elements == 0) {
			return null;
		}
		queue.putReadBuffer(capsuleBuffer, true).putReadBuffer(
				springlLabelBuffer, true);
		ByteBuffer buffer = capsuleBuffer.getBuffer();

		Point3f[] points = new Point3f[3 * elements];
		int[] indices = new int[3 * elements];
		double[][] data = new double[elements][6];
		int index = 0;
		float scale = SpringlsConstants.scaleUp;
		for (int n = 0; n < elements; n++) {
			float px = buffer.getFloat();
			float py = buffer.getFloat();
			float pz = buffer.getFloat();
			buffer.getFloat();
			float[] a = new float[3];
			for (int i = 0; i < 3; i++) {
				indices[index] = index;
				points[index++] = new Point3f(scale * buffer.getFloat(), scale
						* buffer.getFloat(), scale * buffer.getFloat());
				a[i] = buffer.getFloat();
			}
			data[n] = new double[] { scale * px, scale * py, scale * pz,
					scale * a[0], scale * a[1], scale * a[2] };
		}
		EmbeddedSurface surf = new EmbeddedSurface(points, indices);

		surf.setCellData(data);
		surf.setName("springls");
		buffer.rewind();
		return surf;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public CLDevice.Type getType() {
		return type;
	}

	/**
	 * Gets the unsigned level set.
	 * 
	 * @return the unsigned level set
	 */
	public float[][][] getUnsignedLevelSet() {
		float[][][] imageMat;
		queue.putReadBuffer(unsignedLevelSetBuffer, true);
		FloatBuffer levelSet = unsignedLevelSetBuffer.getBuffer();
		imageMat = new float[rows][cols][slices];
		for (int k = 0; k < slices; k++) {
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < rows; i++) {
					imageMat[i][j][k] = levelSet.get();
				}
			}
		}
		levelSet.rewind();
		return imageMat;
	}

	/**
	 * Checks if is active set valid.
	 * 
	 * @return true, if is active set valid
	 */
	public boolean isActiveSetValid() {
		return isActiveSetValid;
	}

	/**
	 * Checks if is resampling enabled.
	 * 
	 * @return true, if is resampling enabled
	 */
	public boolean isResamplingEnabled() {
		return resamplingEnabled;
	}

	/**
	 * Prints the byte buffer.
	 * 
	 * @param readBuffer
	 *            the read buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public void printByteBuffer(CLBuffer<ByteBuffer> readBuffer, int snapshot) {
		ByteBuffer buffer = readBuffer.getBuffer();
		queue.putReadBuffer(readBuffer, true);
		for (int i = 0; i < snapshot; i++) {
			if (i % 16 == 0) {
				out.print("\n");
			}
			out.printf("%6.2f,", buffer.getFloat());
		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Prints the check sublist sorted.
	 * 
	 * @param readBuffer
	 *            the read buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public void printCheckSublistSorted(CLBuffer<IntBuffer> readBuffer,
			int snapshot) {
		IntBuffer buffer = readBuffer.getBuffer();
		queue.putReadBuffer(readBuffer, true);
		int lastValue = -1;
		int currentValue = 0;
		for (int i = 0; i < snapshot; i++) {
			currentValue = buffer.get();
			if (i % SpringlsConstants.maxNearestBins != 0) {
				if (lastValue > currentValue) {
					System.err.println("NOT SORTED "
							+ (i / SpringlsConstants.maxNearestBins) + ") "
							+ lastValue + ">" + currentValue);
					buffer.rewind();
					return;
				}
			}
			lastValue = currentValue;
		}
		// System.out.println("SORTED!");
		buffer.rewind();
	}

	/**
	 * Prints the float buffer.
	 * 
	 * @param readBuffer
	 *            the read buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public void printFloatBuffer(CLBuffer<FloatBuffer> readBuffer, int snapshot) {
		FloatBuffer buffer = readBuffer.getBuffer();
		queue.putReadBuffer(readBuffer, true);
		for (int i = 0; i < snapshot; i++) {
			if (i % 128 == 0) {
				out.print("\n");
			}
			out.printf("%8.4f,", buffer.get());
		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Prints the int buffer.
	 * 
	 * @param readBuffer
	 *            the read buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public void printIntBuffer(CLBuffer<IntBuffer> readBuffer, int snapshot) {
		IntBuffer buffer = readBuffer.getBuffer();
		queue.putReadBuffer(readBuffer, true);
		for (int i = 0; i < snapshot; i++) {
			if (!buffer.hasRemaining()) {
				break;
			}
			if (i % SpringlsConstants.maxNearestBins == 0) {
				System.err.print("\n");
			}
			System.err.print(buffer.get() + ", ");
		}
		System.err.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Prints the struct buffer.
	 * 
	 * @param readBuffer
	 *            the read buffer
	 * @param snapshot
	 *            the snapshot
	 */
	public void printStructBuffer(CLBuffer<ByteBuffer> readBuffer, int snapshot) {

		queue.putReadBuffer(readBuffer, true);
		ByteBuffer buffer = readBuffer.getBuffer();
		for (int i = 0; i < snapshot; i++) {
			out.print(buffer.getFloat() + ", ");
		}
		out.println("...; " + buffer.remaining() + " more");
		buffer.rewind();
	}

	/**
	 * Sets the active set valid.
	 * 
	 * @param valid
	 *            the new active set valid
	 */
	public void setActiveSetValid(boolean valid) {
		this.isActiveSetValid = valid;
	}

	/**
	 * Sets the initial signed level set.
	 * 
	 * @param image
	 *            the new initial signed level set
	 */
	public void setInitialSignedLevelSet(float[][][] image) {
		FloatBuffer buff = signedLevelSetBuffer.getBuffer();
		for (int k = 0; k < image[0][0].length; k++) {
			for (int j = 0; j < image[0].length; j++) {
				for (int i = 0; i < image.length; i++) {
					buff.put((float) Math
							.max(-(SpringlsEvolveLevelSet3D.MAX_DISTANCE + 0.5),
									Math.min(
											SpringlsEvolveLevelSet3D.MAX_DISTANCE + 0.5,
											image[i][j][k])));
				}
			}
		}
		buff.rewind();
		queue.putWriteBuffer(signedLevelSetBuffer, false);
	}

	/**
	 * Sets the initial unsigned level set.
	 * 
	 * @param image
	 *            the new initial unsigned level set
	 */
	public void setInitialUnsignedLevelSet(float[][][] image) {
		FloatBuffer buff = unsignedLevelSetBuffer.getBuffer();
		for (int k = 0; k < image[0][0].length; k++) {
			for (int j = 0; j < image[0].length; j++) {
				for (int i = 0; i < image.length; i++) {
					buff.put(Math.abs(image[i][j][k]));
				}
			}
		}
		buff.rewind();
		queue.putWriteBuffer(unsignedLevelSetBuffer, true);
	}

	/**
	 * Sets the reference image.
	 * 
	 * @param refImage
	 *            the new reference image
	 */
	public void setReferenceImage(ImageData refImage) {
		this.refImage = refImage;
	}

	/**
	 * Sets the reference level set.
	 * 
	 * @param image
	 *            the new reference level set
	 */
	public void setReferenceLevelSet(float[][][] image) {
		FloatBuffer buff = originalUnsignedLevelSetBuffer.getBuffer();
		for (int k = 0; k < image[0][0].length; k++) {
			for (int j = 0; j < image[0].length; j++) {
				for (int i = 0; i < image.length; i++) {
					buff.put(Math.abs(image[i][j][k]));
				}
			}
		}
		buff.rewind();
		queue.putWriteBuffer(originalUnsignedLevelSetBuffer, true);
	}

	/**
	 * Sets the resampling.
	 * 
	 * @param resample
	 *            the new resampling
	 */
	public void setResampling(boolean resample) {
		this.resamplingEnabled = resample;
	}
}
