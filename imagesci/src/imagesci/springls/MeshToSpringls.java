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
package imagesci.springls;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;

import javax.vecmath.Point3f;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;

import data.PlaceHolder;

import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContour3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourMesh3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationSpringlsActiveContourVolume3D;
import edu.jhu.cs.cisst.vent.widgets.VisualizationTriangleMesh;
import edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

// TODO: Auto-generated Javadoc
/**
 * The Class MeshToSpringls.
 */
public class MeshToSpringls extends AbstractCalculation {

	/** The Constant DISTANCE_FIELD_EXTENT. */
	protected static final int DISTANCE_FIELD_EXTENT = 2;

	/** The commons. */
	protected SpringlsCommon3D commons;

	/** The dist field. */
	ImageDataFloat distField;

	/** The MA x_ cycles. */
	protected final int MAX_CYCLES = 16;

	/**
	 * Instantiates a new mesh to springls.
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
	public MeshToSpringls(int rows, int cols, int slices) throws IOException {
		super();
		this.commons = new SpringlsCommon3D(CLDevice.Type.CPU);
		commons.initialize(rows, cols, slices, false);
		commons.signedLevelSetBuffer = commons.context.createFloatBuffer(rows
				* cols * slices, USE_BUFFER, READ_WRITE);
	}

	/**
	 * Solve.
	 * 
	 * @param initialSurface
	 *            the initial surface
	 * @return the image data float
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ImageDataFloat solve(EmbeddedSurface initialSurface)
			throws IOException {
		return solve(initialSurface, 0.1f, 10);
	}

	/**
	 * Solve.
	 * 
	 * @param initialSurface
	 *            the initial surface
	 * @param smoothWeight
	 *            the smooth weight
	 * @param smoothingIters
	 *            the smoothing iters
	 * @return the image data float
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ImageDataFloat solve(EmbeddedSurface initialSurface,
			float smoothWeight, int smoothingIters) throws IOException {
		commons.setSpringls(new SpringlsSurface(initialSurface));
		long startTime = System.nanoTime();
		CLBuffer<FloatBuffer> tmpSignedLevelSet = commons.context
				.createFloatBuffer(
						commons.rows * commons.cols * commons.slices,
						USE_BUFFER, READ_WRITE);
		SpringlsEvolveLevelSet3D evolve = new SpringlsEvolveLevelSet3D(commons,
				-smoothWeight);
		createUnsignedLevelSet(commons);
		evolve.extendUnsignedDistanceField(4);
		convertUnsignedToSigned(commons, tmpSignedLevelSet);
		commons.queue.finish();

		if (smoothingIters > 0) {
			evolve.setMaxIterations(smoothingIters);
			evolve.setTimeStep(0.25f);
			evolve.rebuildNarrowBand();
			evolve.evolve();
		}

		tmpSignedLevelSet.release();
		long endTime = System.nanoTime();
		System.out.println("Elapsed Time:" + ((endTime - startTime) * 1E-9)
				+ " sec");
		distField = new ImageDataFloat(commons.getSignedLevelSet());
		distField.setName(initialSurface.getName() + "_distfield");
		commons.dispose();
		markCompleted();
		return distField;
	}

	/**
	 * Creates the unsigned level set.
	 * 
	 * @param commons
	 *            the commons
	 */
	private void createUnsignedLevelSet(SpringlsCommon3D commons) {
		CLKernel buildDistanceField = commons.kernelMap
				.get("buildNBDistanceField");
		CLKernel initDistanceField = commons.kernelMap.get("initDistanceField");
		initDistanceField.putArgs(commons.unsignedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(initDistanceField, 0, commons.rows
				* commons.cols * commons.slices,
				SpringlsCommon3D.WORKGROUP_SIZE);
		buildDistanceField
				.putArgs(commons.capsuleBuffer, commons.unsignedLevelSetBuffer)
				.putArg(commons.elements).rewind();
		commons.queue.put1DRangeKernel(buildDistanceField, 0,
				commons.arrayLength, SpringlsCommon3D.WORKGROUP_SIZE);
	}

	/**
	 * Convert unsigned to signed.
	 * 
	 * @param commons
	 *            the commons
	 * @param tmpSignedLevelSet
	 *            the tmp signed level set
	 */
	private void convertUnsignedToSigned(SpringlsCommon3D commons,
			CLBuffer<FloatBuffer> tmpSignedLevelSet) {

		// Init to unlabeled
		final CLKernel initSignedLevelSet = commons.kernelMap
				.get("initSignedLevelSet");
		final CLKernel erodeLevelSet = commons.kernelMap.get("erodeLevelSet");
		final CLKernel multiplyLevelSets = commons.kernelMap
				.get("multiplyLevelSets");
		initSignedLevelSet.putArg(commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(
				initSignedLevelSet,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
		int blockSize = Math.max(Math.max(commons.rows, commons.cols),
				commons.slices) / (MAX_CYCLES);
		CLBuffer<FloatBuffer> buffIn = commons.signedLevelSetBuffer;
		CLBuffer<FloatBuffer> buffOut = tmpSignedLevelSet;
		int cycles = 0;
		int lastBlockSize = Math.max(Math.max(commons.rows, commons.cols),
				commons.slices);
		while (blockSize >= 1) {
			cycles = lastBlockSize / (blockSize);
			int globalSize = commons.rows * commons.cols * commons.slices
					/ (blockSize * blockSize * blockSize);
			for (int i = 0; i < 2 * cycles; i++) {
				erodeLevelSet
						.putArgs(commons.unsignedLevelSetBuffer, buffIn,
								buffOut).putArg(blockSize).rewind();

				commons.queue.put1DRangeKernel(erodeLevelSet, 0,
						SpringlsCommon3D.roundToWorkgroupPower(globalSize,
								SpringlsCommon3D.WORKGROUP_SIZE), Math.min(
								globalSize, SpringlsCommon3D.WORKGROUP_SIZE));
				CLBuffer<FloatBuffer> tmp = buffOut;
				buffOut = buffIn;
				buffIn = tmp;
			}
			lastBlockSize = blockSize;
			blockSize /= 2;

		}
		commons.queue.finish();
		multiplyLevelSets.putArgs(commons.unsignedLevelSetBuffer,
				commons.signedLevelSetBuffer).rewind();
		commons.queue.put1DRangeKernel(
				multiplyLevelSets,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.cols * commons.slices),
				SpringlsCommon3D.WORKGROUP_SIZE);
		CLKernel copyLevelSet = commons.kernelMap.get("copyLevelSet");
		copyLevelSet.putArgs(commons.unsignedLevelSetBuffer, tmpSignedLevelSet)
				.rewind();
		commons.queue.put1DRangeKernel(
				copyLevelSet,
				0,
				SpringlsCommon3D.roundToWorkgroupPower(commons.rows
						* commons.slices * commons.cols),
				SpringlsCommon3D.WORKGROUP_SIZE);
	}

	/**
	 * Gets the distance field.
	 * 
	 * @return the distance field
	 */
	public ImageDataFloat getDistanceField() {
		// TODO Auto-generated method stub
		return distField;
	}

	public static void main(String[] args) {

		try {
			File f = (args.length > 0) ? new File(args[0]) : new File(
					PlaceHolder.class.getResource("cow.stl").toURI());
			EmbeddedSurface mesh = SurfaceReaderWriter.getInstance().read(f);
			MeshToSpringls mtos = new MeshToSpringls(256, 192, 128);
			ImageDataFloat levelSet = mtos.solve(mesh);

			SpringlsActiveContour3D simulator = new SpringlsActiveContour3D();
			simulator.setTask(ActiveContour3D.Task.ACTIVE_CONTOUR);
			simulator.setReferenceImage(levelSet);
			simulator.setInitialDistanceFieldImage(levelSet);
			simulator.setInitialSurface(mesh);
			try {
				simulator.init();
				VisualizationSpringlsActiveContour3D vis = new VisualizationSpringlsActiveContourVolume3D(
						600, 600, simulator);
				VisualizationApplication app = new VisualizationApplication(vis);
				app.setPreferredSize(new Dimension(1024, 768));
				app.setShowToolBar(false);
				app.addListener(vis);
				app.runAndWait();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
