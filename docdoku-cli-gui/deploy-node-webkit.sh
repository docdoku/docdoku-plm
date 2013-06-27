#!/bin/sh
killall nw;
echo "Cleaning old app";
rm -f app.nw;
echo "Creating new app archive";
zip -r ../app.zip *;
cd ..;
mv app.zip app.nw;
echo "Run the app";
/home/cangac/Projets/node-webkit-v0.5.1-linux-x64/nw app.nw;