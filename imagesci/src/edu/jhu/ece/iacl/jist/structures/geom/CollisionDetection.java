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
package edu.jhu.ece.iacl.jist.structures.geom;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.Material;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.pickfast.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;

// TODO: Auto-generated Javadoc
/**
 * The Class CollisionDetection.
 */
public class CollisionDetection {

	/**
	 * The Class Triangle.
	 */
	public static class Triangle {

		/** The points. */
		Point3d[] points = new Point3d[3];

		/**
		 * Instantiates a new triangle.
		 * 
		 * @param p1
		 *            the p1
		 * @param p2
		 *            the p2
		 * @param p3
		 *            the p3
		 */
		public Triangle(Point3d p1, Point3d p2, Point3d p3) {
			points[0] = p1;
			points[1] = p2;
			points[2] = p3;
		}

		/**
		 * Gets the bounds.
		 * 
		 * @param tris
		 *            the tris
		 * 
		 * @return the bounds
		 */
		public static BoundingBox getBounds(ArrayList<Triangle> tris) {
			double minx = 1E10, miny = 1E10, minz = 1E10;
			double maxx = -1E10, maxy = -1E10, maxz = -1E10;
			for (Triangle t : tris) {
				minx = Math.min(
						minx,
						Math.min(t.points[0].x,
								Math.min(t.points[1].x, t.points[2].x)));
				miny = Math.min(
						miny,
						Math.min(t.points[0].y,
								Math.min(t.points[1].y, t.points[2].y)));
				minz = Math.min(
						minz,
						Math.min(t.points[0].z,
								Math.min(t.points[1].z, t.points[2].z)));
				maxx = Math.max(
						maxx,
						Math.max(t.points[0].x,
								Math.max(t.points[1].x, t.points[2].x)));
				maxy = Math.max(
						maxy,
						Math.max(t.points[0].y,
								Math.max(t.points[1].y, t.points[2].y)));
				maxz = Math.max(
						maxz,
						Math.max(t.points[0].z,
								Math.max(t.points[1].z, t.points[2].z)));
			}
			return new BoundingBox(new Point3d(minx, miny, minz), new Point3d(
					maxx, maxy, maxz));
		}

		/**
		 * Gets the in bounds.
		 * 
		 * @param tris
		 *            the tris
		 * @param box
		 *            the box
		 * 
		 * @return the in bounds
		 */
		public static ArrayList<Triangle> getInBounds(ArrayList<Triangle> tris,
				BoundingBox box) {
			ArrayList<Triangle> result = new ArrayList<Triangle>();
			for (Triangle t : tris) {
				if (t.inBounds(box)) {
					result.add(t);
				}
			}
			return result;
		}

		/**
		 * In bounds.
		 * 
		 * @param box
		 *            the box
		 * 
		 * @return true, if successful
		 */
		public boolean inBounds(BoundingBox box) {
			return (box.intersect(points[0]) || box.intersect(points[1]) || box
					.intersect(points[2]));
		}
	}

	/** The Constant minChildSize. */
	private static final int minChildSize = 10;

	/** The bounds. */
	protected double[] bounds = { -1, 1, -1, 1, -1, 1 };// set min/max X,
	// min/max Y, and
	// min/max Z for the
	// display

	/** The center. */
	protected Vector3d center = new Vector3d();

	/** The light directions. */
	private Vector3f[] lightDirections = { new Vector3f(1, 0, 0),
			new Vector3f(0, 1, 0), new Vector3f(0, 0, 1),
			new Vector3f(-1, 0, 0), new Vector3f(0, -1, 0),
			new Vector3f(0, 0, -1) };

	/** The limits. */
	protected double[] limits = { -1, 1, -1, 1, -1, 1 };// set min/max X,
	// min/max Y, and
	// min/max Z for the
	// Graph2D

	/** The max depth. */
	private int maxDepth;

	/** The mesh color. */
	private Color3f meshColor = new Color3f(0.1f, 0.1f, 0.1f);

	/** The picker. */
	private PickTool picker;

	/** The slices. */
	private int rows, cols, slices;

	/** The scale. */
	protected Vector3d scale = new Vector3d();

	/** The universe. */
	private SimpleUniverse universe;

	/**
	 * Instantiates a new collision detection.
	 * 
	 * @param mesh
	 *            the mesh
	 * @param rows
	 *            the rows
	 * @param cols
	 *            the cols
	 * @param slices
	 *            the slices
	 * @param maxDepth
	 *            the max depth
	 */
	public CollisionDetection(EmbeddedSurface mesh, int rows, int cols,
			int slices, int maxDepth) {
		this.rows = rows;
		this.cols = cols;
		this.slices = slices;
		this.maxDepth = maxDepth;
		init(mesh);
	}

	/**
	 * Inits the.
	 * 
	 * @param mesh
	 *            the mesh
	 */
	public void init(EmbeddedSurface mesh) {
		JFrame frame = new JFrame("Intersector");
		GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
		GraphicsEnvironment env = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration config = device.getBestConfiguration(template);
		Canvas3D canvas = new Canvas3D(config, true);
		canvas.setSize(640, 640);
		universe = new SimpleUniverse(canvas);
		computeBounds(mesh);
		/*
		 * BranchGroup group=new BranchGroup(); Shape3D shape=new Shape3D(mesh);
		 * 
		 * shape.setAppearance(createAppearance(1/(float)maxDepth));
		 * com.sun.j3d.utils.picking.PickTool.setCapabilities(shape,
		 * com.sun.j3d.utils.picking.PickTool.INTERSECT_FULL);
		 * group.addChild(shape);
		 */
		BranchGroup group = buildTree(mesh);
		group.setPickable(true);
		group.setBoundsAutoCompute(true);
		/*
		 * Transform3D trans = new Transform3D(); Vector3d
		 * tmp1=(Vector3d)center.clone(); tmp1.negate();
		 * trans.setTranslation(tmp1); trans.setScale(scale); TransformGroup
		 * shapeTG = new TransformGroup(trans);
		 * 
		 * shapeTG.addChild(group); shapeTG.addChild(createLight());
		 * 
		 * BranchGroup bg=new BranchGroup(); bg.setPickable(true);
		 * bg.setBoundsAutoCompute(true); bg.addChild(shapeTG); bg.compile();
		 */
		universe.addBranchGraph(group);
		universe.getViewingPlatform().setNominalViewingTransform();

		OrbitBehavior orbit = new OrbitBehavior(canvas,
				OrbitBehavior.REVERSE_ALL);
		universe.getViewingPlatform().setViewPlatformBehavior(orbit);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
				100.0);
		orbit.setSchedulingBounds(bounds);
		//

		frame.add(canvas);
		picker = new PickTool(group);
		picker.setMode(PickInfo.PICK_GEOMETRY);
		picker.setFlags(PickInfo.CLOSEST_DISTANCE);
		picker.setFlags(PickInfo.CLOSEST_INTERSECTION_POINT);
		frame.setPreferredSize(new Dimension(0, 0));
		frame.setLocation(0, 0);
		frame.pack();
		frame.setVisible(true);
		canvas.startRenderer();
		frame.repaint();
		frame.setVisible(false);
		frame.dispose();
	}

	/**
	 * Compute bounds.
	 * 
	 * @param mesh
	 *            the mesh
	 */
	public void computeBounds(EmbeddedSurface mesh) {
		int sz = mesh.getVertexCount();
		double large = 1E30;
		double[] limits = new double[] { large, -large, large, -large, large,
				-large };
		Point3 p = new Point3();
		for (int i = 0; i < sz; i++) {
			mesh.getCoordinate(i, p);
			limits[0] = Math.min(p.x, limits[0]);
			limits[1] = Math.max(p.x, limits[1]);
			limits[2] = Math.min(p.y, limits[2]);
			limits[3] = Math.max(p.y, limits[3]);
			limits[4] = Math.min(p.z, limits[4]);
			limits[5] = Math.max(p.z, limits[5]);
		}
		setBounds(limits);
	}

	/**
	 * Sets the bounds.
	 * 
	 * @param limits
	 *            the new bounds
	 */
	public void setBounds(double[] limits) {
		this.limits = limits;
		double max = Math.max(
				Math.max(limits[1] - limits[0], limits[3] - limits[2]),
				limits[5] - limits[4]);

		scale.x = 2 / max;
		scale.y = 2 / max;
		scale.z = 2 / max;
		center.x = scale.x * 0.5 * (limits[0] + limits[1]);
		center.y = scale.y * 0.5 * (limits[2] + limits[3]);
		center.z = scale.z * 0.5 * (limits[4] + limits[5]);
		bounds[0] = -(limits[1] - limits[0]) / max;
		bounds[1] = (limits[1] - limits[0]) / max;
		bounds[2] = -(limits[3] - limits[2]) / max;
		bounds[3] = (limits[3] - limits[2]) / max;
		bounds[4] = -(limits[5] - limits[4]) / max;
		bounds[5] = (limits[5] - limits[4]) / max;
	}

	/**
	 * Builds the tree.
	 * 
	 * @param mesh
	 *            the mesh
	 * 
	 * @return the branch group
	 */
	private BranchGroup buildTree(EmbeddedSurface mesh) {
		int[] indices = new int[mesh.getIndexCount()];
		Point3d[] points = new Point3d[mesh.getVertexCount()];
		ArrayList<Triangle> tris = new ArrayList<Triangle>();
		// mesh.getCoordinates(0, points);
		for (int i = 0; i < mesh.getVertexCount(); i++) {
			points[i] = new Point3d();
			mesh.getCoordinate(i, points[i]);
		}
		mesh.getCoordinateIndices(0, indices);
		for (int i = 0; i < indices.length; i += 3) {
			tris.add(new Triangle(points[indices[i]], points[indices[i + 1]],
					points[indices[i + 2]]));
		}
		Triangle.getBounds(tris);
		return buildTree(mesh, 1, tris);
	}

	/**
	 * Builds the tree.
	 * 
	 * @param mesh
	 *            the mesh
	 * @param depth
	 *            the depth
	 * @param tris
	 *            the tris
	 * 
	 * @return the branch group
	 */
	private BranchGroup buildTree(EmbeddedSurface mesh, int depth,
			ArrayList<Triangle> tris) {
		if (tris.size() == 0) {
			return null;
		}
		BranchGroup group = new BranchGroup();
		if (depth == maxDepth || tris.size() <= minChildSize) {
			BoundingBox box = Triangle.getBounds(tris);
			IndexedTriangleArray smallMesh = new IndexedTriangleArray(
					tris.size() * 3, GeometryArray.COORDINATES, tris.size() * 3);
			int index = 0;
			for (Triangle t : tris) {
				smallMesh.setCoordinates(index, t.points);
				smallMesh.setCoordinateIndices(index, new int[] { index,
						index + 1, index + 2 });
				index += 3;
			}
			// GeometryInfo gi=new GeometryInfo(smallMesh);
			// NormalGenerator ng = new NormalGenerator();
			// ng.generateNormals(gi);
			// System.out.println(getClass().getCanonicalName()+"\t"+"TRIANGLES "+depth+" "+tris.size()+" "+volume(box));
			// gi.getGeometryArray()
			Shape3D shape = new Shape3D(smallMesh);
			shape.setAppearance(createAppearance(depth / (float) maxDepth));
			com.sun.j3d.utils.picking.PickTool.setCapabilities(shape,
					com.sun.j3d.utils.picking.PickTool.INTERSECT_FULL);
			// System.out.println(getClass().getCanonicalName()+"\t"+tris.size()+" "+depth+" "+box);
			shape.setBounds(box);
			group.setPickable(true);
			group.setBoundsAutoCompute(true);
			group.addChild(shape);
		} else {
			BoundingBox box = Triangle.getBounds(tris);
			// System.out.println(getClass().getCanonicalName()+"\t"+"BOUNDS "+depth+" "+tris.size()+" "+volume(box));
			Point3d lower = new Point3d();
			Point3d upper = new Point3d();
			box.getLower(lower);
			box.getUpper(upper);
			Point3d lowerBox1 = lower;
			Point3d lowerBox2 = new Point3d((lower.x + upper.x) * 0.5, lower.y,
					lower.z);
			Point3d lowerBox3 = new Point3d((lower.x + upper.x) * 0.5,
					(lower.y + upper.y) * 0.5, lower.z);
			Point3d lowerBox4 = new Point3d(lower.x, (lower.y + upper.y) * 0.5,
					lower.z);

			Point3d lowerBox5 = new Point3d(lower.x, lower.y,
					(lower.z + upper.z) * 0.5);
			Point3d lowerBox6 = new Point3d((lower.x + upper.x) * 0.5, lower.y,
					(lower.z + upper.z) * 0.5);
			Point3d lowerBox7 = new Point3d((lower.x + upper.x) * 0.5,
					(lower.y + upper.y) * 0.5, (lower.z + upper.z) * 0.5);
			Point3d lowerBox8 = new Point3d(lower.x, (lower.y + upper.y) * 0.5,
					(lower.z + upper.z) * 0.5);

			Point3d upperBox1 = new Point3d((lower.x + upper.x) * 0.5,
					(lower.y + upper.y) * 0.5, (lower.z + upper.z) * 0.5);
			Point3d upperBox2 = new Point3d(upper.x, (lower.y + upper.y) * 0.5,
					(lower.z + upper.z) * 0.5);
			Point3d upperBox3 = new Point3d(upper.x, upper.y,
					(lower.z + upper.z) * 0.5);
			Point3d upperBox4 = new Point3d((lower.x + upper.x) * 0.5, upper.y,
					(lower.z + upper.z) * 0.5);

			Point3d upperBox5 = new Point3d((lower.x + upper.x) * 0.5,
					(lower.y + upper.y) * 0.5, upper.z);
			Point3d upperBox6 = new Point3d(upper.x, (lower.y + upper.y) * 0.5,
					upper.z);
			Point3d upperBox7 = upper;
			Point3d upperBox8 = new Point3d((lower.x + upper.x) * 0.5, upper.y,
					upper.z);

			BoundingBox oct1 = new BoundingBox(lowerBox1, upperBox1);
			BoundingBox oct2 = new BoundingBox(lowerBox2, upperBox2);
			BoundingBox oct3 = new BoundingBox(lowerBox3, upperBox3);
			BoundingBox oct4 = new BoundingBox(lowerBox4, upperBox4);

			BoundingBox oct5 = new BoundingBox(lowerBox5, upperBox5);
			BoundingBox oct6 = new BoundingBox(lowerBox6, upperBox6);
			BoundingBox oct7 = new BoundingBox(lowerBox7, upperBox7);
			BoundingBox oct8 = new BoundingBox(lowerBox8, upperBox8);

			BranchGroup bg = null;
			group.setPickable(true);
			group.setBoundsAutoCompute(true);

			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 1 "+depth+" "+volume(oct1));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct1));
			if (bg != null) {
				group.addChild(bg);
			}
			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 2 "+depth+" "+volume(oct2));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct2));
			if (bg != null) {
				group.addChild(bg);
			}
			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 3 "+depth+" "+volume(oct3));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct3));
			if (bg != null) {
				group.addChild(bg);
			}
			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 4 "+depth+" "+volume(oct4));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct4));
			if (bg != null) {
				group.addChild(bg);
			}
			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 5 "+depth+" "+volume(oct5));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct5));
			if (bg != null) {
				group.addChild(bg);
			}
			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 6 "+depth+" "+volume(oct6));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct6));
			if (bg != null) {
				group.addChild(bg);
			}
			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 7 "+depth+" "+volume(oct7));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct7));
			if (bg != null) {
				group.addChild(bg);
			}
			// System.out.println(getClass().getCanonicalName()+"\t"+"OCT 8 "+depth+" "+volume(oct8));
			bg = buildTree(mesh, depth + 1, Triangle.getInBounds(tris, oct8));
			if (bg != null) {
				group.addChild(bg);
			}
		}
		return group;
	}

	/**
	 * Creates the appearance.
	 * 
	 * @param val
	 *            the val
	 * 
	 * @return the appearance
	 */
	Appearance createAppearance(float val) {
		Appearance appear = new Appearance();
		Material material = new Material(new Color3f(1, 0, 0), new Color3f(),
				new Color3f(0.6f, 0.6f, 0.6f), new Color3f(0.5f, 0.5f, 0.5f),
				64.0f);
		PolygonAttributes polyAttr = new PolygonAttributes(
				PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_BACK,
				0.0f);
		appear.setPolygonAttributes(polyAttr);
		appear.setMaterial(material);
		return appear;
	}

	/**
	 * Project to surface.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @return the point3d
	 */
	public Point3d projectToSurface(Point3d p) {
		intersectSegmentPoint(new Point3d(p.x - 1, p.y, p.z), new Point3d(
				p.x + 1, p.y, p.z));
		intersectSegmentPoint(new Point3d(p.x, p.y - 1, p.z), new Point3d(p.x,
				p.y + 1, p.z));
		intersectSegmentPoint(new Point3d(p.x, p.y, p.z - 1), new Point3d(p.x,
				p.y, p.z + 1));

		PickInfo result = picker.pickClosest();
		if (result != null) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"RESULT "+result+" "+result.getClosestDistance());
			return result.getClosestIntersectionPoint();
		}
		return null;
	}

	/**
	 * Intersect segment point.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * 
	 * @return the point3d
	 */
	public Point3d intersectSegmentPoint(Point3d start, Point3d end) {
		picker.setShapeSegment(start, end);
		PickInfo result = picker.pickClosest();
		if (result != null) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"RESULT "+result+" "+result.getClosestDistance());
			return result.getClosestIntersectionPoint();
		}
		return null;
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		universe.cleanup();
	}

	/**
	 * Intersect ray distance.
	 * 
	 * @param start
	 *            the start
	 * @param dir
	 *            the dir
	 * 
	 * @return the double
	 */
	public double intersectRayDistance(Point3d start, Vector3d dir) {
		picker.setShapeRay(start, dir);
		PickInfo result = picker.pickClosest();
		if (result != null) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"RESULT "+result+" "+result.getClosestDistance());
			return result.getClosestDistance();
		}
		return -1;

	}

	/**
	 * Intersect ray point.
	 * 
	 * @param start
	 *            the start
	 * @param dir
	 *            the dir
	 * 
	 * @return the point3d
	 */
	public Point3d intersectRayPoint(Point3d start, Vector3d dir) {
		picker.setShapeRay(start, dir);
		PickInfo result = picker.pickClosest();
		if (result != null) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"RESULT "+result+" "+result.getClosestDistance());
			return result.getClosestIntersectionPoint();
		}
		return null;

	}

	/**
	 * Intersect ray point.
	 * 
	 * @param start
	 *            the start
	 * @param dir
	 *            the dir
	 * 
	 * @return the point3f
	 */
	public Point3f intersectRayPoint(Point3f start, Vector3f dir) {
		picker.setShapeRay(new Point3d(start), new Vector3d(dir));
		PickInfo result = picker.pickClosest();
		if (result != null) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"RESULT "+result+" "+result.getClosestDistance());
			return new Point3f(result.getClosestIntersectionPoint());
		}
		return null;

	}

	/**
	 * Intersect segment distance.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * 
	 * @return the double
	 */
	public double intersectSegmentDistance(Point3d start, Point3d end) {
		picker.setShapeSegment(start, end);
		PickInfo result = picker.pickClosest();
		if (result != null) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"RESULT "+result+" "+result.getClosestDistance());
			return result.getClosestDistance();
		}
		return -1;
	}

	/**
	 * Intersect segment point.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * 
	 * @return the point3f
	 */
	public Point3f intersectSegmentPoint(Point3f start, Point3f end) {
		picker.setShapeSegment(new Point3d(start), new Point3d(end));
		PickInfo result = picker.pickClosest();
		if (result != null) {
			// System.out.println(getClass().getCanonicalName()+"\t"+"RESULT "+result+" "+result.getClosestDistance());
			return new Point3f(result.getClosestIntersectionPoint());
		}
		return null;
	}

	/**
	 * Checks if is inside.
	 * 
	 * @param start
	 *            the start
	 * 
	 * @return true, if is inside
	 */
	public boolean isInside(Point3d start) {
		picker.setShapeRay(start, new Vector3d(0, 0, 1));
		PickInfo[] result = picker.pickAll();
		if (result != null) {
			return (result.length % 2 == 1);
		}
		return false;
	}

	/**
	 * Creates the light.
	 * 
	 * @return the javax.media.j3d. node
	 */
	private javax.media.j3d.Node createLight() {
		Color3f directColor = new Color3f(meshColor);
		BoundingSphere bsphere = new BoundingSphere(new Point3d(), 2.0);
		TransformGroup tg = new TransformGroup();
		DirectionalLight directLight;
		for (Vector3f direction : lightDirections) {
			directLight = new DirectionalLight();
			directLight.setInfluencingBounds(bsphere);
			directLight.setDirection(direction);
			directLight.setColor(directColor);
			tg.addChild(directLight);
		}

		AmbientLight ambientLight = new AmbientLight();
		ambientLight.setInfluencingBounds(bsphere);
		ambientLight.setColor(meshColor);

		tg.addChild(ambientLight);
		return tg;
	}

	/**
	 * Volume.
	 * 
	 * @param box
	 *            the box
	 * 
	 * @return the double
	 */
	private double volume(BoundingBox box) {
		Point3d lower = new Point3d();
		Point3d upper = new Point3d();
		box.getLower(lower);
		box.getUpper(upper);
		if (upper.x < lower.x || upper.y < lower.y || upper.z < lower.z) {
			System.err.println("jist.base" + "INVALID " + box);
		}
		return (upper.x - lower.x) * (upper.y - lower.y) * (upper.z - lower.z);
	}

	// Geometric Tools, Inc.
	// http://www.geometrictools.com
	// Copyright (c) 1998-2006. All Rights Reserved
	//
	// The Wild Magic Version 4 Foundation Library source code is supplied
	// under the terms of the license agreement
	// http://www.geometrictools.com/License/Wm4FoundationLicense.pdf
	// and may not be copied or disclosed except in accordance with the terms
	// of that agreement.
	/*
	 * public static Point3f projectPoint(Point3f p,EmbeddedSurface mesh,double
	 * lambda){ Point3f ret=p; Point3f p1=new Point3f(); Point3f p2=new
	 * Point3f(); Point3f p3=new Point3f(); int in1,in2,in3; int
	 * count=mesh.getIndexCount(); double mind=1E10; double d; for(int
	 * i=0;i<count;i+=3){ in1=mesh.getCoordinateIndex(i);
	 * mesh.getCoordinate(in1,p1); in2=mesh.getCoordinateIndex(i+1);
	 * mesh.getCoordinate(in2,p2); in3=mesh.getCoordinateIndex(i+2);
	 * mesh.getCoordinate(in3,p3);
	 * if(p1.distance(p)>lambda&&p2.distance(p)>lambda
	 * &&p3.distance(p)>lambda)continue; d=triangleDistance(p,p1,p2,p3);
	 * if(d<mind){ mind=d; ret=(Point3f)minPoint.clone(); } } return ret; }
	 * public static double triangleDistance(Point3f p,Point3f p1,Point3f
	 * p2,Point3f p3){ Vector3f kDiff=new Vector3f(); kDiff.sub(p1,p); Vector3f
	 * kEdge0=new Vector3f(); kEdge0.sub(p2,p1); Vector3f kEdge1=new Vector3f();
	 * kEdge1.sub(p3,p1); float fA00 = kEdge0.lengthSquared(); float fA01 =
	 * kEdge0.dot(kEdge1); float fA11 = kEdge1.lengthSquared(); float fB0 =
	 * kDiff.dot(kEdge0); float fB1 = kDiff.dot(kEdge1); float fC =
	 * kDiff.lengthSquared(); float fDet = Math.abs(fA00*fA11-fA01*fA01); float
	 * fS = fA01*fB1-fA11*fB0; float fT = fA01*fB0-fA00*fB1; float fSqrDistance;
	 * minPoint.x=p1.x; minPoint.y=p1.y; minPoint.z=p1.z;
	 * 
	 * if (fS + fT <= fDet) { if (fS < (float)0.0) { if (fT < (float)0.0) //
	 * region 4 { if (fB0 < (float)0.0) { fT = (float)0.0; if (-fB0 >= fA00) {
	 * fS = (float)1.0; fSqrDistance = fA00+((float)2.0)*fB0+fC; } else { fS =
	 * -fB0/fA00; fSqrDistance = fB0*fS+fC; }
	 * 
	 * } else { fS = (float)0.0; if (fB1 >= (float)0.0) { fT = (float)0.0;
	 * fSqrDistance = fC; } else if (-fB1 >= fA11) { fT = (float)1.0;
	 * fSqrDistance = fA11+((float)2.0)*fB1+fC; } else { fT = -fB1/fA11;
	 * fSqrDistance = fB1*fT+fC; } }
	 * 
	 * } else // region 3 { fS = (float)0.0; if (fB1 >= (float)0.0) { fT =
	 * (float)0.0; fSqrDistance = fC; } else if (-fB1 >= fA11) { fT =
	 * (float)1.0; fSqrDistance = fA11+((float)2.0)*fB1+fC; } else { fT =
	 * -fB1/fA11; fSqrDistance = fB1*fT+fC; } } } else if (fT < (float)0.0) //
	 * region 5 { fT = (float)0.0; if (fB0 >= (float)0.0) { fS = (float)0.0;
	 * fSqrDistance = fC; } else if (-fB0 >= fA00) { fS = (float)1.0;
	 * fSqrDistance = fA00+((float)2.0)*fB0+fC; } else { fS = -fB0/fA00;
	 * fSqrDistance = fB0*fS+fC; } } else // region 0 { // minimum at interior
	 * point float fInvDet = ((float)1.0)/fDet; fS *= fInvDet; fT *= fInvDet;
	 * fSqrDistance = fS*(fA00*fS+fA01*fT+((float)2.0)*fB0) +
	 * fT*(fA01*fS+fA11*fT+((float)2.0)*fB1)+fC; } } else { float fTmp0, fTmp1,
	 * fNumer, fDenom;
	 * 
	 * if (fS < (float)0.0) // region 2 { fTmp0 = fA01 + fB0; fTmp1 = fA11 +
	 * fB1; if (fTmp1 > fTmp0) { fNumer = fTmp1 - fTmp0; fDenom =
	 * fA00-2.0f*fA01+fA11; if (fNumer >= fDenom) { fS = (float)1.0; fT =
	 * (float)0.0; fSqrDistance = fA00+((float)2.0)*fB0+fC; } else { fS =
	 * fNumer/fDenom; fT = (float)1.0 - fS; fSqrDistance =
	 * fS*(fA00*fS+fA01*fT+2.0f*fB0) + fT*(fA01*fS+fA11*fT+((float)2.0)*fB1)+fC;
	 * } } else { fS = (float)0.0; if (fTmp1 <= (float)0.0) { fT = (float)1.0;
	 * fSqrDistance = fA11+((float)2.0)*fB1+fC; } else if (fB1 >= (float)0.0) {
	 * fT = (float)0.0; fSqrDistance = fC; } else { fT = -fB1/fA11; fSqrDistance
	 * = fB1*fT+fC; } } } else if (fT < (float)0.0) // region 6 { fTmp0 = fA01 +
	 * fB1; fTmp1 = fA00 + fB0; if (fTmp1 > fTmp0) { fNumer = fTmp1 - fTmp0;
	 * fDenom = fA00-((float)2.0)*fA01+fA11; if (fNumer >= fDenom) { fT =
	 * (float)1.0; fS = (float)0.0; fSqrDistance = fA11+((float)2.0)*fB1+fC; }
	 * else { fT = fNumer/fDenom; fS = (float)1.0 - fT; fSqrDistance =
	 * fS*(fA00*fS+fA01*fT+((float)2.0)*fB0) +
	 * fT*(fA01*fS+fA11*fT+((float)2.0)*fB1)+fC; } } else { fT = (float)0.0; if
	 * (fTmp1 <= (float)0.0) { fS = (float)1.0; fSqrDistance =
	 * fA00+((float)2.0)*fB0+fC; } else if (fB0 >= (float)0.0) { fS =
	 * (float)0.0; fSqrDistance = fC; } else { fS = -fB0/fA00; fSqrDistance =
	 * fB0*fS+fC; } } } else // region 1 { fNumer = fA11 + fB1 - fA01 - fB0; if
	 * (fNumer <= (float)0.0) { fS = (float)0.0; fT = (float)1.0; fSqrDistance =
	 * fA11+((float)2.0)*fB1+fC; } else { fDenom = fA00-2.0f*fA01+fA11; if
	 * (fNumer >= fDenom) { fS = (float)1.0; fT = (float)0.0; fSqrDistance =
	 * fA00+((float)2.0)*fB0+fC; } else { fS = fNumer/fDenom; fT = (float)1.0 -
	 * fS; fSqrDistance = fS*(fA00*fS+fA01*fT+((float)2.0)*fB0) +
	 * fT*(fA01*fS+fA11*fT+((float)2.0)*fB1)+fC; } } } }
	 * 
	 * // account for numerical round-off error if (fSqrDistance < (float)0.0) {
	 * fSqrDistance = (float)0.0; }
	 * 
	 * kEdge0.scale(fS); kEdge1.scale(fT); minPoint.add(kEdge0);
	 * minPoint.add(kEdge1); return Math.sqrt(fSqrDistance); }
	 */
}
