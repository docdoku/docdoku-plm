# DocDoku OpenMesh decimater

## Compiling requirements

To compile the decimater, you need to install a c++ compiler, Qt sdk

For Ubuntu / Debian users :

    apt-get install cmake g++ qt-sdk

First step is to install OpenMesh in release mode.
Download the archive from [this page](http://www.openmesh.org/download/),
then follow the [build instructions](http://www.openmesh.org/media/Documentations/OpenMesh-3.2-Documentation/a00030.html)

Once OpenMesh installed, the decimater can be built :

    mkdir build && cd $_;
    qmake ../src/OpenMeshDecimater.pro && make
    