/*global casper,urls,workspace,documents,$*/

casper.test.begin('Document creation from template tests suite', 2, function documentCreationFromTemplateTestsSuite() {

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
            this.capture('screenshot/documentCreationFromTemplate/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Open folder creation modal
     */

    casper.then(function clickOnDocumentCreationLink() {
        this.click('.actions .new-document');
    });

    /**
     * Wait for modal
     */

    casper.then(function waitForDocumentCreationModal() {
        return this.waitForSelector('.modal.document-modal.new-document .template-selector', function () {

            this.evaluate(function () {
                document.querySelector('.modal.document-modal.new-document .template-selector').selectedIndex = 1;
                $('.modal.document-modal.new-document .template-selector').change();
                $('.modal.document-modal.new-document input.reference').val('000');
                $('.modal.document-modal.new-document input.reference').focus();
                return true;
            });

            this.click('.modal.document-modal.new-document .btn.btn-primary');

        }, function fail() {
            this.capture('screenshot/documentCreationFromTemplate/waitForDocumentCreationModal-error.png');
            this.test.assert(false, 'New document modal can not be found');
        });
    });


    /**
     * Check if document has been created
     */

    casper.then(function checkForDocumentCreation() {
        return this.waitForSelector('#document-management-content table.dataTable tr[title="' + documents.template1.maskGenerated + '"] td.reference', function documentHasBeenCreated() {
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr[title="' + documents.template1.maskGenerated + '"] td.reference a', documents.template1.maskGenerated);
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr[title="' + documents.template1.maskGenerated + '"] td.type', documents.template1.type);
        }, function fail() {
            this.capture('screenshot/documentCreationFromTemplate/checkForDocumentCreation-error.png');
            this.test.assert(false, 'New document created can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
