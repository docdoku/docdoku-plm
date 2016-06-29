/*global casper,urls,workspace,workflows*/

casper.test.begin('Workflow duplication tests suite', 6, function workflowDuplicationTestsSuite() {

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
        this.waitForSelector('a[href="#' + workspace + '/workflows"]', function clickOnChangeWorkflowNavLink() {
            this.click('a[href="#' + workspace + '/workflows"]');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/waitForChangeWorkflowNavLink-error.png');
            this.test.assert(false, 'Workflow nav link can not be found');
        });
    });


    /**
     * Open 1st workflow
     */

    casper.then(function checkWorkflowTable() {
        this.waitForSelector('.workflow-table tbody tr:first-child td.reference', function workflowTableDisplayed() {
            this.click('.workflow-table tbody tr:first-child td.reference');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/checkWorkflowTable-error.png');
            this.test.assert(false, 'Workflow table not displayed');
        });
    });

    /**
     * Wait for workflow editor
     * */

    casper.then(function waitForWorkflowEditor() {
        this.waitForSelector('#workflow-editor', function workflowEditorDisplayed() {
            this.test.assert(true, 'Workflow editor opened');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/waitForWorkflowEditor-error.png');
            this.test.assert(false, 'Workflow editor not displayed');
        });
    });

    /**
     * Wait for duplicate button
     * */

    casper.then(function waitForDuplicateButton() {
        this.waitForSelector('#copy-workflow', function duplicateButtonDisplayed() {
            this.click('#copy-workflow');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/waitForDuplicateButton-error.png');
            this.test.assert(false, 'Workflow duplicate button not displayed');
        });
    });

    /**
     * Wait for the duplication modal
     * */
    casper.then(function waitForDuplicationModal() {
        this.waitForSelector('#modal-copy-workflow.in', function duplicationModalDisplayed() {
            this.test.assert(true, 'Workflow duplication modal displayed');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/waitForDuplicationModal-error.png');
            this.test.assert(false, 'Workflow duplicate modal not displayed');
        });
    });

    /**
     * Wait for the text input to be automatically filled
     * */
    casper.then(function waitForWorkflowNameToBeFilled() {
        this.waitWhileSelector('#workflow-copy-name:invalid', function workflowNameFilled() {
            this.test.assertSelectorExist('#workflow-copy-name[value="' + workflows.workflow1.name + '"]', 'Input should contain ' + workflows.workflow1.name);
            this.sendKeys('#workflow-copy-name', workflows.workflow2.name, {reset: true});
            this.click('#save-copy-workflow-btn');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/waitForWorkflowNameToBeFilled-error.png');
            this.test.assert(false, 'Workflow name not automatically filled');
        });
    });

    /**
     * Wait for the duplication modal to disappear
     * */

    casper.then(function waitForDuplicationModalToDisappear() {
        this.waitWhileSelector('#modal-copy-workflow', function duplicationModalHidden() {
            this.test.assert(true, 'Duplication modal disappeared');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/waitForDuplicationModalToDisappear-error.png');
            this.test.assert(false, 'Workflow duplicate modal did not disappear');
        });
    });

    /**
     * Wait for the workflow editor to disappear
     * */

    casper.then(function waitForWorkflowEditorToDisappear() {
        this.waitWhileSelector('#workflow-editor', function workflowEditorHidden() {
            this.test.assert(true, 'Workflow editor disappeared');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/waitForWorkflowEditorToDisappear-error.png');
            this.test.assert(false, 'Workflow editor did not disappear');
        });
    });

    /**
     * Check that we have two lines in the workflow table
     * */

    casper.then(function checkForNewWorkflowCreated() {
        this.waitForSelector('.workflow-table tbody tr', function workflowTableDisplayed() {
            this.test.assertElementCount('.workflow-table tbody tr', 2, 'Should have 2 rows in workflow table');
        }, function fail() {
            this.capture('screenshot/workflowDuplication/checkForNewWorkflowCreated-error.png');
            this.test.assert(false, 'Workflow table not displayed');
        });
    });


    casper.run(function allDone() {
        this.test.done();
    });
});
