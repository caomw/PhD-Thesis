/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 */
package edu.jhu.ece.iacl.jist.structures.image;

import java.util.Arrays;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageHeader.
 * 
 * @author Blake Lucas (bclucas@jhu.edu) Sagittal da
 */
public class ImageHeader implements Cloneable {

	/**
	 * The Enum AxisOrientation.
	 */
	public enum AxisOrientation {
		/** The A2 p_ type. */
		A2P_TYPE,
		/** The I2 s_ type. */
		I2S_TYPE,
		/** The L2 r_ type. */
		L2R_TYPE,
		/** The P2 a_ type. */
		P2A_TYPE,
		/** The R2 l_ type. */
		R2L_TYPE,
		/** The S2 i_ type. */
		S2I_TYPE,
		/** The UNKNOWN. */
		UNKNOWN;

		/**
		 * Correct for direction.
		 *
		 * @param in the in
		 * @param direction the direction
		 * @return the axis orientation
		 */
		static AxisOrientation correctForDirection(AxisOrientation in,
				int direction) {
			if (true) {
				return in;
			} else {
				switch (in) {
				/** The UNKNOWN. */
				case UNKNOWN:
					return UNKNOWN;
					/** The R2 l_ type. */
				case R2L_TYPE:
					if (direction < 0) {
						return L2R_TYPE;
					} else {
						return R2L_TYPE;
					}
					/** The L2 r_ type. */
				case L2R_TYPE:
					if (direction > 0) {
						return L2R_TYPE;
					} else {
						return R2L_TYPE;
					}
					/** The P2 a_ type. */
				case P2A_TYPE:
					if (direction < 0) {
						return A2P_TYPE;
					} else {
						return P2A_TYPE;
					}

					/** The A2 p_ type. */
				case A2P_TYPE:
					if (direction > 0) {
						return A2P_TYPE;
					} else {
						return P2A_TYPE;
					}
					/** The I2 s_ type. */
				case I2S_TYPE:
					if (direction < 0) {
						return S2I_TYPE;
					} else {
						return I2S_TYPE;
					}

					/** The S2 i_ type. */
				case S2I_TYPE:
					if (direction > 0) {
						return S2I_TYPE;
					} else {
						return I2S_TYPE;
					}
				default:
					return UNKNOWN;

				}
			}
		}
	}

	/**
	 * The Enum Compression.
	 */
	public enum Compression {
		/** The GZIP. */
		GZIP,
		/** The NONE. */
		NONE,
		/** The ZIP. */
		ZIP
	}

	/**
	 * The Enum Endianess.
	 */
	public enum Endianess {
		/** The BIG. */
		BIG,
		/** The LITTLE. */
		LITTLE
	}

	/**
	 * The Enum ImageModality.
	 */
	public enum ImageModality {
		/** The BIOMAGENETI c_ imaging. */
		BIOMAGENETIC_IMAGING,
		/** The COLO r_ flo w_ doppler. */
		COLOR_FLOW_DOPPLER,
		/** The COMPUTE d_ radiography. */
		COMPUTED_RADIOGRAPHY,
		/** The COMPUTE d_ tomography. */
		COMPUTED_TOMOGRAPHY,
		/** The DIAPHANOGRAPHY. */
		DIAPHANOGRAPHY,
		/** The DIGITA l_ radiography. */
		DIGITAL_RADIOGRAPHY,
		/** The DUPLE x_ doppler. */
		DUPLEX_DOPPLER,
		/** The ENDOSCOPY. */
		ENDOSCOPY,
		/** The EXTERNA l_ camer a_ photography. */
		EXTERNAL_CAMERA_PHOTOGRAPHY,
		/** The FA. */
		FA,
		/** The GENERA l_ microscopy. */
		GENERAL_MICROSCOPY,
		/** The HARDCODY. */
		HARDCODY,
		/** The INTRAORA l_ radiography. */
		INTRAORAL_RADIOGRAPHY,
		/** The LASE r_ surfac e_ scan. */
		LASER_SURFACE_SCAN,
		/** The MAGNETI c_ resonance. */
		MAGNETIC_RESONANCE,
		/** The MAGNETI c_ resonanc e_ angiography. */
		MAGNETIC_RESONANCE_ANGIOGRAPHY,
		/** The MAGNETI c_ resonanc e_ spectroscopy. */
		MAGNETIC_RESONANCE_SPECTROSCOPY,
		/** The MAMMOGRAPHY. */
		MAMMOGRAPHY,
		/** The NUCLEA r_ medicine. */
		NUCLEAR_MEDICINE,
		/** The OTHER. */
		OTHER,
		/** The PANORAMI c_ xray. */
		PANORAMIC_XRAY,
		/** The POSITRO n_ emissio n_ tomography. */
		POSITRON_EMISSION_TOMOGRAPHY,
		/** The RADI o_ fluoroscopy. */
		RADIO_FLUOROSCOPY,
		/** The RADIOGRAPHI c_ imaging. */
		RADIOGRAPHIC_IMAGING,
		/** The RADIOTHERAP y_ dose. */
		RADIOTHERAPY_DOSE,
		/** The RADIOTHERAP y_ image. */
		RADIOTHERAPY_IMAGE,
		/** The RADIOTHERAP y_ plan. */
		RADIOTHERAPY_PLAN,
		/** The RADIOTHERAP y_ record. */
		RADIOTHERAPY_RECORD,
		/** The RADIOTHERAP y_ structur e_ set. */
		RADIOTHERAPY_STRUCTURE_SET,
		/** The RE d_ free. */
		RED_FREE,
		/** The SINGL e_ photo n_ emissio n_ compute d_ tomography. */
		SINGLE_PHOTON_EMISSION_COMPUTED_TOMOGRAPHY,
		/** The SLID e_ microscopy. */
		SLIDE_MICROSCOPY,
		/** The THERMOGRAPHY. */
		THERMOGRAPHY,
		/** The ULTRASOUND. */
		ULTRASOUND,
		/** The UNKNOWN. */
		UNKNOWN,
		/** The XRA y_ angiography. */
		XRAY_ANGIOGRAPHY
	};

	/**
	 * The Enum ImageOrientation.
	 */
	public enum ImageOrientation {
		/** The AXIAL. */
		AXIAL,
		/** The CORONAL. */
		CORONAL,
		/** The SAGITTAL. */
		SAGITTAL,
		/** The UNKNOWN. */
		UNKNOWN
	}

	/**
	 * The Enum MeasurementUnit.
	 */
	public enum MeasurementUnit {
		
		/** The ANGSTROMS. */
		ANGSTROMS, 
 /** The CENTIMETERS. */
 CENTIMETERS, 
 /** The DEGREES. */
 DEGREES, 
 /** The HOURS. */
 HOURS, 
 /** The HZ. */
 HZ, 
 /** The INCHES. */
 INCHES, 
 /** The KILOMETERS. */
 KILOMETERS, 
 /** The METERS. */
 METERS, 
 /** The MICROMETERS. */
 MICROMETERS, 
 /** The MICROSEC. */
 MICROSEC, 
 /** The MILES. */
 MILES, 
 /** The MILLIMETERS. */
 MILLIMETERS, 
 /** The MILLISEC. */
 MILLISEC, 
 /** The MINUTES. */
 MINUTES, 
 /** The NANOMETERS. */
 NANOMETERS, 
 /** The NANOSEC. */
 NANOSEC, 
 /** The NULL. */
 NULL, 
 /** The PPM. */
 PPM, 
 /** The RADS. */
 RADS, 
 /** The SECONDS. */
 SECONDS, 
 /** The UNKNOW n_ measure. */
 UNKNOWN_MEASURE,
	};

	/** The Constant axisOrientationNames. */
	public static final String[] axisOrientationNames = { "Unknown",
			"Right to Left", "Left to Right", "Posterior to Anterior",
			"Anterior to Posterior", "Inferior to Superior",
			"Superior to Inferior" };

	/** The Constant imageOrientationNames. */
	public static final String[] imageOrientationNames = { "Axial", "Coronal",
			"Sagittal", "Unkown" };;

	/** The Constant measurementUnitNames. */
	public static final String[] measurementUnitNames = { "NULL", "UNKNOWN",
			"INCHES", "CENTIMETERS", "ANGSTROMS", "NANOMETERS", "MICROMETERS",
			"MILLIMETERS", "METERS", "KILOMETERS", "MILES", "NANOSEC",
			"MICROSEC", "MILLISEC", "SECONDS", "MINUTES", "HOURS", "HZ", "PPM",
			"RADS", "DEGREES" };;

	/** The axis orientation. */
	protected AxisOrientation[] axisOrientation;

	/** The compression type. */
	protected Compression compressionType;

	/** The default header. */
	protected boolean defaultHeader;

	/** The dim resolutions. */
	protected float[] dimResolutions;

	/** The endianess. */
	protected Endianess endianess;

	/** The image orientation. */
	protected ImageOrientation imageOrientation;

	/** The modality. */
	protected ImageModality modality;

	/** The origin. */
	protected float[] origin;

	/** The slice thickness. */
	protected float sliceThickness;

	/** The units of measure. */
	protected MeasurementUnit[] unitsOfMeasure;

	/**
	 * Instantiates a new image header.
	 */
	public ImageHeader() {
		axisOrientation = new AxisOrientation[] { AxisOrientation.UNKNOWN,
				AxisOrientation.UNKNOWN, AxisOrientation.UNKNOWN };
		imageOrientation = ImageOrientation.UNKNOWN;
		modality = ImageModality.UNKNOWN;
		origin = new float[] { 0, 0, 0 };
		dimResolutions = new float[] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };
		unitsOfMeasure = new MeasurementUnit[] { MeasurementUnit.MILLIMETERS,
				MeasurementUnit.MILLIMETERS, MeasurementUnit.MILLIMETERS,
				MeasurementUnit.SECONDS, MeasurementUnit.UNKNOWN_MEASURE };
		compressionType = Compression.NONE;
		endianess = Endianess.LITTLE;
		sliceThickness = 0;
		defaultHeader = true;
	}

	/**
	 * Copy geometry from one image header into this one. Does not alter image
	 * dimensions.
	 * 
	 * @param copyme
	 *            the header from which to copy data
	 */
	public void copyGeometry(ImageHeader copyme) {
		setAxisOrientation(copyme.getAxisOrientation());
		setDimResolutions(copyme.getDimResolutions());
		setImageOrientation(copyme.getImageOrientation());
		setOrigin(copyme.getOrigin());
		setSliceThickness(copyme.getSliceThickness());
		setUnitsOfMeasure(copyme.getUnitsOfMeasure());

	}

	/**
	 * Sets the axis orientation.
	 * 
	 * @param axisOrientation
	 *            the axisOrientation to set
	 */
	public void setAxisOrientation(AxisOrientation[] axisOrientation) {
		this.axisOrientation = axisOrientation;
		defaultHeader = false;
	}

	/**
	 * Sets the dim resolutions.
	 * 
	 * @param dimResolutions
	 *            the dimResolutions to set
	 */
	public void setDimResolutions(float[] dimResolutions) {
		defaultHeader = false;
		this.dimResolutions = dimResolutions;
	}

	/**
	 * Sets the image orientation.
	 * 
	 * @param imageOrientation
	 *            the imageOrientation to set
	 */
	public void setImageOrientation(ImageOrientation imageOrientation) {
		defaultHeader = false;
		this.imageOrientation = imageOrientation;
	}

	/**
	 * Sets the origin.
	 * 
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(float[] origin) {
		defaultHeader = false;
		this.origin = origin;
	}

	/**
	 * Sets the slice thickness.
	 * 
	 * @param sliceThickness
	 *            the sliceThickness to set
	 */
	public void setSliceThickness(float sliceThickness) {
		defaultHeader = false;
		this.sliceThickness = sliceThickness;
	}

	/**
	 * Sets the units of measure.
	 * 
	 * @param unitsOfMeasure
	 *            the unitsOfMeasure to set
	 */
	public void setUnitsOfMeasure(MeasurementUnit[] unitsOfMeasure) {
		defaultHeader = false;
		this.unitsOfMeasure = unitsOfMeasure;
	}

	/**
	 * NOTE: This method is a stub.
	 * 
	 * @param hdr
	 *            - Image header to compare current header with
	 * @return true - geometries are equivalent. false - they are not equal
	 */
	public boolean hasComparableGeometry(ImageHeader hdr) {
		for (int i = 0; i < dimResolutions.length; i++) {
			if (dimResolutions[i] != hdr.dimResolutions[i]) {
				return false;
			}
			if (origin[i] != hdr.origin[i]) {
				return false;
			}
		}
		if (getSliceThickness() != hdr.getSliceThickness()) {
			return false;
		}
		if (axisOrientation != hdr.axisOrientation) {
			return false;
		}
		if (imageOrientation != hdr.imageOrientation) {
			return false;
		}
		for (int i = 0; i < unitsOfMeasure.length; i++) {
			if (unitsOfMeasure[i] != hdr.unitsOfMeasure[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the slice thickness.
	 * 
	 * @return the sliceThickness
	 */
	public float getSliceThickness() {
		return sliceThickness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ImageHeader clone() {
		ImageHeader header = new ImageHeader();
		header.axisOrientation = new AxisOrientation[3];
		header.axisOrientation[0] = axisOrientation[0];
		header.axisOrientation[1] = axisOrientation[1];
		header.axisOrientation[2] = axisOrientation[2];
		header.imageOrientation = imageOrientation;
		header.modality = modality;
		header.origin = origin;
		header.dimResolutions = Arrays.copyOf(dimResolutions,
				dimResolutions.length);
		header.unitsOfMeasure = Arrays.copyOf(unitsOfMeasure,
				unitsOfMeasure.length);
		header.compressionType = compressionType;
		header.endianess = endianess;
		header.sliceThickness = sliceThickness;
		header.defaultHeader = defaultHeader;
		return header;
	}

	/**
	 * Gets the axis orientation.
	 * 
	 * @return the axisOrientation
	 */
	public AxisOrientation[] getAxisOrientation() {
		return axisOrientation;
	}

	/**
	 * Gets the compression type.
	 * 
	 * @return the compressionType
	 */
	public Compression getCompressionType() {
		return compressionType;
	}

	/**
	 * Gets the dim resolutions.
	 * 
	 * @return the dimResolutions
	 */
	public float[] getDimResolutions() {
		return dimResolutions;
	}

	/**
	 * Gets the endianess.
	 * 
	 * @return the endianess
	 */
	public Endianess getEndianess() {
		return endianess;
	}

	/**
	 * Gets the image orientation.
	 * 
	 * @return the imageOrientation
	 */
	public ImageOrientation getImageOrientation() {
		return imageOrientation;
	}

	/**
	 * Gets the modality.
	 * 
	 * @return the modality
	 */
	public ImageModality getModality() {
		return modality;
	}

	/**
	 * Gets the origin.
	 * 
	 * @return the origin
	 */
	public float[] getOrigin() {
		return origin;
	}

	/**
	 * Gets the units of measure.
	 * 
	 * @return the unitsOfMeasure
	 */
	public MeasurementUnit[] getUnitsOfMeasure() {
		return unitsOfMeasure;
	}

	/**
	 * Checks if is default header.
	 * 
	 * @return true, if is default header
	 */
	public boolean isDefaultHeader() {
		return defaultHeader;
	}

	/**
	 * Compares the orientation of this header with another header.
	 *
	 * @param hdr the hdr
	 * @return true - orientations are the same. false otherwise.
	 */
	public boolean isSameOrientation(ImageHeader hdr) {
		int thisnumdims = this.dimResolutions.length;
		int tgtnumdims = hdr.dimResolutions.length;
		if (thisnumdims != tgtnumdims) {
			return false;
		}
		for (int i = 0; i < thisnumdims; i++) {
			if (this.axisOrientation[i] != hdr.getAxisOrientation()[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares the resolution of this header with another header.
	 *
	 * @param hdr the hdr
	 * @return true - resolutions are the same. false otherwise.
	 */
	public boolean isSameResolution(ImageHeader hdr) {
		int thisnumdims = this.dimResolutions.length;
		int tgtnumdims = hdr.dimResolutions.length;
		if (thisnumdims != tgtnumdims) {
			return false;
		}
		for (int i = 0; i < thisnumdims; i++) {
			if (this.dimResolutions[i] != hdr.getDimResolutions()[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sets the compression type.
	 * 
	 * @param compressionType
	 *            the compressionType to set
	 */
	public void setCompressionType(Compression compressionType) {
		defaultHeader = false;
		this.compressionType = compressionType;
	}

	/**
	 * Sets the endianess.
	 * 
	 * @param endianess
	 *            the endianess to set
	 */
	public void setEndianess(Endianess endianess) {
		defaultHeader = false;
		this.endianess = endianess;
	}

	/**
	 * Sets the modality.
	 * 
	 * @param modality
	 *            the modality to set
	 */
	public void setModality(ImageModality modality) {
		defaultHeader = false;
		this.modality = modality;
	}

}
