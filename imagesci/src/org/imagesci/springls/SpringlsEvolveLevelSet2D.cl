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
 
constant int xNeighborhood[4] = {-1, 1, 0, 0};
constant int yNeighborhood[4] = { 0, 0,-1, 1};

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
kernel void copyLevelSet(global float* srcLevelSet,global float* targetLevelSet){
	int id=get_global_id(0);
	if(id>=ROWS*COLS)return;
	targetLevelSet[id]=srcLevelSet[id];
}
kernel void diceCount(global float* srcLevelSet,global float* targetLevelSet,global int* bins,int stride){
	uint id=get_global_id(0);
	srcLevelSet+=id*stride;
	targetLevelSet+=id*stride;
	bins+=3*id;
	float val1,val2;
	bins[0]=0;
	bins[1]=0;
	bins[2]=0;
	for(int i=0;i<stride;i++){
		val1=srcLevelSet[i];
		val2=targetLevelSet[i];
		if(val1<0){
			//Volume 1
			bins[0]++;
			if(val2<0){
				//Overlap
				bins[2]++;
			} 
		} 
		if(val2<0){
			//Volume 2
			bins[1]++;
		}
	}
}
__kernel void extendDistanceField(__global float* unsignedLevelSet,uint band){
	uint id=get_global_id(0);
	int i,j;
	if(id>=ROWS*COLS)return;
	getRowCol(id,&i,&j);
	float v11;
	float v01;
	float v12;
	float v10;
	float v21;
	v11 =getImageValue(unsignedLevelSet,i, j);
	v01 =getImageValue(unsignedLevelSet,i - 1, j);
	v12 =getImageValue(unsignedLevelSet,i, j + 1);
	v10 =getImageValue(unsignedLevelSet,i, j - 1);
	v21 =getImageValue(unsignedLevelSet,i + 1, j);
	if(v11>band+MAX_VEXT){
		v11=1E10f;
		v11=min(v01,v11);
		v11=min(v12,v11);
		v11=min(v10,v11);
		v11=min(v21,v11);	
		v11+=1.0f;	
		unsignedLevelSet[getIndex(i,j)]=v11;
	} 
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
__kernel void evolveLevelSet(
	const global uint* activeList,
	__global float* unsignedLevelSet,
	__global float* signedLevelSet,
	__global float* outLevelSet,
	float weight,
	float curvWeight,
	int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	int i,j;
	getRowCol(id,&i,&j);
	float v11;
	float forceX,forceY;
	float2 grad;
	uint index=id;
	v11 =signedLevelSet[id];
	
	if(v11>0.5f||v11<-0.5f){
		outLevelSet[index]=v11;	
		return;
	}

	float v00 = getImageValue(signedLevelSet,i - 1, j - 1);
	float v01 = getImageValue(signedLevelSet,i - 1, j);
	float v10 = getImageValue(signedLevelSet,i, j - 1);
	float v21 = getImageValue(signedLevelSet,i + 1, j);
	float v20 = getImageValue(signedLevelSet,i + 1, j - 1);
	float v22 = getImageValue(signedLevelSet,i + 1, j + 1);
	float v02 = getImageValue(signedLevelSet,i - 1, j + 1);
	float v12 = getImageValue(signedLevelSet,i, j + 1);
	
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
	grad=getScaledGradientValue(unsignedLevelSet,i,j);
	forceX = weight * grad.x;
	forceY = weight * grad.y;
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
	outLevelSet[index]=v11-advection+kappa;	
}
__kernel void evolveLevelSetTopoRule(
	const global uint* activeList,
	__global float* unsignedLevelSet,
	__global float* signedLevelSet,
	__global float* outLevelSet,
	int flip,
	float weight,
	float curvWeight,
	int elements,
	int xOff,int yOff){
	int i,j;
	float v11;
	float forceX,forceY;
	float2 grad;
	uint id=get_global_id(0);
	if(id>=elements)return;
	uint index=activeList[id];
	getRowCol(index,&i,&j);
	
	if(i%2!=xOff||j%2!=yOff)return;
	float oldValue=getImageValue(outLevelSet,i, j);
	v11 =getImageValue(signedLevelSet,i, j);
	if(v11>0.5f||v11<-0.5f){
		outLevelSet[index]=v11;	
		return;
	}
	float v00 = getImageValue(signedLevelSet,i - 1, j - 1);
	float v01 = getImageValue(signedLevelSet,i - 1, j);
	float v10 = getImageValue(signedLevelSet,i, j - 1);
	float v21 = getImageValue(signedLevelSet,i + 1, j);
	float v20 = getImageValue(signedLevelSet,i + 1, j - 1);
	float v22 = getImageValue(signedLevelSet,i + 1, j + 1);
	float v02 = getImageValue(signedLevelSet,i - 1, j + 1);
	float v12 = getImageValue(signedLevelSet,i, j + 1);
	
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
		kappa = curvWeight * numer * sign(denom) * 1E5;
	}
	if (kappa < -maxCurvatureForce) {
		kappa = -maxCurvatureForce;
	} else if (kappa > maxCurvatureForce) {
		kappa = maxCurvatureForce;
	}
	
	// Level set force should be the opposite sign of advection force so it
	// moves in the direction of the force.
	grad=getScaledGradientValue(unsignedLevelSet,i,j);
	forceX = weight * grad.x;
	forceY = weight * grad.y;
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
	float newValue=v11-advection+kappa;
	int maskIndex=0;
	uint mask=0;
	__const float LEVEL_SET_TOLERANCE=1E-2f;

	if(newValue*oldValue<=0){	
		mask |=((flip*getImageValue(outLevelSet,i-1, j-1) <0) ? (1 << 0) : 0);
		mask |=((flip*getImageValue(outLevelSet,i-1, j+0) <0) ? (1 << 1) : 0);
		mask |=((flip*getImageValue(outLevelSet,i-1, j+1) <0) ? (1 << 2) : 0);
		mask |=((flip*getImageValue(outLevelSet,i+0, j-1) <0) ? (1 << 3) : 0);
		mask |=((flip*getImageValue(outLevelSet,i+0, j+0) <0) ? (1 << 4) : 0);
		mask |=((flip*getImageValue(outLevelSet,i+0, j+1) <0) ? (1 << 5) : 0);
		mask |=((flip*getImageValue(outLevelSet,i+1, j-1) <0) ? (1 << 6) : 0);
		mask |=((flip*getImageValue(outLevelSet,i+1, j+0) <0) ? (1 << 7) : 0);
		mask |=((flip*getImageValue(outLevelSet,i+1, j+1) <0) ? (1 << 8) : 0);
		if(!getBitValue(mask)){
			newValue=sign(oldValue);	
		}
	}
	
	outLevelSet[index]=	newValue;
}
/////
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
		float val=fabs(distanceField[index]);
		if(val<=MAX_DISTANCE){
			total++;
		} else {
			indexBuffer[index]=-1;
			levelset[index]=0.1f+MAX_VEXT;
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
