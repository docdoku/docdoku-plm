#!/bin/sh

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TMP_DIR=${BASE_DIR}/tmp
OUT_DIR=${BASE_DIR}/../docdoku-server/docdoku-server-web/yo/app/download/dplm
mkdir -p ${OUT_DIR}

echo "Copying zips for download in DocdokuPLM"

cp ${TMP_DIR}/linux-ia32/dplm-linux-32.zip ${OUT_DIR}
cp ${TMP_DIR}/linux-x64/dplm-linux-64.zip ${OUT_DIR}
cp ${TMP_DIR}/win-ia32/dplm-win-32.zip ${OUT_DIR}
cp ${TMP_DIR}/win-x64/dplm-win-64.zip ${OUT_DIR}
cp ${TMP_DIR}/osx-ia32/dplm-osx-32.zip ${OUT_DIR}
cp ${TMP_DIR}/osx-x64/dplm-osx-64.zip ${OUT_DIR}


echo "... done"
