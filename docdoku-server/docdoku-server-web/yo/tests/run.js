/*
 * Node script wrapper to execute casper executable
 * execution : node run.js
 * configuration : config.ci.json (override it with config.local.json)
 * */

'use strict';

var sys = require('sys');
var fs = require('fs');
var exec = require('child_process').exec;
var _ = require('underscore');
var ci = require('./config.ci');
var local = require('./config.local');

var conf = _.extend(ci, local);

var casperCommand = 'casperjs test'
    + ' --domain=' + conf.domain
    + ' --port=' + conf.port
    + ' --login=' + conf.login
    + ' --pass=' + conf.pass
    + ' --workspace=' + conf.workspace
    + ' --contextPath=' + conf.contextPath
    + ' --pre=' + conf.pre.join(',')
    + ' --post=' + conf.post.join(',')
    + ' --includes=' + conf.includes.join(',')
    + ' --xunit=' + conf.xunit
    + ' --fail-fast'
    + (conf.verbose ? ' --verbose' : '')
    +' --log-level='+conf.logLevel
    + ' ' + conf.paths.join(' ');

sys.print('Running DocdokuPLM tests. Command : \n ' + casperCommand + '\n\n');

var child = exec(casperCommand, function (error) {
    sys.print(error||'');
});

child.stdout.on('data', sys.print);

child.stderr.on('data', sys.print);

