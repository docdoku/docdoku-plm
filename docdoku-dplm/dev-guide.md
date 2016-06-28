# DPLM dev guide

## Download NodeWebkit

Download the appropriate version for your local environment 32/64 bits:

* http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-linux-ia32.tar.gz
* http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-linux-x64.tar.gz
* http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-osx-ia32.zip
* http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-osx-x64.zip
* http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-win-ia32.zip
* http://dl.node-webkit.org/v0.11.1/node-webkit-v0.11.1-win-x64.zip

Unzip the archive, `nw` executable is the command to launch a node webkit shell

## Run the app in dev mode

Install dependencies

    $ npm install

To activate the debug tools in node webkit, edit `ui/app/package.json`, set toolbar value to `true`    

Then run :

    $ ./build-cli.sh
    $ /path/to/nw ui/app
    
Add `nw` to your $PATH if wanted

    $ nw ui/app    
    
## Build the app
    
Builds all platforms releases 

    $ ./release.sh

## Known issues

* https://github.com/docdoku/docdoku-plm/issues/940 