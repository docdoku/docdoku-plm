# Docdoku API JS

Generate a node module to integrate in your nodejs and/or browser application

## Documentation

This module is designed to work in a nodejs or a browser application

### Browser

Add the script to your page, then it's available from the window object

See [example](example/browser/index.html)

### Node Js

See [example](example/npm/index.js)

## Development guide

Package folders description

* `example` : examples for browser and nodejs usage
* `exports` : Export to window object code 
* `target/docdoku-plm-api/` : generated sources from swagger codegen

Build 
    
* Run `mvn clean install` in docdoku-api module
* Run `mvn clean install` in this module directory
