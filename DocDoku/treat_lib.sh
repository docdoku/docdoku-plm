#!/bin/sh

/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/commons-codec-1.3.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/swingx.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/synthetica.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaAddons.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaBlueMoonAddon.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/l2fprod.jar
/usr/bin/pack200 -J-Xmx256M --repack --segment-limit=-1 DocDoku-war/web/apps/lib/ws.jar
/usr/bin/pack200 --repack --segment-limit=-1 DocDoku-war/web/apps/lib/javaee.jar

/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/commons-codec-1.3.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/swingx.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/synthetica.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaAddons.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/syntheticaBlueMoonAddon.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/l2fprod.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/ws.jar docdoku3
/usr/bin/jarsigner -storepass changeit DocDoku-war/web/apps/lib/javaee.jar docdoku3

/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/commons-codec-1.3.jar.pack.gz DocDoku-war/web/apps/lib/commons-codec-1.3.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/swingx.jar.pack.gz DocDoku-war/web/apps/lib/swingx.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/synthetica.jar.pack.gz DocDoku-war/web/apps/lib/synthetica.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaBlueMoon.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaAddons.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaAddons.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/syntheticaBlueMoonAddon.jar.pack.gz DocDoku-war/web/apps/lib/syntheticaBlueMoonAddon.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/l2fprod.jar.pack.gz DocDoku-war/web/apps/lib/l2fprod.jar
/usr/bin/pack200 -J-Xmx256M --segment-limit=-1 DocDoku-war/web/apps/lib/ws.jar.pack.gz DocDoku-war/web/apps/lib/ws.jar
/usr/bin/pack200 --segment-limit=-1 DocDoku-war/web/apps/lib/javaee.jar.pack.gz DocDoku-war/web/apps/lib/javaee.jar

