#!/bin/sh

if [ $# -ne 1 ] || [ "$1" != "SSH" ] && [ "$1" != "HTTPS" ]
  then
    echo "Usage : $0 <protocol>"
    echo " protocol must be HTTPS or SSH"
    exit 1
fi

HTTPS_BASE=https://github.com/docdoku
SSH_BASE=git@github.com:docdoku

if [ $1 = 'SSH' ];
then 
  BASE=${SSH_BASE}
else 
  BASE=${HTTPS_BASE}
fi

git submodule foreach '
  git remote remove origin
  git remote add origin '$BASE'/${name}.git
  git remote -v
'

