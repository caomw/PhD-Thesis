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
#define INF_FORCE 1E10f
constant int xShift[8] = { 0, 0, 1, 1, 0, 0, 1, 1 };
constant int yShift[8] = { 0, 1, 0, 1, 0, 1, 0, 1 };
constant int zShift[8] = { 0, 0, 0, 0, 1, 1, 1, 1 };

constant int xNeighborhood[6] = {-1, 1, 0, 0, 0, 0 };
constant int yNeighborhood[6] = { 0, 0,-1, 1, 0, 0 };
constant int zNeighborhood[6] = { 0, 0, 0, 0,-1, 1 };

inline uint getSafeIndex(int i, int j, int k) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	return (s * (ROWS * COLS)) + (c * ROWS) + r;
}
inline uint getIndex(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
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
inline float getImageValue(global float* image,int i,int j,int k){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	return image[getIndex(r,c,s)];
}

inline float4 getVectorImageValue(__global float4* image,uint i,uint j,uint k){
	uint r = clamp(i,  (uint)0, (uint)(ROWS - 1));
	uint c = clamp(j, (uint)0, (uint)(COLS - 1));
	uint s = clamp(k, (uint)0, (uint)(SLICES - 1));
	return image[getIndex(r,c,s)];
}

kernel void copyLevelSet(global float* srcLevelSet,global float* targetLevelSet){
	uint id=get_global_id(0);
	targetLevelSet[id]=srcLevelSet[id];
}
kernel void copyBuffers(
	const global uint* activeList,
	global float* oldLevelSet,
	global float* levelSet,
	int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	oldLevelSet[id]=levelSet[id];	
	
}

kernel void maxImageValue(
	global float* deltaLevelSet,
	global float* maxBuffer,
	int elements){
	float maxValue=0;
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	deltaLevelSet+=id*STRIDE;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz;i++){
		float delta=deltaLevelSet[i];
		maxValue=max(fabs(delta),maxValue);
	}
	maxBuffer[id]=maxValue;
}
kernel void maxTimeStep(global float* maxBuffer,int len){
	float maxValue=0;
	uint id=get_global_id(0);
	if(id>0)return;
	for(int i=0;i<len;i++){
		maxValue=max(maxBuffer[i],maxValue);
	}
	const float maxSpeed = 0.999f;
	float timeStep=0.5f * ((maxValue > maxSpeed) ? (maxSpeed / maxValue): maxSpeed);
	maxBuffer[0]=timeStep;
}
kernel void pressureSpeedKernel(
	const global uint* activeList,
	const global float* pressureForce,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	float pressureWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(fabs(oldSignedLevelSet[id])>0.5f){
		deltaLevelSet[gid]=0;
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	float4 grad;
	int offset;
	float pressureValue= pressureForce[id];
	float v111 = getImageValue(oldSignedLevelSet,i, j,k);
	float v010 = getImageValue(oldSignedLevelSet,i - 1, j, k - 1);
	float v120 = getImageValue(oldSignedLevelSet,i, j + 1, k - 1);
	float v110 = getImageValue(oldSignedLevelSet,i, j, k - 1);
	float v100 = getImageValue(oldSignedLevelSet,i, j - 1, k - 1);
	float v210 = getImageValue(oldSignedLevelSet,i + 1, j, k - 1);		
	float v001 = getImageValue(oldSignedLevelSet,i - 1, j - 1, k);
	float v011 = getImageValue(oldSignedLevelSet,i - 1, j, k);
	float v101 = getImageValue(oldSignedLevelSet,i, j - 1, k);
	float v211 = getImageValue(oldSignedLevelSet,i + 1, j, k);
	float v201 = getImageValue(oldSignedLevelSet,i + 1, j - 1, k);
	float v221 = getImageValue(oldSignedLevelSet,i + 1, j + 1, k);
	float v021 = getImageValue(oldSignedLevelSet,i - 1, j + 1, k);
	float v121 = getImageValue(oldSignedLevelSet,i, j + 1, k);
	float v012 = getImageValue(oldSignedLevelSet,i - 1, j, k + 1);
	float v122 = getImageValue(oldSignedLevelSet,i, j + 1, k + 1);
	float v112 = getImageValue(oldSignedLevelSet,i, j, k + 1);
	float v102 = getImageValue(oldSignedLevelSet,i, j - 1, k + 1);
	float v212 = getImageValue(oldSignedLevelSet,i + 1, j, k + 1);
	
	float DxNeg = v111 - v011;
	float DxPos = v211 - v111;
	float DyNeg = v111 - v101;
	float DyPos = v121 - v111;
	float DzNeg = v111 - v110;
	float DzPos = v112 - v111;
	
	float DxNegMin = min(DxNeg, 0.0f);
	float DxNegMax = max(DxNeg, 0.0f);
	float DxPosMin = min(DxPos, 0.0f);
	float DxPosMax = max(DxPos, 0.0f);
	float DyNegMin = min(DyNeg, 0.0f);
	float DyNegMax = max(DyNeg, 0.0f);
	float DyPosMin = min(DyPos, 0.0f);
	float DyPosMax = max(DyPos, 0.0f);
	float DzNegMin = min(DzNeg, 0.0f);
	float DzNegMax = max(DzNeg, 0.0f);
	float DzPosMin = min(DzPos, 0.0f);
	float DzPosMax = max(DzPos, 0.0f);
	
	float GradientSqrPos = 
			  DxNegMax * DxNegMax + DxPosMin * DxPosMin
			+ DyNegMax * DyNegMax + DyPosMin * DyPosMin
			+ DzNegMax * DzNegMax + DzPosMin * DzPosMin;
	float GradientSqrNeg = 
		      DxPosMax * DxPosMax + DxNegMin * DxNegMin
			+ DyPosMax * DyPosMax + DyNegMin * DyNegMin
			+ DzPosMax * DzPosMax + DzNegMin * DzNegMin;
	
	
	float DxCtr = 0.5f * (v211 - v011);
	float DyCtr = 0.5f * (v121 - v101);
	float DzCtr = 0.5f * (v112 - v110);
	float DxxCtr = v211 - v111 - v111 + v011;
	float DyyCtr = v121 - v111 - v111 + v101;
	float DzzCtr = v112 - v111 - v111 + v110;
	float DxyCtr = (v221 - v021 - v201 + v001) * 0.25f;
	float DxzCtr = (v212 - v012 - v210 + v010) * 0.25f;
	float DyzCtr = (v122 - v102 - v120 + v100) * 0.25f;
	
	float numer = 0.5f * (
				(DyyCtr+DzzCtr)*DxCtr*DxCtr
				+(DxxCtr+DzzCtr)*DyCtr*DyCtr
				+(DxxCtr+DyyCtr)*DzCtr*DzCtr
				-2*DxCtr*DyCtr*DxyCtr
				-2*DxCtr*DzCtr*DxzCtr
				-2*DyCtr*DzCtr*DyzCtr);
	float denom = DxCtr * DxCtr + DyCtr * DyCtr+ DzCtr * DzCtr;
	float kappa=0;
	const float maxCurvatureForce = 10.0f;
	if (fabs(denom) > 1E-5f) {
		kappa = curvWeight * numer / denom;
	} else {
		kappa = curvWeight * numer * sign(denom) * 1E5;
	}
	if (kappa < -maxCurvatureForce) {
		kappa = -maxCurvatureForce;
	} else if (kappa > maxCurvatureForce) {
		kappa = maxCurvatureForce;
	}
	float force = pressureWeight *pressureValue;
	float pressure=0;
	if (force > 0) {
		pressure = -force * sqrt(GradientSqrPos);
	} else if (force < 0) {
		pressure = -force * sqrt(GradientSqrNeg);
	} 
	deltaLevelSet[gid]=kappa+pressure;	
}
kernel void pressureVecFieldSpeedKernel(
	const global uint* activeList,
	const global float* pressureForce,
	const global float* vecField,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	float pressureWeight,
	float advectWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(fabs(oldSignedLevelSet[id])>0.5f){
		deltaLevelSet[gid]=0;
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	float4 grad;
	int offset;
	vecField+=3*id;
	forceX = advectWeight * vecField[0];
	forceY = advectWeight * vecField[1];
	forceZ = advectWeight * vecField[2];
	float pressureValue= pressureForce[id];
	float v111 = getImageValue(oldSignedLevelSet,i, j,k);
	float v010 = getImageValue(oldSignedLevelSet,i - 1, j, k - 1);
	float v120 = getImageValue(oldSignedLevelSet,i, j + 1, k - 1);
	float v110 = getImageValue(oldSignedLevelSet,i, j, k - 1);
	float v100 = getImageValue(oldSignedLevelSet,i, j - 1, k - 1);
	float v210 = getImageValue(oldSignedLevelSet,i + 1, j, k - 1);		
	float v001 = getImageValue(oldSignedLevelSet,i - 1, j - 1, k);
	float v011 = getImageValue(oldSignedLevelSet,i - 1, j, k);
	float v101 = getImageValue(oldSignedLevelSet,i, j - 1, k);
	float v211 = getImageValue(oldSignedLevelSet,i + 1, j, k);
	float v201 = getImageValue(oldSignedLevelSet,i + 1, j - 1, k);
	float v221 = getImageValue(oldSignedLevelSet,i + 1, j + 1, k);
	float v021 = getImageValue(oldSignedLevelSet,i - 1, j + 1, k);
	float v121 = getImageValue(oldSignedLevelSet,i, j + 1, k);
	float v012 = getImageValue(oldSignedLevelSet,i - 1, j, k + 1);
	float v122 = getImageValue(oldSignedLevelSet,i, j + 1, k + 1);
	float v112 = getImageValue(oldSignedLevelSet,i, j, k + 1);
	float v102 = getImageValue(oldSignedLevelSet,i, j - 1, k + 1);
	float v212 = getImageValue(oldSignedLevelSet,i + 1, j, k + 1);
	
	float DxNeg = v111 - v011;
	float DxPos = v211 - v111;
	float DyNeg = v111 - v101;
	float DyPos = v121 - v111;
	float DzNeg = v111 - v110;
	float DzPos = v112 - v111;
	
	float DxNegMin = min(DxNeg, 0.0f);
	float DxNegMax = max(DxNeg, 0.0f);
	float DxPosMin = min(DxPos, 0.0f);
	float DxPosMax = max(DxPos, 0.0f);
	float DyNegMin = min(DyNeg, 0.0f);
	float DyNegMax = max(DyNeg, 0.0f);
	float DyPosMin = min(DyPos, 0.0f);
	float DyPosMax = max(DyPos, 0.0f);
	float DzNegMin = min(DzNeg, 0.0f);
	float DzNegMax = max(DzNeg, 0.0f);
	float DzPosMin = min(DzPos, 0.0f);
	float DzPosMax = max(DzPos, 0.0f);
	
	float GradientSqrPos = 
			  DxNegMax * DxNegMax + DxPosMin * DxPosMin
			+ DyNegMax * DyNegMax + DyPosMin * DyPosMin
			+ DzNegMax * DzNegMax + DzPosMin * DzPosMin;
	float GradientSqrNeg = 
		      DxPosMax * DxPosMax + DxNegMin * DxNegMin
			+ DyPosMax * DyPosMax + DyNegMin * DyNegMin
			+ DzPosMax * DzPosMax + DzNegMin * DzNegMin;
	
	
	float DxCtr = 0.5f * (v211 - v011);
	float DyCtr = 0.5f * (v121 - v101);
	float DzCtr = 0.5f * (v112 - v110);
	float DxxCtr = v211 - v111 - v111 + v011;
	float DyyCtr = v121 - v111 - v111 + v101;
	float DzzCtr = v112 - v111 - v111 + v110;
	float DxyCtr = (v221 - v021 - v201 + v001) * 0.25f;
	float DxzCtr = (v212 - v012 - v210 + v010) * 0.25f;
	float DyzCtr = (v122 - v102 - v120 + v100) * 0.25f;
	
	float numer = 0.5f * (
				(DyyCtr+DzzCtr)*DxCtr*DxCtr
				+(DxxCtr+DzzCtr)*DyCtr*DyCtr
				+(DxxCtr+DyyCtr)*DzCtr*DzCtr
				-2*DxCtr*DyCtr*DxyCtr
				-2*DxCtr*DzCtr*DxzCtr
				-2*DyCtr*DzCtr*DyzCtr);
	float denom = DxCtr * DxCtr + DyCtr * DyCtr+ DzCtr * DzCtr;
	float kappa=0;
	const float maxCurvatureForce = 10.0f;
	if (fabs(denom) > 1E-5f) {
		kappa = curvWeight * numer / denom;
	} else {
		kappa = curvWeight * numer * sign(denom) * 1E5;
	}
	if (kappa < -maxCurvatureForce) {
		kappa = -maxCurvatureForce;
	} else if (kappa > maxCurvatureForce) {
		kappa = maxCurvatureForce;
	}
	
	// Level set force should be the opposite sign of advection force so it
	// moves in the direction of the force.


	float advection = 0;
	
	// Dot product force with upwind gradient
	if (forceX > 0) {
		advection = forceX * DxNeg;
	} else if (forceX < 0) {
		advection = forceX * DxPos;
	}
	if (forceY > 0) {
		advection += forceY * DyNeg;
	} else if (forceY < 0) {
		advection += forceY * DyPos;
	}
	if (forceZ > 0) {
		advection += forceZ * DzNeg;
	} else if (forceZ < 0) {
		advection += forceZ * DzPos;
	}
	
	// Force should be negative to move level set outwards if pressure is
	// positive
	float force = pressureWeight * pressureValue;
	float pressure=0;
	if (force > 0) {
		pressure = -force * sqrt(GradientSqrPos);
	} else if (force < 0) {
		pressure = -force * sqrt(GradientSqrNeg);
	} 
	deltaLevelSet[gid]=-advection+kappa+pressure;
}
kernel void vecFieldSpeedKernel(
	const global uint* activeList,
	const global float* vecField,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	float advectWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(fabs(oldSignedLevelSet[id])>0.5f){
		deltaLevelSet[gid]=0;
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	float4 grad;
	int offset;
	vecField+=3*id;
	forceX = advectWeight * vecField[0];
	forceY = advectWeight * vecField[1];
	forceZ = advectWeight * vecField[3];
	float v111 = getImageValue(oldSignedLevelSet,i, j,k);
	float v010 = getImageValue(oldSignedLevelSet,i - 1, j, k - 1);
	float v120 = getImageValue(oldSignedLevelSet,i, j + 1, k - 1);
	float v110 = getImageValue(oldSignedLevelSet,i, j, k - 1);
	float v100 = getImageValue(oldSignedLevelSet,i, j - 1, k - 1);
	float v210 = getImageValue(oldSignedLevelSet,i + 1, j, k - 1);		
	float v001 = getImageValue(oldSignedLevelSet,i - 1, j - 1, k);
	float v011 = getImageValue(oldSignedLevelSet,i - 1, j, k);
	float v101 = getImageValue(oldSignedLevelSet,i, j - 1, k);
	float v211 = getImageValue(oldSignedLevelSet,i + 1, j, k);
	float v201 = getImageValue(oldSignedLevelSet,i + 1, j - 1, k);
	float v221 = getImageValue(oldSignedLevelSet,i + 1, j + 1, k);
	float v021 = getImageValue(oldSignedLevelSet,i - 1, j + 1, k);
	float v121 = getImageValue(oldSignedLevelSet,i, j + 1, k);
	float v012 = getImageValue(oldSignedLevelSet,i - 1, j, k + 1);
	float v122 = getImageValue(oldSignedLevelSet,i, j + 1, k + 1);
	float v112 = getImageValue(oldSignedLevelSet,i, j, k + 1);
	float v102 = getImageValue(oldSignedLevelSet,i, j - 1, k + 1);
	float v212 = getImageValue(oldSignedLevelSet,i + 1, j, k + 1);
	
	float DxNeg = v111 - v011;
	float DxPos = v211 - v111;
	float DyNeg = v111 - v101;
	float DyPos = v121 - v111;
	float DzNeg = v111 - v110;
	float DzPos = v112 - v111;
	
	float DxCtr = 0.5f * (v211 - v011);
	float DyCtr = 0.5f * (v121 - v101);
	float DzCtr = 0.5f * (v112 - v110);
	float DxxCtr = v211 - v111 - v111 + v011;
	float DyyCtr = v121 - v111 - v111 + v101;
	float DzzCtr = v112 - v111 - v111 + v110;
	float DxyCtr = (v221 - v021 - v201 + v001) * 0.25f;
	float DxzCtr = (v212 - v012 - v210 + v010) * 0.25f;
	float DyzCtr = (v122 - v102 - v120 + v100) * 0.25f;
	
	float numer = 0.5f * (
				(DyyCtr+DzzCtr)*DxCtr*DxCtr
				+(DxxCtr+DzzCtr)*DyCtr*DyCtr
				+(DxxCtr+DyyCtr)*DzCtr*DzCtr
				-2*DxCtr*DyCtr*DxyCtr
				-2*DxCtr*DzCtr*DxzCtr
				-2*DyCtr*DzCtr*DyzCtr);
	float denom = DxCtr * DxCtr + DyCtr * DyCtr+ DzCtr * DzCtr;
	float kappa=0;
	const float maxCurvatureForce = 10.0f;
	if (fabs(denom) > 1E-5f) {
		kappa = curvWeight * numer / denom;
	} else {
		kappa = curvWeight * numer * sign(denom) * 1E5;
	}
	if (kappa < -maxCurvatureForce) {
		kappa = -maxCurvatureForce;
	} else if (kappa > maxCurvatureForce) {
		kappa = maxCurvatureForce;
	}
	
	// Level set force should be the opposite sign of advection force so it
	// moves in the direction of the force.

	float advection = 0;
	
	// Dot product force with upwind gradient
	if (forceX > 0) {
		advection = forceX * DxNeg;
	} else if (forceX < 0) {
		advection = forceX * DxPos;
	}
	if (forceY > 0) {
		advection += forceY * DyNeg;
	} else if (forceY < 0) {
		advection += forceY * DyPos;
	}
	if (forceZ > 0) {
		advection += forceZ * DzNeg;
	} else if (forceZ < 0) {
		advection += forceZ * DzPos;
	}
	
	deltaLevelSet[gid]=-advection+kappa;	
}
inline bool getBitValue(global uchar* bytes,int i){
	__const int LEN=(2 << 24);
	return ((bytes[LEN-(i>>3)-1] & (1 << (i % 8))) > 0);
}
#if CLAMP_SPEED
kernel void applyForces(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	global float* signedLevelSet,
	float timeStep,
	int elements){
#else
kernel void applyForces(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	global float* signedLevelSet,
	const global float* maxTmpBuffer,
	int elements){
	float timeStep=maxTmpBuffer[0];
#endif			
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	float v111=oldSignedLevelSet[id];
	if(fabs(v111)>0.5f){
		signedLevelSet[id]=v111;
		return;
	}
	float delta;
#if CLAMP_SPEED
		delta=timeStep*clamp(deltaLevelSet[gid],-1.0f,1.0f);
#else
		delta=timeStep*deltaLevelSet[gid];
#endif
	signedLevelSet[id] = oldSignedLevelSet[id]+delta;	
}
#if CLAMP_SPEED
kernel void applyForcesTopoRule(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	global float* signedLevelSet,
	global uchar* topoLUT,
	float timeStep,
	int elements,int offset){
#else
kernel void applyForcesTopoRule(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	global float* signedLevelSet,
	const global float* maxTmpBuffer,
	global uchar* topoLUT,
	int elements,int offset){
	float timeStep=maxTmpBuffer[0];
#endif			
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	float v111=oldSignedLevelSet[id];
	if(fabs(v111)>0.5f){
		signedLevelSet[id]=v111;
		return;
	}
	float delta;
#if CLAMP_SPEED
		delta=timeStep*clamp(deltaLevelSet[gid],-1.0f,1.0f);
#else
		delta=timeStep*deltaLevelSet[gid];
#endif
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	int xOff=xShift[offset];
	int yOff=yShift[offset];
	int zOff=zShift[offset];
	if(i%2!=xOff||j%2!=yOff||k%2!=zOff)return;
	float oldValue=oldSignedLevelSet[id];
	float newValue=oldValue+delta;
	int mask=0;
	if(newValue*oldValue<=0){	
		mask |=((getImageValue(signedLevelSet,i-1, j-1,k-1 ) <0) ? (1 << 0) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j-1,k+0 ) <0) ? (1 << 1) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j-1,k+1 ) <0) ? (1 << 2) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+0,k-1 ) <0) ? (1 << 3) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+0,k+0 ) <0) ? (1 << 4) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+0,k+1 ) <0) ? (1 << 5) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+1,k-1 ) <0) ? (1 << 6) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+1,k+0 ) <0) ? (1 << 7) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+1,k+1 ) <0) ? (1 << 8) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j-1,k-1 ) <0) ? (1 << 9) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j-1,k+0 ) <0) ? (1 <<10) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j-1,k+1 ) <0) ? (1 <<11) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+0,k-1 ) <0) ? (1 <<12) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+0,k+0 ) <0) ? (1 <<13) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+0,k+1 ) <0) ? (1 <<14) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+1,k-1 ) <0) ? (1 <<15) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+1,k+0 ) <0) ? (1 <<16) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+1,k+1 ) <0) ? (1 <<17) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j-1,k-1 ) <0) ? (1 <<18) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j-1,k+0 ) <0) ? (1 <<19) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j-1,k+1 ) <0) ? (1 <<20) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+0,k-1 ) <0) ? (1 <<21) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+0,k+0 ) <0) ? (1 <<22) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+0,k+1 ) <0) ? (1 <<23) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+1,k-1 ) <0) ? (1 <<24) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+1,k+0 ) <0) ? (1 <<25) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+1,k+1 ) <0) ? (1 <<26) : 0);
		if(!getBitValue(topoLUT,mask)){
			newValue=sign(oldValue);	
		}
	}
	signedLevelSet[id]=	newValue;	
}

__kernel void plugLevelSet(global int* activeList,__global float* signedLevelSet,int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float v11 = signedLevelSet[id];
	float activeValues[6];
	activeValues[0]=getImageValue(signedLevelSet,i+1,j,k);
	activeValues[1]=getImageValue(signedLevelSet,i-1,j,k);
	activeValues[2]=getImageValue(signedLevelSet,i,j+1,k);
	activeValues[3]=getImageValue(signedLevelSet,i,j-1,k);
	activeValues[4]=getImageValue(signedLevelSet,i,j,k+1);
	activeValues[5]=getImageValue(signedLevelSet,i,j,k-1);
	if(v11>=-0.5f&&v11<=0){
		for(int index=0;index<6;index++){
			if(activeValues[index]>=0)return;
		}
		signedLevelSet[id]=MAX_DISTANCE;
	}
}

#define NBOFFSET (ROWS)
kernel void countActiveList(global int* offsets,global float* oldDistanceField,global float* distanceField){
	uint k=get_global_id(0);
	if(k>=SLICES)return;
	distanceField+=k*ROWS*COLS;
	oldDistanceField+=k*ROWS*COLS;
	
	int total=0;
	for(int ij=0;ij<ROWS*COLS;ij++){	
		if(fabs(oldDistanceField[ij])<=MAX_DISTANCE){				
			total++;
		}
		if(fabs(distanceField[ij])>=NBOFFSET){
			distanceField[ij]=oldDistanceField[ij];
		}
	}
	offsets[k]=total;
}
kernel void prefixScanList(global int* offsets,global int* maxBuffer,int sz){
	uint id=get_global_id(0);
	if(id!=0)return;
	int total=0;
	for(int n=0;n<sz;n++){
		total+=offsets[n];
		offsets[n]=total;
	}
	maxBuffer[0]=total;
}

kernel void buildActiveList(global int* offsets,global int* activeListBuffer,global float* distanceField){
	uint k=get_global_id(0);
	if(k>=SLICES)return;
	distanceField+=k*ROWS*COLS;
	if(k>0)activeListBuffer+=offsets[k-1];
	int total=0;
	for(int ij=0;ij<ROWS*COLS;ij++){	
		if(fabs(distanceField[ij])<=MAX_DISTANCE){			
			activeListBuffer[total++]=(k * (ROWS * COLS))+ij;
		}
	}
}
kernel void addCountActiveList(
	global int* offsetList,
	const global uint* activeList,
	global float* distanceField,
	int elements,int offset){
	int xOff=xNeighborhood[offset];
	int yOff=yNeighborhood[offset];
	int zOff=zNeighborhood[offset];
	int i,j,k;
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int total=0;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int n=0;n<sz;n++){
		int index=activeList[n];
		getRowColSlice(index,&i,&j,&k);
		int index2=getSafeIndex(i+xOff,j+yOff,k+zOff);
		float val1=fabs(distanceField[index]);
		float val2=fabs(distanceField[index2]);
		if(val1<=MAX_DISTANCE-1&&
			val2>=MAX_DISTANCE&&val2<NBOFFSET){
				distanceField[index2]=NBOFFSET+offset;
				total++;
		}
	}
	offsetList[6*id+offset]=total;
}

kernel void expandActiveList(
	global int* offsets,
	global int* activeList,
	global float* oldDistanceField,
	global float* distanceField,
	int elements,int offset){
	int xOff=xNeighborhood[offset];
	int yOff=yNeighborhood[offset];
	int zOff=zNeighborhood[offset];
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	int off1=id*STRIDE;
	int off2=elements;
	if(id>0||offset>0)off2+=offsets[(6*id+offset)-1];
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	int i,j,k;
	for(int n=0;n<sz;n++){
		int index=activeList[off1++];
		getRowColSlice(index,&i,&j,&k);
		int index2=getSafeIndex(i+xOff,j+yOff,k+zOff);
		float val1=distanceField[index];
		float val2=distanceField[index2];
		if(fabs(val1)<=MAX_DISTANCE-1&&val2==NBOFFSET+offset){
			activeList[off2++]=index2;
			getRowColSlice(index2,&i,&j,&k);
			val2=oldDistanceField[index2];
			oldDistanceField[index2]=sign(val2)*MAX_DISTANCE;
			distanceField[index2]=sign(val2)*MAX_DISTANCE;				
		}
	}
}
kernel void deleteCountActiveList(
	global int* offsetList,
	const global uint* activeList,
	global float* distanceField,
	int elements){
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int total=0;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz;i++){
		int index=activeList[i];
		float val=fabs(distanceField[index]);
		if(val<=MAX_DISTANCE){
			total++;
		} 
	}
	offsetList[id]=total;
}
kernel void deleteCountActiveListHistory(
	global int* offsetList,
	const global uint* activeList,
	global float* distanceField,
	global char* history,
	int elements){
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int total=0;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz;i++){
		int index=activeList[i];
		float val=fabs(distanceField[index]);
		char times=history[index];
		if(val<=MAX_DISTANCE&&times==1){
			total++;
		} 
	}
	offsetList[id]=total;
}
kernel void diffImageLabels( global float* levelset,global char* history){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	char lab=(char)((levelset[id]<=0)?1:0);
	history[id]=(lab!=history[id])?1:0;	
}
kernel void rememberImageLabels(global float* levelset,global char* history){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	history[id]=((levelset[id]<=0)?1:0);
}
kernel void dilateLabels(global int* activeList,global char* history,int elements,int offset){
	int xOff=xShift[offset];
	int yOff=yShift[offset];
	int zOff=zShift[offset];
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	if(i%2!=xOff||j%2!=yOff||k%2!=zOff)return;	
	char v211 = history[getSafeIndex(i + 1, j, k)];
	char v121 = history[getSafeIndex(i, j + 1, k)];
	char v101 = history[getSafeIndex(i, j - 1, k)];
	char v011 = history[getSafeIndex(i - 1, j, k)];
	char v110 = history[getSafeIndex(i, j, k - 1)];
	char v112 = history[getSafeIndex(i, j, k + 1)];
	char v111 = history[id];
	history[id]=(char)clamp(v211+v121+v101+v011+v110+v112+v111,0,1);
}

kernel void compactActiveList(
	global int* offsets,
	global int* activeList,
	global int* outActiveList,
	global float* oldDistanceField,
	global float* distanceField,
	int elements){
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));	
	if(id>0)outActiveList+=offsets[id-1];
	int off=0;
	for(int i=0;i<sz;i++){	
		int index=activeList[i];
		float val=oldDistanceField[index];
		if(fabs(val)<=MAX_DISTANCE){
			outActiveList[off++]=index;
		} else {
			distanceField[index]=sign(val)*(MAX_DISTANCE+0.5f);
			oldDistanceField[index]=sign(val)*(MAX_DISTANCE+0.5f);
		}
	}
}
kernel void compactActiveListHistory(
	global int* offsets,
	global int* activeList,
	global int* outActiveList,
	global float* oldDistanceField,
	global float* distanceField,
	global char* history,
	int elements){
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));	
	if(id>0)outActiveList+=offsets[id-1];
	int off=0;
	for(int i=0;i<sz;i++){	
		int index=activeList[i];
		float val=oldDistanceField[index];
		char times=history[index];
		if(times==1){
			if(fabs(val)<=MAX_DISTANCE){
				outActiveList[off++]=index;
			} else {
				distanceField[index]=sign(val)*(MAX_DISTANCE+0.5f);
				oldDistanceField[index]=sign(val)*(MAX_DISTANCE+0.5f);
			}
		}
	}
}
kernel void updateDistanceField(
		const global uint* activeList,
		global float* oldLevelSet,
		global float* signedLevelSet,
		int band,
		int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float v111;
	float v011;
	float v121;
	float v101;
	float v211;
	float v110;
	float v112;		
	float activeLevelSet=oldLevelSet[id];
	if(fabs(activeLevelSet)<0.5f){
		return;
	}
	v111 =signedLevelSet[id];
	float oldVal=v111;
	v011 =getImageValue(signedLevelSet,i - 1, j, k);
	v121 =getImageValue(signedLevelSet,i, j + 1, k);
	v101 =getImageValue(signedLevelSet,i, j - 1, k);
	v211 =getImageValue(signedLevelSet,i + 1, j, k);
	v110 =getImageValue(signedLevelSet,i, j, k - 1);
	v112 =getImageValue(signedLevelSet,i, j, k + 1);
	if(v111<-band+0.5f){
		v111=-(MAX_DISTANCE+0.5f);
		v111=(v011>1)?v111:max(v011,v111);
		v111=(v121>1)?v111:max(v121,v111);
		v111=(v101>1)?v111:max(v101,v111);
		v111=(v211>1)?v111:max(v211,v111);
		v111=(v110>1)?v111:max(v110,v111);
		v111=(v112>1)?v111:max(v112,v111);
		v111-=1.0f;		
	} else if(v111>band-0.5f){
		v111=(MAX_DISTANCE+0.5f);
		v111=(v011<-1)?v111:min(v011,v111);
		v111=(v121<-1)?v111:min(v121,v111);
		v111=(v101<-1)?v111:min(v101,v111);
		v111=(v211<-1)?v111:min(v211,v111);
		v111=(v110<-1)?v111:min(v110,v111);
		v111=(v112<-1)?v111:min(v112,v111);	
		v111+=1.0f;	
	}
	
	if(oldVal*v111>0){
		signedLevelSet[id]=v111;		
	} else {
		signedLevelSet[id]=oldVal;
	}
}