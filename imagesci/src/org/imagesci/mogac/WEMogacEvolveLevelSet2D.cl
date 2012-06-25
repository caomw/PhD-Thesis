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
constant int xShift[4] = { 0, 0, 1, 1 };
constant int yShift[4] = { 0, 1, 0, 1 };

constant int xNeighborhood[4] = { -1, 1, 0, 0 };
constant int yNeighborhood[4] = { 0, 0, -1, 1 };
inline uint getSafeIndex(int i, int j) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return (c * ROWS) + r;
}
inline uint getIndex(int i, int j) {
	return (j * ROWS) + i;
}
inline void getRowCol(uint ij,int* i, int* j) {
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}
inline void getRowCol2(uint index,int* i, int* j) {
	(*j)=2*index/ROWS;
	(*i)=index-(*j)*ROWS/2;
}
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

inline float getLevelSetValue(global float* image,global int* labels,int label,int i,int j){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int ii=getIndex(r,c);
	if(labels[ii]==label){
		return -image[ii];
	} else {
		return image[ii];
	}
}
inline float getImageValue(global float* image,int i,int j){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return image[getIndex(r,c)];
}
inline float getLabelValue(global int* image,int i,int j){
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
kernel void extendDistanceFieldFullGrid(
	global float* oldLevelSet,
	global float* unsignedLevelSet,
	global int* labels,uint band){
	uint id=get_global_id(0);
	int i,j;
	getRowCol(id,&i,&j);
	float v11;
	float v01;
	float v12;
	float v10;
	float v21;	
	if(oldLevelSet[id]<=0.5f)return;
	
	int label=labels[id];
	v01 =getLevelSetValue(unsignedLevelSet,labels,label,i - 1, j);
	v12 =getLevelSetValue(unsignedLevelSet,labels,label,i, j + 1);
	v10 =getLevelSetValue(unsignedLevelSet,labels,label,i, j - 1);
	v21 =getLevelSetValue(unsignedLevelSet,labels,label,i + 1, j);
	if(unsignedLevelSet[id]>band-0.5f){
		v11=1E10f;
		v11=min(fabs(v01-1),v11);
		v11=min(fabs(v12-1),v11);
		v11=min(fabs(v10-1),v11);
		v11=min(fabs(v21-1),v11);	
		unsignedLevelSet[id]=v11;
	} 
}

kernel void extendDistanceField(
	const global uint* activeList,
	global float* oldLevelSet,
	global float* unsignedLevelSet,
	global int* labels,uint band,int elements){
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
	if(oldLevelSet[id]<=0.5f)return;
	
	int label=labels[id];
	v01 =getLevelSetValue(unsignedLevelSet,labels,label,i - 1, j);
	v12 =getLevelSetValue(unsignedLevelSet,labels,label,i, j + 1);
	v10 =getLevelSetValue(unsignedLevelSet,labels,label,i, j - 1);
	v21 =getLevelSetValue(unsignedLevelSet,labels,label,i + 1, j);
	if(unsignedLevelSet[id]>band-0.5f){
		v11=1E10f;
		v11=min(fabs(v01-1),v11);
		v11=min(fabs(v12-1),v11);
		v11=min(fabs(v10-1),v11);
		v11=min(fabs(v21-1),v11);	
		unsignedLevelSet[id]=v11;
	} 
}
	
kernel void maxImageValue(
	global float* deltaLevelSet,
	global float* maxBuffer,
	int elements){
	float maxValue=0;	
	uint id=get_global_id(0);
	if(id*STRIDE>elements)return;
	deltaLevelSet+=5*id*STRIDE;
	int sz=min((int)STRIDE,(int)(elements-id*STRIDE));
	for(int i=0;i<sz;i++){
		for(int j=0;j<5;j++){
			float delta=deltaLevelSet[5*i+j];
			maxValue=max(fabs(delta),maxValue);
		}
	}
	maxBuffer[id]=maxValue;
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
		float val1=distanceField[index];
		float val2=distanceField[index2];
		if(	val1>-0.5&&	val1<=MAX_DISTANCE-1&&
			val2>=MAX_DISTANCE){
				distanceField[index2]=-offset-1;
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
		if(val1>-0.5f&&val1<=MAX_DISTANCE-1&&val2==-offset-1){
			activeList[off2++]=index2;
			/*
			float minVal=MAX_DISTANCE;
			getRowCol(index2,&i,&j);
			for(int nn=0;nn<4;nn++){
				int ii=xNeighborhood[nn];
				int jj=yNeighborhood[nn];
				minVal=min(getImageValue(oldDistanceField,i+ii,j+jj),minVal);
			}		
			oldDistanceField[index2]=minVal+1;
			distanceField[index2]=minVal+1;	
			*/
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
		if(distanceField[index]<=MAX_DISTANCE){
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
		if(oldDistanceField[index]<=MAX_DISTANCE){
			outActiveList[off++]=index;
		} else {
			distanceField[index]=MAX_DISTANCE+0.5f;
			oldDistanceField[index]=MAX_DISTANCE+0.5f;
		}
	}
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
		for(int index=0;index<5;index++){
			deltaLevelSet[5*gid+index]=0;
		}
		return;
	}
	int i,j;
	getRowCol(id,&i,&j);
	float forceX,forceY;
	int activeLabels[5];

	activeLabels[0]=getLabelValue(oldLabelImage,i,j);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1);
	int label;
	int offset;
	deltaLevelSet+=gid*5;
	objectIds+=gid*5;
	vecField+=2*id;
	forceX = advectWeight * vecField[0];
	forceY = advectWeight * vecField[1];
	for(int index=0;index<5;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			} else {
				float v11 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j);
				float v00 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1);
				float v01 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j);
				float v10 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1);
				float v21 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j);
				float v20 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1);
				float v22 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1);
				float v02 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1);
				float v12 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1);
		
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
				if (fabs(denom) > 1E-3f) {
					kappa = curvWeight * numer / denom;
				} else {
					kappa = curvWeight * numer * sign(denom) * 1E3;
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
		for(int index=0;index<5;index++){
			deltaLevelSet[5*gid+index]=0;
		}
		return;
	}
	int i,j;
	getRowCol(id,&i,&j);
	float forceX,forceY;
	int activeLabels[5];

	activeLabels[0]=getLabelValue(oldLabelImage,i,j);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1);
	int label;
	int offset;
	deltaLevelSet+=gid*5;
	objectIds+=gid*5;
	float2 grad=getScaledGradientValue(unsignedLevelSet,i,j);
	forceX = advectWeight * grad.x;
	forceY = advectWeight * grad.y;
	for(int index=0;index<5;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			} else {
				float v11 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j);
				float v00 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1);
				float v01 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j);
				float v10 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1);
				float v21 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j);
				float v20 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1);
				float v22 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1);
				float v02 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1);
				float v12 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1);
		
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
				if (fabs(denom) > 1E-3f) {
					kappa = curvWeight * numer / denom;
				} else {
					kappa = curvWeight * numer * sign(denom) * 1E3;
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
	
				deltaLevelSet[index]=-advection+kappa;	
			}
		}
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
		for(int index=0;index<5;index++){
			deltaLevelSet[5*gid+index]=0;
		}
		return;
	}
	int i,j;
	getRowCol(id,&i,&j);
	float forceX,forceY;
	float4 grad;
	int activeLabels[5];

	activeLabels[0]=getLabelValue(oldLabelImage,i,j);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1);
	int label;
	int offset;
	deltaLevelSet+=gid*5;
	objectIds+=gid*5;
	vecField+=2*id;
	forceX = advectWeight * vecField[0];
	forceY = advectWeight * vecField[1];
	float pressureValue= pressureForce[id];
	for(int index=0;index<5;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			} else {
				float v11 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j);
				float v00 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1);
				float v01 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j);
				float v10 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1);
				float v21 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j);
				float v20 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1);
				float v22 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1);
				float v02 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1);
				float v12 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1);
		
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
		for(int index=0;index<5;index++){
			deltaLevelSet[5*gid+index]=0;
		}
		return;
	}
	int i,j;
	getRowCol(id,&i,&j);
	float forceX,forceY;
	float4 grad;
	int activeLabels[5];

	activeLabels[0]=getLabelValue(oldLabelImage,i,j);
	activeLabels[1]=getLabelValue(oldLabelImage,i+1,j);
	activeLabels[2]=getLabelValue(oldLabelImage,i-1,j);
	activeLabels[3]=getLabelValue(oldLabelImage,i,j+1);
	activeLabels[4]=getLabelValue(oldLabelImage,i,j-1);
	int label;
	int offset;
	deltaLevelSet+=gid*5;
	objectIds+=gid*5;
	float pressureValue= pressureForce[id];
	for(int index=0;index<5;index++){
		label=activeLabels[index];
		if(label==0){
			objectIds[index]=0;
			deltaLevelSet[index]=0;
		} else {
			offset=getOffset(label,labelMask);
			objectIds[index]=label;
			if(forceIndexes[offset]<0){			
				deltaLevelSet[index]=0.999f;	
			} else {
				float v11 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j);
				float v00 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j - 1);
				float v01 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j);
				float v10 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j - 1);
				float v21 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j);
				float v20 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j - 1);
				float v22 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i + 1, j + 1);
				float v02 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i - 1, j + 1);
				float v12 = getLevelSetValue(oldSignedLevelSet,oldLabelImage,label,i, j + 1);
					
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
					kappa = curvWeight * numer * sign(denom) * 1E5;
				}
				if (kappa < -maxCurvatureForce) {
					kappa = -maxCurvatureForce;
				} else if (kappa > maxCurvatureForce) {
					kappa = maxCurvatureForce;
				}
					
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
	int i,j;
	getRowCol(id,&i,&j);
	float minValue1=INF_FORCE;
	float minValue2=INF_FORCE;
	int minLabel1=-1;
	int minLabel2=-1;
	int mask=0;
	float delta;
	bool signChange;
	float update=0;
	deltaLevelSet+=5*gid;
	objectIds+=5*gid;
	for(int l=0;l<5;l++){
		mask=objectIds[l];
#if CLAMP_SPEED
		delta=timeStep*clamp(deltaLevelSet[l],-1.0f,1.0f);
#else
		delta=timeStep*deltaLevelSet[l];
#endif
		if(mask!=-1){
			update=getLevelSetValue(signedLevelSet,labelImage,mask,i,j)+delta;
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
			labelImage[id] = min(minLabel1, minLabel2);
		} else {
			labelImage[id] = minLabel1;
		}
		signedLevelSet[id] = fabs(0.5f * (float) (minValue1 - minValue2));	
	} else if(minValue1<INF_FORCE){
		labelImage[id] = minLabel1;
		signedLevelSet[id] = fabs(minValue1);
	}
}
constant char lut4_8[64] = { 123, -13, -5, -13, -69,
			51, -69, 51, -128, -13, -128, -13, 0, 51, 0, 51, -128, -13, -128,
			-13, -69, -52, -69, -52, -128, -13, -128, -13, -69, -52, -69, -52,
			-128, 0, -128, 0, -69, 51, -69, 51, 0, 0, 0, 0, 0, 51, 0, 51, -128,
			-13, -128, -13, -69, -52, -69, -52, -128, -13, -128, -13, -69, -52,
			-69, -52, 123, -13, -5, -13, -69, 51, -69, 51, -128, -13, -128,
			-13, 0, 51, 0, 51, -128, -13, -128, -13, -69, -52, -69, -52, -128,
			-13, -128, -13, -69, -52, -69, -52, -128, 0, -128, 0, -69, 51, -69,
			51, 0, 0, 0, 0, 0, 51, 0, 51, -128, -13, -128, -13, -69, -52, -69,
			-52, -128, -13, -128, -13, -69, -52, -69, -52 };
inline bool getBitValue(int i){
	return (((uchar)(lut4_8[63-(i>>3)]) & (1 << (i % 8))) > 0);
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
	int elements,
	int offset){
	float timeStep=maxTmpBuffer[0];
#endif	
	int xOff=xShift[offset];
	int yOff=yShift[offset];
	uint gid=get_global_id(0);
	if(gid>=elements)return;
	uint id=activeList[gid];
	int i,j;
	getRowCol(id,&i,&j);
	if(i%2!=xOff||j%2!=yOff)return;	
	if(oldSignedLevelSet[id]>0.5f)return;

	float minValue1=INF_FORCE;
	float minValue2=INF_FORCE;
	int minLabel1=-1;
	int minLabel2=-1;
	int mask=0;
	int oldLabel=labelImage[id];
	float delta;
	bool signChange;
	float update=0;
	deltaLevelSet+=5*gid;
	objectIds+=5*gid;
	for(int l=0;l<5;l++){
		mask=objectIds[l];
#if CLAMP_SPEED
		delta=timeStep*clamp(deltaLevelSet[l],-1.0f,1.0f);
#else
		delta=timeStep*deltaLevelSet[l];
#endif
		if(mask!=-1){
			update=getLevelSetValue(signedLevelSet,labelImage,mask,i,j)+delta;
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
	mask |=((getLabelValue(labelImage,i-1, j-1)==oldLabel) ? (1 << 0) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+0)==oldLabel) ? (1 << 1) : 0);
	mask |=((getLabelValue(labelImage,i-1, j+1)==oldLabel) ? (1 << 2) : 0);
	mask |=((getLabelValue(labelImage,i+0, j-1)==oldLabel) ? (1 << 3) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+0)==oldLabel) ? (1 << 4) : 0);
	mask |=((getLabelValue(labelImage,i+0, j+1)==oldLabel) ? (1 << 5) : 0);
	mask |=((getLabelValue(labelImage,i+1, j-1)==oldLabel) ? (1 << 6) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+0)==oldLabel) ? (1 << 7) : 0);
	mask |=((getLabelValue(labelImage,i+1, j+1)==oldLabel) ? (1 << 8) : 0);
	
	if(!getBitValue(mask)){
		signedLevelSet[id] = 1.0f;
		return;
	}
	
	if(minLabel2>=0){
		if (minValue1 == minValue2) {
			labelImage[id] = min(minLabel1, minLabel2);
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
	int i,j;
	getRowCol(id,&i,&j);
	int label=getLabelValue(labels,i,j);
	float v11 = signedLevelSet[id];
	int activeLabels[8];
	activeLabels[0]=getLabelValue(labels,i+1,j);
	activeLabels[1]=getLabelValue(labels,i-1,j);
	activeLabels[2]=getLabelValue(labels,i,j+1);
	activeLabels[3]=getLabelValue(labels,i,j-1);
	activeLabels[4]=getLabelValue(labels,i-1,j-1);
	activeLabels[5]=getLabelValue(labels,i+1,j-1);
	activeLabels[6]=getLabelValue(labels,i-1,j+1);
	activeLabels[7]=getLabelValue(labels,i+1,j+1);
	int count=0;
	for(int index=0;index<8;index++){
		if(label==activeLabels[index])count++;
	}
	//Pick any label other than this to fill the hole
	if(count==0){
		labels[id]=(i>0)?activeLabels[1]:activeLabels[0];
		signedLevelSet[id]=3.0f;
	}
}
kernel void labelsToLevelSet(global int* labels,global int* oldLabels,global float* levelset,global float* oldLevelset){
    uint id = get_global_id(0);
    if(id>=ROWS*COLS)return;
   	int i,j;
	getRowCol(id,&i,&j);
	int currentLabel=labels[id];
	oldLabels[id]=currentLabel;
	int activeLabels[4];
	activeLabels[0]=getLabelValue(labels,i+1,j);
	activeLabels[1]=getLabelValue(labels,i-1,j);
	activeLabels[2]=getLabelValue(labels,i,j+1);
	activeLabels[3]=getLabelValue(labels,i,j-1);
	for(int n=0;n<4;n++){
		if(currentLabel<activeLabels[n]){
			levelset[id]=0.01f;
			oldLevelset[id]=0.01f;
			return;
		}
	}
	levelset[id]=1.0f;
	oldLevelset[id]=1.0f;
}
kernel void countActiveList(global int* offsets,global float* oldDistanceField,global float* distanceField){
	uint j=get_global_id(0);
	distanceField+=j*ROWS;
	oldDistanceField+=j*ROWS;
	int total=0;
	for(int i=0;i<ROWS;i++){	
		if(oldDistanceField[i]<=MAX_DISTANCE){				
			total++;
		}
		if(distanceField[i]<0){
			distanceField[i]=oldDistanceField[i];
		}
	}
	offsets[j]=total;
}
kernel void prefixScanList(global int* offsets,global int* maxBuffer,int sz){
	uint id=get_global_id(0);
	if(id!=0)return;
	int total=0;
	for(int j=0;j<sz;j++){
		total+=offsets[j];
		offsets[j]=total;
	}
	maxBuffer[0]=total;
}

kernel void buildActiveList(global int* offsets,global int* activeListBuffer,global float* distanceField){
	uint j=get_global_id(0);
	distanceField+=j*ROWS;
	if(j>0)activeListBuffer+=offsets[j-1];
	int total=0;
	for(int i=0;i<ROWS;i++){	
		if(distanceField[i]<MAX_DISTANCE){									
			activeListBuffer[total++]=getIndex(i,j);
		}
	}
}