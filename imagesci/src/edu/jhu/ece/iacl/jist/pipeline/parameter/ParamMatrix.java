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
package edu.jhu.ece.iacl.jist.pipeline.parameter;

import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import Jama.Matrix;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamMatrixFactory;
import edu.jhu.ece.iacl.jist.utility.JistXMLUtil;

// TODO: Auto-generated Javadoc
/**
 * Number Parameter storage.
 * 
 * @author Blake Lucas
 */
public class ParamMatrix extends ParamModel<Matrix> {

	/** The cols. */
	protected int cols;

	/** The matrix. */
	protected Matrix matrix;

	/** The rows. */
	protected int rows;

	/**
	 * Instantiates a new param matrix.
	 */
	public ParamMatrix() {
		factory = new ParamMatrixFactory(this);
	}

	/**
	 * Instantiates a new param matrix.
	 * 
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 */
	public ParamMatrix(int rows, int cols) {
		super();

		matrix = new Matrix(rows, cols);
		this.rows = rows;
		this.cols = cols;
	}

	/**
	 * Instantiates a new param matrix.
	 * 
	 * @param mat
	 *            the mat
	 */
	public ParamMatrix(Matrix mat) {
		super();
		this.matrix = mat;
		this.rows = mat.getRowDimension();
		this.cols = mat.getColumnDimension();
	}

	/**
	 * Instantiates a new param matrix.
	 * 
	 * @param name
	 *            the name
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 */
	public ParamMatrix(String name, int rows, int cols) {
		super();
		setName(name);
		matrix = new Matrix(rows, cols);
		this.rows = rows;
		this.cols = cols;
	}

	/**
	 * Instantiates a new param matrix.
	 * 
	 * @param name
	 *            the name
	 * @param mat
	 *            the mat
	 */
	public ParamMatrix(String name, Matrix mat) {
		super();
		setName(name);
		this.matrix = mat;
		this.rows = mat.getRowDimension();
		this.cols = mat.getColumnDimension();
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#probeDefaultValue()
	 */
	@Override
	public String probeDefaultValue() {
		if (matrix == null) {
			return null;
		} else {
			return getXMLValue();
		}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getXMLValue()
	 */
	@Override
	public String getXMLValue() {
		StringWriter sw = new StringWriter();
		for (int i = 0; i < rows; i++) {
			if (i > 0) {
				sw.append(";");
			}
			for (int j = 0; j < cols; j++) {
				if (j > 0) {
					;
				}
				sw.append(" ");
				sw.append(matrix.get(i, j) + "");
			}
		}
		return sw.toString();

	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#setXMLValue(java.lang.String)
	 */
	@Override
	public void setXMLValue(String arg) {
		String[] rows = arg.trim().split(";");
		double[][] vals = new double[rows.length][];
		for (int i = 0; i < rows.length; i++) {
			String[] cols = rows[i].trim().split("\\s+");
			vals[i] = new double[cols.length];
			for (int j = 0; j < cols.length; j++) {
				vals[i][j] = Double.valueOf(cols[j]);
			}
		}
		setValue(new Matrix(vals));
	}

	/**
	 * Set the parameter. The value must be of Number type.
	 * 
	 * @param matrix
	 *            the matrix
	 */
	@Override
	public void setValue(Matrix matrix) {
		this.matrix = matrix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#clone()
	 */
	@Override
	public ParamMatrix clone() {
		ParamMatrix param = new ParamMatrix((Matrix) matrix.clone());
		param.setName(getName());
		param.label = this.label;
		param.shortLabel = shortLabel;
		param.cliTag = cliTag;
		return param;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#equals(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel)
	 */
	@Override
	public boolean equals(ParamModel<Matrix> model) {
		Matrix mat1 = this.getValue();
		Matrix mat2 = model.getValue();
		if (mat1.getRowDimension() != mat2.getRowDimension()
				|| mat1.getColumnDimension() != mat2.getColumnDimension()) {
			return false;
		}
		int rows = mat1.getRowDimension();
		int cols = mat1.getColumnDimension();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (mat1.get(i, j) != mat2.get(i, j)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Gets the cols.
	 * 
	 * @return the cols
	 */
	public int getCols() {
		return cols;
	}

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getHumanReadableDataType()
	 */
	@Override
	public String getHumanReadableDataType() {
		return "matrix semi-colon delimited rows in text";
	}

	/**
	 * Gets the rows.
	 * 
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#getValue()
	 */
	@Override
	public Matrix getValue() {
		return matrix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.PipePort#init()
	 */
	@Override
	public void init() {
		connectible = true;
		factory = new ParamMatrixFactory(this);
	}

	/**
	 * Sets the cols.
	 * 
	 * @param cols
	 *            the new cols
	 */
	public void setCols(int cols) {
		this.cols = cols;
	}

	/**
	 * Sets the rows.
	 * 
	 * @param rows
	 *            the new rows
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * Sets the value.
	 * 
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param val
	 *            the val
	 */
	public void setValue(int i, int j, double val) {
		matrix.set(i, j, val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#toString()
	 */
	@Override
	public String toString() {
		if (matrix == null) {
			return null;
		}
		String str = "<HTML><TABLE border=0>";
		for (int i = 0; i < matrix.getRowDimension(); i++) {
			str += "<TR>";
			for (int j = 0; j < matrix.getColumnDimension(); j++) {
				str += "<TD align='right'>" + matrix.get(i, j) + "</TD>";
			}
			str += "</TR>";
		}
		str += "</TABLE></HTML>";
		return str;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#validate()
	 */
	@Override
	public void validate() throws InvalidParameterException {
		if (matrix == null || rows != matrix.getRowDimension()
				|| cols != matrix.getColumnDimension()) {
			throw new InvalidParameterException(this);
		}
	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlDecodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public void xmlDecodeParam(Document document, Element el) {
		super.xmlDecodeParam(document, el);
		rows = Integer.valueOf(JistXMLUtil.xmlReadTag(el, "rows"));
		cols = Integer.valueOf(JistXMLUtil.xmlReadTag(el, "cols"));
		matrix = new Matrix(rows, cols);

		NodeList nl = JistXMLUtil.xmlReadElement(el, "matrix").getChildNodes();
		String[] entry = nl.item(0).getNodeValue().split(" ");
		int ii = 0, jj = 0;
		for (int i = 0; i < entry.length; i++) {
			matrix.set(ii, jj, Double.valueOf(entry[i]));
			jj++;
			if (jj >= cols) {
				ii++;
				jj = 0;
			}
		}

	};

	/* (non-Javadoc)
	 * @see edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel#xmlEncodeParam(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public boolean xmlEncodeParam(Document document, Element parent) {
		super.xmlEncodeParam(document, parent);
		Element em;
		em = document.createElement("rows");
		em.appendChild(document.createTextNode(rows + ""));
		parent.appendChild(em);

		em = document.createElement("cols");
		em.appendChild(document.createTextNode(cols + ""));
		parent.appendChild(em);

		em = document.createElement("matrix");
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				em.appendChild(document.createTextNode(matrix.get(i, j) + " "));
			}
		}

		parent.appendChild(em);

		return true;
	}
}
