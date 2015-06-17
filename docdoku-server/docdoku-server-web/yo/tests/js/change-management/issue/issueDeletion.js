/*global casper,urls,workspace*/

casper.test.begin('Change issue deletion tests suite', 2, function changeIssueDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        this.open(urls.changeManagement);
    });

    /**
     * Open change issues nav
     */
    casper.then(function waitForChangeIssuesNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/issues"]', function clickOnChangeIssueNavLink() {
            this.click('a[href="#' + workspace + '/issues"]');
        }, function fail() {
            this.capture('screenshot/issueDeletion/waitForChangeIssuesNavLink-error.png');
            this.test.assert(false, 'Change issue nav link can not be found');
        });
    });

    /**
     * Click the 'select all' checkbox
     */
    casper.then(function waitForSelectAllCheckbox() {
        this.waitForSelector('#issue_table thead tr:first-child  th:first-child input', function clickOnSelectAllCheckbox() {
            this.click('#issue_table thead tr:first-child  th:first-child input');
        }, function fail() {
            this.capture('screenshot/issueDeletion/waitForSelectAllCheckbox-error.png');
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
            this.capture('screenshot/issueDeletion/waitForDeleteButton-error.png');
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
            this.capture('screenshot/issueDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Change issue deletion confirmation modal can not be found');
        });
    });

    /**
     * Assert that there's no more entries in the table
     **/
    casper.then(function waitForTableToBeEmpty() {
        this.waitWhileSelector('#issue_table tbody tr:first-child  td:first-child input', function onBaselineTableEmpty() {
            this.test.assert(true, 'No more issues in the list');
        }, function fail() {
            this.capture('screenshot/issueDeletion/waitForTableToBeEmpty-error.png');
            this.test.assert(false, 'Issue table still not empty');
        });
    });


    casper.run(function allDone() {
        this.test.done();
    });
});
