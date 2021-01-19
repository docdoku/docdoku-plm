#!/bin/sh

if [ $# -eq 0 ]
  then
    echo "Usage : $0 <branch name>"
    exit 1
fi

BRANCH=$1

git submodule foreach "git checkout $BRANCH"
