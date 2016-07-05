# DPLM dev guide

## Run the app in dev mode

First, build the app to get all dependencies

    $ npm run build

This will download the right version of node webkit into `cache` folder and install all needed dependencies. This may take a while.

Then, depending on your platform, run :

    grunt dev:<platform>

Available platforms are : `linux32`, `linux64`, `osx64`, `win32`, `win64`

Use F12 to open DevTools

## Build the app
    
    $ npm run build

## Known issues

* https://github.com/docdoku/docdoku-plm/issues/940 