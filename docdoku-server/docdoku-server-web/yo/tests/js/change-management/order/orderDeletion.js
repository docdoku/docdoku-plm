/*global casper,urls,workspace*/

casper.test.begin('Change order deletion tests suite', 2, function changeOrderDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        this.open(urls.changeManagement);
    });

    /**
     * Open change orders nav
     */
    casper.then(function waitForChangeOrdersNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/orders"]', function clickOnChangeOrderNavLink() {
            this.click('a[href="#' + workspace + '/orders"]');
        }, function fail() {
            this.capture('screenshot/orderDeletion/waitForChangeOrdersNavLink-error.png');
            this.test.assert(false, 'Change order nav link can not be found');
        });
    });

    /**
     * Click the 'select all' checkbox
     */
    casper.then(function waitForSelectAllCheckbox() {
        this.waitForSelector('#order_table thead tr:first-child  th:first-child input', function clickOnSelectAllCheckbox() {
            this.click('#order_table thead tr:first-child  th:first-child input');
        }, function fail() {
            this.capture('screenshot/orderDeletion/waitForSelectAllCheckbox-error.png');
            this.test.assert(false, 'Select all checkbox can not be found');
        });
    });

    /**
     * Wait for the delete button to appear
     */
    casper.then(function waitForDeleteButton() {
        this.waitForSelector('.actions .delete', function clickOnDeleteButton() {
            this.click('.actions .delete');
            this.test.assert(true, 'Delete button available');
        }, function fail() {
            this.capture('screenshot/orderDeletion/waitForDeleteButton-error.png');
            this.test.assert(false, 'Select all checkbox can not be found');
        });
    });

    /**
     * Wait for the confirmation modal
     */
    casper.then(function waitForConfirmationModal() {
        this.waitForSelector('.bootbox', function confirmDeletion() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/orderDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Change order deletion confirmation modal can not be found');
        });
    });

    /**
     * Assert that there's no more entries in the table
     **/
    casper.then(function waitForTableToBeEmpty() {
        this.waitWhileSelector('#order_table tbody tr:first-child  td:first-child input', function onBaselineTableEmpty() {
            this.test.assert(true, 'No more orders in the list');
        }, function fail() {
            this.capture('screenshot/orderDeletion/waitForTableToBeEmpty-error.png');
            this.test.assert(false, 'Order table still not empty');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
