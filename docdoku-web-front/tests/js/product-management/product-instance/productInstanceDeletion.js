/*global casper,urls*/

casper.test.begin('Product instance deletion tests suite', 2, function productInstanceDeletionTestsSuite() {
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
            this.capture('screenshot/productInstanceDeletion/waitForProductInstanceNavLink-error.png');
            this.test.assert(false, 'Product instance nav link can not be found');
        });
    });

    /**
     * Click the 'select all' checkbox
     */
    casper.then(function waitForSelectAllCheckbox() {
        return this.waitForSelector('#product_instances_table thead tr:first-child  th:first-child input', function clickOnSelectAllCheckbox() {
            this.click('#product_instances_table thead tr:first-child  th:first-child input');
        }, function fail() {
            this.capture('screenshot/productInstanceDeletion/waitForSelectAllCheckbox-error.png');
            this.test.assert(false, 'Select all checkbox can not be found');
        });
    });

    /**
     * Wait for the delete button to appear
     */
    casper.then(function waitForDeleteButton() {
        return this.waitForSelector('.actions .delete', function clickOnDeleteButton() {
            this.click('.actions .delete');
            this.test.assert(true, 'Delete button available');
        }, function fail() {
            this.capture('screenshot/productInstanceDeletion/waitForDeleteButton-error.png');
            this.test.assert(false, 'Select all checkbox can not be found');
        });
    });

    /**
     * Wait for the confirmation modal
     */
    casper.then(function waitForConfirmationModal() {
        return this.waitForSelector('.bootbox', function confirmDeletion() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/productInstanceDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Product instance deletion confirmation modal can not be found');
        });
    });

    /**
     * Assert that there's no more entries in the table
     **/
    casper.then(function waitForTableToBeEmpty() {
        return this.waitWhileSelector('#product_instances_table tbody tr:first-child  td:first-child input', function onBaselineTableEmpty() {
            this.test.assert(true, 'No more product instances in the list');
        }, function fail() {
            this.capture('screenshot/productInstanceDeletion/waitForTableToBeEmpty-error.png');
            this.test.assert(false, 'Product instance table still not empty');
        });
    });


    casper.run(function allDone() {
        return this.test.done();
    });
});
