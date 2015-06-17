/*global casper,urls,products*/

casper.test.begin('Part template creation tests suite', 5, function partTemplateCreationTestsSuite() {
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
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateNavLink-error.png');
            this.test.assert(false, 'Part template nav link can not be found');
        });
    });

    /**
     * Wait for the creation button
     */
    casper.then(function waitForPartTemplateCreationButton() {
        this.waitForSelector('.actions .new-template', function clickPartTemplateCreationButton() {
            this.click('.actions .new-template');
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationButton-error.png');
            this.test.assert(false, 'Part template creation button can not be found');
        });
    });

    /**
     * Wait for the creation modal
     */
    casper.then(function waitForPartTemplateCreationModal() {
        this.waitForSelector('#part_template_creation_modal', function modalOpened() {

            this.click('#part_template_creation_modal .modal-footer .btn-primary');
            this.test.assertExists('#part_template_creation_modal #part-template-reference:invalid', 'Should not create template without a reference');

        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationModal-error.png');
            this.test.assert(false, 'Part template creation modal not found');
        });
    });


    /**
     * Fill and sumbit the form
     */
    casper.then(function fillAndSubmitTheForm() {
        this.sendKeys('#part_template_creation_modal #part-template-reference', products.template1.number, {reset: true});
        this.sendKeys('#part_template_creation_modal #part-template-type', products.template1.type, {reset: true});
        this.sendKeys('#part_template_creation_modal #part-template-mask', products.template1.mask, {reset: true});
        this.click('#part_template_creation_modal .modal-footer .btn-primary');
    });


    /**
     * Wait for the modal to close
     */
    casper.then(function waitForModalToClose() {
        this.waitWhileSelector('#part_template_creation_modal', function modalClosed() {
            this.test.assert(true, 'Part template creation modal closed');
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForModalToClose-error.png');
            this.test.assert(false, 'Part template creation modal not closed');
        });
    });

    /**
     * Check if template has been created in the list
     */
    casper.then(function checkForTemplateCreated() {
        this.waitForSelector('#part_template_table .reference', function tableDisplayed() {
            this.test.assertSelectorHasText('#part_template_table tbody tr:first-child td.reference', products.template1.number);
            this.test.assertSelectorHasText('#part_template_table tbody tr:first-child td:nth-child(3)', products.template1.type);
            this.test.assertSelectorHasText('#part_template_table tbody tr:first-child td:nth-child(4)', products.template1.mask);
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/checkFortemplateCreated-error.png');
            this.test.assert(false, 'New part template can not be found');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
