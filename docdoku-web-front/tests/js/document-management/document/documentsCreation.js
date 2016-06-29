/*global casper,urls,workspace,documents*/

casper.test.begin('Documents creation tests suite', 4, function documentsCreationTestsSuite() {

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
            this.capture('screenshot/documentsCreation/waitForFolderNavLink-error.png');
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
        return this.waitForSelector('.modal.document-modal.new-document', function () {
            this.click('.modal.document-modal.new-document .btn.btn-primary');
            this.test.assertExists('.modal.document-modal.new-document input.reference:invalid', 'Should not create document without a reference');
        }, function fail() {
            this.capture('screenshot/documentsCreation/waitForDocumentCreationModal-error.png');
            this.test.assert(false, 'New document modal can not be found');
        });
    });

    /**
     * Fill the form and create document
     */

    casper.then(function fillAndSubmitDocumentCreationModal() {
        return this.waitForSelector('.modal.document-modal.new-document input.reference', function () {
            this.sendKeys('.modal.document-modal.new-document input.reference', documents.document2.number);
            this.click('.modal.document-modal.new-document .btn.btn-primary');
        }, function fail() {
            this.capture('screenshot/documentsCreation/fillAndSubmitDocumentCreationModal-error.png');
            this.test.assert(false, 'New document form can not be found');
        });
    });

    /**
     * Check if document has been created
     */

    casper.then(function checkForDocumentCreation() {
        return this.waitForSelector('#document-management-content table.dataTable tr[title="'+documents.document2.number+'"] td.reference a', function documentHasBeenCreated() {
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr[title="'+documents.document2.number+'"] td.reference a', documents.document2.number);
        }, function fail() {
            this.capture('screenshot/documentsCreation/checkForDocumentCreation-error.png');
            this.test.assert(false, 'New document created can not be found');
        });
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/documentsCreation/waitForFolderNavLink-error.png');
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
        return this.waitForSelector('.modal.document-modal.new-document', function () {
            this.click('.modal.document-modal.new-document .btn.btn-primary');
            this.test.assertExists('.modal.document-modal.new-document input.reference:invalid', 'Should not create document without a reference');
        }, function fail() {
            this.capture('screenshot/documentsCreation/waitForDocumentCreationModal-error.png');
            this.test.assert(false, 'New document modal can not be found');
        });
    });

    /**
     * Fill the form and create document
     */

    casper.then(function fillAndSubmitDocumentCreationModal() {
        return this.waitForSelector('.modal.document-modal.new-document input.reference', function () {
            this.sendKeys('.modal.document-modal.new-document input.reference', documents.document3.number);
            this.click('.modal.document-modal.new-document .btn.btn-primary');
        }, function fail() {
            this.capture('screenshot/documentsCreation/fillAndSubmitDocumentCreationModal-error.png');
            this.test.assert(false, 'New document form can not be found');
        });
    });

    /**
     * Check if document has been created
     */

    casper.then(function checkForDocumentCreation() {
        return this.waitForSelector('#document-management-content table.dataTable tr[title="'+documents.document3.number+'"] td.reference a', function documentHasBeenCreated() {
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr[title="'+documents.document3.number+'"] td.reference a', documents.document3.number);
        }, function fail() {
            this.capture('screenshot/documentsCreation/checkForDocumentCreation-error.png');
            this.test.assert(false, 'New document created can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
