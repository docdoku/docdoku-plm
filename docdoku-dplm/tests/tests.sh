#!/bin/sh

OPTIONS_HUMAN="-u foo -p bar -h localhost -P 8080"
OPTIONS_JSON="-u foo -p bar -h localhost -P 8080 -F json"
DPLM="java -Xmx1024M -cp ../ui/dplm/docdoku-cli-jar-with-dependencies.jar com.docdoku.cli.MainCommand"

echo "Human test"
${DPLM} wl ${OPTIONS_HUMAN}
echo "Json test"
${DPLM} wl ${OPTIONS_JSON}
