/*global casper, workspaceUrl,partCreationNumber,partCreationName,__utils__*/
'use strict';

casper.test.begin('Part details tests suite',3, function partDetailsTestsSuite(){

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
     * Wait for part list display
     */
    casper.then(function waitForPartInList(){
        this.waitForSelector('#part_table tbody tr:first-child td.part_number', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child td.part_number span');
        });
    });

    /**
     * Wait for part modal
     */
    casper.then(function waitForModalDisplay(){
        this.waitForSelector('#part-modal',function testPartModal() {
            this.test.assertSelectorHasText('#form-part div:first-child div span',partCreationNumber);
            this.test.assertSelectorHasText('#form-part div:nth-child(2) div span',partCreationName);
        });
    });

    /**
     * Close modal
     */
    casper.then(function waitForCancelButton() {
        this.waitForSelector('#part-modal #cancel-iteration',function testPartModal() {
            this.click('#part-modal #cancel-iteration');
        });
    });

    casper.then(function waitForModalToBeClosed(){
       this.waitWhileSelector('#part-modal',function onPartModalClosed(){
           this.test.assert(true,'Part modal has been closed');
       });
    });

    casper.run(function() {
        this.test.done();
    });
});