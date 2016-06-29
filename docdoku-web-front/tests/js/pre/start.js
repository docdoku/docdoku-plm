/*global casper,homeUrl*/

'use strict';

var extend = function (destination, source) {
    for (var property in source) {
        if (destination[property] && (typeof(destination[property]) === 'object') &&
            (destination[property].toString() === '[object Object]') && source[property]) {
            extend(destination[property], source[property]);
        }
        else {
            destination[property] = source[property];
        }
    }
    return destination;
};

var conf = casper.cli.options;

// This is the first file in the tests suite : use casper.start()
casper.options.viewportSize = {
    width: 1680,
    height: 1050
};

// add AJAX waiting logic to onResourceRequested

casper.options.onResourceRequested = function (casper, requestData) {
    if (conf.debugRequests) {
        if(requestData.url.match('/api/')){
            console.log(requestData.method + ' ' + requestData.url);
            console.log(JSON.stringify(requestData.headers));
        }
    }

    if (conf.waitOnRequest) {
        // do not wait for fonts
        if(requestData.url.indexOf('fonts') === -1 && requestData.url.indexOf('livereload.js') === -1) {
            this.log('Waiting for AJAX request: ' + requestData.url, 'info');
            casper.waitForResource(requestData.url, function () {
                this.log('AJAX request returned: ' + requestData.url, 'info');
            }, function () {
                this.log('AJAX request didn\'t return after wait period: ' + requestData.url, 'warning');
            }, conf.requestTimeOut || 100);
        }
    }
};

if (conf.debugResponses) {
    casper.options.onResourceReceived = function (C, response) {
        if(response.url.match('/api/')){
            console.log('#'+response.status + ' ' +response.statusText + ' ' + response.url);
            console.log(JSON.stringify(response.headers));
        }
    };
}
// Wait actions
casper.options.waitTimeout = 10 * 1000; // 10 sec
// Global test duration
casper.options.timeout = conf.globalTimeout * 60 * 1000;

casper.start();

casper.setFilter('page.confirm', function (msg) {
    this.log('Confirm box: ' + msg, 'warning');
    return true;
});

casper.on('remote.alert', function (msg) {
    this.log('Alert box: ' + msg, 'warning');
    this.capture('screenshot/alert/' + Date.now() + '.png');
    if (conf.debug) {
        this.debugHTML();
    }
    return true;
});

casper.on('remote.message', function remoteMessage(message) {
    this.log('[WebConsole] ' + message, 'info');
});

casper.test.begin('DocdokuPLM Tests suite', 1, function docdokuPLMTestsSuite() {
    casper.thenOpen(homeUrl, function homePageLoaded() {
        this.test.assert(true, 'Tests begins');
    });
    casper.run(function letsGo() {
        this.test.done();
    });
});
