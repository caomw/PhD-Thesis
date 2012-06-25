/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
 *
 * Copyright(C) 2012 Blake Lucas (img.science@gmail.com)
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