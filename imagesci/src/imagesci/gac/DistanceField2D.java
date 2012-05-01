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
package imagesci.gac;

import edu.jhu.ece.iacl.jist.pipeline.AbstractCalculation;
import edu.jhu.ece.iacl.jist.structures.data.BinaryMinHeap;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataUByte;
import edu.jhu.ece.iacl.jist.structures.image.VoxelFloat;
import edu.jhu.ece.iacl.jist.structures.image.VoxelIndexed;
import edu.jhu.ece.iacl.jist.utility.VersionUtil;

// TODO: Auto-generated Javadoc
/**
 * Calculate 2D distance field using the Fast-marching method.
 * 
 * @author Blake Lucas
 */
public class DistanceField2D extends AbstractCalculation {

	/** The Constant ALIVE. */
	private static final byte ALIVE = 1;

	/** The Constant FARAWAY. */
	private static final byte FARAWAY = 3;

	/** The Constant IFij. */
	private static final double IFij = 1.0f;

	/** The Constant ISFij. */
	private static final double ISFij = 1.0f;

	/** The Constant NBAND. */
	private static final byte NBAND = 2;

	/** The Constant sdf. */
	private static final DistanceField2D sdf = new DistanceField2D();

	/** The silent. */
	protected boolean silent = false;

	/**
	 * Instantiates a new distance field2 d.
	 */
	public DistanceField2D() {
		super();
		setLabel("Fast-Marching Distance");
	}

	/**
	 * Instantiates a new distance field2 d.
	 * 
	 * @param parent
	 *            the parent
	 */
	public DistanceField2D(AbstractCalculation parent) {
		super(parent);
		setLabel("Fast-Marching Distance");
	}

	/**
	 * Instantiates a new distance field2 d.
	 * 
	 * @param silent
	 *            the silent
	 */
	public DistanceField2D(boolean silent) {
		super();
		this.silent = silent;
		setLabel("Fast-Marching Distance");
	}

	/**
	 * Solve.
	 * 
	 * @param vol
	 *            the vol
	 * @param maxDistance
	 *            the max distance
	 * 
	 * @return the image data float
	 */
	public ImageDataFloat solve25D(ImageDataFloat vol, double maxDistance) {
		int rows = vol.getRows();
		int cols = vol.getCols();
		int slices = vol.getSlices();
		ImageDataFloat result = new ImageDataFloat(rows, cols, slices);
		result.setName(vol.getName());
		result.setHeader(vol.getHeader());
		for (int k = 0; k < slices; k++) {
			ImageDataFloat tmp = new ImageDataFloat(rows, cols);
			float[][] tmpMat = tmp.toArray2d();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					tmpMat[i][j] = vol.getFloat(i, j, k);
				}
			}
			ImageDataFloat dist = solve(tmp, maxDistance);
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					result.set(i, j, k, dist.getFloat(i, j));
				}
			}
			dist.dispose();
		}
		return result;
	}

	/**
	 * Solve.
	 * 
	 * @param vol
	 *            the vol
	 * @param maxDistance
	 *            the max distance
	 * 
	 * @return the image data float
	 */
	public ImageDataFloat solve(ImageDataFloat vol, double maxDistance) {
		String name = vol.getName();
		int XN = vol.getRows();
		int YN = vol.getCols();
		int LX, HX, LY, HY;
		short NSFlag, WEFlag;
		int i, j, koff;
		int nj, ni;
		double newvalue;
		double s = 0, t = 0;
		double result;
		byte[] neighborsX = new byte[] { 1, 0, -1, 0 };
		byte[] neighborsY = new byte[] { 0, 1, 0, -1 };
		VoxelIndexed<VoxelFloat> he;
		double Nv = 0, Sv = 0, Wv = 0, Ev = 0, Fv = 0, Bv = 0, Cv = 0; /*
																		 * Value
																		 * at
																		 * six
																		 * neighours
																		 * of a
																		 * pixel
																		 */
		int Nl, Sl, Fl, Bl; /* Label at six neighours of a pixel */
		System.out.println("jist.plugins" + "\t" + "MAX DISTANCE "
				+ maxDistance);
		setLabel("Fast-Marching Distance Field");
		ImageDataFloat distVol = new ImageDataFloat(XN, YN);
		float[][] distVolM = distVol.toArray2d();
		ImageDataUByte labelVol = new ImageDataUByte(XN, YN);
		byte[][] labelVolM = labelVol.toArray2d();
		float[][] volM = vol.toArray2d();
		// Initially label all points as far away
		for (i = 0; i < XN; i++) {
			for (j = 0; j < YN; j++) {
				labelVolM[i][j] = (FARAWAY);
			}
		}
		int countAlive = 0;
		// Initialize points just inside the boundary

		for (j = 0; j < YN; j++) {
			if (!silent) {
				incrementCompletedUnits();
			}
			for (i = 0; i < XN; i++) {
				// If level set is exactly zero, then label point alive and
				// set distance to zero
				if (volM[i][j] == 0) {
					distVolM[i][j] = (0);
					labelVolM[i][j] = (ALIVE);
					countAlive++;
				} else {
					// Locate lower and upper neighbors
					LX = (i == 0) ? 1 : 0;
					HX = (i == (XN - 1)) ? 1 : 0;

					LY = (j == 0) ? 1 : 0;
					HY = (j == (YN - 1)) ? 1 : 0;

					NSFlag = 0;
					WEFlag = 0;

					Nv = volM[i][j - 1 + LY];
					Sv = volM[i][j + 1 - HY];
					Wv = volM[i - 1 + LX][j];
					Ev = volM[i + 1 - HX][j];
					Cv = volM[i][j];
					// Check if the current sign is the same as the north
					// sign
					if (Nv * Cv < 0) {
						NSFlag = 1;
						s = Nv;
					}
					// Check if the current sign is the same as the south
					// sign
					if (Sv * Cv < 0) {
						if (NSFlag == 0) {
							NSFlag = 1;
							s = Sv;
						} else {
							s = (Math.abs(Nv) > Math.abs(Sv)) ? Nv : Sv;
						}
					}
					// Check if the current sign is the same as the west
					// sign
					if (Wv * Cv < 0) {
						WEFlag = 1;
						t = Wv;
					}
					// Check if the current sign is the same as the east
					// sign
					if (Ev * Cv < 0) {
						if (WEFlag == 0) {
							WEFlag = 1;
							t = Ev;
						} else {
							t = (Math.abs(Ev) > Math.abs(Wv)) ? Ev : Wv;
						}
					}

					result = 0;
					if (NSFlag != 0) {
						s = Cv / (Cv - s);
						result += 1.0 / (s * s);
					}
					if (WEFlag != 0) {
						t = Cv / (Cv - t);
						result += 1.0 / (t * t);
					}
					if (result == 0) {
						continue;
					}
					/*
					 * if(count++<100){
					 * System.out.println("jist.plugins"+"\t"+"ALIVE1
					 * ("+i+","+j+","+k+") "+result);
					 * System.out.println("jist.plugins"
					 * +"\t"+Sv+" "+Wv+" "+Ev+" "+Fv+" "+Bv+" "+Cv); }
					 */
					countAlive++;
					labelVolM[i][j] = (ALIVE);
					result = Math.sqrt(result);
					distVolM[i][j] = (float) (IFij / result);
				}

			}
		}

		BinaryMinHeap heap = new BinaryMinHeap(countAlive, XN, YN, 1);
		/* Initialize NarrowBand Heap */

		for (j = 0; j < YN; j++) {
			if (!silent) {
				incrementCompletedUnits();
			}
			for (i = 0; i < XN; i++) {

				if (labelVol.get(i, j).shortValue() != ALIVE) {
					continue;
				}
				// if(count++<100)System.out.println("jist.plugins"+"\t"+"ALIVE
				// ("+i+","+j+","+k+")");
				/* Put its 4 neighbors into NarrowBand */
				for (koff = 0; koff < 4; koff++) {/*
													* Find six neighbouring points
													*/
					ni = i + neighborsX[koff];
					nj = j + neighborsY[koff];

					if (nj < 0 || nj >= YN || ni < 0 || ni >= XN) {
						continue; /* Out of computational Boundary */
					}

					if (labelVolM[ni][nj] != FARAWAY) {
						continue;
					}
					labelVolM[ni][nj] = (NBAND);
					/*
					 * Note: Only ALIVE points contribute to the distance
					 * computation
					 */
					/* Neighbour to the north */
					if (nj > 0) {
						Nv = distVolM[ni][nj - 1];
						Nl = labelVolM[ni][nj - 1];
					} else {
						Nl = 0;
					}
					/* Neighbour to the south */
					if (nj < YN - 1) {
						Sv = distVolM[ni][nj + 1];
						Sl = labelVolM[ni][nj + 1];
					} else {
						Sl = 0;
					}
					/* Neighbour to the front */
					if (ni < XN - 1) {
						Fv = distVolM[ni + 1][nj];
						Fl = labelVolM[ni + 1][nj];
					} else {
						Fl = 0;
					}
					/* Neighbour to the back */
					if (ni > 0) {
						Bv = distVolM[ni - 1][nj];
						Bl = labelVolM[ni - 1][nj];
					} else {
						Bl = 0;
					}
					/*
					 * Update the value of this to-be-updated NarrowBand point
					 */
					newvalue = march(Nv, Sv, Ev, Wv, Fv, Bv, Nl, Sl, Fl, Bl);
					// if(count++<100)System.out.println("jist.plugins"+"\t"+"NEW
					// VALUE
					// ("+i+","+j+","+k+") ("+ni+","+nj+","+nk+")
					// "+newvalue);

					distVolM[ni][nj] = (float) (newvalue);
					VoxelIndexed<VoxelFloat> vox = new VoxelIndexed<VoxelFloat>(
							new VoxelFloat((float) newvalue));
					vox.setRefPosition(ni, nj, 0);
					heap.add(vox);
				}

			}
		}
		/*
		 * Begin Fast Marching to get the unsigned distance function inwords and
		 * outwards simultaneously since points inside and outside the contour
		 * won't interfere with each other
		 */
		setLabel("Fast-Marching Distance Field");
		setTotalUnits(heap.size());
		while (!heap.isEmpty()) { /* There are still points not yet accepted */

			// if(heap.size()%50000==0)System.out.println("jist.plugins"+"\t"+heap.size());
			he = (VoxelIndexed<VoxelFloat>) heap.remove(); /*
															 * Label the point
															 * with smallest
															 * value among all
															 * NarrowBand points
															 * as ALIVE
															 */

			/* Put the smallest heap element to ALIVE */
			i = he.getRow();
			j = he.getColumn();

			if (he.getFloat() > maxDistance) {
				break;
			}
			distVolM[i][j] = (he.getFloat());
			labelVolM[i][j] = (ALIVE);
			/* Update its neighbor */
			/*
			 * Put FARAWAY neighbour into NarrowBand, Recompute values at
			 * NarrowBand neighbours, Keep ALIVE (Accepted) neighbour unchanged
			 */
			for (koff = 0; koff < 4; koff++) {
				ni = i + neighborsX[koff];
				nj = j + neighborsY[koff];

				if (nj < 0 || nj >= YN || ni < 0 || ni >= XN) {
					continue; /* Out of boundary */
				}
				if (labelVolM[ni][nj] == ALIVE) {
					continue; /* Don't change ALIVE neighbour */
				}

				/* ReCompute the value at (nk][ nj][ ni) */
				/*
				 * Get the values and labels of six neighbours of the
				 * to-be-updated point. The labels are needed since only values
				 * at ALIVE neighbours will be used to update the value of the
				 * to-be-updated point
				 */

				/*
				 * Note: Only ALIVE points contribute to the distance
				 * computation
				 */
				/* Neighbour to the north */
				if (nj > 0) {
					Nv = distVolM[ni][nj - 1];
					Nl = labelVolM[ni][nj - 1];
				} else {
					Nl = 0;
				}

				/* Neighbour to the south */
				if (nj < YN - 1) {
					Sv = distVolM[ni][nj + 1];
					Sl = labelVolM[ni][nj + 1];
				} else {
					Sl = 0;
				}

				/* Neighbour to the front */
				if (ni < XN - 1) {
					Fv = distVolM[ni + 1][nj];
					Fl = labelVolM[ni + 1][nj];
				} else {
					Fl = 0;
				}

				/* Neighbour to the back */
				if (ni > 0) {
					Bv = distVolM[ni - 1][nj];
					Bl = labelVolM[ni - 1][nj];
				} else {
					Bl = 0;
				}

				/* Update the value of this to-be-updated NarrowBand point */
				newvalue = march(Nv, Sv, Ev, Wv, Fv, Bv, Nl, Sl, Fl, Bl);

				/*
				 * If it was a FARAWAY point, add it to the NarrowBand Heap;
				 * otherwise, just update its value using the backpointer
				 */
				VoxelIndexed<VoxelFloat> vox = new VoxelIndexed<VoxelFloat>(
						new VoxelFloat((float) newvalue));
				vox.setRefPosition(ni, nj, 0);
				if (labelVolM[ni][nj] == NBAND) {
					heap.change(ni, nj, 0, vox);
				} else {
					decrementCompletedUnits();
					heap.add(vox);
					labelVolM[ni][nj] = (NBAND);
				}
			} /* End of updating 6 neighbours */
			if (!silent) {
				incrementCompletedUnits();
			}
		}/* End of marching loop */

		/* Add signs to the unsigned distance function */
		for (i = 0; i < XN; i++) {
			for (j = 0; j < YN; j++) {
				if (labelVolM[i][j] != ALIVE) {
					distVolM[i][j] = (float) (maxDistance);
				}
				if (volM[i][j] < 0) {
					distVolM[i][j] = (-distVolM[i][j]);

				}
			}
		}

		if (!silent) {
			this.markCompleted();
		}
		distVol.setName(name);
		distVol.setHeader(vol.getHeader());
		return distVol;
	}

	/**
	 * March.
	 * 
	 * @param Nv
	 *            the nv
	 * @param Sv
	 *            the sv
	 * @param Ev
	 *            the ev
	 * @param Wv
	 *            the wv
	 * @param Fv
	 *            the fv
	 * @param Bv
	 *            the bv
	 * @param Nl
	 *            the nl
	 * @param Sl
	 *            the sl
	 * @param Fl
	 *            the fl
	 * @param Bl
	 *            the bl
	 * 
	 * @return the double
	 */
	protected double march(double Nv, double Sv, double Ev, double Wv,
			double Fv, double Bv, int Nl, int Sl, int Fl, int Bl) {
		/*
		 * Compute the new value at a NarrowBand point using values of its
		 * ALIVE(Accepted) 6-connected neighbours Note: at least one of its 6
		 * neighbours must be ALIVE If ALIVE neighbours exist only in the
		 * north-south (or east-west, or front-back) neighbours, the updated
		 * value is equal to the value of the smaller one plus 1 (1/F(k,i,j) if
		 * F(k,i,j) ne 1). Otherwise, a quadratic equation need to be solved,
		 * and the bigger root is taken as the updated value
		 */
		/*
		 * The following program assumes F(k,i,j) = 1; if not, replace IFij and
		 * ISFij with the true values. Note, F(k,i,j) should be positive!
		 */

		/* Suppose a, b, and c are the minimum T value in three directions */
		double s, s2; /* s = a + b +c; s2 = a*a + b*b +c*c */
		double tmp;
		int count;

		s = 0;
		s2 = 0;
		count = 0;

		if (Nl == ALIVE && Sl == ALIVE) {
			tmp = Math.min(Nv, Sv); /* Take the smaller one if both ALIVE */
			s += tmp;
			s2 += tmp * tmp;
			count++;
		} else if (Nl == ALIVE) {
			s += Nv; /* Else, take the ALIVE one */
			s2 += Nv * Nv;
			count++;
		} else if (Sl == ALIVE) {
			s += Sv;
			s2 += Sv * Sv;
			count++;
		}

		/*
		 * Similarly in the front-back direction to get correct approximation to
		 * the derivative in the z-direction
		 */
		if (Fl == ALIVE && Bl == ALIVE) {
			tmp = Math.min(Fv, Bv); /* Take the smaller one if both ALIVE */
			s += tmp;
			s2 += tmp * tmp;
			count++;
		} else if (Fl == ALIVE) {
			s += Fv; /* Else, take the ALIVE one */
			s2 += Fv * Fv;
			count++;
		} else if (Bl == ALIVE) {
			s += Bv;
			s2 += Bv * Bv;
			count++;
		}

		/*
		 * count must be greater than zero since there must be one ALIVE pt in
		 * the neighbors
		 */

		tmp = (s + Math.sqrt((s * s - count * (s2 - ISFij)))) / count;
		/* The larger root */
		return tmp;
	}

	/**
	 * Do solve.
	 * 
	 * @param vol
	 *            the vol
	 * @param thresh
	 *            the thresh
	 * 
	 * @return the image data float
	 */
	public static ImageDataFloat doSolve(ImageDataFloat vol, double thresh) {
		return sdf.solve(vol, thresh);
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		return VersionUtil.parseRevisionNumber("$Revision: 1.3 $");
	}

	/**
	 * Max signed distance.
	 * 
	 * @param vol
	 *            the vol
	 * 
	 * @return the float
	 */
	public static float maxSignedDistance(float[][] vol) {
		int rows = vol.length;
		int cols = vol[0].length;
		float max_dist = 0;
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < cols; y++) {
				max_dist = Math.max(Math.abs(vol[x][y]), max_dist);
			}
		}
		System.out.println("jist.plugins" + "\t" + "MAX SIGNED DISTANCE "
				+ max_dist);
		return max_dist;
	}
}
