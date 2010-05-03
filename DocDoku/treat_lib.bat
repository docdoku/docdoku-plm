pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/commons-codec-1.3.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/swingx.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/synthetica.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaAddons.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaBlueMoonAddon.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/l2fprod.jar
pack200 -J-Xmx256M --repack --segment-limit=-1 DocDoku-war/web/apps/lib/ws.jar
pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/javaee.jar

jarsigner -storepass changeit DocDoku-war/web/apps/lib/commons-codec-1.3.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/swingx.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/synthetica.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaAddons.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaBlueMoonAddon.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/l2fprod.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/ws.jar docdoku3
jarsigner -storepass changeit DocDoku-war/web/apps/lib/javaee.jar docdoku3

pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/commons-codec-1.3.jar.pack.gz DocDoku-war/web/apps/lib/commons-codec-1.3.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/swingx.jar.pack.gz DocDoku-war/web/apps/lib/swingx.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/synthetica.jar.pack.gz DocDoku-war/web/apps/lib/synthetica.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaAddons.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaAddons.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaAddons.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaBlueMoonAddon.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/l2fprod.jar.pack.gz DocDoku-war/web/apps/lib/l2fprod.jar
pack200 -J-Xmx256M --segment-limit=-1 DocDoku-war/web/apps/lib/ws.jar.pack.gz DocDoku-war/web/apps/lib/ws.jar
pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/javaee.jar.pack.gz DocDoku-war/web/apps/lib/javaee.jar

pause

