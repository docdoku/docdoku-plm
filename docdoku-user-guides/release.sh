#!/bin/sh
# Usage release.sh  tagname
cd $(dirname "$0");
cd app;
bower install;
cd ..;
npm install;
grunt build;
