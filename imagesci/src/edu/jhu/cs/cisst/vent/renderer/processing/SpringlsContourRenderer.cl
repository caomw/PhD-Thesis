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
inline int clampRow(int row){
	return clamp((int)row,(int)0,(int)(ROWS-1));
}
inline int clampColumn(int col){
	return clamp((int)col,(int)0,(int)(COLS-1));
}
#if ATI
	#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable 
#endif 
#define WORLD_RADIUS 1000.f
#define WORLD_CENTER ((float4)(0.f, -WORLD_RADIUS - 2.f, 0.f, 0.f))
#define BOUNDING_RADIUS_2 4.f
#define ESCAPE_THRESHOLD 1e1f
#define DELTA 1e-4f
typedef struct {
    float x, y, z; // position, also color (r,g,b)
} Vec;

typedef struct {
    Vec orig, target;
    Vec dir, x, y;
} Camera;

typedef struct {
    unsigned int width, height;
    int superSamplingSize;
    int actvateFastRendering;
    int enableShadow;

    unsigned int maxIterations;
    float epsilon;
    float color[4];
    float light[3];
    Camera camera;
} RenderingConfig;

typedef struct{
	float2 particle;
	float2 mapping;
    float2 vertexes[2];
    float phi;
    float psi; //not used, but it makes the number of values even!
} Springl2D;

inline int getIndex(int i, int j) {
	return (j * ROWS) + i;
}
inline void getRowCol(uint index,int* i, int* j) {
	(*j)=index/ROWS;
	(*i)=index-(*j)*ROWS;
}
inline float getImageValue(__global float* image,int i,int j){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return image[getIndex(r,c)];
}
kernel void copyCapsulesToMesh(

		global int* labels,
		global Springl2D* capsules,
		global float2* vertexBuffer,
		global float2* particleBuffer,
		global float2* normalSegmentBuffer,
		global float2* mapPointBuffer,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	vertexBuffer+=id*2;
	float2 particle=SCALE_UP*capsules->particle;
	float2 mapping=SCALE_UP*capsules->mapping;
	particleBuffer[id]=particle;
	//float2 tanget=capsules->vertexes[1]-capsules->vertexes[0];
	//tanget=0.5f*normalize(tanget);
	vertexBuffer[0]=SCALE_UP*capsules->vertexes[0];
	vertexBuffer[1]=SCALE_UP*capsules->vertexes[1];
	float2 norm;
	norm.x=vertexBuffer[0].y-vertexBuffer[1].y;
	norm.y=vertexBuffer[1].x-vertexBuffer[0].x;
	norm=normalize(norm);
	
	normalSegmentBuffer[2*id]=particle;
	normalSegmentBuffer[2*id+1]=particle+0.5f*norm;
	
	mapPointBuffer[2*id]=particle;
	mapPointBuffer[2*id+1]=mapping;

}
kernel void springlsContourRenderer(const global float* levelset,global float4* pixelBuffer,float r,float g,float b,float alpha){
    uint id = get_global_id(0);
   	int i,j;
	getRowCol(id,&i,&j);
	float level=levelset[id];
	float weight=tanh(fabs(level));
	float4 color=(float4)(r,g,b,alpha);
	if(level<=0){
		pixelBuffer[id]=weight*color+(1-weight)*(float4)(0,0,0,alpha);
	} else {
		pixelBuffer[id]=(1-weight)*(float4)(0,0,0,alpha);
	}
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
	int offset=(id>0)?offsets[id-1]:0;
	vertexes+=offset;
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