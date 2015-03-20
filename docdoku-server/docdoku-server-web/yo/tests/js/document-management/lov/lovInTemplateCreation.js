/*global casper,urls,documents*/

casper.test.begin('LOV creation and use in template',0, function LOVTemplateCreationTestsSuite(){
    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */
    casper.then(function(){
        this.open(urls.documentManagement);
    });

    /**
     * Open template nav
     */
    casper.then(function(){
        this.waitForSelector('#template-nav > .nav-list-entry > a',function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        });
    })

    /**
     * Open LOV creation modal and create a lov Color
     */
    casper.then(function (){
        this.waitForSelector('.actions .list-lov',function (){
            this.click('.actions .list-lov');
            this.waitForSelector('.modal.list_lov',function (){
                this.click('.addLOVButton');
                this.test.assertExists('.lovItem', 'An item should be added to the list');
                this.click('.addLOVValue');
                this.click('.addLOVValue');
                this.sendKeys('input.lovItemNameInput',documents.lov.color.itemName);
                this.waitForSelector('#lovCreationform .lovValues .lovPossibleValue:nth-child(1)',function (){
                    this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(1) .lovItemNameValueNameInput',documents.lov.color.namePairValueNameRed);
                    this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(1) .lovItemNameValueValueInput',documents.lov.color.namePairValueValueRed);
                    this.waitForSelector('#lovCreationform .lovValues .lovPossibleValue:nth-child(2)',function (){
                        this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(2) .lovItemNameValueNameInput',documents.lov.color.namePairValueNameGreen);
                        this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(2) .lovItemNameValueValueInput',documents.lov.color.namePairValueValueGreen);
                        this.waitForSelector('#lovCreationform .lovValues .lovPossibleValue:nth-child(3)',function (){
                            this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(3) .lovItemNameValueNameInput',documents.lov.color.namePairValueNameBlue);
                            this.sendKeys('#lovCreationform .lovValues .lovPossibleValue:nth-child(3) .lovItemNameValueValueInput',documents.lov.color.namePairValueValueBlue);
                            this.click('.btn-saveLovs');
                        });
                    });
                });
            });
        });
    });

    /**
     * Open the new model template modal and fill ID
     */
    casper.then(function (){
        this.waitWhileSelector('.modal.list_lov', function(){
            this.click('.actions .new-template');
            this.waitForSelector('.modal.new-template',function(){
                this.waitForSelector('.modal.new-template input.reference',function(){
                    this.sendKeys('.modal.new-template input.reference',documents.lov.template.number);
                });
            });
        }, function(){
            //Modal of lov should be closed
        });

    });

    casper.then(function(){
        var templateTabSelector = '.modal.new-template .tabs > ul > li:nth-child(3) > a';
        this.waitForSelector(templateTabSelector, function(){
            this.click(templateTabSelector);
            //wait until the tab Attributes is display
            var attributViewSelector = '.tab-pane.attributes.attributes-edit.active';
            this.waitForSelector(attributViewSelector, function(){
                this.waitForSelector('.add', function(){
                    this.click('.add');
                    var inputSelector = attributViewSelector+' .list-item input.name';
                    this.waitForSelector(inputSelector, function(){
                        this.sendKeys(inputSelector, documents.lov.template.attributName);
                    });
                });
            });

        });
    });

    ///**
    // * Select type for the attribut
    // */
    //casper.thenEvaluate(function(){
    //    var optionSelector = '.tab-pane.attributes.attributes-edit.active .list-item select.type option[value="'+documents.lov.color.itemName+'"]';
    //    var option = $(optionSelector);
    //    option.attr('selected', true);
    //    option.trigger('change');
    //});

    /**
     * Check the type of the attribut
     */

    casper.then(function() {

        var optionSelector = '.tab-pane.attributes.attributes-edit.active .list-item select.type option[value="'+documents.lov.color.itemName+'"]'
        this.waitForSelector(optionSelector, function(){
            this.evaluate(function () {
                var option = document.querySelector(optionSelector);
                $(option).attr('selected', 'selected').change();
                return true;
            });
            this.wait(1000, function(){
                this.capture('screenshot/lov/templatelov.png');
                this.click('.modal.new-template .btn.btn-primary');
            });
        });

    });

        //var attributSelectSelector = '.tab-pane.attributes.attributes-edit.active .list-item select.type"]';
        //var select = $(attributSelectSelector);

    /**
     * Save the template
     */
    casper.then(function(){

    });

    casper.run(function allDone() {
        this.test.done();
    });

});
