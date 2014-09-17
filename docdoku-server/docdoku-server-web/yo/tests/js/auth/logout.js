/*global casper*/
casper.test.begin('Logout tests suite',1, function logoutTestsSuite(){

    'use strict';

    casper.open(documentManagementUrl);

    /**
     *  Wait for disconnect link, and click it
     */
    casper.waitForSelector("#logout_link", function onLogoutLinkReady() {
        this.click('#logout_link a');
    });

    /**
     * Test session state
     */
    casper.then(function waitForDisconnection(){
        this.wait(500);
    });

    casper.thenOpen(userInfoUrl, function testSessionState(){
        this.test.assertHttpStatus(401,'We should get a 401 HTTP code');
    });

    casper.run(function() {
        this.test.done();
    });
});