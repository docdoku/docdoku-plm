/*global casper,urls*/

casper.test.begin('Product deletion tests suite', 1, function productDeletionTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productManagement);
    });

    /**
     * Go to product nav
     */

    casper.then(function waitForProductNavLink() {
        return this.waitForSelector('#product-nav > .nav-list-entry > a', function clickProductNavLink() {
            this.click('#product-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/productDeletion/waitForProductNavLink-error.png');
            this.test.assert(false, 'Product nav link can not be found');
        });
    });

    /**
     * Test delete a product
     */

    casper.then(function waitForProductInList() {
        return this.waitForSelector('#product_table tbody tr:first-child td.product_id', function clickOnProductCheckbox() {
            this.click('#product_table tbody tr:first-child td:first-child input');
        }, function fail() {
            this.capture('screenshot/productDeletion/waitForProductInList-error.png');
            this.test.assert(false, 'Product to delete rows can not be found');
        });
    });

    casper.then(function clickOnDeleteProductButton() {
        this.click('.actions .delete');
        // Confirm deletion
        return this.waitForSelector('.bootbox', function confirmProductDeletion() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/productDeletion/waitForDeletionConfirmationModal-error.png');
            this.test.assert(false, 'Product deletion confirmation modal can not be found');
        });
    });

    casper.then(function waitForProductDisappear() {
        return this.waitWhileSelector('#product_table tbody tr:first-child td.product_id', function productHasBeenDeleted() {
            casper.test.assert(true, 'Product has been deleted');
        }, function fail() {
            this.capture('screenshot/productDeletion/waitForProductDiseapear-error.png');
            this.test.assert(false, 'Product has not been deleted');
        });
    });

    casper.run(function () {
        return this.test.done();
    });

});
