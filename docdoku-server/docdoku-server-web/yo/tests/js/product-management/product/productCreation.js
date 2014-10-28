/*global casper,urls,products*/

casper.test.begin('Product creation tests suite',3, function productCreationTestsSuite(){

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
     * Open the product creation modal
     */
    casper.then(function waitForProductCreationButton(){
        this.waitForSelector('.actions .new-product',function openProductCreationModal() {
           this.click('.actions .new-product');
        });
    });

    /**
     * Wait for the modal to be displayed
     */
    casper.then(function waitForModalToBeDisplayed(){
        this.waitForSelector('#product_creation_modal');
    });

    /**
     * Create empty product should fail
     * */
    casper.then(function createEmptyProduct(){
        this.click('#product_creation_modal .btn-primary');
        this.test.assertExists('#product_creation_modal #inputPartNumber:invalid', 'Should not create product without a product id');
    });

    /**
     * Create product should
     * */
    casper.then(function fillProductCreationForm(){
        this.waitForSelector('#product_creation_modal #inputDescription',function onNewPartFormReady(){
            this.sendKeys('#product_creation_modal #inputProductId', products.product1.number, {reset:true});
            this.sendKeys('#product_creation_modal #inputDescription', products.product1.name, {reset:true});
            this.sendKeys('#product_creation_modal #inputPartNumber', products.part1.number, {reset:true});
        });
    });

    casper.then(function submitProductCreationForm(){
        this.click('#product_creation_modal .btn-primary');
        this.waitForSelector('#product_table .product_id',function productHasBeenCreated(){
            this.test.assertSelectorHasText('#product_table tbody tr:first-child td.product_id',products.product1.number);
            this.test.assertSelectorHasText('#product_table tbody tr:first-child td:nth-child(3)',products.part1.number);
        });
    });

    casper.run(function allDone(){
        this.test.done();
    });
});