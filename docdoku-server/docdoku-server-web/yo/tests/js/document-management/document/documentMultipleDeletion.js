/*global casper,urls,workspace,documents*/

casper.test.begin('Document deletion tests suite',1, function documentMultipleDeletionTestsSuite(){
    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function(){
        this.open(urls.documentManagement);
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink(){
        this.waitForSelector('a[href="#'+workspace+'/folders/'+documents.folder1+'"]',function(){
            this.click('a[href="#'+workspace+'/folders/'+documents.folder1+'"]');
        },function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForFolderNavLink-error.png');
            this.test.assert(false,'Folder nav link can not be found');
        });
    });

    /**
     * Wait for document2 to be displayed in list
     */

    casper.then(function waitForDocument2Displayed(){
        this.waitForSelector('#document-management-content table.dataTable tr:first-child td.reference',function documentIsDisplayed(){
            this.click('#document-management-content table.dataTable tr:first-child td:nth-child(2) input[type=checkbox]');

        },function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForDocumentDisplayed-error.png');
            this.test.assert(false,'Document to delete rows can not be found');
        });
    });

    /**
     * Wait for document3 to be displayed in list
     */

    casper.then(function waitForDocument3Displayed(){
        this.waitForSelector('#document-management-content table.dataTable tr:nth-child(2) td.reference',function documentIsDisplayed(){
            this.click('#document-management-content table.dataTable tr:nth-child(2) td:nth-child(2) input[type=checkbox]');

        },function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForDocumentDisplayed-error.png');
            this.test.assert(false,'Document to delete rows can not be found');
        });
    });

    /**
     * Wait for document suppression button
     */

    casper.then(function waitForDeleteButtonDisplayed(){
        this.waitForSelector('.actions .delete',function deleteButtonIsDisplayed(){
            this.click('.actions .delete');

        },function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForDeleteButtonDisplayed-error.png');
            this.test.assert(false,'Document delete button can not be found');
        });
    });


    /**
     * Confirm document deletion
     */

    casper.then(function confirmDocumentsDeletion(){
        this.waitForSelector('.bootbox',function confirmBoxAppeared(){
            this.click('.bootbox .modal-footer .btn-primary');

        },function fail() {
            this.capture('screenshot/documentMultipleDeletion/confirmDocumentDeletion-error.png');
            this.test.assert(false,'Document deletion confirmation modal can not be found');
        });
    });

    /**
     * Wait for document to be removed
     */

    casper.then(function waitForDocumentsDeletion(){
        this.waitWhileSelector('#document-management-content table.dataTable tr td.reference',function documentDeleted(){
            this.test.assert(true,'Documents have been deleted');

        },function fail() {
            this.capture('screenshot/documentMultipleDeletion/waitForDocumentDeletion-error.png');
            this.test.assert(false,'Documents have not been deleted');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
