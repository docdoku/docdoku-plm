#!/bin/bash

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SOURCES=${BASE_DIR}/ui-v2

echo "Building docdoku-dplm ... ";
cd ${SOURCES}
grunt build;
echo "... done"
