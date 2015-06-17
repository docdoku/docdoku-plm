/*global casper,urls,products*/

casper.test.begin('Part multiple checkin tests suite', 3, function partsMultipleCheckinTestsSuite() {
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
            this.capture('screenshot/MultiplePartsCheckin/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Select the checkout parts with checkbox
     */
    casper.then(function waitForPartTable() {
        this.waitForSelector('#part_table tbody tr:nth-child(2)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(2)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });
    casper.then(function waitForPartTable() {
        this.waitForSelector('#part_table tbody tr:nth-child(3)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(3)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });
    casper.then(function waitForPartTable() {
        this.waitForSelector('#part_table tbody tr:nth-child(5)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(5)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });

    /**
     * Click on checkin button
     */
    casper.then(function waitForCheckinButton() {
        this.waitForSelector('.actions .checkin', function clickOnCheckinButton() {
            var nbPart = this.evaluate(function () {
                return document.querySelectorAll('i.fa.fa-pencil').length;
            });
            this.test.assertSelectorHasText('.nav-checkedOut-number-item', nbPart, 'checkout number at ' + nbPart + ' in nav');
            this.click('.actions .checkin');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForCheckinButton-error.png');
            this.test.assert(false, 'Checkin button can not be found');
        });
    });

    /**
     * Set an iteration note
     */
    casper.then(function waitForIterationNotePrompt() {
        this.waitForSelector('#prompt_modal #prompt_input.ready', function fillIterationNote() {
            this.sendKeys('#prompt_modal #prompt_input', products.part1.iterationNote, {reset: true});
            this.click('#prompt_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForIterationNotePrompt-error.png');
            this.test.assert(false, 'Iteration note modal not found');
        });
    });

    /**
     * Wait for the checkin button to be disabled
     */
    casper.then(function waitForCheckinButtonDisabled() {
        this.waitForSelector('.actions .checkin:disabled', function partIsCheckin() {
            this.test.assert(true, 'Parts have been checkin');
            this.test.assertSelectorHasText('.nav-checkedOut-number-item', 0, 'checkout number updated (0 in nav)');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForCheckinButtonDisabled-error.png');
            this.test.assert(false, 'Parts havent been checkin');
        });
    });


    casper.run(function allDone() {
        this.test.done();
    });

});
