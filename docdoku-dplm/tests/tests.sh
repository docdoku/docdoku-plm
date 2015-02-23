#!/bin/sh

OPTIONS_HUMAN="-u foo -p bar -h localhost -P 8080"
OPTIONS_JSON="-u foo -p bar -h localhost -P 8080 -F json"
WRONG_AUTH_HUMAN="-u azerty -p azerty -h localhost -P 8080"
WRONG_AUTH_JSON="-u azerty -p azerty -h localhost -P 8080 -F json"

DPLM="java -Xmx1024M -cp ../../ui/app/docdoku-cli-jar-with-dependencies.jar com.docdoku.cli.MainCommand"
SEP="--------------------------------------------------------"

mkdir -p workspace && cd workspace;

echo "Without params"
echo ${SEP}
${DPLM}
echo ${SEP}
${DPLM} -F json
echo ${SEP}

echo "Help"
echo ${SEP}
${DPLM} h
echo ${SEP}
${DPLM} h -F json
echo ${SEP}

echo "Help checkout"
echo ${SEP}
${DPLM} h co
echo ${SEP}
${DPLM} h co -F json
echo ${SEP}

echo "Wrong authent"
echo ${SEP}
${DPLM} wl ${WRONG_AUTH_HUMAN}
echo ${SEP}
${DPLM} wl ${WRONG_AUTH_JSON}
echo ${SEP}

echo "Workspace list"
echo ${SEP}
${DPLM} wl ${OPTIONS_HUMAN}
echo ${SEP}
${DPLM} wl ${OPTIONS_JSON}
echo ${SEP}

echo "Part count"
${DPLM} list part ${OPTIONS_HUMAN} -w cli -c
echo ${SEP}
${DPLM} list part ${OPTIONS_JSON} -w cli -c
echo ${SEP}

echo "Part list"
echo ${SEP}
${DPLM} list part ${OPTIONS_HUMAN} -w cli
echo ${SEP}
${DPLM} list part ${OPTIONS_JSON} -w cli
echo ${SEP}

echo "Part list with start and max"
echo ${SEP}
${DPLM} list part ${OPTIONS_HUMAN} -w cli -s 2 -m 1
echo ${SEP}
${DPLM} list part ${OPTIONS_JSON} -w cli -s 2 -m 1
echo ${SEP}

echo "Part list without workspace"
echo ${SEP}
${DPLM} list part ${OPTIONS_HUMAN}
echo ${SEP}
${DPLM} list part ${OPTIONS_JSON}
echo ${SEP}

echo "Baseline list"
echo ${SEP}
${DPLM} bl ${OPTIONS_HUMAN} -w cli -o test-part-001 -r A
echo ${SEP}
${DPLM} bl ${OPTIONS_JSON} -w cli -o test-part-001 -r A
echo ${SEP}

echo "Checkout part, then undo co"
echo ${SEP}
${DPLM} co part ${OPTIONS_HUMAN} -w cli -o test-part-001 -r A
echo ${SEP}
${DPLM} uco part ${OPTIONS_HUMAN} -w cli -o test-part-001 -r A
echo ${SEP}

echo "Checkout part, then undo co (json)"
echo ${SEP}
${DPLM} co part ${OPTIONS_JSON} -w cli -o test-part-001 -r A
echo ${SEP}
${DPLM} uco part ${OPTIONS_JSON} -w cli -o test-part-001 -r A
echo ${SEP}

echo "Download part"
echo ${SEP}
${DPLM} get part ${OPTIONS_HUMAN} -w cli -o objfile -r A
echo ${SEP}
${DPLM} get part ${OPTIONS_JSON} -w cli -o objfile -r A
echo ${SEP}

echo "Checkout part, then checkin with part number (human)"
echo ${SEP}
${DPLM} co part ${OPTIONS_HUMAN} -w cli -o objfile -r A
echo ${SEP}
${DPLM} ci part ${OPTIONS_HUMAN} -w cli -o objfile -r A
echo ${SEP}

echo "Checkout part, then checkin via file name (human)"
echo ${SEP}
${DPLM} co part ${OPTIONS_HUMAN} -w cli -o objfile -r A
echo ${SEP}
${DPLM} ci part ${OPTIONS_HUMAN} -w cli pumpkin_tall_10k.obj
echo ${SEP}

echo "Checkout part, then checkin with part number (json)"
echo ${SEP}
${DPLM} co part ${OPTIONS_JSON} -w cli -o objfile -r A
echo ${SEP}
${DPLM} ci part ${OPTIONS_JSON} -w cli -o objfile -r A
echo ${SEP}

echo "Checkout part, then checkin via file name (json)"
echo ${SEP}
${DPLM} co part ${OPTIONS_JSON} -w cli -o objfile -r A
echo ${SEP}
${DPLM} ci part ${OPTIONS_JSON} -w cli pumpkin_tall_10k.obj
echo ${SEP}

