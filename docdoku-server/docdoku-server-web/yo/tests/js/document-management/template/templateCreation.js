/*global casper*/

casper.test.begin('Document template creation tests suite',2, function documentTemplateCreationTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function(){
        this.open(documentManagementUrl);
    });

    /**
     * Open template nav
     */

    casper.then(function waitForTemplateNavLink(){
        this.waitForSelector('#template-nav > .nav-list-entry > a',function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        });
    });

    /**
     * Open template creation modal
     */

    casper.then(function waitForTemplateCreationLink(){
        this.waitForSelector('.actions .new-template',function clickOnTemplateCreationLink(){
            this.click('.actions .new-template');
        })
    });

    /**
     * Wait for template creation modal
     */

    casper.then(function waitForTemplateCreationModal(){
        this.waitForSelector('.modal.new-template',function templateCreationModalDisplayed(){
            this.click('.modal.new-template .btn.btn-primary');
            this.test.assertExists('.modal.new-template input.reference:invalid', 'Should not create document template without a reference');
        });
    });

    /**
     * Fill the form and create document template
     */

    casper.then(function fillAndSubmitTemplateCreationModal(){
        this.waitForSelector('.modal.new-template input.reference',function(){
            this.sendKeys('.modal.new-template input.reference',documentTemplateCreationNumber);
            this.click('.modal.new-template .btn.btn-primary');
        });
    });

    /**
     *  Check if template has been created
     * */

     casper.then(function checkIfTemplateHasBeenCreated(){
        this.waitForSelector('#document-management-content table.dataTable tr td.reference',function templateHasBeenCreated(){
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr td.reference',documentTemplateCreationNumber);
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});