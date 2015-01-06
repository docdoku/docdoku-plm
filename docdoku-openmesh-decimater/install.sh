#!/bin/sh
cd build
rm lod.o main.o OpenMeshDecimater Makefile;
qmake ../src/OpenMeshDecimater.pro
make
rm Makefile;
mkdir -p /opt/decimater;
cp * /opt/decimater/
cp ../src/openMeshDecimater.sh /opt/decimater/