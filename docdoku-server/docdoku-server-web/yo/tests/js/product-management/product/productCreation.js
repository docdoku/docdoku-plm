/*global casper,urls,products*/

casper.test.begin('Product creation tests suite', 3, function productCreationTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        this.open(urls.productManagement);
    });

    /**
     * Go to product nav
     */
    casper.then(function waitForProductNavLink() {
        this.waitForSelector('#product-nav > .nav-list-entry > a', function clickProductNavLink() {
            this.click('#product-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/productCreation/waitForProductNavLink-error.png');
            this.test.assert(false, 'Product nav link can not be found');
        });
    });

    /**
     * Open the product creation modal
     */
    casper.then(function waitForProductCreationButton() {
        this.waitForSelector('.actions .new-product', function openProductCreationModal() {
            this.click('.actions .new-product');
        }, function fail() {
            this.capture('screenshot/productCreation/waitForProductCreationButton-error.png');
            this.test.assert(false, 'New product button can not be found');
        });
    });

    /**
     * Wait for the modal to be displayed & Create empty product should fail
     */
    casper.then(function waitForModalToBeDisplayed() {
        this.waitForSelector('#product_creation_modal', function createEmptyProduct() {
            this.click('#product_creation_modal .btn-primary');
            this.test.assertExists('#product_creation_modal #inputProductId:invalid', 'Should not create product without a product id');
        }, function fail() {
            this.capture('screenshot/productCreation/waitForNewProductModal-error.png');
            this.test.assert(false, 'New part modal can not be found');
        });
    });

    /**
     * Create product should
     * */
    casper.then(function fillProductCreationForm() {
        this.waitForSelector('#product_creation_modal #inputDescription', function onNewPartFormReady() {
            this.sendKeys('#product_creation_modal #inputProductId', products.product1.number, {reset: true});
            this.sendKeys('#product_creation_modal #inputDescription', products.product1.description, {reset: true});
            this.sendKeys('#product_creation_modal #inputPart', products.part1.number, {reset: true});

            this.evaluate(function (number, name) {
                document.querySelector('input#inputPartNumber').setAttribute('value', number);
                document.querySelector('input#inputPartName').setAttribute('value', name);
            }, {
                number: products.part1.number,
                name: products.part1.name
            });

        }, function fail() {
            this.capture('screenshot/productCreation/onNewProductFormReady-error.png');
            this.test.assert(false, 'New product form can not be found');
        });
    });

    casper.then(function submitProductCreationForm() {
        this.click('#product_creation_modal .btn-primary');
        this.waitForSelector('#product_table .product_id', function productHasBeenCreated() {
            this.test.assertSelectorHasText('#product_table tbody tr:first-child td.product_id', products.product1.number);
            this.test.assertSelectorHasText('#product_table tbody tr:first-child td:nth-child(4)', products.part1.number);
        }, function fail() {
            this.capture('screenshot/productCreation/waitForProductToBeCreated-error.png');
            this.test.assert(false, 'New product created can not be found');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
