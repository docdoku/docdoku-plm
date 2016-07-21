/*global casper,urls,products*/

casper.test.begin('Part release tests suite', 3, function partReleaseTestsSuite() {
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
            this.capture('screenshot/partRelease/waitForPartNavLink-error.png');
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
            this.capture('screenshot/partRelease/waitForPartTable-error.png');
            this.test.assert(false, 'Part can not be found');
        });
    });

    /**
     * Check and click on the release part button
     */
    casper.then(function waitForReleaseButton() {
        return this.waitForSelector('.actions .new-release', function checkVisible() {
            this.test.assertVisible('.actions .new-release', 'Release button visible');
            this.click('.actions .new-release');
        }, function fail() {
            this.capture('screenshot/partRelease/waitForReleaseButton-error.png');
            this.test.assert(false, 'Release button not visible');
        });
    });

    /**
     * Release selection modal
     */
    casper.then(function waitForReleaseSelectionPrompt() {
        return this.waitForSelector('.bootbox.modal', function confirmReleaseSelection() {
            this.click('.bootbox.modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/partRelease/waitForReleaseSelectionPrompt-error.png');
            this.test.assert(false, 'Release selection modal not found');
        });
    });

    /**
     * Wait for the release button to be disabled
     */
    casper.then(function waitForReleaseButtonDisabled() {
        return this.waitForSelector('.actions .new-release', function checkHidden() {
            this.test.assertNotVisible('.actions .new-release', 'Release button hidden');
        }, function fail() {
            this.capture('screenshot/partRelease/waitForReleaseButtonDisabled-error.png');
            this.test.assert(false, 'Release button not hidden');
        });
    });

    /**
     * Check part has been released
     */
    casper.then(function waitForReleaseIconDisplayed() {
        this.waitForSelector('#part_table i.fa.fa-check', function partIsReleased() {
            this.test.assertElementCount('#part_table i.fa.fa-check', 1, 'Part has been released');
        }, function fail() {
            this.capture('screenshot/partRelease/waitForReleaseIconDisplayed-error.png');
            this.test.assert(false, 'Part has not been released');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
