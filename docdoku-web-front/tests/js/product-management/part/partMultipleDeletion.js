/*global casper,urls*/

casper.test.begin('Part multiple deletion tests suite', 1, function partMultipleDeletionTestsSuite() {
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
            this.capture('screenshot/partMultipleDeletion/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Select all parts with checkbox
     */
    casper.then(function waitForPartTable() {
        var checkbox = '#product-management-content table.dataTable thead tr th input[type="checkbox"]';
        return this.waitForSelector(checkbox, function clickOnPartCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/partMultipleDeletion/waitForPartTable-error.png');
            this.test.assert(false, 'Part table can not be found');
        });
    });

    /**
     * Wait for part suppression button
     */

    casper.then(function waitForDeleteButtonDisplayed() {
        return this.waitForSelector('.actions .delete', function deleteButtonIsDisplayed() {
            this.click('.actions .delete');

        }, function fail() {
            this.capture('screenshot/partMultipleDeletion/waitForDeleteButtonDisplayed-error.png');
            this.test.assert(false, 'Parts delete button can not be found');
        });
    });


    /**
     * Confirm parts deletion
     */

    casper.then(function confirmPartsDeletion() {
        return this.waitForSelector('.bootbox', function confirmBoxAppeared() {
            this.click('.bootbox .modal-footer .btn-primary');

        }, function fail() {
            this.capture('screenshot/partMultipleDeletion/confirmPartDeletion-error.png');
            this.test.assert(false, 'Part deletion confirmation modal can not be found');
        });
    });

    /**
     * Wait for parts to be removed
     */

    casper.then(function waitForDocumentsDeletion() {
        return this.waitWhileSelector('#product-management-content table.dataTable tbody tr td.reference', function partsDeleted() {
            this.test.assert(true, 'Parts have been deleted');

        }, function fail() {
            this.capture('screenshot/partMultipleDeletion/waitForPartDeletion-error.png');
            this.test.assert(false, 'Parts have not been deleted');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
