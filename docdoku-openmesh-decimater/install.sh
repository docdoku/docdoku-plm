#!/bin/bash

# Builds the sources in a build directory
mkdir -p build && cd build && rm *.o
qmake ../src/OpenMeshDecimater.pro && make

# Install the decimater to destination

## Create dirs
mkdir -p /opt/decimater
mkdir -p /opt/decimater/lib/

## Copy executable and launch script
cp OpenMeshDecimater /opt/decimater/
cp ../src/openMeshDecimater.sh /opt/decimater/

## Copy shared objects
if [ $(getconf LONG_BIT) == "64" ]
then
    echo "Using 64 bit shared objects"
    cp ../src/lib64/* /opt/decimater/lib/
else
    echo "Using 32 bit shared objects"
    cp ../src/lib32/* /opt/decimater/lib/
fi
