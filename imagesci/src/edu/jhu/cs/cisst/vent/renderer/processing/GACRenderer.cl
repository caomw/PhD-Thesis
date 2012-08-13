/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012, Blake Lucas (img.science@gmail.com)
 * All rights reserved.
 * 
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the The Johns Hopkins University.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * @author Blake Lucas (img.science@gmail.com)
 */
 /*
Copyright (c) 2009 David Bucciarelli (davibu@interfree.it)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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

typedef struct {
	float m00, m01, m02, m03,
		  m10, m11, m12, m13,
		  m20, m21, m22, m23,
		  m30, m31, m32, m33;
} Matrix4f;



typedef struct {
	float4 origin;
	float4 direction;
	float extent;	
} EdgeSegment;
inline void getRowColSlice(uint index,int* i, int* j, int* k) {
	(*k)=index/(ROWS*COLS);
	uint ij=index-(*k)*(ROWS * COLS);
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}

inline int getIndex(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
}
inline int getSafeIndex(int i,int j,int k){
	int r=max(min(i,(int)(ROWS-1)),(int)0);
	int c=max(min(j,(int)(COLS-1)),(int)0);
	int s=max(min(k,(int)(SLICES-1)),(int)0);
	return getIndex(r,c,s);
}
inline float getImageValue(__global float* image,int i,int j,int k){
	return image[getSafeIndex(i,j,k)];
}
inline float4 getImageValue4(__global float4* image,int i,int j,int k){
	return image[getSafeIndex(i,j,k)];
}

inline Matrix4f invertMatrix3x3(Matrix4f M){
	float detInv=1.0f/(M.m00*M.m11*M.m22 + M.m01*M.m12*M.m20 + M.m02*M.m10*M.m21 - M.m00*M.m12*M.m21 - M.m01*M.m10*M.m22 - M.m02*M.m11*M.m20);	
	Matrix4f Minv={
		detInv*(M.m11*M.m22 - M.m12*M.m21),	detInv*(M.m02*M.m21 - M.m01*M.m22),	detInv*(M.m01*M.m12 - M.m02*M.m11),0,
		detInv*(M.m12*M.m20 - M.m10*M.m22),	detInv*(M.m00*M.m22 - M.m02*M.m20),	detInv*(M.m02*M.m10 - M.m00*M.m12),0,
		detInv*(M.m10*M.m21 - M.m11*M.m20),	detInv*(M.m01*M.m20 - M.m00*M.m21),	detInv*(M.m00*M.m11 - M.m01*M.m10),0,
		0,0,0,1};
		return  Minv;
}
//Get index into volume
inline uint getHashValue(int i, int j, int k) {
	return (k * (ROWS * COLS)) + (j * ROWS) + i;
}
inline float4 transform4(float4 vec,const Matrix4f M){
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
inline float4 worldToImage(float4 pt){
	const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	return (float4)(
			((0.5f*pt.x*MAX_DIM+0.5f*ROWS)),
			((0.5f*pt.y*MAX_DIM+0.5f*COLS)),
			((0.5f*pt.z*MAX_DIM+0.5f*SLICES)),
			0);
}
inline float4 imageToWorld(float4 pt){
	const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	return (float4)(
		2.0f*(pt.x-0.5f*ROWS)/MAX_DIM,
		2.0f*(pt.y-0.5f*COLS)/MAX_DIM,
		2.0f*(pt.z-0.5f*SLICES)/MAX_DIM,0);
}
float interpolate(__global float* data,float x,float y,float z){
	int y0, x0, z0, y1, x1, z1;
	float dx, dy, dz, hx, hy, hz;
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
		return (((getImageValue(data,x0,y0,z0) * hx + getImageValue(data,x1,y0,z0) * dx) * hy + (getImageValue(data,x0,y1,z0)
				* hx + getImageValue(data,x1,y1,z0) * dx)
				* dy)
				* hz + ((getImageValue(data,x0,y0,z1) * hx + getImageValue(data,x1,y0,z1) * dx)
				* hy + (getImageValue(data,x0,y1,z1) * hx + getImageValue(data,x1,y1,z1) * dx) * dy)
				* dz);
	
}
//Distance squared between two points
inline float distanceSquaredPoint(float4 pt1,float4 pt2){
	float4 d=(pt1-pt2);
	return (d.x*d.x+d.y*d.y+d.z*d.z);
}


float4 QuatMult(const float4 q1, const float4 q2) {
    float4 r;

    // a1a2 - b1b2 - c1c2 - d1d2
    r.x = q1.x * q2.x - q1.y * q2.y - q1.z * q2.z - q1.w * q2.w;
    // a1b2 + b1a2 + c1d2 - d1c2
    r.y = q1.x * q2.y + q1.y * q2.x + q1.z * q2.w - q1.w * q2.z;
    // a1c2 - b1d2 + c1a2 + d1b2
    r.z = q1.x * q2.z - q1.y * q2.w + q1.z * q2.x + q1.w * q2.y;
    // a1d2 + b1c2 - c1b2 + d1a2
    r.w = q1.x * q2.w + q1.y * q2.z - q1.z * q2.y + q1.w * q2.x;

    return r;
}

float4 QuatSqr(const float4 q) {
    float4 r;

    r.x = q.x * q.x - q.y * q.y - q.z * q.z - q.w * q.w;
    r.y = 2.f * q.x * q.y;
    r.z = 2.f * q.x * q.z;
    r.w = 2.f * q.x * q.w;

    return r;
}
inline int IntersectTriPlanar(int showXplane,int showYplane,int showZplane,int row,int col,int slice,float4 rayOrig,float4 rayDir,float4* sliceHitPoint,float4* sliceHitNormal){
    	float tmpIntersect;
		int closestPlane=-1;
		float intersect=1E10f;
		float4 tmp;
		if(showXplane&&fabs(rayDir.x)>1E-3f){
			tmpIntersect=(row-rayOrig.x)/rayDir.x;
			tmp=rayOrig+tmpIntersect*rayDir;
			if(tmp.x>-1&&tmp.y>-1&&tmp.z>-1&&tmp.x<ROWS&&tmp.y<COLS&&tmp.z<SLICES){
				closestPlane=0;
				*sliceHitPoint=tmp;
				*sliceHitNormal=(float4)(1.0f,0.0f,0.0f,0.0f);
				intersect=tmpIntersect;
			}		    
		}
		if(showYplane&&fabs(rayDir.y)>1E-3f){
			tmpIntersect=(col-rayOrig.y)/rayDir.y;
			if(tmpIntersect<intersect){
				tmp=rayOrig+tmpIntersect*rayDir;
				if(tmp.x>-1&&tmp.y>-1&&tmp.z>-1&&tmp.x<ROWS&&tmp.y<COLS&&tmp.z<SLICES){
					closestPlane=1;
					*sliceHitPoint=tmp;
					*sliceHitNormal=(float4)(0.0f,1.0f,0.0f,0.0f);
					intersect=tmpIntersect;
				}		    
			}
		}
		if(showZplane&&fabs(rayDir.z)>1E-3f){
			tmpIntersect=(slice-rayOrig.z)/rayDir.z;
			if(tmpIntersect<intersect){
				tmp=rayOrig+tmpIntersect*rayDir;
				if(tmp.x>-1&&tmp.y>-1&&tmp.z>-1&&tmp.x<ROWS&&tmp.y<COLS&&tmp.z<SLICES){
					closestPlane=2;
					*sliceHitPoint=tmp;
					*sliceHitNormal=(float4)(0.0f,0.0f,1.0f,0.0f);
				}		    
			}
		}
		return closestPlane;
}
float IntersectVolume(Matrix4f modelViewMatrix,read_only image3d_t image,const float4 eyeRayOrig, const float4 eyeRayDir,const uint maxIterations, const float epsilon, float4 *hitPoint,  float4* hitNormal, uint* steps) {
    float dist=1000;
    float4 r0 = eyeRayOrig;
    const sampler_t volumeSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
    float4 norm=(float4)(0,0,0,0);
    uint s = 0;
    const int MAX_DIM=max(max(ROWS,COLS),SLICES);
	const float delta=1.0f/MAX_DIM;
	float4 grad;
	float d;
	do {
		r0.w=1;
		float4 pt=worldToImage(transform4(r0,modelViewMatrix));
		pt.w=0;
		grad=read_imagef(image,volumeSampler,pt);
		d = max(0.0f,grad.w);
		grad.w=0;
		if(d<epsilon){
			*hitNormal =grad;
			r0.w=0;
    		*hitPoint = r0;
    		return d;
		}
		s++;
		d=0.98f*d;
		r0 += delta*eyeRayDir*d;
		r0.w=0;
	} while ( (dot(r0, r0) < BOUNDING_RADIUS_2)&&s<maxIterations);
	*steps= s;
    return dist;
}


float IntersectFloorSphere(const float4 eyeRayOrig, const float4 eyeRayDir) {
    const float4 op = WORLD_CENTER - eyeRayOrig;
    const float b = dot(op, eyeRayDir);
    float det = b * b - dot(op, op) + WORLD_RADIUS * WORLD_RADIUS;

    if (det < 0.f)
        return -1.f;
    else
        det = sqrt(det);

    float t = b - det;
    if (t > 0.f)
        return t;
    else {
        // We are inside, avoid the hit
        return -1.f;
    }
}
float IntersectBoundingSphere(const float4 eyeRayOrig, const float4 eyeRayDir) {
    const float4 op = -eyeRayOrig;
    const float b = dot(op, eyeRayDir);
    float det = b * b - dot(op, op) + BOUNDING_RADIUS_2;

    if (det < 0.f)
        return -1.f;
    else
        det = sqrt(det);

    float t = b - det;
    if (t > 0.f)
        return t;
    else {
        t = b + det;

        if (t > 0.f) {
            // We are inside, start from the ray origin
            return 0.0f;
        } else
            return -1.f;
    }
}
float4 Phong(const float4 light, const float4 eye, const float4 pt, float4 N, const float4 diffuse,int twoSided) {

    const float4 ambient = (float4) (0.2f, 0.2f, 0.2f, 0.f);
    float4 L = normalize(light - pt);
    float NdotL = dot(N, L);
    //Surface is two sided!
    
    if (NdotL < 0.f){
		if(twoSided){
			//return diffuse * ambient;
			//two-sided rendering
			NdotL=-NdotL;
			N=-N;
		} else {
			return diffuse*ambient;
		}
	}
    const float specularExponent = 10.0f;
    const float specularity = 0.45f;

    float4 E = normalize(eye - pt);
    float4 H = (L + E) * (float) 0.5f;
	
    return diffuse * NdotL +
           specularity * pow(dot(N, H), specularExponent) +
           diffuse * ambient;
           
}
kernel void multiply(global float *array, const int numElements, const float s) {
    const int gid = get_global_id(0);
    if (gid >= numElements)  {
        return;
    }
    array[gid] *= s;
}
kernel void IsoSurfRender(
						global float* pixels,
						read_only image3d_t refImage,
						read_only image3d_t volume,
                        const global RenderingConfig *config,
                        const global Matrix4f* modelViewMatrix,
                        const global Matrix4f* modelViewInverseMatrix,
                        int enableAccumulation,
                        float sampleX,
                        float sampleY,
                        float fg_r,float fg_g,float fg_b,
                        float row,
                        float col,
                        float slice,
                        int showXplane,
                        int showYplane,
                        int showZplane,
                        int showIsoSurf,
                        float minImageValue,
                        float maxImageValue,
                        float brightness,
                        float contrast,
                        float transparency) {

    const int gid = get_global_id(0);
    unsigned width = config->width;
    unsigned height = config->height;
	Matrix4f M=*modelViewMatrix;
	Matrix4f Minv=*modelViewInverseMatrix;
	const sampler_t imageSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
	
    const unsigned int x = gid % width;
    const int y = gid / width;
	
    // Check if we have to do something
    if (y >= height)
        return;

    const float epsilon = config->actvateFastRendering ? (config->epsilon * (1.f / 0.75f)) : config->epsilon;
    const uint maxIterations = max(1u, config->actvateFastRendering ? (config->maxIterations - 1) : config->maxIterations);

    const float4 light = (float4) (config->light[0], config->light[1], config->light[2], 0.f);
    const global Camera *camera = &config->camera;

    //--------------------------------------------------------------------------
    // Calculate eye ray
    //--------------------------------------------------------------------------

    const float invWidth = 1.f / width;
    const float invHeight = 1.f / height;
    const float kcx = (x + sampleX) * invWidth - .5f;
    const float4 kcx4 = (float4) kcx;
    const float kcy = (y + sampleY) * invHeight - .5f;
    const float4 kcy4 = (float4) kcy;

    const float4 cameraX = (float4) (camera->x.x, camera->x.y, camera->x.z, 0.f);
    const float4 cameraY = (float4) (camera->y.x, camera->y.y, camera->y.z, 0.f);
    const float4 cameraDir = (float4) (camera->dir.x, camera->dir.y, camera->dir.z, 0.f);
    const float4 cameraOrig = (float4) (camera->orig.x, camera->orig.y, camera->orig.z, 0.f);

    float4 eyeRayDir = normalize(cameraX * kcx4 + cameraY * kcy4 + cameraDir);
    float4 eyeRayOrig = eyeRayDir * (float4) 0.1f + cameraOrig;

    //--------------------------------------------------------------------------
    // Check if we hit the bounding sphere
    //--------------------------------------------------------------------------

    float distSet = IntersectBoundingSphere(eyeRayOrig, eyeRayDir);
    float4 hitPoint;
    float4 hitNormal;
    int hitClipPlane=0;
    float4 surfHitPoint;
    float4 tmp;
    float4 diffuse, n, color;
    if (distSet >= 0.f) {
        //--------------------------------------------------------------------------
        // Find the intersection with the set
        //--------------------------------------------------------------------------

        uint steps=0;
        float4 rayOrigWorld= eyeRayOrig + eyeRayDir * (float4) distSet;
        rayOrigWorld.w=1;
        float4 rayOrig=worldToImage(transform4(rayOrigWorld,M));
        eyeRayDir.w=0;
        float4 rayDir=transform4(eyeRayDir,M);
        
    	diffuse = (float4) (fg_r,fg_g,fg_b, 0.f);
        if(showIsoSurf){
	        distSet = IntersectVolume(M,volume,rayOrigWorld, eyeRayDir, maxIterations, epsilon, &hitPoint, &hitNormal, &steps);
	        surfHitPoint=worldToImage(transform4(hitPoint,M));
	        if (distSet > epsilon)
	            distSet = -1.f;
        } else {
        	distSet=-1;
        }
        float4 sliceHitPoint;	
		float4 sliceHitNormal;
		int closestPlane=IntersectTriPlanar(showXplane,showYplane,showZplane,row,col,slice,rayOrig,rayDir,&sliceHitPoint,&sliceHitNormal);
		if(closestPlane>=0&&(distSet<0||distance(sliceHitPoint,rayOrig)<distance(surfHitPoint,rayOrig))){
			//sliceHitPoint=clamp(sliceHitPoint,(float4)(1,1,1,0),(float4)(ROWS-2,COLS-2,SLICES-2,0));
			tmp=read_imagef(refImage,imageSampler,sliceHitPoint);
			float val=(tmp.x-minImageValue)/(maxImageValue-minImageValue);
		    val=clamp(val*contrast + brightness,0.0f,1.0f);
		    tmp=read_imagef(volume,imageSampler,sliceHitPoint);
			float cweight=clamp(tanh(-tmp.w),0.0f,1.0f);
			diffuse=(cweight*(float4)(fg_r,fg_g,fg_b,1.0f)+(1-cweight)*((float4)(0,0,0,1.0f)));
			diffuse=(1-transparency)*(float4)(val,val,val,1.0f)+transparency*diffuse;
		    distSet=0;
		    hitClipPlane=1;
		    hitNormal=sliceHitNormal;
		    hitPoint=transform4(imageToWorld(sliceHitPoint),Minv);
	    } 
    }
    //--------------------------------------------------------------------------
    // Check if we hit the floor
    //--------------------------------------------------------------------------

    float distFloor = IntersectFloorSphere(eyeRayOrig, eyeRayDir);

    //--------------------------------------------------------------------------
    // Select the hit point
    //--------------------------------------------------------------------------

    int doShade = 0;
    int useAO = 1;
    
    if ((distSet < 0.f) && (distFloor < 0.f)) {
        // Sky hit
        color =  (float4)  (config->color[0],config->color[1],config->color[2],config->color[3]);
    } else if ((distSet >= 0.f) && ((distFloor < 0.f) || (distSet <= distFloor))) {
        // Set hit
        float4 ct=worldToImage(transform4(hitPoint,M));

		n=normalize(transform4(hitNormal,Minv));
        doShade = 1;
    } else if ((distFloor >= 0.f) && ((distSet < 0.f) || (distFloor <= distSet))) {
	        // Floor hit
	    if (config->enableShadow&&!hitClipPlane){
	        hitPoint = eyeRayOrig + eyeRayDir * (float4) distFloor;
	        n = hitPoint - WORLD_CENTER;
	        n = normalize(n);
	        // The most important feature in a ray tracer: a checker texture !
	        const int ix = (hitPoint.x > 0.f) ? hitPoint.x : (1.f - hitPoint.x);
	        const int iz = (hitPoint.z > 0.f) ? hitPoint.z : (1.f - hitPoint.z);
	        if ((ix + iz) % 2)
	            diffuse = (float4) (0.75f, 0.75f, 0.75f, 0.f);
	        else
	            diffuse = (float4) (87/255.0f,90/255.0f,93/255.0f,0);
	        doShade = 1;
	        useAO = 0;
        } else {
        	color =  (float4) (config->color[0],config->color[1],config->color[2],config->color[3]);	
        }
    }
    //--------------------------------------------------------------------------
    // Select the shadow pass
    //--------------------------------------------------------------------------

    if (doShade) {
        float shadowFactor = 1.f;
        if (config->enableShadow) {
            float4 L = normalize(light - hitPoint);
            float4 rO = hitPoint + n * 1e-2f;
            float4 shadowHitPoint;

            // Check bounding sphere
            float shadowDistSet = IntersectBoundingSphere(rO, L);
            
            if (shadowDistSet >= 0.f) {
                uint steps;

                rO = rO + L * (float4) shadowDistSet;
                shadowDistSet = IntersectVolume(M,volume,rO, L,  maxIterations, epsilon,
                        &shadowHitPoint, &hitNormal, &steps);
                if (shadowDistSet < epsilon) {
                    if (useAO) {
                        // Use steps count to simulate ambient occlusion
                        shadowFactor = 0.6f - min(steps / 255.f, 0.5f);
                    } else
                        shadowFactor = 0.6f;
                }
            } else
            
                shadowDistSet = -1.f;
        }
		
        //--------------------------------------------------------------------------
        // Direct lighting of hit point
        //--------------------------------------------------------------------------

        color = Phong(light, eyeRayOrig, hitPoint, n, diffuse,hitClipPlane) * shadowFactor;
    }

    //--------------------------------------------------------------------------
    // Write pixel
    //--------------------------------------------------------------------------

    int offset = 3 * (x + y * width);
    color = clamp(color, (float4) (0.f, 0.f, 0.f, 0.f), (float4) (1.f, 1.f, 1.f, 0.f));
    
    if (enableAccumulation) {
        pixels[offset++] += color.s0;
        pixels[offset++] += color.s1;
        pixels[offset  ] += color.s2;
    } else {
        pixels[offset++] = color.s0;
        pixels[offset++] = color.s1;
        pixels[offset  ] = color.s2;
    }
    
}
kernel void copyLevelSet(
	global float* srcImage,
	global float4* volumeColorBuffer){
	int i,j,k;
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
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
	volumeColorBuffer[id]=grad;
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
kernel void bilateralFilterVolume(global float4* buffIn,float edgeSigma,float lambda){
	int i,j,k;
	uint id=get_global_id(0);
	if(id>=ROWS*COLS*SLICES)return;
	getRowColSlice(id,&i,&j,&k);
	float4 v111 = getImageValue4(buffIn,i, j, k);
	float4 v211 = getImageValue4(buffIn,i + 1, j, k);
	float4 v121 = getImageValue4(buffIn,i, j + 1, k);
	float4 v101 = getImageValue4(buffIn,i, j - 1, k);
	float4 v011 = getImageValue4(buffIn,i - 1, j, k);
	float4 v110 = getImageValue4(buffIn,i, j, k - 1);
	float4 v112 = getImageValue4(buffIn,i, j, k + 1);		
	float4 norm=v111-0.16666f*lambda*(
		tukey(v111,v011,edgeSigma)*(v111-v011)+
		tukey(v111,v121,edgeSigma)*(v111-v121)+
		tukey(v111,v101,edgeSigma)*(v111-v101)+
		tukey(v111,v211,edgeSigma)*(v111-v211)+
		tukey(v111,v110,edgeSigma)*(v111-v110)+
		tukey(v111,v112,edgeSigma)*(v111-v112));
	buffIn[id]=norm;
}
