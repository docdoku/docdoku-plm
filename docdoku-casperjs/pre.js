/*global casper,__utils__,authUrl,login,pass,userInfoUrl,deleteDocumentUrl,deleteFolderUrl,deleteProductUrl,deletePartUrl,Tools*/
'use strict';
var _ = require('underscore');

casper.start();

casper.test.begin('User should login',2, function LoginAndCleaningTest(){

    /**
    * Register to application logs
    * */
    casper.on('remote.message', function remoteMessage(message) {
        this.log(message,'info');
    });

    /**
     * Open app home page
     */
    casper.then(function(){
        this.open(authUrl);
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
            this.test.assert(response.status === 200, 'User "'+login+'" should logged in');
        });
    });


    /**
     * Delete test objects
     */
    casper.then(function cleanupDocuments() {

        this.open(deleteDocumentUrl,{method: 'DELETE'}).then(function(response){
           if(response.status === 200){
               this.log('Test document has been deleted','info');
           }else{
               var reason = _(response.headers).findWhere({name:'Reason-Phrase'});
               this.log('Cannot delete test document, reason : ' + reason.value,'warning');
           }
        });
    });

    casper.then(function cleanupFolders() {

        this.open(deleteFolderUrl,{method: 'DELETE'}).then(function(response){
            if(response.status === 200){
                this.log('Test folders has been deleted','info');
            }else{
                var reason = _(response.headers).findWhere({name:'Reason-Phrase'});
                this.log('Cannot delete test folders, reason : ' + reason.value,'warning');
            }
        });
    });

    casper.then(function cleanupProducts() {

        this.open(deleteProductUrl,{method: 'DELETE'}).then(function(response){
            if(response.status === 200){
                this.log('Test products has been deleted','info');
            }else{
                var reason = _(response.headers).findWhere({name:'Reason-Phrase'});
                this.log('Cannot delete test products, reason : ' + reason.value,'warning');
            }
        });
    });

    casper.then(function cleanupParts() {

        this.open(deletePartUrl,{method: 'DELETE'}).then(function(response){
            if(response.status === 200){
                this.log('Test parts has been deleted','info');
            }else{
                var reason = _(response.headers).findWhere({name:'Reason-Phrase'});
                this.log('Cannot delete test parts, reason : ' + reason.value,'warning');
            }
        });

    });

    casper.run(function allDone() {
        this.test.done();
    });

});