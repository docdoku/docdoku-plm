/*global casper,urls,productInstances*/

casper.test.begin('Product instance creation tests suite', 5, function productInstanceCreationTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productManagement);
    });

    /**
     * Go to product instances nav
     */
    casper.then(function waitForProductInstanceNavLink() {
        return this.waitForSelector('#product-instances-nav > .nav-list-entry > a', function clickProductInstanceNavLink() {
            this.click('#product-instances-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/productInstanceCreation/waitForProductInstanceNavLink-error.png');
            this.test.assert(false, 'Product instance nav link can not be found');
        });
    });

    /**
     * Find the new product instance button
     */
    casper.then(function waitForNewProductInstanceButton() {
        return this.waitForSelector('.actions .new-product-instance', function clickOnNewProductInstanceButton() {
            this.click('.actions .new-product-instance');
        }, function fail() {
            this.capture('screenshot/productInstanceCreation/waitForNewProductInstanceButton-error.png');
            this.test.assert(false, 'New product instance button can not be found');
        });
    });

    /**
     * Wait for the modal to be opened
     */
    casper.then(function waitForNewProductInstanceModal() {
        return this.waitForSelector('#product_instance_creation_modal', function modalOpened() {
            this.test.assert(true, 'Product instance creation modal opened');
        }, function fail() {
            this.capture('screenshot/productInstanceCreation/waitForNewProductInstanceModal-error.png');
            this.test.assert(false, 'Product instance creation modal can not be found');
        });
    });

     /**
     * Wait for the selects to be loaded
     */
    casper.then(function waitForData() {
        return this.waitForSelector('#product_instance_creation_modal #inputBaseline > option', function dataReady() {
            this.test.assert(true, 'Product instance ready to create');
        }, function fail() {
            this.capture('screenshot/productInstanceCreation/waitForData-error.png');
            this.test.assert(false, 'No data available to create the product instance');
        });
    });

    /**
     * Try to create the product instance without a serial number
     */
    casper.then(function tryToCreateProductInstanceWithoutSerialNumber() {
        this.click('#product_instance_creation_modal .modal-footer .btn.btn-primary');
        this.test.assertExists('#product_instance_creation_modal #inputSerialNumber:invalid', 'Should not create a product instance without the name');
    });

    /**
     * Try to create the product instance
     */
    casper.then(function tryToCreateProductInstance() {
        return this.waitForSelector('#product_instance_creation_modal', function fillForm() {
            this.sendKeys('#product_instance_creation_modal #inputSerialNumber', productInstances.productInstance1.serialNumber, {reset: true});
            this.click('#product_instance_creation_modal .modal-footer .btn.btn-primary');
        }, function fail() {
            this.capture('screenshot/productInstanceCreation/tryToCreateProductInstance-error.png');
            this.test.assert(false, 'Product instance modal can not be found');
        });
    });

    /**
     * Wait for the modal to be closed
     */
    casper.then(function waitForProductInstanceModalToBeClosed() {
        return this.waitWhileSelector('#product_instance_creation_modal', function onModalClosed() {
            this.test.assert(true, 'Product instance creation modal closed');
        }, function fail() {
            this.capture('screenshot/productInstanceCreation/waitForProductInstanceModalToBeClosed-error.png');
            this.test.assert(false, 'Product instance creation modal not closed');
        });
    });

    /**
     * Wait for the line in the table
     */
    casper.then(function waitForProductInstanceToBeCreated() {
        return this.waitForSelector('#product_instances_table > tbody > tr > td.reference', function onProductInstanceCreated() {
            this.test.assert(true, 'Product instance created');
        }, function fail() {
            this.capture('screenshot/productInstanceCreation/waitForProductInstanceToBeCreated-error.png');
            this.test.assert(false, 'Product instance not in the list');
        });
    });


    casper.run(function allDone() {
        return this.test.done();
    });
});
