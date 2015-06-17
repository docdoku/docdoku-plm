/*global casper,urls,workspace,workflows*/

casper.test.begin('Workflow creation tests suite', 8, function workflowCreationTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        this.open(urls.changeManagement);
    });

    /**
     * Open change workflow nav
     */
    casper.then(function waitForChangeWorkflowNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/workflows"]', function clickOnChangeWorkflowNavLink() {
            this.click('a[href="#' + workspace + '/workflows"]');
        }, function fail() {
            this.capture('screenshot/workflowCreation/waitForChangeWorkflowNavLink-error.png');
            this.test.assert(false, 'Workflow nav link can not be found');
        });
    });

    /**
     * Wait for workflow button
     */
    casper.then(function waitForNewWorkflowButton() {
        this.waitForSelector('.actions .new:enabled', function clickOnNewWorkflowButton() {
            this.click('.actions .new');
            this.test.assert(true, 'New Workflow button is displayed');
        }, function fail() {
            this.capture('screenshot/workflowCreation/waitForNewWorkflowButton-error.png');
            this.test.assert(false, 'New Workflow button can not be found');
        });
    });


    /**
     * Wait for workflow editor
     */
    casper.then(function waitForWorkflowEditor() {
        this.waitForSelector('#workflow-editor', function editorDisplayed() {
            this.test.assert(true, 'Editor displayed');
        }, function fail() {
            this.capture('screenshot/workflowCreation/waitForWorkflowEditor-error.png');
            this.test.assert(false, 'Editor can not be displayed');
        });
    });

    /**
     * Try to create a workflow without a name
     */
    casper.then(function tryToCreateWorkflowWithoutName() {
        this.waitForSelector('.actions #save-workflow', function clickOnSaveWorkflowButton() {
            this.click('.actions #save-workflow');
            this.test.assertExists('.actions #workflow-name:invalid', 'Should not create a workflow without a name');
            this.sendKeys('.actions #workflow-name', workflows.workflow1.name, {reset: true});
            this.sendKeys('#workflow-editor #final-state', workflows.workflow1.finalState, {reset: true});
        }, function fail() {
            this.capture('screenshot/workflowCreation/tryToCreateWorkflowWithoutName-error.png');
            this.test.assert(false, 'Save workflow button can not be found');
        });
    });

    /**
     * Try to create an activity
     */
    casper.then(function createActivity() {
        this.waitForSelector('#workflow-editor #add-activity', function clickOnNewActivityButton() {
            this.click('#workflow-editor #add-activity');
            this.test.assertExists('#activity-list .activity-section .activity', 'Should create an activity');
            this.sendKeys('#activity-list .activity-section .activity .activity-topbar .activity-state', workflows.workflow1.activities.activity1.name, {reset: true});

        }, function fail() {
            this.capture('screenshot/workflowCreation/createActivity-error.png');
            this.test.assert(false, 'Add activity button can not be found');
        });
    });

    /**
     * Try to create a task
     */
    casper.then(function createTask() {
        this.waitForSelector('#activity-list .activity-section .activity .add-task', function clickOnNewTaskButton() {

            this.click('#activity-list .activity-section .activity .add-task');
            this.test.assertExists('#activity-list .activity-section .activity .task-list .task', 'Should create an task');
            this.click('#activity-list .activity-section .activity .task-list .task p.task-name');
            this.test.assertExists('#activity-list .activity-section .activity .task-list .task input.task-name', 'Should show the input of the task');
            this.sendKeys('#activity-list .activity-section .activity .task-list .task input.task-name', workflows.workflow1.activities.activity1.tasks.task1.name, {reset: true});
        }, function fail() {
            this.capture('screenshot/workflowCreation/createTask-error.png');
            this.test.assert(false, 'Add task button can not be found');
        });
    });

    /**
     * Save the workflow
     */
    casper.then(function saveWorkflow() {
        this.click('.actions #save-workflow');
        this.waitWhileSelector('#workflow-editor', function workflowEditorClosed() {
            this.test.assert(true, 'Workflow editor has been closed');
        }, function fail() {
            this.capture('screenshot/workflowCreation/saveWorkflow-error.png');
            this.test.assert(false, 'Workflow editor can not be closed');
        });
    });

    /**
     * Check if workflow is now in the list
     */
    casper.then(function checkForWorkflowToBeCreated() {
        this.waitForSelector('.workflow-table', function workflowTableDisplayed() {
            this.test.assertSelectorHasText('.workflow-table tbody tr:first-child td.reference', workflows.workflow1.name);
        }, function fail() {
            this.capture('screenshot/workflowCreation/checkForWorkflowToBeCreated-error.png');
            this.test.assert(false, 'Workflow not in the list');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
