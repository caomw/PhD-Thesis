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
 * @author Blake Lucas
 */
 /*
Copyright (c) 2009 David Bucciarelli (davibu@interfree.it)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
#define WORLD_RADIUS 1000.f
#define WORLD_CENTER ((float4)(0.f, -WORLD_RADIUS - 2.f, 0.f, 0.f))
#define BOUNDING_RADIUS_2 4.f
#define ESCAPE_THRESHOLD 1e1f
#define DELTA 1e-4f
constant int a2fVertexOffset[8][3] = { { 0, 0, 0 },
			{ 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
			{ 1, 1, 1 }, { 0, 1, 1 } };
constant int a2iEdgeConnection[12][2] = { { 0, 1 }, { 1, 2 },
			{ 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
constant float a2fEdgeDirection[12][3] = { { 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f },
			{ 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f },
			{ 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f } };
typedef struct {
    float x, y, z; // position, also color (r,g,b)
} Vec;

typedef struct {
    Vec orig, target;
    Vec dir, x, y;
} Camera;
typedef struct{
	int4 e1;
	int4 e2;
	float4 pt3d;
	int vid;
} EdgeSplit;
typedef struct {
    unsigned int width, height;
    int superSamplingSize;
    int actvateFastRendering;
    int enableShadow;

    unsigned int maxIterations;
    float epsilon;
    float color[4];
    float light[3];
    Camera camera;
} RenderingConfig;

typedef struct{
	float4 particle;
    float4 vertexes[3];
} Springl3D;

//Store capsule id and vertex id [0,1,2]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor3D;




typedef struct {
	float m00, m01, m02, m03,
		  m10, m11, m12, m13,
		  m20, m21, m22, m23,
		  m30, m31, m32, m33;
} Matrix4f;



typedef struct {
	float4 origin;
	float4 direction;
	float extent;	
} EdgeSegment;
inline void getRowColSlice(uint index,int* i, int* j, int* k) {
	(*k)=index/(ROWS*COLS);
	uint ij=index-(*k)*(ROWS * COLS);
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}

inline int getIndex(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
}
inline int getSafeIndex(int i,int j,int k){
	int r=max(min(i,(int)(ROWS-1)),(int)0);
	int c=max(min(j,(int)(COLS-1)),(int)0);
	int s=max(min(k,(int)(SLICES-1)),(int)0);
	return getIndex(r,c,s);
}
inline float getImageValue(__global float* image,int i,int j,int k){
	return image[getSafeIndex(i,j,k)];
}
inline float4 getImageValue4(__global float4* image,int i,int j,int k){
	return image[getSafeIndex(i,j,k)];
}
inline int clampRow(int row){
	return clamp((int)row,(int)0,(int)(ROWS-1));
}
inline int clampColumn(int col){
	return clamp((int)col,(int)0,(int)(COLS-1));
}
inline int clampSlice(int slice){
	return clamp((int)slice,(int)0,(int)(SLICES-1));
}
inline float getNudgedValue(global float* signedLevelSet,uint index){
	float val=signedLevelSet[index];
	if (val < 0) {
		val = min(val, - 0.1f);
	} else {
		val = max(val,  0.1f);
	}
	return val;
}
inline float fGetOffset(__global float* signedLevelSet,int4 v1, int4 v2) {
		float fValue1 = getNudgedValue(signedLevelSet,getIndex(v1.x, v1.y, v1.z));
		float fValue2 = getNudgedValue(signedLevelSet,getIndex(v2.x, v2.y, v2.z));
		float fDelta = fValue2 - fValue1;
		if (fDelta == 0.0f) {
			return 0.5f;
		}
		return (float) (( - fValue1) / fDelta);
}
inline Matrix4f invertMatrix3x3(Matrix4f M){
	float detInv=1.0f/(M.m00*M.m11*M.m22 + M.m01*M.m12*M.m20 + M.m02*M.m10*M.m21 - M.m00*M.m12*M.m21 - M.m01*M.m10*M.m22 - M.m02*M.m11*M.m20);	
	Matrix4f Minv={
		detInv*(M.m11*M.m22 - M.m12*M.m21),	detInv*(M.m02*M.m21 - M.m01*M.m22),	detInv*(M.m01*M.m12 - M.m02*M.m11),0,
		detInv*(M.m12*M.m20 - M.m10*M.m22),	detInv*(M.m00*M.m22 - M.m02*M.m20),	detInv*(M.m02*M.m10 - M.m00*M.m12),0,
		detInv*(M.m10*M.m21 - M.m11*M.m20),	detInv*(M.m01*M.m20 - M.m00*M.m21),	detInv*(M.m00*M.m11 - M.m01*M.m10),0,
		0,0,0,1};
		return  Minv;
}
//Get index into volume
inline uint getHashValue(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
}
inline float4 transform4(float4 vec,const Matrix4f M){
           float4 vecOut;
            vecOut.x = M.m00*vec.x + M.m01*vec.y
                     + M.m02*vec.z + M.m03*vec.w;
            vecOut.y = M.m10*vec.x + M.m11*vec.y
                     + M.m12*vec.z + M.m13*vec.w;
            vecOut.z = M.m20*vec.x + M.m21*vec.y
                     + M.m22*vec.z + M.m23*vec.w;
            vecOut.w =  M.m30*vec.x + M.m31*vec.y
                      + M.m32*vec.z + M.m33*vec.w;
           return vecOut;
}
inline float4 worldToImage(float4 pt){
	const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	return (float4)(
			((0.5f*pt.x*MAX_DIM+0.5f*ROWS)),
			((0.5f*pt.y*MAX_DIM+0.5f*COLS)),
			((0.5f*pt.z*MAX_DIM+0.5f*SLICES)),
			0);
}
inline float4 imageToWorld(float4 pt){
	const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	return (float4)(
		2.0f*(pt.x-0.5f*ROWS)/MAX_DIM,
		2.0f*(pt.y-0.5f*COLS)/MAX_DIM,
		2.0f*(pt.z-0.5f*SLICES)/MAX_DIM,0);
}
float interpolate(__global float* data,float x,float y,float z){
	int y0, x0, z0, y1, x1, z1;
	float dx, dy, dz, hx, hy, hz;
		x1 = ceil(x);
		y1 = ceil(y);
		z1 = ceil(z);
		x0 = floor(x);
		y0 = floor(y);
		z0 = floor(z);
		dx = x - x0;
		dy = y - y0;
		dz = z - z0;
	
		// Introduce more variables to reduce computation
		hx = 1.0f - dx;
		hy = 1.0f - dy;
		hz = 1.0f - dz;
		// Optimized below
		return (((getImageValue(data,x0,y0,z0) * hx + getImageValue(data,x1,y0,z0) * dx) * hy + (getImageValue(data,x0,y1,z0)
				* hx + getImageValue(data,x1,y1,z0) * dx)
				* dy)
				* hz + ((getImageValue(data,x0,y0,z1) * hx + getImageValue(data,x1,y0,z1) * dx)
				* hy + (getImageValue(data,x0,y1,z1) * hx + getImageValue(data,x1,y1,z1) * dx) * dy)
				* dz);
	
}
inline float4 getGradientValue(__global float* image,float4 pt){
	float i=pt.x;
	float j=pt.y;
	float k=pt.z;
	float v211 = interpolate(image, i + 0.5f, j, k);
	float v011 = interpolate(image, i - 0.5f, j, k);
	float v121 = interpolate(image, i, j + 0.5f, k);
	float v101 = interpolate(image, i, j - 0.5f, k);
	float v110 = interpolate(image, i, j, k - 0.5f);
	float v112 = interpolate(image, i, j, k + 0.5f);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w = 1E-6f;
	return grad;
}
kernel void copyCapsulesToMesh(

		global int* labels,
		global Springl3D* capsules,
		global float4* vertexBuffer,
		global float4* particleBuffer,
		global float* normalBuffer,
		global float4* normalSegmentBuffer,
		read_only image3d_t colorImage,
		global float4* colorBuffer,
		float transparency,
		uint elements){
	uint id=get_global_id(0);

	if(id>=elements)return;
	const sampler_t volumeSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

	
	capsules+=id;
	vertexBuffer+=id*3;
	normalBuffer+=id*9;
	float4 v;
	float4 particle=SCALE_UP*capsules->particle;
	particle.w=1;
	particleBuffer[id]=particle;
	float a[3];
	for(int i=0;i<3;i++){
		v=SCALE_UP*capsules->vertexes[i];
		a[i]=v.w;
		v.w=1;
		vertexBuffer[i]=v;
	}
	float4 color=0;
	color=read_imagef(colorImage,volumeSampler,(float4)(a[0]*(CUBE_DIM/(float)ROWS),a[1]*(CUBE_DIM/(float)COLS),a[2]*(CUBE_DIM/(float)SLICES),0));
	if(labels[id]<0){
		color=(float4)(1.0f,1.0f,1.0f,1.0f);
	}
	color.w=transparency;
	colorBuffer[3*id]=color;
	colorBuffer[3*id+1]=color;
	colorBuffer[3*id+2]=color;
	
	float4 norm=normalize(cross(vertexBuffer[1]-vertexBuffer[0],vertexBuffer[2]-vertexBuffer[0]));
	norm.w=0;
	for(int i=0;i<3;i++){
		normalBuffer[3*i]=norm.x;
		normalBuffer[3*i+1]=norm.y;
		normalBuffer[3*i+2]=norm.z;
	}			
	normalSegmentBuffer[2*id]=particle;
	normalSegmentBuffer[2*id+1]=particle+0.5f*norm;
	

}
__kernel void copyIsoSurfaceToMesh(
		__global int* labels,
		__global float4* vertexBuffer,
		__global float4* vertexCorrespondenceBuffer,
		__global float* normalBuffer,
		__global float4* colorBuffer,
		read_only image3d_t colorImage,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	vertexBuffer+=3*id;
	vertexCorrespondenceBuffer+=3*id;
	normalBuffer+=9*id;
	colorBuffer+=3*id;
	float4 a,color;
	const sampler_t volumeSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

	float4 norm=normalize(cross(vertexBuffer[1]-vertexBuffer[0],vertexBuffer[2]-vertexBuffer[0]));
	norm.w=0;
	for(int i=0;i<3;i++){
		normalBuffer[0]=norm.x;
		normalBuffer[1]=norm.y;
		normalBuffer[2]=norm.z;
		
		a=vertexCorrespondenceBuffer[i];
		color=read_imagef(colorImage,volumeSampler,(float4)(a.x*(CUBE_DIM/(float)ROWS),a.y*(CUBE_DIM/(float)COLS),a.z*(CUBE_DIM/(float)SLICES),0));
		if(labels[id]<0){
			color=(float4)(1.0f,1.0f,1.0f,1.0f);
		}
		colorBuffer[i]=color;
		normalBuffer+=3;
	}
}
#if GPU
__kernel void computeCapsuleColor(
		global Springl3D* capsules,
		global int* labels,
		global float4* colorBuffer,
		read_only image3d_t colorImage,
		uint elements){
		
	uint id=get_global_id(0);
	float a[3]={128,128,128};
	float4 v;
	if(id>=elements)return;
	
	const float4 BG=0.5f*(float4)(162/255.0f, 165/255.0f, 164/255.0f, 1.0f);
	
	const sampler_t volumeSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
	Springl3D capsule=capsules[id];
	if(labels[id]!=-1){
		a[0]=SCALE_UP*capsule.vertexes[0].w;
		a[1]=SCALE_UP*capsule.vertexes[1].w;
		a[2]=SCALE_UP*capsule.vertexes[2].w;
		if(a[0]*a[0]+a[1]*a[1]+a[2]*a[2]>2){
			colorBuffer[id]=read_imagef(colorImage,volumeSampler,(float4)(a[0]*(CUBE_DIM/(float)ROWS),a[1]*(CUBE_DIM/(float)COLS),a[2]*(CUBE_DIM/(float)SLICES),0));
		} else {
			colorBuffer[id]=BG;
		}
	} else {
		colorBuffer[id]=BG;
	}
}
#endif

//Distance squared between two points
inline float distanceSquaredPoint(float4 pt1,float4 pt2){
	float4 d=(pt1-pt2);
	return (d.x*d.x+d.y*d.y+d.z*d.z);
}
kernel void copyLevelSetImage(global float* srcImage,write_only image3d_t destImage){
	uint id=get_global_id(0);
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float v111 = srcImage[id];
	float v211 = getImageValue(srcImage, i + 1, j, k);
	float v121 = getImageValue(srcImage, i, j + 1, k);
	float v101 = getImageValue(srcImage, i, j - 1, k);
	float v011 = getImageValue(srcImage, i - 1, j, k);
	float v110 = getImageValue(srcImage, i, j, k - 1);
	float v112 = getImageValue(srcImage, i, j, k + 1);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w=1E-6f;
	grad=normalize(grad);
	grad.w=clamp(v111,-10.0f,10.0f);	
	grad=0.1f*grad+1.0f;
	#if ATI
		write_imagef(destImage, (int4)(i,j,k,0),grad);
	#endif 
}
kernel void copyLevelSetImageNoATI(global float* srcImage,global float4* destImage){
	uint id=get_global_id(0);
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	
	float v111 = srcImage[id];
	float v211 = getImageValue(srcImage, i + 1, j, k);
	float v121 = getImageValue(srcImage, i, j + 1, k);
	float v101 = getImageValue(srcImage, i, j - 1, k);
	float v011 = getImageValue(srcImage, i - 1, j, k);
	float v110 = getImageValue(srcImage, i, j, k - 1);
	float v112 = getImageValue(srcImage, i, j, k + 1);

	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w=1E-6f;
	grad=normalize(grad);
	grad.w=clamp(v111,-10.0f,10.0f);	
	if(i==ROWS-1||j==COLS-1||k==SLICES-1||i==0||j==0||k==0)grad.w=max(grad.w,1.5f);
	destImage[id]=grad;
}

float4 QuatMult(const float4 q1, const float4 q2) {
    float4 r;

    // a1a2 - b1b2 - c1c2 - d1d2
    r.x = q1.x * q2.x - q1.y * q2.y - q1.z * q2.z - q1.w * q2.w;
    // a1b2 + b1a2 + c1d2 - d1c2
    r.y = q1.x * q2.y + q1.y * q2.x + q1.z * q2.w - q1.w * q2.z;
    // a1c2 - b1d2 + c1a2 + d1b2
    r.z = q1.x * q2.z - q1.y * q2.w + q1.z * q2.x + q1.w * q2.y;
    // a1d2 + b1c2 - c1b2 + d1a2
    r.w = q1.x * q2.w + q1.y * q2.z - q1.z * q2.y + q1.w * q2.x;

    return r;
}

float4 QuatSqr(const float4 q) {
    float4 r;

    r.x = q.x * q.x - q.y * q.y - q.z * q.z - q.w * q.w;
    r.y = 2.f * q.x * q.y;
    r.z = 2.f * q.x * q.z;
    r.w = 2.f * q.x * q.w;

    return r;
}
inline int IntersectTriPlanar(int showXplane,int showYplane,int showZplane,int row,int col,int slice,float4 rayOrig,float4 rayDir,float4* sliceHitPoint,float4* sliceHitNormal){
    	float tmpIntersect;
		int closestPlane=-1;
		float intersect=1E10f;
		float4 tmp;
		if(showXplane&&fabs(rayDir.x)>1E-3f){
			tmpIntersect=(row-rayOrig.x)/rayDir.x;
			tmp=rayOrig+tmpIntersect*rayDir;
			if(tmp.x>-1&&tmp.y>-1&&tmp.z>-1&&tmp.x<ROWS&&tmp.y<COLS&&tmp.z<SLICES){
				closestPlane=0;
				*sliceHitPoint=tmp;
				*sliceHitNormal=(float4)(1.0f,0.0f,0.0f,0.0f);
				intersect=tmpIntersect;
			}		    
		}
		if(showYplane&&fabs(rayDir.y)>1E-3f){
			tmpIntersect=(col-rayOrig.y)/rayDir.y;
			if(tmpIntersect<intersect){
				tmp=rayOrig+tmpIntersect*rayDir;
				if(tmp.x>-1&&tmp.y>-1&&tmp.z>-1&&tmp.x<ROWS&&tmp.y<COLS&&tmp.z<SLICES){
					closestPlane=1;
					*sliceHitPoint=tmp;
					*sliceHitNormal=(float4)(0.0f,1.0f,0.0f,0.0f);
					intersect=tmpIntersect;
				}		    
			}
		}
		if(showZplane&&fabs(rayDir.z)>1E-3f){
			tmpIntersect=(slice-rayOrig.z)/rayDir.z;
			if(tmpIntersect<intersect){
				tmp=rayOrig+tmpIntersect*rayDir;
				if(tmp.x>-1&&tmp.y>-1&&tmp.z>-1&&tmp.x<ROWS&&tmp.y<COLS&&tmp.z<SLICES){
					closestPlane=2;
					*sliceHitPoint=tmp;
					*sliceHitNormal=(float4)(0.0f,0.0f,1.0f,0.0f);
				}		    
			}
		}
		return closestPlane;
}
#if GPU
float IntersectVolume(Matrix4f modelViewMatrix,read_only image3d_t image,const float4 eyeRayOrig, const float4 eyeRayDir,const uint maxIterations, const float epsilon, float4 *hitPoint,  float4* hitNormal, uint* steps) {
    float dist=1000;
    float4 r0 = eyeRayOrig;
    const sampler_t volumeSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
    float4 norm=(float4)(0,0,0,0);
    uint s = 0;
    const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	const float delta=1.0f/MAX_DIM;
	float4 grad;
	float d;
	do {
		r0.w=1;
		float4 pt=worldToImage(transform4(r0,modelViewMatrix));
		pt.w=0;
		grad=read_imagef(image,volumeSampler,pt);
		d = max(0.0f,grad.w);
		grad.w=0;
		if(d<epsilon){
			*hitNormal =grad;
			r0.w=0;
    		*hitPoint = r0;
    		return d;
		}
		s++;
		d=0.98f*d;
		r0 += delta*eyeRayDir*d;
		r0.w=0;
	} while ( (dot(r0, r0) < BOUNDING_RADIUS_2)&&s<maxIterations);
	*steps= s;
    return dist;
}

float IntersectVolumeColor(
		const global Springl3D* capsules,
		const global int* indexMap,
		const global int* spatialLookUp,
		const global float4* springlColors,
		Matrix4f modelViewMatrix,read_only image3d_t image,const float4 eyeRayOrig, const float4 eyeRayDir,const uint maxIterations, const float epsilon, float4 *hitPoint,  float4* hitNormal, float4* hitDiffuse,uint* steps) {
    float dist=1000;
    float4 r0 = eyeRayOrig;
    const sampler_t volumeSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
    float4 norm=(float4)(0,0,0,0);
    uint s = 0;
    const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	const float delta=1.0f/MAX_DIM;
	float4 grad;
	float d;
	int startIndex=-1;
	float4 pt;
	do {
		r0.w=1;
		pt=worldToImage(transform4(r0,modelViewMatrix));
		pt.w=0;
		grad=read_imagef(image,volumeSampler,pt);
		d = max(0.0f,grad.w);
		grad.w=0;
		if(d<epsilon){
			*hitNormal =grad;
			r0.w=0;
    		*hitPoint = r0;
    		dist=d;
			break;
		}
		s++;
		d=0.98f*d;
		r0 += delta*eyeRayDir*d;
		r0.w=0;
	} while ( (dot(r0, r0) < BOUNDING_RADIUS_2)&&s<maxIterations);
	Springl3D cap;
	int refId=-1;
	
	startIndex = indexMap[getSafeIndex(round(pt.x),round(pt.y),round(pt.z))];
	if(startIndex>=0){
		spatialLookUp+=MAX_BIN_SIZE*startIndex;
		float minDist=1E10;
		for(int index = 1;index<MAX_BIN_SIZE;index++){
			//If at the start of a new key section, break;
			int id=spatialLookUp[index];
			if(id<0)break;
			cap = capsules[id];
			cap.particle*=SCALE_UP;
			cap.particle.w=0;
			d=distance(cap.particle,pt);
			if(d<minDist){
				minDist=d;
				refId=id;
			}
		}
		if(refId>=0){*hitDiffuse=springlColors[refId];}
	} 
	*steps= s;
    return dist;
}
#endif

//OpenCL implementation of algorithm found in:
//Schneider, P. J. and D. H. Eberly (2003). Geometric tools for computer graphics, Morgan Kaufmann Pub.

 int rayTriangleIntersect(float4* pts,float4 org, float4 dr,float4* intersect) {
	// compute the offset origin, edges, and normal
	//float4 dr = normalize(v);
	float len = 1E3f;
	float4 kDiff =   org-pts[0];
	float4 kEdge1 =  pts[1]-pts[0];
	float4 kEdge2 =  pts[2]-pts[0];
	float4 kNormal = cross(kEdge1, kEdge2);
	float fDdN = dot(dr,kNormal);	
	float fSign=sign(fDdN);
	fDdN = fSign*fDdN;
	float4 crs = cross(kDiff, kEdge2);
	float fDdQxE2 = fSign * dot(dr,crs);
	crs=cross(kEdge1, kDiff);
	float fDdE1xQ = fSign * dot(dr,crs);
	float fQdN = -fSign * dot(kDiff,kNormal);
	float fExtDdN = len * fDdN;
	if (fDdQxE2 >= 0.0f&&fDdE1xQ >= 0.0f&&fDdQxE2 + fDdE1xQ <= fDdN&&-fExtDdN <= fQdN && fQdN <= fExtDdN) {
		// segment intersects triangle
		*intersect=org+dr*(fQdN / fDdN);
		return 1;
	}
	return 0;
}

// OpenCL implementation of algorithm found in:
// Schneider, P. J. and D. H. Eberly (2003). Geometric tools for computer graphics, Morgan Kaufmann Pub.
// Source code licensed under:
// Geometric Tools, LLC
// Copyright (c) 1998-2011
// Distributed under the Boost Software License, Version 1.0.
// http://www.boost.org/LICENSE_1_0.txt
// http://www.geometrictools.com/License/Boost/LICENSE_1_0.txt

float IntersectTriangle(

                        const Matrix4f modelViewMatrix,
                        const Matrix4f modelViewInverseMatrix,
						const global int* indexMap,
						const global int* keys,
						const global uint* values,
						const global Springl3D* capsules,
                        uint N,
                        float4* oldR0,
                        float4 eyeRayOrig, 
                        float4 eyeRayDir,
                        const uint maxIterations,
                        const float epsilon,
                        float4 *hitPoint,
                        float4* hitNormal,
                        int* refId,
                        uint* steps) {
    float4 r0 = *oldR0;
    uint s = *steps;
    const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	const float delta=(4.0f/MAX_DIM);
	Springl3D cap;
	int index;
	int startIndex=-1;
	int hashValue;
	float4 pt;
	float4 closestPt;
	float4 origin=worldToImage(transform4(eyeRayOrig,modelViewMatrix));	
	int flag,id;
	do {
		r0.w=1;
		pt=worldToImage(transform4(r0,modelViewMatrix));
		hashValue=getSafeIndex(round(pt.x),round(pt.y),round(pt.z));
		startIndex = indexMap[hashValue];
		r0 += delta*eyeRayDir;
		if(startIndex>=0){
			break;
		}
		s++;
		r0.w=0;		
	} while ( (dot(r0, r0) < BOUNDING_RADIUS_2)&&s<maxIterations);
	if(startIndex<0)return -2;
	for(index = startIndex;index<N&&keys[index] == hashValue;index++){
		//If at the start of a new key section, break;
		id=values[index];
		cap = capsules[id];
		cap.vertexes[0]*=SCALE_UP;
		cap.vertexes[1]*=SCALE_UP;
		cap.vertexes[2]*=SCALE_UP;
		//Compute distance squared between point and triangle
		flag=rayTriangleIntersect(cap.vertexes,origin,(pt-origin),&closestPt);
		if(flag){
			*hitPoint=transform4(imageToWorld(closestPt),modelViewInverseMatrix);
			*hitNormal = normalize(cross(cap.vertexes[1]-cap.vertexes[0],cap.vertexes[2]-cap.vertexes[0]));
			*refId=id;
			*steps= s+1; 
			*oldR0=r0;
			return 0;
		}
	}
	*steps=s;
	*oldR0=r0;
    return -1;
}
float NBIntersectTriangle(

                        const Matrix4f modelViewMatrix,
                        const Matrix4f modelViewInverseMatrix,
						const global int* indexMap,
						const global int* spatialLookUp,
						const global Springl3D* capsules,
                        uint N,
                        float4* oldR0,
                        float4 eyeRayOrig, 
                        float4 eyeRayDir,
                        const uint maxIterations,
                        const float epsilon,
                        float4 *hitPoint,
                        float4* hitNormal,
                        int* refId,
                        uint* steps) {
    float4 r0 = *oldR0;
    uint s = *steps;
    const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	const float delta=(4.0f/MAX_DIM);
	Springl3D cap;
	int index;
	int startIndex=-1;
	int hashValue;
	float4 pt;
	float4 closestPt;
	float4 origin=worldToImage(transform4(eyeRayOrig,modelViewMatrix));	
	int flag,id;
	do {
		r0.w=1;
		pt=worldToImage(transform4(r0,modelViewMatrix));
		hashValue=getSafeIndex(round(pt.x),round(pt.y),round(pt.z));
		startIndex = indexMap[hashValue];
		r0 += delta*eyeRayDir;
		if(startIndex>=0&&spatialLookUp[MAX_BIN_SIZE*startIndex]>0){
			break;
		}
		s++;
		r0.w=0;		
	} while ( (dot(r0, r0) < BOUNDING_RADIUS_2)&&s<maxIterations);
	if(startIndex<0)return -2;
	
	spatialLookUp+=MAX_BIN_SIZE*startIndex;
	for(index = 1;index<MAX_BIN_SIZE;index++){
		//If at the start of a new key section, break;
		id=spatialLookUp[index];
		if(id<0)break;
		cap = capsules[id];
		cap.vertexes[0]*=SCALE_UP;
		cap.vertexes[1]*=SCALE_UP;
		cap.vertexes[2]*=SCALE_UP;
		//Compute distance squared between point and triangle
		flag=rayTriangleIntersect(cap.vertexes,origin,(pt-origin),&closestPt);
		if(flag){
			*hitPoint=transform4(imageToWorld(closestPt),modelViewInverseMatrix);
			*hitNormal = normalize(cross(cap.vertexes[1]-cap.vertexes[0],cap.vertexes[2]-cap.vertexes[0]));
			*refId=id;
			*steps= s+1; 
			*oldR0=r0;
			return 0;
		}
	}
	*steps=s;
	*oldR0=r0;
    return -1;
}
float IntersectFloorSphere(const float4 eyeRayOrig, const float4 eyeRayDir) {
    const float4 op = WORLD_CENTER - eyeRayOrig;
    const float b = dot(op, eyeRayDir);
    float det = b * b - dot(op, op) + WORLD_RADIUS * WORLD_RADIUS;

    if (det < 0.f)
        return -1.f;
    else
        det = sqrt(det);

    float t = b - det;
    if (t > 0.f)
        return t;
    else {
        // We are inside, avoid the hit
        return -1.f;
    }
}
float IntersectBoundingSphere(const float4 eyeRayOrig, const float4 eyeRayDir) {
    const float4 op = -eyeRayOrig;
    const float b = dot(op, eyeRayDir);
    float det = b * b - dot(op, op) + BOUNDING_RADIUS_2;

    if (det < 0.f)
        return -1.f;
    else
        det = sqrt(det);

    float t = b - det;
    if (t > 0.f)
        return t;
    else {
        t = b + det;

        if (t > 0.f) {
            // We are inside, start from the ray origin
            return 0.0f;
        } else
            return -1.f;
    }
}
float4 Phong(const float4 light, const float4 eye, const float4 pt, float4 N, const float4 diffuse,int twoSided) {

    const float4 ambient = (float4) (0.2f, 0.2f, 0.2f, 0.f);
    float4 L = normalize(light - pt);
    float NdotL = dot(N, L);
    //Surface is two sided!
    
    if (NdotL < 0.f){
		if(twoSided){
			//return diffuse * ambient;
			//two-sided rendering
			NdotL=-NdotL;
			N=-N;
		} else {
			return diffuse*ambient;
		}
	}
    const float specularExponent = 10.0f;
    const float specularity = 0.45f;

    float4 E = normalize(eye - pt);
    float4 H = (L + E) * (float) 0.5f;
	
    return diffuse * NdotL +
           specularity * pow(dot(N, H), specularExponent) +
           diffuse * ambient;
           
}
kernel void multiply(global float *array, const int numElements, const float s) {
    const int gid = get_global_id(0);
    if (gid >= numElements)  {
        return;
    }
    array[gid] *= s;
}
#if GPU
kernel void IsoSurfRender(
						global float* pixels,
						read_only image3d_t refImage,
						read_only image3d_t volume,
						const global float4* springlColors,
						const global int* indexMap,
						const global int* spatialLookUp,
						const global Springl3D* capsules,
                        const global RenderingConfig *config,
                        const global Matrix4f* modelViewMatrix,
                        const global Matrix4f* modelViewInverseMatrix,
                        int enableAccumulation,
                        float sampleX,
                        float sampleY,
                        float fg_r,float fg_g,float fg_b,
                        float row,
                        float col,
                        float slice,
                        int showXplane,
                        int showYplane,
                        int showZplane,
                        int showIsoSurf,
                        int showVertexTracking,
                        float minImageValue,
                        float maxImageValue,
                        float brightness,
                        float contrast,
                        float transparency) {

    const int gid = get_global_id(0);
    unsigned width = config->width;
    unsigned height = config->height;
	Matrix4f M=*modelViewMatrix;
	Matrix4f Minv=*modelViewInverseMatrix;
	const sampler_t imageSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
	
    const unsigned int x = gid % width;
    const int y = gid / width;
	
    // Check if we have to do something
    if (y >= height)
        return;

    const float epsilon = config->actvateFastRendering ? (config->epsilon * (1.f / 0.75f)) : config->epsilon;
    const uint maxIterations = max(1u, config->actvateFastRendering ? (config->maxIterations - 1) : config->maxIterations);

    const float4 light = (float4) (config->light[0], config->light[1], config->light[2], 0.f);
    const global Camera *camera = &config->camera;

    //--------------------------------------------------------------------------
    // Calculate eye ray
    //--------------------------------------------------------------------------

    const float invWidth = 1.f / width;
    const float invHeight = 1.f / height;
    const float kcx = (x + sampleX) * invWidth - .5f;
    const float4 kcx4 = (float4) kcx;
    const float kcy = (y + sampleY) * invHeight - .5f;
    const float4 kcy4 = (float4) kcy;

    const float4 cameraX = (float4) (camera->x.x, camera->x.y, camera->x.z, 0.f);
    const float4 cameraY = (float4) (camera->y.x, camera->y.y, camera->y.z, 0.f);
    const float4 cameraDir = (float4) (camera->dir.x, camera->dir.y, camera->dir.z, 0.f);
    const float4 cameraOrig = (float4) (camera->orig.x, camera->orig.y, camera->orig.z, 0.f);

    const float4 eyeRayDir = normalize(cameraX * kcx4 + cameraY * kcy4 + cameraDir);
    const float4 eyeRayOrig = eyeRayDir * (float4) 0.1f + cameraOrig;

    //--------------------------------------------------------------------------
    // Check if we hit the bounding sphere
    //--------------------------------------------------------------------------

    float distSet = IntersectBoundingSphere(eyeRayOrig, eyeRayDir);
    float4 hitPoint;
    float4 hitNormal;
    int hitClipPlane=0;
    float4 surfHitPoint;
    float4 tmp;
    float4 diffuse, n, color;
    if (distSet >= 0.f) {
        //--------------------------------------------------------------------------
        // Find the intersection with the set
        //--------------------------------------------------------------------------

        uint steps=0;
        float4 rayOrigWorld= eyeRayOrig + eyeRayDir * (float4) distSet;
        rayOrigWorld.w=1;
        float4 rayOrig=worldToImage(transform4(rayOrigWorld,M));
        eyeRayDir.w=0;
        float4 rayDir=transform4(eyeRayDir,M);
        
    	diffuse = (float4) (fg_r,fg_g,fg_b, 0.f);
        if(showIsoSurf){
        	if(showVertexTracking){
		        distSet = IntersectVolumeColor(capsules,indexMap,spatialLookUp,springlColors,M,volume,rayOrigWorld, eyeRayDir, maxIterations, epsilon, &hitPoint, &hitNormal,&diffuse, &steps);  		
        	} else {
	        	distSet = IntersectVolume(M,volume,rayOrigWorld, eyeRayDir, maxIterations, epsilon, &hitPoint, &hitNormal, &steps);
	        }
	        surfHitPoint=worldToImage(transform4(hitPoint,M));
	        if (distSet > epsilon)
	            distSet = -1.f;
        } else {
        	distSet=-1;
        }
        float4 sliceHitPoint;	
		float4 sliceHitNormal;
		int closestPlane=IntersectTriPlanar(showXplane,showYplane,showZplane,row,col,slice,rayOrig,rayDir,&sliceHitPoint,&sliceHitNormal);
		if(closestPlane>=0&&(distSet<0||distance(sliceHitPoint,rayOrig)<distance(surfHitPoint,rayOrig))){
			//sliceHitPoint=clamp(sliceHitPoint,(float4)(1,1,1,0),(float4)(ROWS-2,COLS-2,SLICES-2,0));
			tmp=read_imagef(refImage,imageSampler,sliceHitPoint);
			float val=(tmp.x-minImageValue)/(maxImageValue-minImageValue);
		    val=clamp(val*contrast + brightness,0.0f,1.0f);
		    tmp=read_imagef(volume,imageSampler,sliceHitPoint);
			float cweight=clamp(tanh(-tmp.w),0.0f,1.0f);
			diffuse=(cweight*(float4)(fg_r,fg_g,fg_b,1.0f)+(1-cweight)*((float4)(0,0,0,1.0f)));
			diffuse=(1-transparency)*(float4)(val,val,val,1.0f)+transparency*diffuse;
		    distSet=0;
		    hitClipPlane=1;
		    hitNormal=sliceHitNormal;
		    hitPoint=transform4(imageToWorld(sliceHitPoint),Minv);
	    } 
    }
    //--------------------------------------------------------------------------
    // Check if we hit the floor
    //--------------------------------------------------------------------------

    float distFloor = IntersectFloorSphere(eyeRayOrig, eyeRayDir);

    //--------------------------------------------------------------------------
    // Select the hit point
    //--------------------------------------------------------------------------

    int doShade = 0;
    int useAO = 1;
    
    if ((distSet < 0.f) && (distFloor < 0.f)) {
        // Sky hit
        color =  (float4)  (config->color[0],config->color[1],config->color[2],config->color[3]);
    } else if ((distSet >= 0.f) && ((distFloor < 0.f) || (distSet <= distFloor))) {
        // Set hit
        float4 ct=worldToImage(transform4(hitPoint,M));

		n=normalize(transform4(hitNormal,Minv));
        doShade = 1;
    } else if ((distFloor >= 0.f) && ((distSet < 0.f) || (distFloor <= distSet))) {
	        // Floor hit
	    if (config->enableShadow&&!hitClipPlane){
	        hitPoint = eyeRayOrig + eyeRayDir * (float4) distFloor;
	        n = hitPoint - WORLD_CENTER;
	        n = normalize(n);
	        // The most important feature in a ray tracer: a checker texture !
	        const int ix = (hitPoint.x > 0.f) ? hitPoint.x : (1.f - hitPoint.x);
	        const int iz = (hitPoint.z > 0.f) ? hitPoint.z : (1.f - hitPoint.z);
	        if ((ix + iz) % 2)
	            diffuse = (float4) (0.75f, 0.75f, 0.75f, 0.f);
	        else
	            diffuse = (float4) (87/255.0f,90/255.0f,93/255.0f,0);
	        doShade = 1;
	        useAO = 0;
        } else {
        	color =  (float4) (config->color[0],config->color[1],config->color[2],config->color[3]);	
        }
    }
    //--------------------------------------------------------------------------
    // Select the shadow pass
    //--------------------------------------------------------------------------

    if (doShade) {
        float shadowFactor = 1.f;
        if (config->enableShadow) {
            float4 L = normalize(light - hitPoint);
            float4 rO = hitPoint + n * 1e-2f;
            float4 shadowHitPoint;

            // Check bounding sphere
            float shadowDistSet = IntersectBoundingSphere(rO, L);
            
            if (shadowDistSet >= 0.f) {
                uint steps;

                rO = rO + L * (float4) shadowDistSet;
                shadowDistSet = IntersectVolume(M,volume,rO, L,  maxIterations, epsilon,
                        &shadowHitPoint, &hitNormal, &steps);
                if (shadowDistSet < epsilon) {
                    if (useAO) {
                        // Use steps count to simulate ambient occlusion
                        shadowFactor = 0.6f - min(steps / 255.f, 0.5f);
                    } else
                        shadowFactor = 0.6f;
                }
            } else
            
                shadowDistSet = -1.f;
        }
		
        //--------------------------------------------------------------------------
        // Direct lighting of hit point
        //--------------------------------------------------------------------------

        color = Phong(light, eyeRayOrig, hitPoint, n, diffuse,hitClipPlane) * shadowFactor;
    }

    //--------------------------------------------------------------------------
    // Write pixel
    //--------------------------------------------------------------------------

    int offset = 3 * (x + y * width);
    color = clamp(color, (float4) (0.f, 0.f, 0.f, 0.f), (float4) (1.f, 1.f, 1.f, 0.f));
    
    if (enableAccumulation) {
        pixels[offset++] += color.s0;
        pixels[offset++] += color.s1;
        pixels[offset  ] += color.s2;
    } else {
        pixels[offset++] = color.s0;
        pixels[offset++] = color.s1;
        pixels[offset  ] = color.s2;
    }
    
}
kernel void SpringlsRender(

						global float* pixels,
						read_only image3d_t refImage,
						read_only image3d_t volume,
						const global Springl3D* capsules,
						const global float4* colors,
						const global int* indexMap,
						const global int* keys,
						const global uint* values,
                        const global RenderingConfig *config,
                        const global Matrix4f* modelViewMatrix,
                        const global Matrix4f* modelViewInverseMatrix,
                        uint N,
                        uint elements,
                        int enableAccumulation,
                        float sampleX,
                        float sampleY,
                        float fg_r,float fg_g,float fg_b,
                        float row,
                        float col,
                        float slice,
                        int showXplane,
                        int showYplane,
                        int showZplane,
                        int showIsoSurf,
                        float minImageValue,
                        float maxImageValue,
                        float brightness,
                        float contrast,
                        float transparency) {
	
    const int gid = get_global_id(0);
    unsigned width = config->width;
    unsigned height = config->height;
	const sampler_t imageSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_MIRRORED_REPEAT | CLK_FILTER_LINEAR;

    const unsigned int x = gid % width;
    const int y = gid / width;

    // Check if we have to do something
    if (y >= height)
        return;

    const float epsilon = config->actvateFastRendering ? (config->epsilon * (1.f / 0.75f)) : config->epsilon;
    const uint maxIterations = max(1u, config->actvateFastRendering ? (config->maxIterations - 1) : config->maxIterations);

    const float4 light = (float4) (config->light[0], config->light[1], config->light[2], 0.f);
    const global Camera *camera = &config->camera;

    //--------------------------------------------------------------------------
    // Calculate eye ray
    //--------------------------------------------------------------------------

    const float invWidth = 1.f / width;
    const float invHeight = 1.f / height;
    const float kcx = (x + sampleX) * invWidth - .5f;
    const float4 kcx4 = (float4) kcx;
    const float kcy = (y + sampleY) * invHeight - .5f;
    const float4 kcy4 = (float4) kcy;

    const float4 cameraX = (float4) (camera->x.x, camera->x.y, camera->x.z, 0.f);
    const float4 cameraY = (float4) (camera->y.x, camera->y.y, camera->y.z, 0.f);
    const float4 cameraDir = (float4) (camera->dir.x, camera->dir.y, camera->dir.z, 0.f);
    const float4 cameraOrig = (float4) (camera->orig.x, camera->orig.y, camera->orig.z, 0.f);

    const float4 eyeRayDir = normalize(cameraX * kcx4 + cameraY * kcy4 + cameraDir);
    const float4 eyeRayOrig = eyeRayDir * (float4) 0.1f + cameraOrig;

    //--------------------------------------------------------------------------
    // Check if we hit the bounding sphere
    //--------------------------------------------------------------------------

    float distSet = IntersectBoundingSphere(eyeRayOrig, eyeRayDir);
    
    float4 hitPoint;
    float4 hitNormal;
	int refId=0;
	float4 hitPoint2;
	float4 hitNormal2;
	const float4 BG=0.5f*(float4)(fg_r,fg_g,fg_b, 0.f);
    float4 hitColor=BG;
    Matrix4f M=*modelViewMatrix;
    Matrix4f Minv=*modelViewInverseMatrix;
    eyeRayOrig.w=1;
    float4 surfHitPoint;
    float4 origin=worldToImage(transform4(eyeRayOrig,M));
	float4 diffuse, n, color;
    float4 tmp;
    int hitClipPlane=0;
    if (distSet >= 0.f) {
		uint steps=0;
        float4 rayOrigWorld= eyeRayOrig + eyeRayDir * (float4) distSet;
        float4 rayOrig=worldToImage(transform4(rayOrigWorld,M));
        eyeRayDir.w=0;
        float4 rayDir=transform4(eyeRayDir,M);
        float4 r0=rayOrigWorld;
	    float distSet2 =0;
		if(showIsoSurf){
		    distSet2 =IntersectVolume(M,volume,rayOrigWorld, eyeRayDir, maxIterations, epsilon, &hitPoint2, &hitNormal2, &steps);
		    surfHitPoint=worldToImage(transform4(hitPoint2,M));
	    } else {
	     	distSet2 =2000;
	    }
	    //The intel compiler crashes if you try to write this as a for loop.
	    if (distSet2 < epsilon ) {
            distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);   
			if(distSet==-1){
	        	distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
				if(distSet==-1){
	        		distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
					if(distSet==-1){
	        			distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
						if(distSet==-1){
	        				distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
							if(distSet==-1){
		        				distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
								if(distSet==-1){
			        				distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
									if(distSet==-1){
				        				distSet=IntersectTriangle(M,Minv,indexMap,keys,values,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
									}
								}
							}
						}
					}	
				}
            } 
            
	        if(distSet>=0&&distance(hitPoint,hitPoint2)<0.1f){
	       		diffuse=colors[refId];
			} else {
				diffuse=BG;
			}			
			distSet=0;
	    } else {
	    	diffuse=BG;
	    	distSet=-1;
	    }
	    hitNormal=hitNormal2;
		hitPoint=hitPoint2;
		
		float4 sliceHitPoint;	
		float4 sliceHitNormal;
		int closestPlane=IntersectTriPlanar(showXplane,showYplane,showZplane,row,col,slice,rayOrig,rayDir,&sliceHitPoint,&sliceHitNormal);
		if(closestPlane>=0&&(distSet<0||distance(sliceHitPoint,rayOrig)<distance(surfHitPoint,rayOrig))){
			//sliceHitPoint=clamp(sliceHitPoint,(float4)(1,1,1,0),(float4)(ROWS-2,COLS-2,SLICES-2,0));
			tmp=read_imagef(refImage,imageSampler,sliceHitPoint);
			float val=(tmp.x-minImageValue)/(maxImageValue-minImageValue);
		    val=clamp(val*contrast + brightness,0.0f,1.0f);
		    tmp=read_imagef(volume,imageSampler,sliceHitPoint);
			float cweight=clamp(tanh(-tmp.w),0.0f,1.0f);
			diffuse=(cweight*(float4)(fg_r,fg_g,fg_b,1.0f)+(1-cweight)*((float4)(0,0,0,1.0f)));
			diffuse=(1-transparency)*(float4)(val,val,val,1.0f)+transparency*diffuse;
		    distSet=0;
		    hitClipPlane=1;
		    hitNormal=sliceHitNormal;
		    hitPoint=transform4(imageToWorld(sliceHitPoint),Minv);
	    } 
    }
//--------------------------------------------------------------------------
    // Check if we hit the floor
    //--------------------------------------------------------------------------

    float distFloor = IntersectFloorSphere(eyeRayOrig, eyeRayDir);

    //--------------------------------------------------------------------------
    // Select the hit point
    //--------------------------------------------------------------------------

    int doShade = 0;
    int useAO = 1;
    if ((distSet < 0.f) && (distFloor < 0.f)) {
        // Sky hit
        color = (float4)  (config->color[0],config->color[1],config->color[2],config->color[3]);
    } else if ((distSet >= 0.f) && ((distFloor < 0.f) || (distSet <= distFloor))) {
        // Set hit
		n=normalize(transform4(hitNormal,Minv));
        doShade = 1;
    } else if ((distFloor >= 0.f) && ((distSet < 0.f) || (distFloor <= distSet))) {
        // Floor hit
        if (config->enableShadow){
	        hitPoint = eyeRayOrig + eyeRayDir * (float4) distFloor;
	        n = hitPoint - WORLD_CENTER;
	        n = normalize(n);
	        // The most important feature in a ray tracer: a checker texture !
	        const int ix = (hitPoint.x > 0.f) ? hitPoint.x : (1.f - hitPoint.x);
	        const int iz = (hitPoint.z > 0.f) ? hitPoint.z : (1.f - hitPoint.z);
	        if ((ix + iz) % 2)
	            diffuse = (float4) (0.75f, 0.75f, 0.75f, 0.f);
	        else
	            diffuse = (float4) (87/255.0f,90/255.0f,93/255.0f,0);
	        doShade = 1;
	        useAO = 0;
        } else {
        	color = (float4)  (config->color[0],config->color[1],config->color[2],config->color[3]);
        }
    }
    //--------------------------------------------------------------------------
    // Select the shadow pass
    //--------------------------------------------------------------------------

    if (doShade) {
        float shadowFactor = 1.f;
        
        if (config->enableShadow) {
            float4 L = normalize(light - hitPoint);
            float4 rO = hitPoint + n * 1e-2f;
            float4 shadowHitPoint;

            // Check bounding sphere
            float shadowDistSet = IntersectBoundingSphere(rO, L);
            if (shadowDistSet >= 0.f) {
                uint steps;

                rO = rO + L * (float4) shadowDistSet;
                shadowDistSet = IntersectVolume(M,volume,rO, L,  maxIterations, epsilon,
                        &shadowHitPoint, &hitNormal, &steps);
                if (shadowDistSet < epsilon) {
                    if (useAO) {
                        // Use steps count to simulate ambient occlusion
                        shadowFactor = 0.6f - min(steps / 255.f, 0.5f);
                    } else
                        shadowFactor = 0.6f;
                }
            } else
                shadowDistSet = -1.f;
        }
		
        //--------------------------------------------------------------------------
        // Direct lighting of hit point
        //--------------------------------------------------------------------------

        color = Phong(light, eyeRayOrig, hitPoint, n, diffuse,hitClipPlane) * shadowFactor;
    }

    int offset = 3 * (x + y * width);
    color = clamp(color, (float4) (0.f, 0.f, 0.f, 0.f), (float4) (1.f, 1.f, 1.f, 0.f));
    if (enableAccumulation) {
        pixels[offset++] += color.s0;
        pixels[offset++] += color.s1;
        pixels[offset  ] += color.s2;
    } else {
        pixels[offset++] = color.s0;
        pixels[offset++] = color.s1;
        pixels[offset  ] = color.s2;
    }
    
}
kernel void NBSpringlsRender(

						global float* pixels,
						read_only image3d_t refImage,
						read_only image3d_t volume,
						const global Springl3D* capsules,
						const global float4* colors,
						const global int* indexMap,
						const global int* spatialLookUp,
                        const global RenderingConfig *config,
                        const global Matrix4f* modelViewMatrix,
                        const global Matrix4f* modelViewInverseMatrix,
                        uint N,
                        uint elements,
                        int enableAccumulation,
                        float sampleX,
                        float sampleY,
                        float fg_r,float fg_g,float fg_b,
                        float row,
                        float col,
                        float slice,
                        int showXplane,
                        int showYplane,
                        int showZplane,
                        int showIsoSurf,
                        int showVertexTracking,
                        float minImageValue,
                        float maxImageValue,
                        float brightness,
                        float contrast,
                        float transparency) {
	
    const int gid = get_global_id(0);
    unsigned width = config->width;
    unsigned height = config->height;

    const unsigned int x = gid % width;
    const int y = gid / width;

    // Check if we have to do something
    if (y >= height)
        return;

    const float epsilon = config->actvateFastRendering ? (config->epsilon * (1.f / 0.75f)) : config->epsilon;
    const uint maxIterations = max(1u, config->actvateFastRendering ? (config->maxIterations - 1) : config->maxIterations);

    const float4 light = (float4) (config->light[0], config->light[1], config->light[2], 0.f);
    const global Camera *camera = &config->camera;

    //--------------------------------------------------------------------------
    // Calculate eye ray
    //--------------------------------------------------------------------------
	const sampler_t imageSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_MIRRORED_REPEAT | CLK_FILTER_LINEAR;

    const float invWidth = 1.f / width;
    const float invHeight = 1.f / height;
    const float kcx = (x + sampleX) * invWidth - .5f;
    const float4 kcx4 = (float4) kcx;
    const float kcy = (y + sampleY) * invHeight - .5f;
    const float4 kcy4 = (float4) kcy;

    const float4 cameraX = (float4) (camera->x.x, camera->x.y, camera->x.z, 0.f);
    const float4 cameraY = (float4) (camera->y.x, camera->y.y, camera->y.z, 0.f);
    const float4 cameraDir = (float4) (camera->dir.x, camera->dir.y, camera->dir.z, 0.f);
    const float4 cameraOrig = (float4) (camera->orig.x, camera->orig.y, camera->orig.z, 0.f);

    const float4 eyeRayDir = normalize(cameraX * kcx4 + cameraY * kcy4 + cameraDir);
    const float4 eyeRayOrig = eyeRayDir * (float4) 0.1f + cameraOrig;

    //--------------------------------------------------------------------------
    // Check if we hit the bounding sphere
    //--------------------------------------------------------------------------

    float distSet = IntersectBoundingSphere(eyeRayOrig, eyeRayDir);
    
    float4 hitPoint;
    float4 hitNormal;
	int refId=0;
	float4 hitPoint2;
	float4 hitNormal2;
	const float4 BG=0.5f*(float4)(fg_r,fg_g,fg_b, 0.f);
    float4 hitColor=BG;
    Matrix4f M=*modelViewMatrix;
    Matrix4f Minv=*modelViewInverseMatrix;
    eyeRayOrig.w=1;
    float4 diffuse, n, color;
    float4 tmp;
    int hitClipPlane=0;
    float4 origin=worldToImage(transform4(eyeRayOrig,M));
    float4 surfHitPoint;
    
    if (distSet >= 0.f) {
		uint steps=0;
        float4 rayOrigWorld= eyeRayOrig + eyeRayDir * (float4) distSet;
        float4 rayOrig=worldToImage(transform4(rayOrigWorld,M));
        eyeRayDir.w=0;
        float4 rayDir=transform4(eyeRayDir,M);
        float4 r0=rayOrigWorld;
	    float distSet2 =0;
		if(showIsoSurf){
		    distSet2 =IntersectVolume(M,volume,rayOrigWorld, eyeRayDir, maxIterations, epsilon, &hitPoint2, &hitNormal2, &steps);
		    surfHitPoint=worldToImage(transform4(hitPoint2,M));
	    } else {
	     	distSet2 =2000;
	    }
	    //The intel compiler crashes if you try to write this as a for loop.
	    if (distSet2 < epsilon ) {
            distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);   
			
			if(distSet==-1){
	        	distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
				if(distSet==-1){
	        		distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
					if(distSet==-1){
	        			distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
						if(distSet==-1){
	        				distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
							if(distSet==-1){
		        				distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
								if(distSet==-1){
			        				distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
									if(distSet==-1){
				        				distSet=NBIntersectTriangle(M,Minv,indexMap,spatialLookUp,capsules,N,&r0,rayOrigWorld, eyeRayDir,maxIterations,epsilon,&hitPoint,&hitNormal,&refId,&steps);
									}
								}
							}
						}
					}	
				}
            } 
            
	        if(distSet>=0&&distance(hitPoint,hitPoint2)<0.1f){
	       		diffuse=(showVertexTracking)?colors[refId]:(float4)(fg_r,fg_g,fg_b, 0.f);
			} else {
				diffuse=BG;
			}			
			distSet=0;
	    } else {
	    	diffuse=BG;
	    	distSet=-1;
	    }
	    hitNormal=hitNormal2;
		hitPoint=hitPoint2;
		
		float4 sliceHitPoint;	
		float4 sliceHitNormal;
		int closestPlane=IntersectTriPlanar(showXplane,showYplane,showZplane,row,col,slice,rayOrig,rayDir,&sliceHitPoint,&sliceHitNormal);
		if(closestPlane>=0&&(distSet<0||distance(sliceHitPoint,rayOrig)<distance(surfHitPoint,rayOrig))){
			sliceHitPoint=clamp(sliceHitPoint,(float4)(1,1,1,0),(float4)(ROWS-2,COLS-2,SLICES-2,0));
			tmp=read_imagef(refImage,imageSampler,sliceHitPoint);
			float val=(tmp.x-minImageValue)/(maxImageValue-minImageValue);
		    val=clamp(val*contrast + brightness,0.0f,1.0f);
		    tmp=read_imagef(volume,imageSampler,sliceHitPoint);
			float cweight=clamp(tanh(-tmp.w),0.0f,1.0f);
			diffuse=(cweight*(float4)(fg_r,fg_g,fg_b,1.0f)+(1-cweight)*((float4)(0,0,0,1.0f)));
			diffuse=(1-transparency)*(float4)(val,val,val,1.0f)+transparency*diffuse;
		    distSet=0;
		    hitClipPlane=1;
		    hitNormal=sliceHitNormal;
		    hitPoint=transform4(imageToWorld(sliceHitPoint),Minv);
	    } 
    }

//--------------------------------------------------------------------------
    // Check if we hit the floor
    //--------------------------------------------------------------------------

    float distFloor = IntersectFloorSphere(eyeRayOrig, eyeRayDir);

    //--------------------------------------------------------------------------
    // Select the hit point
    //--------------------------------------------------------------------------

    int doShade = 0;
    int useAO = 1;
    if ((distSet < 0.f) && (distFloor < 0.f)) {
        // Sky hit
        color = (float4)  (config->color[0],config->color[1],config->color[2],config->color[3]);
    } else if ((distSet >= 0.f) && ((distFloor < 0.f) || (distSet <= distFloor))) {
        // Set hit
		n=normalize(transform4(hitNormal,Minv));
        doShade = 1;
    } else if ((distFloor >= 0.f) && ((distSet < 0.f) || (distFloor <= distSet))) {
        // Floor hit
        if (config->enableShadow){
	        hitPoint = eyeRayOrig + eyeRayDir * (float4) distFloor;
	        n = hitPoint - WORLD_CENTER;
	        n = normalize(n);
	        // The most important feature in a ray tracer: a checker texture !
	        const int ix = (hitPoint.x > 0.f) ? hitPoint.x : (1.f - hitPoint.x);
	        const int iz = (hitPoint.z > 0.f) ? hitPoint.z : (1.f - hitPoint.z);
	        if ((ix + iz) % 2)
	            diffuse = (float4) (0.75f, 0.75f, 0.75f, 0.f);
	        else
	            diffuse = (float4) (87/255.0f,90/255.0f,93/255.0f,0);
	        doShade = 1;
	        useAO = 0;
        } else {
        	color = (float4)  (config->color[0],config->color[1],config->color[2],config->color[3]);
        }
    }
    //--------------------------------------------------------------------------
    // Select the shadow pass
    //--------------------------------------------------------------------------

    if (doShade) {
        float shadowFactor = 1.f;
        
        if (config->enableShadow) {
            float4 L = normalize(light - hitPoint);
            float4 rO = hitPoint + n * 1e-2f;
            float4 shadowHitPoint;

            // Check bounding sphere
            float shadowDistSet = IntersectBoundingSphere(rO, L);
            if (shadowDistSet >= 0.f) {
                uint steps;

                rO = rO + L * (float4) shadowDistSet;
                shadowDistSet = IntersectVolume(M,volume,rO, L,  maxIterations, epsilon,
                        &shadowHitPoint, &hitNormal, &steps);
                if (shadowDistSet < epsilon) {
                    if (useAO) {
                        // Use steps count to simulate ambient occlusion
                        shadowFactor = 0.6f - min(steps / 255.f, 0.5f);
                    } else
                        shadowFactor = 0.6f;
                }
            } else
                shadowDistSet = -1.f;
        }
		
        //--------------------------------------------------------------------------
        // Direct lighting of hit point
        //--------------------------------------------------------------------------

        color = Phong(light, eyeRayOrig, hitPoint, n, diffuse,hitClipPlane) * shadowFactor;
    }

    int offset = 3 * (x + y * width);
    color = clamp(color, (float4) (0.f, 0.f, 0.f, 0.f), (float4) (1.f, 1.f, 1.f, 0.f));
    if (enableAccumulation) {
        pixels[offset++] += color.s0;
        pixels[offset++] += color.s1;
        pixels[offset  ] += color.s2;
    } else {
        pixels[offset++] = color.s0;
        pixels[offset++] = color.s1;
        pixels[offset  ] = color.s2;
    }
    
}
#endif
kernel void copyLevelSet(
	global float4* volumeColorBuffer,
	global float* srcImage){
	int i,j,k;
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	getRowColSlice(id,&i,&j,&k);
	
	float v111 = srcImage[id];
	float v211 = getImageValue(srcImage, i + 1, j, k);
	float v121 = getImageValue(srcImage, i, j + 1, k);
	float v101 = getImageValue(srcImage, i, j - 1, k);
	float v011 = getImageValue(srcImage, i - 1, j, k);
	float v110 = getImageValue(srcImage, i, j, k - 1);
	float v112 = getImageValue(srcImage, i, j, k + 1);

	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w=1E-6f;
	grad=normalize(grad);
	grad.w=clamp(v111,-10.0f,10.0f);	
	if(i==ROWS-1||j==COLS-1||k==SLICES-1||i==0||j==0||k==0)grad.w=max(grad.w,1.5f);
	volumeColorBuffer[id]=grad;
}
kernel void copyNarrowBandToImage(
	global float4* volumeColorBuffer,
	global int* activeList,
	global float4* colorListBuffer,
	int activeListSize){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	volumeColorBuffer[id]=colorListBuffer[gid];
}
float4 tukey(float4 v1,float4 v2,float sigma){
	float sqr=(0.2f/(sigma*sigma));
	float4 v=v1-v2;
	if(dot(v1,v2)>sigma){
		return (float4)(
			0.5f*(1-v.x*v.x*sqr)*(1-v.x*v.x*sqr),
			0.5f*(1-v.y*v.y*sqr)*(1-v.y*v.y*sqr),
			0.5f*(1-v.z*v.z*sqr)*(1-v.z*v.z*sqr),
			0);
	} else {
		return (float4)(0,0,0,0);
	}
}
kernel void clearNarrowBandToImage(
	global float4* volumeColorBuffer,
	global int* activeList,
	int activeListSize){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	volumeColorBuffer[id]=(float4)(0,0,0,4.0f);
}
kernel void setDistance(
	global float4* volumeColorBuffer,const global float* distBuffer,float dist){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	volumeColorBuffer[id].w=((distBuffer[id]>=0)?1:-1)*dist;
}
kernel void bilateralFilterVolume(global float4* buffIn,float edgeSigma,float lambda){
	int i,j,k;
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	getRowColSlice(id,&i,&j,&k);
	float4 v111 = getImageValue4(buffIn,i, j, k);
	float4 v211 = getImageValue4(buffIn,i + 1, j, k);
	float4 v121 = getImageValue4(buffIn,i, j + 1, k);
	float4 v101 = getImageValue4(buffIn,i, j - 1, k);
	float4 v011 = getImageValue4(buffIn,i - 1, j, k);
	float4 v110 = getImageValue4(buffIn,i, j, k - 1);
	float4 v112 = getImageValue4(buffIn,i, j, k + 1);		
	float4 norm=v111-0.16666f*lambda*(
		tukey(v111,v011,edgeSigma)*(v111-v011)+
		tukey(v111,v121,edgeSigma)*(v111-v121)+
		tukey(v111,v101,edgeSigma)*(v111-v101)+
		tukey(v111,v211,edgeSigma)*(v111-v211)+
		tukey(v111,v110,edgeSigma)*(v111-v110)+
		tukey(v111,v112,edgeSigma)*(v111-v112));
	buffIn[id]=norm;
}
kernel void bilateralFilter(global int* activeList,global float4* buffIn,float edgeSigma,float lambda,int activeListSize,int xOff,int yOff,int zOff){
	int i,j,k;
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	getRowColSlice(id,&i,&j,&k);
	if(i%2!=xOff||j%2!=yOff||k%2!=zOff)return;	
	
	float4 v111 = getImageValue4(buffIn,i, j, k);
	float4 v211 = getImageValue4(buffIn,i + 1, j, k);
	float4 v121 = getImageValue4(buffIn,i, j + 1, k);
	float4 v101 = getImageValue4(buffIn,i, j - 1, k);
	float4 v011 = getImageValue4(buffIn,i - 1, j, k);
	float4 v110 = getImageValue4(buffIn,i, j, k - 1);
	float4 v112 = getImageValue4(buffIn,i, j, k + 1);		
	float4 norm=v111-0.16666f*lambda*(
		tukey(v111,v011,edgeSigma)*(v111-v011)+
		tukey(v111,v121,edgeSigma)*(v111-v121)+
		tukey(v111,v101,edgeSigma)*(v111-v101)+
		tukey(v111,v211,edgeSigma)*(v111-v211)+
		tukey(v111,v110,edgeSigma)*(v111-v110)+
		tukey(v111,v112,edgeSigma)*(v111-v112));
	buffIn[id]=norm;
}
kernel void reduceNormals(
		__global int* activeList,
		__global int* spatialLookup,
		__global Springl3D* capsules,
		global float4* destImage,
		uint activeListSize){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int i,j,k;
	float4 pt;
	float4 ret;
	Springl3D cap;
	getRowColSlice(id,&i,&j,&k);
	spatialLookup+=gid*MAX_BIN_SIZE;
	float4 normal=(float4)(0,0,0,0);	
	int index;
	float4 currentNorm=destImage[id];
	float value=currentNorm.w;
	currentNorm.w=0;
	float4 npt=SCALE_DOWN*(float4)(i,j,k,0);
	float minDist=1E10f;
	for(index=1;index<MAX_BIN_SIZE;index++){
		int binId=spatialLookup[index];
		if(binId<0)break;
		cap = capsules[binId];
		float4 v1=cap.vertexes[1]-cap.vertexes[0];
		float4 v2=cap.vertexes[2]-cap.vertexes[0];
		v1.w=0;
		v2.w=0;
		float4 norm=cross(v1,v2);
		float d=distance(cap.particle,npt);
		if(d<minDist){
			normal=norm;
			minDist=d;
		}	
		normal+=norm;
	}
	if(minDist<100){
		normal=normalize(normal);
		if(dot(normal,currentNorm)<0.94f){
			return;
		}
		normal.w=value;
		destImage[id]=normal;
	}
}

__kernel void isoSurfCount(
	const global float* signedLevelSet,
	const global int* activeList,
	global int* counts,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int activeListSize) {


	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*signedLevelSet[getSafeIndex(v.x, v.y, v.z)] <= 0)iFlagIndex |= 1 << iVertex;

	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		counts[gid]=0;
		return;
	}
	EdgeSplit split;

	float4 pt3d;
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge
		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffset(signedLevelSet,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
			split.pt3d = pt3d;
		}
	}
	int count=0;
	// Generate list of triangles
	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		count+=3;
	}
	counts[gid]=count;
}

__kernel void isoSurfGen(
	global float4* vertexes,
	global float* normals,
	const global float* signedLevelSet,
	const global int* activeList,
	global int* offsets,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int activeListSize){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	int offset=(gid>0)?offsets[gid-1]:0;
	vertexes+=offset;
	normals+=3*offset;
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*signedLevelSet[getSafeIndex(v.x, v.y, v.z)] <= 0)iFlagIndex |= 1 << iVertex;

	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		return;
	}
	EdgeSplit split;

	float4 pt3d;
	EdgeSplit asEdgeVertex[12];
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge

		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffset(signedLevelSet,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
				pt3d.w=1;
			split.pt3d = pt3d;
			asEdgeVertex[iEdge] = split;
		}
	}
	// Generate list of triangles

	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		if(sign<0){
			for (int iCorner = 0; iCorner < 3; iCorner++) {
				iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
				split = asEdgeVertex[iVertex];
				vertexes[2-iCorner]=split.pt3d;
			}
		} else {
			for (int iCorner = 0; iCorner < 3; iCorner++) {
				iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
				split = asEdgeVertex[iVertex];
				vertexes[iCorner]=split.pt3d;
			}		
		}
		for(int i=0;i<3;i++){
			float4 norm=normalize(getGradientValue(signedLevelSet,vertexes[i]));
			normals[0]=norm.x;
			normals[1]=norm.y;
			normals[2]=norm.z;
			normals+=3;
		}				
		vertexes+=3;		
	}
}
__kernel void isoSurfGenSmooth(
	global float4* vertexes,
	global float* normals,
	const global float* signedLevelSet,
	const global int* activeList,
	global int* offsets,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int activeListSize){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	int offset=(gid>0)?offsets[gid-1]:0;
	vertexes+=offset;
	normals+=3*offset;
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*signedLevelSet[getSafeIndex(v.x, v.y, v.z)] <= 0)iFlagIndex |= 1 << iVertex;

	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		return;
	}
	EdgeSplit split;

	float4 pt3d;
	EdgeSplit asEdgeVertex[12];
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge

		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffset(signedLevelSet,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
				pt3d.w=1;
			split.pt3d = pt3d;
			asEdgeVertex[iEdge] = split;
		}
	}
	// Generate list of triangles

	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		if(sign<0){
			for (int iCorner = 0; iCorner < 3; iCorner++) {
				iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
				split = asEdgeVertex[iVertex];
				vertexes[2-iCorner]=split.pt3d;
			}
		} else {
			for (int iCorner = 0; iCorner < 3; iCorner++) {
				iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
				split = asEdgeVertex[iVertex];
				vertexes[iCorner]=split.pt3d;
			}		
		}
		const float OP=1;
		const float ON=-1;
		for(int i=0;i<3;i++){
			float4 vt=vertexes[i];
			float4 n1=getGradientValue(signedLevelSet,vt+(float4)(OP,OP,OP,0.0f));
			float4 n2=getGradientValue(signedLevelSet,vt+(float4)(OP,OP,ON,0.0f));
			float4 n3=getGradientValue(signedLevelSet,vt+(float4)(OP,ON,OP,0.0f));
			float4 n4=getGradientValue(signedLevelSet,vt+(float4)(OP,ON,ON,0.0f));
			float4 n5=getGradientValue(signedLevelSet,vt+(float4)(OP,OP,OP,0.0f));
			float4 n6=getGradientValue(signedLevelSet,vt+(float4)(OP,OP,ON,0.0f));
			float4 n7=getGradientValue(signedLevelSet,vt+(float4)(OP,ON,OP,0.0f));
			float4 n8=getGradientValue(signedLevelSet,vt+(float4)(OP,ON,ON,0.0f));
			
			float4 norm=normalize(n1+n2+n3+n4+n5+n6+n7+n8);
			normals[0]=norm.x;
			normals[1]=norm.y;
			normals[2]=norm.z;
			normals+=3;
		}				
		vertexes+=3;		
	}
}