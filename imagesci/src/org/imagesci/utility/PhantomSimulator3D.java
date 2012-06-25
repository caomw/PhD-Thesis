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
package org.imagesci.utility;


import java.util.Random;

import javax.vecmath.Point3i;

import org.imagesci.gac.DistanceField3D;

import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;
import edu.jhu.ece.iacl.jist.utility.VersionUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class PhantomSimulator3D.
 */
public abstract class PhantomSimulator3D {
	/**
	 * The Enum Heaviside.
	 */
	public enum Heaviside {

		/** The ARCTAN. */
		ARCTAN,
		/** The BINARY. */
		BINARY,
		/** The SIGMOID. */
		SIGMOID,
		/** The SIN. */
		SIN
	}

	/**
	 * The Enum NoiseType.
	 */
	public enum NoiseType {

		/** The Gaussian. */
		Gaussian,
		/** The Uniform. */
		Uniform
	}

	/**
	 * The Enum Phantom.
	 */
	public enum Phantom {

		/** The Bubbles. */
		Bubbles,
		/** The Cube. */
		Cube,
		/** The Metasphere. */
		Metasphere,
		/** The Sphere. */
		Sphere
	}

	/** The Constant phantomNames. */
	public static final String[] phantomNames = new String[] { "Sphere",
			"Cube", "Bubbles", "Metasphere" };

	/** The cols. */
	protected int cols;

	/** The fuzziness. */
	protected double fuzziness;

	/** The heaviside. */
	protected Heaviside heaviside = Heaviside.ARCTAN;

	/** The image. */
	protected ImageDataFloat image;

	/** The invert image. */
	protected boolean invertImage;

	/** The levelset. */
	protected ImageDataFloat levelset;;

	/** The noise level. */
	protected double noiseLevel = 0.1;

	/** The noise type. */
	protected NoiseType noiseType = NoiseType.Uniform;

	/** The randn. */
	Random randn;

	/** The rows. */
	protected int rows;

	/** The slices. */
	protected int slices;

	/** The surf. */
	protected EmbeddedSurface surf;

	/**
	 * Instantiates a new phantom simulator3 d.
	 * 
	 * @param dims
	 *            the dims
	 */
	public PhantomSimulator3D(Point3i dims) {
		this.rows = dims.x;
		this.cols = dims.y;
		this.slices = dims.z;
		levelset = new ImageDataFloat(rows, cols, slices);
		image = new ImageDataFloat(rows, cols, slices);
		randn = new Random(43897075348790543l);
	}

	/**
	 * Creates the.
	 * 
	 * @param phantom
	 *            the phantom
	 * @param dims
	 *            the dims
	 * 
	 * @return the phantom simulator3 d
	 */
	public static PhantomSimulator3D create(Phantom phantom, Point3i dims) {
		switch (phantom) {
		case Sphere:
			return new PhantomSphere(dims);
		case Metasphere:
			return new PhantomMetasphere(dims);
		case Cube:
			return new PhantomCube(dims);
		case Bubbles:
			return new PhantomBubbles(dims);
		}
		return null;
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		return VersionUtil.parseRevisionNumber("$Revision: 1.5 $");
	}

	/**
	 * Heaviside derivative.
	 * 
	 * @param val
	 *            the val
	 * @param fuzziness
	 *            the fuzziness
	 * @param heavy
	 *            the heavy
	 * 
	 * @return the double
	 */
	public static double heavisideDerivative(double val, double fuzziness,
			Heaviside heavy) {
		double invPI = 1.0 / Math.PI;
		double invFuzziness = 1.0 / fuzziness;
		switch (heavy) {
		case ARCTAN:
			return (invPI * fuzziness / (fuzziness * fuzziness + val * val));
		case BINARY:
			return (val == 0) ? 1 : 0;
		case SIGMOID:
			double exp = Math.exp(-val / fuzziness);
			return 2 * invFuzziness * exp / ((1 + exp) * (1 + exp));
		case SIN:
			if (val > fuzziness) {
				return 0;
			} else if (val < -fuzziness) {
				return 0;
			} else {
				return (0.5 * invFuzziness * (1 + Math.cos(invFuzziness * val
						* Math.PI)));
			}
		default:
			return (val == 0) ? 1 : 0;

		}
	}

	/**
	 * Gets the image.
	 * 
	 * @return the image
	 */
	public ImageDataFloat getImage() {
		return image;
	}

	/**
	 * Gets the levelset.
	 * 
	 * @return the levelset
	 */
	public ImageDataFloat getLevelset() {
		return levelset;
	}

	/**
	 * Gets the surface.
	 * 
	 * @return the surface
	 */
	public EmbeddedSurface getSurface() {
		return surf;
	}

	/**
	 * Sets the fuzziness.
	 * 
	 * @param fuzziness
	 *            the new fuzziness
	 */
	public void setFuzziness(double fuzziness) {
		this.fuzziness = fuzziness;
	}

	/**
	 * Sets the heaviside.
	 * 
	 * @param heaviside
	 *            the new heaviside
	 */
	public void setHeaviside(Heaviside heaviside) {
		this.heaviside = heaviside;
	}

	/**
	 * Sets the invert image.
	 *
	 * @param invertImage the new invert image
	 */
	public void setInvertImage(boolean invertImage) {
		this.invertImage = invertImage;
	}

	/**
	 * Sets the noise level.
	 * 
	 * @param noiseLevel
	 *            the new noise level
	 */
	public void setNoiseLevel(double noiseLevel) {
		this.noiseLevel = noiseLevel;
	}

	/**
	 * Sets the noise type.
	 * 
	 * @param noiseType
	 *            the new noise type
	 */
	public void setNoiseType(NoiseType noiseType) {
		this.noiseType = noiseType;
	}

	/**
	 * Solve.
	 */
	public abstract void solve();

	/**
	 * Finish.
	 */
	protected void finish() {

		DistanceField3D df = new DistanceField3D();
		levelset = df.solve(levelset, 10);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < slices; k++) {
					double l = levelset.getDouble(i, j, k);
					double noise = 0;
					switch (noiseType) {
					case Uniform:
						noise = noiseLevel * (2 * randn.nextDouble() - 1);
						break;
					case Gaussian:
						noise = noiseLevel * randn.nextGaussian();
						break;
					}
					double v = noise + heaviside(l, fuzziness, heaviside);
					if (invertImage) {
						image.set(i, j, k, 1 - v);
					} else {
						image.set(i, j, k, v);
					}
				}
			}
		}
		IsoSurfaceGenerator isosurf = new IsoSurfaceGenerator();
		surf = isosurf.solve(levelset, 0);
		surf.setName(image.getName());
	}

	/**
	 * Heaviside.
	 * 
	 * @param val
	 *            the val
	 * @param fuzziness
	 *            the fuzziness
	 * @param heavy
	 *            the heavy
	 * 
	 * @return the double
	 */
	public static double heaviside(double val, double fuzziness, Heaviside heavy) {
		double invPI = 1.0 / Math.PI;
		double invFuzziness = 1.0 / fuzziness;
		switch (heavy) {
		case ARCTAN:
			return (0.5 + invPI * Math.atan(invFuzziness * val));
		case BINARY:
			return (val > 0) ? 1 : 0;
		case SIGMOID:
			double exp = Math.exp(-val / fuzziness);
			return ((1 - exp) / (1 + exp));
		case SIN:
			if (val > fuzziness) {
				return 1;
			} else if (val < -fuzziness) {
				return 0;
			} else {
				return (0.5 * (1 + val * invFuzziness + invPI
						* Math.sin(invFuzziness * val * Math.PI)));
			}
		default:
			return (val > 0) ? 1 : 0;

		}
	}
}
