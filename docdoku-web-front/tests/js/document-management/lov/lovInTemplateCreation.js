/*global $,casper,urls,documents*/
/*jshint -W040*/
casper.test.begin('LOV creation and use in template', 5, function LOVTemplateCreationTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */
    casper.then(function () {
        return this.open(urls.documentManagement);
    });

    function cantFindElement(selector) {
        this.test.assertNotExists(selector, selector + ' should be displayed');
    }

    /**
     * Open template nav
     */
    casper.then(function () {
        var templateMenuItemSelecor = '#template-nav > .nav-list-entry > a';
        return this.waitForSelector(templateMenuItemSelecor, function clickTemplateNavLink() {
            this.click(templateMenuItemSelecor);
        }, cantFindElement.bind(this, templateMenuItemSelecor));
    });

    /**
     * Open LOV creation modal and create a lov Color
     * TODO : Split this 'wait selectors' into unit functions
     */
    casper.then(function () {
        var actionNewLovButtonSelector = '.actions .list-lov';
        return this.waitForSelector(actionNewLovButtonSelector, function () {
            this.click(actionNewLovButtonSelector);
            var modalSelector = '.modal.list_lov';
            this.waitForSelector(modalSelector, function () {
                this.click('.addLOVButton');
                this.test.assertExists('.lovItem', 'An item should be added to the list');
                this.click('.addLOVValue');
                this.click('.addLOVValue');
                this.sendKeys('input.lovItemNameInput', documents.lov.color.itemName);
                var firstItemSelector = '#lovCreationform .lovValues .lovPossibleValue:nth-child(1)';
                this.waitForSelector(firstItemSelector, function () {
                    this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(1) .lovItemNameValueNameInput', documents.lov.color.namePairValueNameRed);
                    this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(1) .lovItemNameValueValueInput', documents.lov.color.namePairValueValueRed);
                    var secondItemSelector = '#lovCreationform .lovValues .lovPossibleValue:nth-child(2)';
                    this.waitForSelector(secondItemSelector, function () {
                        this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(2) .lovItemNameValueNameInput', documents.lov.color.namePairValueNameGreen);
                        this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(2) .lovItemNameValueValueInput', documents.lov.color.namePairValueValueGreen);
                        var thirdItemSelector = '#lovCreationform .lovValues .lovPossibleValue:nth-child(3)';
                        this.waitForSelector(thirdItemSelector, function () {
                            this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(3) .lovItemNameValueNameInput', documents.lov.color.namePairValueNameBlue);
                            this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(3) .lovItemNameValueValueInput', documents.lov.color.namePairValueValueBlue);
                            this.click('.btn-saveLovs');
                        }, cantFindElement.bind(this, thirdItemSelector));
                    }, cantFindElement.bind(this, secondItemSelector));
                }, cantFindElement.bind(this, firstItemSelector));
            }, cantFindElement.bind(this, modalSelector));
        }, cantFindElement.bind(this, actionNewLovButtonSelector));
    });

    /**
     * Open the new model template modal and fill ID
     * TODO : Split this 'wait selectors' into unit functions
     */
    casper.then(function () {
        return this.waitWhileSelector('.modal.list_lov', function () {
            this.click('.actions .new-template');
            this.waitForSelector('.modal.new-template', function () {
                this.waitForSelector('.modal.new-template input.reference', function () {
                    this.sendKeys('.modal.new-template input.reference', documents.lov.template.number);
                }, cantFindElement.bind(this, '.modal.new-template input.reference'));
            }, cantFindElement.bind(this, '.modal.new-template'));
        }, function () {
            this.capture('screenshot/lov/lovCreationSave-error.png');
            this.test.assert(false, 'The save of the lov failed because the modal is still open');
        });

    });
    /*
    * * TODO : Split this 'wait selectors' into unit functions
     */
    casper.then(function () {
        var templateTabSelector = '.modal.new-template .tabs > ul > li:nth-child(3) > a';
        return this.waitForSelector(templateTabSelector, function () {
            this.click(templateTabSelector);
            //wait until the tab Attributes is display
            var attributViewSelector = '.tab-pane.attributes.attributes-edit.active';
            this.waitForSelector(attributViewSelector, function () {
                this.waitForSelector('.add', function () {
                    this.click('.add');
                    var inputSelector = attributViewSelector + ' .list-item input.name';
                    this.waitForSelector(inputSelector, function () {
                        this.sendKeys(inputSelector, documents.lov.template.attributeName);
                    }, cantFindElement.bind(this, inputSelector));
                }, cantFindElement.bind(this, '.add'));
            }, cantFindElement.bind(this, attributViewSelector));

        }, cantFindElement.bind(this, templateTabSelector));
    });

    /**
     * Select type for the attribut to be the LOV and save
     */
    casper.then(function () {
        var selector = '.tab-pane.attributes.attributes-edit.active .list-item select.type option[value="' + documents.lov.color.itemName + '"]';
        return this.waitForSelector(selector, function () {
            this.evaluate(function (pOptionSelector) {
                var option = document.querySelector(pOptionSelector);
                $(option).attr('selected', 'selected').change();
                return true;
            }, selector);
            this.click('.modal.new-template .btn.btn-primary');
        }, cantFindElement.bind(this, selector));

    });

    /**
     * Check if the template has been created and open the modal
     * TODO : Split this 'wait selectors' into unit functions
     */
    casper.then(function () {
        var modalOfTemplateCreationSelector = '.modal.new-template';
        return this.waitWhileSelector(modalOfTemplateCreationSelector, function () {
            var templateReferenceInListSelector = '#document-management-content table.dataTable tr td.reference';
            this.waitForSelector(templateReferenceInListSelector, function templateHasBeenCreated() {
                this.test.assertSelectorHasText(templateReferenceInListSelector, documents.lov.template.number);
                this.click('.dataTables_wrapper .items > tr:first-child .reference');
                this.waitForSelector(modalOfTemplateCreationSelector, function () {
                    this.test.assertExist('.modal.new-template input.reference[value="' + documents.lov.template.number + '"]', 'Reference of the template should be' + documents.lov.template.number);
                }, cantFindElement.bind(this, modalOfTemplateCreationSelector));
            }, cantFindElement.bind(this, templateReferenceInListSelector));
        }, function () {
            this.capture('screenshot/lov/templateWithLOVAttributeSave-error.png');
            this.test.assert(false, 'The save of the template failed because the modal is still open');
        });

    });

    /**
     * Check the type of the attribute
     * TODO : Split this 'wait selectors' into unit functions
     */
    casper.then(function () {
        var templateAttributTabSelector = '.modal.new-template .tabs > ul > li:nth-child(3) > a';
        return this.waitForSelector(templateAttributTabSelector, function () {
            this.click(templateAttributTabSelector);
            var attributViewSelector = '.tab-pane.attributes.attributes-edit.active';
            this.waitForSelector(attributViewSelector, function () {
                var attributeNameSelector = '.tab-pane.attributes.attributes-edit.active .list-item input.name[value="' + documents.lov.template.attributeName + '"]';
                this.test.assertExist(attributeNameSelector, 'Attribut name should be ' + documents.lov.template.attributeName);
                var selectSelector = '.tab-pane.attributes.attributes-edit.active .list-item select.type';
                var expectedValue = documents.lov.color.itemName;
                var isValueOk = this.evaluate(function (selector, expectedValueForSelect) {
                    var selectValue = $(selector).val();
                    return selectValue === expectedValueForSelect;
                }, selectSelector, expectedValue);
                this.test.assertTrue(isValueOk, 'Value of the type of the attribut should be ' + expectedValue);
            }, cantFindElement.bind(this, attributViewSelector));
        }, cantFindElement.bind(this, templateAttributTabSelector));
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
