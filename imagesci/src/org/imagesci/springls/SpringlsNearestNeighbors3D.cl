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
 
 #pragma OPENCL_EXTENSION cl_khr_local_int32_base_atomics : enable
#define STATIC_SPRINGL (-1E8f)
typedef struct{
	float4 particle;
    float4 vertexes[3];
} Springl3D;

//Store capsule id and vertex id [0,1,2]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor3D;

//Comparators for sorting
inline void ComparatorLocal(
    __local uint *keyA,
    __local uint *keyB,
    uint dir
){
    if( (*keyA > *keyB) == dir ){
        uint t;
        t = *keyA; *keyA = *keyB; *keyB = t;
    }
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

inline void getRowColSlice(uint index,int* i, int* j, int* k) {
	(*k)=index/(ROWS*COLS);
	int ij=index-(*k)*(ROWS * COLS);
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}
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
__kernel void splatBBoxCount(
__global Springl3D *capsules,__global uint *counts,__global uint* mask,uint N){
		uint id=get_global_id(0);
		if(id>=N)return;
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
		float vol=dim.x*dim.y*dim.z;
		if(vol>128){
			counts[id]=0;
			mask[id]=0;	
		}
		
		uint count=0;		
		float4 ret;
		float4 pt;
		int index=0;
		uint iFlagIndex=0;
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
						count++;
						iFlagIndex|=(1<<index);
					}
					index++;
				}
			}
		}
		mask[id]=iFlagIndex;
		//Record number of bins needed for each capsule
		counts[id]=count;
}
kernel void buildLUT(
	global Springl3D *capsules,
	global int* indexMap,
	global int* spatialLookUp,
	uint N,
	uint activeListSize){
		uint id=get_global_id(0);
		if(id>=N)return;
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
						int offsetIndex=MAX_BIN_SIZE*indexMap[getSafeIndex(i,j,k)];
						int currentOffset=atomic_add(&spatialLookUp[offsetIndex],1);
						if(currentOffset<MAX_BIN_SIZE){
							spatialLookUp[offsetIndex+currentOffset]=id;
						}
					}
				}
			}
		}
}
__kernel void splatBBoxCountMesh(
__global float4* vertexes,__global uint *counts,__global uint* mask,uint N){
		uint id=get_global_id(0);
		if(id>=N)return;
		float4 lowerPoint = (float4)(1E10f, 1E10f, 1E10f,0);
		float4 upperPoint = (float4)(-1E10f, -1E10f, -1E10f,0);
		vertexes+=3*id;
		//#pragma unroll
		//Find axis aligned bounding box
		for (uint i=0;i<3;i++) {
			float4 v=vertexes[i];
			v.w=0;
			lowerPoint=min(v,lowerPoint);
			upperPoint=max(v,upperPoint);
		}
		__const float4 ZERO=(float4)(0,0,0,0);
		__const float4 IMAGE_MAX=(float4)(ROWS-1,COLS-1,SLICES-1,0);
		//Compute lower index
		float4 lower=max(ZERO,floor(lowerPoint));
		
		int lowerRow=(int)lower.x;
		int lowerCol=(int)lower.y;
		int lowerSlice=(int)lower.z;
		
		//Compute upper index
		float4 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		int upperRow=(int)upper.x;
		int upperCol=(int)upper.y;
		int upperSlice=(int)upper.z;

		float4 dim=upper-lower;
		float vol=dim.x*dim.y*dim.z;
		if(vol>128){
			counts[id]=0;
			mask[id]=0;	
		}
		
		uint count=0;		
		float4 ret;
		float4 pt;
		int index=0;
		float4 v0=vertexes[0];
		float4 v1=vertexes[1];
		float4 v2=vertexes[2];
		uint iFlagIndex=0;
		for (int k = lowerSlice; k < upperSlice; k++) {
			for (int j = lowerCol; j < upperCol; j++) {
				for (int i = lowerRow; i < upperRow; i++) {
						pt = (float4)(
								i ,
								j ,
								k ,0);
					float d2 = distanceSquaredTriangle(pt,v0,v1,v2,&ret);
					//To minimize the size of the map, compute which voxels are near the target triangle and store them in a bit mask
					if(d2<=4*(SCALE_UP*MAX_VEXT)*(SCALE_UP*MAX_VEXT)){
						count++;
						iFlagIndex|=(1<<index);
					}
					index++;
				}
			}
		}
		mask[id]=iFlagIndex;
		//Record number of bins needed for each capsule
		counts[id]=count;
}
//Initialize the index map
__kernel void initIndexMapNB(__global int* indexMap){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	//initialize index map
	indexMap[id]=(-1);
}
__kernel void initIndexMap(__global int* indexMap){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	//initialize index map
	indexMap[id]=(-1);
}
__kernel void createIndexMap(__global int* indexMap,__global uint *keys,__global uint *values){
	uint id=get_global_id(0);
	if(id==0)return;
	int prevValue=keys[id-1];
	int currentValue=keys[id];
	//Create index map from image location into key/value map
	if(currentValue!=prevValue&&currentValue<IMAGE_SIZE){
		indexMap[currentValue]=id;
	}
}
__kernel void initLUT(__global int* spatialLookUp,int arraySize){
	uint id=get_global_id(0);
	if(id>=arraySize)return;
	spatialLookUp+=MAX_BIN_SIZE*id;
	spatialLookUp[0]=1;
	for(int i=1;i<MAX_BIN_SIZE;i++){
		spatialLookUp[i]=-1;
	}
}
//Create list of indexes into map
__kernel void updateIndexMap(__global int* indexMap,global uint *activeList,int activeListSize){
	uint id=get_global_id(0);
	if(id>=activeListSize)return;
	indexMap[activeList[id]]=id;
}

//Compute level set value in reduction phase
__kernel void reduceLevelSet(
		__global int* activeList,
		__global int* spatialLookup,
		__global Springl3D* capsules,
		__global float* imageMat,
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
	pt = (float4)(
		i * SCALE_DOWN,
		j * SCALE_DOWN,
		k * SCALE_DOWN,0);
	float value = (0.1f+MAX_VEXT)*(0.1f+MAX_VEXT);
	for(int index=1;index<MAX_BIN_SIZE;index++){
		int binId=spatialLookup[index];
		if(binId<0)break;
		cap = capsules[binId];
		//Compute distance squared between point and triangle
		float d2 = distanceSquared(pt,&cap,&ret);
		if (d2 < value) {
			value = d2;
		}
	}
	value = native_sqrt(value);
	imageMat[id] = value;	
}

__kernel void reduceNormals(
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




//Compute level set value in reduction phase
__kernel void reduceLevelSetMesh(
		__global int* indexMap,
		__global uint* keys,
		__global uint* values,
		__global float4* vertexes,
		__global float* imageMat,
		uint N){
	uint id=get_global_id(0);
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float4 pt;
	float4 ret;
	int index;
	uint hashValue = id;
	int startIndex = indexMap[hashValue];	
	
	pt = (float4)(i ,j ,k ,0);
	float value = SCALE_UP*SCALE_UP*(0.1f+MAX_VEXT)*(0.1f+MAX_VEXT);
	index = startIndex;
	
	//label = -1;
	float4 v0,v1,v2;
	do {
		//If at the start of a new key section, break;
		if (keys[index] != hashValue)break;
		id=values[index];
		v0=vertexes[3*id];
		v1=vertexes[3*id+1];
		v2=vertexes[3*id+2];

		//Compute distance squared between point and triangle
		float d2 = distanceSquaredTriangle(pt,v0,v1,v2,&ret);
		if (d2 < value) {
			value = d2;
		}
		index++;
	} while (index < N);
	value = SCALE_DOWN*native_sqrt(value);
	imageMat[hashValue] = value;	
	
}
//Compute level set value in reduction phase
__kernel void unsignedToSignedLevelSet(
		__global float* unsignedLevelSet,
		__global float* signedLevelSet){
	int i,j,k;	
	uint id=get_global_id(0);
	uint index=0;
	getRowColSlice(id,&i,&j,&k);
	int sign=1;
	bool borderRegion=false;
	int count=0;
	for(k=0;k<SLICES;k++){
		index=getHashValue(i,j,k);
		if(!borderRegion&&unsignedLevelSet[index]<MAX_VEXT){
			borderRegion=true;
			sign*=-1;
		} if(borderRegion&&unsignedLevelSet[index]>MAX_VEXT){
			borderRegion=false;
		}
		signedLevelSet[index]=unsignedLevelSet[index]*sign;
		count++;
	}
	
	
	sign=1;
	borderRegion=false;
	for(k=SLICES-1;k>=0;k--){
		index=getHashValue(i,j,k);
		if(!borderRegion&&unsignedLevelSet[index]<MAX_VEXT){
			borderRegion=true;
		}
		if(!borderRegion&&signedLevelSet[index]<0)signedLevelSet[index]=unsignedLevelSet[index]*sign;
	}
	
	
}
//Splat the bounding boxes to the map
__kernel void splatBBox(
	__global Springl3D *capsules,
	__global uint *offsets,
	__global uint* mask,
	__global uint *keys,
	__global uint *values,uint N,uint mapSize){

		uint id=get_global_id(0);
		if(id>=N)return;
		float4 lowerPoint = (float4)(1E10f, 1E10f, 1E10f,0);
		float4 upperPoint = (float4)(-1E10f, -1E10f, -1E10f,0);
		Springl3D cap=capsules[id];
		uint offset=offsets[id];
		//#pragma unroll
		for (uint i=0;i<3;i++) {
			float4 v=cap.vertexes[i];
			v.w=0;
			lowerPoint=min(v,lowerPoint);
			upperPoint=max(v,upperPoint);
		}
		
		__const float4 ZERO=(float4)(0,0,0,0);
		__const float4 IMAGE_MAX=(float4)(ROWS-1,COLS-1,SLICES-1,0);
		
		lowerPoint*=SCALE_UP;
		float4 lower=max(ZERO,floor(lowerPoint));
		
		uint lowerRow=(uint)lower.x;
		uint lowerCol=(uint)lower.y;
		uint lowerSlice=(uint)lower.z;

		upperPoint*=SCALE_UP;
		float4 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		uint upperRow=(uint)upper.x;
		uint upperCol=(uint)upper.y;
		uint upperSlice=(uint)upper.z;
		
		float4 dim=upper-lower;
		float vol=dim.x*dim.y*dim.z;
		if(vol>128){
			return;
		}
		
		uint iFlagIndex=mask[id];
		int index=0;
		for (int k = lowerSlice; k < upperSlice; k++) {
			for (int j = lowerCol; j < upperCol; j++) {
				for (int i = lowerRow; i < upperRow; i++) {
					//Use the bit mask to test if point is near the triangle.
					if(((iFlagIndex >> index) & 1)){
						if(offset>=mapSize)return;
						uint hash = getHashValue(i, j, k);
						values[offset]=id;
						keys[offset]=hash;
						offset++;
						//Map size exceeded! Fail gracefully.

					}
					index++;
				}
			}
		}
}
//Splat the bounding boxes to the map
__kernel void splatBBoxMesh(
	__global float4* vertexes,
	__global uint *offsets,
	__global uint* mask,
	__global uint *keys,
	__global uint *values,uint N,uint mapSize){
		
		uint id=get_global_id(0);
		if(id>=N)return;
		float4 lowerPoint = (float4)(1E10f, 1E10f, 1E10f,0);
		float4 upperPoint = (float4)(-1E10f, -1E10f, -1E10f,0);
		vertexes+=3*id;
		uint offset=offsets[id];
		for (uint i=0;i<3;i++) {
			float4 v=vertexes[i];
			v.w=0;
			lowerPoint=min(v,lowerPoint);
			upperPoint=max(v,upperPoint);
		}
		
		__const float4 ZERO=(float4)(0,0,0,0);
		__const float4 IMAGE_MAX=(float4)(ROWS-1,COLS-1,SLICES-1,0);
		
		float4 lower=max(ZERO,floor(lowerPoint));
		
		uint lowerRow=(uint)lower.x;
		uint lowerCol=(uint)lower.y;
		uint lowerSlice=(uint)lower.z;

		float4 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		uint upperRow=(uint)upper.x;
		uint upperCol=(uint)upper.y;
		uint upperSlice=(uint)upper.z;
		
		float4 dim=upper-lower;
		float vol=dim.x*dim.y*dim.z;
		if(vol>128){
			return;
		}
		
		uint iFlagIndex=mask[id];
		int index=0;
		for (int k = lowerSlice; k < upperSlice; k++) {
			for (int j = lowerCol; j < upperCol; j++) {
				for (int i = lowerRow; i < upperRow; i++) {
					//Use the bit mask to test if point is near the triangle.
					if(((iFlagIndex >> index) & 1)){
						if(offset>=mapSize)return;
						uint hash = getHashValue(i, j, k);
						values[offset]=id;
						keys[offset]=hash;
						offset++;
						//Map size exceeded! Fail gracefully.

					}
					index++;
				}
			}
		}
}
__kernel void findClosestCorrespondencePoints(
		global int* indexMap,
		global int* spatialLookUp,
		global Springl3D* capsules,
		global float4* pointsIn,
		global float4* pointsOut,
		global int* labels,
		uint elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	float4 pt=pointsIn[gid];
	pt.w=0;
	float4 ret;
	int id;
	uint lowerRow=(uint)clamp((uint)floor(pt.x-5),(uint)0,(uint)(ROWS-1));
	uint lowerCol=clamp((uint)floor(pt.y-5),(uint)0,(uint)(COLS-1));
	uint lowerSlice=clamp((uint)floor(pt.z-5),(uint)0,(uint)(SLICES-1));
	uint upperRow=(uint)clamp((uint)ceil(pt.x+6),(uint)0,(uint)(ROWS-1));
	uint upperCol=clamp((uint)ceil(pt.y+6),(uint)0,(uint)(COLS-1));
	uint upperSlice=clamp((uint)ceil(pt.z+6),(uint)0,(uint)(SLICES-1));
	int startIndex;
	uint hashValue;
	
	float value = 1E10f;//(0.1f+MAX_VEXT)*(0.1f+MAX_VEXT);
	pt=SCALE_DOWN*pt;
	float4 closestPt=(float4)(-1,-1,-1,0);
	Springl3D cap;
	for (int k = lowerSlice; k < upperSlice; k++) {
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
				hashValue = getSafeIndex(i, j, k);
				startIndex = MAX_BIN_SIZE*indexMap[hashValue];
				if(startIndex<0)continue;
				for(int index=1;index<MAX_BIN_SIZE;index++){
					id=spatialLookUp[startIndex+index];
					if(id<0)break;
					cap = capsules[id];
					//Compute distance squared between point and triangle
					float d2 = distanceSquared(pt,&cap,&ret);	
					if (d2 < value&&labels[id]!=-1) {
						value = d2;
						closestPt=SCALE_UP*(float4)(cap.vertexes[0].w,cap.vertexes[1].w,cap.vertexes[2].w,SCALE_DOWN);					
					}
				} 	
			}
		}
	}
	pointsOut[gid]=closestPt;
}
__kernel void mapNearestNeighbors(
		__global int* nbrs,
		__global int* indexMap,
		__global int* spatialLookup,
		__global Springl3D* capsules,
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
		int cid,startIndex;
		uint hashValue;
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
						if (cid!= id) {//Ignore the current triangle
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
////////////////////////////////////////////////////////////////////////////////
// Monolithic bitonic sort kernel for short arrays fitting into local memory
// From NVIDIA! Too slow for INTEL :(
////////////////////////////////////////////////////////////////////////////////
/*
__kernel void sortNearestNeighbors(__global uint *d_DstKey, uint elements){

    __local  uint l_key[LOCAL_SIZE_LIMIT];
    __const uint dir=1;
    
    //Offset to the beginning of subbatch and load data
    uint startIndex=get_group_id(0) * LOCAL_SIZE_LIMIT + get_local_id(0);
    
    d_DstKey += startIndex;
    if(startIndex<elements){
    	l_key[get_local_id(0) +                      0] = d_DstKey[                     0];
    } else {
    	l_key[get_local_id(0) +                      0] = MAX_VALUE;	
    }
    if(startIndex+ (LOCAL_SIZE_LIMIT / 2)<elements){
    	l_key[get_local_id(0) + (LOCAL_SIZE_LIMIT / 2)] = d_DstKey[(LOCAL_SIZE_LIMIT / 2)];
    } else {
    	l_key[get_local_id(0) + (LOCAL_SIZE_LIMIT / 2)] = MAX_VALUE;	
    }
    
	    for(uint size = 2; size < MAX_NEAREST_BINS; size <<= 1){
	        //Bitonic merge
	        uint ddd = dir ^ ( (get_local_id(0) & (size / 2)) != 0 );
	        for(uint stride = size / 2; stride > 0; stride >>= 1){
	            barrier(CLK_LOCAL_MEM_FENCE);
	            for(int offset=0;offset<LOCAL_SIZE_LIMIT;offset+=MAX_NEAREST_BINS){
		            uint pos = 2 * get_local_id(0) - (get_local_id(0) & (stride - 1))+offset;
		            ComparatorLocal(
		                &l_key[pos +      0], 
		                &l_key[pos + stride], 
		                ddd
		            );
	            }
	        }
	    }
	    //ddd == dir for the last bitonic merge step
	    {
	        for(uint stride = MAX_NEAREST_BINS / 2; stride > 0; stride >>= 1){
	            barrier(CLK_LOCAL_MEM_FENCE);
	            for(int offset=0;offset<LOCAL_SIZE_LIMIT;offset+=MAX_NEAREST_BINS){
		            uint pos = 2 * get_local_id(0) - (get_local_id(0) & (stride - 1))+offset;
		            ComparatorLocal(
		                &l_key[pos +      0], 
		                &l_key[pos + stride], 
		                dir
		            );
	            }
	        }
	    }	    
   barrier(CLK_LOCAL_MEM_FENCE);
   if(startIndex+ (LOCAL_SIZE_LIMIT / 2)<elements){ 
   		d_DstKey[                     0] = l_key[get_local_id(0) +                      0];
   }
   if(startIndex+ (LOCAL_SIZE_LIMIT / 2)<elements){
   		d_DstKey[(LOCAL_SIZE_LIMIT / 2)] = l_key[get_local_id(0) + (LOCAL_SIZE_LIMIT / 2)];
   }
    
}
*/

//Traditional in-place merge sort (http://www.iti.fh-flensburg.de/lang/algorithmen/sortieren/merge/mergen.htm)
inline void Merge(__global uint* a,__global uint* b, uint* c, uint m, uint n ){
  uint i = 0, j = 0, k = 0;
  while (i < m && j < n){
    if( a[i] < b[j] ){
      c[k++] = a[i++];
    } else {
      c[k++] = b[j++];
    }
  }
  while ( i < m ) {
    c[k++] = a[i++];
  }
  while ( j < n ){
    c[k++] = b[j++];
  }
}
//Traditional in-place merge sort (http://www.iti.fh-flensburg.de/lang/algorithmen/sortieren/merge/mergen.htm)
__kernel void sortNearestNeighbors(__global uint *d_DstKey, uint elements){
    uint w[MAX_NEAREST_BINS];
    uint id=get_global_id(0);
    if(id>=elements)return;
    d_DstKey += id*MAX_NEAREST_BINS;
    uint j,k;
    for( k = 1; k < MAX_NEAREST_BINS; k *= 2 )
    {
      for( j = 0; j < (MAX_NEAREST_BINS - k); j += 2 * k )
      {
        Merge(d_DstKey + j,d_DstKey + j + k, w + j, k, k);
      }
 
      for ( j = 0; j < MAX_NEAREST_BINS; j++) d_DstKey[j] = w[j];
    }
}



//Find nearest neighbors for each point
__kernel void reduceNearestNeighbors(
	__global Springl3D* capsules,
	__global CapsuleNeighbor3D* capsuleNeighbors,
	__global int* nbrs,
	int elements) {
		//triangle id
		uint id=get_global_id(0)/3;
		if(id>=elements)return;
		//vertex id
		uint n=get_global_id(0)-id*3;
		
		//Get pivot vertex	
		float4 pt=capsules[id].vertexes[n];		
		pt.w=0;
		float minDistSquared = MAX_RADIUS*MAX_RADIUS;
		
		int lastNeighborId = -1;
		int offset=0;
		
		float4 closestPoint;
		nbrs+=MAX_NEAREST_BINS*(get_global_id(0));
		capsuleNeighbors+=MAX_NEIGHBORS*(get_global_id(0));
		for(int i=0;i<MAX_NEAREST_BINS;i++) {
			id=nbrs[i];
			if(id==MAX_VALUE)break;
			if (lastNeighborId != id) {
				Springl3D capsule=capsules[id];
				//Enumerate all unique edges neighboring a point
				if (edgeDistanceSquared(pt, capsule.vertexes[0],capsule.vertexes[1], &closestPoint) < minDistSquared) {
					CapsuleNeighbor3D nbrCapsule;
					nbrCapsule.capsuleId=id;
					nbrCapsule.vertexId=0;
					capsuleNeighbors[offset]=nbrCapsule;
					offset++;
					if (offset >=MAX_NEIGHBORS) {
						return;
					}
				}
				if (edgeDistanceSquared(pt, capsule.vertexes[1],capsule.vertexes[2], &closestPoint) < minDistSquared) {
					CapsuleNeighbor3D nbrCapsule;
					nbrCapsule.capsuleId=id;
					nbrCapsule.vertexId=1;
					capsuleNeighbors[offset]=nbrCapsule;
					offset++;
					if (offset >=MAX_NEIGHBORS) {
						return;
					}
				}
				if (edgeDistanceSquared(pt, capsule.vertexes[2],capsule.vertexes[0], &closestPoint) < minDistSquared) {
					CapsuleNeighbor3D nbrCapsule;
					nbrCapsule.capsuleId=id;
					nbrCapsule.vertexId=2;
					capsuleNeighbors[offset]=nbrCapsule;
					offset++;
					if (offset >=MAX_NEIGHBORS) {
						return;
					}
				}
				lastNeighborId = id;
			}
		}
		//Indicate end of list with -1
		capsuleNeighbors[min(offset,MAX_NEAREST_BINS-1)].capsuleId=-1;
	}
kernel void initSignedLevelSet(global float* levelSet){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	levelSet[id]=-1;
}
kernel void multiplyLevelSets(global float* levelSetIn,global float* levelSetOut){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	levelSetOut[id]*=levelSetIn[id];
}
inline void getBlockRowColSlice(uint index,int* i, int* j, int* k,uint blockSize) {
	(*k)=clamp((int)((blockSize*blockSize*index)/(ROWS*COLS)),0,(int)(SLICES/blockSize-1));
	int ij=index-((*k)*ROWS * COLS)/(blockSize*blockSize);
	(*j)=clamp((int)((blockSize*ij)/ROWS),0,(int)(COLS/blockSize-1));
	(*i)=clamp((int)(ij-((*j)*ROWS)/blockSize),0,(int)(ROWS/blockSize-1));
}

inline float getBlockValue(global float* levelSet,int i,int j,int k){
	if(k<0){//if(i<0||j<0||k<0||i>=ROWS-1||j>=COLS-1||k>=SLICES-1){
		return 1E10f;
	} else {
		return levelSet[getSafeIndex(i,j,k)];
	}
}

inline float getLevelSetBlockValue(global float* levelSet,int i,int j,int k,int blockSize){
	float minValue=1E10f;
	for(int kk=0;kk<blockSize;kk++){		
		for(int jj=0;jj<blockSize;jj++){
			for(int ii=0;ii<blockSize;ii++){
				minValue=min(minValue,levelSet[getSafeIndex(i+ii,j+jj,k+kk)]);
			}
		}
	}
	return minValue;
}

inline float setBlockValue(global float* levelSet,int i,int j,int k,int blockSize,float value){
	float minValue=1;
	int offset;
	for(int kk=0;kk<blockSize;kk++){		
		for(int jj=0;jj<blockSize;jj++){
			for(int ii=0;ii<blockSize;ii++){
				offset=getHashValue(i+ii,j+jj,k+kk);
				levelSet[offset]=value;
			}
		}
	}
	return minValue;
}
inline int hasNeighbor(global float* levelSet,int i,int j,int k,int blk){
	float v011 =getBlockValue(levelSet,i - blk, j, k);
	float v121 =getBlockValue(levelSet,i, j + blk, k);
	float v101 =getBlockValue(levelSet,i, j - blk, k);
	float v211 =getBlockValue(levelSet,i + blk, j, k);
	float v110 =getBlockValue(levelSet,i, j, k - blk);
	float v112 =getBlockValue(levelSet,i, j, k + blk);
	return (
	   v011>0||
	   v121>0||
	   v101>0||
	   v211>0||
	   v110>0||
	   v112>0);
}
kernel void erodeLevelSet(global float* unsignedLevelSet,global float* levelSetIn,global float* levelSetOut,uint blockSize){
	uint id=get_global_id(0);
	int i,j,k;
	getBlockRowColSlice(id,&i,&j,&k,blockSize);
	i*=blockSize;
	j*=blockSize;
	k*=blockSize;
	if(i+blockSize>ROWS||j+blockSize>COLS||k+blockSize>SLICES)return;
	if(hasNeighbor(levelSetIn,i,j,k,blockSize)){
		float val=getLevelSetBlockValue(unsignedLevelSet,i,j,k,blockSize);
		if(val>1.5f*MAX_VEXT){		
			setBlockValue(levelSetOut,i,j,k,blockSize,1);
		}
	}
}
kernel void initDistanceField(global float* distanceField){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;	
	distanceField[id]=2*MAX_VEXT;
}
kernel void buildNBDistanceField(
	global Springl3D *capsules,
	global float* distanceField,
	uint N){
	uint id=get_global_id(0);
	if(id>=N)return;
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
					int index=getSafeIndex(i,j,k);
					float oldValue=atomic_xchg(&distanceField[index],10);
					float smaller=min(oldValue,sqrt(d2));
					atomic_xchg(&distanceField[index],smaller);
				}
			}
		}
	}
}