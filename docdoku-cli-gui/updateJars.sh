#!/bin/sh

cd /home/cangac/Projets/gitsrc/docdoku-plm/docdoku-cli;
mvn clean install;

cp /home/cangac/Projets/gitsrc/docdoku-plm/docdoku-cli/target/*.jar /home/cangac/Projets/gitsrc/docdoku-plm/docdoku-cli-gui/dplm/;