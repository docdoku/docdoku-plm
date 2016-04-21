/*global casper,urls,workspace,changeItems*/

casper.test.begin('Change issue creation tests suite', 3, function changeIssueCreationTestsSuite() {

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
            this.capture('screenshot/issueCreation/waitForChangeIssuesNavLink-error.png');
            this.test.assert(false, 'Change issue nav link can not be found');
        });
    });

    /**
     * Open issue creation modal
     */
    casper.then(function openNewChangeIssueModal() {
        this.waitForSelector('.actions .new-issue',
            function clickOnChangeIssueCreationLink() {
                this.click('.actions .new-issue');
            }, function fail() {
                this.capture('screenshot/issueCreation/openNewChangeIssueModal-error.png');
                this.test.assert(false, 'New issue button can not be found');
            }
        );
    });

    /**
     * Try to create an issue without a name
     */
    casper.then(function waitForChangeIssueCreationModal() {
        this.waitForSelector('#issue_creation_modal .modal-footer .btn-primary', function createIssueWithoutName() {
            this.click('#issue_creation_modal .modal-footer .btn-primary');
            this.test.assertExists('#issue_creation_modal #inputIssueName:invalid', 'Should not create an issue without a name');
        }, function fail() {
            this.capture('screenshot/issueCreation/waitForChangeIssueCreationModal-error.png');
            this.test.assert(false, 'Change issue modal can not be found');
        });
    });

    /**
     * Fill the form and create the issue
     */
    casper.then(function fillAndSubmitChangeIssueCreationModal() {
        this.waitForSelector('#issue_creation_modal input#inputIssueName', function fillForm() {
            this.sendKeys('#issue_creation_modal input#inputIssueName', changeItems.changeIssue1.number, {reset: true});
            this.click('#issue_creation_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/issueCreation/fillAndSubmitChangeIssueCreationModal-error.png');
            this.test.assert(false, 'Change issue modal can not be found');
        });
    });

    /**
     * Wait for modal to close
     */
    casper.then(function waitForChangeIssueCreationModalToBeClosed() {
        this.waitWhileSelector('#issue_creation_modal', function modalClosed() {
            this.test.assert(true, 'Modal is closed');
        }, function fail() {
            this.capture('screenshot/issueCreation/checkForChangeIssueCreation-error.png');
            this.test.assert(false, 'Change issue creation modal not closed');
        });
    });

    /**
     * Verify the issue is in the list
     */
    casper.then(function checkIssueIsCreated() {
        this.waitForSelector('#issue_table tr td.reference', function modalClosed() {
            this.test.assertSelectorHasText('#issue_table tr td.reference', changeItems.changeIssue1.number);
        }, function fail() {
            this.capture('screenshot/issueCreation/checkForChangeIssueCreation-error.png');
            this.test.assert(false, 'Change issue not in the list');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
