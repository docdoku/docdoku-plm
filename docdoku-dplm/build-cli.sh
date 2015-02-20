#!/bin/bash

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SOURCES=${BASE_DIR}/ui
CLI_DIR=${BASE_DIR}/../docdoku-cli

echo "Building docdoku-cli ... ";
cd ${CLI_DIR}
mvn clean install
cp ${CLI_DIR}/target/docdoku-cli-jar-with-dependencies.jar ${SOURCES}/app/
echo "... done"
