/*global casper,urls,workspace,documents*/

casper.test.begin('Document checkin tests suite', 1, function documentCheckinTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        this.open(urls.documentManagement);
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/documentCheckin/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select the first document with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable tbody tr:first-child td:nth-child(2) input';
        this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/documentCheckin/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Click on checkin button
     */
    casper.then(function waitForCheckinButton() {
        this.waitForSelector('.actions .checkin', function clickOnCheckinButton() {
            this.click('.actions .checkin');
        }, function fail() {
            this.capture('screenshot/documentCheckin/waitForCheckinButton-error.png');
            this.test.assert(false, 'Checkin button can not be found');
        });
    });

    /**
     * Set an iteration note
     */
    casper.then(function waitForIterationNotePrompt() {
        this.waitForSelector('#prompt_modal #prompt_input.ready', function fillIterationNote() {
            this.sendKeys('#prompt_modal #prompt_input', documents.document1.iterationNote, {reset: true});
            this.click('#prompt_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/documentCheckin/waitForIterationNotePrompt-error.png');
            this.test.assert(false, 'Iteration note modal not found');
        });
    });

    /**
     * Wait for the checkin button to be disabled
     */
    casper.then(function waitForCheckinButtonDisabled() {
        this.waitForSelector('.actions .checkin:disabled', function documentIsCheckin() {
            this.test.assert(true, 'Document has been checkin');
        }, function fail() {
            this.capture('screenshot/documentCheckin/waitForCheckinButtonDisabled-error.png');
            this.test.assert(false, 'Document has not been checkin');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
