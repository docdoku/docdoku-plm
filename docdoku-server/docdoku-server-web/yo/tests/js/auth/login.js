/*global casper*/

casper.test.begin('Login tests suite',2, function loginTestsSuite(){

    'use strict';

    /**
     * Open app home page
     */
    casper.then(function(){
        this.open(homeUrl);
    });

    /**
     * Test to find the login form
     */
    casper.then(function(){
        this.waitForSelector('form[id="login_form"]',function loginFormFound(){
            this.test.assert(true,'Login form found');
        });
    });

    /**
     * Fill the login form
     */
    casper.then(function fillLoginForm(){
        this.fill('form[id="login_form"]', {
            'login_form-login': login,
            'login_form-password': pass
        }, false);
    });

    /**
     * Submit the login form
     */
    casper.then(function submitLoginForm(){
        casper.click('#login_button_container input');
    });

    /**
     * Check if we are connected
     */
    casper.then(function checkSessionState(){
        this.open(userInfoUrl).then(function(response){
            this.test.assert(response.status === 200, 'User "'+login+'" should log in');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});