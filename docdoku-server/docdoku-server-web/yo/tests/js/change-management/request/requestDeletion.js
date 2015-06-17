/*global casper,urls,workspace*/

casper.test.begin('Change request deletion tests suite', 2, function changeRequestDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        this.open(urls.changeManagement);
    });

    /**
     * Open change requests nav
     */
    casper.then(function waitForChangeRequestsNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/requests"]', function clickOnChangeRequestNavLink() {
            this.click('a[href="#' + workspace + '/requests"]');
        }, function fail() {
            this.capture('screenshot/requestDeletion/waitForChangeRequestsNavLink-error.png');
            this.test.assert(false, 'Change requests nav link can not be found');
        });
    });

    /**
     * Click the 'select all' checkbox
     */
    casper.then(function waitForSelectAllCheckbox() {
        this.waitForSelector('#request_table thead tr:first-child  th:first-child input', function clickOnSelectAllCheckbox() {
            this.click('#request_table thead tr:first-child  th:first-child input');
        }, function fail() {
            this.capture('screenshot/requestDeletion/waitForSelectAllCheckbox-error.png');
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
            this.capture('screenshot/requestDeletion/waitForDeleteButton-error.png');
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
            this.capture('screenshot/requestDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Change request deletion confirmation modal can not be found');
        });
    });

    /**
     * Assert that there's no more entries in the table
     **/
    casper.then(function waitForTableToBeEmpty() {
        this.waitWhileSelector('#request_table tbody tr:first-child  td:first-child input', function onBaselineTableEmpty() {
            this.test.assert(true, 'No more requests in the list');
        }, function fail() {
            this.capture('screenshot/requestDeletion/waitForTableToBeEmpty-error.png');
            this.test.assert(false, 'Request table still not empty');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
