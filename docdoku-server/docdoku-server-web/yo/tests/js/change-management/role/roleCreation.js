/*global casper,urls,workspace,roles*/

casper.test.begin('Role creation tests suite', 6, function roleCreationTestsSuite() {

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
            this.capture('screenshot/roleCreation/waitForChangeWorkflowNavLink-error.png');
            this.test.assert(false, 'Workflow nav link can not be found');
        });
    });

    /**
     * Wait for role button
     */
    casper.then(function waitForRoleButton() {
        this.waitForSelector('.actions .roles', function clickOnRolesButton() {
            this.click('.actions .roles');
            this.test.assert(true, 'Role button is displayed');
        }, function fail() {
            this.capture('screenshot/roleCreation/waitForRoleButton-error.png');
            this.test.assert(false, 'Role button can not be found');
        });
    });

    /**
     * Wait for role modal
     */
    casper.then(function waitForRoleModal() {
        this.waitForSelector('#roles-modal', function roleModalDisplayed() {
            this.test.assert(true, 'Role modal is displayed');
        }, function fail() {
            this.capture('screenshot/roleCreation/waitForRoleButton-error.png');
            this.test.assert(false, 'Role button can not be found');
        });
    });

    /**
     * Try to create a role without a name
     */
    casper.then(function tryToCreateRoleWithoutName() {
        this.click('#roles-modal #form-new-role #new-role');
        this.test.assertExists('#roles-modal #form-new-role .role-name:invalid', 'Should not create a role without a name');

    });

    /**
     * Try to add a role
     */
    casper.then(function tryToAddRole() {
        this.sendKeys('#roles-modal #form-new-role .role-name', roles.role1.name, {reset: true});
        this.click('#new-role');

        this.waitForSelector('#form-roles > div > input[value="' + roles.role1.name + '"]', function roleAdded() {
            this.test.assert(true, 'Role added');
        }, function fail() {
            this.capture('screenshot/roleCreation/tryToAddRole-error.png');
            this.test.assert(false, 'Role can not be added');
        });
    });

    /**
     * Try to create the added role
     */
    casper.then(function tryToCreateRole() {
        this.click('#save-roles');

        this.waitWhileSelector('#roles-modal', function modalClosed() {
            this.test.assert(true, 'Modal closed');
        }, function fail() {
            this.capture('screenshot/roleCreation/tryToCreateRole-error.png');
            this.test.assert(false, 'Modal not closed');
        });
    });

    /**
     * Wait to new worflow button enabling
     */
    casper.then(function waitForNewWorkflowButtonEnabling() {
        this.waitForSelector('.actions .new:enabled', function clickOnNewWorkflowButton() {
            this.test.assert(true, 'New Workflow button is enabled');
        }, function fail() {
            this.capture('screenshot/roleCreation/waitForNewButtonEnabling-error.png');
            this.test.assert(false, 'New Workflow button still disabled');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
