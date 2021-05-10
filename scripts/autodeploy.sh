#!/bin/bash

set -e

mvn clean install -f docdoku-plm-server/pom.xml
cp docdoku-plm-server/docdoku-plm-server-ear/target/docdoku-plm-server-ear.ear docdoku-plm-docker/autodeploy/
