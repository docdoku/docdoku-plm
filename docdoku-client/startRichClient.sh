#!/bin/bash

java -classpath ~/.m2/repository/commons-codec/commons-codec/1.7/commons-code-1.7.jar:~/.m2/repository/com/l2fprod/common/l2fprod-common-directorychooser/6.9.1/l2fprod-common-directorychooser-6.9.1.jar:~/.m2/repository/com/l2fprod/common/l2fprod-common-shared/6.9.1/l2fprod-common-shared-6.9.1.jar:~/.m2/repository/uk/co/mmscomputing/scanner/1.0/scanner-1.0.jar:~/.m2/repository/org/swinglabs/swingx/1.6.1/swingx-1.6.1.jar:target/docdoku-client.jar:../docdoku-common/target/docdoku-common.jar com.docdoku.client.ExplorerBoot http://localhost:8080/apps
