/*global casper,urls,documents*/

casper.test.begin('Document template creation tests suite', 4, function documentTemplateCreationTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        this.open(urls.documentManagement);
    });

    /**
     * Open template nav
     */

    casper.then(function waitForTemplateNavLink() {
        this.waitForSelector('#template-nav > .nav-list-entry > a', function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        });
    });

    /**
     * Open template creation modal
     */

    casper.then(function waitForTemplateCreationLink() {
        this.waitForSelector('.actions .new-template', function clickOnTemplateCreationLink() {
            this.click('.actions .new-template');
        });
    });

    /**
     * Wait for template creation modal
     */

    casper.then(function waitForTemplateCreationModal() {
        this.waitForSelector('.modal.new-template', function templateCreationModalDisplayed() {
            this.click('.modal.new-template .btn.btn-primary');
            this.test.assertExists('.modal.new-template input.reference:invalid', 'Should not create document template without a reference');
        });
    });

    /**
     * Fill the form and create document template
     */

    casper.then(function fillAndSubmitTemplateCreationModal() {
        this.waitForSelector('.modal.new-template input.reference', function () {
            this.sendKeys('.modal.new-template input.reference', documents.template1.number);
            this.sendKeys('.modal.new-template input.type', documents.template1.type);
            this.sendKeys('.modal.new-template input.mask', documents.template1.mask);
            this.click('.modal.new-template input.id-generated');
            this.click('.modal.new-template .btn.btn-primary');
        });
    });

    /**
     *  Check if template has been created
     * */

    casper.then(function checkIfTemplateHasBeenCreated() {
        this.waitForSelector('#document-management-content table.dataTable tr td.reference', function templateHasBeenCreated() {
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr td.reference', documents.template1.number);
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr td.type', documents.template1.type);
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr td.mask', documents.template1.mask);
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
