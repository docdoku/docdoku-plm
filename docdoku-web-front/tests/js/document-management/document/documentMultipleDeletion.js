/*global casper,urls,workspace,documents*/

casper.test.begin('Document multiple deletion tests suite', 1, function documentMultipleDeletionTestsSuite() {
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
        return this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select all documents with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable thead tr th input[type="checkbox"]';
        return this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Wait for document suppression button
     */

    casper.then(function waitForDeleteButtonDisplayed() {
        return this.waitForSelector('.actions .delete', function deleteButtonIsDisplayed() {
            this.click('.actions .delete');

        }, function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForDeleteButtonDisplayed-error.png');
            this.test.assert(false, 'Document delete button can not be found');
        });
    });


    /**
     * Confirm document deletion
     */

    casper.then(function confirmDocumentsDeletion() {
        return this.waitForSelector('.bootbox', function confirmBoxAppeared() {
            this.click('.bootbox .modal-footer .btn-primary');

        }, function fail() {
            this.capture('screenshot/documentMultipleDeletion/confirmDocumentDeletion-error.png');
            this.test.assert(false, 'Document deletion confirmation modal can not be found');
        });
    });

    /**
     * Wait for document to be removed
     */

    casper.then(function waitForDocumentsDeletion() {
        return this.waitWhileSelector('#document-management-content table.dataTable tbody tr td.reference', function documentDeleted() {
            this.test.assert(true, 'Documents have been deleted');

        }, function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForDocumentDeletion-error.png');
            this.test.assert(false, 'Documents have not been deleted');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
