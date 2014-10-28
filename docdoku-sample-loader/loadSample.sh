#!/bin/bash

java -classpath target/docdoku-sample-loader.jar:../docdoku-common/target/docdoku-common.jar:../docdoku-cli/target/docdoku-cli.jar com.docdoku.loaders.ProductStructureSampleLoader $*