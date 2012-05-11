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
inline float lengthSquared(float2 v){
	return (v.x*v.x+v.y*v.y);
}
inline uint getIndex(int i, int j) {
	return (j * ROWS) + i;
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
inline float crossprod(float2 u,float2 v) {
	return u.x * v.y - v.x * u.y;
}
__kernel void relaxNeighbors(__global Springl2D* capsules,__global CapsuleNeighbor2D* capsuleNeighbors,__global float2* updatePoints,float timeStep,uint elements){
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
		tanget=SCALE_UP *(start-particlePt);

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
			dir=SCALE_UP *(pt2-start);
			len = length(dir);
			w = maxForce*clamp(((len - 2 * PARTICLE_RADIUS)/ (MAX_VEXT + 2 * PARTICLE_RADIUS)),-1.0f,1.0f);
			w = WEIGHT_FUNC;
			startVelocity += (w * dir);
		}	
		len = max(1E-6f,length(startVelocity));
		float t=SHARPNESS*len;
		vertexVelocity[i] = timeStep*startVelocity*(THRESHOLD_FUNC / len);
		springForce[i] = timeStep*SPRING_CONSTANT*(2 * PARTICLE_RADIUS - tangetLengths[i]);
		resultantMoment+=crossprod(vertexVelocity[i], tangets[i]);
	}
	float2 tmp;
	resultantMoment*=SCALE_DOWN;
	for (int i = 0; i < 2; i++) {
		start=capsule.vertexes[i]-particlePt;
		dotProd = max(	length(start)
						+ SCALE_DOWN*dot(vertexVelocity[i],tangets[i])
						+ springForce[i],
						0.001f);
	
		start = dotProd*tangets[i];
		tmp.x=	start.x*cos(resultantMoment)+start.y*sin(resultantMoment);
		tmp.y=	-start.x*sin(resultantMoment)+start.y*cos(resultantMoment);
		updatePoints[i]=tmp+particlePt;
	}
	
}


__kernel void computeAdvectionForcesPandV(__global Springl2D* capsules,__global float* pressureImage,__global float* advectionImage,__global float2* forceUpdates,float pressureWeight,float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	float2 force;
	capsules+=id;
	float2 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float2 norm=normalize((float2)(-v1.y,v1.x));		
	float2 pt = capsules->particle;
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float pressure = interpolate(pressureImage,x,y);
	float scale = pressure * pressureWeight;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	
	float vx=interpolateVectorField(advectionImage,x,y, 0);
	float vy=interpolateVectorField(advectionImage,x,y, 1);


	force.x=norm.x+vx*advectionWeight;
	force.y=norm.y+vy*advectionWeight;
	forceUpdates[id]=force;
}
__kernel void computeAdvectionForcesP(__global Springl2D* capsules,__global float* pressureImage,__global float2* forceUpdates,float pressureWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	float2 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float2 norm=normalize((float2)(-v1.y,v1.x));		
	float2 pt = capsules->particle;
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float pressure = interpolate(pressureImage,x,y);
	float scale = pressure * pressureWeight;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	forceUpdates[id]=norm;
}
__kernel void computeAdvectionForcesV(__global Springl2D* capsules,__global float* advectionImage,__global float2* forceUpdates,float advectionWeight,uint elements){
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
__kernel void computeMaxForces(__global float2* forceUpdates,__global float* maxForces,uint stride,uint elements){
	uint id=get_global_id(0);
	float maxForce=0;
	forceUpdates+=id*stride;
	float2 force;
	for(int i=0;i<stride;i++){
		if(i+stride*id>=elements)break;
		force=forceUpdates[i];
		maxForce=max(force.x*force.x+force.y*force.y,maxForce);
	}
	maxForces[id]=maxForce;
}
__kernel void applyForces(__global Springl2D* capsules,__global float* levelSetMat,__global float2* forceUpdates,float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	float2 force=forceUpdates[id];
	float2 startPoint=capsules->particle;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float2 endPoint;
	endPoint.x = (startPoint.x + maxForce * force.x);
	endPoint.y = (startPoint.y + maxForce * force.y);
	float2 displacement=endPoint-startPoint;
	capsules->particle = endPoint;
	capsules->phi=(float)interpolate(levelSetMat,x, y) * SCALE_DOWN;
	capsules->vertexes[0]+=displacement;
	capsules->vertexes[1]+=displacement;	
}
__kernel void applyForcesTopoRule(__global Springl2D* capsules,__global float* levelSetMat,__global float2* forceUpdates,float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	float2 force=forceUpdates[id];
	float2 startPoint=capsules->particle;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float2 endPoint;

	float2 displacement;

	float phi=(float)interpolate(levelSetMat,x, y) * SCALE_DOWN;
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
		lend = interpolate(levelSetMat,x,y) * SCALE_DOWN;
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
