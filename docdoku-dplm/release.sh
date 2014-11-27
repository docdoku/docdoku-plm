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
bash build-dplm.sh;
bash download.sh;
bash zip-ui.sh;
bash linux-all-platforms.sh;
bash windows-all-platforms.sh;
bash osx-all-platforms.sh;
bash export.sh;