/*global casper,urls,workspace,documents, workflows*/

casper.test.begin('Document creation with workflow tests suite', 7, function documentCreationWithWorkflowTestsSuite() {

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

    /***
     * Open the document modal
     */
    casper.then(function openCreatedDocument() {
        this.click('#document-management-content table.dataTable tr[title="' + documents.documentWithWorkflow.number + '"] td.reference');
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/openCreatedDocument-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });


    /**
     * Wait the modal tab
     */
    casper.then(function waitForDocumentModalTab() {
        this.waitForSelector('.document-modal .upload-btn', function tabSelected() {
            this.test.assert(true, 'File upload tab opened');
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/waitForDocumentModalTab-error.png');
            this.test.assert(false, 'Document modal tab can not be found');
        });
    });

    /**
     * Choose a file and upload
     */

    casper.then(function setFileAndUpload() {
        this.fill('.document-modal .upload-form', {
            'upload': 'res/document-upload.txt'
        }, false);

        casper.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('.document-modal .attachedFiles ul.file-list li').length === 1;
            });
        }, function then() {
            this.test.assert(true, 'File has been uploaded to document');
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/setFileAndUpload-error.png');
            this.test.assert(false, 'Cannot upload the file');
        });

    });

    /**
     * Checkin file
     */
    casper.then(function openIterationTab() {
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-iteration"]';
        this.waitForSelector(modalTab, function tabOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/openIterationTab-error.png');
            this.test.assert(false, 'Iteration tab can not be found');
        });
    });

    /**
     * Checkin file
     */
    casper.then(function checkinDocument() {
        var checkinButton = '#tab-iteration-iteration .action-checkin';
        this.waitForSelector(checkinButton, function buttonFound() {
            this.click(checkinButton);
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/checkinDocument-error.png');
            this.test.assert(false, 'Checkin button can not be found');
        });
    });

    /**
     * Checkin file
     */
    casper.then(function documentCheckedIn() {
        var checkoutButton = '#tab-iteration-iteration .action-checkout';
        this.waitForSelector(checkoutButton, function buttonFound() {
            this.test.assert(true, 'File has been checked in');
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/documentCheckedIn-error.png');
            this.test.assert(false, 'Checkout button can not be found');
        });
    });

    /**
     * Download file
     */
    casper.then(function openFileTab() {
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/openFileTab/downloadDocumentFile-error.png');
            this.test.assert(false, 'Modal tab can not be found');
        });
    });

    /**
     * Download file
     */
    casper.then(function downloadDocumentFile() {
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        this.waitForSelector(modalTab, function modalOpened() {
            var fileLink = '#iteration-files > div > ul > li > a[href="/api/files/'+workspace+'/documents/'+documents.documentWithWorkflow.number +'/A/1/document-upload.txt"]';
            this.test.assertSelectorExist(fileLink, 'A link should be present to download the document');
            this.click(fileLink);
        }, function fail() {
            this.capture('screenshot/documentCreationWithWorkflow/downloadDocumentFile-error.png');
            this.test.assert(false, 'Document file can not be found');
        });
    });


    casper.run(function allDone() {
        this.test.done();
    });
});
