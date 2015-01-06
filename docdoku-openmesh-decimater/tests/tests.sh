#!/bin/sh
export LD_LIBRARY_PATH=/usr/local/lib/OpenMesh:${LD_LIBRARY_PATH}
mkdir -p build
rm -rf out
mkdir out
cd build
rm lod.o main.o OpenMeshDecimater Makefile;
qmake ../../src/OpenMeshDecimater.pro
make
cd ..
DIR=`pwd`
build/OpenMeshDecimater -i ${DIR}/test.obj -o ${DIR}/out 1 0.6 0.2

