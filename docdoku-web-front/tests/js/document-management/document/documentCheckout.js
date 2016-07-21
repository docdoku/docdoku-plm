/*global casper,urls,$*/

casper.test.begin('Document checkout tests suite', 2, function documentCheckoutTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */
    casper.then(function () {
        return this.open(urls.documentManagement);
    });

    /**
     * Open folder nav
     */
    casper.then(function waitForFolderNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/documentCheckout/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select the first document with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable tbody tr:first-child td:nth-child(2) input';
        return this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/documentCheckout/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Click on checkout button
     */
    casper.then(function waitForCheckoutButton() {
        return this.waitForSelector('.actions .checkout', function clickOnCheckoutButton() {
            this.click('.actions .checkout');
        }, function fail() {
            this.capture('screenshot/documentCheckout/waitForCheckoutButton-error.png');
            this.test.assert(false, 'Checkout button can not be found');
        });
    });

    /**
     * Wait for the checkout button to be disabled
     */
    casper.then(function waitForCheckoutButtonDisabled() {
        return this.waitForSelector('.actions .checkout:disabled', function documentIsCheckout() {
            this.test.assert(true, 'Document has been checked out');
        }, function fail() {
            this.capture('screenshot/documentCheckout/waitForCheckoutButtonDisabled-error.png');
            this.test.assert(false, 'Document has not been checked out');
        });
    });

    /**
     * Wait for the checked out number to be updated
     */
    casper.then(function waitForCheckedOutNumberUpdated() {
        return this.waitForSelector('.nav-checkedOut-number-item', function checkCheckedOutNumber() {
            this.test.assertSelectorHasText('.nav-checkedOut-number-item', '3', 'Checkout nav number updated');
        }, function fail() {
            this.capture('screenshot/documentCheckout/waitForNavUpdateCount.png');
            this.test.assert(false, 'Checkout nav number not updated');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
