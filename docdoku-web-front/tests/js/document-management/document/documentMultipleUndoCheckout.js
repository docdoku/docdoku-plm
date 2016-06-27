/*global casper,urls,workspace,documents*/

casper.test.begin('Documents multiple undocheckout tests suite', 1, function documentUndoCheckoutTestsSuite() {

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
            this.capture('screenshot/MultipleDocumentUndoCheckout/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select the first document with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable tbody tr:first-child td:nth-child(2) input';
        this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/MultipleDocumentUndoCheckout/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Select the second document with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable tbody tr:nth-child(2) td:nth-child(2) input';
        this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/MultipleDocumentUndoCheckout/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Select the 3rd document with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable tbody tr:nth-child(3) td:nth-child(2) input';
        this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/MultipleDocumentUndoCheckout/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Click on undocheckout button
     */
    casper.then(function waitForUndoCheckoutButton() {
        this.waitForSelector('.actions .undocheckout', function clickOnUndoCheckoutButton() {
            this.click('.actions .undocheckout');
        }, function fail() {
            this.capture('screenshot/MultipleDocumentUndoCheckout/waitForUndoCheckoutButton-error.png');
            this.test.assert(false, 'UndoCheckout button can not be found');
        });
    });

    /**
     * Wait for confirmation box
     */
    casper.then(function waitForConfirmationBox() {

        this.waitForSelector('div.modal-body', function fillIterationNote() {
            this.click('.modal-footer a[data-handler="1"]');
        }, function fail() {
            this.capture('screenshot/MultipleDocumentUndoCheckout/waitForIterationNotePrompt-error.png');
            this.test.assert(false, 'Iteration note modal not found');
        });
    });

    /**
     * Wait for the undocheckout button to be disabled
     */
    casper.then(function waitForUndoCheckoutButtonDisabled() {
        this.waitForSelector('.actions .undocheckout:disabled', function documentIsUndoCheckout() {
            this.test.assert(true, 'Documents have been undocheckout');
        }, function fail() {
            this.capture('screenshot/MultipleDocumentUndoCheckout/waitForUndoCheckoutButtonDisabled-error.png');
            this.test.assert(false, 'Documents have not been checkout');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
