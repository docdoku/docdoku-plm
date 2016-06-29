/*global casper,urls*/

casper.test.begin('Parts  multiple checkout tests suite', 1, function partsMultipleUndoCheckoutTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productManagement);
    });

    /**
     * Go to part nav
     */
    casper.then(function waitForPartNavLink() {
        return this.waitForSelector('#part-nav > .nav-list-entry > a', function clickPartNavLink() {
            this.click('#part-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/partUndoCheckout/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Select the parts with checkbox
     */
    casper.then(function waitForPartTable() {
        return this.waitForSelector('#part_table tbody tr:nth-child(2)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(2)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/partUndoCheckout/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });
    casper.then(function waitForPartTable() {
        return this.waitForSelector('#part_table tbody tr:nth-child(3)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(3)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/partUndoCheckout/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });
    casper.then(function waitForPartTable() {
        return this.waitForSelector('#part_table tbody tr:nth-child(5)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(5)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/partUndoCheckout/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });


    /**
     * Click on checkout button
     */
    casper.then(function waitForUndoCheckoutButton() {
        return this.waitForSelector('.actions .undocheckout', function clickOnUndoCheckoutButton() {
            this.click('.actions .undocheckout');
        }, function fail() {
            this.capture('screenshot/partUndoCheckout/waitForUndoCheckoutButton-error.png');
            this.test.assert(false, 'undoCheckout button can not be found');
        });
    });

    /**
     * Wait for confirmation box
     */
    casper.then(function waitForConfirmationBox() {
        return this.waitForSelector('div.modal-body', function fillIterationNote() {
            this.click('.modal-footer a[data-handler="1"]');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForIterationNotePrompt-error.png');
            this.test.assert(false, 'Iteration note modal not found');
        });
    });

    /**
     * Wait for the checkout button to be disabled
     */
    casper.then(function waitForCheckoutButtonDisabled() {
        return this.waitForSelector('.actions .undocheckout:disabled', function partIsCheckout() {
            this.test.assert(true, 'Parts have been undocheckout');
        }, function fail() {
            this.capture('screenshot/partUndoCheckout/waitForCheckoutButtonDisabled-error.png');
            this.test.assert(false, 'Parts have not been checkout');
        });
    });


    casper.run(function allDone() {
        return this.test.done();
    });

});
