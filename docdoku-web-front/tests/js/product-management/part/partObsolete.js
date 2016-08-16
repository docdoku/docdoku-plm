/*global casper,urls,products*/

casper.test.begin('Part obsolete tests suite', 3, function partObsoleteTestsSuite() {
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
            this.capture('screenshot/partObsolete/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Select the first part with checkbox
     */
    casper.then(function waitForPartTable() {
        return this.waitForSelector('#part_table tbody tr:first-child  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/partObsolete/waitForPartTable-error.png');
            this.test.assert(false, 'Part can not be found');
        });
    });

    /**
     * Check and click on Mark as obsolete button
     */
    casper.then(function waitForMarkAsObsoleteButton() {
        return this.waitForSelector('.actions .mark-as-obsolete', function checkVisible() {
            this.test.assertVisible('.actions .mark-as-obsolete', 'Mark as obsolete button visible');
            this.click('.actions .mark-as-obsolete');
        }, function fail() {
            this.capture('screenshot/partObsolete/waitForMarkAsObsoleteButton-error.png');
            this.test.assert(false, 'Mark as obsolete button not visible');
        });
    });

    /**
     * Mark as obsolete modal
     */
    casper.then(function waitForMarkAsObsoletePrompt() {
        return this.waitForSelector('.bootbox.modal', function confirmMarkAsObsolete() {
            this.click('.bootbox.modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/partObsolete/waitForMarkAsObsoletePrompt-error.png');
            this.test.assert(false, 'Mark as obsolete modal not found');
        });
    });

    /**
     * Wait for the Mark as obsolete button to be disabled
     */
    casper.then(function waitForMarkAsObsoleteButtonDisabled() {
        return this.waitWhileVisible('.actions .mark-as-obsolete', function checkHidden() {
            this.test.assert(true, 'Mark as obsolete button hidden');
        }, function fail() {
            this.capture('screenshot/partObsolete/waitForMarkAsObsoleteButtonDisabled-error.png');
            this.test.assert(false, 'Mark as obsolete button not hidden');
        });
    });

    /**
     * Check part has been marked as obsolete
     */
    casper.then(function waitForObsoleteIconDisplayed() {
        this.waitForSelector('#part_table i.fa.fa-frown-o', function partIsObsolete() {
            this.test.assertElementCount('#part_table i.fa.fa-frown-o', 1, 'Part has been marked as obsolete');
        }, function fail() {
            this.capture('screenshot/partObsolete/waitForObsoleteIconDisplayed-error.png');
            this.test.assert(false, 'Part has not been marked as obsolete');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
