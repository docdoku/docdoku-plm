/*global casper*/
casper.test.begin('User can login',3, function(){
    casper.start(authUrl, function loginTest() {
        this.test.assertExists('form[id="login_form"]', 'Login form found');
        this.fill('form[id="login_form"]', {
            'login_form:login': login,
            'login_form:password': pass
        }, false);
        this.test.assertExists('#login_button_container input','Submit button exists');
        this.click("#login_button_container input");
    });

    casper.thenOpen(userInfoUrl, function loggedIn() {

        this.test.assertHttpStatus(200, 'Logged in');
    });

    casper.then(function cleanup() {
        this.open(deleteDocumentUrl,{method: 'DELETE'});
        this.open(deleteProductUrl,{method: 'DELETE'});
        this.open(deletePartUrl,{method: 'DELETE'});
        this.open(deleteFolderUrl,{method: 'DELETE'});
    });

    casper.run(function() {
        this.test.done();
    });
});