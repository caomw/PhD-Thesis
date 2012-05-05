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
 #define INF_FORCE 1E10f
 #define STATIC_SPRINGL (-1E8f)
 typedef struct{
	float4 particle;
    float4 vertexes[3];
} Springl3D;
constant int xShift[8] = { 0, 0, 1, 1, 0, 0, 1, 1 };
constant int yShift[8] = { 0, 1, 0, 1, 0, 1, 0, 1 };
constant int zShift[8] = { 0, 0, 0, 0, 1, 1, 1, 1 };

constant int xNeighborhood[6] = {-1, 1, 0, 0, 0, 0 };
constant int yNeighborhood[6] = { 0, 0,-1, 1, 0, 0 };
constant int zNeighborhood[6] = { 0, 0, 0, 0,-1, 1 };
 
inline int getIndex(int i, int j, int k) {
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
inline void getRowColSlice2(uint index,int* i, int* j, int* k) {
	(*k)=4*index/(ROWS*COLS);
	int ij=index-(*k)*(ROWS * COLS/4);
	(*j)=2*ij/ROWS;
	(*i)=ij-(*j)*ROWS/2;
}
inline float getImageValue(__global float* image,int i,int j,int k){
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
inline float getNonBackgroundLevelSetValue(global float* image,global int* labels,uint i,uint j,uint k){
	uint r = clamp((uint)i,(uint)0,(uint)(ROWS-1));
	uint c = clamp((uint)j,(uint)0,(uint)(COLS-1));
	uint s = clamp((uint)k,(uint)0,(uint)(SLICES-1));
	uint ii=getIndex(r,c,s);
	if(labels[ii]==0){
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
__kernel void addToVolume(__global float* unsignedLevelSet,float value){
	int id=get_global_id(0);
	unsignedLevelSet[id]+=value;
}
kernel void copyLevelSet(global float* srcLevelSet,global float* targetLevelSet){
	int id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	targetLevelSet[id]=srcLevelSet[id];
}
#define UNION 0
#define INTERSECT 1
#define A_MINUS_B 2
#define B_MINUS_A 3
kernel void levelSetCSG(global float* levelsetA,global float* levelsetB,global float* levelsetC,int operation){
	uint id=get_global_id(0);
	float val=0;
	float valA=levelsetA[id];
	float valB=levelsetB[id];
	if(operation==UNION){
		val=min(valA,valB);	
	} else if(operation==INTERSECT){
		val=max(valA,valB);	
	} else if(operation==A_MINUS_B){
		val=max(valA,-valB);	
	}  else if(operation==B_MINUS_A){
		val=max(valB,-valA);	
	}
	levelsetC[id]=val;
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
__kernel void thresholdDistanceField(__global float* signedLevelSet,__global float* outLevelSet){
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
	v111 =getImageValue(signedLevelSet,i, j, k);
	v011 =getImageValue(signedLevelSet,i - 1, j, k);
	v121 =getImageValue(signedLevelSet,i, j + 1, k);
	v101 =getImageValue(signedLevelSet,i, j - 1, k);
	v211 =getImageValue(signedLevelSet,i + 1, j, k);
	v110 =getImageValue(signedLevelSet,i, j, k - 1);
	v112 =getImageValue(signedLevelSet,i, j, k + 1);
	float sgn=1;
	sgn=min(v111,sgn);
	sgn=min(v011,sgn);
	sgn=min(v121,sgn);
	sgn=min(v101,sgn);
	sgn=min(v211,sgn);
	sgn=min(v110,sgn);
	sgn=min(v112,sgn);
	outLevelSet[getIndex(i,j,k)]=(sgn>=0)?vExtent*4:v111;
}
kernel void extendDistanceField(
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
kernel void extendSignedDistanceField(
global float* signedLevelSet,int band){
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
	v111 =signedLevelSet[id];
	float oldVal=v111;
	v011 =getImageValue(signedLevelSet,i - 1, j, k);
	v121 =getImageValue(signedLevelSet,i, j + 1, k);
	v101 =getImageValue(signedLevelSet,i, j - 1, k);
	v211 =getImageValue(signedLevelSet,i + 1, j, k);
	v110 =getImageValue(signedLevelSet,i, j, k - 1);
	v112 =getImageValue(signedLevelSet,i, j, k + 1);
	if(v111<-band+0.5f){
		v111=-(1E10);
		v111=(v011>1)?v111:max(v011,v111);
		v111=(v121>1)?v111:max(v121,v111);
		v111=(v101>1)?v111:max(v101,v111);
		v111=(v211>1)?v111:max(v211,v111);
		v111=(v110>1)?v111:max(v110,v111);
		v111=(v112>1)?v111:max(v112,v111);
		v111-=1.0f;		
	} else if(v111>band-0.5f){
		v111=(1E10);
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
	if(activeLevelSet<0.5&&activeLevelSet>-0.5){
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
		v111=-1E10f;
		v111=(v011>1)?v111:max(v011,v111);
		v111=(v121>1)?v111:max(v121,v111);
		v111=(v101>1)?v111:max(v101,v111);
		v111=(v211>1)?v111:max(v211,v111);
		v111=(v110>1)?v111:max(v110,v111);
		v111=(v112>1)?v111:max(v112,v111);
		v111-=1.0f;		
	} else if(v111>band-0.5f){
		v111=1E10f;
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
		signedLevelSet[id]=oldVal;//(oldVal>0)?0.01:-0.01;
	}
}					
kernel void evolveLevelSet(
		const global uint* activeList,
		global float* unsignedLevelSet,
		global float* signedLevelSet,
		global float* outLevelSet,
		float weight,
		float curvWeight,
		int elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	id=activeList[id];
	int i,j,k;
	getRowColSlice(id,&i,&j,&k);
	float v111;
	float DxNeg;
	float DxPos;
	float DyNeg;
	float DyPos;
	float DzNeg;
	float DzPos;
	float forceX,forceY,forceZ;
	float4 grad;
	uint index=getIndex(i,j,k);
	v111 =getImageValue(signedLevelSet,i, j, k);
	
	if(v111>0.5f||v111<-0.5f){
		outLevelSet[index]=v111;	
		return;
	}
	float v010 = getImageValue(signedLevelSet,i - 1, j, k - 1);
	float v120 = getImageValue(signedLevelSet,i, j + 1, k - 1);
	float v110 = getImageValue(signedLevelSet,i, j, k - 1);
	float v100 = getImageValue(signedLevelSet,i, j - 1, k - 1);
	float v210 = getImageValue(signedLevelSet,i + 1, j, k - 1);		
	float v001 = getImageValue(signedLevelSet,i - 1, j - 1, k);
	float v011 = getImageValue(signedLevelSet,i - 1, j, k);
	float v101 = getImageValue(signedLevelSet,i, j - 1, k);
	float v211 = getImageValue(signedLevelSet,i + 1, j, k);
	float v201 = getImageValue(signedLevelSet,i + 1, j - 1, k);
	float v221 = getImageValue(signedLevelSet,i + 1, j + 1, k);
	float v021 = getImageValue(signedLevelSet,i - 1, j + 1, k);
	float v121 = getImageValue(signedLevelSet,i, j + 1, k);
	float v012 = getImageValue(signedLevelSet,i - 1, j, k + 1);
	float v122 = getImageValue(signedLevelSet,i, j + 1, k + 1);
	float v112 = getImageValue(signedLevelSet,i, j, k + 1);
	float v102 = getImageValue(signedLevelSet,i, j - 1, k + 1);
	float v212 = getImageValue(signedLevelSet,i + 1, j, k + 1);
		
	DxNeg = v111 - v011;
	DxPos = v211 - v111;
	DyNeg = v111 - v101;
	DyPos = v121 - v111;
	DzNeg = v111 - v110;
	DzPos = v112 - v111;
	
	
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
	grad=getScaledGradientValue(unsignedLevelSet,i,j,k);
	forceX = weight * grad.x;
	forceY = weight * grad.y;
	forceZ = weight * grad.z;
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
	outLevelSet[index]=v111-clamp(advection+kappa,-0.5f,0.5f);	

}
inline bool getBitValue(global uchar* bytes,int i){
	__const int LEN=(2 << 24);
	return ((bytes[LEN-(i>>3)-1] & (1 << (i % 8))) > 0);
}
kernel void evolveLevelSetTopoRule(
	const global uint* activeList,
	global float* unsignedLevelSet,
	global float* signedLevelSet,
	global float* outLevelSet,
	global uchar* topoLUT,
	int flip,
	float weight,
	float curvWeight,
	int elements,
	int offset){
	uint id=get_global_id(0);
	if(id>=elements)return;
	uint index=activeList[id];
	int i,j,k;
	getRowColSlice(index,&i,&j,&k);
	int xOff=xShift[offset];
	int yOff=yShift[offset];
	int zOff=zShift[offset];
	if(i%2!=xOff||j%2!=yOff||k%2!=zOff)return;
	
	float v111;
	float DxNeg;
	float DxPos;
	float DyNeg;
	float DyPos;
	float DzNeg;
	float DzPos;
	float forceX,forceY,forceZ;
	float4 grad;
	float oldValue=getImageValue(outLevelSet,i, j, k);
	v111 =getImageValue(signedLevelSet,i, j, k);
	
	if(v111>0.5f||v111<-0.5f){
		outLevelSet[index]=v111;	
		return;
	}
	
	float v010 = getImageValue(signedLevelSet,i - 1, j, k - 1);
	float v120 = getImageValue(signedLevelSet,i, j + 1, k - 1);
	float v110 = getImageValue(signedLevelSet,i, j, k - 1);
	float v100 = getImageValue(signedLevelSet,i, j - 1, k - 1);
	float v210 = getImageValue(signedLevelSet,i + 1, j, k - 1);		
	float v001 = getImageValue(signedLevelSet,i - 1, j - 1, k);
	float v011 = getImageValue(signedLevelSet,i - 1, j, k);
	float v101 = getImageValue(signedLevelSet,i, j - 1, k);
	float v211 = getImageValue(signedLevelSet,i + 1, j, k);
	float v201 = getImageValue(signedLevelSet,i + 1, j - 1, k);
	float v221 = getImageValue(signedLevelSet,i + 1, j + 1, k);
	float v021 = getImageValue(signedLevelSet,i - 1, j + 1, k);
	float v121 = getImageValue(signedLevelSet,i, j + 1, k);
	float v012 = getImageValue(signedLevelSet,i - 1, j, k + 1);
	float v122 = getImageValue(signedLevelSet,i, j + 1, k + 1);
	float v112 = getImageValue(signedLevelSet,i, j, k + 1);
	float v102 = getImageValue(signedLevelSet,i, j - 1, k + 1);
	float v212 = getImageValue(signedLevelSet,i + 1, j, k + 1);
		
	DxNeg = v111 - v011;
	DxPos = v211 - v111;
	DyNeg = v111 - v101;
	DyPos = v121 - v111;
	DzNeg = v111 - v110;
	DzPos = v112 - v111;
	
	
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
	grad=getScaledGradientValue(unsignedLevelSet,i,j,k);
	forceX = weight * grad.x;
	forceY = weight * grad.y;
	forceZ = weight * grad.z;
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
	
	float newValue=v111-clamp(advection+kappa,-0.5f,0.5f);
	int maskIndex=0;
	uint mask=0;
	__const float LEVEL_SET_TOLERANCE=1E-2f;

	if(newValue*oldValue<=0){	
		mask |=((getImageValue(outLevelSet,i-1, j-1,k-1 ) <0) ? (1 << 0) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j-1,k+0 ) <0) ? (1 << 1) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j-1,k+1 ) <0) ? (1 << 2) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j+0,k-1 ) <0) ? (1 << 3) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j+0,k+0 ) <0) ? (1 << 4) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j+0,k+1 ) <0) ? (1 << 5) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j+1,k-1 ) <0) ? (1 << 6) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j+1,k+0 ) <0) ? (1 << 7) : 0);
		mask |=((getImageValue(outLevelSet,i-1, j+1,k+1 ) <0) ? (1 << 8) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j-1,k-1 ) <0) ? (1 << 9) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j-1,k+0 ) <0) ? (1 <<10) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j-1,k+1 ) <0) ? (1 <<11) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j+0,k-1 ) <0) ? (1 <<12) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j+0,k+0 ) <0) ? (1 <<13) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j+0,k+1 ) <0) ? (1 <<14) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j+1,k-1 ) <0) ? (1 <<15) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j+1,k+0 ) <0) ? (1 <<16) : 0);
		mask |=((getImageValue(outLevelSet,i+0, j+1,k+1 ) <0) ? (1 <<17) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j-1,k-1 ) <0) ? (1 <<18) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j-1,k+0 ) <0) ? (1 <<19) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j-1,k+1 ) <0) ? (1 <<20) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j+0,k-1 ) <0) ? (1 <<21) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j+0,k+0 ) <0) ? (1 <<22) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j+0,k+1 ) <0) ? (1 <<23) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j+1,k-1 ) <0) ? (1 <<24) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j+1,k+0 ) <0) ? (1 <<25) : 0);
		mask |=((getImageValue(outLevelSet,i+1, j+1,k+1 ) <0) ? (1 <<26) : 0);
		if(!getBitValue(topoLUT,mask)){
			newValue=sign(oldValue)*0.5f;	
		}
	}
	

	outLevelSet[index]=	newValue;
}
kernel void copyLevelSetImage(global float* srcImage,global float4* destImage){
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

kernel void copyNarrowBand(
	global int* activeList,
	global float4* colorListBuffer,
	global float* srcImage,
	int activeListSize){
	
	int i,j,k;
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
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
	colorListBuffer[gid]=grad;
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
kernel void deleteCountActiveListHistory(
	global int* offsetList,
	const global uint* activeList,
	global float* distanceField,
	global int* indexBuffer,
	global float* levelset,
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
		} else {
			indexBuffer[index]=-1;
			if(times==1)levelset[index]=0.1f+MAX_VEXT;
		}
	}
	offsetList[id]=total;
}
kernel void diffImageLabels( global float* levelset,global char* history){
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	int lab=((levelset[id]<=0)?1:0);
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
__kernel void markStaticSpringls(
		global Springl3D *capsules,
		global char* historyBuffer,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		return;
	}
	Springl3D capsule=capsules[id];
	float4 pt=SCALE_UP*capsule.particle;
	char label=historyBuffer[getSafeIndex((int)round(pt.x),(int)round(pt.y),(int)round(pt.z))];
	if(label==0){
		capsule.particle.w=STATIC_SPRINGL;
		capsules[id]=capsule;
	}
}