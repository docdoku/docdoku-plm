/*global casper,urls,workspace,changeItems*/

casper.test.begin('Change request creation tests suite', 3, function changeRequestCreationTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        this.open(urls.changeManagement);
    });

    /**
     * Open change requests nav
     */
    casper.then(function waitForChangeRequestsNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/requests"]', function clickOnChangeRequestsNavLink() {
            this.click('a[href="#' + workspace + '/requests"]');
        }, function fail() {
            this.capture('screenshot/requestCreation/waitForChangeRequestsNavLink-error.png');
            this.test.assert(false, 'Change request nav link can not be found');
        });
    });

    /**
     * Open request creation modal
     */
    casper.then(function openNewChangeRequestModal() {
        this.waitForSelector('.actions .new-request',
            function clickOnChangeRequestCreationLink() {
                this.click('.actions .new-request');
            }, function fail() {
                this.capture('screenshot/requestCreation/openNewChangeRequestModal-error.png');
                this.test.assert(false, 'New request button can not be found');
            }
        );
    });

    /**
     * Try to create an request without a name
     */
    casper.then(function waitForChangeRequestCreationModal() {
        this.waitForSelector('#request_creation_modal .modal-footer .btn-primary', function createRequestWithoutName() {
            this.click('#request_creation_modal .modal-footer .btn-primary');
            this.test.assertExists('#request_creation_modal #inputRequestName:invalid', 'Should not create an request without a name');
        }, function fail() {
            this.capture('screenshot/requestCreation/waitForChangeRequestCreationModal-error.png');
            this.test.assert(false, 'Change request modal can not be found');
        });
    });

    /**
     * Fill the form and create the request
     */
    casper.then(function fillAndSubmitChangeRequestCreationModal() {
        this.waitForSelector('#request_creation_modal input#inputRequestName', function fillForm() {
            this.sendKeys('#request_creation_modal input#inputRequestName', changeItems.changeRequest1.number, {reset: true});
            this.click('#request_creation_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/requestCreation/fillAndSubmitChangeRequestCreationModal-error.png');
            this.test.assert(false, 'Change request modal can not be found');
        });
    });

    /**
     * Wait for modal to close
     */
    casper.then(function waitForChangeRequestCreationModalToBeClosed() {
        this.waitWhileSelector('#request_creation_modal', function modalClosed() {
            this.test.assert(true, 'Change request creation modal is closed');
        }, function fail() {
            this.capture('screenshot/requestCreation/checkForChangeRequestCreation-error.png');
            this.test.assert(false, 'Change request creation modal not closed');
        });
    });

    /**
     * Verify the request is in the list
     */
    casper.then(function checkRequestIsCreated() {
        this.waitForSelector('#request_table tr td.reference', function modalClosed() {
            this.test.assertSelectorHasText('#request_table tr td.reference', changeItems.changeRequest1.number);
        }, function fail() {
            this.capture('screenshot/requestCreation/checkForChangeRequestCreation-error.png');
            this.test.assert(false, 'Change request not in the list');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
