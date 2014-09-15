/*global casper*/

'use strict';

casper.test.begin('Document template deletion tests suite',1, function documentTemplateDeletionTestsSuite(){

    /**
     * Open document management URL
     * */
    casper.open(documentManagementUrl);

    /**
     * Open template nav
     */
    casper.then(function waitForTemplateNavLink(){
        this.waitForSelector('#template-nav > .nav-list-entry > a',function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        });
    });

    /**
     * Wait for template to be displayed in list
     */
    casper.then(function waitForTemplateDisplayed(){
        this.waitForSelector('#document-management-content table.dataTable tr td.reference',function templateIsDisplayed(){
            this.click('#document-management-content table.dataTable tr td:first-child input[type=checkbox]');
        });
    });

    /**
     * Wait for template suppression button
     */
    casper.then(function waitForDeleteButtonDisplayed(){
        this.waitForSelector('.actions .delete',function deleteButtonIsDisplayed(){
            this.click('.actions .delete');
        });
    });

    /**
     * Wait for template to be removed
     */
    casper.then(function waitForTemplateDeletion(){
        this.waitWhileSelector('#document-management-content table.dataTable tr td.reference',function templateDeleted(){
            this.test.assert(true,'Template deleted');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});