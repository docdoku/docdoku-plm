/*global casper,urls*/

casper.test.begin('Product deletion tests suite',1, function productDeletionTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function(){
        this.open(urls.productManagement);
    });

    /**
     * Go to product nav
     */

    casper.then(function waitForProductNavLink(){
        this.waitForSelector('#product-nav > .nav-list-entry > a',function clickProductNavLink() {
            this.click('#product-nav > .nav-list-entry > a');
        });
    });

    /**
     * Test delete a product
     */

    casper.then(function waitForProductInList(){
        this.waitForSelector('#product_table tbody tr:first-child td.product_id', function clickOnProductCheckbox() {
            this.click('#product_table tbody tr:first-child td:first-child input');
        });
    });

    casper.then(function clickOnDeleteProductButton(){
        this.click('.actions .delete');
    });

    casper.then(function waitForProductDisappear(){
        this.waitWhileSelector('#product_table tbody tr:first-child td.product_id',function productHasBeenDeleted(){
            casper.test.assert(true, "Product has been deleted");
        });
    });

    casper.run(function() {
        this.test.done();
    });

});