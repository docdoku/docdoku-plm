/*global casper,authUrl,__utils__,userInfoUrl*/
'use strict';
casper.test.begin('User can logout',1, function(){
    casper.thenOpen(authUrl);
    var exists;

    /**
     *  Go to account dropdown
     */
    casper.waitForSelector("#account_name_node", function openAccountDropdown() {
        exists = this.evaluate(function() {
            return __utils__.exists('#account_name_link');
        });
        if(!exists){
            this.test.fail('Account dropdown menu not found');
            this.exit('Account dropdown menu not found');
        }
        this.evaluate(function(){__utils__.log('Account dropdown menu found', 'info');});
        this.click("#account_name_link");
    });

    /**
     * Test Logout
     */
    casper.then(function logout() {
        exists = this.evaluate(function() {
            return __utils__.exists('#logout_link a');
        });
        if(!exists){
            this.test.fail('Disconnect link not found');
            this.exit('Disconnect link not found');
        }
        this.evaluate(function(){__utils__.log('Disconnect link found', 'info');});
        this.click("#logout_link a");

        this.wait(1000, function(){
            this.thenOpen(userInfoUrl, function loggedIn() {
                this.test.assertHttpStatus(401,'Should logged out');
            });
        });
    });

    casper.run(function() {
        this.test.done();
    });
});