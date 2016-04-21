/*global casper,urls*/

casper.test.begin('Part template deletion tests suite', 2, function partTemplateDeletionTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        this.open(urls.productManagement);
    });

    /**
     * Go to part template nav
     */
    casper.then(function waitForPartTemplateNavLink() {
        this.waitForSelector('#part-template-nav > .nav-list-entry > a', function clickPartTemplateNavLink() {
            this.click('#part-template-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/partTemplateDeletion/waitForPartTemplateNavLink-error.png');
            this.test.assert(false, 'Part template nav link can not be found');
        });
    });


    /**
     * Click the 'select all' checkbox
     */
    casper.then(function waitForSelectAllCheckbox() {
        this.waitForSelector('#part_template_table thead tr:first-child  th:first-child input', function clickOnSelectAllCheckbox() {
            this.click('#part_template_table thead tr:first-child  th:first-child input');
        }, function fail() {
            this.capture('screenshot/partTemplateDeletion/waitForSelectAllCheckbox-error.png');
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
            this.capture('screenshot/partTemplateDeletion/waitForDeleteButton-error.png');
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
            this.capture('screenshot/partTemplateDeletion/waitForConfirmationModal-error.png');
            this.test.assert(false, 'Part template deletion confirmation modal can not be found');
        });
    });

    /**
     * Assert that there's no more entries in the table
     **/
    casper.then(function waitForTableToBeEmpty() {
        this.waitWhileSelector('#part_template_table tbody tr:first-child  td:first-child input', function onBaselineTableEmpty() {
            this.test.assert(true, 'No more part templates in the list');
        }, function fail() {
            this.capture('screenshot/partTemplateDeletion/waitForTableToBeEmpty-error.png');
            this.test.assert(false, 'Part template table still not empty');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
