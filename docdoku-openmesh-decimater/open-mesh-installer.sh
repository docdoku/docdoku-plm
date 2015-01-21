#!/bin/sh

# requirements cmake, g++, gcc, qt4/qt5
# sudo apt-get install cmake g++ gcc
# Please, visit http://www.openmesh.org/media/Documentations/OpenMesh-Doc-Latest/a00030.html for more informations.
# to install qt on your server :

mkdir -p openmesh;
cd openmesh;
[[ -f OpenMesh-3.3.tar.gz ]] || wget http://www.openmesh.org/media/Releases/3.3/OpenMesh-3.3.tar.gz
tar -xzvf OpenMesh-3.3.tar.gz;
cd OpenMesh-3.3;
mkdir build && cd $_;
cmake -DCMAKE_BUILD_TYPE=Release ..
make install;
