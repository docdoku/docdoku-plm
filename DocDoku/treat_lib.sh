pack200 --repack --strip-debug DocDoku-war/web/apps/lib/swingx.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/synthetica.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/syntheticaAddons.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/commons-logging-1.1.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/commons-codec-1.3.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/commons-httpclient-3.1.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/ws.jar
pack200 --repack --strip-debug DocDoku-war/web/apps/lib/javaee.jar


jarsigner -storepass changeit DocDoku-war/web/apps/lib/swingx.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/synthetica.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaAddons.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/commons-logging-1.1.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/commons-codec-1.3.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/commons-httpclient-3.1.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/ws.jar docdoku2
jarsigner -storepass changeit DocDoku-war/web/apps/lib/javaee.jar docdoku2

pack200 --strip-debug DocDoku-war/web/apps/lib/swingx.jar.pack.gz DocDoku-war/web/apps/lib/swingx.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/synthetica.jar.pack.gz DocDoku-war/web/apps/lib/synthetica.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/syntheticaAddons.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaAddons.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/commons-logging-1.1.jar.pack.gz DocDoku-war/web/apps/lib/commons-logging-1.1.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/commons-codec-1.3.jar.pack.gz DocDoku-war/web/apps/lib/commons-codec-1.3.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/commons-httpclient-3.1.jar.pack.gz DocDoku-war/web/apps/lib/commons-httpclient-3.1.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/ws.jar.pack.gz DocDoku-war/web/apps/lib/ws.jar
pack200 --strip-debug DocDoku-war/web/apps/lib/javaee.jar.pack.gz DocDoku-war/web/apps/lib/javaee.jar

