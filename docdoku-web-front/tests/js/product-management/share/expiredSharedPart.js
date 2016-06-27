/*global casper,urls*/

casper.test.begin('Expired private shared part tests suite', 1, function expiredPrivateSharedPartTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open part management URL
     * */

    casper.then(function () {
        this.open(urls.privatePartPermalinkExpired);
    });

    /**
     * Check we have the expired resource page
     */

    casper.then(function checkForExpiredPage() {
        this.waitForSelector('#not-found-view > h1', function () {
            this.test.assert(true, 'Expired entity page displayed');
        }, function fail() {
            this.capture('screenshot/expiredSharedPart/checkForExpiredPage-error.png');
            this.test.assert(false, 'Expired shared entity page not displayed');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
