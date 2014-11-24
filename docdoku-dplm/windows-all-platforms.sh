#!/bin/sh

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TMP_DIR=${BASE_DIR}/tmp

cd ${TMP_DIR};

mkdir -p win-ia32 && rm -rf win-ia32/*;
mkdir -p win-x64 && rm -rf win-x64/*;

[[ -d node-webkit-v0.11.1-win-ia32 ]] || unzip node-webkit-v0.11.1-win-ia32.zip
[[ -d node-webkit-v0.11.1-win-x64 ]] || unzip  node-webkit-v0.11.1-win-x64.zip

echo "Building windows 32 bits app ...";

# prepare binary and DLLs
/bin/cat ${TMP_DIR}/node-webkit-v0.11.1-win-ia32/nw.exe ${TMP_DIR}/app.nw > ${TMP_DIR}/win-ia32/dplm.exe
chmod +x ${TMP_DIR}/win-ia32/dplm.exe
cp ${TMP_DIR}/node-webkit-v0.11.1-win-ia32/nw.pak ${TMP_DIR}/win-ia32
cp ${TMP_DIR}/node-webkit-v0.11.1-win-ia32/*.dll ${TMP_DIR}/win-ia32
cp ${TMP_DIR}/node-webkit-v0.11.1-win-ia32/*.dat ${TMP_DIR}/win-ia32
cd ${TMP_DIR}/win-ia32;
zip dplm-win-32.zip *;

echo "... done"
echo "Building windows 64 bits app ...";

# prepare binary and DLLs
/bin/cat ${TMP_DIR}/node-webkit-v0.11.1-win-x64/nw.exe ${TMP_DIR}/app.nw > ${TMP_DIR}/win-x64/dplm.exe
chmod +x ${TMP_DIR}/win-x64/dplm.exe
cp ${TMP_DIR}/node-webkit-v0.11.1-win-x64/nw.pak ${TMP_DIR}/win-x64
cp ${TMP_DIR}/node-webkit-v0.11.1-win-x64/*.dll ${TMP_DIR}/win-x64
cp ${TMP_DIR}/node-webkit-v0.11.1-win-x64/*.dat ${TMP_DIR}/win-x64
cd ${TMP_DIR}/win-x64;
zip dplm-win-64.zip *;

echo "... done"
