/*global casper,homeUrl*/

// This is the first file in the tests suite : use casper.start()
casper.options.viewportSize = {
    width: 1680,
    height: 1050
};

// Wait actions
casper.options.waitTimeout = 10 * 1000; // 10 sec
// Global test duration
casper.options.timeout =  15 * 60 * 1000; // 15 min

casper.start();

casper.setFilter('page.confirm', function(msg) {
    'use strict';
    this.log('Confirm box: '+msg,'warning');
    return true;
});

casper.on('remote.message', function remoteMessage(message) {
    'use strict';
    this.log(message,'info');
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
