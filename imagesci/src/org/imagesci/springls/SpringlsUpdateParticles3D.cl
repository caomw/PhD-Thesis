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
 #define STATIC_SPRINGL (-1E8f)
typedef struct{
	float4 particle;
    float4 vertexes[3];
} Springl3D;

//Store capsule id and vertex id [0,1,2]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor3D;

typedef struct {
	float m00, m01, m02, m03,
		  m10, m11, m12, m13,
		  m20, m21, m22, m23,
		  m30, m31, m32, m33;
} Matrix4f;
inline float lengthSquared(float4 v){
	return (v.x*v.x+v.y*v.y+v.z*v.z);
}
inline uint getIndex(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
}
inline uint getVectorIndex(int i, int j, int k,int l) {
	int r = clamp((int)i,(int)0,(int)(ROWS-1));
	int c = clamp((int)j,(int)0,(int)(COLS-1));
	int s = clamp((int)k,(int)0,(int)(SLICES-1));
	return 3*((s * (ROWS * COLS)) + (c * ROWS) + r)+l;
}
inline float interpolateVectorField(__global float* data,float x,float y,float z,int l){
	int y0, x0, z0, y1, x1, z1;
	float dx, dy, dz, hx, hy, hz;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1) || z < 0
			|| z > (SLICES - 1)) {
			int r = max((int)min((int)x, ROWS - 1), 0);
			int c = max((int)min((int)y, COLS - 1), 0);
			int s = max((int)min((int)z, SLICES - 1), 0);
			return data[getVectorIndex(r,c,s,l)];
	} else {
		x1 = ceil(x);
		y1 = ceil(y);
		z1 = ceil(z);
		x0 = floor(x);
		y0 = floor(y);
		z0 = floor(z);
		dx = x - x0;
		dy = y - y0;
		dz = z - z0;
	
		// Introduce more variables to reduce computation
		hx = 1.0f - dx;
		hy = 1.0f - dy;
		hz = 1.0f - dz;
		// Optimized below
		return (((data[getVectorIndex(x0,y0,z0,l)] * hx + data[getVectorIndex(x1,y0,z0,l)] * dx) * hy + (data[getVectorIndex(x0,y1,z0,l)]
				* hx + data[getVectorIndex(x1,y1,z0,l)] * dx)
				* dy)
				* hz + ((data[getVectorIndex(x0,y0,z1,l)] * hx + data[getVectorIndex(x1,y0,z1,l)] * dx)
				* hy + (data[getVectorIndex(x0,y1,z1,l)] * hx + data[getVectorIndex(x1,y1,z1,l)] * dx) * dy)
				* dz);
	}
}
//Distance between point and triangle edge
//Implementation from geometric tools (http://www.geometrictools.com)
inline float4 edgeDistanceSquared(float4 pt, float4 pt1, float4 pt2) {
	pt.w=0;
	pt1.w=0;
	pt2.w=0;
	float4 dir=pt2-pt1;
	float len=length(dir);
	dir=normalize(dir);
	float4 diff=pt-pt1;
	
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

inline float interpolate(__global float* data,float x,float y,float z){
	int y0, x0, z0, y1, x1, z1;
	float dx, dy, dz, hx, hy, hz;
	if (x < 0 || x > (ROWS - 1) || y < 0 || y > (COLS - 1) || z < 0
			|| z > (SLICES - 1)) {
			int r = max(min((int)x, ROWS - 1), 0);
			int c = max(min((int)y, COLS - 1), 0);
			int s = max(min((int)z, SLICES - 1), 0);
			return data[getIndex(r,c,s)];
	} else {
		x1 = ceil(x);
		y1 = ceil(y);
		z1 = ceil(z);
		x0 = floor(x);
		y0 = floor(y);
		z0 = floor(z);
		dx = x - x0;
		dy = y - y0;
		dz = z - z0;
	
		// Introduce more variables to reduce computation
		hx = 1.0f - dx;
		hy = 1.0f - dy;
		hz = 1.0f - dz;
		// Optimized below
		return (((data[getIndex(x0,y0,z0)] * hx + data[getIndex(x1,y0,z0)] * dx) * hy + (data[getIndex(x0,y1,z0)]
				* hx + data[getIndex(x1,y1,z0)] * dx)
				* dy)
				* hz + ((data[getIndex(x0,y0,z1)] * hx + data[getIndex(x1,y0,z1)] * dx)
				* hy + (data[getIndex(x0,y1,z1)] * hx + data[getIndex(x1,y1,z1)] * dx) * dy)
				* dz);
	}
}
inline float4 getGradientValue(__global float* image,float i,float j,float k){
	float v211 = interpolate(image, i + 1, j, k);
	float v121 = interpolate(image, i, j + 1, k);
	float v101 = interpolate(image, i, j - 1, k);
	float v011 = interpolate(image, i - 1, j, k);
	float v110 = interpolate(image, i, j, k - 1);
	float v112 = interpolate(image, i, j, k + 1);
	float4 grad;
	grad.x = 0.5f*(v211-v011);
	grad.y = 0.5f*(v121-v101);
	grad.z = 0.5f*(v112-v110);
	grad.w = 1E-6f;
	return grad;
}
inline Matrix4f multMatrix(Matrix4f M,Matrix4f A){
        float       m00, m01, m02, m03,
                    m10, m11, m12, m13,
                    m20, m21, m22, m23,
                    m30, m31, m32, m33;  // vars for temp result matrix

        m00 = M.m00*A.m00 + M.m01*A.m10 + 
              M.m02*A.m20 + M.m03*A.m30;
        m01 = M.m00*A.m01 + M.m01*A.m11 + 
              M.m02*A.m21 + M.m03*A.m31;
        m02 = M.m00*A.m02 + M.m01*A.m12 + 
              M.m02*A.m22 + M.m03*A.m32;
        m03 = M.m00*A.m03 + M.m01*A.m13 + 
              M.m02*A.m23 + M.m03*A.m33;

        m10 = M.m10*A.m00 + M.m11*A.m10 + 
              M.m12*A.m20 + M.m13*A.m30; 
        m11 = M.m10*A.m01 + M.m11*A.m11 + 
              M.m12*A.m21 + M.m13*A.m31;
        m12 = M.m10*A.m02 + M.m11*A.m12 + 
              M.m12*A.m22 + M.m13*A.m32;
        m13 = M.m10*A.m03 + M.m11*A.m13 + 
              M.m12*A.m23 + M.m13*A.m33;

        m20 = M.m20*A.m00 + M.m21*A.m10 + 
              M.m22*A.m20 + M.m23*A.m30; 
        m21 = M.m20*A.m01 + M.m21*A.m11 + 
              M.m22*A.m21 + M.m23*A.m31;
        m22 = M.m20*A.m02 + M.m21*A.m12 + 
              M.m22*A.m22 + M.m23*A.m32;
        m23 = M.m20*A.m03 + M.m21*A.m13 + 
              M.m22*A.m23 + M.m23*A.m33;

        m30 = M.m30*A.m00 + M.m31*A.m10 + 
              M.m32*A.m20 + M.m33*A.m30; 
        m31 = M.m30*A.m01 + M.m31*A.m11 + 
              M.m32*A.m21 + M.m33*A.m31;
        m32 = M.m30*A.m02 + M.m31*A.m12 + 
              M.m32*A.m22 + M.m33*A.m32;
        m33 = M.m30*A.m03 + M.m31*A.m13 + 
              M.m32*A.m23 + M.m33*A.m33;
 
        M.m00 = m00; M.m01 = m01; M.m02 = m02; M.m03 = m03;
        M.m10 = m10; M.m11 = m11; M.m12 = m12; M.m13 = m13;
        M.m20 = m20; M.m21 = m21; M.m22 = m22; M.m23 = m23;
        M.m30 = m30; M.m31 = m31; M.m32 = m32; M.m33 = m33;
        return M;
}
inline float4 transform4(float4 vec,Matrix4f M){
           float4 vecOut;
            vecOut.x = M.m00*vec.x + M.m01*vec.y
                     + M.m02*vec.z + M.m03*vec.w;
            vecOut.y = M.m10*vec.x + M.m11*vec.y
                     + M.m12*vec.z + M.m13*vec.w;
            vecOut.z = M.m20*vec.x + M.m21*vec.y
                     + M.m22*vec.z + M.m23*vec.w;
            vecOut.w =  M.m30*vec.x + M.m31*vec.y
                      + M.m32*vec.z + M.m33*vec.w;
           return vecOut;
}
inline float4 transform3(float4 vec,Matrix4f M){
           float4 vecOut;
            vecOut.x = M.m00*vec.x + M.m01*vec.y
                     + M.m02*vec.z + M.m03;
            vecOut.y = M.m10*vec.x + M.m11*vec.y
                     + M.m12*vec.z + M.m13;
            vecOut.z = M.m20*vec.x + M.m21*vec.y
                     + M.m22*vec.z + M.m23;
            vecOut.w = 0;
           return vecOut;
}
Matrix4f createAxisAngleMatrix(float4 a1,float angle){
	 Matrix4f M;
     float mag = sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);
     if( mag < 1E-6f ) {
		 M.m00 = 1.0f;
		 M.m01 = 0.0f;
		 M.m02 = 0.0f;
	
		 M.m10 = 0.0f;
		 M.m11 = 1.0f;
		 M.m12 = 0.0f;
	
		 M.m20 = 0.0f;
	 	 M.m21 = 0.0f;
		 M.m22 = 1.0f;
     } else {
	 	mag = 1.0f/mag;
         float ax = a1.x*mag;
         float ay = a1.y*mag;
         float az = a1.z*mag;
         float sinTheta = (float)sin(angle);
         float cosTheta = (float)cos(angle);
         float t = 1.0f - cosTheta;
         
         float xz = ax * az;
         float xy = ax * ay;
         float yz = ay * az;
         
         M.m00 = t * ax * ax + cosTheta;
         M.m01 = t * xy - sinTheta * az;
         M.m02 = t * xz + sinTheta * ay;

         M.m10 = t * xy + sinTheta * az;
         M.m11 = t * ay * ay + cosTheta;
         M.m12 = t * yz - sinTheta * ax;

         M.m20 = t * xz - sinTheta * ay;
         M.m21 = t * yz + sinTheta * ax;
         M.m22 = t * az * az + cosTheta;
      }
      M.m03 = 0.0f;
      M.m13 = 0.0f;
      M.m23 = 0.0f;

      M.m30 = 0.0f;
      M.m31 = 0.0f;
      M.m32 = 0.0f;
      M.m33 = 1.0f;
      return M;
}
__kernel void applyUpdates(__global Springl3D* capsules,__global float4* updatePoints,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	updatePoints+=3*id;
	Springl3D capsule=capsules[id];
	if(capsule.particle.w==STATIC_SPRINGL)return;
	for(int i=0;i<3;i++){
		float4 pt=updatePoints[i];
		capsule.vertexes[i]=(float4)(pt.x,pt.y,pt.z,capsule.vertexes[i].w);
	}
	capsules[id]=capsule;
}
__kernel void relaxNeighbors(__global Springl3D* capsules,__global CapsuleNeighbor3D* capsuleNeighbors,__global float4* updatePoints,float timeStep,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	float w, len;
	float4 tanget;
	float4 dir;
	__const float maxForce = 0.999f;
	float4 vertexVelocity[3];
	float4 tangets[3];
	float springForce[3];
	float tangetLengths[3];
	capsuleNeighbors+=3*MAX_NEIGHBORS*id;
	Springl3D capsule=capsules[id];
	float4 particlePt=capsule.particle;
	if(particlePt.w==STATIC_SPRINGL)return;
	particlePt.w=0;
	updatePoints+=3*id;
	float4 startVelocity=(float4)(0,0,0,0);
	float4 start;
	float dotProd;
	float4 resultantMoment=(float4)(0,0,0,0);
	float4 pt2;
	Springl3D nbr;
	CapsuleNeighbor3D ci;
	for (int i = 0; i < 3; i++) {
		// Edge between magnets
		start = capsule.vertexes[i];
		start.w=0;
		// edge from pivot to magnet
		tanget=SCALE_UP *(start-particlePt);

		tangetLengths[i] = length(tanget);
		if (tangetLengths[i] > 1E-6f) {
			tanget*=(1.0f / tangetLengths[i]);
		}
		tangets[i] = tanget;
		startVelocity=(float4)(0,0,0,0);
		// Sum forces
		//unroll loop
		for (int n=0;n<MAX_NEIGHBORS;n++) {
			ci= capsuleNeighbors[MAX_NEIGHBORS*i+n];
			if(ci.capsuleId==-1)break;
			//Closest point should be recomputed each time and does not need to be stored
			nbr=capsules[ci.capsuleId];
			pt2=edgeDistanceSquared(start,nbr.vertexes[ci.vertexId],nbr.vertexes[(ci.vertexId+1)%3]);
			dir=SCALE_UP * (pt2-start);
			len = length(dir);
			w = maxForce*clamp(((len - 2 * PARTICLE_RADIUS)/ (MAX_VEXT + 2 * PARTICLE_RADIUS)),-1.0f,1.0f);
			w = WEIGHT_FUNC;
			startVelocity += (w * dir);
		}	
		len = max(1E-6f,length(startVelocity));
		float t=SHARPNESS*len;
		vertexVelocity[i] = timeStep*startVelocity*(THRESHOLD_FUNC / len);
		springForce[i] = timeStep*SPRING_CONSTANT*(2 * PARTICLE_RADIUS - tangetLengths[i]);
		resultantMoment+=cross(vertexVelocity[i], tangets[i]);
	}
	Matrix4f rot = createAxisAngleMatrix(resultantMoment,-SCALE_DOWN*length(resultantMoment));
	for (int i = 0; i < 3; i++) {
		start=capsule.vertexes[i]-particlePt;
		start.w=0;
		dotProd = max(length(start) + SCALE_DOWN*(dot(vertexVelocity[i],tangets[i]) + springForce[i]),0.001f);
		start = dotProd*tangets[i];
		start=transform3(start,rot);
		updatePoints[i]=start+particlePt;
	}
	
}


__kernel void computeAdvectionForcesPandV(
	__global Springl3D* capsules,
	const global float* pressureImage,
	const global float* advectionImage,
	__global float* forceUpdates,
	float pressureWeight,float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=3*id;
	capsules+=id;
	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));		
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float z = pt.z * SCALE_UP;
	float pressure = interpolate(pressureImage,x,y,z);
	float scale = pressure * pressureWeight;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	norm.z *= (scale * SCALE_DOWN);
	norm.w = 0;
	
	float vx=interpolateVectorField(advectionImage,x,y,z, 0);
	float vy=interpolateVectorField(advectionImage,x,y,z, 1);
	float vz=interpolateVectorField(advectionImage,x,y,z, 2);

	forceUpdates[0]=norm.x+vx*advectionWeight;
	forceUpdates[1]=norm.y+vy*advectionWeight;
	forceUpdates[2]=norm.z+vz*advectionWeight;
}
__kernel void computeAdvectionForcesNoResamplePandV(
	__global Springl3D* capsules,
	const global float* pressureImage,
	const global float* advectionImage,
	const global float* signedLevelSet,
	__global float* forceUpdates,
	float pressureWeight,float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	capsules+=id;	
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;	
	float4 q;
	float4 norm;
	for(int n=0;n<3;n++){
		q=SCALE_UP*capsules->vertexes[n];
		float pressure = interpolate(pressureImage,q.x,q.y,q.z);
		float scale = pressure * pressureWeight* SCALE_DOWN;
		
		float vx=interpolateVectorField(advectionImage,q.x,q.y,q.z, 0);
		float vy=interpolateVectorField(advectionImage,q.x,q.y,q.z, 1);
		float vz=interpolateVectorField(advectionImage,q.x,q.y,q.z, 2);
		
		norm=scale*normalize(getGradientValue(signedLevelSet,q.x,q.y,q.z));	
		forceUpdates[0]=norm.x+vx*advectionWeight;
		forceUpdates[1]=norm.y+vy*advectionWeight;
		forceUpdates[2]=norm.z+vz*advectionWeight;
		forceUpdates+=3;
	}
}
__kernel void computeAdvectionForcesP(__global Springl3D* capsules,__global float* pressureImage,__global float* forceUpdates,float pressureWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=3*id;
	capsules+=id;
	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));		
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;	
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float z = pt.z * SCALE_UP;
	float pressure = interpolate(pressureImage,x,y,z);
	float scale = pressure * pressureWeight;
	norm.x *= (scale * SCALE_DOWN);
	norm.y *= (scale * SCALE_DOWN);
	norm.z *= (scale * SCALE_DOWN);
	norm.w = 0;

	forceUpdates[0]=norm.x;
	forceUpdates[1]=norm.y;
	forceUpdates[2]=norm.z;
}
__kernel void computeAdvectionForcesNoResampleP(
	__global Springl3D* capsules,
	const global float* pressureImage,
	const global float* signedLevelSet,
	__global float* forceUpdates,float pressureWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	capsules+=id;	
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;	
	float4 q;
	float4 norm;
	for(int n=0;n<3;n++){
		q=SCALE_UP*capsules->vertexes[n];
		float pressure = interpolate(pressureImage,q.x,q.y,q.z);
		float scale = pressure * pressureWeight* SCALE_DOWN;
		norm=scale*normalize(getGradientValue(signedLevelSet,q.x,q.y,q.z));	
		forceUpdates[0]=norm.x;
		forceUpdates[1]=norm.y;
		forceUpdates[2]=norm.z;
		forceUpdates+=3;
	}
}
__kernel void computeAdvectionForcesV(
	__global Springl3D* capsules,
	__global float* advectionImage,
	__global float* forceUpdates,
	float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=3*id;
	capsules+=id;		
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;
	float x = pt.x * SCALE_UP;
	float y = pt.y * SCALE_UP;
	float z = pt.z * SCALE_UP;
	
	float vx=interpolateVectorField(advectionImage,x,y,z, 0);
	float vy=interpolateVectorField(advectionImage,x,y,z, 1);
	float vz=interpolateVectorField(advectionImage,x,y,z, 2);

	forceUpdates[0]=vx*advectionWeight;
	forceUpdates[1]=vy*advectionWeight;
	forceUpdates[2]=vz*advectionWeight;
}
__kernel void computeAdvectionForcesNoResampleV(
	__global Springl3D* capsules,
	const global float* advectionImage,
	__global float* forceUpdates,
	float advectionWeight,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	forceUpdates+=9*id;
	capsules+=id;	
	float4 pt = capsules->particle;
	if(pt.w==STATIC_SPRINGL)return;	
	float4 q;
	for(int n=0;n<3;n++){
		q=SCALE_UP*capsules->vertexes[n];
		float vx=interpolateVectorField(advectionImage,q.x,q.y,q.z, 0);
		float vy=interpolateVectorField(advectionImage,q.x,q.y,q.z, 1);
		float vz=interpolateVectorField(advectionImage,q.x,q.y,q.z, 2);
		forceUpdates[0]=vx*advectionWeight;
		forceUpdates[1]=vy*advectionWeight;
		forceUpdates[2]=vz*advectionWeight;
		forceUpdates+=3;
	}
}
__kernel void computeMaxForces(__global float* forceUpdates,__global float* maxForces,uint stride,uint elements){
	uint id=get_global_id(0);
	float maxForce=0;
	if(id*stride>elements)return;
	forceUpdates+=3*id*stride;
	float x,y,z;
	int sz=min(elements-id*stride,stride);
	for(int i=0;i<sz;i++){
		if(i+stride*id>=elements)break;
		x=forceUpdates[3*i];
		y=forceUpdates[3*i+1];
		z=forceUpdates[3*i+2];
		maxForce=max(x*x+y*y+z*z,maxForce);
	}
	maxForces[id]=maxForce;
}
__kernel void applyForces(__global Springl3D* capsules,__global float* levelSetMat,__global float* forceUpdates,float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	forceUpdates+=3*id;
	float4 startPoint=capsules->particle;
	if(startPoint.w==STATIC_SPRINGL)return;
	
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float z = startPoint.z *SCALE_UP;
	float4 endPoint;

	endPoint.x = (startPoint.x + maxForce * forceUpdates[0]);
	endPoint.y = (startPoint.y + maxForce * forceUpdates[1]);
	endPoint.z = (startPoint.z + maxForce * forceUpdates[2]);
	endPoint.w = 0;
	float4 displacement=endPoint-startPoint;
	displacement.w=0;
	
	endPoint.w = (float)interpolate(levelSetMat,x, y, z) * SCALE_DOWN;
	capsules->particle = endPoint;
	
	for(int i=0;i<3;i++){
		capsules->vertexes[i]+=displacement;
	}
	
}
__kernel void applyForcesNoResample(
	__global Springl3D* capsules,
	__global float* levelSetMat,
	__global float* forceUpdates,
	float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	forceUpdates+=9*id;
	float4 startPoint=capsules->particle;
	if(startPoint.w==STATIC_SPRINGL)return;
	startPoint=(float4)(0,0,0,0);
	float4 endPoint;
	for(int i=0;i<3;i++){
		endPoint=capsules->vertexes[i];
		float w=endPoint.w;
		endPoint.x+=maxForce * forceUpdates[0];
		endPoint.y+=maxForce * forceUpdates[1];
		endPoint.z+=maxForce * forceUpdates[2];
		startPoint+=endPoint;
		endPoint.w=w;
		capsules->vertexes[i]=endPoint;
		forceUpdates+=3;
	}
	startPoint*=0.333333f;
	
	startPoint.w = (float)interpolate(levelSetMat,SCALE_UP*startPoint.x, SCALE_UP*startPoint.y, SCALE_UP*startPoint.z) * SCALE_DOWN;
	capsules->particle =startPoint;
}
__kernel void applyForcesTopoRule(__global Springl3D* capsules,__global float* levelSetMat,__global float* forceUpdates,float maxForce,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	forceUpdates+=3*id;
	float4 startPoint=capsules->particle;
	if(startPoint.w==STATIC_SPRINGL)return;
	
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float z = startPoint.z *SCALE_UP;
	float4 endPoint;
	
	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));
	
	// Adjust speed so that contour does not cross into narrow band
	// of a neighboring contour.
	// This is only possible if a topology rule is in effect.
	
	float lend;
	
	float4 displacement;
	displacement.w=0;
	endPoint.w=(float)interpolate(levelSetMat,x, y, z) * SCALE_DOWN;
	int tries=0;
	
	float4 forceUpdate=(float4)(forceUpdates[0],forceUpdates[1],forceUpdates[2],0);
	
	float dotprod=dot(forceUpdate,norm);
	do {
		endPoint.x = (startPoint.x + maxForce * forceUpdate.x);
		endPoint.y = (startPoint.y + maxForce * forceUpdate.y);
		endPoint.z = (startPoint.z + maxForce * forceUpdate.z);
		x = endPoint.x * SCALE_UP;
		y = endPoint.y * SCALE_UP;
		z = endPoint.z * SCALE_UP;
		lend = interpolate(levelSetMat,x,y,z) * SCALE_DOWN;
		tries++;
		forceUpdate*=0.5f;
	} while ((lend - endPoint.w) * dotprod < 0&& tries <= 4);
	
	if(tries!=4){				
		capsules->particle = endPoint;
		displacement=endPoint-startPoint;
		for(int i=0;i<3;i++){
			capsules->vertexes[i]+=displacement;
		}
	}
}
			
			
			
			
			
__kernel void updateParticleLevelSet(__global Springl3D* capsules,__global float* levelSetMat,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	capsules+=id;
	float4 startPoint=capsules->particle;
	float x = startPoint.x *SCALE_UP;
	float y = startPoint.y *SCALE_UP;
	float z = startPoint.z *SCALE_UP;
	startPoint.w = (float)interpolate(levelSetMat,x, y, z) * SCALE_DOWN;
	capsules->particle = startPoint;
	float4 v1=capsules->vertexes[1]-capsules->vertexes[0];
	float4 v2=capsules->vertexes[2]-capsules->vertexes[0];
	v1.w=0;
	v2.w=0;
	float4 norm=normalize(cross(v1,v2));
	float4 grad=getGradientValue(levelSetMat,x,y,z);
	if(dot(norm,grad)<0){
		v1=capsules->vertexes[1];
		v2=capsules->vertexes[2];
		capsules->vertexes[2]=v1;
		capsules->vertexes[1]=v2;
	}
}
float4 zalesak(float4 pt){
	float4 vel;
	const float FLOAT_PI=3.14159654f;
	vel.y=FLOAT_PI*(SCALE_DOWN*0.5f*ROWS-pt.x)/314.0f;
	vel.x=FLOAT_PI*(pt.y-SCALE_DOWN*0.5f*COLS)/314.0f;
	vel.z=0;
	vel.w=0;	
	return vel;	
}
__kernel void advectZalesak(__global Springl3D* capsules,float h,uint elements){
	uint id=get_global_id(0);
	float pt0,pt1,pt2;
	if(id>=elements)return;
    Springl3D capsule=capsules[id];
	float4 pt;
	float verts[4]={capsule.vertexes[0].w,capsule.vertexes[1].w,capsule.vertexes[2].w,capsule.particle.w};
	float4 vel;
	pt=capsule.particle;
	pt.w=1;
	float4 k1=h*zalesak(pt);
	float4 k2=h*zalesak(pt+0.5f*k1);
	float4 k3=h*zalesak(pt+0.5f*k2);
	float4 k4=h*zalesak(pt+k3);
	vel=(1.0f/6.0f)*(k1+2*k2+2*k3+k4);
	
	pt=pt+vel;		
	pt.w=verts[3];
	capsule.particle=pt;
	
	pt=capsule.vertexes[0]+vel;
	pt.w=verts[0];
	capsule.vertexes[0]=pt;

	pt=capsule.vertexes[1]+vel;
	pt.w=verts[1];
	capsule.vertexes[1]=pt;
	
	pt=capsule.vertexes[2]+vel;
	pt.w=verts[2];
	capsule.vertexes[2]=pt;

	capsules[id]=capsule;
	
}
float4 enright(float t,float4 pt,float T){
	float su=sinpi(SCALE_UP/ROWS*pt.x);
	float sv=sinpi(SCALE_UP/COLS*pt.y);
	float sw=sinpi(SCALE_UP/SLICES*pt.z);
	float4 vel;
	float dt=1.28f*cospi(t/T);
	vel.x=SCALE_DOWN*2*su*su*sinpi(2*SCALE_UP/COLS*pt.y)*sinpi(2*SCALE_UP/SLICES*pt.z);
	vel.y=-SCALE_DOWN*sinpi(2*SCALE_UP/ROWS*pt.x)*sv*sv*sinpi(2*SCALE_UP/SLICES*pt.z);
	vel.z=-SCALE_DOWN*sinpi(2*SCALE_UP/ROWS*pt.x)*sinpi(2*SCALE_UP/COLS*pt.y)*sw*sw;
	vel.w=0;
	return vel*dt;
}
__kernel void advectEnright(__global Springl3D* capsules,float h,float t,float T,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;
	Springl3D capsule=capsules[id];
	float4 pt;
	float verts[4]={capsule.vertexes[0].w,capsule.vertexes[1].w,capsule.vertexes[2].w,capsule.particle.w};
	float4 vel;

	pt=capsule.particle;
	pt.w=1;
	float4 k1=h*enright(t,pt,T);
	float4 k2=h*enright(t+0.5f*h,pt+0.5*k1,T);
	float4 k3=h*enright(t+0.5f*h,pt+0.5*k2,T);
	float4 k4=h*enright(t+h,pt+k3,T);
	vel=(1.0f/6.0f)*(k1+2*k2+2*k3+k4);
	
	pt=pt+vel;
	pt.w=verts[3];
	capsule.particle=pt;
	
	pt=capsule.vertexes[0]+vel;
	pt.w=verts[0];
	capsule.vertexes[0]=pt;

	pt=capsule.vertexes[1]+vel;
	pt.w=verts[1];
	capsule.vertexes[1]=pt;
	
	pt=capsule.vertexes[2]+vel;
	pt.w=verts[2];
	capsule.vertexes[2]=pt;

	capsules[id]=capsule;
}