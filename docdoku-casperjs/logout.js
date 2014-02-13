/*global casper*/
casper.test.begin('Docdoku can logout user',3, function(){
    casper.thenOpen(authUrl);

    casper.waitForSelector("#account_name_link", function openAccountDropdown() {
        this.test.assertExists('#account_name_link', 'Account Name found');
        this.click("#account_name_link");
    });

    casper.then(function logout() {
        this.test.assertExists('#logout_link a', 'Disconnect link found');
        this.click("#logout_link a");
    });

    casper.thenOpen(userInfoUrl, function loggedOut() {
        this.test.assert(this.currentHTTPStatus === 401, 'Logged out');
    });

    casper.run(function() {
        this.test.done();
    });
});