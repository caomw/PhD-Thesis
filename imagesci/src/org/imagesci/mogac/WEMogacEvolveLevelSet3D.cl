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

#define MAX_HISTORY 126
constant int xShift[8] = { 0, 0, 1, 1, 0, 0, 1, 1 };
constant int yShift[8] = { 0, 1, 0, 1, 0, 1, 0, 1 };
constant int zShift[8] = { 0, 0, 0, 0, 1, 1, 1, 1 };

constant int xNeighborhood[6] = {-1, 1, 0, 0, 0, 0 };
constant int yNeighborhood[6] = { 0, 0,-1, 1, 0, 0 };
constant int zNeighborhood[6] = { 0, 0, 0, 0,-1, 1 };

int getOffset(int label,global int* labelMask){
	#if CONTAINS_OVERLAPS
		for(int l=0;l<NUM_LABELS;l++){
			if(labelMask[l]==label){
				return l;
			}
		}
		return 0;
	#else 
		return label;
	#endif
}
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
inline float getImageValue(global float* image,int i,int j,int k){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	return image[getIndex(r,c,s)];
}

inline float getLabelValue(global int* image,int i,int j,int k){
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
inline float4 getScaledGradientValue(__global float* image,int i,int j,int k){
	float v211 = getImageValue(image, i + 1, j, k);
	float v121 = getImageValue(image, i, j + 1, k);
	float v101 = getImageValue(image, i, j - 1, k);
	float v011 = getImageValue(image, i - 1, j, k);
	float v110 = getImageValue(image, i, j, k - 1);
	float v112 = getImageValue(image, i, j, k + 1);
	float v111 = getImageValue(image, i, j, k);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w = 0;
	float len=max(1E-6f,length(grad));
	//NOT TRUE GRADIENT! THIS IS REALLY THE DIRECTION OF THE GRADIENT SCALED BY THE LEVEL SET VALUE.
	//THIS WAS DONE TO IMPROVE CONVERGENCE
	return -(v111*grad/len);
}
kernel void copyLevelSet(global float* srcLevelSet,global float* targetLevelSet){
	uint id=get_global_id(0);
	targetLevelSet[id]=srcLevelSet[id];
}
kernel void copyBuffers(
	const global uint* activeList,
	global float* oldLevelSet,
	global int* oldLabels,
	global float* levelSet,
	global int* labels,
	int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	oldLevelSet[id]=levelSet[id];	
	oldLabels[id]=labels[id];
}

kernel void rememberImageLabels(global int* labels,global int* history){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	history[id]=labels[id];
}
kernel void diffImageLabels( global int* labels,global int* history){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	history[id]=(labels[id]!=history[id])?1:0;	
}
kernel void dilateLabels(global int* activeList,global int* history,int elements,int offset){
	int xOff=xShift[offset];
	int yOff=yShift[offset];
	int zOff=zShift[offset];
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	if(i%2!=xOff||j%2!=yOff||k%2!=zOff)return;	
	int v211 = history[getSafeIndex(i + 1, j, k)];
	int v121 = history[getSafeIndex(i, j + 1, k)];
	int v101 = history[getSafeIndex(i, j - 1, k)];
	int v011 = history[getSafeIndex(i - 1, j, k)];
	int v110 = history[getSafeIndex(i, j, k - 1)];
	int v112 = history[getSafeIndex(i, j, k + 1)];
	int v111 = history[id];
	//Not thread-safe! It's okay, no real noticable difference, and this version is much faster
	history[id]=clamp(v211+v121+v101+v011+v110+v112+v111,0,1);
}
kernel void extendDistanceField(
	const global uint* activeList,
	global float* oldLevelSet,
	global float* unsignedLevelSet,
	global int* labels,uint band,int elements){
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
	if(oldLevelSet[id]<=0.5f)return;	
	
	int label=labels[id];
	v111 =getLevelSetValue(unsignedLevelSet,labels,label,i, j, k);
	v011 =getLevelSetValue(unsignedLevelSet,labels,label,i - 1, j, k);
	v121 =getLevelSetValue(unsignedLevelSet,labels,label,i, j + 1, k);
	v101 =getLevelSetValue(unsignedLevelSet,labels,label,i, j - 1, k);
	v211 =getLevelSetValue(unsignedLevelSet,labels,label,i + 1, j, k);
	v110 =getLevelSetValue(unsignedLevelSet,labels,label,i, j, k - 1);
	v112 =getLevelSetValue(unsignedLevelSet,labels,label,i, j, k + 1);
	
	if(unsignedLevelSet[id]>band-0.5f){
		v111=1E10f;
		v111=min(fabs(v011-1),v111);
		v111=min(fabs(v121-1),v111);
		v111=min(fabs(v101-1),v111);
		v111=min(fabs(v211-1),v111);
		v111=min(fabs(v110-1),v111);
		v111=min(fabs(v112-1),v111);	
		unsignedLevelSet[id]=v111;
	} 
}
kernel void extendDistanceFieldFullGrid(
	global float* oldLevelSet,
	global float* unsignedLevelSet,
	global int* labels,uint band){
	uint id=get_global_id(0);
    int i,j,k;
    if(id>=ROWS*COLS*SLICES)return;
	getRowColSlice(id,&i,&j,&k);
	float v111;
	float v011;
	float v121;
	float v101;
	float v211;
	float v110;
	float v112;	
	if(oldLevelSet[id]<=0.5f)return;	
	
	int label=labels[id];
	v111 =getLevelSetValue(unsignedLevelSet,labels,label,i, j, k);
	v011 =getLevelSetValue(unsignedLevelSet,labels,label,i - 1, j, k);
	v121 =getLevelSetValue(unsignedLevelSet,labels,label,i, j + 1, k);
	v101 =getLevelSetValue(unsignedLevelSet,labels,label,i, j - 1, k);
	v211 =getLevelSetValue(unsignedLevelSet,labels,label,i + 1, j, k);
	v110 =getLevelSetValue(unsignedLevelSet,labels,label,i, j, k - 1);
	v112 =getLevelSetValue(unsignedLevelSet,labels,label,i, j, k + 1);
	
	if(unsignedLevelSet[id]>band-0.5f){
		v111=1E10f;
		v111=min(fabs(v011-1),v111);
		v111=min(fabs(v121-1),v111);
		v111=min(fabs(v101-1),v111);
		v111=min(fabs(v211-1),v111);
		v111=min(fabs(v110-1),v111);
		v111=min(fabs(v112-1),v111);	
		unsignedLevelSet[id]=v111;
	} 
}
kernel void maxImageValue(
	global float* deltaLevelSet,
	global float* maxBuffer,
	int elements){
	float maxValue=0;
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	deltaLevelSet+=7*id*STRIDE;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz*7;i++){
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
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global int* labelMask,
	global int* forceIndexes,
	float pressureWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(oldSignedLevelSet[id]>0.5f){
		for(int index=0;index<7;index++){
			deltaLevelSet[7*gid+index]=0;
		}
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	float4 grad;
	int activeLabels[7];
	
	activeLabels[0]=getLabelValue(oldLabelImage,i,j,k);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j,k);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j,k);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1,k);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1,k);
	activeLabels[5]=getLabelValue(oldLabelImage,i,j,k+1);
	activeLabels[6]=getLabelValue(oldLabelImage,i,j,k-1);
	
	int label;
	int offset;
	deltaLevelSet+=gid*7;
	objectIds+=gid*7;
	float pressureValue= pressureForce[id];
	for(int index=0;index<7;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			#if CONTAINS_OVERLAPS
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			#else
			if(offset<1){			
				deltaLevelSet[index]=0.999f;	
			#endif
			} else {
				float v111 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j,k);
				float v010 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k - 1);
				float v120 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k - 1);
				float v110 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k - 1);
				float v100 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k - 1);
				float v210 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k - 1);		
				float v001 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1, k);
				float v011 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k);
				float v101 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k);
				float v211 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k);
				float v201 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1, k);
				float v221 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1, k);
				float v021 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1, k);
				float v121 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k);
				float v012 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k + 1);
				float v122 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k + 1);
				float v112 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k + 1);
				float v102 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k + 1);
				float v212 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k + 1);
		
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
				
				
				
				// Force should be negative to move level set outwards if pressure is
				// positive
				float force = pressureWeight *pressureValue;
				float pressure=0;
				if (force > 0) {
					pressure = -force * sqrt(GradientSqrPos);
				} else if (force < 0) {
					pressure = -force * sqrt(GradientSqrNeg);
				} 
				deltaLevelSet[index]=kappa+pressure;	
			}
		}
	}
}
kernel void acweSpeedKernel(
	const global uint* activeList,
	const global float* pressureForce,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global int* labelMask,
	global int* forceIndexes,
	global float* averages,
	float pressureWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(oldSignedLevelSet[id]>0.5f){
		for(int index=0;index<7;index++){
			deltaLevelSet[7*gid+index]=0;
		}
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	float4 grad;
	int activeLabels[7];
	
	activeLabels[0]=getLabelValue(oldLabelImage,i,j,k);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j,k);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j,k);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1,k);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1,k);
	activeLabels[5]=getLabelValue(oldLabelImage,i,j,k+1);
	activeLabels[6]=getLabelValue(oldLabelImage,i,j,k-1);
	
	int label;
	int offset;
	deltaLevelSet+=gid*7;
	objectIds+=gid*7;
	float pressureValue= pressureForce[id];
	for(int index=0;index<7;index++){
		label=activeLabels[index];
		offset=label;
		objectIds[index]=label;
				float v111 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j,k);
				float v010 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k - 1);
				float v120 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k - 1);
				float v110 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k - 1);
				float v100 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k - 1);
				float v210 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k - 1);		
				float v001 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1, k);
				float v011 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k);
				float v101 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k);
				float v211 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k);
				float v201 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1, k);
				float v221 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1, k);
				float v021 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1, k);
				float v121 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k);
				float v012 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k + 1);
				float v122 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k + 1);
				float v112 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k + 1);
				float v102 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k + 1);
				float v212 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k + 1);
		
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
				
				
				
				// Force should be negative to move level set outwards if pressure is
				// positive
				float maxDiff=0;
				float diff;
				float avg=averages[offset];
				
				for(int nn=0;nn<7;nn++){
					diff=fabs(pressureValue-averages[activeLabels[nn]]);
					if(diff>maxDiff){
						maxDiff=diff;
					}
				}
				
				float force =pressureWeight *(maxDiff-fabs(pressureValue-avg));
				float pressure=0;
				if (force > 0) {
					pressure = -force * sqrt(GradientSqrPos);
				} else if (force < 0) {
					pressure = -force * sqrt(GradientSqrNeg);
				} 
				deltaLevelSet[index]=kappa+pressure;	
	}
}
kernel void pressureVecFieldSpeedKernel(
	const global uint* activeList,
	const global float* pressureForce,
	const global float* vecField,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global int* labelMask,
	global int* forceIndexes,
	float pressureWeight,
	float advectWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(oldSignedLevelSet[id]>0.5f){
		for(int index=0;index<7;index++){
			deltaLevelSet[7*gid+index]=0;
		}
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	float4 grad;
	int activeLabels[7];
	
	activeLabels[0]=getLabelValue(oldLabelImage,i,j,k);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j,k);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j,k);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1,k);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1,k);
	activeLabels[5]=getLabelValue(oldLabelImage,i,j,k+1);
	activeLabels[6]=getLabelValue(oldLabelImage,i,j,k-1);
	int label;
	int offset;
	deltaLevelSet+=gid*7;
	objectIds+=gid*7;
	vecField+=3*id;
	forceX = advectWeight * vecField[0];
	forceY = advectWeight * vecField[1];
	forceZ = advectWeight * vecField[2];
	float pressureValue= pressureForce[id];
	for(int index=0;index<7;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			#if CONTAINS_OVERLAPS
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			#else
			if(offset<1){			
				deltaLevelSet[index]=0.999f;	
			#endif
			} else {	
				float v111 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j,k);
				float v010 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k - 1);
				float v120 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k - 1);
				float v110 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k - 1);
				float v100 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k - 1);
				float v210 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k - 1);		
				float v001 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1, k);
				float v011 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k);
				float v101 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k);
				float v211 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k);
				float v201 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1, k);
				float v221 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1, k);
				float v021 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1, k);
				float v121 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k);
				float v012 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k + 1);
				float v122 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k + 1);
				float v112 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k + 1);
				float v102 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k + 1);
				float v212 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k + 1);
	
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
				deltaLevelSet[index]=-advection+kappa+pressure;
			}
		}
	}		
}
kernel void vecFieldSpeedKernel(
	const global uint* activeList,
	const global float* vecField,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global int* labelMask,
	global int* forceIndexes,
	float advectWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(oldSignedLevelSet[id]>0.5f){
		for(int index=0;index<7;index++){
			deltaLevelSet[7*gid+index]=0;
		}
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	float4 grad;
	int activeLabels[7];
	
	activeLabels[0]=getLabelValue(oldLabelImage,i,j,k);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j,k);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j,k);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1,k);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1,k);
	activeLabels[5]=getLabelValue(oldLabelImage,i,j,k+1);
	activeLabels[6]=getLabelValue(oldLabelImage,i,j,k-1);
	int label;
	int offset;
	deltaLevelSet+=gid*7;
	objectIds+=gid*7;
	vecField+=3*id;
	forceX = advectWeight * vecField[0];
	forceY = advectWeight * vecField[1];
	forceZ = advectWeight * vecField[3];
	for(int index=0;index<7;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			#if CONTAINS_OVERLAPS
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			#else
			if(offset<1){			
				deltaLevelSet[index]=0.999f;	
			#endif
			} else {
				float v111 =getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j,k);
				float v010 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k - 1);
				float v120 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k - 1);
				float v110 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k - 1);
				float v100 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k - 1);
				float v210 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k - 1);		
				float v001 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1, k);
				float v011 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k);
				float v101 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k);
				float v211 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k);
				float v201 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1, k);
				float v221 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1, k);
				float v021 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1, k);
				float v121 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k);
				float v012 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k + 1);
				float v122 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k + 1);
				float v112 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k + 1);
				float v102 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k + 1);
				float v212 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k + 1);
				
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
				
				deltaLevelSet[index]=-advection+kappa;	
			}
		}
	}
}
kernel void gradientSpeedKernel(
	const global uint* activeList,
	const global float* unsignedLevelSet,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global int* labelMask,
	global int* forceIndexes,
	float advectWeight,
	float curvWeight,
	int elements){
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(oldSignedLevelSet[id]>0.5f){
		for(int index=0;index<7;index++){
			deltaLevelSet[7*gid+index]=0;
		}
		return;
	}
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float forceX,forceY,forceZ;
	int activeLabels[7];
	
	activeLabels[0]=getLabelValue(oldLabelImage,i,j,k);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j,k);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j,k);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1,k);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1,k);
	activeLabels[5]=getLabelValue(oldLabelImage,i,j,k+1);
	activeLabels[6]=getLabelValue(oldLabelImage,i,j,k-1);
	int label;
	int offset;
	deltaLevelSet+=gid*7;
	objectIds+=gid*7;
	float4 grad=getScaledGradientValue(unsignedLevelSet,i,j,k);
	forceX = advectWeight * grad.x;
	forceY = advectWeight * grad.y;
	forceZ = advectWeight * grad.z;
	for(int index=0;index<7;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			#if CONTAINS_OVERLAPS
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			#else
			if(offset<1){			
				deltaLevelSet[index]=0.999f;	
			#endif
			} else {	
				float v111 =getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j,k);
				float v010 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k - 1);
				float v120 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k - 1);
				float v110 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k - 1);
				float v100 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k - 1);
				float v210 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k - 1);		
				float v001 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1, k);
				float v011 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k);
				float v101 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k);
				float v211 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k);
				float v201 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1, k);
				float v221 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1, k);
				float v021 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1, k);
				float v121 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k);
				float v012 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j, k + 1);
				float v122 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1, k + 1);
				float v112 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j, k + 1);
				float v102 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1, k + 1);
				float v212 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j, k + 1);
				
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
				
				deltaLevelSet[index]=-advection+kappa;	
			}
		}
	}
}
#if CLAMP_SPEED
kernel void applyForces(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global float* signedLevelSet,
	global int* labelImage,
	float timeStep,
	int elements){
#else
kernel void applyForces(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global float* signedLevelSet,
	global int* labelImage,
	const global float* maxTmpBuffer,
	int elements){
	float timeStep=maxTmpBuffer[0];
#endif			
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	if(oldSignedLevelSet[id]>0.5f)return;
	uint i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float minValue1=INF_FORCE;
	float minValue2=INF_FORCE;
	int minLabel1=-1;
	int minLabel2=-1;
	int mask=0;
	float delta;
	bool signChange;
	float update=0;
	deltaLevelSet+=7*gid;
	objectIds+=7*gid;
	for(int l=0;l<7;l++){
		mask=objectIds[l];
#if CLAMP_SPEED
		delta=timeStep*clamp(deltaLevelSet[l],-1.0f,1.0f);
#else
		delta=timeStep*deltaLevelSet[l];
#endif
		if(mask!=-1){
			update=getLevelSetValue(signedLevelSet,labelImage,mask,i,j,k)+delta;
			if(mask!=minLabel1&&mask!=minLabel2){
				if (update < minValue1) {
					minValue2 = minValue1;
					minLabel2 = minLabel1;
					minValue1 = update;
					minLabel1 = mask;
				} else if (update < minValue2) {
					minValue2 = update;
					minLabel2 = mask;
				}
			}
		}
	}
	
	if(minLabel2>=0){
		if (minValue1 == minValue2) {
			int oldLabel=labelImage[id];
			//Keep old label if possible
			labelImage[id] = (oldLabel==minLabel1)?oldLabel:minLabel2;
		} else {
			labelImage[id] = minLabel1;
		}
		signedLevelSet[id] = fabs(0.5f * (float) (minValue1 - minValue2));	
	} else if(minValue1<INF_FORCE){
		labelImage[id] = minLabel1;
		signedLevelSet[id] = fabs(minValue1);
	}
	
}
inline bool getBitValue(global uchar* bytes,int i){
	__const int LEN=(2 << 24);
	return ((bytes[LEN-(i>>3)-1] & (1 << (i % 8))) > 0);
}
#if CLAMP_SPEED
kernel void applyForcesTopoRule(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global float* signedLevelSet,
	global int* labelImage,
	global uchar* topoLUT,
	float timeStep,
	int elements,
	int offset){
#else
kernel void applyForcesTopoRule(
	const global uint* activeList,
	global float* oldSignedLevelSet,
	global int* oldLabelImage,
	global float* deltaLevelSet,
	global int* objectIds,
	global float* signedLevelSet,
	global int* labelImage,
	const global float* maxTmpBuffer,
	global uchar* topoLUT,
	int elements,
	int offset){
	float timeStep=maxTmpBuffer[0];
#endif			
	int xOff=xShift[offset];
	int yOff=yShift[offset];
	int zOff=zShift[offset];
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	if(i%2!=xOff||j%2!=yOff||k%2!=zOff)return;	
	if(oldSignedLevelSet[id]>0.5f)return;
	int oldLabel=labelImage[id];
	float minValue1=INF_FORCE;
	float minValue2=INF_FORCE;
	int minLabel1=-1;
	int minLabel2=-1;
	int mask=0;
	float delta;
	bool signChange;
	float update=0;
	deltaLevelSet+=7*gid;
	objectIds+=7*gid;
	for(int l=0;l<7;l++){
		mask=objectIds[l];
#if CLAMP_SPEED
		delta=timeStep*clamp(deltaLevelSet[l],-1.0f,1.0f);
#else
		delta=timeStep*deltaLevelSet[l];
#endif
		if(mask!=-1){
			update=getLevelSetValue(signedLevelSet,labelImage,mask,i,j,k)+delta;
			if(mask!=minLabel1&&mask!=minLabel2){
				if (update < minValue1) {
					minValue2 = minValue1;
					minLabel2 = minLabel1;
					minValue1 = update;
					minLabel1 = mask;
				} else if (update < minValue2) {
					minValue2 = update;
					minLabel2 = mask;
				}
			}
		}
	}
	mask=0;
	mask |=((getLabelValue(labelImage,i-1, j-1,k-1 )==oldLabel) ? (1 << 0) : 0);
	mask |=((getLabelValue(labelImage,i-1, j-1,k+0 )==oldLabel) ? (1 << 1) : 0);
	mask |=((getLabelValue(labelImage,i-1, j-1,k+1 )==oldLabel) ? (1 << 2) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+0,k-1 )==oldLabel) ? (1 << 3) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+0,k+0 )==oldLabel) ? (1 << 4) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+0,k+1 )==oldLabel) ? (1 << 5) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+1,k-1 )==oldLabel) ? (1 << 6) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+1,k+0 )==oldLabel) ? (1 << 7) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+1,k+1 )==oldLabel) ? (1 << 8) : 0);
	mask |=((getLabelValue(labelImage,i+0, j-1,k-1 )==oldLabel) ? (1 << 9) : 0);
	mask |=((getLabelValue(labelImage,i+0, j-1,k+0 )==oldLabel) ? (1 <<10) : 0);
	mask |=((getLabelValue(labelImage,i+0, j-1,k+1 )==oldLabel) ? (1 <<11) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+0,k-1 )==oldLabel) ? (1 <<12) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+0,k+0 )==oldLabel) ? (1 <<13) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+0,k+1 )==oldLabel) ? (1 <<14) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+1,k-1 )==oldLabel) ? (1 <<15) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+1,k+0 )==oldLabel) ? (1 <<16) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+1,k+1 )==oldLabel) ? (1 <<17) : 0);
	mask |=((getLabelValue(labelImage,i+1, j-1,k-1 )==oldLabel) ? (1 <<18) : 0);
	mask |=((getLabelValue(labelImage,i+1, j-1,k+0 )==oldLabel) ? (1 <<19) : 0);
	mask |=((getLabelValue(labelImage,i+1, j-1,k+1 )==oldLabel) ? (1 <<20) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+0,k-1 )==oldLabel) ? (1 <<21) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+0,k+0 )==oldLabel) ? (1 <<22) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+0,k+1 )==oldLabel) ? (1 <<23) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+1,k-1 )==oldLabel) ? (1 <<24) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+1,k+0 )==oldLabel) ? (1 <<25) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+1,k+1 )==oldLabel) ? (1 <<26) : 0);
	
	if(!getBitValue(topoLUT,mask)){
		signedLevelSet[id] = 1.0f;	
		return;
	}
	
	if(minLabel2>=0){
		if (minValue1 == minValue2) {
			int oldLabel=labelImage[id];
			//Keep old label if possible
			labelImage[id] = (oldLabel==minLabel1)?oldLabel:minLabel2;
		} else {
			labelImage[id] = minLabel1;
		}
		signedLevelSet[id] = fabs(0.5f * (float) (minValue1 - minValue2));	
	} else if(minValue1<INF_FORCE){
		labelImage[id] = minLabel1;
		signedLevelSet[id] = fabs(minValue1);
	}
}
__kernel void plugLevelSet(global int* activeList,__global float* signedLevelSet,__global int* labels,int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	int label=getLabelValue(labels,i,j,k);
	float v11 = signedLevelSet[id];
	int activeLabels[6];
	activeLabels[0]=getLabelValue(labels,i+1,j,k);
	activeLabels[1]=getLabelValue(labels,i-1,j,k);
	activeLabels[2]=getLabelValue(labels,i,j+1,k);
	activeLabels[3]=getLabelValue(labels,i,j-1,k);
	activeLabels[4]=getLabelValue(labels,i,j,k+1);
	activeLabels[5]=getLabelValue(labels,i,j,k-1);
	if(v11<=0.5f){
		for(int index=0;index<6;index++){
			if(label!=activeLabels[index])return;
		}
		signedLevelSet[id]=MAX_DISTANCE+0.5f;
	}
}
kernel void labelsToLevelSet(global int* labels,global int* oldLabels,global float* levelset,global float* oldLevelset){
    uint id = get_global_id(0);
   	int i,j,k;
   	if(id>=ROWS*COLS*SLICES)return;
	getRowColSlice(id,&i,&j,&k);
	int currentLabel=labels[id];
	oldLabels[id]=currentLabel;
	int activeLabels[6];
	activeLabels[0]=getLabelValue(oldLabels,i+1,j,k);
	activeLabels[1]=getLabelValue(oldLabels,i-1,j,k);
	activeLabels[2]=getLabelValue(oldLabels,i,j+1,k);
	activeLabels[3]=getLabelValue(oldLabels,i,j-1,k);
	activeLabels[4]=getLabelValue(oldLabels,i,j,k+1);
	activeLabels[5]=getLabelValue(oldLabels,i,j,k-1);
	for(int n=0;n<6;n++){
		if(currentLabel<activeLabels[n]){
			levelset[id]=0.01f;
			oldLevelset[id]=0.01f;
			return;
		}
	}
	levelset[id]=1.0f;
	oldLevelset[id]=1.0f;
}

kernel void copyLevelSetImageNoATI(global float* srcImage,global int* labels,const global float4* colors,global float4* destImage){
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

kernel void bilateralFilter(global int* activeList,global float4* buffIn,float edgeSigma,float lambda,int activeListSize,int xOff,int yOff,int zOff){
	int i,j,k;
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	getRowColSlice(id,&i,&j,&k);
	if(i%2!=xOff||j%2!=yOff||k%2!=zOff)return;	
	
	float4 v111 = getVectorImageValue(buffIn,i, j, k);
	float4 v211 = getVectorImageValue(buffIn,i + 1, j, k);
	float4 v121 = getVectorImageValue(buffIn,i, j + 1, k);
	float4 v101 = getVectorImageValue(buffIn,i, j - 1, k);
	float4 v011 = getVectorImageValue(buffIn,i - 1, j, k);
	float4 v110 = getVectorImageValue(buffIn,i, j, k - 1);
	float4 v112 = getVectorImageValue(buffIn,i, j, k + 1);		
	float4 norm=v111-0.16666f*lambda*(
		tukey(v111,v011,edgeSigma)*(v111-v011)+
		tukey(v111,v121,edgeSigma)*(v111-v121)+
		tukey(v111,v101,edgeSigma)*(v111-v101)+
		tukey(v111,v211,edgeSigma)*(v111-v211)+
		tukey(v111,v110,edgeSigma)*(v111-v110)+
		tukey(v111,v112,edgeSigma)*(v111-v112));
	buffIn[id]=norm;
}

kernel void copyLevelSetImageNB(
	global int* activeList,
	global float* distImage,
	global int* labelImage,
	const global float4* colorLUT,
	global float4* colorListBuffer,
	global int* imageLabelListBuffer,
	int activeListSize){
	
	int i,j,k;
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	getRowColSlice(id,&i,&j,&k);

	float v111 = getNonBackgroundLevelSetValue(distImage,labelImage,colorLUT, i, j, k);
	float v211 = getNonBackgroundLevelSetValue(distImage,labelImage,colorLUT, i + 1, j, k);
	float v121 = getNonBackgroundLevelSetValue(distImage,labelImage,colorLUT, i, j + 1, k);
	float v101 = getNonBackgroundLevelSetValue(distImage,labelImage,colorLUT, i, j - 1, k);
	float v011 = getNonBackgroundLevelSetValue(distImage,labelImage,colorLUT, i - 1, j, k);
	float v110 = getNonBackgroundLevelSetValue(distImage,labelImage,colorLUT, i, j, k - 1);
	float v112 = getNonBackgroundLevelSetValue(distImage,labelImage,colorLUT, i, j, k + 1);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w=1E-6f;
	grad=normalize(grad);
	int label=abs(labelImage[id]);
	if(label>0&&colorLUT[label-1].w==0){
		label=-label;	
	}
	grad.w=clamp(((label<=0)?1:-1)*fabs(v111),-10.0f,10.0f);	
	if(i==ROWS-1||j==COLS-1||k==SLICES-1||i==0||j==0||k==0)grad.w=max(grad.w,1.5f);
	colorListBuffer[gid]=grad;
	imageLabelListBuffer[gid]=label;
}

kernel void countActiveList(global int* offsets,global float* oldDistanceField,global float* distanceField){
	uint k=get_global_id(0);
	if(k>=SLICES)return;
	distanceField+=k*ROWS*COLS;
	oldDistanceField+=k*ROWS*COLS;
	
	int total=0;
	for(int ij=0;ij<ROWS*COLS;ij++){	
		if(oldDistanceField[ij]<=MAX_DISTANCE){				
			total++;
		}
		if(distanceField[ij]<0){
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
		if(distanceField[ij]<=MAX_DISTANCE){			
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
		float val1=distanceField[index];
		float val2=distanceField[index2];
		if(	val1>-0.5&&val1<=MAX_DISTANCE-1&&
			val2>=MAX_DISTANCE){
				distanceField[index2]=-offset-1;
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
		if(val1>-0.5&&val1<=MAX_DISTANCE-1&&val2==-offset-1){
			activeList[off2++]=index2;
			oldDistanceField[index2]=MAX_DISTANCE;
			distanceField[index2]=MAX_DISTANCE;		
		}
	}
}
kernel void expandActiveListHistory(
	global int* offsets,
	global int* activeList,
	global float* oldDistanceField,
	global float* distanceField,
	global char* historyBuffer,
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
		if(val1>-0.5&&val1<=MAX_DISTANCE-1&&val2==-offset-1){
			activeList[off2++]=index2;
			oldDistanceField[index2]=MAX_DISTANCE;
			distanceField[index2]=MAX_DISTANCE;	
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
		float val=distanceField[index];
		if(val>=0&&val<=MAX_DISTANCE){
			total++;
		}
	}
	offsetList[id]=total;
}
kernel void deleteCountActiveListHistory(
	global int* offsetList,
	const global uint* activeList,
	global float* distanceField,
	global int* history,
	int elements){
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	activeList+=id*STRIDE;
	int total=0;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz;i++){
		int index=activeList[i];
		float val=distanceField[index];
		int times=history[index];
		if(val>=0&&val<=MAX_DISTANCE&&times==1){
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
		if(val>=0&&val<=MAX_DISTANCE){
			outActiveList[off++]=index;
		} else {
			distanceField[index]=MAX_DISTANCE+0.5f;
			oldDistanceField[index]=MAX_DISTANCE+0.5f;
		}
	}
}
kernel void compactActiveListHistory(
	global int* offsets,
	global int* activeList,
	global int* outActiveList,
	global float* oldDistanceField,
	global float* distanceField,
	global int* history,
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
		int times=history[index];
		if(times==1){
			if(val>=0&&val<=MAX_DISTANCE){			
				outActiveList[off++]=index;
			} else {
				distanceField[index]=MAX_DISTANCE+0.5f;
				oldDistanceField[index]=MAX_DISTANCE+0.5f;
			}
		}
	}
}
kernel void sumAverages(global float* averages,global float* areas,global float* stddev){
	uint id=get_global_id(0);
	if(id>=NUM_LABELS)return;
	float val=0;
	float area=1E-6f;
	float std=0;
	for(int k=0;k<SLICES;k++){
		val+=averages[id+k*NUM_LABELS];
		area+=areas[id+k*NUM_LABELS];
		std+=stddev[id+k*NUM_LABELS];
	}
	
	val/=area;
	averages[id]=val;
	areas[id]=area;
	stddev[id]=sqrt(std/area-val*val);
}
kernel void regionAverage(global int* labels,global float* levelset,global float* pressures,global int* labelMask,global float* averages,global float* areas,global float* stddev){
    uint k = get_global_id(0);
    if(k>=SLICES)return;
  	int offset;
	averages+=k*NUM_LABELS;
	areas+=k*NUM_LABELS;
	stddev+=k*NUM_LABELS;
	int currentLabel;
	float w;
	float area=0;
	float avg=0;
	float std=0;
	for(int n=0;n<NUM_LABELS;n++){
		areas[n]=0;
		averages[n]=0;
		stddev[n]=0;
	}
	labels+=k*ROWS*COLS;
	pressures+=k*ROWS*COLS;
	for(int ij=0;ij<ROWS*COLS;ij++){
		currentLabel=labels[ij];
		offset=getOffset(currentLabel,labelMask);
		avg=pressures[ij];
		std=avg*avg;
		stddev[offset]+=std;
		averages[offset]+=avg;
		areas[offset]+=1.0f;
	}
}
