#!/bin/bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
export LD_LIBRARY_PATH=/usr/local/lib/OpenMesh:$LD_LIBRARY_PATH
exec ${DIR}/OpenMeshDecimater $*
