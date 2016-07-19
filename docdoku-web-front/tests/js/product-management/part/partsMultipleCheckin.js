/*global casper,urls,products*/

casper.test.begin('Part multiple checkin tests suite', 5, function partsMultipleCheckinTestsSuite() {
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
            this.capture('screenshot/MultiplePartsCheckin/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Select the checkout parts with checkbox
     */
    casper.then(function waitForPartTable() {
        return this.waitForSelector('#part_table tbody tr:nth-child(2)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(2)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });
    casper.then(function waitForPartTable() {
        return this.waitForSelector('#part_table tbody tr:nth-child(3)  td:nth-child(2) input', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:nth-child(3)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForPartTable-error.png');
            this.test.assert(false, 'Part cannot be found');
        });
    });
    casper.then(function waitForPartTable() {
        return this.waitForSelector('#part_table tbody tr:nth-child(5)  td:nth-child(2) input', function clickOnPartCheckbox() {
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
        return this.waitForSelector('.actions .checkin:not([disabled])', function clickOnCheckinButton() {
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
     * Send keys for an iteration note
     */
    casper.then(function waitForIterationNotePrompt() {
        return  this.waitForSelector('#prompt_modal #prompt_input.ready', function promptIterationNoteModal() {
            this.sendKeys('#prompt_modal #prompt_input', products.part1.iterationNote, {reset: true});
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForIterationNotePrompt-error.png');
            this.test.assert(false, 'Iteration note modal not found');
        });
    });

    /**
     * Save an iteration note
     */
    casper.then(function addIterationNote() {
        return this.waitForSelector('#prompt_modal #prompt_input.ready', function fillIterationNote() {
            this.click('#prompt_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForIterationNoteSent-error.png');
            this.test.assert(false, 'Iteration note cannot be sent');
        });
    });

    /**
     * Wait for iteration note modal disappear
     */
    casper.then(function waitForIterationNoteHidden() {
        return this.waitWhileSelector('#prompt_modal #prompt_input.ready', function hideIterationNoteModal() {
            this.test.assert(true, 'Iteration note modal has disappeared');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForIterationNoteGone-error.png');
            this.test.assert(false, 'Iteration note modal still visible');
        });
    });

    /**
     * Wait for the checkin button to be disabled
     */
    casper.then(function waitForCheckinButtonDisabled() {
        return this.waitForSelector('.actions .checkin[disabled]', function partsCheckinInProgress() {
            this.test.assert(true, 'Parts checkin is in progress...');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForCheckinButtonDisabled-error.png');
            this.test.assert(false, 'Parts checkin has not begun');
        });
    });

    /**
     * Wait for the parts to be all checked in
     */
    casper.then(function waitForPartsCheckedIn() {
        return this.waitForSelector('tbody > tr > td.part_number', function partsAreCheckedIn() {
            var nbPart = this.evaluate(function () {
                return document.querySelectorAll('i.fa.fa-pencil').length;
            });
            this.test.assertEquals(nbPart, 0, 'All parts checked in');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForCheckedInParts-error.png');
            this.test.assert(false, 'All parts have not been checked in');
        });
    });

    /**
     * Wait for the checked out number to be updated
     */
    casper.then(function waitForCheckedOutNumberUpdated() {
        return this.waitForSelector('.nav-checkedOut-number-item', function checkedOutNumberIsUpdated() {
            this.test.assertSelectorHasText('.nav-checkedOut-number-item', 0, 'checked out number updated (0 in nav)');
        }, function fail() {
            this.capture('screenshot/MultiplePartsCheckin/waitForCheckedOutNumberUpdated-error.png');
            this.test.assert(false, 'Checked out number has not been updated');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
