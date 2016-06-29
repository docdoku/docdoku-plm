/*global casper,urls,workspace,documents*/

casper.test.begin('Document deletion tests suite', 1, function documentDeletionTestsSuite() {
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
            this.capture('screenshot/documentDeletion/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Wait for document to be displayed in list
     */

    casper.then(function waitForDocumentDisplayed() {
        return this.waitForSelector('#document-management-content table.dataTable tbody tr[title="'+documents.document1.number+'"] td.reference', function documentIsDisplayed() {
            this.click('#document-management-content table.dataTable tbody tr[title="'+documents.document1.number+'"] td input[type=checkbox]');
        }, function fail() {
            this.capture('screenshot/documentDeletion/waitForDocumentDisplayed-error.png');
            this.test.assert(false, 'Document to delete rows can not be found');
        });
    });

    /**
     * Wait for document suppression button
     */

    casper.then(function waitForDeleteButtonDisplayed() {
        return this.waitForSelector('.actions .delete', function deleteButtonIsDisplayed() {
            this.click('.actions .delete');
        }, function fail() {
            this.capture('screenshot/documentDeletion/waitForDeleteButtonDisplayed-error.png');
            this.test.assert(false, 'Document delete button can not be found');
        });
    });


    /**
     * Confirm document deletion
     */

    casper.then(function confirmDocumentDeletion() {
        return this.waitForSelector('.bootbox', function confirmBoxAppeared() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/documentDeletion/confirmDocumentDeletion-error.png');
            this.test.assert(false, 'Document deletion confirmation modal can not be found');
        });
    });

    /**
     * Wait for document to be removed
     */

    casper.then(function waitForDocumentDeletion() {
        return this.waitWhileSelector('#document-management-content table.dataTable tbody tr[title="'+documents.document1.number+'"]', function documentDeleted() {
            this.test.assert(true, 'Document has been deleted');
        }, function fail() {
            this.capture('screenshot/documentDeletion/waitForDocumentDeletion-error.png');
            this.test.assert(false, 'Document has not been deleted');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
