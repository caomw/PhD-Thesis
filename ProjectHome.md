This toolkit provides a collection of methods for multi-object image segmentation in 2D and 3D with level sets and Spring Level Sets (SpringLS). All methods have been implemented in OpenCL and run on either the GPU or CPU, depending on which device is known to perform better for that method. There is also an interactive GUI for volumetric rendering of image data and 3D surface models. The OpenCL code is wrapped in Java and built upon the Processing API. Methods are described in Blake's Ph.D. [thesis](http://cs.jhu.edu/~blake/docs/blake_lucas_thesis.pdf).


### There is a new version of this code ###
Spring Level Sets have been rewritten in C++ 11 and implemented on top of OpenVDB (http://www.openvdb.org/) data structures. The source code remains BSD licensed. Check out https://github.com/bclucas/springls_openvdb

<a href='http://www.youtube.com/watch?feature=player_embedded&v=03jTRJ-j7fQ' target='_blank'><img src='http://img.youtube.com/vi/03jTRJ-j7fQ/0.jpg' width='425' height=344 /></a>
### Before running the demo: ###
Install OpenCL for your Intel CPU: http://software.intel.com/en-us/articles/vcsource-tools-opencl-sdk/
and NVIDIA GPU: http://developer.nvidia.com/cuda-downloads
or ATI GPU: http://developer.amd.com/sdks/amdappsdk/downloads/pages/default.aspx

<a href='http://www.youtube.com/watch?feature=player_embedded&v=IPMqpSoK4sQ' target='_blank'><img src='http://img.youtube.com/vi/IPMqpSoK4sQ/0.jpg' width='425' height=344 /></a><a href='http://www.youtube.com/watch?feature=player_embedded&v=iDyqdaZXQY8' target='_blank'><img src='http://img.youtube.com/vi/iDyqdaZXQY8/0.jpg' width='425' height=344 /></a><a href='http://www.youtube.com/watch?feature=player_embedded&v=c-33jAf6vqw' target='_blank'><img src='http://img.youtube.com/vi/c-33jAf6vqw/0.jpg' width='425' height=344 /></a>

**References**

B. C. Lucas, "Unifying Deformable Model Representations through New Geometric Data Structures," Ph.D. Dissertation, Dept. of Computer Science, Johns Hopkins University, Baltimore, 2012. ([see paper](http://cs.jhu.edu/~blake/docs/blake_lucas_thesis.pdf))

B. C. Lucas, M. Kazhdan, and R. H. Taylor,"Spring Level Sets: A Deformable Model Representation to Provide Interoperability between Meshes and Level Sets," Visualization and Computer Graphics, IEEE Trans on, 2012. ([see paper](http://cs.jhu.edu/~blake/docs/springls_tvcg.pdf))

B. C. Lucas, M. Kazhdan, and R. H. Taylor,"Multi-object Spring Level Sets (MUSCLE)," Medical Image Computing and Computer Assisted Intervention (MICCAI), Conference on, 2012. ([see paper](http://cs.jhu.edu/~blake/docs/sled_miccai2012.pdf))

B. C. Lucas, M. Kazhdan, and R. H. Taylor, " Multi-Object Geodesic Active Contours (MOGAC)," Medical Image Computing and Computer Assisted Intervention (MICCAI), Conference on, 2012. ([see paper](http://cs.jhu.edu/~blake/docs/mogac_miccai12.pdf))

B. C. Lucas, M. Kazhdan, and R. H. Taylor, "SpringLS: A Deformable Model Representation to provide Interoperability between Meshes and Level Sets", Medical Image Computing and Computer Assisted Intervention (MICCAI), Conference on, Toronto, Sept 18-22, 2011. ([see paper](http://cs.jhu.edu/~blake/docs/springls_miccai_final.pdf))

B. Lucas, J. Bogovic, A. Carass, P.-L. Bazin, J. Prince, D. Pham, and B. Landman, "The Java Image Science Toolkit (JIST) for Rapid Prototyping and Publishing of Neuroimaging Software," Neuroinformatics, vol. 8, pp. 5-17, 2010. ([see paper](http://www.ncbi.nlm.nih.gov/pubmed/20077162),[website](http://www.nitrc.org/projects/jist/))