/*global casper*/
casper.test.begin('User can logout',3, function(){
    casper.thenOpen(authUrl);

    casper.waitForSelector("#account_name_link", function openAccountDropdown() {
        this.test.assertExists('#account_name_link', 'Account Name found');
        this.click("#account_name_link");
    });

    casper.then(function logout() {
        this.test.assertExists('#logout_link a', 'Disconnect link found');
    });

    casper.thenClick("#logout_link a", function(){
        this.thenOpen(userInfoUrl, function loggedIn() {
            this.test.assertHttpStatus(401,'Logged out');
        });
    });

    casper.run(function() {
        this.test.done();
    });
});