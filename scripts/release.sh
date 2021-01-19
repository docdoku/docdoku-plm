#!/bin/sh

if [ $# -lt 2 ]
  then
    echo "Usage : $0 <release version> <snapshot version>"
    exit 1
fi

RELEASE=$1
SNAPSHOT=$2

git submodule foreach "

if test -f pom.xml; then
    git checkout dev
    mvn versions:set -DnewVersion=$RELEASE
    git commit -am \"Prepare $RELEASE release\"
    git tag $RELEASE
    mvn versions:set -DnewVersion=$SNAPSHOT
    git commit -am \"Prepare $SNAPSHOT snapshot\"
    git checkout master
    git merge $RELEASE
fi

if test -f package.json; then
    git checkout dev
    npm version $RELEASE
    git commit -am \"Prepare $RELEASE release\"
    git tag $RELEASE
    npm version $SNAPSHOT
    git commit -am \"Prepare $SNAPSHOT snapshot\"
    git checkout master
    git merge $RELEASE
fi

"
