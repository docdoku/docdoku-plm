casper.start(authUrl, function loginTest(response) {
    this.test.assertExists('form[id="login_form"]', 'Login form found');
    this.fill('form[id="login_form"]', {
        'login_form:login': login,
        'login_form:password': pass
    }, false);
    this.test.assertExists('#login_button_container input','Submit button exists');
    this.click("#login_button_container input");
});

casper.thenOpen(userInfoUrl, function loggedIn() {
    this.test.assert(this.currentHTTPStatus === 200, 'Logged in');
});

casper.run(function() {
    this.test.done(3);
});