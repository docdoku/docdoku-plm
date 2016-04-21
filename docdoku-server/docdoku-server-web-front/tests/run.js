/*global require,__dirname*/
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
var xml2js = require('xml2js');
var del = require('del');

del.sync(['screenshot/**']);

var conf = _.extend(ci, local);

var casperCommand = 'casperjs test' +
                    ' --ignore-ssl-errors=true '+
                    ' --protocol=' + conf.protocol +
                    ' --domain=' + conf.domain +
                    ' --port=' + conf.port +
                    ' --login=' + conf.login +
                    ' --pass=' + conf.pass +
                    ' --workspace=' + conf.workspace +
                    ' --contextPath=' + conf.contextPath +
                    ' --pre=' + conf.pre.join(',') +
                    ' --post=' + conf.post.join(',') +
                    ' --includes=' + conf.includes.join(',') +
                    ' --xunit=' + conf.xunit +
                    (conf.failFast ? ' --fail-fast' : '') +
                    (conf.verbose ? ' --verbose' : '') +
                    ' --log-level='+conf.logLevel +
                    ' ' + conf.paths.join(' ');

sys.print('Running DocdokuPLM tests. Command : \n ' + casperCommand + '\n\n');

var child = exec(casperCommand, {maxBuffer: 1024 * 1024}, function (error) {
    sys.print(error||'');
    if(conf.soundOnTestsEnd){
        var parser = new xml2js.Parser();
        fs.readFile(__dirname + '/results.xml', function(err, data) {
            parser.parseString(data, function (err, result) {
                var suites = result.testsuites.testsuite;
                var lastSuite = suites[suites.length-1];
                if(lastSuite.$.failures !== '0'){
                    exec('cvlc --play-and-exit fail.wav');
                }else {
                    exec('cvlc --play-and-exit success.wav');
                }
            });
        });
    }
});

child.stdout.on('data', sys.print);

child.stderr.on('data', sys.print);
