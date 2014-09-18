/*global casper*/

casper.test.begin('Document creation tests suite',2, function documentCreationTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function(){
        this.open(documentManagementUrl);
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink(){
        this.waitForSelector('a[href="#'+workspace+'/folders/'+folderCreationName+'"]',function(){
            this.click('a[href="#'+workspace+'/folders/'+folderCreationName+'"]');
        });
    });

    /**
     * Open folder creation modal
     */

    casper.then(function clickOnDocumentCreationLink(){
        this.click('.actions .new-document');
    });

    /**
     * Wait for modal
     */

    casper.then(function waitForDocumentCreationModal(){
        this.waitForSelector('.modal.document-modal.new-document',function(){
            this.click('.modal.document-modal.new-document .btn.btn-primary');
            this.test.assertExists('.modal.document-modal.new-document input.reference:invalid', 'Should not create document without a reference');
        });
    });

    /**
     * Fill the form and create document
     */

    casper.then(function fillAndSubmitDocumentCreationModal(){
        this.waitForSelector('.modal.document-modal.new-document input.reference',function(){
            this.sendKeys('.modal.document-modal.new-document input.reference',documentCreationNumber);
            this.click('.modal.document-modal.new-document .btn.btn-primary');
        });
    });

    /**
     * Check if document has been created
     */

    casper.then(function checkForDocumentCreation(){
        this.waitForSelector('#document-management-content table.dataTable tr td.reference',function documentHasBeenCreated(){
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr td.reference a',documentCreationNumber);
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});