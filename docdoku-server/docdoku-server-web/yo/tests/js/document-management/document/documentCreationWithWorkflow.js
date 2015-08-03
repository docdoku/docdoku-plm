/*global casper,urls,workspace,documents, workflows*/

casper.test.begin('Document creation with workflow tests suite', 3, function documentCreationWithWorkflowTestsSuite() {

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
            this.capture('screenshot/documentCreationWithWorkflow/waitForFolderNavLink-error.png');
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
        this.waitForSelector('.modal.document-modal.new-document', function () {
            this.sendKeys('.modal.document-modal.new-document input.reference', documents.documentWithWorkflow.number);
            this.click('.modal.document-modal.new-document .nav.nav-tabs > li:nth-child(2) > a');

            this.evaluate(function () {
                document.querySelector('.modal.document-modal.new-document .workflow-selector').selectedIndex = 1;
                $('.modal.document-modal.new-document .workflow-selector').change();
                return true;
            });

            this.test.assertElementCount('.modal.document-modal.new-document .role-mapping .roles-item', 1, 'There should be one role item in role mapping list');
            this.click('.modal.document-modal.new-document .btn.btn-primary');

        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/waitForDocumentCreationModal-error.png');
            this.test.assert(false, 'New document modal can not be found');
        });
    });

    /**
     * Check if document has been created
     */

    casper.then(function checkForDocumentCreation() {
        this.waitForSelector('#document-management-content table.dataTable tr[title="' + documents.documentWithWorkflow.number + '"] td.reference', function documentHasBeenCreated() {
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr[title="' + documents.documentWithWorkflow.number + '"] td.reference a', documents.documentWithWorkflow.number);
            this.test.assertSelectorHasText('#document-management-content table.dataTable tr[title="' + documents.documentWithWorkflow.number + '"] td.life-cycle-state', workflows.workflow1.activities.activity1.name);
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/checkForDocumentCreation-error.png');
            this.test.assert(false, 'New document created can not be found');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
