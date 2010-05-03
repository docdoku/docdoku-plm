copy ..\DocDokuClient\dist\DocDokuClient.jar DocDoku-war\web\apps\docdoku_client.jar
copy ..\DocDoku-Common\dist\DocDoku-Common.jar DocDoku-war\web\apps\docdoku_common.jar

pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/docdoku_client.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/docdoku_common.jar

jarsigner -storepass changeit DocDoku-war/web/apps/docdoku_client.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/docdoku_common.jar docdoku3

pack200 --segment-limit=-1 DocDoku-war/web/apps/docdoku_client.jar.pack.gz DocDoku-war/web/apps/docdoku_client.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/docdoku_common.jar.pack.gz DocDoku-war/web/apps/docdoku_common.jar


pause
