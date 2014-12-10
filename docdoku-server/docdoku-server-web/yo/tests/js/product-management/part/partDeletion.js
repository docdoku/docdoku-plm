/*global casper,urls*/

casper.test.begin('Part deletion tests suite', 1, function partDeletionTestsSuite(){
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
        },function fail() {
            this.capture('screenshot/partDeletion/waitForPartNavLink-error.png');
            this.test.assert(false,'Part nav link can not be found');
        });
    });

    /**
     * Test delete a part
     */

    casper.then(function waitForPartInList(){
        this.waitForSelector('#part_table tbody tr:first-child td.part_number', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child td:first-child input');
        },function fail() {
            this.capture('screenshot/partDeletion/waitForPartInList-error.png');
            this.test.assert(false,'Part to delete rows can not be found');
        });
    });

    casper.then(function clickOnDeletePartButton(){
        this.click('.actions .delete');
        // Confirm deletion
        this.waitForSelector('.bootbox',function confirmPartDeletion(){
            this.click('.bootbox .modal-footer .btn-primary');
        },function fail() {
            this.capture('screenshot/partDeletion/waitForDeletionConfirmationModal-error.png');
            this.test.assert(false,'Part deletion confirmation modal can not be found');
        });
    });

    casper.then(function waitForPartDiseapear(){
        this.waitWhileSelector('#part_table tbody tr:first-child td.part_number',function partHasBeenDeleted(){
            casper.test.assert(true, "Part has been deleted");
        },function fail() {
            this.capture('screenshot/partDeletion/waitForPartDiseapear-error.png');
            this.test.assert(false,'Part has not been deleted');
        });
    });

    casper.run(function() {
        this.test.done();
    });

});
