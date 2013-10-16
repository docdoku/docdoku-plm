/*
    Make requireJs and node native require work together
    Require all needed modules.
*/
var util = require("util");
var os = require("os");
var gui = require('nw.gui');
var path = require('path');
var fs = require('fs');
var wrench = require('wrench');
var moment = require('moment');
var exec = require('child_process').exec;
var spawnDir = process.cwd();
var classPath = spawnDir + '/dplm/docdoku-cli-jar-with-dependencies.jar';

if(gui.App.argv[0] == "devtools"){
    gui.Window.get().showDevTools();
}

window.requireNode = window.require;
// require will be replaced by requireJs
delete window.require;