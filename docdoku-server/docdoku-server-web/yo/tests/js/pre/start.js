/*global casper,homeUrl*/

var ci = require('../../config.ci');
var local = require('../../config.local');
var extend = function(destination, source){
    for (var property in source){
        if (destination[property] && (typeof(destination[property]) == 'object')
            && (destination[property].toString() == '[object Object]') && source[property]){
            extend(destination[property], source[property]);
        }
        else{
            destination[property] = source[property];
        }
    }
    return destination;
};

var conf = extend(ci, local);

// This is the first file in the tests suite : use casper.start()
casper.options.viewportSize = {
    width: 1680,
    height: 1050
};

// add AJAX waiting logic to onResourceRequested
if(conf.waitOnRequest) {
    casper.options.onResourceRequested = function (casper, requestData){

        this.log("Waiting for AJAX request: " + requestData.url,'info');
        casper.waitForResource(requestData.url, function(){
            this.log("AJAX request returned: " + requestData.url,'info');
        }, function(){
            this.log("AJAX request didn't return after wait period: " + requestData.url,'warning');
        }, conf.requestTimeOut);

    };

}

// Wait actions
casper.options.waitTimeout = 10 * 1000; // 10 sec
// Global test duration
casper.options.timeout =  20 * 60 * 1000; // 15 min

casper.start();

casper.setFilter('page.confirm', function(msg) {
    'use strict';
    this.log('Confirm box: '+msg,'warning');
    return true;
});

casper.on('remote.alert', function(msg) {
    'use strict';
    this.log('Alert box: '+msg,'warning');
    this.capture('screenshot/alert/'+Date.now()+'.png');
    if(conf.debug) {
        this.debugHTML();
    }
    return true;
});

casper.on('remote.message', function remoteMessage(message) {
    'use strict';
    this.log('[WebConsole] '+message,'info');
});

casper.test.begin('DocdokuPLM Tests suite',1, function docdokuPLMTestsSuite() {
    'use strict';
    casper.thenOpen(homeUrl,function homePageLoaded(){
        this.test.assert(true,'Tests begins');
    });
    casper.run(function letsGo(){
        this.test.done();
    });
});
