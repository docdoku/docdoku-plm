#!/bin/bash

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TMP_DIR=${BASE_DIR}/tmp
OUT_DIR=${BASE_DIR}/../docdoku-server/docdoku-server-web/src/main/webapp/download/dplm

cd ${TMP_DIR};

mkdir -p linux-ia32 && rm -rf linux-ia32/*;
mkdir -p linux-x64 && rm -rf linux-x64/*;

[[ -d node-webkit-v0.11.1-linux-ia32 ]] || tar -xzvf node-webkit-v0.11.1-linux-ia32.tar.gz
[[ -d node-webkit-v0.11.1-linux-x64 ]] || tar -xzvf node-webkit-v0.11.1-linux-x64.tar.gz

echo "Building linux 32bit app ...";

/bin/cat ${TMP_DIR}/node-webkit-v0.11.1-linux-ia32/nw ${TMP_DIR}/app.nw > ${TMP_DIR}/linux-ia32/dplm
chmod +x ${TMP_DIR}/linux-ia32/dplm
cp ${TMP_DIR}/node-webkit-v0.11.1-linux-ia32/nw.pak ${TMP_DIR}/linux-ia32
cp ${TMP_DIR}/node-webkit-v0.11.1-linux-ia32/*.dat ${TMP_DIR}/linux-ia32
cd ${TMP_DIR}/linux-ia32;
zip dplm-linux-32.zip *;

echo "... done";

echo "Building linux 64bit app ...";

/bin/cat ${TMP_DIR}/node-webkit-v0.11.1-linux-x64/nw ${TMP_DIR}/app.nw > ${TMP_DIR}/linux-x64/dplm
chmod +x ${TMP_DIR}/linux-x64/dplm
cp ${TMP_DIR}/node-webkit-v0.11.1-linux-x64/nw.pak ${TMP_DIR}/linux-x64
cp ${TMP_DIR}/node-webkit-v0.11.1-linux-x64/*.dat ${TMP_DIR}/linux-x64
cd ${TMP_DIR}/linux-x64;
zip dplm-linux-64.zip *;

echo "... done"

