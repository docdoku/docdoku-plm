/*global casper,urls,workspace*/

casper.test.begin('Workflow Deletion tests suite', 2, function workflowDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        return this.open(urls.changeManagement);
    });

    /**
     * Open change workflow nav
     */
    casper.then(function waitForChangeWorkflowNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/workflows"]', function clickOnChangeWorkflowNavLink() {
            this.click('a[href="#' + workspace + '/workflows"]');
        }, function fail() {
            this.capture('screenshot/workflowDeletion/waitForChangeWorkflowNavLink-error.png');
            this.test.assert(false, 'Workflow nav link can not be found');
        });
    });

    /**
     * Select the first workflow by its checkbox
     */
    casper.then(function clickOnAllWorkflowsCheckbox() {
        return this.waitForSelector('.workflow-table thead tr:first-child th:first-child input', function tableDisplayed() {
            this.click('.workflow-table thead tr:first-child th:first-child input');
        }, function fail() {
            this.capture('screenshot/workflowDeletion/clickOnAllWorkflowsCheckbox-error.png');
            this.test.assert(false, 'Workflow table can not be found');
        });
    });

    /**
     * Check if the delete button appears, and click it
     */
    casper.then(function checkForDeleteButton() {
        return this.waitForSelector('.actions .delete', function buttonDisplayed() {
            this.test.assert(true, 'Delete button displayed');
            this.click('.actions .delete');
        }, function fail() {
            this.capture('screenshot/workflowDeletion/checkForDeleteButton-error.png');
            this.test.assert(false, 'Delete workflow button can not be found');
        });
    });

    /**
     * Check if the delete button appears, and click it
     */
    casper.then(function waitForConfirmationModal() {
        return this.waitForSelector('.bootbox', function confirmWorkflowDeletion() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/workflowDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Workflow deletion confirmation modal can not be found');
        });
    });

    /**
     * Check if the workflow has disappeared
     */
    casper.then(function checkForWorkflowsDeleted() {
        return this.waitWhileSelector('.workflow-table tbody tr:first-child td:first-child input', function workflowListEmpty() {
            this.test.assert(true, 'Workflows deleted');
        }, function fail() {
            this.capture('screenshot/workflowDeletion/checkForWorkflowDeleted-error.png');
            this.test.assert(false, 'The workflow list is not empty');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
