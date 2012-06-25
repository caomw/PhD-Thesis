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
typedef struct{
	float2 particle;
	float2 mapping;
    float2 vertexes[2];
    float phi;
    float psi; //not used, but it makes the number of values even!
} Springl2D;
typedef struct{
	int4 e1;
	int4 e2;
	float2 pt3d;
	int vid;
} EdgeSplit;
//Store capsule id and vertex id [0,1]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor2D;
typedef struct {
	float2 points[2];
} Segment;
constant int a2fVertex1Offset[4][2] = { { 0, 0 }, { 1, 0 },
			{ 1, 1 }, { 0, 1 } };
constant int a2fVertex2Offset[4][2] = { { 1, 0 }, { 1, 1 },
			{ 0, 1 }, { 0, 0 } };
constant int afSquareValue[16][4] = {
			{ 4, 4, 4, 4 },// 0000 0
			{ 3, 0, 4, 4 },// 0001 1
			{ 0, 1, 4, 4 },// 0010 2
			{ 3, 1, 4, 4 },// 0011 3
			{ 1, 2, 4, 4 },// 0100 4
			{ 0, 1, 2, 3 },// 0101 5
			{ 0, 2, 4, 4 },// 0110 6
			{ 3, 2, 4, 4 },// 0111 7
			{ 2, 3, 4, 4 },// 1000 8
			{ 2, 0, 4, 4 },// 1001 9
			{ 1, 2, 3, 0 },// 1010 10
			{ 2, 1, 4, 4 },// 1011 11
			{ 1, 3, 4, 4 },// 1100 12
			{ 1, 0, 4, 4 },// 1101 13
			{ 0, 3, 4, 4 },// 1110 14
			{ 4, 4, 4, 4 } // 1111 15
};
inline float lengthSquared(float2 v){
	return (v.x*v.x+v.y*v.y);
}
inline int clampRow(int row){
	return clamp((int)row,(int)0,(int)(ROWS-1));
}
inline int clampColumn(int col){
	return clamp((int)col,(int)0,(int)(COLS-1));
}

inline void getRowCol(uint index,int* i, int* j) {
	(*j)=index/ROWS;
	(*i)=index-(*j)*ROWS;
}
inline uint getIndex(int i, int j) {
	return (j * ROWS) + i;
}
inline float interpolate(__global float* data,float x,float y){
	int y0, x0, y1, x1;
	float dx, dy, hx, hy;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1)) {
			int r = max(min((int)x, ROWS - 1), 0);
			int c = max(min((int)y, COLS - 1), 0);
			return data[getIndex(r,c)];
	} else {
		x1 = ceil(x);
		y1 = ceil(y);
		x0 = floor(x);
		y0 = floor(y);
		dx = x - x0;
		dy = y - y0;
		// Introduce more variables to reduce computation
		hx = 1.0f - dx;
		hy = 1.0f - dy;

	 return (((data[getIndex(x0,y0)] * hx + data[getIndex(x1,y0)] * dx) * hy + (data[getIndex(x0,y1)]
					* hx + data[getIndex(x1,y1)] * dx)
					* dy));
	}
}
inline float2 getGradientValue(__global float* image,float i,float j){
	float v21 = interpolate(image, i + 1, j);
	float v12 = interpolate(image, i, j + 1);
	float v10 = interpolate(image, i, j - 1);
	float v01 = interpolate(image, i - 1, j);
	float2 grad;
	grad.x = 0.5f*(v21-v01);
	grad.y = 0.5f*(v12-v10);
	return grad;
}

__kernel void fixLabels(
		__global Springl2D* capsules,
		__global CapsuleNeighbor2D* capsuleNeighbors,
		__global float* origUnsignedLevelSet,
		__global int* labels,uint N){
	uint id=get_global_id(0);
	if(id>=N||labels[id]!=-1)return;	
	capsuleNeighbors+=2*MAX_NEIGHBORS*id;
	Springl2D capsule=capsules[id];
	Springl2D nbr;
	CapsuleNeighbor2D ci;
	float totalWeight=1.0E-6f;
	float2 particle=capsule.particle;
	
	float2 newPoint=particle;
	float minDist=4.0f;
	int newLabel=-1;
	int label=0;
	for (int i = 0; i < 2; i++) {
		for (int n=0;n<MAX_NEIGHBORS;n++) {
			ci= capsuleNeighbors[MAX_NEIGHBORS*i+n];
			if(ci.capsuleId==-1)break;
			label=labels[ci.capsuleId];
			//There may be a race condition here! But it shouldn't have a big effect on the final mapping
			if(label<0)continue;
			nbr=capsules[ci.capsuleId];
			float2 mapPoint=nbr.mapping;
			float w=distance(particle,nbr.particle);
			if(w<minDist){
				minDist=w;
				newLabel=label;
				newPoint=mapPoint;
			}
			w=1.0f/(w+1E-2f);
			totalWeight+=w;
			//newPoint+=w*mapPoint;
		}
	}
	/*
	newPoint/=totalWeight;
	const uint MAX_ITERATIONS=32;
	const float EPSILON=0.1f; 
	const float DELTA=0.5f;
	float levelSetValue=0;
	
	newPoint*=SCALE_UP;
	for(int i=0;i<MAX_ITERATIONS;i++){
		levelSetValue=interpolate(origUnsignedLevelSet,newPoint.x,newPoint.y);
		if(levelSetValue<=EPSILON)break;
		newPoint-=DELTA*levelSetValue*getGradientValue(origUnsignedLevelSet,newPoint.x,newPoint.y);
	}
	newPoint*=SCALE_DOWN;
	*/
	if(minDist<=3.99f){
		capsule.mapping=newPoint;
		capsules[id]=capsule;
		labels[id]=newLabel;
	}
}
__kernel void contractArray(__global Springl2D* inCapsules,__global int* inLabels,__global Springl2D* outCapsules,__global int* outLabels,__global int* offsets,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;	
	int offset=(id>0)?offsets[id-1]:0;
	outCapsules[offset]=inCapsules[id];
	outLabels[offset]=inLabels[id];
}
__kernel void expandArray(__global Springl2D* inCapsules,__global int* inLabels,__global Springl2D* outCapsules,__global int* outLabels,__global int* offsets,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;	
	int offset=(id>0)?offsets[id-1]:0;
	uint nextOffset=offsets[(int)min(id,elements-1)];
	outCapsules[offset]=inCapsules[id];
	outLabels[offset]=inLabels[id];
	if(nextOffset-offset>1){
		Springl2D capsule=inCapsules[offset];
		Springl2D capsule2;
	    float2 v1 = capsule.vertexes[0];
		float2 v2 = capsule.vertexes[1];
		float2 c=capsule.mapping;
		
		float2 midPoint = 0.5f*(v1+v2);
		capsule.vertexes[0] = v1;
		capsule.vertexes[1] = midPoint;
		capsule.mapping=c;
		
		capsule.particle=0.5f*(midPoint+v1);
		
		capsule2.vertexes[0] = midPoint;
		capsule2.vertexes[1] = v2;
		capsule2.mapping=c;
		capsule2.phi=capsule.phi;
		
		capsule2.particle=0.5f*(midPoint+v2);
				
		outCapsules[offset]=capsule;
		outCapsules[offset+1]=capsule;
		outLabels[offset+1]=outLabels[offset]=inLabels[id];
	}
}
__kernel void copyElements(__global Springl2D* inCapsules,__global int* inLabels,__global Springl2D* outCapsules,__global int* outLabels,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;	
	outCapsules[id]=inCapsules[id];
	outLabels[id]=inLabels[id];
}
__kernel void expandCount(__global Springl2D *capsules,__global uint *counts,uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		return;
	}
	Springl2D capsule=capsules[id];
	float2 pt1=capsule.vertexes[0];
	float2 pt2=capsule.vertexes[1];
	float len=lengthSquared(pt1-pt2);
	if (len >maxAreaThreshold * maxAreaThreshold) {
		counts[id]=2;
	} else {
		counts[id]=1;
	}
}
__kernel void countElements(__global uint *counts,__global uint *sums,uint stride,uint elements){
	uint id=get_global_id(0);
	float sum=0;
	int sz=min(elements-id*stride,stride);
	counts+=id*stride;
	for(int i=0;i<sz;i++){
		sum+=counts[i];
	}
	sums[id]=sum;	
}
__kernel void contractCount(
		__global Springl2D *capsules,
		__global uint *counts,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		return;
	}
	//IS THIS VALUE CORRECT?
	const float particleRadius = 0.05f;
	const float thresh = (float) (PARTICLE_RADIUS * 4);
	Springl2D capsule=capsules[id];
	float2 pt=capsule.particle;
	float levelSetValue=capsule.phi;
	if(fabs(levelSetValue) <= 1.25f * vExtent&&distance(capsule.vertexes[0],capsule.vertexes[1])>thresh) {
		counts[id]=1;
	} else {
		counts[id]=0;
	}
}
__kernel void contractOutliersCount(
		__global Springl2D *capsules,
		__global uint *counts,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		counts[id]=0;
		return;
	}
	Springl2D capsule=capsules[id];
	float2 pt=capsule.particle;
	float levelSetValue=capsule.phi;
	capsules[id].phi=0;
	counts[id]=(fabs(levelSetValue) >= 1.25f * vExtent)?0:1;
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
inline float fGetOffset(__global float* signedLevelSet,int2 v1, int2 v2) {
		float fValue1 = getNudgedValue(signedLevelSet,getIndex(v1.x, v1.y));
		float fValue2 = getNudgedValue(signedLevelSet,getIndex(v2.x, v2.y));
		float fDelta = fValue2 - fValue1;
		if (fabs(fDelta) ==0) {
			return 0.5f;
		}
		return clamp(-fValue1 / fDelta,0.0f,1.0f);
}

__kernel void expandGaps(
	__global Springl2D* capsules,
	__global int* labels,
	__global float* signedLevelSet,
	__global float* unsignedLevelSet,
	const global int* activeList,
	__global int* offsets,
	int sign,
	int elements,
	int activeListSize){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y;
	getRowCol(id,&x,&y);			
	int iFlagIndex = 0;
	int vx,vy;
	for (int iVertex = 0; iVertex < 4; iVertex++) {
		vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
		vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
		if (sign*signedLevelSet[getIndex(vx, vy)] > 0)iFlagIndex |= 1 << iVertex;
	}
	int count=0;
	float2 mid;
	int offset=(gid>0)?offsets[gid-1]:0;
	// Generate list of Segments
	capsules+=elements+offset;
	labels+=elements+offset;
	Springl2D cap;
	int2 pt1,pt2;
	float2 spt1,spt2;
	float fInvOffset;
	float fOffset;
	float levelSetValue;
	if(afSquareValue[iFlagIndex][0]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);
					
		mid=0.5f*(spt1+spt2);
		
		levelSetValue=interpolate(unsignedLevelSet,mid.x,mid.y);
		if(levelSetValue>1.25f*vExtent){
			if(sign<0){
				cap.vertexes[0]=SCALE_DOWN*spt1;
				cap.vertexes[1]=SCALE_DOWN*spt2;
			} else {
				cap.vertexes[0]=SCALE_DOWN*spt2;
				cap.vertexes[1]=SCALE_DOWN*spt1;
			}
			cap.particle=mid*SCALE_DOWN;
			cap.mapping=mid*SCALE_DOWN;
			cap.phi=0;
			capsules[count]=cap;
			labels[count]=-1;
			count++;
		}
	}
	
	if(afSquareValue[iFlagIndex][2]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);
					
		mid=0.5f*(spt1+spt2);
		
		levelSetValue=interpolate(unsignedLevelSet,mid.x,mid.y);
		if(levelSetValue>1.25f*vExtent){
			if(sign<0){
				cap.vertexes[0]=SCALE_DOWN*spt1;
				cap.vertexes[1]=SCALE_DOWN*spt2;
			} else {
				cap.vertexes[0]=SCALE_DOWN*spt2;
				cap.vertexes[1]=SCALE_DOWN*spt1;
			}
			cap.particle=mid*SCALE_DOWN;
			cap.mapping=mid*SCALE_DOWN;
			cap.phi=0;
			capsules[count]=cap;
			labels[count]=-1;
		}
	}
}
__kernel void fillGapCount(
	const global float* signedLevelSet,
	const global float* unsignedLevelSet,
	const global int* activeList,
	__global int* counts,
	int sign,
	int activeListSize) {
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y;
	getRowCol(id,&x,&y);			
	int iFlagIndex = 0;
	int vx,vy;
	int2 pt1,pt2;
	float2 spt1,spt2;
	float fInvOffset;
	float fOffset;
	for (int iVertex = 0; iVertex < 4; iVertex++) {
		vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
		vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
		if (sign*signedLevelSet[getIndex(vx, vy)] > 0)iFlagIndex |= 1 << iVertex;
	}
	int count=0;
	float2 mid;
	float levelSetValue;
	if(afSquareValue[iFlagIndex][0]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);
					
		mid=0.5f*(spt1+spt2);
		
		levelSetValue=interpolate(unsignedLevelSet,mid.x,mid.y);
		if(levelSetValue>1.25f*vExtent){
			count++;
		}
	}
	
	if(afSquareValue[iFlagIndex][2]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);
					
		mid=0.5f*(spt1+spt2);
		
		levelSetValue=interpolate(unsignedLevelSet,mid.x,mid.y);
		if(levelSetValue>1.25f*vExtent){
			count++;
		}
	}
	counts[gid]=count;
}

__kernel void isoSurfCount(__global float* signedLevelSet,__global int* counts,int sign) {
	uint id=get_global_id(0);
	int x,y;
	getRowCol(id,&x,&y);			
	int iFlagIndex = 0;
	int vx,vy;
	for (int iVertex = 0; iVertex < 4; iVertex++) {
		vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
		vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
		if (sign*signedLevelSet[getIndex(vx, vy)] > 0)iFlagIndex |= 1 << iVertex;
	}
	int count=0;
	if(afSquareValue[iFlagIndex][0]<4){
		count+=2;
	}
	if(afSquareValue[iFlagIndex][2]<4){
		count+=2;
	}
	counts[id]=count;
}

__kernel void isoSurfGen(__global float2* vertexes,__global float* signedLevelSet,__global int* offsets,int sign){
	uint id=get_global_id(0);
	int x,y;
	getRowCol(id,&x,&y);			
	int iFlagIndex = 0;
	int vx,vy;
	for (int iVertex = 0; iVertex < 4; iVertex++) {
		vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
		vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
		if (sign*signedLevelSet[getIndex(vx, vy)] > 0)iFlagIndex |= 1 << iVertex;
	}
	int count=0;
	// Generate list of Segments
	vertexes+=offsets[id];
	float2 spt1,spt2;
	int2 pt1,pt2;
	float fInvOffset;
	float fOffset;
	
	if(afSquareValue[iFlagIndex][0]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);
					
		if(sign<0){	
			vertexes[0]=spt1;
			vertexes[1]=spt2;
		} else {
			vertexes[0]=spt2;
			vertexes[1]=spt1;		
		}
	}
	
	if(afSquareValue[iFlagIndex][2]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][1]);
		fOffset = fGetOffset(signedLevelSet,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);
					
		if(sign<0){	
			vertexes[2]=spt1;
			vertexes[3]=spt2;
		} else {
			vertexes[2]=spt2;
			vertexes[3]=spt1;		
		}
	}
}