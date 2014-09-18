/*global casper*/

casper.test.begin('Part deletion tests suite', 1, function partDeletionTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function(){
        this.open(productManagementUrl);
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
     * Test delete a part
     */

    casper.then(function waitForPartInList(){
        this.waitForSelector('#part_table tbody tr:first-child td.part_number', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child td:first-child input');
        });
    });

    casper.then(function clickOnDeletePartButton(){
        this.click('.actions .delete');
    });

    casper.then(function waitForPartDiseapear(){
        this.waitWhileSelector('#part_table tbody tr:first-child td.part_number',function partHasBeenDeleted(){
            casper.test.assert(true, "Part has been deleted");
        });
    });

    casper.run(function() {
        this.test.done();
    });

});