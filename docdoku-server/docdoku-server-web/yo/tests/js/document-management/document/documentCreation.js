/*global casper,urls,workspace,documents*/

casper.test.begin('Document creation tests suite', 2, function documentCreationTestsSuite() {

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
            this.capture('screenshot/documentCreation/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Open folder creation modal
     */

    casper.then(function clickOnDocumentCreationLink() {
        this.click('.actions .new-document');
    });

    /**
     * Wait for modal
     */

    casper.then(function waitForDocumentCreationModal() {
        this.waitForSelector('.modal.document-modal.new-document', function () {
            this.click('.modal.document-modal.new-document .btn.btn-primary');
            this.test.assertExists('.modal.document-modal.new-document input.reference:invalid', 'Should not create document without a reference');
        }, function fail() {
            this.capture('screenshot/documentCreation/waitForDocumentCreationModal-error.png');
            this.test.assert(false, 'New document modal can not be found');
        });
    });

    /**
     * Fill the form and create document
     */

    casper.then(function fillAndSubmitDocumentCreationModal() {
        this.waitForSelector('.modal.document-modal.new-document input.reference', function () {
            this.sendKeys('.modal.document-modal.new-document input.reference', documents.document1.number);
            this.click('.modal.document-modal.new-document .btn.btn-primary');
        }, function fail() {
            this.capture('screenshot/documentCreation/fillAndSubmitDocumentCreationModal-error.png');
            this.test.assert(false, 'New document form can not be found');
        });
    });

    /**
     * Check if document has been created
     */

    casper.then(function checkForDocumentCreation() {
        this.waitForSelector('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference', function documentHasBeenCreated() {
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference a', documents.document1.number);
        }, function fail() {
            this.capture('screenshot/documentCreation/checkForDocumentCreation-error.png');
            this.test.assert(false, 'New document created can not be found');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
