typedef struct{
	float2 particle;
	float2 mapping;
    float2 vertexes[2];
    float phi;
    float psi; //not used, but it makes the number of values even!
} Springl2D;
float4 getColor(int l,global float4 *colors){
	#if CONTAINS_OVERLAPS
		int index = 0;
		float4 color=(float4)(0,0,0,0);
		int count = 0;
		while (l != 0) {
			if ((l & 0x01) != 0) {
				color+=colors[index];
				count++;
			}
			l >>= 0x01;
			index++;
		}
		if(count>0){
			color=color/(float)count;
		} else {
			color=(float4)(0.0f,0.0f,0.0f,0.5f);
		}
		return color;
	#else
		if(l>0){
			return colors[l-1];
		} else {
			return (float4)(0.0f,0.0f,0.0f,0.5f);
		}
	#endif
}
inline uint getIndex(int i, int j) {
	return (j * ROWS) + i;
}
inline void getRowCol(int ij,int* i, int* j) {
	(*j)=ij/ROWS;
	(*i)=ij-(*j)*ROWS;
}
inline float getImageValue(global float* image,uint i,uint j){
	uint r = clamp((uint)i,(uint)0,(uint)(ROWS-1));
	uint c = clamp((uint)j,(uint)0,(uint)(COLS-1));
	return image[getIndex(r,c)];
}
inline float getLabelValue(global int* image,uint i,uint j){
	uint r = clamp((uint)i,(uint)0,(uint)(ROWS-1));
	uint c = clamp((uint)j,(uint)0,(uint)(COLS-1));
	return image[getIndex(r,c)];
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
kernel void IsoContourRenderer(global int* labels,global float* levelset,global float4* pixelBuffer,global float4* colors,float alpha){
    uint id = get_global_id(0);
   	int i,j;
	getRowCol(id,&i,&j);
	float level=levelset[id];
	int currentLabel=labels[id];
	float weight;
	int activeLabels[4];
	activeLabels[0]=getLabelValue(labels,i+1,j);
	activeLabels[1]=getLabelValue(labels,i-1,j);
	activeLabels[2]=getLabelValue(labels,i,j+1);
	activeLabels[3]=getLabelValue(labels,i,j-1);
	float4 c=getColor(currentLabel,colors);
	c.w*=alpha;
	for(int n=0;n<4;n++){
		if(activeLabels[n]!=currentLabel){
			weight=tanh(level);
			pixelBuffer[id]=weight*c+(1-weight)*((float4)(0,0,0,alpha));
			return;			
		}
	}
	pixelBuffer[id]=c;
}

inline float getNudgedValue(__global float* unsignedLevelSet,__global int* labels,int label,int x,int y){
	float val=getLevelSetValue(unsignedLevelSet,labels,label,x,y);
	if (val < 0) {
		val = min(val, - 0.1f);
	} else {
		val = max(val,  0.1f);
	}
	return val;
}
inline float fGetOffset(__global float* unsignedLevelSet,__global int* labels,int label,int2 v1, int2 v2) {
		float fValue1 = getNudgedValue(unsignedLevelSet,labels,label,v1.x, v1.y);
		float fValue2 = getNudgedValue(unsignedLevelSet,labels,label,v2.x, v2.y);
		float fDelta = fValue2 - fValue1;
		if (fabs(fDelta) ==0) {
			return 0.5f;
		}
		return clamp(-fValue1 / fDelta,0.0f,1.0f);
}
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
__kernel void countElements(
	__global int *counts,
	__global int *sums,
	int stride,int elements){
	
	int id=get_global_id(0);
	float sum=0;
	int sz=min(elements-id*stride,stride);
	counts+=id*stride;
	for(int i=0;i<sz;i++){
		sum+=counts[i];
	}
	if(sz>0)sums[id]=sum;	
}
__kernel void isoSurfCount(__global float* unsignedLevelSet,__global int* labels,__global int* counts) {
	uint id=get_global_id(0);
	int x,y;
	getRowCol(id,&x,&y);			
	int iFlagIndex = 0;
	int vx,vy;
	int activeLabels[4];
	activeLabels[0]=getLabelValue(labels,x+1,y);
	activeLabels[1]=getLabelValue(labels,x,y+1);
	activeLabels[2]=getLabelValue(labels,x+1,y+1);
	activeLabels[3]=getLabelValue(labels,x,y);
	int label;
	int count=0;
	for(int n=0;n<4;n++){
		label=activeLabels[n];
		
		if(label==0)continue;
		iFlagIndex = 0;
		for (int iVertex = 0; iVertex < 4; iVertex++) {
			vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
			vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
			if (getLevelSetValue(unsignedLevelSet,labels,label,vx, vy) < 0)iFlagIndex |= 1 << iVertex;
		}
		if(afSquareValue[iFlagIndex][0]<4){
			count+=2;
		}
		if(afSquareValue[iFlagIndex][2]<4){
			count+=2;
		}
	}
	counts[id]=count;
}

__kernel void isoSurfGen(global float2* vertexes,__global float* unsignedLevelSet,__global int* labels,global float4* colorBuffer,global float4* colors,__global int* offsets){
	uint id=get_global_id(0);
	int x,y;
	getRowCol(id,&x,&y);			
	int iFlagIndex = 0;
	int vx,vy;
	int activeLabels[4];
	activeLabels[0]=getLabelValue(labels,x+1,y);
	activeLabels[1]=getLabelValue(labels,x,y+1);
	activeLabels[2]=getLabelValue(labels,x+1,y+1);
	activeLabels[3]=getLabelValue(labels,x,y);
	int label;
	float2 spt1,spt2;
	int2 pt1,pt2;
	float fInvOffset;
	float fOffset;
	int count=0;
	// Generate list of Segments
	int offset=(id>0)?offsets[id-1]:0;
	vertexes+=offset;
	colorBuffer+=offset;
	float4 c;
	
	int maxLabel=0;
	for(int n=0;n<4;n++){
		maxLabel=max(maxLabel,activeLabels[n]);
	}	
	c=getColor(maxLabel,colors);	
	for(int n=0;n<4;n++){
		label=activeLabels[n];
		iFlagIndex = 0;
		if(label==0)continue;
		for (int iVertex = 0; iVertex < 4; iVertex++) {
			vx=clampRow(x+ a2fVertex1Offset[iVertex][0]);
			vy=clampColumn(y+ a2fVertex1Offset[iVertex][1]);
			if (getLevelSetValue(unsignedLevelSet,labels,label,vx, vy) < 0)iFlagIndex |= 1 << iVertex;
		}
		if(afSquareValue[iFlagIndex][0]<4){
			pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][0]][1]);
			pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][0]][1]);
			fOffset = fGetOffset(unsignedLevelSet,labels,label,pt1,pt2);
			fInvOffset = 1.0f - fOffset;
			spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
			spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
			
			pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][1]][1]);
			pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][1]][1]);
			fOffset = fGetOffset(unsignedLevelSet,labels,label,pt1,pt2);
			fInvOffset = 1.0f - fOffset;
			spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
			spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);
			
			vertexes[count]=spt1;
			colorBuffer[count++]=c;
			vertexes[count]=spt2;
			colorBuffer[count++]=c;
		}
		
		if(afSquareValue[iFlagIndex][2]<4){
			pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][2]][1]);
			pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][2]][1]);
			fOffset = fGetOffset(unsignedLevelSet,labels,label,pt1,pt2);
			fInvOffset = 1.0f - fOffset;
			spt1.x = (fInvOffset * pt1.x + fOffset * pt2.x);
			spt1.y = (fInvOffset * pt1.y + fOffset * pt2.y);
			
			pt1=(int2)(x + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex1Offset[afSquareValue[iFlagIndex][3]][1]);
			pt2=(int2)(x + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][0], y + a2fVertex2Offset[afSquareValue[iFlagIndex][3]][1]);
			fOffset = fGetOffset(unsignedLevelSet,labels,label,pt1,pt2);
			fInvOffset = 1.0f - fOffset;
			spt2.x = (fInvOffset * pt1.x + fOffset * pt2.x);
			spt2.y = (fInvOffset * pt1.y + fOffset * pt2.y);			
			vertexes[count]=spt1;
			colorBuffer[count++]=c;
			vertexes[count]=spt2;
			colorBuffer[count++]=c;	
		}
	}
}



kernel void copyCapsulesToMeshMogac(
		global int* imageLabels,
		global int* labels,
		global float4* colors,
		global Springl2D* capsules,
		global float2* vertexBuffer,
		global float2* particleBuffer,
		global float4* particleColorBuffer,
		global float2* normalSegmentBuffer,
		global float2* mapPointBuffer,
		uint elements){
	uint id=get_global_id(0);
	int nbrs[4];
	if(id>=elements)return;
	capsules+=id;
	vertexBuffer+=id*2;
	float2 particle=SCALE_UP*capsules->particle;
	float2 mapping=SCALE_UP*capsules->mapping;
	particleBuffer[id]=particle;
	
	int i=floor(particle.x);
	int j=floor(particle.y);
	int label=labels[id];
	int alabel=abs(label);
	nbrs[0]=getLabelValue(imageLabels,i,j);
	nbrs[1]=getLabelValue(imageLabels,i,j+1);
	nbrs[2]=getLabelValue(imageLabels,i+1,j+1);
	nbrs[3]=getLabelValue(imageLabels,i+1,j);
	float scaleNorm=0.0f;
	for(int k=0;k<4;k++){
		if(nbrs[k]!=0&&alabel!=nbrs[k]){
			scaleNorm=0.5f;
			break;
		}
	}	
	
	vertexBuffer[0]=SCALE_UP*capsules->vertexes[0];
	vertexBuffer[1]=SCALE_UP*capsules->vertexes[1];
	float2 norm;
	norm.x=vertexBuffer[0].y-vertexBuffer[1].y;
	norm.y=vertexBuffer[1].x-vertexBuffer[0].x;
	norm=normalize(norm);
	
	normalSegmentBuffer[2*id]=particle-scaleNorm*norm;
	normalSegmentBuffer[2*id+1]=particle+0.5f*norm;
	
	mapPointBuffer[2*id]=particle;
	if(label>=0){
		mapPointBuffer[2*id+1]=mapping;
	} else {
		mapPointBuffer[2*id+1]=particle;
	}
	particleColorBuffer[id]=getColor(alabel,colors);
}