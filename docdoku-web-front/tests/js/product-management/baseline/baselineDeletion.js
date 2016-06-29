/*global casper,urls*/

casper.test.begin('Baseline deletion tests suite', 2, function baselineDeletionTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productManagement);
    });

    /**
     * Go to baselines nav
     */
    casper.then(function waitForBaselineNavLink() {
        return this.waitForSelector('#baselines-nav > .nav-list-entry > a', function clickBaselineNavLink() {
            this.click('#baselines-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/baselineDeletion/waitForBaselineNavLink-error.png');
            this.test.assert(false, 'Baseline nav link can not be found');
        });
    });

    /**
     * Click the 'select all' checkbox
     */
    casper.then(function waitForSelectAllBaselinesCheckbox() {
        return this.waitForSelector('#baseline_table thead tr:first-child  th:first-child input', function clickOnSelectAllBaselinesCheckbox() {
            this.click('#baseline_table thead tr:first-child  th:first-child input');
        }, function fail() {
            this.capture('screenshot/baselineDeletion/waitForSelectAllBaselinesCheckbox-error.png');
            this.test.assert(false, 'Select all baselines checkbox can not be found');
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
            this.capture('screenshot/baselineDeletion/waitForDeleteButton-error.png');
            this.test.assert(false, 'Select all baselines checkbox can not be found');
        });
    });

    /**
     * Wait for the confirmation modal
     */
    casper.then(function waitForConfirmationModal() {
        return this.waitForSelector('.bootbox', function confirmBaselinesDeletion() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/baselineDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Baseline deletion confirmation modal can not be found');
        });
    });

    /**
     * Assert that there's no more entries in the table
     **/
    casper.then(function waitForTableToBeEmpty() {
        return this.waitWhileSelector('#baseline_table tbody tr:first-child  td:first-child input', function onBaselineTableEmpty() {
            this.test.assert(true, 'No more baselines in the list');
        }, function fail() {
            this.capture('screenshot/baselineDeletion/waitForTableToBeEmpty-error.png');
            this.test.assert(false, 'Baseline table still not empty');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
