/*global casper*/

casper.test.begin('Part creation tests suite', 4, function partCreationTestsSuite(){

    'use strict';

    /**
    * Open product management URL
    * */
    casper.open(productManagementUrl);

    casper.then(function(){
        this.reload();
    });

    /**
     * Go to part nav
     */
    casper.then(function waitForPartNavLink(){
        this.waitForSelector('#part-nav > .nav-list-entry > a',function clickPartNavLink() {
            this.click('#part-nav > .nav-list-entry > a');
        });
    });

    /**
     * Open the part creation modal
     */
    casper.then(function waitForNewPartButton(){
        this.waitForSelector('.actions .new-part',function clickNewPartButton(){
            this.click('.actions .new-part');
        });
    });

    /**
     * Create a part without a part number
     * */
    casper.then(function waitForNewPartModal(){
        this.waitForSelector('#part_creation_modal .btn-primary',function createEmptyPart(){
            this.click('#part_creation_modal .btn-primary');
            this.test.assertExists('#part_creation_modal input#inputPartNumber:invalid', 'Should not create part without a part number');
        });
    });


    /**
     * Create a part with its partNumber and its partName
     */

    casper.then(function fillNewPartModalForm(){
        this.waitForSelector('#part_creation_modal input#inputPartNumber',function onNewPartFormReady(){
            this.sendKeys('#part_creation_modal input#inputPartNumber', partCreationNumber, {reset:true});
            this.sendKeys('#part_creation_modal input#inputPartName', partCreationName, {reset:true});
            this.click('#part_creation_modal .btn-primary');
        });
    });

    casper.then(function waitForPartToBeCreated(){
        this.waitForSelector('#part_table .part_number span',function partHasBeenCreated(){
            this.test.assertSelectorHasText('#part_table tbody tr:first-child td.part_number span',partCreationNumber);
            this.test.assertSelectorHasText('#part_table tbody tr:first-child td:nth-child(6)',partCreationName);
        });
    });

    casper.then(function waitForModalToBeClosed(){
        this.waitWhileSelector('#part_creation_modal',function onPartModalClosed(){
            this.test.assert(true,'Part modal has been closed');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});