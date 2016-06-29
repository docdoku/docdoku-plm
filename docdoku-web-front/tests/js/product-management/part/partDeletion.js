/*global casper,urls,$*/

casper.test.begin('Part deletion tests suite', 3, function partDeletionTestsSuite() {
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
            this.capture('screenshot/partDeletion/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Test delete a part
     */

    casper.then(function waitForPartInList() {
        return this.waitForSelector('#part_table tbody tr:first-child td.part_number', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForPartInList-error.png');
            this.test.assert(false, 'Part to delete rows can not be found');
        });
    });

    casper.then(function clickOnDeletePartButton() {
        this.click('.actions .checkout');
        return this.waitForSelector('.actions .checkout[disabled]', function then() {
            this.test.assertSelectorHasText('.nav-checkedOut-number-item', 1, 'checkout number updated');
            this.click('.actions .delete');
            // Confirm deletion
            // TODO split wait for selector callback
            this.waitForSelector('.bootbox', function confirmPartDeletion() {
                this.click('.bootbox .modal-footer .btn-primary');
            }, function fail() {
                this.capture('screenshot/partDeletion/waitForDeletionConfirmationModal-error.png');
                this.test.assert(false, 'Part deletion confirmation modal can not be found');
            });
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForCheckoutToDisable.png');
            this.test.assert(false, 'checkout button did not set to disabled');
        });
    });

    casper.then(function waitForPartDiseapear() {
        return this.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('#part_table tbody tr').length === 4;
            });
        }, function then() {
            this.test.assert(true, 'Part has been deleted');
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForPartDiseapear-error.png');
            this.test.assert(false, 'Part has not been deleted');
        });
    });

    casper.then(function waitForNavUpdate() {
        return this.waitFor(function check() {
            return this.evaluate(function () {
                return $('.nav-checkedOut-number-item').text() === '0';
            });
        }, function then() {
            this.test.assert(true, 'Checkout nav number updated');
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForNavUpdateCount.png');
            this.test.assert(false, 'Checkout nav number not updated');
        });
    });

    casper.run(function () {
        return this.test.done();
    });

});
