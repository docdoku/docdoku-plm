/*global casper,urls,workspace,documents*/

casper.test.begin('Document multiple checkin tests suite', 2, function documentMultipleCheckinTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        return this.open(urls.documentManagement);
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckin/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select all documents with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable thead tr th input[type="checkbox"]';
        return this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckin/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });


    /**
     * Click on checkin button
     */
    casper.then(function waitForCheckinButton() {
        return this.waitForSelector('.actions .checkin', function clickOnCheckinButton() {
            this.click('.actions .checkin');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckin/waitForCheckinButton-error.png');
            this.test.assert(false, 'Checkin button can not be found');
        });
    });

    /**
     * Set an iteration note
     */
    casper.then(function waitForIterationNotePrompt() {
        return this.waitForSelector('#prompt_modal #prompt_input.ready', function fillIterationNote() {
            this.sendKeys('#prompt_modal #prompt_input', documents.document1.iterationNote, {reset: true});
            this.click('#prompt_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckin/waitForIterationNotePrompt-error.png');
            this.test.assert(false, 'Iteration note modal not found');
        });
    });

    /**
     * Wait for the checkin button to be disabled
     */
    casper.then(function waitForCheckinButtonDisabled() {
        return this.waitForSelector('.actions .checkin:disabled', function documentIsCheckin() {
            this.test.assert(true, 'Document has been checkin');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckin/waitForCheckinButtonDisabled-error.png');
            this.test.assert(false, 'Document has not been checkin');
        });
    });

    /**
     * Wait for all button to be checkin
     */
    casper.then(function waitForDisplayCheckin() {
        return this.waitWhileSelector('tbody > tr > td.reference.doc-ref > a > i.fa.fa-pencil', function () {
            this.test.assert(true, 'Document retrieved');
        }, function fail() {
            this.capture('screenshot/documentMultipleCheckin/waitForCheckinDocuments-error.png');
            this.test.assert(false, 'Document has not been checkin');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
