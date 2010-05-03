#!/bin/sh

cp ../DocDokuClient/dist/DocDokuClient.jar DocDoku-war/web/apps/docdoku_client.jar
cp ../DocDoku-Common/dist/DocDoku-Common.jar DocDoku-war/web/apps/docdoku_common.jar

/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/docdoku_client.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/docdoku_common.jar

/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/docdoku_client.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/docdoku_common.jar docdoku3

/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/docdoku_client.jar.pack.gz DocDoku-war/web/apps/docdoku_client.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/docdoku_common.jar.pack.gz DocDoku-war/web/apps/docdoku_common.jar


