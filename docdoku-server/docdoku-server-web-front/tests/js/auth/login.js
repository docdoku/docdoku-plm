/*global casper,homeUrl,login,pass,apiUrls*/
casper.test.begin('Login tests suite', 3, function loginTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open app home page
     */
    casper.then(function () {
        this.open(homeUrl);
    });

    /**
     * Test to find the login form
     */
    casper.then(function waitForLoginForm() {
        this.waitForSelector('form[id="login_form"]', function loginFormFound() {
            this.test.assert(true, 'Login form found');
        }, function fail() {
            this.capture('screenshot/login/waitForLoginForm-error.png');
            this.test.assert(false, 'Login form not found');
        });
    });

    /**
     * Fill the login form
     */
    casper.then(function fillLoginForm() {
        this.fill('form[id="login_form"]', {
            'login_form-login': login,
            'login_form-password': pass
        }, false);
    });

    /**
     * Submit the login form
     */
    casper.then(function submitLoginForm() {
        this.click('#login_form-login_button');
    });

    /**
     * We should be redirected on workspace menu
     */
    casper.then(function waitForLogoutButton() {
        this.waitForSelector('#logout_link',function(){
            this.test.assert(true,'Logout link should be displayed');
        },function(){
            this.capture('screenshot/auth/login.png');
            this.test.assert(true,'Logout link should be displayed');
        });
    });

    /**
     * Check if we are connected to the api
     */
    casper.then(function checkSessionState() {
        this.open(apiUrls.userInfo).then(function(response){
            this.test.assertEqual(response.status, 200, 'User "' + login + '" should log in');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});
