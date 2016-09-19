/*global casper,urls,workspace,documents*/

casper.test.begin('Document obsolete tests suite', 3, function documentObsoleteTestsSuite() {
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
            this.capture('screenshot/documentObsolete/waitForFolderNavLink-error.png');
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
            this.capture('screenshot/documentObsolete/waitForDocumentTable-error.png');
            this.test.assert(false, 'Document can not be found');
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
            this.capture('screenshot/documentObsolete/waitForMarkAsObsoleteButton-error.png');
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
            this.capture('screenshot/documentObsolete/waitForMarkAsObsoletePrompt-error.png');
            this.test.assert(false, 'Mark as obsolete modal not found');
        });
    });

    /**
     * Wait for the Mark as obsolete button to be disabled
     */
    casper.then(function waitForMarkAsObsoleteButtonDisabled() {
        return this.waitForSelector('.actions .mark-as-obsolete', function checkHidden() {
            this.test.assertNotVisible('.actions .mark-as-obsolete', 'Mark as obsolete button hidden');
        }, function fail() {
            this.capture('screenshot/documentObsolete/waitForMarkAsObsoleteButtonDisabled-error.png');
            this.test.assert(false, 'Mark as obsolete button not hidden');
        });
    });

    /**
     * Check document has been marked as obsolete
     */
    casper.then(function waitForObsoleteIconDisplayed() {
        this.waitForSelector('#document-management-content table.dataTable tbody i.fa.fa-frown-o', function documentIsObsolete() {
            this.test.assertElementCount('#document-management-content table.dataTable tbody i.fa.fa-frown-o', 1, 'Document has been marked as obsolete');
        }, function fail() {
            this.capture('screenshot/documentObsolete/waitForObsoleteIconDisplayed-error.png');
            this.test.assert(false, 'Document has not been marked as obsolete');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
