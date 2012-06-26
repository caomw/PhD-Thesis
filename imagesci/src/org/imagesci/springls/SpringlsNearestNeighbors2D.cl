/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
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
typedef struct{
	float2 particle;
	float2 mapping;
    float2 vertexes[2];
    float phi;
    float psi; //not used, but it makes the number of values even!
} Springl2D;
typedef struct{
	int2 e1;
	int2 e2;
	float2 pt2d;
	int vid;
} EdgeSplit;
//Store capsule id and vertex id [0,1]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor2D;

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
inline uint getSafeIndex(int i, int j) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return (c * ROWS) + r;
}
//Get index into volume
inline uint getHashValue(int i, int j) {
	return (j * ROWS) + i;
}
inline void getRowCol(uint index,int* i, int* j) {
	(*j)=index/ROWS;
	(*i)=index-(*j)*ROWS;
}
//Distance squared between two points
inline float distanceSquaredPoint(float2 pt1,float2 pt2){
	float2 v=pt1-pt2;
	return (v.x*v.x+v.y*v.y);
}


//Distance between point and triangle edge
//Implementation from geometric tools (http://www.geometrictools.com)
float edgeDistanceSquared(float2 pt, float2 pt1, float2 pt2,float2* lastClosestSegmentPoint) {
	float2 dir=pt2-pt1;
	float len=length(dir);
	dir=normalize(dir);
	float2 diff=pt-pt1;
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
float distanceSquared(float2 p, Springl2D* capsule,float2* closestPoint) {
	return edgeDistanceSquared(p,capsule->vertexes[0],capsule->vertexes[1],closestPoint);
}
__kernel void splatBBoxCount(
__global Springl2D *capsules,__global uint *counts,__global uint* mask,uint N){
		uint id=get_global_id(0);
		if(id>=N)return;
		float2 lowerPoint;
		float2 upperPoint;
		Springl2D cap=capsules[id];
		//Find axis aligned bounding box
		lowerPoint=min(cap.vertexes[0],cap.vertexes[1]);
		upperPoint=max(cap.vertexes[0],cap.vertexes[1]);
		__const float2 ZERO=(float2)(0,0);
		__const float2 IMAGE_MAX=(float2)(ROWS-1,COLS-1);
		//Compute lower index
		lowerPoint*=SCALE_UP;
		float2 lower=max(ZERO,floor(lowerPoint));
		int lowerRow=(int)lower.x;
		int lowerCol=(int)lower.y;
		
		//Compute upper index
		upperPoint*=SCALE_UP;
		float2 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		int upperRow=(int)upper.x;
		int upperCol=(int)upper.y;

		float2 dim=upper-lower;
		float vol=dim.x*dim.y;
		if(vol>32){
			counts[id]=0;
			mask[id]=0;	
		}
		uint count=0;		
		float2 ret;
		float2 pt;
		int index=0;
		int iFlagIndex=0;
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
					pt = (float2)(
							i * SCALE_DOWN,
							j * SCALE_DOWN);
				float d2 = distanceSquared(pt,&cap,&ret);
				//To minimize the size of the map, compute which voxels are near the target triangle and store them in a bit mask
				if(d2<=4*(MAX_VEXT)*(MAX_VEXT)){
					count++;
					iFlagIndex|=(1<<index);
				}
				index++;
			}
		}
		mask[id]=iFlagIndex;
		//Record number of bins needed for each capsule
		counts[id]=count;
}
__kernel void splatBBoxCountMesh(
__global float2* vertexes,__global uint *counts,__global uint* mask,uint N){
		uint id=get_global_id(0);
		if(id>=N)return;
		float2 lowerPoint;
		float2 upperPoint;
		vertexes+=2*id;
		//Find axis aligned bounding box
		lowerPoint=min(vertexes[0],vertexes[1]);
		upperPoint=max(vertexes[0],vertexes[1]);
		
		__const float2 ZERO=(float2)(0,0);
		__const float2 IMAGE_MAX=(float2)(ROWS-1,COLS-1);
		//Compute lower index
		float2 lower=max(ZERO,floor(lowerPoint));
		int lowerRow=(int)lower.x;
		int lowerCol=(int)lower.y;
		
		//Compute upper index
		float2 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		int upperRow=(int)upper.x;
		int upperCol=(int)upper.y;

		float2 dim=upper-lower;
		float vol=dim.x*dim.y;
		if(vol>32){
			counts[id]=0;
			mask[id]=0;	
		}
		uint count=0;		
		float2 ret;
		float2 pt;
		int index=0;
		float2 v0=vertexes[0];
		float2 v1=vertexes[1];
		uint iFlagIndex=0;
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
					pt = (float2)(i ,j);
				float d2 = edgeDistanceSquared(pt,v0,v1,&ret);
				//To minimize the size of the map, compute which voxels are near the target triangle and store them in a bit mask
				if(d2<=4*(SCALE_UP*MAX_VEXT)*(SCALE_UP*MAX_VEXT)){
					count++;
					iFlagIndex|=(1<<index);
				}
				index++;
			}
		}
		mask[id]=iFlagIndex;
		//Record number of bins needed for each capsule
		counts[id]=count;
}
__kernel void initIndexMapNB(__global int* indexMap){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS)return;
	//initialize index map
	indexMap[id]=(-1);
}
//Initialize the index map
__kernel void initIndexMap(__global int* indexMap){
	uint id=get_global_id(0);
	//initialize index map
	indexMap[id]=(-1);
}
//Create list of indexes into map
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
//Compute level set value in reduction phase
__kernel void reduceLevelSet(
		__global int* activeList,
		__global int* spatialLookup,
		__global Springl2D* capsules,
		__global float* imageMat,
		uint activeListSize){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int i,j;
	float2 pt;
	float2 ret;
	Springl2D cap;
	getRowCol(id,&i,&j);
	spatialLookup+=gid*MAX_BIN_SIZE;	
	pt = (float2)(
		i * SCALE_DOWN,
		j * SCALE_DOWN);
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
//Compute level set value in reduction phase
__kernel void reduceLevelSetMesh(
		__global int* indexMap,
		__global uint* keys,
		__global uint* values,
		__global float2* vertexes,
		__global float* imageMat,
		uint N){
	uint id=get_global_id(0);
	int i,j;
	getRowCol(id,&i,&j);
	float2 pt;
	float2 ret;
	int index;
	uint hashValue = id;
	int startIndex = indexMap[hashValue];	
	
	pt = (float2)(i ,j);
	float value = SCALE_UP*SCALE_UP*(0.1f+MAX_VEXT)*(0.1f+MAX_VEXT);
	index = startIndex;
	
	//label = -1;
	float2 v0,v1,v2;
	do {
		//If at the start of a new key section, break;
		if (keys[index] != hashValue)break;
		id=values[index];
		v0=vertexes[2*id];
		v1=vertexes[2*id+1];
		//Compute distance squared between point and triangle
		float d2 = edgeDistanceSquared(pt,v0,v1,&ret);
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
	int i,j;	
	uint id=get_global_id(0);
	uint index=0;
	getRowCol(id,&i,&j);
	int sign=1;
	bool borderRegion=false;
	int count=0;
	for(j=0;j<COLS;j++){
		index=getHashValue(i,j);
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
	for(j=COLS-1;j>=0;j--){
		index=getHashValue(i,j);
		if(!borderRegion&&unsignedLevelSet[index]<MAX_VEXT){
			borderRegion=true;
		}
		if(!borderRegion&&signedLevelSet[index]<0)signedLevelSet[index]=unsignedLevelSet[index]*sign;
	}
	
	
}
//Splat the bounding boxes to the map
__kernel void splatBBox(
	__global Springl2D *capsules,
	__global uint *offsets,
	__global uint* mask,
	__global uint *keys,
	__global uint *values,uint N,uint mapSize){

		uint id=get_global_id(0);
		if(id>=N)return;
		float2 lowerPoint;
		float2 upperPoint;
		Springl2D cap=capsules[id];
		int offset=(id>0)?offsets[id-1]:0;
		lowerPoint=min(cap.vertexes[0],cap.vertexes[1]);
		upperPoint=max(cap.vertexes[0],cap.vertexes[1]);
		
		
		__const float2 ZERO=(float2)(0,0);
		__const float2 IMAGE_MAX=(float2)(ROWS-1,COLS-1);
		
		lowerPoint*=SCALE_UP;
		float2 lower=max(ZERO,floor(lowerPoint));
		
		uint lowerRow=(uint)lower.x;
		uint lowerCol=(uint)lower.y;

		upperPoint*=SCALE_UP;
		float2 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		uint upperRow=(uint)upper.x;
		uint upperCol=(uint)upper.y;
		
		float2 dim=upper-lower;
		float vol=dim.x*dim.y;
		if(vol>32){
			return;
		}
		
		uint iFlagIndex=mask[id];
		int index=0;
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
				//Use the bit mask to test if point is near the triangle.
				if(((iFlagIndex >> index) & 1)){
					if(offset>=mapSize)return;
					uint hash = getHashValue(i, j);
					values[offset]=id;
					keys[offset]=hash;
					offset++;
					//Map size exceeded! Fail gracefully.

				}
				index++;
			}
		}
}
//Splat the bounding boxes to the map
__kernel void splatBBoxMesh(
	__global float2* vertexes,
	__global uint *offsets,
	__global uint* mask,
	__global uint *keys,
	__global uint *values,uint N,uint mapSize){
		
		uint id=get_global_id(0);
		if(id>=N)return;
		float2 lowerPoint ;
		float2 upperPoint;
		vertexes+=2*id;
		uint offset=offsets[id];
		lowerPoint=min(vertexes[0],vertexes[1]);
		upperPoint=max(vertexes[0],vertexes[1]);
		
		__const float2 ZERO=(float2)(0,0);
		__const float2 IMAGE_MAX=(float2)(ROWS-1,COLS-1);
		
		float2 lower=max(ZERO,floor(lowerPoint));
		
		uint lowerRow=(uint)lower.x;
		uint lowerCol=(uint)lower.y;

		float2 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		uint upperRow=(uint)upper.x;
		uint upperCol=(uint)upper.y;
		
		float2 dim=upper-lower;
		float vol=dim.x*dim.y;
		if(vol>32){
			return;
		}
		
		uint iFlagIndex=mask[id];
		int index=0;
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
				//Use the bit mask to test if point is near the triangle.
				if(((iFlagIndex >> index) & 1)){
					if(offset>=mapSize)return;
					uint hash = getHashValue(i, j);
					values[offset]=id;
					keys[offset]=hash;
					offset++;
					//Map size exceeded! Fail gracefully.

				}
				index++;
			}
		}
}

__kernel void mapNearestNeighbors(
		__global int* nbrs,
		__global int* indexMap,
		__global int* spatialLookup,
		__global Springl2D* capsules,
		int elements) {
		//Triangle index
		int gid=get_global_id(0);
		int id=gid/2;
		if(id>=elements)return;
		//Triangle vertex index without modulus!
		int n=gid-id*2;
		Springl2D cap = capsules[id];
		float2 pt = cap.vertexes[n];
		nbrs+=MAX_NEAREST_BINS*gid;
		
		float2 lowerPoint=pt-MAX_RADIUS;
		float2 upperPoint=pt+MAX_RADIUS;
		
		__const float2 ZERO=(float2)(0,0);
		__const float2 IMAGE_MAX=(float2)(ROWS-1,COLS-1);
		
		lowerPoint*=SCALE_UP;
		float2 lower=max(ZERO,floor(lowerPoint));
		
		int lowerRow=(int)lower.x;
		int lowerCol=(int)lower.y;

		upperPoint*=SCALE_UP;
		float2 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
		
		int upperRow=(int)upper.x;
		int upperCol=(int)upper.y;
		
		int offset=0;
		int cid,startIndex;
		uint hashValue;
		//Enumerate all triangles that lie in bounding sphere around point
		
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
				hashValue = getSafeIndex(i, j);
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
	__global Springl2D* capsules,
	__global CapsuleNeighbor2D* capsuleNeighbors,
	__global int* nbrs,
	int elements) {
		//triangle id
		uint id=get_global_id(0)/2;
		if(id>=elements)return;
		//vertex id
		uint n=get_global_id(0)-id*2;
		
		//Get pivot vertex	
		float2 pt=capsules[id].vertexes[n];		
		float minDistSquared = MAX_RADIUS*MAX_RADIUS;
		
		int lastNeighborId = -1;
		int offset=0;
		
		float2 closestPoint;
		nbrs+=MAX_NEAREST_BINS*(get_global_id(0));
		capsuleNeighbors+=MAX_NEIGHBORS*(get_global_id(0));
		for(int i=0;i<MAX_NEAREST_BINS;i++) {
			id=nbrs[i];
			if(id==MAX_VALUE)break;
			if (lastNeighborId != id) {
				Springl2D capsule=capsules[id];
				//Enumerate all unique edges neighboring a point
				if (edgeDistanceSquared(pt, capsule.vertexes[0],capsule.vertexes[1], &closestPoint) < minDistSquared) {
					CapsuleNeighbor2D nbrCapsule;
					nbrCapsule.capsuleId=id;
					nbrCapsule.vertexId=0;
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
	levelSet[id]=-1;
}
kernel void multiplyLevelSets(global float* levelSetIn,global float* levelSetOut){
	uint id=get_global_id(0);
	levelSetOut[id]*=levelSetIn[id];
}
kernel void buildLUT(
	global Springl2D *capsules,
	global int* indexMap,
	global int* spatialLookUp,
	uint N,
	uint activeListSize){
	uint id=get_global_id(0);
	if(id>=N)return;
	float2 lowerPoint = (float2)(1E10f, 1E10f);
	float2 upperPoint = (float2)(-1E10f, -1E10f);
	Springl2D cap=capsules[id];
	//#pragma unroll
	//Find axis aligned bounding box
	for (uint i=0;i<2;i++) {
		float2 v=cap.vertexes[i];
		lowerPoint=min(v,lowerPoint);
		upperPoint=max(v,upperPoint);
	}
	__const float2 ZERO=(float2)(0,0);
	__const float2 IMAGE_MAX=(float2)(ROWS-1,COLS-1);
	//Compute lower index
	lowerPoint*=SCALE_UP;
	float2 lower=max(ZERO,floor(lowerPoint));
	int lowerRow=(int)lower.x;
	int lowerCol=(int)lower.y;
	
	//Compute upper index
	upperPoint*=SCALE_UP;
	float2 upper=min(IMAGE_MAX,ceil(upperPoint)+1);
	int upperRow=(int)upper.x;
	int upperCol=(int)upper.y;
	float2 dim=upper-lower;
	uint count=0;		
	float2 ret;
	float2 pt;
	for (int j = lowerCol; j < upperCol; j++) {
		for (int i = lowerRow; i < upperRow; i++) {
			pt = (float2)(
						i * SCALE_DOWN,
						j * SCALE_DOWN);
			float d2 = distanceSquared(pt,&cap,&ret);
			//To minimize the size of the map, compute which voxels are near the target triangle and store them in a bit mask
			if(d2<=4*(MAX_VEXT)*(MAX_VEXT)){
				int offsetIndex=MAX_BIN_SIZE*indexMap[getSafeIndex(i,j)];
				int currentOffset=atomic_add(&spatialLookUp[offsetIndex],1);
				if(currentOffset<MAX_BIN_SIZE){
					spatialLookUp[offsetIndex+currentOffset]=id;
				}
			}
		}
	}
}
__kernel void updateIndexMap(__global int* indexMap,global uint *activeList,int activeListSize){
	uint id=get_global_id(0);
	if(id>=activeListSize)return;
	indexMap[activeList[id]]=id;
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