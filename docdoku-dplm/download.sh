#!/bin/bash

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TMP_DIR=${BASE_DIR}/tmp

echo "Downloading node webkit if needed ..."

[[ -f ${TMP_DIR}/node-webkit-v0.11.1-linux-ia32.tar.gz ]] || wget http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-linux-ia32.tar.gz -P ${TMP_DIR}
[[ -f ${TMP_DIR}/node-webkit-v0.11.1-linux-x64.tar.gz ]] || wget http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-linux-x64.tar.gz -P ${TMP_DIR}
[[ -f ${TMP_DIR}/node-webkit-v0.11.1-osx-ia32.zip ]] || wget http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-osx-ia32.zip -P ${TMP_DIR}
[[ -f ${TMP_DIR}/node-webkit-v0.11.1-osx-x64.zip ]] || wget http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-osx-x64.zip -P ${TMP_DIR}
[[ -f ${TMP_DIR}/node-webkit-v0.11.1-win-ia32.zip ]] || wget http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-win-ia32.zip -P ${TMP_DIR}
[[ -f ${TMP_DIR}/node-webkit-v0.11.1-win-x64.zip ]] || wget http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-win-x64.zip -P ${TMP_DIR}

echo "... done\n Building docdoku-cli ..."