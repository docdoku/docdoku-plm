#!/bin/bash

java -classpath lib/commons-code-1.3.jar:lib/l2fprod.jar:lib/scanner.jar:lib/swingx.jar:target/docdoku-client.jar:../docdoku-common/target/docdoku-common.jar com.docdoku.client.ExplorerBoot http://localhost:8080/apps
