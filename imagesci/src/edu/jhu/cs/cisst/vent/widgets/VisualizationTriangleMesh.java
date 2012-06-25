/**
 * JIST Extensions for Computer-Integrated Surgery
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
 * @author Blake Lucas
 */
package edu.jhu.cs.cisst.vent.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.net.URISyntaxException;

import javax.media.j3d.BoundingBox;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import data.PlaceHolder;

import processing.core.PImage;
import processing.core.PMatrix;
import Jama.Matrix;
import edu.jhu.cs.cisst.vent.VisualizationApplication;
import edu.jhu.cs.cisst.vent.VisualizationProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.RendererProcessing3D;
import edu.jhu.cs.cisst.vent.renderer.processing.TriangleMeshRenderer;
import edu.jhu.ece.iacl.jist.io.SurfaceReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamBoolean;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamColor;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamDouble;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamFile.DialogType;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamMatrix;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamPointFloat;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refreshable;
import edu.jhu.ece.iacl.jist.pipeline.view.input.Refresher;
import edu.jhu.ece.iacl.jist.structures.geom.EmbeddedSurface;
import edu.jhu.ece.iacl.jist.utility.VersionUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationTriangleMesh.
 */
public class VisualizationTriangleMesh extends VisualizationProcessing3D
		implements Refreshable, MouseWheelListener {

	/** The axis length. */
	protected float axisLength = 100;

	/** The bounding box. */
	protected BoundingBox bbox;

	/** The bg color. */
	protected Color bgColor = new Color(Color.white.getRGB());

	/** The bg color param. */
	protected ParamColor bgColorParam;

	/** The capture param. */
	ParamBoolean captureParam;

	/** The center. */
	protected Point3f center = new Point3f();

	/** The center param. */
	protected ParamPointFloat centerParam, autoRotateParam;

	/** The count cycle. */
	protected int countCycle = 0;

	/** The enable auto rotate. */
	protected ParamBoolean enableAutoRotate;

	/** The initial view. */
	protected PMatrix initialView;

	/** The last time. */
	protected long lastTime = 0;

	/** The output file param. */
	ParamFile outputFileParam;

	/** The pose. */
	protected Matrix4f pose = null;

	/** The pose param. */
	protected ParamMatrix poseParam;

	/** The refresher. */
	protected Refresher refresher;

	/** The refresh lock. */
	protected boolean refreshLock = false;

	/** The request update. */
	protected boolean requestUpdate = false;

	/** The rotation rate in radians. */
	protected float rotRate = 0.01f;

	/** The scale. */
	float scale = 1;

	/** The scale object. */
	protected float scaleObject = 1;

	/** The scale rate. */
	protected float scaleRate = 0.25f;

	/** The scene params. */
	protected ParamCollection sceneParams;

	/** The show axes. */
	protected boolean showAxes = true;

	/** The show axes param. */
	protected ParamBoolean showAxesParam;

	/** The translation in y. */
	float tx = 0, ty = 0;

	/** The scale param. */
	protected ParamDouble txParam, tyParam, scaleParam;

	/** The scale rate. */
	protected float wheelScaleRate = 0.1f;

	/**
	 * Instantiates a new visualization triangle mesh.
	 * 
	 */
	public VisualizationTriangleMesh() {
		super();

		pose = new Matrix4f();
		pose.setIdentity();
		setName("Visualize - Triangle Mesh");
	}

	/**
	 * Instantiates a new visualization triangle mesh.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public VisualizationTriangleMesh(int width, int height) {
		super(width, height);

		pose = new Matrix4f();
		pose.setIdentity();
		setName("Visualize - Triangle Mesh");
	}

	/**
	 * Creates the visualization parameters.
	 * 
	 * @param visualizationParameters
	 *            the visualization parameters
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationParameters#createVisualizationParameters(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection)
	 */
	@Override
	public void createVisualizationParameters(
			ParamCollection visualizationParameters) {
		super.createVisualizationParameters(visualizationParameters);
		resetScale();
		sceneParams = new ParamCollection("Scene Controls");
		sceneParams.add(bgColorParam = new ParamColor("Background", bgColor));
		sceneParams.add(poseParam = new ParamMatrix("Pose", 3, 4));
		updatePoseParam();
		sceneParams.add(scaleParam = new ParamDouble("Scale", 0, 1E10, scale));
		sceneParams.add(centerParam = new ParamPointFloat("Center", center));
		sceneParams.add(autoRotateParam = new ParamPointFloat("Rotation Speed",
				new Point3f(0, 0.01f, 0)));
		sceneParams.add(enableAutoRotate = new ParamBoolean(
				"Enable Auto-Rotation", false));
		sceneParams.add(txParam = new ParamDouble("Translation X", -1E10, 1E10,
				ty));
		sceneParams.add(tyParam = new ParamDouble("Translation Y", -1E10, 1E10,
				tx));
		sceneParams.add(captureParam = new ParamBoolean("Capture Screenshots",
				false));
		sceneParams.add(outputFileParam = new ParamFile("Output Directory",
				DialogType.DIRECTORY));
		outputFileParam.setValue(new File(""));
		visualizationParameters.add(sceneParams);

	}

	/**
	 * Reset scale.
	 */
	protected void resetScale() {
		bbox = new BoundingBox(new Point3d(1E10, 1E10, 1E10), new Point3d(
				-1E10, -1E10, -1E10));
		for (RendererProcessing3D renderer : renderers) {
			bbox.combine(renderer.getBoundingBox());
		}
		Point3d lower = new Point3d();
		Point3d upper = new Point3d();
		bbox.getLower(lower);
		bbox.getUpper(upper);
		center = new Point3f((float) (0.5 * (lower.x + upper.x)),
				(float) (0.5 * (lower.y + upper.y)),
				(float) (0.5 * (lower.z + upper.z)));
		scaleObject = Math.min(width, height)
				/ (float) Math.max(
						Math.max(upper.x - lower.x, upper.y - lower.y), upper.z
								- lower.z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing3D#draw()
	 */

	@Override
	public void draw() {
		float scaleCamera = (float) ((height / 2.0) / Math
				.tan(PI * 60.0 / 360.0));
		float xp = width / 2;
		float yp = height / 2;
		resetMatrix();
		camera(xp, yp, scaleCamera, xp, yp, 0, 0, 1, 0);

		background(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(),
				bgColor.getAlpha());

		translate(width / 2.0f - tx, height / 2.0f - ty);

		rotateZ(PI);

		applyMatrix(pose.m00, pose.m01, pose.m02, pose.m03, pose.m10, pose.m11,
				pose.m12, pose.m13, pose.m20, pose.m21, pose.m22, pose.m23,
				pose.m30, pose.m31, pose.m32, pose.m33);
		scale(-1, 1);
		scale(scaleObject * scale);
		translate(-center.x, -center.y, -center.z);
		super.draw();
		if (captureParam.getValue()) {
			if (lastTime % 60 == 0) {
				PImage img = captureScreenshot();
				String f = (new File(outputFileParam.getValue(), String.format(
						"screenshot_%05d.png", lastTime / 60)))
						.getAbsolutePath();
				System.out.println("Saving " + f);
				img.save(f);
				img.delete();
				Point3f rot = autoRotateParam.getValue();
				if (Math.abs(rot.x) > 1E-3f || Math.abs(rot.y) > 1E-3f
						|| Math.abs(rot.z) > 1E-3f) {
					applyRotation(rot.x, rot.y, rot.z);
					updatePoseParam();
				}
			}
			lastTime++;
		}
	}

	/**
	 * Key pressed.
	 * 
	 * @see processing.core.PApplet#mouseDragged()
	 */
	@Override
	public void keyPressed() {
		float rotx = 0, roty = 0, rotz = 0;
		if (keyCode == KeyEvent.VK_LEFT) {
			rotz = 5 * rotRate;
			if (rotz < 0) {
				rotz += 2 * Math.PI;
			}
			applyRotation(rotx, roty, rotz);
			updatePoseParam();
			requestUpdate = true;
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			rotz = 5 * rotRate;
			if (rotz > 2 * Math.PI) {
				rotz -= 2 * Math.PI;
			}
			applyRotation(rotx, roty, rotz);
			updatePoseParam();
			requestUpdate = true;
		} else if (keyCode == KeyEvent.VK_UP) {
			rotx = 5 * rotRate;
			if (rotx > 2 * Math.PI) {
				rotx -= 2 * Math.PI;
			}
			applyRotation(rotx, roty, rotz);
			updatePoseParam();
			requestUpdate = true;
		} else if (keyCode == KeyEvent.VK_DOWN) {
			rotx = -5 * rotRate;
			if (rotx < 0) {
				rotx += 2 * Math.PI;
			}
			applyRotation(rotx, roty, rotz);
			updatePoseParam();
			requestUpdate = true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#mouseDragged()
	 */
	@Override
	public void mouseDragged() {
		float rotx, roty;
		if (mouseButton == RIGHT) {
			scale = Math.max(0.01f, scale + scaleRate * (mouseY - pmouseY));
			scaleParam.setValue(scale);
			requestUpdate = true;
		} else if (mouseButton == LEFT && !keyPressed) {
			rotx = (pmouseY - mouseY) * rotRate;
			roty = (mouseX - pmouseX) * rotRate;
			if (rotx > 2 * Math.PI) {
				rotx -= 2 * Math.PI;
			}
			if (rotx < 0) {
				rotx += 2 * Math.PI;
			}
			if (roty > 2 * Math.PI) {
				roty -= 2 * Math.PI;
			}
			if (roty < 0) {
				roty += 2 * Math.PI;
			}
			applyRotation(-rotx, -roty, 0);
			updatePoseParam();
			requestUpdate = true;
		} else if (mouseButton == CENTER
				|| (mouseButton == LEFT && keyPressed && keyCode == KeyEvent.VK_CONTROL)) {
			tx += 2 * (pmouseX - mouseX);
			ty += 2 * (pmouseY - mouseY);

			txParam.setValue(tx);
			tyParam.setValue(ty);
			requestUpdate = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.Refreshable#refresh()
	 */
	@Override
	public void refresh() {
		if (requestUpdate) {
			requestUpdate = false;
			refreshLock = true;
			sceneParams.getInputView().update();
			refreshLock = false;
		} else if (enableAutoRotate.getValue() && !mousePressed
				&& !captureParam.getValue()) {
			Point3f rot = autoRotateParam.getValue();
			if (Math.abs(rot.x) > 1E-3f || Math.abs(rot.y) > 1E-3f
					|| Math.abs(rot.z) > 1E-3f) {
				applyRotation(rot.x, rot.y, rot.z);
				updatePoseParam();
				if (countCycle == 0) {
					refreshLock = true;
					sceneParams.getInputView().update();
					refreshLock = false;
				}
				countCycle++;
				if (countCycle == 100) {
					countCycle = 0;
				}
			}
		}
	}

	/**
	 * Update pose param.
	 */
	protected void updatePoseParam() {
		Matrix M = new Matrix(3, 4);
		M.set(0, 0, pose.m00);
		M.set(0, 1, pose.m01);
		M.set(0, 2, pose.m02);
		M.set(0, 3, pose.m03);
		M.set(1, 0, pose.m10);
		M.set(1, 1, pose.m11);
		M.set(1, 2, pose.m12);
		M.set(1, 3, pose.m13);
		M.set(2, 0, pose.m20);
		M.set(2, 1, pose.m21);
		M.set(2, 2, pose.m22);
		M.set(2, 3, pose.m23);
		poseParam.setValue(M);
	}

	/**
	 * Apply rotation.
	 * 
	 * @param rotx
	 *            the rotx
	 * @param roty
	 *            the roty
	 * @param rotz
	 *            the rotz
	 */
	public void applyRotation(float rotx, float roty, float rotz) {
		Matrix4f R = new Matrix4f();
		R.rotZ(rotz);
		pose.mul(R, pose);
		R.rotY(roty);
		pose.mul(R, pose);
		R.rotX(rotx);
		pose.mul(R, pose);
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		return VersionUtil.parseRevisionNumber("$Revision: 1.15 $");
	}

	/**
	 * Adds the.
	 * 
	 * @param surf
	 *            the surf
	 */
	public void add(EmbeddedSurface surf) {
		renderers.add(new TriangleMeshRenderer(this, surf));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#dispose()
	 */
	@Override
	public void dispose() {

		refresher.stop();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing#getVideoFrames(long,
	 * long)
	 */
	@Override
	public Image[] getVideoFrames(long frameRate, long duration) {
		// Need to create new axis to rotate around that is in plane with the
		// view plane.
		/*
		 * float currentRotZ=roty; int frames=(int)(duration*frameRate); float
		 * rotRate=360.0f/frames; Image[] images=new Image[frames]; for(int
		 * i=0;i<frames;i++){ roty=(float)Math.toRadians(rotRate*i);
		 * images[i]=getScreenshot(); } roty=currentRotZ; return images;
		 */
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
	 * MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scale = Math.max(0, scale - wheelScaleRate * (e.getWheelRotation()));
		scaleParam.setValue(scale);
		requestUpdate = true;
	}

	/**
	 * Sets the axes visible.
	 * 
	 * @param visible
	 *            the new axes visible
	 */
	public void setAxesVisible(boolean visible) {
		this.showAxes = visible;
		tx = txParam.getFloat();
		ty = tyParam.getFloat();
		scale = scaleParam.getFloat();
		center = centerParam.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.jhu.cs.cisst.vent.VisualizationProcessing3D#setup()
	 */
	@Override
	public void setup() {
		super.setup();

		initialView = getMatrix();
		this.addMouseWheelListener(this);
		float fov = PI / 3.0f;
		float cameraZ = (height / 2.0f) / tan(fov / 2.0f);
		perspective(fov, (float) (width) / (float) (height), cameraZ / 10.0f,
				cameraZ * 10.0f);
		refresher = new Refresher();
		refresher.add(this);
		refresher.setRefreshInterval(50);
		refresher.start();
		beginCamera();
		ambientLight(32, 32, 32);
		directionalLight(192, 192, 192, 1, -1, 1);
		lightFalloff(1, 0, 0);
		lightSpecular(0, 0, 0);
		endCamera();
	}

	/**
	 * Update.
	 * 
	 * @param model
	 *            the model
	 * @param view
	 *            the view
	 * 
	 * @see edu.jhu.ece.iacl.jist.pipeline.view.input.ParamViewObserver#update(edu.jhu.ece.iacl.jist.pipeline.parameter.ParamModel,
	 *      edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView)
	 */
	@Override
	public void update(ParamModel model, ParamInputView view) {
		if (!refreshLock) {
			super.update(model, view);
			if (model == poseParam) {
				Matrix M = poseParam.getValue();
				pose.set(new Matrix4d(M.get(0, 0), M.get(0, 1), M.get(0, 2), M
						.get(0, 3), M.get(1, 0), M.get(1, 1), M.get(1, 2), M
						.get(1, 3), M.get(2, 0), M.get(2, 1), M.get(2, 2), M
						.get(2, 3), 0, 0, 0, 1));
			} else if (model == txParam) {
				tx = txParam.getFloat();
			} else if (model == tyParam) {
				ty = tyParam.getFloat();
			} else if (model == scaleParam) {
				scale = scaleParam.getFloat();
			} else if (model == centerParam) {
				center = centerParam.getValue();
			} else if (model == bgColorParam) {
				bgColor = bgColorParam.getValue();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.jhu.cs.cisst.vent.VisualizationProcessing3D#updateVisualizationParameters
	 * ()
	 */
	@Override
	public void updateVisualizationParameters() {
		super.updateVisualizationParameters();
		Matrix M = poseParam.getValue();
		pose.set(new Matrix4d(M.get(0, 0), M.get(0, 1), M.get(0, 2), M
				.get(0, 3), M.get(1, 0), M.get(1, 1), M.get(1, 2), M.get(1, 3),
				M.get(2, 0), M.get(2, 1), M.get(2, 2), M.get(2, 3), 0, 0, 0, 1));
		tx = txParam.getFloat();
		ty = tyParam.getFloat();

		scale = scaleParam.getFloat();
		center = centerParam.getValue();
		bgColor = bgColorParam.getValue();

	}

	/**
	 * Draw axes.
	 */
	protected void drawAxes() {
		pushStyle();
		strokeWeight(2);
		stroke(color(255, 0, 0));
		float axisScale = scaleObject * 0.5f;
		line(0, 0, 0, axisScale, 0, 0);
		stroke(color(0, 255, 0));
		line(0, 0, 0, 0, axisScale, 0);
		stroke(color(0, 0, 255));
		line(0, 0, 0, 0, 0, axisScale);
		popStyle();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		try {
			File f = (args.length > 0) ? new File(args[0]) : new File(
					PlaceHolder.class.getResource("cow.stl").toURI());

			EmbeddedSurface mesh = SurfaceReaderWriter.getInstance().read(f);
			VisualizationTriangleMesh visual = new VisualizationTriangleMesh(
					600, 600);
			visual.add(mesh);
			VisualizationApplication app = new VisualizationApplication(visual);
			app.setPreferredSize(new Dimension(1024, 768));
			app.runAndWait();
			System.exit(0);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
