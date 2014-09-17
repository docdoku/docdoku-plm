/*global casper*/

// This is the first file in the tests suite : use casper.start()
casper.start();

casper.test.begin('DocdokuPLM Tests suite',0, function docdokuPLMTestsSuite() {

    casper.options.viewportSize = {
        width: 1680,
        height: 1050
    };

    casper.options.waitTimeout = 2000;
    casper.options.timeout = 2000;

    casper.setFilter("page.confirm", function(msg) {
        this.log("Confirm box: "+msg,'info');
        return true;
    });

    casper.on('remote.message', function remoteMessage(message) {
        this.log(message,'info');
    });

    casper.test.done();

});