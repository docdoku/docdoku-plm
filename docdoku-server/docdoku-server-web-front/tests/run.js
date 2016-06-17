/*global require,__dirname*/
/*
 * Node script wrapper to execute casper executable
 * execution : node run.js
 * configuration : config.js - override any arguments from command line
 * node run.js --domain=foo.com --port=8383   ...
 * */

'use strict';

var util = require('util');
var fs = require('fs');
var exec = require('child_process').exec;
var _ = require('underscore');
var config = require('./config');
var xml2js = require('xml2js');
var del = require('del');
var argv = require('yargs').argv;

del.sync(['screenshot/**']);

var conf = _.extend(config, argv);

var casperCommand = 'casperjs test' +
                    ' --ignore-ssl-errors=true '+
                    ' --protocol=' + conf.protocol +
                    ' --domain=' + conf.domain +
                    ' --port=' + conf.port +
                    ' --login=' + conf.login +
                    ' --pass=' + conf.pass +
                    ' --workspace=' + conf.workspace +
                    ' --contextPath=' + conf.contextPath +
                    ' --requestTimeOut=' + conf.requestTimeOut +
                    ' --globalTimeout=' + conf.globalTimeout +
                    (conf.debug ? ' --debug=true' : '') +
                    (conf.debugRequests ? ' --debugRequests=true' : '') +
                    (conf.waitOnRequest ? ' --waitOnRequest=true' : '') +
                    (conf.debugResponses ? ' --debugResponses=true' : '') +
                    ' --pre=' + conf.pre.join(',') +
                    ' --post=' + conf.post.join(',') +
                    ' --includes=' + conf.includes.join(',') +
                    ' --xunit=' + conf.xunit +
                    (conf.failFast ? ' --fail-fast' : '') +
                    (conf.verbose ? ' --verbose' : '') +
                    ' --log-level='+conf.logLevel +
                    ' ' + conf.paths.join(' ');

util.print('Running DocdokuPLM tests. Command : \n ' + casperCommand + '\n\n');

var child = exec(casperCommand, {maxBuffer: 5 * 1024 * 1024}, function (error) {
    util.print(error||'');
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

child.stdout.on('data', util.print);
child.stderr.on('data', util.print);
