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

    casper.then(function waitForDeleteButton() {
        return this.waitForSelector('.actions .delete', function clickOnDeleteButton() {
            this.click('.actions .delete');
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForDeleteButton.png');
            this.test.assert(false, 'Delete button is not displayed');
        });
    });

    casper.then(function waitForDeletionConfirmationModal() {
        this.waitForSelector('.bootbox', function confirmPartDeletion() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForDeletionConfirmationModal-error.png');
            this.test.assert(false, 'Part deletion confirmation modal can not be found');
        });
    });

    casper.then(function waitForDeletionConfirmationModalDisappear() {
        this.waitWhileSelector('.bootbox', function confirmPartDeletion() {
            this.test.assert(true, 'Deletion confirmation modal has disappeared');
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForDeletionConfirmationModalDisappear-error.png');
            this.test.assert(false, 'Part deletion confirmation modal still displayed');
        });
    });

    casper.then(function waitForPartDisappear() {
        return this.waitForSelector('#part_table tbody tr', function check() {
            this.test.assertElementCount('#part_table tbody tr', 4, 'Part has been deleted');
        }, function fail() {
            this.capture('screenshot/partDeletion/waitForPartDisappear-error.png');
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
