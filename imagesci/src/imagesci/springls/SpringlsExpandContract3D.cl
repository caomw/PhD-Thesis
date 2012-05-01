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
typedef struct{
	int4 e1;
	int4 e2;
	float4 pt3d;
	int vid;
} EdgeSplit;
//Store capsule id and vertex id [0,1,2]
typedef struct {
	int capsuleId;
	uint vertexId;
} CapsuleNeighbor3D;
typedef struct {
	float4 points[3];
} Triangle;
inline float lengthSquared(float4 v){
	return (v.x*v.x+v.y*v.y+v.z*v.z);
}
inline int clampRow(int row){
	return clamp((int)row,(int)0,(int)(ROWS-1));
}
inline int clampColumn(int col){
	return clamp((int)col,(int)0,(int)(COLS-1));
}
inline int clampSlice(int slice){
	return clamp((int)slice,(int)0,(int)(SLICES-1));
}
inline void getRowColSlice(uint index,int* i, int* j, int* k) {
	(*k)=index/(ROWS*COLS);
	int ij=index-(*k)*(ROWS * COLS);
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}
inline float angle(float4 v0,float4 v1,float4 v2){
	float4 v=v0-v2;
	float4 w=v1-v2;
	float len1=length(v);
	float len2=length(w);
	if(len1<=1E-3f||len2<=1E-3f)return 0;
	return acos(dot(v,w)/(len1*len2));
}
inline uint getIndex(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
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
	grad.w = 0;
	return grad;
}

__kernel void fixLabels(
		__global Springl3D* capsules,
		__global CapsuleNeighbor3D* capsuleNeighbors,
		__global float* origUnsignedLevelSet,
		__global int* labels,uint N){
	uint id=get_global_id(0);
	if(id>=N||labels[id]!=-1)return;	
	capsuleNeighbors+=3*MAX_NEIGHBORS*id;
	Springl3D capsule=capsules[id];
	Springl3D nbr;
	CapsuleNeighbor3D ci;
	float totalWeight=1.0E-6f;
	float4 newPoint=(float4)(0,0,0,0);
	float4 particle=capsule.particle;
	particle.w=0;
	float minDist=2.0f;
	int newLabel=-1;
	int label=0;
	for (int i = 0; i < 3; i++) {
		for (int n=0;n<MAX_NEIGHBORS;n++) {
			ci= capsuleNeighbors[MAX_NEIGHBORS*i+n];
			if(ci.capsuleId==-1)break;
			label=labels[ci.capsuleId];
			//There may be a race condition here! But it shouldn't have a big effect on the final mapping
			if(label<0)continue;
			nbr=capsules[ci.capsuleId];
			float4 mapPoint=(float4)(nbr.vertexes[0].w,nbr.vertexes[1].w,nbr.vertexes[2].w,0);
			nbr.particle.w=0;
			float w=distance(particle,nbr.particle);
			if(w<minDist){
				minDist=w;
				newLabel=label;
				//newPoint=mapPoint;
			}
			w=1.0f/(w+1E-2f);
			totalWeight+=w;
			newPoint+=w*mapPoint;
		}
	}
	
	newPoint/=totalWeight;
	const uint MAX_ITERATIONS=32;
	const float EPSILON=0.1f; 
	const float DELTA=0.5f;
	float levelSetValue=0;
	newPoint*=SCALE_UP;
	for(int i=0;i<MAX_ITERATIONS;i++){
		levelSetValue=interpolate(origUnsignedLevelSet,newPoint.x,newPoint.y,newPoint.z);
		if(levelSetValue<=EPSILON)break;
		newPoint-=DELTA*levelSetValue*getGradientValue(origUnsignedLevelSet,newPoint.x,newPoint.y,newPoint.z);
	}
	newPoint*=SCALE_DOWN;
	capsule.vertexes[0].w=newPoint.x;
	capsule.vertexes[1].w=newPoint.y;
	capsule.vertexes[2].w=newPoint.z;
	capsules[id]=capsule;
	labels[id]=newLabel;
}
__kernel void contractArray(__global Springl3D* inCapsules,__global int* inLabels,__global Springl3D* outCapsules,__global int* outLabels,__global int* offsets,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;	
	int offset=(id>0)?offsets[id-1]:0;
	outCapsules[offset]=inCapsules[id];
	outLabels[offset]=inLabels[id];
}
__kernel void expandArray(__global Springl3D* inCapsules,__global int* inLabels,__global Springl3D* outCapsules,__global int* outLabels,__global int* offsets,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;	
	int offset=(id>0)?offsets[id-1]:0;
	uint nextOffset=offsets[(int)min(id,elements-1)];
	outCapsules[offset]=inCapsules[id];
	outLabels[offset]=inLabels[id];
	float maxLength=0;
	float edgeLength=0;
	int maxEdgeLength=0;
	float4 pt1,pt2;
	if(nextOffset-offset>1){
		Springl3D capsule=outCapsules[offset];
		Springl3D capsule2;
		for (int i = 0; i < 3; i++) {
			pt1 = capsule.vertexes[i];
			pt2 = capsule.vertexes[(i + 1) % 3];
			pt1.w=0;
			pt2.w=0;
			edgeLength=lengthSquared(pt2-pt1);
			if (edgeLength > maxLength) {
				maxLength = edgeLength;
				maxEdgeLength = i;
			}
		}
	    float4 v1 = capsule.vertexes[maxEdgeLength];
		float4 v2 = capsule.vertexes[(maxEdgeLength + 1) % 3];
		float4 v3 = capsule.vertexes[(maxEdgeLength + 2) % 3];
		float cx=capsule.vertexes[0].w;
		float cy=capsule.vertexes[1].w;
		float cz=capsule.vertexes[2].w;
		v1.w=0;
		v2.w=0;
		v3.w=0;
		
		float4 midPoint = (float4)(0.5f * (v1.x + v2.x),0.5f * (v1.y + v2.y), 0.5f * (v1.z + v2.z),0);
		
		capsule.vertexes[0] = v3;
		capsule.vertexes[1] = midPoint;
		capsule.vertexes[2] = v2;	
		
		capsule.vertexes[0].w = cx;
		capsule.vertexes[1].w = cy;
		capsule.vertexes[2].w = cz;
			
		capsule.particle=0.333333f*(v3+midPoint+v2);
		capsule.particle.w=0;
		
		capsule2.vertexes[0] = v3;
		capsule2.vertexes[1] = v1;
		capsule2.vertexes[2] = midPoint;
		
		capsule2.vertexes[0].w = cx;
		capsule2.vertexes[1].w = cy;
		capsule2.vertexes[2].w  = cz;
		
		capsule2.particle=0.333333f*(v3+midPoint+v1);
		capsule2.particle.w=0;
		
		outCapsules[offset]=capsule;
		outCapsules[offset+1]=capsule2;
		outLabels[offset+1]=outLabels[offset];
	}	
}
__kernel void copyElements(__global Springl3D* inCapsules,__global int* inLabels,__global Springl3D* outCapsules,__global int* outLabels,uint elements){
	uint id=get_global_id(0);
	if(id>=elements)return;	
	outCapsules[id]=inCapsules[id];
	outLabels[id]=inLabels[id];
}
__kernel void expandCount(__global Springl3D *capsules,__global uint *counts,uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		return;
	}
	Springl3D capsule=capsules[id];
	float4 pt1=capsule.vertexes[0];
	float4 pt2=capsule.vertexes[1];
	float4 pt3=capsule.vertexes[2];
	pt1.w=0;
	pt2.w=0;
	pt3.w=0;
	float4 crossProd=cross(pt1-pt2,pt1-pt3);
	if (crossProd.x*crossProd.x+crossProd.y*crossProd.y+crossProd.z*crossProd.z >maxAreaThreshold * maxAreaThreshold) {
		counts[id]=2;
	} else {
		counts[id]=1;
	}
}
__kernel void countElements(__global uint *counts,__global uint *sums,uint stride,uint elements){
	uint id=get_global_id(0);
	float sum=0;
	if(id>id*stride)return;
	counts+=id*stride;
	int sz=min(stride,elements-id*stride);
	for(int i=0;i<sz;i++){
		sum+=counts[i];
	}
	sums[id]=sum;	
}
__kernel void contractCount(
		__global Springl3D *capsules,
		__global uint *counts,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		return;
	}
	Springl3D capsule=capsules[id];
	float4 pt=capsule.particle;
	float levelSetValue=pt.w;
	pt.w=0;
	pt*=SCALE_UP;
	float maxAngle = 0;
	float minAngle = 7;
	float4 pt1,pt2,pt3;
	if(levelSetValue==STATIC_SPRINGL){
		counts[id]=1;
		return;
	}
	capsules[id].particle.w=0;
	if(fabs(levelSetValue) >= 1.25f * vExtent) {
		counts[id]=0;
	} else {
		for (int i = 0; i < 3; i++) {
			pt1 = capsule.vertexes[i];
			pt2 = capsule.vertexes[(i + 1) % 3];
			pt3 = capsule.vertexes[(i + 2) % 3];
			pt1.w=0;
			pt2.w=0;
			pt3.w=0;
	
			float ang = angle(pt1, pt3, pt2);
			
			maxAngle = max(maxAngle, ang);
			minAngle = min(minAngle, ang);
		}
		
		float area=0.5f*length(cross(pt2-pt1,pt3-pt1));
		if (area>0.05f&&maxAngle <= maxAngleTolerance&& minAngle >= minAngleTolerance) {
			counts[id]=1;
		} else {
			counts[id]=0;
		}
	}
}
__kernel void contractOutliersCount(
		__global Springl3D *capsules,
		__global uint *counts,
		uint elements){
	uint id=get_global_id(0);
	if(id>=elements){
		counts[id]=0;
		return;
	}
	Springl3D capsule=capsules[id];
	float4 pt=capsule.particle;
	float levelSetValue=pt.w;
	capsules[id].particle.w=0;
	counts[id]=(fabs(levelSetValue) >= 1.25f * vExtent)?0:1;
}
__kernel void contractCountWithAtlas(
		__global Springl3D *capsules,
		__global uint *counts,
		__global float* atlasBiasBuffer,
		uint elements,
		float atlasThreshold){
	uint id=get_global_id(0);
	if(id>=elements){
		counts[id]=0;
		return;
	}
	Springl3D capsule=capsules[id];
	float4 pt=capsule.particle;
	float levelSetValue=pt.w;
	capsules[id].particle.w=0;
	pt.w=0;
	pt*=SCALE_UP;
	float maxAngle = 0;
	float minAngle = 7;
	float bias=atlasBiasBuffer[getIndex(round(SCALE_UP*capsule.vertexes[0].w),round(SCALE_UP*capsule.vertexes[1].w),round(SCALE_UP*capsule.vertexes[2].w))];
	if(bias>atlasThreshold){
		counts[id]=1;
	} else {
		if(levelSetValue >= 1.25f * vExtent||levelSetValue <= -1.25f * vExtent) {
			counts[id]=0;
		} else {
			for (int i = 0; i < 3; i++) {
				float4 pt1 = capsule.vertexes[i];
				float4 pt2 = capsule.vertexes[(i + 1) % 3];
				float4 pt3 = capsule.vertexes[(i + 2) % 3];
				pt1.w=0;
				pt2.w=0;
				pt3.w=0;
		
				float ang = angle(pt1, pt3, pt2);
				maxAngle = max(maxAngle, ang);
				minAngle = min(minAngle, ang);
			}
			if (maxAngle <= maxAngleTolerance&& minAngle >= minAngleTolerance) {
				counts[id]=1;
			} else {
				counts[id]=0;
			}
		}
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
inline float fGetOffset(__global float* signedLevelSet,int4 v1, int4 v2) {
		float fValue1 = getNudgedValue(signedLevelSet,getIndex(v1.x, v1.y, v1.z));
		float fValue2 = getNudgedValue(signedLevelSet,getIndex(v2.x, v2.y, v2.z));
		float fDelta = fValue2 - fValue1;
		if (fDelta == 0.0f) {
			return 0.5f;
		}
		return (float) (( - fValue1) / fDelta);
}

__kernel void expandGaps(
	global Springl3D* capsules,
	global int* labels,
	const global float* signedLevelSet,
	const global float* unsignedLevelSet,
	const global int* activeList,
	global int* offsets,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int elements,
	int activeListSize){
	__const int a2fVertexOffset[8][3] = { { 0, 0, 0 },
			{ 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
			{ 1, 1, 1 }, { 0, 1, 1 } };
	__const int a2iEdgeConnection[12][2] = { { 0, 1 }, { 1, 2 },
			{ 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
	__const float a2fEdgeDirection[12][3] = { { 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f },
			{ 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f },
			{ 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f } };

	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0)iFlagIndex |= 1 << iVertex;

	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		return;
	}
	EdgeSplit split;

	float4 pt3d;
	EdgeSplit asEdgeVertex[12];
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge

		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffset(signedLevelSet,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
			split.pt3d = pt3d;
			asEdgeVertex[iEdge] = split;
		}
	}
	float4 centroid;
	float4 ray[3];
	float4 pts[3];
	float levelSetValue;
	float len;
	float minLength = 1E10f;
	int offset=(gid>0)?offsets[gid-1]:0;
	// Generate list of triangles
	capsules+=elements+offset;
	labels+=elements+offset;
	Springl3D cap;
	int count=0;
	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		centroid=(float4)(0,0,0,0);
		for (int iCorner = 0; iCorner < 3; iCorner++) {
			iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
			split = asEdgeVertex[iVertex];
			centroid+=pts[iCorner]=split.pt3d;
		}
		centroid*=0.33333f;
		levelSetValue=interpolate(unsignedLevelSet,centroid.x,centroid.y,centroid.z);
		if(levelSetValue>vExtent){			
			minLength = 1E10f;
			for (int i = 0; i < 3; i++) {
				ray[i]=pts[i]-centroid;
				len = max(1E-3f,length(ray[i]));
				if (len < 2 * REST_LENGTH) {
					ray[i]*=(2 * REST_LENGTH / len);
				}
				minLength =min(len, minLength);
			}
			if (minLength < 2 * REST_LENGTH) {
				for (int i = 0; i < 3; i++) {
					pts[i] = centroid;
					pts[i]+=ray[i];
				}
			}			
			if(sign>0){
				cap.vertexes[0]=SCALE_DOWN*pts[0];
				cap.vertexes[1]=SCALE_DOWN*pts[1];
				cap.vertexes[2]=SCALE_DOWN*pts[2];
			} else {
				cap.vertexes[0]=SCALE_DOWN*pts[2];
				cap.vertexes[1]=SCALE_DOWN*pts[1];
				cap.vertexes[2]=SCALE_DOWN*pts[0];
			}
			cap.particle=centroid*SCALE_DOWN;
			capsules[count]=cap;
			labels[count]=-1;
			count++;
		}		
	}
}
__kernel void fillGapCount(
	const global float* signedLevelSet,
	const global float* unsignedLevelSet,
	const global int* activeList,
	global int* counts,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int activeListSize) {
	__const int a2fVertexOffset[8][3] = { { 0, 0, 0 },
			{ 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
			{ 1, 1, 1 }, { 0, 1, 1 } };
	__const int a2iEdgeConnection[12][2] = { { 0, 1 }, { 1, 2 },
			{ 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
	__const float a2fEdgeDirection[12][3] = { { 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f },
			{ 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f },
			{ 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f } };

	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0)iFlagIndex |= 1 << iVertex;

	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		counts[gid]=0;
		return;
	}
	EdgeSplit split;

	float4 pt3d;
	EdgeSplit asEdgeVertex[12];
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge

		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffset(signedLevelSet,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
			split.pt3d = pt3d;
			asEdgeVertex[iEdge] = split;
		}
	}
	float4 centroid;
	float levelSetValue;
	int count=0;
	// Generate list of triangles
	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		centroid=(float4)(0,0,0,0);
		for (int iCorner = 0; iCorner < 3; iCorner++) {
			iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
			split = asEdgeVertex[iVertex];
			centroid+=split.pt3d;
		}
		centroid*=0.33333f;			
		levelSetValue=interpolate(unsignedLevelSet,centroid.x,centroid.y,centroid.z);
		if(levelSetValue>vExtent){
			count++;
		}
	}
	counts[gid]=count;
}
__kernel void isoSurfCount(
	const global float* signedLevelSet,
	const global int* activeList,
	global int* counts,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int activeListSize) {
	__const int a2fVertexOffset[8][3] = { { 0, 0, 0 },
			{ 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
			{ 1, 1, 1 }, { 0, 1, 1 } };
	__const int a2iEdgeConnection[12][2] = { { 0, 1 }, { 1, 2 },
			{ 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
	__const float a2fEdgeDirection[12][3] = { { 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f },
			{ 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f },
			{ 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f } };

	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0)iFlagIndex |= 1 << iVertex;

	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		counts[gid]=0;
		return;
	}
	EdgeSplit split;

	float4 pt3d;
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge
		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffset(signedLevelSet,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
			split.pt3d = pt3d;
		}
	}
	int count=0;
	// Generate list of triangles
	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		count+=3;
	}
	counts[gid]=count;
}

__kernel void isoSurfGen(
	global float4* vertexes,
	const global float* signedLevelSet,
	const global int* activeList,
	global int* offsets,
	const global int* aiCubeEdgeFlags,
	const global int* a2iTriangleConnectionTable,
	int sign,
	int activeListSize){
	__const int a2fVertexOffset[8][3] = { { 0, 0, 0 },
			{ 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 1 },
			{ 1, 1, 1 }, { 0, 1, 1 } };
	__const int a2iEdgeConnection[12][2] = { { 0, 1 }, { 1, 2 },
			{ 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
	__const float a2fEdgeDirection[12][3] = { { 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { -1.0f, 0.0f, 0.0f },
			{ 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f },
			{ 0.0f, 0.0f, 1.0f }, { 0.0f, 0.0f, 1.0f } };

	uint gid=get_global_id(0);
	if(gid>=activeListSize)return;
	uint id=activeList[gid];
	int x,y,z;
	getRowColSlice(id,&x,&y,&z);			
	int iVertex = 0;
	int4 afCubeValue[8];
	int iFlagIndex = 0;
	int4 v;
	
	for (iVertex = 0; iVertex < 8; iVertex++) {
		v = afCubeValue[iVertex] = (int4)(clampRow(x+ a2fVertexOffset[iVertex][0]), clampColumn(y+ a2fVertexOffset[iVertex][1]), clampSlice(z+ a2fVertexOffset[iVertex][2]),0);
		// Find which vertices are inside of the surface and which are
		// outside
		if (sign*signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0)iFlagIndex |= 1 << iVertex;

	}
	// Find which edges are intersected by the surface
	int iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

	// If the cube is entirely inside or outside of the surface, then there
	// will be no intersections
	if (iEdgeFlags == 0) {
		return;
	}
	EdgeSplit split;

	float4 pt3d;
	EdgeSplit asEdgeVertex[12];
	// Find the point of intersection of the surface with each edge
	// Then find the normal to the surface at those points
	for (int iEdge = 0; iEdge < 12; iEdge++) {
		// if there is an intersection on this edge

		if ((iEdgeFlags & (1 << iEdge)) != 0) {
			v = afCubeValue[a2iEdgeConnection[iEdge][0]];
			
			if (signedLevelSet[getIndex(v.x, v.y, v.z)] <= 0) {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][0]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][1]];
			} else {
				split.e1 = afCubeValue[a2iEdgeConnection[iEdge][1]];
				split.e2 = afCubeValue[a2iEdgeConnection[iEdge][0]];
			}
			float fOffset = fGetOffset(signedLevelSet,
				afCubeValue[a2iEdgeConnection[iEdge][0]],
				afCubeValue[a2iEdgeConnection[iEdge][1]]);
				pt3d.x = (x + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset
								* a2fEdgeDirection[iEdge][0]));
				pt3d.y = (y + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset
								* a2fEdgeDirection[iEdge][1]));
				pt3d.z = (z + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset
								* a2fEdgeDirection[iEdge][2]));
				pt3d.w=1;
			split.pt3d = pt3d;
			asEdgeVertex[iEdge] = split;
		}
	}
	// Generate list of triangles
	vertexes+=(gid>0)?offsets[gid-1]:0;
	for (int iTriangle = 0; iTriangle < 5; iTriangle++) {
		if (a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle] < 0)break;
		if(sign<0){
			for (int iCorner = 0; iCorner < 3; iCorner++) {
				iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
				split = asEdgeVertex[iVertex];
				vertexes[2-iCorner]=split.pt3d;
			}
		} else {
			for (int iCorner = 0; iCorner < 3; iCorner++) {
				iVertex = a2iTriangleConnectionTable[iFlagIndex*16+3 * iTriangle+ iCorner];
				split = asEdgeVertex[iVertex];
				vertexes[iCorner]=split.pt3d;
			}		
		}
		vertexes+=3;		
	}
}