

kernel void prefixScanSubList(global int* inBuffer,global int* outBuffer,int stride,int elements){
	uint id=get_global_id(0);
	int offset=id*stride;
	inBuffer+=offset;
	if(offset>elements)return;	
	int total=0;
	int sz=min(elements-offset,stride);
	for(int n=0;n<sz;n++){
		total+=inBuffer[n];
		inBuffer[n]=total;
	}	
	outBuffer[id]=total;
}

kernel void addScanList(global int* inBuffer,global int* offsetBuffer,int stride,int elements){
	uint id=get_global_id(0);
	int offset=id*stride;
	inBuffer+=offset;
	if(offset>=elements)return;	
	int sz=min(elements-offset,stride);
	int sum=(id>0)?offsetBuffer[id-1]:0;
	for(int n=0;n<sz;n++){
		inBuffer[n]+=sum;
	}	
}