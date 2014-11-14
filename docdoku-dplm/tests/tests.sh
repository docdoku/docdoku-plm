#!/bin/sh

OPTIONS_HUMAN="-u foo -p bar -h localhost -P 8080"
OPTIONS_JSON="-u foo -p bar -h localhost -P 8080 -F json"
WRONG_AUTH_HUMAN="-u azerty -p azerty -h localhost -P 8080"
WRONG_AUTH_JSON="-u azerty -p azerty -h localhost -P 8080 -F json"

DPLM="java -Xmx1024M -cp ../../ui/dplm/docdoku-cli-jar-with-dependencies.jar com.docdoku.cli.MainCommand"
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
${DPLM} pl ${OPTIONS_HUMAN} -w foo -c
echo ${SEP}
${DPLM} pl ${OPTIONS_JSON} -w foo -c
echo ${SEP}

echo "Part list"
echo ${SEP}
${DPLM} pl ${OPTIONS_HUMAN} -w foo
echo ${SEP}
${DPLM} pl ${OPTIONS_JSON} -w foo
echo ${SEP}

echo "Part list with start and max"
echo ${SEP}
${DPLM} pl ${OPTIONS_HUMAN} -w foo -s 2 -m 1
echo ${SEP}
${DPLM} pl ${OPTIONS_JSON} -w foo -s 2 -m 1
echo ${SEP}

echo "Part list without workspace"
echo ${SEP}
${DPLM} pl ${OPTIONS_HUMAN}
echo ${SEP}
${DPLM} pl ${OPTIONS_JSON}
echo ${SEP}

echo "Baseline list"
echo ${SEP}
${DPLM} bl ${OPTIONS_HUMAN} -w foo -o test-part-001 -r A
echo ${SEP}
${DPLM} bl ${OPTIONS_JSON} -w foo -o test-part-001 -r A
echo ${SEP}

echo "Checkout part, then undo co"
echo ${SEP}
${DPLM} co ${OPTIONS_HUMAN} -w foo -o test-part-001 -r A
echo ${SEP}
${DPLM} uco ${OPTIONS_HUMAN} -w foo -o test-part-001 -r A
echo ${SEP}

echo "Checkout part, then undo co (json)"
echo ${SEP}
${DPLM} co ${OPTIONS_JSON} -w foo -o test-part-001 -r A
echo ${SEP}
${DPLM} uco ${OPTIONS_JSON} -w foo -o test-part-001 -r A
echo ${SEP}

echo "Download part"
echo ${SEP}
${DPLM} get ${OPTIONS_HUMAN} -w foo -o objfile -r A
echo ${SEP}
${DPLM} get ${OPTIONS_JSON} -w foo -o objfile -r A
echo ${SEP}

echo "Checkout part, then checkin with part number (human)"
echo ${SEP}
${DPLM} co ${OPTIONS_HUMAN} -w foo -o objfile -r A
echo ${SEP}
${DPLM} ci ${OPTIONS_HUMAN} -w foo -o objfile -r A
echo ${SEP}

echo "Checkout part, then checkin via file name (human)"
echo ${SEP}
${DPLM} co ${OPTIONS_HUMAN} -w foo -o objfile -r A
echo ${SEP}
${DPLM} ci ${OPTIONS_HUMAN} -w foo pumpkin_tall_10k.obj
echo ${SEP}

echo "Checkout part, then checkin with part number (json)"
echo ${SEP}
${DPLM} co ${OPTIONS_JSON} -w foo -o objfile -r A
echo ${SEP}
${DPLM} ci ${OPTIONS_JSON} -w foo -o objfile -r A
echo ${SEP}

echo "Checkout part, then checkin via file name (json)"
echo ${SEP}
${DPLM} co ${OPTIONS_JSON} -w foo -o objfile -r A
echo ${SEP}
${DPLM} ci ${OPTIONS_JSON} -w foo pumpkin_tall_10k.obj
echo ${SEP}

