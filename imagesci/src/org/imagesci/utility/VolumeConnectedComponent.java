package org.imagesci.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataInt;

public class VolumeConnectedComponent {
	protected static class VertexLabel {
		public int i, j, k;
		public int label;

		public VertexLabel(int i, int j, int k, int label) {
			this.i = i;
			this.j = j;
			this.k = k;
			this.label = label;
		}
	}

	private static VertexLabel getNeighbor(VertexLabel[][][] labels, int i,
			int j, int k, int rows, int cols, int slices) {
		if (i < 0 || j < 0 || k < 0 || i >= rows || j >= cols || k >= slices)
			return null;
		return labels[i][j][k];
	}

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

	protected static class LabelVolume implements Comparable<LabelVolume> {
		public int label;
		public int volume;

		public LabelVolume(int label, int volume) {
			this.label = label;
			this.volume = volume;
		}

		@Override
		public int compareTo(LabelVolume l) {
			return (int) Math.signum(l.volume - volume);
		}

		public String getString() {
			return ("[" + label + ":" + volume + "]");
		}
	}

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
