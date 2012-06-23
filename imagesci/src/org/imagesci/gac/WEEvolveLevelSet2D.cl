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
#define INF_FORCE 1E10f
constant int xNeighborhood[4] = {-1, 1, 0, 0};
constant int yNeighborhood[4] = { 0, 0,-1, 1};
constant int xShift[4] = { 0, 0, 1, 1 };
constant int yShift[4] = { 0, 1, 0, 1 };
inline uint getSafeIndex(int i, int j) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return (c * ROWS) + r;
}
inline int getIndex(int i, int j) {
	return (j * ROWS) + i;
}
inline void getRowCol(uint index,int* i, int* j) {
	(*j)=index/ROWS;
	(*i)=index-(*j)*ROWS;
}
inline void getRowCol2(uint index,int* i, int* j) {
	(*j)=2*index/ROWS;
	(*i)=index-(*j)*ROWS/2;
}
inline float getImageValue(__global float* image,int i,int j){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return image[getIndex(r,c)];
}

inline float2 getScaledGradientValue(__global float* image,int i,int j){
	float v21 = getImageValue(image, i + 1, j);
	float v12 = getImageValue(image, i, j + 1);
	float v10 = getImageValue(image, i, j - 1);
	float v01 = getImageValue(image, i - 1, j);
	float v11 = getImageValue(image, i, j);
	float2 grad;
	grad.x = 0.5f*(v21-v01);
	grad.y = 0.5f*(v12-v10);
	float len=max(1E-6f,length(grad));
	//NOT TRUE GRADIENT! THIS IS REALLY THE DIRECTION OF THE GRADIENT SCALED BY THE LEVEL SET VALUE.
	//THIS WAS DONE TO IMPROVE CONVERGENCE
	return -(v11*grad/len);
}

inline bool getBitValue(int i){
	const char lut4_8[64] = { 123, -13, -5, -13, -69,
			51, -69, 51, -128, -13, -128, -13, 0, 51, 0, 51, -128, -13, -128,
			-13, -69, -52, -69, -52, -128, -13, -128, -13, -69, -52, -69, -52,
			-128, 0, -128, 0, -69, 51, -69, 51, 0, 0, 0, 0, 0, 51, 0, 51, -128,
			-13, -128, -13, -69, -52, -69, -52, -128, -13, -128, -13, -69, -52,
			-69, -52, 123, -13, -5, -13, -69, 51, -69, 51, -128, -13, -128,
			-13, 0, 51, 0, 51, -128, -13, -128, -13, -69, -52, -69, -52, -128,
			-13, -128, -13, -69, -52, -69, -52, -128, 0, -128, 0, -69, 51, -69,
			51, 0, 0, 0, 0, 0, 51, 0, 51, -128, -13, -128, -13, -69, -52, -69,
			-52, -128, -13, -128, -13, -69, -52, -69, -52 };
	return (((uchar)(lut4_8[63-(i>>3)]) & (1 << (i % 8))) > 0);
}			
__kernel void addToVolume(__global float* unsignedLevelSet,float value){
	int id=get_global_id(0);
	unsignedLevelSet[id]+=value;
}


inline float2 getVectorImageValue(__global float2* image,uint i,uint j){
	return image[getSafeIndex(i,j)];
}

kernel void copyLevelSet(global float* srcLevelSet,global float* targetLevelSet){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS)return;
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
	int i,j;
	getRowCol(id,&i,&j);
	float v11 =oldSignedLevelSet[id];
	float2 grad;
	if(v11>0.5f||v11<-0.5f){
		deltaLevelSet[gid]=0;	
		return;
	}

	float v00 = getImageValue(oldSignedLevelSet,i - 1, j - 1);
	float v01 = getImageValue(oldSignedLevelSet,i - 1, j);
	float v10 = getImageValue(oldSignedLevelSet,i, j - 1);
	float v21 = getImageValue(oldSignedLevelSet,i + 1, j);
	float v20 = getImageValue(oldSignedLevelSet,i + 1, j - 1);
	float v22 = getImageValue(oldSignedLevelSet,i + 1, j + 1);
	float v02 = getImageValue(oldSignedLevelSet,i - 1, j + 1);
	float v12 = getImageValue(oldSignedLevelSet,i, j + 1);
	
	float DxNeg = v11 - v01;
	float DxPos = v21 - v11;
	float DyNeg = v11 - v10;
	float DyPos = v12 - v11;
	
	float DxNegMin = min(DxNeg, 0.0f);
	float DxNegMax = max(DxNeg, 0.0f);
	float DxPosMin = min(DxPos, 0.0f);
	float DxPosMax = max(DxPos, 0.0f);
	float DyNegMin = min(DyNeg, 0.0f);
	float DyNegMax = max(DyNeg, 0.0f);
	float DyPosMin = min(DyPos, 0.0f);
	float DyPosMax = max(DyPos, 0.0f);
	float GradientSqrPos = DxNegMax * DxNegMax + DxPosMin * DxPosMin+ DyNegMax * DyNegMax + DyPosMin * DyPosMin;
	float GradientSqrNeg = DxPosMax * DxPosMax + DxNegMin * DxNegMin+ DyPosMax * DyPosMax + DyNegMin * DyNegMin;
	
	
	float DxCtr = 0.5f * (v21 - v01);
	float DyCtr = 0.5f * (v12 - v10);
	
	float DxxCtr = v21 - v11 - v11 + v01;
	float DyyCtr = v12 - v11 - v11 + v10;
	float DxyCtr = (v22 - v02 - v20 + v00) * 0.25f;

	float numer = 0.5f * (DyCtr * DyCtr * DxxCtr - 2 * DxCtr * DyCtr
				* DxyCtr + DxCtr * DxCtr * DyyCtr);
	float denom = DxCtr * DxCtr + DyCtr * DyCtr;
	float kappa=0;
	
	const float maxCurvatureForce = 10.0f;
	if (fabs(denom) > 1E-5f) {
		kappa = curvWeight * numer / denom;
	} else {
		kappa = curvWeight * numer * sign(denom) * 1E5f;
	}
	if (kappa < -maxCurvatureForce) {
		kappa = -maxCurvatureForce;
	} else if (kappa > maxCurvatureForce) {
		kappa = maxCurvatureForce;
	}
	float force = pressureWeight * pressureForce[id];
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
	int i,j;
	getRowCol(id,&i,&j);
	float v11 =oldSignedLevelSet[id];
	float2 grad;
	if(v11>0.5f||v11<-0.5f){
		deltaLevelSet[gid]=0;	
		return;
	}

	float v00 = getImageValue(oldSignedLevelSet,i - 1, j - 1);
	float v01 = getImageValue(oldSignedLevelSet,i - 1, j);
	float v10 = getImageValue(oldSignedLevelSet,i, j - 1);
	float v21 = getImageValue(oldSignedLevelSet,i + 1, j);
	float v20 = getImageValue(oldSignedLevelSet,i + 1, j - 1);
	float v22 = getImageValue(oldSignedLevelSet,i + 1, j + 1);
	float v02 = getImageValue(oldSignedLevelSet,i - 1, j + 1);
	float v12 = getImageValue(oldSignedLevelSet,i, j + 1);
	
	float DxNeg = v11 - v01;
	float DxPos = v21 - v11;
	float DyNeg = v11 - v10;
	float DyPos = v12 - v11;
	
	float DxNegMin = min(DxNeg, 0.0f);
	float DxNegMax = max(DxNeg, 0.0f);
	float DxPosMin = min(DxPos, 0.0f);
	float DxPosMax = max(DxPos, 0.0f);
	float DyNegMin = min(DyNeg, 0.0f);
	float DyNegMax = max(DyNeg, 0.0f);
	float DyPosMin = min(DyPos, 0.0f);
	float DyPosMax = max(DyPos, 0.0f);
	float GradientSqrPos = DxNegMax * DxNegMax + DxPosMin * DxPosMin+ DyNegMax * DyNegMax + DyPosMin * DyPosMin;
	float GradientSqrNeg = DxPosMax * DxPosMax + DxNegMin * DxNegMin+ DyPosMax * DyPosMax + DyNegMin * DyNegMin;
	
	
	float DxCtr = 0.5f * (v21 - v01);
	float DyCtr = 0.5f * (v12 - v10);
	
	float DxxCtr = v21 - v11 - v11 + v01;
	float DyyCtr = v12 - v11 - v11 + v10;
	float DxyCtr = (v22 - v02 - v20 + v00) * 0.25f;

	float numer = 0.5f * (DyCtr * DyCtr * DxxCtr - 2 * DxCtr * DyCtr
				* DxyCtr + DxCtr * DxCtr * DyyCtr);
	float denom = DxCtr * DxCtr + DyCtr * DyCtr;
	float kappa=0;
	
	const float maxCurvatureForce = 10.0f;
	if (fabs(denom) > 1E-5f) {
		kappa = curvWeight * numer / denom;
	} else {
		kappa = curvWeight * numer * sign(denom) * 1E5f;
	}
	if (kappa < -maxCurvatureForce) {
		kappa = -maxCurvatureForce;
	} else if (kappa > maxCurvatureForce) {
		kappa = maxCurvatureForce;
	}
	float force = pressureWeight * pressureForce[id];
	float pressure=0;
	if (force > 0) {
		pressure = -force * sqrt(GradientSqrPos);
	} else if (force < 0) {
		pressure = -force * sqrt(GradientSqrNeg);
	} 
	
	// Level set force should be the opposite sign of advection force so it
	// moves in the direction of the force.

	vecField+=2*id;
	float forceX = advectWeight * vecField[0];
	float forceY = advectWeight * vecField[1];
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
	int i,j;
	getRowCol(id,&i,&j);
	float v11 =oldSignedLevelSet[id];
	float2 grad;
	if(v11>0.5f||v11<-0.5f){
		deltaLevelSet[gid]=0;	
		return;
	}

	float v00 = getImageValue(oldSignedLevelSet,i - 1, j - 1);
	float v01 = getImageValue(oldSignedLevelSet,i - 1, j);
	float v10 = getImageValue(oldSignedLevelSet,i, j - 1);
	float v21 = getImageValue(oldSignedLevelSet,i + 1, j);
	float v20 = getImageValue(oldSignedLevelSet,i + 1, j - 1);
	float v22 = getImageValue(oldSignedLevelSet,i + 1, j + 1);
	float v02 = getImageValue(oldSignedLevelSet,i - 1, j + 1);
	float v12 = getImageValue(oldSignedLevelSet,i, j + 1);
	
	float DxNeg = v11 - v01;
	float DxPos = v21 - v11;
	float DyNeg = v11 - v10;
	float DyPos = v12 - v11;
		
	float DxCtr = 0.5f * (v21 - v01);
	float DyCtr = 0.5f * (v12 - v10);
	
	float DxxCtr = v21 - v11 - v11 + v01;
	float DyyCtr = v12 - v11 - v11 + v10;
	float DxyCtr = (v22 - v02 - v20 + v00) * 0.25f;

	float numer = 0.5f * (DyCtr * DyCtr * DxxCtr - 2 * DxCtr * DyCtr
				* DxyCtr + DxCtr * DxCtr * DyyCtr);
	float denom = DxCtr * DxCtr + DyCtr * DyCtr;
	float kappa=0;
	
	const float maxCurvatureForce = 10.0f;
	if (fabs(denom) > 1E-5f) {
		kappa = curvWeight * numer / denom;
	} else {
		kappa = curvWeight * numer * sign(denom) * 1E5f;
	}
	if (kappa < -maxCurvatureForce) {
		kappa = -maxCurvatureForce;
	} else if (kappa > maxCurvatureForce) {
		kappa = maxCurvatureForce;
	}
	
	// Level set force should be the opposite sign of advection force so it
	// moves in the direction of the force.

	vecField+=2*id;
	float forceX = advectWeight * vecField[0];
	float forceY = advectWeight * vecField[1];
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
	
	deltaLevelSet[gid]=-advection+kappa;
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
	if(fabs(oldSignedLevelSet[id])>0.5f)return;
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
	float timeStep,
	int elements,int offset){
#else
kernel void applyForcesTopoRule(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global float* deltaLevelSet,
	global float* signedLevelSet,
	const global float* maxTmpBuffer,
	int elements,int offset){
	float timeStep=maxTmpBuffer[0];
#endif			
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	float v11=oldSignedLevelSet[id];
	if(fabs(v11)>0.5f){
		signedLevelSet[id]=	v11;	
		return;
	}
	float delta;
#if CLAMP_SPEED
		delta=timeStep*clamp(deltaLevelSet[gid],-1.0f,1.0f);
#else
		delta=timeStep*deltaLevelSet[gid];
#endif
	int i,j;
	getRowCol(id,&i,&j);
	int xOff=xShift[offset];
	int yOff=yShift[offset];
	if(i%2!=xOff||j%2!=yOff)return;
	float oldValue=oldSignedLevelSet[id];
	float newValue=oldValue+delta;
	int mask=0;
	if(newValue*oldValue<=0){	
		mask |=((getImageValue(signedLevelSet,i-1, j-1) <0) ? (1 << 0) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+0) <0) ? (1 << 1) : 0);
		mask |=((getImageValue(signedLevelSet,i-1, j+1) <0) ? (1 << 2) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j-1) <0) ? (1 << 3) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+0) <0) ? (1 << 4) : 0);
		mask |=((getImageValue(signedLevelSet,i+0, j+1) <0) ? (1 << 5) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j-1) <0) ? (1 << 6) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+0) <0) ? (1 << 7) : 0);
		mask |=((getImageValue(signedLevelSet,i+1, j+1) <0) ? (1 << 8) : 0);
		if(!getBitValue(mask)){
			newValue=sign(oldValue);	
		}
	}
	signedLevelSet[id]=	newValue;	
}

__kernel void plugLevelSet(const global uint* activeList,__global float* signedLevelSet,int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	int i,j;
	getRowCol(id,&i,&j);
	float v11;
	float v01;
	float v12;
	float v10;
	float v21;
	
	v11 =getImageValue(signedLevelSet,i, j);
	float sgn=sign(v11);
	v11=sgn*v11;
	v01=sgn*getImageValue(signedLevelSet,i - 1, j);
	v12=sgn*getImageValue(signedLevelSet,i, j + 1);
	v10=sgn*getImageValue(signedLevelSet,i, j - 1);
	v21=sgn*getImageValue(signedLevelSet,i + 1, j);
	if(v11>0&&v11<0.5f&&v01>0&&v12>0&&v10>0&&v21>0){
		signedLevelSet[id]=sgn*MAX_DISTANCE;
	} 
}

#define NBOFFSET (ROWS)
kernel void countActiveList(global int* offsets,global float* oldDistanceField,global float* distanceField){
	uint j=get_global_id(0);
	if(j>=COLS)return;
	distanceField+=j*ROWS;
	oldDistanceField+=j*ROWS;
	
	int total=0;
	for(int i=0;i<ROWS;i++){	
		if(fabs(oldDistanceField[i])<=MAX_DISTANCE){				
			total++;
		}
		if(fabs(distanceField[i])>=NBOFFSET){
			distanceField[i]=oldDistanceField[i];
		}
	}
	offsets[j]=total;
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
	uint j=get_global_id(0);
	if(j>=COLS)return;
	distanceField+=j*ROWS;
	if(j>0)activeListBuffer+=offsets[j-1];
	int total=0;
	for(int i=0;i<ROWS;i++){	
		if(fabs(distanceField[i])<=MAX_DISTANCE){			
			activeListBuffer[total++]=(j *ROWS)+i;
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
	int i,j;
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int total=0;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int n=0;n<sz;n++){
		int index=activeList[n];
		getRowCol(index,&i,&j);
		int index2=getSafeIndex(i+xOff,j+yOff);
		float val1=fabs(distanceField[index]);
		float val2=fabs(distanceField[index2]);
		if(val1<=MAX_DISTANCE-1&&
			val2>=MAX_DISTANCE&&val2<NBOFFSET){
				distanceField[index2]=NBOFFSET+offset;
				total++;
		}
	}
	offsetList[4*id+offset]=total;
}

kernel void expandActiveList(
	global int* offsets,
	global int* activeList,
	global float* oldDistanceField,
	global float* distanceField,
	int elements,int offset){
	int xOff=xNeighborhood[offset];
	int yOff=yNeighborhood[offset];
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	int off1=id*STRIDE;
	int off2=elements;
	if(id>0||offset>0)off2+=offsets[(4*id+offset)-1];
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	int i,j;
	for(int n=0;n<sz;n++){
		int index=activeList[off1++];
		getRowCol(index,&i,&j);
		int index2=getSafeIndex(i+xOff,j+yOff);
		float val1=distanceField[index];
		float val2=distanceField[index2];
		if(fabs(val1)<=MAX_DISTANCE-1&&val2==NBOFFSET+offset){
			activeList[off2++]=index2;
			getRowCol(index2,&i,&j);
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
__kernel void updateDistanceField(
		const global uint* activeList,
		global float* oldLevelSet,
		global float* signedLevelSet,
		int band,
		int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	int i,j;
	getRowCol(id,&i,&j);
	float v11;
	float v01;
	float v12;
	float v10;
	float v21;
	float activeLevelSet=getImageValue(oldLevelSet,i, j);
	if(activeLevelSet<=0.5f&&activeLevelSet>=-0.5f){
		return;
	}
	v11 =getImageValue(signedLevelSet,i, j);
	float oldVal=v11;
	v01 =getImageValue(signedLevelSet,i - 1, j);
	v12 =getImageValue(signedLevelSet,i, j + 1);
	v10 =getImageValue(signedLevelSet,i, j - 1);
	v21 =getImageValue(signedLevelSet,i + 1, j);
	if(v11<-band+0.5f){
		v11=-1E10f;
		v11=(v01>1)?v11:max(v01,v11);
		v11=(v12>1)?v11:max(v12,v11);
		v11=(v10>1)?v11:max(v10,v11);
		v11=(v21>1)?v11:max(v21,v11);
		v11-=1.0f;		
	} else if(v11>band-0.5f){
		v11=1E10f;
		v11=(v01<-1)?v11:min(v01,v11);
		v11=(v12<-1)?v11:min(v12,v11);
		v11=(v10<-1)?v11:min(v10,v11);
		v11=(v21<-1)?v11:min(v21,v11);
		v11+=1.0f;	
	}
	
	if(oldVal*v11>0){
		signedLevelSet[getIndex(i,j)]=v11;		
	} else {
		signedLevelSet[getIndex(i,j)]=oldVal;
	}
}	