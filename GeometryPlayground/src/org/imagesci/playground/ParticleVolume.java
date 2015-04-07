package org.imagesci.playground;

import java.util.LinkedList;
import java.util.Vector;
import javax.vecmath.Point2f;

import edu.jhu.ece.iacl.jist.structures.image.ImageDataFloat;

public class ParticleVolume {
	public Vector<FluidParticle> particles;
	public Vector<FluidInterfaceParticle> interfaceParticles;
	private int[][] grid;
	private Vector<LinkedList<FluidParticle>> lut;
	private int width, height;
	ImageDataFloat unsignedLevelSet;
	ImageDataFloat signedLevelSet;
	ImageDataFloat particleLevelSet;
	final float density=0.5f;
	public ParticleVolume(ImageDataFloat levelSet) {
		width = levelSet.getRows();
		height = levelSet.getCols();
		particles = new Vector<FluidParticle>();

		float[][] img = levelSet.toArray2d();
		float radius = 0.25f;
		int r = img.length;
		int c = img[0].length;
		for (int i = 0; i < 2 * r; i++) {
			for (int j = 0; j < 2 * c; j++) {
				float x = 0.5f * i + radius;
				float y = 0.5f * j + radius;
				double l = DataOperations.interpolate(x, y, img, r, c);
				if (l < -radius) {
					particles.add(new FluidParticle(x, y, radius));
				}
			}
		}
		interfaceParticles = new Vector<FluidInterfaceParticle>();
		IsoContourGenerator gen = new IsoContourGenerator(true);
		ContourArray isoContour = gen.solve(levelSet);
		for (int i = 0; i < isoContour.indexes.length; i += 2) {
			Point2f pt1 = isoContour.points[isoContour.indexes[i]];
			Point2f pt2 = isoContour.points[isoContour.indexes[i + 1]];
			interfaceParticles.add(new FluidInterfaceParticle(pt1.x, pt1.y,
					pt2.x, pt2.y, radius));
		}
		update();
	}

	public void update() {
		grid = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				grid[i][j] = -1;
			}
		}
		lut = new Vector<LinkedList<FluidParticle>>();
		int id;
		for (FluidParticle p : particles) {
			int i = (int) Math.floor(p.x);
			int j = (int) Math.floor(p.y);
			id = grid[i][j];
			if (id < 0) {
				id = grid[i][j] = lut.size();
				lut.add(new LinkedList<FluidParticle>());
			}
			lut.get(id).add(p);
		}
	}

	public LinkedList<FluidParticle> getNeighbors(float x, float y, float r) {
		LinkedList<FluidParticle> neighbors = new LinkedList<FluidParticle>();
		int mnX = (int) Math.max(0, Math.floor(x - r));
		int mnY = (int) Math.max(0, Math.floor(y - r));
		int mxX = (int) Math.min(width - 1, Math.ceil(x + r));
		int mxY = (int) Math.min(height - 1, Math.ceil(y + r));
		for (int i = mnX; i <= mxX; i++) {
			for (int j = mnY; j <= mxY; j++) {
				int id = grid[i][j];
				if (id >= 0)
					neighbors.addAll(lut.get(id));
			}
		}
		return neighbors;
	}

	private float evaluateParticleLevelSet(float x, float y,float r) {
		float minDistance = 1.0f;
		LinkedList<FluidParticle> particles = getNeighbors(x, y, 2);
		for (FluidParticle p : particles) {
			float d = (float) Math.sqrt((p.x - x) * (p.x - x) + (p.y - y)
					* (p.y - y));
			float w = d - r;
			minDistance = Math.min(minDistance, w);
		}
		return minDistance;
	}

	private float evaluateUnsignedLevelSet(float x, float y) {
		return (float) DataOperations.interpolate(x, y,
				unsignedLevelSet.toArray2d(), unsignedLevelSet.getRows(),
				unsignedLevelSet.getCols());
	}

	private float evaluateSignedLevelSet(float x, float y) {
		float minDistance = 1.5f;
		LinkedList<FluidParticle> particles = getNeighbors(x, y, 2);
		FluidParticle minParticle = null;
		for (FluidParticle p : particles) {
			float d = (float) Math.sqrt((p.x - x) * (p.x - x) + (p.y - y)
					* (p.y - y));
			if (d < minDistance) {
				minDistance = d;
				minParticle = p;
			}
		}
		return minDistance-0.5f;//Math.min(len-t,1.0f);////;
		
	}

	double f(double x) {
		return 0;
	}


	public ImageDataFloat createParticleLevelSet(int res) {
		int w = res * width;
		int h = res * height;
		particleLevelSet = new ImageDataFloat(w, h);
		float data[][] = particleLevelSet.toArray2d();
		float x, y;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				x = i / (float) res;
				y = j / (float) res;
				data[i][j] = evaluateParticleLevelSet(x, y,density);
			}
		}
		return particleLevelSet;
	}

	public ImageDataFloat createSignedLevelSet(int res) {
		int w = res * width;
		int h = res * height;
		//particleLevelSet = new ImageDataFloat(w, h);
		float pdata[][] = particleLevelSet.toArray2d();
		signedLevelSet= new ImageDataFloat(w, h);
		float udata[][] = unsignedLevelSet.toArray2d();
		float sdata[][]=signedLevelSet.toArray2d();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				float val=udata[i][j];
				if(pdata[i][j]+density<Math.abs(val)){
					sdata[i][j]=-density;
				} else {
					sdata[i][j]=udata[i][j];
				}
			}
		}
		return signedLevelSet;
	}

	public ImageDataFloat createUnsignedLevelSet(int res) {
		int w = res * width;
		int h = res * height;
		unsignedLevelSet = new ImageDataFloat(w, h);
		float data[][] = unsignedLevelSet.toArray2d();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				data[i][j] = density;
			}
		}
		for (FluidInterfaceParticle p : interfaceParticles) {
			float r = 2 * p.radius;
			int mnX = (int) Math.max(0,
					Math.floor(res * Math.min(p.x1 - r, p.x2 - r)));
			int mnY = (int) Math.max(0,
					Math.floor(res * Math.min(p.y1 - r, p.y2 - r)));
			int mxX = (int) Math.min(w - 1,
					Math.ceil(res * Math.max(p.x1 + r, p.x2 + r)));
			int mxY = (int) Math.min(h - 1,
					Math.ceil(res * Math.max(p.y1 + r, p.y2 + r)));
			for (int i = mnX; i <= mxX; i++) {
				for (int j = mnY; j <= mxY; j++) {
					float d=p.distance(i / (float) res, j / (float) res);
					float oldD=data[i][j];
					if(Math.abs(d)<Math.abs(oldD)){
						data[i][j] = d;
					}
				}
			}
		}

		return unsignedLevelSet;
	}
}
