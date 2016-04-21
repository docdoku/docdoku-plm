/*global casper,urls,workspace,documents*/

casper.test.begin('Documents multiple checkout tests suite', 2, function documentMultipleCheckoutTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        this.open(urls.documentManagement);
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckout/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select all documents with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable thead tr th input[type="checkbox"]';
        this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckin/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Click on checkout button
     */
    casper.then(function waitForCheckoutButton() {
        this.waitForSelector('.actions .checkout', function clickOnCheckoutButton() {
            this.click('.actions .checkout');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckout/waitForCheckoutButton-error.png');
            this.test.assert(false, 'Checkout button can not be found');
        });
    });


    /**
     * Wait for the checkout button to be disabled
     */
    casper.then(function waitForCheckoutButtonDisabled() {
        this.waitForSelector('.actions .checkout:disabled', function documentIsCheckout() {
            this.test.assert(true, 'Documents have been checkout');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckout/waitForCheckoutButtonDisabled-error.png');
            this.test.assert(false, 'Documents have not been checkout');
        });
    });

    /**
     * Wait for all button to be checkout
     */
    casper.then(function waitForDisplayCheckin() {
        this.waitWhileSelector('tbody > tr > td.reference.doc-ref > a > i.fa.fa-eye', function () {
            this.test.assert(true, 'Document retrieved');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckout/waitForCheckinDocuments-error.png');
            this.test.assert(false, 'Document has not been checkin');
        });
    });


    casper.run(function allDone() {
        this.test.done();
    });
});
