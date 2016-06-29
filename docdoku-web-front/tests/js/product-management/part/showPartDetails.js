/*global casper,urls,products*/

casper.test.begin('Part details tests suite', 3, function partDetailsTestsSuite() {

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
        });
    });

    /**
     * Wait for part list display
     */

    casper.then(function waitForPartInList() {
        return this.waitForSelector('#part_table tbody tr:first-child td.part_number', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child td.part_number span');
        });
    });

    /**
     * Wait for part modal
     */
    casper.then(function waitForModalDisplay() {
        return this.waitForSelector('#part-modal', function testPartModal() {
            this.test.assertSelectorHasText('#form-part div:first-child div span', products.part1.number);
            this.test.assertSelectorHasText('#form-part div:nth-child(2) div span', products.part1.name);
        });
    });

    /**
     * Close modal
     */

    casper.then(function waitForCancelButton() {
        return this.waitForSelector('#part-modal button.close', function closePartModal() {
            this.click('#part-modal button.close');
        });
    });

    casper.then(function waitForModalToBeClosed() {
        return this.waitWhileSelector('#part-modal', function onPartModalClosed() {
            this.test.assert(true, 'Part modal has been closed');
        });
    });

    casper.run(function () {
        return this.test.done();
    });

});
