/*global casper,urls,workspace,documents*/

casper.test.begin('Documents multiple checkout tests suite', 1, function documentMultipleCheckoutTestsSuite(){

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
            this.capture('screenshot/MultipleDocumentCheckout/waitForFolderNavLink-error.png');
            this.test.assert(false,'Folder nav link can not be found');
        });
    });

    /**
     * Select the first document with checkbox
     */
    casper.then(function waitForDocumentTable(){
        var checkbox = '#document-management-content table.dataTable tbody tr:first-child td:nth-child(2) input';
        this.waitForSelector(checkbox,function clickOnDocumentCheckbox() {
            this.click(checkbox);
        },function fail(){
            this.capture('screenshot/MultipleDocumentCheckout/waitForDocumentTable-error.png');
            this.test.assert(false,'Document can not be found');
        });
    });

    /**
     * Select the second document with checkbox
     */
    casper.then(function waitForDocumentTable(){
        var checkbox = '#document-management-content table.dataTable tbody tr:nth-child(2) td:nth-child(2) input';
        this.waitForSelector(checkbox,function clickOnDocumentCheckbox() {
            this.click(checkbox);
        },function fail(){
            this.capture('screenshot/MultipleDocumentCheckout/waitForDocumentTable-error.png');
            this.test.assert(false,'Document can not be found');
        });
    });

    /**
     * Select the 3rd document with checkbox
     */
    casper.then(function waitForDocumentTable(){
        var checkbox = '#document-management-content table.dataTable tbody tr:nth-child(3) td:nth-child(2) input';
        this.waitForSelector(checkbox,function clickOnDocumentCheckbox() {
            this.click(checkbox);
        },function fail(){
            this.capture('screenshot/MultipleDocumentCheckout/waitForDocumentTable-error.png');
            this.test.assert(false,'Document can not be found');
        });
    });

    /**
     * Click on checkout button
     */
    casper.then(function waitForCheckoutButton(){
        this.waitForSelector('.actions .checkout',function clickOnCheckoutButton() {
            this.click('.actions .checkout');
        },function fail() {
            this.capture('screenshot/MultipleDocumentCheckout/waitForCheckoutButton-error.png');
            this.test.assert(false,'Checkout button can not be found');
        });
    });



    /**
     * Wait for the checkout button to be disabled
     */
    casper.then(function waitForCheckoutButtonDisabled(){
        this.waitForSelector('.actions .checkout:disabled',function documentIsCheckout() {
            this.test.assert(true,'Documents have been checkout');
        },function fail() {
            this.capture('screenshot/MultipleDocumentCheckout/waitForCheckoutButtonDisabled-error.png');
            this.test.assert(false,'Documents have not been checkout');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
