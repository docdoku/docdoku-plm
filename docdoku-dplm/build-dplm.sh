#!/bin/bash

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SOURCES=${BASE_DIR}/ui

echo "Building docdoku-dplm ... ";
cd ${SOURCES}
grunt build;
[[ $? -eq 0 ]] || { echo "Grunt build failed"; exit 1; }
echo "... done"
