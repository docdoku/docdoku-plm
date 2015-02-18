#!/bin/sh

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TMP_DIR=${BASE_DIR}/tmp
SOURCE=${BASE_DIR}/ui/dist

cd ${TMP_DIR};

mkdir -p osx-ia32 && rm -rf osx-ia32/*;
mkdir -p osx-x64 && rm -rf osx-x64/*;

[[ -d node-webkit-v0.11.1-osx-ia32 ]] || unzip node-webkit-v0.11.1-osx-ia32.zip
[[ -d node-webkit-v0.11.1-osx-x64 ]] || unzip  node-webkit-v0.11.1-osx-x64.zip

echo "Building osx 32 bits app ..."

cp -R ${TMP_DIR}/node-webkit-v0.11.1-osx-ia32/node-webkit.app ${TMP_DIR}/osx-ia32
mv ${TMP_DIR}/osx-ia32/node-webkit.app ${TMP_DIR}/osx-ia32/dplm.app
mkdir ${TMP_DIR}/osx-ia32/dplm.app/Contents/Resources/app.nw
cp -R ${SOURCE}/* ${TMP_DIR}/osx-ia32/dplm.app/Contents/Resources/app.nw
cp ${BASE_DIR}/nw.icns ${TMP_DIR}/osx-ia32/dplm.app/Contents/Resources
chmod -R 0775 ${TMP_DIR}/osx-ia32/dplm.app
cd ${TMP_DIR}/osx-ia32
zip -r dplm-osx-32.zip dplm.app

echo "... done"

echo "Building osx 64 bits app ..."

cp -R ${TMP_DIR}/node-webkit-v0.11.1-osx-x64/node-webkit.app ${TMP_DIR}/osx-x64
mv ${TMP_DIR}/osx-x64/node-webkit.app ${TMP_DIR}/osx-x64/dplm.app
mkdir ${TMP_DIR}/osx-x64/dplm.app/Contents/Resources/app.nw
cp -R ${SOURCE}/* ${TMP_DIR}/osx-x64/dplm.app/Contents/Resources/app.nw
cp ${BASE_DIR}/nw.icns ${TMP_DIR}/osx-x64/dplm.app/Contents/Resources
chmod -R 0775 ${TMP_DIR}/osx-x64/dplm.app
cd ${TMP_DIR}/osx-x64
zip -r dplm-osx-64.zip dplm.app

echo "... done"
