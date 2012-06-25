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
 #pragma OPENCL_EXTENSION cl_khr_local_int32_base_atomics : enable
 #pragma OPENCL_EXTENSION cl_khr_local_int32_extended_atomics : enable

#define STATIC_SPRINGL (-1E8f)
typedef struct{
	float4 particle;
    float4 vertexes[3];
} Springl3D;
typedef struct{
	int4 e1;
	int4 e2;
	float4 pt3d;
	int vid;
} EdgeSplit;
//Store capsule id and vertex id [0,1,2]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor3D;
inline int clampRow(int row){
	return clamp((int)row,(int)0,(int)(ROWS-1));
}
inline int clampColumn(int col){
	return clamp((int)col,(int)0,(int)(COLS-1));
}
inline int clampSlice(int slice){
	return clamp((int)slice,(int)0,(int)(SLICES-1));
}
inline uint getIndex(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
}
//Get index into volume
inline uint getHashValue(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
}
inline uint getSafeIndex(int i, int j, int k) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	return (s * (ROWS * COLS)) + (c * ROWS) + r;
}
inline uint getVectorIndex(int i, int j, int k,int l) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	return 3*((s * (ROWS * COLS)) + (c * ROWS) + r)+l;
}
inline void getRowColSlice(uint index,int* i, int* j, int* k) {
	(*k)=index/(ROWS*COLS);
	int ij=index-(*k)*(ROWS * COLS);
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}
inline void getRowColSlice2(uint index,int* i, int* j, int* k) {
	(*k)=4*index/(ROWS*COLS);
	int ij=index-(*k)*(ROWS * COLS/4);
	(*j)=2*ij/ROWS;
	(*i)=ij-(*j)*ROWS/2;
}
inline float getLevelSetValue(global float* image,global int* labels,int label,int i,int j,int k){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	int ii=getIndex(r,c,s);
	if(labels[ii]==label){
		return -image[ii];
	} else {
		return image[ii];
	}
}
typedef struct {
	float m00, m01, m02, m03,
		  m10, m11, m12, m13,
		  m20, m21, m22, m23,
		  m30, m31, m32, m33;
} Matrix4f;
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
inline float getImageValue(global float* image,int i,int j,int k){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	return image[getIndex(r,c,s)];
}
inline float getLabelValue(global int* image,int i,int j,int k){
	return image[getSafeIndex(i,j,k)];
}
inline float4 getVectorImageValue(__global float4* image,uint i,uint j,uint k){
	uint r = clamp(i,  (uint)0, (uint)(ROWS - 1));
	uint c = clamp(j, (uint)0, (uint)(COLS - 1));
	uint s = clamp(k, (uint)0, (uint)(SLICES - 1));
	return image[getIndex(r,c,s)];
}
inline float getNonBackgroundLevelSetValue(global float* image,global int* labels,global float4* colors,int i,int j,int k){
	uint ii=getSafeIndex(i,j,k);
	int label=labels[ii];
	if(label!=0&&colors[label-1].w==0){
		return 1.0f;
	}
	if(label<=0){
		return image[ii];
	} else {
		return max(-1.0f,-image[ii]);
	}
}
inline float interpolate(__global float* data,float x,float y,float z){
	int y0, x0, z0, y1, x1, z1;
	float dx, dy, dz, hx, hy, hz;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1) || z < 0
			|| z > (SLICES - 1)) {
			int r = max(min((int)x, ROWS - 1), 0);
			int c = max(min((int)y, COLS - 1), 0);
			int s = max(min((int)z, SLICES - 1), 0);
			return data[getIndex(r,c,s)];
	} else {
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
		return (((data[getIndex(x0,y0,z0)] * hx + data[getIndex(x1,y0,z0)] * dx) * hy + (data[getIndex(x0,y1,z0)]
				* hx + data[getIndex(x1,y1,z0)] * dx)
				* dy)
				* hz + ((data[getIndex(x0,y0,z1)] * hx + data[getIndex(x1,y0,z1)] * dx)
				* hy + (data[getIndex(x0,y1,z1)] * hx + data[getIndex(x1,y1,z1)] * dx) * dy)
				* dz);
	}
}
inline float interpolateLevelSet(__global float* levelset,global int* imageLabels,int label,float x,float y,float z){
	int y0, x0, z0, y1, x1, z1;
	float dx, dy, dz, hx, hy, hz;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1) || z < 0
			|| z > (SLICES - 1)) {
			int r = max(min((int)x, ROWS - 1), 0);
			int c = max(min((int)y, COLS - 1), 0);
			int s = max(min((int)z, SLICES - 1), 0);
			return getLevelSetValue(levelset,imageLabels,label,r,c,s);
	} else {
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
		return (((getLevelSetValue(levelset,imageLabels,label,x0,y0,z0) * hx + getLevelSetValue(levelset,imageLabels,label,x1,y0,z0) * dx) * hy + (getLevelSetValue(levelset,imageLabels,label,x0,y1,z0)
				* hx + getLevelSetValue(levelset,imageLabels,label,x1,y1,z0) * dx)
				* dy)
				* hz + ((getLevelSetValue(levelset,imageLabels,label,x0,y0,z1) * hx + getLevelSetValue(levelset,imageLabels,label,x1,y0,z1) * dx)
				* hy + (getLevelSetValue(levelset,imageLabels,label,x0,y1,z1) * hx + getLevelSetValue(levelset,imageLabels,label,x1,y1,z1) * dx) * dy)
				* dz);
	}
}
inline float4 getGradientValue(__global float* image,float i,float j,float k){
	float v211 = interpolate(image, i + 1, j, k);
	float v121 = interpolate(image, i, j + 1, k);
	float v101 = interpolate(image, i, j - 1, k);
	float v011 = interpolate(image, i - 1, j, k);
	float v110 = interpolate(image, i, j, k - 1);
	float v112 = interpolate(image, i, j, k + 1);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w = 0;
	return grad;
}
inline float4 getNormalValue(__global float* image,global int* labels,int label,float i,float j,float k){
	float v111 = interpolateLevelSet(image,labels,label, i, j, k);
	float v211 = interpolateLevelSet(image,labels,label, i + 1, j, k);
	float v121 = interpolateLevelSet(image,labels,label, i, j + 1, k);
	float v101 = interpolateLevelSet(image,labels,label, i, j - 1, k);
	float v011 = interpolateLevelSet(image,labels,label, i - 1, j, k);
	float v110 = interpolateLevelSet(image,labels,label, i, j, k - 1);
	float v112 = interpolateLevelSet(image,labels,label, i, j, k + 1);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w=1E-6f;
	grad=normalize(grad);
	return grad;
}
inline float interpolateVectorField(__global float* data,float x,float y,float z,int l){
	int y0, x0, z0, y1, x1, z1;
	float dx, dy, dz, hx, hy, hz;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1) || z < 0
			|| z > (SLICES - 1)) {
			int r = max((int)min((int)x, ROWS - 1), 0);
			int c = max((int)min((int)y, COLS - 1), 0);
			int s = max((int)min((int)z, SLICES - 1), 0);
			return data[getVectorIndex(r,c,s,l)];
	} else {
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
		return (((data[getVectorIndex(x0,y0,z0,l)] * hx + data[getVectorIndex(x1,y0,z0,l)] * dx) * hy + (data[getVectorIndex(x0,y1,z0,l)]
				* hx + data[getVectorIndex(x1,y1,z0,l)] * dx)
				* dy)
				* hz + ((data[getVectorIndex(x0,y0,z1,l)] * hx + data[getVectorIndex(x1,y0,z1,l)] * dx)
				* hy + (data[getVectorIndex(x0,y1,z1,l)] * hx + data[getVectorIndex(x1,y1,z1,l)] * dx) * dy)
				* dz);
	}
}
inline float getNudgedValueMogac(__global float* unsignedLevelSet,__global int* labels,int label,int x,int y,int z){
	float val=getLevelSetValue(unsignedLevelSet,labels,label,x,y,z);
	if (val < 0) {
		val = min(val, - 0.1f);
	} else {
		val = max(val,  0.1f);
	}
	return val;
}
inline float fGetOffsetMogac(__global float* unsignedLevelSet,__global int* labels,int label,int4 v1, int4 v2) {
		float fValue1 = getNudgedValueMogac(unsignedLevelSet,labels,label,v1.x, v1.y,v1.z);
		float fValue2 = getNudgedValueMogac(unsignedLevelSet,labels,label,v2.x, v2.y,v2.z);
		float fDelta = fValue2 - fValue1;
		if (fabs(fDelta) ==0) {
			return 0.5f;
		}
		return clamp(-fValue1 / fDelta,0.0f,1.0f);
}
//Distance squared between two points
inline float distanceSquaredPoint(float4 pt1,float4 pt2){
	float4 d=(pt1-pt2);
	return (d.x*d.x+d.y*d.y+d.z*d.z);
}
//Implementation from geometric tools (http://www.geometrictools.com)
inline float4 parametricTriangle(float4 e0, float4 e1,float s, float t, float4 B) {
		float4 Bsum=B+s*e0+t*e1;
		return Bsum;
}

//Squared distance between point and triangle
//Implementation from geometric tools (http://www.geometrictools.com)
float distanceSquaredTriangle(float4 p, float4 v0,float4 v1,float4 v2,float4* closestPoint) {
		float distanceSquared = 0;
		int region_id = 0;
		v0.w=0;
		v1.w=0;
		v2.w=0;
		
		float4 P = p;
		float4 B = v0;
		float4 e0=v1-v0;
		float4 e1=v2-v0;
		float a = dot(e0,e0);
		float b = dot(e0,e1);
		float c = dot(e1,e1);
		float4 dv=B-P;
		float d = dot(e0,dv);
		float e = dot(e1,dv);
		float f = dot(dv,dv);
		// Determine which region_id contains s, t

		float det = a * c - b * b;
		float s = b * e - c * d;
		float t = b * d - a * e;

		if (s + t <= det) {
			if (s < 0) {
				if (t < 0) {
					region_id = 4;
				} else {
					region_id = 3;
				}
			} else if (t < 0) {
				region_id = 5;
			} else {
				region_id = 0;
			}
		} else {
			if (s < 0) {
				region_id = 2;
			} else if (t < 0) {
				region_id = 6;
			} else {
				region_id = 1;
			}
		}

		// Parametric Triangle Point
		float4 T = (float4)(0,0,0,0);

		if (region_id == 0) {// Region 0
			float invDet = (float) 1 / (float) det;
			s *= invDet;
			t *= invDet;

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			float4 tmp=P-T;
			distanceSquared =  tmp.x*tmp.x+tmp.y*tmp.y+tmp.z*tmp.z;

		} else if (region_id == 1) {// Region 1
			float numer = c + e - b - d;

			if (numer < +0) {
				s = 0;
			} else {
				float denom = a - 2 * b + c;
				s = (numer >= denom ? 1 : numer / denom);
			}
			t = 1 - s;

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			float4 tmp=P-T;
			distanceSquared = tmp.x*tmp.x+tmp.y*tmp.y+tmp.z*tmp.z;

		} else if (region_id == 2) {// Region 2
			float tmp0 = b + d;
			float tmp1 = c + e;

			if (tmp1 > tmp0) {
				float numer = tmp1 - tmp0;
				float denom = a - 2 * b + c;
				s = (numer >= denom ? 1 : numer / denom);
				t = 1 - s;
			} else {
				s = 0;
				t = (tmp1 <= 0 ? 1 : (e >= 0 ? 0 : -e / c));
			}

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			float4 tmp=P-T;
			distanceSquared =  tmp.x*tmp.x+tmp.y*tmp.y+tmp.z*tmp.z;

		} else if (region_id == 3) {// Region 3
			s = 0;
			t = (e >= 0 ? 0 : (-e >= c ? 1 : -e / c));

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			float4 tmp=P-T;
			distanceSquared =  tmp.x*tmp.x+tmp.y*tmp.y+tmp.z*tmp.z;

		} else if (region_id == 4) {// Region 4
			float tmp0 = c + e;
			float tmp1 = a + d;

			if (tmp0 > tmp1) {
				s = 0;
				t = (tmp1 <= 0 ? 1 : (e >= 0 ? 0 : -e / c));
			} else {
				t = 0;
				s = (tmp1 <= 0 ? 1 : (d >= 0 ? 0 : -d / a));
			}

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			float4 tmp=P-T;
			distanceSquared =  tmp.x*tmp.x+tmp.y*tmp.y+tmp.z*tmp.z;

		} else if (region_id == 5) {// Region 5
			t = 0;
			s = (d >= 0 ? 0 : (-d >= a ? 1 : -d / a));

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			float4 tmp=P-T;
			distanceSquared =  tmp.x*tmp.x+tmp.y*tmp.y+tmp.z*tmp.z;

		} else {// Region 6
			float tmp0 = b + e;
			float tmp1 = a + d;

			if (tmp1 > tmp0) {
				float numer = tmp1 - tmp0;
				float denom = c - 2 * b + a;
				t = (numer >= denom ? 1 : numer / denom);
				s = 1 - t;
			} else {
				t = 0;
				s = (tmp1 <= 0 ? 1 : (d >= 0 ? 0 : -d / a));
			}

			// Find point on parametric triangle based on s and t
			T = parametricTriangle(e0, e1, s, t, B);
			// Find distance from P to T
			float4 tmp=P-T;
			distanceSquared =  tmp.x*tmp.x+tmp.y*tmp.y+tmp.z*tmp.z;

		}
		(*closestPoint)=T;
		return distanceSquared;
}
float distanceSquared(float4 p, Springl3D* capsule,float4* closestPoint) {
	return distanceSquaredTriangle(p,capsule->vertexes[0],capsule->vertexes[1],capsule->vertexes[2],closestPoint);
}
//Distance between point and triangle edge
//Implementation from geometric tools (http://www.geometrictools.com)
float edgeDistanceSquared(float4 pt, float4 pt1, float4 pt2,float4* lastClosestSegmentPoint) {
	pt.w=0;
	pt1.w=0;
	pt2.w=0;
	float4 dir=pt2-pt1;
	float len=length(dir);
	dir=normalize(dir);
	float4 diff=pt-pt1;
	float mSegmentParameter = dot(dir,diff);
	if (0 < mSegmentParameter) {
		if (mSegmentParameter < len) {
			*lastClosestSegmentPoint=dir*mSegmentParameter+pt1;
		} else {
			*lastClosestSegmentPoint=pt2;
		}
	} else {
		*lastClosestSegmentPoint=pt1;
	}
	return distanceSquaredPoint(pt,*lastClosestSegmentPoint);
}
__kernel void mapNearestNeighborsMogac(
	__global int* nbrs,
	__global int* indexMap,
	__global int* spatialLookup,
	__global Springl3D* capsules,
	__global int* labels,
	int elements) {
	//Triangle index
	int gid=get_global_id(0);
	int id=gid/3;
	if(id>=elements)return;
	//Triangle vertex index without modulus!
	int n=gid-id*3;
	Springl3D cap = capsules[id];
	float4 pt = cap.vertexes[n];
	pt.w=0;
	nbrs+=MAX_NEAREST_BINS*gid;
	
	float4 lowerPoint=pt-MAX_RADIUS;
	float4 upperPoint=pt+MAX_RADIUS;
	
	__const float4 ZERO=(float4)(0,0,0,0);
	__const float4 IMAGE_MAX=(float4)(ROWS-1,COLS-1,SLICES-1,0);
	
	lowerPoint*=SCALE_UP;
	float4 lower=max(ZERO,floor(lowerPoint));
	
	int lowerRow=(int)lower.x;
	int lowerCol=(int)lower.y;
	int lowerSlice=(int)lower.z;

	upperPoint*=SCALE_UP;
	float4 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
	
	int upperRow=(int)upper.x;
	int upperCol=(int)upper.y;
	int upperSlice=(int)upper.z;
	
	int offset=0;
	int cid,index,startIndex;
	uint hashValue;
	int label=abs(labels[id]);
	//Enumerate all triangles that lie in bounding sphere around point
	for (int k = lowerSlice; k < upperSlice; k++) {
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
				hashValue = getSafeIndex(i, j, k);
				startIndex = MAX_BIN_SIZE*indexMap[hashValue];
				if(startIndex<0)continue;
				for(int index=1;index<MAX_BIN_SIZE;index++){
					cid=spatialLookup[startIndex+index];
					if(cid<0)break;
					if (cid!= id&&abs(labels[cid])==label) { //Ignore the current triangle
						nbrs[offset++]=cid;
						if(offset>=MAX_NEAREST_BINS){
							return;
						}
					}
				}
			}
		}
	}
	while(offset<MAX_NEAREST_BINS){
		nbrs[offset++]=MAX_VALUE;	
	}	
}
__kernel void fillGapCountMogac(
	const global float* signedLevelSet,
	global int* labels,
	const global float* unsignedLevelSet,
	const global int* activeList,
	global int* counts,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int activeListSize,
	int label) {
	
	__const int a2fVertexOffset[8][3] = { { 0, 0, 0 },
			{ 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
			{ 1, 1, 1 }, { 0, 1, 1 } };
	__const int a2iEdgeConnection[12][2] = { { 0, 1 }, { 1, 2 },
			{ 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
	__const float a2fEdgeDirection[12][3] = { { 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f },
			{ 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f },
			{ 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f } };

	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	EdgeSplit asEdgeVertex[12];
	float4 centroid;
	float4 pt3d;
	float levelSetValue;
	int count=0;
	EdgeSplit split;
	iFlagIndex = 0;
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*getLevelSetValue(signedLevelSet,labels,label,v.x, v.y, v.z) <= 0)iFlagIndex |= 1 << iVertex;
	
	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];
	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		counts[gid]=0;
		return;
	}
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge
		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			if (getLevelSetValue(signedLevelSet,labels,label,v.x, v.y, v.z) <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffsetMogac(signedLevelSet,labels,label,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
			split.pt3d = pt3d;
			asEdgeVertex[iEdge] = split;
		}
	}
	
	// Generate list of triangles
	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		centroid=(float4)(0,0,0,0);
		for (int iCorner = 0; iCorner < 3; iCorner++) {
			iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
			split = asEdgeVertex[iVertex];
			centroid+=split.pt3d;
		}
		centroid*=0.33333f;			
		levelSetValue=interpolate(unsignedLevelSet,centroid.x,centroid.y,centroid.z);
		if(levelSetValue>vExtent){
			count++;
		}
	}
	counts[gid]=count;
}
__kernel void expandGapsMogac(
	global Springl3D* capsules,
	global int* labels,
	const global float* signedLevelSet,
	const global int* imageLabels,
	const global float* unsignedLevelSet,
	const global int* activeList,
	global int* offsets,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int elements,
	int activeListSize,
	int label){
	__const int a2fVertexOffset[8][3] = { { 0, 0, 0 },
			{ 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
			{ 1, 1, 1 }, { 0, 1, 1 } };
	__const int a2iEdgeConnection[12][2] = { { 0, 1 }, { 1, 2 },
			{ 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
	__const float a2fEdgeDirection[12][3] = { { 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f },
			{ 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f },
			{ 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f } };

	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);		
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	float4 centroid;
	float4 pt3d;
	float levelSetValue;
	int count=0;
	EdgeSplit split;
	float4 ray[3];
	float4 pts[3];
	float len;
	EdgeSplit asEdgeVertex[12];
	int offset=(gid>0)?offsets[gid-1]:0;
	capsules+=elements+offset;
	labels+=elements+offset;
	float minLength = 1E10f;
	Springl3D cap;
	iFlagIndex = 0;
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*getLevelSetValue(signedLevelSet,imageLabels,label,v.x, v.y, v.z) <= 0)iFlagIndex |= 1 << iVertex;
	
	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];
	
	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		return;
	}

	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge
	
		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (getLevelSetValue(signedLevelSet,imageLabels,label,v.x, v.y, v.z) <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffsetMogac(signedLevelSet,imageLabels,label,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
			split.pt3d = pt3d;
			asEdgeVertex[iEdge] = split;
		}
	}


	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		centroid=(float4)(0,0,0,0);
		for (int iCorner = 0; iCorner < 3; iCorner++) {
			iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
			split = asEdgeVertex[iVertex];
			centroid+=pts[iCorner]=split.pt3d;
		}
		centroid*=0.33333f;
		levelSetValue=interpolate(unsignedLevelSet,centroid.x,centroid.y,centroid.z);
		if(levelSetValue>vExtent){			
			minLength = 1E10f;
			for (int i = 0; i < 3; i++) {
				ray[i]=pts[i]-centroid;
				len = max(1E-3f,length(ray[i]));
				if (len < 2 * REST_LENGTH) {
					ray[i]*=(2 * REST_LENGTH / len);
				}
				minLength =min(len, minLength);
			}
			if (minLength < 2 * REST_LENGTH) {
				for (int i = 0; i < 3; i++) {
					pts[i] = centroid;
					pts[i]+=ray[i];
				}
			}			
			if(sign>0){
				cap.vertexes[0]=SCALE_DOWN*pts[0];
				cap.vertexes[1]=SCALE_DOWN*pts[1];
				cap.vertexes[2]=SCALE_DOWN*pts[2];
			} else {
				cap.vertexes[0]=SCALE_DOWN*pts[2];
				cap.vertexes[1]=SCALE_DOWN*pts[1];
				cap.vertexes[2]=SCALE_DOWN*pts[0];
			}
			cap.particle=centroid*SCALE_DOWN;
			capsules[count]=cap;
			labels[count]=-label;
			count++;
		}		
	}
}
__kernel void computeAdvectionForcesPandVMogac(
	__global Springl3D* capsules,
	__global float* pressureImage,
	__global float* advectionImage,
	global int* labels,
	global int* imageLabels,
	__global float* forceUpdates,
	float pressureWeight,float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=3*id;
	capsules+=id;

	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;

	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));		
	
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float z = pt.z * SCALE_UP;
	
	int nbrs[8];
	int i=floor(x);
	int j=floor(y);
	int k=floor(z);
	nbrs[0]=getLabelValue(imageLabels,i,j,k);
	nbrs[1]=getLabelValue(imageLabels,i,j+1,k);
	nbrs[2]=getLabelValue(imageLabels,i+1,j+1,k);
	nbrs[3]=getLabelValue(imageLabels,i+1,j,k);
	nbrs[4]=getLabelValue(imageLabels,i,j,k+1);
	nbrs[5]=getLabelValue(imageLabels,i,j+1,k+1);
	nbrs[6]=getLabelValue(imageLabels,i+1,j+1,k+1);
	nbrs[7]=getLabelValue(imageLabels,i+1,j,k+1);
		
	float vx=interpolateVectorField(advectionImage,x,y,z, 0);
	float vy=interpolateVectorField(advectionImage,x,y,z, 1);
	float vz=interpolateVectorField(advectionImage,x,y,z, 2);
	int label=abs(labels[id]);
	for(int k=0;k<8;k++){
		if(nbrs[k]!=0&&label!=nbrs[k]){
			forceUpdates[0]=vx*advectionWeight;
			forceUpdates[1]=vy*advectionWeight;
			forceUpdates[2]=vz*advectionWeight;
			return;
		}
	}
	
	float pressure = interpolate(pressureImage,x,y,z);
	float scale = pressure * pressureWeight;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	norm.z *= (scale * SCALE_DOWN);
	norm.w = 0;


	forceUpdates[0]=norm.x+vx*advectionWeight;
	forceUpdates[1]=norm.y+vy*advectionWeight;
	forceUpdates[2]=norm.z+vz*advectionWeight;
}

__kernel void computeAdvectionForcesNoResamplePandVMogac(
	__global Springl3D* capsules,
	__global float* pressureImage,
	__global float* advectionImage,
	global int* labels,
	global float* distanceField,
	global int* imageLabels,
	__global float* forceUpdates,
	float pressureWeight,
	float advectionWeight,
	uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	capsules+=id;	
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	int nbrs[8];
	float4 norm;
	float scale=1;
	bool foundNeighbor;
	float pressure;
	float4 q;
	//Could not find neighbor for springl. Assume springl is at boundary with background.
	int label=abs(labels[id]);
	for(int n=0;n<3;n++){
		q=SCALE_UP*capsules->vertexes[n];
		pressure = interpolate(pressureImage,q.x,q.y,q.z);
		int i=floor(q.x);
		int j=floor(q.y);
		int k=floor(q.z);
		nbrs[0]=getLabelValue(imageLabels,i,j,k);
		nbrs[1]=getLabelValue(imageLabels,i,j+1,k);
		nbrs[2]=getLabelValue(imageLabels,i+1,j+1,k);
		nbrs[3]=getLabelValue(imageLabels,i+1,j,k);
		nbrs[4]=getLabelValue(imageLabels,i,j,k+1);
		nbrs[5]=getLabelValue(imageLabels,i,j+1,k+1);
		nbrs[6]=getLabelValue(imageLabels,i+1,j+1,k+1);
		nbrs[7]=getLabelValue(imageLabels,i+1,j,k+1);
		foundNeighbor=false;
		for(int nn=0;nn<8;nn++){
			int nbrLabel=abs(nbrs[nn]);
			if(nbrLabel!=label){
				foundNeighbor=true;
			}
		}
		float vx=interpolateVectorField(advectionImage,q.x,q.y,q.z, 0);
		float vy=interpolateVectorField(advectionImage,q.x,q.y,q.z, 1);
		float vz=interpolateVectorField(advectionImage,q.x,q.y,q.z, 2);
		scale =(foundNeighbor)?pressure * pressureWeight:0;
		norm=scale*getNormalValue(distanceField,imageLabels,label,q.x,q.y,q.z);	
		forceUpdates[0]=norm.x+vx*advectionWeight;
		forceUpdates[1]=norm.y+vy*advectionWeight;
		forceUpdates[2]=norm.z+vz*advectionWeight;
		forceUpdates+=3;
	}
}
__kernel void computeAdvectionForcesACWE(__global Springl3D* capsules,__global float* pressureImage,global int* labels,global int* imageLabels,__global float* forceUpdates,global float* averages,float pressureWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=3*id;
	capsules+=id;
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	
	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));		
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float z = pt.z * SCALE_UP;
	
	int nbrs[8];
	int i=floor(x);
	int j=floor(y);
	int k=floor(z);
	nbrs[0]=getLabelValue(imageLabels,i,j,k);
	nbrs[1]=getLabelValue(imageLabels,i,j+1,k);
	nbrs[2]=getLabelValue(imageLabels,i+1,j+1,k);
	nbrs[3]=getLabelValue(imageLabels,i+1,j,k);
	nbrs[4]=getLabelValue(imageLabels,i,j,k+1);
	nbrs[5]=getLabelValue(imageLabels,i,j+1,k+1);
	nbrs[6]=getLabelValue(imageLabels,i+1,j+1,k+1);
	nbrs[7]=getLabelValue(imageLabels,i+1,j,k+1);
	
	float pressure = interpolate(pressureImage,x,y,z);
	int label=abs(labels[id]);
	float currentPressure=(label>0)?fabs(pressure-averages[label]):0;	
	float altPressure=0;
	bool foundNeighbor=false;
	for(int k=0;k<8;k++){
		int nbrLabel=abs(nbrs[k]);
		if(nbrLabel!=label){
			altPressure=max(fabs(pressure-averages[nbrLabel]),altPressure);
			foundNeighbor=true;
		}
	}
	
	//Could not find neighbor for springl. Assume springl is at boundary with background.
	float scale =(foundNeighbor)?(altPressure-currentPressure) * pressureWeight:0;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	norm.z *= (scale * SCALE_DOWN);
	norm.w = 0;

	forceUpdates[0]=norm.x;
	forceUpdates[1]=norm.y;
	forceUpdates[2]=norm.z;
}

__kernel void computeAdvectionForcesNoResampleACWE(__global Springl3D* capsules,__global float* pressureImage,global int* labels,global float* distancefield,global int* imageLabels,__global float* forceUpdates,global float* averages,float pressureWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	capsules+=id;	
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	int nbrs[8];
	float4 norm;
	float scale=1;
	bool foundNeighbor;
	float altPressure;
	float pressure;
	float currentPressure;
	float4 q;
	//Could not find neighbor for springl. Assume springl is at boundary with background.
	int label=abs(labels[id]);
	for(int n=0;n<3;n++){
		q=SCALE_UP*capsules->vertexes[n];
		pressure = interpolate(pressureImage,q.x,q.y,q.z);
		currentPressure=(label>0)?fabs(pressure-averages[label]):0;	
		int i=floor(q.x);
		int j=floor(q.y);
		int k=floor(q.z);
		nbrs[0]=getLabelValue(imageLabels,i,j,k);
		nbrs[1]=getLabelValue(imageLabels,i,j+1,k);
		nbrs[2]=getLabelValue(imageLabels,i+1,j+1,k);
		nbrs[3]=getLabelValue(imageLabels,i+1,j,k);
		nbrs[4]=getLabelValue(imageLabels,i,j,k+1);
		nbrs[5]=getLabelValue(imageLabels,i,j+1,k+1);
		nbrs[6]=getLabelValue(imageLabels,i+1,j+1,k+1);
		nbrs[7]=getLabelValue(imageLabels,i+1,j,k+1);
		altPressure=0;
		foundNeighbor=false;
		
		for(int k=0;k<8;k++){
			int nbrLabel=abs(nbrs[k]);
			if(nbrLabel!=label){
				altPressure=max(fabs(pressure-averages[nbrLabel]),altPressure);
				foundNeighbor=true;
			}
		}
		
		scale =((foundNeighbor)?(altPressure-currentPressure) * pressureWeight:0);
		
		norm=scale*getNormalValue(distancefield,imageLabels,label,q.x,q.y,q.z);	
		forceUpdates[0]=norm.x;
		forceUpdates[1]=norm.y;
		forceUpdates[2]=norm.z;
		forceUpdates+=3;
	}
}

__kernel void applyForcesNoResampleMogac(
	global Springl3D* capsules,
	global float* levelSetMat,
	global int* imageLabels,
	global int* labels,
	global float* forceUpdates,
	float maxForce,
	uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	forceUpdates+=9*id;
	float4 startPoint=(float4)(0,0,0,0);
	float4 endPoint;
	int label=abs(labels[id]);
	for(int i=0;i<3;i++){
		endPoint=capsules->vertexes[i];
		float w=endPoint.w;
		endPoint.x+=maxForce * forceUpdates[0];
		endPoint.y+=maxForce * forceUpdates[1];
		endPoint.z+=maxForce * forceUpdates[2];
		startPoint+=endPoint;
		endPoint.w=w;
		capsules->vertexes[i]=endPoint;
		forceUpdates+=3;
	}
	startPoint*=0.333333f;
	startPoint.w = (float)interpolateLevelSet(levelSetMat,imageLabels,label,SCALE_UP*startPoint.x, SCALE_UP*startPoint.y, SCALE_UP*startPoint.z) * SCALE_DOWN;
	capsules->particle = startPoint;
}
__kernel void computeAdvectionForcesPMogac(__global Springl3D* capsules,__global float* pressureImage,global int* labels,global int* imageLabels,__global float* forceUpdates,float pressureWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=3*id;
	capsules+=id;
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	
	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));		
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float z = pt.z * SCALE_UP;
	
	int nbrs[8];
	int i=floor(x);
	int j=floor(y);
	int k=floor(z);
	nbrs[0]=getLabelValue(imageLabels,i,j,k);
	nbrs[1]=getLabelValue(imageLabels,i,j+1,k);
	nbrs[2]=getLabelValue(imageLabels,i+1,j+1,k);
	nbrs[3]=getLabelValue(imageLabels,i+1,j,k);
	nbrs[4]=getLabelValue(imageLabels,i,j,k+1);
	nbrs[5]=getLabelValue(imageLabels,i,j+1,k+1);
	nbrs[6]=getLabelValue(imageLabels,i+1,j+1,k+1);
	nbrs[7]=getLabelValue(imageLabels,i+1,j,k+1);
	
	int label=abs(labels[id]);
	for(int k=0;k<8;k++){
		if(nbrs[k]!=0&&label!=nbrs[k]){
			forceUpdates[0]=0;
			forceUpdates[1]=0;
			forceUpdates[2]=0;
			return;
		}
	}
	
	float pressure = interpolate(pressureImage,x,y,z);
	float scale = pressure * pressureWeight * SCALE_DOWN;
	norm.x *= (scale);
	norm.y *= (scale);
	norm.z *= (scale);
	norm.w = 0;

	forceUpdates[0]=norm.x;
	forceUpdates[1]=norm.y;
	forceUpdates[2]=norm.z;
}
__kernel void computeAdvectionForcesNoResamplePMogac(
	__global Springl3D* capsules,
	__global float* pressureImage,
	global int* labels,
	global float* distanceField,
	global int* imageLabels,
	global float* forceUpdates,
	float pressureWeight,
	uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	capsules+=id;	
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	int nbrs[8];
	float4 norm;
	float scale=1;
	bool foundNeighbor;
	float pressure;
	float4 q;
	//Could not find neighbor for springl. Assume springl is at boundary with background.
	int label=abs(labels[id]);
	for(int n=0;n<3;n++){
		q=SCALE_UP*capsules->vertexes[n];
		pressure = interpolate(pressureImage,q.x,q.y,q.z);
		int i=floor(q.x);
		int j=floor(q.y);
		int k=floor(q.z);
		nbrs[0]=getLabelValue(imageLabels,i,j,k);
		nbrs[1]=getLabelValue(imageLabels,i,j+1,k);
		nbrs[2]=getLabelValue(imageLabels,i+1,j+1,k);
		nbrs[3]=getLabelValue(imageLabels,i+1,j,k);
		nbrs[4]=getLabelValue(imageLabels,i,j,k+1);
		nbrs[5]=getLabelValue(imageLabels,i,j+1,k+1);
		nbrs[6]=getLabelValue(imageLabels,i+1,j+1,k+1);
		nbrs[7]=getLabelValue(imageLabels,i+1,j,k+1);
		foundNeighbor=false;
		
		
		for(int nn=0;nn<8;nn++){
			int nbrLabel=abs(nbrs[nn]);
			if(nbrLabel!=label){
				foundNeighbor=true;
			}
		}
		
		scale =(foundNeighbor)?pressure * pressureWeight:0;
		norm=scale*getNormalValue(distanceField,imageLabels,label,q.x,q.y,q.z);	
		forceUpdates[0]=norm.x;
		forceUpdates[1]=norm.y;
		forceUpdates[2]=norm.z;
		forceUpdates+=3;
		
	}
}
__kernel void computeAdvectionForcesVMogac(
	__global Springl3D* capsules,
	__global float* advectionImage,
	__global float* forceUpdates,
	float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=3*id;
	capsules+=id;		
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float z = pt.z * SCALE_UP;
	
	float vx=interpolateVectorField(advectionImage,x,y,z, 0);
	float vy=interpolateVectorField(advectionImage,x,y,z, 1);
	float vz=interpolateVectorField(advectionImage,x,y,z, 2);

	forceUpdates[0]=vx*advectionWeight;
	forceUpdates[1]=vy*advectionWeight;
	forceUpdates[2]=vz*advectionWeight;
}
__kernel void computeAdvectionForcesNoResampleVMogac(
	__global Springl3D* capsules,
	__global float* advectionImage,
	__global float* forceUpdates,
	float advectionWeight,
	uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	capsules+=id;	
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	float4 q;
	for(int n=0;n<3;n++){
		q=SCALE_UP*capsules->vertexes[n];
		float vx=interpolateVectorField(advectionImage,q.x,q.y,q.z, 0);
		float vy=interpolateVectorField(advectionImage,q.x,q.y,q.z, 1);
		float vz=interpolateVectorField(advectionImage,q.x,q.y,q.z, 2);
		forceUpdates[0]=vx*advectionWeight;
		forceUpdates[1]=vy*advectionWeight;
		forceUpdates[2]=vz*advectionWeight;
		forceUpdates+=3;
	}
}

__kernel void fixLabelsMogac(
		__global Springl3D* capsules,
		__global CapsuleNeighbor3D* capsuleNeighbors,
		__global float* origUnsignedLevelSet,
		__global int* labels,uint N){
	uint id=get_global_id(0);
	int oldLabel=0;
	if(id>=N||(oldLabel=labels[id])>=0)return;	
	capsuleNeighbors+=3*MAX_NEIGHBORS*id;
	Springl3D capsule=capsules[id];
	Springl3D nbr;
	CapsuleNeighbor3D ci;
	float totalWeight=1.0E-6f;
	float4 newPoint=(float4)(0,0,0,0);
	float4 particle=capsule.particle;
	particle.w=0;
	float minDist=2.0f;
	int newLabel=-1;
	int label=0;
	oldLabel=abs(oldLabel);
	for (int i = 0; i < 3; i++) {
		for (int n=0;n<MAX_NEIGHBORS;n++) {
			ci= capsuleNeighbors[MAX_NEIGHBORS*i+n];
			if(ci.capsuleId==-1)break;
			label=labels[ci.capsuleId];
			//There may be a race condition here! But it shouldn't have a big effect on the final mapping
			if(label<0)continue;
			nbr=capsules[ci.capsuleId];
			float4 mapPoint=(float4)(nbr.vertexes[0].w,nbr.vertexes[1].w,nbr.vertexes[2].w,0);
			nbr.particle.w=0;
			float w=distance(particle,nbr.particle);
			if(w<minDist&&label==oldLabel){
				minDist=w;
				newLabel=oldLabel;
				//newPoint=mapPoint;
			}
			w=1.0f/(w+1E-2f);
			totalWeight+=w;
			newPoint+=w*mapPoint;
		}
	}	
	if(newLabel<0){
		return;
	}

	
	newPoint/=totalWeight;
	const uint MAX_ITERATIONS=32;
	const float EPSILON=0.1f; 
	const float DELTA=0.5f;
	float levelSetValue=0;
	newPoint*=SCALE_UP;
	for(int i=0;i<MAX_ITERATIONS;i++){
		levelSetValue=interpolate(origUnsignedLevelSet,newPoint.x,newPoint.y,newPoint.z);
		if(levelSetValue<=EPSILON)break;
		newPoint-=DELTA*levelSetValue*getGradientValue(origUnsignedLevelSet,newPoint.x,newPoint.y,newPoint.z);
	}
	newPoint*=SCALE_DOWN;
	capsule.vertexes[0].w=newPoint.x;
	capsule.vertexes[1].w=newPoint.y;
	capsule.vertexes[2].w=newPoint.z;
	capsules[id]=capsule;
	labels[id]=newLabel;
}
float distanceSquaredCapsule(float4 p, Springl3D* capsule,float4* closestPoint) {
	return distanceSquaredTriangle(p,capsule->vertexes[0],capsule->vertexes[1],capsule->vertexes[2],closestPoint);
}
//Compute level set value in reduction phase
__kernel void reduceLevelSetMogac(
		global int* activeList,
		global int* spatialLookup,
		global Springl3D* capsules,
		global float* imageMat,
		global int* labels,
		uint activeListSize,
		int label){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float4 pt;
	float4 ret;
	spatialLookup+=gid*MAX_BIN_SIZE;
	Springl3D cap;
	pt = (float4)(
		i * SCALE_DOWN,
		j * SCALE_DOWN,
		k * SCALE_DOWN,0);
	float value = (0.1f+MAX_VEXT)*(0.1f+MAX_VEXT);
	for(int index=1;index<MAX_BIN_SIZE;index++){
		int binId=spatialLookup[index];
		if(binId<0)break;
		if(abs(labels[binId])==label){
			cap = capsules[binId];
			//Compute distance squared between point and triangle
			float d2 = distanceSquaredCapsule(pt,&cap,&ret);
			if (d2 < value) {
				value = d2;
			}
		}
	}
	value = native_sqrt(value);
	imageMat[id] = value;
}
kernel void combineLabelImages(global int* imageLabels,global float* distanceField,int label){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	float levelset=distanceField[id];
	if(levelset<=0){	
		imageLabels[id]=label;
	}
}

kernel void copyLevelSetImageMOGAC(global float* srcImage,global int* labels,const global float4* colors,global float4* destImage){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float v111 = getNonBackgroundLevelSetValue(srcImage,labels,colors, i, j, k);
	float v211 = getNonBackgroundLevelSetValue(srcImage,labels,colors, i + 1, j, k);
	float v121 = getNonBackgroundLevelSetValue(srcImage,labels,colors, i, j + 1, k);
	float v101 = getNonBackgroundLevelSetValue(srcImage,labels,colors, i, j - 1, k);
	float v011 = getNonBackgroundLevelSetValue(srcImage,labels,colors, i - 1, j, k);
	float v110 = getNonBackgroundLevelSetValue(srcImage,labels,colors, i, j, k - 1);
	float v112 = getNonBackgroundLevelSetValue(srcImage,labels,colors, i, j, k + 1);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w=1E-6f;
	grad=normalize(grad);
	grad.w=clamp(v111,-10.0f,10.0f);	
	//if(i==ROWS-1||j==COLS-1||k==SLICES-1||i==0||j==0||k==0)grad.w=max(grad.w,1.5f);
	destImage[id]=grad;
}
kernel void initDeformationField(
	global Springl3D* capsules,
	global float* advectionImage,
	global float* forceUpdates,
	uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	Springl3D capsule=capsules[id];		
	float4 pt;
	float4 mean=(float4)(0,0,0,0);
	for(int i=0;i<3;i++){
		pt=capsule.vertexes[i];
		float x = pt.x * SCALE_UP;
		float y = pt.y * SCALE_UP;
		float z = pt.z * SCALE_UP;
		float vx=interpolateVectorField(advectionImage,x,y,z, 0);
		float vy=interpolateVectorField(advectionImage,x,y,z, 1);
		float vz=interpolateVectorField(advectionImage,x,y,z, 2);
		forceUpdates[0]=vx* SCALE_DOWN;
		forceUpdates[1]=vy* SCALE_DOWN;
		forceUpdates[2]=vz* SCALE_DOWN;	
		forceUpdates+=3;
	}
}
__kernel void applyForcesMogac(
	global Springl3D* capsules,
	global float* levelSetMat,
	global int* imageLabels,
	global int* labels,
	global float* forceUpdates,
	float maxForce,
	uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	forceUpdates+=3*id;
	float4 startPoint=capsules->particle;
	if(startPoint.w==STATIC_SPRINGL)return;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float z = startPoint.z *SCALE_UP;
	int label=abs(labels[id]);
	float4 endPoint;

	endPoint.x = (startPoint.x + maxForce * forceUpdates[0]);
	endPoint.y = (startPoint.y + maxForce * forceUpdates[1]);
	endPoint.z = (startPoint.z + maxForce * forceUpdates[2]);
	endPoint.w = 0;
	float4 displacement=endPoint-startPoint;
	displacement.w=0;
	
	endPoint.w = (float)interpolateLevelSet(levelSetMat,imageLabels,label,x, y, z) * SCALE_DOWN;
	capsules->particle = endPoint;
	
	for(int i=0;i<3;i++){
		capsules->vertexes[i]+=displacement;
	}
}
__kernel void applyForcesTopoRuleMogac(
	global Springl3D* capsules,
	global float* levelSetMat,
	global int* imageLabels,
	global int* labels,
	global float* forceUpdates,
	float maxForce,
	uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	forceUpdates+=3*id;
	float4 startPoint=capsules->particle;
	if(startPoint.w==STATIC_SPRINGL)return;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float z = startPoint.z *SCALE_UP;
	int label=abs(labels[id]);
	float4 endPoint;
	
	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));
	
	// Adjust speed so that contour does not cross into narrow band
	// of a neighboring contour.
	// This is only possible if a topology rule is in effect.
	
	float lend;
	
	float4 displacement;
	displacement.w=0;
	endPoint.w=(float)interpolateLevelSet(levelSetMat,imageLabels,label,x, y, z) * SCALE_DOWN;
	int tries=0;
	
	float4 forceUpdate=(float4)(forceUpdates[0],forceUpdates[1],forceUpdates[2],0);
	
	float dotprod=dot(forceUpdate,norm);
	do {
		endPoint.x = (startPoint.x + maxForce * forceUpdate.x);
		endPoint.y = (startPoint.y + maxForce * forceUpdate.y);
		endPoint.z = (startPoint.z + maxForce * forceUpdate.z);
		x = endPoint.x * SCALE_UP;
		y = endPoint.y * SCALE_UP;
		z = endPoint.z * SCALE_UP;
		lend = interpolateLevelSet(levelSetMat,imageLabels,label,x,y,z) * SCALE_DOWN;
		tries++;
		forceUpdate*=0.5f;
	} while ((lend - endPoint.w) * dotprod < 0&& tries <= 4);
	
	if(tries!=4){				
		capsules->particle = endPoint;
		displacement=endPoint-startPoint;
		for(int i=0;i<3;i++){
			capsules->vertexes[i]+=displacement;
		}
	}
}
kernel void deleteCountActiveListMogac(
	global int* offsetList,
	const global uint* activeList,
	global float* distanceField,
	global int* indexBuffer,
	global float* levelset,
	int elements){
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int total=0;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz;i++){
		int index=activeList[i];
		float val=distanceField[index];
		if(val>=0&&val<=MAX_DISTANCE){
			total++;
		} else {
			indexBuffer[index]=-1;
			levelset[index]=0.1f+MAX_VEXT;
		}
	}
	offsetList[id]=total;
}
kernel void deleteCountActiveListHistoryMogac(
	global int* offsetList,
	const global uint* activeList,
	global float* distanceField,
	global int* indexBuffer,
	global float* levelset,
	global int* history,
	int elements){
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int total=0;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz;i++){
		int index=activeList[i];
		int times=history[index];
		float val=distanceField[index];
		if(val>=0&&val<=MAX_DISTANCE&&times==1){
			total++;
		} else {
			indexBuffer[index]=-1;
			if(times==1)levelset[index]=0.1f+MAX_VEXT;
		}
	}
	offsetList[id]=total;
}
__kernel void applyDeformationMogac(__global Springl3D* capsules,__global float* levelSetMat,global int* imageLabels,global int* labels,__global float* forceUpdates,float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	Springl3D capsule=capsules[id];
	float4 startPoint=capsule.particle;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float z = startPoint.z *SCALE_UP;
	int label=abs(labels[id]);
	float4 endPoint=(float4)(0,0,0,0);	
	for(int i=0;i<3;i++){
		capsule.vertexes[i].x+=maxForce*forceUpdates[0];
		capsule.vertexes[i].y+=maxForce*forceUpdates[1];
		capsule.vertexes[i].z+=maxForce*forceUpdates[2];
		forceUpdates+=3;
		endPoint+=capsule.vertexes[i];
	}	
	endPoint*=0.333333f;
	endPoint.w = (float)interpolateLevelSet(levelSetMat,imageLabels,label,x, y, z) * SCALE_DOWN;
	capsule.particle = endPoint;
	capsules[id]=capsule;
}
__kernel void transformImageMogac(__global float* outLevelSet,global int* outLabels,__global float* inLevelSet,global int* inLabels,global Matrix4f* invMat){
	uint id=get_global_id(0);
	int i,j,k;
	if(id>=ROWS*COLS*SLICES)return;
	getRowColSlice(id,&i,&j,&k);
	float4 pt=(float4)(i,j,k,1);
	Matrix4f M=*invMat;
	pt=transform4(pt,M);
	
	outLevelSet[id]=interpolate(inLevelSet,pt.x,pt.y,pt.z);
	outLabels[id]=inLabels[getSafeIndex((int)round(pt.x),(int)round(pt.y),(int)round(pt.z))];
	
}
__kernel void affineTransformMogac(__global Springl3D* capsules,const global Matrix4f* fwdTrans,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	Matrix4f M=*fwdTrans;
	Springl3D capsule=capsules[id];
	float4 startPoint=capsule.particle;
	float sw=startPoint.w;
	float4 endPoint=(float4)(0,0,0,0);	
	float4 vert;
	float w;
	for(int i=0;i<3;i++){
		vert=capsule.vertexes[i];
		w=vert.w;
		vert*=SCALE_UP;
		vert.w=1;
		vert=transform4(vert,M);
		vert*=SCALE_DOWN;
		endPoint+=vert;
		vert.w=w;
		capsule.vertexes[i]=vert;
	}	
	endPoint*=0.333333f;
	endPoint.w = sw;
	capsule.particle = endPoint;
	capsules[id]=capsule;	
}
kernel void copyFullBuffer(
	global float* oldLevelSet,
	global int* oldLabels,
	global float* levelSet,
	global int* labels){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	oldLevelSet[id]=levelSet[id];	
	oldLabels[id]=labels[id];
}

kernel void initDistanceField(global float* distanceField){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;	
	distanceField[id]=2*MAX_VEXT;
}
kernel void buildDistanceField(
	global Springl3D *capsules,
	global int* labels,
	global float* distanceField,
	int label,
	uint N){
	uint id=get_global_id(0);
	if(id>=N||(label>0&&labels[id]!=label))return;
	float4 lowerPoint = (float4)(1E10f, 1E10f, 1E10f,0);
	float4 upperPoint = (float4)(-1E10f, -1E10f, -1E10f,0);
	Springl3D cap=capsules[id];
	//#pragma unroll
	//Find axis aligned bounding box
	for (uint i=0;i<3;i++) {
		float4 v=cap.vertexes[i];
		v.w=0;
		lowerPoint=min(v,lowerPoint);
		upperPoint=max(v,upperPoint);
	}
	
	__const float4 ZERO=(float4)(0,0,0,0);
	__const float4 IMAGE_MAX=(float4)(ROWS-1,COLS-1,SLICES-1,0);
	//Compute lower index
	lowerPoint*=SCALE_UP;
	float4 lower=max(ZERO,floor(lowerPoint));
	
	int lowerRow=(int)lower.x;
	int lowerCol=(int)lower.y;
	int lowerSlice=(int)lower.z;
	
	//Compute upper index
	upperPoint*=SCALE_UP;
	float4 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
	int upperRow=(int)upper.x;
	int upperCol=(int)upper.y;
	int upperSlice=(int)upper.z;
	float4 dim=upper-lower;
	uint count=0;		
	float4 ret;
	float4 pt;
	for (int k = lowerSlice; k < upperSlice; k++) {
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
				pt = (float4)(
							i * SCALE_DOWN,
							j * SCALE_DOWN,
							k * SCALE_DOWN,0);
				float d2 = distanceSquared(pt,&cap,&ret);
				//To minimize the size of the map, compute which voxels are near the target triangle and store them in a bit mask
				if(d2<=4*(MAX_VEXT)*(MAX_VEXT)){
					int index=getIndex(i,j,k);
					float oldValue=atomic_xchg(&distanceField[index],10);
					float smaller=min(oldValue,sqrt(d2));
					atomic_xchg(&distanceField[index],smaller);
				}
			}
		}
	}
}

inline float getSimpleBlockValue(global float* levelSet,int i,int j,int k){
	if(k<0){
		return 1E10;
	} else return levelSet[getSafeIndex(i,j,k)];
}
inline int hasSimpleNearestNeighbor(global float* levelSet,int i,int j,int k){
	float v011 =getSimpleBlockValue(levelSet,i - 1, j, k);
	float v121 =getSimpleBlockValue(levelSet,i, j + 1, k);
	float v101 =getSimpleBlockValue(levelSet,i, j - 1, k);
	float v211 =getSimpleBlockValue(levelSet,i + 1, j, k);
	float v110 =getSimpleBlockValue(levelSet,i, j, k - 1);
	float v112 =getSimpleBlockValue(levelSet,i, j, k + 1);
	return (
	   v011>0||
	   v121>0||
	   v101>0||
	   v211>0||
	   v110>0||
	   v112>0);
}
kernel void simpleErodeLevelSet(global float* unsignedLevelSet,global float* levelSetIn,global float* levelSetOut){
	uint id=get_global_id(0);
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	if(hasSimpleNearestNeighbor(levelSetIn,i,j,k)){
		float val=unsignedLevelSet[id];
		if(val>1.5f*MAX_VEXT){		
			levelSetOut[id]=1;
		}
	}
}
__kernel void reduceNormalsMOGAC(
		__global int* activeList,
		__global int* spatialLookup,
		__global Springl3D* capsules,
		global float4* destImage,
		global int* labels,
		global int* activeLabels,
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
	float4 normal=(float4)(1,1,0,0);	
	int index;
	int count=0;
	float4 currentNorm=destImage[id];
	float value=currentNorm.w;
	currentNorm.w=0;
	float minDist=1E10;
	float4 npt=SCALE_DOWN*(float4)(i,j,k,0);

	for(index=1;index<MAX_BIN_SIZE;index++){
		int binId=spatialLookup[index];
		if(binId<0)break;
		int l=abs(labels[binId]);
		if(l<1||activeLabels[l-1]==0)continue;
		cap = capsules[binId];
		cap.particle.w=0;
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
__kernel void markStaticSpringlsMogac(
		global Springl3D *capsules,
		global int* historyBuffer,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		return;
	}
	Springl3D capsule=capsules[id];
	float4 pt=SCALE_UP*capsule.particle;
	//float uval=interpolate(unsignedLevelSet,pt.x,pt.y,pt.z);
	int label=historyBuffer[getSafeIndex((int)round(pt.x),(int)round(pt.y),(int)round(pt.z))];
	if(label==0){
		capsule.particle.w=STATIC_SPRINGL;
		capsules[id]=capsule;
	}
}
kernel void extendUnsignedDistanceField(
global float* unsignedLevelSet,uint band){
	uint id=get_global_id(0);
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float v111;
	float v011;
	float v121;
	float v101;
	float v211;
	float v110;
	float v112;	
	v111 =getImageValue(unsignedLevelSet,i, j, k);
	v011 =getImageValue(unsignedLevelSet,i - 1, j, k);
	v121 =getImageValue(unsignedLevelSet,i, j + 1, k);
	v101 =getImageValue(unsignedLevelSet,i, j - 1, k);
	v211 =getImageValue(unsignedLevelSet,i + 1, j, k);
	v110 =getImageValue(unsignedLevelSet,i, j, k - 1);
	v112 =getImageValue(unsignedLevelSet,i, j, k + 1);
	if(v111>band+MAX_VEXT){
		v111=1E10f;
		v111=min(v011,v111);
		v111=min(v121,v111);
		v111=min(v101,v111);
		v111=min(v211,v111);
		v111=min(v110,v111);
		v111=min(v112,v111);	
		v111+=1.0f;	
		unsignedLevelSet[getIndex(i,j,k)]=v111;
	} 
}