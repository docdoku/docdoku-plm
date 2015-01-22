#!/bin/sh

# Requirements : OpenMesh
# Please, visit http://www.openmesh.org/media/Documentations/OpenMesh-Doc-Latest/a00030.html for more informations.
# to install qt on your server :

apt-get install cmake g++ qt-sdk;
mkdir -p openmesh;
cd openmesh;
[[ -f OpenMesh-3.2.tar.gz ]] || wget http://www.openmesh.org/media/Releases/3.2/OpenMesh-3.2.tar.gz
tar -xzvf OpenMesh-3.2.tar.gz;
cd OpenMesh-3.2;
mkdir build && cd $_;
cmake -DCMAKE_BUILD_TYPE=Release .. && make install;
