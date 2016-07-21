/*global casper,urls,products*/

casper.test.begin('Document release tests suite', 3, function documentReleaseTestsSuite() {
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
            this.capture('screenshot/documentRelease/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select the first document with checkbox
     */
    casper.then(function waitForDocumentTable() {
        var checkbox = '#document-management-content table.dataTable tbody tr:first-child td:nth-child(2) input';
        return this.waitForSelector(checkbox, function clickOnDocumentCheckbox() {
            this.click(checkbox);
        }, function fail() {
            this.capture('screenshot/documentRelease/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Check and click on the release document button
     */
    casper.then(function waitForReleaseButton() {
        return this.waitForSelector('.actions .new-release', function checkVisible() {
            this.test.assertVisible('.actions .new-release', 'Release button visible');
            this.click('.actions .new-release');
        }, function fail() {
            this.capture('screenshot/documentRelease/waitForReleaseButton-error.png');
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
            this.capture('screenshot/documentRelease/waitForReleaseSelectionPrompt-error.png');
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
            this.capture('screenshot/documentRelease/waitForReleaseButtonDisabled-error.png');
            this.test.assert(false, 'Release button not hidden');
        });
    });

    /**
     * Check document has been released
     */
    casper.then(function waitForReleaseIconDisplayed() {
        this.waitForSelector('#document-management-content table.dataTable tbody i.fa.fa-check', function documentIsReleased() {
            this.test.assertElementCount('#document-management-content table.dataTable tbody i.fa.fa-check', 1, 'Document has been released');
        }, function fail() {
            this.capture('screenshot/documentRelease/waitForReleaseIconDisplayed-error.png');
            this.test.assert(false, 'Document has not been released');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
