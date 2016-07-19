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
     * Check release button display
     */
    casper.then(function waitForReleaseButton() {
        return this.waitFor(function checkVisible() {
            return this.evaluate(function() {
                return $('.actions .new-release').is(':visible');
            });
        }, function success() {
            this.test.assert(true, 'Release button visible');
        }, function fail() {
            this.test.assert(false, 'Release button not visible');
        });
    });

    /**
     * Click on the release part button
     * */
    casper.then(function releasePart() {
        return this.waitForSelector('.actions .new-release', function buttonIsVisible() {
            this.click('.actions .new-release');
        }, function fail() {
            this.capture('screenshot/partRelease/waitForReleaseButton-error.png');
            this.test.assert(false, 'Release button can not be found');
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
        return this.waitFor(function checkHidden() {
            return this.evaluate(function() {
                return $('.actions .new-release').is(':hidden');
            });
        }, function success() {
            this.test.assert(true, 'Release button hidden');
        }, function fail() {
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
