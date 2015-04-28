#!/bin/bash

#
# DPLM Gui Releaser
#
# Author : Morgan Guimard
# Date : Wed Oct 2 2013
#

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

mkdir -p tmp;

bash build-cli.sh;
[[ $? -eq 0 ]] || { echo "Build cli failed"; exit $?; }
bash build-dplm.sh;
[[ $? -eq 0 ]] || { echo "Build dplm failed"; exit $?; }
bash download.sh;
[[ $? -eq 0 ]] || { echo "Download nw failed"; exit $?; }
bash zip-ui.sh;
[[ $? -eq 0 ]] || { echo "Zipping app failed"; exit $?; }
bash linux-all-platforms.sh;
[[ $? -eq 0 ]] || { echo "Linux build failed"; exit $?; }
bash windows-all-platforms.sh;
[[ $? -eq 0 ]] || { echo "Windows build failed"; exit $?; }
bash osx-all-platforms.sh;
[[ $? -eq 0 ]] || { echo "OSX build failed"; exit $?; }
bash export.sh;
[[ $? -eq 0 ]] || { echo "Export built app failed"; exit $?; }

