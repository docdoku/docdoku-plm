/*global casper*/

casper.test.begin('Workflow Deletion tests suite',2, function workflowDeletionTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function(){
        this.open(urls.changeManagement);
    });

    /**
     * Open change workflow nav
     */
    casper.then(function waitForChangeWorkflowNavLink(){
        this.waitForSelector('a[href="#'+workspace+'/workflows"]',function clickOnChangeWorkflowNavLink(){
            this.click('a[href="#'+workspace+'/workflows"]');
        },function fail() {
            this.capture('screenshot/workflowDeletion/waitForChangeWorkflowNavLink-error.png');
            this.test.assert(false,'Workflow nav link can not be found');
        });
    });

    /**
     * Select the first workflow by its checkbox
     */
    casper.then(function clickOnWorkflowCheckbox(){
        this.waitForSelector('.workflow-table',function tableDisplayed(){
            this.click('.workflow-table tbody tr:first-child td:first-child input');
        },function fail() {
            this.capture('screenshot/workflowDeletion/clickOnWorkflowCheckbox-error.png');
            this.test.assert(false,'Workflow table can not be found');
        });
    });

    /**
     * Check if the delete button appears, and click it
     */
    casper.then(function checkForDeleteButton(){
        this.waitForSelector('.actions .delete',function buttonDisplayed(){
            this.click('.actions .delete');
            this.test.assert(true,'Delete button displayed');
        },function fail() {
            this.capture('screenshot/workflowDeletion/checkForDeleteButton-error.png');
            this.test.assert(false,'Delete workflow button can not be found');
        });
    });

    /**
     * Check if the workflow has disappeared
     */
    casper.then(function checkForWorkflowDeleted(){
        this.waitWhileSelector('.workflow-table tbody tr:first-child td:first-child input',function workflowListEmpty(){
            this.test.assert(true,'Workflow deleted');
        },function fail() {
            this.capture('screenshot/workflowDeletion/checkForWorkflowDeleted-error.png');
            this.test.assert(false,'The workflow list is not empty');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
