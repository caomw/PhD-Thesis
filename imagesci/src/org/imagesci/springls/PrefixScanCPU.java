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
package org.imagesci.springls;

/*

 * 22:12 Sunday, February 28 2010
 */

import static com.jogamp.opencl.CLMemory.Mem.COPY_BUFFER;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.USE_BUFFER;
import static com.jogamp.opencl.CLProgram.CompilerOptions.ENABLE_MAD;
import static java.lang.System.out;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Random;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLResource;

// TODO: Auto-generated Javadoc
/**
 * The Class PrefixScan.
 * 
 * @author Michael Bien
 */
public class PrefixScanCPU implements CLResource {

	/** The add scan list. */
	private final CLKernel addScanList;

	/** The batch size. */
	protected int batchSize = 1024;

	/** The buffer. */
	private CLBuffer<IntBuffer> buffer;

	/** The max value buffer. */
	private CLBuffer<IntBuffer> maxValueBuffer = null;
	
	/** The prefix scan sub list. */
	private final CLKernel prefixScanSubList;

	/** The program. */
	private final CLProgram program;

	/** The queue. */
	private final CLCommandQueue queue;
	
	/** The released. */
	boolean released = false;

	/** The Constant WORKGROUP_SIZE. */
	private final int WORKGROUP_SIZE;

	/**
	 * Instantiates a new prefix scan.
	 *
	 * @param queue the queue
	 * @param workgroupSize the workgroup size
	 * @param batchSize the batch size
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public PrefixScanCPU(CLCommandQueue queue, int workgroupSize, int batchSize)
			throws IOException {

		this.queue = queue;
		CLContext context = queue.getContext();
		WORKGROUP_SIZE = workgroupSize;
		this.batchSize = batchSize;
		program = context.createProgram(
				getClass().getResourceAsStream("PrefixScanCPU.cl")).build(
				CLProgram.define("WORKGROUP_SIZE", WORKGROUP_SIZE), ENABLE_MAD);

		prefixScanSubList = program.createCLKernel("prefixScanSubList");
		addScanList = program.createCLKernel("addScanList");
	}

	/**
	 * Close.
	 */
	public void close() {
		release();
		released = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jogamp.opencl.CLResource#release()
	 */
	@Override
	public void release() {
		program.release();

		if (buffer != null) {
			buffer.release();
		}
		released = true;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		PrefixScanCPU.test();
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
	 * Test.
	 */
	public static void test() {
		CLPlatform[] platforms = CLPlatform.listCLPlatforms();
		CLDevice device = null;
		for (CLPlatform p : platforms) {
			device = p.getMaxFlopsDevice(CLDevice.Type.CPU);
			if (device != null) {
				break;
			}
		}
		CLContext context = CLContext.create(device);
		CLCommandQueue queue = device.createCommandQueue();
		/*
		 * Map<String, String> map = device.getProperties(); for (String key :
		 * map.keySet()) { System.out.println(key + " = " + map.get(key)); }
		 */
		final int elements = 256 * 256 * 256;
		final int maxvalue = 32;
		int arrayLength = elements;
		PrefixScanCPU scan;
		try {
			scan = new PrefixScanCPU(queue, 32, 1024);

			out.println("Creating OpenCL memory objects...");
			CLBuffer<IntBuffer> src = context.createIntBuffer(elements,
					READ_WRITE, USE_BUFFER);

			out.println("Initializing data..." + elements + "\n ");
			Random random = new Random();
			int[] data = new int[elements];
			for (int i = 0; i < elements; i++) {
				int rnd = 1 + random.nextInt(maxvalue - 1);
				data[i] = rnd;
				src.getBuffer().put(i, rnd);
			}
			long startTime = System.nanoTime();

			int maxValue = scan.scan(src, arrayLength);

			long endTime = System.nanoTime();
			out.println(String.format("Scan: %8.3f sec",
					(endTime - startTime) * 1E-9));
			int count = 0;

			queue.putReadBuffer(src, true);
			IntBuffer buff = src.getBuffer();
			for (int i = 0; i < data.length; i++) {
				count += data[i];
				if (buff.get() != count) {
					System.err.println("Wrong value! " + count);
				}

			}

			System.out.println("Done scan " + count + " " + maxValue);

		} catch (IOException e) {
			// TODO Auto-generated cftch block
			e.printStackTrace();
		}
		context.release();
	}

	/**
	 * Checks if is released.
	 *
	 * @return true, if is released
	 */
	public boolean isReleased() {
		return released;
	}

	// main exclusive scan routine
	/**
	 * Scan.
	 *
	 * @param src the src
	 * @param arrayLength the array length
	 * @return the int
	 */
	public int scan(CLBuffer<IntBuffer> src, int arrayLength) {
		int bufferSize = 1 + arrayLength / batchSize;
		buffer = queue.getContext().createIntBuffer(bufferSize, READ_WRITE);
		if (maxValueBuffer == null) {
			maxValueBuffer = queue.getContext().createIntBuffer(1, READ_WRITE,
					COPY_BUFFER);
		}

		int global_size = SpringlsCommon3D.roundToWorkgroupPower(bufferSize,
				WORKGROUP_SIZE);
		prefixScanSubList.putArgs(src, buffer).putArg(batchSize)
				.putArg(arrayLength).rewind();
		queue.put1DRangeKernel(prefixScanSubList, 0, global_size,
				WORKGROUP_SIZE);
		prefixScanSubList.putArgs(buffer, maxValueBuffer).putArg(bufferSize)
				.putArg(bufferSize).rewind();
		// queue.putReadBuffer(buffer, true);
		// printSnapshot(buffer.getBuffer(), bufferSize);

		queue.put1DRangeKernel(prefixScanSubList, 0, 1, 1);
		addScanList.putArgs(src, buffer).putArg(batchSize).putArg(arrayLength)
				.rewind();

		queue.put1DRangeKernel(addScanList, 0, global_size, WORKGROUP_SIZE);
		queue.finish();
		buffer.release();
		queue.putReadBuffer(maxValueBuffer, true);
		return maxValueBuffer.getBuffer().get(0);
	}
}
