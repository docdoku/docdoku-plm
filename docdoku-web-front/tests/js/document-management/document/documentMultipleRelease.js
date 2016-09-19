/*global casper,urls,workspace,documents*/

casper.test.begin('Documents multiple release tests suite', 3, function documentMultipleReleaseTestsSuite() {
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
            this.capture('screenshot/MultipleDocumentsRelease/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Select 2 checked in documents with checkbox
     */
    casper.then(function waitForDocumentTable() {
        return this.waitForSelector('#document-management-content table.dataTable tbody tr:nth-child(2)  td:nth-child(2) input', function clickOnDocumentCheckbox() {
            this.click('#document-management-content table.dataTable tbody tr:nth-child(2)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/MultipleDocumentsRelease/waitForDocumentTable1-error.png');
            this.test.assert(false, 'Document cannot be found');
        });
    });
    casper.then(function waitForDocumentTable() {
        return this.waitForSelector('#document-management-content table.dataTable tbody tr:nth-child(3)  td:nth-child(2) input', function clickOnDocumentCheckbox() {
            this.click('#document-management-content table.dataTable tbody tr:nth-child(3)  td:nth-child(2) input');
        }, function fail() {
            this.capture('screenshot/MultipleDocumentsRelease/waitForDocumentTable2-error.png');
            this.test.assert(false, 'Document cannot be found');
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
            this.capture('screenshot/MultipleDocumentsRelease/waitForReleaseButton-error.png');
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
            this.capture('screenshot/MultipleDocumentsRelease/waitForReleaseSelectionPrompt-error.png');
            this.test.assert(false, 'Release selection modal not found');
        });
    });

    /**
     * Wait for the release button to be disabled
     */
    casper.then(function waitForReleaseButtonDisabled() {
        return this.waitWhileVisible('.actions .new-release', function checkHidden() {
            this.test.assert(true, 'Release button hidden');
        }, function fail() {
            this.capture('screenshot/MultipleDocumentsRelease/waitForReleaseButtonDisabled-error.png');
            this.test.assert(false, 'Release button not hidden');
        });
    });

    /**
     * Check document has been released
     */
    casper.then(function waitForReleaseIconDisplayed() {
        this.waitForSelector('#document-management-content table.dataTable tbody i.fa.fa-check', function documentIsReleased() {
            this.test.assertElementCount('#document-management-content table.dataTable tbody i.fa.fa-check', 2, 'Documents have been released');
        }, function fail() {
            this.capture('screenshot/MultipleDocumentsRelease/waitForReleaseIconDisplayed-error.png');
            this.test.assert(false, 'Documents have not been released');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
