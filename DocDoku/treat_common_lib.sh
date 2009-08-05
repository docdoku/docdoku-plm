rm DocDoku-war/web/apps/lib/docdoku_common.jar
rm DocDoku-war/web/apps/lib/docdoku_common.jar.pack.gz
mv DocDoku-war/web/apps/lib/DocDoku-Common.jar DocDoku-war/web/apps/lib/docdoku_common.jar

pack200 --repack --strip-debug DocDoku-war/web/apps/lib/docdoku_common.jar
jarsigner -storepass changeit DocDoku-war/web/apps/lib/docdoku_common.jar docdoku
pack200 --strip-debug DocDoku-war/web/apps/lib/docdoku_common.jar.pack.gz DocDoku-war/web/apps/lib/docdoku_common.jar



rm DocDoku-war/web/apps/docdoku_client.jar
rm DocDoku-war/web/apps/docdoku_client.jar.pack.gz
mv DocDoku-war/web/apps/DocDokuClient.jar DocDoku-war/web/apps/docdoku_client.jar

pack200 --repack --strip-debug DocDoku-war/web/apps/docdoku_client.jar
jarsigner -storepass changeit DocDoku-war/web/apps/docdoku_client.jar docdoku
pack200 --strip-debug DocDoku-war/web/apps/docdoku_client.jar.pack.gz DocDoku-war/web/apps/docdoku_client.jar
