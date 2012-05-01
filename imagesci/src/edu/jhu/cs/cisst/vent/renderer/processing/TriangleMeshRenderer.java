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
package edu.jhu.cs.cisst.vent.renderer.processing;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.j3d.BoundingBox;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLPointerFunc;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import processing.core.PMatrix3D;
import processing.opengl2.PGraphicsOpenGL2;

import com.jogamp.common.nio.Buffers;

import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamDoubleSliderInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.structures.geom.NormalGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class SurfaceRenderer.
 */
public class TriangleMeshRenderer extends RendererProcessing3D {

	/** The applet. */
	protected VisualizationProcessing3D applet = null;

	/** The color buffer. */
	protected FloatBuffer colorBuffer;
	/** The diffuse color. */
	protected Color diffuseColor = new Color(0, 102, 204);
	/** The diffuse color param. */
	protected ParamColor diffuseColorParam;
	/** The gouraud. */
	protected boolean gouraud = false;

	/** The index buffer. */
	protected IntBuffer indexBuffer;

	/** The name. */
	protected String name = "Triangle Mesh";

	/** The normal buffer. */
	protected FloatBuffer normalBuffer;

	/** The tex map buffer. */
	protected FloatBuffer texMapBuffer;

	/** The texture index. */
	protected int textureIndex = -1;

	/** The transform. */
	protected PMatrix3D transform = null;

	/** The transparency. */
	float transparency = 1.0f;

	/** The transparency param. */
	protected ParamDouble transparencyParam;

	/** The vertex buffer. */
	protected FloatBuffer vertexBuffer;

	/** The vertex component length. */
	protected int vertexComponentLength = 3;
	/** The visible. */
	protected boolean visible = true;

	/** The visible param. */
	protected ParamBoolean visibleParam, gouraudParam, wireframeParam,
			showTextureParam, showColorParam;

	/** The flip normals. */
	protected boolean wireframe = false;

	/**
	 * Instantiates a new surface renderer.
	 * 
	 * @param applet
	 *            the applet
	 * @param surf
	 *            the surf
	 */
	public TriangleMeshRenderer(VisualizationProcessing3D applet,
			EmbeddedSurface surf) {
		this.applet = applet;
		this.name = surf.getName();
		Point3f[] vertCopy = surf.getVertexCopy();
		int[] indexes = surf.getIndexCopy();
		Vector3f[] normCopy = NormalGenerator.generate(vertCopy, indexes);
		bbox = new BoundingBox(new Point3d(1E10, 1E10, 1E10), new Point3d(
				-1E10, -1E10, -1E10));
		vertexBuffer = Buffers.newDirectFloatBuffer(vertCopy.length * 3);
		normalBuffer = Buffers.newDirectFloatBuffer(normCopy.length * 3);
		for (Point3f pt : vertCopy) {
			bbox.combine(new Point3d(pt));
			vertexBuffer.put(pt.x);
			vertexBuffer.put(pt.y);
			vertexBuffer.put(pt.z);
		}
		vertexBuffer.rewind();
		for (Vector3f pt : normCopy) {
			normalBuffer.put(pt.x);
			normalBuffer.put(pt.y);
			normalBuffer.put(pt.z);
		}
		normalBuffer.rewind();
		indexBuffer = Buffers.newDirectIntBuffer(indexes);
	}

	/**
	 * Instantiates a new triangle mesh renderer.
	 * 
	 * @param applet
	 *            the applet
	 * @param vertexBuffer
	 *            the vertex buffer
	 * @param colorBuffer
	 *            the color buffer
	 * @param indexBuffer
	 *            the index buffer
	 * @param normalBuffer
	 *            the normal buffer
	 * @param texMapBuffer
	 *            the tex map buffer
	 * @param len
	 *            the len
	 */
	public TriangleMeshRenderer(VisualizationProcessing3D applet,
			FloatBuffer vertexBuffer, FloatBuffer colorBuffer,
			IntBuffer indexBuffer, FloatBuffer normalBuffer,
			FloatBuffer texMapBuffer, int len) {
		this.applet = applet;
		this.indexBuffer = indexBuffer;
		this.vertexBuffer = vertexBuffer;
		this.colorBuffer = colorBuffer;
		this.normalBuffer = normalBuffer;
		this.vertexComponentLength = len;
		this.texMapBuffer = texMapBuffer;
		bbox = new BoundingBox(new Point3d(1E10, 1E10, 1E10), new Point3d(
				-1E10, -1E10, -1E10));
		while (vertexBuffer.hasRemaining()) {
			bbox.combine(new Point3d(vertexBuffer.get(), vertexBuffer.get(),
					vertexBuffer.get()));

		}
		vertexBuffer.rewind();
	}

	/**
	 * Instantiates a new triangle mesh renderer.
	 * 
	 * @param applet
	 *            the applet
	 * @param vertexBuffer
	 *            the vertex buffer
	 * @param colorBuffer
	 *            the color buffer
	 * @param indexBuffer
	 *            the index buffer
	 * @param normalBuffer
	 *            the normal buffer
	 * @param len
	 *            the len
	 */
	public TriangleMeshRenderer(VisualizationProcessing3D applet,
			FloatBuffer vertexBuffer, FloatBuffer colorBuffer,
			IntBuffer indexBuffer, FloatBuffer normalBuffer, int len) {
		this.applet = applet;
		this.indexBuffer = indexBuffer;
		this.vertexBuffer = vertexBuffer;
		this.colorBuffer = colorBuffer;
		this.normalBuffer = normalBuffer;
		this.vertexComponentLength = len;
		bbox = new BoundingBox(new Point3d(1E10, 1E10, 1E10), new Point3d(
				-1E10, -1E10, -1E10));
		while (vertexBuffer.hasRemaining()) {
			bbox.combine(new Point3d(vertexBuffer.get(), vertexBuffer.get(),
					vertexBuffer.get()));

		}
		vertexBuffer.rewind();
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		if (model == visibleParam) {
			setVisible(visibleParam.getValue());
		} else if (model == diffuseColorParam) {
			diffuseColor = diffuseColorParam.getValue();
		} else if (model == wireframeParam) {
			wireframe = wireframeParam.getValue();
		} else if (model == gouraudParam) {
			gouraud = gouraudParam.getValue();
		} else if (model == transparencyParam) {
			transparency = transparencyParam.getFloat();
		}
	}

	/**
	 * Update visualization parameters.
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#updateVisualizationParameters()
	 */
	@Override
	public void updateVisualizationParameters() {
		setVisible(visibleParam.getValue());
		diffuseColor = diffuseColorParam.getValue();
		wireframe = wireframeParam.getValue();
		transparency = transparencyParam.getFloat();
		gouraud = gouraudParam.getValue();
	}

	/**
	 * Sets the visible.
	 * 
	 * @param visible
	 *            the new visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		// TODO Auto-generated method stub
		visualizationParameters.setName(name);
		visualizationParameters.add(diffuseColorParam = new ParamColor(
				"Diffuse Color", diffuseColor));
		visualizationParameters.add(transparencyParam = new ParamDouble(
				"Transparency", 0, 1, transparency));
		transparencyParam.setInputView(new ParamDoubleSliderInputView(
				transparencyParam, 4, false));
		visualizationParameters.add(gouraudParam = new ParamBoolean(
				"Gouraud Shading", gouraud));
		visualizationParameters.add(wireframeParam = new ParamBoolean(
				"Wireframe", wireframe));
		visualizationParameters.add(visibleParam = new ParamBoolean(
				"Triangles", visible));
		visualizationParameters.add(showColorParam = new ParamBoolean(
				"Color Map", false));
		if (texMapBuffer != null) {
			visualizationParameters.add(showTextureParam = new ParamBoolean(
					"Texture Map", true));
		}
	}

	/**
	 * Draw.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#draw()
	 */
	@Override
	public void draw() {
		applet.pushMatrix();
		if (transform != null) {
			applet.applyMatrix(transform);
		}
		applet.pushStyle();
		final float scale = 0.0039215f;
		GL2 gl = (GL2) (((PGraphicsOpenGL2) applet.g).beginGL());
		gl.glEnable(GL.GL_LINE_SMOOTH);
		if (gouraud) {
			gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		} else {
			gl.glShadeModel(GLLightingFunc.GL_FLAT);
		}
		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
		gl.glVertexPointer(vertexComponentLength, GL.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);
		if (visible) {
			if (transparency < 0.99) {
				gl.glDisable(GLLightingFunc.GL_LIGHTING);
				gl.glDisable(GL.GL_DEPTH_TEST);
				if (texMapBuffer != null && textureIndex >= 0
						&& showTextureParam.getValue()) {
					gl.glEnable(GL.GL_TEXTURE_2D);

					gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
					gl.glBindTexture(GL.GL_TEXTURE_2D, textureIndex);
					gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, texMapBuffer);
				} else {
					gl.glColor4f(scale * diffuseColor.getRed(), scale
							* diffuseColor.getGreen(),
							scale * diffuseColor.getBlue(), transparency);
				}
				if (colorBuffer != null && showColorParam.getValue()) {
					gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
					gl.glColorPointer(4, GL.GL_FLOAT, 0, colorBuffer);
				}
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
				if (indexBuffer != null) {
					gl.glDrawElements(GL.GL_TRIANGLES, indexBuffer.capacity(),
							GL.GL_UNSIGNED_INT, indexBuffer);
				} else {
					gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexBuffer.capacity()
							/ vertexComponentLength);
				}
				if (colorBuffer != null && showColorParam.getValue()) {

					gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				}
				if (texMapBuffer != null) {
					gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
					gl.glDisable(GL.GL_TEXTURE_2D);
				}

				gl.glEnable(GL.GL_DEPTH_TEST);
			} else {
				gl.glEnable(GLLightingFunc.GL_LIGHTING);
				if (texMapBuffer != null && textureIndex >= 0
						&& showTextureParam.getValue()) {
					gl.glEnable(GL.GL_TEXTURE_2D);

					gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
					gl.glBindTexture(GL.GL_TEXTURE_2D, textureIndex);
					gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, texMapBuffer);
				} else {
					gl.glColor4f(scale * diffuseColor.getRed(), scale
							* diffuseColor.getGreen(),
							scale * diffuseColor.getBlue(), transparency);
				}
				if (colorBuffer != null && showColorParam.getValue()) {
					gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
					gl.glColorPointer(4, GL.GL_FLOAT, 0, colorBuffer);
				}
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
				if (indexBuffer != null) {
					gl.glDrawElements(GL.GL_TRIANGLES, indexBuffer.capacity(),
							GL.GL_UNSIGNED_INT, indexBuffer);
				} else {
					gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexBuffer.capacity()
							/ vertexComponentLength);
				}
				if (colorBuffer != null && showColorParam.getValue()) {

					gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
				}
				if (texMapBuffer != null) {
					gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
					gl.glDisable(GL.GL_TEXTURE_2D);
				}
			}
		}
		if (wireframe) {

			gl.glDisable(GLLightingFunc.GL_LIGHTING);
			gl.glShadeModel(GLLightingFunc.GL_FLAT);
			gl.glLineWidth(1.5f);
			gl.glColor4f(0, 0, 0, transparency);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
			if (indexBuffer != null) {
				gl.glDrawElements(GL.GL_TRIANGLES, indexBuffer.capacity(),
						GL.GL_UNSIGNED_INT, indexBuffer);
			} else {
				gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexBuffer.capacity()
						/ vertexComponentLength);
			}

			gl.glEnable(GLLightingFunc.GL_LIGHTING);
		}

		gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
		((PGraphicsOpenGL2) applet.g).endGL();

		((GL2) ((PGraphicsOpenGL2) applet.g).gl)
				.glEnable(GLLightingFunc.GL_LIGHTING);
		applet.popStyle();
		applet.popMatrix();
	}

	/**
	 * Sets the buffers.
	 * 
	 * @param vertexBuffer
	 *            the vertex buffer
	 * @param colorBuffer
	 *            the color buffer
	 * @param indexBuffer
	 *            the index buffer
	 * @param normalBuffer
	 *            the normal buffer
	 * @param texMapBuffer
	 *            the tex map buffer
	 * @param len
	 *            the len
	 */
	public void setBuffers(FloatBuffer vertexBuffer, FloatBuffer colorBuffer,
			IntBuffer indexBuffer, FloatBuffer normalBuffer,
			FloatBuffer texMapBuffer, int len) {
		this.indexBuffer = indexBuffer;
		this.vertexBuffer = vertexBuffer;
		this.colorBuffer = colorBuffer;
		this.normalBuffer = normalBuffer;
		this.texMapBuffer = texMapBuffer;
		this.vertexComponentLength = len;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the texture index.
	 * 
	 * @param index
	 *            the new texture index
	 */
	public void setTextureIndex(int index) {

		this.textureIndex = index;
	}

	/**
	 * Sets the transform.
	 * 
	 * @param mat
	 *            the new transform
	 */
	public void setTransform(PMatrix3D mat) {
		transform = mat;
	}

	/**
	 * Setup.
	 * 
	 * @see edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing#setup()
	 */
	@Override
	public void setup() {
	}

}
