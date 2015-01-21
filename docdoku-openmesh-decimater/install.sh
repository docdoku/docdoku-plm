#!/bin/bash

mkdir -p build && cd build && rm -rf *;
qmake ../src/OpenMeshDecimater.pro
make
rm Makefile;
mkdir -p /opt/decimater;
cp * /opt/decimater/
cp ../src/openMeshDecimater.sh /opt/decimater/

mkdir -p /opt/decimater/lib/

if [ $(getconf LONG_BIT) == "64" ]
then
    echo "Using 64 bit shared objects";
    cp ../src/lib64/* /opt/decimater/lib/
else
    echo "Using 32 bit shared objects";
    cp ../src/lib32/* /opt/decimater/lib/
fi
