#!/bin/sh

git submodule foreach "
  git fetch origin -p&& \
  git reset --hard && \
  git checkout master && \
  git reset --hard origin/master && \
  git checkout dev && \
  git reset --hard origin/dev
"

