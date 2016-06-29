/*global casper,urls*/
casper.test.begin('Logout tests suite', 1, function logoutTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        return  this.open(urls.documentManagement);
    });

    /**
     *  Wait for disconnect link, and click it
     */
    casper.then(function () {
        return  this.waitForSelector('#logout_link a', function onLogoutLinkReady() {
            this.click('#logout_link a');
        });
    });

    /**
     * Test to find the login form
     */
    casper.then(function checkForLoginForm() {
        return  this.waitForSelector('form[id="login_form"]', function loginFormFound() {
            this.test.assert(true, 'Login form found');
        });
    });

    /**
     * Test session state

     casper.then(function checkResource(){
        console.log(apiUrls.userInfo)
        this.open(apiUrls.userInfo, {method: 'GET'}).then(function (response) {
            console.log(this.getPageContent())
            this.test.assert(response.status == 401,'We should get a 401 HTTP code');
        });
    });
     */
    casper.run(function () {
        return this.test.done();
    });
});
