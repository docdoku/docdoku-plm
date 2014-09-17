/*global casper,__utils__,workspaceUrl,productCreationNumber,productCreationName,partCreationNumber*/
'use strict';

casper.test.begin('Product creation tests suite',3, function productCreationTestsSuite(){

    /**
     * Open product management URL
     * */
    casper.open(productManagementUrl);

    casper.then(function(){
        this.reload();
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
            this.sendKeys('#product_creation_modal #inputProductId', productCreationNumber, {reset:true});
            this.sendKeys('#product_creation_modal #inputDescription', productCreationName, {reset:true});
            this.sendKeys('#product_creation_modal #inputPartNumber', partCreationNumber, {reset:true});
        });
    });

    casper.then(function submitProductCreationForm(){
        this.click('#product_creation_modal .btn-primary');
        this.waitForSelector('#product_table .product_id',function productHasBeenCreated(){
            this.test.assertSelectorHasText('#product_table tbody tr:first-child td.product_id',productCreationNumber);
            this.test.assertSelectorHasText('#product_table tbody tr:first-child td:nth-child(3)',partCreationNumber);
        });
    });

    casper.run(function allDone(){
        this.test.done();
    });
});