/*global casper,__utils__,authUrl,login,pass,userInfoUrl,deleteDocumentUrl,deleteFolderUrl,deleteProductUrl,deletePartUrl,Tools*/
'use strict';
casper.test.begin('User should login',1, function LoginAndCleaningTest(){
    var exists;

    /**
     * Test to find the login form
     */
    casper.start(authUrl, function testLoginFormExisting()  {
//        Tools.assertExist(this,'form[id="login_form"]','Login form found','Login form not found');
        exists = this.evaluate(function() {
            return __utils__.exists('form[id="login_form"]');
        });
        if(!exists){
            this.test.fail('Login form not found');
            this.exit('Login form not found');
        }
        this.evaluate(function(){__utils__.log('Login form found', 'info');});

        exists = this.evaluate(function testLoginButtonExisting() {
            return __utils__.exists('#login_button_container input');
        });
        if(!exists){
            this.test.fail('Login button not found');
            this.exit('Login button not found');
        }
        this.evaluate(function(){__utils__.log('Login button found', 'info');});
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
    casper.thenClick('#login_button_container input', function tryLogin(){
        this.thenOpen(userInfoUrl, function loggedIn() {
            this.test.assertHttpStatus(200, 'User "'+login+'" should logged in');
        });
    });

    /**
     * Delete test object if there was a previous crash
     */
    casper.then(function cleanup() {
        this.open(deleteDocumentUrl,{method: 'DELETE'});
        this.evaluate(function(){__utils__.log('Test documents has been deleted', 'info');});
        this.open(deleteFolderUrl,{method: 'DELETE'});
        this.evaluate(function(){__utils__.log('Test folders has been deleted', 'info');});
        this.open(deleteProductUrl,{method: 'DELETE'});
        this.evaluate(function(){__utils__.log('Test products has been deleted', 'info');});
        this.open(deletePartUrl,{method: 'DELETE'});
        this.evaluate(function(){__utils__.log('Test parts has been deleted', 'info');});
    });

    casper.run(function() {
        this.test.done();
    });
});