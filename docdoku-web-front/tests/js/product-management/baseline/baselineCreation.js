/*global casper,urls,baselines*/

casper.test.begin('Baseline creation tests suite', 2, function baselineCreationTestsSuite() {
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
            this.capture('screenshot/baselineCreation/waitForProductNavLink-error.png');
            this.test.assert(false, 'Product nav link can not be found');
        });
    });

    /**
     * Select the first product with checkbox
     */
    casper.then(function waitForProductTable() {
        return this.waitForSelector('#product_table tbody tr:first-child  td:first-child input', function clickOnProductCheckbox() {
            this.click('#product_table tbody tr:first-child  td:first-child input');
        }, function fail() {
            this.capture('screenshot/baselineCreation/waitForProductTable-error.png');
            this.test.assert(false, 'Product can not be found');
        });
    });

    /**
     * Click on baseline creation button
     */
    casper.then(function waitForBaselineCreationButton() {
        return this.waitForSelector('.actions .new-baseline', function openBaselineCreationModal() {
            this.click('.actions .new-baseline');
        }, function fail() {
            this.capture('screenshot/baselineCreation/waitForBaselineCreationButton-error.png');
            this.test.assert(false, 'New baseline button can not be found');
        });
    });

    /**
     * Try to create a baseline without a name
     */
    casper.then(function waitForBaselineCreationModal() {
        return this.waitForSelector('#baseline_creation_modal .modal-footer .btn-primary', function baselineCreationModalOpened() {
            this.click('#baseline_creation_modal .modal-footer .btn-primary');
            this.test.assertExists('#baseline_creation_modal #inputBaselineName:invalid', 'Should not create baseline without a name');
        }, function fail() {
            this.capture('screenshot/baselineCreation/waitForBaselineCreationModal-error.png');
            this.test.assert(false, 'New baseline modal can not be found');
        });
    });

    /**
     * Try to create the baseline
     */
    casper.then(function tryToCreateABaseline() {
        return this.waitForSelector('#baseline_creation_modal #inputBaselineName', function fillBaselineCreationForm() {
            this.sendKeys('#baseline_creation_modal #inputBaselineName', baselines.baseline1.name, {reset: true});
            this.sendKeys('#baseline_creation_modal #inputBaselineDescription', baselines.baseline1.description, {reset: true});
            this.click('#baseline_creation_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/baselineCreation/tryToCreateABaseline-error.png');
            this.test.assert(false, 'Cannot create the baseline');
        });
    });

    /**
     * Wait for the modal to be closed
     */
    casper.then(function waitForModalToBeClosed() {
        return this.waitWhileSelector('#baseline_creation_modal', function onBaselineCreationModalClosed() {
            this.test.assert(true, 'Baseline creation modal has been closed');
        }, function fail() {
            this.capture('screenshot/baselineCreation/waitForModalToBeClosed-error.png');
            this.test.assert(false, 'Baseline creation modal can not close');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
