/*global casper,urls,workspace,changeItems*/

casper.test.begin('Milestone creation tests suite', 5, function milestoneCreationTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        this.open(urls.changeManagement);
    });

    /**
     * Open milestones nav
     */
    casper.then(function waitForMilestonesNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/milestones"]', function clickOnMilestoneNavLink() {
            this.click('a[href="#' + workspace + '/milestones"]');
        }, function fail() {
            this.capture('screenshot/milestoneCreation/waitForMilestonesNavLink-error.png');
            this.test.assert(false, 'Milestone nav link can not be found');
        });
    });

    /**
     * Open milestone creation modal
     */
    casper.then(function openNewMilestoneModal() {
        this.waitForSelector('.actions .new-milestone',
            function clickOnMilestoneCreationLink() {
                this.click('.actions .new-milestone');
            }, function fail() {
                this.capture('screenshot/milestonCreation/openNewMilestoneModal-error.png');
                this.test.assert(false, 'New mileston button can not be found');
            }
        );
    });

    /**
     * Try to create an milestone without a title
     */
    casper.then(function waitForMilestoneCreationModal() {
        this.waitForSelector('#milestone_creation_modal.ready', function createMilestoneWithoutName() {
            this.click('#milestone_creation_modal .modal-footer .btn-primary');
            this.test.assertExists('#milestone_creation_modal #inputMilestoneTitle:invalid', 'Should not create a milestone without a name');
        }, function fail() {
            this.capture('screenshot/milestoneCreation/waitForMilestoneCreationModal-error.png');
            this.test.assert(false, 'Milestone modal can not be found');
        });
    });

    /**
     * Try to create an milestone without a date
     */

    casper.then(function waitForMilestoneCreationModal() {
        this.waitForSelector('#milestone_creation_modal .modal-footer .btn-primary', function createMilestoneWithoutDate() {
            this.sendKeys('#milestone_creation_modal input#inputMilestoneTitle', changeItems.milestone1.title, {reset: true});
            this.click('#milestone_creation_modal .modal-footer .btn-primary');
            this.test.assertExists('#milestone_creation_modal #inputMilestoneDueDate:invalid', 'Should not create a milestone without a date');
        }, function fail() {
            this.capture('screenshot/milestoneCreation/waitForMilestoneCreationModal-error.png');
            this.test.assert(false, 'Milestone modal can not be found');
        });
    });

    /**
     * Fill the form and create the milestone
     */
    casper.then(function fillAndSubmitMilestoneCreationModal() {
        this.waitForSelector('#milestone_creation_modal.ready', function fillForm() {
            this.sendKeys('#milestone_creation_modal input#inputMilestoneTitle', changeItems.milestone1.title, {reset: true});
            this.sendKeys('#milestone_creation_modal input#inputMilestoneDueDate', changeItems.milestone1.date, {reset: true});
            this.waitWhileSelector('#milestone_creation_modal input:invalid', function submit() {
                this.click('#milestone_creation_modal .modal-footer .btn-primary');
                this.test.assert(true, 'milestone created');
            }, function fail() {
                this.capture('screenshot/milestoneCreation/WaitForInvalidInput.png');
                this.test.assert(false, 'input are still invalid after sending the data');
            });
        }, function fail() {
            this.capture('screenshot/milestoneCreation/fillAndSubmitMilestoneCreationModal-error.png');
            this.test.assert(false, 'Milestone modal can not be found');
        });
    });

    /**
     * Wait for modal to close
     */
    casper.then(function waitForMilestoneCreationModalToBeClosed() {
        this.waitWhileSelector('#milestone_creation_modal', function modalClosed() {
            this.test.assert(true, 'Modal is closed');
        }, function fail() {
            this.capture('screenshot/milestoneCreation/waitForMilestoneCreationModalToBeClosed-error.png');
            this.test.assert(false, 'Milestone creation modal not closed');
        });
    });

    /**
     * Verify the milestone is in the list
     */
    casper.then(function checkMilestoneIsCreated() {
        this.waitForSelector('#milestone_table tr td.reference', function modalClosed() {
            this.test.assertSelectorHasText('#milestone_table tr td.reference', changeItems.milestone1.title);
        }, function fail() {
            this.capture('screenshot/milestoneCreation/checkMilestoneIsCreated-error.png');
            this.test.assert(false, 'Milestone not in the list');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
