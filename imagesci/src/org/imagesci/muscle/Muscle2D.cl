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
	float2 pt3d;
	int vid;
} EdgeSplit;
//Store capsule id and vertex id [0,1]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor2D;

typedef struct {
	float m00, m01, m02,
		  m10, m11, m12,
		  m20, m21, m22;
} Matrix3f;
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
inline uint getSafeIndex(int i, int j) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return (c * ROWS) + r;
}
inline uint getIndex(int i, int j) {
	return (j * ROWS) + i;
}
inline int clampRow(int row){
	return clamp((int)row,(int)0,(int)(ROWS-1));
}
inline int clampColumn(int col){
	return clamp((int)col,(int)0,(int)(COLS-1));
}
inline float getLevelSetValue(global float* image,global int* labels,int label,uint i,uint j){
	uint r = clamp((uint)i,(uint)0,(uint)(ROWS-1));
	uint c = clamp((uint)j,(uint)0,(uint)(COLS-1));
	uint ii=getIndex(r,c);
	if(labels[ii]==label){
		return -image[ii];
	} else {
		return image[ii];
	}
}
inline float lengthSquared(float2 v){
	return (v.x*v.x+v.y*v.y);
}
inline void getRowCol(uint ij,int* i, int* j) {
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}

inline uint getVectorIndex(int i, int j, int l) {
	return 2*((j * ROWS) + i)+l;
}
inline float interpolateVectorField(__global float* data,float x,float y,int l){
	int y0, x0, y1, x1;
	float dx, dy, hx, hy;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1)) {
			int r = max((int)min((int)x, ROWS - 1), 0);
			int c = max((int)min((int)y, COLS - 1), 0);
			return data[getVectorIndex(r,c,l)];
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
		// Optimized below
		 return (((data[getVectorIndex(x0,y0,l)] * hx + data[getVectorIndex(x1,y0,l)] * dx) * hy + (data[getVectorIndex(x0,y1,l)]
						* hx + data[getVectorIndex(x1,y1,l)] * dx)
						* dy));
	}
}
//Distance between point and triangle edge
//Implementation from geometric tools (http://www.geometrictools.com)
inline float2 edgeDistanceSquared(float2 pt, float2 pt1, float2 pt2) {
	float2 dir=pt2-pt1;
	float len=length(dir);
	dir=normalize(dir);
	float2 diff=pt-pt1;
	
	float mSegmentParameter = dot(dir,diff);
	if (0 < mSegmentParameter) {
		if (mSegmentParameter < len) {
			return dir*mSegmentParameter+pt1;
		} else {
			return pt2;
		}
	} else {
		return pt1;
	}
}
inline float getLabelValue(global int* image,int i,int j){
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	return image[getIndex(r,c)];
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
inline float interpolateLevelSet(__global float* levelset,global int* imageLabels,int label,float x,float y){
	int y0, x0, y1, x1;
	float dx, dy, hx, hy;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1)) {
			int r = max(min((int)x, ROWS - 1), 0);
			int c = max(min((int)y, COLS - 1), 0);
			return getLevelSetValue(levelset,imageLabels,label,r,c);
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

	 return (((getLevelSetValue(levelset,imageLabels,label,x0,y0) * hx + getLevelSetValue(levelset,imageLabels,label,x1,y0) * dx) * hy + (getLevelSetValue(levelset,imageLabels,label,x0,y1)
					* hx + getLevelSetValue(levelset,imageLabels,label,x1,y1) * dx)
					* dy));
	}
}
inline float2 getGradientValue(__global float* image,float i,float j){
	float v211 = interpolate(image, i + 1, j);
	float v121 = interpolate(image, i, j + 1);
	float v101 = interpolate(image, i, j - 1);
	float v011 = interpolate(image, i - 1, j);
	float2 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	return grad;
}

__kernel void applyUpdates(__global Springl2D* capsules,__global float2* updatePoints,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	updatePoints+=2*id;
	capsules+=id;
	capsules->vertexes[0]=updatePoints[0];
	capsules->vertexes[1]=updatePoints[1];
}
__kernel void applyForcesMogac(__global Springl2D* capsules,__global float* levelSetMat,global int* imageLabels,global int* labels,__global float2* forceUpdates,float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	float2 force=forceUpdates[id];
	float2 startPoint=capsules->particle;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	int label=abs(labels[id]);
	float2 endPoint;
	endPoint.x = (startPoint.x + maxForce * force.x);
	endPoint.y = (startPoint.y + maxForce * force.y);
	float2 displacement=endPoint-startPoint;
	capsules->particle = endPoint;
	capsules->phi=(float)interpolateLevelSet(levelSetMat,imageLabels,label,x, y) * SCALE_DOWN;
	capsules->vertexes[0]+=displacement;
	capsules->vertexes[1]+=displacement;	
}
__kernel void applyForcesTopoRuleMogac(__global Springl2D* capsules,__global float* levelSetMat,global int* imageLabels,global int* labels,__global float2* forceUpdates,float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	float2 force=forceUpdates[id];
	float2 startPoint=capsules->particle;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float2 endPoint;

	float2 displacement;
	int label=abs(labels[id]);
	float phi=(float)interpolateLevelSet(levelSetMat,imageLabels,label,x, y) * SCALE_DOWN;
	float lend;
	int tries=0;
	float2 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float2 norm=normalize((float2)(-v1.y,v1.x));	
	float dotprod=dot(force,norm);
	do {
		endPoint.x = (startPoint.x + maxForce * force.x);
		endPoint.y = (startPoint.y + maxForce * force.y);
		x = endPoint.x * SCALE_UP;
		y = endPoint.y * SCALE_UP;
		lend = interpolateLevelSet(levelSetMat,imageLabels,label,x,y) * SCALE_DOWN;
		tries++;
		force*=0.5f;
	} while ((lend - phi) * dotprod < 0&& tries <= 4);
	
	if(tries!=4){				
		capsules->particle = endPoint;
		capsules->phi = phi;
		displacement=endPoint-startPoint;
		capsules->vertexes[0]+=displacement;
		capsules->vertexes[1]+=displacement;
	}
}
inline float crossprod(float2 u,float2 v) {
	return u.x * v.y - v.x * u.y;
}
//Distance squared between two points
inline float distanceSquaredPoint(float2 pt1,float2 pt2){
	float2 v=pt1-pt2;
	return (v.x*v.x+v.y*v.y);
}

//Distance between point and triangle edge
//Implementation from geometric tools (http://www.geometrictools.com)
float edgeDistanceSquaredCapsule(float2 pt, float2 pt1, float2 pt2,float2* lastClosestSegmentPoint) {
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
	return edgeDistanceSquaredCapsule(p,capsule->vertexes[0],capsule->vertexes[1],closestPoint);
}
__kernel void relaxNeighborsMogac(__global Springl2D* capsules,__global CapsuleNeighbor2D* capsuleNeighbors,__global float2* updatePoints,float timeStep,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	float w, len;
	float2 tanget;
	float2 dir;
	__const float maxForce = 0.999f;
	float2 vertexVelocity[2];
	float2 tangets[2];
	float springForce[2];
	float tangetLengths[2];
	capsuleNeighbors+=2*MAX_NEIGHBORS*id;
	Springl2D capsule=capsules[id];
	float2 particlePt=capsule.particle;
	updatePoints+=2*id;
	float2 startVelocity=(float2)(0,0);
	float2 start;
	float dotProd;
	float resultantMoment=0;
	float2 pt2;
	Springl2D nbr;
	CapsuleNeighbor2D ci;
	for (int i = 0; i < 2; i++) {
		// Edge between magnets
		start = capsule.vertexes[i];
		// edge from pivot to magnet
		tanget=start-particlePt;

		tangetLengths[i] = length(tanget);
		if (tangetLengths[i] > 1E-6f) {
			tanget*=(1.0f / tangetLengths[i]);
		}
		tangets[i] = tanget;
		startVelocity=(float2)(0,0);
		// Sum forces
		//unroll loop
		for (int n=0;n<MAX_NEIGHBORS;n++) {
			ci= capsuleNeighbors[MAX_NEIGHBORS*i+n];
			if(ci.capsuleId==-1)break;
			//Closest point should be recomputed each time and does not need to be stored
			nbr=capsules[ci.capsuleId];
			pt2=edgeDistanceSquared(start,nbr.vertexes[ci.vertexId],nbr.vertexes[(ci.vertexId+1)%3]);
			dir=pt2-start;
			len = length(dir);
			w = maxForce*max(-1.0f, min(1.0f,((len - 2 * PARTICLE_RADIUS)/ MAX_RADIUS)));
			w = WEIGHT_FUNC;
			startVelocity += (w * dir);
		}	
		len = length(startVelocity);
		float t=SHARPNESS*len;
		if (len > 1E-6f) {
			startVelocity*=(THRESHOLD_FUNC / len);
		}	
		vertexVelocity[i] = startVelocity;
		springForce[i] = SPRING_CONSTANT* (REST_LENGTH - tangetLengths[i]);
		vertexVelocity[i]*=(SCALE_DOWN * timeStep);	
		resultantMoment+=crossprod(vertexVelocity[i], tangets[i]);
	}
	float2 tmp;
	for (int i = 0; i < 2; i++) {
		start=capsule.vertexes[i]-particlePt;
		dotProd = max(	length(start)
						+ dot(vertexVelocity[i],tangets[i])
						+ springForce[i],
						0.001f);
	
		start = dotProd*tangets[i];
		tmp.x=	start.x*cos(resultantMoment)+start.y*sin(resultantMoment);
		tmp.y=	-start.x*sin(resultantMoment)+start.y*cos(resultantMoment);
		updatePoints[i]=tmp+particlePt;
	}
	
}


__kernel void computeAdvectionForcesPandVMogac(__global Springl2D* capsules,__global float* pressureImage,global int* labels,global int* imageLabels,__global float* advectionImage,__global float2* forceUpdates,float pressureWeight,float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	float2 force;
	capsules+=id;
	int nbrs[4];
	float2 pt = capsules->particle;
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	int i=floor(x);
	int j=floor(y);
	nbrs[0]=getLabelValue(imageLabels,i,j);
	nbrs[1]=getLabelValue(imageLabels,i,j+1);
	nbrs[2]=getLabelValue(imageLabels,i+1,j+1);
	nbrs[3]=getLabelValue(imageLabels,i+1,j);
	int label=abs(labels[id]);
	float vx=interpolateVectorField(advectionImage,x,y, 0);
	float vy=interpolateVectorField(advectionImage,x,y, 1);
	for(int k=0;k<4;k++){
		if(nbrs[k]!=0&&label!=nbrs[k]){
			forceUpdates[id]=(float2)(vx*advectionWeight,vy*advectionWeight);
			return;
		}
	}	
	float2 v1=capsules->vertexes[1]-capsules->vertexes[0];

	float2 norm=normalize((float2)(-v1.y,v1.x));		
	float pressure = interpolate(pressureImage,x,y);
	float scale = pressure * pressureWeight;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	


	force.x=norm.x+vx*advectionWeight;
	force.y=norm.y+vy*advectionWeight;
	forceUpdates[id]=force;
}
__kernel void computeAdvectionForcesPMogac(__global Springl2D* capsules,__global float* pressureImage,global int* labels,global int* imageLabels,__global float2* forceUpdates,float pressureWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	int nbrs[4];
	float2 pt = capsules->particle;
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	int i=floor(x);
	int j=floor(y);
	nbrs[0]=getLabelValue(imageLabels,i,j);
	nbrs[1]=getLabelValue(imageLabels,i,j+1);
	nbrs[2]=getLabelValue(imageLabels,i+1,j+1);
	nbrs[3]=getLabelValue(imageLabels,i+1,j);
	
	int label=abs(labels[id]);
	for(int k=0;k<4;k++){
		if(nbrs[k]!=0&&label!=nbrs[k]){
			forceUpdates[id]=(float2)(0,0);
			return;
		}
	}
	
	float2 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float2 norm=normalize((float2)(-v1.y,v1.x));		

	float pressure = interpolate(pressureImage,x,y);
	float scale = pressure * pressureWeight;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	forceUpdates[id]=norm;
}
__kernel void computeAdvectionForcesVMogac(__global Springl2D* capsules,__global float* advectionImage,__global float2* forceUpdates,float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	float2 force;
	capsules+=id;		
	float2 pt = capsules->particle;
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float vx=interpolateVectorField(advectionImage,x,y, 0);
	float vy=interpolateVectorField(advectionImage,x,y, 1);
	force.x=vx*advectionWeight;
	force.y=vy*advectionWeight;
	forceUpdates[id]=force;
}
__kernel void mapNearestNeighborsMogac(
	__global int* nbrs,
	__global int* indexMap,
	__global int* spatialLookup,
	__global Springl2D* capsules,
	__global int* labels,
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
	int cid,index,startIndex;
	uint hashValue;
	int label=abs(labels[id]);
	//Enumerate all triangles that lie in bounding sphere around point
		for (int j = lowerCol; j < upperCol; j++) {
			for (int i = lowerRow; i < upperRow; i++) {
				hashValue = getSafeIndex(i, j);
				startIndex = MAX_BIN_SIZE*indexMap[hashValue];
				if(startIndex<0)continue;
				for(int index=1;index<MAX_BIN_SIZE;index++){
					cid=spatialLookup[startIndex+index];
					if(cid<0)break;
					if (cid!= id&&abs(labels[cid])==label) { //Ignore the current triangle
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
inline float getNudgedValueMogac(__global float* unsignedLevelSet,__global int* labels,int label,int x,int y){
	float val=getLevelSetValue(unsignedLevelSet,labels,label,x,y);
	if (val < 0) {
		val = min(val, - 0.1f);
	} else {
		val = max(val,  0.1f);
	}
	return val;
}
inline float fGetOffsetMogac(__global float* unsignedLevelSet,__global int* labels,int label,int2 v1, int2 v2) {
		float fValue1 = getNudgedValueMogac(unsignedLevelSet,labels,label,v1.x, v1.y);
		float fValue2 = getNudgedValueMogac(unsignedLevelSet,labels,label,v2.x, v2.y);
		float fDelta = fValue2 - fValue1;
		if (fabs(fDelta) ==0) {
			return 0.5f;
		}
		return clamp(-fValue1 / fDelta,0.0f,1.0f);
}
__kernel void expandGapsMogac(
	__global Springl2D* capsules,
	__global int* labels,
	__global float* signedLevelSet,
	__global int* labelBuffer,
	__global float* unsignedLevelSet,
	const global int* activeList,
	__global int* offsets,
	int sign,
	int elements,
	int activeListSize,
	int label){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y;
	getRowCol(id,&x,&y);			
	int iFlagIndex = 0;
	int vx,vy;
	int count=0;
	float2 mid;
	int offset=(gid>0)?offsets[gid-1]:0;
	capsules+=elements+offset;
	labels+=elements+offset;
	Springl2D cap;
	int2 pt1,pt2;
	float2 spt1,spt2;
	float fInvOffset;
	float fOffset;
	float levelSetValue;
	iFlagIndex = 0;
	for (int iVertex = 0; iVertex < 4; iVertex++) {
		vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
		vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
		if (sign*getLevelSetValue(signedLevelSet,labelBuffer,label,vx, vy) > 0)iFlagIndex |= 1 << iVertex;
	}
	
	if(afSquareValue[iFlagIndex][0]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][1]);
		fOffset = fGetOffsetMogac(signedLevelSet,labelBuffer,label,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][1]);
		fOffset = fGetOffsetMogac(signedLevelSet,labelBuffer,label,pt1,pt2);
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
			labels[count]=-label;
			count++;
		}
	}
	
	if(afSquareValue[iFlagIndex][2]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][1]);
		fOffset = fGetOffsetMogac(signedLevelSet,labelBuffer,label,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][1]);
		fOffset = fGetOffsetMogac(signedLevelSet,labelBuffer,label,pt1,pt2);
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
			labels[count]=-label;
			count++;
		}
	}

}
__kernel void fillGapCountMogac(
	const global float* signedLevelSet,
	const global int* labels,
	const global float* unsignedLevelSet,
	const global int* activeList,
	__global int* counts,
	int sign,
	int activeListSize,
	int label) {
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
	int count=0;
	float2 mid;
	float levelSetValue;
	iFlagIndex = 0;
	for (int iVertex = 0; iVertex < 4; iVertex++) {
		vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
		vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
		if (sign*getLevelSetValue(signedLevelSet,labels,label,vx, vy) > 0)iFlagIndex |= 1 << iVertex;
	}
	
	if(afSquareValue[iFlagIndex][0]<4){
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][1]);
		fOffset = fGetOffsetMogac(signedLevelSet,labels,label,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][1]);
		fOffset = fGetOffsetMogac(signedLevelSet,labels,label,pt1,pt2);
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
		fOffset = fGetOffsetMogac(signedLevelSet,labels,label,pt1,pt2);
		fInvOffset = 1.0f - fOffset;
		spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
		spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
		
		pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][1]);
		pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][1]);
		fOffset = fGetOffsetMogac(signedLevelSet,labels,label,pt1,pt2);
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
__kernel void fixLabelsMogac(
		__global Springl2D* capsules,
		__global CapsuleNeighbor2D* capsuleNeighbors,
		__global float* origUnsignedLevelSet,
		__global int* labels,uint N){
	uint id=get_global_id(0);
	int oldLabel=0;
	if(id>=N||(oldLabel=labels[id])>=0)return;		
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
	oldLabel=abs(oldLabel);
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
			if(w<minDist&&label==oldLabel){
				minDist=w;
				newLabel=oldLabel;
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
	if(newLabel<0)return;
	capsule.mapping=newPoint;
	capsules[id]=capsule;
	labels[id]=newLabel;
}
//Compute level set value in reduction phase
__kernel void reduceLevelSetMogac(
		global int* activeList,
		global int* spatialLookup,
		global Springl2D* capsules,
		global float* imageMat,
		global int* labels,
		uint activeListSize,
		int label){
	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int i,j,k;
	getRowCol(id,&i,&j);
	float2 pt;
	float2 ret;
	spatialLookup+=gid*MAX_BIN_SIZE;
	Springl2D cap;
	pt = (float2)(
		i * SCALE_DOWN,
		j * SCALE_DOWN);
	float value = (0.1f+MAX_VEXT)*(0.1f+MAX_VEXT);
	for(int index=1;index<MAX_BIN_SIZE;index++){
		int binId=spatialLookup[index];
		if(binId<0)break;
		if(abs(labels[binId])==label){
			cap = capsules[binId];
			//Compute distance squared between point and triangle
			float d2 = distanceSquared(pt,&cap,&ret);
			if (d2 < value) {
				value = d2;
			}
		}
	}
	value = native_sqrt(value);
	imageMat[id] = value;
}
kernel void deleteCountActiveListMogac(
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
		if(distanceField[index]<=MAX_DISTANCE){
			total++;
		}  else {
			indexBuffer[index]=-1;
			levelset[index]=0.1f+MAX_VEXT;
		}
	}
	offsetList[id]=total;
}