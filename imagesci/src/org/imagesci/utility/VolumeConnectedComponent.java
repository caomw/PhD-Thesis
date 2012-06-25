/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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
 * @author Blake Lucas (img.science@gmail.com)
 */
package org.imagesci.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

// TODO: Auto-generated Javadoc
/**
 * The Class VolumeConnectedComponent.
 */
public class VolumeConnectedComponent {
	
	/**
	 * The Class VertexLabel.
	 */
	protected static class VertexLabel {
		
		/** The k. */
		public int i, j, k;
		
		/** The label. */
		public int label;

		/**
		 * Instantiates a new vertex label.
		 *
		 * @param i the i
		 * @param j the j
		 * @param k the k
		 * @param label the label
		 */
		public VertexLabel(int i, int j, int k, int label) {
			this.i = i;
			this.j = j;
			this.k = k;
			this.label = label;
		}
	}

	/**
	 * Gets the neighbor.
	 *
	 * @param labels the labels
	 * @param i the i
	 * @param j the j
	 * @param k the k
	 * @param rows the rows
	 * @param cols the cols
	 * @param slices the slices
	 * @return the neighbor
	 */
	private static VertexLabel getNeighbor(VertexLabel[][][] labels, int i,
			int j, int k, int rows, int cols, int slices) {
		if (i < 0 || j < 0 || k < 0 || i >= rows || j >= cols || k >= slices)
			return null;
		return labels[i][j][k];
	}

	/**
	 * Extract largest non background component.
	 *
	 * @param image the image
	 * @return the image data int
	 */
	public static ImageDataInt extractLargestNonBackgroundComponent(
			ImageDataInt image) {
		int rows = image.getRows();
		int cols = image.getCols();
		int slices = image.getSlices();
		ImageDataInt outImage = new ImageDataInt(rows, cols, slices);
		int[][][] labelImage = image.toArray3d();
		Stack<VertexLabel> stack = new Stack<VertexLabel>();
		VertexLabel[][][] labels = new VertexLabel[rows][cols][slices];
		int id = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					if (labelImage[i][j][k] != 0) {
						labels[i][j][k] = new VertexLabel(i, j, k, -1);
					}
				}
			}
		}
		int labelCount = 0;
		HashMap<Integer, Integer> volumes = new HashMap<Integer, Integer>();
		while (true) {
			VertexLabel first = null;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						if (labels[i][j][k] != null
								&& labels[i][j][k].label == -1) {
							first = labels[i][j][k];
							break;
						}
					}
				}
			}
			if (first == null)
				break;
			int label;
			first.label = (label = labelCount++);
			stack.push(first);
			int vol = 1;
			while (!(stack.isEmpty())) {
				VertexLabel top = (VertexLabel) stack.pop();
				VertexLabel[] nbrs = new VertexLabel[6];
				int i = top.i;
				int j = top.j;
				int k = top.k;
				nbrs[0] = getNeighbor(labels, i + 1, j, k, rows, cols, slices);
				nbrs[1] = getNeighbor(labels, i - 1, j, k, rows, cols, slices);
				nbrs[2] = getNeighbor(labels, i, j + 1, k, rows, cols, slices);
				nbrs[3] = getNeighbor(labels, i, j - 1, k, rows, cols, slices);
				nbrs[4] = getNeighbor(labels, i, j, k + 1, rows, cols, slices);
				nbrs[5] = getNeighbor(labels, i, j, k - 1, rows, cols, slices);
				for (VertexLabel nbr : nbrs) {
					if (nbr != null && nbr.label == -1) {
						nbr.label = label;
						vol++;
						stack.push(nbr);
					}
				}
			}
			volumes.put(label, vol);
		}
		int max = 0;
		int bestLabel = 0;
		for (Integer label : volumes.keySet()) {
			int vol = volumes.get(label);
			System.out.println(label + ": " + vol);
			if (vol > max) {
				bestLabel = label;
				max = vol;
			}
		}
		System.out.println("Biggest Object " + bestLabel);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					if (labels[i][j][k] != null
							&& labels[i][j][k].label == bestLabel) {
						outImage.set(i, j, k, labelImage[i][j][k]);
					}
				}
			}
		}
		outImage.setHeader(image.getHeader());
		outImage.setName(image.getName());
		return outImage;
	}

	/**
	 * Extract largest foreground component.
	 *
	 * @param image the image
	 * @param targetLabel the target label
	 * @return the image data int
	 */
	public static ImageDataInt extractLargestForegroundComponent(
			ImageDataInt image, int targetLabel) {
		int rows = image.getRows();
		int cols = image.getCols();
		int slices = image.getSlices();
		ImageDataInt outImage = new ImageDataInt(rows, cols, slices);
		int[][][] labelImage = image.toArray3d();
		Stack<VertexLabel> stack = new Stack<VertexLabel>();
		VertexLabel[][][] labels = new VertexLabel[rows][cols][slices];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					if (labelImage[i][j][k] == targetLabel) {
						labels[i][j][k] = new VertexLabel(i, j, k, -1);
					}
				}
			}
		}
		int labelCount = 0;
		HashMap<Integer, Integer> volumes = new HashMap<Integer, Integer>();
		while (true) {
			VertexLabel first = null;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						if (labels[i][j][k] != null
								&& labels[i][j][k].label == -1) {
							first = labels[i][j][k];
							break;
						}
					}
				}
			}
			if (first == null)
				break;
			int label;
			first.label = (label = labelCount++);
			stack.push(first);
			int vol = 1;
			while (!(stack.isEmpty())) {
				VertexLabel top = (VertexLabel) stack.pop();
				VertexLabel[] nbrs = new VertexLabel[6];
				int i = top.i;
				int j = top.j;
				int k = top.k;
				nbrs[0] = getNeighbor(labels, i + 1, j, k, rows, cols, slices);
				nbrs[1] = getNeighbor(labels, i - 1, j, k, rows, cols, slices);
				nbrs[2] = getNeighbor(labels, i, j + 1, k, rows, cols, slices);
				nbrs[3] = getNeighbor(labels, i, j - 1, k, rows, cols, slices);
				nbrs[4] = getNeighbor(labels, i, j, k + 1, rows, cols, slices);
				nbrs[5] = getNeighbor(labels, i, j, k - 1, rows, cols, slices);
				for (VertexLabel nbr : nbrs) {
					if (nbr != null && nbr.label == -1) {
						nbr.label = label;
						vol++;
						stack.push(nbr);
					}
				}
			}
			volumes.put(label, vol);
		}
		int max = 0;
		int bestLabel = 0;
		for (Integer label : volumes.keySet()) {
			int vol = volumes.get(label);
			System.out.println(label + ": " + vol);
			if (vol > max) {
				bestLabel = label;
				max = vol;
			}
		}
		System.out.println("Biggest Object " + bestLabel);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					if (labels[i][j][k] != null
							&& labels[i][j][k].label != bestLabel) {
						outImage.set(i, j, k, 0);
					} else {
						outImage.set(i, j, k, labelImage[i][j][k]);
					}
				}
			}
		}
		outImage.setHeader(image.getHeader());
		outImage.setName(image.getName());
		return outImage;
	}

	/**
	 * The Class LabelVolume.
	 */
	protected static class LabelVolume implements Comparable<LabelVolume> {
		
		/** The label. */
		public int label;
		
		/** The volume. */
		public int volume;

		/**
		 * Instantiates a new label volume.
		 *
		 * @param label the label
		 * @param volume the volume
		 */
		public LabelVolume(int label, int volume) {
			this.label = label;
			this.volume = volume;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(LabelVolume l) {
			return (int) Math.signum(l.volume - volume);
		}

		/**
		 * Gets the string.
		 *
		 * @return the string
		 */
		public String getString() {
			return ("[" + label + ":" + volume + "]");
		}
	}

	/**
	 * Label componenets.
	 *
	 * @param image the image
	 * @return the image data int
	 */
	public static ImageDataInt labelComponenets(ImageDataInt image) {
		int rows = image.getRows();
		int cols = image.getCols();
		int slices = image.getSlices();
		ImageDataInt outImage = new ImageDataInt(rows, cols, slices);
		int[][][] labelImage = image.toArray3d();
		Stack<VertexLabel> stack = new Stack<VertexLabel>();
		VertexLabel[][][] labels = new VertexLabel[rows][cols][slices];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					if (labelImage[i][j][k] == 0) {
						labels[i][j][k] = new VertexLabel(i, j, k, 0);
					} else {
						labels[i][j][k] = new VertexLabel(i, j, k, -1);
					}
				}
			}
		}
		int labelCount = 0;
		HashMap<Integer, Integer> volumes = new HashMap<Integer, Integer>();
		while (true) {
			VertexLabel first = null;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int k = 0; k < slices; k++) {
						if (labels[i][j][k] != null
								&& labels[i][j][k].label == -1) {
							first = labels[i][j][k];
							break;
						}
					}
				}
			}
			if (first == null)
				break;
			int label;
			first.label = (label = ++labelCount);
			stack.push(first);
			int vol = 1;
			while (!(stack.isEmpty())) {
				VertexLabel top = (VertexLabel) stack.pop();
				VertexLabel[] nbrs = new VertexLabel[6];
				int i = top.i;
				int j = top.j;
				int k = top.k;
				nbrs[0] = getNeighbor(labels, i + 1, j, k, rows, cols, slices);
				nbrs[1] = getNeighbor(labels, i - 1, j, k, rows, cols, slices);
				nbrs[2] = getNeighbor(labels, i, j + 1, k, rows, cols, slices);
				nbrs[3] = getNeighbor(labels, i, j - 1, k, rows, cols, slices);
				nbrs[4] = getNeighbor(labels, i, j, k + 1, rows, cols, slices);
				nbrs[5] = getNeighbor(labels, i, j, k - 1, rows, cols, slices);
				for (VertexLabel nbr : nbrs) {
					if (nbr != null && nbr.label == -1) {
						nbr.label = label;
						vol++;
						stack.push(nbr);
					}
				}
			}
			volumes.put(label, vol);
		}
		int max = 0;
		int index = 0;
		LabelVolume[] labelVols = new LabelVolume[volumes.keySet().size()];
		for (Integer label : volumes.keySet()) {
			int vol = volumes.get(label);
			labelVols[index++] = new LabelVolume(label, vol);

		}
		Arrays.sort(labelVols);
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		index = 1;
		for (LabelVolume lab : labelVols) {
			System.out.println(index + ") " + lab.label + ": " + lab.volume);
			map.put(lab.label, index++);

		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					if (labels[i][j][k].label > 0)
						outImage.set(i, j, k, map.get(labels[i][j][k].label));
				}
			}
		}
		outImage.setHeader(image.getHeader());
		outImage.setName(image.getName());
		return outImage;
	}

}
