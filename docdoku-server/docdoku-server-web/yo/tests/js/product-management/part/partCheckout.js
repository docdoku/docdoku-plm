/*global casper,urls,$*/

casper.test.begin('Part checkout tests suite', 3, function partCheckoutTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        this.open(urls.productManagement);
    });

    /**
     * Go to part nav
     */
    casper.then(function waitForPartNavLink() {
        this.waitForSelector('#part-nav > .nav-list-entry > a', function clickPartNavLink() {
            this.click('#part-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/partCheckout/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Select the first part with checkbox
     */
    casper.then(function waitForPartTable() {
        this.waitForSelector('#part_table tbody tr:first-child  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child  td:nth-child(2) input');
            this.test.assertSelectorHasText('.nav-checkedOut-number-item', 0, 'checkout number at 0 in nav');
        }, function fail() {
            this.capture('screenshot/partCheckout/waitForPartTable-error.png');
            this.test.assert(false, 'Part can not be found');
        });
    });

    /**
     * Click on checkout button
     */
    casper.then(function waitForCheckoutButton() {
        this.waitForSelector('.actions .checkout', function clickOnCheckoutButton() {
            this.click('.actions .checkout');
        }, function fail() {
            this.capture('screenshot/partCheckout/waitForCheckoutButton-error.png');
            this.test.assert(false, 'Checkout button can not be found');
        });
    });

    /**
     * Wait for the checkout button to be disabled
     */
    casper.then(function waitForCheckoutButtonDisabled() {
        this.waitForSelector('.actions .checkout:disabled', function partIsCheckout() {
            this.test.assert(true, 'Part has been checkout');
        }, function fail() {
            this.capture('screenshot/partCheckout/waitForCheckoutButtonDisabled-error.png');
            this.test.assert(false, 'Part has not been checkout');
        });
    });

    /**
     * Wait for the checked out number to be updated
     */
    casper.then(function waitForCheckedOutNumberUpdated() {
        this.waitFor(function check() {
            return this.evaluate(function () {
                return $('.nav-checkedOut-number-item').text() === '1';
            });
        }, function then() {
            this.test.assert(true, 'Checkout nav number updated');
        }, function fail() {
            this.capture('screenshot/partCheckout/waitForNavUpdateCount.png');
            this.test.assert(false, 'Checkout nav number not updated');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});
