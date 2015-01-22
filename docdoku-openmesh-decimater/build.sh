#!/bin/bash

# Builds the sources in a build directory
mkdir -p build && cd build && rm *.o
qmake ../src/OpenMeshDecimater.pro && make
