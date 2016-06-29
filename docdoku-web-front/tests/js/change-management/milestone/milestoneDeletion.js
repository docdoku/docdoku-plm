/*global casper,urls,workspace*/

casper.test.begin('Milestone deletion tests suite', 2, function milestoneDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        return this.open(urls.changeManagement);
    });

    /**
     * Open milestones nav
     */
    casper.then(function waitForMilestonesNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/milestones"]', function clickOnMilestoneNavLink() {
            this.click('a[href="#' + workspace + '/milestones"]');
        }, function fail() {
            this.capture('screenshot/milestoneDeletion/waitForMilestonesNavLink-error.png');
            this.test.assert(false, 'Milestone nav link can not be found');
        });
    });

    /**
     * Click the 'select all' checkbox
     */
    casper.then(function waitForSelectAllCheckbox() {
        return this.waitForSelector('#milestone_table thead tr:first-child  th:first-child input', function clickOnSelectAllCheckbox() {
            this.click('#milestone_table thead tr:first-child  th:first-child input');
        }, function fail() {
            this.capture('screenshot/milestoneDeletion/waitForSelectAllCheckbox-error.png');
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
            this.capture('screenshot/milestoneDeletion/waitForDeleteButton-error.png');
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
            this.capture('screenshot/milestoneDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Change issue deletion confirmation modal can not be found');
        });
    });

    /**
     * Assert that there's no more entries in the table
     **/
    casper.then(function waitForTableToBeEmpty() {
        return this.waitWhileSelector('#milestone_table tbody tr:first-child  td:first-child input', function onBaselineTableEmpty() {
            this.test.assert(true, 'No more issues in the list');
        }, function fail() {
            this.capture('screenshot/milestoneDeletion/waitForTableToBeEmpty-error.png');
            this.test.assert(false, 'Issue table still not empty');
        });
    });


    casper.run(function allDone() {
        return this.test.done();
    });
});
