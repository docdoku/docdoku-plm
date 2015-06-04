/*global casper,urls,products*/

casper.test.begin('Part creation tests suite', 6, function partCreationTestsSuite(){
    'use strict';

    casper.open('');

    /**
    * Open product management URL
    * */

    casper.then(function(){
        this.open(urls.productManagement);
    });

    /**
     * Go to part nav
     */
    casper.then(function waitForPartNavLink(){
        this.waitForSelector('#part-nav > .nav-list-entry > a',function clickPartNavLink() {
            this.click('#part-nav > .nav-list-entry > a');
        },function fail(){
            this.capture('screenshot/partCreation/waitForPartNavLink-error.png');
            this.test.assert(false,'Part nav link can not be found');
        });
    });

    /**
     * Open the part creation modal
     */
    casper.then(function waitForNewPartButton(){
        this.waitForSelector('.actions .new-part',function clickNewPartButton(){
            this.click('.actions .new-part');
        },function fail() {
            this.capture('screenshot/partCreation/waitForNewPartButton-error.png');
            this.test.assert(false,'New part button can not be found');
        });
    });

    /**
     * Create a part without a part number
     * */
    casper.then(function waitForNewPartModal(){
        this.waitForSelector('#part_creation_modal .btn-primary',function createEmptyPart(){
            this.click('#part_creation_modal .btn-primary');
            this.test.assertExists('#part_creation_modal input#inputPartNumber:invalid', 'Should not create part without a part number');
        },function fail() {
            this.capture('screenshot/partCreation/waitForNewPartModal-error.png');
            this.test.assert(false,'New part modal can not be found');
        });
    });


    /**
     * Create a part with its partNumber and its partName
     */

    casper.then(function fillNewPartModalForm(){
        this.waitForSelector('#part_creation_modal input#inputPartNumber',function onNewPartFormReady(){
            this.test.assertElementCount('#inputPartTemplate option',3,'template options are present');
            this.evaluate(function() {
                document.querySelector('#inputPartTemplate').selectedIndex = 2;
                $('#inputPartTemplate').change();
                return true;
            });
            this.sendKeys('#part_creation_modal input#inputPartNumber', products.part1.number, {reset:true});
            this.sendKeys('#part_creation_modal input#inputPartName', products.part1.name, {reset:true});
        },function fail() {
            this.capture('screenshot/partCreation/onNewPartFormReady-error.png');
            this.test.assert(false,'New part form can not be found');
        });
    });


    casper.then(function fillAttributeValue() {
        var attributesTabSelector = '.nav.nav-tabs > li:nth-child(3) > a';
        this.waitForSelector(attributesTabSelector, function () {

            this.click(attributesTabSelector);
            this.waitForSelector('.nav.nav-tabs > li:nth-child(3).active', function () {
                this.test.assert(true, 'Attribute tab found');
                this.waitForSelector('#attributes-list .list-item', function addAttr() {
                    this.sendKeys('#attributes-list input[required].value', products.part1.attributeValue);
                    this.click('#part_creation_modal .btn-primary');
                }, function fail() {
                    this.capture('screenshot/partCreation/listAttributeNotFound.png');
                    this.test.assert(false, 'attributes list not found');
                });

            }, function fail() {
                this.capture('screenshot/partCreation/attributeTabBecomeActive-error.png');
                this.test.assert(false, 'Attribute tab not appearing');
            });
        }, function fail() {
            this.capture('screenshot/partCreation/clickOnAttributeTab-error.png');
            this.test.assert(false, 'Attribute tab cannot be found');
        });
    });

    /**
     * Wait for the part to be created, will appears in the list
     */
    casper.then(function waitForPartToBeCreated(){
        this.capture('screenshot/newPartNotFound.png');
        this.waitForSelector('#part_table .part_number span',function partHasBeenCreated(){
            this.test.assertSelectorHasText('#part_table tbody tr:first-child td.part_number span',products.part1.number);
            this.test.assertSelectorHasText('#part_table tbody tr:first-child td:nth-child(8)',products.part1.name);
        },function fail() {
            this.capture('screenshot/partCreation/waitForPartToBeCreated-error.png');
            this.test.assert(false,'New part created can not be found');
        });
    });

    casper.then(function waitForModalToBeClosed(){
        this.waitWhileSelector('#part_creation_modal',function onPartModalClosed(){
            this.test.assert(true,'Part modal has been closed');
        },function fail() {
            this.capture('screenshot/partCreation/waitForModalToBeClosed-error.png');
            this.test.assert(false,'Part modal can not close');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});
