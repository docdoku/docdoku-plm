/*global casper,__utils__,workspaceUrl,folderCreationName*/
'use strict';
casper.test.begin('Document deletion tests suite',1, function documentDeletionTestsSuite(){

    /**
     * Open document management URL
     * */
    casper.open(documentManagementUrl);

    casper.then(function(){
        this.reload();
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
     * Wait for document to be displayed in list
     */
    casper.then(function waitForDocumentDisplayed(){
        this.waitForSelector('#document-management-content table.dataTable tr td.reference',function documentIsDisplayed(){
            this.click('#document-management-content table.dataTable tr td:nth-child(2) input[type=checkbox]');
        });
    });

    /**
     * Wait for document suppression button
     */
    casper.then(function waitForDeleteButtonDisplayed(){
        this.waitForSelector('.actions .delete',function deleteButtonIsDisplayed(){
            this.click('.actions .delete');
        });
    });

    /**
     * Wait for document to be removed
     */
    casper.then(function waitForDocumentDeletion(){
        this.waitWhileSelector('#document-management-content table.dataTable tr td.reference',function documentDeleted(){
            this.test.assert(true,'Document deleted');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});