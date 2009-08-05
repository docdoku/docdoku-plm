cp ../DocDokuClient/dist/DocDokuClient.jar DocDoku-war/web/apps/docdoku_client.jar
cp ../DocDoku-Common/dist/DocDoku-Common.jar DocDoku-war/web/apps/docdoku_common.jar


pack200 --repack --strip-debug DocDoku-war/web/apps/docdoku_client.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/docdoku_common.jar

jarsigner -storepass changeit DocDoku-war/web/apps/docdoku_client.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/docdoku_common.jar docdoku2


pack200 --strip-debug DocDoku-war/web/apps/docdoku_client.jar.pack.gz DocDoku-war/web/apps/docdoku_client.jar
pack200 --strip-debug DocDoku-war/web/apps/docdoku_common.jar.pack.gz DocDoku-war/web/apps/docdoku_common.jar


