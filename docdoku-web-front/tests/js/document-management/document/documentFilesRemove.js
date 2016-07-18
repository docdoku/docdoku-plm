/*global casper,urls,workspace,documents*/
casper.test.begin('Document Files Remove tests suite', 14, function documentUploadCadTestsSuite() {
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
            this.capture('screenshot/documentFilesRemove/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Wait for document to be displayed in list
     */

    casper.then(function waitForDocumentDisplayed() {
        return this.waitForSelector('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference', function documentIsDisplayed() {
            this.click('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentDisplayed-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Wait for document modal
     */

    casper.then(function waitForDocumentModal() {
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModal-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForDocumentModalTab() {
        return this.waitForSelector('.document-modal .upload-btn', function tabSelected() {
            this.test.assert(true, 'File upload tab opened');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModalTab-error.png');
            this.test.assert(false, 'Upload button can not be found');
        });
    });
    /**
     * Wait for the files to be displayed
     */
    casper.then(function setFileAndUpload() {

        return this.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('.document-modal .attachedFiles ul.file-list li').length === 1;
            });
        }, function then() {
            this.test.assert(true, 'File previously uploaded is present');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/setFileAndUpload-error.png');
            this.test.assert(false, 'File previously uploaded is not present');
        });
    });

    /**
     * Set the file to be deleted
     */
    casper.then(function removeFile() {
        this.click('ul.file-list li.file input.file-check');
        return this.waitForSelector('input.file-check:checked', function then() {
                this.test.assert(true,'File is set to be removed');
            }, function fail() {
                this.capture('screenshot/documentFilesRemove/setFileToRemove-error.png');
                this.test.assert(false,'File can not be set to be removed');
            });
    });

    /**
     * Save all
     */
    casper.then(function saveChanged() {
        this.click('#save-iteration');
        return this.waitWhileSelector('div.document-modal', function then () {
            this.test.assert(true,'Modal closed');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/closeModal-error.png');
            this.test.assert(false,'Could not close modal');
        });
    });

    /**
     * Wait for document modal
     */

    casper.then(function waitForDocumentModal() {
        this.click('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference');
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModal-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForDocumentModalTab() {
        return this.waitForSelector('.document-modal .upload-btn', function tabSelected() {
            this.test.assert(true, 'File upload tab opened');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModalTab-error.png');
            this.test.assert(false, 'Upload button can not be found');
        });
    });

    /**
     * Choose a file and upload
     */
    casper.then(function setFileAndUpload() {
        this.fill('.document-modal .upload-form', {
            'upload': 'res/document-upload.txt'
        }, false);

        return this.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('.document-modal .attachedFiles ul.file-list li').length === 1;
            });
        }, function then() {
            this.test.assert(true, 'File has been uploaded to document');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/setFileAndUpload-error.png');
            this.test.assert(false, 'Cannot upload the file');
        });

    });

    /**
     * Choose a second file and upload
     */
    casper.then(function setFileAndUpload() {
        this.fill('.document-modal .upload-form', {
            'upload': 'res/document-upload2.txt'
        }, false);

        return this.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('.document-modal .attachedFiles ul.file-list li').length === 2;
            });
        }, function then() {
            this.test.assert(true, 'Second File has been uploaded to document');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/setFileAndUpload-error.png');
            this.test.assert(false, 'Cannot upload the file');
        });

    });

    /**
     * Save all
     */
    casper.then(function saveChanged() {
        this.click('#save-iteration');
    });

    /**
     * Wait for the modal to close
     */
    casper.then(function waitForClosingModal() {
        return this.waitWhileSelector('div.document-modal', function then () {
            this.test.assert(true,'Modal closed');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/closeModal-error.png');
            this.test.assert(false,'Could not close modal');
        });
    });

    /**
     * Wait for document modal
     */

    casper.then(function waitForDocumentModal() {
        this.click('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference');
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModal-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForDocumentModalTab() {
        return this.waitForSelector('.document-modal .upload-btn', function tabSelected() {
            this.test.assert(true, 'File upload tab opened');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModalTab-error.png');
            this.test.assert(false, 'Upload button can not be found');
        });
    });

    /**
     * Assert that the 2 files are present in the modal
     */
    casper.then(function assertFilesUploaded() {
        return this.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('.document-modal .attachedFiles ul.file-list li').length === 2;
            });
        }, function then() {
            this.test.assert(true, 'Files uploaded are present in the modal');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/setFileAndUpload-error.png');
            this.test.assert(false, 'Cannot find uploaded files');
        });
    });

    /**
     * Check All the files to be deleted
     */
    casper.then(function checkAll() {
        this.click('a.toggle-checkAll');
        return this.waitFor(function checkAll() {
            return this.evaluate(function() {
                return document.querySelectorAll('input.file-check:checked').length === 2;
            });
        }, function then() {
            this.test.assert(true, 'All files have been checked');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/checkAllButton-error.png');
            this.test.assert(false, 'All files have not been checked');
        });
    });

    /**
     * Save all
     */
    casper.then(function saveChanged() {
        this.click('#save-iteration');
        return this.waitWhileSelector('div.document-modal', function then () {
            this.test.assert(true,'Modal closed');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/closeModal-error.png');
        });
    });

    /**
     * Wait for document modal
     */

    casper.then(function waitForDocumentModal() {
        this.click('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference');
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModal-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForDocumentModalTab() {
        return this.waitForSelector('.document-modal .upload-btn', function tabSelected() {
            this.test.assert(true, 'File upload tab opened');
        }, function fail() {
            this.capture('screenshot/documentFilesRemove/waitForDocumentModalTab-error.png');
            this.test.assert(false, 'Upload button can not be found');
        });
    });

    /**
     * Assert that no files are present
     */
    casper.then(function assertAllFilesRemoved() {
        return this.waitForSelector('.document-modal .attachedFiles ul.file-list', function checkNoFile() {
            this.test.assertDoesntExist('.document-modal .attachedFiles ul.file-list li','There should be no file present');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});

